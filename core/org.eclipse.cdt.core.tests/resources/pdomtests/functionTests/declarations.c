void forwardCDeclaration();

void normalCDeclaration1() {
	forwardCDeclaration();
}

void normalCDeclaration2() {
	normalCDeclaration1();
}

void forwardCDeclaration() {
	normalCDeclaration2();
}

void spin() {
	normalCDeclaration1();
	normalCDeclaration2();
	normalCDeclaration2();
	forwardCDeclaration();
}
	
