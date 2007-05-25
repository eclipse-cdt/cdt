/*******************************************************************************
 * Copyright (c) 2006, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.search;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;

import org.eclipse.cdt.internal.ui.viewsupport.IndexUI;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMSearchElementQuery extends PDOMSearchQuery {

	private ISourceReference element;
	
	public PDOMSearchElementQuery(ICElement[] scope, ISourceReference element, int flags) {
		super(scope, flags);
		this.element = element;
	}

	public IStatus runWithIndex(IIndex index, IProgressMonitor monitor) throws OperationCanceledException {
		try {
			if (element instanceof ICElement) {
				IBinding binding= IndexUI.elementToBinding(index, (ICElement) element);
				if (binding != null) {
					createMatches(index, binding);
				}
			}
			return Status.OK_STATUS;
		} catch (CoreException e) {
			return e.getStatus();
		}
	}

	public String getLabel() {
		if (element instanceof ICElement)
			return super.getLabel() + " " + ((ICElement)element).getElementName(); //$NON-NLS-1$
		else
			return super.getLabel() + " something."; //$NON-NLS-1$
	}
}
