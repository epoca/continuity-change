package it.epocaricerca.standalone.continuityChange.controller;

import it.epocaricerca.standalone.continuityChange.parser.csv.CSVImporter;
import it.epocaricerca.standalone.continuityChange.repository.TagRepository;
import it.epocaricerca.standalone.continuityChange.transfer.Response;
import it.epocaricerca.standalone.continuityChange.view.CSVView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class MainController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	private final String UPLOAD_CSV_URL = "/upload";
	
	private final String CHART_URL = "/chart/memory/{memory}";

	private final String ENTITIES_URL = "/entities";

	private final String DROP_DATABASE_URL = "/drop";

	private final String CSV_EXPORT_URL = "/export/memory/{memory}";
	
	@Autowired
	private CSVImporter csvImporter;
	
	@Autowired
	private TagRepository tagRepository;

	private List<String> privateEntities = new ArrayList<String>();
	
	@RequestMapping(value = ENTITIES_URL, method = RequestMethod.GET)
	@ResponseBody
	public String[] getEntities(HttpServletRequest request, HttpSession session, HttpServletResponse response) throws Exception {
		return this.privateEntities.toArray(new String[]{});
	}
	
	@RequestMapping(value = UPLOAD_CSV_URL, method = RequestMethod.POST)
	@ResponseBody
	public Response uploadData(HttpServletRequest request, HttpSession session, HttpServletResponse response) throws Exception {
		long start = System.currentTimeMillis();
		logger.info("Received upload multipart request");
		File destFile = null;
		MultipartHttpServletRequest mrequest = (MultipartHttpServletRequest) request;
		Map<String, MultipartFile> fileMap = mrequest.getFileMap();

		for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet()) {
			MultipartFile mfile = entry.getValue();

			if (mfile.getBytes().length > 0) {
				destFile = new File(mfile.getName());

				logger.info("Create " + destFile);

				BufferedInputStream is = new BufferedInputStream(mfile.getInputStream());

				// write the current file to disk
				FileOutputStream fos = new FileOutputStream(destFile);
				BufferedOutputStream dest = new BufferedOutputStream(fos,1024);

				IOUtils.copy(is, dest);
				
				is.close();
				dest.close();
			}
		}

		csvImporter.importCSVLines(destFile.getAbsolutePath());
		destFile.delete();
		
		privateEntities.clear();
		privateEntities = this.tagRepository.findDistinctAttributes();
		
		Response uploadResponse = new Response();
		uploadResponse.setSuccess("true");
		logger.info("Time to upload: " + (System.currentTimeMillis() - start));
		return uploadResponse;
	}
	
	@RequestMapping(value = CSV_EXPORT_URL, method = RequestMethod.POST, headers = "content-type=application/x-www-form-urlencoded;charset=UTF-8")
	@ResponseBody
	public ModelAndView continuityChangeExport(@PathVariable int memory, HttpServletRequest request, HttpServletResponse response) throws Exception {
		long start = System.currentTimeMillis();
		logger.info("invocked continuityChangeChart controller");
		
		CSVView view = new CSVView();
		
		//1. Get labels
		ArrayList<String> labels = new ArrayList<String>();
		labels.add("Time");
		labels.add("EntityId");
		labels.add("Change1");
		labels.add("Change2");
		labels.add("Change3");
		labels.add("Depth1");
		labels.add("Depth2");
		labels.add("Depth3");

		List<String> allEntities = this.tagRepository.findDistinctEntities();
		List<Object[]> data = new ArrayList<Object[]>();
		data.add(labels.toArray());
		
		for (String entityId : allEntities) {
			logger.info("Selected entity: " + entityId);
			List<Integer> allTimes = this.tagRepository.findDistinctTimesForEntity(entityId);

			List<Object> dataForYear = new ArrayList<Object>();
			
			for (Integer time : allTimes) {
				logger.info("Selected time: " + time);
				dataForYear.clear();
				dataForYear.add("" + time);
				dataForYear.add(entityId);
				
				List<String> currentAttributes = this.tagRepository.findByEntityIdAndTime(entityId, time);
				
				//For each year get the new citations
				int previousTime = time.intValue() - 1;

				int countNewAttributesForEntity = 0;
				int totalRepetitionsForEntity = 0;
				int countNewAttributesForOthers = 0;
				int totalRepetitionsForOthers = 0;
				int countNewAttributesForAll = 0;
				int totalRepetitionsForAll = 0;
				
				for (String currentAttribute : currentAttributes) {
					boolean isNewForEntity = true; 
					boolean isNewForOthers = true;
					boolean isNewForAll = true;
					int countRepetitionsForEntity = 0;
					int countRepetitionsForOthers = 0;
					int countRepetitionsForAll = 0;
					for (int j = previousTime; j >= (previousTime - (memory-1)); j--) {
						int repetitionsForEntity = this.tagRepository.countAttributeRepetitionsForEntity(entityId, j, currentAttribute);
						int repetitionsForOthers = this.tagRepository.countAttributeRepetitionsForOthers(entityId, j, currentAttribute);
						int repetitionsForAll = this.tagRepository.countAttributeRepetitionsForAll(j, currentAttribute);
						
						countRepetitionsForEntity += repetitionsForEntity;
						countRepetitionsForOthers += repetitionsForOthers;
						countRepetitionsForAll += repetitionsForAll;
						
						if(repetitionsForEntity != 0)
							isNewForEntity = false;
						if(repetitionsForOthers != 0)
							isNewForOthers = false;
						if(repetitionsForAll != 0)
							isNewForAll = false;
					}
					totalRepetitionsForEntity += countRepetitionsForEntity;
					totalRepetitionsForOthers += countRepetitionsForOthers;
					totalRepetitionsForAll += countRepetitionsForAll;
					if(isNewForEntity) {
						countNewAttributesForEntity++;
					}
					if(isNewForOthers) {
						countNewAttributesForOthers++;
					}
					if(isNewForAll) {
						countNewAttributesForAll++;
					}
				}
				
				float changeForEntity = 0;
				float depthForEntity = 0;
				float changeForOthers = 0;
				float depthForOthers = 0;
				float changeForAll = 0;
				float depthForAll = 0;
				float totalAttributes = currentAttributes.size();
				
				if(totalAttributes > 0) {
					changeForEntity = countNewAttributesForEntity/totalAttributes;
					depthForEntity = totalRepetitionsForEntity/totalAttributes;
					changeForOthers = countNewAttributesForOthers/totalAttributes;
					depthForOthers = totalRepetitionsForOthers/totalAttributes;
					changeForAll = countNewAttributesForAll/totalAttributes;
					depthForAll = totalRepetitionsForAll/totalAttributes;
				}
				logger.info("ChangeForEntity: " + changeForEntity + " DepthForEntity: " + depthForEntity + 
						" ChangeForOthers: " + changeForOthers + " DepthForOthers: " + depthForOthers + 
						 " ChangeForAll: " + changeForAll + " DepthForAll: " + depthForAll + " for year " + time.intValue());
				logger.info("");
				dataForYear.add(changeForEntity);
				dataForYear.add(changeForOthers);
				dataForYear.add(changeForAll);
				dataForYear.add(depthForEntity);
				dataForYear.add(depthForOthers);
				dataForYear.add(depthForAll);
				data.add(dataForYear.toArray());
				
			}
		}
		
		Map<String, Object> model = getModel(request);
		model.put("dataSource", data);
		model.put("fileName", "output.csv");

		logger.info("Time to export: " + (System.currentTimeMillis() - start));
		return new ModelAndView(view, model);
	}
	
	@RequestMapping(value = DROP_DATABASE_URL, method = RequestMethod.GET)
	@ResponseBody
	public Response dropDatabase(HttpServletRequest request, HttpSession session) throws Exception {
		
		logger.info("Dropping database...");
	
		this.tagRepository.deleteAll();
		this.privateEntities.clear();
		
		Response response = new Response();
		response.setSuccess("true");
		return response;
	}
	
	public Map<String, Object> getModel(HttpServletRequest request) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("format", getFormat(request));
		return model;
	}

	private String getFormat(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String format = uri.substring(uri.lastIndexOf(".") + 1);
		return format;
	}
	
//	@ExceptionHandler(Exception.class)
//	public void handleException(Exception ex, HttpServletResponse response) {
//		try {
//			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
}
