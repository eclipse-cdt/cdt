/*******************************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.macros;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacro;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class represents the set of Build Macros that could be loaded
 * and stored in XML
 * 
 * @since 3.0
 *
 */
public class StorableMacros {
	public static final String MACROS_ELEMENT_NAME = "macros"; //$NON-NLS-1$
	public static final String EXPAND_ENVIRONMENT_MACROS = "expandEnvironmentMacros"; //$NON-NLS-1$
	public static final String TRUE = "true"; //$NON-NLS-1$
	private Map fMacros;
	private boolean fExpandInMakefile = false;
	private boolean fIsDirty = false;
	private boolean fIsChanged = false;
	
	private Map getMap(){
		if(fMacros == null)
			fMacros = new HashMap();
		return fMacros;
	}
	
	public StorableMacros() {

	}

	public StorableMacros(Element element) {
		load(element);
	}
	
	private void load(Element element){
		fExpandInMakefile = TRUE.equals(element.getAttribute(EXPAND_ENVIRONMENT_MACROS));
		
		NodeList nodeList = element.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); ++i) {
			Node node = nodeList.item(i);
			String name = node.getNodeName(); 
			if (StorableBuildMacro.STRING_MACRO_ELEMENT_NAME.equals(name)) {
				addMacro(new StorableBuildMacro((Element)node));
			}
			else if (StorableBuildMacro.STRINGLIST_MACRO_ELEMENT_NAME.equals(name)) {
				addMacro(new StorableBuildMacro((Element)node));
			}
		}
		fIsDirty = false;
		fIsChanged = false;
	}
	
	public void serialize(Document doc, Element element){
		if(fExpandInMakefile)
			element.setAttribute(EXPAND_ENVIRONMENT_MACROS,TRUE);
		
		if(fMacros != null){
			Iterator iter = fMacros.values().iterator();
			while(iter.hasNext()){
				StorableBuildMacro macro = (StorableBuildMacro)iter.next();
				Element macroEl;
				if(MacroResolver.isStringListMacro(macro.getMacroValueType()))
					macroEl = doc.createElement(StorableBuildMacro.STRINGLIST_MACRO_ELEMENT_NAME);
				else
					macroEl = doc.createElement(StorableBuildMacro.STRING_MACRO_ELEMENT_NAME);
				element.appendChild(macroEl);
				macro.serialize(doc,macroEl);
			}
		}
		fIsDirty = false;
	}

	private void addMacro(IBuildMacro macro){
		String name = macro.getName();
		if(name == null)
			return;
		
		getMap().put(name,macro);
	}

	public IBuildMacro createMacro(String name, int type, String value){
		if(name == null || "".equals(name = name.trim()) || MacroResolver.isStringListMacro(type)) //$NON-NLS-1$
			return null;

		StorableBuildMacro macro = new StorableBuildMacro(name, type, value);
		addMacro(macro);
		fIsDirty = true;
		fIsChanged = true;
		return macro;
	}

	public IBuildMacro createMacro(IBuildMacro copy){
		String name = copy.getName();
		if(name == null || "".equals(name = name.trim())) //$NON-NLS-1$
			return null;

		int type = copy.getMacroValueType();
		
		StorableBuildMacro macro = null;
		try{
			if(MacroResolver.isStringListMacro(type)){
				String value[] = copy.getStringListValue();
				macro = new StorableBuildMacro(name, type, value);
			}
			else {
				String value = copy.getStringValue();
				macro = new StorableBuildMacro(name, type, value);
			}
			addMacro(macro);
			fIsDirty = true;
			fIsChanged = true;

		}catch(BuildMacroException e){
		}
		return macro;
	}

	public IBuildMacro createMacro(String name, int type, String value[]){
		if(name == null || "".equals(name = name.trim()) || !MacroResolver.isStringListMacro(type)) //$NON-NLS-1$
			return null;

		StorableBuildMacro macro = new StorableBuildMacro(name, type, value);
		addMacro(macro);
		fIsDirty = true;
		fIsChanged = true;
		return macro;
	}

	/**
	 * Returns the "dirty" state for this set of macros.
	 * If the dirty state is <code>true</code>, that means that the macros 
	 * is out of synch with the repository and the macros need to be serialized.
	 * <br><br>
	 * The dirty state is automatically set to <code>false</code> when the macros are serialized
	 * by calling the serialize() method  
	 * @return boolean 
	 */
	public boolean isDirty(){
		return fIsDirty;
	}
	
	/**
	 * sets the "dirty" state for this set of macros.
	 * @see org.eclipse.cdt.managedbuilder.internal.macros.StorableMacros#isDirty()
	 * @param dirty represents the new state
	 */
	public void setDirty(boolean dirty){
		fIsDirty = dirty;
	}
	
	/**
	 * Returns the "change" state for this set of macros.
	 * The "change" state represents whether the macros were changed or not.
	 * This state is not reset when the serialize() method is called
	 * Users can use this state to monitor whether the macros were changed or not.
	 * The "change" state can be reset only by calling the setChanged(false) method 
	 * @return boolean
	 */
	public boolean isChanged(){
		return fIsChanged;
	}
	
	public boolean isExpanded(){
		return fExpandInMakefile;
	}
	
	public void setExpanded(boolean expand){
		if(fExpandInMakefile != expand){
			fExpandInMakefile = expand;
			fIsDirty = true;
			//should we set the change state here?
			fIsChanged = true;
		}
	}
	
	/**
	 * sets the "change" state for this set of macros.
	 * @see org.eclipse.cdt.managedbuilder.internal.macros.StorableMacros#isChanged()
	 * @param changed represents the new "change" state
	 */
	public void setChanged(boolean changed){
		fIsChanged = changed;
	}

	public IBuildMacro getMacro(String name){
		if(name == null || "".equals(name = name.trim())) //$NON-NLS-1$
			return null;
		
		return (IBuildMacro)getMap().get(name);
	}
	
	public IBuildMacro[] getMacros(){
		Collection macros = getMap().values();
		
		return (IBuildMacro[])macros.toArray(new IBuildMacro[macros.size()]);
	}
	
	IBuildMacro deleteMacro(String name){
		if(name == null || "".equals(name = name.trim())) //$NON-NLS-1$
			return null;

		IBuildMacro macro = (IBuildMacro)getMap().remove(name);
		if(macro != null){
			fIsDirty = true;
			fIsChanged = true;
		}

		return macro;
	}
	
	public boolean deleteAll(){
		Map map = getMap();
		if(map.size() > 0){
			fIsDirty = true;
			fIsChanged = true;
			map.clear();
			return true;
		}
		return false;
	}
}
