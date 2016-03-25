/**
 * Created by wuhaitao on 2016/2/25.
 */
var userModule = angular.module('ct', ['ui.router']);
userModule.constant('ENDPOINT_URI', '/api')
    .constant('BASE_URI', '')
    .constant('UPLOAD_FILE', 'upload/')
    .config(function ($stateProvider) {
        $stateProvider.state('ct', {
            url: '/ct',
            params: {
                'id':1,
                'diagnosis':'',
                'username':''
            },
            templateUrl: 'app/ct/ctimages.html',
            controller: 'CTImageCtrl'
        })
        .state('diagnosis', {
            url: '/ct/diagnosis',
            params: {
                'id':1,
                'type':1,
                'file':'',
                'diagnosis':'',
                'recordId':1
            },
            templateUrl: 'app/ct/diagnosis.html',
            controller: 'DiagnosisCtrl'
        });
    })
    .service('CTImageService', function($http, ENDPOINT_URI){
        var service = this;
        service.getCTImages = function(recordId){
            return $http.get(ENDPOINT_URI+'/ct/data/'+recordId);
        };

        service.deleteCT = function(id){
            return $http.delete(ENDPOINT_URI+'/ct/'+id);
        };

        service.getCTImagesByPage = function(postData){
            return $http.post(ENDPOINT_URI+'/ct', postData);
        };
    })
    .controller('CTImageCtrl', function($scope, $state, $http, $stateParams, CTImageService, BASE_URI){
        $scope.id = $stateParams.id;
        $scope.diagnosis = $stateParams.diagnosis;
        $scope.username = $stateParams.username;

        function getCTImages(consultationId){
            CTImageService.getCTImages(consultationId)
                .then(function(result){
                    $scope.ctImages = result.data.ct;
                },function(error){
                    console.log(error);
                });
        }

        var getCTImagesByPage = function(){
            var postData = {
                'recordId': parseInt($scope.id),
                'pageIndex': parseInt($scope.paginationConf.currentPage),
                'pageSize': parseInt($scope.paginationConf.itemsPerPage)
            }
            CTImageService.getCTImagesByPage(postData)
                .then(function(result){
                    $scope.ctImages = result.data.ct;
                    $scope.paginationConf.totalItems = result.data.count;
                    console.log($scope.ctImages);
                    console.log($scope.paginationConf.totalItems);
                },function(error){
                    console.log(error);
                });
        };

        $scope.isLiver = function(type){
            if(type == '肝脏'){
                return true;
            }
            else{
                return false;
            }
        };

        $scope.isLung = function(type){
            if(type == '肺部'){
                return true;
            }
            else{
                return false;
            }
        };

        /*更新病历诊断结果*/
        $scope.updateDiagnosisResult = '';
        $scope.updateDiagnosis = function(){
            $http.put(BASE_URI+'/api/records/diagnosis', {
                'id': parseInt($scope.id),
                'diagnosis': $scope.diagnosis
            }).then(function(result){
                $scope.updateDiagnosisResult = result.data;
            },function(error){
                console.log(error);
            });
        }

        /*进入辅助诊断界面*/
        $scope.goCAD = function(ctImage){
            console.log(ctImage);
            $state.go('cad', {
                'id':ctImage.id,
                'type':ctImage.type,
                'file':ctImage.file,
                'diagnosis':ctImage.diagnosis,
                'recordId':ctImage.recordId
            });
        };

        /*进入CT诊断界面*/
        $scope.goDiagnosis = function(ctImage){
            $state.go('diagnosis', {
                'id':ctImage.id,
                'type':ctImage.type,
                'file':ctImage.file,
                'diagnosis':ctImage.diagnosis,
                'recordId':ctImage.recordId
            });
        };

        /*删除CT*/
        $scope.deleteCT = function(ctId){
            CTImageService.deleteCT(ctId)
                .then(function(result){
                    console.log(result.data);
                    $scope.ctImages.forEach(function(r, i) {
                        if(ctId === r.id)
                            $scope.ctImages.splice(i, 1);
                    });
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

        getCTImagesByPage();

        /***************************************************************
         当页码和页面记录数发生变化时监控后台查询
         如果把currentPage和itemsPerPage分开监控的话则会触发两次后台事件。
         ***************************************************************/
        $scope.$watch('paginationConf.currentPage + paginationConf.itemsPerPage', getCTImagesByPage);
    })
    .controller('DiagnosisCtrl', function($scope, $state, $stateParams, UPLOAD_FILE){
        $scope.id = $stateParams.id;
        $scope.type = $stateParams.type;
        $scope.diagnosis = $stateParams.diagnosis;
        $scope.recordId = $stateParams.recordId;
        $scope.file = UPLOAD_FILE+$stateParams.file;
    });
    ;