import json, os, re

CUSTOMS = r"D:\earthworm-main\backend\src\main\resources\customs"
os.makedirs(CUSTOMS, exist_ok=True)

# (id, title, description, cover_url, courses)
# course: (cid, order, title, desc, [(ru, zh, ph)])
PACKS = []

def P(id, title, desc, cover, *courses):
    PACKS.append({
        "id": id, "title": title, "description": desc, "cover": cover, "isFree": True,
        "courses": [{"id": c[0], "order": c[1], "title": c[2], "description": c[3],
                      "statements": [{"english": s[0], "chinese": s[1], "soundmark": s[2]} for s in c[4]]} for c in courses]
    })

# ============ BASIC series (8 packs) ============

P("ru-basic-phonetics", "俄语发音与语音规则", "系统学习俄语元音、辅音的发音方法以及重音、语调等语音规则。",
  "https://images.unsplash.com/photo-1536064479547-7ee40b74b0f4?q=80&w=1200&auto=format&fit=crop",
  ("ruph-01", 1, "俄语元音发音", "学习6个俄语基本元音的正确发音。", [
    ("А а — [а]", "字母Аа读作[a]，口张大", "a"),
    ("О о — [о]", "字母Оо读作[o]，双唇收圆", "o"),
    ("У у — [у]", "字母Уу读作[u]，双唇前伸", "oo"),
    ("Э э — [э]", "字母Ээ读作[e]，舌尖抵下齿", "eh"),
    ("Ы ы — [ы]", "字母Ыы读作[ɨ]，舌尖后缩", "y"),
    ("И и — [и]", "字母Ии读作[i]，类似汉语'一'", "ee"),
  ]),
  ("ruph-02", 2, "俄语辅音发音（清浊对应）", "学习成对清浊辅音的发音区别。", [
    ("Б — б [b]", "浊辅音б，声带振动", "b"),
    ("П — п [p]", "清辅音п，声带不振动", "p"),
    ("В — в [v]", "浊辅音в，上齿咬下唇", "v"),
    ("Ф — ф [f]", "清辅音ф，上齿咬下唇", "f"),
    ("Г — г [ɡ]", "浊辅音г，舌根抵软腭", "g"),
    ("К — к [k]", "清辅音к，舌根抵软腭", "k"),
    ("Д — д [d]", "浊辅音д，舌尖抵上齿", "d"),
    ("Т — т [t]", "清辅音т，舌尖抵上齿", "t"),
    ("З — з [z]", "浊辅音з，舌尖抵下齿", "z"),
    ("С — с [s]", "清辅音с，舌尖抵下齿", "s"),
  ]),
  ("ruph-03", 3, "重音与语调", "学习俄语词重音和基本语调类型。", [
    ("молоко́", "牛奶（重音在最后音节）", "ma-la-KO"),
    ("хо́рошо", "好（重音在第一音节）", "KHO-ra-sha"),
    ("говори́ть", "说（重音在最后音节）", "ga-va-REET"),
    ("у́ченик", "学生（重音在第一音节）", "OO-che-nik"),
    ("Она́ до́ма?", "她在家里吗？（疑问语调）", "a-NA DO-ma"),
    ("Он говори́т.", "他说。（陈述语调）", "on ga-va-REET"),
    ("Како́й кра́сный!", "多红啊！（感叹语调）", "ka-KOY KRAS-ny"),
  ]))

P("ru-basic-greetings", "俄语问候与日常交际", "掌握俄语正式与非正式场合的问候、告别、致谢等日常交际用语。",
  "https://images.unsplash.com/photo-1529156069898-49953e39b3ac?q=80&w=1200&auto=format&fit=crop",
  ("rugr-01", 1, "正式问候", "正式场合问候用语。", [
    ("Здра́вствуйте!", "您好！", "ZDRA-stvooy-tye"),
    ("До́брое у́тро!", "早上好！", "DOB-ra-ye OOT-ra"),
    ("До́брый день!", "下午好！", "DOB-ry den"),
    ("До́брый ве́чер!", "晚上好！", "DOB-ry VYE-cher"),
    ("Как ва́ши дела́?", "您最近怎么样？", "kak VA-shee de-LA"),
    ("О́чень прия́тно.", "非常高兴见到您。", "O-chen pree-YAT-na"),
  ]),
  ("rugr-02", 2, "非正式问候与告别", "与朋友熟人的日常问候和告别。", [
    ("Приве́т!", "嗨！你好！", "pree-VYET"),
    ("Здоро́во!", "嘿！（非常随意）", "zda-RO-va"),
    ("Как дела́?", "最近咋样？", "kak de-LA"),
    ("Норма́льно.", "还行。", "nar-MAL-na"),
    ("Отли́чно!", "棒极了！", "at-LEECH-na"),
    ("Пока́!", "拜拜！", "pa-KA"),
    ("До за́втра!", "明天见！", "da ZAV-tra"),
    ("До встре́чи!", "回头见！", "da VSTRYE-chee"),
    ("Споко́йной но́чи!", "晚安！", "spa-KOY-noy NO-chee"),
  ]))

P("ru-basic-numbers", "俄语数字与计量", "系统学习俄语基数词、序数词、钟点日期等数字表达。",
  "https://images.unsplash.com/photo-1551288049-bebda4e38f71?q=80&w=1200&auto=format&fit=crop",
  ("runu-01", 1, "基数词1–100", "学习俄语基数词1-100。", [
    ("оди́н", "一", "a-DEEN"), ("два", "二", "dva"), ("три", "三", "tree"),
    ("четы́ре", "四", "che-TY-re"), ("пять", "五", "pyat"), ("де́сять", "十", "DYE-syat"),
    ("два́дцать", "二十", "DVAD-tsat"), ("три́дцать", "三十", "TREED-tsat"),
    ("со́рок", "四十", "SO-rak"), ("пятьдеся́т", "五十", "pyat-de-SYAT"), ("сто", "一百", "sto"),
  ]),
  ("runu-02", 2, "序数词", "俄语序数词构成和用法。", [
    ("пе́рвый", "第一", "PYER-vy"), ("второ́й", "第二", "vta-ROY"),
    ("тре́тий", "第三", "TRE-tee"), ("четвёртый", "第四", "chet-VYOR-ty"),
    ("пя́тый", "第五", "PYA-ty"), ("деся́тый", "第十", "de-SYA-ty"),
  ]))

P("ru-basic-daily", "日常生活词汇", "涵盖饮食、家居等日常生活中最常用的俄语词汇和表达。",
  "https://images.unsplash.com/photo-1544027993-37dbfe43562a?q=80&w=1200&auto=format&fit=crop",
  ("ruda-01", 1, "饮食", "常见食物和饮品的俄语名称。", [
    ("хлеб", "面包", "khlyep"), ("молоко́", "牛奶", "ma-la-KO"), ("ма́сло", "黄油/油", "MAS-la"),
    ("мя́со", "肉", "MYA-sa"), ("ры́ба", "鱼", "RY-ba"), ("я́йцо", "鸡蛋", "yay-TSO"),
    ("рис", "米饭", "rees"), ("суп", "汤", "soop"), ("чай", "茶", "chay"),
    ("ко́фе", "咖啡", "KO-fye"), ("са́хар", "糖", "SA-khar"), ("соль", "盐", "sol"),
  ]),
  ("ruda-02", 2, "家居物品", "家中常见物品的俄语名称。", [
    ("дом", "房子", "dom"), ("ко́мната", "房间", "KOM-na-ta"), ("ку́хня", "厨房", "KOOKH-nya"),
    ("спа́льня", "卧室", "SPAL-nya"), ("ва́нная", "浴室", "VAN-na-ya"), ("крова́ть", "床", "kra-VAT"),
    ("шкаф", "柜子", "shkaf"), ("стол", "桌子", "stol"), ("стул", "椅子", "stool"), ("ла́мпа", "灯", "LAM-pa"),
  ]))

