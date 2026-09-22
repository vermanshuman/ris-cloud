function numbers_only(myfield, e, int, float) {

	var key;
	var keychar;
	var value = myfield.value;
	var isCtrl = false;

	if (window.event) {
		key = window.event.keyCode;
	} else if (e) {
		key = e.which;
		isCtrl = e.ctrlKey;
	} else {
		return true;
	}
	keychar = String.fromCharCode(key);

	// control keys
	if ((key == null)
			|| (key == 0)
			|| (key == 8)
			|| (key == 9)
			|| (key == 13)
			|| (key == 27)
			|| (isCtrl && ((key == 120) || (key == 97) || (key == 99) || (key == 118)))) {
		return true;
	} else if ((("0123456789").indexOf(keychar) > -1)) {
		if (value.indexOf(".") != -1
				&& (typeof myfield.selectionStart == "number"
						&& myfield.selectionStart > value.indexOf(".") || typeof myfield.selectionStart != "number")
				&& value.indexOf(".") <= value.length - (float + 1)) {
			return false;
		} else if (value.indexOf(",") != -1
				&& (typeof myfield.selectionStart == "number"
						&& myfield.selectionStart > value.indexOf(",") || typeof myfield.selectionStart != "number")
				&& value.indexOf(",") <= value.length - (float + 1)) {
			return false;
		} else {
			if (value.length >= int && value.indexOf(",") == -1
					&& value.indexOf(".") == -1) {
				return false;
			} else if (value.indexOf(".") != -1
					&& value.substring(0, value.indexOf(".")).length >= int
					&& (typeof myfield.selectionStart == "number" && myfield.selectionStart <= value
							.indexOf("."))) {
				return false;
			} else if (value.indexOf(",") != -1
					&& value.substring(0, value.indexOf(",")).length >= int
					&& (typeof myfield.selectionStart == "number" && myfield.selectionStart <= value
							.indexOf(","))) {
				return false;
			} else {
				return true;
			}
		}
	} else if (((keychar == ".") || (keychar == ",")) && float > 0) {
		if (value.indexOf(".") != -1 || value.indexOf(",") != -1) {
			return false;
		} else if (value.length == 0) {
			return false;
		} else {
			return true;
		}
	} else
		return false;
}

function check_numbers(field, int, float) {
	var valueStr = field.value;
	if (!isNaN(valueStr.replace(",", "."))
			|| !isNaN(valueStr.replace(".", ","))) {
		if (valueStr.indexOf(".") != -1
				&& valueStr.indexOf(".") < valueStr.length - (float + 1)
				&& valueStr.substring(0, valueStr.indexOf(".")).length <= int) {
			if (isNaN(valueStr)) {
				valueStr = valueStr.replace(".", ",");
			}
			var value = parseFloat(valueStr);
			field.value = value.toFixed(float);
			return;
		} else if (valueStr.indexOf(",") != -1
				&& valueStr.indexOf(",") < valueStr.length - (float + 1)
				&& valueStr.substring(0, valueStr.indexOf(",")).length <= int) {
			if (isNaN(valueStr)) {
				valueStr = valueStr.replace(",", ".");
			}
			var value = parseFloat(valueStr);
			field.value = value.toFixed(float);
			return;
		} else if (valueStr.length > int && valueStr.indexOf(",") == -1
				&& valueStr.indexOf(".") == -1)

		{
			field.value = "";
			return;
		} else if ((valueStr.indexOf(".") != -1 && valueStr.substring(0,
				valueStr.indexOf(".")).length > int)
				|| (valueStr.indexOf(",") != -1 && valueStr.substring(0,
						valueStr.indexOf(",")).length > int)) {
			field.value = "";
			return;
		}

	} else if (field.value == "-") {
		return;
	} else {
		field.value = "";
	}
}

function text_area_limit(myfield, e, number) {
	if (window.event) {
		key = window.event.keyCode;
	} else if (e) {
		key = e.which;
		isCtrl = e.ctrlKey;
	} else
		return true;

	if ((key == null) || (key == 0) || (key == 8) || (key == 9) || (key == 13)
			|| (key == 27) || isCtrl) {
		return true;
	}

	if (myfield.value.length >= number) {
		return false;
	} else {
		return true;
	}
}

function text_area_check(myfield, number) {
	if (myfield.value.length > number) {
		myfield.value = myfield.value.substring(0, number);
	}
}

function checknumber(field) {

	var valueStr = field.value;
	if (!isNaN(valueStr)) {
		return;
	} else if (field.value == "-") {
		return;
	} else {
		field.value = "";
	}
}

function isnumberOrDot(char) {
	if (("0123456789.,").indexOf(char) > -1) {
		return true;
	} else {
		return false;
	}
}

function isnumber(char) {
	if (("-0123456789").indexOf(char) > -1) {
		return true;
	} else {
		return false;
	}
}

function FormatMoneyString(str) {
	var str1 = parseFloat(str).toFixed(2);
	str1 = str1.replace('.', ',');
	var str2 = str1.substring(0, str1.indexOf(',', 0));
	var str3 = str1.substring(str1.indexOf(',', 0), str1.length);
	var str4 = '';
	var i = 0;
	while (str2[i] != null) {
		if (i != 0 && (str2.length - i) % 3 == 0) {
			str4 += '.';
		}
		str4 += str2[i];
		i++;
	}
	i = 0;
	while (str3[i] != null) {
		str4 += str3[i];
		i++;
	}
	return "€&nbsp;" + str4;
}

