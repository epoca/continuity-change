// Load the Visualization API and chart package.
google.load('visualization', '1.0', {
	'packages' : [ 'corechart', 'geochart', 'table' ]
});

var CHART_MEMORY = 5;

$(document).ready(function() {
    csvmanualuploader = $('#csv-fine-uploader').fineUploader({
		  request: {
		    endpoint: '/upload'
		  },
		  autoUpload: true,
		  multiple: false,
		  debug: true,
		  validation: {
		    allowedExtensions: ['csv', 'txt']
		  },
		  text: {
          uploadButton: '<div><i class="fa fa-upload"></i> Upload CSV file</div>'
        },
        template: '<div class="qq-uploader span12">' +
                    '<pre class="qq-upload-drop-area span12"><span>{dragZoneText}</span></pre>' +
                    '<div class="qq-upload-button btn btn-azure" style="width: auto;">{uploadButtonText}</div>' +
                    '<span class="qq-drop-processing"><span>{dropProcessingText}</span><span class="qq-drop-processing-spinner"></span></span>' +
                    '<ul class="qq-upload-list" style="margin-top: 10px; text-align: center;"></ul>' +
                  '</div>',
        classes: {
          success: 'alert alert-success',
          fail: 'alert alert-error'
        }
	});

	// file uploader callbacks
	csvmanualuploader.on('error', function(event, id, name, reason) {
			console.log('error');
			uploadRunning(false);
		}).on('complete', function(event, id, name, responseJSON){
			console.log('complete');
			uploadRunning(false);

			if(responseJSON != null && responseJSON.success == "true") {
				$('#insert_memory_title').fadeIn('fast');
				$('#insert_memory_div').fadeIn('fast');
				$('#csv-fine-uploader .qq-upload-button').fadeOut('fast');
				$('#csv-fine-uploader .qq-upload-list').css('margin-top', '0px');
				$('#remove_file_button').fadeIn('fast');
				CHART_MEMORY = responseJSON.maxMemory;
			}
		})
		.on('submit', function(event, id, name, responseJSON) {
			uploadRunning(true);
			console.log('submit');
		});

	$('#continue_button').click(function() {

		var memory = $('#insert_memory_input').val();
		
		if($.isNumeric(memory) && memory !== "")
			CHART_MEMORY = memory;
		
		$('#get_results_title').fadeIn('fast');
		$('#export_button_div').fadeIn('fast');
    	$('.progress').removeClass('progress-success');
    	$('.progress').addClass('progress-info');
    	$('#importProgressBar').css('width', '0%');
    	$('#importMsg').text('Progress: 0%');
		$('#progress_bar_div').fadeIn('fast');
	});

	$('#export_button').click(exportChartAsCSV);

	$('#drop_button').click(function(){

		$('#drop_loader').fadeIn('fast');
		
		var request = $.ajax({
			type : 'GET',
			url : '/drop',
			dataType : "json",
			contentType : 'application/json'
		});

		request.done(function(jsonResponse) {
			workflowRunning(false);
			$('#csv-fine-uploader .qq-upload-button').fadeIn('fast');
			$('#csv-fine-uploader .qq-upload-list').css('margin-top', '10px');
			$('#remove_file_button').fadeOut('fast');
			$('#drop_loader').fadeOut('fast');
		});

		request.fail(function(jqXHR, textStatus) {
			console.log("Request failed: " + textStatus);
			console.log(jqXHR);
			$('#drop_loader').fadeOut('fast');
		});
	});
	
	$('#remove_file_button').click(function(){
		
		$('#remove_file_loader').fadeIn('fast');
		
		var request = $.ajax({
			type : 'GET',
			url : '/drop',
			dataType : "json",
			contentType : 'application/json'
		});

		request.done(function(jsonResponse) {
			workflowRunning(false);
			$('#remove_file_button').fadeOut('fast');
			$('#csv-fine-uploader .qq-upload-button').fadeIn('fast');
			$('#csv-fine-uploader .qq-upload-list').css('margin-top', '10px');
			$('#export_button_div').fadeOut('fast');
			$('#drop_button_title').fadeOut('fast');
			$('#drop_button_div').fadeOut('fast');
			$('#get_results_title').fadeOut('fast');
			$('#progress_bar_div').fadeOut('fast');
			$('.progress').css('visibility', 'hidden');
	    	$('#importMsg').css('visibility', 'hidden');
	    	$('.progress').removeClass('progress-success');
	    	$('.progress').addClass('progress-info');
	    	$('#importProgressBar').css('width', '0%');
	    	$('#importMsg').text('Progress: 0%');
	    	$('#remove_file_loader').fadeOut('fast');
		});

		request.fail(function(jqXHR, textStatus) {
			console.log("Request failed: " + textStatus);
			console.log(jqXHR);
	    	$('#remove_file_loader').fadeOut('fast');
		});
	});
	

}); //END OF DOCUMENT READY

