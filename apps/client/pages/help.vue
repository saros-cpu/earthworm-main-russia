<template>
  <div class="w-full py-6">
    <section
      class="mb-5 rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900"
    >
      <p class="text-sm font-bold text-emerald-600 dark:text-emerald-300">{{ $t("pages.help") }}</p>
      <h1 class="mt-1 text-3xl font-black text-slate-950 dark:text-white">帮助文档</h1>
      <p class="mt-2 text-sm text-slate-500 dark:text-slate-400">
        Earthworm 俄语闯关学习平台使用说明：按从上到下的顺序读一遍，基本就能上手。
      </p>
    </section>

    <section class="mb-5 grid gap-4 md:grid-cols-2">
      <article
        v-for="block in BLOCKS"
        :key="block.title"
        class="rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900"
      >
        <div class="mb-3 flex items-center gap-2">
          <UIcon
            :name="block.icon"
            class="h-5 w-5 text-emerald-600 dark:text-emerald-300"
          />
          <h2 class="text-lg font-black text-slate-950 dark:text-white">{{ block.title }}</h2>
        </div>
        <ul class="space-y-2 text-sm leading-7 text-slate-600 dark:text-slate-300">
          <li
            v-for="(line, i) in block.lines"
            :key="i"
            class="flex gap-2"
          >
            <span class="mt-2 inline-block h-1.5 w-1.5 shrink-0 rounded-full bg-emerald-500"></span>
            <span v-html="line"></span>
          </li>
        </ul>
      </article>
    </section>

    <section class="mb-5 grid gap-4 md:grid-cols-2">
      <article
        v-for="block in MODE_BLOCKS"
        :key="block.title"
        class="rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900"
      >
        <div class="mb-3 flex items-center gap-2">
          <UIcon
            :name="block.icon"
            class="h-5 w-5 text-emerald-600 dark:text-emerald-300"
          />
          <h2 class="text-lg font-black text-slate-950 dark:text-white">{{ block.title }}</h2>
        </div>
        <ul class="space-y-2 text-sm leading-7 text-slate-600 dark:text-slate-300">
          <li
            v-for="(line, i) in block.lines"
            :key="i"
            class="flex gap-2"
          >
            <span class="mt-2 inline-block h-1.5 w-1.5 shrink-0 rounded-full bg-emerald-500"></span>
            <span v-html="line"></span>
          </li>
        </ul>
      </article>
    </section>

    <section
      class="mb-5 rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900"
    >
      <h2 class="mb-3 text-lg font-black text-slate-950 dark:text-white">快捷键速查</h2>
      <div class="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
        <div
          v-for="kb in KEYBINDS"
          :key="kb.action"
          class="flex items-center justify-between rounded-md border border-slate-100 px-3 py-2 dark:border-slate-800"
        >
          <span class="text-sm text-slate-700 dark:text-slate-300">{{ kb.action }}</span>
          <kbd
            class="rounded border border-slate-300 bg-slate-50 px-2 py-0.5 text-xs font-bold text-slate-700 dark:border-slate-700 dark:bg-slate-950 dark:text-slate-200"
          >
            {{ kb.key }}
          </kbd>
        </div>
      </div>
    </section>

    <section
      class="rounded-md border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900"
    >
      <h2 class="mb-2 text-lg font-black text-slate-950 dark:text-white">遇到问题？</h2>
      <p class="text-sm leading-7 text-slate-600 dark:text-slate-300">
        点击侧边菜单的「建议反馈」直接在网页留言，开发者会定期查看。
      </p>
    </section>
  </div>
</template>

