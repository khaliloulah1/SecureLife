package gestion.securelife.dto.request;

import gestion.securelife.entity.enums.Role;


import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private String nomComplet;
    private Role role;
}

