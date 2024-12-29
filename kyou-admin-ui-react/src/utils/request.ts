import { HttpStatus } from '@/constants';
import { request } from 'umi';

const get = (url: string, params: any = {}) => {
  return request(url, {
    params,
  }).then((resp: ExtendAjaxResult<any>) => {
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const { code, msg, data: DATA, success, ...REST } = resp;

    if (code === HttpStatus.SUCCESS) {
      // 判断是否有其他属性
      if (Object.keys(REST).length) {
        return resp;
      }
      return DATA || { msg };
    }
    return { msg };
  });
};

const postJSON = (
  url: string,
  data: any = {},
  params: any = {},
  headers = {},
) => {
  return request(url, {
    method: 'POST',
    data,
    params,
    headers,
  }).then((resp: ExtendAjaxResult<any>) => {
    const { code, msg, data: DATA, ...REST } = resp;
    if (code === HttpStatus.SUCCESS) {
      if (REST) {
        // 存在其他属性
        return resp;
      }
      return DATA || { msg };
    }
    return { msg };
  });
};

const putJSON = (url: string, data: any = {}, params: any = {}) => {
  return request(url, {
    method: 'PUT',
    data,
    params,
  }).then((resp: ExtendAjaxResult<any>) => {
    const { code, msg, data: DATA } = resp;
    if (code === HttpStatus.SUCCESS) {
      return DATA || { msg };
    }
    return { msg };
  });
};

const deleteJSON = (url: string, data: any = {}, params: any = {}) => {
  return request(url, {
    method: 'DELETE',
    data,
    params,
  }).then((resp: ExtendAjaxResult<any>) => {
    const { code, msg, data: DATA } = resp;
    if (code === HttpStatus.SUCCESS) {
      return DATA || { msg };
    }
    return { msg };
  });
};

const postForm = (url: string, requestParams: any = {}) => {
  return request(url, {
    method: 'POST',
    params: requestParams,
  }).then((resp: ExtendAjaxResult<any>) => {
    const { code, msg, data: DATA } = resp;
    if (code === HttpStatus.SUCCESS) {
      return DATA || { msg };
    }
    return { msg };
  });
};

const putForm = (url: string, requestParams: any = {}) => {
  return request(url, {
    method: 'PUT',
    data: requestParams,
  }).then((resp: ExtendAjaxResult<any>) => {
    const { code, msg, data: DATA } = resp;
    if (code === HttpStatus.SUCCESS) {
      return DATA || { msg };
    }
    return { msg };
  });
};

const postFile = (url: string, formData: FormData, headers = {}) => {
  return request(url, {
    method: 'POST',
    data: formData,
    headers,
  }).then((resp: ExtendAjaxResult<any>) => {
    const { code, msg, data: DATA } = resp;
    if (code === HttpStatus.SUCCESS) {
      return DATA || { msg };
    }
    return { msg };
  });
};

/**
 * @description: 下载文件
 * @param url 下载链接
 * @param params 下载参数
 * @param filename 下载文件名
 * @param options 用来覆盖默认的请求配置
 */
const downloadFile = async (
  url: string,
  params: any,
  filename: string,
  options: { method?: 'GET' | 'POST' } = {},
) => {
  const res = await request(url, {
    method: 'GET',
    params,
    responseType: 'blob',
    ...options,
  });
  const link_url = window.URL.createObjectURL(new Blob([res]));
  const link = document.createElement('a');
  link.href = link_url;
  link.setAttribute('download', filename);
  document.body.appendChild(link);
  link.click();
  link.remove();
};

export {
  deleteJSON,
  downloadFile,
  get,
  postFile,
  postForm,
  postJSON,
  putForm,
  putJSON,
};
