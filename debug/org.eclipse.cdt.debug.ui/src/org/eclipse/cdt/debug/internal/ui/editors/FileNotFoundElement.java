/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.editors;

import org.eclipse.cdt.debug.core.model.ICStackFrame;
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
		if ( fStackFrame instanceof ICStackFrame )
		{
			String fn = ((ICStackFrame)fStackFrame).getFile();
			if ( fn != null && fn.trim().length() > 0 )
			{
				Path path = new Path( fn );
				if ( path.isValidPath( fn ) )
				{
					return path;
				}
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
