/*******************************************************************************
 * Copyright (c) 2007, 2017 Intel Corporation and others.
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
package org.eclipse.cdt.internal.core.settings.model;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.ListenerList;

public abstract class CExternalSettingContainerFactoryWithListener extends CExternalSettingContainerFactory {
	private ListenerList<ICExternalSettingsListener> fListenerList;

	@Override
	public void addListener(ICExternalSettingsListener listener) {
		if (fListenerList == null)
			fListenerList = new ListenerList<>();

		fListenerList.add(listener);
	}

	@Override
	public void removeListener(ICExternalSettingsListener listener) {
		if (fListenerList == null)
			return;

		fListenerList.remove(listener);
	}

	protected void notifySettingsChange(IProject project, String cfgId, CExternalSettingsContainerChangeInfo[] infos) {
		if (fListenerList == null)
			return;

		if (infos.length == 0)
			return;

		CExternalSettingChangeEvent event = new CExternalSettingChangeEvent(infos);

		for (ICExternalSettingsListener listener : fListenerList) {
			listener.settingsChanged(project, cfgId, event);
		}
	}
}
