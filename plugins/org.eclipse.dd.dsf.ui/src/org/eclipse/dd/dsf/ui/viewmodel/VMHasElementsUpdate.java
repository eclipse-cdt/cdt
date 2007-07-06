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
package org.eclipse.dd.dsf.ui.viewmodel;

import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;

/** 
 * Helper class implementation of an update object to use with IElementContentProvider.
 * @see IElementContentProvider
 */
@SuppressWarnings("restriction")
public class VMHasElementsUpdate extends VMViewerUpdate implements IHasChildrenUpdate {

    final private DataRequestMonitor<Boolean> fHasElemsRequestMonitor;
    
    public VMHasElementsUpdate(IHasChildrenUpdate clientUpdate, DataRequestMonitor<Boolean> rm) {
        super(clientUpdate, rm);
        fHasElemsRequestMonitor = rm;
    }
    
    public void setHasChilren(boolean hasChildren) {
        fHasElemsRequestMonitor.setData(hasChildren);
    }

    @Override
    public void done() {
        assert isCanceled() || fHasElemsRequestMonitor.getData() != null || !fHasElemsRequestMonitor.getStatus().isOK();
        super.done();            
    }
}
