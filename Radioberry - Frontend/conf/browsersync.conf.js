const conf = require('./gulp.conf');


var httpProxy = require('http-proxy');

var proxy = httpProxy.createProxyServer({
   target: 'http://169.254.214.88:8000/'
 });

var proxyMiddleware = function(req, res, next) {
   if (req.url.indexOf('radioberry') != -1) {
     proxy.web(req, res);
   } else {
     next();
   }
};


module.exports = function () {
  return {
    server: {
      middleware: proxyMiddleware,
      baseDir: [
        conf.paths.tmp,
        conf.paths.src
      ],
      routes: {
        '/bower_components': 'bower_components'
      }
    },
    open: false
  };
};
