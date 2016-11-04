var lastSubmitInput= [];
var variables = new Set();
var dataSet = [];
var myTable;

/**
 * This function ensures that the set called variables include the most recent list of 
 * variables in the query.
 * 
 */
function updateVariables(){
	var allInputs = document.getElementsByTagName("input");
	variables.clear();
	
	
	// find missing variables
	for(var i =0; i<allInputs.length;i++)
		if(allInputs[i].value.startsWith('?') && allInputs[i].id!="mySelect"){
				variables.add(allInputs[i].value);
		}
			
	
	
	// constract select input
//	console.log("before replace: "+document.getElementById("mySelect").value);
	document.getElementById("mySelect").value = 
		document.getElementById("mySelect").value.replace(/(^|\W)\?(\w+)/g, '');
//	console.log("after replace: "+document.getElementById("mySelect").value);
	
	variables.forEach(function(item) {
//		console.log("Add to select statement: "+item);
		document.getElementById("mySelect").value = document.getElementById("mySelect").value.concat(" ",item);
		});
	
//	console.log("after fix: "+document.getElementById("mySelect").value);
		
}


function copyArray(oldA, newA){
	
	oldA = [newA.length];
	
	for(var i =0; i<newA.length;i++)
		oldA[i]= newA[i].value;
	
	updateVariables();
	
	return oldA;
}

/**
 * This function was made to validate the contents of the query 
 * in the client side. 
 * 
 * However, it is disabled now until we agree on all constrains.
 * 
 * @returns {Boolean}
 */
function validateQuery(){
	
	var allInputs = document.getElementsByTagName("input");
	
	console.log("current input collected, old length = " + lastSubmitInput.length);
	
	// If all are variables --> Don't run query (This will take forever)
	var allVariables = true;
	for(var itr = 0; itr < allInputs.length; ++itr) {
		if(allInputs[itr].value.substring(0,1) != "?") {
			allVariables = false;
			break;
		}
	}
	if(allVariables) {
		return false;
	}
	console.log("allVariables = " + allVariables);
	
	// If length is different --> different query
	if(lastSubmitInput.length != allInputs.length){
		lastSubmitInput = copyArray(lastSubmitInput, allInputs);
		console.log("last Submit Input updated-1");
		return true;
	}
	
	// Length can be the same but different values
	var differentValues = false;
	for(var x=0; x<allInputs.length; x++){
		console.log("checking " + lastSubmitInput[x] +" ? " + allInputs[x].value);
		if(lastSubmitInput[x] != allInputs[x].value)
			differentValues = true;
	}

	if(differentValues == true){
		lastSubmitInput = copyArray(lastSubmitInput, allInputs);
		console.log("last Submit Input updated-2");
		return true;
	}
	else{
		console.log("same input, do not run queries");
		return false;
	}

	// first input is the select input
	// I am checking the object and predicate only
	// object should have "<some text>"@en format
	// predicate should be surrounded by < and >
	//
	for(var x=1; x<allInputs.length; x=x+3){
   
		// fix the date objects
		if(allInputs[x+2].type=="date"){
			allInputs[x+2].type="input";
			allInputs[x+2].value="\""+allInputs[x+2].value+"\"^^xsd:date";
		}
			
			
		var check = true;
		// objective
		check = check & new RegExp("\"@en"+"$").test(allInputs[x+2].value);
		check = check & allInputs[x+2].value.slice(0, 1) == "\"";
		
		check = check & allInputs[x+1].value.slice(0, 1) == "<";
		check = check & allInputs[x+1].value.slice(-1) == ">";

		return check;
	}
	
	
}


function changeInput2Date(id){
	
	// temporarely disable this feature
	return;
	
	console.log(document.getElementById("predicate-"+id).value);
	console.log(document.getElementById("predicate-"+id).value.toLowerCase());
	console.log(document.getElementById("predicate-"+id).value.toLowerCase().indexOf("date"));
	
	if(document.getElementById("predicate-"+id).value.toLowerCase().indexOf("date") > -1){  
		document.getElementById("object-"+id).type = "date";
		document.getElementById("object-"+id).value = "";
	}
	else
		document.getElementById("object-"+id).type = "input";
}


function UpdateTriple(f,t,s,p,o,newV){

	
	console.log("updateTriple="+ t +"-"+ s +"-"+ p +"-"+ o +"-"+ newV);
	
    var allInputs = document.getElementsByTagName("input");
    console.log(allInputs.length);
    for(var x=1;x<allInputs.length;x=x+3)
        if(allInputs[x].value == s && allInputs[x+1].value == p && allInputs[x+2].value == o){
        	console.log("triple-FOUND");
        	if(t=='O')
        		allInputs[x+2].value = newV;
        	else if (t=='P')
        		allInputs[x+1].value = newV;
        	else 
        		allInputs[x].value = newV;
        }
}

function validate(f,id){
	var valid = true;
	if( document.getElementById("subject-"+id).value == '' 	|| document.getElementById("subject-"+id).value == 'Subject'	||
		document.getElementById("predicate-"+id).value == '' || document.getElementById("predicate-"+id).value == 'Predicate' ||
		document.getElementById("object-"+id).value == '' || document.getElementById("object-"+id).value == 'Object')
	{
		valid = false;
	}
	return valid;
}



