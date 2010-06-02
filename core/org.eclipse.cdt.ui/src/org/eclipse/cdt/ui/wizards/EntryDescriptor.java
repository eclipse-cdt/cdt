/*******************************************************************************
 * Copyright (c) 2007, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.wizards;

import org.eclipse.swt.graphics.Image;

import org.eclipse.cdt.core.settings.model.util.CDataUtil;

/**
 * This class stores data for each tree item
 * in "Project types" tree of New Project Wizard.
 */
public final class EntryDescriptor {
	private String id = null;
	private String name = null;
	private boolean isCategory = false;
	private String parentId = null;
	private Image image = null;
	private CWizardHandler handler = null;
	private String path = null;
	private EntryDescriptor parent = null;
	private boolean isDefaultForCategory = false;

	public EntryDescriptor(String _id, String _par, String _name, boolean _cat, CWizardHandler _h, Image _image) {
		id = _id;
		parentId = _par;
		name = _name;
		isCategory = _cat;
		handler = _h;
		image = _image;
	}
	// these parameters are set in constructor only
	public String getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public boolean isCategory() {
		return isCategory;
	}
	public Image getImage() {
		return image;
	}

	// these parameters can be set anywhere
	public void setParentId(String pId) {
		parentId = pId;
	}
	public String getParentId() {
		return parentId;
	}
	
	public void setPath(String p) { 
		path = p; 
	}
	public String getPath() { 
		return path; 
	}

	public String[] getPathArray() { 
		return CDataUtil.stringToArray(path, "/");  //$NON-NLS-1$
	}

	public void setParent(EntryDescriptor p) { 
		parent = p; 
	}
	public EntryDescriptor getParent() { 
		return parent; 
	}

	public void setHandler(CWizardHandler h) {
		handler = h;
	}
	public CWizardHandler getHandler() {
		return handler;
	}
	/**
	 * @since 5.1
	 */
	public void setDefaultForCategory(boolean isDefaultForCategory) {
		this.isDefaultForCategory = isDefaultForCategory;
	}
	/**
	 * @since 5.1
	 */
	public boolean isDefaultForCategory() {
		return isDefaultForCategory;
	}
}
