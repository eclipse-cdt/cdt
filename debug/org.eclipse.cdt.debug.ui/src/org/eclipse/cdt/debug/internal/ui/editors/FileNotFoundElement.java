/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.editors;

import org.eclipse.cdt.debug.core.model.IStackFrameInfo;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IStackFrame;

/**
 * The source locator creates an instance of this class if it cannot find the file specified in stack frame.
 * 
 * @since: Feb 21, 2003
 */
public class FileNotFoundElement
{
	private IStackFrame fStackFrame;
 
	/**
	 * Constructor for FileNotFoundElement.
	 */
	public FileNotFoundElement( IStackFrame stackFrame )
	{
		fStackFrame = stackFrame;
	}

	public IPath getFullPath()
	{
		IStackFrameInfo frameInfo = (IStackFrameInfo)fStackFrame.getAdapter( IStackFrameInfo.class );
		if ( frameInfo != null && frameInfo.getFile() != null && frameInfo.getFile().length() > 0 )
		{
			Path path = new Path( frameInfo.getFile() );
			if ( path.isValidPath( frameInfo.getFile() ) )
			{
				return path;
			}
		}
		return null;
	}

	public String getName()
	{
		IPath path = getFullPath();
		return ( path != null ) ? path.lastSegment() : ""; //$NON-NLS-1$
	}

	public IStackFrame getStackFrame()
	{
		return fStackFrame;
	}
	
	public ILaunch getLaunch()
	{
		return ( fStackFrame != null ) ? fStackFrame.getLaunch() : null;
	}
}
