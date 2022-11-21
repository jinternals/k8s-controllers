package com.jinternals.controllers.utils;

import io.kubernetes.client.util.Yaml;
import lombok.SneakyThrows;
import org.springframework.core.io.Resource;

import java.io.InputStreamReader;

import static org.springframework.util.FileCopyUtils.copyToString;

public class YamlUtils {

    private YamlUtils(){
       throw new RuntimeException("private constructor");
    }

    @SneakyThrows
    public static <T> T loadYamlAs(Resource resource, Class<T> klass) {
        var yaml = copyToString(new InputStreamReader(resource.getInputStream()));
        return Yaml.loadAs(yaml, klass);
    }
}
