function fixExpandRow() {
	for (var i = 0; $('.ui-expanded-row-content').length > i; i++) {
		for (var j = 0; $('.ui-expanded-row-content')[i].parentNode.rows.length > j; j++) {
			if ($('.ui-expanded-row-content')[i].parentNode.rows[j] == $('.ui-expanded-row-content')[i]) {
				$($('.ui-expanded-row-content')[i])
						.addClass(
								$('.ui-expanded-row-content')[i].parentNode.rows[j - 1].className
										.split(' ')[1]);
			}
		}
	}
}

function autocompleteOnEnter(event, widget) {
	if (window.event) {
		key = window.event.keyCode;
	} else if (event) {
		key = event.which;
	} else {
		return true;
	}

	if (key == 13) {
		if (widget.input[0].value == '') {
			widget.search('');
		}
		return false;
	}
	return true;
}

function searchOnEnter(event, but_id) {
	if (window.event) {
		key = window.event.keyCode;
	} else if (event) {
		key = event.which;
	} else {
		return true;
	}

	if (key == 13) {
		$(document.getElementById(but_id)).click();
		return false;
	}
	return true;
}

function stopOnEnter(event, but_id) {
	if (window.event) {
		key = window.event.keyCode;
	} else if (event) {
		key = event.which;
	} else {
		return true;
	}

	if (key == 13) {
		return false;
	}
	return true;
}

function isEnterKey(event) {
	if (window.event) {
		key = window.event.keyCode;
	} else if (event) {
		key = event.which;
	} else {
		return false;
	}

	if (key == 13) {
		return true;
	}
	return false;
}

function fixCalendarInput() {
	for (var i = 0; i < $(".ui-inputfield.hasDatepicker").length; i++) {
		if ($(".ui-inputfield.hasDatepicker")[i].alt != "") {
			$($(".ui-inputfield.hasDatepicker")[i]).mask(
					$(".ui-inputfield.hasDatepicker")[i].alt);
			continue;
		}
		$($(".ui-inputfield.hasDatepicker")[i]).mask('99/99/9999');
	}

	$(".ui-inputfield.hasTimepicker:not(.time_only)").mask('99:99');
}

function fixImageUpload() {

	for (var i = 0; i < $('.image_upload').length; i++) {
		$('.image_upload')[i].accept = "image/jpeg,image/png,image/bmp,image/gif";
	}

	$('.image_upload').change(
			function() {
				var filename = $(this).val();
				if (!/\.jpg$/.test(filename) && !/\.jpeg$/.test(filename)
						&& !/\.png$/.test(filename) && !/\.bmp$/.test(filename)
						&& !/\.gif$/.test(filename)) {
					alert('Please select a image');
					$(this).val('');
				}
			});

	for (var i = 0; i < $('.csv_upload').length; i++) {
		$('.csv_upload')[i].accept = "text/x-comma-separated-values";
	}

	$('.csv_upload').change(function() {
		var filename = $(this).val();
		if (!/\.csv$/.test(filename)) {
			alert('Please select a CSV file');
			$(this).val('');
		}
	});

	for (var i = 0; i < $('.mp3_upload').length; i++) {
		$('.mp3_upload')[i].accept = "audio/mpeg";
	}

	$('.mp3_upload').change(function() {
		var filename = $(this).val();
		if (!/\.mp3$/.test(filename)) {
			alert('Please select a MP3 file');
			$(this).val('');
		}
	});

	for (var i = 0; i < $('.ogg_upload').length; i++) {
		$('.ogg_upload')[i].accept = "audio/ogg";
	}

	$('.ogg_upload').change(function() {
		var filename = $(this).val();
		if (!/\.ogg$/.test(filename)) {
			alert('Please select a OGG file');
			$(this).val('');
		}
	});

	for (var i = 0; i < $('.xml_upload').length; i++) {
		$('.xml_upload')[i].accept = "text/xml";
	}

	$('.xml_upload').change(function() {
		var filename = $(this).val();
		if (!/\.xml$/.test(filename)) {
			alert('Please select a XML file');
			$(this).val('');
		}
	});

}

function startTime() {
	var today = new Date();
	var h = today.getHours();
	var m = today.getMinutes();
	var s = today.getSeconds();

	m = checkTime(m);
	s = checkTime(s);
	document.getElementById('time').innerHTML = h + ":" + m + ":" + s;
	t = setTimeout(function() {
		startTime();
	}, 500);
}

function checkTime(i) {
	if (i < 10) {
		i = "0" + i;
	}
	return i;
}

