package it.nexera.ris.common.helpers.logic;

import it.nexera.ris.common.helpers.BaseHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import org.hibernate.Session;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;

public class DiagnosticHelper extends BaseHelper {
    public static String getNextProgressiveNumber() {
        String newProgressiveNumber = null;

        try {
            Session session = DaoManager.getSession();

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            Long year = Long.valueOf(calendar.get(Calendar.YEAR));

            Long maxYear = (Long) session.createQuery(
                    "select max(cardNumberYear) from Diagnostic")
                    .uniqueResult();

            boolean resetProgressiveNumber = false;
            if (maxYear != null) {
                Long curYear = year;
                if (curYear > maxYear) {
                    resetProgressiveNumber = true;
                }
            } else {
                resetProgressiveNumber = true;
            }

            Long progressiveNumber = null;
            if (!resetProgressiveNumber) {
                String progressiveNumberStr = (String) session
                        .createQuery(
                                "select max(cardNumber) from Diagnostic where cardNumberYear =:curYear")
                        .setParameter("curYear", year).uniqueResult();
                progressiveNumberStr = progressiveNumberStr.substring(4);
                progressiveNumber = Long.valueOf(progressiveNumberStr);

                ++progressiveNumber;
            } else {
                progressiveNumber = 1l;
            }

            StringBuilder sb = new StringBuilder();
            sb.append(year);
            DecimalFormat dec = new DecimalFormat("0000000");
            sb.append(dec.format(progressiveNumber));

            newProgressiveNumber = sb.toString();
        } catch (Exception e) {
            LogHelper.log(log, e);
        }

        return newProgressiveNumber;
    }
}
