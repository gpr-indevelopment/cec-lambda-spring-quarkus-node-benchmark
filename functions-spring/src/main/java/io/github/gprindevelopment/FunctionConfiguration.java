package io.github.gprindevelopment;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FunctionConfiguration {

	// Usar header spring.cloud.function.definition na chamada para fazer routing da função.
	public static void main(String[] args) {
		// Pela minha experiencia deployar sem o SpringApplication.run diminui o tempo total de execução.
		// empty unless using Custom runtime at which point it should include
		// SpringApplication.run(FunctionConfiguration.class, args);
	}
}
