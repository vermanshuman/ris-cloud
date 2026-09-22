// This is modified plugin
var voisisPluginVer = '2.12.2';

//Voisis NameSpace
var voisisNS = window.voisisNS || {};

voisisNS.browserPar = ["MIC:", "AU:"];
voisisNS.userAgent = "";
voisisNS.pluginLog = Object.freeze({"INIT": -1, "ERROR": 1, "API":2, "EVENTS":3, "TEXT":4});
voisisNS.jsonLog = [];
voisisNS.initParCompleted = false;
voisisNS.initEditorCompleted = [];
voisisNS.focusedEditor = null;
voisisNS.atEditor = null;
voisisNS.dialogImgPath = '/VoisisWebEditor/img';
voisisNS.lexTab = true;
voisisNS.ckEditor = "ckeditorVoisis";
voisisNS.quill = "quillVoisis";


var voisisEditorArr = [];
var voisisCbk = [];
var voisisAtFiles = [];
var voisisUserConnected = [];
var voisisConnectFailed = [];

var stopStartTimer = null;
function stopStartTrans() {
	$(".voisis-modal").hide();
	if(voisisApp.hybridApp){
		var message = JSON.stringify({ type: "mic", content: "enable-butt" });
		window.chrome.webview.postMessage(message);
	}
}

//////////////////

///////////////////

