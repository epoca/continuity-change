// Load the Visualization API and chart package.
google.load('visualization', '1.0', {
	'packages' : [ 'corechart', 'geochart', 'table' ]
});

var allFirms;

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
			$('#draw_chart_button').fadeIn('fast');
			$('#chart_controls').fadeIn('fast');
			
			var request = $.ajax({
				type : 'GET',
				url : '/firms',
				dataType : "json",
				contentType : 'application/json'
			});

			request.done(function(jsonResponse) {
				console.log(jsonResponse);
				allFirms = jsonResponse;
			});
			
			request.fail(function(jqXHR, textStatus) {
				console.log("Request failed: " + textStatus);
				console.log(jqXHR);
			});
		})
		.on('submit', function(event, id, name, responseJSON) {
			console.log('submit');
		});
	
	$('#draw_chart_button').click(function() {
		
		$('#chart_loader').fadeIn('fast');
		
		ContinuityChangeChart = '/chart/top/35/memory/35';

		firms = ['r766', 'r773'];
		
//		if ($('#chart10_epr1').val()!='')
//			chart10eprs.push($('#chart10_epr1').val());
//		if ($('#chart10_epr2').val()!='')
//			chart10eprs.push($('#chart10_epr2').val());
//		if ($('#chart10_epr3').val()!='')
//			chart10eprs.push($('#chart10_epr3').val());
	//	
//		if(chart10eprs.length == 0)
//			$('#chart_10_div').fadeOut('fast');
//		else {
//			$('#no_chart_message_div').fadeOut('fast');
//			$('#chart_10_div').fadeIn('fast');
//		}
		
		chartFirms_json_data = JSON.stringify({
			firms: firms
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
			
			$('#chart_loader').fadeOut('fast');
			
			chart.draw(chartDataTable, chartOptions);
			
		});
		
		request.fail(function(jqXHR, textStatus) {
			console.log("Request failed: " + textStatus);
			console.log(jqXHR);
		});
	});
	
});

$('#chart_loader').fadeOut('fast');
$('#draw_chart_button').fadeOut('fast');
$('#chart_controls').fadeOut('fast');

//var chart;
//var chartDataTable;
//var chartOptions;
//var chartfirms_json_data;
//
//var CHART_YEARS = 25;
//var CHART_MEMORY = 5;
//
////years slider
//$(function() {
//	$("#chart_slider1").slider({
//		value : 25,
//		min : 25,
//		max : 50,
//		step : 1,
//		stop : function(event, ui) {
//			CHART_YEARS = ui.value;
//			$("#chart10_slider1_badge").text(ui.value);
////			if (ui.value != 1) {
////				$("#chart10_slider1_badge").text(ui.value - 1);
////			} else {
////				$("#chart10_slider1_badge").text(ui.value);
////			}
//			drawChart();
//		}
//	});
//	$("#chart_slider1_badge").text($("#chart_slider1").slider("value"));
//});
//
//$(function() {
//	$("#chart_slider2").slider({
//		value : 5,
//		min : 1,
//		max : 10,
//		step : 1,
//		stop : function(event, ui) {
//			CHART_MEMORY = ui.value;
//			$("#chart_slider2_badge").text(CHART_MEMORY);
//			drawChart();
//		}
//	});
//	$("#chart_slider_badge").text(5);
//});
//
//function errorHandler(errorMessage) {
//    google.visualization.errors.removeError(errorMessage.id);
//}

//google.setOnLoadCallback(drawChart);

//function drawChart() {
	
//	$('#chart_loader').fadeIn('fast');
	
//	ContinuityChangeChart = '/continuityChangeChart/type/' + CHART_10_DEFAULT_IPCRANGE + '/top/'+ CHART_10_YEARS + '/memory/'+ CHART_10_MEMORY;
//	ContinuityChangeChart = '/chart/top/35/memory/35';
//
//	
//	chartFirms = ['r766', 'r773'];
	
//	if ($('#chart10_epr1').val()!='')
//		chart10eprs.push($('#chart10_epr1').val());
//	if ($('#chart10_epr2').val()!='')
//		chart10eprs.push($('#chart10_epr2').val());
//	if ($('#chart10_epr3').val()!='')
//		chart10eprs.push($('#chart10_epr3').val());
//	
//	if(chart10eprs.length == 0)
//		$('#chart_10_div').fadeOut('fast');
//	else {
//		$('#no_chart_message_div').fadeOut('fast');
//		$('#chart_10_div').fadeIn('fast');
//	}
	
//	chartFirms_json_data = JSON.stringify(chartFirms);
//	
//	var request = $.ajax({
//		type : 'POST',
//		url : ContinuityChangeChart,
//		dataType : "json",
//		contentType : 'application/json',
//		data: chartFirms_json_data
//	});
//
//	request.done(function(jsonResponse) {
//		chartDataTable = new google.visualization.arrayToDataTable(jsonResponse.data);
//		chart = new google.visualization.ComboChart(document.getElementById('chart_div'));
//		
//		chartOptions = {
//			vAxes: [
//			   {title: "Change"},
//			   {title: "Depth"}
//			],
//			hAxis: {title: CHARTLABEL_YEARS},
//			seriesType: "bars",
//			series: {
//		    	1: {type: "line",targetAxisIndex:1},
//				3: {type: "line",targetAxisIndex:1},
//				5: {type: "line",targetAxisIndex:1}
//			},
//			'fontSize': '12',	
//			'colors': ['#33B5E5','#99CC00','#AA66CC','#FFBB33','#FF4444','#0099CC'],
//			'legend' : {position: 'bottom'},
//			'fontName': 'Open Sans, sans-serif'
//		};
//		
//		$('#chart_loader').fadeOut('fast');
//		
//		google.visualization.events.addListener(chart, 'error', errorHandler); 
//		
//		chart.draw(chartDataTable, chartOptions);
//		
//	});
//	
//	request.fail(function(jqXHR, textStatus) {
//		console.log("Request failed: " + textStatus);
//		console.log(jqXHR);
//	});             
//}

//$('.chart_firmselector').change(function() {
//	drawChart();
//});