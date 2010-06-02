/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.ui.viewsupport;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * A selection provider that adapts the elements of structured selections
 * to a requested type. 
 * @author markus.schorn@windriver.com
 */
public class AdaptingSelectionProvider implements ISelectionProvider, ISelectionChangedListener {

	private Class<?> fTargetType;
	private ListenerList fListenerList;
	private ISelectionProvider fProvider;

	public AdaptingSelectionProvider(Class<?> targetType, ISelectionProvider provider) {
		fProvider= provider;
		fTargetType= targetType;
		fListenerList= new ListenerList();
	}
	
	private ISelection convertSelection(ISelection selection) {
		if (selection != null) {
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection ss= (IStructuredSelection) selection;
				ArrayList<Object> adapted= new ArrayList<Object>();
				for (Iterator<?> iter = ss.iterator(); iter.hasNext(); ) {
					Object elem= adaptElem(iter.next());
					if (elem != null) {
						adapted.add(elem);
					}
				}
				return new StructuredSelection(adapted);
			}
		}
		return selection;
	}

	private Object adaptElem(Object elem) {
		if (fTargetType.isInstance(elem)) {
			return elem;
		}
		if (elem instanceof IAdaptable) {
			return ((IAdaptable) elem).getAdapter(fTargetType);
		}
		return null;
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		if (fListenerList.isEmpty()) {
			fProvider.addSelectionChangedListener(this);
		}
		fListenerList.add(listener);
	}

	public ISelection getSelection() {
		return convertSelection(fProvider.getSelection());
	}

	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		fListenerList.remove(listener);
		if (fListenerList.isEmpty()) {
			fProvider.removeSelectionChangedListener(this);
		}
	}

	public void setSelection(ISelection selection) {
		throw new UnsupportedOperationException();
	}

	public void selectionChanged(SelectionChangedEvent event) {
		SelectionChangedEvent event2= new SelectionChangedEvent(this, convertSelection(event.getSelection()));
		Object[] listeners= fListenerList.getListeners();
		for (Object listener : listeners) {
			ISelectionChangedListener l= (ISelectionChangedListener) listener;
			l.selectionChanged(event2);
		}
	}
}
