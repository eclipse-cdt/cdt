/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.debug.core;

import java.util.HashMap;

import org.eclipse.cdt.debug.core.model.ICAddressBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
import org.eclipse.cdt.debug.internal.core.breakpoints.CAddressBreakpoint;
import org.eclipse.cdt.debug.internal.core.breakpoints.CFunctionBreakpoint;
import org.eclipse.cdt.debug.internal.core.breakpoints.CLineBreakpoint;
import org.eclipse.cdt.debug.internal.core.breakpoints.CWatchpoint;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;

/**
 * Provides utility methods for creating debug sessions, targets and 
 * breakpoints specific to the CDI debug model.
 */
public class CDIDebugModel {
	/**
	 * Returns the identifier for the CDI debug model plug-in
	 *
	 * @return plugin identifier
	 */
	public static String getPluginIdentifier() {
		return CDebugCorePlugin.getUniqueIdentifier();
	}	

	/**
	 * Creates and returns a line breakpoint for the source defined by 
	 * the given source handle, at the given line number. The marker associated 
	 * with the breakpoint will be created on the specified resource.
	 * 
	 * @param sourceHandle the handle to the breakpoint source
	 * @param resource the resource on which to create the associated breakpoint marker
	 * @param lineNumber the line number on which the breakpoint is set - line
	 *   numbers are 1 based, associated with the source file in which the breakpoint is set
	 * @param enabled whether to enable or disable this breakpoint
	 * @param ignoreCount the number of times this breakpoint will be ignored
	 * @param condition the breakpoint condition
	 * @param register whether to add this breakpoint to the breakpoint manager
	 * @return a line breakpoint
	 * @throws CoreException if this method fails. Reasons include:<ul> 
	 * <li>Failure creating underlying marker.  The exception's status contains
	 * the underlying exception responsible for the failure.</li></ul>
	 */
	public static ICLineBreakpoint createLineBreakpoint( String sourceHandle,
														 IResource resource, 
														 int lineNumber, 
														 boolean enabled, 
														 int ignoreCount, 
														 String condition, 
														 boolean register ) throws CoreException {
		HashMap attributes = new HashMap( 10 );
		attributes.put( IBreakpoint.ID, getPluginIdentifier() );
		attributes.put( IMarker.LINE_NUMBER, new Integer( lineNumber ) );
		attributes.put( IBreakpoint.ENABLED, new Boolean( enabled ) );
		attributes.put( ICBreakpoint.IGNORE_COUNT, new Integer( ignoreCount ) );
		attributes.put( ICBreakpoint.CONDITION, condition );
		attributes.put( ICBreakpoint.SOURCE_HANDLE, sourceHandle );
		return new CLineBreakpoint( resource, attributes, register );
	}

	/**
	 * Creates and returns an address breakpoint for the source defined by 
	 * the given source handle, at the given address. The marker associated 
	 * with the breakpoint will be created on the specified resource.
	 *  
	 * @param sourceHandle the handle to the breakpoint source
	 * @param resource the resource on which to create the associated breakpoint marker
	 * @param address the address on which the breakpoint is set
	 * @param enabled whether to enable or disable this breakpoint
	 * @param ignoreCount the number of times this breakpoint will be ignored
	 * @param condition the breakpoint condition
	 * @param register whether to add this breakpoint to the breakpoint manager
	 * @return an address breakpoint
	 * @throws CoreException if this method fails. Reasons include:<ul> 
	 * <li>Failure creating underlying marker.  The exception's status contains
	 * the underlying exception responsible for the failure.</li></ul>
	 */
	public static ICAddressBreakpoint createAddressBreakpoint( String sourceHandle, 
															   IResource resource, 
															   long address, 
															   boolean enabled, 
															   int ignoreCount, 
															   String condition, 
															   boolean register ) throws CoreException {
		HashMap attributes = new HashMap( 10 );
		attributes.put( IBreakpoint.ID, getPluginIdentifier() );
		attributes.put( IMarker.CHAR_START, new Integer( 0 ) );
		attributes.put( IMarker.CHAR_END, new Integer( 0 ) );
		attributes.put( IMarker.LINE_NUMBER, new Integer( -1 ) );
		attributes.put( IMarker.LINE_NUMBER, new Integer( -1 ) );
		attributes.put( ICAddressBreakpoint.ADDRESS, Long.toString( address ) );
		attributes.put( IBreakpoint.ENABLED, new Boolean( enabled ) );
		attributes.put( ICBreakpoint.IGNORE_COUNT, new Integer( ignoreCount ) );
		attributes.put( ICBreakpoint.CONDITION, condition );
		attributes.put( ICBreakpoint.SOURCE_HANDLE, sourceHandle );
		return new CAddressBreakpoint( resource, attributes, register );
	}

