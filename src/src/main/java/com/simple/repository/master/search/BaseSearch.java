package com.simple.repository.master.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 基础查询对象
 *
 * @author laiqx
 * date 2022-11-12
 */
public class BaseSearch implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 起始位置
     */
    public Integer startIndex = 0;

    /**
     * 截至数
     */
    public Integer endIndex = 10;

    /**
     * 加载数
     */
    private Integer loadSize = 10;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 是否分页
     */
    private Boolean page = true;

    /**
     * 查询条件
     */
    private Condition condition;

    /**
     * 排序
     */
    private List<Sort> sorts = new ArrayList<>();

    /**
     * 新增排序规则
     *
     * @param fieldName 字段
     * @param type      排序类型
     * @return 返回当前查询对象
     */
    public BaseSearch putSort(String fieldName, SortType type) {
        sorts.add(new Sort(fieldName, type));
        return this;
    }

    /**
     * 获取排序
     *
     * @return 查询条件
     */
    public String getSort() {
        if (sorts.isEmpty()) {
            return "";
        }
        String sortStr = "order by ";
        for (Sort sort : sorts) {
            sortStr += sort.fieldName + " " + sort.type + ",";
        }
        return sortStr.substring(0, sortStr.length() - 1);
    }

    /**
     * 排序对象
     */
    class Sort {

        private String fieldName;

        private SortType type;

        public Sort(String fieldName, SortType type) {
            this.fieldName = fieldName;
            this.type = type;
        }
    }

    /**
     * 排序枚举
     */
    public enum SortType {
        asc, desc
    }

    /**
     * 初始化分页下标
     */
    public void initIndex() {
        startIndex = null != this.pageNum && this.pageNum > 1 ?
                (this.pageNum - 1) * this.loadSize : 0;
        endIndex = this.loadSize;
    }

    /**
     * 获取开始节点
     *
     * @return 返回结束节点
     */
    public Integer getStartIndex() {
        startIndex = null != this.pageNum && this.pageNum > 1 ?
                (this.pageNum - 1) * this.loadSize : 0;
        return startIndex;
    }

    /**
     * 获取结束节点
     *
     * @return 返回结束节点
     */
    public Integer getEndIndex() {
        return this.loadSize;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public void setStartIndex(Integer startIndex) {
        this.startIndex = startIndex;
    }

    public void setEndIndex(Integer endIndex) {
        this.endIndex = endIndex;
    }

    public Integer getLoadSize() {
        return loadSize;
    }

    public void setLoadSize(Integer loadSize) {
        this.loadSize = loadSize;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Boolean getPage() {
        return page;
    }

    public void setPage(Boolean page) {
        this.page = page;
    }
}
