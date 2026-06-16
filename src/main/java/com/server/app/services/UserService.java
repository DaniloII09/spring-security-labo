package com.server.app.services;

import com.server.app.dto.auth.LoginDto;
import com.server.app.dto.auth.SignUpDto;
import com.server.app.dto.auth.UpdatePasswordDto;
import com.server.app.dto.auth.UpdateProfileDto;
import com.server.app.exceptions.BadRequestException;
import com.server.app.exceptions.UnauthorizedException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.server.app.dto.user.UserCreateDto;
import com.server.app.dto.user.UserUpdateDto;
import com.server.app.entities.Role;
import com.server.app.entities.User;
import com.server.app.exceptions.ConfictException;
import com.server.app.exceptions.NotFoundException;
import com.server.app.repositories.RoleRepository;
import com.server.app.repositories.UserRepository;

@Service
@AllArgsConstructor
public class UserService {

  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;

  @Transactional
  public User create(UserCreateDto dto) {
    uniqueUsername(dto.getUsername(), null);
    uniqueEmail(dto.getEmail(), null);
    User user = new User();
    user.setUsername(dto.getUsername());
    user.setName(dto.getName());
    user.setSurname(dto.getSurname());
    user.setEmail(dto.getEmail());
    user.setPassword(passwordEncoder.encode(dto.getPassword()));

    if (dto.getRole() != null) {
      Role role = roleRepository.findById(dto.getRole())
          .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
      user.setRole(role);
    }

    return userRepository.save(user);
  }

  public Page<User> findAll(int page, int size, String search) {
    return userRepository.findAll(PageRequest.of(page, size), search);
  }

  public User findById(int id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
  }

  @Transactional
  public User updateUser(int userId, UserUpdateDto dto) {
    User user = findById(userId);

    if (user.isBlocked()) {
      throw new ConfictException("The user: " + user.getUsername() + " is locked");
    }

    if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
      uniqueUsername(dto.getUsername(), userId);
      user.setUsername(dto.getUsername());
    }

    if (dto.getName() != null && !dto.getName().isBlank()) {
      user.setName(dto.getName());
    }

    if (dto.getSurname() != null && !dto.getSurname().isBlank()) {
      user.setSurname(dto.getSurname());
    }

    if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
      uniqueEmail(dto.getEmail(), userId);
      user.setEmail(dto.getEmail());
    }

    if (dto.getBlocked() != null) {
      user.setBlocked(dto.getBlocked());
    }

    if (dto.getRole() != null) {
      Role role = roleRepository.findById(dto.getRole())
          .orElseThrow(() -> new NotFoundException("Rol no encontrado"));
      user.setRole(role);
    }

    if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
      user.setPassword(dto.getPassword());
    }

    return userRepository.save(user);
  }

  private void uniqueUsername(String username, Integer id) {
    userRepository.findUserByUsername(username).ifPresent(existing -> {
      if (id == null || existing.getId() != id) {
        throw new ConfictException("El nombre de usuario ya está en uso");
      }
    });
  }

  private void uniqueEmail(String email, Integer id) {
    userRepository.findUserByEmail(email).ifPresent(existing -> {
      if (id == null || existing.getId() != id) {
        throw new ConfictException("El correo electrónico ya está en uso");
      }
    });
  }

  public User login(LoginDto dto) {
    User user = userRepository.findUserByUsername(dto.getUsername())
            .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas"));

    if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
      throw new UnauthorizedException("Credenciales inválidas");
    }

    if (user.isBlocked()) {
      throw new UnauthorizedException("Tu cuenta ha sido bloqueada");
    }

    Role role = user.getRole();
    if (role == null || !role.getActive()) {
      throw new UnauthorizedException("El rol de tu cuenta no está activo");
    }

    return user;
  }

  public User signUp(SignUpDto dto) {
    if (userRepository.findUserByUsername(dto.getUsername()).isPresent()) {
      throw new BadRequestException("El username ya existe");
    }

    if (userRepository.findUserByEmail(dto.getEmail()).isPresent()) {
      throw new BadRequestException("El email ya está registrado");
    }

    Role adminRole = roleRepository.findByName("ADMIN")
            .orElseThrow(() -> new BadRequestException("Rol ADMIN no existe"));

    if (!adminRole.getActive()) {
      throw new UnauthorizedException("El rol ADMIN no está activo");
    }

    User user = User.builder()
            .username(dto.getUsername())
            .name(dto.getName())
            .surname(dto.getSurname())
            .email(dto.getEmail())
            .password(dto.getPassword())
            .role(adminRole)
            .blocked(false)
            .build();

    return userRepository.save(user);
  }

  public User updateProfile(int userId, UpdateProfileDto dto) {
    User user = findById(userId);

    if (dto.getUsername() != null) {
      if (userRepository.findUserByUsername(dto.getUsername()).isPresent()
              && !user.getUsername().equals(dto.getUsername())) {
        throw new BadRequestException("El username ya existe");
      }
      user.setUsername(dto.getUsername());
    }

    if (dto.getEmail() != null) {
      if (userRepository.findUserByEmail(dto.getEmail()).isPresent()
              && !user.getEmail().equals(dto.getEmail())) {
        throw new BadRequestException("El email ya está registrado");
      }
      user.setEmail(dto.getEmail());
    }

    if (dto.getName() != null) {
      user.setName(dto.getName());
    }

    if (dto.getSurname() != null) {
      user.setSurname(dto.getSurname());
    }

    return userRepository.save(user);
  }

  public User updatePassword(int userId, UpdatePasswordDto dto) {
    User user = findById(userId);

    if (!passwordEncoder.matches(dto.getOldpassword(), user.getPassword())) {
      throw new UnauthorizedException("La contraseña anterior es incorrecta");
    }

    if (!dto.getNewpassword().equals(dto.getConfirmpassword())) {
      throw new BadRequestException("Las contraseñas nuevas no coinciden");
    }

    if (dto.getOldpassword().equals(dto.getNewpassword())) {
      throw new BadRequestException("La nueva contraseña debe ser diferente a la anterior");
    }

    user.setPassword(dto.getNewpassword());
    return userRepository.save(user);
  }
}
