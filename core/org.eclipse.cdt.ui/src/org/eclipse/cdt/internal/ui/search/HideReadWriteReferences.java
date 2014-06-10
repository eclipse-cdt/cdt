/*******************************************************************************
 * Copyright (c) 2014 Ericsson, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search;

import org.eclipse.search.ui.text.Match;
import org.eclipse.search.ui.text.MatchFilter;

/**
 * A filter class that implements both hiding the Read-only references and hiding the reference with write
 * access.
 */
public class HideReadWriteReferences extends MatchFilter {
	public static final MatchFilter READ_ONLY_FILTER = new HideReadWriteReferences(false);
	public static final MatchFilter WRITE_FILTER = new HideReadWriteReferences(true);
	private boolean fIsWriteAccess;
	
	public HideReadWriteReferences(boolean isWriteAccess) {
		super();
		fIsWriteAccess = isWriteAccess;
	}

	@Override
	public boolean filters(Match match) {
		return match instanceof CSearchMatch && ((CSearchMatch) match).isWriteAccess() == fIsWriteAccess;
	}

	@Override
	public String getActionLabel() {
		return fIsWriteAccess ? CSearchMessages.HideWriteReferences_actionLabel : CSearchMessages.HideReadOnlyReferences_actionLabel;
	}

	@Override
	public String getDescription() {
		return fIsWriteAccess ? CSearchMessages.HideWriteReferences_description : CSearchMessages.HideReadOnlyReferences_description;
	}

	@Override
	public String getID() {
		return fIsWriteAccess ? "HideWriteReferences" : "HideReadOnlyReferences"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public String getName() {
		return fIsWriteAccess ? CSearchMessages.HideWriteReferences_name : CSearchMessages.HideReadOnlyReferences_name;
	}
}
