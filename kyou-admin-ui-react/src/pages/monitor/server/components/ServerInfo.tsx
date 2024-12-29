import useIconFont from '@/hooks/useIconFont';
import { ProCard } from '@ant-design/pro-components';

interface ServerInfoProps {
  sys?: {
    computerName: string;
    osName: string;
    computerIp: string;
    osArch: string;
  };
  loading?: boolean;
}

const ServerInfo = ({ sys, loading }: ServerInfoProps) => {
  const IconFont = useIconFont();

  return (
    <ProCard
      loading={!sys || loading}
      title={
        <span>
          <IconFont type={'el-icon-ant-neicun'} className={'mr-2'} />
          服务器信息
        </span>
      }
      headerBordered
      hoverable
    >
      <table className={'w-full text-gray-600'}>
        <tbody>
          <tr className={'border-b leading-8'}>
            <td>服务器名称</td>
            <td>{sys?.computerName}</td>
            <td>操作系统</td>
            <td>{sys?.osName}</td>
          </tr>
          <tr className={'border-b leading-8'}>
            <td>服务器IP</td>
            <td>{sys?.computerIp}</td>
            <td>系统架构</td>
            <td>{sys?.osArch}</td>
          </tr>
        </tbody>
      </table>
    </ProCard>
  );
};

export default ServerInfo;
