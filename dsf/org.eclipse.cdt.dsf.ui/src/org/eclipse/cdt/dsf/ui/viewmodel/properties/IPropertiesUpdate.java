/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
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
import java.util.Set;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;

/**
 * Context sensitive properties update request for an element.
 * 
 * @since 1.0
 */
public interface IPropertiesUpdate extends IViewerUpdate {
    /**
     * Returns the set of element properties that the provider should update.
     */
    public Set<String> getProperties();
    
    /**
     * Sets the given property to update.
     * 
     * @param property Property ID.
     * @param value Property value.
     */
    public void setProperty(String property, Object value);
    
    /**
     * Sets the given map as the complete property map for this update.
     * If other properties were already set to this update, the properties
     * given here will be added.  If properties are added later, the properties 
     * map given here will not be modified, instead it will be copied.
     * 
     * @param properties Full properties map.
     */
    public void setAllProperties(Map<String, Object> properties);
}
