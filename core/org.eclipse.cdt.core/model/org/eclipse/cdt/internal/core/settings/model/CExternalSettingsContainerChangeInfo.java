/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
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

import org.eclipse.cdt.internal.core.settings.model.CExternalSettingsManager.CContainerRef;

public class CExternalSettingsContainerChangeInfo {
	public static final int CHANGED = 1;

	public static final int CONTAINED_ID = 1;
	public static final int CONTAINER_CONTENTS = 1 << 1;

	private int fFlags;
	private String fOldId;
	private CContainerRef fCRef;
	//	private Map fIncludeMap, fExcludeMap;

	//	private class ProjChangeInfo {
	//		private String fProjName;
	//		private Set fCfgIds;
	//	}
	//	private CExternalSettingsContainer fContainer;
	//	private ExtSettingsDelta[] fDeltas;

	CExternalSettingsContainerChangeInfo(int flags, CContainerRef cr,
			//			CExternalSettingsContainer container,
			String oldId/*,
						ExtSettingsDelta[] deltas*/
	) {
		this.fFlags = flags;
		this.fCRef = cr;
		//		this.fContainer = container;
		this.fOldId = oldId;
		//		this.fIncludeMap = includeMap;
		//		this.fExcludeMap = exludeMap;
		//		this.fDeltas = deltas;
	}

	public int getEventType() {
		return CHANGED;
	}

	public int getChangeFlags() {
		return fFlags;
	}

	//	public CExternalSettingsContainer getContainer(){
	//		return fContainer;
	//	}

	//	public ExtSettingsDelta[] getDeltas(){
	//		return fDeltas;
	//	}

	public String getOldId() {
		return fOldId;
	}

	public String getNewId() {
		return fCRef.getContainerId();
	}

	public CContainerRef getContainerInfo() {
		return fCRef;
	}
}
