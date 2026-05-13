import { Injectable, Logger } from "@nestjs/common";

/**
 * 测试模式：LogtoService stub，不发起任何真实网络请求。
 * 所有方法返回空/mock 数据，避免依赖 Logto 服务。
 */
@Injectable()
export class LogtoService {
  private readonly logger = new Logger(LogtoService.name);

  /** mock logtoApi，user 相关调用返回 stub 数据 */
  logtoApi = {
    get: async (path: string) => {
      this.logger.debug(`[DEV STUB] logtoApi.get ${path}`);
      // 返回一个最小的用户对象
      return {
        data: {
          id: "dev-user-001",
          username: "dev-user",
          avatar: "",
          primaryEmail: "dev@example.com",
        },
      };
    },
    patch: async (path: string, data: any) => {
      this.logger.debug(`[DEV STUB] logtoApi.patch ${path}`);
      return { data };
    },
  };

  async fetchToken(options: { resource?: string; scope?: string } = {}): Promise<string> {
    return "dev-stub-token";
  }
}
