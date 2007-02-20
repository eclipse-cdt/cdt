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
package org.eclipse.cdt.managedbuilder.internal.dataprovider;

import org.eclipse.cdt.core.settings.model.extension.CTargetPlatformData;
import org.eclipse.cdt.managedbuilder.internal.core.TargetPlatform;

public class BuildTargetPlatformData extends CTargetPlatformData {
	private TargetPlatform fTargetPlatform;

	public BuildTargetPlatformData(TargetPlatform targetPlatform){
		fTargetPlatform = targetPlatform;
	}
	public String[] getBinaryParserIds() {
		return fTargetPlatform.getBinaryParserList();
	}

	public void setBinaryParserIds(String[] ids) {
		fTargetPlatform.setBinaryParserList(ids);
	}

	public String getId() {
		return fTargetPlatform.getId();
	}

	public String getName() {
		return fTargetPlatform.getName();
	}

	public boolean isValid() {
		//TODO:
		return true;
	}

	public void setName(String name) {
		fTargetPlatform.setName(name);
	}

}
