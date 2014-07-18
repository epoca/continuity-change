<!DOCTYPE html>
<html lang="en">
<head>
<link rel="stylesheet" href="resources/css/font-awesome.min.css">
<link href="resources/css/bootstrap.min.css" rel="stylesheet">
<link href="resources/css/bootstrap-responsive.css" rel="stylesheet">
<link href="resources/css/bootstrap-formhelpers.css" rel="stylesheet">
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
				<div class="span12">
					<!-- empty row -->
				</div>
			</div>
			
			<div class="row-fluid">
				<div class="span6">
					<h3>1. Create your database</h3>
				</div>
				<div id="insert_memory_title" class="span6">
					<h3>2. Insert memory value</h3>
				</div>
			</div>
				
			<div class="row-fluid">
				<div class="span6">
					<div class="span5" id="csv-fine-uploader"></div>
				</div>
				<div class="span6 input-append" id="insert_memory_div">
				  	<input class="span2" id="insert_memory_input" type="number" required>
				  	<button id="calculate_button" class="btn" type="button">Go!</button>
				</div>
			</div>
			
			<div class="row-fluid">
				<div class="span12">
					<!-- empty row -->
				</div>
			</div>
			
			<div class="row-fluid">
				<div id="get_results_title" class="span6">
					<h3>3. Calculate results</h3>
				</div>
				<div id="drop_button_title" class="span6">
					<h3>4. Drop database and repeat</h3>
				</div>
			</div>
			
			<div class="row-fluid">
				<div class="span6">
					<div class="span5 qq-upload-button btn btn-azure" id="export_button"><i class="fa fa-download"></i> Export as CSV</div>
				</div>
				<div class="span6">
					<div class="span5 qq-upload-button btn btn-azure" id="drop_button"><i class="fa fa-trash-o"></i> Drop</div>
				</div>
			</div>
		
	</div>

	<!-- end of container -->
	<script src="resources/js/jquery-1.11.1.min.js"></script>
	<script src="resources/js/jquery-ui-1.10.4.min.js"></script>
	<script src="resources/js/bootstrap-formhelpers-selectbox.js"></script>
	<script type="text/javascript" src="https://www.google.com/jsapi"></script>
	<script type="text/javascript" src="resources/js/jquery.fineuploader-3.3.0.min.js"></script>
	<script src="resources/js/continuity-change.js"></script>

</body>
</html>
