/********************************************************************************
 * Copyright (c) 2007 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/
package org.eclipse.rse.core.subsystems;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.IPropertySet;

public abstract class AbstractDelegatingConnectorService implements IDelegatingConnectorService 
{

	protected IHost _host;
	public AbstractDelegatingConnectorService(IHost host)
	{
		_host = host;
	}
	

	public abstract IConnectorService getRealConnectorService();
	
	public void addCommunicationsListener(ICommunicationsListener listener) 
	{
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.addCommunicationsListener(listener);
		}
		
	}

	public void clearPassword(boolean clearDiskCache) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.clearPassword(clearDiskCache);
		}

	}

	public void clearUserId() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.clearUserId();
		}
	}

	public void connect(IProgressMonitor monitor) throws Exception {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.connect(monitor);
		}
	}

	public void deregisterSubSystem(ISubSystem ss) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.deregisterSubSystem(ss);
		}
	}

	public void disconnect(IProgressMonitor monitor) throws Exception {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.disconnect(monitor);
		}

	}

	public String getHomeDirectory() 
	{
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.getHomeDirectory();
		}
		return null;
	}

	public IHost getHost() 
	{
		return _host;
	}

	public String getHostName() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.getHostName();
		}
		return null;
	}

	public String getHostType() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.getHostType();
		}
		return null;
	}

	public String getName() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.getName();
		}
		return null;
	}

	public int getPort() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.getPort();
		}
		return 0;
	}

	public ISubSystem getPrimarySubSystem() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.getPrimarySubSystem();
		}
		return null;
	}

	public IServerLauncherProperties getRemoteServerLauncherProperties() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.getRemoteServerLauncherProperties();
		}
		return null;
	}

	public ISubSystem[] getSubSystems() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.getSubSystems();
		}
		return null;
	}

	public String getTempDirectory() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.getTempDirectory();
		}
		return null;
	}

	public String getUserId() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.getUserId();
		}
		return null;
	}

	public String getVersionReleaseModification() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.getVersionReleaseModification();
		}		
		return null;
	}

	public boolean hasRemoteServerLauncherProperties() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.hasRemoteServerLauncherProperties();
		}
		return false;
	}

	public boolean inheritConnectionUserPassword() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.inheritConnectionUserPassword();
		}
		return false;
	}

	public boolean isConnected() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.isConnected();
		}
		return false;
	}

	public boolean hasPassword(boolean onDisk) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.hasPassword(onDisk);
		}
		return false;
	}

	public boolean isSuppressSignonPrompt() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.isSuppressSignonPrompt();
		}
		return false;
	}

	public boolean isUsingSSL() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.isUsingSSL();
		}
		return false;
	}

	public void notifyConnection() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.notifyConnection();
		}
	}

	public void notifyDisconnection() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.notifyDisconnection();
		}
	}

	public void notifyError() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.notifyError();
		}
	}

	public void promptForPassword(boolean forcePrompt)
			throws InterruptedException {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.promptForPassword(forcePrompt);
		}
	}

	public void registerSubSystem(ISubSystem ss) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.registerSubSystem(ss);
		}
	}

	public void removeCommunicationsListener(ICommunicationsListener listener) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.removeCommunicationsListener(listener);
		}
	}

	public boolean requiresPassword() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.requiresPassword();
		}
		return false;
	}

	public boolean requiresUserId() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.requiresUserId();
		}
		return false;
	}

	public void reset() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.reset();
		}
	}

	public void setHost(IHost host) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.setHost(host);
		}
	}

	public void setIsUsingSSL(boolean flag) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.setIsUsingSSL(flag);
		}
	}

	public void setPassword(String matchingUserId, String password,
			boolean persist) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.setPassword(matchingUserId, password, persist);
		}
	}

	public void setPort(int port) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.setPort(port);
		}
	}

	public void setRemoteServerLauncherProperties(
			IServerLauncherProperties value) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.setRemoteServerLauncherProperties(value);
		}
	}

	public void setSuppressSignonPrompt(boolean suppressSignonPrompt) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.setSuppressSignonPrompt(suppressSignonPrompt);			
		}
	}

	public void setUserId(String userId) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.setUserId(userId);
		}
	}

	public boolean shareUserPasswordWithConnection() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.shareUserPasswordWithConnection();
		}
		return false;
	}

	public boolean supportsPassword() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.supportsPassword();
		}
		return false;
	}

	public boolean supportsRemoteServerLaunching() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.supportsRemoteServerLaunching();
		}
		return false;
	}

	public boolean supportsServerLaunchProperties() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.supportsServerLaunchProperties();
		}
		return false;
	}

	public boolean supportsUserId() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.supportsUserId();
		}
		return false;
	}

	public String getDescription() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.getDescription();
		}
		return null;
	}

	public boolean addPropertySet(IPropertySet set) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.addPropertySet(set);
		}
		return false;
	}

	public boolean addPropertySets(IPropertySet[] sets) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.addPropertySets(sets);
		}
		return false;
	}

	public IPropertySet createPropertySet(String name) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.createPropertySet(name);
		}
		return null;
	}

	public IPropertySet createPropertySet(String name, String description) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.createPropertySet(name, description);
		}
		return null;
	}

	public IPropertySet getPropertySet(String name) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.getPropertySet(name);
		}
		return null;
	}

	public IPropertySet[] getPropertySets() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.getPropertySets();
		}
		return null;
	}

	public boolean removePropertySet(String name) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.removePropertySet(name);
		}
		return false;
	}

	public boolean commit() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.commit();
		}
		return false;
	}

	public boolean isDirty() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.isDirty();
		}
		return false;
	}

	public void setDirty(boolean flag) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.setDirty(flag);
		}
	}

	public void setWasRestored(boolean flag) {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			conServ.setWasRestored(flag);
		}
	}

	public boolean wasRestored() {
		IConnectorService conServ = getRealConnectorService();
		if (conServ != null)
		{
			return conServ.wasRestored();
		}
		return false;
	}

}
