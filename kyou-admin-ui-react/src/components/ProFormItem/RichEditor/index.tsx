import '@wangeditor/editor/dist/css/style.css'; // 引入 css

import { IDomEditor, IToolbarConfig } from '@wangeditor/core';
import { Editor, Toolbar } from '@wangeditor/editor-for-react';
import React, { useEffect, useState } from 'react';

type RichEditorProps = {
  value?: string;
  onChange?: (value: string) => void;
};

const RichEditor: React.FC<RichEditorProps> = ({ value, onChange }) => {
  const [editor, setEditor] = useState<IDomEditor | null>(null); // TS 语法
  const toolbarConfig: Partial<IToolbarConfig> = {}; // TS 语法

  // 及时销毁 editor ，重要！
  useEffect(() => {
    return () => {
      if (editor === null) {
        return;
      }
      editor.destroy();
      setEditor(null);
    };
  }, [editor]);

  return (
    <div style={{ border: '1px solid #ccc', zIndex: 100 }}>
      <Toolbar
        editor={editor}
        defaultConfig={toolbarConfig}
        mode="default"
        style={{ borderBottom: '1px solid #ccc' }}
      />
      <Editor
        onCreated={setEditor}
        mode="default"
        value={value}
        onChange={(editor) => onChange?.(editor.getHtml())}
        style={{ height: '500px', overflowY: 'hidden' }}
      />
    </div>
  );
};

export default RichEditor;
