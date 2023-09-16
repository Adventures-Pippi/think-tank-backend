package com.thinktank.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.thinktank.common.exception.ThinkTankException;
import com.thinktank.common.utils.R;
import com.thinktank.generator.entity.SysUser;
import com.thinktank.generator.entity.SysUserRole;
import com.thinktank.generator.mapper.SysUserMapper;
import com.thinktank.generator.mapper.SysUserRoleMapper;
import com.thinktank.user.dto.SysUserDto;
import com.thinktank.user.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * @Author: 弘
 * @CreateTime: 2023年09⽉08⽇ 23:23
 * @Description: 用户管理接口实现类
 * @Version: 1.0
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public R<SysUser> getUserInfo(Long id) {
        // 查询用户
        SysUser sysUser = sysUserMapper.selectById(id);

        if (sysUser == null) {
            throw new ThinkTankException("用户不存在！");
        }

        sysUser.setPassword(null); // 密码是敏感数据，需要做空值处理
        return R.success(sysUser);
    }

    @Transactional
    @Override
    public R<SysUser> update(SysUserDto sysUserDto) {
        // 用户信息拷贝
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(sysUserDto, sysUser);
        sysUser.setId(StpUtil.getLoginIdAsLong()); // 前端不会传用户id，需要设置当前登录会话的id

        // 更改用户信息
        sysUserMapper.updateById(sysUser);
        return R.success(sysUser);
    }
}
