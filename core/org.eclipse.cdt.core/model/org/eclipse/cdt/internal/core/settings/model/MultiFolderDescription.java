/*******************************************************************************
 * Copyright (c) 2007, 2011 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICMultiFolderDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 *
 *
 */
public class MultiFolderDescription extends MultiResourceDescription implements ICMultiFolderDescription {

	public MultiFolderDescription(ICFolderDescription[] res) {
		super(res);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICFolderDescription#createLanguageSettingForContentTypes(java.lang.String, java.lang.String[])
	 */
	@Override
	public ICLanguageSetting createLanguageSettingForContentTypes(String languageId, String[] typeIds)
			throws CoreException {
		if (DEBUG)
			System.out.println("Bad multi access: MultiFolderDescription.createLanguageSettingForContentType()"); //$NON-NLS-1$
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICFolderDescription#createLanguageSettingForExtensions(java.lang.String, java.lang.String[])
	 */
	@Override
	public ICLanguageSetting createLanguageSettingForExtensions(String languageId, String[] extensions)
			throws CoreException {
		if (DEBUG)
			System.out.println("Bad multi access: MultiFolderDescription.createLanguageSettingForExt()"); //$NON-NLS-1$
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICFolderDescription#getLanguageSettingForFile(java.lang.String)
	 */
	@Override
	public ICLanguageSetting getLanguageSettingForFile(String fileName) {
		ICLanguageSetting ls0 = ((ICFolderDescription) fRess[0]).getLanguageSettingForFile(fileName);
		if (ls0 == null || ls0.getName() == null)
			return null;
		for (int i = 1; i < fRess.length; i++) {
			if (fRess[i] instanceof ICFolderDescription) {
				ICLanguageSetting ls1 = ((ICFolderDescription) fRess[i]).getLanguageSettingForFile(fileName);
				if (ls1 == null || !ls0.getName().equals(ls1.getName()))
					return null;
			}
		}
		return ls0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICFolderDescription#getLanguageSettings()
	 */
	@Override
	public ICLanguageSetting[][] getLanguageSettingsM(Comparator<Object> comp) {
		ICLanguageSetting[][] ls = new ICLanguageSetting[fRess.length][];
		for (int i = 0; i < fRess.length; i++) {
			if (fRess[i] instanceof ICFolderDescription) {
				ls[i] = ((ICFolderDescription) fRess[i]).getLanguageSettings();
				Arrays.sort(ls[i], comp);
			}
		}
		return ls;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICFolderDescription#getNestedResourceDescription(org.eclipse.core.runtime.IPath, boolean)
	 */
	@Override
	public ICResourceDescription getNestedResourceDescription(IPath relPath, boolean exactPath) {
		if (DEBUG)
			System.out.println("Bad multi access: MultiFolderDescription.getNestedResourceDescription(path, exact)"); //$NON-NLS-1$
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICFolderDescription#getNestedResourceDescriptions(int)
	 */
	@Override
	public ICResourceDescription[] getNestedResourceDescriptions(int kind) {
		if (DEBUG)
			System.out.println("Bad multi access: MultiFolderDescription.getNestedResourceDescriptions(kind)"); //$NON-NLS-1$
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICFolderDescription#getNestedResourceDescriptions()
	 */
	@Override
	public ICResourceDescription[] getNestedResourceDescriptions() {
		if (DEBUG)
			System.out.println("Bad multi access: MultiFolderDescription.getNestedResourceDescriptions()"); //$NON-NLS-1$
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICFolderDescription#isRoot()
	 */
	@Override
	public boolean isRoot() {
		for (int i = 0; i < fRess.length; i++)
			if (!((ICFolderDescription) fRess[i]).isRoot())
				return false;
		return true;
	}

	@Override
	public ICLanguageSetting[] getLanguageSettings() {
		return ((ICFolderDescription) fRess[0]).getLanguageSettings();
	}

}
