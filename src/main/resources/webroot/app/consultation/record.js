/**
 * Created by wuhaitao on 2016/3/10.
 */
angular.module('record',['ui.router', 'auth'])
    /*.constant('BASE_URI', 'http://localhost:8080')
    .constant('UPLOAD_FILE', 'http://localhost:8080/upload/')*/
    .constant('BASE_URI', '')
    .constant('UPLOAD_FILE', 'upload/')
    .config(function($stateProvider){
        $stateProvider.state('record', {
            url: '/record/:id',
            templateUrl: 'app/consultation/record.html',
            controller: 'RecordCtrl'
        });
    })
    .service('RecordService', function($http, BASE_URI){
        var service = this;
        service.getRecord = function(id){
            return $http.get(BASE_URI+'/api/consultation/record/'+id);
        };
    })
    .controller('RecordCtrl', function($scope, $state, $stateParams, RecordService, UPLOAD_FILE){
        var id = $stateParams.id;
        $scope.id = id;
        /*$scope.goArea = function(){
            $state.go('area', {image : $scope.file});
        }*/
        function getRecord(id){
            RecordService.getRecord(id)
                .then(function(result){
                    $scope.type = result.data.type;
                    $scope.file = UPLOAD_FILE+result.data.file;
                    $scope.diagnosis = result.data.diagnosis;
                },function(error){
                    console.log(error);
                });
            /*$scope.record = "混合性肝癌伴肝内多发子灶形成首先考虑，门脉主干及左右支栓塞，脾静脉近段栓塞；后腹膜多发淋巴结肿大。  肝硬化、腹水；门脉海绵样变性，胃底食管静脉丛曲张。";*/
        }

        getRecord(id);
    });