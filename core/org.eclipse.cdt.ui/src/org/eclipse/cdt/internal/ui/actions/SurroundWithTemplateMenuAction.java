/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Axel Mueller - [289339] ported from JDT to CDT
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IEditingSupport;
import org.eclipse.jface.text.IEditingSupportRegistry;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.IRewriteTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;
import org.eclipse.ui.actions.QuickMenuCreator;
import org.eclipse.ui.dialogs.PreferencesUtil;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.text.contentassist.CContentAssistInvocationContext;
import org.eclipse.cdt.internal.ui.text.contentassist.TemplateCompletionProposalComputer;

public class SurroundWithTemplateMenuAction implements IWorkbenchWindowPulldownDelegate2 {

	public static final String SURROUND_WITH_QUICK_MENU_ACTION_ID= "org.eclipse.cdt.ui.edit.text.c.surround.with.quickMenu";  //$NON-NLS-1$

	private static final String C_TEMPLATE_PREFERENCE_PAGE_ID= "org.eclipse.cdt.ui.preferences.TemplatePreferencePage"; //$NON-NLS-1$

	private static final String TEMPLATE_GROUP= "templateGroup"; //$NON-NLS-1$

	private static final String CONFIG_GROUP= "configGroup"; //$NON-NLS-1$

	private static class ConfigureTemplatesAction extends Action {

		public ConfigureTemplatesAction() {
			super(ActionMessages.SurroundWithTemplateMenuAction_ConfigureTemplatesActionName);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			PreferencesUtil.createPreferenceDialogOn(getShell(), C_TEMPLATE_PREFERENCE_PAGE_ID, new String[] {C_TEMPLATE_PREFERENCE_PAGE_ID}, null).open();
		}

		private Shell getShell() {
			return CUIPlugin.getActiveWorkbenchShell();
		}
	}

	private static Action NONE_APPLICABLE_ACTION= new Action(ActionMessages.SurroundWithTemplateMenuAction_NoneApplicable) {
		@Override
		public void run() {
			//Do nothing
		}
		@Override
		public boolean isEnabled() {
			return false;
		}
	};

	private Menu fMenu;
	private IPartService fPartService;
	private IPartListener fPartListener= new IPartListener() {

		@Override
		public void partActivated(IWorkbenchPart part) {
		}

		@Override
		public void partBroughtToTop(IWorkbenchPart part) {
		}

		@Override
		public void partClosed(IWorkbenchPart part) {
		}

		@Override
		public void partDeactivated(IWorkbenchPart part) {
			disposeMenuItems();
		}

		@Override
		public void partOpened(IWorkbenchPart part) {
		}
	};

