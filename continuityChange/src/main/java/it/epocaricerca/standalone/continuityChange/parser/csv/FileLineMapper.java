package it.epocaricerca.standalone.continuityChange.parser.csv;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import it.epocaricerca.standalone.continuityChange.model.FileLine;

public class FileLineMapper implements FieldSetMapper<FileLine> {

	@Override
	public FileLine mapFieldSet(FieldSet fieldSet) throws BindException {
		
		FileLine fileLine = new FileLine();
		
		fileLine.setFirm(fieldSet.readString("FIRM"));
		fileLine.setYear(fieldSet.readString("YEAR"));
		
		String citations = fieldSet.readString("CITATIONS");
		String[] splittedValues = citations.split(" ");
		
		List<String> result = new ArrayList<String>();
		
		for (String citation : splittedValues) {
			result.add(citation);
		}
		
		fileLine.setCitations(result);

		return fileLine;
	}
	
}
