package it.epocaricerca.standalone.continuityChange.parser.csv;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class FileLineMapper implements FieldSetMapper<FileLine> {

	@Override
	public FileLine mapFieldSet(FieldSet fieldSet) throws BindException {
		
		FileLine fileLine = new FileLine();
		
		fileLine.setAttribute(fieldSet.readString("ATTRIBUTE"));
		fileLine.setTime(fieldSet.readString("TIME"));
		fileLine.setEntityId(fieldSet.readString("ENTITYID"));
		fileLine.setProductId(fieldSet.readString("PRODUCTID"));

		return fileLine;
	}
	
}
