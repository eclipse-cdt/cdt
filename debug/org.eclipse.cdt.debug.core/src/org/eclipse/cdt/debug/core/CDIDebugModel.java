/*******************************************************************************
 * Copyright (c) 2004, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Freescale Semiconductor - Address watchpoints, https://bugs.eclipse.org/bugs/show_bug.cgi?id=118299
 * QNX Software Systems - catchpoints - bug 226689
 * Ericsson             - tracepoints - bug 284286
 *******************************************************************************/
package org.eclipse.cdt.debug.core;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryExecutable;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.settings.model.ICConfigExtensionReference;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.model.ICAddressBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpoint2;
import org.eclipse.cdt.debug.core.model.ICBreakpointType;
import org.eclipse.cdt.debug.core.model.ICEventBreakpoint;
import org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint2;
import org.eclipse.cdt.debug.core.model.ICTracepoint;
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
import org.eclipse.cdt.debug.core.model.ICWatchpoint2;
import org.eclipse.cdt.debug.internal.core.breakpoints.CAddressBreakpoint;
import org.eclipse.cdt.debug.internal.core.breakpoints.CAddressTracepoint;
import org.eclipse.cdt.debug.internal.core.breakpoints.CEventBreakpoint;
import org.eclipse.cdt.debug.internal.core.breakpoints.CFunctionBreakpoint;
import org.eclipse.cdt.debug.internal.core.breakpoints.CFunctionTracepoint;
import org.eclipse.cdt.debug.internal.core.breakpoints.CLineBreakpoint;
import org.eclipse.cdt.debug.internal.core.breakpoints.CLineTracepoint;
import org.eclipse.cdt.debug.internal.core.breakpoints.CWatchpoint;
import org.eclipse.cdt.debug.internal.core.model.CDebugTarget;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IProcess;

