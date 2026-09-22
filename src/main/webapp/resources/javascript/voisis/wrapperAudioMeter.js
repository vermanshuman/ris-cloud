var vmAudioContext = null;
var meter = null;
var canvasContext = null;
var WIDTH=35;
var HEIGHT=10;
var rafID = null;
var recorder;
var audio_stream;
var audioBuffer;

function onMicrophoneDenied() {
	alert('Stream generation failed.');
}

var mediaStreamSource = null;

function onMicrophoneGranted(stream) {
	
	audio_stream = stream;
	
	// Create an AudioNode from the stream.
	mediaStreamSource = vmAudioContext.createMediaStreamSource(stream);

	// Create a new volume meter and connect it.
	meter = createAudioMeter(vmAudioContext, 0.9, 0.4);
	mediaStreamSource.connect(meter);

	// kick off the visual updating
	onLevelChange();
	
	// Initialize the BufferRecorder Library
	recorder = new BufferRecorder(mediaStreamSource);

	// Start recording !
	recorder && recorder.record();
}

function onLevelChange( time ) {
	// clear the background
	canvasContext.clearRect(0,0,WIDTH,HEIGHT);

	// check if we're currently clipping
	if (meter.checkClipping())
		canvasContext.fillStyle = "red";
	else
		canvasContext.fillStyle = "green";

	// draw a bar based on the current volume
	canvasContext.fillRect(0, 0, meter.volume * WIDTH * 1.4, HEIGHT);

	// set up the next visual callback
	rafID = window.requestAnimationFrame( onLevelChange );
}

function initVmeter(canvasId, cwidth, cheight){
	// grab our canvas
	canvasContext = document.getElementById(canvasId).getContext("2d");
	WIDTH = cwidth;
	HEIGHT = cheight;

	// monkeypatch Web Audio
	window.AudioContext = window.AudioContext || window.webkitAudioContext;

	// grab an audio context
	vmAudioContext = getContext(); //new AudioContext();

	// Attempt to get audio input
	try {
		// monkeypatch getUserMedia
		navigator.getUserMedia = 
			navigator.getUserMedia ||
			navigator.webkitGetUserMedia ||
			navigator.mozGetUserMedia;

		// ask for an audio input
		navigator.getUserMedia(
				{
					"audio": {
						"mandatory": {
							"googEchoCancellation": "false",
							"googAutoGainControl": "false",
							"googNoiseSuppression": "false",
							"googHighpassFilter": "false"
						},
						"optional": []
					},
				}, onMicrophoneGranted, onMicrophoneDenied);
	} catch (e) {
		alert('getUserMedia threw exception :' + e);
	}
}

function stopVmeter(callback, AudioFormat){
	
	if((mediaStreamSource != null) && (meter != null)){
		mediaStreamSource.disconnect(meter);
	}
	
	// Stop the recorder instance
	recorder && recorder.stop();

	// Stop the getUserMedia Audio Stream !
	audio_stream.getAudioTracks()[0].stop();


	// Use the BufferRecorder Library to export the recorder Audio as a .wav file
	// The callback providen in the stop recording method receives the blob
	if(typeof(callback) == "function"){

		/**
		 * Export the AudioBLOB using the exportWAV method.
		 * Note that this method exports too with mp3 if
		 * you provide the second argument of the function
		 */
		recorder && recorder.exportWAV(function (blob) {
			
			var reader = new FileReader();
	    	reader.onload = function(event){	    	    
	    		audioBuffer = event.target.result;
	    	};
	    	reader.readAsDataURL(blob);
		
			callback(blob);

			// create WAV download link using audio data blob
			// createDownloadLink();

			// Clear the BufferRecorder to start again !
			recorder.clear();
		}, (AudioFormat || "audio/wav"));		
	}
}

function getAudioBuffer(callback){	
	if(typeof(callback) == "function"){
		callback(audioBuffer);
		audioBuffer = "";
	}
}

//Singleton
var getContext = function() {
	var ac = null;
	if ( !window.AudioContext && !window.webkitAudioContext ) {
		alert('Web Audio API not supported in this browser');
	} else {
		ac = new ( window.AudioContext || window.webkitAudioContext )();
	}
	return function() {
		return ac;
	};
}();
