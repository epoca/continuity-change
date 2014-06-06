package it.epocaricerca.standalone.continuityChange.test.unit;

import it.epocaricerca.standalone.continuityChange.Application;
import it.epocaricerca.standalone.continuityChange.parser.csv.CSVImporter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class ApplicationTests {

	@Autowired
	private CSVImporter importer;
	
	@Test
	public void contextLoads() {}

	@Test
	public void testCSVReading() throws Exception {
		importer.importCSVLines("src/test/resources/import/315_tags_ma.txt");
	}

}
