/**
 * @desc 修改测试
 * @name updateTest
 */
update from test set name = &{name} , age = ${age} where id = ${id};

/**
 * @desc 分析销售数据
 * @name testSelect
 */
select * from test where id in (&{ids})