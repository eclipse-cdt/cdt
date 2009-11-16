/*******************************************************************************
 * Copyright (c) 2005, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Intel Corporation - Initial API and implementation
 *    James Blackburn (Broadcom Corp.)
 *******************************************************************************/
package org.eclipse.cdt.utils.envvar;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariableManager;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.internal.core.envvar.EnvironmentVariableManager;
import org.eclipse.cdt.internal.core.settings.model.ExceptionFactory;
import org.eclipse.cdt.utils.envvar.StorableEnvironmentLoader.ISerializeInfo;

/**
 * This class represents the set of environment variables that could be loaded
 * and stored in XML
 * 
 * @since 3.0
 */
public class StorableEnvironment /*implements Cloneable*/{
	public static final String ENVIRONMENT_ELEMENT_NAME = "environment"; //$NON-NLS-1$
	private static final String ATTRIBUTE_APPEND = "append";  //$NON-NLS-1$
	private static final String ATTRIBUTE_APPEND_CONTRIBUTED = "appendContributed";  //$NON-NLS-1$
	private static final boolean DEFAULT_APPEND = true;
	/** The map of in-flight environment variables */
	private HashMap<String, IEnvironmentVariable> fVariables;
	/** Map of 'deleted' variables (which shouldn't be updated by a backing store change) */
	private HashMap<String, IEnvironmentVariable> fDeletedVariables;
	private boolean fIsDirty = false;
	private boolean fIsChanged = false;
	private final boolean fIsReadOnly;
	private boolean fAppend = DEFAULT_APPEND;
	private boolean fAppendContributedEnv = DEFAULT_APPEND;


	// State to manage and handle external changes to the environment

	/** A cache copy of the environment as stored in the {@link ISerializeInfo} 
	 *  used to work-out whether the cachedStorableEnvironment map needs refreshing */
	private String fCachedSerialEnvString;
	/** Map of Environment as loaded from the {@link ISerializeInfo} */
	private HashMap<String, IEnvironmentVariable> fCachedSerialEnv;
	private ISerializeInfo fSerialEnv;
	// State to track whether API users have changed these boolean values
	private boolean fAppendChanged = false;
	private boolean fAppendContributedChanged = false;


	/** 
	 * @return the live {@link IEnvironmentVariable} map
	 */
	private Map<String, IEnvironmentVariable> getMap(){
		if(fVariables == null)
			fVariables = new HashMap<String, IEnvironmentVariable>();
		return fVariables;
	}
	/** 
	 * @return the live removed {@link IEnvironmentVariable} map
	 */
	private Map<String, IEnvironmentVariable> getDeletedMap(){
		if(fDeletedVariables == null)
			fDeletedVariables = new HashMap<String, IEnvironmentVariable>();
		return fDeletedVariables;
	}

	public StorableEnvironment(IEnvironmentVariable variables[], boolean isReadOnly) {
		setVariales(variables);
		fIsReadOnly = isReadOnly;
	}

	public StorableEnvironment(boolean isReadOnly) {
		fIsReadOnly = isReadOnly;
	}

	public StorableEnvironment(StorableEnvironment env, boolean isReadOnly) {
		if(env.fVariables != null) {
			@SuppressWarnings("unchecked")
			final HashMap<String, IEnvironmentVariable> clone = (HashMap<String, IEnvironmentVariable>)env.fVariables.clone();
			fVariables = clone;
		}
		if(env.fDeletedVariables != null) {
			@SuppressWarnings("unchecked")
			final HashMap<String, IEnvironmentVariable> clone = (HashMap<String, IEnvironmentVariable>)env.fDeletedVariables.clone();
			fDeletedVariables = clone;
		}
		fSerialEnv = env.fSerialEnv;
		fAppend = env.fAppend;
		fAppendChanged = env.fAppendChanged;
		fAppendContributedEnv = env.fAppendContributedEnv;
		fAppendContributedChanged = env.fAppendContributedChanged;
		fIsReadOnly = isReadOnly;
		fIsDirty = env.isDirty();
	}

	public StorableEnvironment(ICStorageElement element, boolean isReadOnly) {
		load(element);
		fIsReadOnly = isReadOnly;
	}

	/**
	 * Create a StorableEnvironment backed by this ISerializeInfo.
	 * 
	 * @param serializeInfo
	 * @since 5.2
	 */
	StorableEnvironment(ISerializeInfo serializeInfo, boolean isReadOnly) {
		fIsReadOnly = isReadOnly;
		fSerialEnv = serializeInfo;

		// Update the cached state
		checkBackingSerializeInfo();			
	}

	/**
	 * Check and update the state of the backing {@link ISerializeInfo} cache
	 */
	private void checkBackingSerializeInfo() {
		String envString = StorableEnvironmentLoader.loadPreferenceNode(fSerialEnv);

		// Has anything changed?
		if (envString == null || envString.equals(fCachedSerialEnvString))
			return;
		fCachedSerialEnvString = envString;

		ICStorageElement element = StorableEnvironmentLoader.environmentStorageFromString(fCachedSerialEnvString);
		if (element == null)
			return;

		// Now update the cached environment
		if (fCachedSerialEnv == null)
			fCachedSerialEnv = new HashMap<String, IEnvironmentVariable>();
		else
			fCachedSerialEnv.clear();

		for (ICStorageElement child : element.getChildren())
			if (child.getName().equals(StorableEnvVar.VARIABLE_ELEMENT_NAME))
				addVariable(fCachedSerialEnv, new StorableEnvVar(child));

		// If user hasn't changed fAppend or fAppend Contributed, then update
		if (!fAppendChanged) {
			String append = element.getAttribute(ATTRIBUTE_APPEND);
			fAppend = append != null ? Boolean.valueOf(append).booleanValue() : DEFAULT_APPEND;
		}
		if (!fAppendContributedChanged) {
			String append = element.getAttribute(ATTRIBUTE_APPEND_CONTRIBUTED);
			fAppendContributedEnv = append != null ? Boolean.valueOf(append).booleanValue()	: DEFAULT_APPEND;
		}
	}

