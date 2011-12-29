/*******************************************************************************
 * Copyright (c) 2008, 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.gettersandsetters;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {
	public static String GenerateGettersAndSettersInputPage_DeselectAll;
	public static String GenerateGettersAndSettersInputPage_Header;
	public static String GenerateGettersAndSettersInputPage_LinkDescription;
	public static String GenerateGettersAndSettersInputPage_LinkTooltip;
	public static String GenerateGettersAndSettersInputPage_Name;
	public static String GenerateGettersAndSettersInputPage_SeparateDefinition;
	public static String GenerateGettersAndSettersInputPage_SelectAll;
	public static String GenerateGettersAndSettersInputPage_SelectGetters;
	public static String GenerateGettersAndSettersInputPage_SelectSetters;
	public static String GenerateGettersAndSettersRefactoring_NoClassDefFound;
	public static String GenerateGettersAndSettersRefactoring_NoFields;
	public static String GenerateGettersAndSettersRefactoring_NoImplFile;

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	// Do not instantiate
	private Messages() {
	}
}