	/**
	 * Creates and returns a watchpoint for the source defined by 
	 * the given source handle, at the given expression. The marker associated 
	 * with the watchpoint will be created on the specified resource.
	 * 
	 * @param sourceHandle the handle to the watchpoint source
	 * @param resource the resource on which to create the associated watchpoint marker
	 * @param writeAccess whether this is write watchpoint
	 * @param readAccess whether this is read watchpoint
	 * @param expression the expression on which the watchpoint is set
	 * @param enabled whether to enable or disable this breakpoint
	 * @param ignoreCount the number of times this breakpoint will be ignored
	 * @param condition the breakpoint condition
	 * @param register whether to add this breakpoint to the breakpoint manager
	 * @return a watchpoint
	 * @throws CoreException if this method fails. Reasons include:<ul> 
	 * <li>Failure creating underlying marker.  The exception's status contains
	 * the underlying exception responsible for the failure.</li></ul>
	 */
	public static ICWatchpoint createWatchpoint( String sourceHandle, 
												 IResource resource, 
												 boolean writeAccess, 
												 boolean readAccess, 
												 String expression, 
												 boolean enabled, 
												 int ignoreCount, 
												 String condition, 
												 boolean register ) throws CoreException {
		HashMap attributes = new HashMap( 10 );
		attributes.put( IBreakpoint.ID, getPluginIdentifier() );
		attributes.put( IBreakpoint.ENABLED, new Boolean( enabled ) );
		attributes.put( ICBreakpoint.IGNORE_COUNT, new Integer( ignoreCount ) );
		attributes.put( ICBreakpoint.CONDITION, condition );
		attributes.put( ICBreakpoint.SOURCE_HANDLE, sourceHandle );
		attributes.put( ICWatchpoint.EXPRESSION, expression );
		attributes.put( ICWatchpoint.READ, new Boolean( readAccess ) );
		attributes.put( ICWatchpoint.WRITE, new Boolean( writeAccess ) );
		return new CWatchpoint( resource, attributes, register );
	}

	/**
	 * Creates and returns a breakpoint for the function defined by 
	 * the given name. The marker associated with the breakpoint will 
	 * be created on the specified resource.
	 *  
	 * @param sourceHandle the handle to the breakpoint source
	 * @param resource the resource on which to create the associated breakpoint marker
	 * @param function the name of the function this breakpoint suspends execution in
	 * @param charStart the first character index associated with the breakpoint,
	 *   or -1 if unspecified, in the source file in which the breakpoint is set
 	 * @param charEnd the last character index associated with the breakpoint,
	 *   or -1 if unspecified, in the source file in which the breakpoint is set
	 * @param lineNumber the lineNumber on which the breakpoint is set, or -1 if 
	 *   unspecified - line numbers are 1 based, associated with the source file 
	 *   in which the breakpoint is set
	 * @param enabled whether to enable or disable this breakpoint
	 * @param ignoreCount the number of times this breakpoint will be ignored
	 * @param condition the breakpoint condition
	 * @param register whether to add this breakpoint to the breakpoint manager
	 * @return an address breakpoint
	 * @throws CoreException if this method fails. Reasons include:<ul> 
	 * <li>Failure creating underlying marker.  The exception's status contains
	 * the underlying exception responsible for the failure.</li></ul>
	 */
	public static ICFunctionBreakpoint createFunctionBreakpoint( String sourceHandle, 
			 													 IResource resource,
																 String function,
																 int charStart,
																 int charEnd,
																 int lineNumber,
																 boolean enabled, 
																 int ignoreCount, 
																 String condition, 
																 boolean register ) throws CoreException {
		HashMap attributes = new HashMap( 10 );
		attributes.put( IBreakpoint.ID, getPluginIdentifier() );
		attributes.put( IMarker.CHAR_START, new Integer( charStart ) );
		attributes.put( IMarker.CHAR_END, new Integer( charEnd ) );
		attributes.put( IMarker.LINE_NUMBER, new Integer( lineNumber ) );
		attributes.put( ICFunctionBreakpoint.FUNCTION, function );
		attributes.put( IBreakpoint.ENABLED, new Boolean( enabled ) );
		attributes.put( ICBreakpoint.IGNORE_COUNT, new Integer( ignoreCount ) );
		attributes.put( ICBreakpoint.CONDITION, condition );
		attributes.put( ICBreakpoint.SOURCE_HANDLE, sourceHandle );
		return new CFunctionBreakpoint( resource, attributes, register );
	}

