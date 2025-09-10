import React, { useEffect, useState } from 'react';

const TextFilePreview = ({ url }: { url: string }) => {
  const [content, setContent] = useState<string | null>(null);

  useEffect(() => {
    fetch(url)
      .then(res => res.text())
      .then(setContent)
      .catch(() => setContent('Failed to load file.'));
  }, [url]);

  return (
    <pre className='whitespace-pre-wrap bg-gray-100 p-4 rounded overflow-auto'>
      {content || 'Loading...'}
    </pre>
  );
};

export default TextFilePreview;
