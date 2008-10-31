/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.ui.viewmodel;

/**
 * Extension to the IVMAdapter interface which allows access to the array of active
 * providers. 
 * 
 * @since 1.1
 */
public interface IVMAdapterExtension extends IVMAdapter {
    
    /**
     * Retrieves the currently active VM providers in this adapter.
     * 
     * @return array of VM providers
     */
    public IVMProvider[] getActiveProviders();
}
