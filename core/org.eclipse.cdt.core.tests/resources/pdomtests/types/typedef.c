struct CA {
	int x;
};

typedef struct CA * CX;

int g() {
	CX x;
	return x->x;
}
