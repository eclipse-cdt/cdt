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
package org.eclipse.cdt.debug.internal.core.model;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary;
import org.eclipse.cdt.debug.core.model.ICSharedLibrary;
import org.eclipse.debug.core.DebugException;

/**
 * Enter type comment.
 * 
 * @since: Jan 16, 2003
 */
public class CSharedLibrary extends CDebugElement 
							 implements ICSharedLibrary,
							 			ICDIEventListener
{
	private ICDISharedLibrary fCDILib = null;

	/**
	 * Constructor for CSharedLibrary.
	 * @param target
	 */
	public CSharedLibrary( CDebugTarget target, ICDISharedLibrary cdiLib )
	{
		super( target );
		fCDILib = cdiLib;
		getCDISession().getEventManager().addEventListener( this );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICSharedLibrary#getFileName()
	 */
	public String getFileName()
	{
		if ( getCDISharedLibrary() != null )
			return getCDISharedLibrary().getFileName();
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICSharedLibrary#getStartAddress()
	 */
	public IAddress getStartAddress()
	{
		if ( getCDISharedLibrary() != null )
			return getCDISharedLibrary().getStartAddress();
		return ((CDebugTarget)getDebugTarget()).getAddressFactory().getZero();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICSharedLibrary#getEndAddress()
	 */
	public IAddress getEndAddress()
	{
		if ( getCDISharedLibrary() != null )
			return getCDISharedLibrary().getEndAddress();
		return ((CDebugTarget)getDebugTarget()).getAddressFactory().getZero();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICSharedLibrary#areSymbolsLoaded()
	 */
	public boolean areSymbolsLoaded()
	{
		if ( getCDISharedLibrary() != null )
			return getCDISharedLibrary().areSymbolsLoaded();
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICSharedLibrary#loadSymbols()
	 */
	public void loadSymbols() throws DebugException
	{
		try
		{
			if ( getCDISharedLibrary() != null )
				getCDISharedLibrary().loadSymbols();
		}
		catch( CDIException e )
		{
			targetRequestFailed( e.getMessage(), null );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICSharedLibrary#dispose()
	 */
	public void dispose()
	{
		getCDISession().getEventManager().removeEventListener( this );
	}

	public ICDISharedLibrary getCDISharedLibrary()
	{
		return fCDILib;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener#handleDebugEvents(ICDIEvent)
	 */
	public void handleDebugEvents( ICDIEvent[] events )
	{
	}
}
