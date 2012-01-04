/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems and others.
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
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

/**
 * DisassemblyEditor
 */
public class DisassemblyEditor extends DisassemblyPart implements IEditorPart {

	private IEditorInput fInput;
	private ToolBarManager fToolBarManager;
	private Label fContentDescriptionLabel;

	/**
	 * 
	 */
	public DisassemblyEditor() {
		super();
	}

	/*
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.DisassemblyPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		parent.setLayout(layout);
		Composite topBar = new Composite(parent, SWT.NONE);
		topBar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		GridLayout layout2 = new GridLayout(2, false);
		layout2.marginTop = 1;
		layout2.marginLeft = 1;
		layout2.marginWidth = 0;
		layout2.marginHeight = 0;
		topBar.setLayout(layout2);
		fContentDescriptionLabel = new Label(topBar, SWT.NONE);
		fContentDescriptionLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
		fToolBarManager = new ToolBarManager();
		ToolBar toolbar = fToolBarManager.createControl(topBar);
		toolbar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		Composite inner = new Composite(parent, SWT.NONE);
		inner.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		super.createPartControl(inner);
	}
	
	/*
	 * @see org.eclipse.ui.part.WorkbenchPart#setContentDescription(java.lang.String)
	 */
	@Override
	protected void setContentDescription(String description) {
		fContentDescriptionLabel.setText(description);
		fContentDescriptionLabel.getParent().layout(true);
	}

	@Override
	protected IActionBars getActionBars() {
		return getEditorSite().getActionBars();
	}

	@Override
	protected void contributeToActionBars(IActionBars bars) {
		super.contributeToActionBars(bars);
		fillLocalToolBar(fToolBarManager);
		fToolBarManager.update(true);
	}

	/*
	 * @see org.eclipse.ui.IEditorPart#getEditorInput()
	 */
	@Override
	public IEditorInput getEditorInput() {
		return fInput;
	}

	/*
	 * @see org.eclipse.ui.IEditorPart#getEditorSite()
	 */
	@Override
	public IEditorSite getEditorSite() {
		return (IEditorSite)getSite();
	}

	/*
	 * @see org.eclipse.ui.IEditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
	}

	/*
	 * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	/*
	 * @see org.eclipse.ui.ISaveablePart#doSaveAs()
	 */
	@Override
	public void doSaveAs() {
	}

	/*
	 * @see org.eclipse.ui.ISaveablePart#isDirty()
	 */
	@Override
	public boolean isDirty() {
		return false;
	}

	/*
	 * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	/*
	 * @see org.eclipse.ui.ISaveablePart#isSaveOnCloseNeeded()
	 */
	@Override
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
