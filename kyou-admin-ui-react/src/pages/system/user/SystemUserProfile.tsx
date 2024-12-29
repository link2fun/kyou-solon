import WebConst from '@/constants/WebConst';
import ApiSystemUserProfile from '@/services/system/ApiSystemUserProfile';
import { LoadingOutlined, PlusOutlined } from '@ant-design/icons';
import {
  FormInstance,
  ProCard,
  ProForm,
  ProFormRadio,
  ProFormText,
} from '@ant-design/pro-components';
import { useModel } from '@umijs/max';
import { Col, GetProp, message, Row, Tabs, Upload, UploadProps } from 'antd';
import ImgCrop from 'antd-img-crop';
import { useEffect, useRef, useState } from 'react';

type FileType = Parameters<GetProp<UploadProps, 'beforeUpload'>>[0];

const getBase64 = (img: FileType, callback: (url: string) => void) => {
  const reader = new FileReader();
  reader.addEventListener('load', () => callback(reader.result as string));
  reader.readAsDataURL(img);
};

const beforeUpload = (file: FileType) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png';
  if (!isJpgOrPng) {
    message.error('You can only upload JPG/PNG file!');
  }
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isLt2M) {
    message.error('Image must smaller than 2MB!');
  }
  return isJpgOrPng && isLt2M;
};