P("ru-basic-family", "家庭与人称", "学习家庭成员称呼、人称代词和物主代词的俄语表达。",
  "https://images.unsplash.com/photo-1511895426328-dc8714191300?q=80&w=1200&auto=format&fit=crop",
  ("rufa-01", 1, "家庭成员", "俄语中家庭成员的称谓。", [
    ("ма́ма", "妈妈", "MA-ma"), ("па́па", "爸爸", "PA-pa"), ("де́душка", "爷爷", "DYE-doosh-ka"),
    ("ба́бушка", "奶奶", "BA-boosh-ka"), ("брат", "兄弟", "brat"), ("сестра́", "姐妹", "ses-TRA"),
    ("сын", "儿子", "syn"), ("дочь", "女儿", "doch"), ("муж", "丈夫", "moozh"), ("жена́", "妻子", "zhe-NA"),
    ("тётя", "阿姨", "TYO-tya"), ("дя́дя", "叔叔", "DYA-dya"),
  ]),
  ("rufa-02", 2, "人称代词", "俄语人称代词主格形式。", [
    ("я", "我", "ya"), ("ты", "你", "ty"), ("он", "他", "on"), ("она́", "她", "a-NA"),
    ("оно́", "它", "a-NO"), ("мы", "我们", "my"), ("вы", "您/你们", "vy"), ("они́", "他们", "a-NEE"),
  ]))

P("ru-basic-colors", "俄语颜色与形状描述", "学习常见颜色、形状和尺寸的俄语形容词及用法。",
  "https://images.unsplash.com/photo-1513364776144-60967b0f800f?q=80&w=1200&auto=format&fit=crop",
  ("ruco-01", 1, "基本颜色", "俄语基本颜色名称。", [
    ("кра́сный", "红色的", "KRAS-ny"), ("си́ний", "蓝色的", "SEE-ny"), ("зелёный", "绿色的", "zee-LYO-ny"),
    ("жёлтый", "黄色的", "ZHYOL-ty"), ("бе́лый", "白色的", "BYE-ly"), ("чёрный", "黑色的", "CHYOR-ny"),
    ("кори́чневый", "棕色的", "ka-REECH-ne-vy"), ("се́рый", "灰色的", "SYE-ry"),
    ("фиоле́товый", "紫色的", "fee-a-LYE-ta-vy"), ("ро́зовый", "粉色的", "RO-za-vy"),
    ("ора́нжевый", "橙色的", "a-RAN-zhe-vy"), ("голубо́й", "天蓝色的", "ga-loo-BOY"),
  ]),
  ("ruco-02", 2, "形状与尺寸", "形状和尺寸表达。", [
    ("круг", "圆形", "krook"), ("квадра́т", "正方形", "kva-DRAT"), ("треуго́льник", "三角形", "tre-oo-GOL-neek"),
    ("большо́й", "大的", "bal-SHOY"), ("ма́ленький", "小的", "MA-lyen-kiy"),
    ("дли́нный", "长的", "DLEEN-ny"), ("коро́ткий", "短的", "ka-ROT-kiy"),
    ("высо́кий", "高的", "vy-SO-kiy"), ("ни́зкий", "矮的", "NEEZ-kiy"),
  ]))

P("ru-basic-calendar", "日期与天气表达", "学习星期、月份、日期的表达以及描述天气的常用俄语。",
  "https://images.unsplash.com/photo-1506784365847-bbad939a9335?q=80&w=1200&auto=format&fit=crop",
  ("ruca-01", 1, "星期与月份", "俄语星期和月份名称。", [
    ("понеде́льник", "星期一", "pa-nee-DYEL-neek"), ("вто́рник", "星期二", "FTOR-neek"),
    ("среда́", "星期三", "sre-DA"), ("четве́рг", "星期四", "chet-VYERK"),
    ("пя́тница", "星期五", "PYAT-nee-tsa"), ("суббо́та", "星期六", "soo-BO-ta"),
    ("воскресе́нье", "星期日", "va-skree-SYE-nye"), ("янва́рь", "一月", "yan-VAR"),
    ("февра́ль", "二月", "fev-RAL"), ("март", "三月", "mart"),
    ("апре́ль", "四月", "ap-REL"), ("май", "五月", "may"),
  ]),
  ("ruca-02", 2, "天气表达", "描述天气的常用词汇句型。", [
    ("Сего́дня хо́лодно.", "今天冷。", "se-VOD-nya KHO-lad-na"),
    ("Сего́дня те́пло.", "今天暖和。", "se-VOD-nya TYEP-la"),
    ("Идёт дождь.", "下雨了。", "ee-DYOT dozhd"),
    ("Идёт снег.", "下雪了。", "ee-DYOT snyek"),
    ("Со́лнечно.", "晴天。", "SOL-nech-na"),
    ("О́блачно.", "多云。", "OB-lach-na"),
    ("Ве́тер.", "刮风。", "VYE-tyer"),
  ]))

P("ru-basic-school", "学习与课堂用语", "学习用品、课堂指令和学校相关词汇。",
  "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?q=80&w=1200&auto=format&fit=crop",
  ("rusc-01", 1, "学习用品", "教室和学习用品俄语名称。", [
    ("кни́га", "书", "KNEE-ga"), ("тетра́дь", "笔记本", "tet-RAD"), ("ру́чка", "笔", "ROOCH-ka"),
    ("каранда́ш", "铅笔", "ka-ran-DASH"), ("слова́рь", "词典", "sla-VAR"),
    ("уче́бник", "教科书", "oo-CHEB-neek"), ("доска́", "黑板", "das-KA"),
  ]),
  ("rusc-02", 2, "课堂指令", "课堂常用俄语指令和提问。", [
    ("Слу́шайте!", "请听！", "SLOO-shay-tye"), ("Повтори́те!", "请重复！", "pav-ta-REE-tye"),
    ("Чита́йте!", "请读！", "chee-TAY-tye"), ("Пиши́те!", "请写！", "pee-SHEE-tye"),
    ("Я не понима́ю.", "我不明白。", "ya ne pa-nee-MA-yoo"),
    ("Мо́жно вы́йти?", "可以出去吗？", "MOZH-na VY-tee"),
    ("Спаси́бо за уро́к.", "谢谢老师。", "spa-SEE-ba za oo-ROK"),
  ]))

# ============ GRAMMAR series (8 packs) ============

P("ru-grammar-nouns", "名词的性数格", "系统学习俄语名词性阳/阴/中、数和六格变化基本规律。",
  "https://images.unsplash.com/photo-1455390587902-285d26427c2b?q=80&w=1200&auto=format&fit=crop",
  ("rung-01", 1, "名词的性", "通过词尾判断俄语名词的性属。", [
    ("стол (он)", "桌子（阳性）", "stol"), ("кни́га (она́)", "书（阴性）", "KNEE-ga"),
    ("окно́ (оно́)", "窗户（中性）", "ak-NO"), ("уче́бник (он)", "教科书（阳性）", "oo-CHEB-neek"),
    ("ру́чка (она́)", "笔（阴性）", "ROOCH-ka"), ("письмо́ (оно́)", "信（中性）", "pees-MO"),
  ]),
  ("rung-02", 2, "名词的复数", "名词复数形式的构成。", [
    ("стол — столы́", "桌子们", "sta-LY"), ("кни́га — кни́ги", "多本书", "KNEE-gee"),
    ("окно́ — о́кна", "多扇窗户", "OK-na"), ("ночь — но́чи", "多个夜晚", "NO-chee"),
  ]))

P("ru-grammar-verbs", "动词变位", "第一变位法和第二变位法规则及常用动词练习。",
  "https://images.unsplash.com/photo-1513542789411-b6a5d4f31634?q=80&w=1200&auto=format&fit=crop",
  ("ruve-01", 1, "第一变位法", "-ать/-ять结尾动词变位规则。", [
    ("Я чита́ю.", "我读。", "ya chee-TA-yoo"), ("Ты чита́ешь.", "你读。", "ty chee-TA-yesh"),
    ("Он чита́ет.", "他读。", "on chee-TA-yet"), ("Мы чита́ем.", "我们读。", "my chee-TA-yem"),
    ("Вы чита́ете.", "您读。", "vy chee-TA-ye-tye"), ("Они чита́ют.", "他们读。", "a-NEE chee-TA-yoot"),
  ]),
  ("ruve-02", 2, "第二变位法", "-ить结尾动词变位规则。", [
    ("Я говорю́.", "我说。", "ya ga-va-RYOO"), ("Ты говори́шь.", "你说。", "ty ga-va-REESH"),
    ("Он говори́т.", "他说。", "on ga-va-REET"), ("Мы говори́м.", "我们说。", "my ga-va-REEM"),
    ("Вы говори́те.", "你们说。", "vy ga-va-REE-tye"), ("Они́ говоря́т.", "他们说。", "a-NEE ga-va-RYAT"),
  ]))

