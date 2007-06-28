/*******************************************************************************
 * Copyright (c) 2005, 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.utils.envvar;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariableManager;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.internal.core.envvar.EnvironmentVariableManager;
import org.eclipse.cdt.internal.core.settings.model.ExceptionFactory;

/**
 * This class represents the set of environment variables that could be loaded
 * and stored in XML
 * 
 * @since 3.0
 *
 */
public class StorableEnvironment /*implements Cloneable*/{
	public static final String ENVIRONMENT_ELEMENT_NAME = "environment"; //$NON-NLS-1$
	private static final String ATTRIBUTE_APPEND = "append";  //$NON-NLS-1$
	private static final String ATTRIBUTE_APPEND_CONTRIBUTED = "appendContributed";  //$NON-NLS-1$
	private static final boolean DEFAULT_APPEND = true;
	private HashMap fVariables;
	private boolean fIsDirty = false;
	private boolean fIsChanged = false;
	private boolean fIsReadOnly;
	private boolean fAppend = DEFAULT_APPEND;
	private boolean fAppendContributedEnv = DEFAULT_APPEND;
	
	private Map getMap(){
		if(fVariables == null)
			fVariables = new HashMap();
		return fVariables;
	}

	public StorableEnvironment(IEnvironmentVariable variables[], boolean isReadOnly) {
		setVariales(variables);
		fIsReadOnly = isReadOnly;
	}

	public StorableEnvironment(boolean isReadOnly) {
		fIsReadOnly = isReadOnly;
	}

	public StorableEnvironment(StorableEnvironment env, boolean isReadOnly) {
		if(env.fVariables != null)
			fVariables = (HashMap)env.fVariables.clone();
		fAppend = env.fAppend;
		fAppendContributedEnv = env.fAppendContributedEnv;
		fIsReadOnly = isReadOnly;
		fIsDirty = env.isDirty();
	}

	public StorableEnvironment(ICStorageElement element, boolean isReadOnly) {
		load(element);
		fIsReadOnly = isReadOnly;
	}
	
	private void load(ICStorageElement element){
		ICStorageElement children[] = element.getChildren();
		for (int i = 0; i < children.length; ++i) {
			ICStorageElement node = children[i];
			if (node.getName().equals(StorableEnvVar.VARIABLE_ELEMENT_NAME)) {
				addVariable(new StorableEnvVar(node));
			}
		}
		
		String append = element.getAttribute(ATTRIBUTE_APPEND);
		fAppend = append != null ? Boolean.valueOf(append).booleanValue()
				: DEFAULT_APPEND;
		
		append = element.getAttribute(ATTRIBUTE_APPEND_CONTRIBUTED);
		fAppendContributedEnv = append != null ? Boolean.valueOf(append).booleanValue()
				: DEFAULT_APPEND;
		
		fIsDirty = false;
		fIsChanged = false;
	}
	
	public void serialize(ICStorageElement element){
		element.setAttribute(ATTRIBUTE_APPEND, Boolean.valueOf(fAppend).toString());
		element.setAttribute(ATTRIBUTE_APPEND_CONTRIBUTED, Boolean.valueOf(fAppendContributedEnv).toString());
		if(fVariables != null){
			Iterator iter = fVariables.values().iterator();
			while(iter.hasNext()){
				StorableEnvVar var = (StorableEnvVar)iter.next();
				ICStorageElement varEl = element.createChild(StorableEnvVar.VARIABLE_ELEMENT_NAME);
				var.serialize(varEl);
			}
		}
		
		fIsDirty = false;
	}

	private void addVariable(IEnvironmentVariable var){
		String name = var.getName();
		if(name == null)
			return;
		IEnvironmentVariableManager provider = EnvironmentVariableManager.getDefault();
		if(!provider.isVariableCaseSensitive())
			name = name.toUpperCase();
		
		getMap().put(name,var);
	}

	public IEnvironmentVariable createVariable(String name, String value, int op, String delimiter){
		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		
		if(name == null || "".equals(name = name.trim())) //$NON-NLS-1$
			return null;

		IEnvironmentVariable var = checkVariable(name,value,op,delimiter);
		if(var == null){
			var = new StorableEnvVar(name, value, op, delimiter);
			addVariable(var);
			fIsDirty = true;
			fIsChanged = true;
		}
		return var;
	}

	public IEnvironmentVariable createVariable(String name){
		return createVariable(name,null,IEnvironmentVariable.ENVVAR_REPLACE,null);
	}
	
	public IEnvironmentVariable createVariable(String name, String value){
		return createVariable(name,value,IEnvironmentVariable.ENVVAR_REPLACE,null);	
	}
	
	public IEnvironmentVariable createVariable(String name, String value, String delimiter){
		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		return createVariable(name,value,IEnvironmentVariable.ENVVAR_REPLACE,delimiter);	
	}
	
