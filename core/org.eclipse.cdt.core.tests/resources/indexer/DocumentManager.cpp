//#include "StdAfx.h"
#include "DocumentManager.h"

CDocumentManager::CDocumentManager(void)
{
}

CDocumentManager::~CDocumentManager(void)
{
}

void CDocumentManager::addToControlMap(UINT threadID, IUnknown * theControl)
{
	_controlMap.insert(MUL2IUnk_Pair(threadID,theControl));
}

void CDocumentManager::getControl(ULONG threadID, IUnknown ** theControl)
{
	if (_controlMap.find(threadID) != _controlMap.end())
	{
		theControl = &_controlMap[threadID];
	}

	
}