P("ru-grammar-adjectives", "形容词变格与用法", "形容词与名词性数一致关系、变格规律和短尾形式。",
  "https://images.unsplash.com/photo-1516979187457-637abb4f9353?q=80&w=1200&auto=format&fit=crop",
  ("ruad-01", 1, "形容词与名词一致", "形容词性数变化及其与名词搭配。", [
    ("но́вый стол", "新桌子（阳）", "NO-vy stol"), ("но́вая кни́га", "新书（阴）", "NO-va-ya KNEE-ga"),
    ("но́вое окно́", "新窗户（中）", "NO-va-ye ak-NO"), ("интере́сный фильм", "有趣的电影", "in-te-RES-ny feelm"),
    ("хоро́ший челове́к", "好人", "kha-RO-shiy che-la-VYEK"), ("хоро́шая погода", "好天气", "kha-RO-sha-ya pa-GO-da"),
  ]),
  ("ruad-02", 2, "短尾形容词", "短尾形容词构成和用法。", [
    ("Он бо́лен.", "他病了。", "on BO-len"), ("Она́ больна́.", "她病了。", "a-NA bal-NA"),
    ("Э́то возмо́жно.", "这是可能的。", "E-ta vaz-MOZH-na"), ("Я гото́в.", "我准备好了（男）", "ya ga-TOF"),
  ]))

P("ru-grammar-tenses", "动词时态", "俄语动词现在时、过去时和将来时构成与用法。",
  "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=1200&auto=format&fit=crop",
  ("rute-01", 1, "现在时", "未完成体动词现在时的变位和用法。", [
    ("Я рабо́таю.", "我工作。", "ya ra-BO-ta-yoo"), ("Он живёт в Москве́.", "他住在莫斯科。", "on zhee-VYOT v mas-KVYE"),
    ("Она́ говори́т по-ру́сски.", "她讲俄语。", "a-NA ga-va-REET pa-ROO-skee"),
    ("Я ду́маю, что э́то пра́вильно.", "我认为是对的。", "ya DOO-ma-yoo shto E-ta PRA-veel-na"),
  ]),
  ("rute-02", 2, "过去时和将来时", "过去时-л和将来时构成。", [
    ("Я чита́л.", "我读过（男）", "ya chee-TAL"), ("Я чита́ла.", "我读过（女）", "ya chee-TA-la"),
    ("Мы чита́ли.", "我们读过。", "my chee-TA-lee"), ("Я бу́ду чита́ть.", "我将要读。", "ya BOO-doo chee-TAT"),
    ("За́втра бу́дет дождь.", "明天将下雨。", "ZAV-tra BOO-det dozhd"),
  ]))

P("ru-grammar-cases", "名词六格入门", "俄语名词六格主/属/与/宾/造/前的基本用法和词尾变化。",
  "https://images.unsplash.com/photo-1450101499163-c8848c66ca85?q=80&w=1200&auto=format&fit=crop",
  ("ruca-01", 1, "第一、二、三格", "主格、属格和与格基本用法。", [
    ("Э́то кни́га.", "这是一本书（主格）", "E-ta KNEE-ga"), ("Нет кни́ги.", "没有书（属格）", "nyet KNEE-gee"),
    ("Дать кни́гу дру́гу.", "把书给朋友（与格）", "dat KNEE-goo DROO-goo"),
    ("Помо́чь бра́ту.", "帮助弟弟（与格）", "pa-MOCH BRA-too"),
  ]),
  ("ruca-02", 2, "第四、五、六格", "宾格、造格和前置格基本用法。", [
    ("Я ви́жу кни́гу.", "我看见书（宾格）", "ya VEE-zhoo KNEE-goo"),
    ("Я пишу́ ру́чкой.", "我用钢笔写（造格）", "ya pee-SHOO ROOCH-koy"),
    ("Мы живём в Росси́и.", "我们住在俄罗斯（前置格）", "my zhee-VYOM v ras-SEE-ee"),
  ]))

P("ru-grammar-prepositions", "前置词用法", "俄语常用前置词用法总结，包括地点、时间等前置词格搭配。",
  "https://images.unsplash.com/photo-1504711434969-e33886168d6c?q=80&w=1200&auto=format&fit=crop",
  ("rupr-01", 1, "地点前置词", "方位和地点关系的常用前置词。", [
    ("в шко́ле", "在学校里（前置格）", "f SHKO-lye"), ("на уро́ке", "在课上（前置格）", "na oo-RO-kye"),
    ("под столо́м", "在桌子下（造格）", "pad sta-LOM"), ("за шко́лой", "在学校后（造格）", "za SHKO-loy"),
  ]),
  ("rupr-02", 2, "时间前置词", "表示时间关系的常用前置词。", [
    ("в понеде́льник", "在星期一（宾格）", "f pa-nee-DYEL-neek"), ("в январе́", "在一月（前置格）", "v yan-va-RE"),
    ("до уро́ка", "课前（属格）", "da oo-RO-ka"), ("по́сле уро́ка", "课后（属格）", "POS-lye oo-RO-ka"),
    ("че́рез час", "一小时后（宾格）", "CHE-rez chas"),
  ]))

P("ru-grammar-numerals", "数词与数量表达", "基数词和序数词在句中用法、数词与名词搭配规则。",
  "https://images.unsplash.com/photo-1509228627152-72ae9ae6848d?q=80&w=1200&auto=format&fit=crop",
  ("runn-01", 1, "基数词与名词搭配", "数词1-20与名词连用时格的规则。", [
    ("оди́н стол", "一张桌子（主格）", "a-DEEN stol"), ("два стола́", "两张桌子（属格）", "dva sta-LA"),
    ("три стола́", "三张桌子（属格）", "tree sta-LA"), ("пять столо́в", "五张桌子（复数属格）", "pyat sta-LOF"),
  ]),
  ("runn-02", 2, "数量表达", "常用数量表达方式。", [
    ("мно́го люде́й", "很多人（复数属格）", "MNO-ga lyu-DEY"),
    ("ма́ло вре́мени", "很少时间（单数属格）", "MA-la VRE-me-nee"),
    ("ско́лько э́то сто́ит?", "这个多少钱？", "SKOL-ka E-ta STO-eet"),
  ]))

P("ru-grammar-pronouns", "代词系统", "人称代词、物主代词、指示代词和疑问代词的变格与用法。",
  "https://images.unsplash.com/photo-1505499580336-3a1effe0f143?q=80&w=1200&auto=format&fit=crop",
  ("rupn-01", 1, "人称代词变格", "人称代词各格变位形式。", [
    ("меня́", "我（属格/宾格）", "me-NYA"), ("мне", "我（与格/前置格）", "mnye"),
    ("тебя́", "你（属格/宾格）", "te-BYA"), ("его́", "他（属格/宾格）", "ye-VO"),
    ("У меня́ есть кни́га.", "我有一本书。", "oo me-NYA yest KNEE-ga"),
  ]),
  ("rupn-02", 2, "物主代词和指示代词", "物主代词变化和этот的用法。", [
    ("мой стол", "我的桌子（阳）", "moy stol"), ("моя́ кни́га", "我的书（阴）", "ma-YA KNEE-ga"),
    ("на́ш университе́т", "我们的大学", "nash oo-nee-ver-see-TET"),
    ("э́та кни́га", "这本书", "E-ta KNEE-ga"),
  ]))

# ============ SPEAKING series (9 packs) ============

P("ru-spoken-daily", "日常起居对话", "从起床到就寝的日常生活常用俄语对话。",
  "https://images.unsplash.com/photo-1517457215247-5ff9f200adb0?q=80&w=1200&auto=format&fit=crop",
  ("rusd-01", 1, "早上起床", "早晨起床后常用对话。", [
    ("До́брое у́тро! Пора́ встава́ть.", "早上好！该起床了。", "DOB-ra-ye OOT-ra pa-RA vsta-VAT"),
    ("Кото́рый час?", "几点了？", "ka-TO-ry chas"), ("Я проспа́л(а)!", "我睡过头了！", "ya pras-PAL(a)"),
    ("Ну́жно чи́стить зу́бы.", "要刷牙。", "NOOZH-na CHEEST-eet ZOO-by"),
    ("Что у нас на за́втрак?", "早餐吃什么？", "shto oo nas na ZAV-trak"),
  ]),
  ("rusd-02", 2, "晚间休息", "晚上回家和就寝前常用表达。", [
    ("Я верну́лся(ась) домо́й.", "我回家了。", "ya ver-NOOL-sya(s) da-MOY"),
    ("Да, я о́чень уста́л(а).", "是的，我很累。", "da ya O-chen oo-STAL(a)"),
    ("Хо́чешь посмотре́ть фильм?", "想看个电影吗？", "KHO-chesh pa-sma-TRYET feelm"),
    ("Споко́йной но́чи!", "晚安！", "spa-KOY-noy NO-chee"),
  ]))

