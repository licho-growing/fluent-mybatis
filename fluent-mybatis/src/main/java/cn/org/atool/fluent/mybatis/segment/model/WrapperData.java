package cn.org.atool.fluent.mybatis.segment.model;

import cn.org.atool.fluent.mybatis.exception.FluentMybatisException;
import cn.org.atool.fluent.mybatis.base.model.SqlOp;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static cn.org.atool.fluent.mybatis.segment.model.StrConstant.*;
import static cn.org.atool.fluent.mybatis.utility.MybatisUtil.*;
import static java.util.stream.Collectors.joining;

/**
 * WrapperInfo: 查询器或更新器xml需要用到信息
 *
 * @author darui.wu
 * @create 2020/6/23 5:15 下午
 */
@Getter
public class WrapperData implements IWrapperData {
    /**
     * select 前面是否加 DISTINCT 关键字
     */
    @Setter
    private boolean isDistinct = false;
    /**
     * 查询字段
     */
    private Set<String> sqlSelect = new LinkedHashSet<>(8);
    /**
     * SQL 更新字段内容，例如：name='1', age=2
     */
    private final Map<String, String> updates = new LinkedHashMap<>(16);
    /**
     * 表名
     */
    private final String table;
    /**
     * 自定义参数列表
     */
    private final ParameterPair parameters;
    /**
     * 实体类型
     */
    private final Class entityClass;
    /**
     * 对应的嵌套查询类
     */
    private final Class queryClass;
    /**
     * where, group, having ,order by对象列表
     */
    private final MergeSegments mergeSegments;
    /**
     * 分页参数
     */
    @Setter
    private PagedOffset paged;

    public WrapperData(String table, ParameterPair parameters, Class entityClass, Class queryClass) {
        notNull(entityClass, "entityClass must not null,please set entity before use this method!");
        this.table = table;
        this.parameters = parameters;
        this.mergeSegments = new MergeSegments();
        this.entityClass = entityClass;
        this.queryClass = queryClass;
    }

    @Override
    public String getSqlSelect() {
        if (this.sqlSelect.isEmpty()) {
            return null;
        } else {
            String sql = sqlSelect.stream().collect(joining(COMMA_SPACE));
            return isBlank(sql) ? null : sql.trim();
        }
    }

    @Override
    public String getQuerySql() {
        String select = this.getSqlSelect();
        String sql = String.format(SELECT_FROM_WHERE, select == null ? ASTERISK : select, this.table, this.getMergeSql());
        return isBlank(sql) ? null : sql.trim();
    }

    @Override
    public String getUpdateStr() {
        String sql = this.updates.entrySet().stream().map(i -> i.getKey() + " = " + i.getValue()).collect(joining(COMMA_SPACE));
        return isBlank(sql) ? null : sql.trim();
    }

    @Override
    public String getMergeSql() {
        String sql = mergeSegments.sql();
        return isBlank(sql) ? null : sql.trim();
    }

    @Override
    public String getWhereSql() {
        return this.mergeSegments.whereSql();
    }

    @Override
    public String getGroupBy() {
        return this.mergeSegments.groupBy();
    }

    @Override
    public String getOrderBy() {
        return this.mergeSegments.orderBy();
    }

    @Override
    public String getLastSql() {
        return this.mergeSegments.last();
    }

    /**
     * ============================================================
     *                          以下是数据操作部分
     * ============================================================
     */

    /**
     * 附加sql,只允许执行一次
     *
     * @param lastSql 附加sql
     */
    public void last(String lastSql) {
        this.mergeSegments.setLastSql(lastSql);
    }

    /**
     * 增加条件设置
     *
     * @param keyWord  or and
     * @param column   设置条件的字段
     * @param operator 条件操作
     * @param paras    条件参数（填充 operator 中占位符?)
     */
    public void apply(KeyWordSegment keyWord, String column, SqlOp operator, Object... paras) {
        this.apply(keyWord, column, null, operator, paras);
    }

    /**
     * 增加查询字段
     *
     * @param column
     */
    public void addSelectColumn(String column) {
        if (!this.sqlSelect.contains(column)) {
            this.sqlSelect.add(column);
        }
    }

    /**
     * 增加条件设置
     *
     * @param keyWord  or and
     * @param column   设置条件的字段
     * @param format   格式化sql语句
     * @param operator 条件操作
     * @param paras    条件参数（填充 operator 中占位符?)
     */
    public void apply(KeyWordSegment keyWord, String column, String format, SqlOp operator, Object... paras) {
        if (keyWord == null) {
            throw new FluentMybatisException("the first segment should be: 'AND', 'OR', 'GROUP BY', 'HAVING' or 'ORDER BY'");
        }
        String segment = operator.operator(this.getParameters(), format, paras);
        this.getMergeSegments().add(keyWord, () -> column, () -> segment);
    }

    /**
     * 根据函数和变量构建占位符和设置占位符对应的变量值
     *
     * @param functionSql 函数
     * @param values      变量列表
     * @return 参数化后的sql
     */
    public String paramSql(String functionSql, Object[] values) {
        return this.parameters.paramSql(functionSql, values);
    }

    /**
     * 更新column字段值
     *
     * @param column 字段
     * @param value  更新值
     */
    public void updateSet(String column, Object value) {
        this.updateSql(column, QUESTION_MARK, value);
    }

    /**
     * 设置更新（自定义SQL）
     *
     * @param column      更新的字段
     * @param functionSql set function sql
     * @param values      对应的参数
     */
    public void updateSql(String column, String functionSql, Object... values) {
        if (isNotBlank(functionSql)) {
            updates.put(column, this.paramSql(functionSql, values));
        }
    }
}