	/**
	 * Returns the line breakpoint that is already registered with the breakpoint
	 * manager for a source with the given handle and the given resource at the 
	 * given line number.
	 * 
	 * @param sourceHandle the source handle
	 * @param resource the breakpoint resource
	 * @param lineNumber the line number
	 * @return the line breakpoint that is already registered with the breakpoint
	 *  manager or <code>null</code> if no such breakpoint is registered
	 * @exception CoreException if unable to retrieve the associated marker
	 * 	attributes (line number).
	 */
	public static ICLineBreakpoint lineBreakpointExists( String sourceHandle, IResource resource, int lineNumber ) throws CoreException {
		String modelId = getPluginIdentifier();
		String markerType = CLineBreakpoint.getMarkerType();
		IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
		IBreakpoint[] breakpoints = manager.getBreakpoints( modelId );
		for( int i = 0; i < breakpoints.length; i++ ) {
			if ( !(breakpoints[i] instanceof ICLineBreakpoint) ) {
				continue;
			}
			ICLineBreakpoint breakpoint = (ICLineBreakpoint)breakpoints[i];
			if ( breakpoint.getMarker().getType().equals( markerType ) ) {
				if ( sourceHandle != null && sourceHandle.equals( breakpoint.getSourceHandle() ) ) {
					if ( breakpoint.getMarker().getResource().equals( resource ) ) {
						if ( breakpoint.getLineNumber() == lineNumber ) {
							return breakpoint;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns the address breakpoint that is already registered with the breakpoint
	 * manager for a source with the given handle and the given resource at the 
	 * given address.
	 *  
	 * @param sourceHandle the source handle
	 * @param resource the breakpoint resource
	 * @param address the address
	 * @return the address breakpoint that is already registered with the breakpoint
	 *  manager or <code>null</code> if no such breakpoint is registered
	 * @exception CoreException if unable to retrieve the associated marker
	 * 	attributes (line number).
	 */
	public static ICAddressBreakpoint addressBreakpointExists( String sourceHandle, IResource resource, long address ) throws CoreException {
		String modelId = getPluginIdentifier();
		String markerType = CAddressBreakpoint.getMarkerType();
		IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
		IBreakpoint[] breakpoints = manager.getBreakpoints( modelId );
		for( int i = 0; i < breakpoints.length; i++ ) {
			if ( !(breakpoints[i] instanceof ICAddressBreakpoint) ) {
				continue;
			}
			ICAddressBreakpoint breakpoint = (ICAddressBreakpoint)breakpoints[i];
			if ( breakpoint.getMarker().getType().equals( markerType ) ) {
				if ( sourceHandle != null && sourceHandle.equals( breakpoint.getSourceHandle() ) ) {
					if ( breakpoint.getMarker().getResource().equals( resource ) ) {
						try {
							if ( Long.parseLong( breakpoint.getAddress() ) == address ) {
								return breakpoint;
							}
						}
						catch( NumberFormatException e ) {
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns the watchpoint that is already registered with the breakpoint
	 * manager for a source with the given handle and the given resource at the 
	 * given expression.
	 *  
	 * @param sourceHandle the source handle
	 * @param resource the breakpoint resource
	 * @param expression the expression
	 * @return the watchpoint that is already registered with the breakpoint
	 *  manager or <code>null</code> if no such watchpoint is registered
	 * @exception CoreException if unable to retrieve the associated marker
	 * 	attributes (line number).
	 */
	public static ICWatchpoint watchpointExists( String sourceHandle, IResource resource, String expression ) throws CoreException {
		String modelId = getPluginIdentifier();
		String markerType = CWatchpoint.getMarkerType();
		IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
		IBreakpoint[] breakpoints = manager.getBreakpoints( modelId );
		for( int i = 0; i < breakpoints.length; i++ ) {
			if ( !(breakpoints[i] instanceof ICWatchpoint) ) {
				continue;
			}
			ICWatchpoint breakpoint = (ICWatchpoint)breakpoints[i];
			if ( breakpoint.getMarker().getType().equals( markerType ) ) {
				if ( sourceHandle != null && sourceHandle.equals( breakpoint.getSourceHandle() ) ) {
					if ( breakpoint.getMarker().getResource().equals( resource ) ) {
						if ( breakpoint.getExpression().equals( expression ) ) {
							return breakpoint;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns the function breakpoint that is already registered with the breakpoint
	 * manager for a source with the given handle and the given resource with the 
	 * given function name.
	 *  
	 * @param sourceHandle the source handle
	 * @param resource the breakpoint resource
	 * @param function the fully qualified function name
	 * @return the breakpoint that is already registered with the breakpoint
	 *  manager or <code>null</code> if no such breakpoint is registered
	 * @exception CoreException if unable to retrieve the associated marker
	 * 	attributes (line number).
	 */
	public static ICFunctionBreakpoint functionBreakpointExists( String sourceHandle, IResource resource, String function ) throws CoreException {
		String modelId = getPluginIdentifier();
		String markerType = CFunctionBreakpoint.getMarkerType();
		IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
		IBreakpoint[] breakpoints = manager.getBreakpoints( modelId );
		for( int i = 0; i < breakpoints.length; i++ ) {
			if ( !(breakpoints[i] instanceof ICFunctionBreakpoint) ) {
				continue;
			}
			ICFunctionBreakpoint breakpoint = (ICFunctionBreakpoint)breakpoints[i];
			if ( breakpoint.getMarker().getType().equals( markerType ) ) {
				if ( sourceHandle != null && sourceHandle.equals( breakpoint.getSourceHandle() ) ) {
					if ( breakpoint.getMarker().getResource().equals( resource ) ) {
						if ( breakpoint.getFunction() != null && breakpoint.getFunction().equals( function ) ) {
							return breakpoint;
						}
					}
				}
			}
		}
		return null;
	}
}
