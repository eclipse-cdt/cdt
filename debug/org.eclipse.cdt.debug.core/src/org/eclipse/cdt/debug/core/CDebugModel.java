/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core;

import java.text.MessageFormat;
import java.util.HashMap;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.internal.core.breakpoints.CLineBreakpoint;
import org.eclipse.cdt.debug.internal.core.breakpoints.CWatchpoint;
import org.eclipse.cdt.debug.internal.core.model.CDebugElement;
import org.eclipse.cdt.debug.internal.core.model.CDebugTarget;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IExpression;
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

		// Temporary
		try
		{
			cdiTarget.getSession().addSearchPaths( new String[] { project.getLocation().toOSString() } );
		}
		catch( CDIException e )
		{
			CDebugCorePlugin.log( e );
		}

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

	/**
	 * Returns a C/C++ line breakpoint that is already registered with the breakpoint
	 * manager for a file with the given name at the given line number.
	 * 
	 * @param fileName fully qualified file name
	 * @param lineNumber line number
	 * @return a C/C++ line breakpoint that is already registered with the breakpoint
	 *  manager for a file with the given name at the given line number or <code>null</code>
	 * if no such breakpoint is registered
	 * @exception CoreException if unable to retrieve the associated marker
	 * 	attributes (line number).
	 */
	public static ICLineBreakpoint lineBreakpointExists( String fileName, int lineNumber ) throws CoreException
	{
		String modelId = getPluginIdentifier();
		String markerType = CLineBreakpoint.getMarkerType();
		IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
		IBreakpoint[] breakpoints = manager.getBreakpoints( modelId );
		for ( int i = 0; i < breakpoints.length; i++ )
		{
			if ( !( breakpoints[i] instanceof ICLineBreakpoint ) )
			{
				continue;
			}
			ICLineBreakpoint breakpoint = (ICLineBreakpoint)breakpoints[i];
			if ( breakpoint.getMarker().getType().equals( markerType ) )
			{
				if ( breakpoint.getMarker().getResource().getLocation().toOSString().equals( fileName ) )
				{
					if ( breakpoint.getLineNumber() == lineNumber )
					{
						return breakpoint;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Creates and returns a line breakpoint in the file with the
	 * given name, at the given line number. The marker associated with the
	 * breakpoint will be created on the specified resource. If a character
	 * range within the line is known, it may be specified by charStart/charEnd.
	 * If ignoreCount is > 0, the breakpoint will suspend execution when it is
	 * "hit" the specified number of times.
	 * 
	 * @param resource the resource on which to create the associated breakpoint
	 *  marker
	 * @param typeName the fully qualified name of the type the breakpoint is
	 *  to be installed in. If the breakpoint is to be installed in an inner type,
	 *  it is sufficient to provide the name of the top level enclosing type.
	 * 	If an inner class name is specified, it should be formatted as the 
	 *  associated class file name (i.e. with <code>$</code>). For example,
	 * 	<code>example.SomeClass$InnerType</code>, could be specified, but
	 * 	<code>example.SomeClass</code> is sufficient.
	 * @param lineNumber the lineNumber on which the breakpoint is set - line
	 *   numbers are 1 based, associated with the source file in which
	 *   the breakpoint is set
	 * @param charStart the first character index associated with the breakpoint,
	 *   or -1 if unspecified, in the source file in which the breakpoint is set
 	 * @param charEnd the last character index associated with the breakpoint,
	 *   or -1 if unspecified, in the source file in which the breakpoint is set
	 * @param hitCount the number of times the breakpoint will be hit before
	 *   suspending execution - 0 if it should always suspend
	 * @param register whether to add this breakpoint to the breakpoint manager
	 * @param attributes a map of client defined attributes that should be assigned
 	 *  to the underlying breakpoint marker on creation, or <code>null</code> if none.
	 * @return a line breakpoint
	 * @exception DebugException If this method fails. Reasons include: 
	 */
	public static ICLineBreakpoint createLineBreakpoint( IResource resource, 
														 int lineNumber, 
														 boolean enabled,
														 int ignoreCount, 
														 String condition, 
														 boolean add ) throws DebugException
	{
		HashMap attributes = new HashMap( 10 );
		attributes.put( ICBreakpoint.ID, getPluginIdentifier() );
		attributes.put( IMarker.LINE_NUMBER, new Integer( lineNumber ) );
		attributes.put( ICBreakpoint.ENABLED, new Boolean( enabled ) );
		attributes.put( ICBreakpoint.IGNORE_COUNT, new Integer( ignoreCount ) );
		attributes.put( ICBreakpoint.CONDITION, condition );
		return new CLineBreakpoint( resource, attributes, add );
	}

	public static ICWatchpoint watchpointExists( IResource resource, boolean write, boolean read, String expression ) throws CoreException
	{
			String modelId = getPluginIdentifier();
		String markerType = CWatchpoint.getMarkerType();
		IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
		IBreakpoint[] breakpoints = manager.getBreakpoints( modelId );
		for ( int i = 0; i < breakpoints.length; i++ )
		{
			if ( !( breakpoints[i] instanceof ICWatchpoint ) )
			{
				continue;
			}
			ICWatchpoint breakpoint = (ICWatchpoint)breakpoints[i];
			if ( breakpoint.getMarker().getType().equals( markerType )&& 
				 breakpoint.getMarker().getResource().equals( resource ) && 
				 breakpoint.isWriteType() == write &&
				 breakpoint.isReadType() == read &&
				 breakpoint.getExpression().equals( expression ) )
				{
					return breakpoint;
				}
		}
		return null;
}

	public static ICWatchpoint createWatchpoint( IResource resource, 
												 boolean writeAccess,
												 boolean readAccess,
												 String expression, 
												 boolean enabled,
												 int ignoreCount, 
												 String condition, 
												 boolean add ) throws DebugException
	{
		HashMap attributes = new HashMap( 10 );
		attributes.put( ICBreakpoint.ID, getPluginIdentifier() );
		attributes.put( ICBreakpoint.ENABLED, new Boolean( enabled ) );
		attributes.put( ICBreakpoint.IGNORE_COUNT, new Integer( ignoreCount ) );
		attributes.put( ICBreakpoint.CONDITION, condition );
		attributes.put( ICWatchpoint.EXPRESSION, expression );
		attributes.put( ICWatchpoint.READ, new Boolean( readAccess ) );
		attributes.put( ICWatchpoint.WRITE, new Boolean( writeAccess ) );
		return new CWatchpoint( resource, attributes, add );
	}
	
	public static IExpression createExpression( IDebugTarget target, String text ) throws DebugException
	{
		if ( target != null && target instanceof CDebugTarget )
		{
			try
			{
				ICDIExpression cdiExpression = ((CDebugTarget)target).getCDISession().getExpressionManager().createExpression( text );
			}
			catch( CDIException e )
			{
				throw new DebugException( new Status( IStatus.ERROR, 
													  getPluginIdentifier(),
													  DebugException.TARGET_REQUEST_FAILED, 
													  "Create expression failed.", 
													  e ) );
			}
		}
		return null;
	}
}
