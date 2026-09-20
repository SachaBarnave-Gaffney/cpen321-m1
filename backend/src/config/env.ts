import 'dotenv/config';

const rawPort = process.env.PORT;
const port =
  rawPort === undefined || rawPort === ''
    ? 3000
    : Number.parseInt(rawPort, 10);

if (Number.isNaN(port) || port < 1 || port > 65535) {
  throw new Error(`Invalid PORT: ${rawPort}`);
}

export const env = {
  port,
  // Your own name, returned by GET /api/name
  firstName: process.env.MY_FIRST_NAME ?? 'First',
  lastName: process.env.MY_LAST_NAME ?? 'Last',
  // Public IP of the VM (set this to your static IP in .env)
  serverPublicIp: process.env.SERVER_PUBLIC_IP ?? '',
  // Course-provided pixel WebSocket server
  pixelUpstreamUrl: process.env.PIXEL_UPSTREAM_URL ?? 'wss://8.229.22.124',
} as const;
