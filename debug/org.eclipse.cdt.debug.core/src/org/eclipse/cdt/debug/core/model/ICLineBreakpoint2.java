/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

/**
 * Line breakpoint extension that allows a breakpoint to be relocated by a 
 * debugger to a valid source line.  
 * <p>Clients which can determine a valid source based on debuggers symbol 
 * information should call the various <code>setInstalled...</code> methods with 
 * the corrected location attributes. Note, there is no <code>setInstalledSourceHandle</code>, 
 * clients should call {@link ICBreakpoint#setSourceHandle(String)}.  If multiple
 * clients try to change the installed breakpoint location, the last call will 
 * take precedence.  This way debuggers may update the location upon active debug 
 * context change.  <br/>
 * The original breakpoint location as configured by the user can always be 
 * retrieved using the <code>getRequested...</code> methods.
 * </p>   
 * @since 7.2
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ICLineBreakpoint2 extends ICLineBreakpoint, ICBreakpoint2 {

    /**
     * Breakpoint attribute storing the original requested line for this breakpoint
     * This attribute is a <code>int</code>.
     */
    public static final String REQUESTED_LINE = "requestedLine"; //$NON-NLS-1$

    /**
     * Breakpoint attribute storing the original requested column for this breakpoint
     * This attribute is a <code>int</code>.
     */
    public static final String REQUESTED_CHAR_START = "requestedCharStart"; //$NON-NLS-1$

    /**
     * Breakpoint attribute storing the original requested column for this breakpoint
     * This attribute is a <code>int</code>.
     */
    public static final String REQUESTED_CHAR_END = "requestedCharEnd"; //$NON-NLS-1$

    /**
     * Breakpoint attribute storing the original requested file name this breakpoint
     * is set in. 
     * This attribute is a <code>String</code>.
     */
    public static final String REQUESTED_SOURCE_HANDLE = "requestedSourceHandle"; //$NON-NLS-1$ 

    /**
     * Returns the line number where the breakpoint was set before it was relocated to a 
     * valid source line.  
     * 
     * @return Returns the requested line number attribute.
     * @exception CoreException if unable to access the property on this breakpoint's
     *  underlying marker
     */
    public int getRequestedLine() throws CoreException;

    /**
     * Sets the line number where the breakpoint should be set.  
     * 
     * @param The requested line number attribute.
     * @exception CoreException if unable to access the property on this breakpoint's
     *  underlying marker
     */
    public void setRequestedLine(int line) throws CoreException;

    /**
     * Returns starting source index where the breakpoint was set before it 
     * was moved to a valid source location.  
     * 
     * @return Returns the requested start index attribute.
     * @exception CoreException if unable to access the property on this breakpoint's
     *  underlying marker
     */
    public int getRequestedCharStart() throws CoreException;


    /**
     * Sets the starting source index where the breakpoint should be set.
     * 
     * @param The requested start index attribute.
     * @exception CoreException if unable to access the property on this breakpoint's
     *  underlying marker
     */
    public void setRequestedCharStart(int charStart) throws CoreException;

    /**
     * Returns ending source index where the breakpoint was set before it 
     * was moved to a valid source location.  
     * 
     * @return Returns the requested end index attribute.
     * @exception CoreException if unable to access the property on this breakpoint's
     *  underlying marker
     */
    public int getRequestedCharEnd() throws CoreException;

    /**
     * Sets the staring source index where the breakpoint should be set.
     * 
     * @param The requested end index attribute.
     * @exception CoreException if unable to access the property on this breakpoint's
     *  underlying marker
     */
    public void setRequestedCharEnd(int charEnd) throws CoreException;
    
    /**
     * Returns the file name where the breakpoint was set before it was relocated to a 
     * valid file.
     * 
     * @return Returns the requested file name.
     * @exception CoreException if unable to access the property on this breakpoint's
     *  underlying marker
     */
    public String getRequestedSourceHandle() throws CoreException;

    /**
     * Sets the file name where the breakpoint should be set.  May be an empty string
     * if the file is not known.  
     * 
     * @param Requested file name.
     * @exception CoreException if unable to access the property on this breakpoint's
     *  underlying marker
     */
    public void setRequestedSourceHandle(String fileName) throws CoreException;

    /**
     * Sets the line number where the breakpoint is actually installed.   This 
     * method only updates the {@link IMarker#LINE_NUMBER} attribute and the 
     * breakpoint message. 
     * 
     * @param line Installed line number
     * @throws CoreException if unable to access the property 
     *  on this breakpoint's underlying marker
     */
    public void setInstalledLineNumber(int line) throws CoreException;    

    /**
     * Sets the start index where the breakpoint is actually installed. This method 
     * only updates the {@link IMarker#CHAR_START} attribute and the breakpoint 
     * message. 
     * 
     * @param charStart Installed char start
     * @throws CoreException
     */
    public void setInstalledCharStart(int charStart) throws CoreException;
    
    /**
     * Sets the end index where the breakpoint is actually installed.  This method 
     * only updates the {@link IMarker#CHAR_END} attribute and the breakpoint 
     * message. 
     * 
     * @param charEnd Installed char start
     * @throws CoreException
     */
    public void setInstalledCharEnd(int charStart) throws CoreException;
    
    /**
     * Resets the breakpoint location back to the values specified by the 
     * REQUESTED_* attributes.  This operation should be called automatically
     * by the implementation when the install count is reset to 0, and does
     * not need to be called by the client at that time.
     * 
     * @throws CoreException
     */
    public void resetInstalledLocation() throws CoreException;
}
