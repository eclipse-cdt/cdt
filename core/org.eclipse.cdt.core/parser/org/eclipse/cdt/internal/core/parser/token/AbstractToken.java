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

import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.ITokenDuple;
import org.eclipse.cdt.core.parser.ast.IReferenceManager;
import org.eclipse.cdt.internal.core.parser.scanner2.CharArrayUtils;


/**
 * @author johnc
 */
public abstract class AbstractToken implements IToken, ITokenDuple {

	private final char[] filename;

	public AbstractToken( int type, int lineNumber, char [] filename )
	{
		setType( type );
		this.lineNumber = lineNumber;
		this.filename = filename;
	}

	public AbstractToken( int type, char [] filename, int lineNumber  )
	{
		setType( type );
		this.filename = filename;
		this.lineNumber = lineNumber;
	}
	
	public String toString() {
		return getImage();
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IToken#getFilename()
	 */
	public char[] getFilename() {
		return filename;
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
		if( !CharArrayUtils.equals( ((IToken)other).getCharImage(), getCharImage() ) ) 
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ITokenDuple#contains(org.eclipse.cdt.core.parser.ITokenDuple)
	 */
	public boolean contains(ITokenDuple duple) {
		return ( duple.getFirstToken() == duple.getLastToken() ) && ( duple.getFirstToken() == this );
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ITokenDuple#extractNameFromTemplateId()
	 */
	public char[] extractNameFromTemplateId(){
		return getCharImage();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ITokenDuple#findLastTokenType(int)
	 */
	public int findLastTokenType(int t) {
		if( getType() == t ) return 0;
		return -1;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ITokenDuple#getFirstToken()
	 */
	public IToken getFirstToken() {
		return this;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ITokenDuple#getLastSegment()
	 */
	public ITokenDuple getLastSegment() {
		return this;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ITokenDuple#getLastToken()
	 */
	public IToken getLastToken() {
		return this;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ITokenDuple#getLeadingSegments()
	 */
	public ITokenDuple getLeadingSegments() {
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ITokenDuple#getSegmentCount()
	 */
	public int getSegmentCount() {
		return 1;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ITokenDuple#getStartOffset()
	 */
	public int getStartOffset() {
		return getOffset();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ITokenDuple#getSubrange(int, int)
	 */
	public ITokenDuple getSubrange(int startIndex, int endIndex) {
		if( startIndex == 0 && endIndex == 0 ) return this;
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ITokenDuple#getTemplateIdArgLists()
	 */
	public List[] getTemplateIdArgLists() {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ITokenDuple#getToken(int)
	 */
	public IToken getToken(int index) {
		if( index == 0 ) return this;
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ITokenDuple#isIdentifier()
	 */
	public boolean isIdentifier() {
		return ( getType() == IToken.tIDENTIFIER );
	}
	
	
	private class SingleIterator implements Iterator
	{
		boolean hasNext = true;
		/* (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			throw new UnsupportedOperationException();
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			return hasNext;
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		public Object next() {
			hasNext = false;
			return AbstractToken.this;
		}
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ITokenDuple#iterator()
	 */
	public Iterator iterator() {
		return new SingleIterator();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ITokenDuple#length()
	 */
	public int length() {
		return 1;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ITokenDuple#syntaxOfName()
	 */
	public boolean syntaxOfName() {
		return ( getType() == IToken.tIDENTIFIER );
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ITokenDuple#toQualifiedName()
	 */
	public String[] toQualifiedName() {
		String [] qualifiedName = new String[1];
		qualifiedName[0] = getImage();
		return qualifiedName;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ITokenDuple#freeReferences(org.eclipse.cdt.core.parser.ast.IReferenceManager)
	 */
	public void freeReferences(IReferenceManager manager) {
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ITokenDuple#acceptElement(org.eclipse.cdt.core.parser.ast.IReferenceManager)
	 */
	public void acceptElement(ISourceElementRequestor requestor, IReferenceManager manager) {
	}
}
