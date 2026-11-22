import os
import secrets
import threading
import uuid
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

import torch
from diffusers import DiffusionPipeline
from diffusers.utils import export_to_video
from fastapi import BackgroundTasks, Depends, FastAPI, Header, HTTPException
from fastapi.responses import FileResponse
from pydantic import BaseModel, Field

MODEL_ID = os.getenv("MODEL_ID", "Wan-AI/Wan2.2-TI2V-5B-Diffusers")
API_TOKEN = os.getenv("VIDEO_API_TOKEN", "")
OUTPUT_DIR = Path(os.getenv("VIDEO_OUTPUT_DIR", "/data/videos"))
OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

app = FastAPI(title="AI Film Studio Video API", version="0.1.0")
pipeline: Any = None
jobs: dict[str, dict[str, Any]] = {}
model_lock = threading.Lock()
generation_lock = threading.Lock()


class GenerateRequest(BaseModel):
    prompt: str = Field(min_length=8, max_length=2000)
    # 49 frames = about 2 seconds at 24 fps; Wan temporal lengths use 4k+1.
    num_frames: int = Field(default=49, ge=17, le=81)
    width: int = Field(default=832, ge=256, le=1280)
    height: int = Field(default=480, ge=256, le=720)
    steps: int = Field(default=20, ge=10, le=40)
    seed: int | None = Field(default=None, ge=0, le=2**32 - 1)


def require_token(authorization: str | None = Header(default=None)) -> None:
    if not API_TOKEN:
        raise HTTPException(status_code=503, detail="VIDEO_API_TOKEN is not configured on the server.")
    expected = f"Bearer {API_TOKEN}"
    if authorization is None or not secrets.compare_digest(authorization, expected):
        raise HTTPException(status_code=401, detail="Invalid API token.")


@app.on_event("startup")
def load_video_model() -> None:
    global pipeline
    if not torch.cuda.is_available():
        return
    with model_lock:
        pipeline = DiffusionPipeline.from_pretrained(
            MODEL_ID,
            torch_dtype=torch.bfloat16,
            device_map="cuda",
        )


@app.get("/health")
def health() -> dict[str, Any]:
    return {
        "service": "AI Film Studio Video API",
        "status": "ready" if pipeline is not None else "waiting_for_gpu_or_model",
        "gpu_available": torch.cuda.is_available(),
        "model_loaded": pipeline is not None,
        "model_id": MODEL_ID,
    }


@app.post("/v1/videos", dependencies=[Depends(require_token)], status_code=202)
def create_video(request: GenerateRequest, background_tasks: BackgroundTasks) -> dict[str, str]:
    if pipeline is None:
        raise HTTPException(status_code=503, detail="Video model is not loaded. Check GPU and server logs.")
    job_id = uuid.uuid4().hex
    jobs[job_id] = {
        "id": job_id,
        "status": "queued",
        "created_at": datetime.now(timezone.utc).isoformat(),
        "prompt": request.prompt,
        "video_file": None,
        "error": None,
    }
    background_tasks.add_task(generate_video, job_id, request)
    return {"id": job_id, "status": "queued"}


def generate_video(job_id: str, request: GenerateRequest) -> None:
    job = jobs[job_id]
    try:
        with generation_lock:
            job["status"] = "running"
            generator = None
            if request.seed is not None:
                generator = torch.Generator(device="cuda").manual_seed(request.seed)
            result = pipeline(
                prompt=request.prompt,
                height=request.height,
                width=request.width,
                num_frames=request.num_frames,
                num_inference_steps=request.steps,
                generator=generator,
            )
            frames = result.frames[0]
            output_path = OUTPUT_DIR / f"{job_id}.mp4"
            export_to_video(frames, str(output_path), fps=24)
            job["video_file"] = str(output_path)
            job["status"] = "completed"
    except Exception as exc:
        job["status"] = "failed"
        job["error"] = f"{type(exc).__name__}: {exc}"[:1000]


@app.get("/v1/videos/{job_id}", dependencies=[Depends(require_token)])
def get_video_status(job_id: str) -> dict[str, Any]:
    job = jobs.get(job_id)
    if job is None:
        raise HTTPException(status_code=404, detail="Video job not found.")
    response = {key: value for key, value in job.items() if key != "video_file"}
    if job["status"] == "completed":
        response["video_url"] = f"/v1/videos/{job_id}/file"
    return response


@app.get("/v1/videos/{job_id}/file", dependencies=[Depends(require_token)])
def download_video(job_id: str) -> FileResponse:
    job = jobs.get(job_id)
    if job is None or job.get("status") != "completed" or not job.get("video_file"):
        raise HTTPException(status_code=404, detail="Completed video not found.")
    path = Path(job["video_file"])
    if not path.is_file():
        raise HTTPException(status_code=404, detail="Video file is missing.")
    return FileResponse(path, media_type="video/mp4", filename=f"{job_id}.mp4")
