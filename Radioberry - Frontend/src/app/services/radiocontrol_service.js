angular
    .module('app')
    .service('RadiostateService', ['$http', function ($http) {

        var urlBase = '/api/xxxx';

        var radioState;

        if (localStorage.getItem("radiostate") == undefined) {
            radioState = { "frequency": parseInt(1008000), "frequencystep": String(100), "mode": parseInt(6), "agcmode": parseInt(0), "low": parseInt(-4000), "high": parseInt(4000), "agcgain": parseFloat(85), "att": parseInt(10) };
        } else {
            radioState = JSON.parse(localStorage.getItem("radiostate"));
        }


        this.getRadioState = function () {
            //return $http.get(urlBase);
            return radioState;
        };

        this.updateRadioState = function (radio) {
            //return $http.post(urlBase, radio);
            console.log('update radio state ');

            localStorage.setItem("radiostate", JSON.stringify(radio));

            return radio;
        };

        //return radioState;

    }]);