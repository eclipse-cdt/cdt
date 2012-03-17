/*******************************************************************************
 * Copyright (c) 2012 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal;

import org.eclipse.cdt.internal.core.ICoreInfo;

public class CoreInfo implements ICoreInfo {
	private String fId;
	private String fPhysicalId;
	
	public CoreInfo(String id, String pId) {
		fId = id;
		fPhysicalId = pId;
	}

	@Override
	public String getId() {
		return fId;
	}

	@Override
	public String getPhysicalId() {
		return fPhysicalId;
	}		
}