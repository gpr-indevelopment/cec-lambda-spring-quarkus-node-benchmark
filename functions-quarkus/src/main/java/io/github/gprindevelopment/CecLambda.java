package io.github.gprindevelopment;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import io.github.gprindevelopment.deputados.DeputadoClient;
import io.github.gprindevelopment.dominio.Deputado;

import javax.inject.Named;
import java.io.IOException;
import java.util.Map;

@Named("cec")
public class CecLambda implements RequestHandler<Map<String, String>, Deputado> {

    private final DeputadoClient client = new DeputadoClient();

    @Override
    public Deputado handleRequest(Map<String, String> inputJson, Context context) {
        try {
            return client.consultarDeputadoPorId(Integer.parseInt(inputJson.get("body"))).get();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