	public IEnvironmentVariable checkVariable(String name, String value, int op, String delimiter){
		IEnvironmentVariable var = getVariable(name);
		if(var != null 
				&& checkStrings(var.getValue(),value)
				&& var.getOperation() == op
				&& checkStrings(var.getDelimiter(),delimiter))
			return var;
		return null;
	}
	
	private boolean checkStrings(String str1, String str2){
		if(str1 != null &&
				str1.equals(str2))
			return true;
		return str1 == str2;
	}
	
	/**
	 * Returns the "dirty" state of the environment.
	 * If the dirty state is <code>true</code>, that means that the environment 
	 * is out of synch with the repository and the environment needs to be serialized.
	 * <br><br>
	 * The dirty state is automatically set to <code>false</code> when the environment is serialized
	 * by calling the serialize() method  
	 * @return boolean 
	 */
	public boolean isDirty(){
		return fIsDirty;
	}
	
	/**
	 * sets the "dirty" state of the environment
	 * @param dirty represents the new state
	 */
	public void setDirty(boolean dirty){
		fIsDirty = dirty;
	}
	
	/**
	 * Returns the "change" state of the environment.
	 * The "change" state represents whether the environment was changed or not.
	 * This state is not reset when the serialize() method is called
	 * Users can use this state to monitor whether the environment was changed or not.
	 * This state can be reset to <code>false</code> only by calling the setChanged(false) method 
	 * @return boolean
	 */
	public boolean isChanged(){
		return fIsChanged;
	}
	
	/**
	 * sets the "change" state of the environment
	 * @param changed represents the new "change" state
	 */
	public void setChanged(boolean changed){
		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		fIsChanged = changed;
	}

	public IEnvironmentVariable getVariable(String name){
		if(name == null || "".equals(name = name.trim())) //$NON-NLS-1$
			return null;
		IEnvironmentVariableManager provider = EnvironmentVariableManager.getDefault();
		if(!provider.isVariableCaseSensitive())
			name = name.toUpperCase();
		
		return (IEnvironmentVariable)getMap().get(name);
	}
	
	public void setVariales(IEnvironmentVariable vars[]){
		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		if(vars == null || vars.length == 0)
			deleteAll();
		else{
			if (getMap().size() != 0) {
				Iterator iter = getMap().values().iterator();
				while(iter.hasNext()){
					IEnvironmentVariable v = (IEnvironmentVariable)iter.next();
					int i;
					for(i = 0 ; i < vars.length; i++){
						if(v.getName().equals(vars[i].getName()))
							break;
					}
					if(i == vars.length)
						deleteVariable(v.getName());
				}
			}
			createVriables(vars);
		}
	}
	
	public void createVriables(IEnvironmentVariable vars[]){
		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		for(int i = 0; i < vars.length; i++)
			createVariable(vars[i].getName(),
					vars[i].getValue(),
					vars[i].getOperation(),
					vars[i].getDelimiter());
	}
	
	public IEnvironmentVariable[] getVariables(){
		Collection vars = getMap().values();
		
		return (IEnvironmentVariable[])vars.toArray(new IEnvironmentVariable[vars.size()]);
	}
	
	public IEnvironmentVariable deleteVariable(String name){
		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		if(name == null || "".equals(name = name.trim())) //$NON-NLS-1$
			return null;
		IEnvironmentVariableManager provider = EnvironmentVariableManager.getDefault();
		if(!provider.isVariableCaseSensitive())
			name = name.toUpperCase();

		IEnvironmentVariable var = (IEnvironmentVariable)getMap().remove(name);
		if(var != null){
			fIsDirty = true;
			fIsChanged = true;
		}

		return var;
	}
	
	public boolean deleteAll(){
		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		Map map = getMap();
		if(map.size() > 0){
			fIsDirty = true;
			fIsChanged = true;
			map.clear();
			return true;
		}
		
		return false;
	}
	
	public boolean isReadOnly(){
		return fIsReadOnly;
	}
	
	public boolean appendEnvironment(){
		return fAppend;
	}

	public void setAppendEnvironment(boolean append){
		if(fAppend == append)
			return;

		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();

		fAppend = append;
		fIsDirty = true;
	}

	public boolean appendContributedEnvironment(){
		return fAppendContributedEnv;
	}

	public void setAppendContributedEnvironment(boolean append){
		if(fAppendContributedEnv == append)
			return;

		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		
		fAppendContributedEnv = append;
		fIsDirty = true;
	}
	
	public void restoreDefaults(){
		deleteAll();
		fAppend = DEFAULT_APPEND;
		fAppendContributedEnv = DEFAULT_APPEND;
	}

/*	public Object clone(){
		try {
			StorableEnvironment env = (StorableEnvironment)super.clone();
			env.fVariables = (HashMap)fVariables.clone();
		} catch (CloneNotSupportedException e) {
		}
		return null;
	}
*/
}
