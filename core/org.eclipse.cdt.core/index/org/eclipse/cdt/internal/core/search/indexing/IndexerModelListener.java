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
package org.eclipse.cdt.internal.core.search.indexing;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.internal.core.model.SourceRoot;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

public class IndexerModelListener implements IElementChangedListener {

	private static IndexerModelListener indexerModelListener;
	private static IndexManager indexManager;
	
	private IndexerModelListener() {}

	/**
	 * Return the singleton.
	 */
	public static synchronized IndexerModelListener getDefault() {
		if (indexerModelListener == null) {
			indexerModelListener = new IndexerModelListener();
			CoreModel.getDefault().addElementChangedListener(indexerModelListener);
			indexManager = CoreModel.getDefault().getIndexManager();
		}
		return indexerModelListener;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IElementChangedListener#elementChanged(org.eclipse.cdt.core.model.ElementChangedEvent)
	 */
	public void elementChanged(ElementChangedEvent event) {
		try {
			processDelta(event.getDelta());
		} catch(CModelException e) {
		}
	}
	
	protected void processDelta(ICElementDelta delta) throws CModelException {
		int kind= delta.getKind();
		int flags= delta.getFlags();
		ICElement element= delta.getElement();
		
		switch(kind){		
			case ICElementDelta.CHANGED:
				if ((flags & ICElementDelta.F_CHANGED_PATHENTRY_INCLUDE) != 0 ||
					(flags & ICElementDelta.F_CHANGED_PATHENTRY_MACRO) != 0 ||
					(flags & ICElementDelta.F_ADDED_PATHENTRY_SOURCE) != 0 ){
					IResource tempResource = element.getResource();
					SourceRoot tempRootElement = null;
					
					if (tempResource == null)
						return;
					
					switch(tempResource.getType())
					{
						case IResource.FILE:
						indexManager.addSource((IFile) tempResource,tempResource.getProject().getFullPath());
						break;
						
						case IResource.FOLDER:
						tempRootElement = (SourceRoot) getElementSource(element);
						if (tempRootElement != null){	
						 IProject theProj = tempResource.getProject();
						 indexManager.indexSourceFolder(theProj,tempResource.getFullPath(),tempRootElement.getSourceEntry().fullExclusionPatternChars());
						}
						break;
						
						case IResource.PROJECT:
						indexManager.indexAll(tempResource.getProject());
						break;
					}
					
				} else if( (flags & ICElementDelta.F_REMOVED_PATHENTRY_SOURCE) != 0 ){
					IResource tempResource = element.getResource();
					IProject project = tempResource.getProject();
					if( indexManager.indexProblemsEnabled(project) != 0 ){
						indexManager.removeIndexerProblems( tempResource );
					}
				}
			break;			
		}

		ICElementDelta[] affectedChildren= delta.getAffectedChildren();
		for (int i= 0; i < affectedChildren.length; i++) {
			processDelta(affectedChildren[i]);
		}
	}

	ICElement getElementSource(ICElement element){
		
		if (element instanceof SourceRoot){
			return element;
		}
		
		if (element.getParent() != null){
			return getElementSource(element.getParent());
		}
		
		return null;
		
	}
	
	public void shutdown(){
		if (indexerModelListener  != null)
			CoreModel.getDefault().removeElementChangedListener(indexerModelListener);
	}
	
}
