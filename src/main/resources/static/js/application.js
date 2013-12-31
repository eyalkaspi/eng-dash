/*
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
 
$(function() {
	// Create collections and view models
	var revisionsModel = new $d.models.Revisions();
	var revisionsViewModel = new $d.viewModels.Revisions(revisionsModel);
	//var hashRevisionViewModel = new $d.viewModels.HashRevision(revisionsModel);
	
	// Binds listing view model
	ko.applyBindings(revisionsViewModel, $("#listing").get(0));
	
	var $single = $("#single");
	var $listing = $("#listing");
		
	// Very basic routing. Display listing if hash is undefined, single view otherwise
	function routing() {
		var hash = window.location.hash.substr(1);
		// TODO: This is a hack. Should update the underlying element or list all attributes
		ko.cleanNode($single.get(0));
		if (hash.length) {
			var revisionModel = revisionsModel.findWhere({"commitId": hash});
			if (revisionModel) {
				var hashRevisionViewModel = kb.viewModel(revisionModel);
				ko.applyBindings(hashRevisionViewModel, $single.get(0));
				$single.show();
				$listing.hide();
				return;
			}			
		}
		$single.hide();
		$listing.show();
	}
	
	$(window).bind( 'hashchange', routing);
	
	// Setup automatic refresh
	revisionsModel.fetch().done(function() {
		// Initial routing
		routing();
	});
	setInterval(function() {revisionsModel.fetch(); },1000);
});