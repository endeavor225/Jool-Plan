var ping = require('ping');

var host = 'googlesss.cm'

ping.promise.probe(host).then(function (res) {
        console.log(res.alive); 
    })