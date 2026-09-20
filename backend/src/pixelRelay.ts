import type { Server } from 'node:http';

import WebSocket, { WebSocketServer } from 'ws';

/**
 * WebSocket relay for M1 Button 2.
 * Each app client connecting to wss://<our-server>/pixels gets its own upstream
 * connection to the course server, so every client starts from a blank canvas.
 * Messages are forwarded immediately and unchanged (no batching, no reformatting).
 */
export function attachPixelRelay(server: Server, upstreamUrl: string): WebSocketServer {
  const wss = new WebSocketServer({ server, path: '/pixels' });

  wss.on('connection', (client) => {
    // The course server may use a self-signed certificate.
    const upstream = new WebSocket(upstreamUrl, { rejectUnauthorized: false });

    upstream.on('message', (data, isBinary) => {
      if (client.readyState === WebSocket.OPEN) {
        client.send(data, { binary: isBinary });
      }
    });

    upstream.on('error', (err) => {
      console.error('Upstream pixel socket error:', err.message);
      client.close(1011, 'Upstream error');
    });
    upstream.on('close', () => {
      if (client.readyState === WebSocket.OPEN) client.close(1000, 'Upstream closed');
    });

    client.on('close', () => upstream.close());
    client.on('error', () => upstream.close());
  });

  return wss;
}
