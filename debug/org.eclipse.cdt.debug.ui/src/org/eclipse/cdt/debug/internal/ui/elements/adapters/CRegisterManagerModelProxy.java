/*******************************************************************************
 * Copyright (c) 2010 CodeSourcery and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * CodeSourcery - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.elements.adapters;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy;
import org.eclipse.jface.viewers.Viewer;

public class CRegisterManagerModelProxy extends AbstractModelProxy {

    private CRegisterManagerProxy fRegisterManagerProxy;

    public CRegisterManagerModelProxy( CRegisterManagerProxy rmp ) {
        super();
        fRegisterManagerProxy = rmp;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy#installed(org.eclipse.jface.viewers.Viewer)
     */
    @Override
    public void installed( Viewer viewer ) {
        fRegisterManagerProxy.setModelProxy( this );
        super.installed( viewer );
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.update.EventHandlerModelProxy#dispose()
     */
    @Override
    public synchronized void dispose() {
        fRegisterManagerProxy.setModelProxy( null );
        fRegisterManagerProxy = null;
        super.dispose();
    }

    public void update() {
        ModelDelta delta = new ModelDelta( fRegisterManagerProxy, IModelDelta.CONTENT );
        fireModelChanged( delta );
    }
}
