import EditModalForm from '@/components/EditModalForm';
import useProFormSelectDictRequest from '@/hooks/useProFormSelectDictRequest';
import { EditModalProps } from '@/typing';
import {
  ProFormRadio,
  ProFormSelect,
  ProFormText,
} from '@ant-design/pro-components';
import React from 'react';

const MonitorJobEditModal: React.FC<EditModalProps> = (props) => {
  const sysJobStatusSelectRequest = useProFormSelectDictRequest({
    typeCode: 'sys_job_status',
  });

  return (
    <EditModalForm {...props}>
      <ProFormText
        label={'任务名称'}
        name={'jobName'}
        rules={[{ required: true }]}
      />
      <ProFormSelect
        label={'任务组名'}
        name={'jobGroup'}
        rules={[{ required: true }]}
        request={useProFormSelectDictRequest({ typeCode: 'sys_job_group' })}
      />

      <ProFormText
        label={'调用目标字符串'}
        name={'invokeTarget'}
        rules={[{ required: true }]}
        tooltip={
          <div>
            Bean调用示例：ryTask.ryParams(&quot;ry&quot;)
            <br />
            Class类调用示例：com.ruoyi.quartz.task.RyTask.ryParams(&quot;ry&quot;)
            <br />
            参数说明：支持字符串，布尔类型，长整型，浮点型，整型
          </div>
        }
      />

      <ProFormText label={'cron表达式'} name={'cronExpression'} />

      {props.initData.jobId && (
        <ProFormSelect
          label={'状态'}
          name={'status'}
          rules={[{ required: true }]}
          request={sysJobStatusSelectRequest}
        />
      )}
      <ProFormRadio.Group
        label={'执行策略'}
        name={'misfirePolicy'}
        valueEnum={{
          '1': { text: '立即执行' },
          '2': { text: '执行一次' },
          '3': { text: '放弃执行' },
        }}
      />
      <ProFormRadio.Group
        label={'是否并发'}
        name={'concurrent'}
        valueEnum={{
          '0': { text: '允许' },
          '1': { text: '禁止' },
        }}
      />
    </EditModalForm>
  );
};

export default MonitorJobEditModal;
