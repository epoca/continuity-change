package it.epocaricerca.standalone.continuityChange.controller;

import it.epocaricerca.standalone.continuityChange.parser.csv.CSVImporter;
import it.epocaricerca.standalone.continuityChange.repository.TagRepository;
import it.epocaricerca.standalone.continuityChange.transfer.FirmsTransfer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Controller
public class ChartController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	private final String UPLOAD_CSV_URL = "/upload";
	
	private final String CHART_URL = "/chart/top/{top}/memory/{memory}";
	
	@Autowired
	private CSVImporter csvImporter;
	
	@Autowired
	private TagRepository tagRepository;

	@RequestMapping(value = UPLOAD_CSV_URL, method = RequestMethod.POST)
	@ResponseBody
	public void uploadData(HttpServletRequest request, HttpSession session, HttpServletResponse response) throws Exception {

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
	}
	
	@RequestMapping(value=CHART_URL, method = RequestMethod.POST)
	@ResponseBody
	public LinkedHashMap<String, Object> continuityChangeChart(@PathVariable int top, @PathVariable int memory,
					@RequestBody FirmsTransfer firms, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
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
			
			List<String> currentTags = null;
			List<Object> dataForYear = new ArrayList<Object>();
			dataForYear.add("" + i);

			for (String firm : firms.getFirms()) {
				//2. Get tags by year
				currentTags = this.tagRepository.findByFirmAndYear(firm, "" + i);

				//3. For each year get the new citations
				int previousYear = i - 1;
				logger.info("Selected Year: " + i);

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
				dataForYear.add(score);
				dataForYear.add(depth);
			}
			
			data.add(dataForYear.toArray());
		}
		
		result.put("data", data);
		
		return result;
		
	}
}
