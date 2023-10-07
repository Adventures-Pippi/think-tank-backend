package com.thinktank.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thinktank.admin.service.ApplicationBlockManageService;
import com.thinktank.common.exception.ThinkTankException;
import com.thinktank.generator.dto.BlockApplicationBlockDto;
import com.thinktank.generator.entity.BlockApplicationBlock;
import com.thinktank.generator.entity.BlockInfo;
import com.thinktank.generator.entity.SysUserRole;
import com.thinktank.generator.mapper.BlockApplicationBlockMapper;
import com.thinktank.generator.mapper.BlockInfoMapper;
import com.thinktank.generator.mapper.SysUserRoleMapper;
import com.thinktank.generator.vo.BlockApplicationBlockVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author: 弘
 * @CreateTime: 2023年09⽉27⽇ 16:54
 * @Description: 板块管理接口实现类
 * @Version: 1.0
 */
@Slf4j
@Service
public class ApplicationBlockManageServiceImpl implements ApplicationBlockManageService {
    @Autowired
    private BlockApplicationBlockMapper blockApplicationBlockMapper;

    @Autowired
    private BlockInfoMapper blockInfoMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Override
    public IPage<BlockApplicationBlockVo> getApplicationBlockPage(BlockApplicationBlockDto blockApplicationBlockDto) {
        // 根据条件查询分页
        LambdaQueryWrapper<BlockApplicationBlock> wrapper = new LambdaQueryWrapper<>();

        // 若大分类id存在则新增匹配条件
        wrapper.eq(blockApplicationBlockDto.getBigTypeId() != null,
                BlockApplicationBlock::getBigTypeId,
                blockApplicationBlockDto.getBigTypeId());
        // 若大分类id存在则新增匹配条件
        wrapper.eq(blockApplicationBlockDto.getSmallTypeId() != null,
                BlockApplicationBlock::getSmallTypeId,
                blockApplicationBlockDto.getSmallTypeId());
        wrapper.orderByDesc(BlockApplicationBlock::getCreateTime);

        Page<BlockApplicationBlock> page = new Page<>(blockApplicationBlockDto.getCurrentPage(), blockApplicationBlockDto.getSize());
        IPage<BlockApplicationBlockVo> result = blockApplicationBlockMapper.getApplicationBlockPage(page, wrapper);
        return result;
    }

    // 获取该id的记录是否存在
    private BlockApplicationBlock getRecordExists(Long id) {
        BlockApplicationBlock blockApplicationBlock = blockApplicationBlockMapper.selectById(id);

        if (blockApplicationBlock == null) {
            log.error("未找到记录，该记录id为:{}", id);
            throw new ThinkTankException("未找到该申请记录!");
        }
        return blockApplicationBlock;
    }

    @Transactional
    @Override
    public void allow(Long id) {
        BlockApplicationBlock blockApplicationBlock = getRecordExists(id);

        // 若不为‘0’，则代表该申请记录已处理，无需重复处理
        if (!blockApplicationBlock.getStatus().equals(0)) {
            log.error("该记录已处理过，申请记录id:{}", blockApplicationBlock.getId());
            throw new ThinkTankException("该记录已处理过！");
        }

        // 更改申请记录状态为’通过‘
        blockApplicationBlock.setStatus(1);
        blockApplicationBlockMapper.updateById(blockApplicationBlock);

        // 将该板块信息记录到板块信息表
        BlockInfo blockInfo = new BlockInfo();
        BeanUtils.copyProperties(blockApplicationBlock, blockInfo);
        blockInfo.setAvatar("/block-avatar/default_avatar.png"); // 默认板块头像
        blockInfoMapper.insert(blockInfo);

        //TODO:将板块信息写入elasticsearch数据库


        // 为该用户分配该板块的板主角色
        SysUserRole sysUserRole = new SysUserRole();
        sysUserRole.setUserId(blockApplicationBlock.getUserId());
        sysUserRole.setRoleId(102L);
        sysUserRole.setBlockId(blockInfo.getId());
        sysUserRoleMapper.insert(sysUserRole);
    }


    @Transactional
    @Override
    public void reject(Long id) {
        // 更改申请记录状态为’驳回‘
        BlockApplicationBlock blockApplicationBlock = getRecordExists(id);

        // 若不为‘0’，则代表该申请记录已处理，无需重复处理
        if (!blockApplicationBlock.getStatus().equals(0)) {
            log.error("该记录已处理过，申请记录id:{}", blockApplicationBlock.getId());
            throw new ThinkTankException("该记录已处理过！");
        }
        blockApplicationBlock.setStatus(2);
        blockApplicationBlockMapper.updateById(blockApplicationBlock);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        BlockApplicationBlock blockApplicationBlock = getRecordExists(id);
        blockApplicationBlockMapper.deleteById(blockApplicationBlock.getId());
    }
}