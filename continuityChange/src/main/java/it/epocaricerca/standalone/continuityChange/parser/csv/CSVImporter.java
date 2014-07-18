package it.epocaricerca.standalone.continuityChange.parser.csv;

import it.epocaricerca.standalone.continuityChange.model.Tag;
import it.epocaricerca.standalone.continuityChange.repository.TagRepository;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CSVImporter {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	private List<FileLine> lines = new ArrayList<FileLine>();

	@Autowired
	private TagRepository tagRepository;

	public void importCSVLines(String csvFile) throws Exception {
		logger.info("csvFile " + csvFile);

		FlatFileItemReader<FileLine> reader = FlatFileReadearFactory.createCsvFileLineReader(csvFile);
		reader.open(new ExecutionContext());

		FileLine nextLine;
		int count = 0;
		while ((nextLine = reader.read()) != null) {
			lines.add(nextLine);

			Tag tag = new Tag();
			tag.setEntityId(nextLine.getEntityId());
			tag.setProductId(nextLine.getProductId());
			if(nextLine.getTime() != null && !nextLine.getTime().equals("") && !nextLine.getTime().equals("NULL"))
				tag.setTime(Integer.parseInt(nextLine.getTime()));
			else
				tag.setTime(0);
			tag.setAttribute(nextLine.getAttribute());
			tagRepository.save(tag);
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
