/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import java.util.List;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.internal.ui.dialogs.IStatusChangeListener;
import org.eclipse.cdt.ui.dialogs.ICOptionPage;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class NewIncludesSymbolsTabBlock extends AbstractPathOptionBlock implements IStatusChangeListener {

	private CPathIncludeSymbolEntryPage fIncludeSymbols;

	private List fCPaths;

	private Composite fComposite;

	public NewIncludesSymbolsTabBlock(IStatusChangeListener context, int pageToShow) {
		super(context, pageToShow);
		fIncludeSymbols = new CPathIncludeSymbolEntryPage(this);
	}

	protected void addTab(ICOptionPage tab) {
		tab.setContainer(this);
		tab.createControl(fComposite);
		addOptionPage(tab);
	}

	protected void addTabs() {
	}

	public Control createContents(Composite parent) {
		fComposite = new Composite(parent, SWT.NONE);
		fComposite.setLayout(new GridLayout(1, false));

		addPage(fIncludeSymbols);
		setCurrentPage(fIncludeSymbols);
		initializingTabs = false;

		if (getCProject() != null) {
			fIncludeSymbols.init(getCProject(), fCPaths);
		}
		Dialog.applyDialogFont(fComposite);
		return fComposite;
	}

	protected List getCPaths() {
		if (fIncludeSymbols != null) {
			return fIncludeSymbols.getCPaths();
		} 
		return fCPaths;
	}

	protected int[] getFilteredTypes() {
		return new int[] {IPathEntry.CDT_INCLUDE, IPathEntry.CDT_MACRO, IPathEntry.CDT_CONTAINER};
	}

	protected int[] getAppliedFilteredTypes() {
		return new int[] {IPathEntry.CDT_INCLUDE, IPathEntry.CDT_MACRO};
	}
	
	protected void initialize(ICElement element, List cPaths) {
		fCPaths = cPaths;

		if (fIncludeSymbols != null) {
			fIncludeSymbols.init(getCProject(), cPaths);
		}
		doStatusLineUpdate();
		initializeTimeStamps();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.internal.ui.dialogs.IStatusChangeListener#statusChanged(org.eclipse.core.runtime.IStatus)
	 */
	public void statusChanged(IStatus status) {
		switch (status.getSeverity()) {
			case IStatus.ERROR :
				getPathStatus().setError(status.getMessage());
				break;
			case IStatus.INFO :
				getPathStatus().setInfo(status.getMessage());
				break;
			case IStatus.WARNING :
				getPathStatus().setWarning(status.getMessage());
				break;
			default:
				getPathStatus().setOK();
		}
		updateBuildPathStatus();
		doStatusLineUpdate();
	}
}