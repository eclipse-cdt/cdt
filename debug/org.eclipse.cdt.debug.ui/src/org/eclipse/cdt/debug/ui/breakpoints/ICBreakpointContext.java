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
package org.eclipse.cdt.debug.ui.breakpoints;

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.core.resources.IResource;
import org.eclipse.debug.ui.contexts.IDebugContextProvider;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Input for the breakpoint properties dialog.  It captures both the 
 * selected breakpoint object as well as the selected debug context.
 * This combined context can then be used by breakpoint property
 * pages to access model and target specific breakpoint settings.  
 * 
 * @since 7.2
 */
public interface ICBreakpointContext extends IDebugContextProvider {

    /**
     * Returns the breakpoint object that this context represents.
     * <p>  
     * Note: The returned breakpoint may not yet have an associated marker.  
     * This is for the case where the property dialog is opened for a breakpoint
     * that is yet to be created.
     * 
     * @return Breakpoint object.
     */
    public ICBreakpoint getBreakpoint();
    
    /**
     * Resource object that the breakpoint marker is on.  In case where
     * the breakpoint marker is not yet created, clients can access the intended 
     * breakpoint resource object through this method.
     * 
     * @return The breakpoint's resource object.
     */
    public IResource getResource();
    
    /**
     * Returns the preference store to be used by property pages.  This 
     * preference overrides values in the breakpoint marker.   
     * @return Preference store for the property pages.
     */
    public IPreferenceStore getPreferenceStore();
     
}
