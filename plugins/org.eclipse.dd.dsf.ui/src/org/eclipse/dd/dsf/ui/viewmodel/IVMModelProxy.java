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
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;

/**
 * 
 */
@SuppressWarnings("restriction")
public interface IVMModelProxy extends IModelProxy {
    
    public Object getRootElement();
    
    public boolean isDeltaEvent(Object event);

    public void createDelta(final Object event, final DataRequestMonitor<IModelDelta> rm);

    public void fireModelChanged(IModelDelta delta);

}
