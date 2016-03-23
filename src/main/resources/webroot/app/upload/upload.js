/**
 * Created by wuhaitao on 2016/3/8.
 */
angular.module('upload', ['ui.router','ngFileUpload', 'auth'])
    /*.constant('BASE_URI', 'http://localhost:8080')
    .constant('UPLOAD_URI', 'http://localhost:8080/upload')*/
    .constant('BASE_URI', '')
    .constant('UPLOAD_URI', '/upload')
    .config(function ($stateProvider) {
        $stateProvider
            .state('upload', {
                url: '/upload',
                templateUrl: 'app/upload/upload.html',
                controller: 'UploadCtrl',
            })
            .state('uploadFiles', {
                url: '/uploadFiles',
                templateUrl: 'app/upload/uploadFiles.html',
                controller: 'UploadFilesCtrl',
            });
    })
    .controller('UploadCtrl', ['$scope', 'Upload', 'BASE_URI', 'UPLOAD_URI', function ($scope, Upload, BASE_URI, UPLOAD_URI) {
        // upload later on form submit or something similar
        $scope.id = "";
        $scope.type = 1;
        $scope.result = '';
        $scope.submit = function() {
            if ($scope.form.file.$valid && $scope.file) {
                $scope.upload($scope.file);
                /*console.log($scope.id);
                console.log($scope.type);*/
            }
        };

        // upload on file select or drop
        $scope.upload = function (file) {
            Upload.upload({
                url: UPLOAD_URI,
                data: {file: file, 'id': $scope.id, 'type':$scope.type}
            }).then(function (resp) {
                $scope.result = 'upload success!';
                console.log('Success ' + resp.config.data.file.name + 'uploaded. Response: ' + resp.data);
            }, function (resp) {
                $scope.result = 'upload failed!';
                console.log('Error status: ' + resp.status);
            }, function (evt) {
                var progressPercentage = parseInt(100.0 * evt.loaded / evt.total);
                console.log('progress: ' + progressPercentage + '% ' + evt.config.data.file.name);
            });
        };
    }])
    .controller('UploadFilesCtrl', ['$scope', 'Upload', 'BASE_URI', 'UPLOAD_URI', function ($scope, Upload, UPLOAD_URI) {
        // upload later on form submit or something similar
        $scope.type = 1;
        $scope.result = '';
        $scope.bar = {"width":"0%"};
        $scope.submit = function() {
            if ($scope.form.files.$valid && $scope.files) {
                $scope.uploadFiles($scope.files);
                /*$scope.upload($scope.file);*/
                /*console.log($scope.id);
                 console.log($scope.type);*/
            }
        };

        // for multiple files:
        $scope.uploadFiles = function (files) {
            if (files && files.length) {
                /*for (var i = 0; i < files.length; i++) {
                    Upload.upload({..., data: {file: files[i]}, ...})...;
                }*/
                // or send them all together for HTML5 browsers:
                Upload.upload({
                    url: UPLOAD_URI+'/files',
                    data: {file: files, 'type':$scope.type}
                }).then(function (resp) {
                    $scope.result = 'upload success!';
                    console.log('Success ' + resp.config.data.file.name + 'uploaded. Response: ' + resp.data);
                }, function (resp) {
                    $scope.result = 'upload failed!';
                    console.log('Error status: ' + resp.status);
                }, function (evt) {
                    var progressPercentage = parseInt(100.0 * evt.loaded / evt.total);
                    console.log('progress: ' + progressPercentage + '% ' + evt.config.data.file.name);
                    $scope.bar = {"width":progressPercentage + '%'};
                });
            }
        }
    }]);