function time_only(myfield, e, int, float) {

	var key;
	var keychar;
	var value = myfield.value;
	var isCtrl = false;

	if (window.event) {
		key = window.event.keyCode;
	} else if (e) {
		key = e.which;
	} else {
		return true;
	}
	keychar = String.fromCharCode(key);

	// control keys
	if ((key == null)
			|| (key == 0)
			|| (key == 8)
			|| (key == 9)
			|| (key == 13)
			|| (key == 27)
			|| (isCtrl && ((key == 120) || (key == 97) || (key == 99) || (key == 118)))) {
		return true;
	} else if ((("0123456789").indexOf(keychar) > -1)) {
		if (value.indexOf(".") != -1
				&& (typeof myfield.selectionStart == "number"
						&& myfield.selectionStart > value.indexOf(".") || typeof myfield.selectionStart != "number")
				&& value.indexOf(".") <= value.length - (float + 1)) {
			return false;
		} else if (value.indexOf(",") != -1
				&& (typeof myfield.selectionStart == "number"
						&& myfield.selectionStart > value.indexOf(",") || typeof myfield.selectionStart != "number")
				&& value.indexOf(",") <= value.length - (float + 1)) {
			return false;
		} else {
			if (value.length >= int && value.indexOf(",") == -1
					&& value.indexOf(".") == -1) {
				return false;
			} else if (value.indexOf(".") != -1
					&& value.substring(0, value.indexOf(".")).length >= int
					&& (typeof myfield.selectionStart == "number" && myfield.selectionStart <= value
							.indexOf("."))) {
				return false;
			} else if (value.indexOf(",") != -1
					&& value.substring(0, value.indexOf(",")).length >= int
					&& (typeof myfield.selectionStart == "number" && myfield.selectionStart <= value
							.indexOf(","))) {
				return false;
			} else {
				return true;
			}
		}
	} else if (((keychar == ".") || (keychar == ",")) && float > 0) {
		if (value.indexOf(".") != -1 || value.indexOf(",") != -1) {
			return false;
		} else if (value.length == 0) {
			return false;
		} else {
			return true;
		}
	} else
		return false;
}

function check_date(field) {
	if (!validateDate(field.value)) {
		field.value = "";
	}
}

function validateDate(val) {
	if (val == undefined) {
		return false;
	}
	var str = val.split('/');

	if (str[0] > 31 || str[0] < 1) {
		return false;
	}

	if (str[1] > 12 || str[1] < 1) {
		return false;
	}
	return true;
}

function check_date_time(field) {
	var str = field.value.split(' ');
	if (!validateDate(str[0]) || !validateTime(str[1])) {
		field.value = "";
	}
}

function check_time(field) {
	if (!validateTime(field.value)) {
		field.value = "";
	}
}

function validateTime(val) {
	if (val == undefined) {
		return false;
	}
	var str = val.split(':');

	if (str[0] > 23) {
		return false;
	}

	if (str[1] > 59) {
		return false;
	}

	if (str[2] != undefined && str[2] > 59) {
		return false;
	}
	return true;
}

function check_max(field, val) {
	var str = field.value;

	if (str > val) {
		field.value = "";
	}
}

function fieldValueToUpperCase(field) {
	field.value = field.value.toUpperCase();
}
function fixAutocompletePanel(myfield) {
	var top = $(document.getElementById(myfield + "_input")).position().top;
	top = top + 200.0;

	if (document.getElementById(myfield + '_panel') != null) {
		$(document.getElementById(myfield + '_panel')).css('top', top);
	}
}

function numbers_only_min_max(myfield, e, int, float, min, max) {
	if (!numbers_only(myfield, e, int, float)) {
		return false;
	}

	var value = parseFloat(myfield.value);
	if (!isNaN(value)) {
		if (value < min || value > max) {
			return false;
		}
	}

	return true;
}

function check_numbers_min_max(field, int, float, min, max) {
	check_numbers(field, int, float);
	var value = parseFloat(field.value);
	if (!isNaN(value)) {
		if (value < min) {
			field.value = min;
		} else if (value > max) {
			field.value = max;
		}
	}
}

function hour_minutes_only(myfield) {
	var value = myfield.value;

	var temp = value.indexOf("_");
	if (temp != -1) {
		return;
	}

	var mySplitResult = value.split(":");
	var hour = parseInt(mySplitResult[0]);
	var minute = parseInt(mySplitResult[1]);

	if (hour > 23) {
		myfield.value = "23:" + mySplitResult[1];
		mySplitResult[0] = "23";

	}
	if (minute > 59) {
		mySplitResult[1] = "59";
		myfield.value = mySplitResult[0] + ":" + mySplitResult[1];

	}
}

function numbers_letters_only(textFieldName) {
	var field = $(textFieldName);
	field.bind("input", function(event) {
		var out = "";
		var str = field.val();
		
		for (var i = 0; i < str.length; i++) {
			if (/\w|\s|[']/.test(str.charAt(i))) {
				out = out.concat(str.charAt(i));
			}
		}
		field.val(out);
	});
}

function letters_only(textFieldName) {
	var field = $(textFieldName);
	field.bind("input", function(event) {
		var out = "";
		var str = field.val();
		for (var i = 0; i < str.length; i++) {
			if (/[A-Za-z]/.test(str.charAt(i))) {
				out = out.concat(str.charAt(i));
			}
		}
		field.val(out);
	});
}

function numbers_letters_dot_dash(textFieldName) {
	var field = $(textFieldName);
	field.bind("input", function (event) {
		var out = "";
		var str = field.val();

		for (var i = 0; i < str.length; i++) {
			if (/\w|\s|['.-]/.test(str.charAt(i))) {
				out = out.concat(str.charAt(i));
			}
		}
		field.val(out);
	});
}