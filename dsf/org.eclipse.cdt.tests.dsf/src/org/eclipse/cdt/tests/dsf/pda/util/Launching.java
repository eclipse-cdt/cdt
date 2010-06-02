/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Nokia - create and use backend service. 
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.pda.util;

import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.examples.dsf.pda.service.PDABackend;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.Launch;

/**
 * 
 */
public class Launching {
    
	private static PDABackend fBackendService;
	
    public static Process launchPDA(DsfSession session, Launch launch, String pdaProgram) throws CoreException {
        
        class InitializeBackendServiceQuery extends Query<Object> {
            @Override
            protected void execute(DataRequestMonitor<Object> rm) {
                fBackendService.initialize(rm);
            }
        };

        fBackendService = new PDABackend(session, launch, pdaProgram);
    	InitializeBackendServiceQuery initQuery = new InitializeBackendServiceQuery();
        session.getExecutor().execute(initQuery);
        try {
			initQuery.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		} catch (ExecutionException e) {
			e.printStackTrace();
			return null;
		}        
   	
        return fBackendService.getProcess();
    }

	public static PDABackend getBackendService() {
		return fBackendService;
	}
}
