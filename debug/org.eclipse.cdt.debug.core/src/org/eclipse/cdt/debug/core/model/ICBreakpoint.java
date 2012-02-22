/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;

/**
 * A breakpoint specific to the C/C++ debug model. A C/C++ breakpoint supports:
 * <ul>
 * <li>a condition</li>
 * <li>an ignore count</li>
 * <li>a thread filter to restrict the breakpoint to a specific thread</li>
 * <li>an installed property that indicates a breakpoint was successfully
 * installed in debug target</li>
 * </ul>
 */
public interface ICBreakpoint extends IBreakpoint {

    /** 
     * Breakpoint marker type for this breakpoint type.
     * @since 7.2
     */
    public static final String C_BREAKPOINT_MARKER = "org.eclipse.cdt.debug.core.cBreakpointMarker"; //$NON-NLS-1$
    
    /**
     * This debug model identifier can be returned by a debug implementation 
     * to indicate that a given debugger integration is using C Breakpoints.
     * This model ID will allow breakpoint actions to configure their default
     * selection. 
     * 
     * @since 7.0
     */
    public static final String C_BREAKPOINTS_DEBUG_MODEL_ID = CDIDebugModel.getPluginIdentifier() + ".cbreakpoints"; //$NON-NLS-1$
    
	/**
	 * Breakpoint attribute storing the number of debug targets a breakpoint is
	 * installed in (value
	 * <code>"org.eclipse.cdt.debug.core.installCount"</code>). This
	 * attribute is an <code>int</code>.
	 * 
	 * Note: this attribute is used only for notifying listeners
	 * (IBreakpointListener) of a change in the install count. The attribute is
	 * not used by the CDT breakpoint object to manage the install count, since
	 * it is a transient property of a breakpoint, and marker attributes are
	 * persisted. In other words, it's conceivable that upon breakpoint manager
	 * initialization, a breakpoint is reconstructed with this attribute being
	 * >0. That doesn't mean the breakpoint is installed (at workbench launch
	 * time, there are no installed breakpoints). At that time, the attribute
	 * means absolutely nothing.
	 */
	public static final String INSTALL_COUNT = "org.eclipse.cdt.debug.core.installCount"; //$NON-NLS-1$	

	/**
	 * Breakpoint attribute storing the conditional expression associated with
	 * this breakpoint (value <code>"org.eclipse.cdt.debug.core.condition"</code>). 
	 * This attribute is a <code>String</code>.
	 */
	public static final String CONDITION = "org.eclipse.cdt.debug.core.condition"; //$NON-NLS-1$	

	/**
	 * Breakpoint attribute storing a breakpoint's ignore count value (value
	 * <code>"org.eclipse.cdt.debug.core.ignoreCount"</code>). This attribute
	 * is an <code>int</code>.
	 */
	public static final String IGNORE_COUNT = "org.eclipse.cdt.debug.core.ignoreCount"; //$NON-NLS-1$	

	/**
	 * Breakpoint attribute storing an identifier of the thread this breakpoint
	 * is restricted in (value <code>"org.eclipse.cdt.debug.core.threadId"</code>). 
	 * This attribute is a <code>String</code>.
	 */
	public static final String THREAD_ID = "org.eclipse.cdt.debug.core.threadId"; //$NON-NLS-1$	

	/**
	 * Breakpoint attribute storing the source handle of the file this breakpoint
	 * is set in (value <code>"org.eclipse.cdt.debug.core.sourceHandle"</code>). 
	 * This attribute is a <code>String</code>.
	 */
	public static final String SOURCE_HANDLE = "org.eclipse.cdt.debug.core.sourceHandle"; //$NON-NLS-1$	

	/**
	 * Breakpoint attribute storing the module name this breakpoint
	 * is set in (value <code>"org.eclipse.cdt.debug.core.module"</code>). 
	 * This attribute is a <code>String</code>.
	 * 
	 * @since 3.0
	 */
	public static final String MODULE = "org.eclipse.cdt.debug.core.module"; //$NON-NLS-1$	

	/**
	 * Returns whether this breakpoint is installed in at least one debug
	 * target.
	 * 
	 * @return whether this breakpoint is installed
	 * @exception CoreException if unable to access the property on this breakpoint's
	 *  underlying marker
	 */
	public boolean isInstalled() throws CoreException;

