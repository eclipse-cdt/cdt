/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner;

import java.util.EmptyStackException;
import java.util.Stack;

/**
 * @author jcamelon
 *
 * To change this generated comment edit the template variable 
"typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class BranchTracker {
	
	private static final int IGNORE_SENTINEL = -1;
	
	/**
	 * Default constructor.  
	 * 
	 * @see java.lang.Object#Object()
	 */
	public BranchTracker()
	{
	}
	
	private Stack branches = new Stack();
	
	private int ignore = IGNORE_SENTINEL;
	private static final Boolean FALSE = new Boolean( false );
	private static final Boolean TRUE = new Boolean( true );
	
	/**
	 * Method poundif. 
	 * 
	 * This method is called whenever one encounters a #if, #ifndef
	 * or #ifdef preprocessor directive.
	 * 
	 * @param	 taken		- boolean indicates whether or not the condition
	 * evaluates to true or false
	 * @return boolean		- are we set to continue scanning or not? 
	 */
	public boolean poundIf( boolean taken )
	{
		if( ignore == IGNORE_SENTINEL )
		{	
			// we are entering an if
			// push the taken value onto the stack
			branches.push( new Boolean( taken ) );
			
			if( taken == false )
			{					
				ignore = branches.size();  
			}
			
			return taken;
		}
		branches.push( FALSE ); 
		return false; 
	}	
	
	public boolean queryCurrentBranchForElif()
	{
		if( ignore != IGNORE_SENTINEL && ignore < branches.size() )
			return true;
		return !((Boolean)branches.peek()).booleanValue();
	}
	
	public boolean queryCurrentBranchForIf()
	{
		if( branches.isEmpty() ) return true;
		if( ignore != IGNORE_SENTINEL & ignore < branches.size() )
			return false; 
		return ((Boolean)branches.peek()).booleanValue();
	}
	
	public boolean poundElif( boolean taken ) throws EmptyStackException 
	{
		if( ignore != IGNORE_SENTINEL && ignore < branches.size() )
		{
			branches.pop(); 
			branches.push( FALSE ); 
			return false; 
		}
		
		// so at this point we are either 
		//		--> ignore == IGNORE_SENTINEL
		//		--> ignore >= branches.size()
		// check the branch queue to see whether or not the branch has already been taken 
		Boolean branchAlreadyTaken;
		branchAlreadyTaken = (Boolean) branches.peek();
		
		if( ignore == IGNORE_SENTINEL )
		{	
			if( ! branchAlreadyTaken.booleanValue() )
			{
				branches.pop(); 
				branches.push( new Boolean( taken ) );
				if( ! taken )
					ignore = branches.size();
					
				return taken;
			}
			
			// otherwise this section is to be ignored as well
			ignore = branches.size(); 
			return false;
		}
		
		// if we have gotten this far then ignore == branches.size()
		if( ! branchAlreadyTaken.booleanValue() )
		{
			branches.pop(); 
			branches.push( new Boolean( taken ) );
			if( taken )
				ignore = IGNORE_SENTINEL;
			
			return taken; 
		}
		ignore = branches.size(); 
		return false;
	}
	
	public boolean poundElse() throws EmptyStackException
	{
		if( ignore != IGNORE_SENTINEL && ignore < branches.size() )
		{
			branches.pop(); 
			branches.push( FALSE ); 
			return false; 
		}
				
		Boolean branchAlreadyTaken;
		branchAlreadyTaken = (Boolean) branches.peek();
				
		if( ignore == IGNORE_SENTINEL )
		{
			if( branchAlreadyTaken.booleanValue() )
			{
				ignore = branches.size();
				return false; 
			}
			
			branches.pop(); 
			branches.push( TRUE );
			return true;
			
		}
		
		// now ignore >= branches.size()
		if( branchAlreadyTaken.booleanValue() )
		{
			ignore = branches.size(); 
			return false;
		}
		
		branches.pop(); 
		branches.push( TRUE ); 
		ignore = IGNORE_SENTINEL; 
		return true;
		
	}
	
	// taken only on an #endif 
	public boolean poundEndif( ) throws EmptyStackException
	{
		if( ignore == branches.size() )
			ignore = IGNORE_SENTINEL;
		branches.pop();
		return ( ignore == IGNORE_SENTINEL );
	}
		
	public int getDepth()
	{
		return branches.size(); 
	}
}
