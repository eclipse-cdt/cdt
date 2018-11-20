/*******************************************************************************
 * Copyright (c) 2007, 2017 Wind River Systems, Inc. and others.
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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Control;

public class SelectionProviderMediator implements ISelectionProvider {
	private Map<Control, ISelectionProvider> fProviders = new HashMap<>();
	private ISelectionProvider fActiveProvider = null;
	private ISelectionChangedListener fSelectionChangedListener;
	private FocusListener fFocusListener;

	private ListenerList<ISelectionChangedListener> fListenerList = new ListenerList<>();

	public SelectionProviderMediator() {
		fSelectionChangedListener = event -> onSelectionChanged(event);
		fFocusListener = new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				onFocusGained(e);
			}

			@Override
			public void focusLost(FocusEvent e) {
			}
		};
	}

	@Override
	final public void addSelectionChangedListener(ISelectionChangedListener listener) {
		fListenerList.add(listener);
	}

	@Override
	final public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		fListenerList.remove(listener);
	}

	final protected void fireSelectionChanged() {
		if (!fListenerList.isEmpty()) {
			SelectionChangedEvent event = new SelectionChangedEvent(this, getSelection());

			for (ISelectionChangedListener listener : fListenerList) {
				listener.selectionChanged(event);
			}
		}
	}

	public void addViewer(Viewer viewer) {
		addSelectionProvider(viewer.getControl(), viewer);
	}

	public void addSelectionProvider(Control ctrl, ISelectionProvider sp) {
		fProviders.put(ctrl, sp);
		sp.addSelectionChangedListener(fSelectionChangedListener);
		ctrl.addFocusListener(fFocusListener);
	}

	// overrider
	@Override
	public ISelection getSelection() {
		if (fActiveProvider != null) {
			return fActiveProvider.getSelection();
		}
		return StructuredSelection.EMPTY;
	}

	// overrider
	@Override
	public void setSelection(ISelection selection) {
		if (fActiveProvider != null) {
			fActiveProvider.setSelection(selection);
		}
	}

	protected void onFocusGained(FocusEvent e) {
		ISelectionProvider provider = fProviders.get(e.widget);
		if (provider != null) {
			fActiveProvider = provider;
			fireSelectionChanged();
		}
	}

	protected void onSelectionChanged(SelectionChangedEvent event) {
		if (event.getSelectionProvider() == fActiveProvider) {
			fireSelectionChanged();
		}
	}
}
