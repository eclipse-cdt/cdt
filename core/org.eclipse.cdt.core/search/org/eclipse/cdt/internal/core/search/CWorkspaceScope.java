/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.search;

import java.util.HashSet;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

public class CWorkspaceScope extends CSearchScope {
	
	protected boolean needsInitialize;

	public boolean encloses(String resourcePath) {
		//Workspace scope encloses all elements in the workspace -
		//as long as we add the proper projects to enclosingProjects,
		//we  can return true for all paths
		return true;
	}

	public boolean encloses(ICElement element) {
		//Workspace scope encloses all elements in the workspace -
		//as long as we add the proper projects to enclosingProjects,
		//we  can return true for all paths
		return true;
	}

	public IPath[] enclosingProjects() {
		if (this.needsInitialize) {
			this.initialize();
		}
		return super.enclosingProjects();
	}
	
	public void initialize() {
		super.initialize();
		try {
		IProject[] projects = CCorePlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0, length = projects.length; i < length; i++)
				this.add(projects[i], false, new HashSet(2));
		} catch (CModelException e) {
			// ignore
		}
		this.needsInitialize = false;
	}
	

	public void processDelta(ICElementDelta delta) {
//TODO: BOG Hook this up to the model manager to give us updates when the workspace changes
//		if (this.needsInitialize) return;
//		ICElement element = delta.getElement();
//		switch (element.getElementType()) {
//			case ICElement.C_MODEL:
//				ICElementDelta[] children = delta.getAffectedChildren();
//				for (int i = 0, length = children.length; i < length; i++) {
//					ICElementDelta child = children[i];
//					this.processDelta(child);
//				}
//				break;
//			case ICElement.C_PROJECT:
//				int kind = delta.getKind();
//				switch (kind) {
//					case ICElementDelta.ADDED:
//					case ICElementDelta.REMOVED:
//						this.needsInitialize = true;
//						break;
//					case ICElementDelta.CHANGED:
//						children = delta.getAffectedChildren();
//						for (int i = 0, length = children.length; i < length; i++) {
//							ICElementDelta child = children[i];
//							this.processDelta(child);
//						}
//						break;
//				}
//				break;
//		}
	}
	
	
	public String toString() {
		return "CWorkspaceScope"; //$NON-NLS-1$
	}

}
