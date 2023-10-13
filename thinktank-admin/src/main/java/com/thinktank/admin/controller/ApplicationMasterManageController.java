package com.thinktank.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.thinktank.admin.service.ApplicationMasterManageService;
import com.thinktank.common.utils.R;
import com.thinktank.generator.dto.BlockApplicationMasterDto;
import com.thinktank.generator.vo.BlockApplicationMasterVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: 弘
 * @CreateTime: 2023年10⽉13⽇ 17:59
 * @Description: 申请板主管理
 * @Version: 1.0
 */
@Api(tags = "申请板主管理接口")
@RestController
@RequestMapping("/applicationMasterManage")
public class ApplicationMasterManageController {
    @Autowired
    private ApplicationMasterManageService applicationMasterManageService;

    @ApiOperation("板主申请分页查询")
    @PostMapping("page")
    public R<IPage<BlockApplicationMasterVo>> page(@RequestBody BlockApplicationMasterDto blockApplicationMasterDto) {
        IPage<BlockApplicationMasterVo> page = applicationMasterManageService.page(blockApplicationMasterDto);
        return R.success(page);
    }

    @ApiOperation("通过板主申请")
    @PutMapping("allow/{id}")
    public R<String> allowApplicationBlock(@PathVariable("id") Long id) {

        return R.success("已通过该申请");
    }

    @ApiOperation("驳回板主申请")
    @PutMapping("reject/{id}")
    public R<String> rejectApplicationBlock(@PathVariable("id") Long id) {

        return R.success("已驳回该申请");
    }

    @ApiOperation("删除板主申请记录")
    @DeleteMapping("{id}")
    public R<String> deleteApplicationBlock(@PathVariable("id") Long id) {

        return R.success("已删除该申请记录");
    }
}
