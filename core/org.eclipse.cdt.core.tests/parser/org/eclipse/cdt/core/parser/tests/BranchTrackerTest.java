package org.eclipse.cdt.core.parser.tests;

import java.util.EmptyStackException;

import junit.framework.TestCase;

import org.eclipse.cdt.internal.core.parser.BranchTracker;

/**
 * @author jcamelon
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
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

			assertFalse( bt.poundif( false ) );  
			assertFalse( bt.poundif( true ) ); 
			assertFalse( bt.poundelif( true ) );  
			assertFalse( bt.poundelse() ); 
			assertFalse( bt.poundendif() );  
			assertTrue( bt.poundelse() ); 
			assertTrue( bt.poundendif() ); 

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
			assertFalse( bt.poundif( false ) );
			assertFalse( bt.poundif( true ));   
			assertFalse( bt.poundelif( true ) );  
			assertFalse( bt.poundelse() );  
			assertFalse( bt.poundendif() );  
			assertTrue( bt.poundelse() );  
			assertFalse( bt.poundif( false ) );  
			assertTrue( bt.poundelif( true ) );  
			assertFalse( bt.poundelif( false ) ); 
			assertFalse( bt.poundelif( true ) ); 
			assertFalse( bt.poundelse() ); 
			assertTrue( bt.poundendif() ); 
			assertTrue( bt.poundendif() ); 
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
			 
			assertFalse(bt.poundif(false));
				assertFalse(bt.poundif(true));
				assertFalse(bt.poundelif(false));
				assertFalse(bt.poundelif(true));
				assertFalse(bt.poundelse());
				assertFalse( bt.poundendif() ); 
			assertFalse(bt.poundelif(false));
				assertFalse(bt.poundif(false));
				assertFalse(bt.poundelif(false));
				assertFalse(bt.poundelif(true));
				assertFalse(bt.poundelse());
				assertFalse( bt.poundendif());
			assertTrue(bt.poundelif(true));
				assertFalse(bt.poundif(false));
				assertFalse(bt.poundelif(false));
				assertFalse(bt.poundelif(false));
				assertTrue(bt.poundelse());
				assertTrue( bt.poundendif() ); 
			assertFalse(bt.poundelse());
				assertFalse(bt.poundif(true));
				assertFalse(bt.poundelif(false));
				assertFalse(bt.poundelif(true));
				assertFalse(bt.poundelse());
				assertFalse( bt.poundendif() ); 
			assertTrue( bt.poundendif() ); 
			assertEquals(0, bt.getDepth());
		} catch (EmptyStackException se) {
				fail("Unexpected Scanner exception thrown");
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
			assertTrue( bt.poundif( true ) );  
			assertFalse( bt.poundelse() ); 
			assertTrue( bt.poundendif() );  
			
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
			assertTrue( bt.poundif( true ));
			assertFalse( bt.poundif( false ));  
			assertFalse( bt.poundelif( false ));
			assertTrue( bt.poundelse());
			assertTrue( bt.poundendif() );  
			assertFalse( bt.poundelse() ); 
			assertTrue( bt.poundendif() ); 
			
			/*
			 *	#if 1
			 *	#elsif 1
			 *	#elsif 0
			 *	#else
			 *	#endif 
			 */
			 
			bt  = new BranchTracker(); 
			assertTrue( bt.poundif( true ) ); 
			assertFalse( bt.poundelif( true )); 
			assertFalse( bt.poundelif( false )); 
			assertFalse( bt.poundelse());
			assertTrue( bt.poundendif() ); 
			
			
		}
		catch( EmptyStackException se )
		{
			fail( "Exception" ); 
		}
	}

}
