/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.editors;

import org.eclipse.cdt.debug.core.model.IStackFrameInfo;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IStackFrame;

/**
 * Enter type comment.
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

	public String getName()
	{
		IStackFrameInfo frameInfo = (IStackFrameInfo)fStackFrame.getAdapter( IStackFrameInfo.class );
		if ( frameInfo != null && frameInfo.getFile() != null && frameInfo.getFile().length() > 0 )
		{
			return frameInfo.getFile();
		}
		return "";
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
