import { Module } from "@nestjs/common";
import { ScheduleModule } from "@nestjs/schedule";

import { CourseHistoryModule } from "../course-history/course-history.module";
import { CoursePackModule } from "../course-pack/course-pack.module";
import { CourseModule } from "../course/course.module";
import { CronJobModule } from "../cron-job/cron-job.module";
import { GlobalModule } from "../global/global.module";
import { LogtoModule } from "../logto/logto.module";
import { MasteredElementModule } from "../mastered-element/mastered-element.module";
import { MembershipModule } from "../membership/membership.module";
import { RankModule } from "../rank/rank.module";
import { ToolModule } from "../tool/tool.module";
import { UserCourseProgressModule } from "../user-course-progress/user-course-progress.module";
import { UserLearningActivityModule } from "../user-learning-activity/user-learning-activity.module";
import { UserModule } from "../user/user.module";

@Module({
  imports: [
    GlobalModule,
    LogtoModule,
    UserModule,
    CoursePackModule,
    CourseModule,
    UserCourseProgressModule,
    UserLearningActivityModule,
    ToolModule,
    RankModule,
    CronJobModule,
    CourseHistoryModule,
    MembershipModule,
    MasteredElementModule,
    ScheduleModule.forRoot(),
  ],
})
export class AppModule {}
