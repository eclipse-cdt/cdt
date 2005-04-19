/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.core.parser.token2;

import org.eclipse.cdt.core.parser.IToken;

public class ImagedToken2 extends SimpleToken2 {

    protected char [] image = null;
    
    public ImagedToken2( int t, char[] i, int endOffset, int l ) {
        super( t, 0, l );
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
        int s_length = getCharImage().length;
         switch( getType() )
         {
         case IToken.tSTRING:
            return s_length + 2;
         case IToken.tLSTRING:
            return s_length + 3;
         default:
            return s_length;
         }
    }
}
