void forwardDeclaration();

void normalDeclaration1() {
	forwardDeclaration();
}

void normalDeclaration2() {
	normalDeclaration1();
}

void forwardDeclaration() {
	normalDeclaration2();
}

void spin() {
	normalDeclaration1();
	normalDeclaration2();
	normalDeclaration2();
	forwardDeclaration();
}

int (*int2intPtr)(int);

	
