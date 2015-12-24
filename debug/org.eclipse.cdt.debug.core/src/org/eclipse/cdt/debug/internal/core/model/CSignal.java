/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;
import org.eclipse.cdt.debug.core.model.ICSignal;
import org.eclipse.debug.core.DebugException;

/**
 * Enter type comment.
 * 
 * @since: Jan 31, 2003
 */
public class CSignal extends CDebugElement implements ICSignal, ICDIEventListener
{
	private ICDISignal fCDISignal;

	/**
	 * Constructor for CSignal.
	 * @param target
	 */
	public CSignal( CDebugTarget target, ICDISignal cdiSignal )
	{
		super( target );
		fCDISignal = cdiSignal;
		getCDISession().getEventManager().addEventListener( this );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICSignal#getDescription()
	 */
	@Override
	public String getDescription() throws DebugException
	{
		return getCDISignal().getDescription();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICSignal#getName()
	 */
	@Override
	public String getName() throws DebugException
	{
		return getCDISignal().getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICSignal#isPassEnabled()
	 */
	@Override
	public boolean isPassEnabled() throws DebugException
	{
		return !getCDISignal().isIgnore();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICSignal#isStopEnabled()
	 */
	@Override
	public boolean isStopEnabled() throws DebugException
	{
		return getCDISignal().isStopSet();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICSignal#setPassEnabled(boolean)
	 */
	@Override
	public void setPassEnabled( boolean enable ) throws DebugException
	{
		handle( enable, isStopEnabled() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICSignal#setStopEnabled(boolean)
	 */
	@Override
	public void setStopEnabled( boolean enable ) throws DebugException
	{
		handle( isPassEnabled(), enable );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener#handleDebugEvents(ICDIEvent)
	 */
	@Override
	public void handleDebugEvents( ICDIEvent[] events )
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICSignal#dispose()
	 */
	public void dispose()
	{
		getCDISession().getEventManager().removeEventListener( this );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICSignal#signal()
	 */
	@Override
	public void signal() throws DebugException
	{
		try
		{
			getCDITarget().resume( getCDISignal() );
		}
		catch( CDIException e )
		{
			targetRequestFailed( e.getMessage(), null );
		}
	}
	
	protected ICDISignal getCDISignal()
	{
		return fCDISignal;
	}
	
	private void handle( boolean pass, boolean stop ) throws DebugException
	{
		try
		{
			getCDISignal().handle( !pass, stop );
		}
		catch( CDIException e )
		{
			targetRequestFailed( e.getMessage(), null );
		}
	}	

	@Override
	public boolean canModify() {
		// TODO add canModify method to ICDISignal
		return true;
	}
}
