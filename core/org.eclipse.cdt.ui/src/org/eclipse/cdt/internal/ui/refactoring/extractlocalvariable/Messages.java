/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *     Google
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractlocalvariable;

import org.eclipse.osgi.util.NLS;

final class Messages extends NLS {
	public static String CreateLocalVariable;
	public static String EnterVariableName;
	public static String ExpressionMustBeSelected;
	public static String ExtractLocalVariable;
	public static String NameAlreadyDefined;
	public static String NoExpressionSelected;
	public static String ReplaceExpression;
	public static String TooManyExpressionsSelected;
	public static String VariableName;

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	// Do not instantiate
	private Messages() {
	}
}
