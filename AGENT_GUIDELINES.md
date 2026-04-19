# Agent 协同开发与代码规范指南

本文档用于记录和规范 AI Agent 在操作本项目代码时需要注意的强制性规则与易错点。Agent 在执行复杂重构或生成文件操作前，请务必参考本文档进行校验。

## 1. 文件编码规范 (关键)

**规则**：所有项目文件（尤其是前端项目库下的 `.vue`、`.js`、`.json` 文件，以及后端的 `.java`、配置文件等）**必须严格使用 UTF-8 编码**，且不得包含 BOM (Byte Order Mark)。

**背景与踩坑记录**：
- 在 2026 年 4 月的一次重构中，因底层 Windows PowerShell 默认输出机制的问题，导致 `src/views/login.vue` 文件被静默保存为了 `UTF-16LE` 编码格式。
- UTF-16 包含大量的 `\u0000` (NUL) 空字节，导致前端 `vue-loader` 无法正常解析 `<template>` 和 `<script>` 标签块，最终引发了 `[Vue warn]: Failed to mount component: template or render function not defined.` 错误，导致页面白屏组件失效。

**Agent 操作要求**：
1. **优先使用原生 API**：Agent 在新建文件、写入代码或修改代码时，请始终使用平台提供的代码读写工具（如 `create_file`、`replace_string_in_file`），这些接口能安全地保证 UTF-8 编码。
2. **禁用终端重定向覆写**：**坚决禁止**使用 PowerShell 命令（如 `echo "..." > file.vue`、`Out-File` 等重定向符号）去直接覆写或生成代码文件，除非显式且安全地指定了无 BOM 的 UTF-8 编码参数，以此杜绝次生隐患。

---
*后续开发中如果遇到其他通用踩坑点，请继续向此文档追加。*