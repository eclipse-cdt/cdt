/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console;

import org.eclipse.cdt.dsf.gdb.launching.GDBProcess;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.internal.core.StreamsProxy;
import org.eclipse.debug.internal.ui.views.console.ProcessConsole;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.console.ConsoleColorProvider;
import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.internal.console.IOConsolePage;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * A GDB CLI console.
 * This console simply provides an IOConsole to perform CLI commands
 * towards GDB.  It is used whenever {@link IGDBBackend#isFullGdbConsoleSupported()}
 * returns false.
 */
public class GdbBasicCliConsole extends ProcessConsole implements IGdbConsole {

	private ILaunch fLaunch;
	private String fLabel;
	private IProcess fProcess;
	
	private static class EmptyConsoleColorProvider extends ConsoleColorProvider {
		
		ILaunch fLaunch;
		
		public EmptyConsoleColorProvider(ILaunch launch) {
			fLaunch = launch;
		}
		
		@Override
		public boolean isReadOnly() {
			return false;
		}
		
		@Override
		public Color getColor(String streamIdentifer) {
			return null;
		}
		
		@Override
		public void connect(IProcess process, IConsole console) {
			if (process instanceof GDBProcess) {
				Process process2 = ((GDBProcess)process).getSystemProcess();
				String encoding = fLaunch.getAttribute(DebugPlugin.ATTR_CONSOLE_ENCODING);
				IStreamsProxy streamsProxy = new StreamsProxy(process2, encoding);
				console.connect(streamsProxy);
			}
		}
		
		@Override
		public void disconnect() {
		}
		
	}
	public GdbBasicCliConsole(ILaunch launch, String label, IProcess process) {
		super(process, new EmptyConsoleColorProvider(launch));
		fLaunch = launch;
        fLabel = label;
        fProcess = process;
	}
	
	public IProcess getProcess() {
		return fProcess;
	}
	
	@Override
	public ILaunch getLaunch() {
		return fLaunch;
	}
	
    @Override
	public void resetName() {
    	String newName = computeName();
    	String name = getName();
    	if (!name.equals(newName)) {
    		PlatformUI.getWorkbench().getDisplay().asyncExec(() -> setName(newName));
    	}
    }
	
    protected String computeName() {
        String label = fLabel;

        if (fLaunch == null) return "";
        ILaunchConfiguration config = fLaunch.getLaunchConfiguration();
        if (config != null && !DebugUITools.isPrivate(config)) {
        	String type = null;
        	try {
        		type = config.getType().getName();
        	} catch (CoreException e) {
        	}
        	StringBuffer buffer = new StringBuffer();
        	buffer.append(config.getName());
        	if (type != null) {
        		buffer.append(" ["); //$NON-NLS-1$
        		buffer.append(type);
        		buffer.append("] "); //$NON-NLS-1$
        	}
        	buffer.append(label);
        	label = buffer.toString();
        }

        if (fLaunch.isTerminated()) {
        	return ConsoleMessages.ConsoleMessages_console_terminated + label; 
        }
        
        return label;
    }
	
    @Override
	public IPageBookViewPage createPage(IConsoleView view) {
    	
        return new IOConsolePage(this, view) {
        	@Override
        	protected void configureToolBar(IToolBarManager mgr) {
        	}
        };
    }

    @Override
	public void setInvertedColors(boolean enable) {
    }
}
