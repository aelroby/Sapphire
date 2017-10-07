$(document).ready(function() {
	$.post("AutoComplete");
});

$(document).click(function(){
	$(".form-control").autocomplete({
        source: "AutoComplete",
        minLength: 2,
        select: function(event, ui) {
            
        }
    });
});

$(function() {
    $(".form-control").autocomplete({
        source: "AutoComplete",
        minLength: 2,
        select: function(event, ui) {
            
        }
    });
});
