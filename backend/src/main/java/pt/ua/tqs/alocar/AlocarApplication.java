package pt.ua.tqs.alocar;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Alocar API", version = "v1",
        description = "Shared light vehicles for residential condominiums"))
public class AlocarApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlocarApplication.class, args);
    }

}
