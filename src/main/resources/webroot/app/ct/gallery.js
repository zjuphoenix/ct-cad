/**
 * Created by wuhaitao on 2016/3/24.
 */

var userModule = angular.module('gallery', ['ui.router', 'ngPhotoSwipe']);
userModule.constant('ENDPOINT_URI', '/api')
    .constant('BASE_URI', '')
    .constant('UPLOAD_FILE', 'upload/')
    .config(function ($stateProvider) {
        $stateProvider.state('gallery', {
                url: '/gallery/:recordId',
                templateUrl: 'app/ct/gallery.html',
                controller: 'GalleryCtrl'
            });
    })
    .directive('lightgallery', function() {
        return {
            restrict: 'A',
            link: function(scope, element, attrs) {
                if (scope.$last) {
                    // ng-repeat is completed
                    console.log("lightGallery...");
                    element.parent().lightGallery();
                }
            }
        };
    })
    .service('GalleryService', function($http, ENDPOINT_URI){
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
    .controller('GalleryCtrl', ['$scope', '$window', '$stateParams', '$http', function($scope, $window, $stateParams, $http) {
        var recordId = $stateParams.recordId;
        $scope.images = [];

        $scope.getImages = function () {
            $http.get('/api/ct/data/'+recordId)
                .then(function(result){
                    $scope.ctImages = result.data.ct;
                    $scope.ctImages.forEach(function(r, i) {
                        var ctImage = 'upload/'+ r.file;
                        $scope.images.push({
                            src: ctImage,
                            safeSrc: ctImage,
                            thumb: ctImage,
                            caption: 'CT',
                            size: screenSize(512, 512),
                            type: 'image'
                        });
                    });

                },function(error){
                    console.log(error);
                });
            /*var sizes = [
                {w: 400, h: 300},
                {w: 480, h: 360},
                {w: 640, h: 480},
                {w: 800, h: 600},
                {w: 480, h: 360}
            ];

            for (var i = 1; i <= 5; i++) {
                $scope.images.push({
                    src: 'http://lorempixel.com/' + sizes[i - 1].w + '/' + sizes[i - 1].h + '/cats',
                    safeSrc: 'http://lorempixel.com/' + sizes[i - 1].w + '/' + sizes[i - 1].h + '/cats',
                    thumb: 'http://lorempixel.com/' + sizes[i - 1].w + '/' + sizes[i - 1].h + '/cats',
                    caption: 'Lorem Ipsum Dolor',
                    size: screenSize(sizes[i - 1].w, sizes[i - 1].h),
                    type: 'image'
                });
            }*/
        };

        var screenSize = function (width, height) {
            var x = width ? width : $window.innerWidth;
            var y = height ? height : $window.innerHeight;

            return x + 'x' + y;
        };

        $scope.getImages();
    }])
    /*.controller('GalleryCtrl', function($scope, $state, $stateParams, $http){
        var recordId = $stateParams.recordId;
        $http.get('/api/ct/data/'+recordId)
            .then(function(result){
                $scope.ctImages = result.data.ct;
                $scope.ctImages.forEach(function(r, i) {
                    r.file = 'upload/'+ r.file;
                });
            },function(error){
                console.log(error);
            });
        var vm = this;

        vm.title = 'ngPhotoswipe';

        vm.opts = {
            index: 0
        };

        vm.slides = [{
            src: 'http://lorempixel.com/500/500/nature',
            w: 500, h: 500
        }, {
            src: 'http://lorempixel.com/500/500/abstract',
            w: 500, h: 500
        }, {
            src: 'http://lorempixel.com/500/500/sports',
            w: 500, h: 500
        }, {
            src: 'http://lorempixel.com/500/500/city',
            w: 500, h: 500
        }, {
            src: 'http://lorempixel.com/500/500/food',
            w: 500, h: 500
        }, {
            src: 'http://lorempixel.com/500/500/animals',
            w: 500, h: 500
        }, {
            src: 'http://lorempixel.com/500/500/business',
            w: 500, h: 500
        }, {
            src: 'http://lorempixel.com/500/500/fashion',
            w: 500, h: 500
        }];

        vm.showGallery = function (i) {
            if(angular.isDefined(i)) {
                vm.opts.index = i;
            }
            vm.open = true;
        };

        vm.closeGallery = function () {
            vm.open = false;
        };
    })*/
;