struct blah {
	int lala;
};

struct blahblah{
	struct blah first;
};


struct SimpleCStructure {
	char scsa; 
};

struct CStructure1 {
	int cs1a;
	double cs1b;
	struct CStructure2 {
		float cs2a;
		struct CStructure3 {
			char cs3a;
		} cs2b;
	} cs1c;
};

union CUnion1 {
	int cu1a;
	double cu1b;
	char cu1c;
	union CUnion2 {
		int cu2a;		
	} cu1d;
};

struct CMixedS1 {
	union CMixedU1 {
		struct CMixedS2 {
			double cms2a;
		} cmu1a;
		union CMixedU2 {
			int cmu2a;
		} cmu1b;
		float cmu1c;
	} cms1a;
	struct CMixedS3 {
		char cms3a;
	} cms1b;
};

int main() {
	struct SimpleCStructure css;
	struct SimpleCStructure *cpss;
	
	css.scsa = 0;
	cpss->scsa = 1;
	
	struct CStructure1 cs1;
	struct CStructure1 *cps1 = &cs1;
	
	cs1.cs1a = 0;
	cps1->cs1a = 1;
	
	cs1.cs1b = 2;
	cs1.cs1b = 3;
	cps1->cs1b = 4;
	
	struct CStructure2 cs2;
	struct CStructure2 *cps2 = &cs2;
	
	cs1.cs1c = cs2;
	cs1.cs1c = cs2;
	cs1.cs1c = cs2;
	cps1->cs1c = cs2;

	struct CStructure3 cs3;
	struct CStructure3 *cps3 = &cs3;
		
	cs1.cs1c.cs2b = 9;
	cs1.cs1c.cs2b = 10;
	cs1.cs1c.cs2b = 11;
	cps1->cs1c.cs2b = 12;
	cps2->cs2b = 13;
	
	cs1.cs1c.cs2b.cs3a = 13; 
	cs1.cs1c.cs2b.cs3a = 14; 
	cs1.cs1c.cs2b.cs3a = 15; 
	cs1.cs1c.cs2b.cs3a = 16; 
	cs1.cs1c.cs2b.cs3a = 17; 
	cps1->cs1c.cs2b.cs3a = 18;
	cps2->cs2b.cs3a = 19;
	cps3->cs3a = 19; 

	union CUnion1 cu1;
	union CUnion1 *cpu1 = &cu1;
	
	cu1.cu1a = 0;
	cpu1->cu1a = 1;
	
	union CUnion2 cu2;
	union CUnion2 *cpu2 = &cu1.cu1d;
	
	cu2.cu2a = 2;
	cpu2->cu2a = 3;
	
	union CMixedU1 cmu1;
	union CMixedU1 *cpmu1 = &cmu1;
	
	cmu1.cmu1a;
	cpmu1->cmu1a;
	
	union CMixedU2 cmu2;
	union CMixedU2 *cpmu2 = &cmu2;
	
	cmu2.cmu2a;
	cpmu2->cmu2a;
	
	struct CMixedS2 cms2;
	struct CMixedS2 *cpms2 = &cms2;
	
	cms2.cms2a;
	cpms2->cms2a;
	
	struct CMixedS3 cms3;
	struct CMixedS3 *cpms3 = &cms3;
	
	cms3.cms3a;
	cpms3->cms3a;
	
	return 0;
}
