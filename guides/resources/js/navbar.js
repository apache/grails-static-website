function toggleNavIcon() {
	if ( document.getElementById('nav-icon').className === "open" ) {
		document.getElementById('nav-icon').className = "";	
		document.getElementById('grailsnavbar-nav').classList.add("closemobile");
	} else {
		document.getElementById('nav-icon').className += "open";	
		document.getElementById('grailsnavbar-nav').classList.remove("closemobile");
	}
}
