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


/**
 * @author johnc
 */
public class ImagedToken extends SimpleToken {

	protected char [] image = null;
	
	public ImagedToken( int t, char[] i, int endOffset, char [] f, int l ) {
		super( t, 0, f, l );
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
		
	public int getLength() {
		if( getCharImage() == null )
			return 0;
		return getCharImage().length;
	}
}
