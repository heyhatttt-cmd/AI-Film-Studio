# AI Film Studio Video Server (prototype)

FastAPI worker for short Wan 2.2 TI2V-5B clips. The Android app can submit a prompt, poll a job, and download the resulting MP4.

## Hardware choice

Initial target: RunPod Community Cloud RTX 4090 (24 GB VRAM). The published starting rate observed during planning was about $0.89/hour; actual price and availability vary. Do not leave a Pod running when it is not generating. Storage, network transfer, taxes, and model download time may add cost.

Wan-AI publishes the TI2V-5B Diffusers checkpoint under Apache-2.0 and documents 720p text-to-video and image-to-video support. This first server iteration deliberately starts with text-to-video only. 24 GB is a tight target: validate model loading and generation on the selected image before accepting paid jobs. If it runs out of memory, stop and resize/optimize rather than silently switching to a more expensive GPU.

## Run on a CUDA GPU

1. Create a GPU Pod with a CUDA 12.4 compatible PyTorch environment and persistent volume mounted at /data.
2. Build this directory as a Docker image, or install requirements.txt in a compatible CUDA environment.
3. Set VIDEO_API_TOKEN to a long random secret. Keep it out of Git and out of the APK source.
4. Set VIDEO_OUTPUT_DIR=/data/videos so generated MP4s survive container restarts if /data is a persistent volume.
5. Start: uvicorn app:app --host 0.0.0.0 --port 8000.
6. Put the service behind HTTPS before connecting from the public internet. Restrict inbound traffic; do not expose an unauthenticated GPU endpoint.

## API

- GET /health — service/model/GPU status (no secret).
- POST /v1/videos — JSON with prompt, num_frames, width, height and steps; Bearer token required. Returns HTTP 202 with a job id.
- GET /v1/videos/{id} — job state and video_url, Bearer token required.
- GET /v1/videos/{id}/file — MP4 download, Bearer token required.

The prototype keeps job state in memory and serializes generation to one job at a time. Restarting the process loses job metadata; use a database/queue before multi-user production. It is not yet a hardened public service.

## Local smoke test

curl http://localhost:8000/health

curl -X POST http://localhost:8000/v1/videos -H 'Authorization: Bearer YOUR_TOKEN' -H 'Content-Type: application/json' -d '{"prompt":"A cinematic wide shot of a small white puppy walking through a rainy quiet street at night, realistic film lighting","num_frames":49,"width":832,"height":480,"steps":20}'

This creates a short test clip, not a full film. Scene stitching, audio, lip sync, persistent queue, user accounts, and automatic GPU start/stop are later milestones.
