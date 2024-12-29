import useLoadingState from '@/hooks/useLoadingState';
import Cpu from '@/pages/monitor/server/components/Cpu';
import DiskInfo from '@/pages/monitor/server/components/DiskInfo';
import JvmInfo from '@/pages/monitor/server/components/JvmInfo';
import Memory from '@/pages/monitor/server/components/Memory';
import ServerInfo from '@/pages/monitor/server/components/ServerInfo';
import ApiMonitorServer from '@/services/monitor/ApiMonitorServer';
import { useInterval } from 'ahooks';
import { Col, Row } from 'antd';
import { useState } from 'react';

export interface DiskUsage {
  dirName: string;
  free: string;
  sysTypeName: string;
  total: string;
  typeName: string;
  usage: number;
  used: string;
}

interface ServerAllInfo {
  cpu: any;
  jvm: any;
  mem: any;
  sys: any;
  sysFiles: DiskUsage[];
}

const MonitorServerIndex = () => {
  const loadingState = useLoadingState();
  const [data, setData] = useState<ServerAllInfo>({
    cpu: undefined,
    jvm: undefined,
    mem: undefined,
    sys: undefined,
    sysFiles: [],
  });

  const loadData = async () => {
    await loadingState.wrap(async () => {
      const res = await ApiMonitorServer.serverInfo();
      setData(res);
    });
  };

  useInterval(
    () => {
      loadData();
    },
    10000,
    { immediate: true },
  );

  return (
    <Row gutter={[16, 24]}>
      <Col span={24} md={12}>
        <Cpu cpu={data.cpu} loading={!loadingState.loaded} />
      </Col>
      <Col span={24} md={12}>
        <Memory mem={data.mem} jvm={data.jvm} loading={!loadingState.loaded} />
      </Col>

      <Col span={24}>
        <ServerInfo sys={data.sys} loading={!loadingState.loaded} />
      </Col>
      <Col span={24}>
        <JvmInfo jvm={data.jvm} sys={data.sys} loading={!loadingState.loaded} />
      </Col>

      <Col span={24}>
        <DiskInfo files={data.sysFiles} loading={!loadingState.loaded} />
      </Col>
    </Row>
  );
};

export default MonitorServerIndex;
