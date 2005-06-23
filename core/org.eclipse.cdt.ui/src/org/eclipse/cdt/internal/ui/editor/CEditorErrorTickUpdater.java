/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;


import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.util.IProblemChangedListener;
import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;
import org.eclipse.cdt.internal.ui.viewsupport.CUILabelProvider;
import org.eclipse.cdt.internal.ui.viewsupport.ProblemsLabelDecorator;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;

/**
 * The <code>JavaEditorErrorTickUpdater</code> will register as a AnnotationModelListener
 * on the annotation model of a Java Editor and update the title images when the annotation
 * model changed.
 */
public class CEditorErrorTickUpdater implements IProblemChangedListener {

	protected CEditor fCEditor;
	private CUILabelProvider fLabelProvider;

	public CEditorErrorTickUpdater(CEditor editor) {
		Assert.isNotNull(editor);
		fCEditor= editor;
		fLabelProvider=  new CUILabelProvider(0, CElementImageProvider.SMALL_ICONS);
		fLabelProvider.addLabelDecorator(new ProblemsLabelDecorator(null));
		CUIPlugin.getDefault().getProblemMarkerManager().addListener(this);
	}

	
	/* (non-Javadoc)
	 * @see IProblemChangedListener#problemsChanged(IResource[], boolean)
	 */
	public void problemsChanged(IResource[] resourcesChanged, boolean isMarkerChange) {
		if (isMarkerChange) {
			return;
		}
		IEditorInput input= fCEditor.getEditorInput();
		if (input != null) { // might run async, tests needed
			ICElement celement= (ICElement) input.getAdapter(ICElement.class);
			if (celement != null) {
				IResource resource= celement.getResource();
				if (resource == null) {
					return;
				}
				for (int i = 0; i < resourcesChanged.length; i++){
					if (resource.equals(resourcesChanged[i])) {
						updateEditorImage(celement);
						return;
					}
				}
			}
		}
	}	
			
	public void updateEditorImage(ICElement celement) {
		Image titleImage= fCEditor.getTitleImage();
		if (titleImage == null) {
			return;
		}
		Image newImage= fLabelProvider.getImage(celement);
		if (titleImage != newImage) {
			postImageChange(newImage);
		}
	}
	
	private void postImageChange(final Image newImage) {
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
		fLabelProvider.dispose();
		CUIPlugin.getDefault().getProblemMarkerManager().removeListener(this);
	}
}
