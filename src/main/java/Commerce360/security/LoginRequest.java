package Commerce360.security;


import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
