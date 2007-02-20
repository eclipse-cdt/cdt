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
package org.eclipse.cdt.managedbuilder.internal.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;

public class NotificationManager /*implements ISettingsChangeListener */{
	private static NotificationManager fInstance;
	private List fListeners;
	
	private NotificationManager(){
		fListeners = new ArrayList();
	}
	
	public static NotificationManager getInstance(){
		if(fInstance == null)
			fInstance = new NotificationManager();
		return fInstance;
	}

	public void optionChanged(IResourceInfo rcInfo, IHoldsOptions holder, IOption option, Object oldValue) {
		for(Iterator iter = fListeners.iterator(); iter.hasNext();){
			ISettingsChangeListener listener = (ISettingsChangeListener)iter.next();
			listener.optionChanged(rcInfo, holder, option, oldValue);
		}
	}
	
	public void subscribe(ISettingsChangeListener listener){
//		synchronized (this) {
			fListeners.add(listener);
//		}
	}
	
	public void unsubscribe(ISettingsChangeListener listener){
//		synchronized (this) {
			fListeners.remove(listener);
//		}
	}
	
	
	
	
}
