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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.eclipse.cdt.core.cdtvariables.ICdtVariablesContributor;
import org.eclipse.cdt.core.settings.model.CConfigurationStatus;
import org.eclipse.cdt.core.settings.model.ICBuildSetting;
import org.eclipse.cdt.core.settings.model.ICConfigExtensionReference;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICExternalSetting;
import org.eclipse.cdt.core.settings.model.ICFileDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICMultiConfigDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingContainer;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingObject;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.ICTargetPlatformSetting;
import org.eclipse.cdt.core.settings.model.MultiItemsHolder;
import org.eclipse.cdt.core.settings.model.WriteAccessException;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.QualifiedName;

/**
 * This class represents multi-configuration description holder
 */
public class MultiConfigDescription extends MultiItemsHolder implements ICMultiConfigDescription {

	ICConfigurationDescription[] fCfgs = null;

	public MultiConfigDescription(ICConfigurationDescription[] des) {
		fCfgs = des;
	}

	@Override
	public Object[] getItems() {
		return fCfgs;
	}

	@Override
	public ICConfigExtensionReference create(String extensionPoint, String extension) throws CoreException {
		if (DEBUG)
			System.out.println("Bad multi access: MultiConfigDescription.create()"); //$NON-NLS-1$
		throw new UnsupportedOperationException();
	}

	@Override
	public ICExternalSetting createExternalSetting(String[] languageIDs, String[] contentTypeIds, String[] extensions,
			ICSettingEntry[] entries) throws WriteAccessException {
		if (DEBUG)
			System.out.println("Bad multi access: MultiConfigDescription.createExtSett()"); //$NON-NLS-1$
		return null;
	}

	@Override
	public ICFileDescription createFileDescription(IPath path, ICResourceDescription base)
			throws CoreException, WriteAccessException {
		if (DEBUG)
			System.out.println("Bad multi access: MultiConfigDescription.createFileDesc()"); //$NON-NLS-1$
		return null;
	}

	@Override
	public ICFolderDescription createFolderDescription(IPath path, ICFolderDescription base)
			throws CoreException, WriteAccessException {
		if (DEBUG)
			System.out.println("Bad multi access: MultiConfigDescription.createFolderDesc()"); //$NON-NLS-1$
		return null;
	}

	@Override
	public ICConfigExtensionReference[] get(String extensionPointID) {
		if (DEBUG)
			System.out.println("Bad multi access: MultiConfigDescription.get()"); //$NON-NLS-1$
		return null;
	}

	@Override
	public ICBuildSetting getBuildSetting() {
		if (DEBUG)
			System.out.println("Bad multi access: MultiConfigDescription.getBuildSetting()"); //$NON-NLS-1$
		return null;
	}

	@Override
	public String[][] getErrorParserIDs() {
		String[][] out = new String[fCfgs.length][];
		for (int i = 0; i < fCfgs.length; i++)
			out[i] = fCfgs[i].getBuildSetting().getErrorParserIDs();
		return out;
	}

	@Override
	public void setErrorParserIDs(String[] ids) {
		for (int i = 0; i < fCfgs.length; i++)
			fCfgs[i].getBuildSetting().setErrorParserIDs(ids);
	}

	@Override
	public String getBuildSystemId() {
		return fCfgs[0].getBuildSystemId();
	}

	@Override
	public ICdtVariablesContributor getBuildVariablesContributor() {
		return fCfgs[0].getBuildVariablesContributor();
	}

	@Override
	public CConfigurationData getConfigurationData() {
		if (DEBUG)
			System.out.println("Bad multi access: MultiConfigDescription.getCfgData()"); //$NON-NLS-1$
		return null;
	}

	@Override
	public CConfigurationStatus getConfigurationStatus() {
		CConfigurationStatus st = null;
		for (int i = 1; i < fCfgs.length; i++) {
			st = fCfgs[0].getConfigurationStatus();
			if (!st.isOK())
				return st; // report error in any cfg
		}
		return st;
	}

	@Override
	public String getDescription() {
		return "Multi Configuration"; //$NON-NLS-1$
	}

	@Override
	public ICExternalSetting[] getExternalSettings() {
		return null;
	}

	@Override
	public String[] getExternalSettingsProviderIds() {
		if (DEBUG)
			System.out.println("Bad multi access: MultiConfigDescription.getExtSettProviderIds()"); //$NON-NLS-1$
		return null;
	}

	@Override
	public ICFileDescription[] getFileDescriptions() {
		ArrayList<ICFileDescription> lst = new ArrayList<>();
		for (int i = 0; i < fCfgs.length; i++)
			lst.addAll(Arrays.asList(fCfgs[i].getFileDescriptions()));
		return lst.toArray(new ICFileDescription[lst.size()]);
	}

