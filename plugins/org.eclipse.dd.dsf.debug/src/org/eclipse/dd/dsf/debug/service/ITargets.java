/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.service;

import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.datamodel.IDMData;
import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.datamodel.IDMService;

/**
 * This is just an initial take at the targets interface.
 */
public interface ITargets extends IDMService {

    public interface ITargetDMContext extends IDMContext {}
    
    public interface ITargetDMData extends IDMData {
        String getName();
        boolean isConnected();
    }
    
    public interface ITargetStateChanged extends IDMEvent<ITargetDMContext> {}
    
    public interface ICoreDMContext extends IDMContext {}

    public interface ICoreDMData extends IDMData {
        String getName();
        boolean isConnected();
        IOS.IOSDMContext getOSDMContext();
    }

    public interface ICoreStateChangedDMEvent extends IDMEvent<ICoreDMContext> {}

    public void getTargets(DataRequestMonitor<ITargetDMContext> requestMonitor);
    public void getCores(ITargetDMContext target, DataRequestMonitor<ICoreDMContext> requestMonitor);
    
    public void connectTarget(ITargetDMContext targetDmc, RequestMonitor requestMonitor);
    public void disconnectTarget(ITargetDMContext targetDmc, RequestMonitor requestMonitor);
    public void connectCore(ITargetDMContext targetDmc, RequestMonitor requestMonitor);
    public void disconnectCore(ITargetDMContext targetDmc, RequestMonitor requestMonitor);
    
}
