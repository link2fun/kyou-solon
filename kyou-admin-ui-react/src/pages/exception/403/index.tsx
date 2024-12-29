import { useModel } from '@umijs/max';
import { Button, Result } from 'antd';

const Exception403 = () => {
  const { closeActiveTab } = useModel('global');

  return (
    <Result
      status="403"
      title="403"
      style={{
        background: 'none',
      }}
      subTitle="对不起, 您没有权限访问此页面"
      extra={
        <Button type="primary" onClick={closeActiveTab}>
          关闭
        </Button>
      }
    />
  );
};

export default Exception403;
