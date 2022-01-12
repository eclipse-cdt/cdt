/*******************************************************************************
 * Copyright (c) 2008, 2016 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search;

import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.internal.ui.util.Messages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

/**
 * Query for searching unresolved includes in projects.
 * Could be extended to search resources selections.
 */
public class CSearchUnresolvedIncludesQuery extends CSearchQuery {

	public CSearchUnresolvedIncludesQuery(ICElement[] scope) {
		super(scope, 0);
	}

	@Override
	protected IStatus runWithIndex(final IIndex index, IProgressMonitor monitor) {
		try {
			for (IIndexFile file : index.getFilesWithUnresolvedIncludes()) {
				for (IIndexInclude include : file.getIncludes()) {
					if (include.isActive() && !include.isResolved()) {
						result.addMatch(new CSearchMatch(
								new ProblemSearchElement(IProblem.PREPROCESSOR_INCLUSION_NOT_FOUND,
										include.getFullName(), include.getIncludedByLocation()),
								include.getNameOffset(), include.getNameLength()));
					}
				}
			}
		} catch (CoreException e) {
			return e.getStatus();
		}
		return Status.OK_STATUS;
	}

	@Override
	public String getLabel() {
		return NLS.bind(CSearchMessages.PDOMSearchUnresolvedIncludesQuery_title, getScopeDescription());
	}

	@Override
	public String getResultLabel(int matchCount) {
		String countLabel = Messages.format(CSearchMessages.CSearchResultCollector_matches,
				Integer.valueOf(matchCount));
		return getLabel() + " " + countLabel; //$NON-NLS-1$
	}
}
