/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
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
