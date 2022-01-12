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
package org.eclipse.cdt.internal.ui.indexview;

import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.ui.search.CSearchQuery;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;

/**
 * @author Doug Schaefer
 *
 * This is the search query to be used for searching the PDOM.
 */
public class IndexViewSearchQuery extends CSearchQuery {
	private IIndexBinding fBinding;
	private long fLastWrite;
	private String fName;
	private ICProject fProject;

	public IndexViewSearchQuery(ICElement[] scope, ICProject project, long pdomLastWrite, IIndexBinding binding,
			String name, int flags) {
		super(scope, flags);
		fProject = project;
		fBinding = binding;
		fLastWrite = pdomLastWrite;
		fName = name;
	}

	@Override
	public IStatus runWithIndex(IIndex index, IProgressMonitor monitor) throws OperationCanceledException {
		try {
			if (CCoreInternals.getPDOMManager().getPDOM(fProject).getLastWriteAccess() == fLastWrite) {
				// We should call CPPSemantics.pushLookupPoint() here.
				// Until we do, instantiation of dependent expressions may not work.
				createMatches(index, fBinding);
			}
			return Status.OK_STATUS;
		} catch (CoreException e) {
			return new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, e.getLocalizedMessage(), e);
		}
	}

	@Override
	public String getResultLabel(int numMatches) {
		return super.getResultLabel(fName, numMatches);
	}
}
