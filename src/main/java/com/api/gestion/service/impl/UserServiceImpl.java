package com.api.gestion.service.impl;

import com.api.gestion.constantes.FacturaConstantes;
import com.api.gestion.dao.UserDAO;
import com.api.gestion.pojo.User;
import com.api.gestion.service.UserService;
import com.api.gestion.util.FacturaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDAO userDAO;

    @Override
    public ResponseEntity<String> singUp(Map<String, String> requestMap) {
        log.info("Se intenta registrar un nuevo usuario {}",requestMap);

        try{
            if (validateSingUp(requestMap)){
                User userToSave = userDAO.findByEmail(requestMap.get("email"));
                if (Objects.isNull(userToSave)){
                    userDAO.save(getUserFromMap(requestMap));
                    return FacturaUtils.getResponseEntity("El usuario se registró exitosamente!", HttpStatus.CREATED);
                }else{
                    return FacturaUtils.getResponseEntity("Ya hay un usuario registrado con ese email", HttpStatus.BAD_REQUEST);
                }
            }else{
                return FacturaUtils.getResponseEntity(FacturaConstantes.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return FacturaUtils.getResponseEntity(FacturaConstantes.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Método encargado de validar que existan los campos necesarios
     * @param requestMap
     * @return
     */
    private boolean validateSingUp(Map<String, String> requestMap){
        return (requestMap.containsKey("nombre") &&
                requestMap.containsKey("email") &&
                requestMap.containsKey("password"));
    }


    /**
     * Método para obtener el usuario de un Map
     * @param requestMap
     * @return
     */
    public User getUserFromMap(Map<String, String> requestMap){
        User user = new User();

        user.setNombre(requestMap.get("nombre"));
        user.setEmail(requestMap.get("email"));
        user.setPassword(requestMap.get("password"));
        user.setNumeroDeContacto(requestMap.get("numeroDeContacto"));

//        Configurando valores propios
        user.setRole("user");
        user.setStatus(String.valueOf(false));

        return user;
    }

}
