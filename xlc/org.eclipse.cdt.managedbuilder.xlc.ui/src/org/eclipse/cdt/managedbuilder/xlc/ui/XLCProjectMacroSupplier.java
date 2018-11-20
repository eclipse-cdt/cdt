/*******************************************************************************
 * Copyright (c) 2007, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.xlc.ui;

import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.internal.macros.BuildMacro;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacro;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.macros.IProjectBuildMacroSupplier;
import org.eclipse.cdt.managedbuilder.xlc.ui.preferences.PreferenceConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * @author crecoskie
 *
 */
public class XLCProjectMacroSupplier implements IProjectBuildMacroSupplier {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IProjectBuildMacroSupplier#getMacro(java.lang.String, org.eclipse.cdt.managedbuilder.core.IManagedProject, org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider)
	 */
	@Override
	public IBuildMacro getMacro(String macroName, IManagedProject project, IBuildMacroProvider provider) {

		if (macroName.equals(PreferenceConstants.P_XL_COMPILER_ROOT)) {
			String compilerPath = null;

			// figure out compiler path from properties and preferences

			// search for property first
			IProject theProject = (IProject) project.getOwner();
			try {
				compilerPath = theProject
						.getPersistentProperty(new QualifiedName("", PreferenceConstants.P_XL_COMPILER_ROOT));
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (compilerPath == null) {
				// use the workbench preference
				IPreferenceStore prefStore = XLCUIPlugin.getDefault().getPreferenceStore();
				compilerPath = prefStore.getString(PreferenceConstants.P_XL_COMPILER_ROOT);
			}

			BuildMacro macro = new BuildMacro(macroName, ICdtVariable.VALUE_PATH_DIR, compilerPath);

			return macro;
		}

		else
			return provider.getMacro(macroName, IBuildMacroProvider.CONTEXT_PROJECT, project, true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IProjectBuildMacroSupplier#getMacros(org.eclipse.cdt.managedbuilder.core.IManagedProject, org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider)
	 */
	@Override
	public IBuildMacro[] getMacros(IManagedProject project, IBuildMacroProvider provider) {

		String macroName = PreferenceConstants.P_XL_COMPILER_ROOT;

		String compilerPath = null;

		// figure out compiler path from properties and preferences

		// search for property first
		IProject theProject = (IProject) project.getOwner();
		try {
			compilerPath = theProject
					.getPersistentProperty(new QualifiedName("", PreferenceConstants.P_XL_COMPILER_ROOT));
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (compilerPath == null) {
			// use the workbench preference
			IPreferenceStore prefStore = XLCUIPlugin.getDefault().getPreferenceStore();
			compilerPath = prefStore.getString(PreferenceConstants.P_XL_COMPILER_ROOT);
		}

		BuildMacro macro = new BuildMacro(macroName, ICdtVariable.VALUE_PATH_DIR, compilerPath);

		// our array consists of our macro, plus all the macros from our parent
		IBuildMacro[] parentMacros = provider.getMacros(IBuildMacroProvider.CONTEXT_PROJECT, project, true);

		// look for an existing macro definition
		int foundIndex = -1;
		for (int k = 0; k < parentMacros.length; k++) {

			if (parentMacros[k].getName().equals(macro.getName())) {
				foundIndex = k;
				break;
			}
		}

		int numMacros = (foundIndex == -1) ? parentMacros.length + 1 : parentMacros.length;

		IBuildMacro[] macros = new IBuildMacro[numMacros];

		// if there was no existing value then add it to the front
		if (foundIndex == -1) {
			macros[0] = macro;
			for (int k = 1; k < macros.length; k++) {
				macros[k] = parentMacros[k - 1];
			}
		}

		else { // replace the old value
			for (int k = 0; k < macros.length; k++) {
				macros[k] = parentMacros[k];
			}
			macros[foundIndex] = macro;
		}

		return macros;

	}

}
