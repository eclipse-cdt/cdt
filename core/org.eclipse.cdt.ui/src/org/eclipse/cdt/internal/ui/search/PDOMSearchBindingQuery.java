/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
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

import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.ui.CUIPlugin;

/**
 * @author Doug Schaefer
 * 
 * This is the search query to be used for searching the PDOM.
 */
public class PDOMSearchBindingQuery extends PDOMSearchQuery {

	private IIndexBinding binding;
	
	public PDOMSearchBindingQuery(ICElement[] scope, IIndexBinding binding, int flags) {
		super(scope, flags);
		this.binding = binding;
	}
	
	public IStatus runWithIndex(IIndex index, IProgressMonitor monitor) throws OperationCanceledException {
		try {
			createMatches(index, binding);
			return Status.OK_STATUS;
		} catch (CoreException e) {
			return new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, 0, e.getLocalizedMessage(), e);
		}
	}

	public String getLabel() {
		return super.getLabel() + " " + binding.getName(); //$NON-NLS-1$
	}

}
