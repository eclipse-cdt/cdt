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
    
    /**
     * Returns a user-presentable name for the given property.
     */
    public String getPropertyName(String property);
    
    /**
     * Returns a description for the given property.
     */
    public String getPropertyDescription(String property);
}
