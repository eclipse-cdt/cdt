/*******************************************************************************
 * Copyright (c) 2005, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.envvar;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;

/**
 * The default implementation of the IContextInfo used by the Environment Variable Provider
 * Used to represent the Configuration, Project, Workspace and Eclipse environment contexts
 * 
 * @since 3.0
 */
public class DefaultEnvironmentContextInfo implements IEnvironmentContextInfo{
	private Object fContextObject;
	private ICoreEnvironmentVariableSupplier fContextSuppliers[];
	
	/**
	 * This constructor is used to create the default context info given a context object
	 * 
	 * @param context
	 */
	public DefaultEnvironmentContextInfo(Object context){
		fContextObject = context;
	}
	
	protected DefaultEnvironmentContextInfo(Object context, ICoreEnvironmentVariableSupplier suppliers[]){
		fContextSuppliers = suppliers;
		fContextObject = context;
	}
	
	/*
	 * answers the list of suppliers that should be used for the given context 
	 */
	protected ICoreEnvironmentVariableSupplier[] getSuppliers(Object context){
		ICoreEnvironmentVariableSupplier suppliers[];
		if(context instanceof ICConfigurationDescription)
			suppliers = new ICoreEnvironmentVariableSupplier[]{EnvironmentVariableManager.fUserSupplier,EnvironmentVariableManager.fExternalSupplier};
		else
			suppliers = new ICoreEnvironmentVariableSupplier[]{EnvironmentVariableManager.fUserSupplier, EnvironmentVariableManager.fEclipseSupplier};
		return suppliers;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.envvar.IContextInfo#getNext()
	 */
	public IEnvironmentContextInfo getNext(){
		DefaultEnvironmentContextInfo next = null;
		if(fContextObject instanceof ICConfigurationDescription) {
			next = new DefaultEnvironmentContextInfo(null);
			if (next.getSuppliers() == null)
				next = null;
		}
		return next;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.envvar.IContextInfo#getSuppliers()
	 */
	public ICoreEnvironmentVariableSupplier[] getSuppliers(){
		if(fContextSuppliers == null)
			fContextSuppliers = getSuppliers(fContextObject);
		return fContextSuppliers;
	}
	
	protected void setSuppliers(ICoreEnvironmentVariableSupplier suppliers[]){
		fContextSuppliers = suppliers;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.envvar.IContextInfo#getContext()
	 */
	public Object getContext(){
		return fContextObject;
	}
}
