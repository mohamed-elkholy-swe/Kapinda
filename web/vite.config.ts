import { defineConfig } from 'vite';
import { safeDiagnostic } from './src/diagnostics';
export default defineConfig({
  plugins: [{
    name: 'kapinda-development-logs',
    configureServer(server) {
      server.ws.on('qanater:diagnostic', data => {
        const entry = safeDiagnostic(data);
        if (!entry) return;
        const line = `[Kapinda] ${entry.event} ${JSON.stringify(entry.details)}`;
        if (entry.event.endsWith('.failed') || entry.event.endsWith('.missing')) server.config.logger.warn(line, { timestamp: true });
        else server.config.logger.info(line, { timestamp: true });
      });
    },
  }],
  server: { port: 5173, proxy: { '/api': { target: 'http://localhost:8080', changeOrigin: true } } },
});
