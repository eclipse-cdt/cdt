/*******************************************************************************
 * Copyright (c) 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationDataProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;

public class CConfigurationDataProviderDescriptor {
	final static String DATA_PROVIDER_EXTPOINT_ID = CCorePlugin.PLUGIN_ID + "." + "CConfigurationDataProvider";	//$NON-NLS-1$	//$NON-NLS-2$
	final static String DATA_PROVIDER = "provider";	//$NON-NLS-1$
	final static String CLASS = "class";	//$NON-NLS-1$
	final static String NATURES = "natures";	//$NON-NLS-1$
	final static String CONFLICTING_NATURES = "conflictingNatures";	//$NON-NLS-1$
	final static String ECLIPSE_BUILDERS = "eclipseBuilders";	//$NON-NLS-1$
	final static String DELIMITER = ";"; //$NON-NLS-1$
	

	private String fId;
	private CConfigurationDataProvider fProvider;
	private IExtension fExtension;
	private IConfigurationElement fProviderElement;
//	private String[] fBuilderIds;
	private String[] fNatureIds;
	private String[] fConflictingNatureIds;
	
	public CConfigurationDataProviderDescriptor(IExtension extension){
		fId = extension.getUniqueIdentifier();
		fExtension = extension;
		fProviderElement = getProviderElement(extension);
		if(fProviderElement != null){
			fNatureIds = toArray(fProviderElement.getAttribute(NATURES));
			fConflictingNatureIds = toArray(fProviderElement.getAttribute(CONFLICTING_NATURES));
//			fBuilderIds = toArray(fProviderElement.getAttribute(ECLIPSE_BUILDERS));
		} else {
			//fBuilderIds = 
			fNatureIds = new String[0];
			fConflictingNatureIds = new String[0];
		}
		try {
			fProvider = createProvider(extension);
		} catch (CoreException e) {
			CCorePlugin.log(e.getStatus());
		}
	}
	
	private static String[] toArray(String value){
		if(value == null)
			return new String[0];
		
		StringTokenizer t = new StringTokenizer(value, DELIMITER);
		int num = t.countTokens();
		List<String> list = new ArrayList<String>(num);
		for(int i = 0; i < num; i++){
			String v = t.nextToken().trim();
			if(v.length() != 0)
				list.add(v);
		}
		
		return list.toArray(new String[list.size()]);
		
	}
	
	public CConfigurationDataProvider getProvider() throws CoreException{
		if(fProvider == null) {
			fProvider = createProvider(fExtension);
		}
		return fProvider;
	}
	
	private static IConfigurationElement getProviderElement(IExtension ext){
		IConfigurationElement elements[] = ext.getConfigurationElements();
		for(int i = 0; i < elements.length; i++){
			IConfigurationElement element = elements[i];
			if(DATA_PROVIDER.equals(element.getName())){
				return element;
			}
		}
		return null;
	}

	private static CConfigurationDataProvider createProvider(IExtension ext) throws CoreException{
		IConfigurationElement elements[] = ext.getConfigurationElements();
		for(int i = 0; i < elements.length; i++){
			IConfigurationElement element = elements[i];
			if(DATA_PROVIDER.equals(element.getName())){
				Object obj = element.createExecutableExtension(CLASS);
				if(obj instanceof CConfigurationDataProvider){
					return (CConfigurationDataProvider)obj;
				} else
					throw ExceptionFactory.createCoreException(SettingsModelMessages.getString("CConfigurationDataProviderDescriptor.1")); //$NON-NLS-1$
			}
		}
		throw ExceptionFactory.createCoreException(SettingsModelMessages.getString("CConfigurationDataProviderDescriptor.2")); //$NON-NLS-1$
	}
	
	public String getId(){
		return fId;
	}
	
	public String[] getNatureIds(){
		return fNatureIds.clone();
	}

	public String[] getConflictingNatureIds(){
		return fConflictingNatureIds.clone();
	}

/*	public String[] getBuilderIds(){
		return (String[])fBuilderIds.clone();
	}
*/
}
