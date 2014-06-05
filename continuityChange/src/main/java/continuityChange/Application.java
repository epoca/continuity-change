package continuityChange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Configuration
@ComponentScan
@Controller
@EnableAutoConfiguration
public class Application {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	@RequestMapping("/")
    @ResponseBody
    public String home() {
		logger.info("calling Application controller");
        return "Hello World!";
    }
	
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
