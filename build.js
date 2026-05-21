const fs = require('fs');
const path = require('path');

// Create www directory
if (!fs.existsSync('www')) fs.mkdirSync('www');

// Copy index.html
fs.copyFileSync('index.html', path.join('www', 'index.html'));

console.log('Build complete: www/index.html ready');
