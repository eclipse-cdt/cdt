/*******************************************************************************
 * Copyright (c) 2009, 2013 Wind River Systems, Inc. and others.
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
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search;

import org.eclipse.search.ui.text.Match;
import org.eclipse.search.ui.text.MatchFilter;

public class HidePolymorphicCalls extends MatchFilter {
	public static final MatchFilter FILTER = new HidePolymorphicCalls();

	@Override
	public boolean filters(Match match) {
		return match instanceof CSearchMatch && ((CSearchMatch) match).isPolymorphicCall();
	}

	@Override
	public String getActionLabel() {
		return CSearchMessages.HidePolymorphicCalls_actionLabel;
	}

	@Override
	public String getDescription() {
		return CSearchMessages.HidePolymorphicCalls_description;
	}

	@Override
	public String getID() {
		return "HidePolymorphicCalls"; //$NON-NLS-1$
	}

	@Override
	public String getName() {
		return CSearchMessages.HidePolymorphicCalls_name;
	}
}
