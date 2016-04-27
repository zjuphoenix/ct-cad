/**
 * Created by wuhaitao on 2016/4/26.
 */
var detectionModule = angular.module('detection', ['ui.router']);
detectionModule.constant('ENDPOINT_URI', '/api')
    .constant('BASE_URI', '')
    .constant('UPLOAD_FILE', 'upload/')
    .config(function ($stateProvider) {
        $stateProvider.state('detection', {
                url: '/detection/:recordId',
                /*params: {
                    'recordId':1
                },*/
                templateUrl: 'app/ct/detection.html',
                controller: 'DetectionCtrl'
            });
    })
    .service('CTImageService', function($http, ENDPOINT_URI){
        var service = this;
        service.getCTImages = function(recordId){
            return $http.get(ENDPOINT_URI+'/ct/data/'+recordId);
        };

        service.getCTImagesByPage = function(postData){
            return $http.post(ENDPOINT_URI+'/ct', postData);
        };
    })
    .controller('DetectionCtrl', function($scope, $state, $stateParams, CTImageService, BASE_URI){
        $scope.recordId = $stateParams.recordId;

        var getCTImagesByPage = function(){
            var postData = {
                'recordId': parseInt($scope.recordId),
                'pageIndex': parseInt($scope.paginationConf.currentPage),
                'pageSize': parseInt($scope.paginationConf.itemsPerPage)
            }
            CTImageService.getCTImagesByPage(postData)
                .then(function(result){
                    $scope.ctImages = result.data.ct;
                    $scope.ctImages.forEach(function(r, i) {
                        if (r.recognition == 1){
                            r.recognition = '正常';
                        }
                        else if(r.recognition == 2){
                            r.recognition = '异常';
                        }
                        else{
                            r.recognition = '尚未识别成功';
                        }
                        r.img = 'upload/'+ r.file;
                        r.liver = 'segmentation/'+ r.file;
                    });
                    $scope.paginationConf.totalItems = result.data.count;
                    /*console.log($scope.ctImages);
                     console.log($scope.paginationConf.totalItems);*/
                },function(error){
                    console.log(error);
                });
        };

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
    });