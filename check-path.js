const { execSync } = require('child_process');
console.log('npm root -g:', execSync('npm root -g').toString().trim());
console.log('npm bin -g:', execSync('npm bin -g').toString().trim());
