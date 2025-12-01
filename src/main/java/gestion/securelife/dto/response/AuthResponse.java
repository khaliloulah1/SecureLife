package gestion.securelife.dto.response;


import lombok.Data;

@Data
public class AuthResponse {
    private String accessToken;
    private String refreshToken;

    public AuthResponse(String access, String refresh) {
        this.accessToken = access;
        this.refreshToken = refresh;
    }
}
