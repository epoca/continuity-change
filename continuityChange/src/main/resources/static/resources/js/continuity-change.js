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
		}).on('complete', function(event, id, name, responseJSON){
			console.log('complete');
			
			if(responseJSON != null && responseJSON.success == "true") {
				$('#insert_memory_title').fadeIn('fast');
				$('#insert_memory_div').fadeIn('fast');
			}
		})
		.on('submit', function(event, id, name, responseJSON) {
			console.log('submit');
		});
	
	$('#calculate_button').click(function() {
		
		var memory = $('#insert_memory_input').val();
		
		if($.isNumeric(memory) && memory !== "") {
			$('#get_results_title').fadeIn('fast');
			$('#export_button').fadeIn('fast');
			CHART_MEMORY = memory;
		} else {
			$('#get_results_title').fadeOut('fast');
			$('#export_button').fadeOut('fast');
			$('#drop_button').fadeOut('fast');
			$('#drop_button_title').fadeOut('fast');
		}
	});
	
	$('#export_button').click(exportChartAsCSV);
	
	$('#drop_button').click(function(){
		
		var request = $.ajax({
			type : 'GET',
			url : '/drop',
			dataType : "json",
			contentType : 'application/json'
		});

		request.done(function(jsonResponse) {
			workflowRunning(false);
		});
		
		request.fail(function(jqXHR, textStatus) {
			console.log("Request failed: " + textStatus);
			console.log(jqXHR);
		});
	});
	
}); //END OF DOCUMENT READY

$('#export_button').fadeOut('fast');
$('#drop_button_title').fadeOut('fast');
$('#drop_button').fadeOut('fast');
$('#insert_memory_title').fadeOut('fast');
$('#get_results_title').fadeOut('fast');
$('#insert_memory_div').fadeOut('fast');

function workflowRunning(isRunning) {
	if (isRunning == true) {
		$('.progress').css('visibility', 'visible');
    	$('#importProgressBar').css('width', '0%');
    	$('#importMsg').css('visibility', 'visible');
    	
    	var exportProgressIntervalId = setInterval(function(){
    	NotificationPath = '/notifications';
        $.ajax({ url: NotificationPath, success: function(data) {
        	$('#importProgressBar').css('width', data.percentageProgress+'%');
        	$('#importMsg').text('Progress: '+data.percentageProgress+'%');
        	if (data.percentageProgress==100) {
	        	clearInterval(exportProgressIntervalId);
		    	$('#importProgressBar').css('width', '100%');
		    	$('.progress').removeClass('progress-info');
		    	$('.progress').addClass('progress-success');
		    	$('#drop_button_title').fadeIn('fast');
		    	$('#drop_button').fadeIn('fast');
        	}
        }, dataType: "json"});
    }, 2000);

	} else {
		$('#export_button').fadeOut('fast');
		$('#drop_button_title').fadeOut('fast');
		$('#drop_button').fadeOut('fast');
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
	
	windowoption = 'width=1,height=1,left=100,top=100,resizable=no,scrollbars=no';

     var form = document.createElement("form");
     form.setAttribute("method", "post");
     form.setAttribute("action", url);
     form.setAttribute("target", name);
     form.setAttribute("accept-charset", "UTF-8");

     document.body.appendChild(form);

     //note I am using a post.htm page since I did not want to make double request to the page 
     //it might have some Page_Load call which might screw things up.
     window.open('export.jsp', '_blank', windowoption);
     
     form.submit();

     document.body.removeChild(form);
}

function exportChartAsCSV() {
	var exportUrl = '/export/memory/' + CHART_MEMORY + '/first/true/second/false/third/false/';
	workflowRunning(true);
//	openWindowWithPost(exportUrl, "ChartExport");
	
	var request = $.ajax({
		type : 'GET',
		url : exportUrl,
		dataType : "json",
		contentType : 'application/json'
	});

	request.done(function(jsonResponse) {
		workflowRunning(false);
	});
	
	request.fail(function(jqXHR, textStatus) {
		console.log("Request failed: " + textStatus);
		console.log(jqXHR);
	});
	
}
