package cn.org.atool.fluent.mybatis.method;

import cn.org.atool.fluent.mybatis.generate.ATM;
import cn.org.atool.fluent.mybatis.generate.entity.StudentEntity;
import cn.org.atool.fluent.mybatis.generate.mapper.StudentMapper;
import cn.org.atool.fluent.mybatis.test.BaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class InsertWithPkTest extends BaseTest {
    @Autowired
    private StudentMapper mapper;

    @Test
    public void testInsertWithPk() {
        db.table(ATM.Table.student).clean();
        StudentEntity student = new StudentEntity()
                .setId(34L)
                .setUserName("user name")
                .setAge(25);
        mapper.insert(student);
        db.table(ATM.Table.student)
                .query()
                .eqDataMap(ATM.DataMap.student.table(1)
                        .id.values(34L)
                        .userName.values("user name")
                        .age.values(25)
                );
    }
}
