(function(window){

	var cmdChunks = [];
	var gumStream = null;
	var vadSilence = true;
	var logSample = 0;
	var cmdMode = true;

	var recCommands = [ '<voisis>start-dictation</voisis>', '<voisis>send-text</voisis>', '<voisis>new-text</voisis>' ];
	var recTmpCommands = [ '+inizio+dettatura+', '+invia+referto+', '+nuovo+referto+' ];

	var cmdRecUrl;

	function searchCommand(commands, str){
		var commandIdx = -1;
		$.each(commands, function(key, value) {
			  if(str.indexOf(value) != -1){
				  commandIdx = key;
				  return false;
			  }
		});
		return commandIdx;
	}
	
	function commandLog(user, str){
		$.ajax({
			url: '/VoisisCloud/clientLogger.php',
			type: 'POST',
			data: {
				user: user,
				cmd: str
			},
		});
	}

	function sendVoice(tech, onSysCommands, user, logLevel, onCustCommands) {

		var fd = new FormData();
		var dataBlob = new Blob(cmdChunks, { 'type' : 'audio/ogg; codecs=opus' } );
		fd.append('data', dataBlob);

		if(tech == 'v'){
			fd.append('user', user);
			$.ajax({
				type: 'POST',
				url: cmdRecUrl+"?user="+user,
				data: fd,
				processData: false,
				crossDomain: true,
				global: false,     // this makes sure ajaxStart is not triggered
				contentType: false,
				success: function (response) {
					if(response['final'] && response['text'] && (response['text'].trim() != "")){
						var str = response['text'].trim();
						if(logLevel > 3)
							dbgout.log("Testo ricevuto: "+str);
                                                if(((commandIdx = searchCommand(recCommands, str)) != -1) || // voice2json
                                                   ((commandIdx = searchCommand(recTmpCommands, str)) != -1)){ //legacy older Vosk

							if(logLevel > 3){
								dbgout.log("Comando riconosciuto: "+str);
								commandLog(user, str);
							}
							var command = recCommands[commandIdx];
							onSysCommands(command);
						}
						else if(onCustCommands && str.startsWith("<voisis")){
							onCustCommands(str);
						}
					}
				},
				error: function (xhr, status) {
				}
			});
		}
		else{ // 'g'
			$.ajax({
				type: 'POST',
				url: cmdRecUrl,
				data: dataBlob,
				processData: false,
				crossDomain: true,
				global: false,     // this makes sure ajaxStart is not triggered
				success: function (response) {
					if(response['hypotheses'] && response['hypotheses'][0] && (response['hypotheses'][0]['utterance'] != "")){
						var str = response['hypotheses'][0]['utterance'].trim();
						if(logLevel > 3)
							dbgout.log("Testo ricevuto: "+str);
						if((commandIdx = searchCommand(recCommands, str)) != -1){
							if(logLevel > 3){
								dbgout.log("Comando riconosciuto: "+str);
								commandLog(user, str);
							}
							var command = recCommands[commandIdx];
							onSysCommands(command);
						}
						else if(onCustCommands && str.startsWith("<voisis>")){
							onCustCommands(str);
						}
					}
				},
				error: function (xhr, status) {
				}
			});
		}
	};

	var Command = function(){

		this.init = function(url, tech, onSysCommands, user, logLevel, onCustCommands=null){
			cmdRecUrl = url;
			var constraints = [];
			constraints['autoGainControl'] = false;
			navigator.mediaDevices.getUserMedia({
				audio: constraints 
			}).then(function(stream) {
				gumStream = stream;

				voisisNS.cmdRecorder = new MediaRecorder(stream);
				var ctx = new AudioContext();
				var mic = ctx.createMediaStreamSource(stream);	

				var vadTimeout;
				var options = {
						source: mic,
						voice_stop: function() {
							vadTimeout = setTimeout(function() {
								vadSilence=true;
							}, 300);
						},
						voice_start: function() {
							//clearTimeout(vadTimeout);
							vadSilence=false;
						},
						energy_threshold_ratio_pos: 3
				};

				var vad = new VAD(options);

				voisisNS.cmdRecorder.ondataavailable = function(e) {
					if(logLevel > 3){
						if(logSample == 1500){
							dbgout.log("+++++++ AUDIO CMD +++++++ mediaRecorder");
							logSample = 0;
						}
						else{
							logSample += 1;
						}
					}

					if(vadSilence){
						if(voisisNS.cmdRecorder.state == 'recording'){
							voisisNS.cmdRecorder.stop()
						}
						if(cmdChunks.length > 10){
							sendVoice(tech, onSysCommands, user, logLevel, onCustCommands);
						}
						cmdChunks = [];
						if(cmdMode)
							voisisNS.cmdRecorder.start(100);
					}
					else{
						cmdChunks.push(e.data);
					}
				};

				voisisNS.cmdRecorder.onerror = function(e) {
					dbgout.log("ERROR: VoisisCommands-Error MediaRec "+e.error.name);
				};

				voisisNS.cmdRecorder.start(100);
			})
		};

		this.start = function(){
			if(voisisNS.cmdRecorder.state != 'recording')
				voisisNS.cmdRecorder.start(100);
			cmdMode = true;
		};

		this.stop = function(){
			if(voisisNS.cmdRecorder.state == 'recording'){
				voisisNS.cmdRecorder.stop()
			}
			cmdMode = false;
		};

		this.changeUrl = function(url){
			cmdRecUrl = url;
		};

	};

	window.Command = Command;

})(window);
