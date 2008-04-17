/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.c.hover;

import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.IInformationProviderExtension2;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

import org.eclipse.cdt.ui.text.c.hover.ICEditorTextHover;


/**
 * Provides information for the current word under the cursor based on the documentation hover.
 * 
 * @see CDocHover
 * @since 5.0
 */
public class CInformationProvider implements IInformationProvider, IInformationProviderExtension2 {

	/**
	 * Default control creator.
	 */
	private static final class ControlCreator extends AbstractReusableInformationControlCreator {
		@Override
		public IInformationControl doCreateInformationControl(Shell parent) {
			return new DefaultInformationControl(parent);
		}
	}
	

	/**
	 * Part listener handling editor close.
	 */
	class EditorWatcher implements IPartListener {
		public void partOpened(IWorkbenchPart part) {
		}
		public void partDeactivated(IWorkbenchPart part) {
		}
		public void partClosed(IWorkbenchPart part) {
			if (part == fEditor) {
				fEditor.getSite().getWorkbenchWindow().getPartService().removePartListener(fPartListener);
				fPartListener= null;
			}
		}
		public void partActivated(IWorkbenchPart part) {
		}
		public void partBroughtToTop(IWorkbenchPart part) {
		}
	}

	protected IEditorPart fEditor;
	protected IPartListener fPartListener;

	protected ICEditorTextHover fImplementation;

	/**
	 * The default presentation control creator.
	 */
	private IInformationControlCreator fPresenterControlCreator;
	

	public CInformationProvider(IEditorPart editor) {

		fEditor= editor;

		if (fEditor != null) {
			fPartListener= new EditorWatcher();
			IWorkbenchWindow window= fEditor.getSite().getWorkbenchWindow();
			window.getPartService().addPartListener(fPartListener);
			fImplementation= new CDocHover();
			fImplementation.setEditor(fEditor);
		}
	}

	/*
	 * @see IInformationProvider#getSubject(ITextViewer, int)
	 */
	public IRegion getSubject(ITextViewer textViewer, int offset) {
		if (textViewer != null && fImplementation != null) {
			return fImplementation.getHoverRegion(textViewer, offset);
		}
		return null;
	}

	/*
	 * @see IInformationProvider#getInformation(ITextViewer, IRegion)
	 */
	@SuppressWarnings("deprecation")
	public String getInformation(ITextViewer textViewer, IRegion subject) {
		if (fImplementation != null) {
			String s= fImplementation.getHoverInfo(textViewer, subject);
			if (s != null && s.trim().length() > 0) {
				return s;
			}
		}
		return null;
	}

	/*
	 * @see IInformationProviderExtension2#getInformationPresenterControlCreator()
	 */
	public IInformationControlCreator getInformationPresenterControlCreator() {
		if (fPresenterControlCreator == null)
			fPresenterControlCreator= new ControlCreator();
		return fPresenterControlCreator;
	}
}
