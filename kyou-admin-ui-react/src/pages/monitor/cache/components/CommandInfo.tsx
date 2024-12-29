import { CommandStatus } from '@/pages/monitor/cache';
import { JavaOutlined } from '@ant-design/icons';
import { Pie } from '@ant-design/plots';
import { ProCard } from '@ant-design/pro-components';

interface CommandInfoProps {
  commandStatus?: CommandStatus[] | undefined;
}

const CommandInfo = ({ commandStatus = [] }: CommandInfoProps) => {
  return (
    <ProCard
      loading={!commandStatus || commandStatus.length < 1}
      title={
        <span>
          <JavaOutlined className={'mr-2'} />
          命令统计
        </span>
      }
      headerBordered
      hoverable
    >
      <Pie
        radius={0.5}
        innerRadius={0.3}
        angleField="value"
        colorField="name"
        data={
          (commandStatus || []).map((item) => {
            return { name: item.name, value: parseInt(item.value) };
          }) as any
        }
        legend={false}
        label={{
          position: 'spider',
          text: (item: { name: string; value: number }) => {
            return `${item.name}: ${item.value}`;
          },
        }}
        tooltip={{
          title: 'name',
        }}
      />
    </ProCard>
  );
};

export default CommandInfo;
