const { io } = require('socket.io-client');

const s = io('http://127.0.0.1:3000', { reconnection: false, timeout: 8000 });

s.on('connect', () => {
  console.log('CONNECTED as', s.id);
  s.emit('start_listener', 7777);
});

s.on('listeners_update', (ports) => {
  console.log('LISTENERS_NOW:', JSON.stringify(ports));
  setTimeout(() => process.exit(0), 600);
});

s.on('connect_error', (e) => {
  console.error('CONNECT_ERROR:', e.message);
  process.exit(1);
});

setTimeout(() => {
  console.error('TIMEOUT_WAITING_LISTENERS_UPDATE');
  process.exit(2);
}, 12000);
