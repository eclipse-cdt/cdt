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

import java.util.List;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFileDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICMultiResourceDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingContainer;
import org.eclipse.cdt.core.settings.model.ICSettingObject;
import org.eclipse.cdt.core.settings.model.MultiItemsHolder;
import org.eclipse.cdt.core.settings.model.WriteAccessException;
import org.eclipse.core.runtime.IPath;

/**
 * This class represents multi-resource holder
 */
public abstract class MultiResourceDescription extends MultiItemsHolder implements ICMultiResourceDescription {
	ICResourceDescription[] fRess = null;
	ICConfigurationDescription fCfg = null;

	public MultiResourceDescription(ICResourceDescription[] res) {
		fRess = res;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICResourceDescription#canExclude(boolean)
	 *
	 * returns TRUE only if all res.configurations return true
	 */
	@Override
	public boolean canExclude(boolean exclude) {
		for (int i = 0; i < fRess.length; i++)
			if (!fRess[i].canExclude(exclude))
				return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICResourceDescription#getParentFolderDescription()
	 */
	@Override
	public ICFolderDescription getParentFolderDescription() {
		if (DEBUG)
			System.out.println("Bad multi access: MultiResourceDescription.getParentFolderDescription()"); //$NON-NLS-1$
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICResourceDescription#getPath()
	 */
	@Override
	public IPath getPath() {
		IPath p = fRess[0].getPath();
		if (p != null) {
			for (int i = 1; i < fRess.length; i++) {
				if (!p.equals(fRess[i].getPath()))
					throw new UnsupportedOperationException();
			}
			return p;
		}
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICResourceDescription#isExcluded()
	 */
	@Override
	public boolean isExcluded() {
		for (int i = 0; i < fRess.length; i++)
			if (!fRess[i].isExcluded())
				return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICResourceDescription#setExcluded(boolean)
	 */
	@Override
	public void setExcluded(boolean excluded) throws WriteAccessException {
		for (int i = 0; i < fRess.length; i++)
			fRess[i].setExcluded(excluded);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICResourceDescription#setPath(org.eclipse.core.runtime.IPath)
	 */
	@Override
	public void setPath(IPath path) throws WriteAccessException {
		for (int i = 0; i < fRess.length; i++)
			fRess[i].setPath(path);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICSettingContainer#getChildSettings()
	 */
	@Override
	public ICSettingObject[] getChildSettings() {
		if (DEBUG)
			System.out.println("Bad multi access: MultiResourceDescription.getChildSettings()"); //$NON-NLS-1$
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICSettingObject#getConfiguration()
	 *
	 * returns multi-configuration object
	 */
	@Override
	public ICConfigurationDescription getConfiguration() {
		if (fCfg == null) {
			ICConfigurationDescription[] cfgs = new ICConfigurationDescription[fRess.length];
			for (int i = 0; i < fRess.length; i++)
				cfgs[i] = fRess[i].getConfiguration();
			fCfg = new MultiConfigDescription(cfgs);
		}
		return fCfg;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICSettingObject#getId()
	 */
	@Override
	public String getId() {
		return fRess[0].getId() + "_etc"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICSettingObject#getName()
	 */
	@Override
	public String getName() {
		return "Multiple Resource Description"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICSettingObject#getParent()
	 *
	 * If there's the same parent for all res cfgs, return it.
	 * Else return null;
	 */
	@Override
	public ICSettingContainer getParent() {
		ICSettingContainer sc = fRess[0].getParent();
		if (sc == null)
			return null;
		for (int i = 1; i < fRess.length; i++)
			if (!sc.equals(fRess[i].getParent()))
				return null;
		return sc;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICSettingObject#getType()
	 *
	 * If there's the same type for all res cfgs, return it.
	 * Else return null;
	 */
	@Override
	public int getType() {
		int t = fRess[0].getType();
		for (int i = 1; i < fRess.length; i++)
			if (t != fRess[i].getType())
				return 0;
		return t;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICSettingObject#isReadOnly()
	 */
	@Override
	public boolean isReadOnly() {
		for (int i = 0; i < fRess.length; i++)
			if (!fRess[i].isReadOnly())
				return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICSettingObject#isValid()
	 */
	@Override
	public boolean isValid() {
		for (int i = 0; i < fRess.length; i++)
			if (!fRess[i].isValid())
				return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.settings.model.ICMultiItemsHolder#getItems()
	 */
	@Override
	public Object[] getItems() {
		return fRess;
	}

	@Override
	public void setSettingEntries(ICLanguageSetting lang, int kind, List<ICLanguageSettingEntry> incs, boolean toAll) {
		for (int i = 0; i < fRess.length; i++) {
			if (fRess[i] instanceof ICFolderDescription) {
				String n = lang.getName();
				ICLanguageSetting[] l = ((ICFolderDescription) fRess[i]).getLanguageSettings();
				for (int j = 0; j < l.length; j++) {
					if (toAll || n.equals(l[j].getName())) {
						l[j].setSettingEntries(kind, incs);
						break;
					}
				}
			} else if (fRess[i] instanceof ICFileDescription) {
				ICLanguageSetting l = ((ICFileDescription) fRess[i]).getLanguageSetting();
				if (l.getName().equals(lang.getName()))
					l.setSettingEntries(kind, incs);
			}
		}
	}
}
