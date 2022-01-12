/*******************************************************************************
 * Copyright (c) 2007, 2016 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

public class ConfigurationBuildState implements IConfigurationBuildState {
	private final static Integer REBUILD_STATE = NEED_REBUILD;
	private final static Integer REMOVED_STATE = REMOVED;
	private final static Integer NONE_STATE = 0;

	private HashMap<Integer, Set<String>> fStateToPathListMap;
	private Properties fPathToStateProps;
	private String fCfgId;
	private IProject fProject;
	private int fState;

	ConfigurationBuildState(IProject project, String cfgId) {
		fCfgId = cfgId;
		fProject = project;
		fState = NEED_REBUILD;
	}

	void setProject(IProject project) {
		fProject = project;
	}

	@Override
	public IPath[] getFullPathsForState(int state) {
		if (fStateToPathListMap == null)
			return new IPath[0];

		Set<String> set = fStateToPathListMap.get(Integer.valueOf(state));
		if (set == null)
			return new IPath[0];

		return setToFullPaths(set);
	}

	private IPath[] setToFullPaths(Set<String> set) {
		IPath paths[] = new IPath[set.size()];
		IPath path = fProject.getFullPath();
		int num = 0;
		for (String projRel : set) {
			paths[num++] = path.append(projRel);
		}
		return paths;
	}

	@Override
	public int getStateForFullPath(IPath fullPath) {
		if (fPathToStateProps == null)
			return 0;
		String str = fullPathToString(fullPath);
		String v = fPathToStateProps.getProperty(str);
		if (v != null) {
			Integer i = stateToInt(v);
			if (i != null)
				return i.intValue();
		}
		return 0;
	}

	@Override
	public void setStateForFullPath(IPath fullPath, int state) {
		String str = fullPathToString(fullPath);
		int cur = getStateForFullPath(fullPath);
		if (cur == state)
			return;

		if (fPathToStateProps == null) {
			fPathToStateProps = new Properties();
			fStateToPathListMap = new HashMap<>();
		}
		String strState = stateToString(Integer.valueOf(state));
		Integer iState = stateToInt(strState);
		if (iState == null)
			throw new IllegalArgumentException();

		if (cur != 0) {
			Set<String> set = fStateToPathListMap.get(Integer.valueOf(cur));
			set.remove(str);
			if (set.size() == 0)
				fStateToPathListMap.remove(iState);
		}

		if (state != 0) {
			fPathToStateProps.setProperty(str, strState);
			Set<String> set = fStateToPathListMap.get(iState);
			if (set == null) {
				set = new HashSet<>();
				fStateToPathListMap.put(iState, set);
			}
			set.add(str);
		} else {
			fPathToStateProps.remove(str);
		}
	}

	private String fullPathToString(IPath fullPath) {
		return fullPath.removeFirstSegments(1).toString();
	}

	public void load(InputStream iStream) throws IOException {
		Properties props = new Properties();
		props.load(iStream);
		load(props);
	}

	private void load(Properties props) {
		HashMap<Integer, Set<String>> map = new HashMap<>();
		for (@SuppressWarnings("rawtypes")
		Entry entry : props.entrySet()) {
			Integer i = stateToInt((String) entry.getValue());
			Set<String> list = map.get(i);
			if (list == null) {
				list = new HashSet<>();
				map.put(i, list);
			}
			list.add((String) entry.getKey());
		}

		//TODO: trim lists
		if (map.size() != 0) {
			fStateToPathListMap = map;
			fPathToStateProps = props;
		}
		fState = 0;
	}

	public void store(OutputStream oStream) throws IOException {
		if (fPathToStateProps != null)
			fPathToStateProps.store(oStream, ""); //$NON-NLS-1$
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

	private Integer stateToInt(String state) {
		try {
			Integer i = Integer.valueOf(state);
			if (i.equals(REBUILD_STATE))
				return REBUILD_STATE;
			if (i.equals(REMOVED_STATE))
				return REMOVED_STATE;
			if (i.equals(NONE_STATE))
				return NONE_STATE;
		} catch (NumberFormatException e) {
			ManagedBuilderCorePlugin.log(e);
		}
		return null;
	}

	private String stateToString(Integer state) {
		return state.toString();
	}

	@Override
	public int getState() {
		return fState;
	}

	@Override
	public void setState(int state) {
		fState = state;
		clear();
	}

	private void clear() {
		fPathToStateProps = null;
		fStateToPathListMap = null;
	}

	@Override
	public String getConfigurationId() {
		return fCfgId;
	}

	@Override
	public IProject getProject() {
		return fProject;
	}

	public boolean exists() {
		return fState == 0;
	}
}
