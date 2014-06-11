// Load the Visualization API and chart package.
google.load('visualization', '1.0', {
	'packages' : [ 'corechart', 'geochart', 'table' ]
});

var CHART_YEARS = 35;
var CHART_MEMORY = 5;
var isFirmSelected = false;

function drawChart() {

	$('#chart_loader').fadeIn('fast');
	
	ContinuityChangeChart = '/chart/top/'+ CHART_YEARS + '/memory/'+ CHART_MEMORY;

	
	chartFirms = [];
	
	if ($('#chart_firm1').val()!='')
		chartFirms.push($('#chart_firm1').val());
	if ($('#chart_firm2').val()!='')
		chartFirms.push($('#chart_firm2').val());
	if ($('#chart_firm3').val()!='')
		chartFirms.push($('#chart_firm3').val());
	
//	if(chartFirms.length == 0)
//		$('#chart_div').fadeOut('fast');
//	else {
////		$('#no_chart_message_div').fadeOut('fast');
//		$('#chart_container').fadeIn('fast');
//		$('#chart_div').fadeIn('fast');
//	}
	
	chartFirms_json_data = JSON.stringify({
		firms: chartFirms
	});
	
	var request = $.ajax({
		type : 'POST',
		url : ContinuityChangeChart,
		dataType : "json",
		contentType : 'application/json',
		data: chartFirms_json_data
	});

	request.done(function(jsonResponse) {
		chartDataTable = new google.visualization.arrayToDataTable(jsonResponse.data);
		chart = new google.visualization.ComboChart(document.getElementById('chart_div'));
		
		chartOptions = {
			vAxes: [
			   {title: "Change"},
			   {title: "Depth"}
			],
			hAxis: {title: 'Years'},
			seriesType: "bars",
			series: {
		    	1: {type: "line",targetAxisIndex:1},
				3: {type: "line",targetAxisIndex:1},
				5: {type: "line",targetAxisIndex:1}
			},
			'fontSize': '12',	
			'colors': ['#33B5E5','#99CC00','#AA66CC','#FFBB33','#FF4444','#0099CC'],
			'legend' : {position: 'bottom'},
			'fontName': 'Open Sans, sans-serif'
		};
		
		$('#chart_loader').hide();
		$('#chart_container').fadeIn('fast');
		$('#chart_div').fadeIn('fast');
		
//		google.visualization.events.addListener(chart, 'error', errorHandler); 
		
		chart.draw(chartDataTable, chartOptions);

		$('#title_export_div').fadeIn('fast');
		$('#export_button').fadeIn('fast');
		$('#title_drop_div').fadeIn('fast');
		$('#drop_button').fadeIn('fast');
		
		$(document).scrollTop($("#chart_div").offset().top); 
		
	});
	
	request.fail(function(jqXHR, textStatus) {
		console.log("Request failed: " + textStatus);
		console.log(jqXHR);
	});             
}

$('.chart_firmselector').change(function() {
	isFirmSelected = true;
	$('#chart_div').hide();
	drawChart();
});

$(function() {
	$("#chart_slider1").slider({
		value : 35,
		min : 25,
		max : 50,
		step : 1,
		stop : function(event, ui) {
			CHART_YEARS = ui.value;
			$("#chart_slider1_badge").text(ui.value);
			if(isFirmSelected) {
				$('#chart_div').hide();
				drawChart();
			}
		}
	});
	$("#chart_slider1_badge").text($("#chart_slider1").slider("value"));
});

$(function() {
	$("#chart_slider2").slider({
		value : 5,
		min : 1,
		max : 10,
		step : 1,
		stop : function(event, ui) {
			CHART_MEMORY = ui.value;
			$("#chart_slider2_badge").text(CHART_MEMORY);
			if(isFirmSelected) {
				$('#chart_div').hide();
				drawChart();
			}
		}
	});
	$("#chart_slider2_badge").text(5);
});

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
			
			var request = $.ajax({
				type : 'GET',
				url : '/firms',
				dataType : "json",
				contentType : 'application/json'
			});

			request.done(function(jsonResponse) {
				$.each(jsonResponse, function(index, firm) {
		            $('.firms-list').append('<li><a tabindex="-1" href="#" data-option=' + firm + '>' + firm + '</a></li>');
		    	});
				$('.qq-upload-list').fadeIn('fast');
				$('#chart_controls').fadeIn('fast');
			});
			
			request.fail(function(jqXHR, textStatus) {
				console.log("Request failed: " + textStatus);
				console.log(jqXHR);
			});
		})
		.on('submit', function(event, id, name, responseJSON) {
			console.log('submit');
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
			$('#chart_controls').fadeOut('fast');
			$('#title_export_div').fadeOut('fast');
			$('#export_button').fadeOut('fast');
			$('#title_drop_div').fadeOut('fast');
			$('#drop_button').fadeOut('fast');
			$('.qq-upload-list').fadeOut('fast');
			$('#chart_div').fadeOut('fast');
			$('#chart_container').fadeOut('fast');
		});
		
		request.fail(function(jqXHR, textStatus) {
			console.log("Request failed: " + textStatus);
			console.log(jqXHR);
		});
	});
	
});

$('#chart_loader').fadeOut('fast');
$('#chart_controls').fadeOut('fast');
$('#title_export_div').fadeOut('fast');
$('#export_button').fadeOut('fast');
$('#title_drop_div').fadeOut('fast');
$('#drop_button').fadeOut('fast');
$('#chart_container').fadeOut('fast');

function openWindowWithPost(url, name, params) {
	
	windowoption = 'width=1,height=1,left=100,top=100,resizable=no,scrollbars=no';

     var form = document.createElement("form");
     form.setAttribute("method", "post");
     form.setAttribute("action", url);
     form.setAttribute("target", name);
     form.setAttribute("accept-charset", "UTF-8");

     for (var i in params) {
         if (params.hasOwnProperty(i)) {
             var input = document.createElement('input');
             input.type = 'hidden';
             input.name = i;
             input.value = params[i];
             form.appendChild(input);
         }
     }

     document.body.appendChild(form);

     //note I am using a post.htm page since I did not want to make double request to the page 
     //it might have some Page_Load call which might screw things up.
     window.open('export.jsp', '_blank', windowoption);
     
     form.submit();

     document.body.removeChild(form);
}

function exportChartAsCSV() {
	var exportUrl = '/export/top/' + CHART_YEARS + '/memory/' + CHART_MEMORY;
	
	formData = {
			"firm1" : $('#chart_firm1').val()!='' ? $('#chart_firm1').val() : "",
			"firm2" : $('#chart_firm2').val()!='' ? $('#chart_firm2').val() : "",
			"firm3" : $('#chart_firm3').val()!='' ? $('#chart_firm3').val() : ""
	};
	openWindowWithPost(exportUrl, "ChartExport", formData);
}
