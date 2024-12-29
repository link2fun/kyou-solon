import { useThrottle } from 'ahooks';
import { App } from 'antd';
import { useState } from 'react';

type LoadingState = {
  defaultValue: boolean;
  wait?: number;
};

// useLoadingState returnType
export type UseLoadingStateReturnType = ReturnType<typeof useLoadingState>;

/** 加载状态 */
const useLoadingState = ({ defaultValue, wait }: LoadingState = { defaultValue: false, wait: 100 }) => {
  const [_loading, setLoading] = useState<boolean>(defaultValue);
  const [loaded, setLoaded] = useState<boolean>(false);
  const { message } = App.useApp();

  const loading = useThrottle(_loading, { wait: wait });

  const beginLoading = () => {
    setLoading(true);
  };

  const endLoading = () => {
    setLoading(false);
    setLoaded(true);
  };
  const wrapLoading = async (action: () => Promise<any>) => {
    setLoading(true);
    let result: any;
    try {
      result = await action();
    } catch (e) {
    } finally {
      setLoaded(true);
      setLoading(false);
    }
    return result;
  };

  type WrapLoadingWithMessage =
    | {
        action: () => Promise<any>;
        loadingMessage?: string;
        successMessage?: string;
        failMessage?: string;
      }
    | (() => Promise<any>);
  const wrapLoadingWithMessage = async (props: WrapLoadingWithMessage) => {
    if (typeof props === 'function') {
      return wrapLoading(props);
    }

    setLoading(true);
    let hide;
    if (props.loadingMessage) {
      hide = message.loading(props.loadingMessage);
    }
    let result: any;
    try {
      result = await props.action();
      if (props.successMessage) {
        message.success(props.successMessage);
      }
    } catch (e) {
      message.error(props.failMessage);
    } finally {
      setLoading(false);
      if (hide) {
        hide();
      }
    }
    return result;
  };

  return {
    value: loading,
    loaded: loaded,
    begin: beginLoading,
    end: endLoading,
    wrap: wrapLoadingWithMessage,
  };
};

export default useLoadingState;
