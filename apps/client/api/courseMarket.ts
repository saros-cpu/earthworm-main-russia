export interface MarketplaceCourse {
  id: string;
  title: string;
  description: string;
  author: string;
  authorId: string;
  statements: MarketplaceStatement[];
  category: "featured" | "official" | "community";
  tags: string[];
  downloads: number;
  rating: number;
  createdAt: string;
  updatedAt: string;
}

export interface MarketplaceStatement {
  russian: string;
  chinese: string;
  phonetic?: string;
}

const STORAGE_KEY = "courseMarketplace";
const MY_COURSES_KEY = "myImportedCourses";

function getAll(): MarketplaceCourse[] {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) || "[]");
  } catch {
    return [];
  }
}

function saveAll(courses: MarketplaceCourse[]) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(courses));
}

function getMyCourses(): string[] {
  try {
    return JSON.parse(localStorage.getItem(MY_COURSES_KEY) || "[]");
  } catch {
    return [];
  }
}

export function fetchMarketplaceCourses(category?: string): MarketplaceCourse[] {
  const all = getAll();
  if (!all.length) {
    const seed = getSeedData();
    saveAll(seed);
    return category ? seed.filter((c) => c.category === category) : seed;
  }
  return category ? all.filter((c) => c.category === category) : all;
}

export function fetchFeaturedCourses(): MarketplaceCourse[] {
  return fetchMarketplaceCourses("featured").slice(0, 4);
}

export function fetchTrendingCourses(): MarketplaceCourse[] {
  return [...fetchMarketplaceCourses()].sort((a, b) => b.downloads - a.downloads).slice(0, 5);
}

export function fetchCourseById(id: string): MarketplaceCourse | undefined {
  return getAll().find((c) => c.id === id);
}

export function cloneCourseToLocal(id: string): boolean {
  const all = getAll();
  const course = all.find((c) => c.id === id);
  if (!course) return false;
  course.downloads++;
  saveAll(all);
  const myCourses = getMyCourses();
  if (!myCourses.includes(id)) {
    myCourses.push(id);
    localStorage.setItem(MY_COURSES_KEY, JSON.stringify(myCourses));
  }
  return true;
}

export function isCourseCloned(id: string): boolean {
  return getMyCourses().includes(id);
}

export function addCommunityCourse(course: MarketplaceCourse) {
  const all = getAll();
  all.unshift(course);
  saveAll(all);
}

function getSeedData(): MarketplaceCourse[] {
  return [
    {
      id: "market-1",
      title: "俄语基础 30 句",
      description: "精选日常最常用的 30 个俄语句子，适合零基础入门",
      author: "Earthworm 官方",
      authorId: "official",
      statements: [
        { russian: "Привет!", chinese: "你好！" },
        { russian: "Как дела?", chinese: "你好吗？" },
        { russian: "Спасибо!", chinese: "谢谢！" },
      ],
      category: "featured",
      tags: ["入门", "日常"],
      downloads: 1280,
      rating: 4.8,
      createdAt: "2026-01-15",
      updatedAt: "2026-03-01",
    },
    {
      id: "market-2",
      title: "餐厅俄语",
      description: "在俄罗斯餐厅点餐、结账必备句型",
      author: "Earthworm 官方",
      authorId: "official",
      statements: [
        { russian: "Можно меню?", chinese: "可以看菜单吗？" },
        { russian: "Счёт, пожалуйста!", chinese: "买单！" },
      ],
      category: "official",
      tags: ["场景", "餐饮"],
      downloads: 856,
      rating: 4.6,
      createdAt: "2026-02-10",
      updatedAt: "2026-03-15",
    },
    {
      id: "market-3",
      title: "旅行俄语急救包",
      description: "旅行中用到的俄语紧急表达",
      author: "Анна",
      authorId: "user_anna",
      statements: [
        { russian: "Где находится туалет?", chinese: "厕所在哪里？" },
        { russian: "Я потерялся!", chinese: "我迷路了！" },
      ],
      category: "community",
      tags: ["旅行", "实用"],
      downloads: 423,
      rating: 4.3,
      createdAt: "2026-02-20",
      updatedAt: "2026-03-10",
    },
  ];
}
