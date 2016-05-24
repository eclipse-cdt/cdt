/*******************************************************************************
 * Copyright (c) 2014 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.breakpoints;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsAddedEvent;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsRemovedEvent;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.breakpoints.IBreakpointUpdater;
import org.eclipse.cdt.dsf.gdb.breakpoints.IBreakpointUpdater.IBreakpointProvider;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.service.DsfSession.SessionEndedListener;
import org.eclipse.cdt.dsf.service.DsfSession.SessionStartedListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

public class BreakpointUpdaterManager implements SessionStartedListener, SessionEndedListener, IBreakpointProvider {

	private static final String BP_UPDATER_EXTENSION_POINT_ID = "breakpointUpdaters"; //$NON-NLS-1$
	private static final String BP_UPDATER_ATTR_CLASS = "class"; //$NON-NLS-1$

	private boolean fActivated = false;
	private IBreakpointUpdater[] fUpdaters = new IBreakpointUpdater[0];

	private Map<String, Map<IBreakpointsTargetDMContext, List<IBreakpointDMContext>>> fTargetBps 
		= new HashMap<String, Map<IBreakpointsTargetDMContext, List<IBreakpointDMContext>>>();

	public void start() {
		load();
		DsfSession.addSessionStartedListener(this);
		DsfSession.addSessionEndedListener(this);
		fActivated = true;
	}
	
	public void stop() {
		fActivated = false;
		DsfSession.removeSessionStartedListener(this);
		DsfSession.removeSessionEndedListener(this);
		fUpdaters = new IBreakpointUpdater[0];
	}

	@Override
	public void sessionStarted(DsfSession session) {
		fTargetBps.put(session.getId(), new HashMap<IBreakpointsTargetDMContext, List<IBreakpointDMContext>>());
		session.addServiceEventListener(this, null);
	}

	@Override
	public void sessionEnded(DsfSession session) {
		session.removeServiceEventListener(this);
		Map<IBreakpointsTargetDMContext, List<IBreakpointDMContext>> map = fTargetBps.remove(session.getId());
		if (map != null) {
			for (List<IBreakpointDMContext> list : map.values()) {
				list.clear();
			}
			map.clear();
		}
	}

	@Override
	public IBreakpointDMContext[] getBreakpointsToUpdate(IDMContext dmc) {
		if (isActivated()) {
			IBreakpointsTargetDMContext targetDmc = DMContexts.getAncestorOfType(dmc, IBreakpointsTargetDMContext.class);
			if (targetDmc != null) {
				Map<IBreakpointsTargetDMContext, List<IBreakpointDMContext>> map = fTargetBps.get(dmc.getSessionId());
				if (map != null) {
					List<IBreakpointDMContext> list = map.get(targetDmc);
					if (list != null) {
						return list.toArray(new IBreakpointDMContext[list.size()]);
					}
				}
			}
		}
		return new IBreakpointDMContext[0];
	}

	@DsfServiceEventHandler
	public void eventDispatched(IBreakpointsAddedEvent event) {
		if (!isActivated()) {
			return;
		}
		String sessionId = event.getDMContext().getSessionId();
		Map<IBreakpointsTargetDMContext, List<IBreakpointDMContext>> targets = fTargetBps.get(sessionId);
		if (targets == null) {
			targets = new HashMap<IBreakpointsTargetDMContext, List<IBreakpointDMContext>>();
			fTargetBps.put(sessionId, targets);
		}
		List<IBreakpointDMContext> breakpoints = targets.get(event.getDMContext());
		if (breakpoints == null) {
			breakpoints = new ArrayList<IBreakpointDMContext>();
			targets.put(event.getDMContext(), breakpoints);
		}
		breakpoints.addAll(Arrays.asList(event.getBreakpoints()));

		for (IBreakpointUpdater bu : getBreakpointUpdaters()) {
			bu.updateBreakpoints(event.getBreakpoints());
		}
	}

	@DsfServiceEventHandler
	public void eventDispatched(IBreakpointsRemovedEvent event) {
		if (!isActivated()) {
			return;
		}
		String sessionId = event.getDMContext().getSessionId();
		Map<IBreakpointsTargetDMContext, List<IBreakpointDMContext>> targets = fTargetBps.get(sessionId);
		if (targets != null) {
			List<IBreakpointDMContext> breakpoints = targets.get(event.getDMContext());
			if (breakpoints != null) {
				breakpoints.removeAll(Arrays.asList(event.getBreakpoints()));
			}
		}
		for (IBreakpointUpdater bu : getBreakpointUpdaters()) {
			bu.updateBreakpoints(event.getBreakpoints());
		}
	}

	private void load() {
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(
				GdbPlugin.getUniqueIdentifier(), BP_UPDATER_EXTENSION_POINT_ID);
		if (extensionPoint != null) {
			IConfigurationElement[] elements = extensionPoint.getConfigurationElements();
			List<IBreakpointUpdater> list = new ArrayList<IBreakpointUpdater>(elements.length);
			for (IConfigurationElement el : elements) {
				try {
					IBreakpointUpdater updater = (IBreakpointUpdater)el.createExecutableExtension(BP_UPDATER_ATTR_CLASS);
					updater.initialize(this);
					list.add(updater);
				}
				catch(CoreException e) {
					GdbPlugin.log(e.getStatus());
				}
			}
			fUpdaters = list.toArray(new IBreakpointUpdater[list.size()]);
		}
	}
	
	private IBreakpointUpdater[] getBreakpointUpdaters() {
		return fUpdaters;
	}
	
	private boolean isActivated() {
		boolean useBPUpdaters = Platform.getPreferencesService().getBoolean(
				GdbPlugin.PLUGIN_ID,
				IGdbDebugPreferenceConstants.PREF_USE_BP_UPDATERS, true, null);
		return fActivated && useBPUpdaters;
	}
}
