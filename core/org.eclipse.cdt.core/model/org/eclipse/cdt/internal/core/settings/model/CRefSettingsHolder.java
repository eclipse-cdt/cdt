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
package org.eclipse.cdt.internal.core.settings.model;

import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.internal.core.settings.model.CExternalSettingsManager.CContainerRef;

public class CRefSettingsHolder extends CExternalSettingsHolder {
	private static final String ATTR_FACTORY_ID = "factoryId"; //$NON-NLS-1$
	private static final String ATTR_CONTAINER_ID = "containerId"; //$NON-NLS-1$
	private CContainerRef fContainerRef;
	private boolean fIsReconsiled;

	public CRefSettingsHolder(CContainerRef ref) {
		super();
		
		fContainerRef = ref;
	}

	public CRefSettingsHolder(CRefSettingsHolder base) {
		super(base);

		fContainerRef = base.fContainerRef;
		fIsReconsiled = base.fIsReconsiled;
	}

	public CRefSettingsHolder(ICStorageElement element) {
		super(element);

		String factoryId = element.getAttribute(ATTR_FACTORY_ID); 
		String containerId = element.getAttribute(ATTR_CONTAINER_ID);
		
		fContainerRef = new CContainerRef(factoryId, containerId);
	}

	public CContainerRef getContainerInfo(){
		return fContainerRef;
	}
	
	public boolean isReconsiled(){
		return fIsReconsiled;
	}
	
	public void setReconsiled(boolean s){
		fIsReconsiled = s;
	}

	public void serialize(ICStorageElement el) {
		super.serialize(el);
		el.setAttribute(ATTR_FACTORY_ID, fContainerRef.getFactoryId());
		el.setAttribute(ATTR_CONTAINER_ID, fContainerRef.getContainerId());
	}
}