	protected void disposeMenuItems() {
		if (fMenu == null || fMenu.isDisposed()) {
			return;
		}
		MenuItem[] items = fMenu.getItems();
		for (int i= 0; i < items.length; i++) {
			MenuItem menuItem= items[i];
			if (!menuItem.isDisposed()) {
				menuItem.dispose();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Menu getMenu(Menu parent) {
		setMenu(new Menu(parent));
		fillMenu(fMenu);
		initMenu();
		return fMenu;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Menu getMenu(Control parent) {
		setMenu(new Menu(parent));
		fillMenu(fMenu);
		initMenu();
		return fMenu;
	}

	public static void fillMenu(IMenuManager menu, CEditor editor) {
		IAction[] actions= getTemplateActions(editor);
		if (actions == null || actions.length == 0) {
			menu.add(NONE_APPLICABLE_ACTION);
		} else {
			menu.add(new Separator(TEMPLATE_GROUP));
			for (int i= 0; i < actions.length; i++)
				menu.add(actions[i]);
		}

		menu.add(new Separator(CONFIG_GROUP));
		menu.add(new ConfigureTemplatesAction());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		if (fPartService != null) {
			fPartService.removePartListener(fPartListener);
			fPartService= null;
		}
		setMenu(null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(IWorkbenchWindow window) {
		if (fPartService != null) {
			fPartService.removePartListener(fPartListener);
			fPartService= null;
		}

		if (window != null) {
			IPartService partService= window.getPartService();
			if (partService != null) {
				fPartService= partService;
				partService.addPartListener(fPartListener);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run(IAction action) {
		IWorkbenchPart activePart= CUIPlugin.getActivePage().getActivePart();
		if (!(activePart instanceof CEditor))
			return;

		final CEditor editor= (CEditor) activePart;

		new QuickMenuCreator() {
			@Override
			protected void fillMenu(IMenuManager menu) {
				SurroundWithTemplateMenuAction.fillMenu(menu, editor);
			}
		}.createMenu();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// Default do nothing
	}

	/**
	 * The menu to show in the workbench menu
	 * @param menu the menu to fill entries into it
	 */
	protected void fillMenu(Menu menu) {

		IWorkbenchPart activePart= CUIPlugin.getActivePage().getActivePart();
		if (!(activePart instanceof CEditor)) {
			ActionContributionItem item= new ActionContributionItem(NONE_APPLICABLE_ACTION);
			item.fill(menu, -1);
			return;
		}

		CEditor editor= (CEditor)activePart;
		IAction[] actions= getTemplateActions(editor);

		if ( actions == null || actions.length <= 0) {
			ActionContributionItem item= new ActionContributionItem(NONE_APPLICABLE_ACTION);
			item.fill(menu, -1);
		} else {
			for (int i= 0; i < actions.length; i++) {
				ActionContributionItem item= new ActionContributionItem(actions[i]);
				item.fill(menu, -1);
			}
		}

		Separator configGroup= new Separator(CONFIG_GROUP);
		configGroup.fill(menu, -1);

		ActionContributionItem configAction= new ActionContributionItem(new ConfigureTemplatesAction());
		configAction.fill(menu, -1);

	}

	protected void initMenu() {
		fMenu.addMenuListener(new MenuAdapter() {
			@Override
			public void menuShown(MenuEvent e) {
				Menu m = (Menu)e.widget;
				MenuItem[] items = m.getItems();
				for (int i=0; i < items.length; i++) {
					items[i].dispose();
				}
				fillMenu(m);
			}
		});
	}

	private void setMenu(Menu menu) {
		if (fMenu != null) {
			fMenu.dispose();
		}
		fMenu = menu;
	}

	private static IAction[] getTemplateActions(CEditor editor) {
		ITextSelection textSelection= getTextSelection(editor);
		if (textSelection == null || textSelection.getLength() == 0)
			return null;

		ITranslationUnit tu= CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editor.getEditorInput());
		if (tu == null)
			return null;

		TemplateCompletionProposalComputer templateComputer = new TemplateCompletionProposalComputer();
		CContentAssistInvocationContext context = new CContentAssistInvocationContext( editor.getViewer(), textSelection.getOffset(), editor, true, false );

		List<ICompletionProposal> proposals= templateComputer.computeCompletionProposals(context, null);
		if (proposals == null || proposals.isEmpty())
			return null;

		return getActionsFromProposals(proposals, context.getInvocationOffset(), editor.getViewer());
	}

	private static ITextSelection getTextSelection(CEditor editor) {
		ISelectionProvider selectionProvider= editor.getSelectionProvider();
		if (selectionProvider == null)
			return null;

		ISelection selection= selectionProvider.getSelection();
		if (!(selection instanceof ITextSelection))
			return null;

		return (ITextSelection)selection;
	}

	private static IAction[] getActionsFromProposals(List<ICompletionProposal> proposals, final int offset, final ITextViewer viewer) {
		List<Action> result= new ArrayList<Action>();
		int j = 1;
		for (Iterator<ICompletionProposal> it= proposals.iterator(); it.hasNext();) {
			final ICompletionProposal proposal= it.next();

			StringBuffer actionName= new StringBuffer();
			if (j < 10) {
				actionName.append('&').append(j).append(' ');
			}
			actionName.append(proposal.getDisplayString());
			Action action= new Action(actionName.toString()) {
				/**
				 * {@inheritDoc}
				 */
				@Override
				public void run() {
					applyProposal(proposal, viewer, (char)0, 0, offset);
				}
			};

			result.add(action);
			j++;
		}
		if (result.size() == 0)
			return null;

		return result.toArray(new IAction[result.size()]);
	}

	private static void applyProposal(ICompletionProposal proposal, ITextViewer viewer, char trigger, int stateMask, final int offset) {
		Assert.isTrue(proposal instanceof ICompletionProposalExtension2);

		IRewriteTarget target= null;
		IEditingSupportRegistry registry= null;
		IEditingSupport helper= new IEditingSupport() {

			@Override
			public boolean isOriginator(DocumentEvent event, IRegion focus) {
				return focus.getOffset() <= offset && focus.getOffset() + focus.getLength() >= offset;
			}

			@Override
			public boolean ownsFocusShell() {
				return false;
			}
		};

		try {
			IDocument document= viewer.getDocument();

			if (viewer instanceof ITextViewerExtension) {
				ITextViewerExtension extension= (ITextViewerExtension) viewer;
				target= extension.getRewriteTarget();
			}

			if (target != null)
				target.beginCompoundChange();

			if (viewer instanceof IEditingSupportRegistry) {
				registry= (IEditingSupportRegistry) viewer;
				registry.register(helper);
			}

			((ICompletionProposalExtension2)proposal).apply(viewer, trigger, stateMask, offset);

			Point selection= proposal.getSelection(document);
			if (selection != null) {
				viewer.setSelectedRange(selection.x, selection.y);
				viewer.revealRange(selection.x, selection.y);
			}
		} finally {
			if (target != null)
				target.endCompoundChange();

			if (registry != null)
				registry.unregister(helper);
		}
	}
}
