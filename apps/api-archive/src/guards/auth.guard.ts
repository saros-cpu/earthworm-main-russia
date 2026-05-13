import {
  CanActivate,
  ExecutionContext,
  Injectable,
  SetMetadata,
} from "@nestjs/common";

export const UncheckAuth = () => SetMetadata("uncheck", true);
export const Permissions = (...permissions: string[]) => SetMetadata("permissions", permissions);

/**
 * 测试模式：跳过 JWT/Logto 验证，固定注入 userId = "dev-user-001"
 * 如需恢复真实认证，将此文件替换为原 auth.guard.ts 内容
 */
@Injectable()
export class AuthGuard implements CanActivate {
  async canActivate(context: ExecutionContext): Promise<boolean> {
    const request = context.switchToHttp().getRequest();
    // 测试模式：始终放行，注入固定 userId
    request["userId"] = "dev-user-001";
    return true;
  }
}
