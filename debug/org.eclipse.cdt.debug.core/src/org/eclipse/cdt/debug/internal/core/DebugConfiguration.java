/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.internal.core;

import java.util.StringTokenizer;

import org.eclipse.cdt.debug.core.ICDebugConfiguration;
import org.eclipse.cdt.debug.core.ICDebugger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

public class DebugConfiguration implements ICDebugConfiguration {

	/**
	 * The configuration element of the extension.
	 */
	private IConfigurationElement fElement;

	public DebugConfiguration(IConfigurationElement element) {
		fElement = element;
	}
	
	private IConfigurationElement getConfigurationElement() {
		return fElement;
	}	
	
	public ICDebugger getDebugger() throws CoreException {
		return (ICDebugger)getConfigurationElement().createExecutableExtension("class");
	}

	public String getName() {
		return getConfigurationElement().getAttribute("name"); //$NON-NLS-1$
	}

	public String getID() {
		return getConfigurationElement().getAttribute("id"); //$NON-NLS-1$
	}

	public String[] getPlatforms() {
		String platform = getConfigurationElement().getAttribute("platform");
		if ( platform == null ) {
			return new String[] {"*"};
		}
		StringTokenizer stoken = new StringTokenizer(platform, ",");
		String[] platforms = new String[stoken.countTokens()];
		for( int i = 0; i < platforms.length; i++ ) {
			platforms[i] = stoken.nextToken();
		}
		return platforms;

		
	}

}
