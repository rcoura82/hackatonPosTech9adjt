package br.gov.sus.rndsressarcimento.blockchain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class PermissionedBlockchainGatewayHttp implements PermissionedBlockchainGateway {

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionedBlockchainGatewayHttp.class);

    private final boolean enabled;
    private final String apiKey;
    private final WebClient webClient;

    public PermissionedBlockchainGatewayHttp(
            @Value("${blockchain.permissioned.enabled:false}") boolean enabled,
            @Value("${blockchain.permissioned.endpoint:http://localhost:8081/api/v1/ledger/events}") String endpoint,
            @Value("${blockchain.permissioned.api-key:dev-key}") String apiKey,
            WebClient.Builder webClientBuilder
    ) {
        this.enabled = enabled;
        this.apiKey = apiKey;
        this.webClient = webClientBuilder.baseUrl(endpoint).build();
    }

    @Override
    public void publish(Block block, BlockchainTransaction transaction) {
        if (!enabled) {
            return;
        }

        Map<String, Object> body = Map.of(
                "blockIndex", block.index(),
                "blockHash", block.hash(),
                "previousHash", block.previousHash(),
                "transaction", transaction
        );

        try {
            webClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-API-KEY", apiKey)
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception ex) {
            LOGGER.warn("Falha ao publicar evento na rede permissionada: {}", ex.getMessage());
        }
    }
}
