/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.internal.core;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDebugger;
import org.eclipse.cdt.debug.core.ICDebuggerManager;
import org.eclipse.cdt.debug.core.ICDebuggerInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;

public class CDebuggerManager implements ICDebuggerManager {

	public CDebuggerManager() {
	}

	public ICDebugger createDebugger(String id) throws CoreException {
		ICDebugger debugger = null;

		IExtensionPoint extension = CDebugCorePlugin.getDefault().getDescriptor().getExtensionPoint("CDebugger");
		if (extension != null) {
			IExtension[] extensions =  extension.getExtensions();
			for(int i = 0; i < extensions.length; i++){
				if ( id.equals(extensions[i].getUniqueIdentifier()) ) {
					IConfigurationElement [] configElements = extensions[i].getConfigurationElements();
					debugger = (ICDebugger)configElements[0].createExecutableExtension("class");
				}
			}
		}
		return debugger;
	}
		
	public ICDebuggerInfo[] queryDebuggers(String platform_id) {
		IExtensionPoint extension = CDebugCorePlugin.getDefault().getDescriptor().getExtensionPoint("CDebugger");
		if (extension != null) {
			IExtension[] extensions =  extension.getExtensions();
			CDebuggerInfo dinfo;
			ArrayList dlist = new ArrayList(extensions.length);
			for(int i = 0; i < extensions.length; i++){
				IConfigurationElement [] configElements = extensions[i].getConfigurationElements();
				String platform = configElements[0].getAttribute("platform");
				if ( platform == null || platform.equals("*") || platform.indexOf(platform_id) != -1 ) {
					dlist.add(dinfo = new CDebuggerInfo());
					dinfo.name = extensions[i].getLabel();
					dinfo.id = extensions[i].getUniqueIdentifier();
					dinfo.platforms = platform;
				}
			}
			return (ICDebuggerInfo[]) dlist.toArray(new ICDebuggerInfo[dlist.size()]);
		}		
		return new ICDebuggerInfo[0];
	}
	
	class CDebuggerInfo implements ICDebuggerInfo {
		String id;
		String name;
		String platforms;
		
		public String getID() {
			return id;
		}
		
		public String getName() {
			return name;
		}
		
		public String[] getSupportedPlatforms() {
			StringTokenizer stoken = new StringTokenizer(platforms, ",");
			String[] platforms = new String[stoken.countTokens()];
			for( int i = 0; i < platforms.length; i++ ) {
				platforms[i] = stoken.nextToken();
			}
			return platforms;
		}
	}
}
