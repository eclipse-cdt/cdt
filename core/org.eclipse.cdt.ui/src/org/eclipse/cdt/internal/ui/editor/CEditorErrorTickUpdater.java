/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;


import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;

/**
 * The <code>JavaEditorErrorTickUpdater</code> will register as a AnnotationModelListener
 * on the annotation model of a Java Editor and update the title images when the annotation
 * model changed.
 */
public class CEditorErrorTickUpdater implements IAnnotationModelListener {

	protected CEditor fCEditor;
	private IAnnotationModel fAnnotationModel;
	private CElementLabelProvider fLabelProvider;

	public CEditorErrorTickUpdater(CEditor editor) {
		fCEditor= editor;
		Assert.isNotNull(editor);
	}

	/**
	 * Defines the annotation model to listen to. To be called when the
	 * annotation model changes.
	 * @param model The new annotation model or <code>null</code>
	 * to uninstall.
	 */
	public void setAnnotationModel(IAnnotationModel model) {
		if (fAnnotationModel != null) {
			fAnnotationModel.removeAnnotationModelListener(this);
		}
				
		if (model != null) {
			if (fLabelProvider == null) {
				fLabelProvider= new CElementLabelProvider(CElementLabelProvider.SHOW_SMALL_ICONS, CElementLabelProvider.getAdornmentProviders(true, null));
			}
			fAnnotationModel=model;
			fAnnotationModel.addAnnotationModelListener(this);
			modelChanged(fAnnotationModel);
		} else {
			if (fLabelProvider != null) {
				fLabelProvider.dispose();
			}
			fLabelProvider= null;
			fAnnotationModel= null;
		}	
	}
			
	/*
	 * @see IAnnotationModelListener#modelChanged(IAnnotationModel)
	 */
	public void modelChanged(IAnnotationModel model) {
		Image titleImage= fCEditor.getTitleImage();
		if (titleImage == null) {
			return;
		}
		IEditorInput input= fCEditor.getEditorInput();
		if (input != null) { // might run async, tests needed
			ICElement celement= (ICElement) input.getAdapter(ICElement.class);
			if (fLabelProvider != null && celement != null) {
				Image newImage= fLabelProvider.getImage(celement);
				if (titleImage != newImage) {
					updatedTitleImage(newImage);
				}
			}
		}
	}
	
	private void updatedTitleImage(final Image newImage) {
		Shell shell= fCEditor.getEditorSite().getShell();
		if (shell != null && !shell.isDisposed()) {
			shell.getDisplay().syncExec(new Runnable() {
				public void run() {
					fCEditor.updatedTitleImage(newImage);
				}
			});
		}
	}	
	
	public void dispose() {
		if (fLabelProvider != null) {
			fLabelProvider.dispose();
			fLabelProvider= null;
		}
		fAnnotationModel= null;
	}
}




