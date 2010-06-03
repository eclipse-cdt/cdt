/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
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

import org.eclipse.core.runtime.IStatus;
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
 * @see PropertiesBasedLabelProvider
 * @see LabelColumnInfo 
 * 
 * @since 1.0
 */
abstract public class LabelAttribute {
    public static final String[] EMPTY_PROPERTY_NAMES_ARRAY = new String[0];
    
    /**
     * @since 2.0
     */
    private String[] fPropertyNames = EMPTY_PROPERTY_NAMES_ARRAY;
    
    public LabelAttribute() {
        this(EMPTY_PROPERTY_NAMES_ARRAY);
    }
    
    /**
     * @since 2.0
     */
    public LabelAttribute(String[] propertyNames) {
        setPropertyNames(propertyNames);
    }
    
    protected void setPropertyNames(String[] propertyNames) {
        fPropertyNames = propertyNames;
    }
    
    /**
     * Returns the properties that are needed by this attribute in order to 
     * determine whether this attribute is enabled and/or for the actual
     * attribute itself.
     * @return Array of names of properties for the element properties provider.
     */
    public String[] getPropertyNames() {
        return fPropertyNames;
    }
    
    /**
     * Returns whether this attribute is enabled for an element which has
     * the given properties.  The default implementation checks if all the 
     * label's attributes are present in the properties map.
     * 
     * @param status Result of the properties update.
     * @param properties Properties supplied by a property update.
     * @return true if this attribute is enabled.
     * 
     * @since 2.0
     */
    public boolean isEnabled(IStatus status, Map<String, Object> properties) {
        for (String propertyName : getPropertyNames()) {
            if (!checkProperty(propertyName, status, properties)) {
                return false;
            }
        }
        return true;
    }


    /**
     * Checks the status of the given property in the given properties map.  The
     * default implementation returns <code>true</code> if the given property
     * exists and is not null.  
     * 
     * @param propertyName Name of the property to check.
     * @param status Result of the properties update.
     * @param properties Properties map following an update.
     * @return <code>true</code> if the property exists in the given map and 
     * its value is not null. 
     * 
     * @since 2.0
     */
    protected boolean checkProperty(String propertyName, IStatus status, Map<String, Object> properties) {
        return properties.get(propertyName) != null;
    }

    /**
     * Updates the label with this attribute. 
     * 
     * @param update Label update object to write to.
     * @param columnIndex Column index to write at.
     * @param status Result of the property update.
     * @param properties Property values map.  It is guaranteed to contain all
     * the properties that this attribute requested through 
     * {@link getPropertyNames()}.
     * 
     * @since 2.0
     */
    abstract public void updateAttribute(ILabelUpdate update, int columnIndex, IStatus status, Map<String, Object> properties);
}