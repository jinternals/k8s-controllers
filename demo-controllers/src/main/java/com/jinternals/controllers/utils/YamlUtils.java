package com.jinternals.controllers.utils;

import io.kubernetes.client.util.Yaml;
import lombok.SneakyThrows;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;

public class YamlUtils {

    private YamlUtils(){
        new RuntimeException("private constructor");
    }

    @SneakyThrows
    public static <T> T loadYamlAs(Resource resource, Class<T> clzz) {
        var yaml = FileCopyUtils.copyToString(new InputStreamReader(resource.getInputStream()));
        return Yaml.loadAs(yaml, clzz);
    }
}