var prevVal = '';
function onDateSelectStart() {
	if (document.getElementById('table:registerDate_input').value == '__/__/____') {
		document.getElementById('table:registerDate_input').value = '';
		if (prevVal != document.getElementById('table:registerDate_input').value) {
			dateWV.fireDateSelectEvent();
		}
	}
	if (prevVal != document.getElementById('table:registerDate_input').value) {
		prevVal = document.getElementById('table:registerDate_input').value;
		$('#ui-datepicker-div').hide();
	}
}

function removeAllTooltips(editorSelector) {
	var elements = $(editorSelector).find('*');
	$.each(elements, function(index, item) {
		$(item).removeAttr('title');
	});
}

/**
 * @param editor CkEditor instance
 */
function configureEditorWithGlossaryCommand(editor) {
	const iframe = $('.cke_wysiwyg_frame');
	if (iframe.length > 0) {
		const ckDocument = iframe[0].contentDocument.documentElement;
		ckDocument.setAttribute('style', 'height: 100% !important');
	}

	const editorDisabled = $('#editorDisabledHidden').val();
	if (editorDisabled == "true") {
		editor.setReadOnly(true);
		$('.cke_button').addClass('cke_button_disabled').css("pointer-events", "none");
	}

	if (!editor.readOnly) {
		// focus editor and place cursor after all text present in the editor
		editor.focus();
		const range = editor.createRange();
		range.moveToElementEditEnd( range.root );
		editor.getSelection().selectRanges([range]);
	}

	editor.addCommand('myGreetingCommand', {
		exec: function (editor, data) {
			PF('insertGlossary').show();
		}
	});
	editor.keystrokeHandler.keystrokes[CKEDITOR.ALT + 90 /* Z */] = 'myGreetingCommand';
}

function onCheck(component, column) {
	var tbody = component.parentNode.parentNode.parentNode;
	var id = parseInt(component.parentNode.parentNode.attributes['data-ri'].nodeValue);

	if (component.parentNode.className.split(' ')[0] == "sub_header") {
		id++;
		var item = tbody.rows[id];
		while (item.cells[column].className.split(' ')[0] != "sub_header") {
			item.childNodes[column].firstChild.checked = component.checked;
			id++;
			item = tbody.rows[id];
		}
	} else {
		id--;
		var item = tbody.rows[id];
		while (item.childNodes[column].className.split(' ')[0] != "sub_header") {
			id--;
			item = tbody.rows[id];
		}
		item.childNodes[column].firstChild.checked = false;
	}
}
function onPageLoadGetMenuItem() {
	var menuItem = $('#menuItemHidden').val();
	$('#' + menuItem).addClass('active');
	
}

function lockValueOnBtn(elementId){
    var valueLi = document.getElementById(elementId).value;
    if(valueLi !== undefined && valueLi.trim() !== '') {
        createHtmlTag(valueLi);
        $('#' + elementId).val('');
    }
}
function lockValue(event) {
    if (event.keyCode == 13) {
        if (event.currentTarget.value != null && event.currentTarget.value.trim() != '') {
            var valueLi = event.currentTarget.value;
            event.currentTarget.value = '';
            createHtmlTag(valueLi);
            event.currentTarget.value = event.currentTarget.value.substring(0, event.currentTarget.value.length - 2);
        }
        return false;
    }
    return true;
}

function createHtmlTag(valueLi) {
    var liel = document.createElement("DIV");
    liel.className = 'ui-autocomplete-token ui-state-active ui-corner-all ui-helper-hidden';
    liel.style = 'display: list-item;';
    liel.setAttribute('data-token-value', valueLi);
    var closeNode = document.createElement("SPAN");
    closeNode.className = 'ui-autocomplete-token-icon ui-icon ui-icon-close';
    closeNode.onclick = function () {
        this.parentElement.remove();
    };
    var liNode = document.createElement("SPAN");
    liNode.className = 'ui-autocomplete-token-label';
    liNode.innerHTML = valueLi;
    liel.appendChild(closeNode);
    liel.appendChild(liNode);
    document.getElementById('tagsContainer').appendChild(liel);
}

function getAbsoluteServerUrl(relativeUrl) {
	let div = document.createElement("div");
	// Uses an innerHTML property to obtain an absolute URL
	div.innerHTML = '<a href="' + relativeUrl + '"/>';
	return div.firstChild.href;
}

function preventBackspaceDefaultBehaviour(event) {
	if (event.key === 'Backspace' &&
		(event.target.tagName !== "TEXTAREA") &&
		(event.target.tagName !== "INPUT")) {

		event.stopPropagation();
		event.preventDefault();
	}
}