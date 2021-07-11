/*******************************************************************************
 * Copyright (c) 2005, 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Markus Schorn (Wind River Systems)
 *     Anton Leherbauer (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.IContextMenuConstants;
import org.eclipse.cdt.internal.ui.actions.AbstractToggleLinkingAction;
import org.eclipse.cdt.internal.ui.actions.ActionMessages;
import org.eclipse.cdt.internal.ui.actions.CollapseAllAction;
import org.eclipse.cdt.internal.ui.cview.SelectionTransferDragAdapter;
import org.eclipse.cdt.internal.ui.cview.SelectionTransferDropAdapter;
import org.eclipse.cdt.internal.ui.dnd.CDTViewerDragAdapter;
import org.eclipse.cdt.internal.ui.dnd.DelegatingDropAdapter;
import org.eclipse.cdt.internal.ui.dnd.TransferDragSourceListener;
import org.eclipse.cdt.internal.ui.dnd.TransferDropTargetListener;
import org.eclipse.cdt.internal.ui.search.actions.SelectionSearchGroup;
import org.eclipse.cdt.internal.ui.util.ProblemTreeViewer;
import org.eclipse.cdt.internal.ui.viewsupport.AppearanceAwareLabelProvider;
import org.eclipse.cdt.internal.ui.viewsupport.CElementLabels;
import org.eclipse.cdt.internal.ui.viewsupport.CUILabelProvider;
import org.eclipse.cdt.internal.ui.viewsupport.DecoratingCOutlineLabelProvider;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IncludesGrouping;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.actions.OpenViewActionGroup;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.handlers.CollapseAllHandler;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.navigator.resources.ProjectExplorer;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.IShowInTargetList;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * Abstract outline page based on CModel.
 *
 * @since 5.0
 */
