
//Voisis @ 2022

//ckEditor
var ckTextNodes = [];
var ckSelectedText;
var ckInsertUppercase = false;

//Quill
var qLastUttLen = 0;
var qLastIns = 0;
var qInsertUppercase = false;

var ButtonPressed  = "ButtonPressed";
var ButtonReleased = "ButtonReleased";
var Disconnect = "Disconnect";
var timer;
var sysPath = '';
var localAsr = false;
var localWs = false;
var audioSessionEnabled = false;

var cmd2DictTransition = false;

var voisisNS = window.voisisNS || {};
voisisNS.micDriver = null
voisisNS.complField = "_";
voisisNS.complFieldIdx = 0;
voisisNS.complFieldNum = 0;
//Old plugin compatibility
if(typeof voisisNS.ckEditor === "undefined")
	voisisNS.ckEditor = "ckeditor";

function Timer(fn, t) {
	var timerObj = setInterval(fn, t);

	this.stop = function() {
		if (timerObj) {
			clearInterval(timerObj);
			timerObj = null;
		}
		return this;
	};

	// start timer using current settings (if it's not already running)
	this.start = function() {
		if (!timerObj) {
			this.stop();
			timerObj = setInterval(fn, t);
		}
		return this;
	};

	// start with new interval, stop current interval
	this.reset = function(newT) {
		t = newT;
		return this.stop().start();
	};
}

function html2text(html){
	var tag = document.createElement('div');
	tag.innerHTML = html;
	return tag.innerText;
}

function cleanTmpUtt(text, newline){

	text = text.replace(/\+punto\+e\+virgola\+/g, ";")
	.replace(/\+punto\+di+\domanda\+/g, "?")
	.replace(/\+punto\+/g, ".")
	.replace(/\+virgola\+/g, ",")
	.replace(/\+a\+capo\+/g, newline)
	.replace(/\+due\+punti\+/g, ":")
	.replace(/\+aperta\+parentesi\+/g, "(")
	.replace(/\+chiusa\+parentesi\+/g, ")")
	.replace(/\+aperte\+virgolette\+/g, "\"")
	.replace(/\+chiuse\+virgolette\+/g, "\"")
	.replace(/\-\s+/g, "-")
	.replace(/\+/g, "")
	.replace(/_/g, " ");

	var leftUppercase = [".", "!", "?", newline];
	var words = text.split(" ");
	for (i = 0; i < words.length; i++) {
		if((i > 0) && (leftUppercase.indexOf(words[i-1]) != -1)){
			words[i] = words[i].charAt(0).toUpperCase() + words[i].slice(1);
		}
	}
	text = words.join(' ');

	text = text.replace(/\s+(\.|\,|\:|\;|\!|\?|\)|\-|\n)/g, "$1");
	text = text.replace(/\n\s+/g, newline);

	return text+" ";
}

function isUpperCase(char){
	return char.match(/[A-Z]/g);
}


