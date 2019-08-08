/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.launch;

import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.datamodel.DataModelInitializedEvent;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.ModelProxyInstalledEvent;
import org.eclipse.cdt.dsf.ui.viewmodel.RootVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.VMDelta;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;

/**
 * Layout node for the standard ILaunch object.  This node can only be used at
 * the root of a hierarchy.  It does not implement the label provider
 * functionality, so the default adapters should be used to retrieve the label.
 */
public class LaunchRootVMNode extends RootVMNode {
	public static class LaunchesEvent {
		public enum Type {
			ADDED, REMOVED, CHANGED, TERMINATED
		}

		public final ILaunch[] fLaunches;
		public final Type fType;

		public LaunchesEvent(ILaunch[] launches, Type type) {
			fLaunches = launches;
			fType = type;
		}
	}

	public LaunchRootVMNode(AbstractVMProvider provider) {
		super(provider);
	}

	@Override
	public String toString() {
		return "LaunchRootVMNode"; //$NON-NLS-1$
	}

	@Override
	public boolean isDeltaEvent(Object rootObject, Object e) {
		if (e instanceof DebugEvent) {
			DebugEvent de = (DebugEvent) e;
			if (de.getSource() instanceof IProcess && !((IProcess) de.getSource()).getLaunch().equals(rootObject)) {
				return false;
			} else if (de.getSource() instanceof IDebugElement
					&& !rootObject.equals(((IDebugElement) de.getSource()).getLaunch())) {
				return false;
			}
		} else if (e instanceof ModelProxyInstalledEvent || e instanceof DataModelInitializedEvent) {
			return true;
		}

		return super.isDeltaEvent(rootObject, e);
	}

	@Override
	public int getDeltaFlags(Object e) {
		int flags = 0;
		if (e instanceof LaunchesEvent) {
			LaunchesEvent le = (LaunchesEvent) e;
			if (le.fType == LaunchesEvent.Type.CHANGED || le.fType == LaunchesEvent.Type.TERMINATED) {
				flags = IModelDelta.STATE | IModelDelta.CONTENT;
			}
		} else if (e instanceof ModelProxyInstalledEvent || e instanceof DataModelInitializedEvent) {
			flags = IModelDelta.EXPAND | IModelDelta.SELECT;
		}

		return flags;
	}

	@Override
	public void createRootDelta(Object rootObject, Object event, final DataRequestMonitor<VMDelta> rm) {
		if (!(rootObject instanceof ILaunch)) {
			rm.setStatus(new Status(IStatus.ERROR, DsfUIPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE,
					"Invalid root element configured with launch root node.", null)); //$NON-NLS-1$
			rm.done();
			return;
		}

		ILaunch rootLaunch = (ILaunch) rootObject;

		/*
		 * Create the root of the delta.  Since the launch object is not at the
		 * root of the view, create the delta with the path to the launch.
		 */
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		List<ILaunch> launchList = Arrays.asList(manager.getLaunches());
		final VMDelta viewRootDelta = new VMDelta(manager, 0, IModelDelta.NO_CHANGE, launchList.size());
		final VMDelta rootDelta = viewRootDelta.addNode(rootLaunch, launchList.indexOf(rootLaunch),
				IModelDelta.NO_CHANGE);

		// Generate delta for launch node.
		if (event instanceof LaunchesEvent) {
			LaunchesEvent le = (LaunchesEvent) event;
			for (ILaunch launch : le.fLaunches) {
				if (rootLaunch == launch) {
					if (le.fType == LaunchesEvent.Type.CHANGED) {
						rootDelta.setFlags(rootDelta.getFlags() | IModelDelta.STATE | IModelDelta.CONTENT);
					} else if (le.fType == LaunchesEvent.Type.TERMINATED) {
						rootDelta.setFlags(rootDelta.getFlags() | IModelDelta.STATE | IModelDelta.CONTENT);
					}
				}
			}
		} else if (event instanceof ModelProxyInstalledEvent || event instanceof DataModelInitializedEvent) {
			rootDelta.setFlags(rootDelta.getFlags() | IModelDelta.EXPAND | IModelDelta.SELECT);
		}
		rm.setData(rootDelta);
		rm.done();
	}

}
