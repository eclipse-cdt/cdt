/*******************************************************************************
 * Copyright (c) 2002, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.rse.internal.useractions.files.compile;

import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.internal.useractions.ui.SystemCmdSubstVarList;
import org.eclipse.rse.internal.useractions.ui.compile.ISystemCompileCommandSubstitutor;
import org.eclipse.rse.internal.useractions.ui.compile.SystemCompileManager;
import org.eclipse.rse.internal.useractions.ui.compile.SystemCompileProfile;
import org.eclipse.rse.internal.useractions.ui.compile.SystemDefaultCompileCommands;

/**
 * Specializatio of compile support for universal file subsystems.
 */
public class UniversalCompileManager extends SystemCompileManager {
	/**
	 * Constructor for UniversalCompileManager.
	 */
	public UniversalCompileManager() {
		super();
	}

	/**
	 * Overridable method to instantiate the SystemCompileProfile for the 
	 * given system profile.
	 * <p>
	 * We return an instance of UniversalCompileProfile
	 */
	protected SystemCompileProfile createCompileProfile(ISystemProfile profile) {
		return new UniversalCompileProfile(this, profile.getName());
	}

	/**
	 * Return the default list of compile commands to prime new documents with.
	 */
	public SystemDefaultCompileCommands getDefaultCompileCommands() {
		return UniversalIBMCompileCommands.getIBMCompileCommands();
	}

	/**
	 * For support of the Work With Compile Commands dialog.
	 * <p>
	 * Return the substitution variables supported by compile commands managed by this manager.
	 */
	public SystemCmdSubstVarList getSubstitutionVariableList() {
		return UniversalCompileSubstList.getInstance();
	}

	/**
	 * @see org.eclipse.rse.internal.useractions.ui.compile.SystemCompileManager#createSubstitutor(IHost)
	 */
	protected ISystemCompileCommandSubstitutor createSubstitutor(IHost connection) {
		return new UniversalCompileSubstitutor(connection);
	}
}
