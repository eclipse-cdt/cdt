/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.navigator;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.ResourceWorkingSetFilter;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.WorkingSetFilterActionGroup;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

/**
 * A Common Navigator action provider adding standard working set filter support.
 * 
 * @see CNavigatorWorkingSetActionGroup
 * @see org.eclipse.cdt.internal.ui.workingsets.WorkingSetFilter
 */
public class CNavigatorWorkingSetActionProvider extends CommonActionProvider {

	private CNavigatorWorkingSetActionGroup fWorkingSetGroup;
	private ResourceWorkingSetFilter fWorkingSetFilter;
	private boolean fContributed;

	/*
	 * @see org.eclipse.ui.actions.ActionGroup#fillActionBars(org.eclipse.ui.IActionBars)
	 */
	public void fillActionBars(IActionBars actionBars) {
		if(fWorkingSetGroup != null && !fContributed) {
			// contribute only once to action bars
			fContributed= true;
			// add an extra separator before the working set filter actions
			// TLETODO [CN] add working set filter actions on top of the menu
			actionBars.getMenuManager().add(new Separator());
			fWorkingSetGroup.fillActionBars(actionBars);
		}
	}

	/*
	 * @see org.eclipse.ui.actions.ActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public void fillContextMenu(IMenuManager menu) {
		if (fWorkingSetGroup != null) {
			fWorkingSetGroup.fillContextMenu(menu);
		}
	}

	/*
	 * @see org.eclipse.ui.navigator.CommonActionProvider#init(org.eclipse.ui.navigator.ICommonActionExtensionSite)
	 */
	public void init(ICommonActionExtensionSite site) {
		super.init(site);
		ICommonViewerWorkbenchSite workbenchSite= null;
		if (site.getViewSite() instanceof ICommonViewerWorkbenchSite) {
			workbenchSite= (ICommonViewerWorkbenchSite) site.getViewSite();
		}
		if (workbenchSite != null) {
			if (workbenchSite.getPart() != null && workbenchSite.getPart() instanceof IViewPart) {
				final StructuredViewer viewer= site.getStructuredViewer();
				fWorkingSetFilter= new ResourceWorkingSetFilter();
				viewer.addFilter(fWorkingSetFilter);
				IPropertyChangeListener workingSetUpdater= new IPropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent event) {
		                String property= event.getProperty();
		                if (WorkingSetFilterActionGroup.CHANGE_WORKING_SET.equals(property)) {
		                    Object newValue= event.getNewValue();
		                    if (newValue instanceof IWorkingSet) {
		                    	fWorkingSetFilter.setWorkingSet((IWorkingSet) newValue);
								viewer.refresh();
		                    } else if (newValue == null) {
		                    	fWorkingSetFilter.setWorkingSet(null);
								viewer.refresh();
		                    }
		                }
					}};
				fWorkingSetGroup= new CNavigatorWorkingSetActionGroup(workbenchSite.getShell(), workingSetUpdater);
			}
		}
	}

	/*
	 * @see org.eclipse.ui.actions.ActionGroup#setContext(org.eclipse.ui.actions.ActionContext)
	 */
	public void setContext(ActionContext context) {
		super.setContext(context);
		if (fWorkingSetGroup != null) {
			fWorkingSetGroup.setContext(context);
		}
	}

	/*
	 * @see org.eclipse.ui.navigator.CommonActionProvider#restoreState(org.eclipse.ui.IMemento)
	 */
	public void restoreState(IMemento memento) {
		super.restoreState(memento);
		if (fWorkingSetGroup != null && memento != null) {
			fWorkingSetGroup.restoreState(memento);
		}
	}

	/*
	 * @see org.eclipse.ui.navigator.CommonActionProvider#saveState(org.eclipse.ui.IMemento)
	 */
	public void saveState(IMemento memento) {
		super.saveState(memento);
		if (fWorkingSetGroup != null && memento != null) {
			fWorkingSetGroup.saveState(memento);
		}
	}

	/*
	 * @see org.eclipse.ui.actions.ActionGroup#dispose()
	 */
	public void dispose() {
		if (fWorkingSetGroup != null) {
			fWorkingSetGroup.dispose();
			fWorkingSetGroup= null;
		}
		super.dispose();
	}

}
