package org.eclipse.cdt.internal.parser;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.parser.generated.Token;

public final class ParserCallback {
	
	public final static int K_CLASS= ICElement.C_CLASS;
	public final static int K_STRUCT= ICElement.C_STRUCT;
	public final static int K_UNION= ICElement.C_UNION;	
	public final static int K_FUNCTION= ICElement.C_FUNCTION;
	public final static int K_DECL= ICElement.C_FUNCTION_DECLARATION;
	public final static int K_CTOR= ICElement.C_CLASS_CTOR;
	public final static int K_DTOR= ICElement.C_CLASS_DTOR;
	public final static int K_STATIC= ICElement.C_STORAGE_STATIC;
	public final static int K_EXTERN= ICElement.C_STORAGE_EXTERN;
		
	private LinePositionInputStream fLinePositions;
	private IStructurizerCallback fCallback;
	private int fStorage;
	
	public ParserCallback(LinePositionInputStream lpiStream, IStructurizerCallback callback) {
		fLinePositions= lpiStream;
		fCallback= callback;
	}
	
	public void functionDeclBegin(Token nameToken, Token firstToken, int kind) {
		int declStart= fLinePositions.getPosition(firstToken.beginLine, firstToken.beginColumn);
		int nameStart= fLinePositions.getPosition(nameToken.beginLine, nameToken.beginColumn);
		int nameEnd= fLinePositions.getPosition(nameToken.endLine, nameToken.endColumn);			
		
		fCallback.functionDeclBegin(nameToken.image, nameStart, nameEnd, declStart,
			firstToken.beginLine, kind, fStorage);
		fStorage = 0;
	}
	
	public void functionDeclEnd(Token lastToken) {
		int declEnd= fLinePositions.getPosition(lastToken.endLine, lastToken.endColumn);
		boolean prototype = ";".equals(lastToken.image);
		
		fCallback.functionDeclEnd(declEnd, lastToken.endLine, prototype);
	}	
	
	public void structDeclBegin(Token nameToken, int kind, Token firstToken) {
		int declStart= fLinePositions.getPosition(firstToken.beginLine, firstToken.beginColumn);
		int nameStart= fLinePositions.getPosition(nameToken.beginLine, nameToken.beginColumn);
		int nameEnd= fLinePositions.getPosition(nameToken.endLine, nameToken.endColumn);		
		
		fCallback.structDeclBegin(nameToken.image, kind, nameStart, nameEnd, declStart, firstToken.beginLine, fStorage);
		fStorage = 0;
	}
	
	public void structDeclEnd(Token lastToken) {
		int declEnd= fLinePositions.getPosition(lastToken.endLine, lastToken.endColumn);
		
		fCallback.structDeclEnd(declEnd, lastToken.endLine);
	}
	
	public void fieldDecl(Token nameToken, Token firstToken, Token lastToken) {
		int declStart= fLinePositions.getPosition(firstToken.beginLine, firstToken.beginColumn);
		int declEnd= fLinePositions.getPosition(lastToken.endLine, lastToken.endColumn);
		int nameStart= fLinePositions.getPosition(nameToken.beginLine, nameToken.beginColumn);
		int nameEnd= fLinePositions.getPosition(nameToken.endLine, nameToken.endColumn);
				
		fCallback.fieldDecl(nameToken.image, nameStart, nameEnd, declStart, declEnd,
			firstToken.beginLine, lastToken.endLine, fStorage);
		fStorage = 0;
	}		
	
	public void superDecl(String name) {
		fCallback.superDecl(name);
	}
	
	public void includeDecl(String name, int line, int column) {
		int start= fLinePositions.getPosition(line, column);
		int end= fLinePositions.getPosition(line, column + name.length()) - 1;
		fCallback.includeDecl(name, start, end, line, line);
	}
	
	public void defineDecl(String name, int line, int column) {
		int start= fLinePositions.getPosition(line, column);
		int end= fLinePositions.getPosition(line, column + name.length()) - 1;
		fCallback.defineDecl(name, start, end, line, line);
	}
	
	public void storageSpecifier(int kind) {
		fStorage |= kind;
	}
		
	public boolean isStorageClassSpecifier(Token token) {
		String str= token.image;
		if (str != null) {
			if ("JNIEXPORT".equals(str)) {
				return true;
			}
			if (str.startsWith("__declspec")) {
				return true;
			}
			if ("JNICALL".equals(str)) {
				return true;
			}			
		}
		return false;
	}

	public boolean overreadBlocks() {
		return true;
	}
	
	// ---- util functions -----
	public static Token createToken(String name, Token positions) {
		Token res= new Token();
		res.image= name;
		res.beginColumn= positions.beginColumn;
		res.beginLine= positions.beginLine;
		res.endColumn= positions.endColumn;
		res.endLine= positions.endLine;
		return res;
	}
	
	public static Token createToken(String name, Token positionBegin, Token positionEnd) {
		Token res= new Token();
		res.image= name;
		res.beginColumn= positionBegin.beginColumn;
		res.beginLine= positionBegin.beginLine;
		res.endColumn= positionEnd.endColumn;
		res.endLine= positionEnd.endLine;
		return res;
	}	
}
