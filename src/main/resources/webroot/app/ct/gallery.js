/**
 * Created by wuhaitao on 2016/3/24.
 */

var userModule = angular.module('gallery', ['ui.router']);
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
    .controller('GalleryCtrl', function($scope, $state, $stateParams, $http){
        /*对应gallery2.html*/
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

    })
;