var voisisApp = {};
(function(context){

	var voisisWebEditorJsUrl;
	var showUppButt;
	var autoCleanUp;
	var autotextReplace;
	var ajaxGif;
	var onInit = [];
	var onConnect;
	var onConnectFailed;
	var onDisconnect;
	var onStartDict;
	var onStopDict;
	var onRecognition;
	var onCommandRecognition;
	var onServerError;
	var useCookie;
	var checkWordsBool = false;
	var textAlign;
	var sysUrl = '';
	var asrTech = '';
	var asrUrl = '';
	var signUrl = '';
	var userId = '';
	var vocabId = '';
	var vocabList = {};
	var userType = '';
	var userDictOff = '';
	var userFontFamily = '';
	var userFontSize = '';
	var logLevel = 0;
	var atDir = '';
	var audioWizard = false;
	var checkWordsEnable = false;
	var checkWords = {};
	var spellCheckEnable = false;
	var sendComment = false;
	var changePwd = false;
	var roughDebounce = false;
	var editorConfig = {};
	var hybridApp = false;
	var dictPar = {};
	var dictToCmd = false;


	///////////////////////////////////////////////////////

	context.editorType = function(editor) {
		if((typeof(Quill) != 'undefined') && (editor instanceof Quill)){
			return voisisNS.quill;
		}
		else{
			return voisisNS.ckEditor;
		}
	};

	context.editorId = function(editor) {
		if(voisisApp.editorType(editor) == voisisNS.quill){
			return editor.container.id;
		}
		else{
			return editor.id;
		}
	};

	context.editorContents = function(editor) {
		if(voisisApp.editorType(editor) == voisisNS.quill){
			return editor.getText();
		}
		else{
			return editor.getData();
		}
	};

	context.setEditorText = function(editor, contents) {
		if(voisisApp.editorType(editor) == voisisNS.quill){
			editor.setText(contents);
		}
		else{
			editor.setData(contents);
		}
	};

	context.setUnsetRO = function(editor, value) {
		if(voisisApp.editorType(editor) == voisisNS.quill){
			editor.enable(value);
		}
		else{
			editor.setReadOnly(!value);
		}
	};

	context.isEditorEnabled = function(editor) {
		if(voisisApp.editorType(editor) == voisisNS.quill){
			return !editor.options.readOnly;
		}
		else{
			return !editor.readOnly;
		}
	};

	context.setEditorBackground = function(editor, color) {
		if(voisisApp.editorType(editor) == voisisNS.quill){
			var id = voisisApp.editorId(editor);
			$('#'+id).css("background-color", color);
		}
		else{
			if(editor.document)
				editor.document.getBody().setStyle('background-color', color);
		}
	};

	context.safeSetEditorPar = function(editor, par1, par2, retryCnt){
		if(voisisApp.editorType(editor) == voisisNS.quill){
			var id = voisisApp.editorId(editor);
			$('#'+id).css(par1, par2);
		}
		else{
			try {
				editor.document.getBody().setStyle(par1, par2);
			}
			catch(err) {
				if(retryCnt < 10){
					retryCnt++;
					setTimeout(voisisApp.safeSetEditorPar, 100, editor, par1, par2, retryCnt);
				}
			}
		}
	};

	context.setEditorHeight = function(editor, pxHeight) {
		if(voisisApp.editorType(editor) == voisisNS.quill){
			var id = voisisApp.editorId(editor);
			$('#'+id).css("height", pxHeight+'px');
		}
		else{
			editor.resize('100%', pxHeight, true);
		}
	};

	context.setEditorSize = function(editor, pxWidth, pxHeight){
		if(voisisApp.editorType(editor) == voisisNS.quill){
			var id = voisisApp.editorId(editor);
			$('#'+id).css("width", pxWidth+'px');
			$('#'+id).css("height", pxHeight+'px');
		}
		else{
			editor.resize(pxWidth, pxHeight, true);
		}
	}

	context.moveCaretToEnd = function(editor) {
		if(voisisApp.editorType(editor) == voisisNS.quill){
			editor.setSelection(editor.getLength(), 0);
		}
		else{
			try {
				var range = editor.createRange();
				range.moveToElementEditEnd(range.root);
				editor.getSelection().selectRanges([range]);
				range.scrollIntoView();
			} catch (error) {
				if (typeof voisisLib !== "undefined")
					voisisLib.pluginLogger(voisisNS.pluginLog.ERROR, "VoisisPlugin-moveCaretToEnd: "+error.message);
			}
		}
	}

	///////////////////////////////////////////////////////

	context.checkServerVersion = function(){
		var allowedRange = { min: "4.13.5" };
		$.ajax({
			type: 'GET',
			url: voisisApp.sysUrl+"/system.php?version="+voisisPluginVer,
			success: function(data) {
				var allowedArr = allowedRange.min.split(".");
				var allowedMaj = parseInt(allowedArr[0]);
				var allowedMin = parseInt(allowedArr[1]);
				var allowedFix = parseInt(allowedArr[2]);

				var serverVersion = data['version'];
				var serverArr = serverVersion.split(".");
				var serverMaj = parseInt(serverArr[0]);
				var serverMin = parseInt(serverArr[1]);
				var serverFix = parseInt(serverArr[2]);
				if((serverVersion &&
					((serverMaj > allowedMaj) ||
						((serverMaj == allowedMaj) && (serverMin > allowedMin)) ||
						((serverMaj == allowedMaj) && (serverMin == allowedMin) && (serverFix >= allowedFix)))) == false){
					alert("Il plugin di Voisis non e' compatibile con la versione del server");
					if (typeof voisisLib !== "undefined")
						voisisLib.pluginLogger(voisisNS.pluginLog.ERROR, "VoisisPlugin-not compatible");
				}
			},
			error: function(result){
				if (typeof voisisLib !== "undefined")
					voisisLib.pluginLogger(voisisNS.pluginLog.ERROR, "VoisisPlugin-access error");
			}
		});
	};

	context.getBrowserPar = function(){
		try {
			voisisNS.userAgent = navigator.userAgent;

			function getAudioApiSettings(track, index, array) {
				if (typeof track.getSettings === "undefined"){
					if (typeof voisisLib !== "undefined")
						voisisLib.pluginLogger(voisisNS.pluginLog.ERROR, "VoisisPlugin-track.getSettings not supported");
					return;
				}
				const settings = track.getSettings();
				if(settings.deviceId == "default")
				{
					if('autoGainControl' in settings){
						voisisNS.browserPar[1] = voisisNS.browserPar[1] + ('autoGainControl:'+settings.autoGainControl) + ";";
					}
					if('echoCancellation' in settings){
						voisisNS.browserPar[1] = voisisNS.browserPar[1] + ('echoCancellation:'+settings.echoCancellation) + ";";
					}
					if('noiseSuppression' in settings){
						voisisNS.browserPar[1] = voisisNS.browserPar[1] + ('noiseSuppression:'+settings.noiseSuppression) + ";";
					}
				}
			}

			// Check for protocol and IExplorer
			var prot = window.location.href.split("/")[0];
			if(prot == 'https:'){
				var ua = window.navigator.userAgent;
				var msie = ua.indexOf("MSIE ");
				if ((msie > 0 || !!navigator.userAgent.match(/Trident.*rv\:11\./)) == false){
					navigator.mediaDevices.getUserMedia({audio: true})
						.then(mediaStream => {
							const tracks = mediaStream.getAudioTracks();
							tracks.forEach(getAudioApiSettings);
						})
						.then(() => navigator.mediaDevices.enumerateDevices())
						.then(devices => {
							devices.forEach(d => {
								if((d.kind=='audioinput') && (d.deviceId=='default')) {
									voisisNS.browserPar[0]=voisisNS.browserPar[0]+d.deviceId+":"+d.label+";";
								}
							});
						});
				}
			}
		}
		catch(e){
			if (typeof voisisLib !== "undefined")
				voisisLib.pluginLogger(voisisNS.pluginLog.ERROR, "VoisisPlugin-exception: "+e.message);
		}
	};


	context.isCheckWords = function() {
		return checkWordsBool;
	};

	context.onChangeListenerCkEditor = function(event) {
		voisisLib.onChangeListenerCkEditor(event, voisisApp.onChangeListenerCkEditor, voisisNS.focusedEditor,
			voisisApp.isCheckWords);
		if(voisisApp.userType == 1){
			voisisLib.writeUserSettings("voisisEditor-"+voisisApp.userId, voisisApp.editorContents(voisisNS.focusedEditor));
		}
	};

	context.onChangeListenerQuill = function(delta, oldDelta, source) {

		if (source == 'user') {
			if(voisisApp.userType == 3) { // EWH: no modifiche da tastiera
				quill.history.undo();
				return;
			}
			delta.forEach(function(item){
				if(item.insert){
					var keyPressed = item.insert.slice(-1).charCodeAt(0);
					voisisLib.onKey(keyPressed, null);
					voisisApp.dictPar.userClick = false;
				}
				else if(item.delete){
					voisisLib.onCharDelete();
				}
			});
		}
		if(voisisApp.editorConfig.textLimit && (voisisApp.editorConfig.textLimit > -1)){
			if (editor.getLength() > (voisisApp.editorConfig.textLimit+1)) {
				quill.history.undo();
				delta.forEach(function(item){
					if(item.insert){
						var pos = editor.getSelection().index;
						setTimeout(() => editor.setSelection(pos), 0)
					}
				});
			}
		}

		voisisLib.onChangeListenerQuill(delta, oldDelta, source, voisisApp.onChangeListenerQuill, voisisNS.focusedEditor, voisisApp.isCheckWords);
		if(voisisApp.userType == 1){
			voisisLib.writeUserSettings("voisisEditor-"+voisisApp.userId, voisisApp.editorContents(voisisNS.focusedEditor));
		}
	};

	context.setCheckOnOff = function(onOff){
		if(voisisApp.editorType(voisisNS.focusedEditor) == voisisNS.quill){
			voisisNS.focusedEditor.off('text-change', voisisApp.onChangeListenerQuill);
			voisisLib.setCheckOnOffQuill(voisisNS.focusedEditor, voisisApp.checkWords, onOff);
			voisisNS.focusedEditor.on('text-change', voisisApp.onChangeListenerQuill);
		}
		else{
			voisisNS.focusedEditor.removeListener('change', voisisApp.onChangeListenerCkEditor);
			voisisLib.setCheckOnOffCkEditor(voisisNS.focusedEditor, voisisApp.checkWords, voisisApp.dictPar, onOff);
			voisisNS.focusedEditor.on('change', voisisApp.onChangeListenerCkEditor);
		}
	};

	context.uttInsertion = function(text, finalUtt, confScore) {

		var pluginChangeListener = voisisApp.onChangeListenerCkEditor;
		if(voisisApp.editorType(voisisNS.focusedEditor) == voisisNS.quill)
			pluginChangeListener = voisisApp.onChangeListenerQuill;
		voisisLib.uttInsertion(voisisNS.focusedEditor, pluginChangeListener, voisisApp.editorConfig,
			voisisApp.dictPar, voisisApp.dictationOn, voisisApp.dictationOff,
			voisisApp.onRecognition, voisisApp.checkWords, text, finalUtt, voisisApp.notifyCommand);

		voisisLib.alignInfo(voisisApp.textAlign);

		if(voisisApp.userType == 1)
			voisisLib.writeUserSettings("voisisEditor-"+voisisApp.userId, voisisApp.editorContents(voisisNS.focusedEditor));

		if(voisisApp.hybridApp){
			if(voisisApp.dictPar.transcribing){
				if(text.toLowerCase().includes("<voisis>start-dictation</voisis>"))
					return;
				var message;
				text = text.replace(/\<voisis\>.*\<\/voisis\>/ig, "");
				if(text.trim() != ""){
					if(finalUtt)
						message = JSON.stringify({ type: "utt-final", content: text })
					else
						message = JSON.stringify({ type: "utt-temp", content: text })
					window.chrome.webview.postMessage(message);
				}
			}
			else if (voisisApp.dictToCmd){
				var n = text.toLowerCase().indexOf("<voisis>stop-dictation</voisis>");
				if(n >= 0)
					text = text.slice(0, n).trimEnd();
				if(finalUtt)
					message = JSON.stringify({ type: "utt-final", content: text })
				else
					message = JSON.stringify({ type: "utt-temp", content: text })
				window.chrome.webview.postMessage(message);
				voisisApp.dictToCmd = false;
			}
		}
		if(finalUtt){
			if(text.toLowerCase().includes("<voisis>send-text</voisis>"))
				voisisApp.showServerResponse("Invia/incolla referto", "info");
		}

	};

	context.showButton = function(element){
		var res = true;
		if(voisisApp.showUppButt === 'no')
			res = false;
		else if(voisisApp.showUppButt[element] === 'no')
			res = false;
		return res;
	};

	context.insertPageHtml = function(editor, containerElId){

		if((voisisApp.ajaxGif == 'yes') || (voisisApp.ajaxGif == 'transition')){
			if (!( $( ".voisis-modal" ).length )){
				$("body").append("<div class='voisis-modal'>");
			}
			if(voisisApp.ajaxGif == 'transition')
				$(".voisis-modal").hide();
		}

		if (voisisApp.userDictOff == 0) {
			$('#voisis-gain-control').slider({disabled: true});
		}

		// Pulsanti
		$('#voisis-button-dict').click(function () {
			if(voisisApp.dictPar.transcribing){
				voisisApp.dictationOff();
			}
			else{
				voisisApp.dictationOn(editor);
			}

			return false;
		});

		var slider = document.getElementById('voisis-gain-control');
		var value = document.getElementById('voisis-gain-value');
		if(slider && value){
			slider.oninput = function() {
				value.innerHTML = parseFloat(this.value).toFixed(1);
			}
		}
	};

	context.startDict = function(editor){
		voisisNS.focusedEditor = editor;

		voisisApp.toggleDictButton("on");
		if(voisisLib.showModal("start-dict")){
			$(".voisis-modal").show();
		}
		voisisLib.startListening(editor, voisisApp.editorType(editor));
		voisisApp.dictPar.transcribing = true;
		if(voisisLib.showModal("start-dict")){
			setTimeout(function() {
				voisisApp.setEditorBackground(editor, '#fcf881');
				$(".voisis-modal").hide();
			}, 3000);
		}
		else{
			voisisApp.setEditorBackground(editor, '#fcf881');
		}

		if(voisisApp.hybridApp){
			var message = JSON.stringify({ type: "status", content: "dictation" });
			window.chrome.webview.postMessage(message);
		}
	}

	context.dictationOn = function(editor){
		if(!voisisLib.config)
			return;
		context.startDict(editor);
		$('#voisis-gain-control').slider({ disabled: false });
	};

	context.dictationOff = function(){

		var editor = voisisNS.focusedEditor;

		if(!voisisLib.config)
			return;

		if(voisisLib.showModal("stop-dict")){
			$(".voisis-modal").show();
			if(voisisApp.hybridApp){
				var message = JSON.stringify({ type: "mic", content: "disable-butt" });
				window.chrome.webview.postMessage(message);
			}
		}

		voisisLib.flushText(editor, "stop");

		voisisApp.setEditorBackground(editor, 'white');
		voisisApp.toggleDictButton("off");
		voisisLib.stopListening(voisisApp.hybridApp);
		voisisApp.dictPar.transcribing = false;

		if(voisisLib.showModal("stop-dict")){
			stopStartTimer = setTimeout(stopStartTrans, 4000);
		}

		if(voisisApp.userDictOff == 0){
			$('#voisis-gain-control').slider({ disabled: true });
		}

		if(voisisApp.hybridApp){
			let status = "mute";
			if (voisisApp.userDictOff != 0)
				status = "commands";
			var message = JSON.stringify({ type: "status", content: status });
			window.chrome.webview.postMessage(message);
			voisisApp.dictToCmd = true;
		}
	};

	context.notifyCommand = function(command){
		if(voisisApp.hybridApp){
			if(command.toLowerCase().includes("new-text") || command.toLowerCase().includes("send-text")){
				if(voisisApp.userType != 1) // Solo in modalità browser
					return;
			}
			var message = JSON.stringify({ type: "cmd", content: command });
			window.chrome.webview.postMessage(message);
		}
	};

	context.toggleDictButton = function(stat){
		$('#voisis-button-dict').attr("class", "voisis-editor-button "+stat);
		$('#voisis-button-dict-img').attr("class", "voisis-editor-button-img "+stat);
		if(stat === "on"){
			$('#voisis-button-dict').attr("title", "disattiva trascrizione");
		}
		else{
			$('#voisis-button-dict').attr("title", "attiva trascrizione");
		}
	};

	context.showError = function(code, data) {
		if(voisisApp.dictPar && voisisApp.dictPar.transcribing){
			voisisLib.errorSound("VoisisPlugin: ERRORE SESSIONE AUDIO");
			if(data == "UserOverlap")
				error = "L'utente ha aperto un'altra sessione";
			else if(data == "ServerBusy")
				error = "Nessun canale disponibile";
			else if(data == "UnknownGroup")
				error = "Il gruppo dell'utente non è stato identificato";
			else if(data == "NoChannels")
				error = "Nessun canale allocato per il gruppo dell'utente";
			else
				error = "Canale disconnesso";
			voisisApp.showServerResponse(error, "error");
			voisisApp.dictationOff();
		}
		if(voisisApp.onServerError){
			if(code == "ServerError"){
				voisisApp.onServerError(data);
			}
			else if(code == "OverlapError"){
				voisisApp.onServerError("L'utente ha aperto un'altra sessione");
			}
		}
	};

	context.showServerResponse = function(response, severity){
		const messagesWdg = PF('messagesVar');
		if (messagesWdg) {
			messagesWdg.show([{ summary: response, severity: severity }]);
		}
	};

	context.serverEvent = function(event, msg) {
		if(event=='start'){
			if(voisisApp.onStartDict)
				voisisApp.onStartDict();
		}
		else if(event=='stop'){
			if(voisisLib.showModal("stop-dict")){
				$(".voisis-modal").hide();
			}
			if(voisisApp.onStopDict)
				voisisApp.onStopDict();
		}
	};

	context.micPressAction = function(data){
		if(data == "Rec"){
			voisisLib.pluginLogger(voisisNS.pluginLog.EVENTS, "VoisisPlugin-Mic REC pressed");
			if(voisisApp.isEditorEnabled(voisisNS.focusedEditor)){
				if($('#voisis-button-dict').length != 0){
					$('#voisis-button-dict').click();
				}
				else if(voisisNS.focusedEditor !== null){
					if(voisisApp.dictPar.transcribing){
						voisisLib.flushText(voisisNS.focusedEditor, "stop");
						voisisApp.setEditorBackground(voisisNS.focusedEditor, 'white');
						voisisLib.stopListening(voisisApp.hybridApp);
						voisisApp.dictPar.transcribing = false;
					}
					else{
						voisisLib.startListening(voisisNS.focusedEditor, voisisApp.editorType(voisisNS.focusedEditor));
						voisisApp.dictPar.transcribing = true;
						voisisApp.setEditorBackground(voisisNS.focusedEditor, '#fcf881');
					}
				}
			}
		}
		else if((data == "Disconnect") && voisisApp.dictPar.transcribing){
			voisisLib.pluginLogger(voisisNS.pluginLog.EVENTS, "VoisisPlugin-Mic Disconnected");
			voisisLib.errorSound("VoisisPlugin: MICROFONO DISCONNESSO");
			voisisApp.showServerResponse("Microfono disconnesso", "error");
			if($('#voisis-button-dict').length != 0){
				$('#voisis-button-dict').click();
			}
			else{
				voisisLib.flushText(voisisNS.focusedEditor, "stop");
				voisisApp.setEditorBackground(voisisNS.focusedEditor, 'white');
				voisisLib.stopListening(voisisApp.hybridApp);
				voisisApp.dictPar.transcribing = false;
			}
		}
	};

	context.completedAction = function(data){
		$('#voisis-button-dict').prop("disabled", false);
		if(voisisLib.showModal("completed")){
			$(".voisis-modal").hide();
		}
	};

	context.configEditor = function(editor, containerElId){
		voisisLib.pluginLogger(voisisNS.pluginLog.INIT, "VoisisPlugin-ConfigEditor "+containerElId);
		if(voisisApp.editorType(editor) == voisisNS.quill){

			editor.off('text-change', voisisApp.onChangeListenerQuill);
			editor.on('text-change', voisisApp.onChangeListenerQuill);

			voisisLib.pluginLogger(voisisNS.pluginLog.INIT, "VoisisPlugin-Quill sel-change "+containerElId);
			editor.on('selection-change', function(range, oldRange, source) {
				if (range && (source == 'user')) {
					voisisNS.atEditor = editor;
					voisisApp.dictPar.userClick = true;
					voisisLib.alignRefresh(voisisApp.textAlign);
					voisisLib.onQSelChange(editor);
				}
			});
		}
		else{
			editor.removeListener('change', voisisApp.onChangeListenerCkEditor);
			editor.on('change', voisisApp.onChangeListenerCkEditor);

			voisisLib.initCkEditorCss(voisisApp.userFontSize, voisisApp.userFontFamily);
			// Force refresh
			editor.setData(editor.getData());
			voisisApp.setCheckOnOff(false);

			editor.on('contentDom', function(e) {
				editor.editable().on('click', function (event) {
					voisisLib.pluginLogger(voisisNS.pluginLog.EVENTS, "VoisisPlugin-on click", {cke: editor.id, op: "ckEditor onClick"});
//					event.stop();
//					event.data.preventDefault();
//					event.data.stopPropagation();
					voisisApp.dictPar.userClick = true;
					voisisLib.alignRefresh(voisisApp.textAlign);
				});
			});

			editor.on('key', function (event) {
				if(voisisApp.userType == 3){ //EWH: no modifiche da tastiera
					event.cancel();
				}
				else{
					var keyPressed = event.data['keyCode'];
					voisisLib.onKey(keyPressed, event);
					voisisApp.dictPar.userClick = false;
					if(keyPressed == CKEDITOR.ALT+49){ // ALT+1
						$('#voisis-button-dict').click();
					}
				}
			});

			editor.on("doubleclick", function (e){
				voisisLib.onDoubleClick(e);
			});
		}

		voisisLib.pluginLogger(voisisNS.pluginLog.INIT, "VoisisPlugin-Config editor completed "+containerElId, {cke: voisisApp.editorId(editor), op: "config editor completed"});
	};

	context.initUserParams = function(user)
	{
		$.ajax({
			async: false,
			type: 'GET',
			url: voisisApp.sysUrl+'/users.php?userLoginPar='+user,
			success: function(par) {
				voisisNS.userId = voisisApp.userId = user;
				voisisNS.vocabId = voisisApp.vocabId = par['vocab'];
				voisisApp.audioWizard = (par['audioWizard'] == 'true');
				voisisApp.checkWordsEnable = (par['checkWords'] == 'true');
				voisisApp.spellCheckEnable = (par['spellCheck'] == 'true');
				voisisApp.sendComment = (par['sendComment'] == 'true');
				voisisApp.changePwd = (par['changePwd'] == 'true');
				voisisApp.logLevel = parseInt(par['logLevel']);
				voisisApp.userType =  parseInt(par['userType']);
				voisisApp.userDictOff = parseInt(par['userDictOff']);
				voisisApp.selectAndCopy = (par['selectAndCopy'] == 'true');
				voisisApp.exit = (par['exit'] == 'true');
				if(par['options'])
					voisisApp.userFontSize = par['options'].replace(/(^\D+|;)/g, '');
				voisisApp.dictPar = { transcribing: false, userClick: false }; //, insertAtEnd: false};

				voisisApp.checkWords = {};
				$.get(voisisApp.sysUrl+"/vocab.php?getCheckWords&user="+voisisApp.userId, function( data ) {
					$.each(data, function(index) {
						var value = data[index];
						voisisApp.checkWords[value.word] = value.color;
					});
				});

				$.get(voisisApp.sysUrl+"/vocab.php?wordlist&vocab="+voisisApp.vocabId, function( data ) {
					voisisApp.wordlist = data.split(',');
				});
			},
			error: function(result){
				voisisApp.showServerResponse("L'utente non &egrave; correttamente configurato", "error");
			}
		});
	};

	context.cleanUp = function(e)
	{
		if(voisisApp.userId){
			var fd = new FormData();
			fd.append('op', 'unload');
			fd.append('user', voisisApp.userId);
			navigator.sendBeacon(voisisApp.sysUrl+'/users.php', fd);
		}
		if(voisisApp.dictPar && voisisApp.dictPar.transcribing){
			// Conferma dell'utente
			e.preventDefault();
			// Chrome requires returnValue to be set
			e.returnValue = '';
		}
	};

})(voisisApp);


