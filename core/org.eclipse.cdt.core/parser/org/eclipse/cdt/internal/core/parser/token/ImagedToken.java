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

import org.eclipse.cdt.internal.core.parser.scanner.ContextStack;
import org.eclipse.cdt.internal.core.parser.scanner.IScannerContext;

/**
 * @author johnc
 */
public class ImagedToken extends SimpleToken {

	protected char [] image = null;
	
	/**
	 * @param t
	 * @param contextStack
	 * @param i
	 */
	public ImagedToken(int t, ContextStack contextStack, char[] i, char [] f) {
		super(t, contextStack, f );
		setImage( i );
		setOffsetAndLength(contextStack.getCurrentContext());
	}
	
	public ImagedToken( int t, char[] i, int endOffset, char [] f ) {
		super( t, 0, f  );
		setImage(i);
		setOffsetAndLength( endOffset );
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.token.AbstractToken#getImage()
	 */
	public final String getImage() {
		if( image == null ) return null;
		return new String( image );
	}
	
	public final char[] getCharImage() {
		return image;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IToken#setImage(java.lang.String)
	 */
	public void setImage(String i) {
		image = i.toCharArray();
	}
	
	public void setImage( char [] image )
	{
		this.image = image;
	}
	
	/**
	 * @param context
	 */
	protected void setOffsetAndLength(IScannerContext context) {
		if( getImage() == null ) return;
		offset = context.getOffset() - getImage().length();		
		if( getType() == tSTRING || getType() == tCHAR )
			offset--;
		else if( getType() == tLSTRING || getType() == tLCHAR )
			offset -= 2;
	}
	
	public int getLength() {
		if( getImage() == null )
			return 0;
		switch( getType() )
		{
			case tSTRING:
			case tCHAR:
				return getImage().length() + 2;  // 'c' is 3 characters, not 1
			case tLSTRING:
			case tLCHAR:
				return getImage().length() + 3;  // L"X" if 4 characters, not 1
			default:
				return getImage().length();
		}
	}
}
