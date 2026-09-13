package com.football_club.Auth.service;

import com.football_club.Auth.dto.RegisterDTO;
import com.football_club.Auth.dto.UserDTO;
import com.football_club.Auth.model.RoleEnum;
import com.football_club.Scouting.model.enums.Region;
import com.football_club.Auth.model.User;

import java.util.List;

public interface IUserService {

    UserDTO registerUser(RegisterDTO userDTO);

    UserDTO getUserById(Long id);

    UserDTO getUserByUsername(String username);

    User getFullUserById(Long id);

    User getFullUserByUsername(String username);

    boolean isUserExists(String username, String email);

    List<UserDTO> getAllUsers();

    List<UserDTO> getUsersByRole(RoleEnum role);

    UserDTO updateUser(Long id, UserDTO userDTO);

    void changeUserRole(Long id, RoleEnum newRole);

    void changeUserStatus(Long id, boolean isActive);

    void assignRegionToScout(Long scoutId, Region region);
}