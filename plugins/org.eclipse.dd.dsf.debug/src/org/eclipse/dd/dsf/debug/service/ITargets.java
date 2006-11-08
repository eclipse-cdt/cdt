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

import org.eclipse.dd.dsf.concurrent.Done;
import org.eclipse.dd.dsf.concurrent.GetDataDone;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.datamodel.IDMData;
import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.datamodel.IDMService;

/**
 * This is just an initial take at the targets interface.
 */
public interface ITargets extends IDMService {

    public interface ITargetDMContext extends IDMContext<ITargetDMData> {}
    
    public interface ITargetDMData extends IDMData {
        String getName();
        boolean isConnected();
    }
    
    public interface ITargetStateChanged extends IDMEvent<ITargetDMContext> {}
    
    public interface ICoreDMContext extends IDMContext<ICoreDMData> {}

    public interface ICoreDMData extends IDMData {
        String getName();
        boolean isConnected();
        IOS.IOSDMContext getOSDMContext();
    }

    public interface ICoreStateChangedDMEvent extends IDMEvent<ICoreDMContext> {}

    public void getTargets(GetDataDone<ITargetDMContext> done);
    public void getCores(ITargetDMContext target, GetDataDone<ICoreDMContext> done);
    
    public void connectTarget(ITargetDMContext targetDmc, Done done);
    public void disconnectTarget(ITargetDMContext targetDmc, Done done);
    public void connectCore(ITargetDMContext targetDmc, Done done);
    public void disconnectCore(ITargetDMContext targetDmc, Done done);
    
}
