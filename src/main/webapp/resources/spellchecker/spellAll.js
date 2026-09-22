var foundeoSpellCheckerURI = "../../resources/spellchecker/"; //must have trailing slash

function ActivSpellClass() {
	this.init();	
}

ActivSpellClass.prototype = new Object();
ActivSpellClass.prototype.init = function() {
	this.argsIndex = -1;
	this.fieldRefs = new Array();
	this.ignore = new Array();
	this.change = new Array();
	this.changeto = new Array();
	this.replacements = new Array();
}

ActivSpell = new ActivSpellClass();

function nextField() {

}

function spell() 
{	
	
	ActivSpell.init();

	if(document.all){ 
		var coll = document.all.tags("TEXTAREA"); 
	
		if (coll!=null) { 	
			for (i=0; i<coll.length; i++) { 				
				var docRef = coll[i].sourceIndex; 
				ActivSpell.fieldRefs[i] = "document.all(" + docRef +").value"; 
			} 
		}
	} else {
		// Mozilla/Gecko DOM? 
				
				// loop round all forms in doc 
				count = 0; 
				for( frm=0; frm < document.forms.length; frm++ ){ 
						// loop round each form element 	
						for( elem=0; elem < document.forms[frm].elements.length; elem++ ){ 					
							// if it's a textarea 							
							thisElem = document.forms[frm].elements[elem]; 					
							if( thisElem.tagName == "TEXTAREA" ){ 						
							ActivSpell.fieldRefs[count] = "document.forms[" + frm + "]." + thisElem.name + ".value"; 	
			
							count++; 
							}//end if 
						}//end for 
				}//end for 
	}//end else if

	
	nextField = function() {
		ActivSpell.argsIndex++;
		
		if(ActivSpell.argsIndex < ActivSpell.fieldRefs.length) {
		
			if (eval(ActivSpell.fieldRefs[ActivSpell.argsIndex]).length == 0) {
				nextField();
			}
		
			ActivSpellWin = window.open(foundeoSpellCheckerURI+"window.jsp?jsvar=" + ActivSpell.fieldRefs[ActivSpell.argsIndex], "ActivSpellWin", "height=230,width=450,status=no,toolbar=no,menubar=no,location=no");
		} else {
			spellCheckComplete();
		}
	}//end nextField function body
	
	//index ActivSpell.argsIndex
	ActivSpell.argsIndex++;
	
	//send the first field to spellcheck
	if (eval(ActivSpell.fieldRefs[ActivSpell.argsIndex]).length == 0) {

		nextField();
	} else {
		ActivSpellWin = window.open(foundeoSpellCheckerURI+"window.jsp?jsvar=" + ActivSpell.fieldRefs[0], "ActivSpellWin", "height=230,width=450,status=no,toolbar=no,menubar=no,location=no");	
	}

}//end spell

function spellCheckComplete() {
	ActivSpellWin.close();
	window.focus();
	alert("Spell Check Complete!");
	
	ActivSpell.argsIndex = -1;
	nextField = function() {}
	
}