import BaseInfo from '@/pages/monitor/cache/components/BaseInfo';
import CommandInfo from '@/pages/monitor/cache/components/CommandInfo';
import MemoryInfo from '@/pages/monitor/cache/components/MemoryInfo';
import ApiMonitorCache from '@/services/monitor/ApiMonitorCache';
import { Col, Row } from 'antd';
import { useEffect, useState } from 'react';

export interface CommandStatus {
  name: string;
  value: string;
}

interface DataType {
  commandStats?: CommandStatus[];
  info?: Record<string, string>;
  dbSize?: number;
}

const MonitorCacheIndex = () => {
  const [data, setData] = useState<DataType>({});

  const loadData = () => {
    ApiMonitorCache.detail().then((res) => setData(res));
  };

  useEffect(() => {
    loadData();
  }, []);

  return (
    <Row gutter={[16, 24]}>
      <Col span={24}>
        <BaseInfo info={data?.info} dbSize={data.dbSize} />
      </Col>
      <Col span={24} lg={12}>
        <CommandInfo commandStatus={data?.commandStats || []} />
      </Col>
      <Col span={24} md={12}>
        <MemoryInfo used={data?.info?.used_memory_human} />
      </Col>
    </Row>
  );
};

export default MonitorCacheIndex;
