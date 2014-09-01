/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.cdt.launchbar.ui.internal.targetsView;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.cdt.launchbar.core.ILaunchTarget;
import org.eclipse.cdt.launchbar.core.internal.LaunchBarManager;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class LaunchTargetsContentProvider implements ITreeContentProvider {

	private LaunchBarManager manager;

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof LaunchBarManager) {
			ILaunchTarget[] targets = ((LaunchBarManager) inputElement).getAllLaunchTargets();
			Arrays.sort(targets, new Comparator<ILaunchTarget>() {
				@Override
				public int compare(ILaunchTarget o1, ILaunchTarget o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			return targets;
		}
		return null;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		return new Object[0];
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof ILaunchTarget) {
			return manager;
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof LaunchBarManager)
			return true;
		else if (element instanceof ILaunchTarget)
			return false;
		return false;
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof LaunchBarManager) {
			manager = (LaunchBarManager) newInput;
		}
	}

}
