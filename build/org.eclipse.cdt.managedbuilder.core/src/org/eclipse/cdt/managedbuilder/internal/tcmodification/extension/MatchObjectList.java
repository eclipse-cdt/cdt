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
package org.eclipse.cdt.managedbuilder.internal.tcmodification.extension;

import org.eclipse.core.runtime.IConfigurationElement;

public class MatchObjectList {
	private MatchObjectElement[] fObjects;
	
	public MatchObjectList(IConfigurationElement element) throws IllegalArgumentException {
		IConfigurationElement[] objectEls = element.getChildren(MatchObjectElement.ELEMENT_NAME);
		MatchObjectElement[] objects = new MatchObjectElement[objectEls.length];
		for(int i = 0; i < objectEls.length; i++){
			objects[i] = new MatchObjectElement(objectEls[i]);
		}
		fObjects = objects;
	}

	public MatchObjectElement[] getMatchObjects(){
		return fObjects.clone();
	}

}