	@Override
	public ICFolderDescription[] getFolderDescriptions() {
		ArrayList<ICFolderDescription> lst = new ArrayList<>();
		for (int i = 0; i < fCfgs.length; i++)
			lst.addAll(Arrays.asList(fCfgs[i].getFolderDescriptions()));
		return lst.toArray(new ICFolderDescription[lst.size()]);
	}

	@Override
	public ICLanguageSetting getLanguageSettingForFile(IPath path, boolean ignoreExludeStatus) {
		if (DEBUG)
			System.out.println("Bad multi access: MultiConfigDescription.getLangSettForFile()"); //$NON-NLS-1$
		return null;
	}

	@Override
	public ICProjectDescription getProjectDescription() {
		ICProjectDescription pd = fCfgs[0].getProjectDescription();
		if (pd == null)
			return null;
		for (int i = 1; i < fCfgs.length; i++)
			if (!pd.equals(fCfgs[i].getProjectDescription()))
				return null; // Different projects !
		return pd;
	}

	@Override
	public Map<String, String> getReferenceInfo() {
		if (DEBUG)
			System.out.println("Bad multi access: MultiConfigDescription.getReferenceInfo()"); //$NON-NLS-1$
		return Collections.emptyMap();
	}

	@Override
	public ICSourceEntry[] getResolvedSourceEntries() {
		return new ICSourceEntry[0];
	}

	@Override
	public ICResourceDescription getResourceDescription(IPath path, boolean isForFolder) {
		ArrayList<ICResourceDescription> lst = new ArrayList<>();
		for (int i = 0; i < fCfgs.length; i++) {
			ICResourceDescription rd = fCfgs[i].getResourceDescription(path, false);
			if (!path.equals(rd.getPath())) {
				try {
					if (isForFolder)
						rd = fCfgs[i].createFolderDescription(path, (ICFolderDescription) rd);
					else
						rd = fCfgs[i].createFileDescription(path, rd);
				} catch (CoreException e) {
				}
			}
			if (rd != null)
				lst.add(rd);
		}
		if (lst.size() == 0)
			return null;
		if (lst.size() == 1)
			return lst.get(0);
		if (isForFolder)
			return new MultiFolderDescription(lst.toArray(new ICFolderDescription[lst.size()]));
		else
			return new MultiFileDescription(lst.toArray(new ICFileDescription[lst.size()]));
	}

	@Override
	public ICResourceDescription[] getResourceDescriptions() {
		ArrayList<ICResourceDescription> lst = new ArrayList<>();
		for (int i = 0; i < fCfgs.length; i++)
			lst.addAll(Arrays.asList(fCfgs[i].getResourceDescriptions()));
		return lst.toArray(new ICResourceDescription[lst.size()]);
	}

	@Override
	public ICFolderDescription getRootFolderDescription() {
		ICFolderDescription[] rds = new ICFolderDescription[fCfgs.length];
		for (int i = 0; i < fCfgs.length; i++)
			rds[i] = fCfgs[i].getRootFolderDescription();
		return new MultiFolderDescription(rds);
	}

	@Override
	public Object getSessionProperty(QualifiedName name) {
		if (DEBUG)
			System.out.println("Bad multi access: MultiConfigDescription.getSessionProperty()"); //$NON-NLS-1$
		throw new UnsupportedOperationException();
	}

	@Override
	public ICSourceEntry[] getSourceEntries() {
		return new ICSourceEntry[0];
	}

