/*******************************************************************************
 * Copyright (c) 2008, 2012 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractconstant;

import org.eclipse.osgi.util.NLS;

final class Messages extends NLS {
	public static String InputPage_ConstName;
	public static String InputPage_EnterConstName;
	public static String InputPage_NameAlreadyDefined;
	public static String ExtractConstantRefactoring_ExtractConst;
	public static String ExtractConstantRefactoring_LiteralMustBeSelected;
	public static String ExtractConstantRefactoring_NoLiteralSelected;
	public static String ExtractConstantRefactoring_TooManyLiteralSelected;
	public static String ExtractConstantRefactoring_CreateConstant;
	public static String ExtractConstantRefactoring_ReplaceLiteral;

	private Messages() {
		// Do not instantiate
	}

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
}
