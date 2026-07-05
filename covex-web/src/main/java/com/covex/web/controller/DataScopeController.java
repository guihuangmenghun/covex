package com.covex.web.controller;

import com.covex.common.annotation.RequiresPermission;
import com.covex.common.result.Result;
import com.covex.service.entity.DataScopeEntity;
import com.covex.service.service.DataScopeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "数据范围管理")
@RestController
@RequestMapping("/api/data-scope")
public class DataScopeController {

    private final DataScopeService dataScopeService;

    public DataScopeController(DataScopeService dataScopeService) {
        this.dataScopeService = dataScopeService;
    }

    @RequiresPermission(code = "role:assign_perm")
    @Operation(summary = "设置角色数据范围")
    @PostMapping("/{roleId}")
    public Result<Void> setDataScopes(@PathVariable Long roleId, @RequestBody List<DataScopeEntity> scopes) {
        dataScopeService.setDataScopes(roleId, scopes);
        return Result.ok();
    }

    @Operation(summary = "查询角色数据范围")
    @GetMapping("/{roleId}")
    public Result<List<DataScopeEntity>> getDataScopes(@PathVariable Long roleId) {
        return Result.ok(dataScopeService.getDataScopes(roleId));
    }
}
