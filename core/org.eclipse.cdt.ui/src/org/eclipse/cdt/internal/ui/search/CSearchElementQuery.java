/*******************************************************************************
 * Copyright (c) 2006, 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.internal.ui.viewsupport.IndexUI;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;

/**
 * @author Doug Schaefer
 */
public class CSearchElementQuery extends CSearchQuery {
	private final ISourceReference element;
	private String label;

	public CSearchElementQuery(ICElement[] scope, ISourceReference element, int flags) {
		super(scope, flags | IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES);
		this.element = element;
		this.label = (element instanceof ICElement) ? ((ICElement) element).getElementName()
				: CSearchMessages.PDOMSearchElementQuery_something;
	}

	@Override
	public IStatus runWithIndex(IIndex index, IProgressMonitor monitor) throws OperationCanceledException {
		try {
			if (element instanceof ICElement) {
				IBinding binding = IndexUI.elementToBinding(index, (ICElement) element);
				if (binding != null) {
					label = labelForBinding(index, binding, label);
					// We should call CPPSemantics.pushLookupPoint() here.
					// Until we do, instantiation of dependent expressions may not work.
					createMatches(index, binding);
				}
			}
			return Status.OK_STATUS;
		} catch (CoreException e) {
			return e.getStatus();
		}
	}

	@Override
	public String getResultLabel(int numMatches) {
		return getResultLabel(label, numMatches);
	}
}
