/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;

/**
 * DisassemblyEditor
 */
public class DisassemblyEditor extends DisassemblyPart implements IEditorPart {

	private IEditorInput fInput;

	/**
	 * 
	 */
	public DisassemblyEditor() {
		super();
	}

	@Override
	protected IActionBars getActionBars() {
		return getEditorSite().getActionBars();
	}

	/*
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.DisassemblyPart#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	protected void fillContextMenu(IMenuManager manager) {
		super.fillContextMenu(manager);
		manager.appendToGroup(IWorkbenchActionConstants.GO_TO, fActionGotoPC);
		manager.appendToGroup(IWorkbenchActionConstants.GO_TO, fActionGotoAddress);
		manager.appendToGroup(IWorkbenchActionConstants.GO_TO, fActionGotoSymbol);
		manager.appendToGroup("group.bottom", fActionRefreshView); //$NON-NLS-1$

	}
	
	/*
	 * @see org.eclipse.ui.IEditorPart#getEditorInput()
	 */
	public IEditorInput getEditorInput() {
		return fInput;
	}

	/*
	 * @see org.eclipse.ui.IEditorPart#getEditorSite()
	 */
	public IEditorSite getEditorSite() {
		return (IEditorSite)getSite();
	}

	/*
	 * @see org.eclipse.ui.IEditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
	 */
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
	}

	/*
	 * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void doSave(IProgressMonitor monitor) {
	}

	/*
	 * @see org.eclipse.ui.ISaveablePart#doSaveAs()
	 */
	public void doSaveAs() {
	}

	/*
	 * @see org.eclipse.ui.ISaveablePart#isDirty()
	 */
	public boolean isDirty() {
		return false;
	}

	/*
	 * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
	 */
	public boolean isSaveAsAllowed() {
		return false;
	}

	/*
	 * @see org.eclipse.ui.ISaveablePart#isSaveOnCloseNeeded()
	 */
	public boolean isSaveOnCloseNeeded() {
		return false;
	}

	//
	// IReusableEditor interface
	//

	/*
	 * @see org.eclipse.ui.IReusableEditor#setInput(org.eclipse.ui.IEditorInput)
	 */
	public void setInput(IEditorInput input) {
		fInput = input;
		// TLETODO [disassembly] initialization based on input
	}

	@Override
	protected void closePart() {
		getEditorSite().getPage().closeEditor(this, false);
	}

}
