import React from 'react';

const TreeTool = {
  getParentKey: (key: React.Key, tree: any[]): React.Key | null => {
    let parentKey: React.Key | null = null;
    for (let i = 0; i < tree.length; i++) {
      const node = tree[i];
      if (node.children) {
        if (node.children.some((item: { id: React.Key }) => item.id === key)) {
          parentKey = node.id;
        } else {
          const nested = TreeTool.getParentKey(key, node.children);
          if (nested !== null) {
            parentKey = nested;
          }
        }
      }
    }
    return parentKey;
  },
};

export default TreeTool;
