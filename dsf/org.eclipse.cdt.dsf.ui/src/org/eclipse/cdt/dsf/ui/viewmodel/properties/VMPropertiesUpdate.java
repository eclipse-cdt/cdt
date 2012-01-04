/*******************************************************************************
 *  Copyright (c) 2009 Wind River Systems and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *      Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.ui.viewmodel.properties;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.VMViewerUpdateTracing;
import org.eclipse.cdt.dsf.internal.DsfPlugin;
import org.eclipse.cdt.dsf.internal.LoggingUtils;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.ui.viewmodel.VMViewerUpdate;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.jface.viewers.TreePath;

/**
 * Properties update used as to collect property data from the provider.  
 * <p>
 * The status returned by the VMPropertiesUpdate is always going to be of type
 * PropertiesUpdateStatus, which allows for setting status for individual
 * properties.
 * </p>
 * 
 * @see PropertiesUpdateStatus
 * 
 * @since 2.0
 */
public class VMPropertiesUpdate extends VMViewerUpdate implements IPropertiesUpdate {

    /**
     * Properties that the client has requested to retrieve.
     */
    private final Set<String> fProperties;
    
    /**
     * Flag indicating that the update has created a new map, as opposed to 
     * using directly a map that was created using setAllProperties() call.
     */
    private boolean fCreatedOwnMap = false;
    
    /**
     * Map of property values, created on demand.
     */
    private Map<String, Object> fValues = Collections.emptyMap();

    public VMPropertiesUpdate(Set<String> properties, IViewerUpdate parentUpdate, DataRequestMonitor<Map<String,Object>> rm) {
        super(parentUpdate, rm);
        super.setStatus(new PropertiesUpdateStatus()); 
        fProperties = properties;
    }

    public VMPropertiesUpdate(Set<String> properties, TreePath elementPath, Object viewerInput, IPresentationContext presentationContext, DataRequestMonitor<Map<String,Object>> rm) {
        super(elementPath, viewerInput, presentationContext, rm);
        super.setStatus(new PropertiesUpdateStatus()); 
        fProperties = properties;
    }
    
    
    @Override
	public Set<String> getProperties() {
        return fProperties;
    }

    /**
     * @since 2.2
     */
    public Map<String, Object> getValues() {
        return fValues;
    }
    
    @Override
	public synchronized void setProperty(String property, Object value) {
        if (!fCreatedOwnMap) {
            fCreatedOwnMap = true;
            Map<String, Object> curValues = fValues;
            fValues = new HashMap<String, Object>(fProperties.size() * 4 / 3, 0.75f);
            if (curValues != null) {
                fValues.putAll(curValues);
            }
        }
        fValues.put(property, value);
    }
    
    @Override
	public synchronized void setAllProperties(Map<String, Object> properties) {
        if (fCreatedOwnMap) {
            fValues.putAll(properties);
        }
        else {
            fValues = properties;
        }
    }
    
    /**
     * Overrides the base class to implement special handling of 
     * {@link PropertiesUpdateStatus}.  If the given status is an instance of 
     * properties status, this new status will be set to the update.  Otherwise, the
     * given status will be merged into the updates existing properties status.
     * This way {@link #getStatus()} should always return an instance of 
     * <code>PropertiesUpdateStatus</code>.
     */
    @Override
    public void setStatus(IStatus status) {
        if (status instanceof PropertiesUpdateStatus) {
            super.setStatus(status);
        } else if ((getStatus() instanceof PropertiesUpdateStatus)) {
            ((PropertiesUpdateStatus)getStatus()).add(status);
        } else {
            assert getStatus().getSeverity() == IStatus.CANCEL : "VMPropertiesUpdate status should always be a PropertiesUpdateStatus unless update is canceled.";  //$NON-NLS-1$
        }
    }    
    
    /**
     * Overrides the standard done in order to store the retrieved values 
     * in the client's request monitor. 
     */
    @Override
    public void done() {
        @SuppressWarnings("unchecked")
        DataRequestMonitor<Map<String,Object>> rm = (DataRequestMonitor<Map<String,Object>>)getRequestMonitor();
        rm.setData(fValues);
        
        // trace our result
        if (VMViewerUpdateTracing.DEBUG_VMUPDATES && !isCanceled() && VMViewerUpdateTracing.matchesFilterRegex(this.getClass())) {
        	StringBuilder str = new StringBuilder();
        	str.append(DsfPlugin.getDebugTime() + " " + LoggingUtils.toString(this) + " marked done; element = " + LoggingUtils.toString(getElement())); //$NON-NLS-1$ //$NON-NLS-2$
        	if (fValues != null) {
	            Iterator<String> keyIter = fValues.keySet().iterator();
	            while (keyIter.hasNext()) {
	            	String prop = keyIter.next();
	            	Object val = fValues.get(prop);
	            	if (val instanceof String[]) {
	            		val = LoggingUtils.toString((String[])val);
	            	}
	                str.append("   " + prop + "=" + val + "\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
	            }
	            str.deleteCharAt(str.length()-1); // remove trailing linefeed
        	}
        	DsfUIPlugin.debug(str.toString());
        }
        
        super.done();
    }
    
    @Override
    public String toString() {
        return "VMPropertiesUpdate:" + getElement() + " " + fProperties; //$NON-NLS-1$ //$NON-NLS-2$/
    }
}