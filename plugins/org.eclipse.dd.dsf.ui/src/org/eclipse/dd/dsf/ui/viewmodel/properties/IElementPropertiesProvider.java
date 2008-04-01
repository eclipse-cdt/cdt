package org.eclipse.dd.dsf.ui.viewmodel.properties;


/**
 * Provides context-sensitive properties.  Can be registered as an adapter for 
 * an element or implemented directly
 */
public interface IElementPropertiesProvider {

    /**
     * Updates the specified property sets.
     * 
     * @param updates each update specifies the element and context for which 
     * a set of properties is requested and stores them
     */
    public void update(IPropertiesUpdate[] updates);
}