P("ru-spoken-restaurant", "餐厅就餐俄语", "在俄语国家餐厅点餐付款评价的全过程对话。",
  "https://images.unsplash.com/photo-1514933651103-005eec06c04b?q=80&w=1200&auto=format&fit=crop",
  ("rurs-01", 1, "进入餐厅与点餐", "进入餐厅就座点餐和询问菜品。", [
    ("Здра́вствуйте! У вас есть свобо́дный столи́к?", "您好！有空桌吗？", "ZDRA-stvooy-tye oo vas YEST sva-BOD-ny sta-LEEK"),
    ("На двои́х.", "两位。", "na dva-YEKH"), ("Вот меню́, пожа́луйста.", "这是菜单。", "vot me-NYU pa-ZHAL-sta"),
    ("Что вы порекоменду́ете?", "您推荐什么？", "shto vy pa-re-ka-men-DOO-ye-tye"),
    ("Э́то о́чень вкусно!", "这非常好吃！", "E-ta O-chen VKOOS-na"),
  ]),
  ("rurs-02", 2, "结账与评价", "用餐结束时的对话。", [
    ("Принеси́те счёт, пожа́луйста.", "请拿账单。", "pri-ne-SEE-tye shyot pa-ZHAL-sta"),
    ("Мо́жно заплати́ть ка́ртой?", "可以用卡支付吗？", "MOZH-na za-pla-TEET KAR-toy"),
    ("Спаси́бо, всё бы́ло о́чень вкусно!", "谢谢，都很好吃！", "spa-SEE-ba vsyo BY-la O-chen VKOOS-na"),
    ("Мы придём ещё раз.", "我们还会再来的。", "my pree-DYOM ye-SHYO raz"),
  ]))

P("ru-spoken-shopping", "购物俄语", "在商店超市和市场的购物对话。",
  "https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?q=80&w=1200&auto=format&fit=crop",
  ("rush-01", 1, "超市购物", "在超市选购商品的常用表达。", [
    ("Ско́лько э́то сто́ит?", "这个多少钱？", "SKOL-ka E-ta STO-eet"),
    ("Да́йте, пожа́луйста, килогра́мм я́блок.", "请给一公斤苹果。", "DAY-tye pa-ZHAL-sta kee-la-GRAM YAB-lak"),
    ("Э́то всё?", "就这些？", "E-ta vsyo"), ("Где ка́сса?", "收银台在哪？", "gde KAS-sa"),
  ]),
  ("rush-02", 2, "服装购物", "买衣服时常用对话。", [
    ("Мо́жно поме́рить?", "可以试穿吗？", "MOZH-na PO-me-reet"),
    ("Где примерочная?", "试衣间在哪？", "gde pree-ME-rach-na-ya"),
    ("Мне ну́жен ра́змер бо́льше.", "要大一号的。", "mnye NOO-zhen RAZ-mer BOL-she"),
    ("Э́то сли́шком до́рого.", "太贵了。", "E-ta SLEESH-kam DO-ra-ga"),
    ("Я беру́.", "我买了。", "ya be-ROO"),
  ]))

P("ru-spoken-transport", "交通出行俄语", "乘坐公交地铁出租车以及在路上的交际用语。",
  "https://images.unsplash.com/photo-1569254715326-9d5c1a9d53a6?q=80&w=1200&auto=format&fit=crop",
  ("rutr-01", 1, "问路与方向", "询问路线和方向的常用表达。", [
    ("Как пройти́ ...?", "怎样走到？", "kak proy-TEE"), ("Где这里...?", "在哪里？", "gde ZDYES"),
    ("Иди́те прямо.", "请直走。", "ee-DEE-tye PRYA-ma"),
    ("Поверни́те нале́во.", "请左转。", "pa-ver-NEE-tye na-LE-va"),
    ("Поверни́те напра́во.", "请右转。", "pa-ver-NEE-tye na-PRA-va"),
  ]),
  ("rutr-02", 2, "公共交通", "公交和地铁常用表达。", [
    ("Где остано́вка авто́буса?", "公交车站在哪？", "gde as-ta-NOV-ka af-TO-boo-sa"),
    ("Ско́лько сто́ит биле́т?", "票价多少？", "SKOL-ka STO-eet bee-LYET"),
    ("Вы выхо́дите?", "您下车吗？", "vy vy-KHO-dee-tye"),
    ("Сле́дующая станция — ...", "下一站是……", "SLYE-doo-yoo-shcha-ya STAN-tsee-ya"),
  ]))

P("ru-spoken-hotel", "酒店住宿俄语", "预订酒店办理入住和退房的全过程俄语对话。",
  "https://images.unsplash.com/photo-1566073771259-6a8506099945?q=80&w=1200&auto=format&fit=crop",
  ("ruho-01", 1, "入住酒店", "办理入住手续的常用表达。", [
    ("У вас есть свобо́дные но́мера?", "有空房吗？", "oo vas YEST sva-BOD-ny-ye NO-me-ra"),
    ("Я заброни́ровал(а) но́мер.", "我预订了。", "ya za-bra-NEE-ra-val(a) NO-mer"),
    ("За́втрак включён?", "含早餐吗？", "ZAV-trak vklyu-CHYON"),
  ]),
  ("ruho-02", 2, "客房服务与退房", "呼叫客房服务和退房。", [
    ("Не рабо́тает телефо́н.", "电话不好用。", "ne ra-BO-ta-yet te-le-FON"),
    ("Я хочу́ продли́ть прожива́ние.", "想延长住宿。", "ya kha-CHOO prad-LEET pra-zhe-VA-nee-ye"),
    ("Во ско́лько вы́езд?", "几点退房？", "va SKOL-ka VY-ye-zd"),
  ]))

P("ru-spoken-hospital", "看病就医俄语", "在医院看病时描述症状取药的俄语表达。",
  "https://images.unsplash.com/photo-1551076805-e1869033e561?q=80&w=1200&auto=format&fit=crop",
  ("ruhi-01", 1, "描述症状", "向医生描述身体不适。", [
    ("Мне ну́жен врач.", "需要看医生。", "mnye NOO-zhen vrach"),
    ("У меня́ боли́т голова́.", "我头疼。", "oo me-NYA ba-LEET ga-la-VA"),
    ("У меня́ боли́т живо́т.", "肚子疼。", "oo me-NYA ba-LEET zhee-VOT"),
    ("У меня́ температу́ра.", "发烧了。", "oo me-NYA tem-pe-ra-TOO-ra"),
    ("У меня́ ка́шель.", "咳嗽。", "oo me-NYA KA-shel"),
  ]),
  ("ruhi-02", 2, "取药与医嘱", "在药房取药和听取医嘱。", [
    ("Где ближа́йшая апте́ка?", "最近药房在哪？", "gde blee-ZHAY-sha-ya ap-TYE-ka"),
    ("Принима́йте два ра́за в день.", "一天吃两次。", "pree-nee-MAY-tye dva RA-za v den"),
    ("Пе́ред едо́й.", "饭前服用。", "PYE-red ye-DOY"),
    ("По́сле еды́.", "饭后服用。", "POS-lye ye-DY"),
  ]))

