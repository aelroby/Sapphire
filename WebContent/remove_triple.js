function removeTriple(formGroup){
    	
	if(accurateCounter == 1){
		alert("Cannot delete this triple. Minimum number of triples reached!");
		return;
	}
	var parentGroup = document.getElementById(formGroup);
	parentGroup.removeChild(parentGroup.childNodes[parentGroup.childNodes.length - 5]);
	accurateCounter--;
	
}


function removeOneTriple(formGroup, i){
	
	var parentGroup = document.getElementById(formGroup);
	parentGroup.removeChild(document.getElementById('triple-'+i));
	accurateCounter--;
}
