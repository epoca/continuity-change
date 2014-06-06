package it.epocaricerca.standalone.continuityChange.parser.csv;

import it.epocaricerca.standalone.continuityChange.model.FileLine;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.stereotype.Component;

@Component
public class CSVImporter {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	private List<FileLine> lines = new ArrayList<FileLine>();

	public void importCSVLines(String csvFile) throws Exception {
		logger.info("csvFile " + csvFile);

		FlatFileItemReader<FileLine> reader = FlatFileReadearFactory.createCsvFileLineReader(csvFile);
		reader.open(new ExecutionContext());
		
		FileLine nextLine;
		int count = 0;
		while ((nextLine = reader.read()) != null) {
			lines.add(nextLine);
			logger.info("firm: " + nextLine.getFirm());
			logger.info("year: " + nextLine.getYear());
			logger.info("citations: ");
			for (String citation : nextLine.getCitations()) {
				logger.info(citation);
			}
			logger.info("");
			count++;
		}
		logger.info("Total lines: " + count);
	}

	private Map<String, Integer> populateColumnsMap(String[] firstLine) {
		Map<String, Integer> out = new HashMap<String, Integer>();
		for (int i = 0; i < firstLine.length; i++) {
			String key = firstLine[i].trim();
			key = key.replaceAll("\\p{C}", "");
			key = key.replaceAll("\\+ACI\\-", "");
			out.put(key, new Integer(i));
		}

		return out;
	}
}
