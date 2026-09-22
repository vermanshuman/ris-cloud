package it.nexera.ris.common.helpers;

import it.nexera.ris.common.enums.SexTypes;

import java.util.Date;
import java.util.GregorianCalendar;

public class CalcoloCodiceFiscale {
    private static final String mesi = "ABCDEHLMPRST";

    private static final String vocali = "AEIOU";

    private static final String consonanti = "BCDFGHJKLMNPQRSTVWXYZ";

    private static final String alfabeto = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static int[][] matricecod;

    static {
        matricecod = new int[91][2];
        matricecod[0][1] = 1;
        matricecod[0][0] = 0;
        matricecod[1][1] = 0;
        matricecod[1][0] = 1;
        matricecod[2][1] = 5;
        matricecod[2][0] = 2;
        matricecod[3][1] = 7;
        matricecod[3][0] = 3;
        matricecod[4][1] = 9;
        matricecod[4][0] = 4;
        matricecod[5][1] = 13;
        matricecod[5][0] = 5;
        matricecod[6][1] = 15;
        matricecod[6][0] = 6;
        matricecod[7][1] = 17;
        matricecod[7][0] = 7;
        matricecod[8][1] = 19;
        matricecod[8][0] = 8;
        matricecod[9][1] = 21;
        matricecod[9][0] = 9;
        matricecod[10][1] = 1;
        matricecod[10][0] = 0;
        matricecod[11][1] = 0;
        matricecod[11][0] = 1;
        matricecod[12][1] = 5;
        matricecod[12][0] = 2;
        matricecod[13][1] = 7;
        matricecod[13][0] = 3;
        matricecod[14][1] = 9;
        matricecod[14][0] = 4;
        matricecod[15][1] = 13;
        matricecod[15][0] = 5;
        matricecod[16][1] = 15;
        matricecod[16][0] = 6;
        matricecod[17][1] = 17;
        matricecod[17][0] = 7;
        matricecod[18][1] = 19;
        matricecod[18][0] = 8;
        matricecod[19][1] = 21;
        matricecod[19][0] = 9;
        matricecod[20][1] = 2;
        matricecod[20][0] = 10;
        matricecod[21][1] = 4;
        matricecod[21][0] = 11;
        matricecod[22][1] = 18;
        matricecod[22][0] = 12;
        matricecod[23][1] = 20;
        matricecod[23][0] = 13;
        matricecod[24][1] = 11;
        matricecod[24][0] = 14;
        matricecod[25][1] = 3;
        matricecod[25][0] = 15;
        matricecod[26][1] = 6;
        matricecod[26][0] = 16;
        matricecod[27][1] = 8;
        matricecod[27][0] = 17;
        matricecod[28][1] = 12;
        matricecod[28][0] = 18;
        matricecod[29][1] = 14;
        matricecod[29][0] = 19;
        matricecod[30][1] = 16;
        matricecod[30][0] = 20;
        matricecod[31][1] = 10;
        matricecod[31][0] = 21;
        matricecod[32][1] = 22;
        matricecod[32][0] = 22;
        matricecod[33][1] = 25;
        matricecod[33][0] = 23;
        matricecod[34][1] = 24;
        matricecod[34][0] = 24;
        matricecod[35][1] = 23;
        matricecod[35][0] = 25;
    }

    @SuppressWarnings("static-access")
    public static String calcola(String nomeIn, String cognomeIn,
                                 Date dataDiNascita, String codiceCatastaleComuneNascita,
                                 SexTypes sesso) throws Exception {
        String retval = null;
        String nome = nomeIn.toUpperCase();
        String cognome = cognomeIn.toUpperCase();
        int anno = 0, mese = 0, giorno = 0, codcontrollo = 0;
        //Date data = Util.stringToDate(dataDiNascita);
        Date data = dataDiNascita;

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(data);
        String a = Integer.toString(cal.get(cal.YEAR));
        a = a.substring(a.length() - 2, a.length());
        anno = Integer.parseInt(a);
        mese = cal.get(cal.MONTH);
        giorno = cal.get(cal.DATE);

        retval = calcolaCognome(noAccentate(cognome.trim()))
                + calcolaNome(noAccentate(nome.trim()));
        if (SexTypes.FEMALE.equals(sesso)) {
            giorno = giorno + 40;
        }
        retval += ((anno < 10) ? "0" : "") + Integer.toString(anno)
                + mesi.charAt(mese) + ((giorno < 10) ? "0" : "")
                + Integer.toString(giorno);
        retval += codiceCatastaleComuneNascita;
        for (int i = 0; i < 15; i++) {
            codcontrollo += matricecod[Character.getNumericValue(retval
                    .charAt(i))][(i + 1) % 2];
        }
        retval += alfabeto.charAt(codcontrollo % 26);
        return retval;
    }

    private static String calcolaCognome(String cogn) {
        int i = 0;
        String stringa = "";
        //trova consonanti
        while ((stringa.length() < 3) && (i + 1 <= cogn.length())) {
            if (consonanti.indexOf(cogn.charAt(i)) > -1) {
                stringa += cogn.charAt(i);
            }
            i++;
        }
        i = 0;
        //se non bastano prende vocali
        while ((stringa.length() < 3) && (i + 1 <= cogn.length())) {
            if (vocali.indexOf(cogn.charAt(i)) > -1) {
                stringa += cogn.charAt(i);
            }
            i++;
        }
        //se non bastano aggiungo le x
        if (stringa.length() < 3) {
            for (i = stringa.length(); i < 3; i++) {
                stringa += "X";
            }
        }
        return stringa;
    }

    private static String calcolaNome(String nom) {
        int i = 0;
        String stringa = "", cons = "";
        //trova consonanti
        while ((cons.length() < 4) && (i + 1 <= nom.length())) {
            if (consonanti.indexOf(nom.charAt(i)) > -1) {
                cons += nom.charAt(i);
            }
            i++;
        }
        //se sono + di 3 prende 1° 3° 4°
        if (cons.length() > 3) {
            stringa = cons.substring(0, 1) + cons.substring(2, 4);
            return stringa;
        } else {
            stringa = cons;
        }
        i = 0;
        //se non bastano prende vocali
        while ((stringa.length() < 3) && (i + 1 <= nom.length())) {
            if (vocali.indexOf(nom.charAt(i)) > -1) {
                stringa += nom.charAt(i);
            }
            i++;
        }
        //se non bastano aggiungo le x
        if (stringa.length() < 3) {
            for (i = stringa.length(); i < 3; i++) {
                stringa += "X";
            }
        }
        return stringa;
    }

    private static String noAccentate(String s) {
        //------------------------------------------------------------------------------//
        // noAccentate //
        // restituisce la stringa s trasformando le lettere accentate in non accentate //
        // ad esempio "andт" viene trasformata "ando" //
        //------------------------------------------------------------------------------//
        final String ACCENTATE = "������������";
        final String NOACCENTO = "AEEIOUAEEIOU";
        int i = 0;
        //scorre la stringa originale
        while (i < s.length()) {
            int p = ACCENTATE.indexOf(s.charAt(i));
            //se ha trovato una lettera accentata
            if (p > -1) {
                //sostituisce con la relativa non accentata
                s = s.substring(0, i) + NOACCENTO.charAt(p)
                        + s.substring(i + 1);
            }
            i++;
        }

        return s;
    }

}
