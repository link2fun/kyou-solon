import { DiskUsage } from '@/pages/monitor/server';
import { HddOutlined } from '@ant-design/icons';
import { ProCard } from '@ant-design/pro-components';

interface DiskInfoProps {
  files?: DiskUsage[];
  loading?: boolean;
}

const DiskInfo = ({ files, loading }: DiskInfoProps) => {
  return (
    <ProCard
      loading={!files || loading}
      title={
        <span>
          <HddOutlined className={'mr-2'} />
          磁盘信息
        </span>
      }
      headerBordered
      hoverable
    >
      <table className={'w-full text-gray-600'}>
        <thead>
          <tr className={'text-left border-b leading-8'}>
            <th>盘符路径</th>
            <th>文件系统</th>
            <th>盘符类型</th>
            <th>总大小</th>
            <th>可用大小</th>
            <th>已用大小</th>
            <th>已用百分比</th>
          </tr>
        </thead>
        <tbody>
          {(files || []).map((file) => (
            <tr className={'border-b leading-8'} key={file.dirName}>
              <td>{file?.dirName}</td>
              <td>{file?.sysTypeName}</td>
              <td>{file?.typeName}</td>
              <td>{file?.total}</td>
              <td>{file?.free}</td>
              <td>{file?.used}</td>
              <td className={`${file?.usage > 80 ? 'text-red-500' : ''}`}>
                {file?.usage}%
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </ProCard>
  );
};

export default DiskInfo;
