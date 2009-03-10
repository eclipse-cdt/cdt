package org.eclipse.cdt.dsf.ui.viewmodel.properties;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.ui.viewmodel.VMViewerUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.jface.viewers.TreePath;

/**
 * Properties update used as to collect property data from the provider.
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
        fProperties = properties;
    }

    public VMPropertiesUpdate(Set<String> properties, TreePath elementPath, Object viewerInput, IPresentationContext presentationContext, DataRequestMonitor<Map<String,Object>> rm) {
        super(elementPath, viewerInput, presentationContext, rm);
        fProperties = properties;
    }
    
    
    public Set<String> getProperties() {
        return fProperties;
    }

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
    
    public synchronized void setAllProperties(Map<String, Object> properties) {
        if (fCreatedOwnMap) {
            fValues.putAll(properties);
        }
        else {
            fValues = properties;
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
        super.done();
    }
    
    @Override
    public String toString() {
        return "VMPropertiesUpdate:" + getElement() + " " + fProperties; //$NON-NLS-1$ //$NON-NLS-2$/
    }
}