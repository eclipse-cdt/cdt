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
package org.eclipse.cdt.managedbuilder.internal.buildproperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildProperties;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildProperty;
import org.eclipse.core.runtime.CoreException;

public class BuildProperties implements IBuildProperties {
	private HashMap<String, IBuildProperty> fPropertiesMap = new HashMap<String, IBuildProperty>();
	private ArrayList<String> fInexistentProperties;
	
	public BuildProperties(){
		
	}
	
	public BuildProperties(String properties){
		StringTokenizer t = new StringTokenizer(properties, BuildPropertyManager.PROPERTIES_SEPARATOR);
		while(t.hasMoreTokens()){
			String property = t.nextToken();
			try {
				BuildProperty prop = new BuildProperty(property);
				addProperty(prop);
			} catch (CoreException e) {
				if(fInexistentProperties == null)
					fInexistentProperties = new ArrayList<String>();
				
				fInexistentProperties.add(property);
			}
		}
		
		if(fInexistentProperties != null)
			fInexistentProperties.trimToSize();
	}
	
	@SuppressWarnings("unchecked")
	public BuildProperties(BuildProperties properties){
		fPropertiesMap.putAll(properties.fPropertiesMap);
		if(properties.fInexistentProperties != null)
			fInexistentProperties = (ArrayList<String>)properties.fInexistentProperties.clone();
	}

	public IBuildProperty[] getProperties(){
		return fPropertiesMap.values().toArray(new BuildProperty[fPropertiesMap.size()]);
	}
	
	public IBuildProperty getProperty(String id){
		return fPropertiesMap.get(id);
	}
	
	void addProperty(IBuildProperty property){
		fPropertiesMap.put(property.getPropertyType().getId(), property);
	}

	public IBuildProperty setProperty(String propertyId, String propertyValue) throws CoreException {
		return setProperty(propertyId, propertyValue, false);
	}

	public IBuildProperty setProperty(String propertyId, String propertyValue, boolean force) throws CoreException {
		try {
			IBuildProperty property = BuildPropertyManager.getInstance().createProperty(propertyId, propertyValue);
			
			addProperty(property);
			
			return property;
		} catch (CoreException e){
			if(force){
				if(fInexistentProperties == null)
					fInexistentProperties = new ArrayList<String>(1);
				
				fInexistentProperties.add(BuildProperty.toString(propertyId, propertyValue));
				fInexistentProperties.trimToSize();
			}
			throw e;
		}
	}
	
	public IBuildProperty removeProperty(String id){
		return fPropertiesMap.remove(id);
	}
	
	void removeProperty(BuildProperty property){
		fPropertiesMap.remove(property.getPropertyType().getId());
	}
	
	@Override
	public String toString(){
		String props = toStringExistingProperties();
		if(fInexistentProperties != null){
			String inexistentProps = CDataUtil.arrayToString(fInexistentProperties.toArray(new String[fInexistentProperties.size()]), BuildPropertyManager.PROPERTIES_SEPARATOR);
			if(props.length() != 0){
				StringBuffer buf = new StringBuffer();
				buf.append(props).append(BuildPropertyManager.PROPERTIES_SEPARATOR).append(inexistentProps);
			} else {
				props = inexistentProps;
			}
		}
		return props;
	}
	
	public String toStringExistingProperties(){
		int size = fPropertiesMap.size(); 
		if(size == 0)
			return ""; //$NON-NLS-1$
		else if(size == 1)
			return fPropertiesMap.values().iterator().next().toString();
		
		StringBuffer buf = new StringBuffer();
		Iterator<IBuildProperty> iter = fPropertiesMap.values().iterator();
		buf.append(iter.next().toString());
		for(;iter.hasNext();){
			buf.append(BuildPropertyManager.PROPERTIES_SEPARATOR);
			buf.append(iter.next().toString());
		}
		return buf.toString();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object clone() {
		try {
			BuildProperties clone = (BuildProperties)super.clone();
			
			if(fInexistentProperties != null)
				clone.fInexistentProperties = (ArrayList<String>)fInexistentProperties.clone();
			
			clone.fPropertiesMap = (HashMap<String, IBuildProperty>)fPropertiesMap.clone();
/*			for(Iterator iter = clone.fPropertiesMap.entrySet().iterator(); iter.hasNext();){
				Map.Entry entry = (Map.Entry)iter.next();
				BuildProperty prop = (BuildProperty)entry.getValue();
				entry.setValue(prop.clone());
			}
*/
			return clone;
		} catch (CloneNotSupportedException e) {
		}
		return null;
	}

	public void clear() {
		fPropertiesMap.clear();
		fInexistentProperties.clear();
	}

	public boolean containsValue(String propertyId, String valueId) {
		IBuildProperty prop = getProperty(propertyId);
		if(prop != null){
			return valueId.equals(prop.getValue().getId());
		}
		return false;
	}
	
	
	
}
