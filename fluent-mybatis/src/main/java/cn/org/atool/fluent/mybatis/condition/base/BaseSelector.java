package cn.org.atool.fluent.mybatis.condition.base;

import cn.org.atool.fluent.mybatis.interfaces.IQuery;
import cn.org.atool.fluent.mybatis.interfaces.PredicateField;
import cn.org.atool.fluent.mybatis.method.metadata.TableMetaHelper;

import static cn.org.atool.fluent.mybatis.utility.MybatisUtil.isNotEmpty;

/**
 * BaseSelector: 查询字段构造
 *
 * @author darui.wu
 * @create 2020/6/21 3:13 下午
 */
public abstract class BaseSelector<S extends BaseSelector<S>> {
    private IQuery query;

    protected BaseSelector(IQuery query) {
        this.query = query;
    }

    /**
     * 过滤查询的字段信息(主键除外!)
     *
     * <p>例1: 只要 java 字段名以 "test" 开头的   -> select(i -> i.getProperty().startsWith("test"))</p>
     * <p>例2: 要全部字段                        -> select(i -> true)</p>
     * <p>例3: 只要字符串类型字段                 -> select(i -> i.getPropertyType instance String)</p>
     *
     * @param predicate 过滤方式
     * @return 字段选择器
     */
    public S apply(PredicateField predicate) {
        String selected = TableMetaHelper.getTableInfo(this.query.getEntityClass()).filter(predicate);
        return this.apply(selected);
    }

    /**
     * 增加查询字段
     *
     * @param columns 查询字段
     * @return 查询字段选择器
     */
    public S apply(String... columns) {
        if (isNotEmpty(columns)) {
            query.select(columns);
        }
        return (S) this;
    }
}