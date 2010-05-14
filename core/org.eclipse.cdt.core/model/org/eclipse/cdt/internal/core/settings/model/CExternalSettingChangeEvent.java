/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

/**
 * External setting change event
 */
class CExternalSettingChangeEvent {
	private final CExternalSettingsContainerChangeInfo[] fChangeInfos;

	CExternalSettingChangeEvent(CExternalSettingsContainerChangeInfo[] infos){
		fChangeInfos = infos;
	}

	public CExternalSettingsContainerChangeInfo[] getChangeInfos(){
		return fChangeInfos;
	}
}
