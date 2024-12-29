import WebConst from '@/constants/WebConst';
import { get } from '@/utils/request';

const ApiMonitorServer = {
  serverInfo: () => get(`${WebConst.API_PREFIX}/monitor/server`),
};

export default ApiMonitorServer;
