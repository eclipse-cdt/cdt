/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core.pdom;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.core.runtime.CoreException;

/**
 * CModel listener used for the PDOMManager.
 * @since 4.0
 */
public class CModelListener implements IElementChangedListener {

	private PDOMManager fManager;

	public CModelListener(PDOMManager manager) {
		fManager= manager;
	}
	
	public void elementChanged(ElementChangedEvent event) {
		// Only respond to post change events
		if (event.getType() != ElementChangedEvent.POST_CHANGE)
			return;
		
		// Walk the delta sending the subtrees to the appropriate indexers
		try { 
			processDelta(event.getDelta());
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}
	
	private void processDelta(ICElementDelta delta) throws CoreException {
		int type = delta.getElement().getElementType();
		switch (type) {
		case ICElement.C_MODEL:
			// Loop through the children
			ICElementDelta[] children = delta.getAffectedChildren();
			for (int i = 0; i < children.length; ++i)
				processDelta(children[i]);
			break;
		case ICElement.C_PROJECT:
			// Find the appropriate indexer and pass the delta on
			final ICProject project = (ICProject)delta.getElement();
			switch (delta.getKind()) {
			case ICElementDelta.ADDED:
				fManager.addProject(project.getProject());
		    	break;
			case ICElementDelta.CHANGED:
				fManager.changeProject(project, delta);
				break;
			case ICElementDelta.REMOVED:
				// handled on pre-close and pre-delete
				break;
			}
		}
	}
}
