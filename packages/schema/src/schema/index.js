// schema.js - Pure JavaScript schema for Earthworm MySQL database
// This file is loaded directly by Node.js, no TypeScript/bundler involved.
const { mysqlTable, int, text, boolean, timestamp, date, json, unique } = require("drizzle-orm/mysql-core");

const coursePack = mysqlTable("course_packs", {
  id: text("id").primaryKey().$defaultFn(() => require("@paralleldrive/cuid2").createId()),
  order: int("order").notNull(),
  title: text("title").notNull(),
  description: text("description"),
  isFree: boolean("is_free"),
  cover: text("cover"),
  creatorId: text("creator_id").notNull(),
  shareLevel: text("share_level").default("private"),
  createdAt: timestamp("created_at").notNull().defaultNow(),
  updatedAt: timestamp("updated_at").$onUpdateFn(() => new Date()),
});

const course = mysqlTable("courses", {
  id: text("id").primaryKey().$defaultFn(() => require("@paralleldrive/cuid2").createId()),
  title: text("title").notNull(),
  description: text("description"),
  video: text("video"),
  order: int("order").notNull(),
  coursePackId: text("course_pack_id").notNull().references(() => coursePack.id),
  createdAt: timestamp("created_at").notNull().defaultNow(),
  updatedAt: timestamp("updated_at").$onUpdateFn(() => new Date()),
});

const statement = mysqlTable("statements", {
  id: text("id").primaryKey().$defaultFn(() => require("@paralleldrive/cuid2").createId()),
  order: int("order").notNull(),
  chinese: text("chinese").notNull(),
  english: text("english").notNull(),
  soundmark: text("soundmark").notNull(),
  courseId: text("course_id").notNull().references(() => course.id),
  createdAt: timestamp("created_at").notNull().defaultNow(),
  updatedAt: timestamp("updated_at").$onUpdateFn(() => new Date()),
});

const courseHistory = mysqlTable("course_history", {
  id: text("id").primaryKey().$defaultFn(() => require("@paralleldrive/cuid2").createId()),
  userId: text("user_id").notNull(),
  courseId: text("course_id").notNull(),
  coursePackId: text("course_pack_id").notNull(),
  completionCount: int("completion_count").notNull(),
  createdAt: timestamp("created_at").notNull().defaultNow(),
  updatedAt: timestamp("updated_at").$onUpdateFn(() => new Date()),
}, (t) => ({
  unq: unique().on(t.userId, t.courseId, t.coursePackId),
}));

const userCourseProgress = mysqlTable("user_course_progress", {
  id: text("id").primaryKey().$defaultFn(() => require("@paralleldrive/cuid2").createId()),
  userId: text("user_id").notNull(),
  coursePackId: text("course_pack_id").notNull(),
  courseId: text("course_id").notNull(),
  statementIndex: int("statement_index").notNull(),
  createdAt: timestamp("created_at").notNull().defaultNow(),
  updatedAt: timestamp("updated_at").$onUpdateFn(() => new Date()),
}, (t) => ({
  unq: unique().on(t.userId, t.coursePackId),
}));

const membership = mysqlTable("memberships", {
  id: text("id").primaryKey().$defaultFn(() => require("@paralleldrive/cuid2").createId()),
  userId: text("user_id").notNull(),
  startDate: timestamp("start_date").notNull(),
  endDate: timestamp("end_date").notNull(),
  isActive: boolean("isActive").default(true),
  createdAt: timestamp("created_at").notNull().defaultNow(),
  updatedAt: timestamp("updated_at").$onUpdateFn(() => new Date()),
  type: text("type").notNull().default("regular"),
});

const masteredElements = mysqlTable("mastered_elements", {
  id: text("id").primaryKey().$defaultFn(() => require("@paralleldrive/cuid2").createId()),
  userId: text("user_id").notNull(),
  content: json("content").notNull(),
  masteredAt: timestamp("mastered_at").defaultNow(),
});

const userLearningActivities = mysqlTable("user_learning_activities", {
  id: text("id").primaryKey().$defaultFn(() => require("@paralleldrive/cuid2").createId()),
  userId: text("user_id").notNull(),
  date: date("date").notNull(),
  activityType: text("activity_type").notNull(),
  courseId: text("course_id"),
  duration: int("duration").notNull(),
  metadata: json("metadata"),
  createdAt: timestamp("created_at").notNull().defaultNow(),
  updatedAt: timestamp("updated_at").$onUpdateFn(() => new Date()),
}, (t) => ({
  unq: unique().on(t.userId, t.date, t.activityType),
}));

const userLearnRecord = mysqlTable("user_learn_record", {
  id: text("id").primaryKey().$defaultFn(() => require("@paralleldrive/cuid2").createId()),
  userId: text("user_id").notNull(),
  count: int("count").notNull().default(0),
  day: date("day").notNull(),
  createdAt: timestamp("created_at").notNull().defaultNow(),
  updatedAt: timestamp("updated_at").$onUpdateFn(() => new Date()),
}, (t) => ({
  unq: unique().on(t.userId, t.day),
}));

module.exports = {
  coursePack,
  course,
  statement,
  courseHistory,
  userCourseProgress,
  membership,
  masteredElements,
  userLearningActivities,
  userLearnRecord,
};
