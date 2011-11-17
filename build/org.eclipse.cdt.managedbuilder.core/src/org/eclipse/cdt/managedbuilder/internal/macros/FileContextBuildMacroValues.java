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
import java.util.Map.Entry;
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

	private HashMap<String, String> fValues = new HashMap<String, String>();
	private HashMap<String, String> fAllValues = new HashMap<String, String>();
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
					String value = fValues.get(names[i]);
					if(value == null)
						value = supperValues.getMacroValue(names[i]);
					if(value != null && value.length() > 0)
						fAllValues.put(names[i],value);
				}
			} else {
				Set<Entry<String, String>> entrySet = fValues.entrySet();
				for (Entry<String, String> entry : entrySet) {
					String value = entry.getValue();
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
	@Override
	public String[] getSupportedMacros() {
		load();
		Set<String> set = fAllValues.keySet();
		String names[] = set.toArray(new String[set.size()]);
		return names;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.macros.IFileContextBuildMacroValues#getMacroValue(java.lang.String)
	 */
	@Override
	public String getMacroValue(String macroName) {
		load();
		return fAllValues.get(macroName);
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
	@SuppressWarnings("unchecked")
	@Override
	public Object clone(){
		FileContextBuildMacroValues cloned = null;
		try{
			cloned = (FileContextBuildMacroValues)super.clone();
			cloned.fValues = (HashMap<String, String>)fValues.clone();
			cloned.fAllValues = (HashMap<String, String>)fAllValues.clone();
		} catch (CloneNotSupportedException e){
		}

		return cloned;
	}
}
