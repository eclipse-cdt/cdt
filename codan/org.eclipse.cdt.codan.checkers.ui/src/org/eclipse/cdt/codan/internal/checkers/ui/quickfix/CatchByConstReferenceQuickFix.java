/*******************************************************************************
 * Copyright (c) 2010, 2012 Alena Laskavaia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - original API and implementation in CatchByReferenceQuickFix
 *    Tomasz Wesolowski - modified for const &
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.cdt.codan.internal.checkers.ui.Messages;

/**
 * Quick fix for catch by value
 */
public class CatchByConstReferenceQuickFix extends CatchByReferenceQuickFix {
	@Override
	public String getLabel() {
		return Messages.CatchByConstReferenceQuickFix_Message;
	}

	@Override
	protected boolean shouldDeclareConst() {
		return true;
	}
}
