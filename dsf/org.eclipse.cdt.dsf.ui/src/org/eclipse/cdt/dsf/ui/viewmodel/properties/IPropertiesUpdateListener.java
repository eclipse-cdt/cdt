/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.ui.viewmodel.properties;

/**
 * Listener for properties updates requested by a property based label provider.
 * 
 * @since 2.2
 */
public interface IPropertiesUpdateListener {
    
    /**
     * Indicates that the given updates were requested from a properties provider.
     */
    public void propertiesUpdatesStarted(IPropertiesUpdate[] updates);
    
    /**
     * Indicates that the given update has been completed.
     */
    public void propertiesUpdateCompleted(IPropertiesUpdate update);

}
