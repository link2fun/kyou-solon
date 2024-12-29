import useIconFont from '@/hooks/useIconFont';
import { ProCard } from '@ant-design/pro-components';

interface CpuProps {
  cpu?: any;
  loading?: boolean;
}

const Cpu = ({ cpu, loading }: CpuProps) => {
  const IconFont = useIconFont();

  return (
    <ProCard
      loading={!cpu || loading}
      title={
        <span>
          <IconFont type={'el-icon-ant-CPU1'} className={'mr-2'} /> CPU
        </span>
      }
      headerBordered
      hoverable
    >
      <table className={'w-full text-gray-600'}>
        <thead>
          <tr className={'text-left border-b leading-8'}>
            <th>键</th>
            <th>值</th>
          </tr>
        </thead>
        <tbody>
          <tr className={'border-b leading-8'}>
            <td>核心数</td>
            <td>{cpu?.cpuNum}</td>
          </tr>
          <tr className={'border-b leading-8'}>
            <td>用户使用率</td>
            <td>{cpu?.used}%</td>
          </tr>
          <tr className={'border-b leading-8'}>
            <td>系统使用率</td>
            <td>{cpu?.sys}%</td>
          </tr>
          <tr className={'border-b leading-8'}>
            <td>当前空闲率</td>
            <td
              className={`${
                cpu?.free < 20
                  ? 'text-red-600'
                  : cpu?.free < 40
                  ? 'text-orange-600'
                  : 'text-green-600'
              }`}
            >
              {cpu?.free}%
            </td>
          </tr>
        </tbody>
      </table>
    </ProCard>
  );
};

export default Cpu;
