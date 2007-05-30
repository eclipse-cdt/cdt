/********************************************************************************
 * Copyright (c) 2007 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * David Dykstal (IBM) - Initial API and implementation
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 ********************************************************************************/

package org.eclipse.rse.ui.validators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.ISystemProfileManager;

/**
 * This class constructs validators for various bits of the user interface.
 */
public class ValidatorFactory {

	/**
	 * Reusable method to return a name validator for renaming a profile.
	 * @param profileName the current profile name on updates. Can be null for new profiles. Used
	 * to remove from the existing name list the current connection.
	 * @return the validator
	 */
	public static ISystemValidator getProfileNameValidator(String profileName) {
		ISystemProfileManager manager = RSECorePlugin.getTheSystemRegistry().getSystemProfileManager();
    	String[] nameArray = manager.getSystemProfileNames();
    	Collection names = Arrays.asList(nameArray);
		if (profileName != null) names.remove(profileName);
		ISystemValidator nameValidator = new ValidatorProfileName(names);
		return nameValidator;
	}

}
