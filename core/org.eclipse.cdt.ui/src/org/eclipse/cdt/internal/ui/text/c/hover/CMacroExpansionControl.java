/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.c.hover;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.keys.IBindingService;

import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.text.AbstractSourceViewerInformationControl;

/**
 * Information control for macro expansion exploration.
 *
 * @since 5.0
 */
public class CMacroExpansionControl extends AbstractSourceViewerInformationControl {

	private static final String COMMAND_ID_EXPANSION_BACK= "org.eclipse.cdt.ui.hover.backwardMacroExpansion"; //$NON-NLS-1$
	private static final String COMMAND_ID_EXPANSION_FORWARD= "org.eclipse.cdt.ui.hover.forwardMacroExpansion"; //$NON-NLS-1$
	private static final String CONTEXT_ID_MACRO_EXPANSION_HOVER= "org.eclipse.cdt.ui.macroExpansionHoverScope"; //$NON-NLS-1$

	private IHandlerService fHandlerService;
	private Collection fHandlerActivations;
	private IContextService fContextService;
	private IContextActivation fContextActivation;
	private int fIndex;
	private CMacroExpansionInput fInput;

	/**
	 * Creates a new control for use as a "quick view" where the control immediately takes the focus.
	 * 
	 * @param parent  parent shell
	 * @param shellStyle  shell style bits
	 * @param style  text viewer style bits
	 * @param input  the input object, may be <code>null</code>
	 */
	public CMacroExpansionControl(Shell parent, int shellStyle, int style, CMacroExpansionInput input) {
		super(parent, shellStyle, style, true, false, true);
		setMacroExpansionInput(input);
	}

	/**
	 * Creates a new control for use as a hover which does not take the focus.
	 * 
	 * @param parent  parent shell
	 * @param statusFieldText  text to be displayed in the status field, may be <code>null</code>
	 */
	public CMacroExpansionControl(Shell parent, String statusFieldText) {
		super(parent, SWT.NO_TRIM | SWT.TOOL, SWT.NONE, false, false, false);
		if (statusFieldText != null) {
			setInfoText(statusFieldText);
		}
		setTitleText(CHoverMessages.CMacroExpansionControl_title_macroExpansion);
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.AbstractSourceViewerInformationControl#hasHeader()
	 */
	protected boolean hasHeader() {
		return true;
	}

	/*
	 * @see org.eclipse.jface.dialogs.PopupDialog#open()
	 */
	public int open() {
		int result= super.open();

		if (fInput != null) {
	        IHandler fBackwardHandler= new AbstractHandler() {
	            public Object execute(ExecutionEvent event) throws ExecutionException {
	                backward();
	                return null;
	            }
	        };
	        IHandler fForwardHandler= new AbstractHandler() {
	            public Object execute(ExecutionEvent event) throws ExecutionException {
	                forward();
	                return null;
	            }
	        };
	
	        IWorkbench workbench= PlatformUI.getWorkbench();
	        fHandlerService= (IHandlerService) workbench.getService(IHandlerService.class);
	        fContextService= (IContextService) workbench.getService(IContextService.class);
	        fContextActivation= fContextService.activateContext(CONTEXT_ID_MACRO_EXPANSION_HOVER);
	        fHandlerActivations= new ArrayList();
	        fHandlerActivations.add(fHandlerService.activateHandler(COMMAND_ID_EXPANSION_BACK, fBackwardHandler));
	        fHandlerActivations.add(fHandlerService.activateHandler(COMMAND_ID_EXPANSION_FORWARD, fForwardHandler));

	        String infoText= getInfoText();
	        if (infoText != null) {
	            setInfoText(infoText);
	        }
		}
		
        return result;
	}

	protected void forward() {
		++fIndex;
		if (fIndex >= fInput.fExpansions.length) {
			fIndex= 0;
		}
		showExpansion(fIndex);
	}

	protected void backward() {
		--fIndex;
		if (fIndex < 0) {
			fIndex += fInput.fExpansions.length;
		}
		showExpansion(fIndex);
	}

	/**
	 * Returns the text to be shown in the popups's information area. 
	 * May return <code>null</code>.
	 *
	 * @return The text to be shown in the popup's information area or <code>null</code>
	 */
	protected String getInfoText() {
		IWorkbench workbench= PlatformUI.getWorkbench();
		IBindingService bindingService= (IBindingService) workbench.getService(IBindingService.class);
		String formattedBindingBack= bindingService.getBestActiveBindingFormattedFor(COMMAND_ID_EXPANSION_BACK);
		String formattedBindingForward= bindingService.getBestActiveBindingFormattedFor(COMMAND_ID_EXPANSION_FORWARD);

		String infoText= null;
		if (formattedBindingBack != null && formattedBindingForward != null) {
			infoText= NLS.bind(CHoverMessages.CMacroExpansionControl_statusText, formattedBindingBack, formattedBindingForward); 
		}
		return infoText;
	}

	/*
	 * @see org.eclipse.jface.dialogs.PopupDialog#close()
	 */
	public boolean close() {
		if (fHandlerService != null) {
			fHandlerService.deactivateHandlers(fHandlerActivations);
			fHandlerActivations.clear();
			fHandlerService= null;
		}
		if (fContextActivation != null) {
			fContextService.deactivateContext(fContextActivation);
		}
		return super.close();
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.AbstractSourceViewerInformationControl#getId()
	 */
	protected String getId() {
		return "org.eclipse.cdt.ui.text.hover.CMacroExpansion"; //$NON-NLS-1$
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.AbstractSourceViewerInformationControl#setInput(java.lang.Object)
	 */
	public void setInput(Object input) {
		if (input instanceof String && fInput == null) {
			super.setInput(input);
			return;
		}
		if (input instanceof CMacroExpansionInput) {
			setMacroExpansionInput((CMacroExpansionInput) input);
		}
	}

	/**
	 * Set the input for this information control.
	 * @param input
	 */
	private void setMacroExpansionInput(CMacroExpansionInput input) {
		fInput= input;
		fIndex= input.fExpansions.length - 1;
		showExpansion(fIndex);
	}

	private void showExpansion(int index) {
		if (fIndex == 0) {
			setTitleText(CHoverMessages.CMacroExpansionControl_title_original);
		} else if (fIndex < fInput.fExpansions.length - 1) {
			setTitleText(NLS.bind(CHoverMessages.CMacroExpansionControl_title_expansion, 
					String.valueOf(fIndex), String.valueOf(fInput.fExpansions.length - 1)));
		} else {
			setTitleText(CHoverMessages.CMacroExpansionControl_title_fullyExpanded);
		}
		IDocument document= getSourceViewer().getDocument();
		if (document == null) {
			document= new Document(fInput.fExpansions[index]);
			CUIPlugin.getDefault().getTextTools().setupCDocument(document);
			getSourceViewer().setDocument(document);
		} else {
			document.set(fInput.fExpansions[index]);
		}
	}
}
