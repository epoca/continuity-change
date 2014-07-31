// Load the Visualization API and chart package.
google.load('visualization', '1.0', {
	'packages' : [ 'corechart', 'geochart', 'table' ]
});

var CHART_MEMORY = 5;

$(document).ready(function() {
    var option1;
    var option2;
    var option3;
	
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

        option1 = $('#option1:checked').val()?true:false;
        option2 = $('#option2:checked').val()?true:false;
        option3 = $('#option3:checked').val()?true:false;

		if($.isNumeric(memory) && memory !== "" && (option1 || option2 || option3)) {
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
    var form = document.createElement("form");
    form.setAttribute("method", "post");
    form.setAttribute("action", url);
    form.setAttribute("accept-charset", "UTF-8");

    document.body.appendChild(form);

    form.submit();

    document.body.removeChild(form);
}

function exportChartAsCSV() {
    var exportUrl = '/export/memory/' + CHART_MEMORY + '/first/'+option1+'/second/'+option2+'/third/'+option3+'/';
    workflowRunning(true);
    openWindowWithPost(exportUrl, "ChartExport");
}