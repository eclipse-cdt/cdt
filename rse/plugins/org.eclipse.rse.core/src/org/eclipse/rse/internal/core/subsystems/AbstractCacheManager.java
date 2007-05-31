/********************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Martin Oberhuber (Wind River) - [190231] Moved from org.eclipse.rse.ui/subsystems to core
 *********************************************************************************/

package org.eclipse.rse.internal.core.subsystems;

import org.eclipse.rse.core.subsystems.ICacheManager;

/**
 * Abstract implementation of ICacheManager.  Can be used by cache manager to provide some
 * basic caching helper methods.
 */
public abstract class AbstractCacheManager implements ICacheManager {

    
    private boolean restoreFromMemento = false;
    
    /* (non-Javadoc)
     * @see org.eclipse.rse.core.subsystems.ICacheManager#setRestoreFromMemento(boolean)
     */
    public void setRestoreFromMemento(boolean restore) {
           restoreFromMemento = restore;
    }

    /* (non-Javadoc)
     * @see org.eclipse.rse.core.subsystems.ICacheManager#isRestoreFromMemento()
     */
    public boolean isRestoreFromMemento() {
           return restoreFromMemento;
    }

}