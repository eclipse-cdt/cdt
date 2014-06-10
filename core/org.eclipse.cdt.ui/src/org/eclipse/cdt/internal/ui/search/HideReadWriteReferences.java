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

public class HideReadWriteReferences extends MatchFilter {
	public static final MatchFilter READ_FILTER = new HideReadWriteReferences(false);
	public static final MatchFilter WRITE_FILTER = new HideReadWriteReferences(true);
	private boolean fIsWriteOnly;
	
	public HideReadWriteReferences(boolean isWriteOnly) {
		super();
		fIsWriteOnly = isWriteOnly;
	}

	@Override
	public boolean filters(Match match) {
		return match instanceof CSearchMatch && ((CSearchMatch) match).isWriteAccess() == fIsWriteOnly;
	}

	@Override
	public String getActionLabel() {
		return fIsWriteOnly ? CSearchMessages.HideWriteReferences_actionLabel : CSearchMessages.HideReadReferences_actionLabel;
	}

	@Override
	public String getDescription() {
		return fIsWriteOnly ? CSearchMessages.HideWriteReferences_description : CSearchMessages.HideReadReferences_description;
	}

	@Override
	public String getID() {
		return fIsWriteOnly ? "HideWriteReferences" : "HideReadReferences"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public String getName() {
		return fIsWriteOnly ? CSearchMessages.HideWriteReferences_name : CSearchMessages.HideReadReferences_name;
	}
}
