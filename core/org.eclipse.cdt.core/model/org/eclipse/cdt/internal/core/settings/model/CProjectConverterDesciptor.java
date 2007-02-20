/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.extension.ICProjectConverter;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;

public class CProjectConverterDesciptor {
	final static String PROJECT_CONVERTER_EXTPOINT_ID = CCorePlugin.PLUGIN_ID + "." + "projectConverter";	//$NON-NLS-1$	//$NON-NLS-2$
	final static String CONVERTER = "converter";	//$NON-NLS-1$
	final static String CLASS = "class";	//$NON-NLS-1$

	private String fId;
	private ICProjectConverter fConverter;
	private IExtension fExtension;
	
	CProjectConverterDesciptor(IExtension extension){
		fId = extension.getUniqueIdentifier();
		fExtension = extension;
	}
	
	public ICProjectConverter getConverter() throws CoreException{
		if(fConverter == null) {
			fConverter = createConverter(fExtension);
		}
		return fConverter;
	}
	
	private static ICProjectConverter createConverter(IExtension ext) throws CoreException{
		IConfigurationElement elements[] = ext.getConfigurationElements();
		for(int i = 0; i < elements.length; i++){
			IConfigurationElement element = elements[i];
			if(CONVERTER.equals(element.getName())){
				Object obj = element.createExecutableExtension(CLASS);
				if(obj instanceof ICProjectConverter){
					return (ICProjectConverter)obj;
				} else
					throw ExceptionFactory.createCoreException("illegal provider implementation");
			}
		}
		throw ExceptionFactory.createCoreException("no provider defined");
	}
	
	public String getId(){
		return fId;
	}

	public boolean canConvertProject(IProject project, String oldOwnerId, ICProjectDescription des){
		try {
			ICProjectConverter converter = getConverter();
			return converter.canConvertProject(project, oldOwnerId, des);
		} catch (CoreException e) {
		}
		return false;
	}
}
