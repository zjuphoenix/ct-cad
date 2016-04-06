/**
 * Created by wuhaitao on 2016/3/24.
 */
angular.module('recognition', ['ui.router', 'ngDialog'])
    /*.constant('BASE_URI', 'http://localhost:8080')*/
    .constant('BASE_URI', '')
    .constant('UPLOAD_FILE', 'upload/')
    .config(function ($stateProvider) {
        $stateProvider.state('cad', {
                url: '/cad',
                params: {
                    'id':1,
                    'type':'',
                    'file':'',
                    'diagnosis':'',
                    'recordId':1
                },
                templateUrl: 'app/ct/recognition.html',
                controller: 'CADCtrl'
            })
            .state('label', {
                url: '/label',
                params: {
                    'ct':'',
                    'x1':0,
                    'y1':0,
                    'x2':0,
                    'y2':0,
                    'type':''
                },
                templateUrl: 'app/ct/label.html',
                controller: 'LabelCtrl'
            });
    })
    .controller('CADCtrl', function($scope, $stateParams, $state, $http, BASE_URI, UPLOAD_FILE, ngDialog) {
        /*var img = $stateParams.ctImage;
         console.log(img);*/
        /*console.log($stateParams);*/
        var x1,x2,y1,y2;

        $scope.addFeatureResult = '';

        $scope.myImage=UPLOAD_FILE+$stateParams.file;
        $scope.myCroppedImage='';

        $scope.result = '';
        $scope.id = $stateParams.id;
        $scope.type = $stateParams.type;
        $scope.diagnosis = $stateParams.diagnosis;
        $scope.recordId = $stateParams.recordId;

        $scope.updateDiagnosis = function(){
            $http.put(BASE_URI+'/api/ct',{
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
                "y2":y2,
                "type":$scope.type
            }).then(function (result) {
                /*$scope.result = result.data.lesion;*/
                $scope.result = result.data;
                console.log($scope.result);
                ngDialog.open({ template: 'app/ct/recognitionDialog.html',//模式对话框内容为test.html
                    className: 'ngdialog-theme-plain',
                    scope:$scope //将scope传给test.html,以便显示地址详细信息
                });
            }, function (error) {
                console.log(error);
            });
        };
        $scope.labelLesion = function () {
            $state.go('label', {
                'ct':$scope.myImage,
                'x1':x1,
                'y1':y1,
                'x2':x2,
                'y2':y2,
                'type':$scope.type
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
        };

        angular.element(document.querySelector('#target')).Jcrop({
            onChange:   $scope.areaChange
            /*onSelect:   $scope.areaChange,
             onRelease:  $scope.areaChange*/
        },function(){
        });

    })
    .controller('LabelCtrl', function($scope, $stateParams, $http, BASE_URI, ngDialog){
        $scope.ct = $stateParams.ct;
        $scope.labelx1 = $stateParams.x1;
        $scope.labelx2 = $stateParams.x2;
        $scope.labely1 = $stateParams.y1;
        $scope.labely2 = $stateParams.y2;
        $scope.type = $stateParams.type;
        console.log($scope.type);

        $scope.addFeatureResult = '';
        if($scope.type == '肝脏'){
            $scope.lesion = {
                1 :"正常",
                2 :"肝癌",
                3 :"肝血管瘤",
                4 :"肝囊肿",
                5 :"其他"
            };
        }
        else if($scope.type == '肺部'){
            $scope.lesion = {
                1 :"正常",
                2 :"肺结核",
                3 :"肺结节"
            };
        }

        $scope.label = '';

        $scope.labelAreaChange = function(c) {
            $scope.labelx1 = c.x;
            $scope.labely1 = c.y;
            $scope.labelx2 = c.x2;
            $scope.labely2 = c.y2;
            /*console.log($scope.labelx1);
            console.log($scope.labely1);
            console.log($scope.labelx2);
            console.log($scope.labely2);*/
        };

        $scope.addFeatureLabel = function(){
            var url = '';
            if($scope.type == '肝脏'){
                url = BASE_URI+'/api/ct/addLiverfeature';
            }
            else if($scope.type == '肺部'){
                url = BASE_URI+'/api/ct/addLungfeature';
            }
            console.log('label:'+$scope.label);
            if(url != '' && $scope.label != ''){
                $http.post(url,{
                    "image":$scope.ct,
                    "x1":$scope.labelx1,
                    "y1":$scope.labely1,
                    "x2":$scope.labelx2,
                    "y2":$scope.labely2,
                    "label":$scope.label
                }).then(function (result) {
                    $scope.addFeatureResult = result.data.result;
                    ngDialog.open({ template: '<div>标注类型:{{label}}</div><div>标注结果:{{addFeatureResult}}</div>',//模式对话框内容为test.html
                        className: 'ngdialog-theme-plain',
                        scope:$scope, //将scope传给test.html,以便显示地址详细信息
                        plain:true//template为html字符串
                    });
                }, function (error) {
                    console.log(error);
                });
            }
            else{
                ngDialog.open({ template: '<div>未选择标注病变类型！</div>',//模式对话框内容为test.html
                    className: 'ngdialog-theme-plain',
                    scope:$scope, //将scope传给test.html,以便显示地址详细信息
                    plain:true//template为html字符串
                });
            }

        };

        angular.element(document.querySelector('#labelCT')).Jcrop({
            onChange:   $scope.labelAreaChange
        },function(){
            this.animateTo([$scope.labelx1,$scope.labely1,$scope.labelx2,$scope.labely2]);
        });
    });
