/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser;

import java.io.IOException;
import java.io.Reader;

import org.eclipse.cdt.core.parser.ILineOffsetReconciler;
import org.eclipse.cdt.core.parser.IOffsetDuple;

/**
 * @author jcamelon
 *
 */
public class LineOffsetReconciler implements ILineOffsetReconciler
{
	private Reader ourReader; 
    /**
     * @param input
     */
    public LineOffsetReconciler(Reader input)
    {
        ourReader = input; 
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ILineOffsetReconciler#getLineNumberForOffset(int)
     */
    public int getLineNumberForOffset(int offset)
    {
        resetReader();
        int lineNumber = 1; 
        for( int i = 0; i < offset; ++i )
        {
            int c = getChar();
        	if( c == '\n' )
        		++lineNumber;
        }
        return lineNumber;
    }
    
    private int getChar()
    {
        int c;
        try
        {
           c = ourReader.read();
        }
        catch (IOException e)
        {
           throw new Error( "Could not read");
               
        }
        return c;
    }
    private void resetReader()
    {
        try
        {
            ourReader.reset();
        }
        catch (IOException e)
        {
            throw new Error( "Could not reset Reader" ); 
        } 
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ILineOffsetReconciler#getOffsetRangeForLineNumber(int)
     */
    public IOffsetDuple getOffsetRangeForLineNumber(int LineNumber)
    {
        int lineNumber = 1;
        int floor= -1, ceiling = -1; 
        int offset = 0; 
        while( lineNumber != LineNumber )
        {
        	int c = getChar();
        	++offset;  
        	if( c == '\n' ) ++lineNumber;
        }
        floor = offset; 
		while( lineNumber == LineNumber )
		{
			int c = getChar();
			++offset;  
			if( c == '\n' ) ++lineNumber;
		}        
        ceiling = offset;
        return new OffsetDuple( floor, ceiling );
    }
}
