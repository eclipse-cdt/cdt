/*******************************************************************************
 * Copyright (c) 2006, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.viewsupport;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkingSetFilterActionGroup;

/**
 * Wraps {@link WorkingSetFilterActionGroup} and handles the property changed
 * events
 */
public abstract class WorkingSetFilterUI {
	private IPropertyChangeListener fWorkingSetListener;
	private IWorkingSet fWorkingSet;
	WorkingSetFilterActionGroup fWorkingSetFilterGroup;
	private IViewPart fViewPart;
	private WorkingSetFilter fWorkingSetFilter = null;
	private IWorkingSetManager fWSManager;

	public WorkingSetFilterUI(IViewPart viewPart, IMemento memento, String key) {
		fWSManager = PlatformUI.getWorkbench().getWorkingSetManager();
		fViewPart = viewPart;

		if (memento != null) {
			memento = memento.getChild(key);
			if (memento != null) {
				IWorkingSet ws = fWSManager.createWorkingSet(memento);
				if (ws != null) {
					fWorkingSet = fWSManager.getWorkingSet(ws.getName());
					if (fWorkingSet == null) {
						fWorkingSet = ws;
						fWSManager.addWorkingSet(ws);
					}
				}
			}
		}
		fWorkingSetListener = new IPropertyChangeListener() {
			@Override
			public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event) {
				onWorkingSetPropertyChange(event);
			}
		};
		fWSManager.addPropertyChangeListener(fWorkingSetListener);
		IPropertyChangeListener workingSetUpdater = new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				onWorkingSetFilterUpdate(event);
			}
		};

		fWorkingSetFilterGroup = new WorkingSetFilterActionGroup(fViewPart.getSite().getShell(), workingSetUpdater);
		fWorkingSetFilterGroup.setWorkingSet(fWorkingSet);
	}

	public void dispose() {
		fWSManager.removePropertyChangeListener(fWorkingSetListener);
		fWorkingSetFilterGroup.dispose();
	}

	private void applyWorkingSetFilter() {
		if (fWorkingSet == null) {
			fWorkingSetFilter = null;
		} else {
			fWorkingSetFilter = new WorkingSetFilter();
			fWorkingSetFilter.setWorkingSet(fWorkingSet);
		}
	}

	protected void onWorkingSetPropertyChange(PropertyChangeEvent evt) {
		if (fWorkingSet == null) {
			return;
		}
		boolean doRefresh = false;
		String propertyName = evt.getProperty();
		Object newValue = evt.getNewValue();
		Object oldValue = evt.getOldValue();

		if (propertyName.equals(IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE)) {
			if (fWorkingSet == newValue) { // weired, but this is how it works
				doRefresh = true;
			}
		} else if (propertyName.equals(IWorkingSetManager.CHANGE_WORKING_SET_NAME_CHANGE)) {
			if (fWorkingSet == newValue) {
				onWorkingSetNameChange();
			}
		} else if (propertyName.equals(IWorkingSetManager.CHANGE_WORKING_SET_REMOVE)) {
			if (fWorkingSet == oldValue) {
				fWorkingSet = null;
				doRefresh = true;
			}
		}
		if (doRefresh) {
			applyWorkingSetFilter();
			onWorkingSetChange();
		}
	}

	protected void onWorkingSetFilterUpdate(PropertyChangeEvent event) {
		String property = event.getProperty();
		if (WorkingSetFilterActionGroup.CHANGE_WORKING_SET.equals(property)) {
			Object newValue = event.getNewValue();

			if (newValue instanceof IWorkingSet) {
				fWorkingSet = (IWorkingSet) newValue;
				fWSManager.addRecentWorkingSet(fWorkingSet);
			} else {
				fWorkingSet = null;
			}
			applyWorkingSetFilter();
			onWorkingSetChange();
			onWorkingSetNameChange();
		}
	}

	protected abstract void onWorkingSetChange();

	protected abstract void onWorkingSetNameChange();

	public void fillActionBars(IActionBars actionBars) {
		fWorkingSetFilterGroup.fillActionBars(actionBars);
	}

	public boolean isPartOfWorkingSet(ICElement element) {
		if (fWorkingSetFilter == null) {
			return true;
		}
		return fWorkingSetFilter.isPartOfWorkingSet(element);
	}

	public boolean isPartOfWorkingSet(IPath resourceOrExternalPath) {
		if (fWorkingSetFilter == null) {
			return true;
		}
		return fWorkingSetFilter.isPartOfWorkingSet(resourceOrExternalPath);
	}

	public IWorkingSet getWorkingSet() {
		return fWorkingSet;
	}

	public void setWorkingSet(IWorkingSet workingSet) {
		fWorkingSet = workingSet;
		fWorkingSetFilterGroup.setWorkingSet(fWorkingSet);
	}

	public List<String> getRecent() {
		IWorkingSet[] workingSets = fWSManager.getRecentWorkingSets();
		ArrayList<String> result = new ArrayList<>(workingSets.length);
		for (int i = 0; i < workingSets.length; i++) {
			result.add(workingSets[i].getName());
		}
		return result;
	}

	public void saveState(IMemento memento, String key) {
		if (fWorkingSet != null) {
			fWorkingSet.saveState(memento.createChild(key));
		}
	}
}
