import PermissionButton from '@/components/BizButtons/PermissionButton';
import useLoadingState from '@/hooks/useLoadingState';
import ApiSystemUser from '@/services/system/ApiSystemUser';
import { DownloadOutlined, ImportOutlined } from '@ant-design/icons';
import {
  ModalForm,
  ProFormCheckbox,
  ProFormUploadDragger,
} from '@ant-design/pro-components';
import { App, Button, UploadFile } from 'antd';
import { useState } from 'react';

const SystemUserImportModal = () => {
  const { modal } = App.useApp();
  const [fileList, setFileList] = useState<UploadFile[]>([]);

  const loadingState = useLoadingState();

  // noinspection JSUnusedGlobalSymbols
  return (
    <ModalForm
      title={'导入用户数据'}
      trigger={
        <PermissionButton
          icon={<ImportOutlined />}
          permissionsRequired={['system:user:import']}
        >
          导入
        </PermissionButton>
      }
      layout={'horizontal'}
      submitter={{
        render: (_props, defaultButtons) => {
          return [
            <Button
              loading={loadingState.value}
              key="downloadTemplate"
              icon={<DownloadOutlined />}
              onClick={() =>
                loadingState.wrap(() => ApiSystemUser.importTemplate())
              }
            >
              下载模板
            </Button>,
            ...defaultButtons,
          ];
        },
      }}
      onFinish={async (values) => {
        const file: any = fileList[0];

        try {
          await ApiSystemUser.import(file, values.updateSupport);
        } catch (e) {
          const { message: _message, msg } = e as any;
          const errMsg = _message || msg;
          // 拆分错误信息, 根据 <br/> 进行拆分成数组
          let errArr: string[] = errMsg.split('<br/>');

          modal.warning({
            content: (
              <div>
                {errArr.map((errLine) => {
                  return <p key={errLine}>{errLine}</p>;
                })}
              </div>
            ),
          });
          return false;
        }

        return true;
      }}
    >
      <ProFormUploadDragger
        max={1}
        rules={[{ required: true, message: '请上传文件' }]}
        label=""
        accept={'.xls,.xlsx'}
        name="file"
        tooltip={'仅允许导入xls、xlsx格式文件'}
        fieldProps={{
          onRemove: (file) => {
            const index = fileList.indexOf(file);
            const newFileList = fileList.slice();
            newFileList.splice(index, 1);
            setFileList(newFileList);
          },
          beforeUpload: (file) => {
            setFileList([...fileList, file]);

            return false;
          },
          fileList,
        }}
      />
      <ProFormCheckbox
        name={'updateSupport'}
        label={'是否更新已经存在的用户数据'}
      />
    </ModalForm>
  );
};

export default SystemUserImportModal;
