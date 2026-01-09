package com.starmuseum.modules.admin;

import com.starmuseum.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PingController {

    @Operation(summary = "ping")
    @GetMapping("/ping")
    public Result<String> ping() {
        return Result.ok("pong");
    }
}
