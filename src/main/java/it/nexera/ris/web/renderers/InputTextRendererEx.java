package it.nexera.ris.web.renderers;

import it.nexera.ris.common.helpers.ValidationHelper;
import org.primefaces.component.inputtext.InputText;
import org.primefaces.component.inputtext.InputTextRenderer;

import javax.faces.context.FacesContext;
import java.io.IOException;

public class InputTextRendererEx extends InputTextRenderer {
    @Override
    protected void encodeMarkup(FacesContext context, InputText inputText)
            throws IOException {
        if (ValidationHelper.isNullOrEmpty(inputText.getAttributes().get(
                "maxlength"))
                || ((Integer) inputText.getAttributes().get("maxlength")) < 0) {
            inputText.getAttributes().put("maxlength", 255);
        }

        super.encodeMarkup(context, inputText);
    }

}
