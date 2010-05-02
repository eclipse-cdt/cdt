/*******************************************************************************
 * Copyright (c) 2009,2010 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui;

import org.eclipse.osgi.util.NLS;

/**
 * TODO: add description
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.codan.internal.checkers.ui.messages"; //$NON-NLS-1$
	public static String CatchByReferenceQuickFix_Message;
	public static String QuickFixAssignmentInCondition_Message;
	public static String SuggestedParenthesisQuickFix_Message;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