var voisisLib = {

		init: function( settings ) {
			var serverAddr = '';
			var postProc = '';
			var serverStatusUrl = '';
			var cmdServerAddr = '';
			var model = '';

			sysPath = settings.sysUrl;  	

			$.ajax({
				async: false,
				type: 'GET',
				url: sysPath+'/system.php?srvTech='+settings.userId,
				success: function(res) {
					if(res['srvTech']){
						settings.srvTech = res['srvTech'];
						if(settings.srvTech == 'v'){
							serverAddr = settings.sysAuUrl;
							cmdServerAddr = res['cmdAddr'];
							model = res['model'];
						}
						else{ // 'g'
							serverAddr = settings.sysAuUrl+"/client/ws/speech";
							cmdServerAddr = res['cmdAddr'];
							serverStatusUrl = settings.sysAuUrl+"/client/ws/status";
							model = res['model'];
						}
					}
				}
			}); 	
			postProc = new KPostProc(); 		

			var url = settings.sysAuUrl.match(/^(([a-z]+:)?(\/\/)?[^\/]+).*$/)[1] || settings.sysAuUrl;
			var parts = url.split(':');
			var asrPort = parseInt(parts[parts.length - 1], 10);		    
			if(parts[1].includes('localhost') || parts[1].includes('127.0.0.1'))
				localAsr = true;
			if(asrPort && (asrPort != "0"))
				localWs = true;

			voisisLib.config = {   		
					sysAuUrl: settings.sysAuUrl,
					asrTech: settings.asrTech,
					userId: settings.userId,
					groupId: settings.groupId,
					userDictOff: settings.userDictOff,
					vocabId: settings.vocabId,
					clientId: settings.clientId,
					browserId: settings.browserId,
					editor: settings.editor,
					editorType: settings.editorType,
					encoderPath: settings.encoderPath,
					statusNotif: settings.statusNotif,
					channelNotif: settings.channelNotif,
					messageNotif: settings.messageNotif,
					errorNotif: settings.errorNotif,
					micPress: settings.micPress,
					getChange: settings.getChange,
					completed: settings.completed,
					dictTimeout: settings.dictTimeout,
					logLevel: settings.logLevel,
					tabNext: settings.tabNext,
					selText: settings.selText, 
					onRefreshTextInfo: settings.onRefreshTextInfo,

					tt: new Transcription(), // Transcription object
					postProc : postProc,

					clicked: false,
					enterPressed: false,
					firstChar: false,
					newLine: false,
					space: false,
					error: false,
					sessionOpened: false,

					command: null,

					dictate: new Dictate({
						server : serverAddr,
						vcSignAddr: settings.sysCmdUrl,
						serverStatus : serverStatusUrl,
						srvTech: settings.srvTech,
						model: model,
						recorderWorkerPath : "./js/recorderWorker.js",
						userDictOff: settings.userDictOff,
						onReadyForSpeech : function() {
							if(voisisLib.config.statusNotif)
								voisisLib.config.statusNotif("Server in ascolto...");
							voisisLib.config.error = false;
							voisisLib.config.sessionOpened = true;
						},
						onEndOfSpeech : function() {
							if(voisisLib.config.statusNotif)
								voisisLib.config.statusNotif("Fine ascolto");
							voisisLib.config.sessionOpened = false;
						},
						onEndOfSession : function() {
							if(voisisLib.config.sessionOpened)
								voisisLib.config.dictate.cancel();
							voisisLib.config.sessionOpened = false;
							if(voisisLib.config.dictTimeout)
								voisisLib.config.dictTimeout();
						},
						onPartialResults : function(hypos) {
							if(settings.srvTech == 'v'){
								voisisLib.config.tt.add(hypos, false);
								voisisLib.updateTranscription(voisisLib.config.tt.toString(), false, 0);
							}
							else{
								voisisLib.config.tt.add(hypos[0].transcript, false);
								voisisLib.updateTranscription(voisisLib.config.tt.toString(), false, hypos[0].likelihood);
							}
						},
						onResults : function(hypos) {
							if(settings.srvTech == 'v'){
								voisisLib.config.tt.add(hypos); 
								voisisLib.updateTranscription(voisisLib.config.tt.toString(), true, 0);
							}
							else{
								voisisLib.config.tt.add(hypos[0].transcript, true);
								voisisLib.updateTranscription(voisisLib.config.tt.toString(), true, 0);
							}
						},
						onSelResults : function(selStar,selNumChar ){            	            		
							voisisLib.selectionTranscription(selStar,selNumChar);
						},
						onError : function(code, data) {
							voisisLib.config.errorNotif(code, data);
						},
						onEvent : function(code, data) {
							event = "";
							if(code == 9)
								event = "start";
							else if((code == 2) || (code == 10))
								event = "stop";
							if((event != "") && voisisLib.config.messageNotif)
								voisisLib.config.messageNotif(event);
						},
						onMicEvent : function(data)
						{
							if(voisisLib.config.logLevel > 0)
								dbgout.log("VoisisVLib-MicEvent");

							var res = JSON.parse(data);
							if (res.EventType == ButtonPressed){ 						
								voisisLib.config.micPress(res.ButtonType);
							}
							else if (res.EventType == Disconnect){ 						
								voisisLib.config.errorNotif(res.EventType, '');
							}	
						},
						onGetChange : function(data)
						{            	
							var res = JSON.parse(data);
							voisisLib.config.getChange(res);						
						}, 
						onCompleted : function(data)
						{            	
							var res = JSON.parse(data);
							if(voisisLib.config.userType != 3)
								voisisLib.config.completed(res);
							if(res['MessageName'] == "OpenProfile"){
								// Start dictation
								if((voisisLib.config.editorType == voisisNS.ckEditor) && (getCookie('autostartDictation') != null)){					    
									eraseCookie('autostartDictation');
									$('#buttonDict').click();	
								}
								// Log
								$.ajax({
									async: false,
									type: 'GET',
									url: sysPath+'/users.php?userLog='+voisisLib.config.userId,
									success: function(res) {
										if(res){
											size = parseInt(res.replace("'",""));
											if(!isNaN(size) && (size != 0))
												voisisLib.config.dictate.setLog(size);
										}
									}
								}); 							
							}
						},
						onDictTimeout: function(data)
						{
							voisisLib.config.dictTimeout();
						}
					}),

			};

			$.ajax({
				type: 'GET',
				url: voisisApp.sysUrl+'/system.php?params',
				success: function(par) {													
					var drvPort = par['drvPort'];
					if(drvPort && (drvPort != 0)){
						try {
							$.getScript(voisisNS.sysUrl+"/js/micDriver.js").done(function( script, textStatus ) {
								new MicDriver(drvPort, voisisLib.config.micPress);							
							}).fail(function( jqxhr, settings, exception ) {
								dbgout.log("VoisisLib: errore nel caricamento di micDriver");
							});

						} catch (error) {
							dbgout.log("VoisisLib: no mic driver " + error);
						}
					}
					if(par['audioSessionMaxSize'] > 0)
						audioSessionEnabled = true;
					else
						audioSessionEnabled = false;
				},
				error: function(result){
					dbgout.log("VoisisLib: errore in get system params");
				}
			});	

			if((typeof voisisLib.config.userDictOff !== 'undefined') && (voisisLib.config.userDictOff != 0)){
				if(!localAsr || localWs){ 
					voisisLib.config.command = new Command();  
					var cmdUrl = '';
					if((settings.srvTech == 'v') || (voisisLib.config.userDictOff == 2)){
						cmdUrl = cmdServerAddr; 
					}
					else{
						cmdUrl = settings.sysAuUrl.replace("wss", "https")+"/client/dynamic/recognize"+"?user="+voisisLib.config.userId+"&vocab="+voisisLib.config.vocabId;
					}
					var cmdTech = settings.srvTech;
					if(voisisLib.config.userDictOff == 2){
						cmdTech = 'v';
					}
					voisisLib.config.command.init(cmdUrl,
							cmdTech,
							function(command) {
						voisisLib.config.tt.add(command, true);
						voisisLib.updateTranscription(voisisLib.config.tt.toString(), true, '');
					},
					voisisLib.config.userId,
					voisisLib.config.logLevel,
					settings.onCommand
					);
				}
			}

			$.ajax({
				url: sysPath+'/users.php?userMicSet='+voisisLib.config.userId,
				success: function(data) {
					micSet = data;
					if(!micSet)
						micSet = "aurec";
					if(localAsr){
						if(localWs){
							voisisLib.config.dictate.init(voisisLib.config.userId, voisisLib.config.vocabId, voisisLib.config.logLevel, 
									"wav", "2048", "4", "10", voisisLib.config.encoderPath, micSet, localAsr, localWs);
						}
						else{
							voisisLib.config.dictate.init(voisisLib.config.userId, voisisLib.config.vocabId, voisisLib.config.logLevel, 
									"none", "", "", "", "", "", localAsr, localWs);
						}
					}
					else{
						voisisLib.config.dictate.init(voisisLib.config.userId, voisisLib.config.vocabId, voisisLib.config.logLevel, 
								"ogg", "2048", "4", "10", voisisLib.config.encoderPath, micSet, localAsr, localWs);
					}
				},
				error: function(req, status, err) {
					dbgout.log("VoisisLib: errore in get micset");
				}
			});

			// Overriding default config
			$.extend( voisisLib.config, settings );    

			if(voisisLib.config.userType == 2){ // Per il PP è analogo a setText(=inizio referto)
				voisisLib.config.setText = true;
			}

			$.ajax({
				url: sysPath+'/users.php?userCompField='+voisisLib.config.userId,
				success: function(data) {
					if(data){
						voisisNS.complField = data;
					}
				},
				error: function(result){
					dbgout.log("VoisisLib: errore in get compfield");
				}
			});	
		},

		readUserSettings: function (id){
			return localStorage.getItem(id);
		},

		writeUserSettings: function(id, value){
			localStorage.setItem(id, value);
		},

		deleteUserSettings: function(id){
			if(localStorage.getItem(id))
				localStorage.removeItem(id);
			if(voisisNS.micDriver)
				voisisNS.micDriver.close();
		},

		reserveChannel: function(url, user){
			var defer = $.Deferred();  
			var res = false;
			voisisLib.pluginLogger(voisisNS.pluginLog.INIT, "VoisisPlugin-reserveChannel: "+user);
			$.ajax({
				url: url+'/gateway.php',
				type: 'POST',
				data: {
					op: "reserve",
					user: user
				},
				success:function (answer) {
					if(answer['res'] === 'ok'){
						if(answer['token'])
							res = answer['token'];
						else
							res = true;
					}            		
					defer.resolve(res);
				},
				error: function(req, status, err) {
					defer.reject(err);
				}
			}); 
			return defer.promise();
		},

		releaseChannel: function(){
			var defer = $.Deferred();  
			var res = false;
			$.ajax({
				url: sysPath+'/gateway.php',
				type: 'POST',
				data: {
					op: "release",
					user: voisisLib.config.userId
				},
				success:function (answer) {
					if(answer['res'] === 'ok')
						res = true;
					defer.resolve(res);
				},
				error: function(req, status, err) {
					defer.reject(err);
				}
			}); 
			return defer.promise();
		},

		checkUser: function(url, user){
			var defer = $.Deferred();	
			var res = false;
			$.ajax({
				url: url+"/users.php?user="+user,
				success: function(answer) {
					voisisLib.pluginLogger(voisisNS.pluginLog.INIT, "VoisisPlugin-checkUser: "+user);
					if(answer['res'] === 'yes'){
						res = true;
					}
					defer.resolve(res);
				},
				error: function(req, status, err) {
					defer.reject(err);
				}
			});	 
			return defer.promise();
		},

		disconnectUser: function(){
			var defer = $.Deferred();  
			var res = false;
			$.ajax({
				url: sysPath+'/users.php',
				type: 'POST',
				data: {
					op: "disconnect",
					user: voisisLib.config.userId
				},
				success:function (answer) {
					if(answer['res'] === 'ok'){
						res = true;
						if(voisisNS.micDriver)
							voisisNS.micDriver.close();
					}
					defer.resolve(res);
				},
				error: function(req, status, err) {
					defer.reject(err);
				}
			}); 
			return defer.promise();
		},

		loginUser: function(url, user, password, groupId){
			var defer = $.Deferred();  
			var res = false;
			voisisLib.pluginLogger(voisisNS.pluginLog.INIT, "VoisisPlugin-loginUser: "+user);
			$.ajax({
				url: url+'/login.php',
				type: 'POST',
				data: {
					user: user,
					password: password,
					groupId: groupId,
					integration: 'plugin'
				},
				success:function (answer) {
					if(answer['res'] === 'ok'){
						res = true;
					}
					defer.resolve(res);
				},
				error: function(req, status, err) {
					defer.reject(err);
				}
			}); 
			return defer.promise();
		},

		startListening: function(editor, editorType) {

			cmd2DictTransition = true;  	
			voisisNS.complFieldIdx = 0;

			voisisLib.config.editor = editor;
			voisisLib.config.editorType = editorType;
			voisisLib.config.dictate.startListening(voisisLib.config.clientId, voisisLib.config.groupId);

			// Se non e' presente del testo (solo tag HTML), settiamo firstChar (per la maiuscola iniziale)
			if(voisisLib.config.editorType == voisisNS.ckEditor){
				var wholeText = voisisLib.config.editor.getData();
				var span = document.createElement('span');
				span.innerHTML = wholeText.trim();
				var elText = span.textContent || span.innerText;
				if(elText.trim() == "")
					voisisLib.config.firstChar = true;
			}
			else if(voisisLib.config.editorType == voisisNS.quill){
				if(voisisLib.config.editor.getText().trim() == "")
					voisisLib.config.firstChar = true;
			}

			if(localAsr){
				$.ajax({
					url: sysPath+'/gateway.php',
					type: 'POST',
					data: {
						op: "userStartDict",
						user: voisisLib.config.userId
					}					
				});  
			}
			if((voisisLib.config.userDictOff != 0) && (voisisLib.config.command))
				voisisLib.config.command.stop();

			if(voisisNS.micDriver)
				voisisNS.micDriver.start();
		},

		stopListening: function(hybridApp=false) {

			cmd2DictTransition = false;  	
			if(localAsr){
				$.ajax({
					url: sysPath+'/gateway.php',
					type: 'POST',
					data: {
						op: "userStopDict",
						user: voisisLib.config.userId
					}					
				});  
			}
			if((voisisLib.config.userDictOff != 0) && (voisisLib.config.command))
				voisisLib.config.command.start();

			if(voisisNS.micDriver){
				voisisNS.micDriver.stop();
				voisisNS.micDriver.disableButt();
			}

			if(audioSessionEnabled){
				$.ajax({
					async: false,
					type: 'GET',
					url: sysPath+'/users.php?checkAudioSessionLimit='+voisisLib.config.userId,
					success: function(answer) {
						if(answer['res'] != "ok"){
							if(hybridApp){
								message = JSON.stringify({ type: "audio-session", content: "max size exceeded" })
								window.chrome.webview.postMessage(message);
							}
						}
					}
				});
			}

			if(voisisLib.config.editorType == voisisNS.quill){
				setTimeout(function(){
					var cursor = 0;
					var range = voisisLib.config.editor.getSelection();
					if(range){
						cursor = range.index;
					}
					voisisLib.config.editor.formatText(cursor, qLastUttLen, 'color', 'black'); 
					var text = cleanTmpUtt(voisisLib.config.editor.getText(cursor, qLastUttLen), "\n");
					var Delta = Quill.import('delta');
					voisisLib.config.editor.updateContents(new Delta().retain(cursor).delete(qLastUttLen).insert(text));
					if(hybridApp){
						message = JSON.stringify({ type: "utt-final", content: text })
						window.chrome.webview.postMessage(message);
					}
					if(voisisNS.micDriver)
						voisisNS.micDriver.enableButt();
					voisisLib.config.dictate.stopListening();
				}, 2000);
			}
			else if(voisisLib.config.editorType == voisisNS.ckEditor){
				setTimeout(function(){
					var el = voisisLib.config.editor.editable().findOne('.tempUtt');
					if(el != null){
						el.removeAttribute('class');
						el.removeAttribute('style');
						text = cleanTmpUtt(el.getHtml(), "<p>");
						el.setHtml(text);
						var range = new CKEDITOR.dom.range(voisisLib.config.editor.document);
						range.moveToElementEditablePosition(el, true);
						voisisLib.config.editor.getSelection().selectRanges([range]);
						if(hybridApp){
							message = JSON.stringify({ type: "utt-final", content: text })
							window.chrome.webview.postMessage(message);
						}
					}
					if(voisisNS.micDriver)
						voisisNS.micDriver.enableButt();
					voisisLib.config.dictate.stopListening();
				}, 2000);
			}
		},

		initListening: function(url, user, editorInstance, editorType) {
			voisisLib.config.editor = editorInstance;
			voisisLib.config.editorType = editorType;
		},

		alignInfo: function(textAlign){
		},

		alignRefresh: function(textAlign){
			voisisNS.complFieldIdx = 0;
		},

		audioWizard: function(){
		},

		setAutotextReplace: function(user, value){
			$.ajax({
				url: sysPath+'/autotext.php',
				type: 'POST',
				data: {
					op: "setAutotextReplace",
					user: user,
					value: value
				}					
			}); 
		},

		keyWordOption: function(){
			var defer = $.Deferred();	
			var res = false;
			$.ajax({
				url: sysPath+'/autotext.php?data=atKeyType&vocab='+voisisLib.config.vocabId,
				success: function(data) {
					res = data['atKeyType'];
					defer.resolve(res);
				},
				error: function(req, status, err) {
					defer.reject(err);
				}
			});	 
			return defer.promise();
		},

		synchUser: function(){
			if(localAsr)
				voisisLib.config.dictate.synchUser();
		},

		addWord: function(newWord, newSpForm){
		},

		showModal: function(funct = ""){
			var res = false;
			if(funct){
				if(funct === "stop-dict")
					res = true;
				else if((funct === "start-dict") && localAsr)
					res = true;
			}
			return res;
		},

		flushText: function(editorInstance, cause="stop"){
		},

		setTextDone: function(value){
			if(voisisLib.config)
				voisisLib.config.setText = value;
		},

		startSound: function(){
			sound = document.getElementById('voisis-start-sound');
			if(sound){
				sound.play();
			}
		},

		stopSound: function(){
			sound = document.getElementById('voisis-stop-sound');
			if(sound){
				sound.play();
			}
		},

		errorSound: function(error){
			sound = document.getElementById('voisis-remove-sound');
			if(sound){
				sound.play();
				this.pluginLogger(voisisNS.pluginLog.EVENTS, error);
			}
		},

		uttInsertion: function(editorInstance, onChangeListener, editorConfig, dictPar, dictationOn, dictationOff, onRecognition, checkWords, text, finalUtt, notifyCommand) {

			if(finalUtt){
				//console.log(">"+text+"<");
				this.pluginLogger(voisisNS.pluginLog.TEXT, "VoisisPlugin-utt-final: "+text, 
						{ op: "uttInsertion", final: finalUtt, text: text});
				// AT newlines
				if(text)
					text = text.replace(/<br\/>/gi, '<br>&#8203;');
			}
			else{
				//console.log("?"+text+"?");
				this.pluginLogger(voisisNS.pluginLog.TEXT, "VoisisPlugin-utt-hyp: "+text);
			}	

			if(cmd2DictTransition){
				cmd2DictTransition = false;
				return;
			}

			var stopDictation = false;

			if(dictPar.transcribing){
				if(finalUtt){
					if(text.toLowerCase().includes("<voisis>start-dictation</voisis>")){
						text = text.replace(/<voisis>start-dictation<\/voisis>.*/gi, '');
					}
					if(text.toLowerCase().includes("<voisis>stop-dictation</voisis>")){
						text = text.replace(/<voisis>stop-dictation<\/voisis>.*/gi, '');
						stopDictation = true;
					}
					if(text.toLowerCase().includes("<voisis>send-text</voisis>")){
						text = text.replace(/<voisis>send-text<\/voisis>.*/gi, '');
						if(notifyCommand)
							notifyCommand("send-text");
					}
					if(text.toLowerCase().includes("<voisis>new-text</voisis>")){
						text = text.replace(/<voisis>new-text<\/voisis>.*/gi, '');
					}
					if(text.toLowerCase().includes("<voisis>fill-form</voisis>")){
						if(voisisLib.config.editorType == voisisNS.ckEditor){
							var el = editorInstance.editable().findOne('.tempUtt');
							if(el != null){
								el.remove();
							}   	
						}
						else{
							var range = editorInstance.getSelection();
							editorInstance.deleteText(qLastIns, qLastUttLen);
							qLastUttLen = 0;
						}
						voisisLib.config.newLine = false;
						this.selectComplField();						
						return;
					}
				}
				else{
					if(text.trim() == "+completa+campo+"){
						if(voisisLib.config.editorType == voisisNS.ckEditor){
							var range = window.voisisApp.voisisCkEditor.createRange();
							range.moveToElementEditEnd(range.root);
							window.voisisApp.voisisCkEditor.getSelection().selectRanges([range]);
						}
						return;
					}
				}
			}
			else if(voisisLib.config.userDictOff != 0){
				if(finalUtt){
					if(text.toLowerCase().includes("<voisis>start-dictation</voisis>")){
						if(voisisLib.config.editorType == voisisNS.ckEditor){
							var el = editorInstance.editable().findOne('.tempUtt');
							if(el != null){
								el.remove();
							}
						}
						this.startSound();
						dictationOn(editorInstance);				
					}
					if(text.toLowerCase().includes("<voisis>send-text</voisis>")){
						text = text.replace(/<voisis>send-text<\/voisis>.*/gi, '');
						if(notifyCommand)
							notifyCommand("send-text");
					}
					if(text.toLowerCase().includes("<voisis>new-text</voisis>")){
						if(voisisLib.config.editorType == voisisNS.ckEditor){
							var startDictCallback = function(){
								dictationOn(editorInstance);
							};
							editorInstance.setData("", startDictCallback);
						}
						else if(voisisLib.config.editorType == voisisNS.quill){
							editorInstance.setText("");
							dictationOn(editorInstance);	
						}
						if(notifyCommand)
							notifyCommand("new-text");									
						this.startSound();
					}
				}
				return;
			}

			if(voisisLib.config.editorType == voisisNS.quill){

				if(this.leadingSpace(editorInstance))
					text = " " + text;

				if(finalUtt){
					if(qInsertUppercase){
						var firstNewChar = text.trim().charAt(0);
						text = text.replace(firstNewChar, firstNewChar.toUpperCase());
						qInsertUppercase = false;
					}
					voisisLib.config.postProc.itn(editorInstance, text, 'br', dictPar.userClick, voisisLib.config.logLevel);
					if(text.toLowerCase().endsWith("<br>") || (text.toLowerCase().endsWith("<br>&#8203;"))){
						voisisLib.config.newLine = true;
					}
					else{
						if(stopDictation && (((text.trim().length == 0) && voisisLib.config.newLine) || (text.trim().toLowerCase().endsWith("<br>"))))
							voisisLib.config.newLine = true;
						else
							voisisLib.config.newLine = false;
					}
					text = voisisLib.config.postProc.itnText(); // PostProcessing ASR

					this.pluginLogger(voisisNS.pluginLog.TEXT, "VoisisPlugin-utt-PP 1: "+text, { op: "uttPostprocessing 1", text: text});
					var selRange = editorInstance.getSelection();
					if((selRange.index > 0) && (voisisLib.config.postProc.isSpaceBefore()))
						text = " " + text;
					if(dictPar.userClick){
						if (selRange && (selRange.length > 0)) {
							var lastChar = text.substr(-1).trim();
							if(lastChar == "")
								text = text.slice(0, -1);
						}
						if(!text.toLowerCase().endsWith("<br>") && this.appendSpace(editorInstance, editorConfig['insertMode'], text))
							text = text+' ';
						dictPar.userClick = false;
						//dictPar.insertAtEnd = false;
					}
					//else if (dictPar.insertAtEnd){
					//	editorInstance.setSelection(editorInstance.getLength(), 0);						
					//	dictPar.insertAtEnd = false
					//	if(this.leadingSpace(editorInstance))
					//		text = " " + text;
					//}
					this.pluginLogger(voisisNS.pluginLog.TEXT, "VoisisPlugin-utt-PP 2: "+text, { op: "uttPostprocessing 2", text: text});

					if(onRecognition){
						this.pluginLogger(voisisNS.pluginLog.TEXT, "VoisisPlugin-TextBeforeOnRec: "+text, { op: "before onRecognition", textBefore: text});
						text = onRecognition(text);
						this.pluginLogger(voisisNS.pluginLog.TEXT, "VoisisPlugin-TextAfterOnRec: "+text, {op: "after onRecognition", textAfter: text });			
					}				
				}

				var cursor = 0;
				var rangeLen = 0;
				var range = editorInstance.getSelection();
				if(range){
					cursor = range.index;
					rangeLen = range.length;

					var format = editorInstance.getFormat();
					var extraLen = 0;
					if(rangeLen > qLastUttLen)
						extraLen = rangeLen - qLastUttLen; 
					var Delta = Quill.import('delta');
					text = html2text(text.replace(/<br>/g, '\n'));

					if(cursor != qLastIns){
						editorInstance.deleteText(qLastIns, qLastUttLen);
						qLastIns = cursor;
					}
					editorInstance.updateContents(new Delta().retain(cursor).delete(qLastUttLen).insert(text).delete(extraLen));

					if(format)
						editorInstance.formatText(cursor, text.length, format);
					this.pluginLogger(voisisNS.pluginLog.TEXT, "VoisisPlugin-updateContents: "+text, { op: "updateContents Quill", text: text }, true);

					if(finalUtt){
						editorInstance.formatText(cursor, text.length, 'color', 'black');
						editorInstance.setSelection(cursor+text.length);
						qLastUttLen = 0;
						qLastIns = cursor+text.length;
						voisisLib.config.firstChar = false;
					}
					else{
						editorInstance.formatText(cursor, text.length, 'color', 'blue'); 
						qLastUttLen = text.length;
					}

				}
				else{
					editorInstance.focus();
					this.pluginLogger(voisisNS.pluginLog.TEXT, "VoisisPlugin-updateContents: refocused");
				}

			}
			else if(voisisLib.config.editorType == voisisNS.ckEditor){
				editorInstance.removeListener('change', onChangeListener);

				var sel = editorInstance.getSelection();
				var range = sel.getRanges()[0];
				if (!range) {
					range = editorInstance.createRange();
					range.selectNodeContents( editorInstance.editable() );
					sel.selectRanges( [ range ] );
				}

				// Identifichiamo ultimo risultato parziale, se c'e'
				var el = editorInstance.editable().findOne('.tempUtt');
				if(el != null){
					el.remove();
				}

				if(!finalUtt){	
					ckSelectedText = editorInstance.getSelection().getSelectedText();
					if(ckSelectedText)
						ckSelectedText = ckSelectedText.trim();
					//if (dictPar.insertAtEnd){
					//	range = editorInstance.createRange();
					//	range.moveToElementEditEnd(range.root);
					//	editorInstance.getSelection().selectRanges([range]);
					//}
					if(this.leadingSpace(editorInstance))
						text = "&nbsp;" + text;
					editorInstance.insertHtml("<span class='tempUtt' style='color:grey;'>"+text+"</span>", editorConfig['insertMode']);

					if(ckSelectedText.trim().length > 0){
						if(isUpperCase(ckSelectedText.trim().charAt(0)))
							ckInsertUppercase = true;
					}
				}
				// Utterance finale
				else{
					// Se non c'è una temporanea, e c'è del testo selezionato, rivalutiamo ckInsertUppercase
					var el = editorInstance.editable().findOne('.tempUtt');
					if(el == null){
						ckSelectedText = editorInstance.getSelection().getSelectedText();
						if(ckSelectedText){
							ckSelectedText = ckSelectedText.trim();
							if(ckSelectedText.trim().length > 0){
								if(isUpperCase(ckSelectedText.trim().charAt(0)))
									ckInsertUppercase = true;
							}
						}
					}
					if(ckInsertUppercase){
						var firstNewChar = text.trim().charAt(0);
						text = text.replace(firstNewChar, firstNewChar.toUpperCase());
						ckInsertUppercase = false;
					}

					voisisLib.config.postProc.itn(editorInstance, text, editorConfig['newline'], dictPar.userClick, voisisLib.config.logLevel);
					if(text.toLowerCase().endsWith("<br>") || (text.toLowerCase().endsWith("<br>&#8203;"))){
						voisisLib.config.newLine = true;
					}
					else{
						if(stopDictation && (((text.trim().length == 0) && voisisLib.config.newLine) || (text.trim().toLowerCase().endsWith("<br>"))))
							voisisLib.config.newLine = true;
						else
							voisisLib.config.newLine = false;
					}
					text = voisisLib.config.postProc.itnText(); // PostProcessing ASR
					this.pluginLogger(voisisNS.pluginLog.TEXT, "VoisisPlugin-utt-PP 1: "+text, 
							{ op: "uttPostprocessing 1", text: text});
					if(voisisLib.config.postProc.isSpaceBefore())   							
						text = "&nbsp;" + text;
					if(dictPar.userClick){
						if(ckSelectedText == ""){
							if(!text.toLowerCase().endsWith("<br>") && this.appendSpace(editorInstance, editorConfig['insertMode'], text))
								text = text+'&nbsp;';
						}
						else{
							var lastChar = text.substr(-1).trim();
							if(lastChar == "")
								text = text.slice(0, -1);
							ckSelectedText = "";
						}
						dictPar.userClick = false;
						//dictPar.insertAtEnd = false;
					}
					//else if (dictPar.insertAtEnd){
					//	range = editorInstance.createRange();
					//	range.moveToElementEditEnd(range.root);
					//	editorInstance.getSelection().selectRanges([range]);
					//	dictPar.insertAtEnd = false
					//	if(this.leadingSpace(editorInstance))
					//		text = "&nbsp;" + text;
					//}
					this.pluginLogger(voisisNS.pluginLog.TEXT, "VoisisPlugin-utt-PP 2: "+text, 
							{ op: "uttPostprocessing 2", text: text});

					if(onRecognition){
						this.pluginLogger(voisisNS.pluginLog.TEXT, "VoisisPlugin-TextBeforeOnRec: "+text, { op: "before onRecognition", textBefore: text});
						text = onRecognition(text);
						this.pluginLogger(voisisNS.pluginLog.TEXT, "VoisisPlugin-TextAfterOnRec: "+text, {op: "after onRecognition", textAfter: text });			
					}

					if($("#voisis-button-check-words") &&
							$("#voisis-button-check-words").attr("class") &&
							$("#voisis-button-check-words").attr("class").indexOf("down") > -1){
						$.each(checkWords, function (word, color) {	
							// Nb: replace case insensitive
							text = text.replace(new RegExp("\\b" + word + "\\b", "gi"), 
									function(v) { return "<span class=\"voisisHL"+color+"\">"+v+"</span>"; });
						});
					}

					// presenza di a-capo
					if((editorConfig['newline'] == 'p') && text.includes("<br>")){
						if(text.startsWith("<br>"))
							text = "&#8203;"+text;
						var paragraphs = text.split("<br>");
						paragraphs.forEach(function(elem, idx, array) {
							editorInstance.insertHtml(elem, editorConfig['insertMode']);
							if(idx < (array.length-1))
								editorInstance.execCommand( 'shiftEnter' );
						});
					}
					else{
						if(editorConfig['phraseBoundary'] == 'span')
							text = "<span>"+text+"</span>";
						editorInstance.insertHtml(text, editorConfig['insertMode']);
					}

					voisisLib.config.firstChar = false;
					this.pluginLogger(voisisNS.pluginLog.TEXT, "VoisisPlugin-insHtml: "+text, { op: "insHtml ckEditor", text: text }, true);

				}

				// Breve timeout per consentire completamento
				setTimeout(function() {
					editorInstance.on('change', onChangeListener);	
				}, 10);
			}

			if(stopDictation){
				dictationOff();
				this.stopSound();
			}		

			if(voisisLib.config && finalUtt && voisisLib.config.setText)
				voisisLib.config.setText = false;

		},

		toUTF8Array: function(str) {
			var utf8 = [];
			for (var i=0; i < str.length; i++) {
				var charcode = str.charCodeAt(i);
				if (charcode < 0x80) utf8.push(charcode);
				else if (charcode < 0x800) {
					utf8.push(0xc0 | (charcode >> 6), 
							0x80 | (charcode & 0x3f));
				}
				else if (charcode < 0xd800 || charcode >= 0xe000) {
					utf8.push(0xe0 | (charcode >> 12), 
							0x80 | ((charcode>>6) & 0x3f), 
							0x80 | (charcode & 0x3f));
				}
				// surrogate pair
				else {
					i++;
					charcode = ((charcode&0x3ff)<<10)|(str.charCodeAt(i)&0x3ff)
					utf8.push(0xf0 | (charcode >>18), 
							0x80 | ((charcode>>12) & 0x3f), 
							0x80 | ((charcode>>6) & 0x3f), 
							0x80 | (charcode & 0x3f));
				}
			}
			return utf8;
		},

		////////////// checkwords /////////////
		// legacy
		setCheckOnOff: function(editorInstance, checkWords, dictPar, onOff) {
			this.setCheckOnOffCkEditor(editorInstance, checkWords, dictPar, onOff);
		},
		// ckEditor
		setCheckOnOffCkEditor: function(editorInstance, checkWords, dictPar, onOff) {

			var sel = editorInstance.getSelection();
			if(!sel)
				return;

			var oldRanges = editorInstance.getSelection().getRanges();
			var oldRange = oldRanges[oldRanges.length - 1];
			if(oldRange && (oldRange.endOffset - oldRange.startOffset) > 1){
				var newRange = editorInstance.createRange();
				newRange.setStart(oldRange.endContainer, oldRange.endOffset);
				newRange.setEnd(oldRange.endContainer, oldRange.endOffset);
				editorInstance.getSelection().selectRanges([ newRange ]);
				sel = editorInstance.getSelection();
			}

			var element = sel.getStartElement();
			if(element){
				editorInstance.insertHtml("<mark id=\"cursorPos\">_</mark>");
				sel.selectElement(element);
				range = editorInstance.getSelection().getRanges()[0];
			}

			var htmlStr = String(editorInstance.getData()); 

			cleanHtmlStr = htmlStr.replace(/<span><span class="voisisHL(\w+)">(.+?)<\/span>(\s+?)<\/span>/g, "$2");
			cleanHtmlStr = cleanHtmlStr.replace(/<span class="voisisHL(\w+)">(.+?)<\/span>/g, "$2");
			cleanHtmlStr = cleanHtmlStr.replace(/<font color="(.+)"><u>/g, "");
			cleanHtmlStr = cleanHtmlStr.replace(/<\/u><\/font>/g, "");
			if(onOff)
			{
				$.each(checkWords, function (word, color) {			
					cleanHtmlStr = cleanHtmlStr.replace(new RegExp("\\b" + word + "\\b", "gi"), 
							function(v) { return "<span class='voisisHL"+color+"'>"+v+"</span>"; });
				});
				const regexp = /((\s|\>|&nbsp;)(\S*))?\<mark id=\"cursorPos\"\>_\<\/mark\>(\S*)(\s\<)/gi;
				const matches = cleanHtmlStr.matchAll(regexp);
				for (const match of matches) {
					word = match[3]+match[4];
					if(typeof checkWords[word] !== 'undefined') {
						var color =  checkWords[word];
						cleanHtmlStr = cleanHtmlStr.replace(match[3]+"<mark id=\"cursorPos\"\>_\<\/mark\>"+match[4], 
								"<span class='voisisHL"+color+"'>"+match[3]+"<mark id=\"cursorPos\"\>_\<\/mark\>"+match[4]+"</span>");	
					}
				}
			}

			// La setData e' asincrona
			var fooCallback = function(){				
				if(dictPar && dictPar.transcribing){
					editorInstance.document.getBody().setStyle('background-color', '#fcf881');				
				}

				var element = editorInstance.editable().findOne('mark');
				var range;

				if(element) {
					element.scrollIntoView();

					// Thank you S/O
					// http://stackoverflow.com/questions/16835365/set-cursor-to-specific-position-in-ckeditor
					range = editorInstance.createRange();
					range.moveToPosition(element, CKEDITOR.POSITION_AFTER_START);
					editorInstance.getSelection().selectRanges([range]);

					editorInstance.editable().findOne('mark').remove();
				}
			};
			//if(onOff && !cleanHtmlStr.endsWith("&nbsp;"))
			//	cleanHtmlStr = cleanHtmlStr + "&nbsp;";

			editorInstance.setData(cleanHtmlStr, fooCallback);

		},
		// quill
		setCheckOnOffQuill:function(editorInstance, checkWords, onOff) {

			let totalText = editorInstance.getText();
			editorInstance.removeFormat(0, totalText.length);
			if(onOff){				
				$.each(checkWords, function (word, color) {
					let re = new RegExp(word, "g");
					let match = re.test(totalText);
					if (match) {
						let indices = voisisLib.getIndicesOf(word, totalText , false);
						let length = word.length;
						indices.forEach(index => editorInstance.formatText(index, length, {'color': color}));
					}
				});

				var range = editorInstance.getSelection();
				if(range && voisisLib.config.space){
					editorInstance.removeFormat(range.index-1, 1);
				}
			}

		},
		////////////////////////////////////////////

		////////////// change listener /////////////
		// legacy
		onChangeListener: function(event, pluginOnChangeListener, editorInstance, textAlign, isCheckWords, editorType) {
			this.onChangeListenerCkEditor(event, pluginOnChangeListener, editorInstance, isCheckWords);
		},
		// ckEditor
		onChangeListenerCkEditor: function(event, pluginOnChangeListener, editorInstance, isCheckWords) {

			this.pluginLogger(voisisNS.pluginLog.EVENTS, "VoisisPlugin-ONCHLIST", {op: "onChangeListenerCkEditor"}); 

			var checkWordsBool = false;
			if(typeof isCheckWords === 'function')
				checkWordsBool = isCheckWords();
			else
				checkWordsBool = isCheckWords;

			if(checkWordsBool){
				this.pluginLogger(voisisNS.pluginLog.EVENTS, "VoisisPlugin-chWrd ON", { op: "checkWordsBool ON"});

				editorInstance.removeListener('change', pluginOnChangeListener);

				var s = editorInstance.getSelection();  
				var node = s._.cache.nativeSel.focusNode;
				if(node){
					var parentNode = node.parentElement;
					if(parentNode){
						parentNode.classList.remove("voisisHLblue");
						parentNode.classList.remove("voisisHLred");
						parentNode.classList.remove("voisisHLgreen");
						s.root.mergeSiblings();
					}

					var wholeText = node.wholeText;
					if (wholeText !== undefined) {
						node.parentNode.normalize();
						var wholeText = node.wholeText;
						var newHtml = wholeText;
						const words = wholeText.trim().split(" ");
						$.each(voisisApp.checkWords, function (checkWord, color) {
							$.each(words, function (index, word) {
								if(word == checkWord)
									newHtml = newHtml.replace(word, "<span class='voisisHL"+color+"'>"+word+"</span>");
							});
						});
						if(newHtml != wholeText){
							node.data = "";
							editorInstance.insertHtml(newHtml);
						}
					}
				}

				editorInstance.on('change', pluginOnChangeListener);
			}
		},
		// quill
		getIndicesOf: function(searchStr, str, caseSensitive) {
			var searchStrLen = searchStr.length;
			if (searchStrLen == 0) {
				return [];
			}
			var startIndex = 0, index, indices = [];
			if (!caseSensitive) {
				str = str.toLowerCase();
				searchStr = searchStr.toLowerCase();
			}
			while ((index = str.indexOf(searchStr, startIndex)) > -1) {
				indices.push(index);
				startIndex = index + searchStrLen;
			}
			return indices;
		},
		onChangeListenerQuill: function(delta, oldDelta, source, pluginOnChangeListener, editorInstance, isCheckWords) {

			if(isCheckWords()){
				editorInstance.off('text-change', pluginOnChangeListener);

				this.setCheckOnOffQuill(editorInstance, voisisApp.checkWords, true);

				editorInstance.on('text-change', pluginOnChangeListener);
			}
		},
		///////////////////////////////////////////

		////////////// selection change listener /////////////
		onQSelChange: function(editorInstance){
			var range = editorInstance.getSelection();
			if(range){
				qLastIns = range.index;
				// Almeno un carattere selezionato dall'utente
				if(range.length > 0){
					var firstSelChar = editorInstance.getText(range.index, 1);
					if(isUpperCase(firstSelChar))
						qInsertUppercase = true;
				}
			}
		},

		onKey: function(keyPressed, event){
			// Enter	
			if(keyPressed == 13)
				voisisLib.config.newLine = true;
			else
				voisisLib.config.newLine = false;
			// Tab
			if(voisisLib.config.tabNext && voisisLib.config.tabNext == 'yes' && keyPressed == 9){
				voisisLib.config.newLine = false;
				this.selectComplField();
				if(event)
					event.data.domEvent.preventDefault();
			}
			// Space
			if(keyPressed == 32){
				voisisLib.config.space = true;
			}
			else {
				voisisLib.config.space = false;
				voisisLib.config.firstChar = false;
			}
		},

		onCharDelete: function(){
			voisisLib.config.space = false;
		},

		onDoubleClick: function(e){
			if(voisisLib.config.editorType == voisisNS.ckEditor){
				if (e.data.element.$.nodeName == "A")
				{
					href = e.data.element.$.href;
					window.open(href);
					return false;
				}
			}
		},



		cancel: function() {
			voisisLib.config.dictate.cancel();
		},

		hexDump: function(str)
		{
			var arr1 = [];
			for (var n = 0, l = str.length; n < l; n ++) 
			{
				var hex = Number(str.charCodeAt(n)).toString(16);
				arr1.push(hex);
			}
			return arr1.join('');
		},

		copyToClipboard: function(editorInstance, elem){

			if(voisisLib.config.editorType == voisisNS.ckEditor){
				this.pluginLogger(voisisNS.pluginLog.EVENTS, "VoisisPlugin-Copy: "+this.hexDump(editorInstance.getData()));
			}
			else if(voisisLib.config.editorType == voisisNS.quill){
				this.pluginLogger(voisisNS.pluginLog.EVENTS, "VoisisPlugin-Copy: "+this.hexDump(editorInstance.getText()));
			}

			// create hidden text element, if it doesn't already exist
			var targetId = "_hiddenCopyText_";

			// must use a temporary form element for the selection and copy
			target = document.getElementById(targetId);
			if (!target) {
				var target = document.createElement("textarea");
				target.style.position = "absolute";
				target.style.left = "-9999px";
				target.style.top = "0";
				target.id = targetId;
				document.body.appendChild(target);
			}
			if(voisisLib.config.editorType == voisisNS.ckEditor){
				elem.textContent = elem.textContent.replace(/\n\n/g, "\n");
			}
			target.textContent = elem.textContent;

			// Rimozione caratteri speciali 
			target.textContent = target.textContent.replace(String.fromCharCode(8217), "'"); // carattere esteso per apostrofo
			target.textContent = target.textContent.replace(/[^\x00-\xFF]/g,"")
			target.textContent = target.textContent.replace(/\xa0/g, ' '); // non-break space

			// select the content
			var currentFocus = document.activeElement;
			target.focus();
			target.setSelectionRange(0, target.value.length);

			// copy the selection
			var succeed;
			try {
				succeed = document.execCommand("copy");
			} catch(e) {
				succeed = false;
				this.pluginLogger(voisisNS.pluginLog.ERROR, "VoisisPlugin-exception: "+e.message);
			}
			// restore original focus
			if (currentFocus && typeof currentFocus.focus === "function") {
				currentFocus.focus();
			}

			// clear temporary content
			target.textContent = "";

			return succeed;

		},

		selectComplField: function(){

			var text = "";
			if(voisisLib.config.editorType == voisisNS.quill){
				text = voisisLib.config.editor.getText();
			}
			else{
				text = html2text(voisisLib.config.editor.getData()); 
			}

			var fieldNum = text.split(voisisNS.complField).length - 1;
			if((fieldNum > 0) && (voisisNS.complFieldNum - fieldNum) > 0){
				voisisNS.complFieldIdx -= (voisisNS.complFieldNum - fieldNum);
			}
			voisisNS.complFieldNum = fieldNum; 

			if(voisisLib.config.editorType == voisisNS.quill){
				var found = false; var cnt = 0;
				var index = text.indexOf(voisisNS.complField);
				while (index > -1) {
					if(cnt == voisisNS.complFieldIdx){					
						found = true;
						break;
					}
					else{
						cnt++;
						index = text.indexOf(voisisNS.complField, index+1);
					}
				} 

				if(found){
					voisisLib.config.editor.setSelection(index, voisisNS.complField.length);
					voisisNS.complFieldIdx++;
				}
				// Seleziona il primo
				else if (voisisNS.complFieldIdx > 0){
					index = text.indexOf(voisisNS.complField);
					if (index > -1) {
						voisisLib.config.editor.setSelection(index, voisisNS.complField.length);
						voisisNS.complFieldIdx = 1;
					}   						
				}
			}
			else{
				var selection = voisisLib.config.editor.getSelection(), root = selection.root, ranges = [], range, ccText, index;

				// Ricerca ricorsiva di tutti i nodi di testo
				ckTextNodes = [];
				this.getCkTextNodes(root); 

				// Selezione dell'istanza del carattere 'Completa campo'
				var found = false; var cnt = 0;
				for (var i = ckTextNodes.length; i--;){
					ccText = ckTextNodes[i];
					index = ccText.getText().indexOf(voisisNS.complField);
					while (index > -1) {
						if(cnt == voisisNS.complFieldIdx){
							range = voisisLib.config.editor.createRange();
							range.setStart(ccText, index);
							range.setEnd(ccText, index + voisisNS.complField.length); 
							ranges.push(range);
							found = true;
							break;
						}
						else{
							cnt++;
							index = ccText.getText().indexOf(voisisNS.complField, index+1);
						}
					}  
					if(found)
						break;
				}   						

				if(found){
					selection.selectRanges( ranges );
					voisisNS.complFieldIdx++;
				}
				// Seleziona il primo
				else if (voisisNS.complFieldIdx > 0){
					for (var i = ckTextNodes.length; i--;){
						ccText = ckTextNodes[i];
						index = ccText.getText().indexOf(voisisNS.complField);
						if (index > -1) {
							range = voisisLib.config.editor.createRange();
							range.setStart(ccText, index);
							range.setEnd(ccText, index + voisisNS.complField.length); 
							ranges.push(range);
							selection.selectRanges( ranges );
							voisisNS.complFieldIdx = 1;
							break;
						}  
					}   						
				}
			}
		},

		getCkTextNodes: function (element) {
			var children = element.getChildren(), child;
			for (var i = children.count(); i--;) {
				child = children.getItem(i);
				if (child.type == CKEDITOR.NODE_ELEMENT) 
					voisisLib.getCkTextNodes(child);
				else if (child.type == CKEDITOR.NODE_TEXT)
					ckTextNodes.push(child);
			}
		},

		selectionTranscription: function(selStar, selNumChars) {
			voisisLib.config.selText(selStar, selNumChars);
		},

		updateTranscription: function(text, finalUtt, confScore) {
			voisisLib.config.uttInsertion(text, finalUtt, confScore);
		},

		leadingSpace: function(editorInstance){
			var text = "";
			if(voisisLib.config.editorType == voisisNS.quill){
				var text = editorInstance.getText();
			}
			else if(voisisLib.config.editorType == voisisNS.ckEditor){
				var text = html2text(editorInstance.getData());
			}
			if(text.trim().length == 0)
				return false;
			return true;
		},

		appendSpace: function(editorInstance, insertMode, text){

			var nextChar = '';

			if(voisisLib.config.editorType == voisisNS.ckEditor){

				var sel = editorInstance.getSelection();
				if(!sel)
					return;
				var element = sel.getStartElement();
				if(!element)
					return;

				editorInstance.insertHtml("<span id='userClick' style='display:none;'>userClick</span>", insertMode);
				var htmlStr = String(editorInstance.getData());
				nextChar = htmlStr.split('userClick</span></p>').pop()[0];
				if(!nextChar)
					nextChar = htmlStr.split('userClick</span>').pop()[0];

				var el = editorInstance.editable().findOne('#userClick');
				if(el != null){
					el.remove();
				}
			}
			else if(voisisLib.config.editorType == voisisNS.quill){
				var selRange = editorInstance.getSelection();
				if(selRange)
					var nextChar = editorInstance.getText(selRange.index+qLastUttLen, 1);
			} 

			if(!nextChar)
				return false;

			var punct = [".", ",", ":", ";", "!", "?", ")", "-", " ", "\n"];
			if(punct.indexOf(nextChar) == -1)
				return true;
			return false;
		},

		initCkEditorCss: function(fontSize, fontFamily){

			CKEDITOR.addCss( '.voisisHLgreen {color: rgb(0,196,0); text-decoration: underline; !important }'  ); 
			CKEDITOR.addCss( '.voisisHLred {color: red; text-decoration: underline; !important }'  ); 
			CKEDITOR.addCss( '.voisisHLblue {color: blue; text-decoration: underline; !important }'  ); 
			CKEDITOR.addCss( '.voisisUnknown {text-decoration: red underline; text-decoration-style: dashed; !important }'  ); 

			if ((fontSize && (fontSize != "")) || (fontFamily && (fontFamily != "")))
			{
				CKEDITOR.config.fontSize_defaultLabel = fontSize;	
				CKEDITOR.config.font_defaultLabel = fontFamily;
				var cssString = '.cke_editable { ';
				if (fontSize && (fontSize != ""))
					cssString = cssString + 'font-size: ' + fontSize + 'pt !important;';
				if (fontFamily && (fontFamily != ""))
					cssString = cssString + 'font-family: ' + fontFamily + ' !important;';
				cssString = cssString + '}';
				CKEDITOR.addCss(cssString);
			}
		},

		readyQuill: function(editorInstance){		
		},

		readyCkEditor: function(editorInstance){		
			editorInstance.on( 'paste', function( evt ) {
				if($("#voisis-button-check-words") && $("#voisis-button-check-words").attr("class") && $("#voisis-button-check-words").attr("class").indexOf("down") > -1){
					setTimeout(function() {
						voisisLib.setCheckOnOffCkEditor(editorInstance, voisisApp.checkWords, voisisApp.dictPar, false);
						setTimeout(function() {
							voisisLib.setCheckOnOffCkEditor(editorInstance, voisisApp.checkWords, voisisApp.dictPar, true);
						}, 200)
					}, 200)
				}
			});
		},

		userVocabs: function(){		
			var userVocabs = [];
			$.ajax({
				async: false,
				type: 'GET',
				url: sysPath+"/users.php?userVocabs="+voisisLib.config.userId,
				success: function(data) {
					userVocabs = data;
				},
				error: function(result){
					dbgout.log("VoisisLib: errore in get user vocabs");
				}
			});
			return userVocabs;
		},

		changeUserVocab: function(userId, vocabId, clientId) {
			voisisLib.config.userId = userId;
			voisisLib.config.vocabId = vocabId;
			voisisLib.config.clientId = clientId;
			voisisLib.config.dictate.changeUserVocab(userId, vocabId, clientId);
			if(voisisLib.config.command && voisisLib.config.srvTech == 'g'){
				cmdUrl = voisisLib.config.sysAuUrl.replace("wss", "https")+"/client/dynamic/recognize"+"?user="+voisisLib.config.userId+"&vocab="+voisisLib.config.vocabId;
				voisisLib.config.command.changeUrl(cmdUrl);
			}
		},

		pluginLogger: function(opLogLevel, clientPrompt, serverPrompt="", flushToServer=false) {
			/// PATCH per hook mancanti
			if(serverPrompt['op'] && (serverPrompt['op'] == 'setText') && voisisLib.config){
				voisisLib.config.setText = true;	
			}

			if(opLogLevel == voisisNS.pluginLog.ERROR){
				dbgout.log(clientPrompt);
				return;
			}		

			if((opLogLevel == voisisNS.pluginLog.INIT) || 
					(voisisLib.config && voisisLib.config.logLevel && (voisisLib.config.logLevel >= voisisNS.pluginLog.API) 
							&& (opLogLevel <= voisisLib.config.logLevel))){
				dbgout.log(clientPrompt);
				if(serverPrompt){
					var date = new Date();
					serverPrompt.date = date.getHours()+":"+date.getMinutes()+":"+date.getSeconds();
					voisisNS.jsonLog.push(serverPrompt);				
					if(flushToServer){
						$.ajax({
							url: sysPath+'/clientLogger.php',
							type: 'POST',
							data: {
								user: voisisLib.config.userId,
								log: JSON.stringify(voisisNS.jsonLog)
							},
							success:function (answer) {		
								voisisNS.jsonLog = [];
								return true;
							},
							error: function(result){
								return false;
							}
						});	
					}
				};
			}
		}
};

//In caso di reload della pagina, back, chiusura del browser
$(window).on('beforeunload', function(evt){
	if (voisisApp && voisisApp.sysUrl) {
		$.ajax({
			async: false,
			url: voisisApp.sysUrl + '/gateway.php',
			type: 'POST',
			data: {
				op: "release",
				user: voisisLib.config.userId
			},
			success:function (answer) {
				// do nothing
			}
		});
	}
});
