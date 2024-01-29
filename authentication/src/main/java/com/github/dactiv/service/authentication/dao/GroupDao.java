
package com.github.dactiv.service.authentication.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.dactiv.service.authentication.domain.entity.GroupEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * tb_group 用户组数据访问
 *
 * <p>Table: tb_group - 用户组</p>
 *
 * @author maurice
 * @see GroupEntity
 * @since 2021-08-22 04:45:14
 */
@Mapper
@Repository
public interface GroupDao extends BaseMapper<GroupEntity> {

}
