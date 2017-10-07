/******************************************************************************* 
 * Copyright (c) 2017 Pavel Marek 
 * All rights reserved. This program and the accompanying materials  
 * are made available under the terms of the Eclipse Public License v1.0  
 * which accompanies this distribution, and is available at  
 * http://www.eclipse.org/legal/epl-v10.html   
 *  
 * Contributors:  
 *      Pavel Marek - initial API and implementation 
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.overridemethods;

import org.eclipse.osgi.util.NLS;

final class Messages extends NLS {
	public static String OverrideMethodsInputPage_Name;
	public static String OverrideMethodsInputPage_Header;
	public static String OverrideMethodsInputPage_SelectAll;
	public static String OverrideMethodsInputPage_DeselectAll;
	public static String OverrideMethodsRefactoring_SelNotInClass;
	public static String OverrideMethodsRefactoring_NoMethods;

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	// Do not instantiate
	private Messages() {
	}
}
