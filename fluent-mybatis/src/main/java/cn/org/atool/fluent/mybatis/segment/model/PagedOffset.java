package cn.org.atool.fluent.mybatis.segment.model;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * PageOffset: 分页查询设置
 *
 * @author darui.wu
 * @create 2020/6/16 2:00 下午
 */
@Getter
@Accessors(chain = true)
public class PagedOffset {
    /**
     * 查询其实位移
     */
    private int offset = 0;
    /**
     * 查询最大数量
     */
    private int limit = 1;

    public PagedOffset() {
    }

    public PagedOffset(int offset, int limit) {
        this.setOffset(offset);
        this.setLimit(limit);
    }

    public PagedOffset setOffset(int offset) {
        this.offset = offset < 0 ? 0 : offset;
        return this;
    }

    public PagedOffset setLimit(int limit) {
        this.limit = limit < 1 ? 1 : limit;
        return this;
    }

    public int getEndOffset() {
        return this.offset + this.limit;
    }
}