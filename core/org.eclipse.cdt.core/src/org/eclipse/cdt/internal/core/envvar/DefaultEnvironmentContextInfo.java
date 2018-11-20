/*******************************************************************************
 * Copyright (c) 2005, 2011 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.envvar;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.core.resources.IBuildConfiguration;

/**
 * The default implementation of the IContextInfo used by the Environment Variable Provider
 * Used to represent the Configuration, Project, Workspace and Eclipse environment contexts
 *
 * @since 3.0
 */
public class DefaultEnvironmentContextInfo implements IEnvironmentContextInfo {
	private Object fContextObject;
	private ICoreEnvironmentVariableSupplier fContextSuppliers[];

	/**
	 * This constructor is used to create the default context info given a context object
	 *
	 * @param context
	 */
	public DefaultEnvironmentContextInfo(Object context) {
		fContextObject = context;
	}

	protected DefaultEnvironmentContextInfo(Object context, ICoreEnvironmentVariableSupplier suppliers[]) {
		fContextSuppliers = suppliers;
		fContextObject = context;
	}

	/*
	 * answers the list of suppliers that should be used for the given context
	 */
	protected ICoreEnvironmentVariableSupplier[] getSuppliers(Object context) {
		ICoreEnvironmentVariableSupplier suppliers[];
		if (context instanceof ICConfigurationDescription)
			suppliers = new ICoreEnvironmentVariableSupplier[] { EnvironmentVariableManager.fUserSupplier,
					EnvironmentVariableManager.fExternalSupplier };
		else if (context instanceof IBuildConfiguration)
			suppliers = new ICoreEnvironmentVariableSupplier[] { EnvironmentVariableManager.fBuildConfigSupplier,
					EnvironmentVariableManager.fToolChainSupplier, EnvironmentVariableManager.fUserSupplier };
		else
			suppliers = new ICoreEnvironmentVariableSupplier[] { EnvironmentVariableManager.fUserSupplier,
					EnvironmentVariableManager.fEclipseSupplier };
		return suppliers;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.envvar.IContextInfo#getNext()
	 */
	@Override
	public IEnvironmentContextInfo getNext() {
		DefaultEnvironmentContextInfo next = null;
		if (fContextObject instanceof ICConfigurationDescription) {
			next = new DefaultEnvironmentContextInfo(null);
			if (next.getSuppliers() == null)
				next = null;
		}
		return next;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.envvar.IContextInfo#getSuppliers()
	 */
	@Override
	public ICoreEnvironmentVariableSupplier[] getSuppliers() {
		if (fContextSuppliers == null)
			fContextSuppliers = getSuppliers(fContextObject);
		return fContextSuppliers;
	}

	protected void setSuppliers(ICoreEnvironmentVariableSupplier suppliers[]) {
		fContextSuppliers = suppliers;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.envvar.IContextInfo#getContext()
	 */
	@Override
	public Object getContext() {
		return fContextObject;
	}
}