P("ru-spoken-work", "职场俄语", "工作场合俄语对话，包括面试开会日常办公等。",
  "https://images.unsplash.com/photo-1600880292203-757bb62b4baf?q=80&w=1200&auto=format&fit=crop",
  ("ruwk-01", 1, "办公室日常", "办公室日常交流常用表达。", [
    ("До́брое у́тро, колле́ги!", "早上好同事们！", "DOB-ra-ye OOT-ra ka-LE-gee"),
    ("Каки́е пла́ны на сего́дня?", "今天有什么计划？", "ka-KEE-ye PLA-ny na se-VOD-nya"),
    ("За́втра в 10 утра́ совеща́ние.", "明天上午10点开会。", "ZAV-tra v dye-syat oot-RA sa-ve-SHCHA-nee-ye"),
    ("Есть вопро́сы?", "有问题吗？", "yest vap-RO-sy"),
  ]),
  ("ruwk-02", 2, "商务沟通", "商务场合正式沟通用语。", [
    ("О́чень рад(а) познако́миться.", "很高兴认识您。", "O-chen rat(a) paz-na-KO-meet-sya"),
    ("Чем вы занима́етесь?", "您从事什么工作？", "chem vy za-nee-MA-ye-tyes"),
    ("Мы мо́жем обсуди́ть э́то.", "可以讨论一下。", "my MO-zhem ab-soo-DEET E-ta"),
    ("Договори́лись.", "就这么说定了。", "da-ga-va-REE-lees"),
  ]))

P("ru-spoken-phone", "电话俄语", "接打电话预约和留言的俄语实用表达。",
  "https://images.unsplash.com/photo-1534536281715-e28d76689b9e?q=80&w=1200&auto=format&fit=crop",
  ("ruph-01", 1, "接打电话", "电话沟通基本用语。", [
    ("Алло́!", "喂！", "a-LO"), ("Мину́точку, подожди́те.", "请稍等。", "mee-NOO-tach-koo pa-da-ZHDEE-tye"),
    ("Его́/её нет на ме́сте.", "他不在。", "ye-VO/ye-YO nyet na MES-tye"),
    ("Перезвони́те по́зже.", "请稍后再打。", "pe-re-zva-NEE-tye POZ-zhe"),
  ]),
  ("ruph-02", 2, "预约与留言", "电话预约和留言。", [
    ("Мо́жно записа́ться на приём?", "可以预约吗？", "MOZH-na za-pee-SAT-sya na pree-YOM"),
    ("Я хочу́ заброни́ровать столи́к.", "想订桌。", "ya kha-CHOO za-bra-NEE-ra-vat sta-LEEK"),
    ("Переда́йте ему́, что звони́л(а) ...", "请转告他……来过电话。", "pe-re-DAY-tye ye-MOO shto zva-NEEL(a)"),
  ]))

P("ru-spoken-emergency", "紧急情况俄语", "遇到紧急情况时的求救和报警用语。",
  "https://images.unsplash.com/photo-1587745416684-47953f16f82b?q=80&w=1200&auto=format&fit=crop",
  ("ruem-01", 1, "紧急求救", "遇到危险时的呼救表达。", [
    ("Помоги́те!", "救命！", "pa-ma-GEE-tye"),
    ("Позови́те ско́рую по́мощь!", "叫救护车！", "pa-za-VEE-tye SKO-roo-yu PO-mashch"),
    ("Вы́зовите поли́цию!", "叫警察！", "VY-za-vee-tye pa-LEE-tsee-yoo"),
    ("Меня́ огра́били!", "被抢劫了！", "me-NYA a-GRA-bee-lee"),
    ("Я потеря́лся(ась)!", "迷路了！", "ya pa-te-RYAL-sya(s)"),
  ]),
  ("ruem-02", 2, "寻求帮助", "向他人请求帮助的表达。", [
    ("101 — Пожа́рная слу́жба", "101火警", "sto-a-DEEN pa-ZHAR-na-ya sloo-ZHBA"),
    ("102 — Поли́ция", "102报警", "sto-DVA pa-LEE-tsee-ya"),
    ("103 — Ско́рая по́мощь", "103急救", "sto-TREE SKO-ra-ya PO-mashch"),
    ("Я не понима́ю. Говори́те ме́дленнее.", "听不懂请说慢点。", "ya ne pa-nee-MA-yoo ga-va-REE-tye MED-le-ne-ye"),
  ]))

P("ru-spoken-visiting", "做客与社交俄语", "拜访朋友参加聚会和社交场合的俄语表达。",
  "https://images.unsplash.com/photo-1543269865-cbf427effbad?q=80&w=1200&auto=format&fit=crop",
  ("ruvs-01", 1, "拜访朋友", "去朋友家做客的礼貌用语。", [
    ("Проходи́те, сади́тесь.", "请进请坐。", "pra-kha-DEE-tye sa-DEE-tyes"),
    ("Чу́вствуйте себя́ как до́ма.", "请像在家一样。", "CHOOF-stvooy-tye se-BYA kak DO-ma"),
    ("Спаси́бо за гостеприи́мство!", "谢谢款待！", "spa-SEE-ba za gas-te-pree-EEM-stva"),
  ]),
  ("ruvs-02", 2, "社交谈话", "聚会和社交场合聊天话题。", [
    ("Отку́да вы?", "您从哪里来？", "at-KOO-da vy"),
    ("Вы уже́ бы́ли в России?", "去过俄罗斯吗？", "vy OO-zhe BY-lee v ras-SEE-ee"),
    ("Бы́ло о́чень прия́тно познако́миться!", "很高兴认识您！", "BY-la O-chen pree-YAT-na paz-na-KO-meet-sya"),
  ]))

# ============ EXAM series (3 packs) ============

P("exam-graduate-ru", "考研俄语 · 备考练习", "全国硕士研究生入学统一考试俄语202备考词汇与练习。",
  "https://images.unsplash.com/photo-1434030216411-0b793f4b4173?q=80&w=1200&auto=format&fit=crop",
  ("exgr-01", 1, "考研高频词汇", "考研俄语常见高频词汇。", [
    ("иссле́дование", "研究", "ees-SLYE-da-va-nee-ye"), ("разви́тие", "发展", "raz-VEE-tee-ye"),
    ("управле́ние", "管理", "oo-prav-LE-nee-ye"), ("экономи́ческий", "经济的", "e-ka-na-MEE-che-skee"),
    ("обще́ственный", "社会的", "ap-SHCHEST-ven-ny"), ("де́ятельность", "活动", "DYE-ya-tel-nost"),
    ("увели́чивать", "增加", "oo-ve-LEE-chee-vat"), ("снижа́ть", "降低", "snee-ZHAT"),
  ]),
  ("exgr-02", 2, "阅读理解基础", "考研阅读中常见句式和连接词。", [
    ("В да́нной статье́ ...", "在本文中……", "v DAN-nay sta-TYE"),
    ("Автор счита́ет, что ...", "作者认为……", "AF-tar schee-TA-yet shto"),
    ("Ва́жно подчеркну́ть, что ...", "重要的是强调……", "VAZH-na pad-cherk-NOOT shto"),
    ("таки́м о́бразом", "因此", "ta-KEEM OB-ra-zam"),
  ]))

P("exam-college-ru", "大学俄语四六级 · 备考", "全国大学俄语四六级考试CRT-4/6备考词汇与语法练习。",
  "https://images.unsplash.com/photo-1523050854058-8df90110c7f1?q=80&w=1200&auto=format&fit=crop",
  ("exco-01", 1, "四级高频词汇", "俄语四级考试常见词汇。", [
    ("сообще́ние", "通知消息", "sa-ap-SHCHA-nee-ye"), ("объявле́ние", "公告", "ab-yav-LE-nee-ye"),
    ("предприя́тие", "企业", "pred-pree-YA-tee-ye"), ("специали́ст", "专家", "spe-tsia-LEEST"),
    ("должность", "职位", "DOLZH-nost"), ("командиро́вка", "出差", "ka-man-dee-ROV-ka"),
  ]),
  ("exco-02", 2, "六级高频词汇", "俄语六级考试常见词汇。", [
    ("законода́тельство", "立法", "za-ka-na-DA-tel-stva"), ("междунаро́дный", "国际的", "mezh-doo-na-ROD-ny"),
    ("сотру́дничество", "合作", "sa-TROOD-nee-che-stva"), ("конкуренция", "竞争", "kan-koo-REN-tsee-ya"),
    ("инвести́ция", "投资", "in-ves-TEE-tsee-ya"), ("технология", "技术", "tekh-na-LO-gee-ya"),
  ]))

