/* eslint-disable @typescript-eslint/no-unused-vars */
import ProListTransfer from '@/components/ProListTransfer';
import { TableTransferProps } from '@/components/TableTransfer';
import useLoadingState from '@/hooks/useLoadingState';
import ApiSystemRole from '@/services/system/ApiSystemRole';
import { ReloadOutlined } from '@ant-design/icons';
import { useLocation } from '@umijs/max';
import { App, Button, Descriptions, Divider, Tag, TransferProps } from 'antd';
import { useEffect, useMemo, useState } from 'react';

/** 给角色授权用户 */
const SystemRoleAuthUser = () => {
  const { search } = useLocation();
  const { message } = App.useApp();
  const loadingState = useLoadingState();
  const [allocatedList, setAllocatedList] = useState<SysUserDTO[]>([]);
  const [unallocatedList, setUnallocatedList] = useState<SysUserDTO[]>([]);
  const [role, setRole] = useState<Partial<SysRoleDTO>>({});

  // @ts-ignore
  const roleId: string = useMemo(() => {
    const searchParams = new URLSearchParams(search);
    if (searchParams.has('roleId')) {
      return searchParams.get('roleId') as string;
    }
    return undefined;
  }, [search]);

  const dataSource = useMemo(() => {
    return [...(allocatedList || []), ...(unallocatedList || [])].map(
      (user) => {
        return {
          key: user.userId,
          title: user.userName,
          tag: user.userName,
          description: user.deptName,
          ...user,
        };
      },
    );
  }, [allocatedList, unallocatedList]);

  const checkedKeys = useMemo(() => {
    return allocatedList.map((item) => item.userId);
  }, [allocatedList]);

  const [targetKeys, setTargetKeys] = useState<TransferProps['targetKeys']>([]);
  const loadData = async () => {
    loadingState.begin();
    try {
      const [_allocatedList, _unallocatedList, _role] = await Promise.all([
        ApiSystemRole.authUserAllocatedList({
          roleId,
          pageSize: 10_000,
          total: 0,
        }),
        ApiSystemRole.authUserUnallocatedList({
          roleId,
          pageSize: 10_000,
          total: 0,
        }),
        ApiSystemRole.detail(roleId),
      ]);
      setAllocatedList(_allocatedList.rows);
      setUnallocatedList(_unallocatedList.rows);
      setRole(_role);
    } catch (e) {
    } finally {
      loadingState.end();
    }
  };

  const filterOption = (input: string, item: SysUserDTO) =>
    item.userName?.includes(input) || item.nickName?.includes(input);

  const onChange: TableTransferProps['onChange'] = async (
    nextTargetKeys,
    direction,
    moveKeys,
  ) => {
    setTargetKeys(nextTargetKeys);
    loadingState.begin();

    try {
      if (direction === 'left') {
        // 取消授权
        const hide = message.info('取消授权中');
        await ApiSystemRole.authUserCancelAll(roleId, moveKeys as string[]);
        hide();
        message.success('取消授权成功');
      } else {
        // 授权
        const hide = message.info('授权中');
        await ApiSystemRole.authUserSelectAll(roleId, moveKeys as string[]);
        hide();
        message.success('授权成功');
      }
      await loadData();
    } catch (e) {
    } finally {
      loadingState.end();
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  useEffect(() => {
    setTargetKeys(checkedKeys);
  }, [checkedKeys]);

  return (
    <div>
      <Descriptions
        title={<Divider orientation={'left'}>角色信息</Divider>}
        extra={
          <Button
            icon={<ReloadOutlined />}
            type={'primary'}
            onClick={loadData}
            loading={loadingState.value}
          >
            刷新
          </Button>
        }
      >
        <Descriptions.Item label={'角色编号'}>{role.roleId}</Descriptions.Item>
        <Descriptions.Item label={'角色名称'}>
          {role.roleName}
        </Descriptions.Item>
        <Descriptions.Item label={'角色权限'}>{role.roleKey}</Descriptions.Item>
      </Descriptions>

      <Divider orientation={'left'}>授权用户</Divider>
      <ProListTransfer
        dataSource={dataSource}
        targetKeys={targetKeys}
        showSearch={true}
        showSelectAll={true}
        onChange={onChange}
        filterOption={filterOption}
        loading={loadingState.value}
        metas={{
          title: {
            render: (text, record) => {
              return `${record.userName} / ${record.nickName}`;
            },
          },
          subTitle: {
            render: (text, record) => (
              <Tag color={'cyan'}>{record.deptName}</Tag>
            ),
          },
        }}
      />
    </div>
  );
};

export default SystemRoleAuthUser;
