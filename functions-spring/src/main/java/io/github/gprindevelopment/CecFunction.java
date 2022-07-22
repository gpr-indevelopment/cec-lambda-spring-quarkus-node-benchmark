package io.github.gprindevelopment;

import io.github.gprindevelopment.deputados.DeputadoClient;
import io.github.gprindevelopment.dominio.Deputado;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

@Component
public class CecFunction implements Function<Map<String, String>, Deputado> {

    private final DeputadoClient deputadoClient = new DeputadoClient();

    @Override
    public Deputado apply(Map<String, String> inputJson) {
        try {
            System.out.println("Bateu aqui!");
            return deputadoClient.consultarDeputadoPorId(Integer.parseInt(inputJson.get("body"))).get();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
