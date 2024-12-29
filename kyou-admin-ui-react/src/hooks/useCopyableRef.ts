import copyToClipboard from 'copy-to-clipboard'; // You'll need this package: `yarn add copy-to-clipboard`.
import { useRef, useState } from 'react';

const useCopyableRef = <T extends HTMLElement = HTMLElement>(
  delay: number = 4 * 1000, // You may want to change this to 4000, or define SECONDS somewhere in your application.
) => {
  const ref = useRef<T>(null);

  const [isCopied, setCopied] = useState(false);
  const [textContent, setTextContent] = useState('');
  const copy = () => {
    if (isCopied) return;

    if (!ref.current) throw new Error('Ref is nil.');

    copyToClipboard(ref.current.textContent || '');
    setTextContent(ref.current.textContent || '');

    setCopied(true);
    setTimeout(() => setCopied(false), delay);
  };

  return { ref, isCopied, copy, textContent };
};

export default useCopyableRef;
