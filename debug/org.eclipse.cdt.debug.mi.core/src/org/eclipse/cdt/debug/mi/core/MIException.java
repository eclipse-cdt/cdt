/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

/**
 * 
 * A checked exception representing a failure.
 * 
 * @author Mikhail Khodjaiants
 * @since Jul 11, 2002
 */
public class MIException extends CoreException
{
	/**
	 * Constructor for MIException.
	 */
	public MIException( IStatus status )
	{
		super( status );
	}
}