$('#export_button_div').fadeOut('fast');
$('#drop_button_title').fadeOut('fast');
$('#drop_button_div').fadeOut('fast');
$('#insert_memory_title').fadeOut('fast');
$('#get_results_title').fadeOut('fast');
$('#insert_memory_div').fadeOut('fast');
$('#progress_bar_div').fadeOut('fast');
$('#remove_file_button').fadeOut('fast');
$('#drop_loader').fadeOut('fast');
$('#remove_file_loader').fadeOut('fast');

function uploadRunning(isRunning) {
	if (isRunning == true) {
    	$('#importProgressMsg').css('visibility', 'visible');

    	var importProgressIntervalId = setInterval(function(){
	    	NotificationImportPath = '/notifications/import';
	        $.ajax({ url: NotificationImportPath, success: function(data) {
	        	$('#importProgressMsg').text('Processed '+data.percentageProgress+'% of total lines.');
	        	if (data.percentageProgress>=99) {
		        	clearInterval(importProgressIntervalId);
		        	$('#importProgressMsg').text('Saving lines on database...');
	        	}
	        }, dataType: "json"});
    	}, 500);

	} else {
		$('#importProgressMsg').css('visibility', 'hidden');
		$('#importProgressMsg').text('');
	}
};

function workflowRunning(isRunning) {
	if (isRunning == true) {
		$('.progress').css('visibility', 'visible');
    	$('#importProgressBar').css('width', '0%');
    	$('#importMsg').css('visibility', 'visible');

    	var exportProgressIntervalId = setInterval(function(){
	    	NotificationExportPath = '/notifications/export';
	        $.ajax({ 
	        	url: NotificationExportPath, 
	        	success: function(data) {
		        	$('#importProgressBar').css('width', data.percentageProgress+'%');
		        	$('#importMsg').text('Progress: '+data.percentageProgress+'%');
		        	console.log('Progress: '+data.percentageProgress+'%');
		        	if (data.percentageProgress==100) {
			        	clearInterval(exportProgressIntervalId);
				    	$('#importProgressBar').css('width', '100%');
				    	$('.progress').removeClass('progress-info');
				    	$('.progress').addClass('progress-success');
				    	$('#drop_button_title').fadeIn('fast');
				    	$('#drop_button_div').fadeIn('fast');
		        	}
	        	}, 
	        	dataType: "json"
	        });
    	}, 500);

	} else {
		$('#export_button_div').fadeOut('fast');
		$('#progress_bar_div').fadeIn('fast');
		$('#drop_button_title').fadeOut('fast');
		$('#drop_button_div').fadeOut('fast');
		$('.alert-success').remove();
		$('#get_results_title').fadeOut('fast');
		$('#insert_memory_title').fadeOut('fast');
		$('#insert_memory_div').fadeOut('fast');
		$('.progress').css('visibility', 'hidden');
    	$('#importMsg').css('visibility', 'hidden');
    	$('.progress').removeClass('progress-success');
    	$('.progress').addClass('progress-info');
    	$('#importProgressBar').css('width', '0%');
    	$('#importMsg').text('Progress: 0%');
	}
};

function openWindowWithPost(url, name) {
    var form = document.createElement("form");
    form.setAttribute("method", "post");
    form.setAttribute("action", url);
    form.setAttribute("accept-charset", "UTF-8");

    document.body.appendChild(form);

    form.submit();

    document.body.removeChild(form);
}

function exportChartAsCSV() {
    var exportUrl = '/export/memory/' + CHART_MEMORY + '/';
    workflowRunning(true);
    openWindowWithPost(exportUrl, "ChartExport");
}
