# 项目约定（重要！请先阅读）

## Nuxt 3 组件自动导入规则

`components/` 下的组件按**目录路径**自动注册为 PascalCase 名称：

```
components/media/VideoPlayer.vue    → <MediaVideoPlayer>
components/media/VideoQuiz.vue      → <MediaVideoQuiz>
components/media/AudioLrcPlayer.vue → <MediaAudioLrcPlayer>
```

**切勿**去掉目录前缀。目录名 `media` 会作为组件名的一部分，这是 Nuxt 3 的默认行为。

## 媒体课程包（视频/音频）

- seed 数据在 `backend/src/main/resources/customs/*.json`，`video` 字段指向本地 `.avi/.mp4/.mp3` 或 YouTube URL
- 本地视频通过 `/api/backend/media/stream?path=...` 流式播放，后端自动 ffmpeg 转码并缓存到 `runtime/transcode-cache/`
- `lyrics` 仅 `ru-songs` 包需要，其他课程包从 `statements` 回退生成（用户已知此限制）
- 媒体组件（VideoPlayer / VideoQuiz / AudioLrcPlayer）接受 `src`、`type`、`title` 等 props，由 detail 页面传入
