import { JavaOutlined } from '@ant-design/icons';
import { ProCard } from '@ant-design/pro-components';

interface JvmInfoProps {
  jvm?: {
    name: string;
    version: string;
    startTime: string;
    runTime: string;
    home: string;
    inputArgs: string;
  };
  sys?: {
    userDir: string;
  };
  loading?: boolean;
}

const JvmInfo = ({ jvm, sys, loading }: JvmInfoProps) => {
  return (
    <ProCard
      loading={!jvm || !sys || loading}
      title={
        <span>
          <JavaOutlined className={'mr-2'} />
          Java虚拟机信息
        </span>
      }
      headerBordered
      hoverable
    >
      <table className={'w-full text-gray-600'}>
        <tbody>
          <tr className={'border-b leading-8'}>
            <td className={'min-w-20'}>Java名称</td>
            <td>{jvm?.name}</td>
            <td>Java版本</td>
            <td>{jvm?.version}</td>
          </tr>
          <tr className={'border-b leading-8'}>
            <td>启动时间</td>
            <td>{jvm?.startTime}</td>
            <td>运行时长</td>
            <td>{jvm?.runTime}</td>
          </tr>
          <tr className={'border-b leading-8'}>
            <td>安装路径</td>
            <td colSpan={3}>{jvm?.home}</td>
          </tr>
          <tr className={'border-b leading-8'}>
            <td>项目路径</td>
            <td colSpan={3}>{sys?.userDir}</td>
          </tr>
          <tr className={'border-b leading-8 break-all'}>
            <td>运行参数</td>
            <td colSpan={3}>{jvm?.inputArgs}</td>
          </tr>
        </tbody>
      </table>
    </ProCard>
  );
};

export default JvmInfo;
