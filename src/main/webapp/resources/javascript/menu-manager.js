function SetCookieMenu(id) {
	var coo = new Cookie(id);
	var oldValue = coo.Load();
	var newValue;
	if (oldValue == null) {
		newValue = "no";
	} else {
		if (oldValue == "yes") {
			newValue = "no";
		} else {
			newValue = "yes";
		}
	}
	var exp = new Date();
	exp.setFullYear(exp.getFullYear() + 1);
	var coo = new Cookie(id, newValue, "/", exp.toGMTString());
	coo.Save();
}

function GetMenuCookie(id) {
	var coo = new Cookie(id);
	var oldValue = coo.Load();
	if (oldValue == null) {
		oldValue = 'yes';
	}
	if (oldValue == "yes") {
		$(document.getElementById(id)).next().show();
	} else {
		$(document.getElementById(id)).toggleClass("close_group").next().hide();
	}
}

function GetSideMenuCookie(id) {
	var coo = new Cookie(id);
	var oldValue = coo.Load();
	if (oldValue == null) {
		oldValue = 'yes';
	}

	if (oldValue == "yes") {
		$("#left_btn").click(function() {
			var coo = new Cookie(id);
			var oldValue = coo.Load();

			if (oldValue == "yes") {
				SetCookieMenu("sideMenu");
				$(".left_part").animate({
					"margin-left" : "-240px"
				}, "fast");
				$(".center_part").animate({
					"margin-left" : "50px"
				}, "fast");
				foldMenu();
			} else {
				SetCookieMenu("sideMenu");
				expandMenu();
			}
		});
	} else {
		$(".left_part").css("margin-left", "-240px");
		$(".center_part").css("margin-left", "50px");
		foldMenu();

		$("#left_btn").click(function() {
			var coo = new Cookie(id);
			var oldValue = coo.Load();

			if (oldValue == "yes") {
				SetCookieMenu("sideMenu");
				$(".left_part").animate({
					"margin-left" : "-240px"
				}, "fast");
				$(".center_part").animate({
					"margin-left" : "50px"
				}, "fast");
				foldMenu();
			} else {
				SetCookieMenu("sideMenu");
				expandMenu();
			}
		});
	}
}

function GetRightMenuCookie(id) {
	var coo = new Cookie(id);
	var oldValue = coo.Load();
	if (oldValue == null) {
		oldValue = 'yes';
	}

	if (oldValue == "yes") {
		$("#right_btn").toggle(function() {
			SetCookieMenu("rightMenu");
			$(".right_part").animate({
				"margin-right" : "-290px"
			}, "fast");
			$(".center_part").animate({
				"margin-right" : "50px"
			}, "fast");
			foldRight();
		}, function() {
			SetCookieMenu("rightMenu");
			expandRight();
		});
	} else {
		$(".right_part").css("margin-right", "-290px");
		$(".center_part").css("margin-right", "50px");
		foldRight();

		$("#right_btn").toggle(

		function() {
			SetCookieMenu("rightMenu");
			expandRight();
		}, function() {
			SetCookieMenu("rightMenu");
			$(".right_part").animate({
				"margin-right" : "-290px"
			}, "fast");
			$(".center_part").animate({
				"margin-right" : "50px"
			}, "fast");
			foldRight();
		});
	}
}

function PreGetSideMenuCookie(id) {
	var coo = new Cookie(id);
	var oldValue = coo.Load();
	if (oldValue == null) {
		oldValue = 'yes';
	}

	if (oldValue != "yes") {
		if (id == "sideMenu")
			$(".left_part").css("margin-left", "-240px");
		if (id == "rightMenu")
			$(".right_part").css("margin-right", "-290px")
					.css("float", "right");
	}
}

function foldMenu() {

	$("#left_btn").attr('title', 'Clicca per visualizzare il menu di sinistra');
	$("#left_btn .ui-button-icon-left").removeClass('ui-icon-triangle-1-w')
			.addClass('ui-icon-triangle-1-e');
	$('.main_menu').hide();
	$('.sub_menu').hide();
	$('.ui-accordion').hide();
	$('.ui-treetable').hide();
}

function expandMenu() {
	$(".left_part").animate({
		"margin-left" : "0px"
	}, "fast");
	$(".center_part").animate({
		"margin-left" : "290px"
	}, "fast");
	$("#left_btn").attr('title', 'Clicca per nascondere menu di sinistra');
	$("#left_btn .ui-button-icon-left").removeClass('ui-icon-triangle-1-e')
			.addClass('ui-icon-triangle-1-w');
	$('.main_menu').show();
	$('.sub_menu').show();
	$('.ui-accordion').show();
	$('.ui-treetable').show();
}

function foldRight() {
	$("#right_btn")
			.attr('title', 'Clicca per visualizzare il menu di sinistra');
	$("#right_btn .ui-button-icon-left").removeClass('ui-icon-triangle-1-e')
			.addClass('ui-icon-triangle-1-w');
	$('#table').hide();
}

function expandRight() {
	$(".left_part").animate({
		"margin-right" : "0px"
	}, "fast");
	$(".center_part").animate({
		"margin-right" : "290px"
	}, "fast");
	$("#right_btn").attr('title', 'Clicca per nascondere menu di sinistra');
	$("#right_btn .ui-button-icon-left").removeClass('ui-icon-triangle-1-w')
			.addClass('ui-icon-triangle-1-e');
	$('#table').show();
}
