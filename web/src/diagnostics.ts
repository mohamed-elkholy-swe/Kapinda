// Keep diagnostics to fixed event names and explicitly allowed metadata.
// Never log errors wholesale: Firebase errors can contain account data or tokens.
export const diagnosticEvents = [
  'auth.config.ready', 'auth.config.missing', 'auth.google.started',
  'auth.google.succeeded', 'auth.google.failed', 'auth.email.started',
  'auth.email.succeeded', 'auth.email.failed', 'auth.state.signed-in',
  'auth.state.signed-out', 'auth.state.failed', 'account.sync.started',
  'account.sync.succeeded', 'account.sync.failed', 'api.started',
  'api.succeeded', 'api.failed', 'ui.action.failed',
] as const;
export type DiagnosticEvent = typeof diagnosticEvents[number];
type Details = { code?: string; status?: number; elapsedMs?: number; method?: string; path?: string };

export function errorCode(error: unknown): string | undefined {
  if (typeof error !== 'object' || error === null || !('code' in error)) return undefined;
  const code = error.code;
  return typeof code === 'string' && /^auth\/[a-z-]+$/.test(code) ? code : undefined;
}

// Also used by the Vite receiver so browser input is never printed unchecked.
export function safeDiagnostic(input: unknown): { event: DiagnosticEvent; details: Details } | null {
  if (typeof input !== 'object' || input === null) return null;
  const record = input as Record<string, unknown>;
  if (!diagnosticEvents.includes(record.event as DiagnosticEvent)) return null;
  const source = typeof record.details === 'object' && record.details !== null ? record.details as Record<string, unknown> : {};
  const details: Details = {};
  if (typeof source.code === 'string' && /^auth\/[a-z-]+$/.test(source.code)) details.code = source.code;
  if (typeof source.status === 'number' && Number.isInteger(source.status) && source.status >= 100 && source.status <= 599) details.status = source.status;
  if (typeof source.elapsedMs === 'number' && Number.isFinite(source.elapsedMs) && source.elapsedMs >= 0) details.elapsedMs = Math.round(source.elapsedMs);
  if (typeof source.method === 'string' && /^(GET|POST|PUT|PATCH|DELETE)$/.test(source.method)) details.method = source.method;
  if (typeof source.path === 'string' && /^\/[a-zA-Z0-9/_:-]{1,120}$/.test(source.path)) details.path = source.path;
  return { event: record.event as DiagnosticEvent, details };
}

export function diagnostic(event: DiagnosticEvent, details: Details = {}) {
  if (!import.meta.env.DEV) return;
  const entry = safeDiagnostic({ event, details })!;
  const write = event.endsWith('.failed') || event.endsWith('.missing') ? console.warn : console.info;
  write(`[Kapinda] ${new Date().toISOString()} ${event}`, entry.details);
  import.meta.hot?.send('qanater:diagnostic', entry);
}
