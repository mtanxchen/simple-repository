/**
 * @desc 修改测试
 * @name updateTest
 */
update from test set name = &{name} , age = ${age} where id = ${id};

/**
 * 查询测试
 * @name testSelect
 */
select * from test where name like &{name} and age > ${age};