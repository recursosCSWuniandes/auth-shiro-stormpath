/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.edu.uniandes.csw.auth.model;

import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.group.Group;
import com.stormpath.sdk.group.GroupList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Jonatan
 */
@XmlRootElement
public class UserDTO {

    private String givenName;
    private String middleName;
    private String surName;
    private String userName;
    private String fullName;
    private String password;
    private String email;
    private boolean rememberMe;
    private List<String>  role;
    public UserDTO() {
        
    }
    public UserDTO(Account account) {
        this.givenName = account.getGivenName();
        this.middleName = account.getMiddleName();
        this.surName = account.getSurname();
        this.userName = account.getUsername();
        this.fullName = account.getFullName();        
        this.email = account.getEmail();               
        GroupList groups = account.getGroups();
            for(Group grp : groups) {
                role.add(grp.getName());
            } 
        //this.rememberMe = rememberMe; 
        //this.password = password;
        
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getSurName() {
        return surName;
    }

    public void setSurName(String surName) {
        this.surName = surName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    public List<String> getRole() {
        return role;
    }

    public void setRole(List<String> role) {
        this.role = role;
    }
    

    

    

}

