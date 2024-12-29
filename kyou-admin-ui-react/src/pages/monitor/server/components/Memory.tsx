import useIconFont from '@/hooks/useIconFont';
import { ProCard } from '@ant-design/pro-components';

interface MemoryProps {
  mem?: any;
  jvm?: any;
  loading?: boolean;
}

const Memory = ({ mem, jvm, loading }: MemoryProps) => {
  const IconFont = useIconFont();

  return (
    <ProCard
      loading={!mem || !jvm || loading}
      title={
        <span>
          <IconFont type={'el-icon-ant-neicun'} className={'mr-2'} />
          内存
        </span>
      }
      headerBordered
      hoverable
    >
      <table className={'w-full text-gray-600'}>
        <thead>
          <tr className={'text-left border-b leading-8'}>
            <th>属性</th>
            <th>内存</th>
            <th>JVM</th>
          </tr>
        </thead>
        <tbody>
          <tr className={'border-b leading-8'}>
            <td>总内存</td>
            <td>{mem?.total}G</td>
            <td>{jvm?.total}M</td>
          </tr>
          <tr className={'border-b leading-8'}>
            <td>已用内存</td>
            <td>{mem?.used}G</td>
            <td>{jvm?.used}M</td>
          </tr>
          <tr className={'border-b leading-8'}>
            <td>剩余内存</td>
            <td>{mem?.free}G</td>
            <td>{jvm?.free}M</td>
          </tr>
          <tr className={'border-b leading-8'}>
            <td>使用率</td>
            <td
              className={`${
                mem?.usage > 80
                  ? 'text-red-600'
                  : mem?.usage > 60
                  ? 'text-orange-600'
                  : 'text-green-600'
              }`}
            >
              {mem?.usage}%
            </td>
            <td
              className={`${
                jvm?.usage > 80
                  ? 'text-red-600'
                  : jvm?.usage > 60
                  ? 'text-orange-600'
                  : 'text-green-600'
              }`}
            >
              {jvm?.usage}%
            </td>
          </tr>
        </tbody>
      </table>
    </ProCard>
  );
};

export default Memory;
