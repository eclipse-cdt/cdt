/*******************************************************************************
 * Copyright (c) 2003,2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/
package org.eclipse.cdt.core.parser.tests;

import java.util.EmptyStackException;

import junit.framework.TestCase;

import org.eclipse.cdt.internal.core.parser.scanner.BranchTracker;

/**
 * @author jcamelon
 */
public class BranchTrackerTest extends TestCase {
	
	public BranchTrackerTest( String ignoreMe )
	{
		super( ignoreMe );
	}
	
	public static void assertFalse( boolean input )
	{
		assertTrue( input == false ); 
	}
	
	public void testIgnore()
	{
		 
		 BranchTracker bt = new BranchTracker(); 
		 try
		 {
			/*
			 * #if 0	
			 * #	if	1 
			 * #	elif 1 
			 * #	else 
			 * #	endif 
			 * #else 
			 * #endif
			 */

			assertFalse( bt.poundIf( false ) );  
			assertFalse( bt.poundIf( true ) ); 
			assertFalse( bt.poundElif( true ) );  
			assertFalse( bt.poundElse() ); 
			assertFalse( bt.poundEndif() );  
			assertTrue( bt.poundElse() ); 
			assertTrue( bt.poundEndif() ); 

			/*
			 * #if 0	
			 * #	if	1 
			 * #	elif 1 
			 * #	else 
			 * #	endif 
			 * #else
			 * #	if 0  
			 * #	elif 1
			 * #	elif 0
			 * #	elif 1
			 * #	else
			 * #	endif
			 * #endif
			 */
			 
			bt = new BranchTracker();
			assertFalse( bt.poundIf( false ) );
			assertFalse( bt.poundIf( true ));   
			assertFalse( bt.poundElif( true ) );  
			assertFalse( bt.poundElse() );  
			assertFalse( bt.poundEndif() );  
			assertTrue( bt.poundElse() );  
			assertFalse( bt.poundIf( false ) );  
			assertTrue( bt.poundElif( true ) );  
			assertFalse( bt.poundElif( false ) ); 
			assertFalse( bt.poundElif( true ) ); 
			assertFalse( bt.poundElse() ); 
			assertTrue( bt.poundEndif() ); 
			assertTrue( bt.poundEndif() ); 
			assertEquals( 0, bt.getDepth() ); 
			
			/*
			 * #if 0
			 * #	if 1
			 * #	elif 0
			 * #	elif 1
			 * #	else
			 * #	endif	
			 * #elif 0
			 * #	if 0
			 * #	elif 0
			 * #	elif 1
			 * #	else
			 * #	endif  
			 * #elif 1 
			 * #	if 0
			 * #	elif 0
			 * #	elif 0
			 * #	else
			 * #	endif
			 * #else
			 * #	if 1
			 * #	elif 0
			 * #	elif 1
			 * #	else
			 * #	endif 
			 * #endif
			 */
			 
			assertFalse(bt.poundIf(false));
				assertFalse(bt.poundIf(true));
				assertFalse(bt.poundElif(false));
				assertFalse(bt.poundElif(true));
				assertFalse(bt.poundElse());
				assertFalse( bt.poundEndif() ); 
			assertFalse(bt.poundElif(false));
				assertFalse(bt.poundIf(false));
				assertFalse(bt.poundElif(false));
				assertFalse(bt.poundElif(true));
				assertFalse(bt.poundElse());
				assertFalse( bt.poundEndif());
			assertTrue(bt.poundElif(true));
				assertFalse(bt.poundIf(false));
				assertFalse(bt.poundElif(false));
				assertFalse(bt.poundElif(false));
				assertTrue(bt.poundElse());
				assertTrue( bt.poundEndif() ); 
			assertFalse(bt.poundElse());
				assertFalse(bt.poundIf(true));
				assertFalse(bt.poundElif(false));
				assertFalse(bt.poundElif(true));
				assertFalse(bt.poundElse());
				assertFalse( bt.poundEndif() ); 
			assertTrue( bt.poundEndif() ); 
			assertEquals(0, bt.getDepth());
		} catch (EmptyStackException se) {
				fail("Unexpected Scanner exception thrown"); //$NON-NLS-1$
			}
		}
	
	public void testSimpleBranches()
	{
		try
		{
			/*
			 * code sequence is 
			 * #if 1 
			 * #else 
			 * #endif
			 */
			BranchTracker bt = new BranchTracker();
			assertTrue( bt.poundIf( true ) );  
			assertFalse( bt.poundElse() ); 
			assertTrue( bt.poundEndif() );  
			
			/*
			 * code sequence is
			 * #if 1
			 * #	if 0
			 * #	elif 0 
			 * #	else
			 * #	endif
			 * #else 
			 * #endif
			 */
			bt = new BranchTracker(); 
			assertTrue( bt.poundIf( true ));
			assertFalse( bt.poundIf( false ));  
			assertFalse( bt.poundElif( false ));
			assertTrue( bt.poundElse());
			assertTrue( bt.poundEndif() );  
			assertFalse( bt.poundElse() ); 
			assertTrue( bt.poundEndif() ); 
			
			/*
			 *	#if 1
			 *	#elsif 1
			 *	#elsif 0
			 *	#else
			 *	#endif 
			 */
			 
			bt  = new BranchTracker(); 
			assertTrue( bt.poundIf( true ) ); 
			assertFalse( bt.poundElif( true )); 
			assertFalse( bt.poundElif( false )); 
			assertFalse( bt.poundElse());
			assertTrue( bt.poundEndif() ); 
			
			
		}
		catch( EmptyStackException se )
		{
			fail( "Exception" );  //$NON-NLS-1$
		}
	}
}
