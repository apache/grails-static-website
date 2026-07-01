'use strict';

angular.module('todoListApp')
.controller('mainCtrl', function($scope, dataService) {
  $scope.addTodo = function() {
  	dataService.addTodo(function(response) {
  		$scope.todos.unshift(response.data);	
	});
  };
    
  dataService.getTodos(function(response) { 
      $scope.todos = response.data;
    });
  
  $scope.deleteTodo = function(todo, $index) {
    dataService.deleteTodo(todo, function(response) {	   
	    $scope.todos.splice($index, 1);	    
    });
  };
  
 $scope.saveTodo = function(todo, $index) {
    dataService.saveTodo(todo, function(response) {
	    console.log(response.data)
    });
  };
})