package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"app.load-map-on-startup=false",
		"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.jdbc.JdbcClientAutoConfiguration",
		"spring.sql.init.mode=never"
})
class DiplomApplicationTests {

	@Test
	void contextLoads() {
	}

}
