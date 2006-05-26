/*******************************************************************************
 * Copyright (c) 2005, 2006 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.macros;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.macros.IFileContextBuildMacroValues;

/**
 * The implementation of the IFileContextBuildMacroValues interface
 * @since 3.0
 */
public class FileContextBuildMacroValues implements
		IFileContextBuildMacroValues, Cloneable {
	
	private IBuilder fBuilder;
	private IFileContextBuildMacroValues fSupperClassValues;

	private HashMap fValues = new HashMap();
	private HashMap fAllValues = new HashMap();
	private boolean fInitialized;
	
	public FileContextBuildMacroValues(IBuilder builder, IManagedConfigElement element){
		fBuilder = builder;
		load(element);
	}

	private void load(IManagedConfigElement element){
		String names[] = MbsMacroSupplier.getInstance().getMacroNames(IBuildMacroProvider.CONTEXT_FILE);
		fValues.clear();
		for(int i = 0; i < names.length; i++){
			String value = element.getAttribute(PREFIX + names[i] + SUFFIX);
			if(value != null)
				fValues.put(names[i],value);
		}
	}
	
	private void load(){
		if(!fInitialized){
			fAllValues.clear();
			IFileContextBuildMacroValues supperValues = getSupperClassValues();
			if(supperValues != null) {
				String names[] = MbsMacroSupplier.getInstance().getMacroNames(IBuildMacroProvider.CONTEXT_FILE);
				for(int i = 0; i < names.length; i++){
					String value = (String)fValues.get(names[i]); 
					if(value == null)
						value = supperValues.getMacroValue(names[i]);
					if(value != null && value.length() > 0)
						fAllValues.put(names[i],value);
				}
			} else {
				Iterator iter = fValues.entrySet().iterator();
				while(iter.hasNext()){
					Map.Entry entry = (Map.Entry)iter.next();
					String value = (String)entry.getValue();
					if(value != null && value.length() > 0)
						fAllValues.put(entry.getKey(),value);
				}
			}
			fInitialized = true;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IFileContextBuildMacroValues#getSupportedMacros()
	 */
	public String[] getSupportedMacros() {
		load();
		Set set = fAllValues.keySet();
		String names[] = new String[set.size()];
		Iterator iter = set.iterator();
		for(int i = 0; i < names.length; i++)
			names[i] = (String)iter.next();
		return names;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IFileContextBuildMacroValues#getMacroValue(java.lang.String)
	 */
	public String getMacroValue(String macroName) {
		load();
		return (String)fAllValues.get(macroName);
	}
	
	public IFileContextBuildMacroValues getSupperClassValues(){
		if(fBuilder != null){
			IBuilder supperClass = fBuilder.getSuperClass();
			if(supperClass != null)
				fSupperClassValues = supperClass.getFileContextBuildMacroValues();
		}
		return fSupperClassValues;
	}
	
	public void setBuilder(IBuilder builder){
		fBuilder = builder;
		fInitialized = false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public Object clone(){
		FileContextBuildMacroValues cloned = null;
		try{
			cloned = (FileContextBuildMacroValues)super.clone();
			cloned.fValues = (HashMap)fValues.clone();
			cloned.fAllValues = (HashMap)fAllValues.clone();
		} catch (CloneNotSupportedException e){
		}
		
		return cloned;
	}
}
