/**
 * Created by wuhaitao on 2016/2/26.
 */
angular.module('ct.area', ['ui.router', 'auth'])
    /*.constant('BASE_URI', 'http://localhost:8080')*/
    .constant('BASE_URI', '')
    .constant('UPLOAD_FILE', 'upload/')
    .config(function ($stateProvider) {
        $stateProvider.state('area', {
            url: '/area/:image',
            templateUrl: 'app/ct/area.html',
            controller: 'AreaCtrl'
        })
        .state('cad', {
            url: '/cad',
            params: {
                'id':1,
                'type':1,
                'file':'',
                'diagnosis':'',
                'consultationId':1
            },
            templateUrl: 'app/ct/area.html',
            controller: 'CADCtrl'
        });
    })
    .controller('AreaCtrl', function($scope, $stateParams, $http, BASE_URI) {
        var img = $stateParams.image;
        var x1,x2,y1,y2;

        $scope.myImage=img;
        $scope.myCroppedImage='';

        $scope.result = '';

        $scope.predictLesionType = function () {
            $http.post(BASE_URI+'/api/ct/predict',{
                "image":$scope.myImage,
                "x1":x1,
                "y1":y1,
                "x2":x2,
                "y2":y2
            }).then(function (result) {
                $scope.result = result.data.lesion;
            }, function (error) {
                console.log(error);
            });
        };

        $scope.areaChange = function(c) {
            x1 = c.x;
            y1 = c.y;
            x2 = c.x2;
            y2 = c.y2;
            angular.element(document.querySelector('#x1')).val(c.x);
            angular.element(document.querySelector('#y1')).val(c.y);
            angular.element(document.querySelector('#x2')).val(c.x2);
            angular.element(document.querySelector('#y2')).val(c.y2);
            angular.element(document.querySelector('#w')).val(c.w);
            angular.element(document.querySelector('#h')).val(c.h);
        }


        angular.element(document.querySelector('#target')).Jcrop({
            onChange:   $scope.areaChange
            /*onSelect:   $scope.areaChange,
            onRelease:  $scope.areaChange*/
        },function(){
        });

    })
    .controller('CADCtrl', function($scope, $stateParams, $http, BASE_URI, UPLOAD_FILE) {
        /*var img = $stateParams.ctImage;
        console.log(img);*/
        console.log($stateParams)
        var x1,x2,y1,y2;

        $scope.addFeatureResult = '';

        $scope.myImage=UPLOAD_FILE+$stateParams.file;
        $scope.myCroppedImage='';

        $scope.result = '';
        $scope.id = $stateParams.id;
        $scope.type = $stateParams.type;
        $scope.diagnosis = $stateParams.diagnosis;
        $scope.consultationId = $stateParams.consultationId;

        $scope.updateDiagnosis = function(){
            $http.post(BASE_URI+'/api/consultation/record/updateDiagnosis',{
                'id':$scope.id,
                'diagnosis':$scope.diagnosis
            }).then(function (result) {
                $scope.status = result.data;
            }, function (error) {
                console.log(error);
            });
        };
        $scope.predictLesionType = function () {
            $http.post(BASE_URI+'/api/ct/predict',{
                "image":$scope.myImage,
                "x1":x1,
                "y1":y1,
                "x2":x2,
                "y2":y2
            }).then(function (result) {
                $scope.result = result.data.lesion;
            }, function (error) {
                console.log(error);
            });
        };
        $scope.addFeatureData = function () {
            $http.post(BASE_URI+'/api/ct/addfeature',{
                "image":$scope.myImage,
                "x1":x1,
                "y1":y1,
                "x2":x2,
                "y2":y2,
                "label":$scope.result
            }).then(function (result) {
                $scope.addFeatureResult = result.data.result;
            }, function (error) {
                console.log(error);
            });
        };

        $scope.areaChange = function(c) {
            x1 = c.x;
            y1 = c.y;
            x2 = c.x2;
            y2 = c.y2;
            $scope.addFeatureResult = '';
            angular.element(document.querySelector('#x1')).val(c.x);
            angular.element(document.querySelector('#y1')).val(c.y);
            angular.element(document.querySelector('#x2')).val(c.x2);
            angular.element(document.querySelector('#y2')).val(c.y2);
            angular.element(document.querySelector('#w')).val(c.w);
            angular.element(document.querySelector('#h')).val(c.h);
        }


        angular.element(document.querySelector('#target')).Jcrop({
            onChange:   $scope.areaChange
            /*onSelect:   $scope.areaChange,
             onRelease:  $scope.areaChange*/
        },function(){
        });

    });
