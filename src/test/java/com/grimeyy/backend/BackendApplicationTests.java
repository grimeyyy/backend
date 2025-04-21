package com.grimeyy.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}
	
	@Test
    void mainMethodRunsWithoutExceptions() {
        String[] args = new String[] {};

        BackendApplication.main(args);
    }

}
