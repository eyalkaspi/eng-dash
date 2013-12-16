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
		this.revisions = kb.collectionObservable(revisionsModel);
	}
})();