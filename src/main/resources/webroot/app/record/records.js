/**
 * Created by wuhaitao on 2016/3/23.
 */
angular.module('records',['ui.router', 'tm.pagination', 'auth'])
    /*.constant('BASE_URI', 'http://localhost:8080')*/
    .constant('BASE_URI', '')
    .config(function($stateProvider, $urlRouterProvider){
        $stateProvider.state('records', {
                url: '/records',
                templateUrl: 'app/record/records.html',
                controller: 'RecordsCtrl'
            })
            .state('user_records', {
                url: '/records/:username',
                templateUrl: 'app/record/records.html',
                controller: 'UserRecordsCtrl'
            });
        $urlRouterProvider.otherwise('/records');
    })
    .service('RecordsService', function($http, BASE_URI){
        var service = this;
        service.getRecordsByPage = function(postData){
            return $http.post(BASE_URI+'/api/records', postData);
        };
    })
    .controller('RecordsCtrl', function($scope, RecordsService){

        var getRecordsByPage = function(){
            var postData = {
                'pageIndex': parseInt($scope.paginationConf.currentPage),
                'pageSize': parseInt($scope.paginationConf.itemsPerPage)
            }
            RecordsService.getRecordsByPage(postData)
                .then(function(result){
                    $scope.records = result.data.records;
                    $scope.paginationConf.totalItems = result.data.count;
                },function(error){
                    console.log(error);
                });
        };

        //配置分页基本参数
        $scope.paginationConf = {
            currentPage: 1,
            itemsPerPage: 5,
            pagesLength: 5
        };

        getRecordsByPage();

        $scope.$watch('paginationConf.currentPage + paginationConf.itemsPerPage', getRecordsByPage);
    })
    .controller('UserRecordsCtrl', function($scope, $stateParams, RecordsService){
        var username = $stateParams.username;
        var getRecordsByPage = function(){
            var postData = {
                'username': username,
                'pageIndex': parseInt($scope.paginationConf.currentPage),
                'pageSize': parseInt($scope.paginationConf.itemsPerPage)
            }
            RecordsService.getRecordsByPage(postData)
                .then(function(result){
                    $scope.records = result.data.records;
                    $scope.paginationConf.totalItems = result.data.count;
                },function(error){
                    console.log(error);
                });
        };

        //配置分页基本参数
        $scope.paginationConf = {
            currentPage: 1,
            itemsPerPage: 5,
            pagesLength: 5
        };

        getRecordsByPage();

        $scope.$watch('paginationConf.currentPage + paginationConf.itemsPerPage', getRecordsByPage);
    })
;