/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.ui.viewmodel.properties;

import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;

/**
 * This is a base class for a label attribute used in generating label 
 * information based on properties of an element.  There are currently 
 * four types of attributes: text, image, font, and color, and a given 
 * attribute can be either enabled or disabled based on the element 
 * properties.
 * <p/>
 * Clients are intended to override this class and its extensions to 
 * implement the {@link LabelAttribute#isEnabled(Map)} and 
 * {@link LabelAttribute#getPropertyNames()} methods as needed. Clients can 
 * also override how the attribute settings are stored, for example in 
 * order to use a preference.  
 * 
 * @see PropertyBasedLabelProvider
 * @see LabelColumnInfo 
 * 
 * @since 1.0
 */
@SuppressWarnings("restriction")
abstract public class LabelAttribute {
    public static final String[] EMPTY_PROPERTY_NAMES_ARRAY = new String[0];
    
    /**
     * Listeners for when this attribute is modified.
     */
    private ListenerList fListeners = new ListenerList();
    
    public LabelAttribute() {
    }
    
    /**
     * Disposes this attribute.
     */
    public void dispose() {
    }
    
    /**
     * Registers the given listener for changes in this attribute. A change in 
     * the attributes of a label should cause a view to repaint.
     * @param listener Listener to register.
     */
    public void addChangedListener(ILabelAttributeChangedListener listener) {
        fListeners.add(listener);
    }
    
    /**
     * Unregisters the given listener.
     * @param listener Listener to unregister.
     */
    public void removeChangedListener(ILabelAttributeChangedListener listener) {
        fListeners.remove(listener);
    }
    
    /**
     * Calls the listeners to notify them that this attribute has changed.
     */
    protected void fireAttributeChanged() {
        Object[] listeners = fListeners.getListeners();
        for (Object listener : listeners) {
            ((ILabelAttributeChangedListener)listener).attributesChanged();
        }
    }
    
    /**
     * Returns the propertis that are needed by this attribute in order to 
     * determine whether this attribute is enabled and/or for the actual
     * attribute itself.
     * @return Array of names of properties for the element properties provider.
     */
    public String[] getPropertyNames() {
        return EMPTY_PROPERTY_NAMES_ARRAY;
    }
    
    /**
     * Returns whether this attribute is enabled for an element which has
     * the given properties.
     * @param properties Map or element properties.  The client should ensure
     * that all properties specified by {@link #getPropertyNames()} are 
     * supplied in this map.
     * @return true if this attribute is enabled.
     */
    public boolean isEnabled(Map<String, Object> properties) {
        return true;
    }
    
    /**
     * Updates the label with this attribute.
     * 
     * @param update Label update object to write to.
     * @param columnIndex Colum index to write at.
     * @param properties Element properties to use.
     */
    abstract public void updateAttribute(ILabelUpdate update, int columnIndex, Map<String, Object> properties);
}