P("exam-highschool-ru", "俄语高考 · 词汇与语法", "普通高等学校招生全国统一考试俄语科目备考。",
  "https://images.unsplash.com/photo-1524178232363-1fb2b075b655?q=80&w=1200&auto=format&fit=crop",
  ("exhi-01", 1, "高考必背词汇", "高考俄语高频词汇。", [
    ("шко́ла", "学校", "SHKO-la"), ("учи́тель", "教师", "oo-CHEE-tel"),
    ("экза́мен", "考试", "ek-ZA-men"), ("зада́ние", "任务题目", "za-DA-nee-ye"),
    ("оце́нка", "分数评价", "a-TSEN-ka"), ("кани́кулы", "假期", "ka-NEE-koo-ly"),
  ]),
  ("exhi-02", 2, "高频语法考点", "高考俄语语法考查重点。", [
    ("Я бу́ду учи́ться в университе́те.", "我将在大学学习。", "ya BOO-doo oo-CHEET-sya v oo-nee-ver-see-TYE-tye"),
    ("Мы до́лжны́ помо́чь друг дру́гу.", "应该互相帮助。", "my dalzh-NY pa-MOCH droog DROO-goo"),
    ("Чем бо́льше, тем лу́чше.", "越多越好。", "chem BOL-she tem LOOCH-she"),
  ]))

# ============ FLUENT series (8 packs) ============

P("ru-construction", "建筑工程俄语", "建筑工地工程管理施工材料等建筑行业常用俄语词汇。",
  "https://images.unsplash.com/photo-1504307651254-84280e79f956?q=80&w=1200&auto=format&fit=crop",
  ("ruco-01", 1, "建筑材料", "常见建筑材料的俄语名称。", [
    ("цеме́нт", "水泥", "tse-MENT"), ("бето́н", "混凝土", "be-TON"), ("кирпи́ч", "砖", "keer-PEECH"),
    ("песо́к", "沙子", "pe-SOK"), ("сталь", "钢材", "stal"), ("де́рево", "木材", "DYE-re-va"),
  ]),
  ("ruco-02", 2, "施工现场", "施工现场常用词汇和指令。", [
    ("строи́тельная площа́дка", "建筑工地", "stra-EE-tel-na-ya pla-SHCHAD-ka"),
    ("кра́н", "起重机", "kran"), ("армату́ра", "钢筋", "ar-ma-TOO-ra"),
    ("те́хника безопа́сности", "安全规范", "TEKH-nee-ka be-za-PAS-nas-tee"), ("ка́ска", "安全帽", "KAS-ka"),
  ]))

P("ru-logistics", "国际物流俄语", "国际物流海关运输仓储等外贸物流领域的俄语专业词汇。",
  "https://images.unsplash.com/photo-1579027989536-b7b1f875659b?q=80&w=1200&auto=format&fit=crop",
  ("rulo-01", 1, "物流运输", "运输方式和物流操作词汇。", [
    ("гру́з", "货物", "groz"), ("конте́йнер", "集装箱", "kan-TEY-ner"),
    ("морско́й по́рт", "海港", "mar-SKOY port"), ("аэропо́рт", "机场", "a-e-ro-PORT"),
    ("авиаперево́зка", "空运", "a-vee-a-pe-re-VOZ-ka"), ("морска́я перево́зка", "海运", "mar-SKA-ya pe-re-VOZ-ka"),
  ]),
  ("rulo-02", 2, "海关与仓储", "清关和仓储相关词汇。", [
    ("тамо́жня", "海关", "ta-MOZH-nya"), ("тамо́женная деклара́ция", "报关单", "ta-MO-zhen-na-ya de-kla-RA-tsee-ya"),
    ("по́шлина", "关税", "POSH-lee-na"), ("склад", "仓库", "sklad"), ("отгру́зка", "发货", "at-GROOZ-ka"),
    ("получа́тель", "收货人", "pa-loo-CHA-tel"),
  ]))

P("ru-it-tech", "IT技术俄语", "计算机软件开发网络技术等信息科技领域俄语专业词汇。",
  "https://images.unsplash.com/photo-1555066931-4365d14bab8c?q=80&w=1200&auto=format&fit=crop",
  ("ruit-01", 1, "计算机基础", "计算机软硬件基础词汇。", [
    ("компью́тер", "计算机", "kam-PYU-tyer"),
    ("програ́ммное обеспе́чение", "软件", "pra-GRAM-na-ye a-bes-PYE-che-nee-ye"),
    ("операцио́нная систе́ма", "操作系统", "a-pe-ra-tsee-ON-na-ya sis-TYE-ma"),
    ("се́рвер", "服务器", "SER-ver"), ("сеть", "网络", "set"), ("ба́за да́нных", "数据库", "BA-za DAN-nykh"),
  ]),
  ("ruit-02", 2, "编程与技术", "编程和技术开发的常用词汇。", [
    ("язы́к програ́ммирования", "编程语言", "ya-ZYK pra-GRAM-mee-ro-va-nee-ya"),
    ("код", "代码", "kod"), ("алгори́тм", "算法", "al-go-REETM"),
    ("фу́нкция", "函数", "FOONK-tsee-ya"), ("приложе́ние", "应用程序", "pree-la-ZHE-nee-ye"),
    ("разрабо́тчик", "开发者", "raz-ra-BOT-cheek"),
  ]))

P("ru-legal", "法律俄语", "法律文书合同术语和法庭用语等法律行业俄语词汇。",
  "https://images.unsplash.com/photo-1589578532925-fd0c6f0e1f5a?q=80&w=1200&auto=format&fit=crop",
  ("rule-01", 1, "法律术语", "法律领域基础专业词汇。", [
    ("зако́н", "法律", "za-KON"), ("догово́р", "合同", "da-ga-VOR"), ("пра́во", "权利法律", "PRA-va"),
    ("обяза́нность", "义务", "a-bya-ZAN-nost"), ("су́д", "法院", "sood"), ("юри́ст", "律师", "yoo-REEST"),
  ]),
  ("rule-02", 2, "合同与诉讼", "合同签订和诉讼相关词汇。", [
    ("и́ск", "诉讼", "eesk"), ("исте́ц", "原告", "ees-TETS"), ("отве́тчик", "被告", "at-VYET-cheek"),
    ("сторо́ны", "双方", "sta-RO-ny"), ("подписа́ть догово́р", "签署合同", "pad-pee-SAT da-ga-VOR"),
  ]))

P("ru-medical", "医学俄语", "人体解剖疾病名称医疗操作等医学领域俄语词汇。",
  "https://images.unsplash.com/photo-1559757175-5700dde675bc?q=80&w=1200&auto=format&fit=crop",
  ("rumd-01", 1, "人体器官", "人体主要器官俄语名称。", [
    ("се́рдце", "心脏", "SER-tse"), ("лёгкие", "肺", "LYOHK-kee-ye"), ("пе́чень", "肝脏", "PE-chen"),
    ("желу́док", "胃", "zhe-LOO-dak"), ("кровь", "血液", "krov"), ("мо́зг", "大脑", "mozk"),
  ]),
  ("rumd-02", 2, "疾病与诊断", "常见疾病和诊断术语。", [
    ("диа́гноз", "诊断", "dee-AG-nos"), ("симпто́м", "症状", "seemp-TOM"),
    ("просту́да", "感冒", "pra-STOO-da"), ("воспале́ние", "炎症", "vas-pa-LE-nee-ye"),
    ("инфе́кция", "感染", "in-FEK-tsee-ya"), ("реце́пт", "处方", "re-TSEPT"),
  ]))

P("ru-trade", "经贸俄语", "国际贸易商务谈判和市场销售等商务俄语专业词汇。",
  "https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?q=80&w=1200&auto=format&fit=crop",
  ("rutd-01", 1, "贸易词汇", "国际贸易基础专业词汇。", [
    ("вне́шняя торго́вля", "对外贸易", "VNESH-nyay-a tar-GOV-lya"), ("и́мпорт", "进口", "EEM-port"),
    ("э́кспорт", "出口", "EK-sport"), ("контра́кт", "合约", "kan-TRAKT"),
    ("сде́лка", "交易", "ZDYEL-ka"), ("поста́вка", "供货", "pas-TAV-ka"),
  ]),
  ("rutd-02", 2, "谈判与市场", "商务谈判和市场推广词汇。", [
    ("перегово́ры", "谈判", "pe-re-ga-VO-ry"), ("ски́дка", "折扣", "SKEED-ka"),
    ("конкуре́нт", "竞争者", "kan-koo-RENT"), ("рекла́ма", "广告", "rek-LA-ma"),
    ("партнёр", "合作伙伴", "part-NYOR"), ("при́быль", "利润", "PREE-bel"),
  ]))

