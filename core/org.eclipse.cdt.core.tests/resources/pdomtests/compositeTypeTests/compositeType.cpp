struct SimpleStructure {
	char ssa; 
};

struct Structure1 {
	int s1a;
	double s1b;
	struct Structure2 {
		float s2a;
		struct Structure3 {
			char s3a;
		} s2b;
	} s1c;
};

union Union1 {
	int u1a;
	double u1b;
	char u1c;
	union Union2 {
		int u2a;		
	} u1d;
};

struct MixedS1 {
	union MixedU1 {
		struct MixedS2 {
			double ms2a;
		} mu1a;
		union MixedU2 {
			int mu2a;
		} mu1b;
		float mu1c;
	} ms1a;
	struct MixedS3 {
		char ms3a;
	} ms1b;
}

int main() {
	struct SimpleStructure ss;
	struct SimpleStructure *pss;
	
	ss.ssa = 0;
	pss->ssa = 1;
	
	struct Structure1 s1;
	struct Structure1 *ps1 = &s1;
	
	s1.s1a = 0;
	ps1->s1a = 1;
	
	s1.s1b = 2;
	s1.s1b = 3;
	ps1->s1b = 4;
	
	struct Structure1::Structure2 s2;
	struct Structure1::Structure2 *ps2 = &s2;
	
	s1.s1c = s2;
	s1.s1c = s2;
	s1.s1c = s2;
	ps1->s1c = s2;

	struct Structure1::Structure2::Structure3 s3;
	struct Structure1::Structure2::Structure3 *ps3 = &s3;
		
	s1.s1c.s2b = 9;
	s1.s1c.s2b = 10;
	s1.s1c.s2b = 11;
	ps1->s1c.s2b = 12;
	ps2->s2b = 13;
	
	s1.s1c.s2b.s3a = 13; 
	s1.s1c.s2b.s3a = 14; 
	s1.s1c.s2b.s3a = 15; 
	s1.s1c.s2b.s3a = 16; 
	s1.s1c.s2b.s3a = 17; 
	ps1->s1c.s2b.s3a = 18;
	ps2->s2b.s3a = 19;
	ps3->s3a = 19; 

	Union1 u1;
	Union1 *pu1 = &u1;
	
	u1.u1a = 0;
	pu1->u1a = 1;
	
	Union1::Union2 u2;
	Union1::Union2 *pu2 = &u1.u1d;
	
	u2.u2a = 2;
	pu2->u2a = 3;
	
	MixedS1::MixedU1 mu1;
	MixedS1::MixedU1 *pmu1 = &mu1;
	
	mu1.mu1a;
	pmu1->mu1a;
	
	MixedS1::MixedU1::MixedU2 mu2;
	MixedS1::MixedU1::MixedU2 *pmu2 = &mu2;
	
	mu2.mu2a;
	pmu2->mu2a;
	
	MixedS1::MixedU1::MixedS2 ms2;
	MixedS1::MixedU1::MixedS2 *pms2 = &ms2;
	
	ms2.ms2a;
	pms2->ms2a;
	
	MixedS1::MixedS3 ms3;
	MixedS1::MixedS3 *pms3 = &ms3;
	
	ms3.ms3a;
	pms3->ms3a;
	
	return 0;
}
