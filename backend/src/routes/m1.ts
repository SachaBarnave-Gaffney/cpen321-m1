import { Router } from 'express';

import { env } from '../config/env';
import { formatTimeWithGmtOffset } from '../utils/time';

export const m1Router = Router();

// GET /api/ip -> the server's public IP, plus the caller's IP as seen by the server
m1Router.get('/ip', (req, res) => {
  const clientIp = (req.ip ?? '').replace(/^::ffff:/, '');
  res.json({ serverIp: env.serverPublicIp, clientIp });
});

// GET /api/time -> server local time, "hh:mm:ss GMT+hh:mm"
m1Router.get('/time', (_req, res) => {
  res.json({ serverTime: formatTimeWithGmtOffset(new Date()) });
});

// GET /api/name -> my first and last name
m1Router.get('/name', (_req, res) => {
  res.json({ firstName: env.firstName, lastName: env.lastName });
});
