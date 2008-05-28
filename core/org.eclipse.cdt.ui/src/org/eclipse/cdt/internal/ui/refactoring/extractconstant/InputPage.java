/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractconstant;

import java.util.ArrayList;

import org.eclipse.osgi.util.NLS;

import org.eclipse.cdt.internal.ui.refactoring.NameNVisibilityInformation;
import org.eclipse.cdt.internal.ui.refactoring.dialogs.ExtractInputPage;

public class InputPage extends ExtractInputPage {

	private final ArrayList<String> usedNames;

	public InputPage(String name, NameNVisibilityInformation info) {
		super(name, info);
		label = Messages.InputPage_ConstName; 
		errorLabel = Messages.InputPage_EnterContName; 
		usedNames = info.getUsedNames();
	}

	@Override
	protected void verifyName(String name) {
		if(usedNames.contains(name)) {
			setErrorMessage(NLS.bind(Messages.InputPage_NameAlreadyDefined, name)); 
			setPageComplete(false);
		}
	}
}
