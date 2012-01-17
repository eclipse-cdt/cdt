/*******************************************************************************
 * Copyright (c) 2006, 2010 STMicroelectronics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * STMicroelectronics - Process console enhancements
 * Alena Laskavaia (QNX) - Fix for 186172
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.ui.console;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.cdt.debug.mi.core.GDBProcess;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.cdt.debug.mi.ui.console.actions.MiConsoleSaveAction;
import org.eclipse.cdt.debug.mi.ui.console.actions.MiConsoleVerboseModeAction;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.console.IConsole;

/**
 * Enhances ProcessConsole when the process attached is a GDBProcess
 * @since 6.1
 *
 */
public class MiConsolePageParticipant implements IConsolePageParticipant, IDebugEventSetListener, Observer {

	private MiConsoleSaveAction fSaveConsole = null;
	private MiConsoleVerboseModeAction fVerboseMode = null;
	private org.eclipse.debug.ui.console.IConsole fConsole = null;
	private org.eclipse.cdt.debug.mi.core.GDBProcess GDBProcess = null;

	@Override
	public void activated() {
	}

	@Override
	public void deactivated() {
	}

	@Override
	public void dispose() {
		if (GDBProcess != null) {
			DebugPlugin.getDefault().removeDebugEventListener(this);
		}
		fSaveConsole = null;
		fVerboseMode = null;
		GDBProcess = null;
		fConsole = null;
	}

	@Override
	public void init(IPageBookViewPage page, IConsole console) {

		if(console instanceof org.eclipse.debug.ui.console.IConsole)
		{
			fConsole = (org.eclipse.debug.ui.console.IConsole) console;
			if(fConsole.getProcess() instanceof GDBProcess) {

				GDBProcess = (GDBProcess) fConsole.getProcess();

				// add two new actions: save console content and verbose console mode switcher
				IActionBars bars = page.getSite().getActionBars();
				bars.getToolBarManager().appendToGroup(IConsoleConstants.OUTPUT_GROUP, new Separator());
				fSaveConsole = new MiConsoleSaveAction(fConsole);
				bars.getToolBarManager().appendToGroup(IConsoleConstants.OUTPUT_GROUP, fSaveConsole);
				fVerboseMode = new MiConsoleVerboseModeAction(fConsole);
				bars.getToolBarManager().appendToGroup(IConsoleConstants.OUTPUT_GROUP, fVerboseMode);
				bars.getToolBarManager().appendToGroup(IConsoleConstants.OUTPUT_GROUP, new Separator());

				// add a debug event listener
				DebugPlugin.getDefault().addDebugEventListener(this);
				// if we miss change event update enablement manually
				fVerboseMode.updateStateAndEnablement();
				Target target = GDBProcess.getTarget();
				if (target != null) {
					// register this object as MISession observer
					target.getMISession().addObserver(this);
				}
			}
		}
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public void handleDebugEvents(DebugEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			DebugEvent event = events[i];
			if (event.getSource().equals(GDBProcess)) {
				if (fVerboseMode != null) {
					fVerboseMode.updateStateAndEnablement();
					Target target = GDBProcess.getTarget();
					if (target != null) {
						// register this object as MISession observer
						target.getMISession().addObserver(this);
					}
				}
			}
		}
	}

	/** 
	 * Handle MISession notification
	 */
	 @Override
	public void update(Observable arg0, Object arg1) {
		 if((arg1!=null) && (arg1 instanceof VerboseModeChangedEvent) && (fVerboseMode != null)) {
			 try {
				fVerboseMode.updateStateAndEnablement();
			} catch (Exception e) {
			}          
		 }
	 }

}