P("ru-tourism", "旅游行业俄语", "导游服务景点介绍酒店预订等旅游行业俄语专业词汇。",
  "https://images.unsplash.com/photo-1467269204594-9661b134dd2b?q=80&w=1200&auto=format&fit=crop",
  ("ruto-01", 1, "导游服务", "导游工作常用表达。", [
    ("экскурсово́д", "导游", "ek-skoor-sa-VOD"), ("достопримеча́тельность", "名胜古迹", "da-sta-pree-me-CHA-tel-nost"),
    ("экскурсия", "游览", "ek-SKOOR-see-ya"), ("биле́т", "门票", "bee-LYET"),
    ("расписа́ние", "时刻表", "ra-pee-SA-nee-ye"),
  ]),
  ("ruto-02", 2, "景点与饮食", "旅游景点和特色饮食表达。", [
    ("Кра́сная пло́щадь", "红场", "KRAS-na-ya PLO-shchad"), ("Кремль", "克里姆林宫", "kreml"),
    ("Эрмита́ж", "冬宫博物馆", "er-mee-TAZH"), ("блины́", "薄饼", "blee-NY"),
    ("борщ", "红菜汤", "borshch"), ("и́кра", "鱼子酱", "E-kra"),
  ]))

P("ru-education", "教育俄语", "教育体系学校管理教学方法等教育领域俄语词汇。",
  "https://images.unsplash.com/photo-1529390079861-591de354faf5?q=80&w=1200&auto=format&fit=crop",
  ("rued-01", 1, "教育体系", "俄罗斯教育体系相关词汇。", [
    ("образова́ние", "教育", "ab-ra-za-VA-nee-ye"), ("шко́ла", "中小学", "SHKO-la"),
    ("университе́т", "大学", "oo-nee-ver-see-TET"), ("аспира́нт", "研究生", "as-pee-RANT"),
    ("дипло́м", "毕业证", "dee-PLOM"),
  ]),
  ("rued-02", 2, "教学管理", "教学管理相关词汇。", [
    ("факульте́т", "系学院", "fa-kool-TET"), ("ле́кция", "讲座课", "LEK-tsee-ya"),
    ("семина́р", "研讨课", "se-mee-NAR"), ("зачёт", "考查", "za-CHYOT"), ("экза́мен", "考试", "ek-ZA-men"),
  ]))

# ============ OTHER series (10 packs) ============

P("ru-culture", "俄罗斯文化", "俄罗斯文学艺术传统习俗等文化知识相关俄语词汇。",
  "https://images.unsplash.com/photo-1513326738677-b964603b136d?q=80&w=1200&auto=format&fit=crop",
  ("rucl-01", 1, "文化艺术", "俄罗斯文化和艺术相关词汇。", [
    ("бале́т", "芭蕾舞", "ba-LET"), ("о́пера", "歌剧", "O-pe-ra"), ("теа́тр", "剧院", "te-ATR"),
    ("жи́вопись", "绘画", "ZHEE-va-pees"), ("музе́й", "博物馆", "moo-ZEY"), ("фолькло́р", "民间传说", "fal-KLOR"),
  ]),
  ("rucl-02", 2, "传统习俗", "俄罗斯传统节日和习俗词汇。", [
    ("Ма́сленица", "谢肉节", "MAS-le-nee-tsa"), ("матрёшка", "套娃", "ma-TRYOSH-ka"),
    ("самова́р", "俄式茶壶", "sa-ma-VAR"), ("берёза", "白桦树（俄罗斯象征）", "be-RYO-za"),
    ("Но́вый год", "新年", "NO-vy god"), ("День Побе́ды", "胜利日", "den pa-BYE-dy"),
  ]))

P("ru-history", "俄罗斯历史", "从基辅罗斯到现代俄罗斯的重要历史事件和人物相关俄语词汇。",
  "https://images.unsplash.com/photo-1467269204594-9661b134dd2b?q=80&w=1200&auto=format&fit=crop",
  ("ruhi-01", 1, "历史时期", "俄罗斯主要历史时期名称。", [
    ("Ки́евская Русь", "基辅罗斯", "KEE-yev-ska-ya Roos"), ("Росси́йская импе́рия", "俄罗斯帝国", "ras-SEE-ska-ya eem-PE-ree-ya"),
    ("СССР", "苏联", "es-es-es-ER"), ("ца́рь", "沙皇", "tsar"), ("рево́люция", "革命", "re-VO-lyu-tsee-ya"),
  ]),
  ("ruhi-02", 2, "历史人物", "俄罗斯重要历史人物。", [
    ("Пётр Пе́рвый", "彼得大帝", "pyotr PYER-vy"), ("Екатери́на Втора́я", "叶卡捷琳娜二世", "ye-ka-te-REE-na fta-RA-ya"),
    ("Пу́шкин", "普希金", "POOSH-keen"), ("Толсто́й", "托尔斯泰", "tal-STOY"),
    ("Гага́рин", "加加林", "ga-GA-reen"),
  ]))

P("ru-literature", "俄语文学名句", "俄罗斯经典文学作品中的著名语句。",
  "https://images.unsplash.com/photo-1495446815901-a7297e633e8d?q=80&w=1200&auto=format&fit=crop",
  ("ruli-01", 1, "经典语句", "俄罗斯文学中的经典名句。", [
    ("У́мом Росси́ю не поня́ть.", "用理智无法理解俄罗斯。", "OO-mom ras-SEE-yoo ne pa-NYAT"),
    ("Красота́ спасёт мир.", "美将拯救世界。", "kra-sa-TA spa-SYOT meer"),
    ("Челове́к — э́то звучи́т го́рдо!", "人这个字听起来多么自豪！", "che-la-VYEK E-ta zvoo-CHEET GOR-da"),
  ]),
  ("ruli-02", 2, "文学体裁", "文学体裁和写作相关词汇。", [
    ("рома́н", "长篇小说", "ra-MAN"), ("расска́з", "短篇小说", "ras-KAZ"),
    ("стихотворе́ние", "诗歌", "stee-kha-tva-RE-nee-ye"), ("поэ́т", "诗人", "pa-ET"),
    ("пи́сатель", "作家", "PEE-sa-tel"),
  ]))

P("ru-songs", "俄语经典歌曲", "通过俄罗斯经典歌曲学习俄语歌词中的常用词汇和表达。",
  "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=1200&auto=format&fit=crop",
  ("ruso-01", 1, "经典歌曲词汇", "俄罗斯经典歌曲名称和歌词词汇。", [
    ("Катю́ша", "喀秋莎", "ka-TYOO-sha"), ("Подмоско́вные вечера́", "莫斯科郊外的晚上", "pad-mas-KOV-ny-ye ve-che-RA"),
    ("Кали́нка", "卡林卡", "ka-LEEN-ka"), ("Пусть всегда́ бу́дет со́лнце!", "让永远有太阳！", "pust vseg-DA BOO-det SON-tse"),
  ]),
  ("ruso-02", 2, "音乐词汇", "音乐相关基础词汇。", [
    ("пе́сня", "歌曲", "PES-nya"), ("гита́ра", "吉他", "gee-TA-ra"), ("балала́йка", "巴拉莱卡琴", "ba-la-LAY-ka"),
    ("конце́рт", "音乐会", "kan-TSERT"), ("мело́дия", "旋律", "me-LO-dee-ya"),
  ]))

P("ru-proverbs", "俄语谚语与俗语", "俄罗斯常用谚语俗语和格言，理解俄语语言智慧。",
  "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?q=80&w=1200&auto=format&fit=crop",
  ("rupr-01", 1, "常用谚语", "俄罗斯最常见谚语。", [
    ("Без труда́ не вы́тащишь и ры́бку из пруда́.", "不劳则无获。", "bes troo-DA ne VY-ta-shchish ee RYEP-koo ees proo-DA"),
    ("Ти́ше е́дешь — да́льше бу́дешь.", "欲速则不达。", "TEE-she YE-desh DAL-she BOO-desh"),
    ("Повторе́ние — мать уче́ния.", "重复是学习之母。", "pav-ta-RE-nee-ye mat oo-CHE-nee-ya"),
    ("Век живи́ — век учи́сь.", "活到老学到老。", "vyek zhee-VEE vyek oo-CHEES"),
  ]),
  ("rupr-02", 2, "俗语与格言", "日常俗语和励志格言。", [
    ("В гостя́х хорошо́, а до́ма лу́чше.", "做客虽好不如在家。", "f gas-TYAKH kha-ra-SHO a DO-ma LOOCH-she"),
    ("Друг познаётся в беде́.", "患难见真情。", "droog paz-na-YO-t-sa v be-DE"),
    ("Де́лу — вре́мя, поте́хе — час.", "工作有时娱乐有时。", "DYE-loo VRE-mya pa-TYE-khe chas"),
  ]))

