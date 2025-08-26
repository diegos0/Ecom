package com.ecom.service.impl;

import com.ecom.model.UserDtls;
import com.ecom.repository.UserRepository;
import com.ecom.service.UserService;
import com.ecom.util.AppConstraint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public UserDtls saveUser(UserDtls user) {
        user.setRole("ROLE_USER");
        user.setIsEnable(true);
        user.setAccountNonLocked(true);
        user.setFailedAttempts(0);
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        return userRepository.save(user);
    }

    @Override
    public UserDtls getUserByEmail(String email) {
        return findByEmail(email); // 🔁 llama internamente sin autoinyectarse
    }

    @Override
    public UserDtls findByEmail(String email) {
        return userRepository.findByEmail(email); // Asegúrate que este método exista
    }

    @Override
    public Boolean updateAccountStatus(Integer id, Boolean status) {
        Optional<UserDtls> findByuser = userRepository.findById(id);
        if(findByuser.isPresent()){
            UserDtls userDtls = findByuser.get();
            userDtls.setIsEnable(Boolean.valueOf(status));
            userRepository.save(userDtls);
            return true;
        }

        return false;
    }

    @Override
    public void increaseFailedAttempt(UserDtls user) {
        int attempt = user.getFailedAttempts() + 1;
        user.setFailedAttempts(attempt);
        userRepository.save(user);

    }

    @Override
    public void userAccountLock(UserDtls user) {
    user.setAccountNonLocked(false);
    user.setLockTime(new Date());
    userRepository.save(user);

    
    }

    @Override
    public boolean unlockAccountTimeExpired(UserDtls user) {
        Date lockTime = user.getLockTime();
        long unlockTimeMillis = lockTime.getTime() + AppConstraint.UNLOCK_DURATION_TIME;
        Date unlockDate = new Date(unlockTimeMillis);
       long currentTime = System.currentTimeMillis();
        if (unlockDate.getTime() < currentTime) {            user.setAccountNonLocked(true);
            user.setFailedAttempts(0);
            user.setLockTime(null);
            userRepository.save(user);
            return true;
        }

        return false;
    }

    @Override
    public void resetAttempt(int userId) {

    }

    @Override
    public void updateUserResetToken(String email, String resetToken) {
       UserDtls findByEmail = findByEmail(email);
       findByEmail.setResetToken(resetToken);
       userRepository.save(findByEmail);
    }

    @Override
    public UserDtls getUserByResetToken(String token) {


        return null;
    }

    @Override
    public UserDtls updateUser(UserDtls user) {
       return  userRepository.save(user);

    }

    @Override
    public List<UserDtls> getUsers(String role) {
        List<UserDtls> list = userRepository.findByRole(role);
        System.out.println("Usuarios con rol " + role + ": " + list.size());
        list.forEach(user -> System.out.println(user.getEmail() + " - " + user.getRole()));
        return list;
    }

}
