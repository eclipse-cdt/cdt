/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IFilesystemUtility;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

/**
 * Manager class that consults contributors to the FileSystemUtility extension point to perform operations corresponding to those filesystems.
 *
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the CDT team.
 * 
 * @author crecoskie
 * @noextend This class is not intended to be extended by clients.
 * @since 5.2
 */
public class FileSystemUtilityManager {
	
	private static FileSystemUtilityManager instance;
	
	private Map<String, IFilesystemUtility> fSchemeToUtilityImplementerMap;
	
	private static String EXTENSION_ID = "FileSystemUtility"; //$NON-NLS-1$
	
	private FileSystemUtilityManager() {
		fSchemeToUtilityImplementerMap = new HashMap<String, IFilesystemUtility>();
		loadExtensions();
	}
	
	private void loadExtensions() {
		IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID,
				EXTENSION_ID);
		if (extension != null) {
			IExtension[] extensions = extension.getExtensions();
			for (int i = 0; i < extensions.length; i++) {
				IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
				for (int j = 0; j < configElements.length; j++) {

					String scheme = configElements[j].getAttribute("scheme"); //$NON-NLS-1$
					String utility = configElements[j].getAttribute("class"); //$NON-NLS-1$

					if (utility != null) {
						try {
							Object execExt = configElements[j].createExecutableExtension("class"); //$NON-NLS-1$
							if (execExt instanceof IFilesystemUtility) {
								fSchemeToUtilityImplementerMap.put(scheme, (IFilesystemUtility) execExt);
							}
						} catch (CoreException e) {
							CCorePlugin.log(e);
						}
					}

				}
			}
		}

	}

	public synchronized static FileSystemUtilityManager getDefault() {
		if(instance == null) {
			instance = new FileSystemUtilityManager();
		}
		return instance;
	}
	
	/**
	 * Gets the path out of a URI.  Right now this is hardcoded to deal with a select few filesystems.
	 * In the future, it would be better if EFS had an API for this.
	 * 
	 * @param locationURI
	 * @return String representing the path.
	 */
	public String getPathFromURI(URI locationURI) {
		IFilesystemUtility utility = fSchemeToUtilityImplementerMap.get(locationURI.getScheme());
		
		if(utility == null) {
			return locationURI.getPath();
		}
			
		else {
			return utility.getPathFromURI(locationURI);
		}
		
	}
	
	/**
	 * In the case of a managed (linked) filesystem, returns the URI that this URI ultimately will
	 * point to.  Otherwise, returns null.
	 * 
	 * @param locationURI
	 * @return URI
	 */
	public URI getManagedURI(URI locationURI) {
		IFilesystemUtility utility = fSchemeToUtilityImplementerMap.get(locationURI.getScheme());
		
		if(utility == null) {
			return null;
		}
			
		else {
			return utility.getBaseURI(locationURI);
		}
	}

	/**
	 * Creates a new URI which clones the contents of the original URI, but with the path
	 * replaced by the given path.  Returns null on error.
	 * 
	 * @param uri
	 * @param path
	 * @return URI
	 */
	public URI replacePath(URI uri, String path) {
		IFilesystemUtility utility = fSchemeToUtilityImplementerMap.get(uri.getScheme());
		
		if(utility == null) {
			// if there is no corresponding utility, then assume we can just replace the path field
			
			// Is it a local filesystem uri? Its URIs are a bit weird sometimes, so use URIUtil
			if(uri.getScheme().equals("file")) { //$NON-NLS-1$
				return URIUtil.toURI(path);
			}
			
			try {
				return  new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(),
			               path, // replaced! 
			               uri.getQuery(),uri.getFragment());
			} catch (URISyntaxException e) {
				String message = "Problem converting path to URI [" + path.toString() + "]";  //$NON-NLS-1$//$NON-NLS-2$
				CCorePlugin.log(message, e);
			}
			
			return null;
		}
			
		else {
			return utility.replacePathInURI(uri, path);
		}
	}
	
	public String getMappedPath(URI uri) {
		IFilesystemUtility utility = fSchemeToUtilityImplementerMap.get(uri.getScheme());
		
		if(utility == null) {
			// if there is no corresponding utility, then assume it's just the path field
			return uri.getPath();

		}
			
		else {
			return utility.getMappedPath(uri);
		}
	}

}
