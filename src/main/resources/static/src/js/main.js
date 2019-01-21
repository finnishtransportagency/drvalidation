var $ = require('jquery');
require('jquery-ui');
global.jQuery = global.$ = $;
require('jquery-ui/custom');
require('bootstrap');
var grid = require('../../modules/gridController.js');

global.jQuery(document).ready(function($) {
	grid.initValidationGrid();
});

