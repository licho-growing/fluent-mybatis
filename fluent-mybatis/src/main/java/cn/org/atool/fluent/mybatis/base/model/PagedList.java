package cn.org.atool.fluent.mybatis.base.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 分页查询结果
 *
 * @author wudarui
 */
@Getter
@Setter
@Accessors(chain = true)
public class PagedList<E> {
    /**
     * 总记录数
     */
    private int total;

    /**
     * 本次查询结果集
     */
    private List<E> data;

    public PagedList() {
    }

    public PagedList(int total, List<E> data) {
        this.total = total;
        this.data = data;
    }
}