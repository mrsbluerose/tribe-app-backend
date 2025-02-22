package com.savvato.tribeapp.controllers;

import com.savvato.tribeapp.controllers.annotations.controllers.PermissionsAPIController.*;
import com.savvato.tribeapp.controllers.dto.PermissionsRequest;
import com.savvato.tribeapp.dto.GenericResponseDTO;
import com.savvato.tribeapp.dto.UserDTO;
import com.savvato.tribeapp.entities.UserRole;
import com.savvato.tribeapp.services.GenericResponseService;
import com.savvato.tribeapp.services.UserRoleMapService;
import com.savvato.tribeapp.services.UserRoleService;
import com.savvato.tribeapp.services.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/permissions")
@Tag(name = "permissions", description = "Delegation of user permissions")
public class PermissionsAPIController {
  @Autowired UserRoleMapService userRoleMapService;

  @Autowired private GenericResponseService GenericResponseService;

  @Autowired UserRoleService userRoleService;

  @Autowired UserService userService;

  @GetAllUsers
  @GetMapping("/users")
  public ResponseEntity<Iterable<UserDTO>> getAllUsers() {

    // WHY is this defined here? Instead of UserAPIController? It's because only
    // permissions needs this functionality, at the moment. Also, all the methods
    // In UserAPIController assume public, no-password access. This endpoint would
    // require a login. When another domain eventually needs a list of users,
    // let's think about what they have in common, and create a controller/service
    // then, and removing this method, of course.

    Iterable<UserDTO> rtn = userService.getAllUsers();

    if (rtn != null) {
      return ResponseEntity.status(HttpStatus.OK).body(rtn);
    } else {
      return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(null);
    }
  }

  @GetAllUserRoles
  @GetMapping("/user-roles")
  public ResponseEntity<Iterable<String>> getAllUserRoles() {
    Iterable<String> rtn = userRoleMapService.getRoles();

    if (rtn != null) {
      return ResponseEntity.status(HttpStatus.OK).body(rtn);
    } else {
      return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(null);
    }
  }

  @GetAllRoles
  @GetMapping("/user-roles-list")
  public ResponseEntity<Iterable<UserRole>> getAllRoles() {
    Iterable<UserRole> rtn = userRoleService.getRoles();
    if (rtn != null) {
      return ResponseEntity.status(HttpStatus.OK).body(rtn);
    } else {
      return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(null);
    }
  }

  @AddPermissions
  @PostMapping
  public ResponseEntity<GenericResponseDTO> addPermissions(@RequestBody @Valid PermissionsRequest request) {
    boolean val = userRoleMapService.addRolesToUser(request.id, request.permissions);
      GenericResponseDTO rtn =  GenericResponseService.createDTO(val);
      if (val) {
          return ResponseEntity.status(HttpStatus.OK).body(rtn);
    } else {
          return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(rtn);
    }
  }

  @DeletePermissions
  @DeleteMapping
  public ResponseEntity<GenericResponseDTO> deletePermissions(@RequestBody @Valid PermissionsRequest request) {
    boolean val = userRoleMapService.removeRolesFromUser(request.id, request.permissions);
    GenericResponseDTO rtn =  GenericResponseService.createDTO(val);
    if (val) {
      return ResponseEntity.status(HttpStatus.OK).body(rtn);
    } else {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(rtn);
    }
  }
}