	private void load(ICStorageElement element){
		ICStorageElement children[] = element.getChildren();
		for (int i = 0; i < children.length; ++i) {
			ICStorageElement node = children[i];
			if (node.getName().equals(StorableEnvVar.VARIABLE_ELEMENT_NAME)) {
				addVariable(getMap(), new StorableEnvVar(node));
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

	/**
	 * Serialize the Storable enviornment into the ICStorageElement
	 * 
	 * NB assumes that any variables part of the ISerializeInfo will continue to be serialized
	 * @param element
	 */
	public void serialize(ICStorageElement element){
		checkBackingSerializeInfo();
		Map<String, IEnvironmentVariable> map = new HashMap<String, IEnvironmentVariable>();
		if (fCachedSerialEnv != null)
			map.putAll(fCachedSerialEnv);
		if (fDeletedVariables != null) {
			for (String rem : fDeletedVariables.keySet())
				map.remove(rem);
			fDeletedVariables.clear();
		}
		if (fVariables != null)
			map.putAll(fVariables);

		element.setAttribute(ATTRIBUTE_APPEND, Boolean.valueOf(fAppend).toString());
		element.setAttribute(ATTRIBUTE_APPEND_CONTRIBUTED, Boolean.valueOf(fAppendContributedEnv).toString());
		if(!map.isEmpty()){
			Iterator<IEnvironmentVariable> iter = map.values().iterator();
			while(iter.hasNext()){
				StorableEnvVar var = (StorableEnvVar)iter.next();
				ICStorageElement varEl = element.createChild(StorableEnvVar.VARIABLE_ELEMENT_NAME);
				var.serialize(varEl);
			}
		}

		fIsDirty = false;
	}

	/**
	 * Add the environment variable to the map
	 * @param map
	 * @param var
	 */
	private void addVariable(Map<String, IEnvironmentVariable> map, IEnvironmentVariable var){
		String name = var.getName();
		if(name == null)
			return;
		IEnvironmentVariableManager provider = EnvironmentVariableManager.getDefault();
		if(!provider.isVariableCaseSensitive())
			name = name.toUpperCase();

		map.put(name,var);
	}

	public IEnvironmentVariable createVariable(String name, String value, int op, String delimiter){
		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		
		if(name == null || "".equals(name = name.trim())) //$NON-NLS-1$
			return null;

		IEnvironmentVariable var = checkVariable(name,value,op,delimiter);
		if(var == null){
			var = new StorableEnvVar(name, value, op, delimiter);
			addVariable(getMap(), var);
			// Variable added, ensure it's not in the removed set
			if (fDeletedVariables != null)
				fDeletedVariables.remove(EnvironmentVariableManager.getDefault().isVariableCaseSensitive() ? name : name.toUpperCase());
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

	/**
	 * @param name
	 * @return the environment variable with the given name, or null
	 */
	public IEnvironmentVariable getVariable(String name){
		if(name == null || "".equals(name = name.trim())) //$NON-NLS-1$
			return null;
		IEnvironmentVariableManager provider = EnvironmentVariableManager.getDefault();
		if(!provider.isVariableCaseSensitive())
			name = name.toUpperCase();

		IEnvironmentVariable var = getMap().get(name);
		if (var != null)
			return var;

		if (fDeletedVariables != null && fDeletedVariables.containsKey(name))
			return null;

		checkBackingSerializeInfo();
		if (fCachedSerialEnv != null)
			return fCachedSerialEnv.get(name);
		return null;
	}
	
	/**
	 * Set the enviornment variables in this {@link StorableEnvironment}
	 * @param vars
	 */
	public void setVariales(IEnvironmentVariable vars[]){
		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		if(vars == null || vars.length == 0)
			deleteAll();
		else{
			if (getMap().size() != 0) {
				Iterator<IEnvironmentVariable> iter = getMap().values().iterator();
				while(iter.hasNext()){
					IEnvironmentVariable v = iter.next();
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
		checkBackingSerializeInfo();
		// Get all the environment from the backing store first
		Map<String, IEnvironmentVariable> vars = new HashMap<String, IEnvironmentVariable>();
		if (fCachedSerialEnv != null)
			vars.putAll(fCachedSerialEnv);
		if (fDeletedVariables != null)
			for (String name : fDeletedVariables.keySet())
				vars.remove(name);

		// Now overwrite with the live variables set, and return
		vars.putAll(getMap());

		return vars.values().toArray(new IEnvironmentVariable[vars.size()]);
	}

	public IEnvironmentVariable deleteVariable(String name){
		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		if(name == null || "".equals(name = name.trim())) //$NON-NLS-1$
			return null;
		IEnvironmentVariableManager provider = EnvironmentVariableManager.getDefault();
		if(!provider.isVariableCaseSensitive())
			name = name.toUpperCase();

		IEnvironmentVariable var = getMap().remove(name);
		getDeletedMap().put(name, var);
		if(var != null){
			fIsDirty = true;
			fIsChanged = true;
		}

		return var;
	}
	
	public boolean deleteAll(){
		if(fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		Map<String, IEnvironmentVariable> map = getMap();
		if(map.size() > 0){
			fIsDirty = true;
			fIsChanged = true;
			getDeletedMap().putAll(map);
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
		fAppendChanged = true;
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
		fAppendContributedChanged = true;
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