	@Override
	public ICTargetPlatformSetting getTargetPlatformSetting() {
		if (DEBUG)
			System.out.println("Bad multi access: MultiConfigDescription.getTargetPlatfSetting()"); //$NON-NLS-1$
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isActive() {
		for (int i = 0; i < fCfgs.length; i++)
			if (fCfgs[i].isActive())
				return true;
		return false;
	}

	@Override
	public boolean isModified() {
		for (int i = 0; i < fCfgs.length; i++)
			if (fCfgs[i].isModified())
				return true;
		return false;
	}

	@Override
	public boolean isPreferenceConfiguration() {
		return false;
	}

	@Override
	public void remove(ICConfigExtensionReference ext) throws CoreException {
		for (int i = 0; i < fCfgs.length; i++)
			fCfgs[i].remove(ext);
	}

	@Override
	public void remove(String extensionPoint) throws CoreException {
		for (int i = 0; i < fCfgs.length; i++)
			fCfgs[i].remove(extensionPoint);
	}

	@Override
	public void removeExternalSetting(ICExternalSetting setting) throws WriteAccessException {
		for (int i = 0; i < fCfgs.length; i++)
			fCfgs[i].removeExternalSetting(setting);
	}

	@Override
	public void removeExternalSettings() throws WriteAccessException {
		for (int i = 0; i < fCfgs.length; i++)
			fCfgs[i].removeExternalSettings();
	}

	@Override
	public void removeResourceDescription(ICResourceDescription des) throws CoreException, WriteAccessException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setActive() throws WriteAccessException {
		for (int i = 0; i < fCfgs.length; i++)
			fCfgs[i].setActive();
	}

	@Override
	public void setConfigurationData(String buildSystemId, CConfigurationData data) throws WriteAccessException {
		if (DEBUG)
			System.out.println("Bad multi access: MultiConfigDescription.getConfigurationData()"); //$NON-NLS-1$
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDescription(String des) throws WriteAccessException {
		if (DEBUG)
			System.out.println("Bad multi access: MultiConfigDescription.setDescription()"); //$NON-NLS-1$
		throw new UnsupportedOperationException();
	}

	@Override
	public void setExternalSettingsProviderIds(String[] ids) {
		for (int i = 0; i < fCfgs.length; i++)
			fCfgs[i].setExternalSettingsProviderIds(ids);
	}

	@Override
	public void setName(String name) throws WriteAccessException {
		if (DEBUG)
			System.out.println("Bad multi access: MultiConfigDescription.setName()"); //$NON-NLS-1$
		throw new UnsupportedOperationException();
	}

	@Override
	public void setReferenceInfo(Map<String, String> refs) throws WriteAccessException {
		for (int i = 0; i < fCfgs.length; i++)
			fCfgs[i].setReferenceInfo(refs);
	}

	@Override
	public void setSessionProperty(QualifiedName name, Object value) {
		for (int i = 0; i < fCfgs.length; i++)
			fCfgs[i].setSessionProperty(name, value);
	}

	@Override
	public void setSourceEntries(ICSourceEntry[] entries) throws CoreException, WriteAccessException {
		for (int i = 0; i < fCfgs.length; i++)
			fCfgs[i].setSourceEntries(entries);
	}

	@Override
	public void updateExternalSettingsProviders(String[] ids) throws WriteAccessException {
		for (int i = 0; i < fCfgs.length; i++)
			fCfgs[i].updateExternalSettingsProviders(ids);
	}

	@Override
	public ICSettingObject[] getChildSettings() {
		return new ICSettingObject[0];
	}

	@Override
	public ICConfigurationDescription getConfiguration() {
		return this;
	}

	@Override
	public String getId() {
		return fCfgs[0].getId() + "_etc"; //$NON-NLS-1$
	}

	@Override
	public String getName() {
		return "Multiple Config Description"; //$NON-NLS-1$
	}

	@Override
	public ICSettingContainer getParent() {
		ICSettingContainer p = fCfgs[0].getParent();
		if (p == null)
			return null;
		for (int i = 1; i < fCfgs.length; i++)
			if (!p.equals(fCfgs[i].getParent()))
				return null;
		return p;
	}

	@Override
	public int getType() {
		int t = fCfgs[0].getType();
		for (int i = 1; i < fCfgs.length; i++)
			if (t != fCfgs[i].getType())
				return 0;
		return t;
	}

	@Override
	public boolean isReadOnly() {
		for (int i = 0; i < fCfgs.length; i++)
			if (!fCfgs[i].isReadOnly())
				return false;
		return true;
	}

	@Override
	public void setReadOnly(boolean readOnly, boolean keepModify) {
		for (ICConfigurationDescription cfg : fCfgs)
			cfg.setReadOnly(readOnly, keepModify);
	}

	@Override
	public boolean isValid() {
		for (int i = 0; i < fCfgs.length; i++)
			if (!fCfgs[i].isValid())
				return false;
		return true;
	}

	@Override
	public ICStorageElement getStorage(String id, boolean create) throws CoreException {
		if (DEBUG)
			System.out.println("Bad multi access: MultiConfigDescription.getStorage()"); //$NON-NLS-1$
		return null;
	}

	@Override
	public ICStorageElement importStorage(String id, ICStorageElement el)
			throws UnsupportedOperationException, CoreException {
		if (DEBUG)
			System.out.println("Bad multi access: MultiConfigDescription.importStorage()"); //$NON-NLS-1$
		return null;
	}

	@Override
	public void removeStorage(String id) throws CoreException {
		for (int i = 0; i < fCfgs.length; i++)
			fCfgs[i].removeStorage(id);
	}

}
