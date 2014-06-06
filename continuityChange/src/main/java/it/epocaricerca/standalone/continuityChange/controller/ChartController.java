package it.epocaricerca.standalone.continuityChange.controller;

import it.epocaricerca.standalone.continuityChange.parser.csv.CSVImporter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@Controller
public class ChartController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	private final String UPLOAD_CSV_URL = "/upload";
	
	@Autowired
	private CSVImporter csvImporter;

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
}
