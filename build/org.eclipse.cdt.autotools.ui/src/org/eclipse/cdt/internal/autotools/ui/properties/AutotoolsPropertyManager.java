/*******************************************************************************
 * Copyright (c) 2007, 2016 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.properties;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.ListenerList;

public class AutotoolsPropertyManager implements IPropertyChangeManager {

	private static volatile AutotoolsPropertyManager fInstance;
	private Map<IProject, ListenerList<IProjectPropertyListener>> projectList;
	
	private AutotoolsPropertyManager() {
		projectList = new HashMap<>();
	}
	
	public static AutotoolsPropertyManager getDefault() {
		if (fInstance == null)
			fInstance = new AutotoolsPropertyManager();
		return fInstance;
	}
	
	@Override
	public synchronized void addProjectPropertyListener(IProject project,
			IProjectPropertyListener listener) {
		ListenerList<IProjectPropertyListener> list = projectList.get(project);
		if (list == null) {
			list = new ListenerList<>();
			projectList.put(project, list);
		}
		list.add(listener);
	}

	@Override
	public synchronized void notifyPropertyListeners(IProject project, String property) {
		ListenerList<IProjectPropertyListener> list = projectList.get(project);
		if (list != null) {
			Object[] listeners = list.getListeners();
			for (int i = 0; i < listeners.length; ++i) {
				((IProjectPropertyListener)listeners[i]).handleProjectPropertyChanged(project, property);
			}
		}
	}

	@Override
	public synchronized void removeProjectPropertyListener(IProject project, 
			IProjectPropertyListener listener) {
		ListenerList<IProjectPropertyListener> list = projectList.get(project);
		if (list != null)
			list.remove(listener);
	}

}
