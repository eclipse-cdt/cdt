/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.internal.core;

import java.util.StringTokenizer;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICOwner;
import org.eclipse.cdt.core.ICOwnerInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class COwner implements ICOwnerInfo {
	String ownerID;
	IExtension extension;
	
	public COwner(String id) throws CoreException {
		ownerID = id;
		IExtensionPoint extpoint = CCorePlugin.getDefault().getDescriptor().getExtensionPoint("CProjectOwner");
		if (extpoint != null) {
			extension =  extpoint.getExtension(ownerID);
		} else {
			IStatus status = new Status(IStatus.ERROR, CCorePlugin.getDefault().PLUGIN_ID, -1, "Invalid CDTProject owner ID", (Throwable)null);
			throw new CoreException(status);
		}
	}

	public String getID() {
		return ownerID;
	}
	
	public String getName() {
		return extension == null ? null : extension.getLabel();
	}

	public String[] getPlatforms() {
		IConfigurationElement element[] = extension.getConfigurationElements();
		String platforms[] = new String[element.length];
		for( int i = 0; i < element.length; i++ ) {
			platforms[i] = element[i].getAttribute("id");
		}
		return platforms;
	}

	public String getPlatformName(String platform) {
		IConfigurationElement element[] = extension.getConfigurationElements();
		String platforms[] = new String[element.length];
		for( int i = 0; i < element.length; i++ ) {
			if ( platform.equals(element[i].getAttribute("id")) ) {
				return element[i].getAttribute("name");
			}
		}
		return "";
	}
	
	public String[] getArchitectures(String platform) {
		IConfigurationElement element[] = extension.getConfigurationElements();
		String platforms[] = new String[element.length];
		for( int i = 0; i < element.length; i++ ) {
			if ( platform.equals(element[i].getAttribute("id")) ) {
				StringTokenizer stoken = new StringTokenizer(element[i].getAttribute("architecture"), ",");
				String[] archs = new String[stoken.countTokens()];
				for( int j = 0; j < archs.length; j++ ) {
					archs[i] = stoken.nextToken();
				}
			}
		}
		return new String[0];
	}
	
	void configure(IProject project, ICDescriptor cproject) throws CoreException {
		IConfigurationElement element[] = extension.getConfigurationElements();
		for( int i = 0; i < element.length; i++ ) {
			if ( element[i].getName().equalsIgnoreCase("run") ) {
				ICOwner owner = (ICOwner) element[i].createExecutableExtension("class");
				owner.configure(cproject);
				return;
			}
		}
		IStatus status = new Status(IStatus.ERROR, CCorePlugin.getDefault().PLUGIN_ID, -1, "Invalid CDTProject owner extension", (Throwable)null);
		throw new CoreException(status);
	}

	void update(IProject project, ICDescriptor cproject) throws CoreException {
		IConfigurationElement element[] = extension.getConfigurationElements();
		for( int i = 0; i < element.length; i++ ) {
			if ( element[i].getName().equalsIgnoreCase("run") ) {
				ICOwner owner = (ICOwner) element[i].createExecutableExtension("class");
				owner.update(cproject);
				return;
			}
		}
		IStatus status = new Status(IStatus.ERROR, CCorePlugin.getDefault().PLUGIN_ID, -1, "Invalid CDTProject owner extension", (Throwable)null);
		throw new CoreException(status);
	}

}