/**
 * Provides utility methods for creating debug sessions, targets and breakpoints
 * specific to the CDI debug model.
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
     * Creates and returns a debug target for the given CDI target, with the
     * specified name, and associates it with the given process for console I/O.
     * The debug target is added to the given launch.
     * 
     * @param launch
     *            the launch the new debug target will be contained in
     * @param project
     *            the project to use to persist breakpoints.
     * @param cdiTarget
     *            the CDI target to create a debug target for
     * @param name
     *            the name to associate with this target, which will be returned
     *            from <code>IDebugTarget.getName</code>.
     * @param debuggeeProcess
     *            the process to associate with the debug target, which will be
     *            returned from <code>IDebugTarget.getProcess</code>
     * @param file
     *            the executable to debug.
     * @param allowTerminate
     *            allow terminate().
     * @param allowDisconnect
     *            allow disconnect().
     * @param stopSymbol
     *            place temporary breakpoint at <code>stopSymbol</code>, ignore
     *            if <code>null</code> or empty.
     * @param resumeTarget
     *            resume target.
     * @return a debug target
     * @throws DebugException
     * @since 3.1
     */
    public static IDebugTarget newDebugTarget(final ILaunch launch, final IProject project, final ICDITarget cdiTarget,
        final String name, final IProcess debuggeeProcess, final IBinaryObject file, final boolean allowTerminate,
        final boolean allowDisconnect, final String stopSymbol, final boolean resumeTarget) throws DebugException {
        final IDebugTarget[] target = new IDebugTarget[1];
        IWorkspaceRunnable r = new IWorkspaceRunnable() {

            @Override
            public void run(IProgressMonitor m) throws CoreException {
                target[0] = new CDebugTarget(launch, project, cdiTarget, name, debuggeeProcess, file, allowTerminate,
                    allowDisconnect);
                ((CDebugTarget) target[0]).start(stopSymbol, resumeTarget);
            }
        };
        try {
            ResourcesPlugin.getWorkspace().run(r, null);
        } catch (CoreException e) {
            CDebugCorePlugin.log(e);
            throw new DebugException(e.getStatus());
        }
        return target[0];
    }

    /**
     * Creates and returns a debug target for the given CDI target, with the
     * specified name, and associates it with the given process for console I/O.
     * The debug target is added to the given launch.
     * 
     * @param launch
     *            the launch the new debug target will be contained in
     * @param project
     *            the project to use to persist breakpoints.
     * @param cdiTarget
     *            the CDI target to create a debug target for
     * @param name
     *            the name to associate with this target, which will be returned
     *            from <code>IDebugTarget.getName</code>.
     * @param debuggeeProcess
     *            the process to associate with the debug target, which will be
     *            returned from <code>IDebugTarget.getProcess</code>
     * @param file
     *            the executable to debug.
     * @param allowTerminate
     *            allow terminate().
     * @param allowDisconnect
     *            allow disconnect().
     * @param stopInMain
     *            place temporary breakpoint at main()
     * @param resumeTarget
     *            resume target.
     * @return a debug target
     * @throws DebugException
     * @deprecated
     */
    @Deprecated
    public static IDebugTarget newDebugTarget(final ILaunch launch, final IProject project, final ICDITarget cdiTarget,
        final String name, final IProcess debuggeeProcess, final IBinaryObject file, final boolean allowTerminate,
        final boolean allowDisconnect, final boolean stopInMain, final boolean resumeTarget) throws DebugException {
        final IDebugTarget[] target = new IDebugTarget[1];
        IWorkspaceRunnable r = new IWorkspaceRunnable() {

            @Override
            public void run(IProgressMonitor m) throws CoreException {
                String stopSymbol = null;
                if (stopInMain)
                    stopSymbol = launch.getLaunchConfiguration().getAttribute(
                        ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL,
                        ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_SYMBOL_DEFAULT);
                target[0] = new CDebugTarget(launch, project, cdiTarget, name, debuggeeProcess, file, allowTerminate,
                    allowDisconnect);
                ((CDebugTarget) target[0]).start(stopSymbol, resumeTarget);
            }
        };
        try {
            ResourcesPlugin.getWorkspace().run(r, null);
        } catch (CoreException e) {
            CDebugCorePlugin.log(e);
            throw new DebugException(e.getStatus());
        }
        return target[0];
    }

    /**
     * Creates and returns a debug target for the given CDI target, with the
     * specified name, and associates it with the given process for console I/O.
     * The debug target is added to the given launch.
     * 
     * @param launch
     *            the launch the new debug target will be contained in
     * @param project
     *            the project to use to persist breakpoints.
     * @param cdiTarget
     *            the CDI target to create a debug target for
     * @param name
     *            the name to associate with this target, which will be returned
     *            from <code>IDebugTarget.getName</code>.
     * @param debuggeeProcess
     *            the process to associate with the debug target, which will be
     *            returned from <code>IDebugTarget.getProcess</code>
     * @param file
     *            the executable to debug.
     * @param allowTerminate
     *            allow terminate().
     * @param allowDisconnect
     *            allow disconnect().
     * @param resumeTarget
     *            resume target.
     * @return a debug target
     * @throws DebugException
     */
    public static IDebugTarget newDebugTarget(ILaunch launch, IProject project, ICDITarget cdiTarget,
        final String name, IProcess debuggeeProcess, IBinaryObject file, boolean allowTerminate,
        boolean allowDisconnect, boolean resumeTarget) throws DebugException {
        return newDebugTarget(launch, project, cdiTarget, name, debuggeeProcess, file, allowTerminate, allowDisconnect,
            null, resumeTarget);
    }

    /**
     * Calculates breakpoint marker ID based on the breakpoint object type.
     * 
     * @since 7.2
     */
    public static String calculateMarkerType(IBreakpoint bp) {
        if (bp instanceof ICBreakpoint2) {
            return ((ICBreakpoint2) bp).getMarkerType();
        }
        if (bp instanceof ICTracepoint) {
            if (bp instanceof ICFunctionBreakpoint) {
                return ICTracepoint.C_FUNCTION_TRACEPOINT_MARKER;
            } else if (bp instanceof ICAddressBreakpoint) {
                return ICTracepoint.C_ADDRESS_TRACEPOINT_MARKER;
            } else if (bp instanceof ICLineBreakpoint) {
                return ICTracepoint.C_LINE_TRACEPOINT_MARKER;
            } else {
                return ICTracepoint.C_TRACEPOINT_MARKER;
            }
        } else if (bp instanceof ICFunctionBreakpoint) {
            return ICFunctionBreakpoint.C_FUNCTION_BREAKPOINT_MARKER;
        } else if (bp instanceof ICAddressBreakpoint) {
            return ICAddressBreakpoint.C_ADDRESS_BREAKPOINT_MARKER;
        } else if (bp instanceof ICLineBreakpoint) {
            return ICLineBreakpoint.C_LINE_BREAKPOINT_MARKER;
        } else if (bp instanceof ICEventBreakpoint) {
            return ICEventBreakpoint.C_EVENT_BREAKPOINT_MARKER;
        } else if (bp instanceof ICBreakpoint) {
            return ICBreakpoint.C_BREAKPOINT_MARKER;
        } else if (bp instanceof ILineBreakpoint) {
            return IBreakpoint.LINE_BREAKPOINT_MARKER;
        }
        return IBreakpoint.BREAKPOINT_MARKER;
    }

    /**
     * Creates and returns a line breakpoint for the source defined by the given
     * source handle, at the given line number. The marker associated with the
     * breakpoint will be created on the specified resource.
     * 
     * @param sourceHandle
     *            the handle to the breakpoint source
     * @param resource
     *            the resource on which to create the associated breakpoint
     *            marker
     * @param lineNumber
     *            the line number on which the breakpoint is set - line numbers
     *            are 1 based, associated with the source file in which the
     *            breakpoint is set
     * @param enabled
     *            whether to enable or disable this breakpoint
     * @param ignoreCount
     *            the number of times this breakpoint will be ignored
     * @param condition
     *            the breakpoint condition
     * @param register
     *            whether to add this breakpoint to the breakpoint manager
     * @return a line breakpoint
     * @throws CoreException
     *             if this method fails. Reasons include:
     *             <ul>
     *             <li>Failure creating underlying marker. The exception's
     *             status contains the underlying exception responsible for the
     *             failure.</li>
     *             </ul>
     * @deprecated as of CDT 5.0 use
     *             {@link #createLineBreakpoint(String, IResource, int, int, boolean, int, String, boolean)}
     */
    @Deprecated
    public static ICLineBreakpoint createLineBreakpoint(String sourceHandle, IResource resource, int lineNumber,
        boolean enabled, int ignoreCount, String condition, boolean register) throws CoreException {
        return createLineBreakpoint(sourceHandle, resource, ICBreakpointType.REGULAR, lineNumber, enabled, ignoreCount,
            condition, register);
    }

    /**
     * Creates and returns a line breakpoint for the source defined by the given
     * source handle, at the given line number. The marker associated with the
     * breakpoint will be created on the specified resource.
     * 
     * @param sourceHandle
     *            the handle to the breakpoint source
     * @param resource
     *            the resource on which to create the associated breakpoint
     *            marker
     * @param type
     *            a type constant from ICBreakpointType
     * @param lineNumber
     *            the line number on which the breakpoint is set - line numbers
     *            are 1 based, associated with the source file in which the
     *            breakpoint is set
     * @param enabled
     *            whether to enable or disable this breakpoint
     * @param ignoreCount
     *            the number of times this breakpoint will be ignored
     * @param condition
     *            the breakpoint condition
     * @param register
     *            whether to add this breakpoint to the breakpoint manager
     * @return a line breakpoint
     * @throws CoreException
     *             if this method fails. Reasons include:
     *             <ul>
     *             <li>Failure creating underlying marker. The exception's
     *             status contains the underlying exception responsible for the
     *             failure.</li>
     *             </ul>
     */
    public static ICLineBreakpoint createLineBreakpoint(String sourceHandle, IResource resource, int type,
        int lineNumber, boolean enabled, int ignoreCount, String condition, boolean register) throws CoreException {
        HashMap<String, Object> attributes = new HashMap<String, Object>(10);
        setLineBreakpointAttributes(attributes, sourceHandle, type, lineNumber, enabled, ignoreCount, condition);
        return new CLineBreakpoint(resource, attributes, register);
    }

    /**
     * Creates a breakpoint without associated marker.
     * <p>
     * Note: Before a breakpoint created using this method can be used, the
     * client must first create a marker and register the breakpoint. The former
     * is accomplished using {@link IBreakpoint#setMarker(IMarker)}, the latter
     * using {@link IBreakpointManager#addBreakpoint(IBreakpoint)}.
     * 
     * @since 7.2
     */
    public static ICLineBreakpoint createBlankLineBreakpoint() {
        return new CLineBreakpoint();
    }

    /**
     * @since 7.0
     */
    public static ICLineBreakpoint createLineTracepoint(String sourceHandle, IResource resource, int type,
        int lineNumber, boolean enabled, int ignoreCount, String condition, boolean register) throws CoreException {
        HashMap<String, Object> attributes = new HashMap<String, Object>(10);
        setLineBreakpointAttributes(attributes, sourceHandle, type, lineNumber, enabled, ignoreCount, condition);
        return new CLineTracepoint(resource, attributes, register);
    }

    /**
     * Creates a breakpoint without associated marker.
     * <p>
     * Note: Before a breakpoint created using this method can be used, the
     * client must first create a marker and register the breakpoint. The former
     * is accomplished using {@link IBreakpoint#setMarker(IMarker)}, the latter
     * using {@link IBreakpointManager#addBreakpoint(IBreakpoint)}.
     * 
     * @since 7.2
     */
    public static ICLineBreakpoint createBlankLineTracepoint() {
        return new CLineTracepoint();
    }

    /**
     * Helper function for setting common line breakpoint attributes.
     * 
     * @param attributes
     *            Map to write the attributes into.
     * @param sourceHandle
     *            The handle to the breakpoint source.
     * @param resource
     *            The resource on which to create the associated breakpoint
     *            marker.
     * @param type
     *            A type constant from ICBreakpointType.
     * @param lineNumber
     *            The line number on which the breakpoint is set - line numbers
     *            are 1 based, associated with the source file in which the
     *            breakpoint is set.
     * @param enabled
     *            Whether to enable or disable this breakpoint.
     * @param ignoreCount
     *            The number of times this breakpoint will be ignored.
     * @param condition
     *            The breakpoint condition.
     * @param register
     *            Whether to add this breakpoint to the breakpoint manager.
     * 
     * @since 7.2
     */
    public static void setLineBreakpointAttributes(Map<String, Object> attributes, String sourceHandle, Integer type,
        int lineNumber, boolean enabled, int ignoreCount, String condition) {
        attributes.put(IBreakpoint.ID, getPluginIdentifier());
        attributes.put(IMarker.LINE_NUMBER, new Integer(lineNumber));
        attributes.put(IBreakpoint.ENABLED, Boolean.valueOf(enabled));
        attributes.put(ICBreakpoint.IGNORE_COUNT, new Integer(ignoreCount));
        attributes.put(ICBreakpoint.CONDITION, condition);
        attributes.put(ICBreakpoint.SOURCE_HANDLE, sourceHandle);
        attributes.put(ICBreakpointType.TYPE, type);

        // Added for source relocated breakpoints.
        if (!attributes.containsKey(ICLineBreakpoint2.REQUESTED_SOURCE_HANDLE)) {
            attributes.put(ICLineBreakpoint2.REQUESTED_SOURCE_HANDLE, sourceHandle);
        }
        if (!attributes.containsKey(ICLineBreakpoint2.REQUESTED_LINE)) {
            attributes.put(ICLineBreakpoint2.REQUESTED_LINE, new Integer(lineNumber));
        }
        if (attributes.containsKey(IMarker.CHAR_START)
            && !attributes.containsKey(ICLineBreakpoint2.REQUESTED_CHAR_START)) {
            attributes.put(ICLineBreakpoint2.REQUESTED_CHAR_START, attributes.get(IMarker.CHAR_START));
        }
        if (attributes.containsKey(IMarker.CHAR_END) && !attributes.containsKey(ICLineBreakpoint2.REQUESTED_CHAR_END)) {
            attributes.put(ICLineBreakpoint2.REQUESTED_CHAR_END, attributes.get(IMarker.CHAR_END));
        }
    }

    /**
     * Creates and returns an address breakpoint for the source defined by the
     * given source handle, at the given address. The marker associated with the
     * breakpoint will be created on the specified resource.
     * 
     * @param module
     *            the module name the breakpoint is set in
     * @param sourceHandle
     *            the handle to the breakpoint source
     * @param resource
     *            the resource on which to create the associated breakpoint
     *            marker
     * @param address
     *            the address on which the breakpoint is set
     * @param enabled
     *            whether to enable or disable this breakpoint
     * @param ignoreCount
     *            the number of times this breakpoint will be ignored
     * @param condition
     *            the breakpoint condition
     * @param register
     *            whether to add this breakpoint to the breakpoint manager
     * @return an address breakpoint
     * @throws CoreException
     *             if this method fails. Reasons include:
     *             <ul>
     *             <li>Failure creating underlying marker. The exception's
     *             status contains the underlying exception responsible for the
     *             failure.</li>
     *             </ul>
     * @deprecated as of CDT 5.0 use
     *             {@link #createAddressBreakpoint(String, String, IResource, int, int, IAddress, boolean, int, String, boolean)}
     */
    @Deprecated
    public static ICAddressBreakpoint createAddressBreakpoint(String module, String sourceHandle, IResource resource,
        IAddress address, boolean enabled, int ignoreCount, String condition, boolean register) throws CoreException {
        return createAddressBreakpoint(module, sourceHandle, resource, ICBreakpointType.REGULAR, -1, address, enabled,
            ignoreCount, condition, register);
    }

    /**
     * Creates and returns an address breakpoint for the source defined by the
     * given source handle, at the given address. The marker associated with the
     * breakpoint will be created on the specified resource.
     * 
     * @param module
     *            the module name the breakpoint is set in
     * @param sourceHandle
     *            the handle to the breakpoint source
     * @param resource
     *            the resource on which to create the associated breakpoint
     *            marker
     * @param type
     *            a type constant from ICBreakpointType
     * @param address
     *            the address on which the breakpoint is set
     * @param enabled
     *            whether to enable or disable this breakpoint
     * @param ignoreCount
     *            the number of times this breakpoint will be ignored
     * @param condition
     *            the breakpoint condition
     * @param register
     *            whether to add this breakpoint to the breakpoint manager
     * @return an address breakpoint
     * @throws CoreException
     *             if this method fails. Reasons include:
     *             <ul>
     *             <li>Failure creating underlying marker. The exception's
     *             status contains the underlying exception responsible for the
     *             failure.</li>
     *             </ul>
     */
    public static ICAddressBreakpoint createAddressBreakpoint(String module, String sourceHandle, IResource resource,
        int type, IAddress address, boolean enabled, int ignoreCount, String condition, boolean register)
        throws CoreException {
        return createAddressBreakpoint(module, sourceHandle, resource, type, -1, address, enabled, ignoreCount,
            condition, register);
    }

    /**
     * Creates and returns an address breakpoint for the source defined by the
     * given source handle, at the given address. The marker associated with the
     * breakpoint will be created on the specified resource.
     * 
     * @param module
     *            the module name the breakpoint is set in
     * @param sourceHandle
     *            the handle to the breakpoint source
     * @param resource
     *            the resource on which to create the associated breakpoint
     *            marker
     * @param type
     *            a type constant from ICBreakpointType
     * @param lineNumber
     *            the line number in the source file
     * @param address
     *            the address on which the breakpoint is set
     * @param enabled
     *            whether to enable or disable this breakpoint
     * @param ignoreCount
     *            the number of times this breakpoint will be ignored
     * @param condition
     *            the breakpoint condition
     * @param register
     *            whether to add this breakpoint to the breakpoint manager
     * @return an address breakpoint
     * @throws CoreException
     *             if this method fails. Reasons include:
     *             <ul>
     *             <li>Failure creating underlying marker. The exception's
     *             status contains the underlying exception responsible for the
     *             failure.</li>
     *             </ul>
     */
    public static ICAddressBreakpoint createAddressBreakpoint(String module, String sourceHandle, IResource resource,
        int type, int lineNumber, IAddress address, boolean enabled, int ignoreCount, String condition, boolean register)
        throws CoreException {
        HashMap<String, Object> attributes = new HashMap<String, Object>(10);
        setAddressBreakpointAttributes(attributes, module, sourceHandle, type, lineNumber, address, enabled,
            ignoreCount, condition);
        return new CAddressBreakpoint(resource, attributes, register);
    }

    /**
     * Creates a breakpoint without associated marker.
     * <p>
     * Note: Before a breakpoint created using this method can be used, the
     * client must first create a marker and register the breakpoint. The former
     * is accomplished using {@link IBreakpoint#setMarker(IMarker)}, the latter
     * using {@link IBreakpointManager#addBreakpoint(IBreakpoint)}.
     * 
     * @since 7.2
     */
    public static ICAddressBreakpoint createBlankAddressBreakpoint() {
        return new CAddressBreakpoint();
    }

    /**
     * @since 7.0
     */
    public static ICAddressBreakpoint createAddressTracepoint(String module, String sourceHandle, IResource resource,
        int type, int lineNumber, IAddress address, boolean enabled, int ignoreCount, String condition, boolean register)
        throws CoreException {
        HashMap<String, Object> attributes = new HashMap<String, Object>(10);
        setAddressBreakpointAttributes(attributes, module, sourceHandle, type, lineNumber, address, enabled,
            ignoreCount, condition);
        return new CAddressTracepoint(resource, attributes, register);
    }

    /**
     * Creates a breakpoint without associated marker.
     * <p>
     * Note: Before a breakpoint created using this method can be used, the
     * client must first create a marker and register the breakpoint. The former
     * is accomplished using {@link IBreakpoint#setMarker(IMarker)}, the latter
     * using {@link IBreakpointManager#addBreakpoint(IBreakpoint)}.
     * 
     * @since 7.2
     */
    public static ICAddressBreakpoint createBlankAddressTracepoint() {
        return new CAddressTracepoint();
    }

    /**
     * Helper function for setting common address breakpoint attributes.
     * 
     * @param attributes
     *            Map to write the attributes into.
     * @param module
     *            the module name the breakpoint is set in
     * @param sourceHandle
     *            the handle to the breakpoint source
     * @param resource
     *            the resource on which to create the associated breakpoint
     *            marker
     * @param type
     *            a type constant from ICBreakpointType
     * @param lineNumber
     *            the line number in the source file
     * @param address
     *            the address on which the breakpoint is set
     * @param enabled
     *            whether to enable or disable this breakpoint
     * @param ignoreCount
     *            the number of times this breakpoint will be ignored
     * @param condition
     *            the breakpoint condition
     * @param register
     *            whether to add this breakpoint to the breakpoint manager
     * 
     * @since 7.2
     */
    public static void setAddressBreakpointAttributes(Map<String, Object> attributes, String module,
        String sourceHandle, int type, int lineNumber, IAddress address, boolean enabled, int ignoreCount,
        String condition) {
        setLineBreakpointAttributes(attributes, sourceHandle, type, lineNumber, enabled, ignoreCount, condition);
        attributes.put(IMarker.CHAR_START, new Integer(-1));
        attributes.put(IMarker.CHAR_END, new Integer(-1));
        attributes.put(ICLineBreakpoint.ADDRESS, address.toHexAddressString());
        attributes.put(ICBreakpoint.MODULE, module);
    }

    /**
     * Creates a breakpoint without associated marker.
     * <p>
     * Note: Before a breakpoint created using this method can be used, the
     * client must first create a marker and register the breakpoint. The former
     * is accomplished using {@link IBreakpoint#setMarker(IMarker)}, the latter
     * using {@link IBreakpointManager#addBreakpoint(IBreakpoint)}.
     * 
     * @since 7.2
     */
    public static ICWatchpoint createBlankWatchpoint() {
        return new CWatchpoint();
    }

    /**
     * Creates and returns a watchpoint for the source defined by the given
     * source handle, at the given expression. The marker associated with the
     * watchpoint will be created on the specified resource.
     * 
     * @param sourceHandle
     *            the handle to the watchpoint source
     * @param resource
     *            the resource on which to create the associated watchpoint
     *            marker
     * @param writeAccess
     *            whether this is write watchpoint
     * @param readAccess
     *            whether this is read watchpoint
     * @param expression
     *            the expression on which the watchpoint is set
     * @param enabled
     *            whether to enable or disable this breakpoint
     * @param ignoreCount
     *            the number of times this breakpoint will be ignored
     * @param condition
     *            the breakpoint condition
     * @param register
     *            whether to add this breakpoint to the breakpoint manager
     * @return a watchpoint
     * @throws CoreException
     *             if this method fails. Reasons include:
     *             <ul>
     *             <li>Failure creating underlying marker. The exception's
     *             status contains the underlying exception responsible for the
     *             failure.</li>
     *             </ul>
     */
    public static ICWatchpoint createWatchpoint(String sourceHandle, IResource resource, boolean writeAccess,
        boolean readAccess, String expression, boolean enabled, int ignoreCount, String condition, boolean register)
        throws CoreException {
        HashMap<String, Object> attributes = new HashMap<String, Object>(10);
        setWatchPointAttributes(attributes, sourceHandle, resource, writeAccess, readAccess, expression, "", //$NON-NLS-1$ 
            BigInteger.ZERO, enabled, ignoreCount, condition);
        return new CWatchpoint(resource, attributes, register);
    }

    /**
     * Creates and returns a watchpoint for the source defined by the given
     * source handle, at the given expression. The marker associated with the
     * watchpoint will be created on the specified resource.
     * 
     * @param sourceHandle
     *            the handle to the watchpoint source
     * @param resource
     *            the resource on which to create the associated watchpoint
     *            marker
     * @param charStart
     *            the first character index associated with the watchpoint, or
     *            -1 if unspecified, in the source file in which the watchpoint
     *            is set
     * @param charEnd
     *            the last character index associated with the watchpoint, or -1
     *            if unspecified, in the source file in which the watchpoint is
     *            set
     * @param lineNumber
     *            the lineNumber on which the watchpoint is set, or -1 if
     *            unspecified - line numbers are 1 based, associated with the
     *            source file in which the watchpoint is set
     * @param writeAccess
     *            whether this is write watchpoint
     * @param readAccess
     *            whether this is read watchpoint
     * @param expression
     *            the expression on which the watchpoint is set
     * @param memorySpace
     *            the memory space in which the watchpoint is set
     * @param range
     *            the range of the watchpoint in addressable units
     * @param enabled
     *            whether to enable or disable this breakpoint
     * @param ignoreCount
     *            the number of times this breakpoint will be ignored
     * @param condition
     *            the breakpoint condition
     * @param register
     *            whether to add this breakpoint to the breakpoint manager
     * @return a watchpoint
     * @throws CoreException
     *             if this method fails. Reasons include:
     *             <ul>
     *             <li>Failure creating underlying marker. The exception's
     *             status contains the underlying exception responsible for the
     *             failure.</li>
     *             </ul>
     */
    public static ICWatchpoint createWatchpoint(String sourceHandle, IResource resource, int charStart, int charEnd,
        int lineNumber, boolean writeAccess, boolean readAccess, String expression, String memorySpace,
        BigInteger range, boolean enabled, int ignoreCount, String condition, boolean register) throws CoreException {
        HashMap<String, Object> attributes = new HashMap<String, Object>(10);
        setWatchPointAttributes(attributes, sourceHandle, resource, writeAccess, readAccess, expression, memorySpace,
            range, enabled, ignoreCount, condition);
        attributes.put(IMarker.CHAR_START, new Integer(charStart));
        attributes.put(IMarker.CHAR_END, new Integer(charEnd));
        attributes.put(IMarker.LINE_NUMBER, new Integer(lineNumber));
        return new CWatchpoint(resource, attributes, register);
    }

    /**
     * Creates and returns a watchpoint for the source defined by the given
     * source handle, at the given expression and over the given range. The
     * marker associated with the watchpoint will be created on the specified
     * resource.
     * 
     * @param sourceHandle
     *            the handle to the watchpoint source
     * @param resource
     *            the resource on which to create the associated watchpoint
     *            marker
     * @param writeAccess
     *            whether this is write watchpoint
     * @param readAccess
     *            whether this is read watchpoint
     * @param expression
     *            the expression on which the watchpoint is set
     * @param memorySpace
     *            the memory space in which the watchpoint is set
     * @param range
     *            the range of the watchpoint in addressable units
     * @param enabled
     *            whether to enable or disable this breakpoint
     * @param ignoreCount
     *            the number of times this breakpoint will be ignored
     * @param condition
     *            the breakpoint condition
     * @param register
     *            whether to add this breakpoint to the breakpoint manager
     * @return the watchpoint that was created
     * @throws CoreException
     *             if this method fails. Reasons include:
     *             <ul>
     *             <li>Failure creating underlying marker. The exception's
     *             status contains the underlying exception responsible for the
     *             failure.</li>
     *             </ul>
     */
    public static ICWatchpoint createWatchpoint(String sourceHandle, IResource resource, boolean writeAccess,
        boolean readAccess, String expression, String memorySpace, BigInteger range, boolean enabled, int ignoreCount,
        String condition, boolean register) throws CoreException {
        HashMap<String, Object> attributes = new HashMap<String, Object>(10);
        setWatchPointAttributes(attributes, sourceHandle, resource, writeAccess, readAccess, expression, memorySpace,
            range, enabled, ignoreCount, condition);
        return new CWatchpoint(resource, attributes, register);
    }

    /**
     * Helper function for setting common watchpoint attributes.
     * 
     * @param attributes
     *            Map to write the attributes into.
     * @param sourceHandle
     *            the handle to the watchpoint source
     * @param resource
     *            the resource on which to create the associated watchpoint
     *            marker
     * @param writeAccess
     *            whether this is write watchpoint
     * @param readAccess
     *            whether this is read watchpoint
     * @param expression
     *            the expression on which the watchpoint is set
     * @param memorySpace
     *            the memory space in which the watchpoint is set
     * @param range
     *            the range of the watchpoint in addressable units
     * @param enabled
     *            whether to enable or disable this breakpoint
     * @param ignoreCount
     *            the number of times this breakpoint will be ignored
     * @param condition
     *            the breakpoint condition
     * @param register
     *            whether to add this breakpoint to the breakpoint manager
     * 
     * @since 7.2
     */
    public static void setWatchPointAttributes(Map<String, Object> attributes, String sourceHandle, IResource resource,
        boolean writeAccess, boolean readAccess, String expression, String memorySpace, BigInteger range,
        boolean enabled, int ignoreCount, String condition) {
        attributes.put(IBreakpoint.ID, getPluginIdentifier());
        attributes.put(IBreakpoint.ENABLED, Boolean.valueOf(enabled));
        attributes.put(ICBreakpoint.IGNORE_COUNT, new Integer(ignoreCount));
        attributes.put(ICBreakpoint.CONDITION, condition);
        attributes.put(ICBreakpoint.SOURCE_HANDLE, sourceHandle);
        attributes.put(ICWatchpoint.EXPRESSION, expression);
        attributes.put(ICWatchpoint2.MEMORYSPACE, memorySpace);
        attributes.put(ICWatchpoint2.RANGE, range.toString());
        attributes.put(ICWatchpoint.READ, Boolean.valueOf(readAccess));
        attributes.put(ICWatchpoint.WRITE, Boolean.valueOf(writeAccess));
    }

    /**
     * Creates and returns a breakpoint for the function defined by the given
     * name. The marker associated with the breakpoint will be created on the
     * specified resource.
     * 
     * @param sourceHandle
     *            the handle to the breakpoint source
     * @param resource
     *            the resource on which to create the associated breakpoint
     *            marker
     * @param function
     *            the name of the function this breakpoint suspends execution in
     * @param charStart
     *            the first character index associated with the breakpoint, or
     *            -1 if unspecified, in the source file in which the breakpoint
     *            is set
     * @param charEnd
     *            the last character index associated with the breakpoint, or -1
     *            if unspecified, in the source file in which the breakpoint is
     *            set
     * @param lineNumber
     *            the lineNumber on which the breakpoint is set, or -1 if
     *            unspecified - line numbers are 1 based, associated with the
     *            source file in which the breakpoint is set
     * @param enabled
     *            whether to enable or disable this breakpoint
     * @param ignoreCount
     *            the number of times this breakpoint will be ignored
     * @param condition
     *            the breakpoint condition
     * @param register
     *            whether to add this breakpoint to the breakpoint manager
     * @return an address breakpoint
     * @throws CoreException
     *             if this method fails. Reasons include:
     *             <ul>
     *             <li>Failure creating underlying marker. The exception's
     *             status contains the underlying exception responsible for the
     *             failure.</li>
     *             </ul>
     * @deprecated as of CDT 5.0 use
     *             {@link #createFunctionBreakpoint(String, IResource, int, String, int, int, int, boolean, int, String, boolean)}
     */
    @Deprecated
    public static ICFunctionBreakpoint createFunctionBreakpoint(String sourceHandle, IResource resource,
        String function, int charStart, int charEnd, int lineNumber, boolean enabled, int ignoreCount,
        String condition, boolean register) throws CoreException {
        return createFunctionBreakpoint(sourceHandle, resource, ICBreakpointType.REGULAR, function, charStart, charEnd,
            lineNumber, enabled, ignoreCount, condition, register);
    }

    /**
     * Creates and returns a breakpoint for the function defined by the given
     * name. The marker associated with the breakpoint will be created on the
     * specified resource.
     * 
     * @param sourceHandle
     *            the handle to the breakpoint source
     * @param resource
     *            the resource on which to create the associated breakpoint
     *            marker
     * @param type
     *            a type constant from ICBreakpointType
     * @param function
     *            the name of the function this breakpoint suspends execution in
     * @param charStart
     *            the first character index associated with the breakpoint, or
     *            -1 if unspecified, in the source file in which the breakpoint
     *            is set
     * @param charEnd
     *            the last character index associated with the breakpoint, or -1
     *            if unspecified, in the source file in which the breakpoint is
     *            set
     * @param lineNumber
     *            the lineNumber on which the breakpoint is set, or -1 if
     *            unspecified - line numbers are 1 based, associated with the
     *            source file in which the breakpoint is set
     * @param enabled
     *            whether to enable or disable this breakpoint
     * @param ignoreCount
     *            the number of times this breakpoint will be ignored
     * @param condition
     *            the breakpoint condition
     * @param register
     *            whether to add this breakpoint to the breakpoint manager
     * @return an address breakpoint
     * @throws CoreException
     *             if this method fails. Reasons include:
     *             <ul>
     *             <li>Failure creating underlying marker. The exception's
     *             status contains the underlying exception responsible for the
     *             failure.</li>
     *             </ul>
     */
    public static ICFunctionBreakpoint createFunctionBreakpoint(String sourceHandle, IResource resource, int type,
        String function, int charStart, int charEnd, int lineNumber, boolean enabled, int ignoreCount,
        String condition, boolean register) throws CoreException {
        HashMap<String, Object> attributes = new HashMap<String, Object>(10);
        setFunctionBreakpointAttributes(attributes, sourceHandle, type, function, charStart, charEnd, lineNumber,
            enabled, ignoreCount, condition);
        return new CFunctionBreakpoint(resource, attributes, register);
    }

    /**
     * Creates a breakpoint without associated marker.
     * <p>
     * Note: Before a breakpoint created using this method can be used, the
     * client must first create a marker and register the breakpoint. The former
     * is accomplished using {@link IBreakpoint#setMarker(IMarker)}, the latter
     * using {@link IBreakpointManager#addBreakpoint(IBreakpoint)}.
     * 
     * @since 7.2
     */
    public static ICFunctionBreakpoint createBlankFunctionBreakpoint() {
        return new CFunctionBreakpoint();
    }

    /**
     * Creates and returns a tracepoint for the function defined by the given
     * name. The marker associated with the breakpoint will be created on the
     * specified resource.
     * 
     * @param sourceHandle
     *            the handle to the breakpoint source
     * @param resource
     *            the resource on which to create the associated breakpoint
     *            marker
     * @param type
     *            a type constant from ICBreakpointType
     * @param function
     *            the name of the function this breakpoint suspends execution in
     * @param charStart
     *            the first character index associated with the breakpoint, or
     *            -1 if unspecified, in the source file in which the breakpoint
     *            is set
     * @param charEnd
     *            the last character index associated with the breakpoint, or -1
     *            if unspecified, in the source file in which the breakpoint is
     *            set
     * @param lineNumber
     *            the lineNumber on which the breakpoint is set, or -1 if
     *            unspecified - line numbers are 1 based, associated with the
     *            source file in which the breakpoint is set
     * @param enabled
     *            whether to enable or disable this breakpoint
     * @param ignoreCount
     *            the number of times this breakpoint will be ignored
     * @param condition
     *            the breakpoint condition
     * @param register
     *            whether to add this breakpoint to the breakpoint manager
     * @return an address breakpoint
     * @throws CoreException
     *             if this method fails. Reasons include:
     *             <ul>
     *             <li>Failure creating underlying marker. The exception's
     *             status contains the underlying exception responsible for the
     *             failure.</li>
     *             </ul>
     * @since 7.0
     */
    public static ICFunctionBreakpoint createFunctionTracepoint(String sourceHandle, IResource resource, int type,
        String function, int charStart, int charEnd, int lineNumber, boolean enabled, int ignoreCount,
        String condition, boolean register) throws CoreException {
        HashMap<String, Object> attributes = new HashMap<String, Object>(10);
        setFunctionBreakpointAttributes(attributes, sourceHandle, type, function, charStart, charEnd, lineNumber,
            enabled, ignoreCount, condition);
        return new CFunctionTracepoint(resource, attributes, register);
    }

    /**
     * Creates a breakpoint without associated marker.
     * <p>
     * Note: Before a breakpoint created using this method can be used, the
     * client must first create a marker and register the breakpoint. The former
     * is accomplished using {@link IBreakpoint#setMarker(IMarker)}, the latter
     * using {@link IBreakpointManager#addBreakpoint(IBreakpoint)}.
     * 
     * @since 7.2
     */
    public static ICFunctionBreakpoint createBlankFunctionTracepoint() {
        return new CFunctionTracepoint();
    }

    /**
     * Helper function for setting common address breakpoint attributes.
     * 
     * @param attributes
     *            Map to write the attributes into.
     * @param sourceHandle
     *            the handle to the breakpoint source
     * @param resource
     *            the resource on which to create the associated breakpoint
     *            marker
     * @param type
     *            a type constant from ICBreakpointType
     * @param function
     *            the name of the function this breakpoint suspends execution in
     * @param charStart
     *            the first character index associated with the breakpoint, or
     *            -1 if unspecified, in the source file in which the breakpoint
     *            is set
     * @param charEnd
     *            the last character index associated with the breakpoint, or -1
     *            if unspecified, in the source file in which the breakpoint is
     *            set
     * @param lineNumber
     *            the lineNumber on which the breakpoint is set, or -1 if
     *            unspecified - line numbers are 1 based, associated with the
     *            source file in which the breakpoint is set
     * @param enabled
     *            whether to enable or disable this breakpoint
     * @param ignoreCount
     *            the number of times this breakpoint will be ignored
     * @param condition
     *            the breakpoint condition
     * @param register
     *            whether to add this breakpoint to the breakpoint manager
     * 
     * @since 7.2
     */
    public static void setFunctionBreakpointAttributes(Map<String, Object> attributes, String sourceHandle, int type,
        String function, int charStart, int charEnd, int lineNumber, boolean enabled, int ignoreCount, String condition) {
        setLineBreakpointAttributes(attributes, sourceHandle, type, lineNumber, enabled, ignoreCount, condition);
        attributes.put(IMarker.CHAR_START, new Integer(charStart));
        attributes.put(IMarker.CHAR_END, new Integer(charEnd));
        attributes.put(ICLineBreakpoint.FUNCTION, function);
    }

    /**
     * Returns the line breakpoint that is already registered with the
     * breakpoint manager for a source with the given handle and the given
     * resource at the given line number.
     * 
     * @param sourceHandle
     *            the source handle
     * @param resource
     *            the breakpoint resource
     * @param lineNumber
     *            the line number
     * @return the line breakpoint that is already registered with the
     *         breakpoint manager or <code>null</code> if no such breakpoint is
     *         registered
     * @exception CoreException
     *                if unable to retrieve the associated marker attributes
     *                (line number).
     */
    public static ICLineBreakpoint lineBreakpointExists(String sourceHandle, IResource resource, int lineNumber)
        throws CoreException {
        String modelId = getPluginIdentifier();
        IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
        IBreakpoint[] breakpoints = manager.getBreakpoints(modelId);
        for (int i = 0; i < breakpoints.length; i++) {
            if (!(breakpoints[i] instanceof ICLineBreakpoint)) {
                continue;
            }
            ICLineBreakpoint breakpoint = (ICLineBreakpoint) breakpoints[i];
            if (sameSourceHandle(sourceHandle, breakpoint.getSourceHandle())) {
                if (breakpoint.getLineNumber() == lineNumber) {
                    return breakpoint;
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
     * @param sourceHandle
     *            the source handle
     * @param resource
     *            the breakpoint resource
     * @param expression
     *            the expression
     * @return the watchpoint that is already registered with the breakpoint
     *         manager or <code>null</code> if no such watchpoint is registered
     * @exception CoreException
     *                if unable to retrieve the associated marker attributes
     *                (line number).
     */
    public static ICWatchpoint watchpointExists(String sourceHandle, IResource resource, String expression)
        throws CoreException {
        String modelId = getPluginIdentifier();
        String markerType = ICWatchpoint.C_WATCHPOINT_MARKER;
        IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
        IBreakpoint[] breakpoints = manager.getBreakpoints(modelId);
        for (int i = 0; i < breakpoints.length; i++) {
            if (!(breakpoints[i] instanceof ICWatchpoint)) {
                continue;
            }
            ICWatchpoint breakpoint = (ICWatchpoint) breakpoints[i];
            if (breakpoint.getMarker().getType().equals(markerType)) {
                if (sameSourceHandle(sourceHandle, breakpoint.getSourceHandle())) {
                    if (breakpoint.getMarker().getResource().equals(resource)) {
                        if (breakpoint.getExpression().equals(expression)) {
                            return breakpoint;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns the function breakpoint that is already registered with the
     * breakpoint manager for a source with the given handle and the given
     * resource with the given function name.
     * 
     * @param sourceHandle
     *            the source handle
     * @param resource
     *            the breakpoint resource
     * @param function
     *            the fully qualified function name
     * @return the breakpoint that is already registered with the breakpoint
     *         manager or <code>null</code> if no such breakpoint is registered
     * @exception CoreException
     *                if unable to retrieve the associated marker attributes
     *                (line number).
     */
    public static ICFunctionBreakpoint functionBreakpointExists(String sourceHandle, IResource resource, String function)
        throws CoreException {
        String modelId = getPluginIdentifier();
        String markerType = ICFunctionBreakpoint.C_FUNCTION_BREAKPOINT_MARKER;
        IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
        IBreakpoint[] breakpoints = manager.getBreakpoints(modelId);
        for (int i = 0; i < breakpoints.length; i++) {
            if (!(breakpoints[i] instanceof ICFunctionBreakpoint)) {
                continue;
            }
            ICFunctionBreakpoint breakpoint = (ICFunctionBreakpoint) breakpoints[i];
            if (breakpoint.getMarker().getType().equals(markerType)) {
                if (sameSourceHandle(sourceHandle, breakpoint.getSourceHandle())) {
                    if (breakpoint.getMarker().getResource().equals(resource)) {
                        if (breakpoint.getFunction() != null && breakpoint.getFunction().equals(function)) {
                            return breakpoint;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static IDebugTarget newDebugTarget(ILaunch launch, ICDITarget target, String name, IProcess iprocess,
        IProcess debuggerProcess, IFile file, boolean allowTerminate, boolean allowDisconnect, boolean stopInMain)
        throws CoreException {
        IBinaryExecutable exeFile = getBinary(file);
        String stopSymbol = null;
        if (stopInMain)
            stopSymbol = launch.getLaunchConfiguration().getAttribute(
                ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL,
                ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_SYMBOL_DEFAULT);
        return newDebugTarget(launch, file.getProject(), target, name, iprocess, exeFile, allowTerminate,
            allowDisconnect, stopSymbol, true);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static IDebugTarget newAttachDebugTarget(ILaunch launch, ICDITarget target, String name,
        IProcess debuggerProcess, IFile file) throws CoreException {
        IBinaryExecutable exeFile = getBinary(file);
        return newDebugTarget(launch, file.getProject(), target, name, null, exeFile, true, true, false);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static IDebugTarget newCoreFileDebugTarget(final ILaunch launch, final ICDITarget target, final String name,
        final IProcess debuggerProcess, final IFile file) throws CoreException {
        IBinaryExecutable exeFile = getBinary(file);
        return newDebugTarget(launch, file.getProject(), target, name, null, exeFile, true, false, false);
    }

    private static IBinaryExecutable getBinary(IFile file) throws CoreException {
        IProject project = file.getProject();
        ICConfigExtensionReference[] binaryParsersExt = CCorePlugin.getDefault().getDefaultBinaryParserExtensions(
            project);
        for (int i = 0; i < binaryParsersExt.length; i++) {
            IBinaryParser parser = CoreModelUtil.getBinaryParser(binaryParsersExt[i]);
            try {
                IBinaryFile exe = parser.getBinary(file.getLocation());
                if (exe instanceof IBinaryExecutable) {
                    return (IBinaryExecutable) exe;
                }
            } catch (IOException e) {
            }
        }
        throw new CoreException(new Status(IStatus.ERROR, CDebugCorePlugin.getUniqueIdentifier(), -1,
            DebugCoreMessages.getString("CDIDebugModel.0"), null)); //$NON-NLS-1$
    }

    private static boolean sameSourceHandle(String handle1, String handle2) {
        if (handle1 == null || handle2 == null)
            return false;
        IPath path1 = new Path(handle1);
        IPath path2 = new Path(handle2);
        if (path1.isValidPath(handle1) && path2.isValidPath(handle2)) {
            return path1.equals(path2);
        }
        // If handles are not file names ????
        return handle1.equals(handle2);
    }

    /**
     * Checks whether an event breakpoint with given type and argument already
     * exists. If multiple event breakpoints exist that match given parameters,
     * only one of them will be returned.
     * 
     * @param type
     *            Event type.
     * @param arg
     *            Event argument.
     * @return Event breakpoint, if found.
     * @throws CoreException
     *             Exception in reading breakpoint properties.
     */
    public static ICEventBreakpoint eventBreakpointExists(String type, String arg) throws CoreException {
        String modelId = getPluginIdentifier();

        IBreakpointManager manager = DebugPlugin.getDefault().getBreakpointManager();
        IBreakpoint[] breakpoints = manager.getBreakpoints(modelId);
        for (int i = 0; i < breakpoints.length; i++) {
            if (!(breakpoints[i] instanceof ICEventBreakpoint)) {
                continue;
            }
            ICEventBreakpoint breakpoint = (ICEventBreakpoint) breakpoints[i];

            if (breakpoint.getEventType().equals(type)) {
                String arg1 = breakpoint.getEventArgument();
                if (arg1 == null)
                    arg1 = ""; //$NON-NLS-1$
                String arg2 = arg == null ? "" : arg; //$NON-NLS-1$
                if (arg1.equals(arg2))
                    return breakpoint;
            }

        }
        return null;
    }

    /**
     * Creates and registers a new event breakpoint.
     * 
     * @param attributes
     *            Map to write the attributes into.
     * @param type
     *            Event breakpoint type.
     * @param arg
     *            Event-specific argument value.
     * @param register
     *            whether to add this breakpoint to the breakpoint manager
     * @return an event breakpoint
     * 
     * @throws CoreException
     *             if this method fails. Reasons include:
     *             <ul>
     *             <li>Failure creating underlying marker. The exception's
     *             status contains the underlying exception responsible for the
     *             failure.</li>
     *             </ul>
     */
    public static ICEventBreakpoint createEventBreakpoint(String type, String arg, boolean register)
        throws CoreException {
        final IResource resource = ResourcesPlugin.getWorkspace().getRoot();
        final Map<String, Object> attributes = new HashMap<String, Object>();
        setEventBreakpointAttributes(attributes, type, arg);
        return new CEventBreakpoint(resource, attributes, register);

    }

    /**
     * Helper function for setting common event breakpoint attributes.
     * 
     * @param attributes
     *            Map to write the attributes into.
     * @param type
     *            Event breakpoint type.
     * @param arg
     *            Event-specific argument value.
     * @param register
     *            whether to add this breakpoint to the breakpoint manager
     * 
     * @since 7.2
     */
    public static void setEventBreakpointAttributes(Map<String, Object> attributes, String type, String arg) {
        attributes.put(IBreakpoint.ID, CDIDebugModel.getPluginIdentifier());
        attributes.put(IBreakpoint.ENABLED, true);
        attributes.put(ICBreakpoint.IGNORE_COUNT, 0);
        attributes.put(ICBreakpoint.CONDITION, ""); //$NON-NLS-1$
        attributes.put(ICEventBreakpoint.EVENT_TYPE_ID, type);
        attributes.put(ICEventBreakpoint.EVENT_ARG, arg);
    }

    /**
     * Creates a breakpoint without associated marker.
     * <p>
     * Note: Before a breakpoint created using this method can be used, the
     * client must first create a marker and register the breakpoint. The former
     * is accomplished using {@link IBreakpoint#setMarker(IMarker)}, the latter
     * using {@link IBreakpointManager#addBreakpoint(IBreakpoint)}.
     * 
     * @since 7.2
     */
    public static ICEventBreakpoint createBlankEventBreakpoint() {
        return new CEventBreakpoint();
    }

    /**
     * Creates a marker for given C breakpoint.
     * 
     * @param breakpoint
     *            Breakpoint to create the marker for.
     * @param resource
     *            Resource to create the marker on.
     * @param attributes
     *            Marker attributes to use.
     * @param add
     *            Whether to register the breakpoint with breakpoint manager.
     * @throws CoreException
     *             Error thrown while creating marker.
     * 
     * @since 7.2
     */
    public static void createBreakpointMarker(final ICBreakpoint breakpoint, final IResource resource,
        final Map<String, Object> attributes, final boolean add) throws CoreException {
        if (breakpoint.getMarker() != null) {
            throw new CoreException(new Status(IStatus.ERROR, CDebugCorePlugin.PLUGIN_ID,
                "Cannot create breakpoint marker breakpoint given breakpoint already has an assotiated maker")); //$NON-NLS-1$
        }

        IWorkspaceRunnable wr = new IWorkspaceRunnable() {
            @Override
            public void run(IProgressMonitor monitor) throws CoreException {
                // create the marker
                IMarker marker = resource.createMarker(calculateMarkerType(breakpoint));
                breakpoint.setMarker(marker);

                // set attributes
                marker.setAttributes(attributes);

                // set the marker message
                if (breakpoint instanceof ICBreakpoint2) {
                    ((ICBreakpoint2) breakpoint).refreshMessage();
                }

                // add to breakpoint manager if requested
                if (add) {
                    DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(breakpoint);
                }
            }
        };
        ResourcesPlugin.getWorkspace().run(wr, null);
    }
}
