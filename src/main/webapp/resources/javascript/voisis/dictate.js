function createVumeterNode(stream, recorder){

	var ctx = new AudioContext();
	var mic = ctx.createMediaStreamSource(stream);	
	var gainNode = ctx.createGain();
	mic.connect(gainNode);

	var analyser = ctx.createAnalyser();
	analyser.smoothingTimeConstant = 0.8;
	analyser.fftSize = 1024;
	vumeterNode = ctx.createScriptProcessor(2048, 1, 1);
	gainNode.connect(analyser);
	analyser.connect(vumeterNode);

	var dest = ctx.createMediaStreamDestination();
	analyser.connect(dest);
	vumeterNode.connect(dest);
	$('#voisis-gain-value').on('DOMSubtreeModified',function(){
		var val = document.getElementById('voisis-gain-value').innerHTML;
		if(val){
			gainNode.gain.value = parseFloat(val);
			if(config.logLevel > 3)
				dbgout.log("GAIN: "+val);
		}
	});
	gainNode.gain.value = parseFloat(document.getElementById('voisis-gain-value').innerHTML);

	if(recorder == 'wav')
		voisisNS.recorder = new wavRecorder(mic, { workerPath : '/VoisisCloud/js/recorderWorker.js'});
	else
		voisisNS.recorder = new MediaRecorder(dest.stream);		

	// Store the source and destination in a global variable
	// to avoid losing the audio to garbage collection.
	window.leakMyAudioNodes = [mic, dest];

	canvasContext = $("#voisis-vu-meter")[0].getContext("2d");

	vumeterNode.onaudioprocess = function() {
		var array = new Uint8Array(analyser.frequencyBinCount);
		analyser.getByteFrequencyData(array);
		var values = 0;
		var length = array.length;
		for (var i = 0; i < length; i++) {
			values += (array[i]);
		}
		var average = 3.5*values / length;
		canvasContext.clearRect(0, 0, 120, 7);
		if(average < 40)
			canvasContext.fillStyle = '#ffff66';
		else if(average < 80)
			canvasContext.fillStyle = '#BadA55';
		else
			canvasContext.fillStyle = '#ff0000';
		canvasContext.fillRect(0, 0, average, 7);
		canvasContext.fillStyle = '#262626';
	}
}


