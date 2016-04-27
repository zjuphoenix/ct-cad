/**
 * Created by wuhaitao on 2016/3/28.
 */
angular.module('algorithm', ['ui.router'])
    /*.constant('BASE_URI', 'http://localhost:8080')*/
    .constant('BASE_URI', '')
    .config(function ($stateProvider) {
        $stateProvider
            .state('algorithm', {
                url: '/algorithm',
                templateUrl: 'app/algorithm/algorithm.html',
                controller: 'AlgorithmCtrl',
            })
            .state('detection_algorithm', {
                url: '/detection_algorithm',
                templateUrl: 'app/algorithm/detection_algorithm.html',
                controller: 'DetectionAlgorithmCtrl',
            });
    })
    .controller('AlgorithmCtrl', ['$scope', '$http', 'BASE_URI', function ($scope, $http, BASE_URI) {
        $scope.treeNum = 50;
        $scope.type = 1;
        $scope.result = '';
        $scope.submit = function() {
            $http.post(BASE_URI+'/api/ct/generateRecognitionModel', {
                "type":parseInt($scope.type),
                "treeNum":parseInt($scope.treeNum)
            }).then(function(result){
                $scope.result = result.data;
            },function(error){
                console.log(error);
            });
        };
    }])
    .controller('DetectionAlgorithmCtrl', ['$scope', '$http', 'BASE_URI', function ($scope, $http, BASE_URI) {
        $scope.treeNum = 50;
        $scope.type = 1;
        $scope.result = '';
        $scope.submit = function() {
            $http.post(BASE_URI+'/api/ct/generateDetectionModel', {
                "type":parseInt($scope.type),
                "treeNum":parseInt($scope.treeNum)
            }).then(function(result){
                $scope.result = result.data;
            },function(error){
                console.log(error);
            });
        };
    }]);