package com.api.gestion.service.impl;

import com.api.gestion.constantes.FacturaConstantes;
import com.api.gestion.dao.UserDAO;
import com.api.gestion.pojo.User;
import com.api.gestion.security.CustomerDetailsService;
import com.api.gestion.security.jwt.JwtUtil;
import com.api.gestion.service.UserService;
import com.api.gestion.util.FacturaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomerDetailsService customerDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public ResponseEntity<String> signUp(Map<String, String> requestMap) {
        log.info("Se intenta registrar un nuevo usuario {}", requestMap);

        try {
            if (validateSingUp(requestMap)) {
                User userToSave = userDAO.findByEmail(requestMap.get("email"));
                if (Objects.isNull(userToSave)) {
                    userDAO.save(getUserFromMap(requestMap));
                    return FacturaUtils.getResponseEntity("El usuario se registró exitosamente!", HttpStatus.CREATED);
                } else {
                    return FacturaUtils.getResponseEntity("Ya hay un usuario registrado con ese email", HttpStatus.BAD_REQUEST);
                }
            } else {
                return FacturaUtils.getResponseEntity(FacturaConstantes.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return FacturaUtils.getResponseEntity(FacturaConstantes.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> login(Map<String, String> requestMap) {
        log.info("Intento de inicio de sesión por parte del usuario {}", requestMap);

        if (!emailExist(requestMap.get("email"))){
            return new ResponseEntity<>("{\"mensaje\":\"Parece que aún no hay ninguna cuenta creada para este email\"}", HttpStatus.BAD_REQUEST);
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestMap.get("email"), requestMap.get("password"))
            );

            if (authentication.isAuthenticated()) {
                if (customerDetailsService.getUserDetail().getStatus().equalsIgnoreCase("true")) {
                    return new ResponseEntity<String>(
                            "{\"token\":\"" +
                                    jwtUtil.generateToken(
                                            customerDetailsService.getUserDetail().getEmail(),
                                            customerDetailsService.getUserDetail().getRole())
                                    + "\"}", HttpStatus.OK);
                }
            }

        } catch (BadCredentialsException bad) {
            return new ResponseEntity<>("{\"mensaje\":\"La contraseña no coincide con el email\"}", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("{}", e);
        }
        return new ResponseEntity<>("{\"mensaje\": \"Espere la aprobación del administrador\"}", HttpStatus.BAD_REQUEST);
    }

    /**
     * Método encargado de validar que existan los campos necesarios
     *
     * @param requestMap
     * @return
     */
    private boolean validateSingUp(Map<String, String> requestMap) {
        return (requestMap.containsKey("nombre") &&
                requestMap.containsKey("email") &&
                requestMap.containsKey("password"));
    }


    /**
     * Método para obtener el usuario de un Map
     *
     * @param requestMap
     * @return
     */
    public User getUserFromMap(Map<String, String> requestMap) {
        User user = new User();

        user.setNombre(requestMap.get("nombre"));
        user.setEmail(requestMap.get("email"));

        String encodedPassword = passwordEncoder.encode(requestMap.get("password"));
        user.setPassword(encodedPassword);

        user.setNumeroDeContacto(requestMap.get("numeroDeContacto"));

//        Configurando valores propios
        user.setRole("user");
        user.setStatus(String.valueOf(false));

        return user;
    }


    public boolean emailExist(String email) {
        return (userDAO.findByEmail(email) != null);
    }

}
