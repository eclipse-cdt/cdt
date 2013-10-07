/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.remote.internal.core;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.osgi.util.NLS;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.core.IRemoteServicesDescriptor;
import org.eclipse.remote.core.IRemoteServicesFactory;
import org.eclipse.remote.internal.core.messages.Messages;

public class RemoteServicesDescriptor implements IRemoteServicesDescriptor {
	private static final String ATTR_ID = "id"; //$NON-NLS-1$
	private static final String ATTR_NAME = "name"; //$NON-NLS-1$
	private static final String ATTR_SCHEME = "scheme"; //$NON-NLS-1$
	private static final String ATTR_CLASS = "class"; //$NON-NLS-1$

	private static String getAttribute(IConfigurationElement configElement, String name, String defaultValue) {
		String value = configElement.getAttribute(name);
		if (value != null) {
			return value;
		}
		if (defaultValue != null) {
			return defaultValue;
		}
		throw new IllegalArgumentException(NLS.bind(Messages.RemoteServicesProxy_0, name));
	}

	private final IConfigurationElement fConfigElement;

	private final String fId;
	private final String fName;
	private final String fScheme;
	private IRemoteServicesFactory fFactory;
	private IRemoteServices fDelegate = null;

	public RemoteServicesDescriptor(IConfigurationElement configElement) {
		fConfigElement = configElement;
		fId = getAttribute(configElement, ATTR_ID, null);
		fName = getAttribute(configElement, ATTR_NAME, fId);
		fScheme = getAttribute(configElement, ATTR_SCHEME, null);
		getAttribute(configElement, ATTR_CLASS, null);
		fFactory = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(IRemoteServicesDescriptor arg0) {
		return getName().compareTo(arg0.getName());
	}

	/**
	 * Get the factory from the plugin
	 * 
	 * @return instance of the factory
	 */
	public IRemoteServicesFactory getFactory() {
		if (fFactory != null) {
			return fFactory;
		}
		try {
			fFactory = (IRemoteServicesFactory) fConfigElement.createExecutableExtension(ATTR_CLASS);
		} catch (Exception e) {
			RemoteCorePlugin.log(NLS.bind(Messages.RemoteServicesProxy_1, new Object[] { fConfigElement.getAttribute(ATTR_CLASS),
					fId, fConfigElement.getDeclaringExtension().getNamespaceIdentifier() }));
		}
		return fFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteServices#getId()
	 */
	@Override
	public String getId() {
		return fId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteServices#getName()
	 */
	@Override
	public String getName() {
		return fName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteServices#getScheme()
	 */
	@Override
	public String getScheme() {
		return fScheme;
	}

	/**
	 * Get the remote services implementation for this descriptor. The service has not been initialized.
	 * 
	 * @return the remote services implementation
	 */
	public IRemoteServices getServices() {
		loadServices();
		return fDelegate;
	}

	/**
	 * Create the remote services factory. Note that the services will not be
	 * initialized.
	 */
	private void loadServices() {
		if (fDelegate == null) {
			IRemoteServicesFactory factory = getFactory();
			if (factory != null) {
				fDelegate = factory.getServices(this);
			}
		}
	}
}
