/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core;

import java.text.MessageFormat;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.internal.core.model.CDebugElement;
import org.eclipse.cdt.debug.internal.core.model.CDebugTarget;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;

/**
 * 
 * Provides utility methods for creating debug sessions, targets and 
 * breakpoints specific to the CDI debug model.
 * 
 * @since Aug 1, 2002
 */
public class CDebugModel
{
	/**
	 * Constructor for CDebugModel.
	 */
	public CDebugModel()
	{
		super();
	}

	/**
	 * Returns the identifier for the CDI debug model plug-in
	 *
	 * @return plugin identifier
	 */
	public static String getPluginIdentifier()
	{
		return CDebugCorePlugin.getUniqueIdentifier();
	}

	/**
	 * Creates and returns a debug target for the given CDI target, with
	 * the specified name, and associates the debug target with the
	 * given process for console I/O. The allow terminate flag specifies whether
	 * the debug target will support termination (<code>ITerminate</code>).
	 * The allow disconnect flag specifies whether the debug target will
	 * support disconnection (<code>IDisconnect</code>). The resume
	 * flag specifies if the target process should be resumed on startup. 
	 * The debug target is added to the given launch.
	 *
	 * @param launch the launch the new debug target will be contained in
	 * @param cdiTarget the CDI target to create a debug target for
	 * @param name the name to associate with this target, which will be 
	 *   returned from <code>IDebugTarget.getName</code>.
	 * @param process the process to associate with the debug target,
	 *   which will be returned from <code>IDebugTarget.getProcess</code>
	 * @param allowTerminate whether the target will support termianation
	 * @param allowDisconnect whether the target will support disconnection
	 * @param resume whether the target is to be resumed on startup.
	 * @return a debug target
	 * 
	 * @since 2.0
	 */
	public static IDebugTarget newDebugTarget( final ILaunch launch,
											   final ICDITarget cdiTarget,
											   final String name,
											   final IProcess process,
											   final IProject project,
											   final boolean allowTerminate,
											   final boolean allowDisconnect,
											   final boolean stopInMain ) throws DebugException
	{
		final IDebugTarget[] target = new IDebugTarget[1];
		IWorkspaceRunnable r = new IWorkspaceRunnable()
		{
			public void run( IProgressMonitor m )
			{
				target[0] = new CDebugTarget( launch, 
											  cdiTarget, 
											  name,
											  process,
											  project,
											  allowTerminate,
											  allowDisconnect );
			}
		};
		try
		{
			ResourcesPlugin.getWorkspace().run( r, null );
		}
		catch( CoreException e )
		{
			CDebugCorePlugin.log( e );
		}
		// Temporary
		if ( stopInMain )
		{
			ICDILocation location = cdiTarget.getSession().getBreakpointManager().createLocation( "", "main", 0 );
			try
			{
				ICDIBreakpoint bkpt = cdiTarget.getSession().getBreakpointManager().
											setLocationBreakpoint( ICDIBreakpoint.REGULAR, //ICDIBreakpoint.TEMPORARY,
																   location,
																   null,
																   null );
			}
			catch( CDIException e )
			{
				((CDebugElement)target[0]).targetRequestFailed( MessageFormat.format( "{0} occurred setting temporary breakpoint.", new String[] { e.toString() } ), e );
			}
		}
		target[0].resume();

		return target[0];
	}
}
