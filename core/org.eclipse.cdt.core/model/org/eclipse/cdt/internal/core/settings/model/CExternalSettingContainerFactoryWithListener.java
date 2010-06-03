/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.ListenerList;


public abstract class CExternalSettingContainerFactoryWithListener extends
		CExternalSettingContainerFactory {
	private ListenerList fListenerList;

	@Override
	public void addListener(ICExternalSettingsListener listener){
		if(fListenerList == null)
			fListenerList = new ListenerList();
		
		fListenerList.add(listener);
	}
	
	@Override
	public void removeListener(ICExternalSettingsListener listener){
		if(fListenerList == null)
			return;
		
		fListenerList.remove(listener);
	}

	protected void notifySettingsChange(IProject project, String cfgId, CExternalSettingsContainerChangeInfo[] infos){
		if(fListenerList == null)
			return;
		
		if(infos.length == 0)
			return;
		
		CExternalSettingChangeEvent event = new CExternalSettingChangeEvent(infos);
		
		Object[] listeners = fListenerList.getListeners();
		for(int i = 0; i < listeners.length; i++){
			((ICExternalSettingsListener)listeners[i]).settingsChanged(project, cfgId, event);
		}
	}
}
