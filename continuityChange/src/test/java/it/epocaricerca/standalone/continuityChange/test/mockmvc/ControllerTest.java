package it.epocaricerca.standalone.continuityChange.test.mockmvc;

import java.io.InputStream;

import it.epocaricerca.standalone.continuityChange.controller.Application;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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

	@Autowired
	private WebApplicationContext wac;

	private MockMvc mockMvc;

	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}

	@Test
	public void testCSVUploadAndImport() throws Exception {
		
		InputStream is = this.getClass().getResourceAsStream("/import/315_tags_ma.txt");
		
		MockMultipartFile file = new MockMultipartFile("315_tags_ma.txt", IOUtils.toByteArray(is));

		// Test upload csv
		MvcResult result = mockMvc.perform(fileUpload("/upload")
				.file(file)
				.contentType(MediaType.parseMediaType("multipart/form-data")))
				.andExpect(status().isOk())
				.andReturn();
		
		is.close();
	}
}
