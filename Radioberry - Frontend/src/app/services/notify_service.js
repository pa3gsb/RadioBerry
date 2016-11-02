angular
    .module('app')
    .factory('NotifyingService', ['$rootScope' ,function ($rootScope) {

        return {
            subscribe: function (scope, callback) {
                var handler = $rootScope.$on('notifying-service-event', callback);
                scope.$on('$destroy', handler);
            },

            notify: function () {
                $rootScope.$emit('notifying-service-event');
            },

        };

    }]);