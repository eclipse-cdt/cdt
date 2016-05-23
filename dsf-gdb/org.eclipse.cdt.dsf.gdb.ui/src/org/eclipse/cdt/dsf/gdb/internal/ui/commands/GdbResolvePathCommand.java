/*******************************************************************************
 * Copyright (c) 2008, 2016 Stefan Sprenger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Stefan Sprenger Bug 491514: Command to handle path mapping via wizard
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.commands;

import java.util.List;

import org.eclipse.cdt.debug.core.model.IResolveHandler;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.gdb.actions.IResolve;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.ILaunch;

public class GdbResolvePathCommand implements IResolveHandler, IResolve {

	private final ILaunch fLaunch;
	private final DsfExecutor fExecutor;
    private final DsfServicesTracker fTracker;
    
    public GdbResolvePathCommand(DsfSession session, ILaunch launch) {
    	fLaunch = launch;
        fExecutor = session.getExecutor();
        fTracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), session.getId());
    }    
	
	@Override
	public void resolve(List<String> fileList) {
		// TODO DIALOG
		System.out.println("THIS SHOULD BE IN THE WIZARD: "+fileList);
	}

}

