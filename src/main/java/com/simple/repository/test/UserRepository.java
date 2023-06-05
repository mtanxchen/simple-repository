package com.simple.repository.test;

import com.simple.repository.test.userEntity.UserEntity;
import com.simple.repository.master.BaseRepository;

public class UserRepository extends BaseRepository<UserEntity> {

    public UserRepository(){
        this.setEntityClass(UserEntity.class);
    }

}
