package it.epocaricerca.standalone.continuityChange.parser.csv;

import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineCallbackHandler;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.core.io.FileSystemResource;

import it.epocaricerca.standalone.continuityChange.model.FileLine;

public class FlatFileReadearFactory {
	
	public static FlatFileItemReader<FileLine> createCsvFileLineReader(String csvFilePath) {
		FlatFileItemReader<FileLine> itemReader = new FlatFileItemReader<FileLine>();
		itemReader.setResource(new FileSystemResource(csvFilePath));
		itemReader.setLinesToSkip(1);
		itemReader.setEncoding("UTF-8");
		DefaultLineMapper<FileLine> lineMapper = new DefaultLineMapper<FileLine>();

		final DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
		tokenizer.setDelimiter(',');

		lineMapper.setLineTokenizer(tokenizer);
		lineMapper.setFieldSetMapper(new FileLineMapper());
		itemReader.setLineMapper(lineMapper);
		itemReader.setSkippedLinesCallback(new LineCallbackHandler() {
			
			public void handleLine(String line) {
				tokenizer.setNames(line.split(","));
			}
		});
		
		return itemReader;
	}
}