P("ru-idioms", "俄语成语与固定搭配", "俄语常用成语动词固定搭配和习惯用语。",
  "https://images.unsplash.com/photo-1504904126296-3a0e56c1ddd9?q=80&w=1200&auto=format&fit=crop",
  ("ruid-01", 1, "常用成语", "俄罗斯人常用成语表达。", [
    ("бить баклу́ши", "无所事事", "BEET bak-LOO-shee"), ("как ры́ба в воде́", "如鱼得水", "kak RY-ba v va-DE"),
    ("медве́жья услу́га", "帮倒忙", "med-VYEZH-ya oo-SLOO-ga"), ("сесть в лу́жу", "出丑", "sest v LOO-zhoo"),
    ("ши́то бе́лыми ни́тками", "破绽百出", "SHEE-ta BYE-ly-mee NEET-ka-mee"),
  ]),
  ("ruid-02", 2, "动词固定搭配", "常见动词+名词固定搭配。", [
    ("приня́ть реше́ние", "做决定", "pree-NYAT re-SHE-nee-ye"), ("обрати́ть внима́ние", "注意", "ab-ra-TEET vnee-MA-nee-ye"),
    ("приня́ть уча́стие", "参加", "pree-NYAT oo-CHA-stee-ye"), ("одержа́ть побе́ду", "取得胜利", "a-der-ZHAT pa-BYE-doo"),
    ("нанести́ визи́т", "拜访", "na-nes-TEE vee-ZEET"),
  ]))

P("ru-geography", "俄罗斯地理", "俄罗斯地理名称自然景观和城市相关俄语词汇。",
  "https://images.unsplash.com/photo-1540648639573-8c848de23f0a?q=80&w=1200&auto=format&fit=crop",
  ("ruge-01", 1, "主要城市", "俄罗斯主要城市俄语名称。", [
    ("Москва́", "莫斯科", "mas-KVA"), ("Санкт-Петербург", "圣彼得堡", "sankt-pe-ter-BOORK"),
    ("Владивосто́к", "符拉迪沃斯托克", "vla-dee-vas-TOK"), ("Со́чи", "索契", "SO-chee"),
    ("Каза́нь", "喀山", "ka-ZAN"),
  ]),
  ("ruge-02", 2, "自然地理", "自然环境地理术语。", [
    ("Волга", "伏尔加河", "VOL-ga"), ("Ура́л", "乌拉尔", "oo-RAL"),
    ("Сиби́рь", "西伯利亚", "see-BEER"), ("тайга́", "泰加林", "tay-GA"), ("Байка́л", "贝加尔湖", "bay-KAL"),
  ]))

P("ru-news", "俄语新闻用语", "政治经济社会等新闻中常用的俄语词汇和句式。",
  "https://images.unsplash.com/photo-1495020689067-958852a7765e?q=80&w=1200&auto=format&fit=crop",
  ("runw-01", 1, "新闻常用词汇", "新闻报导中高频出现的词汇。", [
    ("президе́нт", "总统", "pre-zee-DENT"), ("прави́тельство", "政府", "pra-vee-TEL-stva"),
    ("перегово́ры", "谈判", "pe-re-ga-VO-ry"), ("соглаше́ние", "协议", "sa-gla-SHE-nee-ye"),
    ("экономи́ческий кри́зис", "经济危机", "e-ka-na-MEE-che-skee KREE-zis"),
    ("безрабо́тица", "失业", "bez-ra-BO-tee-tsa"),
  ]),
  ("runw-02", 2, "新闻句式", "新闻报导常用句式。", [
    ("По сообще́ниям ...", "据……报道", "pa sa-APSHCHA-nee-yam"),
    ("Ожида́ется, что ...", "预计……", "a-zhee-DA-ye-tsa shto"),
    ("Специали́сты отме́тили, что ...", "专家指出……", "spe-tsia-LEE-sty at-MYE-tee-lee shto"),
  ]))

P("ru-movies", "俄语影视台词", "通过俄罗斯经典电影和电视剧台词学习地道俄语口语表达。",
  "https://images.unsplash.com/photo-1536440136628-849c177e76a1?q=80&w=1200&auto=format&fit=crop",
  ("rumo-01", 1, "经典电影", "俄罗斯经典电影台词选段。", [
    ("Бриллиа́нтовая рука́", "钻石胳膊（经典喜剧）", "bree-lee-AN-ta-va-ya roo-KA"),
    ("Иро́ния судьбы́", "命运的捉弄", "ee-RO-nee-ya sood-BI"),
    ("Москва́ слеза́м не ве́рит", "莫斯科不相信眼泪", "mas-KVA sle-ZAM ne VE-reet"),
  ]),
  ("rumo-02", 2, "影视词汇", "影视和表演相关基础词汇。", [
    ("кино́", "电影", "kee-NO"), ("фильм", "影片", "feelm"), ("сериа́л", "电视剧", "se-ree-AL"),
    ("режиссёр", "导演", "re-zhee-SYOR"), ("сюже́т", "剧情", "syoo-ZHET"),
  ]))

P("ru-festivals", "俄罗斯节日与传统", "俄罗斯官方节日宗教节日和民间传统活动的俄语表达。",
  "https://images.unsplash.com/photo-1519671482749-fd09be7ccebf?q=80&w=1200&auto=format&fit=crop",
  ("rufe-01", 1, "官方节日", "俄罗斯重要官方节日。", [
    ("Но́вый год", "新年1月1日", "NO-vy god"), ("Рождество́ Христо́во", "圣诞节1月7日", "razh-de-STVO khrees-TO-va"),
    ("День Побе́ды", "胜利日5月9日", "den pa-BYE-dy"), ("День Росси́и", "俄罗斯日6月12日", "den ras-SEE-ee"),
  ]),
  ("rufe-02", 2, "传统节庆", "俄罗斯传统民间节庆活动。", [
    ("Ма́сленица", "谢肉节送冬节", "MAS-le-nee-tsa"), ("блины́ на Ма́сленицу", "谢肉节吃薄饼", "blee-NY na MAS-le-nee-tsoo"),
    ("хорово́д", "圆圈舞", "kha-ra-VOT"), ("креще́ние", "主显节", "kre-SHCHA-nee-ye"),
  ]))

# ============ WRITE FILES + SQL ============

EXISTING_IDS = {
    "ru-basic-pack", "ru-zero-basic",
    "vocab-pack-c36573e1-048f-4873-bbf3-65c2cdbc0db9", "ru-grammar-pack",
    "ru-spoken-pack",
    "catti-prep-pack",
    "torfl-a1-e5769998", "torfl-a2-e192410a", "torfl-b1-ce9db7bd", "torfl-b2-e526f4eb", "torfl-c1-9417e92f", "torfl-c2-b8cb9ae6",
    "ru-baby-care", "ru-oil-station",
    "east-uni-1", "east-uni-2", "east-uni-3", "east-uni-4", "east-uni-5", "east-uni-6", "east-uni-7", "east-uni-8",
}

written = []
for pack in PACKS:
    assert pack['id'] not in EXISTING_IDS, f"Duplicate pack id: {pack['id']}"
    fname = re.sub(r'[^a-z0-9]', '_', pack['id']) + '.json'
    path = os.path.join(CUSTOMS, fname)
    with open(path, 'w', encoding='utf-8') as f:
        json.dump(pack, f, ensure_ascii=False, indent=2)
    written.append((fname, pack['id'], pack['title']))

print(f'Created {len(written)} new course pack JSON files:\\n')
for fname, pid, title in written:
    print(f'  {fname}  ({pid})')
    print(f'    -> {title}')
    print()
