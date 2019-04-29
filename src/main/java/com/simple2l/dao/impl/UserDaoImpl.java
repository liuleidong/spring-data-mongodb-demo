package com.simple2l.dao.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.simple2l.dao.IUserDao;
import com.simple2l.entity.Access;
import com.simple2l.entity.Contact;
import com.simple2l.entity.User;

@Service("userDao")
public class UserDaoImpl implements IUserDao{

	@Autowired
    private MongoTemplate mongoTemplate;
	
	@Override
	@Transactional
	public void addUser(User user, Contact contact, Access access) {
		// TODO Auto-generated method stub
		mongoTemplate.save(user);
		mongoTemplate.save(contact);
		mongoTemplate.save(access);
	}

	@Override
	public void removeUser(String id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveOrUpdateUser(User User, Contact contact, Access access) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public User findById(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User findByUsername(String username) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<User> findAll() {
		// TODO Auto-generated method stub
		List<User> list = mongoTemplate.findAll(User.class);
		return list;
	}

}
