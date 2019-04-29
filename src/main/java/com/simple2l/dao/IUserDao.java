package com.simple2l.dao;

import java.util.List;

import com.simple2l.entity.Access;
import com.simple2l.entity.Contact;
import com.simple2l.entity.User;

public interface IUserDao {
	/**
     * 添加
     * @param User
     */
    public void addUser(User user,Contact contact,Access access);

    /**
     * 删除
     * @param id
     */
    public void removeUser(String id);


    /**
     * 保存或修改
     * @param User
     */
    public void saveOrUpdateUser(User User,Contact contact,Access access);


    /**
     * 根据id查询单个
     * @param id
     * @return
     */
    public User findById(String id);
    
    /**
     * 根据用户名查询
     * @param id
     * @return
     */
    public User findByUsername(String username);


    /**
     * 查询所有
     * @return
     */
    public List<User> findAll();
}
