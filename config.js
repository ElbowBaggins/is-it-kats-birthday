// copy this file to config.js, and change the values below to real ones

module.exports = {

  development: {
    port: 80,
    visible: false,

    streaming: [
      ""
    ],

    log: {
      join: false,
      info: false,
      debug: false
    },

    ifttt: "",
    ifttt_debug: false
  },

  production: {
    port: 80,
    visible: false,

    streaming: [
      "http://localhost:3000"
    ],

    log: {
      join: false,
      info: false,
      debug: false
    },

    ifttt: "",
    ifttt_debug: false
  },
}