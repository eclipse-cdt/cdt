package org.eclipse.dd.dsf.ui.viewmodel.properties;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;

/**
 * Context sensitive properties update request for an element.
 */
@SuppressWarnings("restriction")
public interface IPropertiesUpdate extends IViewerUpdate {
    /**
     * Returns the list of element properties that the provider should set.
     * If <code>null</code>, all available properties should be set. 
     */
    public String[] getProperties();
    
    /**
     * Sets the given property to update.
     * @param property Property ID.
     * @param value Property value.
     */
    public void setProperty(String property, Object value);
}
