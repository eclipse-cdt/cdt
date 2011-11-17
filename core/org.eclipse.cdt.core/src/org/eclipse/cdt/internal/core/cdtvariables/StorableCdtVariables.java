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
package org.eclipse.cdt.internal.core.cdtvariables;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.core.cdtvariables.IStorableCdtVariables;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.internal.core.cdtvariables.UserDefinedVariableSupplier.VarKey;
import org.eclipse.cdt.internal.core.settings.model.ExceptionFactory;
import org.eclipse.cdt.utils.cdtvariables.CdtVariableResolver;

/**
 * This class represents the set of Build Macros that could be loaded
 * and stored in XML
 *
 * @since 3.0
 *
 */
public class StorableCdtVariables implements IStorableCdtVariables {
	public static final String MACROS_ELEMENT_NAME = "macros"; //$NON-NLS-1$
	private HashMap<String, ICdtVariable> fMacros;
	private boolean fIsDirty = false;
	private boolean fIsChanged = false;
	private boolean fIsReadOnly;

	private HashMap<String, ICdtVariable> getMap(){
		if(fMacros == null)
			fMacros = new HashMap<String, ICdtVariable>();
		return fMacros;
	}

	public StorableCdtVariables(boolean readOnly) {
		fIsReadOnly = readOnly;
	}

	@SuppressWarnings("unchecked")
	public StorableCdtVariables(StorableCdtVariables base, boolean readOnly) {
		fMacros = (HashMap<String, ICdtVariable>)base.getMap().clone();
		fIsReadOnly = readOnly;
	}

	public StorableCdtVariables(ICdtVariable vars[], boolean readOnly) {
		fMacros = new HashMap<String, ICdtVariable>(vars.length);
		for (ICdtVariable var : vars) {
			addMacro(var);
		}
		fIsReadOnly = readOnly;
	}

	public StorableCdtVariables(ICStorageElement element, boolean readOnly) {
		load(element);
		fIsReadOnly = readOnly;
	}

	private void load(ICStorageElement element){
//		fExpandInMakefile = TRUE.equals(element.getAttribute(EXPAND_ENVIRONMENT_MACROS));

		ICStorageElement nodeList[] = element.getChildren();
		for (int i = 0; i < nodeList.length; ++i) {
			ICStorageElement node = nodeList[i];
			String name = node.getName();
			if (StorableCdtVariable.STRING_MACRO_ELEMENT_NAME.equals(name)) {
				addMacro(new StorableCdtVariable(node));
			}
			else if (StorableCdtVariable.STRINGLIST_MACRO_ELEMENT_NAME.equals(name)) {
				addMacro(new StorableCdtVariable(node));
			}
		}
		fIsDirty = false;
		fIsChanged = false;
	}

	public void serialize(ICStorageElement element){
		if(fMacros != null){
			for (ICdtVariable v : fMacros.values()){
				StorableCdtVariable macro = (StorableCdtVariable)v;
				ICStorageElement macroEl;
				if(CdtVariableResolver.isStringListVariable(macro.getValueType()))
					macroEl = element.createChild(StorableCdtVariable.STRINGLIST_MACRO_ELEMENT_NAME);
				else
					macroEl = element.createChild(StorableCdtVariable.STRING_MACRO_ELEMENT_NAME);
				macro.serialize(macroEl);
			}
		}
		fIsDirty = false;
	}

	private void addMacro(ICdtVariable macro){
		String name = macro.getName();
		if(name == null)
			return;

		getMap().put(name,macro);
	}

	@Override
	public ICdtVariable createMacro(String name, int type, String value){
		if(name == null || "".equals(name = name.trim()) || CdtVariableResolver.isStringListVariable(type)) //$NON-NLS-1$
			return null;

		ICdtVariable macro = checkMacro(name, type, value);
		if(macro == null){
			macro = new StorableCdtVariable(name, type, value);
			addMacro(macro);
			fIsDirty = true;
			fIsChanged = true;
		}
		return macro;
	}

	public ICdtVariable checkMacro(String name, int type, String value){
		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		ICdtVariable macro = getMacro(name);
		if(macro != null){
			if(macro.getName().equals(name)
					&& macro.getValueType() == type){
				try {
					String val = macro.getStringValue();
					if((val != null
								&& val.equals(value))
							|| val == value){
						return macro;
					}
				} catch (CdtVariableException e) {
				}
			}
		}
		return null;
	}

	public ICdtVariable checkMacro(String name, int type, String value[]){
		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		ICdtVariable macro = getMacro(name);
		if(macro != null){
			if(macro.getName().equals(name)
					&& macro.getValueType() == type){
				try {
					String val[] = macro.getStringListValue();
					if(val != null){
						if(value != null && value.length == val.length){
							int i;
							for(i = 0; i < val.length; i++){
								if(!value[i].equals(val[i]))
									break;
							}
							if(i == value.length)
								return macro;
						}
					} else if (value == val){
						return macro;
					}
				} catch (CdtVariableException e) {
				}
			}
		}
		return null;
	}

