/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.cdt.core.model.util.CDTListComparator;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 *
 *
 */
public class MultiFolderDescription extends MultiResourceDescription implements
		ICFolderDescription {

	private static final Comparator<Object> comp = CDTListComparator.getInstance();
	private ICLanguageSetting[] lsets = null;
	
	public MultiFolderDescription(ICFolderDescription[] res, int mode) {
		super(res, mode);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICFolderDescription#createLanguageSettingForContentTypes(java.lang.String, java.lang.String[])
	 */
	public ICLanguageSetting createLanguageSettingForContentTypes(
			String languageId, String[] typeIds) throws CoreException {
		System.out.println("Bad multi access: MultiFolderDescription.createLanguageSettingForContentType()");
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICFolderDescription#createLanguageSettingForExtensions(java.lang.String, java.lang.String[])
	 */
	public ICLanguageSetting createLanguageSettingForExtensions(
			String languageId, String[] extensions) throws CoreException {
		System.out.println("Bad multi access: MultiFolderDescription.createLanguageSettingForExt()");
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICFolderDescription#getLanguageSettingForFile(java.lang.String)
	 */
	public ICLanguageSetting getLanguageSettingForFile(String fileName) {
		ICLanguageSetting ls0 = ((ICFolderDescription)fRess[0]).getLanguageSettingForFile(fileName);
		if (ls0 == null || ls0.getName() == null)
			return null;
		for (int i=1; i<fRess.length; i++) {
			if (fRess[i] instanceof ICFolderDescription) {
				ICLanguageSetting ls1 = ((ICFolderDescription)fRess[i]).getLanguageSettingForFile(fileName);
				if (ls1 == null || ! ls0.getName().equals(ls1.getName()))
					return null; 
			}
		}
		return ls0;
	}

	private ICLanguageSetting[] conv2LS(Object[] ob) {
		ICLanguageSetting[] se = new ICLanguageSetting[ob.length];
		System.arraycopy(ob, 0, se, 0, ob.length);
		return se;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICFolderDescription#getLanguageSettings()
	 */
	public ICLanguageSetting[] getLanguageSettings() {
		if (lsets != null)
			return lsets;
		
		ICLanguageSetting[][] ls = new ICLanguageSetting[fRess.length][];
		for (int i=0; i<fRess.length; i++) {
			if (fRess[i] instanceof ICFolderDescription) {
				ls[i] = ((ICFolderDescription)fRess[i]).getLanguageSettings();
				Arrays.sort(ls[i], comp);
			}
		}
		ICLanguageSetting[] fs = conv2LS(getListForDisplay(ls, comp));
		lsets = new ICLanguageSetting[fs.length];
		for (int i=0; i<fs.length; i++) {
			ArrayList<ICLanguageSetting> list = new ArrayList<ICLanguageSetting>(fRess.length);
			for (int j=0; j<ls.length; j++) {
				int x = Arrays.binarySearch(ls[j], fs[i], comp);
				if (x >= 0)
					list.add(ls[j][x]);
			}
			if (list.size() == 1)
				lsets[i] = (ICLanguageSetting)list.get(0);
			else if (list.size() > 1)
				lsets[i] = new MultiLanguageSetting(list, getConfiguration());
		}
		return lsets;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICFolderDescription#getNestedResourceDescription(org.eclipse.core.runtime.IPath, boolean)
	 */
	public ICResourceDescription getNestedResourceDescription(IPath relPath,
			boolean exactPath) {
		System.out.println("Bad multi access: MultiFolderDescription.getNestedResourceDescription(path, exact)");
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICFolderDescription#getNestedResourceDescriptions(int)
	 */
	public ICResourceDescription[] getNestedResourceDescriptions(int kind) {
		System.out.println("Bad multi access: MultiFolderDescription.getNestedResourceDescriptions(kind)");
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICFolderDescription#getNestedResourceDescriptions()
	 */
	public ICResourceDescription[] getNestedResourceDescriptions() {
		System.out.println("Bad multi access: MultiFolderDescription.getNestedResourceDescriptions()");
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICFolderDescription#isRoot()
	 */
	public boolean isRoot() {
		for (int i=0; i<fRess.length; i++)
			if (! ((ICFolderDescription)fRess[0]).isRoot())
				return false;
		return true;
	}

}
