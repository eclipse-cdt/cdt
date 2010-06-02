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
package org.eclipse.cdt.tests.dsf.service;

import java.util.Hashtable;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.DsfTestPlugin;
import org.osgi.framework.BundleContext;

public class SimpleTestService extends AbstractDsfService {

    public SimpleTestService(DsfSession session) {
        super(session);
    }
    
    @Override 
    protected BundleContext getBundleContext() {
        return DsfTestPlugin.getBundleContext();
    }    

    @Override 
    public void initialize(final RequestMonitor requestMonitor) {
        super.initialize(
            new RequestMonitor(getExecutor(), requestMonitor) { 
                @Override
                public void handleSuccess() {
                    doInitialize(requestMonitor);
                }
            });
    }
            
    private void doInitialize(RequestMonitor requestMonitor) {
        register(new String[]{SimpleTestService.class.getName()}, new Hashtable<String,String>());
        requestMonitor.done();
    }

    @Override 
    public void shutdown(RequestMonitor requestMonitor) {
        unregister();
        super.shutdown(requestMonitor);
    }
}