<script setup lang="ts">
const BLOCKS = [
  {
    icon: "i-ph-rocket-launch",
    title: "开始练习",
    lines: [
      "首页点「先玩一课」或导航栏「我的课程包」浏览全部系列：基础入门、走遍俄罗斯（1–8 册）、语法学习、考试备考（高考/考研/大学俄语/CATTI）、生活口语、行业专业（石油/医学/法律/IT 等）、人文知识、影音娱乐等 <b>80+ 课程包</b>。",
      "选一节课点「开始闯关」进入 <b>中译俄</b> 模式：看到中文提示，用键盘输入对应的俄语句子。",
      "西里尔字母可以用拼音键映射输入（如 'a' → а, 'o' → о, 'f' → ф），无需切换输入法。",
    ],
  },
  {
    icon: "i-ph-book-open-text",
    title: "闯关界面",
    lines: [
      "看到紫色下划线亮起，直接输入对应俄语。回车提交答案，错词会标红，按 <kbd>Space</kbd> 跳到首个错词并清空输入。",
      "答对后自动播放该句俄语朗读（浏览器 TTS），可选装俄语语音包获得更自然的发音。",
      "左上角工具栏可切换闯关模式、查看课程目录、打开关联视频、调整设置或重置进度。",
      "闯关结束后展示评分（S/A/B/C/D）、连击数、准确率、总分，支持分享打卡图片。",
    ],
  },
  {
    icon: "i-ph-game-controller",
    title: "7 种闯关模式",
    lines: [
      "<b>中译俄</b>：看中文拼写俄语，经典模式。<br>",
      "<b>俄语听写</b>：听俄语音频，写下听到的内容，训练听力。<br>",
      "<b>连词成句</b>：打乱单词顺序，拖拽拼回正确句子。<br>",
      "<b>口语测评</b>：对着麦克风说俄语，浏览器语音评分（0–100）。<br>",
      "<b>听力课程</b>：播放视频/音频，边看边学，可切换字幕显示。<br>",
      "<b>音乐课程</b>：俄语歌曲逐句播放，跟随歌词学习。<br>",
      "<b>混合模式</b>：随机轮换中译俄、听写、连词成句，综合训练。",
    ],
  },
  {
    icon: "i-ph-graduation-cap",
    title: "复习与单词本",
    lines: [
      "练习中答错的句子自动进入 <b>错题本</b>，按错误次数排序，集中突破薄弱环节。",
      "闯关后可标记「已掌握」，进入 <b>复习本</b> 集中浏览。",
      "<b>复习</b> 基于 SuperMemo SM-2 间隔重复算法，到期在导航栏显示红色数字提醒。",
      "生词可加入 <b>生词本</b>，支持模糊搜索和 TTS 发音。",
      "<b>单词库</b> 浏览所有已收藏单词的详情（词性、音标、例句、学习程度 1–5 级）。",
    ],
  },
  {
    icon: "i-ph-video",
    title: "媒体课程",
    lines: [
      "<b>视频课程</b>：支持本地 .avi/.mp4 和 YouTube 链接，自动 ffmpeg 转码流式播放。",
      "<b>音频课程</b>：.mp3/.wma/.m4a 等格式，同步字幕逐行高亮。",
      "<b>歌曲学习</b>（ru-songs 包）：带时间轴 LRC 歌词，逐句跟唱学习。",
      "每个媒体课程支持 <b>播放器</b>、<b>播放+答题</b>、<b>音频+字幕</b> 三种视图。",
    ],
  },
  {
    icon: "i-ph-users-three",
    title: "学习小组与对战",
    lines: [
      "创建或加入 <b>学习小组</b>，通过邀请码拉人一起学习。",
      "<b>好友对战</b>：创建房间、选课程包，与好友实时 PK 俄语答题（2 秒轮询同步）。",
      "个人 <b>学习统计</b> 展示总练习量、准确率、连续学习天数、84 天学习热力图。",
    ],
  },
];

const MODE_BLOCKS = [
  {
    icon: "i-ph-books",
    title: "课程包分类",
    lines: [
      "覆盖 <b>80+ 课程包</b>，按系列筛选：",
      "• <b>基础入门</b>：字母发音、基础词汇、日常问候",
      "• <b>走遍俄罗斯</b>：经典教材 east-uni 1–8 册 + 讲座课",
      "• <b>语法学习</b>：名词/形容词/动词变格、时态、前置词等专项",
      "• <b>考试备考</b>：高考俄语、考研、大学俄语、CATTI 翻译（二/三级）",
      "• <b>生活口语</b>：日常/餐厅/购物/交通/酒店/医院/电话/工作/应急",
      "• <b>行业专业</b>：石油工程、医护、法律、物流、贸易、旅游、IT",
      "• <b>人文知识</b>：文化、历史、地理、文学、节日、谚语、新闻",
    ],
  },
  {
    icon: "i-ph-gear",
    title: "个性化设置",
    lines: [
      "右上角头像 → 设置：选择默认闯关模式、自定义快捷键（发音/答案/上题/跳过/标记掌握/暂停）。",
      "声音设置：键盘打字音、自动朗读、发音引擎选择。",
      "答题设置：显示词宽、空格提交、自动下一题、连续 3 次错误自动显示答案。",
      "TORFL 等级（A1–C2）按 ТРКИ 词频划分，可在课程包列表筛选。",
    ],
  },
];

const KEYBINDS = [
  { action: "开始 / 下一题", key: "Enter" },
  { action: "跳到下个单词 / 错词", key: "Space" },
  { action: "上一个错词", key: "Backspace" },
  { action: "播放朗读", key: "Ctrl + '" },
  { action: "显示答案", key: "Ctrl + ;" },
  { action: "标记已掌握", key: "Ctrl + M" },
  { action: "暂停 / 继续", key: "Ctrl + P" },
  { action: "上一句", key: "Ctrl + [" },
  { action: "跳过本句", key: "Ctrl + ]" },
];
</script>
