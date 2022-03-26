/*******************************************************************************
 * Copyright (c) 2022 Mathema and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.internal.ui.cview.IncludeRefContainer;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.actions.ActionDelegate;

public class IncludeGroupAction extends ActionDelegate {

	private IStructuredSelection selection = StructuredSelection.EMPTY;

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = (selection instanceof IStructuredSelection) ? (IStructuredSelection) selection
				: StructuredSelection.EMPTY;
	}

	@Override
	public void runWithEvent(IAction action, Event event) {
		var id = action.getId();

		Set<IResource> prjs = new HashSet<>();

		for (var o : selection.toArray()) {
			if (o instanceof IncludeRefContainer) {
				prjs.add(((IncludeRefContainer) o).getCProject().getResource());
			}
		}

		assert !prjs.isEmpty();

		for (var prj : prjs) {
			var valstr = IncludeRefContainer.qname;

			try {
				if (id.equals("org.eclipse.cdt.make.ui.incgroup.list")) { //$NON-NLS-1$
					prj.setPersistentProperty(valstr, IncludeRefContainer.Representation.List.toString());
				} else if (id.equals("org.eclipse.cdt.make.ui.incgroup.single")) { //$NON-NLS-1$
					prj.setPersistentProperty(valstr, IncludeRefContainer.Representation.Single.toString());
				} else if (id.equals("org.eclipse.cdt.make.ui.incgroup.compact")) { //$NON-NLS-1$
					prj.setPersistentProperty(valstr, IncludeRefContainer.Representation.Compact.toString());
				} else if (id.equals("org.eclipse.cdt.make.ui.incgroup.smart")) { //$NON-NLS-1$
					prj.setPersistentProperty(valstr, IncludeRefContainer.Representation.Smart.toString());
				} else {
					assert (false);
				}

			} catch (CoreException e) {
				CUIPlugin.log(e);
			}
		}

		// The persistent properties don't have listeners, so to force the UI to update
		// we change this "fake" preference which the appropriate UIs listen to and
		// refresh their viewers.
		PreferenceConstants.cViewForceRefresh();

	}
}