/////////////////////////////////////////

function voisisDictation(url, editor, config){

	voisisNS.sysUrl = voisisApp.sysUrl = url;

	voisisApp.checkServerVersion();

	var _id = voisisApp.editorId(editor);
	var _containerElId;
	voisisEditorArr[_id] = editor;
	voisisNS.focusedEditor = editor;

	if (typeof voisisLib !== "undefined")
		voisisLib.pluginLogger(voisisNS.pluginLog.INIT, "VoisisPlugin-voisisDictation", { op: "voisisDictation", id: _id});

	if(config){
		if('containerElId' in config)
			_containerElId = config.containerElId;

		if('jsBaseUrl' in config)
			voisisApp.voisisWebEditorJsUrl = config.jsBaseUrl;
		if('proxyLoginUrl' in config)
			voisisApp.proxyLoginUrl = config.proxyLoginUrl;
		if('showUppButt' in config)
			voisisApp.showUppButt = config.showUppButt;
		if('dialogSize' in config){
			if('imgPath' in config.dialogSize){
				voisisNS.dialogImgPath = config.dialogSize.imgPath;
			}
		}
		if('autoCleanUp' in config)
			voisisApp.autoCleanUp = config.autoCleanUp;
		if('autotextReplace' in config)
			voisisApp.autotextReplace = config.autotextReplace;
		if('ajaxGif' in config)
			voisisApp.ajaxGif = config.ajaxGif;
		else
			voisisApp.ajaxGif = 'yes';
		if('onInit' in config)
			voisisApp.onInit[_id] = config.onInit;
		if('onConnect' in config)
			voisisApp.onConnect = config.onConnect;
		if('onConnectFailed' in config)
			voisisApp.onConnectFailed = config.onConnectFailed;
		if('onDisconnect' in config)
			voisisApp.onDisconnect = config.onDisconnect;
		if('onStartDict' in config)
			voisisApp.onStartDict = config.onStartDict;
		if('onStopDict' in config)
			voisisApp.onStopDict = config.onStopDict;
		if('onRecognition' in config)
			voisisApp.onRecognition = config.onRecognition;
		if('onCommandRecognition' in config)
			voisisApp.onCommandRecognition = config.onCommandRecognition;
		if('onServerError' in config)
			voisisApp.onServerError = config.onServerError;
		if('useCookie' in config)
			voisisApp.useCookie = config.useCookie;
		if('editorConfig' in config){
			var insertMode = ["html", "unfiltered_html", "text"];
			var newLine = ["p", ""];
			var phraseBoundary = ["span", ""];
			if((config.editorConfig.insertMode && (insertMode.indexOf(config.editorConfig.insertMode) == -1)) ||
				(config.editorConfig.newLine && (newLine.indexOf(config.editorConfig.newLine) == -1)) ||
				(config.editorConfig.phraseBoundary && (phraseBoundary.indexOf(config.editorConfig.phraseBoundary) == -1)) ||
				(config.editorConfig.textLimit && (!Number.isInteger(config.editorConfig.textLimit)))){
				voisisLib.pluginLogger(voisisNS.pluginLog.ERROR, "VoisisPlugin-editorConfig wrong par");
			}
			else{
				voisisApp.editorConfig = config.editorConfig;
			}
		}
	}

	// Settaggi sistema
	$.ajax({
		type: 'GET',
		url: voisisApp.sysUrl+'/system.php?params',
		success: function(par) {
			voisisNS.asrTech = voisisApp.asrTech = par['asrTech'];
			voisisApp.asrUrl = par['asrUrl'];
			voisisApp.signUrl = par['signUrl'];
			voisisNS.initParCompleted = true;
			if(voisisApp.editorType(editor) == voisisNS.quill){
				if(voisisApp.onInit && voisisApp.onInit[_id])
					(voisisApp.onInit[_id])();
			}
			else{
				for (var key in voisisNS.initEditorCompleted) {
					if(key){
						if(voisisNS.initEditorCompleted[key] && voisisApp.onInit)
							(voisisApp.onInit[key])();
					}
				}
			}
		},
		error: function(result){
			if (typeof voisisLib !== "undefined")
				voisisLib.pluginLogger(voisisNS.pluginLog.ERROR, "VoisisPlugin-sysParams: "+result);
		}
	});

	// Funzioni
	function connectUser(editor, url, user, groupId){
		return voisisLib.checkUser(url, user)
			.then(function(res) {
				if(res){
					return loginUserThroughProxy(voisisApp.proxyLoginUrl, groupId)
						.fail(function (req) {
							if (req.status === 404 && req.responseJSON) {
								voisisApp.showServerResponse(req.responseJSON.msg, 'error');
							}
						});
				}
				else{
					if(!voisisConnectFailed[voisisApp.editorId(editor)]){
						if(voisisApp.onConnectFailed)
							voisisApp.onConnectFailed();
						else
							voisisApp.showServerResponse("L'utente non &egrave; correttamente configurato", "error");
						voisisConnectFailed[voisisApp.editorId(editor)] = true;
					}
				}
			})
			.then(function(res) {
				var defer = $.Deferred();
				if(res){
					voisisApp.initUserParams(user);
					voisisUserConnected[voisisApp.editorId(editor)] = true;

					editor.on('dataReady', function (evt) {
						const zoomUI = editor.ui.instances['Zoom'];
						if (zoomUI) {
							zoomUI.onClick(zoomUI.findCookie('profile_zoom_value'));
						}
					});

					if((voisisApp.userType == 1) && voisisLib.readUserSettings("voisisEditor-"+voisisApp.userId)){
						voisisApp.setEditorText(editor, voisisLib.readUserSettings("voisisEditor-"+voisisApp.userId));
					}

					if(voisisNS.browserPar){
						const parString = voisisNS.browserPar.join("\n");
						$.ajax({
							url: voisisApp.sysUrl+'/users.php',
							type: 'POST',
							data: {
								op: "logClientPar",
								user: user,
								userAgent: voisisNS.userAgent,
								browserPar: parString
							},
							cache: false,
							success:function (answer) {
							},
							error: function(result){
							}
						});
					}
					return defer.resolve(res);
				}
				else{
					if(!voisisConnectFailed[voisisApp.editorId(editor)]){
						if(voisisApp.onConnectFailed)
							voisisApp.onConnectFailed();
						else
							voisisApp.showServerResponse("User e/o password errate", "error");
						voisisConnectFailed[voisisApp.editorId(editor)] = true;
					}
					return defer.reject();
				}
			});
	}

	function loginUserThroughProxy(url, groupId){
		var defer = $.Deferred();
		var res = false;
		$.ajax({
			url: url,
			type: 'POST',
			data: {
				groupId: groupId,
				integration: 'plugin'
			},
			success:function (answer) {
				if(answer['res'] === 'ok'){
					res = true;
				}
				defer.resolve(res);
			},
			error: function(req) {
				defer.reject(req);
			}
		});
		return defer.promise();
	}

	// API //
	this.connect = function(user, groupId=0){

		if(voisisUserConnected[_id])
			return;

		voisisConnectFailed[_id] = false;

		var editor = voisisEditorArr[_id];
		var connectPromise = connectUser(editor, voisisApp.sysUrl, user, groupId);
		connectPromise.then(function() {

			var tokenPromise = voisisLib.reserveChannel(voisisApp.sysUrl, user);
			tokenPromise.done(function(token) {
				if(token){
					if(voisisApp.asrTech == 'k'){
						voisisLib.init({
							sysUrl : voisisApp.sysUrl,
							sysAuUrl: voisisApp.asrUrl,
							sysCmdUrl: voisisApp.signUrl,
							asrTech : voisisApp.asrTech,
							userId: user,
							vocabId: voisisApp.vocabId,
							clientId: token,
							groupId: groupId,
							userType: voisisApp.userType,
							userDictOff: voisisApp.userDictOff,
							editorType: voisisApp.editorType(editor),
							audioFormat: "ogg",
							encoderPath: voisisApp.voisisWebEditorJsUrl,
							uttInsertion: voisisApp.uttInsertion,
							messageNotif: voisisApp.serverEvent,
							errorNotif: voisisApp.showError,
							micPress: voisisApp.micPressAction,
							completed: voisisApp.completedAction,
							dictTimeout: function(data){
								if(voisisApp.dictPar.transcribing){
									voisisLib.errorSound("VoisisPlugin: SESSIONE AUDIO SCADUTA");
									voisisApp.showServerResponse("Canale disconnesso", "error");
									voisisApp.dictationOff();
								}
								else{
									clearTimeout(stopStartTimer);
									stopStartTrans();
								}
							},
							logLevel: voisisApp.logLevel,
							tabNext: voisisApp.editorConfig['tabNext'],
							onCommand: voisisApp.onCommandRecognition
						});
					}
					else{
						voisisLib.pluginLogger(voisisNS.pluginLog.ERROR, "VoisisPlugin-API: unsupported asr tech. "+voisisApp.asrTech);
					}

					voisisLib.pluginLogger(voisisNS.pluginLog.API, "VoisisPlugin-API: user connected "+_id, { cke: _id, op: "connected", user: user}, true);

					// HTML
					voisisLib.pluginLogger(voisisNS.pluginLog.API, "VoisisPlugin-API: insertPageHtml "+_id);
					voisisApp.insertPageHtml(editor, _containerElId);
					voisisApp.configEditor(editor, _containerElId);
					if($("#voisis-upper-buttons"))
						$('#voisis-upper-buttons').show();

					if(voisisApp.editorType(voisisEditorArr[_id]) == voisisNS.ckEditor){
						if(CKEDITOR.plugins.voisis)
							CKEDITOR.plugins.voisis.buildMenu(voisisEditorArr[_id], voisisApp.showButton('vocab-lex'));
					}

					if(voisisApp.onConnect){
						voisisApp.onConnect();
						voisisLib.pluginLogger(voisisNS.pluginLog.API, "VoisisPlugin-API: connected callback "+_id, { cke: _id, op: "connected callback", user: user}, true);
					}

					voisisLib.initListening(voisisApp.sysUrl, user, voisisEditorArr[_id], voisisApp.editorType(voisisEditorArr[_id]));

					voisisLib.setAutotextReplace(user, voisisApp.autotextReplace);

					if ((volGain = voisisLib.readUserSettings("voisisVolGain-" + user)) && !isNaN(volGain)) {
						$("#voisis-gain-value").html(volGain);
						$("#voisis-gain-control").val(volGain);
					}
					$("#voisis-gain-control").on('change', function () {
						voisisLib.writeUserSettings("voisisVolGain-" + user, $("#voisis-gain-value").html());
					});

					if(window.chrome && window.chrome.webview){
						var commands = "off";
						if(voisisApp.userDictOff != 0)
							commands = "on";
						var message = JSON.stringify({ type: "status", content: "login", user: user, usertype: voisisApp.userType, commands: commands });
						window.chrome.webview.postMessage(message);
					}

				}
				else{
					if(!voisisConnectFailed[_id]){
						if(voisisApp.onConnectFailed)
							voisisApp.onConnectFailed();
						voisisConnectFailed[_id] = true;
					}
				}
			});
		});
	};

	this.isUserConnected = function(user){
		if (typeof voisisLib !== "undefined")
			voisisLib.pluginLogger(voisisNS.pluginLog.API, "VoisisPlugin-API", { cke: _id, op: "isUserConnected", user: user});

		var defer = $.Deferred();
		$.ajax({
			url: voisisApp.sysUrl+"/users.php?userConn="+user,
			success: function(answer) {
				defer.resolve(answer.res);
			},
			error: function(req, status, err) {
				defer.reject(err);
			}
		});
		return defer.promise();
	};

	this.disconnect = function(user){
		voisisLib.pluginLogger(voisisNS.pluginLog.API, "VoisisPlugin-API", { op: "disconnect", user: user});

		if(!user)
			user = voisisApp.userId;
		$.ajax({
			url: voisisApp.sysUrl+'/users.php',
			type: 'POST',
			data: {
				op: "disconnect",
				user: user
			},
			success:function (answer) {
				voisisUserConnected[_id] = false;
				voisisApp.userId = '';
				voisisApp.vocabId = '';
				if(voisisApp.onDisconnect)
					voisisApp.onDisconnect();
				else
					window.location.reload();
			}
		});
	};

	this.disconnectSync = function(){
		voisisLib.pluginLogger(voisisNS.pluginLog.API, "VoisisPlugin-API", { cke: _id, op: "disconnectSync", user: voisisApp.userId});

		var defer = $.Deferred();
		$.ajax({
			url: voisisApp.sysUrl+'/users.php',
			type: 'POST',
			data: {
				op: "disconnect",
				user: voisisApp.userId
			},
			success:function (answer) {
				voisisLib.pluginLogger(voisisNS.pluginLog.API, "VoisisPlugin-API", { cke: _id, op: "disconnectSync completed", user: voisisApp.userId}, true);

				$('#voisis-upper-buttons').hide();
				voisisUserConnected[_id] = false;
				voisisApp.userId = '';
				voisisNS.userId = '';
				voisisApp.vocabId = '';
				defer.resolve();
			},
			error: function(req, status, err) {
				defer.reject(err);
			}
		});
		return defer.promise();
	};
	this.toHtmlEntities = function(str) {
		return str.replace(/./gm, function(s) {
			// return "&#" + s.charCodeAt(0) + ";";
			return (s.match(/[a-z0-9\s]+/i)) ? s : "&#" + s.charCodeAt(0) + ";";
		});
	};
	this.getText = function(){
		var editor = voisisEditorArr[_id];
		if (typeof voisisLib !== "undefined")
			voisisLib.flushText(editor, "getText");

		if(voisisApp.editorType(editor) == voisisNS.quill){
			text = editor.root.innerHTML;
			if(text.trim() == "<p><br></p>")
				text = "";
			else {
				text = text.replace(/à/g, "&agrave;").replace(/è/g, "&egrave;").replace(/ì/g, "&igrave;").replace(/ò/g, "&ograve;").replace(/ù/g, "&ugrave;").replace(/é/g, "&eacute;");
				text = text.replace(/[\u00A0-\u00FF\u2022-\u2135]/g, function(c) {
					return '&#'+c.charCodeAt(0)+';';
				});
			}
		}
		else{
			var text = voisisApp.editorContents(editor);
			text = text.replace("<span class=\"tempUtt\" style=\"color:grey;\">", "<span>");
			if(voisisApp.editorConfig['newline'] == "")
				text = text.replace(/\n\n/g, "\n");
			else if(voisisApp.editorConfig['newline'] == "p-br"){
				text = text.replace(/<p>/g, "<br>");
				text = text.replace(/<\/p>/g, "");
			}
			else if(voisisApp.editorConfig['newline'] == "p"){
				text = text.replace(/\<br\s*\/\>\n/g, "<br>");
			}

			// Fix spazio<br>
			text = text.replace(/\&puncsp\;\<br\>/g, " <br>");
			text = text.replace(/\s?style="font-size:\s?\d+[a-z]+;"/g, ""); // font ckeditor
		}

		// Sostituzione altri caratteri
		text = text.replace(/\&nbsp;/g, " ");	// non breaking space
		text = text.replace(/\&#8203;/g, "");	// zero width space

		if (typeof voisisLib !== "undefined")
			voisisLib.pluginLogger(voisisNS.pluginLog.API, "VoisisPlugin-API", { cke: _id, op: "getText", text: text});

		return text;
	};

	this.getPlainText = function(){
		var editor = voisisEditorArr[_id];
		var html = voisisApp.editorContents(editor);
		if(voisisApp.editorType(editor) == voisisNS.quill){
			text = html;
			// Sostituzione altri caratteri
			text = text.replace(/\&nbsp;/g, " ");	// non breaking space
			text = text.replace(/\&#8203;/g, "");	// zero width space
			if(text == "\n")
				text = "";
		}
		else{
			var dom1 = document.createElement("div");
			dom1.innerHTML = html.replace(/\&nbsp;/g, " ").replace(/<br>/g, "\r\n").replace(/\&#8203;/g, "");
			if(voisisApp.editorConfig['newline'] == "")
				dom1.innerHTML = dom1.innerHTML.replace(/\n\n/g, "\n");
			var text = dom1.textContent || dom1.innerText;
			// Fix spazi<br>
			text = text.replace(/[\u2008]/g, " ");
		}

		voisisLib.pluginLogger(voisisNS.pluginLog.API, "VoisisPlugin-API", { cke: _id, op: "getPlainText", text: text});

		return text;
	};

	this.setText = function(text, callback){
		if (typeof voisisLib !== "undefined")
			voisisLib.pluginLogger(voisisNS.pluginLog.API, "VoisisPlugin-API", { cke: _id, op: "setText", text: text});

		var editor = voisisEditorArr[_id];

		if(voisisApp.editorType(editor) == voisisNS.quill){
			const delta = editor.clipboard.convert(text)
			editor.setContents(delta, 'silent')
			voisisApp.moveCaretToEnd(editor);
		}
		else {
			var fooCallback = function(){
				voisisApp.moveCaretToEnd(editor);

				if(voisisApp.dictPar && voisisApp.dictPar.transcribing){
					editor.document.getBody().setStyle('background-color', '#fcf881');
				}

				if(callback !== undefined){
					callback();
					voisisLib.pluginLogger(voisisNS.pluginLog.EVENTS, "VoisisPlugin-SetData: completed");
				}
				voisisLib.pluginLogger(voisisNS.pluginLog.API, "VoisisPlugin-API", { cke: _id, op: "setText completed", text: text}, true);
			}

			// Fix spazi<br>
			text = text.replace(/(\s+)\<br\>/g, function(match, capture) {
				return "&puncsp;".repeat(capture.length)+"<br>";
			});
			// Fix spazi multipli
			text = text.replace(/([ ]{2,})/g, function(match, capture) {
				return "&puncsp;".repeat(capture.length);
			});

			editor.setData(text, fooCallback);
		}

		voisisLib.setTextDone(true);
	};

	this.insertText = function(text){
		if (typeof voisisLib !== "undefined")
			voisisLib.pluginLogger(voisisNS.pluginLog.API, "VoisisPlugin-API", { cke: _id, op: "insertText", text: "<span>"+text+"</span>"}, true);

		var editor = voisisEditorArr[_id];
		if(voisisApp.editorType(editor) == voisisNS.quill){
			var cursor = 0;
			var selLen = 0;
			var sel = editor.getSelection(true);
			if(sel){
				cursor = sel.index;
				selLen = sel.length;
			}
			var Delta = Quill.import('delta');
			const delta = editor.clipboard.convert(text);
			editor.clipboard.dangerouslyPasteHTML(cursor, text);
		}
		else{
			editor.insertHtml("<span>"+text+"</span>");
		}
	};

	this.startDictation = function(){
		voisisLib.pluginLogger(voisisNS.pluginLog.API, "VoisisPlugin-API", { cke: _id, op: "startDictation"}, true);

		var editor = voisisEditorArr[_id];
		voisisApp.dictationOn(editor);
	};

	this.stopDictation = function(){
		voisisLib.pluginLogger(voisisNS.pluginLog.API, "VoisisPlugin-API", { cke: _id, op: "stopDictation"});

		voisisApp.dictationOff();
	};

	this.disableEditor = function(){
		if (typeof voisisLib !== "undefined")
			voisisLib.pluginLogger(voisisNS.pluginLog.API, "VoisisPlugin-API", { cke: _id, op: "disableEditor"});

		var editor = voisisEditorArr[_id];
		$('#voisis-button-dict').prop("disabled", true);

		voisisApp.setUnsetRO(editor, false);
		voisisApp.safeSetEditorPar('background-color', '#eae8e8', 1);
	};

	this.enableEditor = function(){
		if (typeof voisisLib !== "undefined")
			voisisLib.pluginLogger(voisisNS.pluginLog.API, "VoisisPlugin-API", { cke: _id, op: "enableEditor"});

		var editor = voisisEditorArr[_id];
		voisisApp.setUnsetRO(editor, true);

		$('#voisis-button-dict').prop("disabled", false);

		if(voisisApp.dictPar && voisisApp.dictPar.transcribing)
		{
			voisisApp.safeSetEditorPar('background-color', '#fcf881', 1);
		}
		else{
			voisisApp.safeSetEditorPar('background-color', 'white', 1);
		}
	};

	this.focusEditor = function(){
		if (typeof voisisLib !== "undefined")
			voisisLib.pluginLogger(voisisNS.pluginLog.API, "VoisisPlugin-API: ", { cke: _id, op: "focus editor"});

		var editor = voisisEditorArr[_id];
		editor.focus();
		voisisApp.moveCaretToEnd(editor);
		voisisNS.focusedEditor = editor;
		voisisNS.atEditor = editor;
	};

	this.setEditorHeight = function(height){
		if (typeof voisisLib !== "undefined")
			voisisLib.pluginLogger(voisisNS.pluginLog.API, "VoisisPlugin-API: ", { cke: _id, op: "setEditorHeight", height: height});

		var editor = voisisEditorArr[_id];
		const number = height.replace(/\D/g, '');
		const unit = height.replace(/\d/g, '');
		if((number > 0) && editor){
			// Bisogna trasformare tutto in pixels
			// https://github.com/ckeditor/ckeditor-dev/issues/1883
			var pxHeight = 600; // Default
			if(unit == '%'){
				const screenHeight = window.innerHeight;
				pxHeight = (number*screenHeight)/100;
			}
			else if(unit == 'px'){
				pxHeight = number;
			}
			else if(unit == 'pt'){
				pxHeight = number*4/3;
			}
			else if(unit == 'em'){
				pxHeight = number/16;
			}
			// Header
			outH = 60;
			if($('#voisis-component').outerHeight())
				outH = $('#voisis-component').outerHeight();
			pxHeight = pxHeight - outH;
			if(voisisApp.editorType(editor) == voisisNS.ckEditor){
				pxHeight = pxHeight - $('.cke_top').outerHeight();
			}
			if(pxHeight > 0){
				try{
					voisisApp.setEditorHeight(editor, pxHeight);
				} catch(e){
					if (typeof voisisLib !== "undefined")
						voisisLib.pluginLogger(voisisNS.pluginLog.ERROR, "VoisisPlugin-exception: "+e.message);
				}
			}
		}
	};

	this.setEditorSize = function(height, width){
		if (typeof voisisLib !== "undefined")
			voisisLib.pluginLogger(voisisNS.pluginLog.API, "VoisisPlugin-API: ", { cke: _id, op: "setEditorsize", height: height, width: width});

		var editor = voisisEditorArr[_id];
		const numberH = height.replace(/\D/g, '');
		const unitH = height.replace(/\d/g, '');
		const numberW = width.replace(/\D/g, '');
		const unitW = width.replace(/\d/g, '');
		if((numberH > 0) && (numberW > 0) && editor){
			// Bisogna trasformare tutto in pixels
			// https://github.com/ckeditor/ckeditor-dev/issues/1883
			var pxHeight = 600; // Default
			if(unitH == '%'){
				const screenHeight = window.innerHeight;
				pxHeight = (numberH*screenHeight)/100;
			}
			else if(unitH == 'px'){
				pxHeight = numberH*1;
			}
			else if(unitH == 'pt'){
				pxHeight = numberH*4/3;
			}
			else if(unitH == 'em'){
				pxHeight = numberH/16;
			}

			var pxWidth = '100%';
			if(unitW == '%'){
				const screenWidth = window.innerWidth;
				pxWidth = (numberW*screenWidth)/100;
			}
			else if(unitW == 'px'){
				pxWidth = numberW*1;
			}
			else if(unitW == 'pt'){
				pxWidth = numberW*4/3;
			}
			else if(unitW == 'em'){
				pxWidth = numberW/16;
			}

			// Header
			var outH = 0;
			if($('#voisis-component').outerHeight())
				outH = $('#voisis-component').outerHeight();
			pxHeight = pxHeight - outH;
			if(voisisApp.editorType(editor) == voisisNS.ckEditor){
				pxHeight = pxHeight - $('.cke_top').outerHeight();
			}
			if((pxHeight > 0) && (pxWidth > 0)){
				try{
					voisisApp.setEditorSize(editor, pxWidth, pxHeight);
				} catch(e){
					if (typeof voisisLib !== "undefined")
						voisisLib.pluginLogger(voisisNS.pluginLog.ERROR, "VoisisPlugin-exception: "+e.message);
				}
			}
		}
	};

	// font = family,size
	this.setFont = function(font, callback){
		if (typeof voisisLib !== "undefined")
			voisisLib.pluginLogger(voisisNS.pluginLog.API, "VoisisPlugin-API: ", { cke: _id, op: "setFont", font: font});

		var editor = voisisEditorArr[_id];
		var fontPar = font.split(',');
		if(fontPar.length == 2){
			var fontFamily = fontPar[0];
			var fontSize = fontPar[1];
			if(fontFamily !== null) {
				voisisApp.userFontFamily = fontFamily;
				voisisApp.userFontSize = fontSize;
				if(voisisApp.editorType(editor) == voisisNS.quill){
					var id = voisisApp.editorId(editor);
					$('#'+id).css('font-size', fontSize);
					$('#'+id).css('font-family', fontFamily);
					if (typeof voisisLib !== "undefined")
						voisisLib.pluginLogger(voisisNS.pluginLog.API, "VoisisPlugin-API", { cke: _id, op: "setFont completed"}, true);
				}
				else{
					voisisLib.initCkEditorCss(voisisApp.userFontSize, voisisApp.userFontFamily);
					var fooCallback = function(){
						if(callback !== undefined){
							callback();
						}
						if (typeof voisisLib !== "undefined")
							voisisLib.pluginLogger(voisisNS.pluginLog.API, "VoisisPlugin-API", { cke: _id, op: "setFont completed"}, true);
					}
					editor.setData(editor.getData(), fooCallback);
				}
			}
		}
	};

	this.getAvailCh = function(){
		voisisLib.pluginLogger(voisisNS.pluginLog.API, "VoisisPlugin-API: ", { cke: _id, op: "getAvailCh"});

		var defer = $.Deferred();
		$.ajax({
			url: voisisApp.sysUrl+'/system.php?availCh',
			success: function(answer) {
				defer.resolve(answer.availCh);
			},
			error: function(req, status, err) {
				defer.reject(err);
			}
		});
		return defer.promise();
	};

	this.cleanUp = function(e){
		voisisApp.cleanUp(e);
	};

	this.commandsEnabled = function(){
		if(voisisApp.userDictOff != 0)
			return true;
		return false;
	};

	this.setHybridApp = function(){
		voisisApp.hybridApp = true;
	};

};

/////////////////////////////////

$(document).ready(function () {

	if(voisisApp.voisisWebEditorJsUrl === undefined)
		voisisApp.voisisWebEditorJsUrl = window.location.href.substring(0, window.location.href.lastIndexOf('/'))+'/js';
	if(voisisApp.showUppButt === undefined)
		voisisApp.showUppButt = 'yes';
	if(voisisApp.autoCleanUp === undefined)
		voisisApp.autoCleanUp = 'yes';
	if(voisisApp.autotextReplace === undefined)
		voisisApp.autotextReplace = 'yes';
	if(voisisApp.ajaxGif === undefined)
		voisisApp.ajaxGif = 'yes';
	if(voisisApp.useCookie === undefined)
		voisisApp.useCookie = 'no';
	if(voisisApp.editorConfig === undefined)
		voisisApp.editorConfig = {"newline": "p", "phraseBoundary": "span", "insertMode": "unfiltered_html"};
	if(voisisApp.onInit === undefined)
		voisisApp.onInit = [];

	var scriptTag = document.getElementById('voisis-dictation');
	if(scriptTag){
		var brPar = scriptTag.getAttribute("data-brpar");
		if(brPar == "yes")
			voisisApp.getBrowserPar();
	}
	else{
		voisisApp.getBrowserPar();
	}

	if(typeof(CKEDITOR) !== 'undefined'){
		CKEDITOR.on('instanceReady', function(event) {
			var _id = event.editor.id;
			if (typeof voisisLib !== "undefined")
				voisisLib.pluginLogger(voisisNS.pluginLog.INIT, "VoisisPlugin-voisisDictation", { cke: _id, op: "ckEditor instanceReady"});
			if(voisisEditorArr[_id]){
				var editor = voisisEditorArr[_id];
				voisisNS.initEditorCompleted[_id] = true;
				if (typeof voisisLib !== "undefined"){
					voisisLib.readyCkEditor(editor);
				}
				if(voisisNS.initParCompleted && voisisApp.onInit[_id])
					voisisApp.onInit[_id]();
			}
		});
	}

	window.onbeforeunload = function (e) {
		if(voisisApp.autoCleanUp == 'yes')
			voisisApp.cleanUp(e);
	};

	window.addEventListener("pagehide", event => {
		if(voisisApp.hybridApp){
			var message = JSON.stringify({ type: "window", content: "app-close" });
			window.chrome.webview.postMessage(message);
		}
	}, false);

	$(document).on({
		ajaxStart: function() { if(voisisApp.ajaxGif == 'yes')  $("body").addClass("voisis-loading"); },
		ajaxStop: function() { if(voisisApp.ajaxGif == 'yes')  $("body").removeClass("voisis-loading"); }
	});

});
