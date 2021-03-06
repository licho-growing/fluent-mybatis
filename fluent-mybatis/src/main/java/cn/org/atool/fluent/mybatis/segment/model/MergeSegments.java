package cn.org.atool.fluent.mybatis.segment.model;

import cn.org.atool.fluent.mybatis.segment.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import static cn.org.atool.fluent.mybatis.If.isBlank;
import static cn.org.atool.fluent.mybatis.mapper.StrConstant.EMPTY;
import static cn.org.atool.fluent.mybatis.mapper.StrConstant.SPACE;
import static cn.org.atool.fluent.mybatis.segment.model.KeyWordSegment.*;
import static cn.org.atool.fluent.mybatis.utility.MybatisUtil.trim;

/**
 * 合并 SQL 片段
 *
 * @author darui.wu
 */
@Getter
public class MergeSegments extends BaseSegmentList {

    private final WhereSegmentList where = new WhereSegmentList();

    private final GroupBySegmentList groupBy = new GroupBySegmentList();

    private final HavingSegmentList having = new HavingSegmentList();

    private final OrderBySegmentList orderBy = new OrderBySegmentList();

    @Setter(AccessLevel.NONE)
    private String lastSql = EMPTY;

    public MergeSegments setLastSql(String lastSql) {
        this.lastSql = lastSql;
        return this;
    }

    /**
     * 添加sql片段
     *
     * @param first    sql
     * @param segments sql片段
     */
    @Override
    public MergeSegments add(ISqlSegment first, ISqlSegment... segments) {
        if (first == null) {
            return this;
        }
        if (ORDER_BY == first) {
            orderBy.add(first, segments);
        } else if (GROUP_BY == first) {
            groupBy.add(first, segments);
        } else if (HAVING == first) {
            having.add(first, segments);
        } else {
            where.add(first, segments);
        }
        super.cache = null;
        return this;
    }


    /**
     * <pre>
     * 拼接sql语句 (where)
     * ... AND ...
     * group by ...
     * having ...
     * order by ...
     * last sql
     * </pre>
     *
     * @return sql
     */
    @Override
    protected String build() {
        String sql = where.sql() + groupBy.sql() + having.sql() + orderBy.sql();
        return sql.trim() + last();
    }

    public String last() {
        return isBlank(lastSql) ? EMPTY : SPACE + lastSql.trim();
    }

    /**
     * 返回where语句
     *
     * @return
     */
    public String whereSql() {
        return trim(where.sql());
    }

    /**
     * groupBy... having... 部分
     *
     * @return
     */
    public String groupBy() {
        return trim(groupBy.sql() + having.sql());
    }

    /**
     * orderBy... 部分
     *
     * @return
     */
    public String orderBy() {
        return trim(orderBy.sql());
    }
}