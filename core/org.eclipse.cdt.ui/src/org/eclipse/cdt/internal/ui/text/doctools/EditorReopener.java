/*******************************************************************************
 * Copyright (c) 2008, 2009 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.doctools;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.WorkbenchJob;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.doctools.IDocCommentOwner;
import org.eclipse.cdt.ui.text.doctools.IDocCommentOwnershipListener;

import org.eclipse.cdt.internal.ui.editor.ITranslationUnitEditorInput;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.internal.ui.util.ExternalEditorInput;

/**
 * Listens to change in doc-comment ownership and reinitializes
 * the editors (or a safe superset of) that need reopening.
 */
public class EditorReopener implements IDocCommentOwnershipListener {

	/*
	 * @see org.eclipse.cdt.ui.text.doctools.IDocCommentOwnershipListener#ownershipChanged(org.eclipse.core.resources.IResource, boolean, org.eclipse.cdt.ui.text.doctools.IDocCommentOwner, org.eclipse.cdt.ui.text.doctools.IDocCommentOwner)
	 */
	public void ownershipChanged(IResource resource, boolean recursive,
			IDocCommentOwner old, IDocCommentOwner newOwner) {
		IWorkbenchWindow window= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if(window!=null) {
			try {
				IEditorPart[] parts= getEditorsToRepon(window, resource);
				if(queryIfNeeded(window.getShell(), parts)) {
					reopenEditors(window, parts);
				}
			} catch(CoreException ce) {
				CUIPlugin.log(ce);
			}
		}
	}

	/*
	 * @see org.eclipse.cdt.ui.text.doctools.IDocCommentOwnershipListener#workspaceOwnershipChanged(org.eclipse.cdt.ui.text.doctools.IDocCommentOwner, org.eclipse.cdt.ui.text.doctools.IDocCommentOwner)
	 */
	public void workspaceOwnershipChanged(IDocCommentOwner old, IDocCommentOwner newOwner) {
		IWorkbenchWindow window= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if(window!=null) {
			try {
				IEditorPart[] parts= getEditorsToRepon(window, null);
				if(queryIfNeeded(window.getShell(), parts)) {
					reopenEditors(window, parts);
				}
			} catch(CoreException ce) {
				CUIPlugin.log(ce);
			}
		}
	}

	/**
	 * @param window
	 * @param resource may be null to indicate all CDT editors should be reopened
	 * @return an array of {@link IEditorPart} objects that might need to be reinitialized
	 * based on doc-comment ownership of the specified resource changing
	 */
	/*
	 * This could be smarter in determining smaller sets of editors to re-open
	 */
	private IEditorPart[] getEditorsToRepon(IWorkbenchWindow window, IResource resource) {
		List<IEditorPart> needReopening= new ArrayList<IEditorPart>();
		if(window.getActivePage()!=null) {
			IEditorReference[] es= window.getActivePage().getEditorReferences();
			for(int i=0; i<es.length; i++) {
				IEditorPart part= es[i].getEditor(false);
				if(part!=null) {
					IEditorInput iei= part.getEditorInput();
					if(resource!=null) {
						if(iei instanceof IFileEditorInput) {
							IFile file= ((IFileEditorInput) iei).getFile();
							IProject project= resource.getProject();
							if(file.getProject().equals(project) && CoreModel.hasCNature(project)) {
								needReopening.add(part);
							}
						}
					} else {
						if(iei instanceof ITranslationUnitEditorInput || iei instanceof IFileEditorInput) {
							needReopening.add(part);
						}
					}
				}
			}
		}
		return needReopening.toArray(new IEditorPart[needReopening.size()]);
	}

	private boolean queryIfNeeded(Shell shell, IEditorPart[] editorParts) throws CoreException {
		boolean anyUnsaved= false;
		for(int j=0; j<editorParts.length; j++)
			if(editorParts[j].isSaveOnCloseNeeded())
				anyUnsaved= true;

		boolean saveAndReopen= !anyUnsaved;
		if(anyUnsaved) {
			String title= Messages.EditorReopener_ShouldSave_Title;
			String msg= Messages.EditorReopener_ShouldSave_Message; 

			if (MessageDialog.openQuestion(shell, title, msg))
				saveAndReopen= true;
		}
		return saveAndReopen;
	}

	private void reopenEditors(final IWorkbenchWindow window, final IEditorPart[] editorParts) throws CoreException {
		WorkbenchJob job= new WorkbenchJob(Messages.EditorReopener_ReopenJobStart) {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				IEditorPart oldActive= window.getActivePage().getActiveEditor();
				IEditorPart newActive= null;

				for(int j=0; j<editorParts.length; j++) {
					IEditorPart oldPart= editorParts[j];
					if(oldPart.isDirty()) {
						oldPart.doSave(new NullProgressMonitor());
					}
					window.getActivePage().closeEditor(oldPart, false);
					IEditorInput oldInput= oldPart.getEditorInput();

					try {
						IEditorPart newPart= null;
						if(oldInput instanceof IFileEditorInput) {
							newPart= EditorUtility.openInEditor(((IFileEditorInput)oldInput).getFile());
						} else if(oldInput instanceof ExternalEditorInput) {
							ExternalEditorInput eei= (ExternalEditorInput) oldInput;
							ICElement element= CoreModel.getDefault().create(eei.getMarkerResource());
							newPart= EditorUtility.openInEditor(eei.getPath(), element);
						}
						if(oldPart == oldActive)
							newActive= newPart;
					} catch(PartInitException pie) {
						CUIPlugin.log(pie);
					} catch(CModelException cme) {
						CUIPlugin.log(cme);
					}
				}

				if(newActive != null) {
					window.getActivePage().activate(newActive);
				}
				return new Status(IStatus.OK, CUIPlugin.PLUGIN_ID, Messages.EditorReopener_ReopenJobComplete);
			}
		};
		job.schedule();
	}
}
