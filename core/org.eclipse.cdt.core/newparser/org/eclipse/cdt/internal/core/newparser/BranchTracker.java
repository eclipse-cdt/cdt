package org.eclipse.cdt.internal.core.newparser;

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
	
	private static final Integer BRANCH_AVAILABLE = new Integer( 1 );		// we should evaluate all branches at this level
	private static final Integer BRANCH_UNAVAILABLE = new Integer( 2 ); 		// we are inside a ppConditional, all others should be neglected
	
	public BranchTracker()
	{
	}
	
	private Stack branches = new Stack();
		
	public void createBranch()
	{
		branches.push( BRANCH_AVAILABLE );
	}
	
	public void endBranch()
	{
		branches.pop(); 
	}
	
	public void takeBranch()
	{
		branches.pop();
		branches.push( BRANCH_UNAVAILABLE ); 
	}
		
	public boolean isBranchAvailable()
	{
		Integer peek = (Integer) branches.peek(); 
		if( peek == BRANCH_AVAILABLE )
			return true; 
		return false; 
	}
	
	public int depth()
	{
		return branches.size(); 
	}
}
