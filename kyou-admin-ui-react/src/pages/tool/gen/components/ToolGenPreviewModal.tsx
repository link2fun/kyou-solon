import useCopyableRef from '@/hooks/useCopyableRef';
import { CheckOutlined, CopyOutlined } from '@ant-design/icons';
import { useGetState } from 'ahooks';
import { Button, Modal, Tabs } from 'antd';
import { memo, useEffect, useMemo } from 'react';
import Highlight from 'react-highlight';

interface ToolGenPreviewModalProps {
  open?: boolean;
  onCancel?: () => void;
  items: any[];
  onRefresh?: () => Promise<void>;
}

const ToolGenPreviewModal = memo(({ open, onCancel, items = [], onRefresh }: ToolGenPreviewModalProps) => {
  const [activeKey, setActiveKey, getActiveKey] = useGetState<string>('');

  // 从文件名提取代码语言类型
  const getMode = (filename: string): string => {
    if (filename.endsWith('xml.vm')) return 'xml';
    if (filename.endsWith('js.vm') || filename.endsWith('vue.vm')) return 'javascript';
    if (filename.endsWith('java.vm')) return 'java';
    if (filename.endsWith('sql.vm')) return 'sql';
    return '';
  };

  const { ref, isCopied, copy } = useCopyableRef<any>(2000);

  // 使用 useMemo 缓存 tabList
  const tabList = useMemo(() => {
    return items.map((item) => ({
      label: item.label,
      key: item.key,
      children: <></>,
    }));
  }, [items]);

  // 设置默认激活的 tab
  useEffect(() => {
    if (!activeKey && items.length > 0) {
      setActiveKey(items[0].key);
    }
  }, [activeKey, items, setActiveKey]);

  const handleCancel = () => {
    onCancel?.();
    setActiveKey('');
  };

  const activeContent = useMemo(() => {
    return items.find((s) => s.label === activeKey)?.content || '';
  }, [activeKey, items]);

  return (
    <Modal
      open={open}
      onCancel={handleCancel}
      title="预览"
      width="80vw"
      height={200}
      destroyOnClose
      okText="刷新"
      cancelText="关闭"
      okButtonProps={{ onClick: onRefresh }}
    >
      <Tabs
        items={tabList}
        onChange={setActiveKey}
        tabBarExtraContent={
          <Button type="link" disabled={isCopied} icon={isCopied ? <CheckOutlined /> : <CopyOutlined />} onClick={copy}>
            复制{isCopied ? '成功' : ''}
          </Button>
        }
      />

      <span ref={ref}>
        <Highlight className={`${getMode(getActiveKey() || '')} h-96`} key={activeKey}>
          {activeContent}
        </Highlight>
      </span>
    </Modal>
  );
});

ToolGenPreviewModal.displayName = 'ToolGenPreviewModal';

export default ToolGenPreviewModal;
