#pragma once

#include <map>

typedef  map<ULONG, IUnknown *> MUL2IUnk;
typedef pair <ULONG, IUnknown *> MUL2IUnk_Pair;

class CDocumentManager
{
public:
	CDocumentManager(void);
	~CDocumentManager(void);

private:
	MUL2IUnk _controlMap;

public:
	void addToControlMap(UINT threadID, IUnknown * theControl);
	void getControl(ULONG threadID, IUnknown ** theControl);
};
