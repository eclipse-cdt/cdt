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
package org.eclipse.cdt.managedbuilder.internal.envvar;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class represents the set of environment variables that could be loaded
 * and stored in XML
 * 
 * @since 3.0
 *
 */
public class StorableEnvironment {
	public static final String ENVIRONMENT_ELEMENT_NAME = "environment"; //$NON-NLS-1$
	private Map fVariables;
	private boolean fIsDirty = false;
	private boolean fIsChanged = false;
	
	private Map getMap(){
		if(fVariables == null)
			fVariables = new HashMap();
		return fVariables;
	}
	
	public StorableEnvironment() {

	}

	public StorableEnvironment(Element element) {
		load(element);
	}
	
	private void load(Element element){
		NodeList nodeList = element.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); ++i) {
			Node node = nodeList.item(i);
			if (node.getNodeName().equals(StorableEnvVar.VARIABLE_ELEMENT_NAME)) {
				addVariable(new StorableEnvVar((Element)node));
			}
		}
		fIsDirty = false;
		fIsChanged = false;
	}
	
	public void serialize(Document doc, Element element){
		if(fVariables != null){
			Iterator iter = fVariables.values().iterator();
			while(iter.hasNext()){
				StorableEnvVar var = (StorableEnvVar)iter.next();
				Element varEl = doc.createElement(StorableEnvVar.VARIABLE_ELEMENT_NAME);
				element.appendChild(varEl);
				var.serialize(doc,varEl);
			}
		}
		fIsDirty = false;
	}

	private void addVariable(IBuildEnvironmentVariable var){
		String name = var.getName();
		if(name == null)
			return;
		IEnvironmentVariableProvider provider = ManagedBuildManager.getEnvironmentVariableProvider();
		if(!provider.isVariableCaseSensitive())
			name = name.toUpperCase();
		
		getMap().put(name,var);
	}

	public IBuildEnvironmentVariable createVariable(String name, String value, int op, String delimiter){
		if(name == null || "".equals(name = name.trim())) //$NON-NLS-1$
			return null;

		IBuildEnvironmentVariable var = checkVariable(name,value,op,delimiter);
		if(var == null){
			var = new StorableEnvVar(name, value, op, delimiter);
			addVariable(var);
			fIsDirty = true;
			fIsChanged = true;
		}
		return var;
	}

	public IBuildEnvironmentVariable createVariable(String name){
		return createVariable(name,null,IBuildEnvironmentVariable.ENVVAR_REPLACE,null);
	}
	
	public IBuildEnvironmentVariable createVariable(String name, String value){
		return createVariable(name,value,IBuildEnvironmentVariable.ENVVAR_REPLACE,null);	
	}
	
	public IBuildEnvironmentVariable createVariable(String name, String value, String delimiter){
		return createVariable(name,value,IBuildEnvironmentVariable.ENVVAR_REPLACE,delimiter);	
	}
	
	public IBuildEnvironmentVariable checkVariable(String name, String value, int op, String delimiter){
		IBuildEnvironmentVariable var = getVariable(name);
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
	 * @see org.eclipse.cdt.managedbuilder.internal.envvar.StorableEnvironment#isDirty()
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
	 * @see org.eclipse.cdt.managedbuilder.internal.envvar.StorableEnvironment#isChanged()
	 * @param changed represents the new "change" state
	 */
	public void setChanged(boolean changed){
		fIsChanged = changed;
	}

	public IBuildEnvironmentVariable getVariable(String name){
		if(name == null || "".equals(name = name.trim())) //$NON-NLS-1$
			return null;
		IEnvironmentVariableProvider provider = EnvironmentVariableProvider.getDefault();
		if(!provider.isVariableCaseSensitive())
			name = name.toUpperCase();
		
		return (IBuildEnvironmentVariable)getMap().get(name);
	}
	
	public void setVariales(IBuildEnvironmentVariable vars[]){
		if(vars == null || vars.length == 0)
			deleteAll();
		else{
			if (getMap().size() != 0) {
				Iterator iter = getMap().values().iterator();
				while(iter.hasNext()){
					IBuildEnvironmentVariable v = (IBuildEnvironmentVariable)iter.next();
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
	
	public void createVriables(IBuildEnvironmentVariable vars[]){
		for(int i = 0; i < vars.length; i++)
			createVariable(vars[i].getName(),
					vars[i].getValue(),
					vars[i].getOperation(),
					vars[i].getDelimiter());
	}
	
	public IBuildEnvironmentVariable[] getVariables(){
		Collection vars = getMap().values();
		
		return (IBuildEnvironmentVariable[])vars.toArray(new IBuildEnvironmentVariable[vars.size()]);
	}
	
	IBuildEnvironmentVariable deleteVariable(String name){
		if(name == null || "".equals(name = name.trim())) //$NON-NLS-1$
			return null;
		IEnvironmentVariableProvider provider = ManagedBuildManager.getEnvironmentVariableProvider();
		if(!provider.isVariableCaseSensitive())
			name = name.toUpperCase();

		IBuildEnvironmentVariable var = (IBuildEnvironmentVariable)getMap().remove(name);
		if(var != null){
			fIsDirty = true;
			fIsChanged = true;
		}

		return var;
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
