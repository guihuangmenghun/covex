package com.covex.web.controller;

import com.covex.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@Tag(name = "健康检查")
@RestController
public class HealthController {

    @Operation(summary = "应用健康检查")
    @GetMapping("/api/health")
    public Result<Map<String, Object>> health() {
        return Result.ok(Map.of(
            "status", "UP",
            "app", "Covex",
            "time", LocalDateTime.now().toString()
        ));
    }
}
