/********************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 *
 * Contributors:
 * David Dykstal (IBM) - 168977: refactoring IConnectorService and ServerLauncher hierarchies
 * David Dykstal (IBM) - 142806: refactoring persistence framework
 * Martin Oberhuber (Wind River) - [185750] Remove IConnectorService.getHostType()
 * David Dykstal (IBM) - [210474] Deny save password function missing
 * David Dykstal (IBM) - [225089][ssh][shells][api] Canceling connection leads to exception
 * David McKnight   (IBM)        - [338510] "Copy Connection" operation deletes the registered property set in the original connection
 ********************************************************************************/
package org.eclipse.rse.core.subsystems;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.IPropertySet;
import org.eclipse.rse.core.model.IPropertySetContainer;
import org.eclipse.rse.core.model.IRSEPersistableContainer;

public abstract class AbstractDelegatingConnectorService implements IDelegatingConnectorService
{

	protected IHost _host;

	/**
	 * Creates a new delegating connector service for a particular host.
	 * Should be invoked from the constructor for any concrete subclasses.
	 * @param host The host associated with this connector service.
	 */
	public AbstractDelegatingConnectorService(IHost host)
	{
		_host = host;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IDelegatingConnectorService#getRealConnectorService()
	 */
	public abstract IConnectorService getRealConnectorService();

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#addCommunicationsListener(org.eclipse.rse.core.subsystems.ICommunicationsListener)
	 */
	public void addCommunicationsListener(ICommunicationsListener listener)
	{
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.addCommunicationsListener(listener);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IPropertySetContainer#addPropertySet(org.eclipse.rse.core.model.IPropertySet)
	 */
	public boolean addPropertySet(IPropertySet set) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.addPropertySet(set);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IPropertySetContainer#addPropertySets(org.eclipse.rse.core.model.IPropertySet[])
	 */
	public boolean addPropertySets(IPropertySet[] sets) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.addPropertySets(sets);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#clearPassword(boolean, boolean)
	 */
	public void clearPassword(boolean clearDiskCache, boolean propagate) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.clearPassword(clearDiskCache, propagate);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#clearCredentials()
	 */
	public void clearCredentials() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.clearCredentials();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IRSEPersistableContainer#commit()
	 */
	public boolean commit() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.commit();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#connect(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void connect(IProgressMonitor monitor) throws Exception {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.connect(monitor);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IPropertySetContainer#createPropertySet(java.lang.String)
	 */
	public IPropertySet createPropertySet(String name) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.createPropertySet(name);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IPropertySetContainer#createPropertySet(java.lang.String, java.lang.String)
	 */
	public IPropertySet createPropertySet(String name, String description) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.createPropertySet(name, description);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#deregisterSubSystem(org.eclipse.rse.core.subsystems.ISubSystem)
	 */
	public void deregisterSubSystem(ISubSystem ss) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.deregisterSubSystem(ss);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#disconnect(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void disconnect(IProgressMonitor monitor) throws Exception {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.disconnect(monitor);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IRSEModelObject#getDescription()
	 */
	public String getDescription() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.getDescription();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#getHomeDirectory()
	 */
	public String getHomeDirectory()
	{
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.getHomeDirectory();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#getHost()
	 */
	public IHost getHost()
	{
		return _host;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#getHostName()
	 */
	public String getHostName() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.getHostName();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IRSEModelObject#getName()
	 */
	public String getName() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.getName();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#getPort()
	 */
	public int getPort() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.getPort();
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#getPrimarySubSystem()
	 */
	public ISubSystem getPrimarySubSystem() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.getPrimarySubSystem();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IPropertySetContainer#getPropertySet(java.lang.String)
	 */
	public IPropertySet getPropertySet(String name) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.getPropertySet(name);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IPropertySetContainer#getPropertySets()
	 */
	public IPropertySet[] getPropertySets() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.getPropertySets();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#getRemoteServerLauncher()
	 */
	public IServerLauncher getRemoteServerLauncher() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.getRemoteServerLauncher();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#getRemoteServerLauncherProperties()
	 */
	public IServerLauncherProperties getRemoteServerLauncherProperties() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.getRemoteServerLauncherProperties();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#getSubSystems()
	 */
	public ISubSystem[] getSubSystems() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.getSubSystems();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#getTempDirectory()
	 */
	public String getTempDirectory() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.getTempDirectory();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#getUserId()
	 */
	public String getUserId() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.getUserId();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#getVersionReleaseModification()
	 */
	public String getVersionReleaseModification() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.getVersionReleaseModification();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#hasPassword(boolean)
	 */
	public boolean hasPassword(boolean onDisk) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.hasPassword(onDisk);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#hasRemoteServerLauncherProperties()
	 */
	public boolean hasRemoteServerLauncherProperties() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.hasRemoteServerLauncherProperties();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#inheritsCredentials()
	 */
	public boolean inheritsCredentials() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.inheritsCredentials();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#isConnected()
	 */
	public boolean isConnected() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.isConnected();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IRSEPersistableContainer#isDirty()
	 */
	public boolean isDirty() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.isDirty();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#isServerLaunchTypeEnabled(org.eclipse.rse.core.subsystems.ISubSystem, org.eclipse.rse.core.subsystems.ServerLaunchType)
	 */
	public boolean isServerLaunchTypeEnabled(ISubSystem subsystem,
			ServerLaunchType serverLaunchType) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.isServerLaunchTypeEnabled(subsystem, serverLaunchType);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#isSuppressed()
	 */
	public boolean isSuppressed() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.isSuppressed();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#isUsingSSL()
	 */
	public boolean isUsingSSL() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.isUsingSSL();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#acquireCredentials(boolean)
	 */
	public void acquireCredentials(boolean forcePrompt)
			throws OperationCanceledException {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.acquireCredentials(forcePrompt);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#registerSubSystem(org.eclipse.rse.core.subsystems.ISubSystem)
	 */
	public void registerSubSystem(ISubSystem ss) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.registerSubSystem(ss);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#removeCommunicationsListener(org.eclipse.rse.core.subsystems.ICommunicationsListener)
	 */
	public void removeCommunicationsListener(ICommunicationsListener listener) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.removeCommunicationsListener(listener);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IPropertySetContainer#removePropertySet(java.lang.String)
	 */
	public boolean removePropertySet(String name) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.removePropertySet(name);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#reset()
	 */
	public void reset() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.reset();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IRSEPersistableContainer#setDirty(boolean)
	 */
	public void setDirty(boolean flag) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.setDirty(flag);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#setHost(org.eclipse.rse.core.model.IHost)
	 */
	public void setHost(IHost host) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.setHost(host);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#setIsUsingSSL(boolean)
	 */
	public void setIsUsingSSL(boolean flag) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.setIsUsingSSL(flag);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#setPassword(java.lang.String, java.lang.String, boolean, boolean)
	 */
	public void setPassword(String matchingUserId, String password,
			boolean persist, boolean propagate) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.setPassword(matchingUserId, password, persist, propagate);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#setPort(int)
	 */
	public void setPort(int port) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.setPort(port);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#setRemoteServerLauncherProperties(org.eclipse.rse.core.subsystems.IServerLauncherProperties)
	 */
	public void setRemoteServerLauncherProperties(
			IServerLauncherProperties value) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.setRemoteServerLauncherProperties(value);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#setSuppressed(boolean)
	 */
	public void setSuppressed(boolean suppressSignonPrompt) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.setSuppressed(suppressSignonPrompt);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#setUserId(java.lang.String)
	 */
	public void setUserId(String userId) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.setUserId(userId);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IRSEPersistableContainer#setWasRestored(boolean)
	 */
	public void setWasRestored(boolean flag) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.setWasRestored(flag);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#sharesCredentials()
	 */
	public boolean sharesCredentials() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.sharesCredentials();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#supportsPassword()
	 */
	public boolean supportsPassword() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.supportsPassword();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#supportsRemoteServerLaunching()
	 */
	public boolean supportsRemoteServerLaunching() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.supportsRemoteServerLaunching();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#supportsServerLaunchProperties()
	 */
	public boolean supportsServerLaunchProperties() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.supportsServerLaunchProperties();
		}
		return false;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#supportsUserId()
	 */
	public boolean supportsUserId() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.supportsUserId();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#saveUserId()
	 */
	public void saveUserId() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.saveUserId();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#removeUserId()
	 */
	public void removeUserId() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.removeUserId();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#savePassword()
	 */
	public void savePassword() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.savePassword();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#removePassword()
	 */
	public void removePassword() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.removePassword();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IRSEPersistableContainer#wasRestored()
	 */
	public boolean wasRestored() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.wasRestored();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#requiresPassword()
	 */
	public boolean requiresPassword() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.requiresPassword();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.IConnectorService#requiresUserId()
	 */
	public boolean requiresUserId() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.requiresUserId();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IRSEPersistableContainer#isTainted()
	 */
	public boolean isTainted() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.isTainted();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IRSEPersistableContainer#setTainted(boolean)
	 */
	public void setTainted(boolean flag) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.setTainted(flag);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IRSEPersistableContainer#getPersistableParent()
	 */
	public IRSEPersistableContainer getPersistableParent() {
		return getHost();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.model.IRSEPersistableContainer#getPersistableChildren()
	 */
	public IRSEPersistableContainer[] getPersistableChildren() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.getPersistableChildren();
		}
		return IRSEPersistableContainer.NO_CHILDREN;
	}

	/**
	 * {@inheritDoc}
	 * @since org.eclipse.rse.core 3.0
	 */
	public int setDenyPasswordSave(boolean deny) {
		int n = 0;
		IConnectorService connectorService = getRealConnectorService();
		if (connectorService != null) {
			n = connectorService.setDenyPasswordSave(deny);
		}
		return n;
	}

	/**
	 * {@inheritDoc}
	 * @since org.eclipse.rse.core 3.0
	 */
	public boolean getDenyPasswordSave() {
		boolean result = false;
		IConnectorService connectorService = getRealConnectorService();
		if (connectorService != null) {
			result = connectorService.getDenyPasswordSave();
		}
		return result;
	}

	/**
	 * @since 3.2
	 */
	public void clonePropertySets(IPropertySetContainer targetContainer) {
		IConnectorService connectorService = getRealConnectorService();
		if (connectorService != null) {
			connectorService.clonePropertySets(targetContainer);
		}
	}

	
}
