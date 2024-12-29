import { useModel } from '@umijs/max';
import { Button, Result } from 'antd';

const Exception404 = () => {
  const { closeActiveTab } = useModel('global');

  return (
    <Result
      status="403"
      title="403"
      style={{
        background: 'none',
      }}
      subTitle="对不起，您访问的页面不存在"
      extra={
        <Button type="primary" onClick={closeActiveTab}>
          关闭
        </Button>
      }
    />
  );
};

export default Exception404;
