/**
 * Created by wuhaitao on 2016/3/9.
 */

angular.module('consultation',['ui.router', 'record', 'ct.area', 'tm.pagination', 'auth', 'lung'])
    /*.constant('BASE_URI', 'http://localhost:8080')*/
    .constant('BASE_URI', '')
    .config(function($stateProvider, $urlRouterProvider){
        $stateProvider.state('consultation', {
            url: '/consultation',
            templateUrl: 'app/consultation/consultations.html',
            controller: 'ConsultationCtrl'
        })
        .state('ctImages', {
            url: '/consultation/:id',
            templateUrl: 'app/consultation/ctimages.html',
            controller: 'CTImageCtrl'
        });
        $urlRouterProvider.otherwise('/consultation');
    })
    .service('ConsultationService', function($http, BASE_URI){
        var service = this;
        service.getConsultations = function(){
            return $http.get(BASE_URI+'/api/consultation');
        };
        service.getConsultationsByPage = function(postData){
            return $http.post(BASE_URI+'/api/consultation/page', postData);
        };
    })
    .controller('ConsultationCtrl', function($scope, ConsultationService){
        function getConsultations(){
            ConsultationService.getConsultations()
                .then(function(result){
                    $scope.consultations = result.data;
                },function(error){
                    console.log(error);
                });
        }

        var getConsultationsByPage = function(){
            var postData = {
                'pageIndex': parseInt($scope.paginationConf.currentPage),
                'pageSize': parseInt($scope.paginationConf.itemsPerPage)
            }
            ConsultationService.getConsultationsByPage(postData)
                .then(function(result){
                    $scope.consultations = result.data.consultations;
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

        /*getConsultations();*/
        getConsultationsByPage();

        $scope.$watch('paginationConf.currentPage + paginationConf.itemsPerPage', getConsultationsByPage);
    })
    .service('CTImageService', function($http, BASE_URI){
        var service = this;
        service.getCTImages = function(id){
            return $http.get(BASE_URI+'/api/consultation/'+id);
        };

        service.deleteCT = function(id){
            return $http.get(BASE_URI+'/api/consultation/deleteCT/'+id);
        };

        service.getCTImagesByPage = function(postData){
            return $http.post(BASE_URI+'/api/consultation/ct/page', postData);
        };
    })
    .controller('CTImageCtrl', function($scope, $state, $stateParams, CTImageService){
        var id = $stateParams.id;
        function getCTImages(consultationId){
            CTImageService.getCTImages(consultationId)
                .then(function(result){
                    $scope.ctImages = result.data;
                },function(error){
                    console.log(error);
                });
        }

        var getCTImagesByPage = function(){
            var postData = {
                'id': parseInt(id),
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

        $scope.goCAD = function(ctImage){
            console.log(ctImage);
            $state.go('cad', {
                'id':ctImage.id,
                'type':ctImage.type,
                'file':ctImage.file,
                'diagnosis':ctImage.diagnosis,
                'consultationId':ctImage.consultationId
            });
        };

        $scope.deleteCT = function(ctId){
            CTImageService.deleteCT(ctId)
                .then(function(result){
                    console.log(result.data);
                    $scope.ctImages.forEach(function(r, i) {
                        if(ctId === r.id)
                            $scope.ctImages.splice(i, 1);
                    });
                    /*$scope.$apply();*/
                    /*var length = $scope.ctImages.length;
                    for(var i=0;i<length;i++){
                        if($scope.ctImages[i].id == ctId){
                            $scope.ctImages.slice(i,1);
                            break;
                        }
                    }*/
                },function(error){
                    console.log(error);
                });
        };
        /*getCTImages(id);*/


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
    .filter('type', function(){
        return function(e){
            var out = [];
            for(var i=0;i< e.length;i++){
                var t = e[i].type;
                console.log(t);
                if(t == 1){
                    e[i].type = "肝脏";
                }
                else{
                    e[i].type = "肺部";
                }
                out.push(e[i]);
            }
            return out;
        }
    });