angular
    .module('app')
    .factory('WebSocketService', ['$window', '$rootScope', '$websocket', function ($window, $rootScope, $websocket) {

        // Open a WebSocket connection
        //using for local test... 
        //var dataStream = $websocket('ws://169.254.214.88:8000/ws');
        var dataStream = $websocket('ws://' + $window.location.host + '/ws');
    
        $rootScope.spectrumData={};

        dataStream.onMessage(function (message) {
            $rootScope.spectrumData.spectrum = JSON.parse(message.data);
            $rootScope.$emit('spectrum-event');
        });

        return {
            data: $rootScope.spectrumData,

            subscribe: function (scope, callback) {
                var handler = $rootScope.$on('spectrum-event', callback);
                scope.$on('$destroy', handler);
            }
        };

    }]);