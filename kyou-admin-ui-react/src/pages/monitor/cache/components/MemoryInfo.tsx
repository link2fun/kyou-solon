import { JavaOutlined } from '@ant-design/icons';
import { Gauge } from '@ant-design/plots';
import { ProCard } from '@ant-design/pro-components';

interface MemoryInfoProps {
  used?: string | undefined;
}

const MemoryInfo = ({ used }: MemoryInfoProps) => {
  const config = {
    data: {
      target: parseInt(used || '0') || 0,
      total: 1000,
    },
    legend: false,
    style: {
      textContent: () => `${used}`,
    },
  };
  return (
    <ProCard
      title={
        <span>
          <JavaOutlined className={'mr-2'} />
          内存信息
        </span>
      }
      headerBordered
      hoverable
    >
      <Gauge {...config} />
    </ProCard>
  );
};

export default MemoryInfo;
