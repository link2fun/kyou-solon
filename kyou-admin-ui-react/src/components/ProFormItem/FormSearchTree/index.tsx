import useManualTreeExpandControl from '@/hooks/useManualTreeExpandControl';
import { FormItemProps } from '@/typing';
import { Input, Spin, Tree, TreeProps } from 'antd';
import React, { Key, useEffect, useMemo } from 'react';

const { Search } = Input;

type TreePropsConfig = {
  id: string;
  label: string;
  children: string;
};

type SearchTreeProps = FormItemProps & {
  checkable?: boolean;
  checkStrictly?: boolean;
  action?: string;
  loadData: () => Promise<any[]>;
  placeholder?: string;
  treeProps?: TreePropsConfig;
};

const SearchTree: React.FC<SearchTreeProps> = (props) => {
  const idField = props?.treeProps?.id || 'id';
  const labelField = props?.treeProps?.label || 'label';
  const childrenField = props?.treeProps?.children || 'children';

  const expandControl = useManualTreeExpandControl({
    treeProps: { id: idField, label: labelField, children: childrenField },
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
        const strTitle = item[labelField] as string;
        const index = strTitle.indexOf(expandControl.searchValue);
        const beforeStr = strTitle.substring(0, index);
        const afterStr = strTitle.slice(index + expandControl.searchValue.length);
        const title =
          index > -1 ? (
            <span key={item[idField]}>
              {beforeStr}
              <span className="bg-amber-300">{expandControl.searchValue}</span>
              {afterStr}
            </span>
          ) : (
            <span key={item[idField]}>{strTitle}</span>
          );
        if (item[childrenField as string]) {
          return {
            title,
            key: item[idField],
            children: loop(item[childrenField as string]),
          };
        }

        return {
          title,
          key: item[idField],
        };
      });

    let result = loop(expandControl.treeData);
    expandControl.onSearchChange(expandControl.searchValue);
    return result;
  }, [expandControl.searchValue, expandControl.treeData]);

  const dynamicProps: TreeProps = useMemo(() => {
    if (props.checkable) {
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
  }, [props.value, props.checkStrictly, props.checkable]);

  useEffect(() => {
    props.loadData().then((data: any[]) => {
      expandControl.cacheTreeData(data);
    });
  }, []);

  useEffect(() => {
    if (props.action === undefined || props.action === '') {
      return;
    }

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
      <Search style={{ marginBottom: 8 }} placeholder={props.placeholder || '请输入搜索内容'} onChange={onChange} />
      <Spin spinning={!expandControl.loadingState.loaded}>
        <Tree
          height={250}
          {...dynamicProps}
          checkStrictly={props.checkStrictly}
          onExpand={onExpand}
          expandedKeys={[...expandControl.expandedKeys]}
          autoExpandParent={expandControl.autoExpandParent}
          treeData={treeData}
          onSelect={(selectedKeys, info) => {
            if (props.checkable) {
              return;
            }
            // @ts-ignore
            const selectedId = info?.node?.title?.key;
            props?.onChange?.(selectedId || 0);
          }}
          onCheck={(checkedInfo) => {
            if (Array.isArray(checkedInfo)) {
              props?.onChange?.((checkedInfo || []) as Key[]);
              return;
            }

            const { checked } = checkedInfo;
            props?.onChange?.((checked || []) as Key[]);
          }}
        />
      </Spin>
    </div>
  );
};

export default SearchTree;
