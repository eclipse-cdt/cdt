/*
 * (c) Copyright QNX Software System Ltd. 2002.
 * All Rights Reserved.
 */
package org.eclipse.cdt.debug.internal.core;

import java.util.HashSet;
import java.util.Set;
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
	private HashSet fModes;

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
		String name = getConfigurationElement().getAttribute("name"); //$NON-NLS-1$
		return name != null ? name : "";
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
	
	public boolean supportsMode(String mode) {
		return getModes().contains(mode);
	}

	/**
	 * Returns the set of modes specified in the configuration data.
	 * 
	 * @return the set of modes specified in the configuration data
	 */
	protected Set getModes() {
		if (fModes == null) {
			String modes= getConfigurationElement().getAttribute("modes"); //$NON-NLS-1$
			if (modes == null) {
				return new HashSet(0);
			}
			StringTokenizer tokenizer= new StringTokenizer(modes, ","); //$NON-NLS-1$
			fModes = new HashSet(tokenizer.countTokens());
			while (tokenizer.hasMoreTokens()) {
				fModes.add(tokenizer.nextToken().trim());
			}
		}
		return fModes;
	}

}
