package esprit.mindmatch.Entities;

import lombok.Getter;

@Getter
public enum EmailTemplateName {
    RESET_PASSWORD("reset_password") ;

    private final String name ;

    EmailTemplateName(String name) {
        this.name = name;
    }
}
