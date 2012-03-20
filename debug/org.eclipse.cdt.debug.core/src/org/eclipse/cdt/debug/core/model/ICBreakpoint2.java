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

import org.eclipse.core.runtime.CoreException;

/**
 * Extension that allows client to force breakpoint message to refresh.
 * 
 * @since 7.2
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ICBreakpoint2 extends ICBreakpoint {
    /**
     * Refresh the marker message for the breakpoint.
     * <p>
     * Many of breakpoint settings are backed by marker attributes and it is 
     * sometimes more convenient to modify those attributes in the marker 
     * directly rather than through accessor methods of the breakpoint.  This 
     * method allows the client to force the breakpoint to refresh its 
     * {@link org.eclipse.core.resources.IMarker#MESSAGE} attribute to reflect
     * its current attribute values.
     * </p>
     * 
     * @throws CoreException if unable to access the property 
     *  on this breakpoint's underlying marker
     */
    public void refreshMessage() throws CoreException;    

}
