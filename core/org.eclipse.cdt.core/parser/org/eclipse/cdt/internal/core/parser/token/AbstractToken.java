/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.token;

import org.eclipse.cdt.core.parser.IToken;


/**
 * @author johnc
 */
public abstract class AbstractToken implements IToken {

	public AbstractToken( int type, int lineNumber )
	{
		setType( type );
		this.lineNumber = lineNumber;
	}

	public AbstractToken( int type )
	{
		setType( type );
	}
	
	public String toString() {
		return "Token=" + getType() + " \"" + getImage() + " @ line:" + getLineNumber() + " offset=" + getOffset(); 	 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
	
	public abstract String getImage();
	public abstract int getOffset();
	public abstract int getLength();
	
	public int getType() { return type; }

	public void setType(int i) {
		type = i;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public int getEndOffset() { return getOffset() + getLength(); }

	protected int type;
	protected int lineNumber = 1;
	protected IToken next = null;

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		if( other == null ) return false;
		if( !( other instanceof IToken ) ) 
			return false;
		if( ((IToken)other).getType() != getType() ) 
			return false;
		if( !(((IToken)other).getImage().equals( getImage() ))) 
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IToken#isKeyword()
	 */
	public boolean canBeAPrefix() {
		switch( getType() )
		{
			case tIDENTIFIER:
			case tCOMPL:
				return true;
			default:
				if( getType() >= t_and && getType() <= t_xor_eq ) return true;
				if( getType() >= t__Bool && getType() <= t_restrict ) return true;
		}
		return false;
	}

	public boolean looksLikeExpression()
	{
		switch( getType() )
		{
			case IToken.tINTEGER:
			case IToken.t_false:
			case IToken.t_true:
			case IToken.tSTRING:
			case IToken.tLSTRING:
			case IToken.tFLOATINGPT:
			case IToken.tCHAR:
			case IToken.tAMPER:
			case IToken.tDOT:
			case IToken.tLPAREN:
			case IToken.tMINUS:
			case IToken.tSTAR: 
			case IToken.tPLUS: 
			case IToken.tNOT:
			case IToken.tCOMPL:
				return true;
			default:
				break;
		}	
		return false;
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
	
	public boolean isPointer()
	{
		return (getType() == IToken.tAMPER || getType() == IToken.tSTAR);
	}



	public final IToken getNext() { return next; }
	public void setNext(IToken t) { next = t; }
	
}
