import { createApp } from './app';
import { env } from './config/env';
import { attachPixelRelay } from './pixelRelay';

const app = createApp();

const server = app.listen(env.port, () => {
  console.log(`Server listening on port ${env.port}`);
});

attachPixelRelay(server, env.pixelUpstreamUrl);

for (const signal of ['SIGINT', 'SIGTERM'] as const) {
  process.on(signal, () => {
    server.close(() => {
      process.exit(0);
    });
  });
}
