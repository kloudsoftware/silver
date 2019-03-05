package software.kloud.silver.persistence;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KeyReader {
    private List<String> authorizedKeys = new ArrayList<>();

    private KeyReader() {
        var gson = new Gson();
        var fsWriter = new FsWriter();
        try {
            var json = fsWriter.read();
            System.out.println(json);
            Map<String, String> map = gson.fromJson(json, new TypeToken<Map<String, String>>() {
            }.getType());

            authorizedKeys.addAll(map.keySet());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isValid(String key) {
        return authorizedKeys.contains(key);
    }

    public static KeyReader getInstance() {
        return INSTANCE_HOLDER.instance;
    }

    private static class INSTANCE_HOLDER {
        private static final KeyReader instance = new KeyReader();
    }
}
