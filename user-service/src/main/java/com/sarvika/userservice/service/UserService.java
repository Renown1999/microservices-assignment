package com.sarvika.userservice.service;

import com.sarvika.userservice.dto.UserDTO;

import java.util.List;

public interface UserService {
    UserDTO createUser(UserDTO userDTO);
    
    UserDTO getUserById(Long id);
    
    UserDTO updateUser(Long id, UserDTO userDTO);  
    
    List<UserDTO> getAllUsers();
    
    void deleteUser(Long id);
}
