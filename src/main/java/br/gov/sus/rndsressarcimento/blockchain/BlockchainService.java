package br.gov.sus.rndsressarcimento.blockchain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import br.gov.sus.rndsressarcimento.persistence.entity.BlockchainEventEntity;
import br.gov.sus.rndsressarcimento.persistence.repository.BlockchainEventRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class BlockchainService {

    private final ObjectMapper objectMapper;
    private final BlockchainEventRepository blockchainEventRepository;
    private final PermissionedBlockchainGateway permissionedBlockchainGateway;
    private final List<Block> chain = new CopyOnWriteArrayList<>();

    public BlockchainService(
            ObjectMapper objectMapper,
            BlockchainEventRepository blockchainEventRepository,
            PermissionedBlockchainGateway permissionedBlockchainGateway
    ) {
        this.objectMapper = objectMapper;
        this.blockchainEventRepository = blockchainEventRepository;
        this.permissionedBlockchainGateway = permissionedBlockchainGateway;
        chain.add(new Block(0, Instant.now(), "0", "GENESIS", List.of()));
    }

    public synchronized Block appendTransaction(String smartContract, String operation, Map<String, Object> payload) {
        BlockchainTransaction tx = new BlockchainTransaction(
                UUID.randomUUID().toString(),
                smartContract,
                operation,
                Instant.now(),
                Map.copyOf(payload)
        );

        Block latest = chain.getLast();
        int index = latest.index() + 1;
        Instant now = Instant.now();
        String hash = calculateHash(index, now, latest.hash(), List.of(tx));
        Block block = new Block(index, now, latest.hash(), hash, List.of(tx));
        chain.add(block);
        persistEvent(block, tx);
        permissionedBlockchainGateway.publish(block, tx);
        return block;
    }

    public List<Block> getLedger() {
        return List.copyOf(chain);
    }

    public boolean isValid() {
        for (int i = 1; i < chain.size(); i++) {
            Block current = chain.get(i);
            Block previous = chain.get(i - 1);
            if (!current.previousHash().equals(previous.hash())) {
                return false;
            }
            String recalculated = calculateHash(
                    current.index(),
                    current.timestamp(),
                    current.previousHash(),
                    current.transactions()
            );
            if (!recalculated.equals(current.hash())) {
                return false;
            }
        }
        return true;
    }

    private String calculateHash(int index, Instant timestamp, String previousHash, List<BlockchainTransaction> transactions) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            List<Object> data = new ArrayList<>();
            data.add(index);
            data.add(timestamp.toString());
            data.add(previousHash);
            data.add(transactions);
            byte[] hash = digest.digest(toJson(data).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponivel", e);
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Falha ao serializar bloco", e);
        }
    }

    private void persistEvent(Block block, BlockchainTransaction transaction) {
        BlockchainEventEntity entity = new BlockchainEventEntity();
        entity.setId(transaction.id());
        entity.setBlockIndex(block.index());
        entity.setBlockHash(block.hash());
        entity.setPreviousHash(block.previousHash());
        entity.setSmartContract(transaction.smartContract());
        entity.setOperation(transaction.operation());
        entity.setPayloadJson(toJson(transaction.payload()));
        entity.setTimestamp(transaction.timestamp());
        blockchainEventRepository.save(entity);
    }
}
