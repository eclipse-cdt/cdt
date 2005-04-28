/**********************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.internal.envvar;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableSupplier;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * The default implementation of the IContextInfo used by the Environment Variable Provider
 * Used to represent the Configuration, Project, Workspace and Eclipse environment contexts
 * 
 * @since 3.0
 */
public class DefaultContextInfo implements IContextInfo{
	private Object fContextObject;
	private IEnvironmentVariableSupplier fContextSuppliers[];
	
	/**
	 * This constructor is used to create the default context info given a context object
	 * 
	 * @param context
	 */
	public DefaultContextInfo(Object context){
		fContextObject = context;
	}
	
	protected DefaultContextInfo(Object context, IEnvironmentVariableSupplier suppliers[]){
		fContextSuppliers = suppliers;
		fContextObject = context;
	}
	
	/*
	 * answers the list of suppliers that should be used for the given context 
	 */
	protected IEnvironmentVariableSupplier[] getSuppliers(Object context){
		IEnvironmentVariableSupplier suppliers[];
		if(context == null)
			suppliers = new IEnvironmentVariableSupplier[]{EnvironmentVariableProvider.fEclipseSupplier};
		else if(context instanceof IWorkspace)
			suppliers = new IEnvironmentVariableSupplier[]{EnvironmentVariableProvider.fUserSupplier};
		else if(context instanceof IManagedProject)
			suppliers = new IEnvironmentVariableSupplier[]{EnvironmentVariableProvider.fUserSupplier,EnvironmentVariableProvider.fExternalSupplier};
		else if(context instanceof IConfiguration)
			suppliers = new IEnvironmentVariableSupplier[]{EnvironmentVariableProvider.fUserSupplier,EnvironmentVariableProvider.fExternalSupplier,EnvironmentVariableProvider.fMbsSupplier};
		else
			suppliers = null;
		return suppliers;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.envvar.IContextInfo#getNext()
	 */
	public IContextInfo getNext(){
		DefaultContextInfo next = null;
		if(fContextObject == null)
			next = null;
		else if(fContextObject instanceof IWorkspace)
			next = new DefaultContextInfo(null);
		else if(fContextObject instanceof IManagedProject)
			next = new DefaultContextInfo(ResourcesPlugin.getWorkspace());
		else if(fContextObject instanceof IConfiguration)
			next = new DefaultContextInfo(((IConfiguration)fContextObject).getManagedProject());
		else
			next = null;

		if(next != null && next.getSuppliers() == null)
			next = null;
		return next;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.envvar.IContextInfo#getSuppliers()
	 */
	public IEnvironmentVariableSupplier[] getSuppliers(){
		if(fContextSuppliers == null)
			fContextSuppliers = getSuppliers(fContextObject);
		return fContextSuppliers;
	}
	
	protected void setSuppliers(IEnvironmentVariableSupplier suppliers[]){
		fContextSuppliers = suppliers;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.envvar.IContextInfo#getContext()
	 */
	public Object getContext(){
		return fContextObject;
	}
}
