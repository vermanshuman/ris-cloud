package it.nexera.ris.web.beans.wrappers;

import it.nexera.ris.persistence.beans.entities.domain.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenWrapper {

    private boolean validToken;
    private User user;
}
