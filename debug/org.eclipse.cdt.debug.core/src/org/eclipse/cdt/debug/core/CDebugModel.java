/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core;

import java.text.MessageFormat;
import java.util.HashMap;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIConfiguration;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.ICDISessionObject;
import org.eclipse.cdt.debug.core.cdi.event.ICDISuspendedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMemoryBlock;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICDebugTargetType;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
import org.eclipse.cdt.debug.core.model.IFormattedMemoryBlock;
import org.eclipse.cdt.debug.internal.core.CDebugUtils;
import org.eclipse.cdt.debug.internal.core.ICDebugInternalConstants;
import org.eclipse.cdt.debug.internal.core.breakpoints.CLineBreakpoint;
import org.eclipse.cdt.debug.internal.core.breakpoints.CWatchpoint;
import org.eclipse.cdt.debug.internal.core.model.CDebugTarget;
import org.eclipse.cdt.debug.internal.core.model.CExpression;
import org.eclipse.cdt.debug.internal.core.model.CFormattedMemoryBlock;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
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
	 * given process for console I/O. The allow terminate flag specifies 
	 * whether the debug target will support termination (<code>ITerminate</code>).
	 * The allow disconnect flag specifies whether the debug target will
	 * support disconnection (<code>IDisconnect</code>). The resume
	 * flag specifies if the target process should be resumed on startup. 
	 * The debug target is added to the given launch.
	 *
	 * @param launch the launch the new debug target will be contained in
	 * @param cdiTarget the CDI target to create a debug target for
	 * @param name the name to associate with this target, which will be 
	 *   returned from <code>IDebugTarget.getName</code>.
	 * @param debuggeeProcess the process to associate with the debug target,
	 *   which will be returned from <code>IDebugTarget.getProcess</code>
	 * @param debuggerProcess the process to associate with the debugger.
	 * @param file the executable to debug.
	 * @param allowTerminate whether the target will support termianation
	 * @param allowDisconnect whether the target will support disconnection
	 * @param stopInMain whether to set a temporary breakpoint in main.
	 * @return a debug target
	 */
	public static IDebugTarget newDebugTarget( final ILaunch launch,
											   final ICDITarget cdiTarget,
											   final String name,
											   final IProcess debuggeeProcess,
											   final IProcess debuggerProcess,
											   final IFile file,
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
											  ICDebugTargetType.TARGET_TYPE_LOCAL_RUN, 
											  cdiTarget, 
											  name,
											  debuggeeProcess,
											  debuggerProcess,
											  file,
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
			throw new DebugException( e.getStatus() );
		}

		ICDIConfiguration config = cdiTarget.getSession().getConfiguration();

		if ( config.supportsBreakpoints() && stopInMain )
		{
			stopInMain( (CDebugTarget)target[0] );
		}

		if ( config.supportsResume() )
		{
			target[0].resume();
		}

		return target[0];
	}

	public static IDebugTarget newAttachDebugTarget( final ILaunch launch,
													 final ICDITarget cdiTarget,
													 final String name,
													 final IProcess debuggerProcess,
													 final IFile file ) throws DebugException
	{
		final IDebugTarget[] target = new IDebugTarget[1];

		IWorkspaceRunnable r = new IWorkspaceRunnable()
		{
			public void run( IProgressMonitor m )
			{
				target[0] = new CDebugTarget( launch, 
											  ICDebugTargetType.TARGET_TYPE_LOCAL_ATTACH, 
											  cdiTarget, 
											  name,
											  null,
											  debuggerProcess,
											  file,
											  false,
											  true );
			}
		};
		try
		{
			ResourcesPlugin.getWorkspace().run( r, null );
		}
		catch( CoreException e )
		{
			CDebugCorePlugin.log( e );
			throw new DebugException( e.getStatus() );
		}

		((CDebugTarget)target[0]).handleDebugEvent( new ICDISuspendedEvent()
														{
															public ICDISessionObject getReason()
															{
																return null;
															}
	
															public ICDIObject getSource()
															{
																return cdiTarget;
															}

														} );

		return target[0];
	}

	public static IDebugTarget newCoreFileDebugTarget( final ILaunch launch,
													   final ICDITarget cdiTarget,
													   final String name,
													   final IProcess debuggerProcess,
													   final IFile file ) throws DebugException
	{
		final IDebugTarget[] target = new IDebugTarget[1];

		IWorkspaceRunnable r = new IWorkspaceRunnable()
		{
			public void run( IProgressMonitor m )
			{
				target[0] = new CDebugTarget( launch, 
											  ICDebugTargetType.TARGET_TYPE_LOCAL_CORE_DUMP, 
											  cdiTarget, 
											  name,
											  null,
											  debuggerProcess,
											  file,
											  true,
											  false );
			}
		};
		try
		{
			ResourcesPlugin.getWorkspace().run( r, null );
		}
		catch( CoreException e )
		{
			CDebugCorePlugin.log( e );
			throw new DebugException( e.getStatus() );
		}

		((CDebugTarget)target[0]).handleDebugEvent( new ICDISuspendedEvent()
														{
															public ICDISessionObject getReason()
															{
																return null;
															}
	
															public ICDIObject getSource()
															{
																return cdiTarget;
															}

														} );

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
				return new CExpression( (CDebugTarget)target, cdiExpression );
			}
			catch( CDIException e )
			{
				throw new DebugException( new Status( IStatus.ERROR, 
													  getPluginIdentifier(),
													  DebugException.TARGET_REQUEST_FAILED, 
													  e.getMessage(), 
													  null ) );
			}
		}
		return null;
	}

	public static IFormattedMemoryBlock createFormattedMemoryBlock( IDebugTarget target, 
																	String addressExpression,
																    int format,
																    int wordSize,
																    int numberOfRows,
																    int numberOfColumns, 
																    char paddingChar ) throws DebugException
	{
		if ( target != null && target instanceof CDebugTarget )
		{
			try
			{
				ICDIExpression expression = ((CDebugTarget)target).getCDISession()
																  .getExpressionManager()
																  .createExpression( addressExpression );
				ICDIMemoryBlock cdiMemoryBlock = ((CDebugTarget)target).getCDISession()
																	   .getMemoryManager()
																	   .createMemoryBlock( expression.getName(), wordSize * numberOfRows * numberOfColumns );
				return new CFormattedMemoryBlock( (CDebugTarget)target, 
												  cdiMemoryBlock,
												  expression,
												  format,
												  wordSize,
												  numberOfRows,
												  numberOfColumns,
												  paddingChar );
			}
			catch( CDIException e )
			{
				throw new DebugException( new Status( IStatus.ERROR, 
													  getPluginIdentifier(),
													  DebugException.TARGET_REQUEST_FAILED, 
													  e.getMessage(), 
													  null ) );
			}
		}
		return null;
	}

	public static IFormattedMemoryBlock createFormattedMemoryBlock( IDebugTarget target, 
																	String addressExpression,
																    int format,
																    int wordSize,
																    int numberOfRows,
																    int numberOfColumns ) throws DebugException
	{
		if ( target != null && target instanceof CDebugTarget )
		{
			try
			{
				ICDIExpression expression = ((CDebugTarget)target).getCDISession()
																  .getExpressionManager()
																  .createExpression( addressExpression );
				ICDIMemoryBlock cdiMemoryBlock = ((CDebugTarget)target).getCDISession()
																	   .getMemoryManager()
																	   .createMemoryBlock( expression.getName(), wordSize * numberOfRows * numberOfColumns );
				return new CFormattedMemoryBlock( (CDebugTarget)target, 
												  cdiMemoryBlock,
												  expression,
												  format,
												  wordSize,
												  numberOfRows,
												  numberOfColumns );
			}
			catch( CDIException e )
			{
				throw new DebugException( new Status( IStatus.ERROR, 
													  getPluginIdentifier(),
													  DebugException.TARGET_REQUEST_FAILED, 
													  e.getMessage(), 
													  null ) );
			}
		}
		return null;
	}

	private static void stopInMain( CDebugTarget target ) throws DebugException
	{
		ICDILocation location = target.getCDISession().getBreakpointManager().createLocation( "", "main", 0 );
		try
		{
			target.setInternalTemporaryBreakpoint( location );
		}
		catch( DebugException e )
		{
			String message = MessageFormat.format( "Unable to set temporary breakpoint in main.\nReason: {0}\nContinue?", new String[] { e.getStatus().getMessage() } );
			IStatus newStatus = new Status( IStatus.WARNING, 
											e.getStatus().getPlugin(), 
											ICDebugInternalConstants.STATUS_CODE_QUESTION, 
											message,
											null );
			if ( !CDebugUtils.question( newStatus, target ) )
			{
				target.terminate();
				throw new DebugException( new Status( IStatus.OK, 
													  e.getStatus().getPlugin(),
													  e.getStatus().getCode(),
													  e.getStatus().getMessage(),
													  null ) );
			}
		}
	}
}
