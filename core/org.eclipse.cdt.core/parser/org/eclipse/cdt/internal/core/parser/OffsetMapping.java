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

import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;



/**
 * @author jcamelon
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class OffsetMapping  {

	public OffsetMapping()
	{
	}
	
	public void newLine( int offset )
	{
		lastOffset = offset;
		store.put( new Integer( offset ), new Integer( ++lineCounter ) );
	}
	
	public void recantLastNewLine()
	{
		if( store.remove( new Integer( lastOffset ) ) != null ) 
		{
			--lineCounter;
			lastOffset = -1;
		}
	}
	
	public int getLineNo( int offset )
	{
		Iterator iter = store.keySet().iterator();
		int first = -1, second = -1; 
		if( ! iter.hasNext() ) return 1; 
		first = ((Integer)iter.next()).intValue();
		if( ( offset <= first ) ||  ! iter.hasNext() ) 
			return ((Integer)store.get( new Integer( first ))).intValue();

		while( true )
		{
			second = ((Integer)iter.next()).intValue();
			if( offset > first && offset <= second )
				return ((Integer)store.get( new Integer( second ))).intValue();
			if( ! iter.hasNext() ) break;
			first = second; 
		}
		
		return lineCounter;
	}
	
	public int getCurrentLineNumber()
	{
		return lineCounter;
	}
	
	private int lineCounter = 1;
	private int lastOffset = -1;  
	private SortedMap store = new TreeMap();
}
