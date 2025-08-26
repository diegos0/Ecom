package com.ecom.service;

import com.ecom.model.UserDtls;

import java.util.List;

public interface UserService   {

    public UserDtls saveUser(UserDtls user);

    UserDtls getUserByEmail(String email);


    List<UserDtls> getUsers(String role);

   public  UserDtls findByEmail(String email);

    Boolean updateAccountStatus(Integer id, Boolean status);

    public void increaseFailedAttempt(UserDtls user);
    public void userAccountLock(UserDtls user);
    boolean unlockAccountTimeExpired(UserDtls user);
    public void resetAttempt(int userId);


    void updateUserResetToken(String email, String resetToken);

    public UserDtls getUserByResetToken(String token);

    UserDtls updateUser(UserDtls user);

}
