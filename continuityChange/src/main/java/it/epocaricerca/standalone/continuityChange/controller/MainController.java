package it.epocaricerca.standalone.continuityChange.controller;

import it.epocaricerca.standalone.continuityChange.parser.csv.CSVImporter;
import it.epocaricerca.standalone.continuityChange.repository.TagRepository;
import it.epocaricerca.standalone.continuityChange.transfer.Notification;
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

	private final String ENTITIES_URL = "/entities";

	private final String DROP_DATABASE_URL = "/drop";

	private final String CSV_EXPORT_URL = "/export/memory/{memory}/first/{first}/second/{second}/third/{third}/";

	private final String NOTIFICATIONS_URL = "/notifications";

	@Autowired
	private CSVImporter csvImporter;

	@Autowired
	private TagRepository tagRepository;

	@Autowired
	private ExportState exportState;

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
	public ModelAndView continuityChangeExport(@PathVariable int memory, @PathVariable boolean first, @PathVariable boolean second, 
			@PathVariable boolean third, HttpServletRequest request, HttpServletResponse response) throws Exception {
		long start = System.currentTimeMillis();
		logger.info("invocked continuityChangeChart controller");

		CSVView view = new CSVView();
		
		List<ChangeDepthThread> threads = new ArrayList<MainController.ChangeDepthThread>();

		this.exportState.reset();
		
		//1. Get labels
		ArrayList<String> labels = new ArrayList<String>();
		labels.add("Time");
		labels.add("EntityId");
		if(first) {
			labels.add("Change1");
			labels.add("Depth1");
		}
		if(second) {
			labels.add("Change2");
			labels.add("Depth2");
		}
		if(third) {
			labels.add("Change3");
			labels.add("Depth3");
		}

		List<String> allEntities = this.tagRepository.findDistinctEntities();
		List<Object[]> data = new ArrayList<Object[]>();
		data.add(labels.toArray());
		
		logger.info("Total entities: " + allEntities.size());
		
		int numberOfCores = Runtime.getRuntime().availableProcessors();
		
		logger.info("N of threads: " + Runtime.getRuntime().availableProcessors());
		
		int chunk = allEntities.size()/numberOfCores;
		
		List<List<String>> subLists = new ArrayList<List<String>>();
		int index = 0;
		for (int i = 0; i < (numberOfCores-1); i++) {
			subLists.add(allEntities.subList(index, (index + chunk)));
			index += chunk;
		}
		subLists.add(allEntities.subList(index, (allEntities.size())));
		
		for (List<String> subList : subLists) {
			
			ChangeDepthThread thread = new ChangeDepthThread(subList, data, memory, allEntities.size(), first, second, third);
			threads.add(thread);
			thread.start();
		}

		for (ChangeDepthThread currentThread : threads) {
			currentThread.join();
		}
		
		Map<String, Object> model = getModel(request);
		model.put("dataSource", data);
		model.put("fileName", "output.csv");

		logger.info("Time to export: " + (System.currentTimeMillis() - start));
		return new ModelAndView(view, model);
	}

	private class ChangeDepthThread extends Thread {

		private List<String> entities;
		
		private List<Object[]> data;
		
		private int memory;
		
		private int totalEntities;
		
		private boolean first;
		private boolean second;
		private boolean third;

		public ChangeDepthThread(List<String> entities, List<Object[]> data, int memory, int totalEntities, boolean first,
				boolean second, boolean third) {
			this.entities = entities;
			this.data = data;
			this.memory = memory;
			this.totalEntities = totalEntities;
			this.first = first;
			this.second = second;
			this.third = third;
		}

		@Override
		public void run() {

			for (String entityId : entities) {
				
				long startEntity = System.currentTimeMillis();
				
				List<Integer> allTimes = tagRepository.findDistinctTimesForEntity(entityId);
				logger.info("Total times: " + allTimes.size());
				
				List<Object> dataForYear = new ArrayList<Object>();
				for (Integer time : allTimes) {
					long startTime = System.currentTimeMillis();
				
					dataForYear.clear();
					dataForYear.add("" + time);
					dataForYear.add(entityId);

					List<String> currentAttributes = tagRepository.findByEntityIdAndTime(entityId, time);
					logger.info("Total attributes: " + currentAttributes.size());

					//For each year get the new citations
					int previousTime = time.intValue() - 1;

					int countNewAttributesForEntity = 0;
					int totalRepetitionsForEntity = 0;
					int countNewAttributesForOthers = 0;
					int totalRepetitionsForOthers = 0;
					int countNewAttributesForAll = 0;
					int totalRepetitionsForAll = 0;

					for (String currentAttribute : currentAttributes) {
						
						long startAttribute = System.currentTimeMillis();
						boolean isNewForEntity = true; 
						boolean isNewForOthers = true;
						boolean isNewForAll = true;
						int countRepetitionsForEntity = 0;
						int countRepetitionsForOthers = 0;
						int countRepetitionsForAll = 0;
						for (int j = previousTime; j >= (previousTime - (memory-1)); j--) {
//							long start = System.currentTimeMillis();
							if(first) {
								int repetitionsForEntity = tagRepository.countAttributeRepetitionsForEntity(entityId, j, currentAttribute);
								countRepetitionsForEntity += repetitionsForEntity;
								if(repetitionsForEntity != 0)
									isNewForEntity = false;
							}
//							logger.info("Time to execute query: " + (System.currentTimeMillis() - start));
//							start = System.currentTimeMillis();
							if(second) {
								int repetitionsForOthers = tagRepository.countAttributeRepetitionsForOthers(entityId, j, currentAttribute);
								countRepetitionsForOthers += repetitionsForOthers;
								if(repetitionsForOthers != 0)
									isNewForOthers = false;
							}
//							logger.info("Time to execute query: " + (System.currentTimeMillis() - start));
//							start = System.currentTimeMillis();
							if(third) {
								int repetitionsForAll = tagRepository.countAttributeRepetitionsForAll(j, currentAttribute);
								countRepetitionsForAll += repetitionsForAll;
								if(repetitionsForAll != 0)
									isNewForAll = false;
							}
//							logger.info("Time to execute query: " + (System.currentTimeMillis() - start));
							
						}
						totalRepetitionsForEntity += countRepetitionsForEntity;
						totalRepetitionsForOthers += countRepetitionsForOthers;
						totalRepetitionsForAll += countRepetitionsForAll;
						if(first && isNewForEntity) {
							countNewAttributesForEntity++;
						}
						if(second && isNewForOthers) {
							countNewAttributesForOthers++;
						}
						if(third && isNewForAll) {
							countNewAttributesForAll++;
						}
						logger.info("Time for single attribute: " + (System.currentTimeMillis() - startAttribute));
					}

					float changeForEntity = 0;
					float depthForEntity = 0;
					float changeForOthers = 0;
					float depthForOthers = 0;
					float changeForAll = 0;
					float depthForAll = 0;
					float totalAttributes = currentAttributes.size();

					if(totalAttributes > 0) {
						if(first) {
							changeForEntity = countNewAttributesForEntity/totalAttributes;
							depthForEntity = totalRepetitionsForEntity/totalAttributes;
						}
						if(second) {
							changeForOthers = countNewAttributesForOthers/totalAttributes;
							depthForOthers = totalRepetitionsForOthers/totalAttributes;
						}
						if(third) {
							changeForAll = countNewAttributesForAll/totalAttributes;
							depthForAll = totalRepetitionsForAll/totalAttributes;
						}
					}
					logger.info("ChangeForEntity: " + changeForEntity + " DepthForEntity: " + depthForEntity + 
							" ChangeForOthers: " + changeForOthers + " DepthForOthers: " + depthForOthers + 
							" ChangeForAll: " + changeForAll + " DepthForAll: " + depthForAll + " for year " + time.intValue());
					logger.info("");
					if(first) {
						dataForYear.add(changeForEntity);
						dataForYear.add(depthForEntity);
					}
					if(second) {
						dataForYear.add(changeForOthers);
						dataForYear.add(depthForOthers);
					}
					if(third) {
						dataForYear.add(changeForAll);
						dataForYear.add(depthForAll);
					}
					data.add(dataForYear.toArray());
					
					logger.info("Time for single time: " + (System.currentTimeMillis() - startTime));
				}

				exportState.increment();
				exportState.setValue(exportState.getCounter()*100/totalEntities);
				
				logger.info("Time for single entity: " + (System.currentTimeMillis() - startEntity));
			}
			
		}
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
	
	@RequestMapping(value = NOTIFICATIONS_URL, method = RequestMethod.GET)
	@ResponseBody
	public Notification getNotifications(HttpServletRequest request, HttpSession session, HttpServletResponse response) throws Exception {
		
		Notification notification = new Notification(exportState.getValue(), "");
		return notification;
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
