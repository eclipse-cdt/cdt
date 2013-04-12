/*******************************************************************************
 * Copyright (c) 2013 Ericsson AB and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alvaro Sanchez-Leon (Ericsson AB) - Support for Step into selection (bug 244865)
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.actions;

import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.internal.ui.DsfUiUtils;
import org.eclipse.cdt.dsf.debug.internal.ui.Messages;
import org.eclipse.cdt.dsf.debug.internal.ui.sourcelookup.DsfSourceSelectionResolver;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl3;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
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
			DsfSourceSelectionResolver resolveSelection = new DsfSourceSelectionResolver(fEditor, fSelection);
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
		
		final DsfSession session = DsfSession.getSession(context.getSessionId());
		if (session == null || !session.isActive()) {
			return null;
		}
		
		if(!isExecutable(context)) {
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

	private boolean isExecutable(final IExecutionDMContext dmc) {
		final DsfSession session = DsfSession.getSession(dmc.getSessionId());
		if (session == null || !session.isActive()) {
			return false;
		}

		try {
			Query<Boolean> query = new Query<Boolean>() {
				@Override
				protected void execute(DataRequestMonitor<Boolean> rm) {
					DsfServicesTracker tracker = new DsfServicesTracker(DsfUIPlugin.getBundleContext(), dmc.getSessionId());
					IRunControl3 runControl = tracker.getService(IRunControl3.class);
					tracker.dispose();

					if (runControl != null) {
						runControl.canStepIntoSelection(dmc, null, null, null, rm);
					} else {
						rm.setData(false);
						rm.done();
					}
				}
			};
			
			session.getExecutor().execute(query);
			return query.get();
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
		} 

		return false;
	}
	
}
