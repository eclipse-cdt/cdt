package org.eclipse.cdt.dsf.ui.viewmodel.properties;

import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.ui.viewmodel.VMViewerUpdate;

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

    public Set<String> getProperties() {
        return fParentUpdate.getProperties();
    }

    public void setProperty(String property, Object value) {
        fParentUpdate.setProperty(property, value);
    }
    
    public void setAllProperties(Map<String, Object> properties) {
        fParentUpdate.setAllProperties(properties);
    }
    
    @Override
    public String toString() {
        return "VMDelegatingPropertiesUpdate -> " + fParentUpdate; //$NON-NLS-1$ 
    }
}