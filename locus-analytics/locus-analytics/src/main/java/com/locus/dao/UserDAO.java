package com.locus.dao;

import com.locus.model.User;

import java.util.List;

public interface UserDAO {

    User findById(String userId);

    User findByEmail(String email);

    boolean insert(User user);

    boolean update(User user);

    boolean delete(String userId);

    List<User> findAll();
}