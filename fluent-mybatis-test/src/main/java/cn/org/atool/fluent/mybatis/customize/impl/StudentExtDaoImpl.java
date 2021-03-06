package cn.org.atool.fluent.mybatis.customize.impl;

import cn.org.atool.fluent.mybatis.base.IBaseDao;
import cn.org.atool.fluent.mybatis.customize.StudentExtDao;
import cn.org.atool.fluent.mybatis.generate.dao.base.StudentBaseDao;
import cn.org.atool.fluent.mybatis.generate.entity.StudentEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class StudentExtDaoImpl extends StudentBaseDao implements StudentExtDao, IBaseDao<StudentEntity> {
    @Override
    public int count(String userName) {
        return super.query()
            .where.userName().eq(userName)
            .end()
            .execute(super::count);
    }

    @Override
    public List<String> selectFields(Long... ids) {
        return super.listEntity(
            super.query()
                .where.id().in(ids)
                .end()
        ).stream()
            .map(StudentEntity::getUserName)
            .collect(Collectors.toList());
    }

    @Override
    public List<StudentEntity> selectList(Long... ids) {
        return super.query()
            .where.id().in(ids).end()
            .execute(super::listEntity);
    }

    @Override
    public List<String> selectObjs(Long... ids) {
        return super.listPoJos(
            super.query()
                .select.apply(userName).end()
                .where.id().in(ids).end(),
            (map) -> (String) map.get(userName.column)
        );
    }

    @Override
    public List<String> selectObjs2(Long... ids) {
        return super.listPoJos(
            super.query()
                .select.apply(userName, age).end()
                .where.id().in(ids).end(),
            (map) -> (String) map.get(userName.column)
        );
    }

    @Override
    public StudentEntity selectOne(String likeName) {
        return super.query()
            .where.userName().like(likeName)
            .end()
            .execute(super::findOne)
            .orElse(null);
    }

    @Override
    public String selectOne(long id) {
        return super.findOne(super.query()
            .where.id().eq(id).end())
            .map(StudentEntity::getUserName)
            .orElse(null);
    }

    @Override
    public void deleteByQuery(String username) {
        super.query()
            .where.userName().eq(username).end()
            .execute(super::deleteBy);
    }

    @Override
    public void deleteByQuery(String... userNames) {
        super.query()
            .where.userName().in(userNames).end()
            .execute(super::deleteBy);
    }

    @Override
    public void updateUserNameById(String newUserName, long id) {
        super.updater()
            .update.userName().is(newUserName).end()
            .where.id().eq(id).end()
            .execute(super::updateBy);
    }
}