(function(window){

	// Defaults
	var SERVER = "";
	var SERVER_STATUS = "";
	var CONTENT_TYPE = "content-type=audio/x-raw,+layout=(string)interleaved,+rate=(int)16000,+format=(string)S16LE,+channels=(int)1";
	var TAG_END_OF_SENTENCE = "EOS";

	// Error codes (mostly following Android error names and codes)
	var ERR_NETWORK = 2;
	var ERR_AUDIO = 3;
	var ERR_SERVER = 4;
	var ERR_CLIENT = 5;

	// Event codes
	//var MSG_WAITING_MICROPHONE = 1;
	//var MSG_MEDIA_STREAM_CREATED = 2;
	var MSG_INIT_RECORDER = 3;
	var MSG_RECORDING = 4;
	var MSG_SEND = 5;
	var MSG_SEND_EMPTY = 6;
	var MSG_SEND_EOS = 7;
	var MSG_WEB_SOCKET = 8;
	var MSG_WEB_SOCKET_OPEN = 9;
	var MSG_WEB_SOCKET_CLOSE = 10;
	var MSG_STOP = 11;
	var MSG_SERVER_CHANGED = 12;

	// Server status codes
	// from https://github.com/alumae/kaldi-gstreamer-server
	var SERVER_STATUS_CODE = {
			0: 'Success', // Usually used when recognition results are sent
			1: 'No speech', // Incoming audio contained a large portion of silence or non-speech
			2: 'Aborted', // Recognition was aborted for some reason
			9: 'No available', // Recognizer processes are currently in use and recognition cannot be performed
	};

	var Dictate = function(cfg) {
		var config = cfg || {};
		config.server = config.server || SERVER;

		config.audioSourceId = config.audioSourceId;
		config.serverStatus = config.serverStatus || SERVER_STATUS;
		config.contentType = config.contentType || CONTENT_TYPE;
		config.onReadyForSpeech = config.onReadyForSpeech || function() {};
		config.onEndOfSpeech = config.onEndOfSpeech || function() {};
		config.onPartialResults = config.onPartialResults || function(data) {};
		config.onSelResults = config.onSelResults || function(data) {};
		config.onResults = config.onResults || function(data) {};
		config.onEndOfSession = config.onEndOfSession || function() {};
		config.onEvent = config.onEvent || function(e, data) {};
		config.onError = config.onError || function(e, data) {};
		config.onMicEvent = config.onMicEvent  || function(data) {};
		config.onGetChange = config.onGetChange  || function(data) {};
		config.onCompleted = config.onCompleted  || function(data) {};
		config.onDictTimeout = config.onDictTimeout  || function(data) {};
		config.userId = config.userId;
		config.clientId = config.clientId;
		config.interval = config.interval || 250;	// Wav recorder
		config.userDictOff = config.userDictOff;
		config.showVumeter = false;
		config.srvTech = config.srvTech;
		config.model = config.model;

		if (config.onServerStatus) {
			monitorServerStatus();
		}

		// Endpoints
		var wsAudioServerStatus = null;

		// Wav recorder
		var audioContext = null;
		var intervalKey = 0;

		var gumStream = null;
		var chunks = [];

		var logSample = 0;

		this.init = function(user, vocab, logLevel, audioFormat, bufferLength, maxBuffersPerPage, resampleQuality, encoderPath, micSet, localAsr, localWs) {

			config.userId = user;
			config.vocabId = vocab;
			config.logLevel = logLevel;
			config.audioFormat = audioFormat;
			config.micSet = micSet;
			config.localAsr = localAsr;
			config.localWs = localWs;

			if(config.logLevel > 1)
				dbgout.log("VoisisDictate-Init-BEGIN");

			if(audioFormat == "wav"){
				navigator.mediaDevices.getUserMedia({
					audio: true
				}).then(function(stream) {
					if(document.getElementById('voisis-gain-value')) {
						createVumeterNode(stream, 'wav');
					}
					else{
						voisisNS.recorder = new MediaRecorder(stream);
						var ctx = new AudioContext();
						var mic = ctx.createMediaStreamSource(stream);	
					}
				});
			}
			else if(audioFormat == "ogg"){
				if(config.micSet == "aurec"){
					if(!encoderPath)
						encoderPath = "./js/encoderWorker.min.js";
					else
						encoderPath = encoderPath+"/encoderWorker.min.js";
					if(!voisisNS.recorder){
						voisisNS.recorder = new Recorder({
							bufferLength: bufferLength,
							maxBuffersPerPage: maxBuffersPerPage,
							monitorGain: 0,
							numberOfChannels: 1,
							wavBitDepth: 16,
							encoderPath: encoderPath,
							encoderApplication: 2048, // Voice
							streamPages: true,
							leaveStreamOpen: true,
							resampleQuality: resampleQuality,
							encoderSampleRate: 16000
						});
						try
						{
							voisisNS.recorder.initStream().then(function () {
								// Attesa inizializzazione
								sleep(1000).then(() => {
									voisisNS.recorder.addEventListener( "dataAvailable", function(e){
										if(config.logLevel > 3){
											if(logSample == 10){
												dbgout.log("+++++++ AUDIO DICT +++++++ recorder");
												logSample = 0;
											}
											else{
												logSample += 1;
											}
										}
										var dataBlob = new Blob( [e.detail], { type: 'audio/'+config.audioFormat } );
										socketSend(voisisNS.wsAudio, dataBlob);
										config.onEvent(MSG_RECORDING, 'On data available');
									});
									if (typeof voisisNS.recorder.setShowVumeter == 'function'){
										voisisNS.recorder.setShowVumeter(true);
										voisisNS.recorder.start();
										voisisNS.recorder.stop();
									}
								});
							});
						}
						catch (e)
						{
							dbgout.log("ERROR: VoisisDictate-Init-EXC");
						}
					}
				}
				else{
					var constraints = [];
					if (localStorage.getItem('voisis-ausettings-'+user)){
						var auSettings = JSON.parse(localStorage.getItem('voisis-ausettings-'+user));
						if(auSettings.hasOwnProperty('agc'))
							constraints['autoGainControl'] = auSettings['agc'];
						if(auSettings.hasOwnProperty('echo'))
							constraints['echoCancellation'] = auSettings['echo'];
						if(auSettings.hasOwnProperty('noise'))
							constraints['noiseSuppression'] = auSettings['noise'];
					}
					else{
						constraints['autoGainControl'] = false;
					}

					navigator.mediaDevices.getUserMedia({
						audio: constraints	
					}).then(function(stream) {
						gumStream = stream;
						if(document.getElementById('voisis-gain-value')) {
							createVumeterNode(stream, 'ogg');
						}
						else{
							voisisNS.recorder = new MediaRecorder(stream);
							var ctx = new AudioContext();
							var mic = ctx.createMediaStreamSource(stream);	
						}

						voisisNS.recorder.ondataavailable = function(e) {
							if(config.logLevel > 3){
								if(logSample == 1500){
									dbgout.log("+++++++ AUDIO DICT +++++++ mediaRecorder");
									logSample = 0;
								}
								else{
									logSample += 1;
								}
							}
							chunks = [];
							chunks.push(e.data);
							var dataBlob = new Blob(chunks, { 'type' : 'audio/ogg; codecs=opus' } );				
							if(voisisNS.wsAudio)
								socketSend(voisisNS.wsAudio, dataBlob);
						};

						voisisNS.recorder.onerror = function(e) {
							dbgout.log("ERROR: VoisisDictate-Error MediaRec "+e.error.name);
						};

					});
				}
			}

			config.onEvent(MSG_INIT_RECORDER, 'Recorder initialized');

			try 
			{
				if(!voisisNS.wsSign && config.vcSignAddr){
					if(!localAsr)
						voisisNS.wsSign = voisisNS.wsAudio; // Msg di sign inviati su socket audio
					else
						voisisNS.wsSign = createSignWebSocket(user, vocab);
				}
			} catch (e) {
				config.onError(ERR_CLIENT, "No web socket support in this browser!");
			};	

			if(config.logLevel > 1)
				dbgout.log("VoisisDictate-Init-END");
		};

		this.close = function() {	
			if(config.logLevel > 1)
				dbgout.log("VoisisDictate-Close");

			socketSend(voisisNS.wsSign, JSON.stringify({
				cmd: "close"
			}));			
		};

		this.setLog = function(size) {
			if(config.logLevel > 1)
				dbgout.log("VoisisDictate-SetLog");

			if(voisisNS.wsSign.readyState == 1){
				socketSend(voisisNS.wsSign, JSON.stringify({
					cmd: "setLog",
					size: size,
					path: "clientLog"
				}));
				return true;
			}
			else{
				return false;
			}
		};		


		this.audioWizard = function() {
			if(config.logLevel > 1)
				dbgout.log("VoisisDictate-AudioWizard");

			socketSend(voisisNS.wsSign, JSON.stringify({
				cmd: "audiowizard",
				user: config.userId
			}));	
		};

		this.synchUser = function() {	
			if(config.logLevel > 1)
				dbgout.log("VoisisDictate-SynchUser");

			socketSend(voisisNS.wsSign, JSON.stringify({
				cmd: "synchUser",
				user: config.userId,
				synchPath: '//'+window.location.host+'/VoisisData'
			}));	
		};

		this.addWord = function(newWord, newSpForm){
			if(config.logLevel > 1)
				dbgout.log("VoisisDictate-AddWord");

			socketSend(voisisNS.wsSign, JSON.stringify({
				cmd: "addWord",
				user: config.userId,
				vocab: config.vocabId,
				word: newWord,
				spForm: newSpForm
			}));	
		};

		this.startListening = function(clientId, groupId) {

			if(config.logLevel > 1)
				dbgout.log("VoisisDictate-StartListening");

			// Prima mandiamo il nome dell'utente e l'id del client
			config.clientId = clientId;

			if(config.localAsr && !config.localWs){
				socketSend(voisisNS.wsSign, JSON.stringify({
					cmd: "start",
					user: config.userId,
					vocab: config.vocabId,
					client: config.clientId
				}));
			}
			else{
				if(config.audioFormat == "ogg"){
					if (! voisisNS.recorder) {
						config.onError(ERR_AUDIO, "Recorder undefined");
						return;
					}
				}

				if (voisisNS.wsAudio) {
					voisisNS.wsAudio.close();
					voisisNS.wsAudio = null;
				}

				try {
					voisisNS.wsAudio = createWebSocket(groupId);
				} catch (e) {
					config.onError(ERR_CLIENT, "No web socket support in this browser!");
				}
			}
		};

		// Stop listening, i.e. recording and sending of new input.
		this.stopListening = function() {

			if(config.logLevel > 1)
				dbgout.log("VoisisDictate-StopListening");

			if(config.audioFormat == "wav"){
				clearInterval(intervalKey);
				// Stop recording
				if (recorder) {
					recorder.stop();
					config.onEvent(MSG_STOP, 'Stopped recording');
					// Push the remaining audio to the server
					recorder.export16kMono(function(blob) {
						socketSend(voisisNS.wsAudio, blob);
						socketSend(voisisNS.wsAudio, TAG_END_OF_SENTENCE);
						recorder.clear();
					}, 'audio/x-raw');
					config.onEndOfSpeech();
				} else {
					config.onError(ERR_AUDIO, "Recorder undefined");
				}
			}
			else if(config.audioFormat == "ogg"){
				if (voisisNS.recorder && voisisNS.recorder.state != 'inactive') {
					voisisNS.recorder.stop();
					config.onEvent(MSG_STOP, 'Stopped recording');
					config.onEndOfSpeech();
				} else {
					config.onError(ERR_AUDIO, "Recorder undefined");
				}				
			}	

			if (voisisNS.wsAudio) {
				voisisNS.wsAudio.close();
				voisisNS.wsAudio = null;
			}
			else if(voisisNS.wsSign){
				socketSend(voisisNS.wsSign, JSON.stringify({
					cmd: "stop",
					user: config.userId,
					vocab: config.vocabId,
					client: config.clientId
				}));			
			}
		};

		this.changeUserVocab = function(userId, vocabId, clientId) {
			config.userId = userId;
			config.vocabId = vocabId;
			config.clientId = clientId;
		};

		// Cancel everything without waiting on the server
		this.cancel = function() {
			if(config.logLevel > 1)
				dbgout.log("VoisisDictate-Cancel");

			if(config.audioFormat == "wav"){
				clearInterval(intervalKey);
				if (recorder) {
					recorder.stop();
					recorder.clear();
					config.onEvent(MSG_STOP, 'Stopped recording');
				}
			}
			else if(config.audioFormat == "ogg"){
				if (voisisNS.recorder) {
					voisisNS.recorder.stop();
					config.onEvent(MSG_STOP, 'Stopped recording');
				}			
			}
			if (voisisNS.wsAudio) {
				voisisNS.wsAudio.close();
				voisisNS.wsAudio = null;
			}
		};

		// Sets the URL of the speech server
		this.setServer = function(server) {
			config.server = server;
			config.onEvent(MSG_SERVER_CHANGED, 'Server changed: ' + server);
		};

		// Sets the URL of the speech server status server
		this.setServerStatus = function(serverStatus) {
			config.serverStatus = serverStatus;
			if (config.onServerStatus) {
				monitorServerStatus();
			}
			config.onEvent(MSG_SERVER_CHANGED, 'Server status server changed: ' + serverStatus);
		};	

		function sleep (time) {
			return new Promise((resolve) => setTimeout(resolve, time));
		}

		function socketSend(ws, item) {
			if (ws) {
				var state = ws.readyState;
				if (state == 1) {
					// If item is an audio blob
					if (item instanceof Blob) {
						if (item.size > 0) {
							ws.send(item);
							config.onEvent(MSG_SEND, 'Send: blob: ' + item.type + ', ' + item.size);
						} else {
							config.onEvent(MSG_SEND_EMPTY, 'Send: blob: ' + item.type + ', EMPTY');

						}
						// Otherwise it's the EOS tag (string)
					} else {
						ws.send(item);
						config.onEvent(MSG_SEND_EOS, 'Send tag: ' + item);
					}
				} else {
					config.onError(ERR_NETWORK, 'WebSocket: readyState!=1: ' + state + ": failed to send: " + item);
					if(config.logLevel > 1)
						dbgout.log("VoisisDictate-SocketSend: state "+state);
				}
			} else {
				config.onError(ERR_CLIENT, 'No web socket connection: failed to send: ' + item);
				//dbgout.log("ERROR: VoisisDictate-SocketSend: NO conn ");
			}
		}

		this.SetTextInfo = function SetTextInfo(haveChanges, start, numChars, text, selStart, selNumChars, visibleStart, visibleNumChars)
		{
			if(config.logLevel > 1)
				dbgout.log("VoisisDictate-SetTextInfo: "+Date().substring(4, 25)+":"+start+":"+numChars+":"+selStart+":"+selNumChars+":"+visibleStart+":"+text.length+"<"+text+">");
			//dbgout.log("VoisisDictate-SetTextInfo: "+Date().substring(4, 25)+":"+start+":"+numChars+":"+selStart+":"+selNumChars+":"+visibleStart+":"+visibleNumChars+"<"+text+">");

			socketSend(voisisNS.wsSign, JSON.stringify({
				cmd: "GetChange",
				HaveChanges: haveChanges,
				NumChars: numChars,
				SelNumChars: selNumChars,
				SelStart: selStart,
				Start: start,
				Text: text,
				VisibleNumChars: text.length+1, //visibleNumChars,
				VisibleStart: visibleStart
			}));

			return true;
		};


		function createSignWebSocket(){

			if(config.logLevel > 1)
				dbgout.log("VoisisDictate-CreateCMDSck");

			var userFeatures = parseInt($('#userFeatures').val());
			if(userFeatures & 4)
				return true;

			var url = config.vcSignAddr;	
			voisisNS.wsSign = new WebSocket(url);
			voisisNS.wsSign.onopen = function(e){
				language = config.vocabId.substr(0, 2);
				socketSend(voisisNS.wsSign, JSON.stringify({
					cmd: "open",
					user: config.userId,
					vocab: config.vocabId,
					model: config.model,
					language: language
				}));
			}

			voisisNS.wsSign.onmessage = function(e) {
				var data = e.data;
				var message = JSON.parse(data);
				if(message.res){
					if(message.res == 'ok')
						config.onCompleted(data);
					else
						config.onError("ServerError", message.errorDetail);
				}
				else if (message.text){
					if(message.final == 'true')
						config.onResults(message.text);
					else{
						config.onPartialResults(message.text);
					}
				}
			};

			voisisNS.wsSign.onclose = function(e) {
				if(config.logLevel > 1)
					dbgout.log("VoisisDictate-CloseCMDSck");
			};

			voisisNS.wsSign.onerror = function(e) {
				dbgout.log("ERROR: VoisisDictate-ErrorCMDSck");
				if((config.vcSignAddr.indexOf("localhost") >= 0) || (config.vcSignAddr.indexOf("127.0.0.1") >= 0)){
					config.onError("ServerError", "L'applicazione Voisis non &egrave; avviata o non risponde");
				}
				else{
					config.onError("ServerError", "Il server di trascrizione non risponde");
				}
			};

			return voisisNS.wsSign;			
		}

		function createWebSocket(groupId) {
			if(config.logLevel > 1)
				dbgout.log("VoisisDictate-CreateAUSck");

			var url = config.server + '?user='+config.userId+'&vocab='+config.vocabId+'&client='+config.clientId;

			/// PATCH VCloud prod
			if((config.server == "wss://vc02b.voisis.it:443") && (config.srvTech == 'v'))
				url = 'wss://vcloud3.voisis.it:443?user='+config.userId+'&vocab='+config.vocabId+'&client='+config.clientId;
			//////

			if(groupId > 0)
				url += '&group='+groupId;
			if(!voisisNS.wsAudio){
				voisisNS.wsAudio = new WebSocket(url);

				voisisNS.wsAudio.onmessage = function(e) {
					var data = e.data;
					if(config.srvTech == 'v'){
						var res = JSON.parse(data);
						if (res.hasOwnProperty('text')){
							if(res.final == 'true')
								config.onResults(res.text.replaceAll("§", '"'));
							else{
								config.onPartialResults(res.text);
							}
						}
						else{
							if(res.status == 11)
								config.onError("ServerError", "ServerBusy");	
							else if(res.status == 12)
								config.onError("ServerError", "UnknownGroup");	
							else if(res.status == 13)
								config.onError("ServerError", "NoChannels");	
							else if(res.status == 14)
								config.onError("ServerError", "UserOverlap");	
							else if(res.event == "timeout")
								config.onDictTimeout();
						}
					}else{
						config.onEvent(MSG_WEB_SOCKET, data);
						if (data instanceof Object && ! (data instanceof Blob)) {
							config.onError(ERR_SERVER, 'WebSocket: onEvent: got Object that is not a Blob');
						} else if (data instanceof Blob) {
							config.onError(ERR_SERVER, 'WebSocket: got Blob');
						} else {
							var res = JSON.parse(data);
							if (res.status == 0) {
								if (res.result) {
									if(res.SelNumChar > 0){							
										config.onSelResults(res.SelStar,res.SelNumChar);
									}							
									else if (res.result.final) {
										config.onResults(res.result.hypotheses);
									}							
									else {
										config.onPartialResults(res.result.hypotheses);
									}
								}
								else{
									config.onError(ERR_SERVER, res.status);	
								}
							} else {
								if(res.status == 11)
									config.onError("ServerError", "ServerBusy");	
								else if(res.status == 12)
									config.onError("ServerError", "UnknownGroup");	
								else if(res.status == 13)
									config.onError("ServerError", "NoChannels");	
								else if(res.status == 14)
									config.onError("ServerError", "UserOverlap");	
								else if(res.event == "timeout")
									config.onDictTimeout();
							}
						}
					}
				};

				voisisNS.wsAudio.onopen = function(e) {

					var msg = JSON.stringify({ cmd: "start", user: config.userId, vocab: config.vocabId, client: config.clientId, logLevel: config.logLevel });
					socketSend(voisisNS.wsAudio, msg);

					if(config.audioFormat == "wav"){
						intervalKey = setInterval(function() {
							voisisNS.recorder.export16kMono(function(blob) {
								socketSend(voisisNS.wsAudio, blob);
								voisisNS.recorder.clear();
							}, 'audio/x-raw');
						}, config.interval);
						voisisNS.recorder.record();
					}
					else if(config.audioFormat == "ogg"){
						if(config.micSet == "aurec"){
							voisisNS.recorder.start();
						}					
						else{
							if(voisisNS.recorder.state != "recording"){
								voisisNS.recorder.start(100);
							}
						}
					}

					config.onReadyForSpeech();
					config.onEvent(MSG_WEB_SOCKET_OPEN, e);
				};

				voisisNS.wsAudio.onclose = function(e) {
					if((config.audioFormat == "wav") || (config.audioFormat == "ogg")){
						if(config.micSet == "aurec"){
							voisisNS.recorder.stop();
						}
					}
					config.onEndOfSession();
					config.onEvent(MSG_WEB_SOCKET_CLOSE, e.code + "/" + e.reason + "/" + e.wasClean);
					if(config.logLevel > 1)
						dbgout.log("VoisisDictate-CloseAUSck");
				};

				voisisNS.wsAudio.onerror = function(e) {
					var data = e.data;
					config.onError(ERR_NETWORK, data);
					dbgout.log("ERROR: VoisisDictate-ErrorAUSck");
				};
			}

			return voisisNS.wsAudio;
		}

		function monitorServerStatus() {
			try
			{
				if (wsAudioServerStatus) {
					wsAudioServerStatus.close();
				}

				wsAudioServerStatus = new WebSocket(config.serverStatus);
				wsAudioServerStatus.onmessage = function(evt) {
					config.onServerStatus(JSON.parse(evt.data));
				};
			}
			catch(err)
			{

			}
		}

		function getDescription(code) {
			if (code in SERVER_STATUS_CODE) {
				return SERVER_STATUS_CODE[code];
			}
			return "Unknown error";
		}
	};

	// Simple class for persisting the transcription.
	var Transcription = function(cfg) {
		var index = 0;
		var list = [];

		this.add = function(text, isFinal) {
			list[index] = text;
		};

		this.toString = function() {
			return list[index];
		};
	};

	window.Dictate = Dictate;
	window.Transcription = Transcription;

})(window);
