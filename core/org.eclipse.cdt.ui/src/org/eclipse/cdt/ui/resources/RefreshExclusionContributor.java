/*******************************************************************************
 *  Copyright (c) 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.resources;

import org.eclipse.swt.widgets.Composite;
import org.w3c.dom.Element;

import org.eclipse.cdt.core.resources.RefreshExclusion;

/**
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the CDT team.
 * 
 * @author crecoskie
 *
 */
public abstract class RefreshExclusionContributor {
	
	protected String fID;
	
	public String getID() {
		return fID;
	}
	
	public void setID(String id) {
		fID = id;
	}
	
	abstract public RefreshExclusion createExclusion();
	abstract public void createProperiesUI(Composite parent);
	abstract public RefreshExclusion createExclusionFromXML(Element exclusionElement);
}
