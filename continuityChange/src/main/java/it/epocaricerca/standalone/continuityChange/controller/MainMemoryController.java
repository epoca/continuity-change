package it.epocaricerca.standalone.continuityChange.controller;

import it.epocaricerca.standalone.continuityChange.parser.csv.CSVImporter;
import it.epocaricerca.standalone.continuityChange.repository.InMemoryRepository;
import it.epocaricerca.standalone.continuityChange.repository.TagRepository;
import it.epocaricerca.standalone.continuityChange.transfer.Notification;
import it.epocaricerca.standalone.continuityChange.transfer.Response;
import it.epocaricerca.standalone.continuityChange.view.CSVView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.aspectj.weaver.tools.cache.AsynchronousFileCacheBacking.ClearCommand;
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
public class MainMemoryController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	private final String UPLOAD_CSV_URL = "/upload";

	private final String DROP_DATABASE_URL = "/drop";

	private final String CSV_EXPORT_URL = "/export/memory/{memory}/";

	private final String EXPORT_NOTIFICATIONS_URL = "/notifications/export";

	private final String IMPORT_NOTIFICATIONS_URL = "/notifications/import";

	private final String TERMINATE_APPLICATION_URL = "/terminate";

	private Comparator<Integer> comparator;
	
	@Autowired
	private CSVImporter csvImporter;

	@Autowired
	private TagRepository tagRepository;

	@Autowired
	private InMemoryRepository inMemoryRepository;

	@Autowired
	private ProgressState progressState;

	@RequestMapping(value = UPLOAD_CSV_URL, method = RequestMethod.POST)
	@ResponseBody
	public Response uploadData(HttpServletRequest request, HttpSession session, HttpServletResponse response) throws Exception {
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

		int memory = 0;

		for (Integer value : this.inMemoryRepository.getEntityIntervalTime().values()) {
			if(memory < value.intValue())
				memory = value.intValue();
		}

		Response uploadResponse = new Response();
		uploadResponse.setSuccess("true");
		uploadResponse.setMaxMemory(memory);
		return uploadResponse;
	}

	@RequestMapping(value = CSV_EXPORT_URL, method = RequestMethod.POST, headers = "content-type=application/x-www-form-urlencoded;charset=UTF-8")
	@ResponseBody
	public ModelAndView continuityChangeExport(@PathVariable int memory, HttpServletRequest request, HttpServletResponse response) throws Exception {
		logger.info("invocked continuityChangeExport controller with memory " + memory);

		CSVView view = new CSVView();

		List<ChangeDepthThread> threads = new ArrayList<ChangeDepthThread>();

		this.progressState.resetExportValues();

		//1. Get labels
		ArrayList<String> labels = new ArrayList<String>();
		labels.add("Time");
		labels.add("EntityId");
		labels.add("NumChange1");
		labels.add("DenChange1");
		labels.add("Change1");
		labels.add("NumChange2");
		labels.add("DenChange2");
		labels.add("Change2");
		labels.add("NumChange3");
		labels.add("DenChange3");
		labels.add("Change3");
		labels.add("MaxNumDepth1");
		labels.add("MinNumDepth1");
		labels.add("StDevNumDepth1");
		labels.add("AvgNumDepth1");
		labels.add("NumDepth1");
		labels.add("DenDepth1");
		labels.add("Depth1");
		labels.add("MaxNumDepth2");
		labels.add("MinNumDepth2");
		labels.add("StDevNumDepth2");
		labels.add("AvgNumDepth2");
		labels.add("NumDepth2");
		labels.add("DenDepth2");
		labels.add("Depth2");
		labels.add("MaxNumDepth3");
		labels.add("MinNumDepth3");
		labels.add("StDevNumDepth3");
		labels.add("AvgNumDepth3");
		labels.add("NumDepth3");
		labels.add("DenDepth3");
		labels.add("Depth3");

		//		long instant = System.currentTimeMillis();
		//		List<String> allEntities = this.tagRepository.findDistinctEntities();
		//		logger.info("Time for loading all entities from db: " + (System.currentTimeMillis() - instant));

		long instant = System.currentTimeMillis();
		List<String> allEntities = new ArrayList<String>();
		allEntities.addAll(this.inMemoryRepository.getEntityYearsMap().keySet());
		logger.info("Time for loading all entities from memory: " + (System.currentTimeMillis() - instant));

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

			ChangeDepthThread thread = new ChangeDepthThread(subList, data, memory, allEntities.size());
			threads.add(thread);
			thread.start();
		}

		for (ChangeDepthThread currentThread : threads) {
			currentThread.join();
		}

		Map<String, Object> model = getModel(request);
		model.put("dataSource", data);
		model.put("fileName", "output.csv");
		
		return new ModelAndView(view, model);
	}

	private class ChangeDepthThread extends Thread {

		private List<String> entities;

		private List<Object[]> data;

		private int memory;

		private int totalEntities;

		public ChangeDepthThread(List<String> entities, List<Object[]> data, int memory, int totalEntities) {
			this.entities = entities;
			this.data = data;
			this.memory = memory;
			this.totalEntities = totalEntities;
		}

		@Override
		public void run() {

			for (String entityId : entities) {

				List<Integer> allTimes = new ArrayList<Integer>();
				allTimes.addAll(inMemoryRepository.getEntityYearsMap().get(entityId));

				List<Object> dataForYear = new ArrayList<Object>();

				List<Integer> depthNumsForEntity = new ArrayList<Integer>();
				List<Integer> depthNumsForOthers = new ArrayList<Integer>();
				List<Integer> depthNumsForAll = new ArrayList<Integer>();
				
				for (Integer time : allTimes) {

					dataForYear.clear();
					dataForYear.add("" + time);
					dataForYear.add(entityId);

					List<String> currentAttributes = new ArrayList<String>();
					currentAttributes.addAll(inMemoryRepository.getEntityAttributesMap().get(entityId + "|" + time));
					
					//For each year get the new citations
					int previousTime = time.intValue() - 1;

					int countNewAttributesForEntity = 0;
					int totalRepetitionsForEntity = 0;
					int countNewAttributesForOthers = 0;
					int totalRepetitionsForOthers = 0;
					int countNewAttributesForAll = 0;
					int totalRepetitionsForAll = 0;

					depthNumsForEntity.clear();
					depthNumsForOthers.clear();
					depthNumsForAll.clear();
					
					for (String currentAttribute : currentAttributes) {

						boolean isNewForEntity = true; 
						boolean isNewForOthers = true;
						boolean isNewForAll = true;
						int countRepetitionsForEntity = 0;
						int countRepetitionsForOthers = 0;
						int countRepetitionsForAll = 0;
						
						for (int j = previousTime; j >= (previousTime - (memory-1)); j--) {
							Integer repetitionsForEntity = inMemoryRepository.getEntityAttributeCount().get(entityId + "|" + j + "|" + currentAttribute);
							if(repetitionsForEntity != null && repetitionsForEntity.intValue() != 0) {
								countRepetitionsForEntity += repetitionsForEntity.intValue();
								isNewForEntity = false;
							}
							int repetitionsForOthers = inMemoryRepository.countAttributeRepetitionsForOthers(j, currentAttribute, entityId, false);
							if(repetitionsForOthers != 0) {
								countRepetitionsForOthers += repetitionsForOthers;
								isNewForOthers = false;
							}
							int repetitionsForAll = inMemoryRepository.countAttributeRepetitionsForOthers(j, currentAttribute, entityId, true);
							if(repetitionsForAll != 0) {
								countRepetitionsForAll += repetitionsForAll;
								isNewForAll = false;
							}

						}
						if(entityId.equals("185")) {
							logger.info("EntityId: " + entityId + " Time: " + time + " Attribute: " + currentAttribute + " repetitionsForOthers: " + countRepetitionsForOthers);
						}
						
						depthNumsForEntity.add(new Integer(countRepetitionsForEntity));
						depthNumsForOthers.add(new Integer(countRepetitionsForOthers));
						depthNumsForAll.add(new Integer(countRepetitionsForAll));
						
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

					float changeForEntity = 0f;
					float depthForEntity = 0f;
					float changeForOthers = 0f;
					float depthForOthers = 0f;
					float changeForAll = 0f;
					float depthForAll = 0f;
					float totalAttributes = currentAttributes.size();
					double depthNumStDev1 = 0f;
					double depthNumStDev2 = 0f;
					double depthNumStDev3 = 0f;
					double depthNumAvg1 = 0f;
					double depthNumAvg2 = 0f;
					double depthNumAvg3 = 0f;
					if(totalAttributes > 0) {
						changeForEntity = countNewAttributesForEntity/totalAttributes;
						depthForEntity = totalRepetitionsForEntity/totalAttributes;
						changeForOthers = countNewAttributesForOthers/totalAttributes;
						depthForOthers = totalRepetitionsForOthers/totalAttributes;
						changeForAll = countNewAttributesForAll/totalAttributes;
						depthForAll = totalRepetitionsForAll/totalAttributes;
						
						StandardDeviation standardDeviation = new StandardDeviation();
						Mean mean = new Mean();
						double[] numsForEntityArray = toDoubleArray(depthNumsForEntity);
						double[] numsForOthersArray = toDoubleArray(depthNumsForOthers);
						double[] numsForAllArray = toDoubleArray(depthNumsForAll);
						depthNumStDev1 = standardDeviation.evaluate(numsForEntityArray);
						depthNumStDev2 = standardDeviation.evaluate(numsForOthersArray);
						depthNumStDev3 = standardDeviation.evaluate(numsForAllArray);
						depthNumAvg1 = mean.evaluate(numsForEntityArray);
						depthNumAvg2 = mean.evaluate(numsForOthersArray);
						depthNumAvg3 = mean.evaluate(numsForAllArray);
						
					}
					
					//Num change 1
					dataForYear.add(countNewAttributesForEntity);
					//Den change 1
					dataForYear.add(totalAttributes);
					//Change 1
					dataForYear.add(changeForEntity);
					//Num change 2
					dataForYear.add(countNewAttributesForOthers);
					//Den change 2
					dataForYear.add(totalAttributes);
					//Change 2
					dataForYear.add(changeForOthers);
					//Num change 3
					dataForYear.add(countNewAttributesForAll);
					//Den change 3
					dataForYear.add(totalAttributes);
					//Change 3
					dataForYear.add(changeForAll);
					//Max num depth 1
					dataForYear.add(Collections.max(depthNumsForEntity, getComparator()));
					//Min num depth 1
					dataForYear.add(Collections.min(depthNumsForEntity, getComparator()));
					//Num st dev depth 1
					dataForYear.add(depthNumStDev1);
					//Avg num depth 1
					dataForYear.add(depthNumAvg1);
					//Num depth 1
					dataForYear.add(totalRepetitionsForEntity);
					//Den depth 1
					dataForYear.add(totalAttributes);
					//Depth 1
					dataForYear.add(depthForEntity);
					//Max num depth 2
					dataForYear.add(Collections.max(depthNumsForOthers, getComparator()));
					//Min num depth 2
					dataForYear.add(Collections.min(depthNumsForOthers, getComparator()));
					//Num st dev depth 2
					dataForYear.add(depthNumStDev2);
					//Avg num depth 2
					dataForYear.add(depthNumAvg2);
					//Num depth 2
					dataForYear.add(totalRepetitionsForOthers);
					//Den depth 2
					dataForYear.add(totalAttributes);
					//Depth 2
					dataForYear.add(depthForOthers);
					//Max num depth 3
					dataForYear.add(Collections.max(depthNumsForAll, getComparator()));
					//Min num depth 3
					dataForYear.add(Collections.min(depthNumsForAll, getComparator()));
					//Num st dev depth 3
					dataForYear.add(depthNumStDev3);
					//Avg num depth 3
					dataForYear.add(depthNumAvg3);
					//Num depth 3
					dataForYear.add(totalRepetitionsForAll);
					//Den depth 3
					dataForYear.add(totalAttributes);
					//Depth 3
					dataForYear.add(depthForAll);
					
					data.add(dataForYear.toArray());
				}

				progressState.incrementExportCounter();
				progressState.setExportValue(progressState.getExportCounter()*100/totalEntities);
			}

		}
	}

	@RequestMapping(value = DROP_DATABASE_URL, method = RequestMethod.GET)
	@ResponseBody
	public Response dropDatabase(HttpServletRequest request, HttpSession session) throws Exception {

		logger.info("Dropping database...");

		this.tagRepository.deleteAll();
		this.inMemoryRepository.resetHashMaps();
		progressState.resetImportValues();

		Response response = new Response();
		response.setSuccess("true");
		return response;
	}

	@RequestMapping(value = EXPORT_NOTIFICATIONS_URL, method = RequestMethod.GET)
	@ResponseBody
	public Notification getExportNotifications(HttpServletRequest request, HttpSession session, HttpServletResponse response) throws Exception {

		Notification notification = new Notification(progressState.getExportValue(), 0, 0, "");
		return notification;
	}

	@RequestMapping(value = TERMINATE_APPLICATION_URL, method = RequestMethod.GET)
	@ResponseBody
	public String terminate(HttpServletRequest request, HttpSession session, HttpServletResponse response) throws Exception {

		System.exit(0);
		return "";
	}

	@RequestMapping(value = IMPORT_NOTIFICATIONS_URL, method = RequestMethod.GET)
	@ResponseBody
	public Notification getImportNotifications(HttpServletRequest request, HttpSession session, HttpServletResponse response) throws Exception {
		logger.info("progressState.getImportValue() " + progressState.getImportValue());
		logger.info("progressState.getTotalImportLines() " + progressState.getTotalImportLines());
		logger.info("progressState.getImportCounter() " + progressState.getImportCounter());
		Notification notification = new Notification(progressState.getImportValue(), progressState.getTotalImportLines(), progressState.getImportCounter(), "");
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

	public Comparator<Integer> getComparator() {
		
		if(comparator == null) {
			comparator = new Comparator<Integer>() {

				@Override
				public int compare(Integer num1, Integer num2) {
					
					if(num1.intValue() > num2.intValue())
						return 1;
					else if(num1.intValue() < num2.intValue())
						return -1;
					else
						return 0;
				}
			};
		}
		
		return comparator;
		
	}
	
	public double[] toDoubleArray(List<Integer> nums) {
		
		double[] values = new double[nums.size()];
		
		for (int i = 0; i < nums.size(); i++) {
			values[i] = nums.get(i).doubleValue();
		}
		
		return values;
	}

}
