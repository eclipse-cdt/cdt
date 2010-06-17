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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.EFSExtensionProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

/**
 * Manager class that consults contributors to the EFSExtensionProvider extension point
 * to perform operations corresponding to those filesystems. The default behaviour if no provider is present
 * is to assumes that URIs for the given filesystem map directly to resources in the physical filesystem, and
 * that the path component of the URI is a direct representation of the absolute path to the file in the
 * physical filesystem. Also, operations will by default respect the syntax and semantics of the local EFS
 * filesystem, if operations are performed with respect to it.
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in progress. There
 * is no guarantee that this API will work or that it will remain the same. Please do not use this API without
 * consulting with the CDT team.
 * 
 * @author crecoskie
 * @noextend This class is not intended to be extended by clients.
 * @since 5.2
 */
public class EFSExtensionManager {
	
	private class DefaultProvider extends EFSExtensionProvider {

	}

	private DefaultProvider fDefaultProvider = new DefaultProvider();

	private static EFSExtensionManager instance;

	private Map<String, EFSExtensionProvider> fSchemeToExtensionProviderMap;

	private static String EXTENSION_ID = "EFSExtensionProvider"; //$NON-NLS-1$

	private EFSExtensionManager() {
		fSchemeToExtensionProviderMap = new HashMap<String, EFSExtensionProvider>();
		loadExtensions();
	}

	private void loadExtensions() {
		IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID,
				EXTENSION_ID);
		if (extension != null) {
			IExtension[] extensions = extension.getExtensions();
			for (IExtension extension2 : extensions) {
				IConfigurationElement[] configElements = extension2.getConfigurationElements();
				for (IConfigurationElement configElement : configElements) {

					String scheme = configElement.getAttribute("scheme"); //$NON-NLS-1$
					String utility = configElement.getAttribute("class"); //$NON-NLS-1$

					if (utility != null) {
						try {
							Object execExt = configElement.createExecutableExtension("class"); //$NON-NLS-1$
							if (execExt instanceof EFSExtensionProvider) {
								fSchemeToExtensionProviderMap.put(scheme,
										(EFSExtensionProvider) execExt);
							}
						} catch (CoreException e) {
							CCorePlugin.log(e);
						}
					}

				}
			}
		}

	}

	public synchronized static EFSExtensionManager getDefault() {
		if (instance == null) {
			instance = new EFSExtensionManager();
		}
		return instance;
	}

	/**
	 * If the EFS store represented by locationURI is backed by a physical file, gets the path corresponding
	 * to the underlying file.  The path returned is suitable for use in constructing a {@link Path} object.  This
	 * method will return the corresponding path regardless of whether or not the EFS store actually exists.
	 * 
	 * 
	 * @param locationURI
	 * @return String representing the path, or <code>null</code> if there is an error or if the store
	 * is not backed by a physical file.
	 */
	public String getPathFromURI(URI locationURI) {
		EFSExtensionProvider provider = fSchemeToExtensionProviderMap.get(locationURI.getScheme());

		if (provider == null) {
			provider = fDefaultProvider;
		}

		return provider.getPathFromURI(locationURI);

	}

	/**
	 * In the case of a virtual filesystem, where URIs in the given filesystem are just soft links in EFS to
	 * URIs in other filesystems, returns the URI that this URI links to. If the filesystem is not virtual,
	 * then this method acts as an identity mapping.
	 * 
	 * @param locationURI
	 * @return A URI corresponding to the linked store, or <code>null</code> on error.
	 */
	public URI getLinkedURI(URI locationURI) {
		EFSExtensionProvider provider = fSchemeToExtensionProviderMap.get(locationURI.getScheme());

		if (provider == null) {
			provider = fDefaultProvider;
		}

		return provider.getLinkedURI(locationURI);

	}

	/**
	 * Creates a new URI which clones the contents of the original URI, but with the path replaced by the
	 * given path, such that calling getPathFromURI() on the returned URI will return the given path. Returns
	 * null on error.
	 * 
	 * @param locationOnSameFilesystem
	 * @param path
	 * @return the new URI, or <code>null</code> on error
	 */
	public URI createNewURIFromPath(URI locationOnSameFilesystem, String path) {
		URI uri = locationOnSameFilesystem;
		EFSExtensionProvider provider = fSchemeToExtensionProviderMap.get(uri.getScheme());

		if (provider == null) {
			return fDefaultProvider.createNewURIFromPath(uri, path);
		}
		else {
			return provider.createNewURIFromPath(uri, path);
		}
	}

	/**
	 * For filesystems that map the path to a physical file in one filesystem (say on a remote machine) to
	 * another path (say, on the local machine), this method returns the path that the store maps to. I.e., it
	 * returns the path that the path returned by getPathFromURI(URI locationURI) maps to. If there is no such
	 * mapping, then an identity mapping of the paths is assumed.
	 * 
	 * Typically if a filesystem maps one filesytem to another, it will place the mapped path in the path
	 * field of its URIs (which the default implementation assumes), but this is not guaranteed to be so for
	 * all filesystem implementations.
	 * 
	 * @return String representing the path, or <code>null</code> on error.
	 */
	public String getMappedPath(URI locationURI) {
		URI uri = locationURI;
		EFSExtensionProvider provider = fSchemeToExtensionProviderMap.get(uri.getScheme());

		if (provider == null) {
			provider = fDefaultProvider;
		}

		return provider.getMappedPath(uri);
	}

	/**
	 * Returns true if the given URI is part of a virtual filesystem and thus points to another underlying
	 * URI. Returns false otherwise. By default, filesystems are assumed to be non-virtual.
	 * 
	 * @param locationURI
	 * @return boolean
	 */
	public boolean isVirtual(URI locationURI) {
		EFSExtensionProvider provider = fSchemeToExtensionProviderMap.get(locationURI
				.getScheme());

		if (provider == null) {
			provider = fDefaultProvider;
		}
		
		return provider.isVirtual(locationURI);
	}
	
	/**
	 * Creates a new URI with the same components as the baseURI, except that calling
	 * getPathFromURI() on the new URI will return a path that has the extension appended to 
	 * the path returned by baseURI.getPathFromURI()
	 * 
	 * @param baseURI
	 * @param extension
	 * @return the new URI, or <code>null</code> on error.
	 */
	public URI append(URI baseURI, String extension) {
		EFSExtensionProvider provider = fSchemeToExtensionProviderMap.get(baseURI
				.getScheme());

		if (provider == null) {
			provider = fDefaultProvider;
		}
		
		return provider.append(baseURI, extension);
	}

}
