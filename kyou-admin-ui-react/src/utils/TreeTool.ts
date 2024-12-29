import React from 'react';

const TreeTool = {
  getParentKey: (key: React.Key, tree: any[]): React.Key => {
    let parentKey: React.Key;
    for (let i = 0; i < tree.length; i++) {
      const node = tree[i];
      if (node.children) {
        if (node.children.some((item: { id: React.Key }) => item.id === key)) {
          parentKey = node.id;
        } else if (TreeTool.getParentKey(key, node.children)) {
          parentKey = TreeTool.getParentKey(key, node.children);
        }
      }
    }
    return parentKey!;
  },
};

export default TreeTool;
