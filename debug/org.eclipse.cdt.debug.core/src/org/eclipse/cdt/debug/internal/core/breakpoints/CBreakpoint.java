/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.core.breakpoints;

import org.eclipse.cdt.debug.core.CDebugModel;
import org.eclipse.cdt.debug.core.ICBreakpoint;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.Breakpoint;

/**
 * 
 * Enter type comment.
 * 
 * @since Aug 21, 2002
 */
public abstract class CBreakpoint extends Breakpoint
								  implements ICBreakpoint, 
								  			 IDebugEventSetListener
{
	/**
	 * Breakpoint attribute storing the number of debug targets a
	 * breakpoint is installed in (value <code>"org.eclipse.cdt.debug.core.installCount"</code>).
	 * This attribute is a <code>int</code>.
	 */
	protected static final String INSTALL_COUNT = "org.eclipse.cdt.debug.core.installCount"; //$NON-NLS-1$	

	/**
	 * Breakpoint attribute storing the the conditional expression 
	 * associated with this breakpoint (value <code>"org.eclipse.cdt.debug.core.condition"</code>).
	 * This attribute is a <code>String</code>.
	 */
	protected static final String CONDITION = "org.eclipse.cdt.debug.core.condition"; //$NON-NLS-1$	

	/**
	 * Breakpoint attribute storing a breakpoint's ignore count value 
	 * (value <code>"org.eclipse.cdt.debug.core.ignoreCount"</code>).
	 * This attribute is a <code>int</code>.
	 */
	protected static final String IGNORE_COUNT = "org.eclipse.cdt.debug.core.ignoreCount"; //$NON-NLS-1$	

	/**
	 * Breakpoint attribute storing an identifier of the thread this 
	 * breakpoint is restricted in (value <code>"org.eclipse.cdt.debug.core.threadId"</code>). 
	 * This attribute is a <code>String</code>.
	 */
	protected static final String THREAD_ID = "org.eclipse.cdt.debug.core.threadId"; //$NON-NLS-1$	

	/**
	 * Constructor for CBreakpoint.
	 */
	public CBreakpoint()
	{
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IBreakpoint#getModelIdentifier()
	 */
	public String getModelIdentifier()
	{
		return CDebugModel.getPluginIdentifier();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICBreakpoint#isInstalled()
	 */
	public boolean isInstalled() throws CoreException
	{
		return ensureMarker().getAttribute( INSTALL_COUNT, 0 ) > 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICBreakpoint#getCondition()
	 */
	public String getCondition() throws CoreException
	{
		return ensureMarker().getAttribute( CONDITION, "" );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICBreakpoint#setCondition(String)
	 */
	public void setCondition( String condition ) throws CoreException
	{
		setAttribute( CONDITION, condition );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICBreakpoint#getIgnoreCount()
	 */
	public int getIgnoreCount() throws CoreException
	{
		return ensureMarker().getAttribute( IGNORE_COUNT, 0 );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICBreakpoint#setIgnoreCount(int)
	 */
	public void setIgnoreCount( int ignoreCount ) throws CoreException
	{
		setAttribute( IGNORE_COUNT, ignoreCount );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICBreakpoint#getThreadId()
	 */
	public String getThreadId() throws CoreException
	{
		return ensureMarker().getAttribute( THREAD_ID, null );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICBreakpoint#setThreadId(String)
	 */
	public void setThreadId( String threadId ) throws CoreException
	{
		setAttribute( THREAD_ID, threadId );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(DebugEvent[])
	 */
	public void handleDebugEvents( DebugEvent[] events )
	{
	}
}
