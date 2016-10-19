var counter = 1;
var accurateCounter = 1;
var limit = 10;
function addTriple(formGroup, addButtonGroup){
    if (accurateCounter == limit)  {
        alert("You have reached the limit of adding " + accurateCounter + " inputs");
    }
    else {
    	
    	var formGroupNewTriple = document.createElement('div');
    	formGroupNewTriple.setAttribute('class','form-group');
    	formGroupNewTriple.setAttribute('id','triple-'+counter);
    	
    	// Indentation 
        var indentationDiv = document.createElement('div');
        indentationDiv.setAttribute('class','col-sm-1');
        formGroupNewTriple.appendChild(indentationDiv);
        
        // Subject
        var subjectDiv = document.createElement('div');
        subjectDiv.setAttribute('class', 'col-sm-3');
        subjectDiv.innerHTML = '<input type="text" id="subject-'+counter+'" onBlur="if(validate(this.form,'+counter+') && validateQuery()) {callQuery(); getSuggestions();}"  class="form-control" placeholder="Subject" name="myInputs[]">';
        formGroupNewTriple.appendChild(subjectDiv);
        
        // Predicate
        var predicateDiv = document.createElement('div');
        predicateDiv.setAttribute('class', 'col-sm-5');
        predicateDiv.innerHTML = '<input type="text"  id="predicate-'+counter+'" onBlur="if(validate(this.form,'+counter+') && validateQuery()) {callQuery(); getSuggestions();} changeInput2Date(0);"   class="form-control" placeholder="Subject" name="myInputs[]">';
        formGroupNewTriple.appendChild(predicateDiv);
        
        // Object
        var objectDiv = document.createElement('div');
        objectDiv.setAttribute('class', 'col-sm-2');
        objectDiv.innerHTML = '<input type="text"  id="object-'+counter+'" onBlur="if(validate(this.form,'+counter+') && validateQuery()) {callQuery(); getSuggestions();}"  class="form-control" placeholder="Subject" name="myInputs[]">';
        formGroupNewTriple.appendChild(objectDiv);

/*
        // Object
        var object2Div = document.createElement('div');
        object2Div.setAttribute('class', 'col-sm-3');
        object2Div.innerHTML = '<input type="text"  id="object2-'+counter+'" onBlur="if(validate(this.form,'+counter+') && validateQuery()) {callQuery(); getSuggestions();}"  class="form-control" placeholder="Subject" name="myInputs[]">';
        formGroupNewTriple.appendChild(object2Div);
*/
        
        // Remove
        var removeDiv = document.createElement('div');
        removeDiv.setAttribute('class', 'col-sm-1');
        removeDiv.innerHTML = '<i class="fa fa-3x fa-fw -square text-primary fa-minus-square" onclick="removeOneTriple(\'id_form\','+counter+');"></i>'
        formGroupNewTriple.appendChild(removeDiv);

        
        
        
        var parentGroup = document.getElementById(formGroup);
        parentGroup.insertBefore(formGroupNewTriple, document.getElementById(addButtonGroup));
    	
        counter++;
        accurateCounter++;
    }
}
