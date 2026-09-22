package it.nexera.ris.common.annotations;

import it.nexera.ris.common.enums.CdaTags;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CdaTag {
    CdaTags value();
}
