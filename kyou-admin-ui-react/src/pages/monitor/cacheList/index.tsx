import PopconfirmButton from '@/components/BizButtons/PopconfirmButton';
import useCacheListControl from '@/pages/monitor/cacheList/hooks/useCacheListControl';
import { DeleteOutlined, ReloadOutlined } from '@ant-design/icons';
import {
  ProCard,
  ProForm,
  ProFormText,
  ProFormTextArea,
  ProTable,
} from '@ant-design/pro-components';
import { Button, Col, Row, Spin } from 'antd';

const MonitorCacheListIndex = () => {
  const control = useCacheListControl();

  return (
    <Row gutter={[16, 24]}>
      <Col span={24} md={8}>
        <ProCard
          headerBordered
          title={'缓存列表'}
          hoverable
          loading={control.cacheNameLoading}
          extra={
            <Button
              icon={<ReloadOutlined />}
              type={'link'}
              onClick={control.loadCacheNameList}
            ></Button>
          }
        >
          <ProTable
            search={false}
            pagination={false}
            options={false}
            dataSource={control.cacheNameList}
            onRow={(record) => {
              return {
                onClick: async () => {
                  console.log(record);
                  await control.onClickCacheName(record);
                },
              };
            }}
            rowKey={'cacheName'}
            columns={[
              {
                title: '',
                renderText: (text, record, index) => `${index + 1}`,
              },
              {
                title: '缓存名称',
                dataIndex: 'cacheName',
              },
              {
                title: '备注',
                dataIndex: 'remark',
              },
              {
                title: '操作',
                key: 'operations',
                render: (_, record) => (
                  <PopconfirmButton
                    buttonText={''}
                    tooltip={'删除该缓存空间下所有缓存值'}
                    buttonProps={{
                      type: 'link',
                      danger: true,
                      icon: <DeleteOutlined />,
                    }}
                    title={'删除缓存空间可能会造成异常, 确认操作?'}
                    onConfirm={() => control.clearCacheName(record)}
                  />
                ),
              },
            ]}
          />
        </ProCard>
      </Col>
      <Col span={24} md={8}>
        <ProCard
          headerBordered
          title={'键名列表'}
          hoverable
          loading={control.cacheKeyLoading}
          extra={
            <Button
              icon={<ReloadOutlined />}
              type={'link'}
              onClick={control.loadCacheKeyList}
            ></Button>
          }
        >
          <ProTable
            dataSource={control.cacheKeyList}
            search={false}
            pagination={false}
            options={false}
            rowKey={'key'}
            onRow={(record) => {
              return {
                onClick: async () => {
                  console.log(record);
                  await control.onClickCacheKey(record);
                },
              };
            }}
            columns={[
              {
                title: '',
                renderText: (text, record, index) => `${index + 1}`,
              },
              { title: '键名', dataIndex: 'key' },
              {
                title: '操作',
                key: 'operations',
                render: (_, record) => (
                  <PopconfirmButton
                    buttonText={''}
                    tooltip={`删除 ${record.key} 缓存值`}
                    buttonProps={{
                      type: 'link',
                      danger: true,
                      icon: <DeleteOutlined />,
                    }}
                    title={`删除 ${record.key} 缓存值, 可能造成业务系统异常, 确认操作?`}
                    onConfirm={() => control.clearCacheKey(record)}
                  />
                ),
              },
            ]}
          />
        </ProCard>
      </Col>
      <Col span={24} md={8}>
        <ProCard headerBordered title={'缓存内容'} hoverable>
          <Spin spinning={control.cacheContentLoading}>
            <ProForm submitter={false} formRef={control.contentFormRef}>
              <ProFormText
                name={'cacheName'}
                label={'缓存名称'}
                allowClear={false}
              />
              <ProFormText
                name={'cacheKey'}
                label={'缓存键名'}
                allowClear={false}
              />
              <ProFormText name={'remark'} label={'备注'} allowClear={false} />
              <ProFormTextArea
                name={'cacheValue'}
                label={'缓存值'}
                allowClear={false}
              />
            </ProForm>
          </Spin>
        </ProCard>
      </Col>
    </Row>
  );
};

export default MonitorCacheListIndex;
