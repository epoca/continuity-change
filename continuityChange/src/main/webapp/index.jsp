<!DOCTYPE html>
<html lang="en">
<head>
<link rel="stylesheet" href="resources/css/font-awesome.min.css">
<link href="resources/css/bootstrap.min.css" rel="stylesheet">
<link href="resources/css/bootstrap-responsive.css" rel="stylesheet">
<link href="resources/css/fineuploader-3.3.0.css" rel="stylesheet">
<link type="text/css" href="resources/css/ui-lightness/jquery-ui-1.10.4.css" rel="stylesheet" />
<link href="resources/css/main.css" rel="stylesheet">
<meta charset="utf-8" />
<title>Continuity Change</title>

</head>

<body>

	<div id="index-container" class="container-fluid">
	
		<section id="content">
			<h1>Continuity change chart</h1>
			
			<div class="row-fluid">
				<div class="span3" id="csv-fine-uploader"></div>
				<div class="span3 qq-upload-button btn btn-azure" id="draw_chart_button"><i class="fa fa-bar-chart-o"></i> Draw chart</div>
			</div>
			
			<section id="section_chart">
				<div class="section-content">
					<div class="row-fluid">
						<div class="span12 chart" id="chart_div" style="height:525px"></div>
					</div>
					
					<div class="row-fluid">
						<div class="span12">
							<!-- empty row -->
						</div>
					</div>
					
					<div class="row-fluid controls_container" id="chart_controls">
						<div class="row-fluid">
							<div class="span1">
								<div class="img-outer"><img src="resources/img/loader_1.gif" id="chart_loader" alt="Chart loader"></div>
							</div>
							
							<div class="span3">
								<strong class="controls_container_label">Show the last &nbsp;</strong> <span
									class="badge badge-info" id="chart_slider1_badge">value</span><strong>
									&nbsp;years</strong>
								<div id="chart_slider1"></div>
							</div> 
								
							<div class="span3">
								<strong class="controls_container_label">Organizational memory </strong><span class="badge badge-success" id="chart_slider2_badge"></span>
								<div id="chart_slider2"></div>
							</div>
						</div>
						
						<div class="row-fluid chart_first_item">
							<div class="span2 offset1">
								<strong>Select a firm</strong>
								<div class="bfh-selectbox">
						  			<input type="hidden" class="chart_firmselector" id="chart_firm1" name="chart_firm1" value="">
						  			<a class="bfh-selectbox-toggle" role="button" data-toggle="bfh-selectbox" href="#">
						    			<span class="bfh-selectbox-option input-small" data-option=""></span>
						    			<b class="caret"></b>
						 		 	</a>
			<!-- 			  			<div class="bfh-selectbox-options"> -->
			<!-- 			    			<input type="text" class="bfh-selectbox-filter"> -->
			<!-- 			    			<div role="listbox"> -->
			<!-- 			    				<ul role="option"> -->
			<!-- 									</c:forEach> -->
			<!-- 			    				</ul> -->
			<!-- 			  				</div> -->
			<!-- 			  			</div> -->
								</div>
							</div> <!-- end of span3 -->
					
							<div class="span2">
								<strong>Select a firm</strong>
								<div class="bfh-selectbox">
						  			<input type="hidden" class="chart_firmselector" id="chart_firm2" name="chart_firm2" value="">
						 	 		<a class="bfh-selectbox-toggle" role="button" data-toggle="bfh-selectbox" href="#">
						    			<span class="bfh-selectbox-option input-small" data-option=""></span>
						    			<b class="caret"></b>
						  			</a>
			<!-- 				  		<div class="bfh-selectbox-options"> -->
			<!-- 					    	<input type="text" class="bfh-selectbox-filter"> -->
			<!-- 					    	<div role="listbox"> -->
			<!-- 					    		<ul role="option"> -->
			<!-- 									</c:forEach> -->
			<!-- 					    		</ul> -->
			<!-- 					  		</div> -->
			<!-- 				  		</div> -->
								</div>
							</div> <!-- end of span3 -->
					
							<div class="span2">
								<strong>Select a firm</strong>
								<div class="bfh-selectbox">
								  	<input type="hidden" class="chart_firmselector" id="chart_firm3" name="chart_firm3" value="">
								  	<a class="bfh-selectbox-toggle" role="button" data-toggle="bfh-selectbox" href="#">
								    	<span class="bfh-selectbox-option input-small" data-option=""></span>
								    	<b class="caret"></b>
								  	</a>
			<!-- 					  	<div class="bfh-selectbox-options"> -->
			<!-- 						    <input type="text" class="bfh-selectbox-filter"> -->
			<!-- 						    <div role="listbox"> -->
			<!-- 						    	<ul role="option"> -->
			<!-- 									</c:forEach> -->
			<!-- 						    	</ul> -->
			<!-- 						  	</div> -->
			<!-- 					  	</div> -->
								</div>
							</div> <!-- end of span3 -->
						</div>
					</div>
				</div>
			</section>
		</section>
		
	</div>

	<!-- end of container -->
	<script src="resources/js/jquery-1.11.1.min.js"></script>
	<script src="resources/js/jquery-ui-1.10.4.min.js"></script>
	<script type="text/javascript" src="https://www.google.com/jsapi"></script>
	<script type="text/javascript" src="resources/js/jquery.fineuploader-3.3.0.min.js"></script>
	<script src="resources/js/continuity-change.js"></script>

</body>
</html>
