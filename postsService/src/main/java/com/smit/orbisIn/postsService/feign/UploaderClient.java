package com.smit.orbisIn.postsService.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "UPLOADERSERVICE", configuration = FeignConfig.class)
public interface UploaderClient {

    @GetMapping("/uploads/core/presign-download")
    Map<String, String> presignDownload(@RequestParam("key") String key);
}
