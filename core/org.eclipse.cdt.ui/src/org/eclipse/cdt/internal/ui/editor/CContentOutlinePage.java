/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Markus Schorn (Wind River Systems)
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.actions.ActionGroup;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.actions.CustomFiltersActionGroup;
import org.eclipse.cdt.ui.actions.MemberFilterActionGroup;
import org.eclipse.cdt.ui.actions.OpenViewActionGroup;
import org.eclipse.cdt.ui.refactoring.actions.CRefactoringActionGroup;

import org.eclipse.cdt.internal.ui.search.actions.SelectionSearchGroup;


/**
 * Outline page for C/C++ translation units.
 */
public class CContentOutlinePage extends AbstractCModelOutlinePage {
	
	private Composite fParent;
	private StackLayout fStackLayout;
	private Composite fOutlinePage;
	private Control fStatusPage;
	private boolean fScalabilityMode;

	public CContentOutlinePage(CEditor editor) {
		super("#TranslationUnitOutlinerContext", editor); //$NON-NLS-1$
	}

	/**
	 * Provide access to the CEditor corresponding to this CContentOutlinePage.
	 * @returns the CEditor corresponding to this CContentOutlinePage.
	 */
	public CEditor getEditor() {
		return (CEditor)fEditor;
	}

	@Override
	public void createControl(Composite parent) {
		fParent = new Composite(parent, SWT.NONE);
		fStackLayout = new StackLayout();
		fParent.setLayout(fStackLayout);
		fOutlinePage = new Composite(fParent, SWT.NONE);
		fOutlinePage.setLayout(new FillLayout());
		super.createControl(fOutlinePage);
		fStatusPage = createStatusPage(fParent);
		updateVisiblePage();
	}

	@Override
	public Control getControl() {
		return fParent;
	}

	private Control createStatusPage(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(CEditorMessages.getString("Scalability.outlineDisabled")); //$NON-NLS-1$
		return label;
	}

	@Override
	public void setInput(ITranslationUnit unit) {
		final CEditor editor= getEditor();
		if (editor.isEnableScalablilityMode() 
				&& PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.SCALABILITY_RECONCILER)) {
			fScalabilityMode = true;
			super.setInput(null);
		} else {
			fScalabilityMode = false;
			super.setInput(unit);
		}
		updateVisiblePage();
	}

	private void updateVisiblePage() {
		if (fStackLayout == null) {
			return;
		}
		if (fScalabilityMode) {
			if (fStackLayout.topControl != fStatusPage) {
				fStackLayout.topControl = fStatusPage;
				fParent.layout();
			}
		} else {
			if (fStackLayout.topControl != fOutlinePage) {
				fStackLayout.topControl = fOutlinePage;
				fParent.layout();
			}
		}
	}

	@Override
	protected SelectionSearchGroup createSearchActionGroup() {
		return new SelectionSearchGroup(this);
	}

	@Override
	protected OpenViewActionGroup createOpenViewActionGroup() {
		OpenViewActionGroup ovag= new OpenViewActionGroup(this, getEditor());
		ovag.setEnableIncludeBrowser(true);
		return ovag;
	}

	@Override
	protected ActionGroup createRefactoringActionGroup() {
		return new CRefactoringActionGroup(this);
	}

	@Override
	protected ActionGroup createCustomFiltersActionGroup() {
		return new CustomFiltersActionGroup("org.eclipse.cdt.ui.COutlinePage", getTreeViewer()); //$NON-NLS-1$
	}

	@Override
	protected ActionGroup createMemberFilterActionGroup() {
		return new MemberFilterActionGroup(getTreeViewer(), "COutlineViewer"); //$NON-NLS-1$
	}

}
