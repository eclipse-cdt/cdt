/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core; 

import java.io.File;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.debug.core.ICDIDebugger2;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
 
/**
 * Base class for the gdb/mi-based <code>ICDIDebugger</code> extension point 
 * implementations.
 */
abstract public class AbstractGDBCDIDebugger implements ICDIDebugger2 {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICDIDebugger#createDebuggerSession(org.eclipse.debug.core.ILaunch, org.eclipse.cdt.core.IBinaryParser.IBinaryObject, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ICDISession createDebuggerSession( ILaunch launch, IBinaryObject exe, IProgressMonitor monitor ) throws CoreException {
		return createSession( launch, exe.getPath().toFile(), monitor );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICDIDebugger2#createSession(org.eclipse.debug.core.ILaunch, java.io.File, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ICDISession createSession( ILaunch launch, File executable, IProgressMonitor monitor ) throws CoreException {
		boolean failed = false;
		if ( monitor == null ) {
			monitor = new NullProgressMonitor();
		}
		if ( monitor.isCanceled() ) {
			throw new OperationCanceledException();
		}
		Session session = createGDBSession( launch, executable, monitor );
		if ( session != null ) {
			ICDITarget[] targets = session.getTargets();
			for( int i = 0; i < targets.length; i++ ) {
				Process debugger = session.getSessionProcess( targets[i] );
				if ( debugger != null ) {
					IProcess debuggerProcess = DebugPlugin.newProcess( launch, debugger, renderDebuggerProcessLabel( launch ) );
					launch.addProcess( debuggerProcess );
				}
				try {
					((Target)targets[i]).getMISession().start();
				}
				catch( MIException e ) {
					failed = true;
					throw newCoreException( e );
				}
			}
		}
		try {
			doStartSession( launch, session, monitor );
		}
		catch( CoreException e ) {
			failed = true;
			throw e;
		}
		finally {
			try {
				if ( failed || monitor.isCanceled() )
					session.terminate();
			}
			catch( CDIException e1 ) {
			}
		}
		return session;
	}

	protected Session createGDBSession( ILaunch launch, File executable, IProgressMonitor monitor ) throws CoreException {
		IPath gdbPath = getGDBPath( launch );
		ILaunchConfiguration config = launch.getLaunchConfiguration();
		CommandFactory factory = getCommandFactory( config );
		String[] extraArgs = getExtraArguments( config );
		boolean usePty = config.getAttribute( ICDTLaunchConfigurationConstants.ATTR_USE_TERMINAL, true );
		try {
			return MIPlugin.getDefault().createSession( getSessionType( config ), gdbPath.toOSString(), factory, executable, extraArgs, usePty, monitor );
		}
		catch( Exception e ) {
			// Catch all wrap them up and rethrow
			if ( e instanceof CoreException ) {
				throw (CoreException)e;
			}
			throw newCoreException( e );
		}
	}

	protected int getSessionType( ILaunchConfiguration config ) throws CoreException {
		String debugMode = config.getAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE, ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN );
		if ( ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN.equals( debugMode ) )
			return MISession.PROGRAM;
		if ( ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH.equals( debugMode ) )
			return MISession.ATTACH;
		if ( ICDTLaunchConfigurationConstants.DEBUGGER_MODE_CORE.equals( debugMode ) )
			return MISession.CORE;
		throw newCoreException( MIPlugin.getResourceString( "src.AbstractGDBCDIDebugger.0" ) + debugMode, null ); //$NON-NLS-1$
	}

	protected String[] getExtraArguments( ILaunchConfiguration config ) throws CoreException {
		return new String[0];
	}

	abstract protected CommandFactory getCommandFactory( ILaunchConfiguration config ) throws CoreException;

	protected void doStartSession( ILaunch launch, Session session, IProgressMonitor monitor ) throws CoreException {		
	}

	protected String renderDebuggerProcessLabel( ILaunch launch ) {
		String format = "{0} ({1})"; //$NON-NLS-1$
		String timestamp = DateFormat.getInstance().format( new Date( System.currentTimeMillis() ) );
		String label = MIPlugin.getResourceString( "src.AbstractGDBCDIDebugger.0" ); //$NON-NLS-1$
		try {
			IPath path = getGDBPath( launch );
			label = path.toOSString();
		}
		catch( CoreException e ) {
		}
		return MessageFormat.format( format, new String[]{ label, timestamp } );
	}

	protected IPath getGDBPath( ILaunch launch ) throws CoreException {
		ILaunchConfiguration config = launch.getLaunchConfiguration();
		return new Path( config.getAttribute( IMILaunchConfigurationConstants.ATTR_DEBUG_NAME, IMILaunchConfigurationConstants.DEBUGGER_DEBUG_NAME_DEFAULT ) );
	}
	/**
	 * Throws a core exception with an error status object built from 
	 * the lower level exception and error code.
	 * 
	 * @param exception lower level exception associated with the error, 
	 *                  or <code>null</code> if none
	 * @param code error code
	 */
	protected CoreException newCoreException( Throwable exception ) {
		String message = MIPlugin.getResourceString( "src.AbstractGDBCDIDebugger.1" ); //$NON-NLS-1$
		return newCoreException( message, exception );
	}

	/**
	 * Throws a core exception with an error status object built from the given
	 * message, lower level exception, and error code.
	 * 
	 * @param message the status message
	 * @param exception lower level exception associated with the error, 
	 *                  or <code>null</code> if none
	 * @param code error code
	 */
	protected CoreException newCoreException( String message, Throwable exception ) {
		int code = ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR;
		String ID = MIPlugin.getUniqueIdentifier();
		MultiStatus status = new MultiStatus( ID, code, message, exception );
		status.add( new Status( IStatus.ERROR, ID, code, exception == null ? new String() : exception.getLocalizedMessage(), exception ) );
		return new CoreException( status );
	}
}
