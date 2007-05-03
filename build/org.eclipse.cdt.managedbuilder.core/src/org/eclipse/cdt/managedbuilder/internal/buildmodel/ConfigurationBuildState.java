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
package org.eclipse.cdt.managedbuilder.internal.buildmodel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

public class ConfigurationBuildState implements IConfigurationBuildState {
	private final static Integer REBUILD_STATE = new Integer(NEED_REBUILD);
	private final static Integer REMOVED_STATE = new Integer(REMOVED);
	private final static Integer NONE_STATE = new Integer(0);
	
	private Map fStateToPathListMap;
	private Properties fPathToStateProps;
	private String fCfgId;
	private IProject fProject;
	private int fState;
	
	ConfigurationBuildState(IProject project, String cfgId){
		fCfgId = cfgId;
		fProject = project;
		fState = NEED_REBUILD;
	}
	
	void setProject(IProject project){
		fProject = project;
	}

	public IPath[] getFullPathsForState(int state) {
		if(fStateToPathListMap == null)
			return new IPath[0];
		
		Set set = (Set)fStateToPathListMap.get(new Integer(state));
		if(set == null)
			return new IPath[0];

		return setToFullPaths(set);
	}
	
	private IPath[] setToFullPaths(Set set){
		IPath paths[] = new IPath[set.size()];
		IPath path = fProject.getFullPath();
		int num = 0;
		for(Iterator iter = set.iterator(); iter.hasNext();){
			String projRel = (String)iter.next();
			paths[num++] = path.append(projRel);
		}
		return paths;
	}

	public int getStateForFullPath(IPath fullPath) {
		if(fPathToStateProps == null)
			return 0;
		String str = fullPathToString(fullPath);
		String v = fPathToStateProps.getProperty(str);
		if(v != null){
			Integer i = stateToInt(v);
			if(i != null)
				return i.intValue();
		}
		return 0;
	}

	public void setStateForFullPath(IPath fullPath, int state) {
		String str = fullPathToString(fullPath);
		int cur = getStateForFullPath(fullPath);
		if(cur == state)
			return;
		
		if(fPathToStateProps == null){
			fPathToStateProps = new Properties();
			fStateToPathListMap = new HashMap();
		}
		String strState = stateToString(new Integer(state));
		Integer iState = stateToInt(strState);
		if(iState == null)
			throw new IllegalArgumentException();
		
		if(cur != 0){
			Set set = (Set)fStateToPathListMap.get(new Integer(cur));
			set.remove(str);
			if(set.size() == 0)
				fStateToPathListMap.remove(iState);
		}

		if(state != 0){
			fPathToStateProps.setProperty(str, strState);
			Set set = (Set)fStateToPathListMap.get(iState);
			if(set == null){
				set = new HashSet();
				fStateToPathListMap.put(iState, set);
			}
			set.add(str);
		} else {
			fPathToStateProps.remove(str);	
		}
	}
	
	private String fullPathToString(IPath fullPath){
		return fullPath.removeFirstSegments(1).toString();
	}

	public void load(InputStream iStream) throws IOException{
		Properties props = new Properties();
		props.load(iStream);
		load(props);
	}

	private void load(Properties props){
		Map map = new HashMap();
		for(Iterator iter = props.entrySet().iterator(); iter.hasNext();){
			Map.Entry entry = (Map.Entry)iter.next();
			Integer i = stateToInt((String)entry.getValue());
			Set list = (Set)map.get(i);
			if(list == null){
				list = new HashSet();
				map.put(i, list);
			}
			list.add(entry.getKey());
		}
		
		//TODO: trim lists
		if(map.size() != 0){
			fStateToPathListMap = map;
			fPathToStateProps = props;
		}
		fState = 0;
	}
	
	public void store(OutputStream oStream) throws IOException{
		if(fPathToStateProps != null)
			fPathToStateProps.store(oStream, "");
//		Properties props = new Properties();
//		store(props);
//		props.store(oStream, "");
	}

//	public void store(Properties props){
//		if(fStateToPathListMap == null)
//			return;
//		
//		for(Iterator iter = fStateToPathListMap.entrySet().iterator(); iter.hasNext();){
//			Map.Entry entry = (Map.Entry)iter.next();
//			String propValue = stateToString((Integer)entry.getKey());
//			List list = (List)entry.getValue();
//			for(int i = 0; i < list.size(); i++){
//				props.setProperty((String)list.get(i), propValue);
//			}
//		}
//	}

	private Integer stateToInt(String state){
		try {
		Integer i = new Integer(state);
		if(i.equals(REBUILD_STATE))
			return REBUILD_STATE;
		if(i.equals(REMOVED_STATE))
			return REMOVED_STATE;
		if(i.equals(NONE_STATE))
			return NONE_STATE;
		} catch (NumberFormatException e){
			ManagedBuilderCorePlugin.log(e);
		}
		return null;
	}

	private String stateToString(Integer state){
		return state.toString();
	}

	public int getState() {
		return fState;
	}

	public void setState(int state) {
		fState = state;
		clear();
	}
	
	private void clear(){
		fPathToStateProps = null;
		fStateToPathListMap = null;
	}

	public String getConfigurationId() {
		return fCfgId;
	}

	public IProject getProject() {
		return fProject;
	}
	
	public boolean exists(){
		return fState == 0;
	}
}
