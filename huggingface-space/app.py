import gc
import os
import tempfile
import threading

import gradio as gr
import spaces
import torch
from diffusers import DiffusionPipeline
from diffusers.utils import export_to_video

MODEL_ID = os.getenv("MODEL_ID", "Wan-AI/Wan2.2-TI2V-5B-Diffusers")
_pipeline = None
_pipeline_lock = threading.Lock()


def load_pipeline():
    global _pipeline
    if _pipeline is not None:
        return _pipeline

    with _pipeline_lock:
        if _pipeline is None:
            if not torch.cuda.is_available():
                raise RuntimeError("ZeroGPU is not active. Open this Space with its GPU runtime.")
            _pipeline = DiffusionPipeline.from_pretrained(
                MODEL_ID,
                torch_dtype=torch.bfloat16,
            )
            # CPU offload reduces peak VRAM demand on shared GPU hardware.
            _pipeline.enable_model_cpu_offload()
    return _pipeline


@spaces.GPU(duration=300)
def generate_video(prompt, steps, seed, progress=gr.Progress()):
    prompt = (prompt or "").strip()
    if len(prompt) < 8:
        raise gr.Error("Lütfen en az 8 karakterlik bir sahne açıklaması yazın.")

    try:
        progress(0.05, desc="Video modeli hazırlanıyor")
        pipe = load_pipeline()
        generator = torch.Generator(device="cuda").manual_seed(int(seed))
        progress(0.15, desc="Sahne oluşturuluyor")
        result = pipe(
            prompt=prompt,
            height=480,
            width=832,
            num_frames=49,
            num_inference_steps=int(steps),
            guidance_scale=5.0,
            generator=generator,
        )
        frames = result.frames[0]
        output_path = tempfile.NamedTemporaryFile(suffix=".mp4", delete=False).name
        export_to_video(frames, output_path, fps=24)
        progress(1.0, desc="Video hazır")
        return output_path
    except gr.Error:
        raise
    except Exception as exc:
        gc.collect()
        if torch.cuda.is_available():
            torch.cuda.empty_cache()
        raise gr.Error(
            f"Video üretilemedi: {type(exc).__name__}: {exc}. "
            "GPU belleği veya ZeroGPU süresi yetersiz olabilir."
        ) from exc


with gr.Blocks(title="AI Film Studio — Video Generator") as demo:
    gr.Markdown(
        "# 🎬 AI Film Studio\n"
        "Wan 2.2 ile kısa bir video sahnesi oluştur. Bu demo 49 karelik, "
        "yaklaşık 2 saniyelik bir MP4 önizlemesi üretir."
    )
    prompt = gr.Textbox(
        label="Sahne açıklaması",
        placeholder="Örn. Sinematik gerçekçilikte, yağmurlu bir sokakta yürüyen küçük beyaz bir köpek...",
        lines=4,
        max_lines=8,
    )
    with gr.Row():
        steps = gr.Slider(10, 30, value=20, step=1, label="Üretim adımı")
        seed = gr.Number(value=42, precision=0, label="Seed")
    generate = gr.Button("🎥 Video oluştur", variant="primary")
    output = gr.Video(label="Oluşturulan MP4", format="mp4")
    generate.click(
        fn=generate_video,
        inputs=[prompt, steps, seed],
        outputs=output,
        api_name="generate_video",
    )
    gr.Markdown(
        "**Not:** ZeroGPU ücretsiz kullanım kotası ve donanım kapasitesiyle sınırlıdır. "
        "Bu demo sürekli açık veya sınırsız ücretsiz video üretimi vaat etmez."
    )

if __name__ == "__main__":
    demo.queue(max_size=1).launch()
