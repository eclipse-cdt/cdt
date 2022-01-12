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

// p1 is a particular type of problem binding as it
// has no corresponding declarator
void KnRfunctionWithProblemParameters(p1,p2,c)
   long p2;
   int c;
{

}

