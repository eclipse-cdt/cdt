/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.core;

import java.lang.reflect.Constructor;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.remote.internal.core.RemoteCorePlugin;

/**
 * Abstract base class for remote connection managers.
 * 
 * @since 8.0
 */
public abstract class AbstractRemoteConnectionManager implements IRemoteConnectionManager {
	private static final String AUTHENTICATOR_EXTENSION_POINT_ID = "authenticator"; //$NON-NLS-1$
	private static final String ID_ATTR = "id"; //$NON-NLS-1$
	private static final String CLASS_ATTR = "class"; //$NON-NLS-1$
	private static final String PRIORITY_ATTR = "priority"; //$NON-NLS-1$

	private final IRemoteServices fRemoteServices;
	private boolean fLoaded;
	private Constructor<?> fUserAuthenticatorConstructor;

	public AbstractRemoteConnectionManager(IRemoteServices services) {
		fRemoteServices = services;
	}

	protected IRemoteServices getRemoteServices() {
		return fRemoteServices;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteConnectionManager#getUserAuthenticator()
	 */
	@Override
	public IUserAuthenticator getUserAuthenticator(IRemoteConnection connection) {
		if (!fLoaded) {
			int currPriority = -1;
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extensionPoint = registry.getExtensionPoint(RemoteCorePlugin.getUniqueIdentifier(),
					AUTHENTICATOR_EXTENSION_POINT_ID);
			final IExtension[] extensions = extensionPoint.getExtensions();

			for (IExtension ext : extensions) {
				final IConfigurationElement[] elements = ext.getConfigurationElements();

				for (IConfigurationElement ce : elements) {
					String id = ce.getAttribute(ID_ATTR);
					if (id.equals(getRemoteServices().getId())) {
						int priority = 0;
						String priorityAttr = ce.getAttribute(PRIORITY_ATTR);
						if (priorityAttr != null) {
							try {
								priority = Integer.parseInt(priorityAttr);
							} catch (NumberFormatException e) {
								// Assume default
							}
						}
						if (priority > currPriority) {
							try {
								String widgetClass = ce.getAttribute(CLASS_ATTR);
								Class<?> cls = Platform.getBundle(ce.getDeclaringExtension().getContributor().getName()).loadClass(
										widgetClass);
								if (cls != null) {
									fUserAuthenticatorConstructor = cls.getConstructor(IRemoteConnection.class);
									currPriority = priority;
								}
							} catch (ClassNotFoundException | NoSuchMethodException e) {
								RemoteCorePlugin.log(e);
							}
						}
					}
				}
			}
			fLoaded = true;
		}
		if (fUserAuthenticatorConstructor != null) {
			try {
				return (IUserAuthenticator) fUserAuthenticatorConstructor.newInstance(connection);
			} catch (Exception e) {
				RemoteCorePlugin.log(e);
			}
		}
		return null;
	}
}