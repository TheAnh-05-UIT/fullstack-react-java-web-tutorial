declare module 'react-quill' {
  import * as React from 'react';
  
  export interface ReactQuillProps {
    value?: string;
    defaultValue?: string;
    onChange?: (content: string, delta: any, source: string, editor: any) => void;
    theme?: string;
    modules?: Record<string, any>;
    formats?: string[];
    bounds?: string | HTMLElement;
    placeholder?: string;
    readOnly?: boolean;
    className?: string;
    style?: React.CSSProperties;
    children?: React.ReactNode;
  }
  
  export default class ReactQuill extends React.Component<ReactQuillProps> {}
}
