package com.thinktank.block.controller;

import com.thinktank.block.dto.BlockClassifyDto;
import com.thinktank.block.service.BlockService;
import com.thinktank.common.utils.R;
import com.thinktank.generator.entity.BlockApplicationBlock;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: 弘
 * @CreateTime: 2023年09⽉22⽇ 14:46
 * @Description: 板块接口
 * @Version: 1.0
 */
@Api(tags = "板块管理接口")
@RestController
public class BlockController {
    @Autowired
    private BlockService blockService;

    @ApiOperation("获取所有板块分类")
    @GetMapping("getBlockClassify")
    public R<List<BlockClassifyDto>> getBlockClassify() {
        return R.success(blockService.getBlockClassify());
    }

    @ApiOperation("申请创建板块")
    @PostMapping("applicationBlock")
    public R<String> applicationBlock(@RequestBody BlockApplicationBlock blockApplicationBlock) {
        blockService.applicationBlock(blockApplicationBlock);
        return R.success("已提交板块创建申请，请耐心等待管理员审核处理。");
    }
}
