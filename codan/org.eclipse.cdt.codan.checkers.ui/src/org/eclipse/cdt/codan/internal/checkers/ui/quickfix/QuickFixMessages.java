/*******************************************************************************
 * Copyright (c) 2011 Alena Laskavaia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.osgi.util.NLS;

public class QuickFixMessages extends NLS {
	public static String CaseBreakQuickFixBreak_Label;
	public static String CaseBreakQuickFixComment_Label;
	public static String QuickFixCreateField_create_field;
	public static String QuickFixCreateLocalVariable_create_local_variable;
	public static String QuickFixCreateParameter_create_parameter;

	static {
		NLS.initializeMessages(QuickFixMessages.class.getName(), QuickFixMessages.class);
	}

	private QuickFixMessages() {
	}
}
