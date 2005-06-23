/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.impl;

import java.util.List;

import org.eclipse.cdt.core.index.IIndexDelta;
import org.eclipse.core.resources.IProject;

public class IndexDelta implements IIndexDelta {

	private List files = null;
	private IProject project = null;
	private IndexDeltaType deltaType = null; 
	
	/**
	 * @param filesTrav
	 * @param project
	 * 
	 */
	public IndexDelta(IProject project, List filesTrav) {
		this(project,filesTrav,null);
	}

	/**
	 * @param filesTrav
	 * @param project
	 * 
	 */
	public IndexDelta(IProject project, List filesTrav, IndexDeltaType indexDeltaType) {
		this.project = project;
		this.files = filesTrav;
		this.deltaType = indexDeltaType;
	}
	
	/**
	 * @return Returns the files.
	 */
	public List getFiles() {
		return files;
	}
	/**
	 * @return Returns the project.
	 */
	public IProject getProject() {
		return project;
	}

	/**
	 * @see org.eclipse.cdt.core.index.IIndexDelta#getDeltaType()
	 */
	public IndexDeltaType getDeltaType() {
		return deltaType;
	}
}
