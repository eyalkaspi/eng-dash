/*
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
 
$(function() {
	var revisionsModel = new $d.models.Revisions();
	var revisionsViewModel = new $d.viewModels.Revisions(revisionsModel);
	ko.applyBindings(revisionsViewModel, $('#revisions_table')[0]);
	revisionsModel.fetch();
	setInterval(function() {revisionsModel.fetch(); },1000);
});