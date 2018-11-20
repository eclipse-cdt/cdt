/*******************************************************************************
 * Copyright (c) 2011, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Greg Watson (IBM Corporation) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.utils.UNCPathConverter;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;

/**
 * UNCPathConverter that combines all registered convertes.
 */
public class UNCPathConverterImpl extends UNCPathConverter {
	private static String CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$
	private static String EXTENSION_POINT = "org.eclipse.cdt.core.UNCPathConverter"; //$NON-NLS-1$

	private static UNCPathConverterImpl fInstance = new UNCPathConverterImpl();

	public static UNCPathConverterImpl getInstance() {
		return fInstance;
	}

	private volatile List<UNCPathConverter> fUNCPathConverters = null;

	private UNCPathConverterImpl() {
	}

	private void loadUNCPathConverters() {
		if (fUNCPathConverters == null) {
			ArrayList<UNCPathConverter> list = new ArrayList<>();

			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extensionPoint = registry.getExtensionPoint(EXTENSION_POINT);
			if (extensionPoint != null) {
				for (IExtension ext : extensionPoint.getExtensions()) {
					for (IConfigurationElement ce : ext.getConfigurationElements()) {
						if (ce.getAttribute(CLASS_ATTRIBUTE) != null) {
							try {
								UNCPathConverter converter = (UNCPathConverter) ce
										.createExecutableExtension(CLASS_ATTRIBUTE);
								list.add(converter);
							} catch (Exception e) {
								CCorePlugin.log(e);
							}
						}
					}
				}
			}
			fUNCPathConverters = list;
		}
	}

	@Override
	public URI toURI(IPath path) {
		if (path.isUNC()) {
			loadUNCPathConverters();
			for (UNCPathConverter converter : fUNCPathConverters) {
				URI uri = converter.toURI(path);
				if (uri != null) {
					return uri;
				}
			}
		}
		return URIUtil.toURI(path);
	}

	@Override
	public URI toURI(String path) {
		if (isUNC(path)) {
			loadUNCPathConverters();
			for (UNCPathConverter converter : fUNCPathConverters) {
				URI uri = converter.toURI(path);
				if (uri != null) {
					return uri;
				}
			}
		}
		return URIUtil.toURI(path);
	}
}