const SystemUserProfile = () => {
  const [loading, setLoading] = useState(false);
  const { initialState, refresh } = useModel('@@initialState');
  const [imageUrl, setImageUrl] = useState<string>(
    initialState?.currentUser?.user?.avatar || '',
  );

  const formRef = useRef<FormInstance>();

  const handleChange: UploadProps['onChange'] = (info) => {
    if (info.file.status === 'uploading') {
      setLoading(true);
      return;
    }
    if (info.file.status === 'done') {
      // Get this url from response in real world.
      getBase64(info.file.originFileObj as FileType, (url) => {
        setLoading(false);
        setImageUrl(url);
      });
    }
  };
  const uploadButton = (
    <button style={{ border: 0, background: 'none' }} type="button">
      {loading ? <LoadingOutlined /> : <PlusOutlined />}
      <div style={{ marginTop: 8 }}>Upload</div>
    </button>
  );

  useEffect(() => {
    formRef.current?.setFieldsValue({ ...initialState?.currentUser?.user });
  }, []);

  return (
    <Row gutter={[8, 16]}>
      <Col span={24} md={12} lg={8}>
        <ProCard title={'个人信息'} headerBordered>
          <table className={'w-full'}>
            <thead>
              <tr className={'border-b leading-8 text-center w-full '}>
                <td colSpan={2} className={''}>
                  <div className={'pb-3'}>
                    <ImgCrop rotationSlider cropShape={'round'}>
                      <Upload
                        name="avatarfile"
                        listType="picture-circle"
                        className="avatar-uploader"
                        showUploadList={false}
                        action={`${WebConst.API_PREFIX}/system/user/profile/avatar`}
                        beforeUpload={beforeUpload}
                        onChange={handleChange}
                      >
                        {imageUrl ? (
                          <img
                            src={imageUrl}
                            alt="avatar"
                            className={'rounded rounded-full'}
                          />
                        ) : (
                          uploadButton
                        )}
                      </Upload>
                    </ImgCrop>
                  </div>
                </td>
              </tr>
            </thead>
            <tbody>
              <tr className={'border-b leading-8'}>
                <td>用户名称</td>
                <td>{initialState?.currentUser?.user?.userName}</td>
              </tr>
              <tr className={'border-b leading-8'}>
                <td>手机号码</td>
                <td>{initialState?.currentUser?.user?.phonenumber}</td>
              </tr>
              <tr className={'border-b leading-8'}>
                <td>用户邮箱</td>
                <td>{initialState?.currentUser?.user?.email}</td>
              </tr>
              <tr className={'border-b leading-8'}>
                <td>所属部门</td>
                <td>{initialState?.currentUser?.user?.deptName}</td>
              </tr>
              <tr className={'border-b leading-8'}>
                <td>所属角色</td>
                <td>{initialState?.currentUser?.roles}</td>
              </tr>
              <tr className={'border-b leading-8'}>
                <td>创建日期</td>
                <td></td>
              </tr>
            </tbody>
          </table>
        </ProCard>
      </Col>
      <Col span={24} md={12} lg={16}>
        <ProCard title={'基本资料'} headerBordered>
          <Tabs
            destroyInactiveTabPane
            onChange={(key) => {
              if (key === 'baseInfo') {
                setTimeout(() => {
                  formRef.current?.setFieldsValue({
                    ...initialState?.currentUser?.user,
                  });
                }, 100);
              }
            }}
            items={[
              {
                key: 'baseInfo',
                label: '基本资料',
                children: (
                  <>
                    <ProForm
                      formRef={formRef}
                      // initialValues={{ ...initialState?.currentUser?.user }}
                      onFinish={async (values) => {
                        if (!initialState) {
                          return;
                        }
                        await ApiSystemUserProfile.updateProfile({
                          userId: initialState?.currentUser?.user?.userId,
                          ...values,
                        });
                        message.success('修改成功');
                        // const _user: CurrentUser =
                        //   (await initialState?.fetchUserInfo?.()) as CurrentUser;
                        // 更新到 initialState 中
                        // setInitialState?.({
                        //   ...initialState,
                        //   currentUser: _user,
                        // });
                        refresh().then(() => {
                          formRef.current?.setFieldsValue({
                            ...initialState?.currentUser?.user,
                          });
                        });
                      }}
                      onReset={() => {
                        formRef.current?.setFieldsValue({
                          ...initialState?.currentUser?.user,
                        });
                      }}
                    >
                      <ProFormText
                        label={'用户昵称'}
                        name={'nickName'}
                        rules={[{ required: true }]}
                      />
                      <ProFormText
                        label={'手机号'}
                        name={'phonenumber'}
                        rules={[{ required: true }]}
                      />
                      <ProFormText
                        label={'邮箱'}
                        name={'email'}
                        rules={[{ required: true }]}
                      />
                      <ProFormRadio.Group
                        label={'性别'}
                        name={'sex'}
                        options={[
                          {
                            label: '男',
                            value: '0',
                          },
                          {
                            label: '女',
                            value: '1',
                          },
                          {
                            label: '未知',
                            value: '2',
                          },
                        ]}
                      />
                    </ProForm>
                  </>
                ),
              },
              {
                key: '2',
                label: '修改密码',
                children: (
                  <ProForm
                    layout={'horizontal'}
                    formRef={formRef}
                    onFinish={async (values: any) => {
                      if (!initialState) {
                        return;
                      }
                      const { oldPassword, newPassword } = values;
                      await ApiSystemUserProfile.updatePwd(
                        oldPassword,
                        newPassword,
                      );
                      message.success('修改成功');
                    }}
                  >
                    <ProFormText.Password
                      label={'旧密码'}
                      name={'oldPassword'}
                      rules={[{ required: true }]}
                    />
                    <ProFormText.Password
                      label={'新密码'}
                      name={'newPassword'}
                      rules={[{ required: true }]}
                    />
                    <ProFormText.Password
                      label={'新密码确认'}
                      name={'newPasswordConfirmed'}
                      rules={[
                        { required: true },
                        {
                          // 判断和新密码是否一致
                          validator: async (rule, value) => {
                            if (
                              value !==
                              formRef.current?.getFieldValue('newPassword')
                            ) {
                              throw new Error('两次密码不一致');
                            } else {
                              return true;
                            }
                          },
                        },
                      ]}
                    />
                  </ProForm>
                ),
              },
            ]}
          />
        </ProCard>
      </Col>
    </Row>
  );
};

export default SystemUserProfile;
