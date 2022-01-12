/*******************************************************************************
 * Copyright (c) 2009, 2017 Wind River Systems, Inc. and others.
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
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly;

import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.DisassemblySelection;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.IDisassemblySelection;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * Selection provider for disassembly selections.
 * Wraps the selection provider of the underlying text viewer and provides
 *  {@link IDisassemblySelection}s instead of {@link ITextSelection}s.
 *
 * @since 2.1
 * @see IDisassemblySelection
 */
class DisassemblySelectionProvider implements ISelectionProvider {

	private final ListenerList<ISelectionChangedListener> fListenerList = new ListenerList<>(ListenerList.IDENTITY);
	private final ISelectionChangedListener fListener = event -> fireSelectionChanged(event);
	private final DisassemblyPart fPart;

	DisassemblySelectionProvider(DisassemblyPart disassemblyPart) {
		fPart = disassemblyPart;
		fPart.getTextViewer().getSelectionProvider().addSelectionChangedListener(fListener);
	}

	private void fireSelectionChanged(SelectionChangedEvent event) {
		SelectionChangedEvent newEvent = new SelectionChangedEvent(this, getSelection());
		for (ISelectionChangedListener listener : fListenerList) {
			listener.selectionChanged(newEvent);
		}
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		fListenerList.add(listener);
	}

	@Override
	public ISelection getSelection() {
		final ISourceViewer textViewer = fPart.getTextViewer();
		ISelectionProvider provider = textViewer.getSelectionProvider();
		if (provider != null) {
			return new DisassemblySelection((ITextSelection) provider.getSelection(), fPart);
		}
		return StructuredSelection.EMPTY;
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		fListenerList.remove(listener);
	}

	@Override
	public void setSelection(ISelection selection) {
		ISelectionProvider provider = fPart.getTextViewer().getSelectionProvider();
		if (provider != null) {
			provider.setSelection(selection);
		}
	}
}