public abstract class AbstractCModelOutlinePage extends Page
		implements IContentOutlinePage, ISelectionChangedListener, IAdaptable {
	/**
	 * The default label provider for the outline.
	 */
	public static class COutlineLabelProvider extends AppearanceAwareLabelProvider {
		/**
		 * Flag whether to show member definitions with qualified or simple names.
		 */
		private boolean fSimpleName;

		public COutlineLabelProvider(long textFlags, int imageFlags) {
			super(textFlags, imageFlags);
			JFaceResources.getFontRegistry().addListener(this);
			PreferenceConstants.getPreferenceStore().addPropertyChangeListener(this);
			PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry().addListener(this);
			fSimpleName = PreferenceConstants.getPreferenceStore()
					.getBoolean(PreferenceConstants.OUTLINE_GROUP_MEMBERS);
		}

		@Override
		public void dispose() {
			JFaceResources.getFontRegistry().removeListener(this);
			PreferenceConstants.getPreferenceStore().removePropertyChangeListener(this);
			PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry().removeListener(this);
			super.dispose();
		}

		@Override
		protected long evaluateTextFlags(Object element) {
			if (fSimpleName) {
				return super.evaluateTextFlags(element) | CElementLabels.M_SIMPLE_NAME | CElementLabels.F_SIMPLE_NAME;
			}
			return super.evaluateTextFlags(element);
		}

		@Override
		public void propertyChange(PropertyChangeEvent event) {
			if (PreferenceConstants.OUTLINE_GROUP_MEMBERS.equals(event.getProperty())) {
				final Object newValue = event.getNewValue();
				if (newValue instanceof Boolean) {
					fSimpleName = ((Boolean) newValue).booleanValue();
				} else {
					fSimpleName = PreferenceConstants.getPreferenceStore()
							.getBoolean(PreferenceConstants.OUTLINE_GROUP_MEMBERS);
				}
			}
			if (PreferenceConstants.OUTLINE_MARK_TEXT_FONT.equals(event.getProperty())) {
				fireLabelProviderChanged(new LabelProviderChangedEvent(this));
			}
			if (PreferenceConstants.OUTLINE_MARK_TEXT_COLOR.equals(event.getProperty())) {
				fireLabelProviderChanged(new LabelProviderChangedEvent(this));
			}
			if (PreferenceConstants.OUTLINE_MARK_DIVIDER_COLOR.equals(event.getProperty())) {
				fireLabelProviderChanged(new LabelProviderChangedEvent(this));
			}
		}
	}

	/**
	 * A specialized tree viewer for outline content.
	 *
	 * @since 5.0
	 */
	private static class OutlineTreeViewer extends ProblemTreeViewer {

		public OutlineTreeViewer(Composite parent, int flags) {
			super(parent, flags);
		}

		@Override
		protected void internalExpandToLevel(Widget node, int level) {
			if (node instanceof Item) {
				Item i = (Item) node;
				final Object data = i.getData();
				// Don't expand include grouping by default.
				if (data instanceof IncludesGrouping) {
					return;
				}
				if (data instanceof ICElement && !shouldExpandElement((ICElement) data)) {
					return;
				}
			}
			super.internalExpandToLevel(node, level);
		}

		private boolean shouldExpandElement(ICElement cElement) {
			final ICElement parent = cElement.getParent();
			if (parent == null) {
				return false;
			}
			// Expand classes and namespaces.
			final int elementType = cElement.getElementType();
			final int parentType = parent.getElementType();
			switch (elementType) {
			case ICElement.C_CLASS:
			case ICElement.C_TEMPLATE_CLASS:
			case ICElement.C_NAMESPACE:
				return parentType == ICElement.C_UNIT || parentType == ICElement.C_NAMESPACE;
			default:
				return false;
			}
		}
	}

	protected static class IncludeGroupingAction extends Action {
		AbstractCModelOutlinePage fOutLinePage;

		public IncludeGroupingAction(AbstractCModelOutlinePage outlinePage) {
			super(ActionMessages.IncludesGroupingAction_label);
			setDescription(ActionMessages.IncludesGroupingAction_description);
			setToolTipText(ActionMessages.IncludesGroupingAction_tooltip);
			CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, CPluginImages.IMG_MENU_GROUP_INCLUDE);

			boolean enabled = isIncludesGroupingEnabled();
			setChecked(enabled);
			fOutLinePage = outlinePage;
		}

		@Override
		public void run() {
			PreferenceConstants.getPreferenceStore().setValue(PreferenceConstants.OUTLINE_GROUP_INCLUDES, isChecked());
		}

		public boolean isIncludesGroupingEnabled() {
			return PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.OUTLINE_GROUP_INCLUDES);
		}
	}

	/**
	 * This action toggles macro grouping.
	 *
	 * @since 5.2
	 */
	protected static class MacroGroupingAction extends Action {

		public MacroGroupingAction(AbstractCModelOutlinePage outlinePage) {
			super(ActionMessages.MacroGroupingAction_label);
			setDescription(ActionMessages.MacroGroupingAction_description);
			setToolTipText(ActionMessages.MacroGroupingAction_tooltip);
			CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, CPluginImages.IMG_ACTION_HIDE_MACROS);
			this.setImageDescriptor(CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_MACRO));
			this.setDisabledImageDescriptor(CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_MACRO));

			boolean enabled = isMacroGroupingEnabled();
			setChecked(enabled);
		}

		@Override
		public void run() {
			PreferenceConstants.getPreferenceStore().setValue(PreferenceConstants.OUTLINE_GROUP_MACROS, isChecked());
		}

		public boolean isMacroGroupingEnabled() {
			return PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.OUTLINE_GROUP_MACROS);
		}
	}

	protected static class HidePragmaMarkAction extends Action {

		public HidePragmaMarkAction(AbstractCModelOutlinePage outlinePage) {
			super(ActionMessages.HidePragmaMarks_label);
			setDescription(ActionMessages.HidePragmaMarks_description);
			setToolTipText(ActionMessages.HidePragmaMarks_tooltip);
			CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, CPluginImages.IMG_MENU_HIDE_PRAGMA_MARKS);

			boolean enabled = isHidePragmaMarkEnabled();
			setChecked(enabled);
		}

		@Override
		public void run() {
			PreferenceConstants.getPreferenceStore().setValue(PreferenceConstants.OUTLINE_HIDE_PRAGMA_MARK,
					isChecked());
		}

		public boolean isHidePragmaMarkEnabled() {
			return PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.OUTLINE_HIDE_PRAGMA_MARK);
		}
	}

	/**
	 * This action toggles whether this C Outline page links
	 * its selection to the active editor.
	 *
	 * @since 3.0
	 */
	public class ToggleLinkingAction extends AbstractToggleLinkingAction {
		/**
		 * Constructs a new action.
		 */
		public ToggleLinkingAction() {
			setChecked(isLinkingEnabled());
		}

		@Override
		public void run() {
			boolean checked = isChecked();
			PreferenceConstants.getPreferenceStore().setValue(PreferenceConstants.OUTLINE_LINK_TO_EDITOR, checked);
			if (checked && fEditor != null)
				synchronizeSelectionWithEditor();
		}
	}

	private static final long TEXT_FLAGS = AppearanceAwareLabelProvider.DEFAULT_TEXTFLAGS
			| CElementLabels.F_APP_TYPE_SIGNATURE | CElementLabels.M_APP_RETURNTYPE;
	private static final int IMAGE_FLAGS = AppearanceAwareLabelProvider.DEFAULT_IMAGEFLAGS;
	protected ITextEditor fEditor;
	protected ITranslationUnit fInput;
	private ProblemTreeViewer fTreeViewer;
	private ListenerList<ISelectionChangedListener> fSelectionChangedListeners = new ListenerList<>(
			ListenerList.IDENTITY);
	protected TogglePresentationAction fTogglePresentation;
	protected String fContextMenuId;
	private Menu fMenu;
	protected OpenIncludeAction fOpenIncludeAction;
	private ToggleLinkingAction fToggleLinkingAction;
	private ActionGroup fMemberFilterActionGroup;
	private ActionGroup fSelectionSearchGroup;
	private ActionGroup fRefactoringActionGroup;
	private ActionGroup fSourceActionGroup;
	private ActionGroup fOpenViewActionGroup;
	/**
	 * Custom filter action group.
	 * @since 3.0
	 */
	private ActionGroup fCustomFiltersActionGroup;
	/**
	 * @since 5.4
	 */
	private CollapseAllAction fCollapseAllAction;

	/**
	 * Create a new outline page for the given editor.
	 * @param contextMenuId  The id of this page's context menu
	 * @param editor  the editor associated with this outline page
	 */
	public AbstractCModelOutlinePage(String contextMenuId, ITextEditor editor) {
		super();
		fEditor = editor;
		fInput = null;
		fContextMenuId = contextMenuId;

		fTogglePresentation = new TogglePresentationAction();
		fTogglePresentation.setEditor(editor);

		fOpenIncludeAction = new OpenIncludeAction(this);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Class<T> key) {
		if (key == IShowInSource.class) {
			return (T) getShowInSource();
		}
		if (key == IShowInTargetList.class) {
			return (T) new IShowInTargetList() {
				@Override
				public String[] getShowInTargetIds() {
					return new String[] { ProjectExplorer.VIEW_ID };
				}
			};
		}
		if (key == IShowInTarget.class) {
			return (T) getShowInTarget();
		}
		return null;
	}

	/**
	 * Returns the {@code IShowInSource} for this view.
	 *
	 * @return the {@link IShowInSource}
	 */
	protected IShowInSource getShowInSource() {
		return new IShowInSource() {
			@Override
			public ShowInContext getShowInContext() {
				return new ShowInContext(null, getSite().getSelectionProvider().getSelection());
			}
		};
	}

	/**
	 * Returns the {@code IShowInTarget} for this view.
	 *
	 * @return the {@link IShowInTarget}
	 */
	protected IShowInTarget getShowInTarget() {
		return new IShowInTarget() {
			@Override
			public boolean show(ShowInContext context) {
				ISelection sel = context.getSelection();
				if (sel instanceof ITextSelection) {
					ITextSelection tsel = (ITextSelection) sel;
					int offset = tsel.getOffset();
					ICElement element = null;
					if (fEditor instanceof CEditor) {
						element = ((CEditor) fEditor).getElementAt(offset, false);
					} else if (fInput != null) {
						try {
							element = fInput.getElementAtOffset(offset);
						} catch (CModelException e) {
							CUIPlugin.log(e);
						}
					}
					if (element != null) {
						setSelection(new StructuredSelection(element));
						return true;
					}
				} else if (sel instanceof IStructuredSelection) {
					setSelection(sel);
					return true;
				}
				return false;
			}
		};
	}

	public boolean isLinkingEnabled() {
		return PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.OUTLINE_LINK_TO_EDITOR);
	}

	public ICElement getRoot() {
		return fInput;
	}

	/**
	 * Sets the selected element to the one at the current cursor position in the editor.
	 */
	public void synchronizeSelectionWithEditor() {
		if (fInput == null || fEditor == null || fTreeViewer == null)
			return;

		ITextSelection editorSelection = (ITextSelection) fEditor.getSelectionProvider().getSelection();
		if (editorSelection == null)
			return;

		int offset = editorSelection.getOffset();

		ICElement editorElement = null;
		try {
			if (fInput.isStructureKnown() && fInput.isConsistent()) {
				editorElement = fInput.getElementAtOffset(offset);
			}
		} catch (CModelException e) {
			return;
		}

		if (editorElement != null) {
			IStructuredSelection selection = new StructuredSelection(editorElement);
			fTreeViewer.setSelection(selection, true);
		}
	}

	/**
	 * called to create the context menu of the outline
	 */
	protected void contextMenuAboutToShow(IMenuManager menu) {
		CUIPlugin.createStandardGroups(menu);

		ISelection selection = getSelection();
		if (fOpenViewActionGroup != null && OpenViewActionGroup.canActionBeAdded(selection)) {
			fOpenViewActionGroup.fillContextMenu(menu);
		}

		if (OpenIncludeAction.canActionBeAdded(selection)) {
			menu.appendToGroup(IContextMenuConstants.GROUP_OPEN, fOpenIncludeAction);
		}

		if (fSelectionSearchGroup != null && SelectionSearchGroup.canActionBeAdded(selection)) {
			fSelectionSearchGroup.fillContextMenu(menu);
			menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		}

		if (fSourceActionGroup != null) {
			fSourceActionGroup.fillContextMenu(menu);
		}

		if (fRefactoringActionGroup != null) {
			fRefactoringActionGroup.fillContextMenu(menu);
		}
	}

	protected CContentOutlinerProvider createContentProvider(TreeViewer viewer) {
		IWorkbenchPart part = getSite().getPage().getActivePart();
		if (part == null) {
			return new CContentOutlinerProvider(viewer);
		}
		return new CContentOutlinerProvider(viewer, part.getSite());
	}

	protected ProblemTreeViewer createTreeViewer(Composite parent) {
		ProblemTreeViewer treeViewer = new OutlineTreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		treeViewer.setContentProvider(createContentProvider(treeViewer));
		treeViewer.setLabelProvider(new DecoratingCOutlineLabelProvider(createLabelProvider()));
		treeViewer.setAutoExpandLevel(3);
		treeViewer.setUseHashlookup(true);
		treeViewer.addSelectionChangedListener(this);
		return treeViewer;
	}

	private CUILabelProvider createLabelProvider() {
		return new COutlineLabelProvider(TEXT_FLAGS, IMAGE_FLAGS);
	}

	@Override
	public void createControl(Composite parent) {
		fTreeViewer = createTreeViewer(parent);
		initDragAndDrop();

		MenuManager manager = new MenuManager(fContextMenuId);
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				contextMenuAboutToShow(manager);
			}
		});
		Control control = fTreeViewer.getControl();
		fMenu = manager.createContextMenu(control);
		control.setMenu(fMenu);

		fTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				if (fOpenIncludeAction != null) {
					fOpenIncludeAction.run();
				}
			}
		});
		// register global actions
		IPageSite site = getSite();
		site.registerContextMenu(fContextMenuId, manager, fTreeViewer);
		site.setSelectionProvider(fTreeViewer);

		IActionBars bars = site.getActionBars();
		bars.setGlobalActionHandler(ITextEditorActionDefinitionIds.TOGGLE_SHOW_SELECTED_ELEMENT_ONLY,
				fTogglePresentation);

		fSelectionSearchGroup = createSearchActionGroup();
		fOpenViewActionGroup = createOpenViewActionGroup();
		fSourceActionGroup = createSourceActionGroup();
		fRefactoringActionGroup = createRefactoringActionGroup();
		// Custom filter group
		fCustomFiltersActionGroup = createCustomFiltersActionGroup();

		// Do this before setting input but after the initializations of the fields filtering
		registerActionBars(bars);
		bars.updateActionBars();

		fTreeViewer.setInput(fInput);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(control, ICHelpContextIds.COUTLINE_VIEW);

		IHandlerService handlerService = site.getService(IHandlerService.class);
		handlerService.activateHandler(CollapseAllHandler.COMMAND_ID, new ActionHandler(fCollapseAllAction));
	}

	@Override
	public void dispose() {
		if (fTreeViewer != null) {
			fTreeViewer.removeSelectionChangedListener(this);
			fTreeViewer = null;
		}

		if (fTogglePresentation != null) {
			fTogglePresentation.setEditor(null);
			fTogglePresentation = null;
		}

		if (fMemberFilterActionGroup != null) {
			fMemberFilterActionGroup.dispose();
			fMemberFilterActionGroup = null;
		}

		if (fOpenViewActionGroup != null) {
			fOpenViewActionGroup.dispose();
			fOpenViewActionGroup = null;
		}

		if (fRefactoringActionGroup != null) {
			fRefactoringActionGroup.dispose();
			fRefactoringActionGroup = null;
		}

		if (fSelectionSearchGroup != null) {
			fSelectionSearchGroup.dispose();
			fSelectionSearchGroup = null;
		}

		if (fCustomFiltersActionGroup != null) {
			fCustomFiltersActionGroup.dispose();
			fCustomFiltersActionGroup = null;
		}

		if (fSelectionChangedListeners != null) {
			fSelectionChangedListeners.clear();
			// don't set the listeners to null, the outline page may be reused.
		}

		if (fMenu != null && !fMenu.isDisposed()) {
			fMenu.dispose();
			fMenu = null;
		}

		fInput = null;

		super.dispose();
	}

	/**
	 * Register actions to the action bars.
	 *
	 * @param actionBars
	 */
	protected void registerActionBars(IActionBars actionBars) {
		IToolBarManager toolBarManager = actionBars.getToolBarManager();

		fCollapseAllAction = new CollapseAllAction(fTreeViewer);
		toolBarManager.add(fCollapseAllAction);
		LexicalSortingAction action = new LexicalSortingAction(getTreeViewer());
		toolBarManager.add(action);

		fMemberFilterActionGroup = createMemberFilterActionGroup();
		if (fMemberFilterActionGroup != null) {
			fMemberFilterActionGroup.fillActionBars(actionBars);
		}
		if (fCustomFiltersActionGroup != null) {
			fCustomFiltersActionGroup.fillActionBars(actionBars);
		}
		if (fOpenViewActionGroup != null) {
			fOpenViewActionGroup.fillActionBars(actionBars);
		}
		if (fRefactoringActionGroup != null) {
			fRefactoringActionGroup.fillActionBars(actionBars);
		}
		if (fSelectionSearchGroup != null) {
			fSelectionSearchGroup.fillActionBars(actionBars);
		}
		IMenuManager menu = actionBars.getMenuManager();
		menu.add(new Separator("EndFilterGroup")); //$NON-NLS-1$

		fToggleLinkingAction = new ToggleLinkingAction();
		menu.add(fToggleLinkingAction);

		menu.add(new Separator("dividers.layout")); //$NON-NLS-1$
		menu.add(new HidePragmaMarkAction(this));
		menu.add(new Separator("group.layout")); //$NON-NLS-1$
		menu.add(new IncludeGroupingAction(this));
		menu.add(new MacroGroupingAction(this));
	}

	/**
	 * return an ActionGroup contributing search actions or {@code null} if search is not supported
	 */
	protected ActionGroup createSearchActionGroup() {
		// default: no search action group
		return null;
	}

	/**
	 * @return an OpenViewActionGroup contributing open view actions or
	 *         {@code null} if open view actions are not wanted
	 */
	protected ActionGroup createOpenViewActionGroup() {
		// default: no open view action group
		return null;
	}

	/**
	 * @return an ActionGroup contributing refactoring actions or
	 *         {@code null} if refactoring is not supported
	 */
	protected ActionGroup createRefactoringActionGroup() {
		// default: no refactoring actions
		return null;
	}

	/**
	 * @return an ActionGroup contributing source actions or
	 *         {@code null} if source actions are not supported
	 */
	protected ActionGroup createSourceActionGroup() {
		// default: no source actions
		return null;
	}

	/**
	 * @return an ActionGroup instance to provide custom filters or
	 *         {@code null} if this action group is not wanted
	 */
	protected ActionGroup createCustomFiltersActionGroup() {
		// default: no custom filters
		return null;
	}

	/**
	 * @return an ActionGroup contributing member filters or {@code null}
	 *         if member filters are not wanted
	 */
	protected ActionGroup createMemberFilterActionGroup() {
		// default: no member filters
		return null;
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		fSelectionChangedListeners.add(listener);
	}

	/**
	 * Fires a selection changed event.
	 *
	 * @param selection the new selection
	 */
	protected void fireSelectionChanged(ISelection selection) {
		// Create an event.
		SelectionChangedEvent event = new SelectionChangedEvent(this, selection);

		// Fire the event.
		Object[] listeners = fSelectionChangedListeners.getListeners();
		for (int i = 0; i < listeners.length; ++i) {
			((ISelectionChangedListener) listeners[i]).selectionChanged(event);
		}
		if (fRefactoringActionGroup != null) {
			fRefactoringActionGroup.setContext(new ActionContext(selection));
			fRefactoringActionGroup.updateActionBars();
		}

		if (fSourceActionGroup != null) {
			fSourceActionGroup.setContext(new ActionContext(selection));
			fSourceActionGroup.updateActionBars();
		}
	}

	@Override
	public Control getControl() {
		if (fTreeViewer == null)
			return null;
		return fTreeViewer.getControl();
	}

	@Override
	public ISelection getSelection() {
		if (fTreeViewer == null)
			return StructuredSelection.EMPTY;
		return fTreeViewer.getSelection();
	}

	/**
	 * Returns this page's tree viewer.
	 *
	 * @return this page's tree viewer, or {@code null} if {@code createControl} has not been called yet
	 */
	protected TreeViewer getTreeViewer() {
		return fTreeViewer;
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		fSelectionChangedListeners.remove(listener);
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		fireSelectionChanged(event.getSelection());
	}

	/**
	 * Sets focus to a part in the page.
	 */
	@Override
	public void setFocus() {
		fTreeViewer.getControl().setFocus();
	}

	@Override
	public void setSelection(ISelection selection) {
		if (fTreeViewer != null)
			fTreeViewer.setSelection(selection);
	}

	/**
	 * Set the current input to the content provider.
	 * @param unit
	 */
	public void setInput(ITranslationUnit unit) {
		fInput = unit;
		if (fTreeViewer != null) {
			fTreeViewer.setInput(fInput);
		}
	}

	private void initDragAndDrop() {
		int ops = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK;
		Transfer[] transfers = new Transfer[] { LocalSelectionTransfer.getTransfer() };

		// Drop adapter.
		TransferDropTargetListener[] dropListeners = new TransferDropTargetListener[] {
				new SelectionTransferDropAdapter(fTreeViewer) };
		fTreeViewer.addDropSupport(ops | DND.DROP_DEFAULT, transfers, new DelegatingDropAdapter(dropListeners));

		// Drag adapter.
		TransferDragSourceListener[] dragListeners = new TransferDragSourceListener[] {
				new SelectionTransferDragAdapter(fTreeViewer) };
		fTreeViewer.addDragSupport(ops, transfers, new CDTViewerDragAdapter(fTreeViewer, dragListeners));
	}
}
