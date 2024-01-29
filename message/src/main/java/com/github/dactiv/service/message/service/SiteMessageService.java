package com.github.dactiv.service.message.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.support.YesOrNo;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.minio.FileObject;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.mybatis.plus.MybatisPlusQueryGenerator;
import com.github.dactiv.framework.mybatis.plus.service.BasicService;
import com.github.dactiv.framework.security.entity.TypeUserDetails;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.service.commons.service.SecurityUserDetailsConstants;
import com.github.dactiv.service.commons.service.enumerate.MessageTypeEnum;
import com.github.dactiv.service.message.dao.SiteMessageDao;
import com.github.dactiv.service.message.domain.body.site.ReadSiteMessageResponseBody;
import com.github.dactiv.service.message.domain.entity.BasicMessageEntity;
import com.github.dactiv.service.message.domain.entity.SiteMessageEntity;
import com.github.dactiv.service.message.resolver.MessageTypeResolver;
import com.github.dactiv.service.message.service.attachment.AttachmentResolver;
import com.github.dactiv.service.message.service.support.SiteMessageSender;
import nl.basjes.parse.useragent.utils.springframework.util.Assert;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * tb_site_message 的业务逻辑
 *
 * <p>Table: tb_site_message - 站内信消息</p>
 *
 * @author maurice.chen
 * @see SiteMessageEntity
 * @since 2021-12-10 09:02:07
 */
@Service
@Transactional(rollbackFor = Exception.class, propagation = Propagation.NOT_SUPPORTED, readOnly = true)
public class SiteMessageService extends BasicService<SiteMessageDao, SiteMessageEntity> implements AttachmentResolver, MessageTypeResolver {

    @Override
    public String getCategory() {
        return getMessageType();
    }

    @Override
    public List<MessageTypeEnum> getMessageTypeList(TypeUserDetails<Integer> userDetails) {

        return Arrays.asList(MessageTypeEnum.SYSTEM, MessageTypeEnum.NOTICE, MessageTypeEnum.WARNING);
    }

    public void deleteById(Collection<? extends Serializable> ids, SecurityUserDetails userDetails) {
        List<SiteMessageEntity> messages = get(ids);
        SecurityUserDetailsConstants.contains(new LinkedList<>(messages), userDetails);
        super.deleteById(ids);
    }

    /**
     * 计数站内信未读数量
     *
     * @param userDetails 用户 id
     * @return 按类型分组的未读数量
     */
    public Map<Integer, Long> countUnreadQuantity(TypeUserDetails<Integer> userDetails) {
        List<SiteMessageEntity> list = lambdaQuery()
                .select(IdEntity::getId, BasicMessageEntity::getType)
                .eq(SiteMessageEntity::getUserId, userDetails.getUserId())
                .eq(SiteMessageEntity::getUserType, userDetails.getUserType())
                .eq(SiteMessageEntity::getReadable, YesOrNo.Yes.getValue())
                .list();
        return list.stream().collect(Collectors.groupingBy(e -> e.getType().getValue(), Collectors.counting()));
    }

    /**
     * 阅读站内信
     *
     * @param types       站内信类型集合
     * @param userDetails 当前用户信息
     */
    public void read(List<Integer> types, SecurityUserDetails userDetails) {
        Date now = new Date();

        LambdaUpdateChainWrapper<SiteMessageEntity> wrapper = lambdaUpdate()
                .set(SiteMessageEntity::getReadable, YesOrNo.No.getValue())
                .set(SiteMessageEntity::getReadTime, now)
                .eq(SiteMessageEntity::getReadable, YesOrNo.Yes.getValue())
                .eq(SiteMessageEntity::getUserId, userDetails.getId())
                .eq(SiteMessageEntity::getUserType, userDetails.getType());

        if (CollectionUtils.isNotEmpty(types)) {
            wrapper = wrapper.in(SiteMessageEntity::getType, types);
        }

        wrapper.update();
    }

    @Override
    public String getMessageType() {
        return SiteMessageSender.DEFAULT_TYPE;
    }

    @Override
    public RestResult<Object> removeAttachment(Integer id, FileObject fileObject) {
        SiteMessageEntity entity = get(id);
        entity.getAttachmentList().removeIf(a -> a.getObjectName().equals(fileObject.getBucketName()) && a.getObjectName().equals(fileObject.getObjectName()));
        save(entity);
        return RestResult.ofSuccess("删除附件成功", entity);
    }

    public SiteMessageEntity read(Integer id, SecurityUserDetails userDetails) {
        SiteMessageEntity entity = get(id);

        Assert.notNull(entity, "找不到 ID 为 [" + id + "] 的站内信消息");
        SecurityUserDetailsConstants.equals(entity, userDetails);
        YesOrNo beforeReadable = entity.getReadable();

        entity.setReadable(YesOrNo.No);
        entity.setReadTime(new Date());

        updateById(entity);

        ReadSiteMessageResponseBody body = Casts.of(entity, ReadSiteMessageResponseBody.class);
        body.setBeforeReadable(beforeReadable);

        return body;
    }

    public void deleteRead(List<Integer> types, TypeUserDetails<Integer> userDetails) {
        LambdaUpdateChainWrapper<SiteMessageEntity> wrapper = lambdaUpdate()
                .eq(SiteMessageEntity::getUserId, userDetails.getUserId())
                .eq(SiteMessageEntity::getUserType, userDetails.getUserType())
                .eq(SiteMessageEntity::getReadable, YesOrNo.No.getValue());

        if (CollectionUtils.isNotEmpty(types)) {
            wrapper.in(BasicMessageEntity::getType, types);
        }

        wrapper.remove();
    }

    public Page<SiteMessageEntity> pageForFrontEnd(PageRequest pageRequest, MessageTypeEnum type, TypeUserDetails<Object> userDetails) {

        Wrapper<SiteMessageEntity> wrapper = Wrappers.<SiteMessageEntity>lambdaQuery()
                .eq(SiteMessageEntity::getUserId, userDetails.getUserId())
                .eq(SiteMessageEntity::getUserType, userDetails.getUserType())
                .eq(SiteMessageEntity::getType, type.getValue())
                .orderByDesc(IdEntity::getId);

        IPage<SiteMessageEntity> result = baseMapper.selectPage(
                MybatisPlusQueryGenerator.createQueryPage(pageRequest),
                wrapper
        );

        return MybatisPlusQueryGenerator.convertResultPage(result);
    }

    public SiteMessageEntity getForFrontEnd(Integer id, YesOrNo read, TypeUserDetails<Integer> userDetails) {
        SiteMessageEntity entity = get(id);
        SecurityUserDetailsConstants.equals(entity, userDetails);
        if (YesOrNo.Yes.equals(read) && YesOrNo.No.equals(entity.getReadable())) {
            entity.setReadable(YesOrNo.No);
            updateById(entity);
        }
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int save(SiteMessageEntity entity) {
        return super.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insert(SiteMessageEntity entity) {
        return super.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateById(SiteMessageEntity entity) {
        return super.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Collection<? extends Serializable> ids, boolean errorThrow) {
        return super.deleteById(ids, errorThrow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByEntity(Collection<SiteMessageEntity> entities, boolean errorThrow) {
        return super.deleteByEntity(entities, errorThrow);
    }
}
