package gestion.securelife.dto.response;


import gestion.securelife.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserProfileResponse {
    private String email;
    private String nomComplet;
    private Role role;
}
