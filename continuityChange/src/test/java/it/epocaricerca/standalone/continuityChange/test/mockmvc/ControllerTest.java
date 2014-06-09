package it.epocaricerca.standalone.continuityChange.test.mockmvc;

import java.io.InputStream;

import flexjson.JSONSerializer;
import it.epocaricerca.standalone.continuityChange.Application;
import it.epocaricerca.standalone.continuityChange.repository.TagRepository;
import it.epocaricerca.standalone.continuityChange.transfer.FirmsTransfer;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class ControllerTest {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@Autowired
	private WebApplicationContext wac;

	@Autowired
	private TagRepository tagRepository;
	
	private MockMvc mockMvc;

	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}

	@Test
	public void testCSVUploadAndImport() throws Exception {
		
		InputStream is = this.getClass().getResourceAsStream("/import/315_tags_ma.txt");
		
		MockMultipartFile file = new MockMultipartFile("315_tags_ma.txt", IOUtils.toByteArray(is));

		is.close();

		// Test upload csv
		mockMvc.perform(fileUpload("/upload")
				.file(file)
				.contentType(MediaType.parseMediaType("multipart/form-data")))
				.andExpect(status().isOk());
		
		logger.info("Num of tags: " + tagRepository.count());
		
		Assert.assertEquals("Pop-Rock", this.tagRepository.findByFirmAndYear("r779", "2003").get(0));
		
		Assert.assertEquals(2, this.tagRepository.countCitationRepetitions("r766", "1983", "Pop-Rock"));
		
		Assert.assertEquals(2, this.tagRepository.countCitationRepetitions("r773", "1992", "Pop-Rock"));
		
		FirmsTransfer firms = new FirmsTransfer();
		firms.setFirms(new String[]{"r766", "r773"});
		
		mockMvc.perform(post("/chart/top/{top}/memory/{memory}", 35, 35)
				.content(new JSONSerializer().exclude("*.class").include("firms").serialize(firms))
				.accept(MediaType.parseMediaType("application/json;charset=UTF-8"))
				.contentType(MediaType.parseMediaType("application/json;charset=UTF-8")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data").isArray());
		
	}
}
