/*******************************************************************************
 * Copyright (c) 2000, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;


import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.util.IProblemChangedListener;
import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;
import org.eclipse.cdt.internal.ui.viewsupport.CUILabelProvider;
import org.eclipse.cdt.internal.ui.viewsupport.ProblemsLabelDecorator;

/**
 * The <code>JavaEditorErrorTickUpdater</code> will register as a AnnotationModelListener
 * on the annotation model of a Java Editor and update the title images when the annotation
 * model changed.
 */
public class CEditorErrorTickUpdater implements IProblemChangedListener {

	/**
	 * Provider for the editor title image. Marks external files with a folder overlay.
	 *
	 * @since 5.0
	 */
	private static class CEditorImageProvider extends CUILabelProvider {
		CEditorImageProvider() {
			super(0, CElementImageProvider.SMALL_ICONS);
		}
		@Override
		protected int evaluateImageFlags(Object element) {
			int flags= getImageFlags();
			if (element instanceof ITranslationUnit) {
				ITranslationUnit tUnit= (ITranslationUnit) element;
				if (tUnit.getResource() == null) {
					flags |= CElementImageProvider.OVERLAY_EXTERNAL;
				}
			}
			return flags;
		}
	}

	protected CEditor fCEditor;
	private CUILabelProvider fLabelProvider;

	public CEditorErrorTickUpdater(CEditor editor) {
		Assert.isNotNull(editor);
		fCEditor= editor;
		fLabelProvider=  new CEditorImageProvider();
		fLabelProvider.addLabelDecorator(new ProblemsLabelDecorator(null));
		CUIPlugin.getDefault().getProblemMarkerManager().addListener(this);
	}

	
	/* (non-Javadoc)
	 * @see IProblemChangedListener#problemsChanged(IResource[], boolean)
	 */
	@Override
	public void problemsChanged(IResource[] resourcesChanged, boolean isMarkerChange) {
		if (!isMarkerChange) {
			return;
		}
		IEditorInput input= fCEditor.getEditorInput();
		if (input != null) { // might run async, tests needed
			ICElement celement= fCEditor.getInputCElement();
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
				@Override
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
