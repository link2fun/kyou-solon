import useManualTreeExpandControl from '@/hooks/useManualTreeExpandControl';
import ApiSystemUser from '@/services/system/ApiSystemUser';
import { FormItemProps } from '@/typing';
import { Input, Spin, Tree, TreeProps } from 'antd';
import React, { Key, useEffect, useMemo } from 'react';

const { Search } = Input;

type SystemDeptSearchTreeProps = FormItemProps & {
  checkable?: boolean;
  checkStrictly?: boolean;
  action?: string;
};

const SystemDeptSearchTree: React.FC<SystemDeptSearchTreeProps> = (props) => {
  const expandControl = useManualTreeExpandControl({
    treeProps: { id: 'id' },
  });

  const onExpand = (newExpandedKeys: React.Key[]) => {
    expandControl.onExpand(newExpandedKeys);
  };

  const onChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { value } = e.target;
    expandControl.onSearchChange(value);
  };

  const treeData = useMemo(() => {
    const loop = (data: any[]): any[] =>
      data.map((item) => {
        const strTitle = item.label as string;
        const index = strTitle.indexOf(expandControl.searchValue);
        const beforeStr = strTitle.substring(0, index);
        const afterStr = strTitle.slice(index + expandControl.searchValue.length);
        const title =
          index > -1 ? (
            <span key={item.id}>
              {beforeStr}
              <span className="bg-amber-300">{expandControl.searchValue}</span>
              {afterStr}
            </span>
          ) : (
            <span key={item.id}>{strTitle}</span>
          );
        if (item.children) {
          return { title, key: item.id, children: loop(item.children) };
        }

        return {
          title,
          key: item.id,
        };
      });

    let result = loop(expandControl.treeData);
    // 数据计算完成后, 触发一次展开节点
    expandControl.onSearchChange(expandControl.searchValue);
    return result;
  }, [expandControl.searchValue, expandControl.treeData]);

  const dynamicProps: TreeProps = useMemo(() => {
    // checkable 和 selectable 互斥
    if (props.checkable) {
      // 开启了勾选, 则不允许选择
      return {
        checkable: props.checkable,
        checkStrictly: props.checkStrictly === undefined ? true : props.checkStrictly,
        selectable: false,
        checkedKeys: props.value as Key[],
      };
    }

    return {
      activeKey: props.value as Key,
      checkable: false,
    };
  }, [props.value, props.checkStrictly]);

  useEffect(() => {
    ApiSystemUser.deptTree({}).then((data: any[]) => {
      expandControl.cacheTreeData(data);
    });
  }, []);

  useEffect(() => {
    if (props.action === undefined || props.action === '') {
      return;
    }
    console.log('action', props.action);
    if (props.action === 'checkAll') {
      const allKeys = expandControl.listData.map((item) => item[expandControl.idField]);
      props?.onChange?.(allKeys);
    } else if (props.action === 'uncheckAll') {
      props?.onChange?.([]);
    } else if (props.action === 'expandAll') {
      expandControl.expandAll();
    } else if (props.action === 'collapseAll') {
      expandControl.collapseAll();
    }
  }, [props.action]);

  return (
    <div>
      <Search style={{ marginBottom: 8 }} placeholder="请输入部门名称" onChange={onChange} />
      <Spin spinning={!expandControl.loadingState.loaded}>
        <Tree
          defaultExpandAll
          {...dynamicProps}
          checkStrictly={props.checkStrictly}
          onExpand={onExpand}
          expandedKeys={expandControl.expandedKeys}
          autoExpandParent={expandControl.autoExpandParent}
          treeData={treeData}
          onSelect={(selectedKeys, info) => {
            if (props.checkable) {
              // 开启了勾选, 使用勾选的结果
              return;
            }
            // @ts-ignore
            const selectedDeptId = info?.node?.title?.key;
            props?.onChange?.(selectedDeptId || 0);
          }}
          onCheck={(checkedInfo) => {
            if (Array.isArray(checkedInfo)) {
              props?.onChange?.((checkedInfo || []) as Key[]);
              console.log(checkedInfo);
              return;
            }

            const { checked } = checkedInfo;
            console.log(checked);
            props?.onChange?.((checked || []) as Key[]);
          }}
        />
      </Spin>
    </div>
  );
};

export default SystemDeptSearchTree;
