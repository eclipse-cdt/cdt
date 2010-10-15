/*******************************************************************************
 *  Copyright (c) 2005, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Markus Schorn (Wind River Systems)
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.dialogs.PreferencesUtil;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.actions.CustomFiltersActionGroup;
import org.eclipse.cdt.ui.actions.GenerateActionGroup;
import org.eclipse.cdt.ui.actions.MemberFilterActionGroup;
import org.eclipse.cdt.ui.actions.OpenViewActionGroup;
import org.eclipse.cdt.ui.refactoring.actions.CRefactoringActionGroup;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.actions.ActionMessages;
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
	 * Provides access to the CEditor corresponding to this CContentOutlinePage.
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
		final Link link= new Link(parent, SWT.NONE);
		link.setText(CEditorMessages.Scalability_outlineDisabled);
		link.setToolTipText(CEditorMessages.Scalability_linkToolTip);
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PreferencesUtil.createPreferenceDialogOn(link.getShell(), "org.eclipse.cdt.ui.preferences.CScalabilityPreferences", null, null).open(); //$NON-NLS-1$
			}
		});
		return link;
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
	protected ActionGroup createSearchActionGroup() {
		return new SelectionSearchGroup(this);
	}

	@Override
	protected ActionGroup createOpenViewActionGroup() {
		OpenViewActionGroup ovag= new OpenViewActionGroup(this, getEditor());
		ovag.setEnableIncludeBrowser(true);
		return ovag;
	}

	@Override
	protected ActionGroup createRefactoringActionGroup() {
		return new CRefactoringActionGroup(this);
	}
	

	@Override
	protected ActionGroup createSourceActionGroup() {
		return new GenerateActionGroup(this);
	}

	@Override
	protected ActionGroup createCustomFiltersActionGroup() {
		return new CustomFiltersActionGroup("org.eclipse.cdt.ui.COutlinePage", getTreeViewer()); //$NON-NLS-1$
	}

	@Override
	protected ActionGroup createMemberFilterActionGroup() {
		return new MemberFilterActionGroup(getTreeViewer(), "COutlineViewer"); //$NON-NLS-1$
	}
	
	/**
	 * This action toggles namespace grouping
	 * 
	 * @since 5.2
	 */
	protected static class NamespaceGroupingAction extends Action {

		public NamespaceGroupingAction(AbstractCModelOutlinePage outlinePage) {
			super(ActionMessages.NamespacesGroupingAction_label);
			setDescription(ActionMessages.NamespacesGroupingAction_description);
			setToolTipText(ActionMessages.NamespacesGroupingAction_tooltip);
			this.setImageDescriptor(CPluginImages.DESC_OBJS_NAMESPACE);
			this.setDisabledImageDescriptor(CPluginImages.DESC_OBJS_NAMESPACE);

			boolean enabled= isNamspacesGroupingEnabled();
			setChecked(enabled);
		}

		/**
		 * Runs the action.
		 */
		@Override
		public void run() {
			PreferenceConstants.getPreferenceStore().setValue(PreferenceConstants.OUTLINE_GROUP_NAMESPACES, isChecked());
		}

		public boolean isNamspacesGroupingEnabled () {
			return PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.OUTLINE_GROUP_NAMESPACES);
		}
	}
	
	/**
	 * This action toggles member definition grouping
	 * 
	 * @since 5.2
	 */
	protected static class MemberGroupingAction extends Action {

		public MemberGroupingAction(AbstractCModelOutlinePage outlinePage) {
			super(ActionMessages.MemberGroupingAction_label);
			setDescription(ActionMessages.MemberGroupingAction_description);
			setToolTipText(ActionMessages.MemberGroupingAction_tooltip);
			CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, CPluginImages.IMG_ACTION_SHOW_PUBLIC);

			boolean enabled= isMemberGroupingEnabled();
			setChecked(enabled);
		}

		/**
		 * Runs the action.
		 */
		@Override
		public void run() {
			PreferenceConstants.getPreferenceStore().setValue(PreferenceConstants.OUTLINE_GROUP_MEMBERS, isChecked());
		}

		public boolean isMemberGroupingEnabled () {
			return PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.OUTLINE_GROUP_MEMBERS);
		}
	}
	
	@Override
	protected void registerActionBars(IActionBars actionBars) {
		super.registerActionBars(actionBars);
		IMenuManager menu= actionBars.getMenuManager();

		// appendToGroup does not work reliably (bug 326748)
//		menu.appendToGroup("group.layout", new MemberGroupingAction(this)); //$NON-NLS-1$
//		menu.appendToGroup("group.layout", new NamespaceGroupingAction(this)); //$NON-NLS-1$

		// add actions directly instead
		menu.add(new MemberGroupingAction(this));
		menu.add(new NamespaceGroupingAction(this));
	}

}
