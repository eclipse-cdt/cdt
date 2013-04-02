/*******************************************************************************
 * Copyright (c) 2013 Ericsson AB and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson AB - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.actions;

import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.internal.ui.DsfUiUtils;
import org.eclipse.cdt.dsf.debug.internal.ui.Messages;
import org.eclipse.cdt.dsf.debug.internal.ui.sourcelookup.DSfSourceSelectionResolver;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @author Alvaro Sanchez-Leon
 * @since 2.4
 *
 */
public class DsfStepIntoSelectionHyperlinkDetector extends AbstractHyperlinkDetector {	
	class DsfStepIntoSelectionHyperlink implements IHyperlink {
		
		private ITextSelection fSelection = null;
		private ITextEditor fEditor = null;
		
		/**
		 * Constructor
		 * @param region
		 */
		public DsfStepIntoSelectionHyperlink(ITextSelection selection, ITextEditor editor) {
			fSelection = selection;
			fEditor = editor;
		}
		
		/**
		 * @see org.eclipse.jface.text.hyperlink.IHyperlink#getHyperlinkRegion()
		 */
		@Override
		public IRegion getHyperlinkRegion() {
			return new Region(fSelection.getOffset(), fSelection.getLength());
		}
		
		/**
		 * @see org.eclipse.jface.text.hyperlink.IHyperlink#getHyperlinkText()
		 */
		@Override
		public String getHyperlinkText() {
			return Messages.DsfUIStepIntoEditorSelection;
		}
		
		/**
		 * @see org.eclipse.jface.text.hyperlink.IHyperlink#getTypeLabel()
		 */
		@Override
		public String getTypeLabel() {
			return Messages.DsfUIStepIntoEditorSelection_label;
		}
		
		/**
		 * @see org.eclipse.jface.text.hyperlink.IHyperlink#open()
		 */
		@Override
		public void open() {
			DSfSourceSelectionResolver resolveSelection = new DSfSourceSelectionResolver(fEditor, fSelection);
			// Resolve UI selection from the the UI thread
			Display.getDefault().syncExec(resolveSelection);
			//Resolve the debug context
			final IExecutionDMContext dmc = resolveDebugContext();
			if (resolveSelection.isSuccessful() && dmc != null) {
				DsfUiUtils.runToSelection(resolveSelection.getLineLocation(), resolveSelection.getFunction(), dmc);
			} else {
				String message = null;
				if (dmc == null){
					message = "DSfStepIntoSelection: Unable to resolve the debug context"; //$NON-NLS-1$
				} else {					
					message = "DSfStepIntoSelection: Unable to resolve a selected function"; //$NON-NLS-1$
				}
				DsfUIPlugin.debug(message);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.hyperlink.IHyperlinkDetector#detectHyperlinks(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion, boolean)
	 */
	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, final IRegion region, boolean canShowMultipleHyperlinks) {
		//Only valid in the context of a selection within the CEditor
		ITextEditor editor = (ITextEditor) getAdapter(ITextEditor.class);
		if (editor == null || region == null || !(editor instanceof CEditor))
			return null;
		
		ISelection selection = textViewer.getSelectionProvider().getSelection();
		if (!(selection instanceof ITextSelection)){
			return null;			
		}

		//Shall only enable hyper link step into selection within a cdt debug execution context
		IExecutionDMContext context = resolveDebugContext();
		if (context == null) {
			return null;
		}

		return new IHyperlink[] {new DsfStepIntoSelectionHyperlink((ITextSelection) selection, editor) };
	}

	private IExecutionDMContext resolveDebugContext()
	{
		IExecutionDMContext execContext = null;
		IAdaptable adaptableContext = DebugUITools.getDebugContext();
		IDMContext debugContext =null;
		if (adaptableContext instanceof IDMVMContext) {
			debugContext = ((IDMVMContext)adaptableContext).getDMContext();	
		} 
		
		if (debugContext != null) {			
			execContext = DMContexts.getAncestorOfType(debugContext, IExecutionDMContext.class);
		}
		
		return execContext;
	}
}
