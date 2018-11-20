/*******************************************************************************
 * Copyright (c) 2005, 2009 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
public class StorableEnvironment {
	public static final String ENVIRONMENT_ELEMENT_NAME = "environment"; //$NON-NLS-1$
	static final String ATTRIBUTE_APPEND = "append"; //$NON-NLS-1$
	static final String ATTRIBUTE_APPEND_CONTRIBUTED = "appendContributed"; //$NON-NLS-1$
	static final boolean DEFAULT_APPEND = true;
	/** The map of in-flight environment variables */
	Map<String, IEnvironmentVariable> fVariables;
	private boolean fIsDirty = false;
	boolean fIsChanged = false;
	final boolean fIsReadOnly;
	boolean fAppend = DEFAULT_APPEND;
	boolean fAppendContributedEnv = DEFAULT_APPEND;

	/**
	 * @return the live {@link IEnvironmentVariable} map
	 */
	Map<String, IEnvironmentVariable> getMap() {
		if (fVariables == null)
			fVariables = new HashMap<>();
		return fVariables;
	}

	/**
	 *
	 * @param variables
	 * @param isReadOnly
	 */
	public StorableEnvironment(IEnvironmentVariable variables[], boolean isReadOnly) {
		setVariales(variables);
		fIsReadOnly = isReadOnly;
	}

	/**
	 * Create new empty StorableEnvironment
	 * @param isReadOnly
	 */
	public StorableEnvironment(boolean isReadOnly) {
		fIsReadOnly = isReadOnly;
	}

	/**
	 * Copy constructor.
	 *
	 * Creates a new StorableEnvironment from an existing StorableEnvironment. Settings
	 * are copied wholesale from the previous enviornment.
	 *
	 * Note that the previous environment's {@link ISerializeInfo} isn't copied
	 * over, as it's expected this environment's settings will be stored elsewhere
	 *
	 * @param env
	 * @param isReadOnly
	 */
	public StorableEnvironment(StorableEnvironment env, boolean isReadOnly) {
		if (env.fVariables != null)
			fVariables = env.getAllVariablesMap();
		fAppend = env.fAppend;
		fAppendContributedEnv = env.fAppendContributedEnv;
		fIsReadOnly = isReadOnly;
		fIsDirty = env.isDirty();
	}

	/**
	 * Initialize the StorableEnvironment from an ICStorageElement tree
	 * @param element
	 * @param isReadOnly
	 */
	public StorableEnvironment(ICStorageElement element, boolean isReadOnly) {
		load(element);
		fIsReadOnly = isReadOnly;
	}

	/**
	 * Load the preferences from an {@link ICStorageElement}
	 * @param element
	 */
	private void load(ICStorageElement element) {
		ICStorageElement children[] = element.getChildren();
		for (int i = 0; i < children.length; ++i) {
			ICStorageElement node = children[i];
			if (node.getName().equals(StorableEnvVar.VARIABLE_ELEMENT_NAME)) {
				addVariable(getMap(), new StorableEnvVar(node));
			}
		}

		String append = element.getAttribute(ATTRIBUTE_APPEND);
		fAppend = append != null ? Boolean.valueOf(append).booleanValue() : DEFAULT_APPEND;

		append = element.getAttribute(ATTRIBUTE_APPEND_CONTRIBUTED);
		fAppendContributedEnv = append != null ? Boolean.valueOf(append).booleanValue() : DEFAULT_APPEND;

		fIsDirty = false;
		fIsChanged = false;
	}

	/**
	 * Serialize the Storable enviornment into the ICStorageElement
	 *
	 * NB assumes that any variables part of the ISerializeInfo will continue to be serialized
	 * @param element
	 */
	public void serialize(ICStorageElement element) {
		element.setAttribute(ATTRIBUTE_APPEND, String.valueOf(fAppend));
		element.setAttribute(ATTRIBUTE_APPEND_CONTRIBUTED, String.valueOf(fAppendContributedEnv));
		if (fVariables != null) {
			Iterator<IEnvironmentVariable> iter = fVariables.values().iterator();
			while (iter.hasNext()) {
				StorableEnvVar var = (StorableEnvVar) iter.next();
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
	void addVariable(Map<String, IEnvironmentVariable> map, IEnvironmentVariable var) {
		String name = getNameForMap(var.getName());
		if (name == null)
			return;
		map.put(name, var);
	}

	public IEnvironmentVariable createVariable(String name, String value, int op, String delimiter) {
		if (fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();

		if (name == null || "".equals(name = name.trim())) //$NON-NLS-1$
			return null;

		IEnvironmentVariable var = checkVariable(name, value, op, delimiter);
		if (var == null) {
			var = new StorableEnvVar(name, value, op, delimiter);
			addVariable(getMap(), var);
			fIsDirty = true;
			fIsChanged = true;
		}
		return var;
	}

	public IEnvironmentVariable createVariable(String name) {
		return createVariable(name, null, IEnvironmentVariable.ENVVAR_REPLACE, null);
	}

	public IEnvironmentVariable createVariable(String name, String value) {
		return createVariable(name, value, IEnvironmentVariable.ENVVAR_REPLACE, null);
	}

	public IEnvironmentVariable createVariable(String name, String value, String delimiter) {
		if (fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		return createVariable(name, value, IEnvironmentVariable.ENVVAR_REPLACE, delimiter);
	}

	public IEnvironmentVariable checkVariable(String name, String value, int op, String delimiter) {
		IEnvironmentVariable var = getVariable(name);
		if (var != null && checkStrings(var.getValue(), value) && var.getOperation() == op
				&& checkStrings(var.getDelimiter(), delimiter))
			return var;
		return null;
	}

	private boolean checkStrings(String str1, String str2) {
		if (str1 != null && str1.equals(str2))
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
	public boolean isDirty() {
		return fIsDirty;
	}

	/**
	 * sets the "dirty" state of the environment
	 * @param dirty represents the new state
	 */
	public void setDirty(boolean dirty) {
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
	public boolean isChanged() {
		return fIsChanged;
	}

	/**
	 * sets the "change" state of the environment
	 * @param changed represents the new "change" state
	 */
	public void setChanged(boolean changed) {
		if (fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		fIsChanged = changed;
	}

	/**
	 * @param name
	 * @return the environment variable with the given name, or null
	 */
	public IEnvironmentVariable getVariable(String name) {
		name = getNameForMap(name);
		if (name == null)
			return null;
		return getMap().get(name);
	}

	/**
	 * Set the enviornment variables in this {@link StorableEnvironment}
	 * @param vars
	 */
	public void setVariales(IEnvironmentVariable vars[]) {
		if (fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		if (vars == null || vars.length == 0)
			deleteAll();
		else {
			if (getMap().size() != 0) {
				Iterator<IEnvironmentVariable> iter = getMap().values().iterator();
				while (iter.hasNext()) {
					IEnvironmentVariable v = iter.next();
					int i;
					for (i = 0; i < vars.length; i++) {
						if (v.getName().equals(vars[i].getName()))
							break;
					}
					if (i == vars.length)
						deleteVariable(v.getName());
				}
			}
			createVriables(vars);
		}
	}

	public void createVriables(IEnvironmentVariable vars[]) {
		if (fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		for (int i = 0; i < vars.length; i++)
			createVariable(vars[i].getName(), vars[i].getValue(), vars[i].getOperation(), vars[i].getDelimiter());
	}

	/**
	 * @return cloned map of all variables set on this storable environment runtime variables + backing store vars
	 */
	Map<String, IEnvironmentVariable> getAllVariablesMap() {
		Map<String, IEnvironmentVariable> vars = new HashMap<>();
		vars.putAll(getMap());
		return vars;
	}

	public IEnvironmentVariable[] getVariables() {
		Map<String, IEnvironmentVariable> vars = getAllVariablesMap();
		return vars.values().toArray(new IEnvironmentVariable[vars.size()]);
	}

	/**
	 * Returns the unique canonical form of the variable name for storage in the Maps.
	 *
	 * The name will be trimmed, and, if the var manager isn't case sensitive, made upper case
	 *
	 * @param name
	 * @return canonical name, or null
	 */
	String getNameForMap(String name) {
		if (name == null || (name = name.trim()).length() == 0)
			return null;
		if (!EnvironmentVariableManager.getDefault().isVariableCaseSensitive())
			return name.toUpperCase();
		return name;
	}

	public IEnvironmentVariable deleteVariable(String name) {
		if (fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		name = getNameForMap(name);
		if (name == null)
			return null;

		IEnvironmentVariable var = getMap().remove(name);
		if (var != null) {
			fIsDirty = true;
			fIsChanged = true;
		}

		return var;
	}

	public boolean deleteAll() {
		if (fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();
		Map<String, IEnvironmentVariable> map = getMap();
		if (map.size() > 0) {
			fIsDirty = true;
			fIsChanged = true;
			map.clear();
			return true;
		}

		return false;
	}

	public boolean isReadOnly() {
		return fIsReadOnly;
	}

	public boolean appendEnvironment() {
		return fAppend;
	}

	public void setAppendEnvironment(boolean append) {
		if (fAppend == append)
			return;

		if (fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();

		fAppend = append;
		fIsDirty = true;
	}

	public boolean appendContributedEnvironment() {
		return fAppendContributedEnv;
	}

	public void setAppendContributedEnvironment(boolean append) {
		if (fAppendContributedEnv == append)
			return;

		if (fIsReadOnly)
			throw ExceptionFactory.createIsReadOnlyException();

		fAppendContributedEnv = append;
		fIsDirty = true;
	}

	public void restoreDefaults() {
		deleteAll();
		fAppend = DEFAULT_APPEND;
		fAppendContributedEnv = DEFAULT_APPEND;
	}

}
