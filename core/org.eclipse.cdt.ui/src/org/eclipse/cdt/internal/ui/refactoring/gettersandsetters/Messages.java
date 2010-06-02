/*******************************************************************************
 * Copyright (c) 2008, 2009 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.gettersandsetters;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.ui.refactoring.gettersandsetters.messages";//$NON-NLS-1$

	private Messages() {
		// Do not instantiate
	}

	public static String GenerateGettersAndSettersInputPage_DeselectAll;
	public static String GenerateGettersAndSettersInputPage_header;
	public static String GenerateGettersAndSettersInputPage_PlaceImplHeader;
	public static String GenerateGettersAndSettersInputPage_SelectAll;
	public static String GenerateGettersAndSettersInputPage_SelectGetters;
	public static String GenerateGettersAndSettersInputPage_SelectSetters;
	public static String GenerateGettersAndSettersRefactoring_NoCassDefFound;
	public static String GenerateGettersAndSettersRefactoring_NoFields;
	public static String GenerateGettersAndSettersRefactoring_NoImplFile;
	public static String GettersAndSetters_Name;

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}
