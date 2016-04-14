var express = require('express');
var path = require('path');
var app = express();

app.use(express.static('../src/main/resources/public'));

app.get('/employees', function (req, res) {
    res.setHeader('Content-Type', 'application/json');
    var employees = [{"firstName":"John1", "lastName":"Doe"}, {"firstName":"Anna1", "lastName":"Smith"}, {"firstName":"Peter1", "lastName":"Jones"},
                {"firstName":"John2", "lastName":"Doe"}, {"firstName":"Anna2", "lastName":"Smith"}, {"firstName":"Peter2", "lastName":"Jones"},
                {"firstName":"John3", "lastName":"Doe"}, {"firstName":"Anna3", "lastName":"Smith"}, {"firstName":"Peter3", "lastName":"Jones"},
                {"firstName":"John4", "lastName":"Doe"}, {"firstName":"Anna4", "lastName":"Smith"}, {"firstName":"Peter4", "lastName":"Jones"},
                {"firstName":"John5", "lastName":"Doe"}, {"firstName":"Anna5", "lastName":"Smith"}, {"firstName":"Peter5", "lastName":"Jones"},
                {"firstName":"John6", "lastName":"Doe"}, {"firstName":"Anna6", "lastName":"Smith"}, {"firstName":"Peter6", "lastName":"Jones"}];
    res.send(JSON.stringify({ employees }));
});

app.listen(63342, function () {
    console.log('Example app listening on port 63342!');
});