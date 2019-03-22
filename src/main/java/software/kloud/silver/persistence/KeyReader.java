package software.kloud.silver.persistence;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KeyReader {
    private List<String> authorizedKeys = new ArrayList<>();

    private KeyReader() {
        ObjectMapper objectMapper = new ObjectMapper();
        var fsWriter = new FsWriter();
        try {
            var json = fsWriter.read();
            System.out.println(json);
            Map<String, String> map = objectMapper.readValue(json, new TypeReference<Map<String, String>>() {
            });

            authorizedKeys.addAll(map.keySet());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static KeyReader getInstance() {
        return INSTANCE_HOLDER.instance;
    }

    public boolean isValid(String key) {
        return authorizedKeys.contains(key);
    }

    private static class INSTANCE_HOLDER {
        private static final KeyReader instance = new KeyReader();
    }
}
