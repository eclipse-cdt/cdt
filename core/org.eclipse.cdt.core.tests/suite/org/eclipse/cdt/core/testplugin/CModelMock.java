/*******************************************************************************
 * Copyright (c) 2010 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.testplugin;

import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.cdtvariables.ICdtVariablesContributor;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.settings.model.CConfigurationStatus;
import org.eclipse.cdt.core.settings.model.ICBuildSetting;
import org.eclipse.cdt.core.settings.model.ICConfigExtensionReference;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICExternalSetting;
import org.eclipse.cdt.core.settings.model.ICFileDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingContainer;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingObject;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.ICTargetPlatformSetting;
import org.eclipse.cdt.core.settings.model.WriteAccessException;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.QualifiedName;

public class CModelMock {

	/**
	 * Dummy implementation of ICProjectDescription for testing.
	 * Feel free to override the methods you are interested to mock.
	 *
	 */
	public static class DummyCProjectDescription implements ICProjectDescription {

		public ICSettingObject[] getChildSettings() {
			return null;
		}

		public String getId() {
			return null;
		}

		public String getName() {
			return null;
		}

		public int getType() {
			return 0;
		}

		public boolean isValid() {
			return false;
		}

		public ICConfigurationDescription getConfiguration() {
			return null;
		}

		public ICSettingContainer getParent() {
			return null;
		}

		public boolean isReadOnly() {
			return false;
		}

		public ICStorageElement getStorage(String id, boolean create)
				throws CoreException {
			return null;
		}

		public void removeStorage(String id) throws CoreException {
		}

		public ICStorageElement importStorage(String id, ICStorageElement el)
				throws UnsupportedOperationException, CoreException {
			return null;
		}

		public void setReadOnly(boolean readOnly, boolean keepModify) {
		}

		public int getConfigurationRelations() {
			return 0;
		}

		public void setConfigurationRelations(int status) {
		}

		public void useDefaultConfigurationRelations() {
		}

		public boolean isDefaultConfigurationRelations() {
			return false;
		}

		public ICConfigurationDescription[] getConfigurations() {
			return null;
		}

		public ICConfigurationDescription getActiveConfiguration() {
			return null;
		}

		public void setActiveConfiguration(ICConfigurationDescription cfg)
				throws WriteAccessException {
		}

		public ICConfigurationDescription createConfiguration(String id,
				String name, ICConfigurationDescription base)
				throws CoreException, WriteAccessException {
			return null;
		}

		public ICConfigurationDescription createConfiguration(
				String buildSystemId, CConfigurationData data)
				throws CoreException, WriteAccessException {
			return null;
		}

		public ICConfigurationDescription getConfigurationByName(String name) {
			return null;
		}

		public ICConfigurationDescription getConfigurationById(String id) {
			return null;
		}

		public void removeConfiguration(String name)
				throws WriteAccessException {
		}

		public void removeConfiguration(ICConfigurationDescription cfg)
				throws WriteAccessException {
		}

		public IProject getProject() {
			return null;
		}

		public boolean isModified() {
			return false;
		}

		public Object getSessionProperty(QualifiedName name) {
			return null;
		}

		public void setSessionProperty(QualifiedName name, Object value) {

		}

		public ICConfigurationDescription getDefaultSettingConfiguration() {
			return null;
		}

		public void setDefaultSettingConfiguration(
				ICConfigurationDescription cfg) {
		}

		public boolean isCdtProjectCreating() {
			return false;
		}

		public void setCdtProjectCreated() {
		}

	}

	/**
	 * Dummy implementation of ICConfigurationDescription for testing.
	 * Feel free to override the methods you are interested to mock.
	 *
	 */
	public static class DummyCConfigurationDescription implements ICConfigurationDescription {
		private String id;

		public DummyCConfigurationDescription(String id) {
			this.id = id;
		}

		public ICSettingObject[] getChildSettings() {
			return null;
		}

		public String getId() {
			return id;
		}

		public String getName() {
			return null;
		}

		public int getType() {
			return 0;
		}

		public boolean isValid() {
			return false;
		}

		public ICConfigurationDescription getConfiguration() {
			return null;
		}

		public ICSettingContainer getParent() {
			return null;
		}

		public boolean isReadOnly() {
			return false;
		}

		public ICStorageElement getStorage(String id, boolean create)
				throws CoreException {
			return null;
		}

		public void removeStorage(String id) throws CoreException {
		}

		public ICStorageElement importStorage(String id, ICStorageElement el)
				throws UnsupportedOperationException, CoreException {
			return null;
		}

		public void setReadOnly(boolean readOnly, boolean keepModify) {
		}

		public boolean isActive() {
			return false;
		}

		public String getDescription() {
			return null;
		}

		public void setDescription(String des) throws WriteAccessException {
		}

		public ICProjectDescription getProjectDescription() {
			return null;
		}

		public ICFolderDescription getRootFolderDescription() {
			return null;
		}

		public ICFolderDescription[] getFolderDescriptions() {
			return null;
		}

		public ICFileDescription[] getFileDescriptions() {
			return null;
		}

		public ICResourceDescription[] getResourceDescriptions() {
			return null;
		}

		public ICResourceDescription getResourceDescription(IPath path,
				boolean exactPath) {
			return null;
		}

		public void removeResourceDescription(ICResourceDescription des)
				throws CoreException, WriteAccessException {
		}

		public ICFileDescription createFileDescription(IPath path,
				ICResourceDescription base) throws CoreException,
				WriteAccessException {
			return null;
		}

		public ICFolderDescription createFolderDescription(IPath path,
				ICFolderDescription base) throws CoreException,
				WriteAccessException {
			return null;
		}

		public String getBuildSystemId() {
			return null;
		}

		public CConfigurationData getConfigurationData() {
			return null;
		}

		public void setActive() throws WriteAccessException {
		}

		public void setConfigurationData(String buildSystemId, CConfigurationData data) throws WriteAccessException {
		}

		public boolean isModified() {
			return false;
		}

		public ICTargetPlatformSetting getTargetPlatformSetting() {
			return null;
		}

		public ICSourceEntry[] getSourceEntries() {
			return null;
		}

		public ICSourceEntry[] getResolvedSourceEntries() {
			return null;
		}

		public void setSourceEntries(ICSourceEntry[] entries) throws CoreException, WriteAccessException {
		}

		public Map<String, String> getReferenceInfo() {
			return null;
		}

		public void setReferenceInfo(Map<String, String> refs) throws WriteAccessException {
		}

		public ICExternalSetting[] getExternalSettings() {
			return null;
		}

		public ICExternalSetting createExternalSetting(String[] languageIDs,
				String[] contentTypeIds, String[] extensions,
				ICSettingEntry[] entries) throws WriteAccessException {
			return null;
		}

		public void removeExternalSetting(ICExternalSetting setting) throws WriteAccessException {
		}

		public void removeExternalSettings() throws WriteAccessException {
		}

		public ICBuildSetting getBuildSetting() {
			return null;
		}

		public ICdtVariablesContributor getBuildVariablesContributor() {
			return null;
		}

		public Object getSessionProperty(QualifiedName name) {
			return null;
		}

		public void setSessionProperty(QualifiedName name, Object value) {
		}

		public void setName(String name) throws WriteAccessException {
		}

		public ICConfigExtensionReference[] get(String extensionPointID) {
			return null;
		}

		public ICConfigExtensionReference create(String extensionPoint, String extension) throws CoreException {
			return null;
		}

		public void remove(ICConfigExtensionReference ext) throws CoreException {
		}

		public void remove(String extensionPoint) throws CoreException {
		}

		public boolean isPreferenceConfiguration() {
			return false;
		}

		public ICLanguageSetting getLanguageSettingForFile(IPath path, boolean ignoreExludeStatus) {
			return null;
		}

		public void setExternalSettingsProviderIds(String[] ids) {
		}

		public String[] getExternalSettingsProviderIds() {
			return null;
		}

		public void updateExternalSettingsProviders(String[] ids) throws WriteAccessException {
		}

		public CConfigurationStatus getConfigurationStatus() {
			return null;
		}

		public void setLanguageSettingProviders(List<ILanguageSettingsProvider> providers) {
		}

		public List<ILanguageSettingsProvider> getLanguageSettingProviders() {
			return null;
		}

	}
}
