import { useEffect, useRef, useState, type ImgHTMLAttributes, type ReactNode } from 'react';

type Props = Omit<ImgHTMLAttributes<HTMLImageElement>, 'src' | 'onError'> & {
  src: string;
  fallback: (retry: () => void) => ReactNode;
  pending?: ReactNode;
};

// A new source gets its own retry budget and cancels the previous source's timer.
export default function RetryImage(props: Props) {
  return <ImageAttempt key={props.src} {...props}/>;
}

function ImageAttempt({ src, fallback, pending, onLoad, ...props }: Props) {
  const [attempt, setAttempt] = useState(0);
  const [status, setStatus] = useState<'loading' | 'waiting' | 'loaded' | 'failed'>('loading');
  const handlingError = useRef(false);
  useEffect(() => {
    if (status !== 'waiting') return;
    const timer = window.setTimeout(() => {
      handlingError.current = false;
      setAttempt(value => value + 1);
      setStatus('loading');
    }, attempt === 0 ? 1000 : 3000);
    return () => window.clearTimeout(timer);
  }, [status, attempt]);

  function retry() {
    handlingError.current = false;
    setAttempt(0);
    setStatus('loading');
  }

  if (status === 'failed') return fallback(retry);
  return <>
    {status !== 'loaded' && pending}
    {status !== 'waiting' && <img {...props} key={attempt} src={src} onLoad={event => {
      setStatus('loaded');
      onLoad?.(event);
    }} onError={() => {
      if (handlingError.current) return;
      handlingError.current = true;
      setStatus(attempt < 2 ? 'waiting' : 'failed');
    }}/>}
  </>;
}
