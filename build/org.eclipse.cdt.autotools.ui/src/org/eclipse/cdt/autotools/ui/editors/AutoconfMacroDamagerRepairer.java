/*******************************************************************************
 * Copyright (c) 2007, 2017 Red Hat, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.autotools.ui.editors;

import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;

public class AutoconfMacroDamagerRepairer extends DefaultDamagerRepairer {

	public final static String UNMATCHED_RIGHT_PARENTHESIS = "UnmatchedRightParenthesis"; //$NON-NLS-1$
	public final static String UNMATCHED_LEFT_PARENTHESIS = "UnmatchedLeftParenthesis"; //$NON-NLS-1$
	public final static String UNMATCHED_RIGHT_QUOTE = "UnmatchedRightQuote"; //$NON-NLS-1$
	public final static String UNMATCHED_LEFT_QUOTE = "UnmatchedLeftQuote"; //$NON-NLS-1$

	/**
	 * Creates a damager/repairer that uses the given scanner. The scanner may
	 * not be <code>null</code> and is assumed to return only token that carry
	 * text attributes.
	 *
	 * @param scanner
	 *            the token scanner to be used, may not be <code>null</code>
	 */
	public AutoconfMacroDamagerRepairer(ITokenScanner scanner) {
		super(scanner);
	}

}
