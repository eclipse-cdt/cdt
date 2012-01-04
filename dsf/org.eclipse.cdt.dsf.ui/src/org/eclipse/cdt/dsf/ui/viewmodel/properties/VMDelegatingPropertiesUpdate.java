/*******************************************************************************
 *  Copyright (c) 2009, 2010 Wind River Systems and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *      Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.ui.viewmodel.properties;

import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.ui.viewmodel.VMViewerUpdate;
import org.eclipse.core.runtime.IStatus;

/**
 * Properties update used as to collect property data from the provider.
 * 
 * @since 2.0
 */
public class VMDelegatingPropertiesUpdate extends VMViewerUpdate implements IPropertiesUpdate {

    /**
     * Update to write the properties to.
     */
    private final IPropertiesUpdate fParentUpdate;
    
    public VMDelegatingPropertiesUpdate(IPropertiesUpdate parentUpdate, RequestMonitor rm) {
        super(parentUpdate, rm);
        fParentUpdate = parentUpdate;
    }

    @Override
	public Set<String> getProperties() {
        return fParentUpdate.getProperties();
    }

    @Override
	public void setProperty(String property, Object value) {
        fParentUpdate.setProperty(property, value);
    }
    
    @Override
	public void setAllProperties(Map<String, Object> properties) {
        fParentUpdate.setAllProperties(properties);
    }
    
    /**
     * @since 2.2
     */
    @Override
    public IStatus getStatus() {
        return fParentUpdate.getStatus();
    }
    
    /**
     * @since 2.2
     */
    @Override
    public void setStatus(IStatus status) {
        fParentUpdate.setStatus(status);
    }    
    
    @Override
    public String toString() {
        return "VMDelegatingPropertiesUpdate -> " + fParentUpdate; //$NON-NLS-1$ 
    }
}