function callQuery(){


	var header = "<thead><tr><th>Query Results:</th></tr></thead>";
	var findingSuggestionsRow = "<tbody><tr><td><i>Please wait, we are trying to find relevant results!</i></td></tr></tbody>"
		
    $("#answers_table").empty();
	$("#answers_table").append(header + findingSuggestionsRow);
	
	
	dataString = $("#id_form").serialize();
    $.post("MainServlet", dataString, function(responseText){
    	
    	$("#answers_table").empty();
    	var responseJSON = JSON.parse(responseText);
    	// Table Header
    	var headerVariables = responseJSON['head']['vars'];

    	
    	
    	var header = "<thead><tr>";
    	for(var i = 0; i < headerVariables.length; i++){
    		header += "<th>" + headerVariables[i] + "</th>";
    	}
    	header += "</tr></thead>";
    	
    	

    	dataSet = []
    	
    	
    	// Table Body
    	var bodyTuples = responseJSON['results']['bindings'];
    	var body = "<tbody>";
    	for(var i = 0; i < bodyTuples.length; i++){
    		
    		// row data
        	rowData = new Array(headerVariables.length);
    		
        	var record = "<tr>";
    		for(var j = 0; j < headerVariables.length; j++){
    			if(bodyTuples[i][headerVariables[j]]['type'] == "uri"){
    				record += "<td><a href=\"" + bodyTuples[i][headerVariables[j]]['value'] + "\" target=\"_blank\">" + bodyTuples[i][headerVariables[j]]['value'] + "</a></td>";
    				rowData.push("<a href=\"" + bodyTuples[i][headerVariables[j]]['value'] + "\" target=\"_blank\">" + bodyTuples[i][headerVariables[j]]['value'] + "</a>");
    				
    			}
    			else{
    				record += "<td>" + bodyTuples[i][headerVariables[j]]['value'] + "</td>";
    				rowData.push(bodyTuples[i][headerVariables[j]]['value']);
    			}
    				
    			
    		}
    		record += "</tr>";
    		body += record;
    		
    		dataSet.push(rowData);
    	}
    	body += "</tbody>";

    	
        if ( $.fn.dataTable.isDataTable( '#answers_table' ) ) {
        	myTable.destroy();
        	//table.find('thead tr th').remove();
        }

    	$("#answers_table").empty();
    	$('#answers_table').find('thead tr th').remove();
    	$("#answers_table").append(header + body);


//    console.log("---------------------------------------");	
//    console.log(dataSet);
    
    myTable = $('#answers_table').DataTable( {
        "scrollY": "200px",
        "paging": false,
        colReorder: true,
        dom: 'Bfrtip',
        select: true,
        buttons: [
			{ extend: "colvis", text: "show/hide Columns" },
                  'print',
                  'copy',
                  'pdf',
                  'csv',
                  'excel'
                  ] 
//        data: dataSet,
//        columns: headerVariables
    } );
    
    
    
    
    
 
} );


}



function getSuggestions(){

	// Table Header    	
	var header = "<thead><tr><th>Query Suggestions:</th></tr></thead>";
	var findingSuggestionsRow = "<tbody><tr><td><i>Please wait, we are trying to find alternative queries!</i></td></tr></tbody>"
		
    $("#suggestions_table").empty();
	$("#suggestions_table").append(header + findingSuggestionsRow);
    
    
	dataString = $("#id_form").serialize();
	$.post("AlternativeQueryFinder", dataString, function(responseText){
    $("#suggestions_table").empty();
    var json = JSON.stringify(eval("(" + responseText + ")"));
    var responseJSON = JSON.parse(json);
    	

  
    	// Table Body
    	var bodyTuples = responseJSON['results']['suggestions'];
    	var body = "<tbody>";
    	for(var i = 0; i < bodyTuples.length; i++){
    		var record = "<tr><td>";
    		var myType = bodyTuples[i]['type'];
    		var mySubject = bodyTuples[i]['subject'];
    		var myPredicate = bodyTuples[i]['predicate'];
    		var myObject = bodyTuples[i]['object'];
    		var myNewValue = bodyTuples[i]['newValue'];
    		var myCount = bodyTuples[i]['resultCount'];
	
    		record += "In triple: <code>"+mySubject+"&nbsp;&nbsp;"+
    			myPredicate.replace("<","&lt;").replace(">","&gt;")+
    			"&nbsp;&nbsp;"+myObject+"</code>:<br>"+
    			"&nbsp;&nbsp;&nbsp;&nbsp;Did you mean <b>"+myNewValue.replace("<","&lt;").replace(">","&gt;")+"</b>, instead of <i>";
    			if(myType=='O')
    				record +=myObject;
    			else if (myType=='P')
    				record +=myPredicate.replace("<","&lt;").replace(">","&gt;");
    			else 
    				record +=mySubject;

    			record +="</i>. There are <b><u>"+myCount+"</u></b> results available?&nbsp;&nbsp;&nbsp;&nbsp;";
    		
    		// The global replace trick was taken from the below URL, more complicated solutions are also available:
    		// http://stackoverflow.com/questions/1144783/replacing-all-occurrences-of-a-string-in-javascript	
    		
    		// double qoutes cannot be escaped in onClick actions. The best way to write double qoutes in HTML is using &quot;
    		// Reference: http://stackoverflow.com/questions/1081573/escaping-double-quotes-in-javascript-onclick-event-handler
    		record += "<a class=\"updateLink\"  onclick=\"UpdateTriple(this.form,'"+myType+"','"+mySubject+"','"+myPredicate+"','"+
    			myObject.replace(/\"/g, '&quot;')+"','"+myNewValue.replace(/\"/g, '&quot;')+"'); callQuery(); getSuggestions();\">Update Query</a>";
    		
    		record += "</td></tr>";
    		body += record;
    	}
    	body += "</tbody>";
    	
$("#suggestions_table").append(header + body);

console.log(header + body);

});
}
