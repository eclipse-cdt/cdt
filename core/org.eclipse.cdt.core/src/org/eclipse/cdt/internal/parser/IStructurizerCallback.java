package org.eclipse.cdt.internal.parser;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public interface IStructurizerCallback {
		
	void includeDecl(String name, int startPos, int endPos, int startLine, int endLine);
	
	void defineDecl(String name, int startPos, int endPos, int startLine, int endLine);
	
	void functionDeclBegin(String name, int nameStartPos, int nameEndPos, int declStartPos, int startLine, int kind, int modifiers);
	void functionDeclEnd(int declEndPos, int endLine, boolean prototype);
	
	void fieldDecl(String name, int nameStartPos, int nameEndPos, int declStartPos, int declEndPos, int startLine, int endLine, int modifiers);
	
	void structDeclBegin(String name, int kind, int nameStartPos, int nameEndPos, int declStartPos, int startLine, int modifiers);
	void structDeclEnd(int declEndPos, int endLine);
	
	void superDecl(String name);

	void reportError(Throwable throwable);

}
