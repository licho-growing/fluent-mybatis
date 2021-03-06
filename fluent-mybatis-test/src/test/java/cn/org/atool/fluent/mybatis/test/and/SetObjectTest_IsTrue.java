package cn.org.atool.fluent.mybatis.test.and;

import cn.org.atool.fluent.mybatis.generate.mapper.StudentMapper;
import cn.org.atool.fluent.mybatis.generate.wrapper.StudentUpdate;
import cn.org.atool.fluent.mybatis.test.BaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * AndBooleanTest_IsTrue
 *
 * @author darui.wu
 * @create 2020/6/19 12:42 下午
 */
public class SetObjectTest_IsTrue extends BaseTest {
    @Autowired
    private StudentMapper mapper;

    @Test
    public void isTrue() {
        StudentUpdate update = new StudentUpdate()
            .update.userName().is("u2")
            .set.isDeleted().is(true)
            .set.addressId().isNull().end()
            .where.isDeleted().eq(false).end();
        mapper.updateBy(update);
        db.sqlList().wantFirstSql()
            .eq("UPDATE t_student SET gmt_modified = now(), user_name = ?, is_deleted = ?, address_id = ? WHERE is_deleted = ?");
    }
}
