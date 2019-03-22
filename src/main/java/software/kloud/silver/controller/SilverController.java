package software.kloud.silver.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import software.kloud.sc.ResponseStatus;
import software.kloud.sc.SilverCommunication;
import software.kloud.sc.StatusResponseDTO;
import software.kloud.sc.TransferDTO;
import software.kloud.silver.persistence.DatabaseReader;
import software.kloud.silver.redis.entities.Page;
import software.kloud.silver.redis.util.Serializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.CRC32;

@Controller
public class SilverController {
    private final static Logger log = LoggerFactory.getLogger(SilverController.class);
    private final static Map<String, Class<? extends SilverCommunication>> classMap = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Serializer serializer;
    private final RedisTemplate<String, Page> redisTemplate;
    private final DatabaseReader dbReader;

    @Value("${silver.redis.queues.wait}")
    private String WAIT_QUEUE;

    @Autowired
    public SilverController(Serializer serializer, RedisTemplate<String, Page> redisTemplate, DatabaseReader dbReader) {
        this.serializer = serializer;
        this.redisTemplate = redisTemplate;
        this.dbReader = dbReader;
    }

    private static synchronized Class<? extends SilverCommunication> classLookup(String path) throws ClassNotFoundException {
        if (classMap.containsKey(path)) {
            return classMap.get(path);
        }

        //noinspection unchecked
        var clazz = (Class<? extends SilverCommunication>) Class.forName(path);
        classMap.put(path, clazz);
        return clazz;
    }

    // TODO figure out path
    @PostMapping("/")
    public <T extends SilverCommunication> ResponseEntity<StatusResponseDTO> post(@RequestBody() TransferDTO transferDTO) {
        long checksum = calculateChecksum(transferDTO);

        if (!(checksum == transferDTO.getChecksum())) {
            var res = new StatusResponseDTO(ResponseStatus.TRANSFER_ERROR, "Checksum mismatch");
            return ResponseEntity.badRequest().body(res);
        }

        T data;
        try {
            //noinspection unchecked
            data = (T) objectMapper.readValue(transferDTO.getPayload(), classLookup(transferDTO.getClazz()));
        } catch (ClassCastException | ClassNotFoundException | IOException e) {
            String errorMsg = "Got invalid json";
            log.error(errorMsg, e);
            return ResponseEntity.status(500).body(new StatusResponseDTO(ResponseStatus.TRANSFER_ERROR, errorMsg));
        }

        data.setSilverIdentifier(UUID.randomUUID().toString());
        Page redisData = null;
        try {
            redisData = serializer.serialize(data);
        } catch (JsonProcessingException e) {
            String errorMsg = "Could not serialize into redis page";
            log.error(errorMsg, e);
            return ResponseEntity.status(500).body(new StatusResponseDTO(ResponseStatus.TRANSFER_ERROR, errorMsg));
        }

        redisTemplate.opsForValue().set(redisData.getKey(), redisData);
        redisTemplate.opsForList().leftPush(WAIT_QUEUE, redisData);

        var resp = new StatusResponseDTO(redisData.getKey());

        // TODO: write data to redis
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/")
    public ResponseEntity<SilverCommunication> get(@RequestParam("key") String key, @RequestParam("clazz") String path) {
        //noinspection unchecked
        var page = redisTemplate.opsForValue().get(key);

        if (page != null) {
            try {
                return ResponseEntity.ok(serializer.deserialize(page, page.getTypeAsClass()));
            } catch (IOException e) {
                String errorMsg = "Could not deserialize request body";
                log.error(errorMsg, e);
                return ResponseEntity.status(500).body(null);
            }
        }

        try {
            var clazzResp = dbReader.read(key, classLookup(path));

            if (clazzResp.isPresent()) {
                var bean = clazzResp.get();
                var redisData = serializer.serialize(bean);
                redisTemplate.opsForValue().set(redisData.getKey(), redisData);

                return ResponseEntity.ok(bean);
            }
        } catch (ClassNotFoundException  | JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
        return ResponseEntity.notFound().build();
    }

    private long calculateChecksum(@RequestBody TransferDTO transferDTO) {
        var crcGen = new CRC32();
        crcGen.update(transferDTO.getPayload().getBytes());
        return crcGen.getValue();
    }
}
