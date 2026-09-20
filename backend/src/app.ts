import express, { type Express } from 'express';

import { m1Router } from './routes/m1';

export function createApp(): Express {
  const app = express();

  // Caddy (HTTPS reverse proxy) runs on the same VM, so trust X-Forwarded-For
  // from localhost only. This makes req.ip the real client IP.
  app.set('trust proxy', 'loopback');

  app.get('/health', (_req, res) => {
    res.json({ status: 'ok' });
  });

  app.use('/api', m1Router);

  app.use((_req, res) => {
    res.status(404).json({ error: 'Not Found' });
  });

  return app;
}
