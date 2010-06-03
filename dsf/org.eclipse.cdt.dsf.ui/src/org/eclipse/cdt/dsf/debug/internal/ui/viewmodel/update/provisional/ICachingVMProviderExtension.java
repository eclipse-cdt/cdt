/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.update.provisional;

import org.eclipse.cdt.dsf.ui.viewmodel.update.ICachingVMProvider;


/**
 * @since 1.1
 */
public interface ICachingVMProviderExtension extends ICachingVMProvider {

    /**
     * Returns the update policies that the given provider supports.
     */
    public IVMUpdateScope[] getAvailableUpdateScopes();

    /**
     * Returns the active update policy.
     */
    public IVMUpdateScope getActiveUpdateScope();

    /**
     * Sets the active update policy.  This has to be one of the update
     * policies supported by the provider.
     */
    public void setActiveUpdateScope(IVMUpdateScope mode);
}
