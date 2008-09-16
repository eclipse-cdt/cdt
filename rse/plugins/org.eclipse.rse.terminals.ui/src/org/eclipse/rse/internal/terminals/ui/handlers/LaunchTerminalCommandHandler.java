/********************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * Martin Oberhuber (Wind River) - [187218] Fix error reporting for connect()
 * Kevin Doyle (IBM)			 - [187083] Launch Shell action available on folders inside virtual files
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * Yu-Fen Kuo       (MontaVista) - Adapted from SystemCommandAction
 * Anna Dushistova  (MontaVista) - [227535] Adapted from  LaunchTerminalAction to remove dependency from files.core
 * Anna Dushistova  (MontaVista) - [244637] [rseterminal] Launch Terminal with selected directory doesn't work
 ********************************************************************************/
package org.eclipse.rse.internal.terminals.ui.handlers;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.terminals.ui.TerminalServiceHelper;
import org.eclipse.rse.internal.terminals.ui.views.TerminalViewer;
import org.eclipse.rse.internal.terminals.ui.views.TerminalsUI;
import org.eclipse.rse.services.clientserver.PathUtility;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.subsystems.terminals.core.ITerminalServiceSubSystem;
import org.eclipse.rse.subsystems.terminals.core.elements.TerminalElement;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.ui.handlers.HandlerUtil;

public class LaunchTerminalCommandHandler extends AbstractHandler {

	private ITerminalServiceSubSystem subSystem;
	private Object selected;
	private ISystemFilterReference selectedFilterRef;

	public LaunchTerminalCommandHandler() {
	}

	private IHost getCurrentHost(IAdaptable adaptable) {
		IHost currentHost = null;

		ISystemViewElementAdapter adapter = (ISystemViewElementAdapter) adaptable
				.getAdapter(ISystemViewElementAdapter.class);
		if (adapter != null) {
			ISubSystem ss = adapter.getSubSystem(adaptable);
			if (ss != null) {
				currentHost = ss.getHost();
			}
		}
		return currentHost;
	}

	private ITerminalServiceSubSystem getTerminalSubSystem() {
		IHost currentHost = null;

		if (selectedFilterRef != null) {
			currentHost = getCurrentHost((IAdaptable) selectedFilterRef);
		} else if (selected != null) {
			currentHost = getCurrentHost((IAdaptable) selected);
		}
		if (currentHost != null) {
			return TerminalServiceHelper.getTerminalSubSystem(currentHost);

		}
		return subSystem;
	}

	private Object getTargetFromFilter() {
		ISystemViewElementAdapter adapter = (ISystemViewElementAdapter) ((IAdaptable) selectedFilterRef)
				.getAdapter(ISystemViewElementAdapter.class);
		if (adapter != null) {
			ISubSystem ss = adapter.getSubSystem(selectedFilterRef);
			if (ss != null) {
				Object target = ss.getTargetForFilter(selectedFilterRef);
				if (target != null) {
					return target;
				}
			}
		}
		return null;
	}

	private void init(IStructuredSelection selection) {
		Iterator e = selection.iterator();
		Object selectedObject = e.next();
		
		selected = null;
		subSystem = null;
		selectedFilterRef = null;
		
		if (selectedObject != null) {
			if (selectedObject instanceof ISystemFilterReference) {
				selectedFilterRef = (ISystemFilterReference) selectedObject;
				selected = getTargetFromFilter();
			} else if (selectedObject instanceof ITerminalServiceSubSystem) {
				subSystem = (ITerminalServiceSubSystem) selectedObject;
			} else {
				selected = selectedObject;
			}
		}
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		init((IStructuredSelection) HandlerUtil.getCurrentSelection(event));
		ITerminalServiceSubSystem terminalSubSystem = getTerminalSubSystem();
		if (terminalSubSystem != null) {
			TerminalsUI terminalsUI = TerminalsUI.getInstance();
			TerminalViewer viewer = terminalsUI.activateTerminalsView();
			if (!terminalSubSystem.isConnected()) {
				try {
					terminalSubSystem.connect(new NullProgressMonitor(), false);
				} catch (OperationCanceledException e) {
					// user canceled, return silently
					return null;
				} catch (Exception e) {
					SystemBasePlugin.logError(e.getLocalizedMessage(), e);
				}
			}
			if (terminalSubSystem.isConnected()) {
				CTabItem tab = viewer.getTabFolder().createTabItem(
						terminalSubSystem.getHost(), getInitialDirectoryCmd());
				TerminalElement element = TerminalServiceHelper
						.createTerminalElement(tab, terminalSubSystem);
				terminalSubSystem.addChild(element);

			}
		}
		return null;
	}

	private String getInitialDirectoryCmd() {
		if (selected == null)
			return null;
		String path = getWorkingDirectory(selected);

		String cdCmd; 
		if (getTerminalSubSystem().getHost().getSystemType().isWindows()) {
			cdCmd = "cd /d \"" + path + '\"'; //$NON-NLS-1$
		} else
		{
			cdCmd = "cd " + PathUtility.enQuoteUnix(path); //$NON-NLS-1$
		}
		return cdCmd + "\r"; //$NON-NLS-1$
	}

	private String getWorkingDirectory(Object element) {
		ISystemViewElementAdapter adapter = (ISystemViewElementAdapter) ((IAdaptable) element)
				.getAdapter(ISystemViewElementAdapter.class);
		if (adapter != null) {
			String path = (String) adapter.getAbsoluteName(element);
			// folder -- real or virtual
			if (ArchiveHandlerManager.isVirtual(path)) {
				path = path
						.substring(
								0,
								path
										.indexOf(ArchiveHandlerManager.VIRTUAL_CANONICAL_SEPARATOR));
			}
			return path;
		}
		return null;
	}


}
