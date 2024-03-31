package com.simple.repository.master.exception;

/**
 * 自定义异常
 *
 * @author laiqx
 */
public class SimpleException extends RuntimeException {
    public SimpleException(Throwable cause) {
        super(cause);
    }

    public SimpleException(String message) {
        super(message);
    }

    public SimpleException(Type type) {
        super(type.getMsg());
    }

    /**
     * 异常类型对象
     */
    public enum Type {

        ID_IS_NULL("主键ID不能为空!"),
        CONDITION_IS_NULL("条件不能为空!"),
        ENTITY_ID_IS_NULL("操作对象和ID不能为空!"),
        ENTITY_IS_NULL("操作对象不能为空!"),
        ENTITY_CONDITION_IS_NULL("操作对象和条件不能为空"),
        SQL_IS_NULL("查询语句为空请检查sqlIndex"),

        DEL_WHERE_IS_NULL("删除语句条件不能为空")
        ;

        Type(String msg) {
            this.msg = msg;
        }

        private final String msg;

        public String getMsg() {
            return msg;
        }
    }


}
