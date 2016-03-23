/**
 * Created by wuhaitao on 2016/3/21.
 */
angular.module('user', ['ui.router', 'auth'])
    .constant('ENDPOINT_URI', '/api')
    /*.constant('BASE_URI', 'http://localhost:8080')*/
    .constant('BASE_URI', '')
    .config(function($stateProvider) {
        $stateProvider
            .state('users', {
                url: '/users',
                templateUrl: 'app/user/userlist.html',
                controller: 'UserListCtrl',
                controllerAs: 'userlist'
            })
            .state('userAdd', {
                url: '/users/new',
                templateUrl: 'app/user/useradd.html',
                controller: 'UserAddCtrl',
                controllerAs: 'useradd'
            })
            .state('userEdit', {
                url: '/users/:username',
                templateUrl: 'app/user/useredit.html',
                controller: 'UserEditCtrl',
                controllerAs: 'useredit'
            })
        ;
    })
    .service('UserService', function($http, BASE_URI, ENDPOINT_URI) {
        var service = this;

        service.getAllUser = function() {
            return $http.get(BASE_URI + ENDPOINT_URI + '/users');
        };

        service.addUser = function(user) {
            return $http.post(BASE_URI + ENDPOINT_URI + '/users', user);
        };

        service.getUserById = function(username) {
            return $http.get(BASE_URI + ENDPOINT_URI + '/users/' + username);
        };

        service.updateUser = function(user) {
            return $http.put(BASE_URI + ENDPOINT_URI + '/users/' + user.username, user);
        };

        service.deleteUser = function(username) {
            return $http.delete(BASE_URI + ENDPOINT_URI + '/users/' + username);
        };

        service.getUserPermissions = function() {
            return $http.post(BASE_URI + ENDPOINT_URI + '/permission');
        }

    })
    .controller('UserListCtrl', function($scope, $state, UserService) {

        $scope.delete = function(user) {
            var deleted = confirm('Are you absolutely sure you want to delete?');
            console.log("delete user");
            if (deleted) {
                UserService.deleteUser(user.USERNAME)
                    .then(function(response) {
                        $scope.users.forEach(function(r, i) {
                            if(user.USERNAME === r.USERNAME)
                                $scope.users.splice(i, 1);
                        });
                        console.log("delete user success!");
                        /*$state.go('users');*/
                    }, function(error) {
                        console.log(error);
                    });
            }
        };

        function getAll() {
            UserService.getAllUser()
                .then(function(result) {
                    $scope.users = result.data;
                }, function(error) {
                    console.log(error);
                });
        }

        getAll();
    })
    .controller('UserAddCtrl', function($state, $scope, UserService) {
        $scope.master = {};
        $scope.info = "";

        $scope.addUser = function(user) {
            if(user.password != user.confirm_password){
                $scope.info = "两次输入的密码不一致";
                return;
            }
            UserService.addUser(user)
                .then(function(response) {
                    $state.go('users');
                }, function(error) {
                    $scope.info = error;
                    console.log(error);
                });

            $scope.reset = function() {
                $scope.user = angular.copy($scope.master);
            };

            $scope.reset();
        };

    })
    .controller('UserEditCtrl', function($state, $scope, $stateParams, UserService) {

        function getById(username) {
            UserService.getUserById(username)
                .then(function(response) {
                    $scope.user = response.data;
                }, function(error) {
                    console.log(error);
                });
        };

        $scope.update = function(user) {
            UserService.updateUser(user)
                .then(function (response) {
                    $state.go('users');
                }, function (error) {
                    console.log(error);
                });
        };

        $scope.delete = function(user) {
            var deleted = confirm('Are you absolutely sure you want to delete?');
            if (deleted) {
                UserService.deleteUser(user.username)
                    .then(function(response) {
                        $state.go('users');
                    }, function(error) {
                        console.log(error);
                    });
            }
        };

        getById($stateParams.username);

    })
;