	/**
	 * Returns whether this breakpoint is conditional.
	 * 
	 * @return whether this breakpoint is conditional
	 * @exception CoreException if unable to access the property on this breakpoint's
	 *  underlying marker
	 */
	public boolean isConditional() throws CoreException;

	/**
	 * Returns the conditional expression associated with this breakpoint.
	 * 
	 * @return this breakpoint's conditional expression
	 * @exception CoreException if unable to access the property on this breakpoint's
	 *  underlying marker
	 */
	public String getCondition() throws CoreException;

	/**
	 * Sets the condition associated with this breakpoint.
	 * 
	 * @param condition the conditional expression
	 * @exception CoreException if unable to access the property on this breakpoint's
	 *  underlying marker
	 */
	public void setCondition( String condition ) throws CoreException;

	/**
	 * Returns the ignore count used by this breakpoint.
	 * 
	 * @return the ignore count used by this breakpoint
	 * @exception CoreException if unable to access the property on this breakpoint's
	 *  underlying marker
	 */
	public int getIgnoreCount() throws CoreException;

	/**
	 * Sets the ignore count attribute for this breakpoint.
	 * 
	 * @param ignoreCount the new ignore count
	 * @exception CoreException if unable to access the property on this breakpoint's
	 *  underlying marker
	 */
	public void setIgnoreCount( int ignoreCount ) throws CoreException;

	/**
	 * Returns the identifier of the thread this breakpoint is restricted in.
	 * 
	 * @return the thread identifier
	 * @exception CoreException if unable to access the property on this breakpoint's
	 *  underlying marker
	 */
	public String getThreadId() throws CoreException;

	/**
	 * Restricts this breakpoint to suspend only in the given thread when
	 * encounterd in the given thread's target.
	 * 
	 * @param threadId the thread identifier
	 * @exception CoreException if unable to access the property on this breakpoint's
	 *  underlying marker
	 */
	public void setThreadId( String threadId ) throws CoreException;

	/**
	 * Returns the module name this breakpoint is set in.
	 * 
	 * @return the module name
	 * @exception CoreException if unable to access the property on this breakpoint's
	 *  underlying marker
	 */
	public String getModule() throws CoreException;

	/**
	 * Sets the module name of this breakpoint.
	 * 
	 * @param module the module name
	 * @exception CoreException if unable to access the property on this breakpoint's
	 *  underlying marker
	 */
	public void setModule( String module ) throws CoreException;

	/**
	 * Returns the source handle this breakpoint is set in.
	 * 
	 * @return the source handle
	 * @exception CoreException if unable to access the property on this breakpoint's
	 *  underlying marker
	 */
	public String getSourceHandle() throws CoreException;

	/**
	 * Sets the source handle of this breakpoint.
	 * 
	 * @param sourceHandle the source handle
	 * @exception CoreException if unable to access the property on this breakpoint's
	 *  underlying marker
	 */
	public void setSourceHandle( String sourceHandle ) throws CoreException;

	/**
	 * Increments the install count of this breakpoint
	 * 
	 * @return the new install count value
	 * @throws CoreException if unable to access the property 
	 * 	on this breakpoint's underlying marker
	 */
	public int incrementInstallCount() throws CoreException;

	/**
	 * Decrements the install count of this breakpoint.
	 * 
	 * @return the new install caount value
	 * @throws CoreException if unable to access the property 
	 * 	on this breakpoint's underlying marker
	 */
	public int decrementInstallCount() throws CoreException;

	/**
	 * Resets the install count of this breakpoint
	 * 
	 * @throws CoreException if unable to access the property 
	 * 	on this breakpoint's underlying marker
	 */
	public void resetInstallCount() throws CoreException;

	/**
	 * Returns a breakpoint extension registered for the given debug model 
	 * and of the given type. 
	 * 
	 * @param debugModelId Debug model ID of the extension.
	 * @param extensionType Type of the extension.
	 * @return Extension instance.
	 * @throws CoreException Throws exception in case the extension doesn't exist or cannot be initialized.
	 */
    public <V extends ICBreakpointExtension> V getExtension(String debugModelId, Class<V> extensionType) throws CoreException;
}
