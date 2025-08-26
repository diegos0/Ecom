package com.ecom.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity // class entity  the mapping a table in the db
public class UserDtls {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String mobileNumber;
    private String email;
    private String password;

    private String address;
    private String city;
    private String state;
    private String pincode;
    private String profileImage;
    private String role;
    private Boolean isEnable;

    private Boolean accountNonLocked;

    private Integer failedAttempts;
    private Date lockTime;

    @Column(name = "reset_token")  // if your database column has underscore
    private String resetToken;
}
