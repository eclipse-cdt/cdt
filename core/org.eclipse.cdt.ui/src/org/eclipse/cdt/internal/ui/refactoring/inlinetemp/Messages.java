/*******************************************************************************
 * Copyright (c) 2013 Simon Taddiken
 * University of Bremen.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Simon Taddiken (University of Bremen)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.inlinetemp;

import org.eclipse.osgi.util.NLS;

final class Messages extends NLS {
	public static String InlineTemp_name;
	public static String InlineTemp_inlineConstant;
	public static String InputPage_label;
	public static String InputPage_inlineAll;
	public static String InputPage_removeDeclaration;
	public static String InputPage_addParenthesis;
	public static String InlineTemp_invalidSelection;
	public static String InlineTemp_noLocalVar;
	public static String InlineTemp_warningInlineAll;
	public static String InlineTemp_cantRemoveDeclarator;
	public static String InlineTemp_noInitializer;
	public static String InlineTemp_constantNotUsed;
	public static String InlineTemp_nonUniqueDeclaration;
	public static String InlineTemp_variableIsModified;
	public static String InlineTemp_noReadOnlyGuarantee;
	public static String InlineTemp_onlyVariablesFromSameFile;
	private Messages() {
		// Do not instantiate
	}

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
}