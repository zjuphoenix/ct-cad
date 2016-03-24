/**
 * Created by wuhaitao on 2016/3/23.
 */
angular.module('records',['ui.router', 'tm.pagination', 'auth'])
    /*.constant('BASE_URI', 'http://localhost:8080')*/
    .constant('BASE_URI', '')
    .constant('HOST', 'http://localhost:8080')
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
        service.deleteRecordById = function(id){
            return $http.delete(BASE_URI+'/api/records/'+id);
        };
        service.report = function(report){
            return $http.post('/api/records/report', {
                'id': parseInt(report.id),
                'diagnosis': report.diagnosis,
                'username': report.username
            });
        };
    })
    .controller('RecordsCtrl', function($scope, $window, RecordsService, HOST){

        $scope.report = function(record){
            RecordsService.report(record)
                .then(function(result){
                    console.log(result.data);
                    /*$window.location.href = HOST+'/api/records/report/'+result.data;*/
                    $window.open(HOST+'/api/records/report/'+result.data);
                },function(error){
                    console.log(error);
                });
        };
        $scope.deleteRecord = function(recordId){
            RecordsService.deleteRecordById(recordId)
                .then(function(result){
                    console.log(result.data);
                    $scope.records.forEach(function(r, i) {
                        if(recordId === r.id)
                            $scope.records.splice(i, 1);
                    });
                    $scope.paginationConf.totalItems = $scope.paginationConf.totalItems-1;
                },function(error){
                    console.log(error);
                });
        };
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
    .controller('UserRecordsCtrl', function($scope, $stateParams, RecordsService, HOST){
        var username = $stateParams.username;

        $scope.report = function(record){
            RecordsService.report(record)
                .then(function(result){
                    console.log(result.data);
                    /*$window.location.href = HOST+'/api/records/report/'+result.data;*/
                    $window.open(HOST+'/api/records/report/'+result.data);
                },function(error){
                    console.log(error);
                });
        };
        $scope.deleteRecord = function(recordId){
            RecordsService.deleteRecordById(recordId)
                .then(function(result){
                    console.log(result.data);
                    $scope.records.forEach(function(r, i) {
                        if(recordId === r.id)
                            $scope.records.splice(i, 1);
                    });
                    $scope.paginationConf.totalItems = $scope.paginationConf.totalItems-1;
                },function(error){
                    console.log(error);
                });
        };
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