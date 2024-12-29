export const DEFAULT_NAME = 'Umi Max';

/**
 * 返回状态码
 */
export class HttpStatus {
  /** 操作成功 */
  static readonly SUCCESS = 200;

  /** 对象创建成功 */
  static readonly CREATED = 201;

  /** 请求已经被接受 */
  static readonly ACCEPTED = 202;

  /** 操作已经执行成功，但是没有返回数据 */
  static readonly NO_CONTENT = 204;

  /** 资源已被移除 */
  static readonly MOVED_PERM = 301;

  /** 重定向 */
  static readonly SEE_OTHER = 303;

  /** 资源没有被修改 */
  static readonly NOT_MODIFIED = 304;

  /** 参数列表错误（缺少，格式不匹配） */
  static readonly BAD_REQUEST = 400;

  /** 未授权 */
  static readonly UNAUTHORIZED = 401;

  /** 访问受限，授权过期 */
  static readonly FORBIDDEN = 403;

  /** 资源，服务未找到 */
  static readonly NOT_FOUND = 404;

  /** 不允许的http方法 */
  static readonly BAD_METHOD = 405;

  /** 资源冲突，或者资源被锁 */
  static readonly CONFLICT = 409;

  /** 不支持的数据，媒体类型 */
  static readonly UNSUPPORTED_TYPE = 415;

  /** 系统内部错误 */
  static readonly ERROR = 500;

  /** 接口未实现 */
  static readonly NOT_IMPLEMENTED = 501;

  /** 系统警告消息 */
  static readonly WARN = 601;
}
