/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Andrew Ferguson (Symbian)
 *     James Blackburn (Broadcom Corp.)
 *******************************************************************************/
package org.eclipse.cdt.core;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Element;
/**
 * Models meta-data stored with a CDT project
 *
 * Consumers should ensure that changes made to an ICDescriptor are done
 * in the context of an {@link ICDescriptorOperation}
 * via {@link ICDescriptorManager#runDescriptorOperation}
 *
 * @see ICDescriptorOperation
 * @see ICDescriptorManager
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @deprecated as this API is not configuration aware. Replaced by 
 * {@link ICConfigurationDescription} which can be fetched with 
 * {@link ICProjectDescription#getConfigurations()}
 */
@Deprecated
public interface ICDescriptor {
	public ICOwnerInfo getProjectOwner();
	public String getPlatform();

	/**
	 * @return the associated project
	 */
	public IProject getProject();

	/**
	 * Returns an {@link ICExtensionReference}[] corresponding to a particular extensionPoint.
	 * This array contains all the ICExtensionReferences from all the ICConfigurationDescriptions
	 * in the project
	 * @param extensionPointID String extensionPointID
	 * @return ICExtensionReference[] ICExtensionReference array
	 * @deprecated
	 * @use {@link ICConfigurationDescription#get(String)}
	 */
	@Deprecated
	public ICExtensionReference[] get(String extensionPointID);

	/**
	 * Returns an {@link ICExtensionReference}[] corresponding to a particular extensionPoint.
	 * This array contains all the ICExtensionReferences from all the ICConfigurationDescriptions
	 * in the project
	 * @param extensionPointID String extensionPointID
	 * @param update updates the COwner
	 * @return ICExtensionReference[] ICExtensionReference array
	 * @deprecated
	 */
	@Deprecated
	public ICExtensionReference[] get(String extensionPointID, boolean update) throws CoreException;

	/**
	 * Create an ICExtensionReference for the given extensionPointId with id = id.
	 * 
	 * This is not CConfigurationDescription aware and so is added to all configurations in the project.
	 * You should instead use:
	 * {@link ICConfigurationDescription#create(String, String)}
	 *
	 * @param extensionPointID
	 * @param id
	 * @return the create ICExtensionReference
	 * @throws CoreException
	 * @deprecated
	 * @use {@link ICConfigurationDescription#create(String, String)}
	 */
	@Deprecated
	public ICExtensionReference create(String extensionPointID, String id) throws CoreException;

	/**
	 * Remove a given ICExtensionReference from the project description.
	 * @param extension
	 * @throws CoreException
	 * @deprecated
	 * @use {@link ICConfigurationDescription#remove(org.eclipse.cdt.core.settings.model.ICConfigExtensionReference)}
	 */
	@Deprecated
	public void remove(ICExtensionReference extension) throws CoreException;

	/**
	 * Remove ICExtensionReferences with the given extensionPoint from
	 * this descriptor
	 * @param extensionPoint
	 * @throws CoreException
	 * @deprecated
	 * @use {@link ICConfigurationDescription#remove(String)}
	 */
	@Deprecated
	public void remove(String extensionPoint) throws CoreException;

	/**
	 * Return a storage element corresponding to the id in which
	 * client related metadata may be stored.
	 *
	 * @param id an identifier that uniquely identifies the client
	 * @return a non-null {@link ICStorageElement} to which client specific meta-data may be attached
	 * @throws CoreException
	 * @since 5.1
	 */
	public ICStorageElement getProjectStorageElement(String id) throws CoreException;

	/**
	 * This method has been superceded by {@link ICDescriptor#getProjectStorageElement(String)}
	 * @param id an identifier that uniquely identifies the client
	 * @return a non-null {@link Element} to which client specific meta-data may be attached
	 * @throws CoreException
	 * @deprecated
	 * @noreference This method is not intended to be referenced by clients.
	 * @use {@link ICDescriptor#getProjectStorageElement(String)}
	 */
	@Deprecated
	public Element getProjectData(String id) throws CoreException;

	/**
	 * Remove the storage element with the given ID from the tree
	 * @param id
	 * @return the ICStorageElement removed or null if there was none for id
	 * @throws CoreException
	 * @since 5.1
	 */
	public ICStorageElement removeProjectStorageElement(String id) throws CoreException;

	/**
	 * Saves any changes made to {@link ICStorageElement} objects obtained from {@link #getProjectStorageElement(String)}
	 * to a CDT defined project meta-data file.
	 * @throws CoreException
	 */
	public void saveProjectData() throws CoreException;

	/**
	 * Returns the current settings configuration (which contains this descriptor) -- currently
	 * equivalent to {@link ICProjectDescription#getDefaultSettingConfiguration()}
	 * @return the current setting {@link ICConfigurationDescription}
	 */
	ICConfigurationDescription getConfigurationDescription();

}
