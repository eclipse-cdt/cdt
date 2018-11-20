/*******************************************************************************
 * Copyright (c) 2008, 2015 ARM Limited and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * ARM Limited - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.disassembly.viewer;

import org.eclipse.cdt.debug.ui.disassembly.IDocumentElementAnnotationProvider;
import org.eclipse.cdt.debug.ui.disassembly.IDocumentPresentation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.ui.progress.UIJob;

/**
 * org.eclipse.cdt.debug.internal.ui.disassembly.viewer.DocumentAnnotationProvider:
 * //TODO Add description.
 */
public class DocumentAnnotationProvider {

	private VirtualDocument fDocument;

	public DocumentAnnotationProvider(VirtualDocument document) {
		fDocument = document;
	}

	public void dispose() {
		fDocument = null;
	}

	public void update(Object parent, Object[] elements, IDocumentPresentation context) {
		IDocumentElementAnnotationProvider annotationProvider = getAnnotationAdapter(parent);
		if (annotationProvider != null) {
			Object root = getDocument().getContentProvider().getRoot();
			Object base = getDocument().getContentProvider().getBase();
			DocumentAnnotationUpdate[] updates = new DocumentAnnotationUpdate[elements.length];
			for (int i = 0; i < elements.length; ++i) {
				updates[i] = new DocumentAnnotationUpdate(this, context, root, base, elements[i], i);
			}
			annotationProvider.update(updates);
		}
	}

	public void update(Object parent, Object element, int index, IDocumentPresentation context) {
		IDocumentElementAnnotationProvider annotationProvider = getAnnotationAdapter(element);
		if (annotationProvider != null) {
			Object root = getDocument().getContentProvider().getRoot();
			Object base = getDocument().getContentProvider().getBase();
			annotationProvider.update(new DocumentAnnotationUpdate[] {
					new DocumentAnnotationUpdate(this, context, root, base, element, index) });
		}
	}

	public void completed(DocumentAnnotationUpdate update) {
		if (update.isCanceled())
			return;

		final int index = update.getIndex();
		final Annotation[] annotations = update.getAnnotations();
		UIJob uiJob = new UIJob("Add annotations") { //$NON-NLS-1$

			/* (non-Javadoc)
			 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
			 */
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				VirtualDocument document = getDocument();
				if (document != null) {
					getDocument().updateAnnotations(index, annotations);
				}
				return Status.OK_STATUS;
			}
		};
		uiJob.setSystem(true);
		uiJob.schedule();
	}

	protected IDocumentElementAnnotationProvider getAnnotationAdapter(Object element) {
		IDocumentElementAnnotationProvider adapter = null;
		if (element instanceof IDocumentElementAnnotationProvider) {
			adapter = (IDocumentElementAnnotationProvider) element;
		} else if (element instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) element;
			adapter = adaptable.getAdapter(IDocumentElementAnnotationProvider.class);
		}
		return adapter;
	}

	protected VirtualDocument getDocument() {
		return fDocument;
	}
}
