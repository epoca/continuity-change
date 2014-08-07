package it.epocaricerca.standalone.continuityChange.parser.csv;

import it.epocaricerca.standalone.continuityChange.model.Tag;
import it.epocaricerca.standalone.continuityChange.repository.InMemoryRepository;
import it.epocaricerca.standalone.continuityChange.repository.TagRepository;

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
import org.springframework.transaction.annotation.Transactional;

@Component
public class CSVImporter {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	private List<FileLine> lines = new ArrayList<FileLine>();

	@Autowired
	private TagRepository tagRepository;
	
	@Autowired
	private InMemoryRepository inMemoryRepository;

	public void importCSVLines(String csvFile) throws Exception {
		logger.info("csvFile " + csvFile);

		FlatFileItemReader<FileLine> reader = FlatFileReadearFactory.createCsvFileLineReader(csvFile);
		reader.open(new ExecutionContext());

		List<ParseCSVThread> threads = new ArrayList<CSVImporter.ParseCSVThread>();
		
		FileLine nextLine;
		int count = 0;
		int threadCount = 0;
		while ((nextLine = reader.read()) != null) {
			lines.add(nextLine);

			if(lines.size() == 5000) {
				ParseCSVThread thread = new ParseCSVThread(lines);
				threads.add(thread);
				thread.start();
				lines = new ArrayList<FileLine>();
				logger.info("Creating thread n." + threadCount);
				threadCount++;
			}
			count++;
		}

		logger.info("n of remaining lines" + lines.size());
		
		if(lines.size() > 0) {
			ParseCSVThread thread = new ParseCSVThread(lines);
			threads.add(thread);
			thread.start();
			lines = new ArrayList<FileLine>();   
			logger.info("Creating thread n." + threadCount);
			threadCount++;
		}
		
		for (ParseCSVThread parseCSVThread : threads) {
			parseCSVThread.join();
		}
		
//		for (String key : inMemoryRepository.getEntityAttributeCount().keySet()) {
//			logger.info(key + " " + inMemoryRepository.getEntityAttributeCount().get(key));
//		}
		
		List<String> allEntities = this.tagRepository.findDistinctEntities();
		for (String entityId : allEntities) {
			List<Integer> allTimes = tagRepository.findDistinctTimesForEntity(entityId);
			inMemoryRepository.addEntityYear(entityId, allTimes);
			
			for (Integer time : allTimes) {
				List<String> currentAttributes = tagRepository.findByEntityIdAndTime(entityId, time);
				inMemoryRepository.addEntityAttributes(entityId + "|" + time, currentAttributes);
			}
		}
		
//		for (String key : inMemoryRepository.getEntityYearsMap().keySet()) {
//			logger.info(key + " " + inMemoryRepository.getEntityYearsMap().get(key));
//		}
//		
//		for (String key : inMemoryRepository.getEntityAttributesMap().keySet()) {
//			logger.info(key + " " + inMemoryRepository.getEntityAttributesMap().get(key));
//		}

		logger.info("Total lines: " + count);
	}

	public class ParseCSVThread extends Thread {

		private List<FileLine> internalLines;
		
		private List<Tag> tags;

		public ParseCSVThread(List<FileLine> lines) {
			this.internalLines = lines;
			this.tags = new ArrayList<Tag>();
		}

		@Override
		public void run() {

			int count = 0;

			for (FileLine line : internalLines) {
				Tag tag = new Tag();
				tag.setEntityId(line.getEntityId());
//				logger.info("entityId " + line.getEntityId());
				tag.setProductId(line.getProductId());
				if(line.getTime() != null && !line.getTime().equals("") && !line.getTime().equals("NULL"))
					tag.setTime(Integer.parseInt(line.getTime()));
				else
					tag.setTime(0);
				tag.setAttribute(line.getAttribute());
				
				inMemoryRepository.addEntityAttributeCount(tag.getEntityId() + "|" + tag.getTime() + "|" + tag.getAttribute(), tag.getAttribute());
				
				inMemoryRepository.addAllOtherEntitiesAttributeCount(tag.getTime() + "|" + tag.getAttribute(), tag.getEntityId(), tag.getAttribute());
				
				inMemoryRepository.addAllEntitiesAttributeCount(tag.getTime() + "|" + tag.getAttribute(), tag.getAttribute());
				
				tags.add(tag);
				count++;
			}
			logger.info("Total lines: " + count);
			tagRepository.save(tags);
			logger.info("Total objects on db: " + tagRepository.count());
			
		}

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
