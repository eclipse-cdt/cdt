/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search.actions;

import java.util.Iterator;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;

public class AddToIndexAction extends Action {
	
	protected IWorkbenchSite fSite;
	protected CEditor fEditor;
	
	
	public AddToIndexAction(CEditor editor, String label, String tooltip){
		fEditor = editor;
		setText(label); //$NON-NLS-1$
		setToolTipText(tooltip); //$NON-NLS-1$
	}
	
	public AddToIndexAction(CEditor editor){
		this(editor,
			CSearchMessages.getString("ActionDefinition.addToIndex.name"), //$NON-NLS-1$
			CSearchMessages.getString("ActionDefinition.addToIndex.description")); //$NON-NLS-1$
	}
	
	public AddToIndexAction(IWorkbenchSite site){
		this(site,
			CSearchMessages.getString("ActionDefinition.addToIndex.name"), //$NON-NLS-1$
			CSearchMessages.getString("ActionDefinition.addToIndex.description")); //$NON-NLS-1$
	}

	public AddToIndexAction(IWorkbenchSite site, String label, String tooltip) {
		fSite = site;
		setText(label);
		setToolTipText(tooltip);
	}
	
	public void run() {
		if (fEditor != null){
			ICElement celement= fEditor.getInputCElement();
			IResource resource = celement.getUnderlyingResource();
			if (resource != null){
				IProject project = resource.getProject();
				if (project != null)
					CCorePlugin.getDefault().getCoreModel().getIndexManager().addResource(project,resource);
			}
		} else if ( fSite != null) {
			if (fSite.getSelectionProvider() != null ){
				IStructuredSelection sel = (IStructuredSelection) fSite.getSelectionProvider().getSelection();
				Iterator iter = sel.iterator();
				while (iter.hasNext()){
					Object element = iter.next();
					if (element instanceof ICElement){
						ICElement cel = (ICElement) element;
						IProject proj = cel.getCProject().getProject();
						IResource resource =cel.getResource();
						if (proj!= null &&
							resource != null){
							CCorePlugin.getDefault().getCoreModel().getIndexManager().addResource(proj,resource);
						}
					} 
					//Can be added to deal with external files -> but need indexer addResourceByPath support
					/*else if (element instanceof ITranslationUnit){
						ITranslationUnit trans = (ITranslationUnit) element;
						IProject proj = trans.getCProject().getProject();
						IPath path=trans.getPath();
						CCorePlugin.getDefault().getCoreModel().getIndexManager().addResourceByPath(proj,path, ICDTIndexer.COMPILATION_UNIT);
					}*/ 
					
				}
			
			}
		}
	}

	
}
