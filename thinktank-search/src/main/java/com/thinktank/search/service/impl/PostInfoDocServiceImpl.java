package com.thinktank.search.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.thinktank.generator.entity.BlockInfo;
import com.thinktank.generator.entity.PostComments;
import com.thinktank.generator.entity.PostInfo;
import com.thinktank.generator.entity.SysUser;
import com.thinktank.generator.mapper.BlockInfoMapper;
import com.thinktank.generator.mapper.PostCommentsMapper;
import com.thinktank.generator.mapper.PostInfoMapper;
import com.thinktank.generator.mapper.SysUserMapper;
import com.thinktank.search.doc.PostInfoDoc;
import com.thinktank.search.service.PostInfoDocService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: 弘
 * @CreateTime: 2023年10⽉21⽇ 19:22
 * @Description: 板块信息文档管理业务接口实现类
 * @Version: 1.0
 */
@Service
public class PostInfoDocServiceImpl implements PostInfoDocService {
    @Autowired
    private PostInfoMapper postInfoMapper;

    @Autowired
    private PostCommentsMapper postCommentsMapper;

    @Autowired
    private BlockInfoMapper blockInfoMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    public PostInfoDoc addPostInfoDoc(Long id) {
        // 查询帖子信息
        PostInfo postInfo = postInfoMapper.selectById(id);

        // 查询帖子前5条发言
        LambdaQueryWrapper<PostComments> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PostComments::getPostId, postInfo.getId());
        queryWrapper.isNull(PostComments::getParentId);
        queryWrapper.last("limit 5");
        List<PostComments> postComments = postCommentsMapper.selectList(queryWrapper);

        // 帖子内容
        String context = postComments.stream()
                .filter(item -> item.getTopicFlag() == 1)
                .map(PostComments::getContent)
                .findFirst()
                .orElse("");

        // 收集所有帖子评论中的图片URL
        Pattern pattern = Pattern.compile("<img\\s+src=\"([^\"]+)\"");
        List<String> imageUrlList = new ArrayList<>();
        for (PostComments comment : postComments) {
            String content = comment.getContent();
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                imageUrlList.add(matcher.group(1));
            }
        }

        // 查询板块名称
        BlockInfo blockInfo = blockInfoMapper.selectById(postInfo.getBlockId());

        // 根据帖子id查询发布帖子用户的名称
        SysUser sysUser = sysUserMapper.selectById(postInfo.getUserId());

        PostInfoDoc postInfoDoc = new PostInfoDoc();
        BeanUtils.copyProperties(postInfo, postInfoDoc);
        postInfoDoc.setContent(context);
        postInfoDoc.setBlockId(blockInfo.getId());
        postInfoDoc.setBlockName(blockInfo.getBlockName());
        postInfoDoc.setUsername(sysUser.getUsername());
        postInfoDoc.setUserId(sysUser.getId());
        postInfoDoc.setImages(imageUrlList);

        // 保存到es文档
        postInfoDoc = elasticsearchRestTemplate.save(postInfoDoc);
        return postInfoDoc;
    }
}
