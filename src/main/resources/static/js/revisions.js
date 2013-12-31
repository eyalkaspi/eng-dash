/*
 * Copyright (c) 2013 by Delphix.
 * All rights reserved.
 */
 
$d = window.$d || {};
$d.models = $d.models || {};
$d.viewModels = $d.viewModels || {};
(function() {
	$d.models.Revision = Backbone.Model.extend({  
		urlRoot: '/resource/revisions',
		initialize: function(){      
		},
		defaults: {  
		}  
	});
	
	$d.models.Revisions = Backbone.Collection.extend({
		model: $d.models.Revision,
		url:'/resource/revisions'
	});
	$d.viewModels.Revisions = function(revisionsModel) {
		var self = this;
		function isComplete(revision) {
			return revision.get("state") !== "RUNNING" && revision.get("state") !== "INITIAL";
		}
		function isTesting(revision) {
			return revision.get("type") === "TESTING";
		}
		this.testingRevisions = kb.collectionObservable(revisionsModel, {
		  filters: function(revision) {
			return !isComplete(revision) && isTesting(revision);
		  }
		});
		this.visible = ko.observable(false);
		this.testedRevisions = kb.collectionObservable(revisionsModel, {
		  filters: function(revision) {
		  	return isComplete(revision) && isTesting(revision);
		  }
		});
		this.pushingRevisions = kb.collectionObservable(revisionsModel, {
		  filters: function(revision) {
		  	return !isComplete(revision) && !isTesting(revision);
		  }
		});
		this.pushedRevisions = kb.collectionObservable(revisionsModel, {
		  filters: function(revision) {
		  	return isComplete(revision) && !isTesting(revision);
		  }
		});
		this.revisionClickHandler = function(revision) {
			window.location.hash = '#' + revision.commitId();
		};
	}
})();