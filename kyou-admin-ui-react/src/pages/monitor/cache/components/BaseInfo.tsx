import { JavaOutlined } from '@ant-design/icons';
import { ProCard } from '@ant-design/pro-components';

interface BaseInfoProps {
  info?: Record<string, string> | undefined;
  dbSize?: number | undefined;
}

const BaseInfo = ({ info, dbSize }: BaseInfoProps) => {
  return (
    <ProCard
      title={
        <span>
          <JavaOutlined className={'mr-2'} />
          基本信息
        </span>
      }
      headerBordered
      hoverable
    >
      <table className={'w-full text-gray-600'}>
        <tbody>
          <tr className={'border-b leading-8'}>
            <td className={'min-w-20'}>Redis版本</td>
            <td>{info?.redis_version}</td>
            <td>运行模式</td>
            <td>{info?.redis_mode === 'standalone' ? '单机' : '集群'}</td>
            <td>端口</td>
            <td>{info?.tcp_port}</td>
            <td>客户端数</td>
            <td>{info?.connected_clients}</td>
          </tr>
          <tr className={'border-b leading-8'}>
            <td>运行时间(天)</td>
            <td>{info?.uptime_in_days}</td>
            <td>使用内存</td>
            <td>{info?.used_memory_human}</td>
            <td>使用CPU</td>
            <td>
              {parseFloat(info?.used_cpu_user_children || '0').toFixed(2)}
            </td>
            <td>内存配置</td>
            <td>{info?.maxmemory_human}</td>
          </tr>
          <tr className={'border-b leading-8'}>
            <td>AOF是否开启</td>
            <td>{info?.aof_enabled === '0' ? '否' : '是'}</td>
            <td>RDB是否成功</td>
            <td>{info?.rdb_last_bgsave_status}</td>
            <td>Key数量</td>
            <td>{dbSize || 0}</td>
            <td>网络入口/出口</td>
            <td>
              {info?.instantaneous_input_kbps}kbps /{' '}
              {info?.instantaneous_output_kbps}kbps
            </td>
          </tr>
        </tbody>
      </table>
    </ProCard>
  );
};

export default BaseInfo;
