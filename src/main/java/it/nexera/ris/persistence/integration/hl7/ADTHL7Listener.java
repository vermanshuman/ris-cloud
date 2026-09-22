package it.nexera.ris.persistence.integration.hl7;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.app.Application;
import ca.uhn.hl7v2.app.ApplicationException;
import ca.uhn.hl7v2.model.Message;
import it.nexera.ris.common.enums.SessionNames;
import it.nexera.ris.common.helpers.LogHelper;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;

public class ADTHL7Listener implements Application {
    private final Logger log = LogManager.getLogger(ADTHL7Listener.class);

    @Override
    public boolean canProcess(Message msg) {
        return true;
    }

    @Override
    public Message processMessage(Message msg) throws ApplicationException,
            HL7Exception {
        Message answer = null;
        try {
            answer = ADTHL7ReceiveHelper.getInstance().handleMessage(msg,
                    SessionNames.ADTReceiveHelper);
        } catch (IOException e) {
            LogHelper.log(log, e);
        }
        return answer;
    }

}
