---
title: AI Film Studio Video Generator
emoji: 🎬
colorFrom: purple
colorTo: indigo
sdk: gradio
sdk_version: 5.23.1
app_file: app.py
pinned: false
---

# AI Film Studio — Wan 2.2

Short text-to-video demo using the Wan 2.2 TI2V-5B Diffusers checkpoint.

## Deploy

Create a Hugging Face Space with the **Gradio** SDK and ZeroGPU access. The GitHub Actions workflow can publish this folder when these repository Actions secrets are configured:

- `HF_TOKEN`: a Hugging Face write token.
- `HF_SPACE_REPO`: the exact Space id, for example `username/ai-film-studio-video`.

Do not put tokens in source files. ZeroGPU has usage limits and is not an always-on free GPU server. Generation may take several minutes and may fail if the model cannot fit the currently assigned GPU.

## Model

`Wan-AI/Wan2.2-TI2V-5B-Diffusers`

This Space creates short preview clips. It is not a full multi-scene film editor yet.
