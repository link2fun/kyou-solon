import useLoadingState from '@/hooks/useLoadingState';
import TreeTool from '@/utils/TreeTool';
import React, { useEffect, useMemo, useState } from 'react';

interface TreeProps {
  id: string;
  label?: string;
  children?: string;
}

interface ManualControlProps {
  onAfterDataLoad?: () => void;
  treeProps: TreeProps;
}

/**
 * 自定义钩子：用于手动控制树形结构的展开和折叠
 * 这个钩子提供了一系列的状态和方法，用于管理树形结构的展开状态、搜索功能和数据加载
 * @param props 控制属性，包含回调函数和树形结构的配置
 * @returns 返回用于控制树形结构的各种状态和方法
 */
const useManualTreeExpandControl = (props: ManualControlProps) => {
  // 定义ID和子节点字段名，如果没有提供则使用默认值
  const idField = props?.treeProps?.id || 'id';
  const childrenField = props?.treeProps.children || 'children';

  // 搜索值状态，用于存储当前的搜索关键词
  const [searchValue, setSearchValue] = useState('');

  // 加载状态，默认为加载中
  const loadingState = useLoadingState({ defaultValue: true });

  // 展开状态相关的状态变量
  const [isExpandAll, setIsExpandAll] = useState(false); // 是否全部展开
  const [expandedKeys, setExpandedKeys] = useState<React.Key[]>([]); // 当前展开的节点keys
  const [autoExpandParent, setAutoExpandParent] = useState(true); // 是否自动展开父节点

  // 树形数据状态，用于存储整个树形结构的数据
  const [treeData, setTreeData] = useState<any[]>([]);

  // 计算列表数据，将树形结构扁平化为一维数组
  const listData = useMemo(() => {
    const _dataList: any[] = [];

    // 递归函数，用于扁平化树形结构
    const loop = (data: any[]) => {
      data.forEach((item) => {
        _dataList.push({ ...item });
        if (item[childrenField]) {
          loop(item[childrenField]);
        }
      });
    };

    // 调用递归函数，扁平化树形结构
    loop(treeData);
    return _dataList;
  }, [treeData]);

  // 数据加载后的回调，当listData变化时触发
  useEffect(() => {
    props?.onAfterDataLoad?.();
  }, [listData]);

  /**
   * 缓存树形数据
   * 这个函数用于更新树形数据并结束加载状态
   * @param data 新的树形数据
   */
  const cacheTreeData = (data: any[]) => {
    setTreeData(data);
    loadingState.end();
  };

  /**
   * 处理树的展开事件
   * 当用户手动展开或折叠节点时调用此函数
   * @param newExpandedKeys 新的展开键数组
   */
  const onExpand = (newExpandedKeys: React.Key[]) => {
    setExpandedKeys(newExpandedKeys);
    setAutoExpandParent(false);
  };

  /**
   * 处理表格的展开事件
   * 适用于表格形式的树形结构，处理单个节点的展开和折叠
   * @param expanded 是否展开
   * @param record 当前操作的记录
   */
  const onTableExpand = (expanded: boolean, record: any) => {
    if (expanded) {
      onExpand([...expandedKeys, record[idField]]);
    } else {
      onExpand(expandedKeys.filter((key) => key !== record[idField]));
    }
  };

  /**
   * 展开并自动展开父节点
   * 通常用于搜索操作后，展开匹配的节点及其所有父节点
   * @param newExpandedKeys 新的展开键数组
   */
  const onExpandAutoParent = (newExpandedKeys: React.Key[]) => {
    setExpandedKeys(newExpandedKeys);
    setAutoExpandParent(true);
  };

  /**
   * 展开全部节点
   * 将所有节点的key添加到expandedKeys中，实现全部展开
   */
  const expandAll = () => {
    const keys = listData.map((item) => item[idField]);
    setExpandedKeys(keys);
    setIsExpandAll(true);
    setAutoExpandParent(true);
  };

  /**
   * 折叠全部节点
   * 清空expandedKeys，实现全部折叠
   */
  const collapseAll = () => {
    setExpandedKeys([]);
    setAutoExpandParent(false);
    setIsExpandAll(false);
  };

  /**
   * 切换展开/折叠全部
   * 根据当前的isExpandAll状态决定是展开还是折叠
   */
  const toggleExpandAll = () => {
    if (isExpandAll) {
      collapseAll();
    } else {
      expandAll();
    }
  };

  /**
   * 处理搜索变化
   * 当搜索关键词变化时，找出匹配的节点并展开它们的父节点
   * @param value 搜索关键词
   */
  const onSearchChange = (value: string) => {
    const newExpandedKeys = listData
      .map((item) => {
        if (item.label.indexOf(value) > -1) {
          return TreeTool.getParentKey(item.id, treeData);
        }
        return null;
      })
      .filter((item, i, self): item is React.Key => !!(item && self.indexOf(item) === i));
    setSearchValue(value);
    onExpandAutoParent(newExpandedKeys);
  };

  // 返回所有需要的状态和方法
  return {
    idField, // ID字段名
    childrenField, // 子节点字段名
    treeData, // 树形数据
    listData, // 扁平化的列表数据
    isExpandAll, // 是否全部展开的标志
    expandedKeys, // 当前展开的节点keys
    autoExpandParent, // 是否自动展开父节点
    searchValue, // 当前的搜索关键词
    loadingState, // 加载状态
    cacheTreeData, // 缓存树形数据的方法
    onExpand, // 处理展开事件的方法
    onExpandAutoParent, // 自动展开父节点的方法
    expandAll, // 展开所有节点的方法
    collapseAll, // 折叠所有节点的方法
    toggleExpandAll, // 切换全部展开/折叠的方法
    onTableExpand, // 处理表格展开的方法
    onSearchChange, // 处理搜索变化的方法
  };
};

export default useManualTreeExpandControl;
