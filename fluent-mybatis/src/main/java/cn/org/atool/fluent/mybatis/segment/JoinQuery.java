package cn.org.atool.fluent.mybatis.segment;

import cn.org.atool.fluent.mybatis.base.IEntity;
import cn.org.atool.fluent.mybatis.base.IQuery;
import cn.org.atool.fluent.mybatis.base.JoinBuilder;
import cn.org.atool.fluent.mybatis.base.JoinBuilder2;
import cn.org.atool.fluent.mybatis.base.impl.BaseQuery;
import cn.org.atool.fluent.mybatis.metadata.JoinType;
import cn.org.atool.fluent.mybatis.segment.model.PagedOffset;
import cn.org.atool.fluent.mybatis.segment.model.Parameters;
import cn.org.atool.fluent.mybatis.utility.MybatisUtil;
import lombok.Getter;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static cn.org.atool.fluent.mybatis.If.isBlank;

/**
 * 联合查询条件
 *
 * @param <QL>
 */
public class JoinQuery<QL extends BaseQuery<?, QL>>
    implements IQuery<IEntity, JoinQuery<QL>>,
    JoinBuilder<QL>, JoinBuilder2<QL> {
    /**
     * 主查询类型
     */
    private final Class<QL> queryClass;
    /**
     * 主查询条件
     */
    private final QL query;
    /**
     * join查询, 允许有多个join
     */
    private final List<BaseQuery> queries = new ArrayList<>();

    private final Parameters parameters;

    @Getter
    private JoinWrapperData wrapperData;

    public JoinQuery(QL query) {
        this.assertQueryAlias(query);
        this.query = query;
        this.queryClass = (Class<QL>) query.getClass();
        this.parameters = query.getWrapperData().getParameters();
        this.wrapperData = new JoinWrapperData(this.query, this.queries);
    }

    public JoinQuery(Class<QL> queryClass, Function<QL, QL> query) {
        this.queryClass = queryClass;
        this.parameters = new Parameters();
        this.query = newQuery(queryClass, alias(), this.parameters);
        query.apply(this.query);
        this.wrapperData = new JoinWrapperData(this.query, this.queries);
    }

    @Override
    public <QR extends BaseQuery<?, QR>> JoinOn<QL, QR, JoinBuilder2<QL>> join(
        Class<QR> clazz,
        Function<QR, QR> query) {
        return join(JoinType.Join, clazz, query);
    }

    @Override
    public <QR extends BaseQuery<?, QR>> JoinOn<QL, QR, JoinBuilder2<QL>> leftJoin(
        Class<QR> clazz,
        Function<QR, QR> query) {
        return join(JoinType.LeftJoin, clazz, query);
    }

    @Override
    public <QR extends BaseQuery<?, QR>> JoinOn<QL, QR, JoinBuilder2<QL>> rightJoin(
        Class<QR> clazz,
        Function<QR, QR> query) {
        return join(JoinType.RightJoin, clazz, query);
    }

    @Override
    public <QR extends BaseQuery<?, QR>> JoinOn<QL, QR, JoinBuilder<QL>> join(QR query) {
        return join(JoinType.Join, query);
    }

    @Override
    public <QR extends BaseQuery<?, QR>> JoinOn<QL, QR, JoinBuilder<QL>> leftJoin(QR query) {
        return join(JoinType.LeftJoin, query);
    }

    @Override
    public <QR extends BaseQuery<?, QR>> JoinOn<QL, QR, JoinBuilder<QL>> rightJoin(QR query) {
        return join(JoinType.RightJoin, query);
    }

    private <QR extends BaseQuery<?, QR>> JoinOn<QL, QR, JoinBuilder<QL>> join(
        JoinType joinType, QR query) {
        this.assertQueryAlias(query);
        if (query.getWrapperData().getParameters() != this.query.getWrapperData().getParameters()) {
            throw new RuntimeException("the parameters in join query table must be same.");
        }
        this.queries.add(query);
        return new JoinOn<>(this, this.queryClass, this.query, joinType, (Class<QR>) query.getClass(), query);
    }

    /**
     * 判断query查询表别名已经设置
     *
     * @param query
     * @param <QR>
     */
    private <QR extends BaseQuery<?, QR>> void assertQueryAlias(QR query) {
        MybatisUtil.assertNotNull("query", query);
        if (isBlank(query.getAlias())) {
            String err = String.format("the alias in the join query table must be set, " +
                "please use constructor: new %s(String alias, Parameters parameters)", query.getClass().getSimpleName());
            throw new RuntimeException(err);
        }
    }

    private <QR extends BaseQuery<?, QR>> JoinOn<QL, QR, JoinBuilder2<QL>> join(
        JoinType joinType,
        Class<QR> queryClass,
        Function<QR, QR> apply) {
        QR query = newQuery(queryClass, alias(), this.query.wrapperData.getParameters());
        this.queries.add(query);
        apply.apply(query);
        return new JoinOn<>(this, this.queryClass, this.query, joinType, queryClass, query);
    }

    @Override
    public JoinQuery<QL> distinct() {
        this.wrapperData.setDistinct(true);
        return this;
    }

    @Override
    public JoinQuery<QL> limit(int limit) {
        this.wrapperData.setPaged(new PagedOffset(0, limit));
        return this;
    }

    @Override
    public JoinQuery<QL> limit(int start, int limit) {
        this.wrapperData.setPaged(new PagedOffset(start, limit));
        return this;
    }

    @Override
    public JoinQuery<QL> last(String lastSql) {
        this.wrapperData.last(lastSql);
        return this;
    }

    @Override
    public IQuery<?, QL> build() {
        return (IQuery) this;
    }

    @Override
    public JoinQuery<QL> selectAll() {
        throw new RuntimeException("not support");
    }

    @Override
    public JoinQuery<QL> selectId() {
        throw new RuntimeException("not support");
    }

    @Override
    public WhereBase<?, JoinQuery<QL>, JoinQuery<QL>> where() {
        throw new RuntimeException("not support");
    }

    private static Map<Class, Constructor> QueryAliasConstructors = new HashMap<>(128);

    private static <Q extends BaseQuery<?, Q>> Q newQuery(Class<Q> queryClass, String alias, Parameters parameters) {
        try {
            if (!QueryAliasConstructors.containsKey(queryClass)) {
                QueryAliasConstructors.put(queryClass, queryClass.getConstructor(String.class, Parameters.class));
            }
            return (Q) QueryAliasConstructors.get(queryClass).newInstance(alias, parameters);
        } catch (Exception e) {
            throw new RuntimeException(String.format("new %s(String, ParameterPair) error: %s",
                queryClass.getSimpleName(), e.getMessage()), e);
        }
    }

    private int aliasIndex = 1;

    private String alias() {
        return String.format("t%d", aliasIndex++);
    }
}