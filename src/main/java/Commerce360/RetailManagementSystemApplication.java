package Commerce360;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RetailManagementSystemApplication  {

	public static void main(String[] args) {
		SpringApplication.run(RetailManagementSystemApplication .class, args);
	}
}