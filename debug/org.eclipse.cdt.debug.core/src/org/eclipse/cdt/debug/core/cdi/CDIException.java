/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.cdi;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

/**
 * 
 * Represents a failure in the CDI model operations.
 * 
 * @since Jul 9, 2002
 */
public class CDIException extends CoreException
{
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.CoreException#CoreException(IStatus)
	 */
	public CDIException( IStatus status )
	{
		super( status );
	}
}
