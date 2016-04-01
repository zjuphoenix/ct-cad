/**
 * Created by wuhaitao on 2016/3/30.
 */
angular.module('segmentation', ['ui.router'])
    /*.constant('BASE_URI', 'http://localhost:8080')*/
    .constant('BASE_URI', '')
    .constant('UPLOAD_FILE', 'upload/')
    .constant('Segmentation_FILE', '/api/ct/segmentation/')
    .config(function ($stateProvider) {
        $stateProvider.state('segmentation', {
            url: '/segmentation',
            params: {
                'id':1,
                'type':1,
                'file':'',
                'diagnosis':'',
                'consultationId':1
            },
            templateUrl: 'app/segmentation/segmentation.html',
            controller: 'SegmentationCtrl'
        });
    })
    .controller('SegmentationCtrl', function($scope, $stateParams, $http, BASE_URI, UPLOAD_FILE, Segmentation_FILE) {
        console.log($stateParams);
        var x1,x2,y1,y2;

        $scope.segmentationResult = '';

        $scope.originImg=UPLOAD_FILE+$stateParams.file;
        /*$scope.segImg='';*/

        $scope.result = '';
        $scope.id = $stateParams.id;
        $scope.type = $stateParams.type;
        $scope.diagnosis = $stateParams.diagnosis;
        $scope.recordId = $stateParams.recordId;

        var segmentation = function(){
            $http.post(BASE_URI+'/api/ct/segmentation',{
                'image':$scope.originImg,
                'seedX':100,
                'seedY':250
            }).then(function (result) {
                $scope.segImg = Segmentation_FILE+result.data;
            }, function (error) {
                console.log(error);
                $scope.segmentationResult = result.data;
            });
        };

        segmentation();

    });