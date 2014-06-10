package it.epocaricerca.standalone.continuityChange.controller;

import it.epocaricerca.standalone.continuityChange.parser.csv.CSVImporter;
import it.epocaricerca.standalone.continuityChange.repository.TagRepository;
import it.epocaricerca.standalone.continuityChange.transfer.FirmsForm;
import it.epocaricerca.standalone.continuityChange.transfer.Response;
import it.epocaricerca.standalone.continuityChange.transfer.FirmsTransfer;
import it.epocaricerca.standalone.continuityChange.view.CSVView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ChartController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	private final String UPLOAD_CSV_URL = "/upload";
	
	private final String CHART_URL = "/chart/top/{top}/memory/{memory}";

	private final String FIRMS_URL = "/firms";

	private final String DROP_DATABASE_URL = "/drop";

	private final String CSV_EXPORT_URL = "/export/top/{top}/memory/{memory}";
	
	@Autowired
	private CSVImporter csvImporter;
	
	@Autowired
	private TagRepository tagRepository;

	private List<String> privateFirms = new ArrayList<String>();
	
	@RequestMapping(value = FIRMS_URL, method = RequestMethod.GET)
	@ResponseBody
	public String[] getFirms(HttpServletRequest request, HttpSession session, HttpServletResponse response) throws Exception {
		return this.privateFirms.toArray(new String[]{});
	}
	
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
		
		privateFirms.clear();
		privateFirms = this.tagRepository.findDistinctFirms();
		
		Response uploadResponse = new Response();
		uploadResponse.setSuccess("true");
		
		return uploadResponse;
	}
	
	@RequestMapping(value=CHART_URL, method = RequestMethod.POST)
	@ResponseBody
	public LinkedHashMap<String, Object> continuityChangeChart(@PathVariable int top, @PathVariable int memory,
					@RequestBody FirmsTransfer firms, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		logger.info("invocked continuityChangeChart controller");
		
		//1. Get labels
		ArrayList<String> firms_label = new ArrayList<String>();
		firms_label.add("Year");

		for (String firm : firms.getFirms()) {
			firms_label.add(firm);
			firms_label.add(firm);
		}
		
		Calendar calendar = Calendar.getInstance();
		int currentYear = calendar.get(Calendar.YEAR);
		calendar.add(Calendar.YEAR, -top);
		int lastYear = calendar.get(Calendar.YEAR);
		
		LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
		List<Object[]> data = new ArrayList<Object[]>();
		data.add(firms_label.toArray());
		
		for (int i = lastYear; i <= currentYear; i++) {

			logger.info("Selected Year: " + i);
			List<String> currentTags = null;
			List<Object> dataForYear = new ArrayList<Object>();
			dataForYear.add("" + i);

			for (String firm : firms.getFirms()) {

				logger.info("Selected firm: " + firm);
				
				//2. Get tags by year
				currentTags = this.tagRepository.findByFirmAndYear(firm, "" + i);

				//3. For each year get the new citations
				int previousYear = i - 1;

				int countNewCitations = 0;
				int totalRepetitions = 0;
				
				for (String currentCitation : currentTags) {
					boolean isNew = true; 
					int countRepetitions = 0;
					for (int j = previousYear; j >= (previousYear - (memory-1)); j--) {
						int repetitions = this.tagRepository.countCitationRepetitions(firm, "" + j, currentCitation);
						
						countRepetitions += repetitions;
						
						if(repetitions != 0)
							isNew = false;
					}
					logger.info("citation: " + currentCitation + " total repetitions: " + countRepetitions);
					totalRepetitions += countRepetitions;
					if(isNew) {
						countNewCitations++;
					}
				}
				
				float score = 0;
				float depth = 0;
				float totalCitations = currentTags.size();
				
				logger.info("New citations: " + countNewCitations + " total: " + totalCitations);
				
				if(totalCitations > 0) {
					score = countNewCitations/totalCitations;
					depth = totalRepetitions/totalCitations;
				}
				logger.info("Score: " + score + " Depth: " + depth + " for year " + i);
				logger.info("");
				dataForYear.add(score);
				dataForYear.add(depth);
			}
			
			data.add(dataForYear.toArray());
		}
		
		result.put("data", data);
		
		return result;
	}
	
	@RequestMapping(value = DROP_DATABASE_URL, method = RequestMethod.GET)
	@ResponseBody
	public Response dropDatabase(HttpServletRequest request, HttpSession session) throws Exception {
		
		logger.info("Dropping database...");
	
		this.tagRepository.deleteAll();
		this.privateFirms.clear();
		
		Response response = new Response();
		response.setSuccess("true");
		return response;
	}
	
	@RequestMapping(value = CSV_EXPORT_URL, method = RequestMethod.POST, headers = "content-type=application/x-www-form-urlencoded;charset=UTF-8")
	protected ModelAndView generateCSVExport(@PathVariable int top, @PathVariable int memory,
			@ModelAttribute FirmsForm form, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		logger.info("invocked generateCSVExport controller");
		
		CSVView view = new CSVView();
		
		//1. Get labels
		List<String> firms = new ArrayList<String>();
		
		ArrayList<String> firms_label = new ArrayList<String>();
		firms_label.add("Year");

		if(form.getFirm1() != null && !form.getFirm1().equals(""))
			firms.add(form.getFirm1());
		if(form.getFirm2() != null && !form.getFirm2().equals(""))
			firms.add(form.getFirm2());
		if(form.getFirm3() != null && !form.getFirm3().equals(""))
			firms.add(form.getFirm3());
		
		for (String firm : firms) {
			firms_label.add(firm);
			firms_label.add(firm);
		}
		
		Calendar calendar = Calendar.getInstance();
		int currentYear = calendar.get(Calendar.YEAR);
		calendar.add(Calendar.YEAR, -top);
		int lastYear = calendar.get(Calendar.YEAR);
		
		List<Object[]> data = new ArrayList<Object[]>();
		data.add(firms_label.toArray());
		
		for (int i = lastYear; i <= currentYear; i++) {

			logger.info("Selected Year: " + i);
			List<String> currentTags = null;
			List<Object> dataForYear = new ArrayList<Object>();
			dataForYear.add("" + i);

			for (String firm : firms) {

				logger.info("Selected firm: " + firm);
				
				//2. Get tags by year
				currentTags = this.tagRepository.findByFirmAndYear(firm, "" + i);

				//3. For each year get the new citations
				int previousYear = i - 1;

				int countNewCitations = 0;
				int totalRepetitions = 0;
				
				for (String currentCitation : currentTags) {
					boolean isNew = true; 
					int countRepetitions = 0;
					for (int j = previousYear; j >= (previousYear - (memory-1)); j--) {
						int repetitions = this.tagRepository.countCitationRepetitions(firm, "" + j, currentCitation);
						
						countRepetitions += repetitions;
						
						if(repetitions != 0)
							isNew = false;
					}
					logger.info("citation: " + currentCitation + " total repetitions: " + countRepetitions);
					totalRepetitions += countRepetitions;
					if(isNew) {
						countNewCitations++;
					}
				}
				
				float score = 0;
				float depth = 0;
				float totalCitations = currentTags.size();
				
				logger.info("New citations: " + countNewCitations + " total: " + totalCitations);
				
				if(totalCitations > 0) {
					score = countNewCitations/totalCitations;
					depth = totalRepetitions/totalCitations;
				}
				logger.info("Score: " + score + " Depth: " + depth + " for year " + i);
				logger.info("");
				dataForYear.add(score);
				dataForYear.add(depth);
			}
			
			data.add(dataForYear.toArray());
		}
		
		Map<String, Object> model = getModel(request);
		model.put("dataSource", data);
		model.put("fileName", "chart_export.csv");
		return new ModelAndView(view, model);
		
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
