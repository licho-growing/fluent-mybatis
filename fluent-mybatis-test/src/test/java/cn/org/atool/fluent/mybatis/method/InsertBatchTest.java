package cn.org.atool.fluent.mybatis.method;

import cn.org.atool.fluent.mybatis.generate.ATM;
import cn.org.atool.fluent.mybatis.generate.entity.StudentEntity;
import cn.org.atool.fluent.mybatis.generate.mapper.StudentMapper;
import cn.org.atool.fluent.mybatis.test.BaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class InsertBatchTest extends BaseTest {
    @Autowired
    private StudentMapper mapper;

    @Test
    public void testInsertBatch_withoutPk() {
        db.table(ATM.Table.student).clean();
        List<StudentEntity> list = list(
            new StudentEntity().setUserName("name1").setAge(23),
            new StudentEntity().setUserName("name2").setAge(24));
        mapper.insertBatch(list);
        db.table(ATM.Table.student).count().eq(2);
        db.table(ATM.Table.student).query().print()
            .eqDataMap(ATM.DataMap.student.table(2)
                .age.values(23, 24)
                .userName.values("name1", "name2")
            );
        want.number(list.get(0).getId()).isNull();
        want.number(list.get(1).getId()).isNull();
    }

    @Test
    public void testInsertBatch_WithId() {
        db.table(ATM.Table.student).clean();
        List<StudentEntity> list = list(
            new StudentEntity().setId(23L).setUserName("name1").setAge(23),
            new StudentEntity().setId(24L).setUserName("name2").setAge(24));
        mapper.insertBatch(list);
        db.table(ATM.Table.student).count().eq(2);
        db.table(ATM.Table.student).query().print()
            .eqDataMap(ATM.DataMap.student.table(2)
                .age.values(23, 24)
                .userName.values("name1", "name2")
            );
        want.array(list.stream().map(StudentEntity::getId).toArray())
            .eqReflect(new long[]{23, 24});
    }

    @DisplayName("部分id有值，实体id不会回写")
    @Test
    public void testInsertBatch() {
        db.table(ATM.Table.student).clean();
        List<StudentEntity> list = list(
            new StudentEntity().setUserName("name1").setAge(23).setId(101L),
            new StudentEntity().setUserName("name2").setAge(24));
        mapper.insertBatch(list);
        db.table(ATM.Table.student).count().eq(2);
        db.table(ATM.Table.student).query().print()
            .eqDataMap(ATM.DataMap.student.table(2)
                .age.values(23, 24)
                .userName.values("name1", "name2")
            );
        want.number(list.get(1).getId()).isNull();
    }
}