	/*
	 * sets the storable macros to hold the geven number of macros
	 * all macros that are present in the store but not included in the given array
	 * will be removed
	 */
	public void setMacros(ICdtVariable macros[]){
		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		if(macros == null || macros.length == 0)
			deleteAll();
		else{
			if (getMap().size() != 0) {
				for (ICdtVariable m : getMap().values()){
					int i;
					for(i = 0 ; i < macros.length; i++){
						if(m.getName().equals(macros[i].getName()))
							break;
					}
					if(i == macros.length)
						deleteMacro(m.getName());
				}
			}
			createMacros(macros);
		}
	}

	@Override
	public void createMacros(ICdtVariable macros[]){
		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		for (ICdtVariable macro : macros) {
			createMacro(macro);
		}
	}

	@Override
	public boolean isEmpty(){
		return fMacros == null || fMacros.isEmpty();
	}

	@Override
	public ICdtVariable createMacro(ICdtVariable copy){
		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		String name = copy.getName();
		if(name == null || "".equals(name = name.trim())) //$NON-NLS-1$
			return null;

		int type = copy.getValueType();

		ICdtVariable macro = null;
		try{
			if(CdtVariableResolver.isStringListVariable(type)){
				String value[] = copy.getStringListValue();
				macro = checkMacro(name, type, value);
				if(macro == null){
					macro = new StorableCdtVariable(name, type, value);
					addMacro(macro);
					fIsDirty = true;
					fIsChanged = true;
				}
			}
			else {
				String value = copy.getStringValue();
				macro = checkMacro(name, type, value);
				if(macro == null){
					macro = new StorableCdtVariable(name, type, value);
					addMacro(macro);
					fIsDirty = true;
					fIsChanged = true;
				}
			}

		}catch(CdtVariableException e){
		}
		return macro;
	}

	@Override
	public ICdtVariable createMacro(String name, int type, String value[]){
		if(name == null || "".equals(name = name.trim()) || !CdtVariableResolver.isStringListVariable(type)) //$NON-NLS-1$
			return null;

		ICdtVariable macro = checkMacro(name, type, value);
		if(macro == null){
			macro = new StorableCdtVariable(name, type, value);
			addMacro(macro);
			fIsDirty = true;
			fIsChanged = true;
		}
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
	 * @see org.eclipse.cdt.internal.core.cdtvariables.StorableCdtVariables#isDirty()
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
	@Override
	public boolean isChanged(){
		return fIsChanged;
	}

/*	public boolean isExpanded(){
		return fExpandInMakefile;
	}
*/
/*	public void setExpanded(boolean expand){
		if(fExpandInMakefile != expand){
			fExpandInMakefile = expand;
			fIsDirty = true;
			//should we set the change state here?
			fIsChanged = true;
		}
	}
*/
	/**
	 * sets the "change" state for this set of macros.
	 * @see org.eclipse.cdt.internal.core.cdtvariables.StorableCdtVariables#isChanged()
	 * @param changed represents the new "change" state
	 */
	public void setChanged(boolean changed){
		fIsChanged = changed;
	}

	@Override
	public ICdtVariable getMacro(String name){
		if(name == null || "".equals(name = name.trim())) //$NON-NLS-1$
			return null;

		ICdtVariable var = getMap().get(name);
		if(var == null){
			int indx = name.indexOf(':');
			if(indx != -1){
				String baseName = name.substring(0, indx);
				ICdtVariable tmp = getMap().get(baseName);
				if(tmp != null
						&& CdtVariableManager.getDefault().toEclipseVariable(tmp, null) != null){
					var = EclipseVariablesVariableSupplier.getInstance().getVariable(name);
				}
			}
		}
		return var;
	}

	@Override
	public ICdtVariable[] getMacros(){
		Collection<ICdtVariable> macros = getMap().values();
		return macros.toArray(new ICdtVariable[macros.size()]);
	}

	@Override
	public ICdtVariable deleteMacro(String name){
		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();

		if(name == null || "".equals(name = name.trim())) //$NON-NLS-1$
			return null;

		ICdtVariable macro = getMap().remove(name);
		if(macro != null){
			fIsDirty = true;
			fIsChanged = true;
		}

		return macro;
	}

	@Override
	public boolean deleteAll(){
		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		Map<String, ICdtVariable> map = getMap();
		if(map.size() > 0){
			fIsDirty = true;
			fIsChanged = true;
			map.clear();
			return true;
		}
		return false;
	}

	@Override
	public boolean contains(ICdtVariable var){
		ICdtVariable curVar = getMacro(var.getName());
		if(curVar == null)
			return false;

		if(new VarKey(curVar, false).equals(new VarKey(var, false)))
			return true;

		return false;

	}
}
