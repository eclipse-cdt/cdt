/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation (JDT)
 *     Tomasz Wesolowski - port from JDT
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import java.util.Stack;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import org.eclipse.cdt.core.model.ISourceRange;

public class SelectionHistory {

	private Stack<ISourceRange> fHistory;
	private CEditor fEditor;
	private ISelectionChangedListener fSelectionListener;
	private int fSelectionChangeListenerCounter;

	public SelectionHistory(CEditor editor) {
		Assert.isNotNull(editor);
		fEditor= editor;
		fHistory= new Stack<ISourceRange>();
		fSelectionListener= new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (fSelectionChangeListenerCounter == 0)
					flush();
			}
		};
		fEditor.getSelectionProvider().addSelectionChangedListener(fSelectionListener);
	}

	public boolean isEmpty() {
		return fHistory.isEmpty();
	}

	public void remember(ISourceRange range) {
		fHistory.push(range);
	}

	public ISourceRange getLast() {
		if (isEmpty())
			return null;
		ISourceRange result= fHistory.pop();
		return result;
	}

	public void flush() {
		if (fHistory.isEmpty())
			return;
		fHistory.clear();
	}

	public void ignoreSelectionChanges() {
		fSelectionChangeListenerCounter++;
	}

	public void listenToSelectionChanges() {
		fSelectionChangeListenerCounter--;
	}

	public void dispose() {
		fEditor.getSelectionProvider().removeSelectionChangedListener(fSelectionListener);
	}
}