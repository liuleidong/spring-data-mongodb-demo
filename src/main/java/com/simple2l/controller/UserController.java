package com.simple2l.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.simple2l.dao.IUserDao;
import com.simple2l.entity.Access;
import com.simple2l.entity.Contact;
import com.simple2l.entity.User;


@Controller
@RequestMapping("/index")
public class UserController {

	@Autowired
    private IUserDao userDao;
	/**
     * 进入首页
     * @return
     */
    @RequestMapping("/index")
    public String index(){
    	return "index/index";
    }
    
    @RequestMapping("/toAddUser")
    public String toAddUser(){
    	return "index/addUser";
    }
    @RequestMapping("/doAddUser")
    public String doAddUser(@RequestParam("name") String name,@RequestParam("pass") String pass,
    		@RequestParam("phone") String phone,@RequestParam("email") String email,
    		@RequestParam("level") String level,@RequestParam("group") String group){
    	User user = new User(name,pass);
    	Contact contact = new Contact(phone,email);
    	Access access = new Access(level,group);
    	userDao.addUser(user, contact, access);
    	return "index/success";
    }
    
    @RequestMapping("/userList")
    @ResponseBody
    public ModelAndView userList(){
    	List<User> list = userDao.findAll();
    	ModelAndView view = new ModelAndView();
    	view.addObject("users", list);
    	view.setViewName("index/userList");
    	return view;
    }
}
