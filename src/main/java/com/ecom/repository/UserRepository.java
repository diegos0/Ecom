package com.ecom.repository;

import com.ecom.model.Product;
import com.ecom.model.UserDtls;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<UserDtls, Integer> {

public UserDtls findByEmail(String username);

public List<UserDtls> findByRole(String role);

public UserDtls findByResetToken(String token);


}
