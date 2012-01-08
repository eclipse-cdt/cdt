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
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.c.hover;

import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.IInformationProviderExtension;
import org.eclipse.jface.text.information.IInformationProviderExtension2;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Provides information for the current word under the cursor based on the documentation hover
 * and spelling correction hover.
 * 
 * @see CTypeHover
 * @since 5.0
 */
public class CInformationProvider implements IInformationProvider, IInformationProviderExtension, IInformationProviderExtension2 {

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
		@Override
		public void partOpened(IWorkbenchPart part) {
		}
		@Override
		public void partDeactivated(IWorkbenchPart part) {
		}
		@Override
		public void partClosed(IWorkbenchPart part) {
			if (part == fEditor) {
				fEditor.getSite().getWorkbenchWindow().getPartService().removePartListener(fPartListener);
				fPartListener= null;
			}
		}
		@Override
		public void partActivated(IWorkbenchPart part) {
		}
		@Override
		public void partBroughtToTop(IWorkbenchPart part) {
		}
	}

	protected IEditorPart fEditor;
	protected IPartListener fPartListener;

	protected CTypeHover fImplementation;

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
			fImplementation= new CTypeHover();
			fImplementation.setEditor(fEditor);
		}
	}

	/*
	 * @see IInformationProvider#getSubject(ITextViewer, int)
	 */
	@Override
	public IRegion getSubject(ITextViewer textViewer, int offset) {
		if (textViewer != null && fImplementation != null) {
			return fImplementation.getHoverRegion(textViewer, offset);
		}
		return null;
	}

	/*
	 * @see IInformationProvider#getInformation(ITextViewer, IRegion)
	 */
	@Override
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
	 * @see org.eclipse.jface.text.information.IInformationProviderExtension#getInformation2(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
	 */
	@Override
	public Object getInformation2(ITextViewer textViewer, IRegion subject) {
		if (fImplementation == null)
			return null;
		return fImplementation.getHoverInfo2(textViewer, subject);
	}

	/*
	 * @see IInformationProviderExtension2#getInformationPresenterControlCreator()
	 */
	@Override
	public IInformationControlCreator getInformationPresenterControlCreator() {
		if (fImplementation != null) {
			return ((IInformationProviderExtension2) fImplementation).getInformationPresenterControlCreator();
		}
		if (fPresenterControlCreator == null)
			fPresenterControlCreator= new ControlCreator();
		return fPresenterControlCreator;
	}
}
