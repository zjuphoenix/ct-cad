/**
 * Created by wuhaitao on 2016/2/25.
 */
var userModule = angular.module('ct.data', ['ui.router', 'ngAnimate', 'ngTouch', 'ct.area']);
userModule.constant('ENDPOINT_URI', '/api')
    .config(function ($stateProvider) {
        $stateProvider.state('ct', {
            url: '/ct',
            templateUrl: 'app/ct/records.html',
            controller: 'CTDataCtrl',
            controllerAs: 'ctlist'
        })
        .state('images', {
            url: '/images/:id',
            templateUrl: 'app/ct/galary.html',
            controller: 'ImagesCtrl'
        })
        ;
    })
    .service('CTDataService', function ($http, ENDPOINT_URI) {
        var service = this;

        service.getCTRecords = function () {
            return $http.get(ENDPOINT_URI + '/ct');
        };

        service.getCTImages = function (id) {
            return $http.get(ENDPOINT_URI + '/ct/' + id);
        };

    })
    .controller('CTDataCtrl', function ($scope, CTDataService) {

        function getCTRecords() {
            CTDataService.getCTRecords()
                .then(function (result) {
                    $scope.ct = result.data;
                }, function (error) {
                    console.log(error);
                });
        }

        getCTRecords();
    })
    .controller('ImagesCtrl', function ($scope, $state, $stateParams, CTDataService) {
        var id = $stateParams.id;
        // Set of Photos
        CTDataService.getCTImages(id)
            .then(function (result) {
                $scope.photos = result.data;
            }, function (error) {
                console.log(error);
            });

        // initial image index
        $scope._Index = 0;

        // if a current image is the same as requested image
        $scope.isActive = function (index) {
            return $scope._Index === index;
        };

        // show prev image
        $scope.showPrev = function () {
            $scope._Index = ($scope._Index > 0) ? --$scope._Index : $scope.photos.length - 1;
        };

        // show next image
        $scope.showNext = function () {
            $scope._Index = ($scope._Index < $scope.photos.length - 1) ? ++$scope._Index : 0;
        };

        // show a certain image
        $scope.showPhoto = function (index) {
            $scope._Index = index;
        };

        $scope.isShown = function (index) {
            if(_Index<=2){
                return index < 6;
            }
            else{
                return index -_Index <= 3 && index -_Index >= -2;
            }
        };

        $scope.showImage = function (index) {
            $state.go('area', {image : $scope.photos[index].src});
        };
    })
    ;