package antifraud.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter @Setter
@NoArgsConstructor
public class UpdateRoleRequest {

    @NotBlank
    private String username;
    @NotBlank
    private String role;
}
