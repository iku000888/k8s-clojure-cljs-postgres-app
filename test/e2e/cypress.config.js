const { defineConfig } = require("cypress");

module.exports = (on, config) => {
  on('before:browser:launch', (browser = {}, launchOptions) => {
    if (browser.family === 'chromium' && browser.name !== 'electron') {
      launchOptions.args.push('--disk-cache-dir=/dev/null')
      launchOptions.args.push('--disk-cache-size=1')

      return launchOptions
    }
  })
}

module.exports = defineConfig({
  e2e: {
    setupNodeEvents(on, config) {
      // implement node event listeners here
    },
  },
});
