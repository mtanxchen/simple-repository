/**
 * 修改测试
 * @name updateTest
 * @required id,name
 */
update test set name = &{name} , age = ${age} where id = ${id};

/**
 * 查询测试
 * @name testSelect
 * @required age,name
 */
select * from test where name like &{name} and age > ${age} and id in ${ids};

/**
 * 修改测试
 * @name updateTest3
 * @required id,name
 */
update test set name = &{name} , age = ${age} where id = ${id};