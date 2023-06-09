package antifraud.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@Setter
@Getter
@NoArgsConstructor
public class UserRegisterResponse {

    @NotBlank
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String username;
    @NotNull
    private String role;
}
