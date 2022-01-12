
int foo(int firstarg, bool secondarg) {
	bool firstvar = true;
	int secondvar = 18;
	int* ptrvar = &secondvar;
	int var = 1;
	int var2 = 2;
	
    return 0;
}

int testArrayMatching() {
    int array[20];
    int arrayInt[10];
    bool arrayBool[20];
    int arrayNot=1,array2=2,array3=3;
    
    array[0] = 20;
    array[1] = 21;
    array[2] = 22;
    array[3] = 23;
    array[4] = 24;
    array[5] = 25;
    return 0;
}

int main (int argc, char *argv[])
{
	int intvar = 80;
    bool boolvar = true;
    char chararray[201];
    
    foo(15, true);
    testArrayMatching();
    return 0;
}

