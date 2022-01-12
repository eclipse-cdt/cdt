/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.expression;

import org.eclipse.osgi.util.NLS;

/**
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class MessagesForExpressionVM extends NLS {
	public static String ExpressionColumnPresentation_expression;
	public static String ExpressionColumnPresentation_name;
	public static String ExpressionColumnPresentation_type;
	public static String ExpressionColumnPresentation_value;
	public static String ExpressionColumnPresentation_address;
	public static String ExpressionColumnPresentation_description;

	public static String ExpressionManagerLayoutNode__invalidExpression_nameColumn_label;
	public static String ExpressionManagerLayoutNode__invalidExpression_valueColumn_label;

	public static String ExpressionManagerLayoutNode__newExpression_label;

	public static String DisabledExpressionVMNode_disabled_value;
	public static String DisabledExpressionVMNode_disabled_no_columns;

	static {
		// initialize resource bundle
		NLS.initializeMessages(MessagesForExpressionVM.class.getName(), MessagesForExpressionVM.class);
	}

	private MessagesForExpressionVM() {
	}
}
