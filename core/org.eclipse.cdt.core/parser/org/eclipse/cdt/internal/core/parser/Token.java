/*******************************************************************************
 * Copyright (c) 2001 Rational Software Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     Rational Software - initial implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.core.parser;

import org.eclipse.cdt.core.parser.IScannerContext;
import org.eclipse.cdt.core.parser.IToken;

public class Token implements IToken {

	public Token(int t, String i, IScannerContext context ) {
		type = t;
		image = i;
		filename = context.getFilename();
		offset = context.getOffset() - image.length() - context.undoStackSize();
		
		if( type == tLSTRING || type == tSTRING || type == tCHAR ){
			offset--;
		}
	}
	
	public Token(int t, String i) {
		type = t;
		image = i;
	}
	
	public String toString()
	{
		return "Token type=" + type + "  image =" + image + " offset=" + offset; 	
	}
	
	public int type;
	public int getType() { return type; }
	
	public String image;
	public String getImage() { return image; }

	public  String filename;
	public int offset;
	public int getOffset() { return offset; }
	public int getLength() { return image.length(); }
	public int getEndOffset() { return getOffset() + getLength(); }
	
	
	public int getDelta( IToken other )
	{
		return other.getOffset() + other.getLength() - getOffset();
	}
	
	private Token next;
	public Token getNext() { return next; }
	public void setNext(Token t) { next = t; }
	
	public boolean looksLikeExpression()
	{
		switch( getType() )
		{
			case tINTEGER:
			case t_false:
			case t_true:
			case tSTRING:
			case tLSTRING:
			case tFLOATINGPT:
			case tCHAR:
			case tAMPER:
			case tDOT:
			case tLPAREN:
				return true;
			default:
				break;
		}
	
		
		return false;
	}
	
	public boolean isPointer()
	{
		return (getType() == tAMPER || getType() == tSTAR);
	}
	
	public boolean isOperator()
	{
		switch( getType() )
		{
			case IToken.t_new:
			case IToken.t_delete:
			case IToken.tPLUS:
			case IToken.tMINUS:
			case IToken.tSTAR:
			case IToken.tDIV:
			case IToken.tXOR:
			case IToken.tMOD:
			case IToken.tAMPER:
			case IToken.tBITOR:
			case IToken.tCOMPL:
			case IToken.tNOT:
			case IToken.tASSIGN:
			case IToken.tLT:
			case IToken.tGT:
			case IToken.tPLUSASSIGN:
			case IToken.tMINUSASSIGN:
			case IToken.tSTARASSIGN:
			case IToken.tDIVASSIGN:
			case IToken.tMODASSIGN:
			case IToken.tBITORASSIGN:
			case IToken.tAMPERASSIGN:
			case IToken.tXORASSIGN:
			case IToken.tSHIFTL:
			case IToken.tSHIFTR:
			case IToken.tSHIFTLASSIGN:
			case IToken.tSHIFTRASSIGN:
			case IToken.tEQUAL:
			case IToken.tNOTEQUAL:
			case IToken.tLTEQUAL:
			case IToken.tGTEQUAL:
			case IToken.tAND:
			case IToken.tOR:
			case IToken.tINCR:
			case IToken.tDECR:
			case IToken.tCOMMA:
			case IToken.tARROW:
			case IToken.tARROWSTAR:
				return true;
			default:
				return false;
		}
	}

}
