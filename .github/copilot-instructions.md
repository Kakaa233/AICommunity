---
name: Wego Agent Guidelines
description: Strict guidelines, coding standards, and pitfall preventions for AI agents working on the Wego project.
---

# Agent 协同开发与代码规范指南 / Agent Development & Coding Guidelines

When working on this project, you MUST strictly adhere to the following rules:

## 1. File Encoding (CRITICAL)
- **Rule**: All project files (especially `.vue`, `.js`, `.json` in the frontend, and `.java`, config files in the backend) **MUST use UTF-8 encoding** without BOM.
- **Background**: In the past, PowerShell default output caused files (e.g., `login.vue`) to be saved as `UTF-16LE`, which resulted in NUL bytes and `vue-loader` compilation failures (`Failed to mount component...`).
- **Required Actions**:
  1. **Prefer Native API**: ALWAYS use platform-provided tools (e.g., `create_file`, `replace_string_in_file`) for file manipulation to safely guarantee UTF-8 encoding.
  2. **No Terminal Redirects**: **STRICTLY PROHIBITED** from using terminal redirects (like `echo "..." > file.vue` or `Out-File` in PowerShell) to create/overwrite files unless you explicitly format it as UTF-8 without BOM.

## 2. API Modification & Conventions
- **Rule**: You must accurately track and record any modified interfaces and their conventions.
- **Required Actions**:
  1. Record any changes made to APIs (request path, parameters, return structure).
  2. Synchronize these updates in code comments or corresponding API documentation to maintain alignment between frontend and backend.

## 3. Long-Running Task Defenses & State Synchronization
When executing complex full-stack refactoring or long-term tasks, you must guard against context overflow and local-vision bias:
- **Required Actions**:
  1. **Summarize Progress**: Break down large tasks. After finishing sub-tasks, record your core progress, technical decisions, and conventions to prevent forgetting them as context grows.
  2. **Global State Sync (CRITICAL)**: When modifying reused entities, global components, or API contracts, **DO NOT just edit the current file**. Use global search (`grep_search`) to find & update ALL references to prevent breakages.
  3. **Stay Focused**: Stick to the main objective. Do not detour to fix unrelated code smells unless the main task is fully complete.
  4. **Avoid Death Loops**: If a debug attempt fails twice using the same logic, forcibly break out, rethink the approach, or seek a different fallback solution.
  5. **Control Log Output**: Be cautious with terminal commands that produce massive logs (e.g., builds/installs) to prevent context truncation and AI hallucinations.
