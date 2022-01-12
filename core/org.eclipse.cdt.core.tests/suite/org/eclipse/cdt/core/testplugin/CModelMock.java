/*******************************************************************************
 * Copyright (c) 2010, 2012 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.core.testplugin;

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

/**
 * Collection of mock classes for testing
 */
public class CModelMock {
	/**
	 * Dummy implementation of ICProjectDescription for testing.
	 * Feel free to override the methods you are interested to mock.
	 */
	public static class DummyCProjectDescription implements ICProjectDescription {

		@Override
		public ICSettingObject[] getChildSettings() {
			return null;
		}

		@Override
		public String getId() {
			return null;
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public int getType() {
			return 0;
		}

		@Override
		public boolean isValid() {
			return false;
		}

		@Override
		public ICConfigurationDescription getConfiguration() {
			return null;
		}

		@Override
		public ICSettingContainer getParent() {
			return null;
		}

		@Override
		public boolean isReadOnly() {
			return false;
		}

		@Override
		public ICStorageElement getStorage(String id, boolean create) throws CoreException {
			return null;
		}

		@Override
		public void removeStorage(String id) throws CoreException {
		}

		@Override
		public ICStorageElement importStorage(String id, ICStorageElement el)
				throws UnsupportedOperationException, CoreException {
			return null;
		}

		@Override
		public void setReadOnly(boolean readOnly, boolean keepModify) {
		}

		@Override
		public int getConfigurationRelations() {
			return 0;
		}

		@Override
		public void setConfigurationRelations(int status) {
		}

		@Override
		public void useDefaultConfigurationRelations() {
		}

		@Override
		public boolean isDefaultConfigurationRelations() {
			return false;
		}

		@Override
		public ICConfigurationDescription[] getConfigurations() {
			return null;
		}

		@Override
		public ICConfigurationDescription getActiveConfiguration() {
			return null;
		}

		@Override
		public void setActiveConfiguration(ICConfigurationDescription cfg) throws WriteAccessException {
		}

		@Override
		public ICConfigurationDescription createConfiguration(String id, String name, ICConfigurationDescription base)
				throws CoreException, WriteAccessException {
			return null;
		}

		@Override
		public ICConfigurationDescription createConfiguration(String buildSystemId, CConfigurationData data)
				throws CoreException, WriteAccessException {
			return null;
		}

		@Override
		public ICConfigurationDescription getConfigurationByName(String name) {
			return null;
		}

		@Override
		public ICConfigurationDescription getConfigurationById(String id) {
			return null;
		}

		@Override
		public void removeConfiguration(String name) throws WriteAccessException {
		}

		@Override
		public void removeConfiguration(ICConfigurationDescription cfg) throws WriteAccessException {
		}

		@Override
		public IProject getProject() {
			return null;
		}

		@Override
		public boolean isModified() {
			return false;
		}

		@Override
		public Object getSessionProperty(QualifiedName name) {
			return null;
		}

		@Override
		public void setSessionProperty(QualifiedName name, Object value) {

		}

		@Override
		public ICConfigurationDescription getDefaultSettingConfiguration() {
			return null;
		}

		@Override
		public void setDefaultSettingConfiguration(ICConfigurationDescription cfg) {
		}

		@Override
		public boolean isCdtProjectCreating() {
			return false;
		}

		@Override
		public void setCdtProjectCreated() {
		}

	}

	/**
	 * Dummy implementation of ICConfigurationDescription for testing.
	 * Feel free to override the methods you are interested to mock.
	 */
	public static class DummyCConfigurationDescription implements ICConfigurationDescription {
		private String id;
		private ICProjectDescription projectDescription;

		public DummyCConfigurationDescription(String id) {
			this.id = id;
			this.projectDescription = new DummyCProjectDescription();
		}

