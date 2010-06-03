/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.extension.ICProjectConverter;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;

public class CProjectConverterDesciptor {
	final static String PROJECT_CONVERTER_EXTPOINT_ID = CCorePlugin.PLUGIN_ID + "." + "projectConverter";	//$NON-NLS-1$	//$NON-NLS-2$
	final static String CONVERTER = "converter";	//$NON-NLS-1$
	final static String CLASS = "class";	//$NON-NLS-1$
	final static String NATURES = "natures";	//$NON-NLS-1$
	final static String OWNERS = "owners";	//$NON-NLS-1$
	final static String DELIMITER = ";";	//$NON-NLS-1$

	private String fId;
	private ICProjectConverter fConverter;
	private IExtension fExtension;
	private IConfigurationElement fConverterElement;
	
	CProjectConverterDesciptor(IExtension extension){
		fId = extension.getUniqueIdentifier();
		fExtension = extension;
	}
	
	public ICProjectConverter getConverter() throws CoreException{
		if(fConverter == null) {
			fConverter = createConverter();
		}
		return fConverter;
	}

	private static IConfigurationElement getConverterElement(IExtension ext){
		IConfigurationElement elements[] = ext.getConfigurationElements();
		for(int i = 0; i < elements.length; i++){
			IConfigurationElement element = elements[i];
			if(CONVERTER.equals(element.getName())){
				return element;
			}
		}
		return null;
	}
	
	private IConfigurationElement getConverterElement(){
		if(fConverterElement == null){
			fConverterElement = getConverterElement(fExtension);
		}
		return fConverterElement;
	}
	
	private ICProjectConverter createConverter() throws CoreException{
		IConfigurationElement element = getConverterElement();
		if(element != null){
			Object obj = element.createExecutableExtension(CLASS);
			if(obj instanceof ICProjectConverter){
				return (ICProjectConverter)obj;
			} else
				throw ExceptionFactory.createCoreException(SettingsModelMessages.getString("CProjectConverterDesciptor.0")); //$NON-NLS-1$
		}
		throw ExceptionFactory.createCoreException(SettingsModelMessages.getString("CProjectConverterDesciptor.1")); //$NON-NLS-1$
	}
	
	public String getId(){
		return fId;
	}

	public boolean canConvertProject(IProject project, String oldOwnerId, ICProjectDescription des){
		if(getConverterElement() == null)
			return false;
		
		String ids[] = getSupportedOwnerIds();
		if(ids != null ){
			if(!idsContain(ids, oldOwnerId))
				return false;
		}
		
		try {
			String supportedIds[] = getSupportedNatureIds();
			if(supportedIds != null){
				IProjectDescription eDes = project.getDescription();
				String natures[] = eDes.getNatureIds();
				Set<String> natureSet = new HashSet<String>(Arrays.asList(natures));
				natureSet.removeAll(Arrays.asList(supportedIds));
				if(natureSet.size() == natures.length)
					return false;
			}
		} catch (CoreException e1) {
			CCorePlugin.log(e1);
		}
		
		try {
			ICProjectConverter converter = getConverter();
			return converter.canConvertProject(project, oldOwnerId, des);
		} catch (CoreException e) {
		}
		return false;
	}
	
	public String[] getSupportedOwnerIds(){
		return getIds(OWNERS);
	}

	public String[] getSupportedNatureIds(){
		return getIds(NATURES);
	}
	
	private String[] getIds(String attribute){
		IConfigurationElement element = getConverterElement();
		if(element == null)
			return null;
		
		String value = element.getAttribute(attribute);
		return CDataUtil.stringToArray(value, DELIMITER);
	}
	
	private boolean idsContain(String[] ids, String id){
		if(ids == null || ids.length == 0)
			return id == null;
		
		for(int i = 0; i < ids.length; i++){
			if(ids[i].equals(id))
				return true;
		}
		
		return false;
	}

	
}
