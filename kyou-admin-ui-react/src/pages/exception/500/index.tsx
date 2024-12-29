import { useModel } from '@umijs/max';
import { Button, Result } from 'antd';

const Exception404 = () => {
  const { closeActiveTab } = useModel('global');

  return (
    <Result
      status="500"
      title="500"
      style={{
        background: 'none',
      }}
      subTitle="对不起，系统发生异常！"
      extra={
        <Button type="primary" onClick={closeActiveTab}>
          关闭
        </Button>
      }
    />
  );
};

export default Exception404;
