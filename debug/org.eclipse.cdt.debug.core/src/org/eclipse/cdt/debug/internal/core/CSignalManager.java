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
package org.eclipse.cdt.debug.internal.core;

import java.util.ArrayList;

import org.eclipse.cdt.debug.core.CDebugModel;
import org.eclipse.cdt.debug.core.ICSignalManager;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIManager;
import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;
import org.eclipse.cdt.debug.core.model.ICSignal;
import org.eclipse.cdt.debug.internal.core.model.CDebugTarget;
import org.eclipse.cdt.debug.internal.core.model.CSignal;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;

/**
 * Enter type comment.
 * 
 * @since: Jan 31, 2003
 */
public class CSignalManager extends CUpdateManager implements ICSignalManager
{
	private ICSignal[] fSignals = null;
	private boolean fIsDisposed = false;

	/**
	 * Constructor for CSignalManager.
	 */
	public CSignalManager( CDebugTarget target )
	{
		super( target );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICSignalManager#getSignals()
	 */
	public ICSignal[] getSignals() throws DebugException
	{
		if ( !isDisposed() && fSignals == null )
		{
			try
			{
				ICDISignal[] cdiSignals = getDebugTarget().getCDITarget().getSignals();
				ArrayList list = new ArrayList( cdiSignals.length );
				for ( int i = 0; i < cdiSignals.length; ++i )
				{
					list.add( new CSignal( getDebugTarget(), cdiSignals[i] ) );
				}
				fSignals = (ICSignal[])list.toArray( new ICSignal[list.size()] );
			}
			catch( CDIException e )
			{
				throwDebugException( e.getMessage(), DebugException.TARGET_REQUEST_FAILED, e );
			}
		}
		return ( fSignals != null ) ? fSignals : new ICSignal[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICSignalManager#dispose()
	 */
	public void dispose()
	{
		if ( fSignals != null )
		for ( int i = 0; i < fSignals.length; ++i )
		{
			((CSignal)fSignals[i]).dispose();
		}
		fSignals = null;
		fIsDisposed = true;
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter( Class adapter )
	{
		if ( adapter.equals( ICSignalManager.class ) )
		{
			return this;
		}
		if ( adapter.equals( CSignalManager.class ) )
		{
			return this;
		}
		return super.getAdapter( adapter );
	}
	
	public void signalChanged( ICDISignal cdiSignal )
	{
		CSignal signal = find( cdiSignal );
		if ( signal != null )
		{
			signal.fireChangeEvent( DebugEvent.STATE );
		}
	}

	private CSignal find( ICDISignal cdiSignal )
	{
		try
		{
			ICSignal[] signals = getSignals();
			for ( int i = 0; i < signals.length; ++i )
				if ( signals[i].getName().equals( cdiSignal.getName() ) )
					return (CSignal)signals[i];
		}
		catch( DebugException e )
		{
		}
		return null;
	}
	
	protected boolean isDisposed()
	{
		return fIsDisposed;
	}

	protected ICDIManager getCDIManager()
	{
		//FIXME: To change we no longer have a ICDISignalManager.
		return null;
	}
	
	/**
	 * Throws a debug exception with the given message, error code, and underlying
	 * exception.
	 */
	protected void throwDebugException( String message, int code, Throwable exception ) throws DebugException 
	{
		throw new DebugException( new Status( IStatus.ERROR, 
											  CDebugModel.getPluginIdentifier(),
											  code, 
											  message, 
											  exception ) );
	}
}