		@Override
		public ICSettingObject[] getChildSettings() {
			return null;
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public int getType() {
			return 0;
		}

		@Override
		public boolean isValid() {
			return false;
		}

		@Override
		public ICConfigurationDescription getConfiguration() {
			return null;
		}

		@Override
		public ICSettingContainer getParent() {
			return null;
		}

		@Override
		public boolean isReadOnly() {
			return false;
		}

		@Override
		public ICStorageElement getStorage(String id, boolean create) throws CoreException {
			return null;
		}

		@Override
		public void removeStorage(String id) throws CoreException {
		}

		@Override
		public ICStorageElement importStorage(String id, ICStorageElement el)
				throws UnsupportedOperationException, CoreException {
			return null;
		}

		@Override
		public void setReadOnly(boolean readOnly, boolean keepModify) {
		}

		@Override
		public boolean isActive() {
			return false;
		}

		@Override
		public String getDescription() {
			return null;
		}

		@Override
		public void setDescription(String des) throws WriteAccessException {
		}

		@Override
		public ICProjectDescription getProjectDescription() {
			return projectDescription;
		}

		@Override
		public ICFolderDescription getRootFolderDescription() {
			return null;
		}

		@Override
		public ICFolderDescription[] getFolderDescriptions() {
			return null;
		}

		@Override
		public ICFileDescription[] getFileDescriptions() {
			return null;
		}

		@Override
		public ICResourceDescription[] getResourceDescriptions() {
			return null;
		}

		@Override
		public ICResourceDescription getResourceDescription(IPath path, boolean exactPath) {
			return null;
		}

		@Override
		public void removeResourceDescription(ICResourceDescription des) throws CoreException, WriteAccessException {
		}

		@Override
		public ICFileDescription createFileDescription(IPath path, ICResourceDescription base)
				throws CoreException, WriteAccessException {
			return null;
		}

		@Override
		public ICFolderDescription createFolderDescription(IPath path, ICFolderDescription base)
				throws CoreException, WriteAccessException {
			return null;
		}

		@Override
		public String getBuildSystemId() {
			return null;
		}

		@Override
		public CConfigurationData getConfigurationData() {
			return null;
		}

		@Override
		public void setActive() throws WriteAccessException {
		}

		@Override
		public void setConfigurationData(String buildSystemId, CConfigurationData data) throws WriteAccessException {
		}

		@Override
		public boolean isModified() {
			return false;
		}

		@Override
		public ICTargetPlatformSetting getTargetPlatformSetting() {
			return null;
		}

		@Override
		public ICSourceEntry[] getSourceEntries() {
			return null;
		}

		@Override
		public ICSourceEntry[] getResolvedSourceEntries() {
			return null;
		}

		@Override
		public void setSourceEntries(ICSourceEntry[] entries) throws CoreException, WriteAccessException {
		}

		@Override
		public Map<String, String> getReferenceInfo() {
			return null;
		}

		@Override
		public void setReferenceInfo(Map<String, String> refs) throws WriteAccessException {
		}

		@Override
		public ICExternalSetting[] getExternalSettings() {
			return null;
		}

		@Override
		public ICExternalSetting createExternalSetting(String[] languageIDs, String[] contentTypeIds,
				String[] extensions, ICSettingEntry[] entries) throws WriteAccessException {
			return null;
		}

		@Override
		public void removeExternalSetting(ICExternalSetting setting) throws WriteAccessException {
		}

		@Override
		public void removeExternalSettings() throws WriteAccessException {
		}

		@Override
		public ICBuildSetting getBuildSetting() {
			return null;
		}

		@Override
		public ICdtVariablesContributor getBuildVariablesContributor() {
			return null;
		}

		@Override
		public Object getSessionProperty(QualifiedName name) {
			return null;
		}

		@Override
		public void setSessionProperty(QualifiedName name, Object value) {
		}

		@Override
		public void setName(String name) throws WriteAccessException {
		}

		@Override
		public ICConfigExtensionReference[] get(String extensionPointID) {
			return null;
		}

		@Override
		public ICConfigExtensionReference create(String extensionPoint, String extension) throws CoreException {
			return null;
		}

		@Override
		public void remove(ICConfigExtensionReference ext) throws CoreException {
		}

		@Override
		public void remove(String extensionPoint) throws CoreException {
		}

		@Override
		public boolean isPreferenceConfiguration() {
			return false;
		}

		@Override
		public ICLanguageSetting getLanguageSettingForFile(IPath path, boolean ignoreExludeStatus) {
			return null;
		}

		@Override
		public void setExternalSettingsProviderIds(String[] ids) {
		}

		@Override
		public String[] getExternalSettingsProviderIds() {
			return null;
		}

		@Override
		public void updateExternalSettingsProviders(String[] ids) throws WriteAccessException {
		}

		@Override
		public CConfigurationStatus getConfigurationStatus() {
			return null;
		}

	}
}
