void decl_0001(char);
void (decl_0002)(char);
void ((decl_0003))(char);

void *decl_0004(char);
void (*decl_0005)(char);
void (*(decl_0006))(char);
void ((*decl_0007))(char);

typedef void decl_0011(char);
typedef void (decl_0012)(char);
typedef void ((decl_0013))(char);

typedef void *decl_0014(char);
typedef void (*decl_0015)(char);
typedef void (*(decl_0016))(char);
typedef void ((*decl_0017))(char);

typedef void decl_0021(char);
void (*decl_0022)(char);
void (*(*decl_0023(int a)))(char) { return &decl_0021; }
void (*(*(*((decl_0024)))(int))(float))(char);

int (*decl_0031)(char(*yyy)(bool));
