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
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.SharedLibraryManager;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.cdt.debug.mi.core.command.CLITargetAttach;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIGDBSet;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
 
/**
 * Implementing the cdebugger extension point for basic launch configurations.
 */
public class GDBCDIDebugger2 extends AbstractGDBCDIDebugger {

	protected String[] getExtraArguments( ILaunchConfiguration config ) throws CoreException {
		String debugMode = config.getAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE, ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN );
		if ( ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN.equals( debugMode ) )
			return getRunArguments( config );
		if ( ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH.equals( debugMode ) )
			return getAttachArguments( config );
		if ( ICDTLaunchConfigurationConstants.DEBUGGER_MODE_CORE.equals( debugMode ) )
			return getCoreArguments( config );
		return new String[0];
	}

	protected String[] getRunArguments( ILaunchConfiguration config ) throws CoreException {
		return new String[]{ getWorkingDirectory( config ), getCommandFile( config ) }; 
	}

	protected String[] getAttachArguments( ILaunchConfiguration config ) throws CoreException {
		return new String[]{ getWorkingDirectory( config ), getCommandFile( config ) }; 
	}

	protected String[] getCoreArguments( ILaunchConfiguration config ) throws CoreException {
		IPath coreFile = new Path( config.getAttribute( ICDTLaunchConfigurationConstants.ATTR_COREFILE_PATH, (String)null ) );
		return new String[]{ getWorkingDirectory( config ), getCommandFile( config ), "-c", coreFile.toFile().getAbsolutePath() };  //$NON-NLS-1$
	}

	protected CommandFactory getCommandFactory( ILaunchConfiguration config ) throws CoreException {
		String factoryID = MIPlugin.getCommandFactory( config );
		CommandFactory factory = MIPlugin.getDefault().getCommandFactoryManager().getCommandFactory( factoryID );
		String miVersion = getMIVersion( config );
		if ( factory != null ) {
			factory.setMIVersion( miVersion );
		}
		return ( factory != null ) ? factory : new CommandFactory( miVersion );
	}

	public static IPath getProjectPath( ILaunchConfiguration configuration ) throws CoreException {
		String projectName = getProjectName( configuration );
		if ( projectName != null ) {
			projectName = projectName.trim();
			if ( projectName.length() > 0 ) {
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject( projectName );
				IPath p = project.getLocation();
				if ( p != null ) {
					return p;
				}
			}
		}
		return Path.EMPTY;
	}

	public static String getProjectName( ILaunchConfiguration configuration ) throws CoreException {
		return configuration.getAttribute( ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null );
	}

	protected String getMIVersion( ILaunchConfiguration config ) {
		return MIPlugin.getMIVersion( config );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.core.AbstractGDBCDIDebugger#doStartSession(org.eclipse.debug.core.ILaunch, org.eclipse.cdt.debug.mi.core.cdi.Session, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void doStartSession( ILaunch launch, Session session, IProgressMonitor monitor ) throws CoreException {
		ILaunchConfiguration config = launch.getLaunchConfiguration();
		initializeLibraries( config, session );
		if ( monitor.isCanceled() ) {
			throw new OperationCanceledException();
		}
		String debugMode = config.getAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE, ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN );
		if ( ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN.equals( debugMode ) )
			startLocalGDBSession( config, session, monitor );
		if ( ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH.equals( debugMode ) )
			startAttachGDBSession( config, session, monitor );
		if ( ICDTLaunchConfigurationConstants.DEBUGGER_MODE_CORE.equals( debugMode ) )
			startCoreGDBSession( config, session, monitor );
	}

	protected void startLocalGDBSession( ILaunchConfiguration config, Session session, IProgressMonitor monitor ) throws CoreException {
		// TODO: need a better solution for new-console
		MISession miSession = getMISession( session );
		try {
			CommandFactory factory = miSession.getCommandFactory();
			MIGDBSet set = factory.createMIGDBSet( new String[]{ "new-console" } ); //$NON-NLS-1$
			miSession.postCommand( set );
			MIInfo info = set.getMIInfo();
			if ( info == null ) {
				throw new MIException( MIPlugin.getResourceString( "src.common.No_answer" ) ); //$NON-NLS-1$
			}
		}
		catch( MIException e ) {
			// We ignore this exception, for example
			// on GNU/Linux the new-console is an error.
		}		
	}

	protected void startAttachGDBSession( ILaunchConfiguration config, Session session, IProgressMonitor monitor ) throws CoreException {
		MISession miSession = getMISession( session );
		CommandFactory factory = miSession.getCommandFactory();
		int pid = -1;
		try {
			pid = config.getAttribute( ICDTLaunchConfigurationConstants.ATTR_ATTACH_PROCESS_ID, -1 );
		}
		catch( CoreException e ) {
			throw newCoreException( MIPlugin.getResourceString( "src.GDBCDIDebugger2.0" ), e ); //$NON-NLS-1$
		}
		if ( pid <= 0 ) {
			throw newCoreException( MIPlugin.getResourceString( "src.GDBCDIDebugger2.1" ), null ); //$NON-NLS-1$
		}
		try {
			CLITargetAttach attach = factory.createCLITargetAttach( pid );
			miSession.postCommand( attach );
			MIInfo info = attach.getMIInfo();
			if ( info == null ) {
				throw new MIException( MIPlugin.getResourceString( "src.common.No_answer" ) ); //$NON-NLS-1$
			}
			miSession.getMIInferior().setInferiorPID( pid );
			// @@@ for attach we nee to manually set the connected state
			// attach does not send the ^connected ack
			miSession.getMIInferior().setConnected();
		}
		catch( MIException e ) {
			throw newCoreException( MessageFormat.format( MIPlugin.getResourceString( "src.GDBCDIDebugger2.2" ), new Integer[] { new Integer( pid ) } ), e ); //$NON-NLS-1$
		}
		// @@@ We have to set the suspended state manually
		miSession.getMIInferior().setSuspended();
		miSession.getMIInferior().update();
	}

	protected void startCoreGDBSession( ILaunchConfiguration config, Session session, IProgressMonitor monitor ) throws CoreException {
		getMISession( session ).getMIInferior().setSuspended();		
		try {
			session.getSharedLibraryManager().update();
		}
		catch( CDIException e ) {
			throw newCoreException( e );
		}
	}

	protected MISession getMISession( Session session ) {
		ICDITarget[] targets = session.getTargets();
		if ( targets.length == 0 || !(targets[0] instanceof Target) )
			return null;
		return ((Target)targets[0]).getMISession();
	}

	protected void initializeLibraries( ILaunchConfiguration config, Session session ) throws CoreException {
		try {
			SharedLibraryManager sharedMgr = session.getSharedLibraryManager();
			boolean autolib = config.getAttribute( IMILaunchConfigurationConstants.ATTR_DEBUGGER_AUTO_SOLIB, IMILaunchConfigurationConstants.DEBUGGER_AUTO_SOLIB_DEFAULT );
			boolean stopOnSolibEvents = config.getAttribute( IMILaunchConfigurationConstants.ATTR_DEBUGGER_STOP_ON_SOLIB_EVENTS, IMILaunchConfigurationConstants.DEBUGGER_STOP_ON_SOLIB_EVENTS_DEFAULT );
			List p = config.getAttribute( IMILaunchConfigurationConstants.ATTR_DEBUGGER_SOLIB_PATH, Collections.EMPTY_LIST );
			ICDITarget[] dtargets = session.getTargets();
			for( int i = 0; i < dtargets.length; ++i ) {
				Target target = (Target)dtargets[i];
				try {
					sharedMgr.setAutoLoadSymbols( target, autolib );
					sharedMgr.setStopOnSolibEvents( target, stopOnSolibEvents );
					sharedMgr.setDeferredBreakpoint( false );
					// The idea is that if the user set autolib, by default
					// we provide with the capability of deferred breakpoints
					// And we set setStopOnSolib events for them(but they should not see those things.
					//
					// If the user explicitly set stopOnSolibEvents well it probably
					// means that they wanted to see those events so do no do deferred breakpoints.
					if ( autolib && !stopOnSolibEvents ) {
						sharedMgr.setDeferredBreakpoint( true );
						sharedMgr.setStopOnSolibEvents( target, true );
					}
				}
				catch( CDIException e ) {
					// Ignore this error
					// it seems to be a real problem on many gdb platform
				}
				if ( p.size() > 0 ) {
					String[] oldPaths = sharedMgr.getSharedLibraryPaths( target );
					String[] paths = new String[oldPaths.length + p.size()];
					System.arraycopy( p.toArray( new String[p.size()] ), 0, paths, 0, p.size() );
					System.arraycopy( oldPaths, 0, paths, p.size(), oldPaths.length );
					sharedMgr.setSharedLibraryPaths( target, paths );
				}
			}
		}
		catch( CDIException e ) {
			throw newCoreException( MIPlugin.getResourceString( "src.GDBDebugger.Error_initializing_shared_lib_options" ) + e.getMessage(), e ); //$NON-NLS-1$
		}
	}

	protected String getWorkingDirectory( ILaunchConfiguration config ) throws CoreException {
		File cwd = getProjectPath( config ).toFile();
		return "--cd=" + cwd.getAbsolutePath(); //$NON-NLS-1$
	}

	protected String getCommandFile( ILaunchConfiguration config ) throws CoreException {
		String gdbinit = config.getAttribute( IMILaunchConfigurationConstants.ATTR_GDB_INIT, IMILaunchConfigurationConstants.DEBUGGER_GDB_INIT_DEFAULT );
		return (gdbinit != null && gdbinit.length() > 0) ? "--command=" + gdbinit : "--nx"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
