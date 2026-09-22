function getPrevChar(editorInstance, skipEmpty) {

	if(typeof(Quill) !== 'undefined'){
		var cursor = 0;
		var range = editorInstance.getSelection();
		if(!range)
			return null;
		cursor = range.index-1;
		if(skipEmpty){
			while(cursor > 0){
				var char = editorInstance.getText(cursor, 1);
				if((char.trim() != '') && (char.trim().charCodeAt(0) != 8203))
					return char;
				cursor--;
			}
			return null;
		}
		else{			
			return editorInstance.getText(cursor, 1);
		}
	}
	else{
		var range = editorInstance.getSelection().getRanges()[ 0 ],
		startNode = range.startContainer;

		if ( startNode.type == CKEDITOR.NODE_TEXT && range.startOffset ){
			if(!skipEmpty){
				// Range at the non-zero position of a text node.
				return startNode.getText()[ range.startOffset - 1 ];
			}
			else{
				return startNode.getText().trim().slice(-1);
			}
		}
		else {
			// Expand the range to the beginning of editable.
			range.collapse( true );
			range.setStartAt( editorInstance.editable(), CKEDITOR.POSITION_AFTER_START );

			// Let's use the walker to find the closes (previous) text node.
			var walker = new CKEDITOR.dom.walker( range ),
			node;

			while ( ( node = walker.previous() ) ) {
				// If found, return the last character of the text node.yy
				if ( node.type == CKEDITOR.NODE_TEXT ){
					if(!skipEmpty)
						return node.getText().slice( -1 );         
					else{
						if((node.getText().trim() != "") && (node.getText().trim().charCodeAt(0) != 8203))
							return node.getText().trim().slice( -1 );         
					} 
				}
			}
		}

		// Selection starts at the 0 index of the text node and/or there's no previous text node in contents.
		return null;
	}
}

function startNewPar(editorInstance){

	if((typeof(CKEDITOR) !== 'undefined') && (editorInstance.container instanceof CKEDITOR.dom.element)){
		try {
			var range = editorInstance.getSelection().getRanges()[0];
			var startNode = range.startContainer;
			if (startNode.type == CKEDITOR.NODE_TEXT)
				return false;
			range.collapse( true );
			range.setStartAt( editorInstance.editable(), CKEDITOR.POSITION_AFTER_START );
			var walker = new CKEDITOR.dom.walker( range ), node;
			node = walker.previous();
			if (node.getName() == 'p')
				return true;
		} catch(e) {
		}
	}
	return false;
}

function getAliasList(){
	voisisNS.ppAliases = [];
	$.get(voisisNS.sysUrl+'/vocab.php?alias=list'+'&user='+voisisNS.userId, function( data ) {
		$.each(data, function(index) {
			voisisNS.ppAliases.push(data[index].alias);
		});	
	})
	.fail(function( jqXHR, textStatus, errorThrown ) {   			
	});
}

function checkCapAlias(text){
	var firstWord = text.split(" ")[0];
	if((firstWord != firstWord.toLowerCase()) && voisisNS.ppAliases.includes(firstWord))
		return true;
	return false;
}

