import UserTool from '@/utils/UserTool';
import { App, Button, message, Upload } from 'antd';
import { ButtonProps } from 'antd/es/button';
import React, { memo } from 'react';

interface UploadButtonProps {
  /** 上传字段名称 */
  name?: string;
  /** 按钮属性 */
  buttonProps: { btnText: string } & ButtonProps;
  /** 上传地址 */
  action: string;
  /** 附带的data */
  data?: Record<string, any>;
  /** 上传完成后的回调 */
  onUploadSuccess?: (response: any) => void;
  /** 上传失败后的回调 */
  onUploadError?: (error: any) => void;
  /** 加载状态 */
  loading?: boolean;
  /** 设置加载状态 */
  setLoading?: (loading: boolean) => void;
  /** 请求头 */
  headers?: Record<string, any>;
}

const UploadButton: React.FC<UploadButtonProps> = memo(
  ({
    name = 'file',
    buttonProps,
    loading = false,
    setLoading = () => {},
    action,
    data = {},
    onUploadSuccess = () => {},
    onUploadError = () => {},
    headers = {},
  }) => {
    const { modal } = App.useApp();

    const handleChange = (info: any) => {
      const { file } = info;
      const { response, status } = file;

      if (status === 'uploading') {
        setLoading(true);
        return;
      }

      if (status === 'error') {
        message.error('文件上传出错，请手动刷新页面(按 Ctrl/Command + F5 )后重试');
        setLoading(false);
        return;
      }

      if (status !== 'done') {
        return;
      }

      const { status: apiStatus, message: _message, msg } = response;

      if (apiStatus !== 200) {
        const errMsg = _message || msg;
        const errArr = errMsg.split('<br/>');

        modal.warning({
          content: (
            <div>
              {errArr.map((errLine: string) => (
                <p key={errLine}>{errLine}</p>
              ))}
            </div>
          ),
        });
        onUploadError(response);
        setLoading(false);
        return;
      }

      setLoading(false);
      onUploadSuccess(response);
    };

    return (
      <Upload
        name={name}
        disabled={loading}
        showUploadList={false}
        action={action}
        headers={{
          passkey: UserTool.getUserToken(),
          ...headers,
        }}
        data={data}
        onChange={handleChange}
      >
        <Button {...buttonProps} loading={loading}>
          {buttonProps.btnText}
        </Button>
      </Upload>
    );
  },
);

UploadButton.displayName = 'UploadButton';

export default UploadButton;
