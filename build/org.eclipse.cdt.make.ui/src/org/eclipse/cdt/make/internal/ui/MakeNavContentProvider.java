/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.IMakeTargetListener;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.MakeTargetEvent;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;

public class MakeNavContentProvider implements ITreeContentProvider, IMakeTargetListener {

	private StructuredViewer viewer;

	public MakeNavContentProvider() {
		MakeCorePlugin.getDefault().getTargetManager().addListener(this);
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (StructuredViewer) viewer;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		// We're not a root provider so this won't get called
		return null;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IContainer) {
			IContainer container = (IContainer) parentElement;
			return getContainer(container);
		} else if (parentElement instanceof ICContainer) {
			IContainer container = ((ICContainer) parentElement).getResource();
			return getContainer(container);
		} else if (parentElement instanceof MakeTargetsContainer) {
			return ((MakeTargetsContainer) parentElement).getTargets();
		}
		return new Object[0];
	}

	private MakeTargetsContainer[] getContainer(IContainer container) {
		try {
			IMakeTarget[] targets = MakeCorePlugin.getDefault().getTargetManager().getTargets(container);
			if (targets != null && targets.length > 0) {
				return new MakeTargetsContainer[] { new MakeTargetsContainer(container, targets) };
			}
		} catch (CoreException e) {
			MakeUIPlugin.log(e.getStatus());
		}
		return new MakeTargetsContainer[0];
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof IMakeTarget || element instanceof MakeTargetsContainer) {
			// TODO need this?
			return null;
		} else {
			return null;
		}
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof IContainer || element instanceof ICContainer
				|| element instanceof MakeTargetsContainer) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void targetChanged(MakeTargetEvent event) {
		if (viewer == null || viewer.getControl().isDisposed()) {
			return;
		}

		switch (event.getType()) {
		case MakeTargetEvent.TARGET_ADD:
		case MakeTargetEvent.TARGET_REMOVED:
			Set<Object> elements = new HashSet<>();

			for (IMakeTarget target : event.getTargets()) {
				IContainer container = target.getContainer();
				elements.add(container);
			}

			if (!elements.isEmpty()) {
				viewer.getControl().getDisplay().asyncExec(() -> {
					for (Object element : elements) {
						viewer.refresh(element);
					}
				});
			}
		}
	}

}
