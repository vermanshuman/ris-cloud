try {
	PrimeFacesExt.locales.TimePicker['it'] = {
		hourText : 'Ora',
		minuteText : 'Minuti',
		amPmText : [ 'AM', 'PM' ],
		closeButtonText : 'Chiudi',
		nowButtonText : 'Adesso',
		deselectButtonText : 'Svuota'
	};
} catch (e) {
}

PrimeFaces.locales['oo'] = {
    closeText : 'Chiuso',
    prevText : 'Precedente',
    nextText : 'Successivo',
    currentText : 'Home',
    monthNames : [ 'Gennaio', 'Febbraio', 'Marzo', 'Aprile', 'Maggio',
        'Giugno', 'Luglio', 'Agosto', 'Settembre', 'Ottobre', 'Novembre',
        'Dicembre' ],
    monthNamesShort : [ 'Gen', 'Feb', 'Mar', 'Apr', 'Mag', 'Giu', 'Lug', 'Ago',
        'Sett', 'Ott', 'Nov', 'Dic' ],
    dayNames : [ 'Domenica', 'Lunedì', 'Martedì', 'Mercoledì', 'Giovedì',
        'Venerdì', 'Sabato' ],
    dayNamesShort : [ 'Dom', 'Lun', 'Mar', 'Mer', 'Gio', 'Ven', 'Sab' ],
    dayNamesMin : [ 'D', 'L', 'M', 'M ', 'G', 'V ', 'S' ],
    weekHeader : 'Settimana',
    firstDay : 1,
    isRTL : false,
    showMonthAfterYear : false,
    yearSuffix : '',
    timeOnlyTitle : 'Seleziona Ora',
    timeText : 'Tempo',
    hourText : 'Ora',
    minuteText : 'Minuti',
    secondText : 'Secondi',
    currentText : 'Data Corrente',
    ampm : false,
    month : 'Mese',
    week : 'Settimana',
    day : 'Giorno',
    allDayText : 'Tutti i giorni',
    hours : 'Housrs'
};

try {
    PrimeFacesExt.locales.TimePicker['oo'] = {
        hourText : 'Ora',
        minuteText : 'Minuti',
        amPmText : [ 'AM', 'PM' ],
        closeButtonText : 'Chiudi',
        nowButtonText : 'Adesso',
        deselectButtonText : 'Svuota'
    };
} catch (e) {
}

function onHourShowCallback(a) {
	return 20 < a || 6 > a ? !1 : !0;
}
function onMinuteShowCallback(a, b) {
	return 20 == a && 30 <= b || 6 == a && 30 > b ? !1 : !0;
}
function tpStartOnHourShowCallback(a) {
	if (a == -1) {
		return !0;
	}
	if ("undefined" === typeof endTimeWidget)
		return !1;
	var b = parseInt(endTimeWidget.getHours());
	return b == -1 ? true : parseInt(a) <= b ? !0 : !1;
}
function tpStartOnMinuteShowCallback(a, b) {
	if (a == -1 && b == -1) {
		return !0;
	}
	if ("undefined" === typeof endTimeWidget)
		return !1;
	var c = parseInt(endTimeWidget.getHours()), d = parseInt(endTimeWidget
			.getMinutes());
	return (c == -1 && d == -1) ? true : parseInt(a) < c || parseInt(a) == c
			&& parseInt(b) < d ? !0 : !1;
}
function tpEndOnHourShowCallback(a) {
	if (a == -1) {
		return !0;
	}
	if ("undefined" === typeof startTimeWidget)
		return !1;
	var b = parseInt(startTimeWidget.getHours());
	return b == -1 ? true : parseInt(a) >= b ? !0 : !1;
}
function tpEndOnMinuteShowCallback(a, b) {
	if (a == -1 && b == -1) {
		return !0;
	}
	if ("undefined" === typeof startTimeWidget)
		return !1;
	var c = parseInt(startTimeWidget.getHours()), d = parseInt(startTimeWidget
			.getMinutes());
	return (c == -1 && d == -1) ? true : parseInt(a) > c || parseInt(a) == c
			&& parseInt(b) > d ? !0 : !1;
};