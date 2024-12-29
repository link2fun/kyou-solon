import ApiCommon from '@/services/common/ApiCommon';
import { useState } from 'react';

/** 使用图片验证码 */
const useCaptchaImage = () => {
  /** 定义state 存储验证码是否启用 */
  const [captchaEnabled, setCaptchaEnabled] = useState<boolean>(false);
  /** 定义state 存储验证码的uuid */
  const [uuid, setUuid] = useState<string>('');
  /** 定义state 存储验证码的图片的base64 eg: data:image/png;base64,**** */
  const [imgBase64, setImgBase64] = useState<string>('');

  /** 刷新图片验证码 */
  const refreshCaptcha = async () => {
    const {
      captchaEnabled: _captchaEnabled,
      uuid: _uuid,
      img: _img,
    } = await ApiCommon.captchaImage();

    setCaptchaEnabled(_captchaEnabled);
    setUuid(_uuid);
    setImgBase64(`data:image/png;base64,${_img}`);
  };

  return {
    refreshCaptcha,
    captchaEnabled,
    uuid,
    imgBase64,
  };
};

export default useCaptchaImage;
