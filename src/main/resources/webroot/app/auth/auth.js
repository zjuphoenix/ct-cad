/**
 * Created by wuhaitao on 2016/3/20.
 */
angular.module('auth', ['angular-storage', 'ui.router'])
    /*.constant('BASE_URI', 'http://localhost:8080')*/
    .constant('BASE_URI', '')
    .config(function($stateProvider, $urlRouterProvider, $httpProvider){
        $stateProvider
            .state('login', {
                url: '/login',
                templateUrl: 'app/auth/login.html',
                controller: 'LoginCtrl',
                controllerAs: 'login'
            });

        $httpProvider.interceptors.push('APIInterceptor');
    })
    .service('APIInterceptor', function($rootScope, UserContext) {
        var service = this;

        service.request = function(config) {
            var currentUser = UserContext.getCurrentUser(),
                access_token = currentUser ? currentUser.access_token : null;

            if (access_token) {
                config.headers.authorization = access_token;
            }
            return config;
        };

        service.responseError = function(response) {
            console.log(response);
            if (response.status === 401) {
                console.log("broadcast unauthorized!");
                $rootScope.$broadcast('unauthorized');
            }
            return response;
        };
    })
    .service('UserContext', function(store) {
        var service = this,
            currentUser = null,
            userPermissions = [];

        service.setCurrentUser = function(user) {
            currentUser = user;
            store.set('user', user);
            return currentUser;
        };

        service.getCurrentUser = function() {
            if (!currentUser) {
                currentUser = store.get('user');
            }
            return currentUser;
        };

        service.setPermissions = function(permissions) {
            userPermissions = permissions;
            store.set('permissions', permissions);
            return userPermissions;
        };

        service.resetPermissions = function() {
            userPermissions = [];
        };

        service.isPermitted = function(permission) {
            if (!userPermissions) {
                userPermissions = store.get('permissions');
            }
            /*console.log('permission:'+permission);
            console.log(userPermissions);*/
            if (userPermissions.length > 0) {
                for (var i = 0; i  < userPermissions.length; i++) {
                    var entry = userPermissions[i];
                    console.log(entry.PERM);
                    if (permission === entry.PERM) {
                        return true;
                    }
                }
            }
            return false;
        }

    })
    .controller('MainCtrl', function ($rootScope, $state, $http, LoginService, UserContext, BASE_URI) {
        var main = this;

        function initPermission() {
            $http.post(BASE_URI+'/api/permission')
                .then(function(response) {
                    /*console.log('permissions:'+response.data);*/
                    UserContext.setPermissions(response.data);
                }, function(error) {
                    console.log(error);
                });
        }

        function logout() {
            LoginService.logout()
                .then(function(response) {
                    main.currentUser = UserContext.setCurrentUser(null);
                    UserContext.resetPermissions();
                    $state.go('login');
                }, function(error) {
                    console.log(error);
                });
        }

        $rootScope.$on('authorized', function() {
            main.currentUser = UserContext.getCurrentUser();
            initPermission();
        });

        $rootScope.$on('unauthorized', function() {
            main.currentUser = UserContext.setCurrentUser(null);
            UserContext.resetPermissions();
            $state.go('login');
        });

        initPermission();

        main.logout = logout;
        main.currentUser = UserContext.getCurrentUser();
        main.isPermitted = function(name) {
            return UserContext.isPermitted(name);
        };
        main.isPatient = function() {
            if (main.currentUser == 'patient'){
                return true;
            }
            else{
                return false;
            }
        };

        main.isAdminOrDoctor = function() {
            if (main.currentUser == 'patient'){
                return false;
            }
            else{
                return true;
            }
        };
    })
    .service('LoginService', function($http, BASE_URI) {
        var service = this;

        service.login = function(credentials) {
            return $http.post(BASE_URI + '/login', credentials);
        };

        service.logout = function() {
            return $http.post(BASE_URI + '/logout');
        };

        service.register = function(user) {
            return $http.post(BASE_URI + '/register', user);
        };
    })
    .controller('LoginCtrl', function($rootScope, $state, LoginService, UserContext){
        var login = this;

        function signIn(user) {
            LoginService.login(user)
                .then(function(response) {
                    if (response.status == 200) {
                        console.log('access_token:'+response.data.id);
                        user.access_token = response.data.id;
                        UserContext.setCurrentUser(user);
                        $rootScope.$broadcast('authorized');
                        $state.go('consultation');
                    } else {
                        login.message = 'Wrong username or password';
                    }
                });
        }

        function register(user) {
            LoginService.register(user)
                .then(function(response) {
                    login(user);
                });
        }

        function submit(user) {
            console.log(user);
            login.newUser ? register(user) : signIn(user);
        }

        login.newUser = false;
        login.submit = submit;
        login.message = null;
    });