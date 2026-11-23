package pt.ua.tqs.alocar.acceptance;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import pt.ua.tqs.alocar.TestClockConfiguration;
import pt.ua.tqs.alocar.TestcontainersConfiguration;

@CucumberContextConfiguration
@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, TestClockConfiguration.class})
public class CucumberSpringConfiguration {}