function KPostProc(){
	
	var punct = [".", ",", ":", ";", "!", "?", ")", "-"];
	var prevEnd = ["(", "-", "=", "/"];		
	var spaceBefore = false;
	var itnText = "";
    
	getAliasList();
	
	this.itn = function(editorInstance, text, enterMode, userClick, logLevel){
    		
		spaceBefore = false;

		if(voisisNS.aliasDirty){
			getAliasList();
			voisisNS.aliasDirty = false;
		}

		// Rimuoviamo eventuali spazi prima e dopo.
		text = text.trim();		
		var span1 = document.createElement('span');
		span1.innerHTML = text;
		var currText = span1.textContent || span1.innerText;
	     	serverPrompt = {op: "pp"};
		var date = new Date();
		serverPrompt.date = date.getHours()+":"+date.getMinutes()+":"+date.getSeconds();
			
		// Verifica se la prima parola è un alias con lettere maiusc.
		var isCapAlias = checkCapAlias(text);
			
		// Se c'e' del testo, e la utt. precedente non finiva con alcuni caratteri
		// e quella attuale non inizia con un segno di punteggiatura, premettiamo uno spazio    					   					
		var lastPrevChar = getPrevChar(editorInstance, false);
		if(logLevel >= voisisNS.pluginLog.TEXT)
			dbgout.log("Voisis-kpp PREVCHAR:"+lastPrevChar);
		if(!lastPrevChar)
			serverPrompt.prevCh = "[null]";
		else
			serverPrompt.prevCh = lastPrevChar+"["+lastPrevChar.charCodeAt(0)+"]";

		if((lastPrevChar == null || voisisLib.config.firstChar)){
			if(!isCapAlias)
				text = text.charAt(0).toUpperCase() + text.slice(1);
		}
		else if((lastPrevChar == '') && (enterMode == '')){
			// Con enterMode == '' gli autotext inseriti in ckEditor non stanno dentro un node del DOM
			spaceBefore = true;
		}		
		else if(((enterMode == 'br') || (lastPrevChar !="")) &&
			(lastPrevChar != '\n') && 
			(lastPrevChar.charCodeAt(0) != 32) && 
			(lastPrevChar.charCodeAt(0) != 160) &&
			((lastPrevChar.charCodeAt(0) != 8203) || (enterMode == 'br')) && 
			(punct.indexOf(currText[0]) == -1) && 
			(prevEnd.indexOf(lastPrevChar) == -1) && 
			!voisisLib.config.newLine && 
			!voisisLib.config.setText){
			spaceBefore = true;
			// Annulliamo se a inizio paragrafo
			if((enterMode == 'p') && startNewPar(editorInstance)){
				spaceBefore = false;
			}
			else {
				if(logLevel >= voisisNS.pluginLog.TEXT)
					dbgout.log("Voisis-kpp SPACEBEFORE-1")	
				serverPrompt.space = "bef1";
			}
		}			
		else if(punct.indexOf(lastPrevChar) !== -1 && (userClick || !voisisLib.config.newLine)){
			spaceBefore = true;
			if(logLevel >= voisisNS.pluginLog.TEXT)
				dbgout.log("Voisis-kpp SPACEBEFORE-2")	
			serverPrompt.space = "bef2";
		}
						

		if (typeof voisisLib.config.insAT === 'undefined')
			voisisLib.config.insAT = false;

		var lastFillChar = getPrevChar(editorInstance, true);
		//if(!isCapAlias && ((voisisLib.config.newLine && !userClick) || 
		if(!isCapAlias && !voisisLib.config.insAT &&((voisisLib.config.newLine && !userClick) || 
			(lastPrevChar == '.') || (lastFillChar == '.') ||
			(lastPrevChar == '!') || (lastFillChar == '!') ||
			(lastPrevChar == '?') || (lastFillChar == '?')))
			text = text.charAt(0).toUpperCase() + text.slice(1); 

		if(voisisLib.config.insAT){
			voisisLib.config.insAT = false;
			userClick = false;
		}

		if(voisisLib.config.setText){
			if(!isCapAlias)
				text = text.charAt(0).toUpperCase() + text.slice(1);
			voisisLib.config.setText = false;
			spaceBefore = false;
		}
		
		itnText = text;

		if(logLevel >= voisisNS.pluginLog.TEXT)
			voisisNS.jsonLog.push(serverPrompt);
	};
	
	this.filtered = function(){
		return filter;
	};
	
	this.testFiltered = function(){
		return testFilter;
	};
	
	this.isSpaceBefore = function(){
		return spaceBefore;
	};
	
	this.itnText = function(){
		return itnText;
	};
    
};
    
