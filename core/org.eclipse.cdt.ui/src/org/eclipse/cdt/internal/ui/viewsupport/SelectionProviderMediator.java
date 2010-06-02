/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	private Map<Control, ISelectionProvider> fProviders= new HashMap<Control, ISelectionProvider>();
	private ISelectionProvider fActiveProvider = null;
	private ISelectionChangedListener fSelectionChangedListener;
	private FocusListener fFocusListener;

    private ListenerList fListenerList= new ListenerList();

	public SelectionProviderMediator() {
		fSelectionChangedListener= new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				onSelectionChanged(event);
			}
		};
		fFocusListener = new FocusListener() {
			public void focusGained(FocusEvent e) {
				onFocusGained(e);
			}
			public void focusLost(FocusEvent e) {
			}
		};
	}

    final public void addSelectionChangedListener(ISelectionChangedListener listener) {
    	fListenerList.add(listener);
    }

    final public void removeSelectionChangedListener(ISelectionChangedListener listener) {
    	fListenerList.remove(listener);
    }

	final protected void fireSelectionChanged() {
		Object[] listeners= fListenerList.getListeners();
		if (listeners.length > 0) {
			SelectionChangedEvent event= new SelectionChangedEvent(this, getSelection());
			
			for (int i = 0; i < listeners.length; i++) {
				ISelectionChangedListener listener= (ISelectionChangedListener) listeners[i];
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
	public ISelection getSelection() {
		if (fActiveProvider != null) {
			return fActiveProvider.getSelection();
		}
		return StructuredSelection.EMPTY;
	}
	// overrider
	public void setSelection(ISelection selection) {
		if (fActiveProvider != null) {
			fActiveProvider.setSelection(selection);
		}
	}

	protected void onFocusGained(FocusEvent e) {
		ISelectionProvider provider= fProviders.get(e.widget);
		if (provider != null) {
			fActiveProvider= provider;
			fireSelectionChanged();
		}
	}

	protected void onSelectionChanged(SelectionChangedEvent event) {
		if (event.getSelectionProvider() == fActiveProvider) {
			fireSelectionChanged();
		}
	}
}
