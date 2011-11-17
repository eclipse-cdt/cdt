/*******************************************************************************
 * Copyright (c) 2002, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Red Hat Inc. - multiple build console support
 *     Dmitry Kozlov (CodeSourcery) - Build error highlighting and navigation
 *                                    Save build output
 *     Alex Collins (Broadcom Corp.) - Global build console
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.buildconsole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.actions.ClearOutputAction;
import org.eclipse.ui.console.actions.TextViewerAction;
import org.eclipse.ui.console.actions.TextViewerGotoLineAction;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.texteditor.FindReplaceAction;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.IUpdate;

import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IBuildConsoleEvent;
import org.eclipse.cdt.ui.IBuildConsoleListener;
import org.eclipse.cdt.ui.IBuildConsoleManager;

import org.eclipse.cdt.internal.ui.preferences.BuildConsolePreferencePage;

public class BuildConsolePage extends Page
		implements
			ISelectionListener,
			IPropertyChangeListener,
			IBuildConsoleListener,
			ITextListener,
			IAdaptable {

	static final int POSITION_NEXT = -1;
	static final int POSITION_PREV = -2;
	static final int POSITION_FIST = -3;

	private BuildConsole fConsole;
	private IConsoleView fConsoleView;
	private String fContextMenuId;
	private BuildConsoleViewer fViewer;
	private IProject fProject;

	// text selection listener
	private ISelectionChangedListener fTextListener = new ISelectionChangedListener() {

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			updateSelectionDependentActions();
		}
	};

	// actions
	private ClearOutputAction fClearOutputAction;
	private Map<String, IAction> fGlobalActions = new HashMap<String, IAction>(10);
	private List<String> fSelectionActions = new ArrayList<String>(3);
	private CopyBuildLogAction fSaveLogAction;

	// menus
	private Menu fMenu;
	private ScrollLockAction fScrollLockAction;
	private boolean fIsLocked;
	private NextErrorAction fNextErrorAction;
	private PreviousErrorAction fPreviousErrorAction;
	private ShowErrorAction fShowErrorAction;

	/**
	 * @param view
	 * @param console
	 * @param contextId
	 */
	public BuildConsolePage(IConsoleView view, BuildConsole console,
			String contextId) {
		fConsole = console;
		fConsoleView = view;
		fContextMenuId = contextId;
	}

	protected void setProject(IProject project) {
		if (fProject != project && project.isAccessible()) {
			fProject = project;
		}
	}

	protected IProject getProject() {
		return fProject;
	}

	protected BuildConsole getConsole() {
		return fConsole;
	}

	protected IDocument setDocument() {
		IProject project = getProject();
		if (project != null) {
			IBuildConsoleManager consoleManager = getConsole().getConsoleManager();
			IDocument document = consoleManager.getConsoleDocument(project);
			IConsole console = consoleManager.getProjectConsole(project);
			getViewer().setDocument(document);
			if (console instanceof BuildConsolePartitioner) {
				BuildConsolePartitioner par = (BuildConsolePartitioner)console;
				// Show the error, but don't show it in the editor if we are viewing the global console.
				// Prevents showing errors in the editor for projects other than the current project.
				showError(par, fShowErrorAction.isChecked() && !(getConsole() instanceof GlobalBuildConsole));
			}
		}
		return null;
	}

	@Override
	public void consoleChange(final IBuildConsoleEvent event) {
		if (event.getType() == IBuildConsoleEvent.CONSOLE_START || event.getType() == IBuildConsoleEvent.CONSOLE_CLOSE) {
			Control control = getControl();
			if (control != null && !control.isDisposed()) {
				Display display = control.getDisplay();
				display.asyncExec(new Runnable() {

					/*
					 * (non-Javadoc)
					 *
					 * @see java.lang.Runnable#run()
					 */
					@Override
					public void run() {
						if (isAvailable()) {
							if (event.getType() == IBuildConsoleEvent.CONSOLE_CLOSE && getProject() != event.getProject()) {
								return;
							}
							setProject(event.getProject());
							if (isAvailable()) {
								setDocument();
								getConsole().setTitle(getProject());
							}
						}
					}
				});
			}
		}
	}

	boolean isAvailable() {
		return getControl() != null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.part.IPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		fViewer = new BuildConsoleViewer(parent);

		MenuManager manager = new MenuManager("#MessageConsole", "#MessageConsole"); //$NON-NLS-1$ //$NON-NLS-2$
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(new IMenuListener() {

			@Override
			public void menuAboutToShow(IMenuManager m) {
				contextMenuAboutToShow(m);
			}
		});
		fMenu = manager.createContextMenu(getControl());
		getControl().setMenu(fMenu);
		IPageSite site = getSite();
		site.registerContextMenu(fContextMenuId, manager, getViewer());
		site.setSelectionProvider(getViewer());
		createActions();
		configureToolBar(site.getActionBars().getToolBarManager());
		fViewer.getSelectionProvider().addSelectionChangedListener(fTextListener);

		JFaceResources.getFontRegistry().addListener(this);
		setFont(JFaceResources.getFont(BuildConsolePreferencePage.PREF_BUILDCONSOLE_FONT));
		setTabs(CUIPlugin.getDefault().getPreferenceStore().getInt(BuildConsolePreferencePage.PREF_BUILDCONSOLE_TAB_WIDTH));

		getConsole().addPropertyChangeListener(this);
		CUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);

		fViewer.addTextListener(this);
		fViewer.getTextWidget().setBackground(getConsole().getBackground());

		setInitialSelection();
	}

	/**
	 * Fill the context menu
	 *
	 * @param menu
	 *            menu
	 */
	protected void contextMenuAboutToShow(IMenuManager menu) {
		menu.add(fGlobalActions.get(ActionFactory.COPY.getId()));
		menu.add(fGlobalActions.get(ActionFactory.SELECT_ALL.getId()));
		menu.add(new Separator("FIND")); //$NON-NLS-1$
		menu.add(fGlobalActions.get(ActionFactory.FIND.getId()));
		menu.add(fGlobalActions.get(ITextEditorActionConstants.GOTO_LINE));
		menu.add(fClearOutputAction);
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		final Object source = event.getSource();
		final String property = event.getProperty();

		if (BuildConsole.P_STREAM_COLOR.equals(property) && source instanceof BuildConsoleStreamDecorator) {
			BuildConsoleStreamDecorator stream = (BuildConsoleStreamDecorator)source;
			if (stream.getConsole().equals(getConsole()) && getControl() != null) {
				Display display = getControl().getDisplay();
				display.asyncExec(new Runnable() {

					@Override
					public void run() {
						getViewer().getTextWidget().redraw();
					}
				});
			}
		} else if (property.equals(BuildConsolePreferencePage.PREF_BUILDCONSOLE_FONT)) {
			setFont(JFaceResources.getFont(BuildConsolePreferencePage.PREF_BUILDCONSOLE_FONT));
		} else if (property.equals(BuildConsolePreferencePage.PREF_BUILDCONSOLE_TAB_WIDTH)) {
			setTabs(CUIPlugin.getDefault().getPreferenceStore().getInt(BuildConsolePreferencePage.PREF_BUILDCONSOLE_TAB_WIDTH));
		} else if (IConsoleConstants.P_BACKGROUND_COLOR.equals(property)) {
			fViewer.getTextWidget().setBackground(fConsole.getBackground());
		}
	}

	protected void createActions() {
		fClearOutputAction = new ClearOutputAction(getViewer());
		fScrollLockAction = new ScrollLockAction(getViewer());
		fScrollLockAction.setChecked(fIsLocked);
		fNextErrorAction = new NextErrorAction(this);
		fPreviousErrorAction = new PreviousErrorAction(this);
		fShowErrorAction = new ShowErrorAction(this);
		fSaveLogAction = new CopyBuildLogAction(this);

		getViewer().setAutoScroll(!fIsLocked);
		// In order for the clipboard actions to accessible via their shortcuts
		// (e.g., Ctrl-C, Ctrl-V), we *must* set a global action handler for
		// each action
		IActionBars actionBars = getSite().getActionBars();
		TextViewerAction action = new TextViewerAction(getViewer(), ITextOperationTarget.COPY);
		action.configureAction(ConsoleMessages.BuildConsolePage__Copy_Ctrl_C_6,
				ConsoleMessages.BuildConsolePage_Copy_7, ConsoleMessages.BuildConsolePage_Copy_7);
		action.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		action.setDisabledImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
				ISharedImages.IMG_TOOL_COPY_DISABLED));
		action.setHoverImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		setGlobalAction(actionBars, ActionFactory.COPY.getId(), action);
		action = new TextViewerAction(getViewer(), ITextOperationTarget.SELECT_ALL);
		action.configureAction(ConsoleMessages.BuildConsolePage_Select__All_Ctrl_A_12,
				ConsoleMessages.BuildConsolePage_Select_All,
				ConsoleMessages.BuildConsolePage_Select_All);
		setGlobalAction(actionBars, ActionFactory.SELECT_ALL.getId(), action);
		//XXX Still using "old" resource access
		ResourceBundle bundle = ResourceBundle.getBundle(ConsoleMessages.BUNDLE_NAME);
		setGlobalAction(actionBars, ActionFactory.FIND.getId(), new FindReplaceAction(bundle, "find_replace_action_", //$NON-NLS-1$
				getConsoleView()));
		action = new TextViewerGotoLineAction(getViewer());
		setGlobalAction(actionBars, ITextEditorActionConstants.GOTO_LINE, action);
		actionBars.updateActionBars();
		fSelectionActions.add(ActionFactory.COPY.getId());
		fSelectionActions.add(ActionFactory.FIND.getId());
	}

	protected void updateSelectionDependentActions() {
		Iterator<String> iterator = fSelectionActions.iterator();
		while (iterator.hasNext()) {
			updateAction(iterator.next());
		}
	}

	protected void updateAction(String actionId) {
		IAction action = fGlobalActions.get(actionId);
		if (action instanceof IUpdate) {
			((IUpdate)action).update();
		}
	}

	protected void setGlobalAction(IActionBars actionBars, String actionID, IAction action) {
		fGlobalActions.put(actionID, action);
		actionBars.setGlobalActionHandler(actionID, action);
	}

	protected void configureToolBar(IToolBarManager mgr) {
		mgr.insertBefore(IConsoleConstants.OUTPUT_GROUP, new GroupMarker(BuildConsole.ERROR_GROUP));
		mgr.appendToGroup(BuildConsole.ERROR_GROUP, fNextErrorAction);
		mgr.appendToGroup(BuildConsole.ERROR_GROUP, fPreviousErrorAction);
		mgr.appendToGroup(BuildConsole.ERROR_GROUP, fShowErrorAction);
		mgr.appendToGroup(IConsoleConstants.OUTPUT_GROUP, fSaveLogAction);
		mgr.appendToGroup(IConsoleConstants.OUTPUT_GROUP, fScrollLockAction);
		mgr.appendToGroup(IConsoleConstants.OUTPUT_GROUP, fClearOutputAction);
	}

	protected BuildConsoleViewer getViewer() {
		return fViewer;
	}

	/**
	 * Returns the view this page is contained in
	 *
	 * @return the view this page is contained in
	 */
	protected IConsoleView getConsoleView() {
		return fConsoleView;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.part.IPage#dispose()
	 */
	@Override
	public void dispose() {
		getSite().getPage().removeSelectionListener(this);
		getConsole().getConsoleManager().removeConsoleListener(this);
		fViewer.removeTextListener(this);
		super.dispose();
	}

	@Override
	public void init(IPageSite pageSite) {
		super.init(pageSite);
		getSite().getPage().addSelectionListener(this);
		getConsole().getConsoleManager().addConsoleListener(this);
	}

	protected void setInitialSelection() {
		// Use the selection, if any
		IWorkbenchPage page= getSite().getPage();
		ISelection selection= null;
		if (page != null)
			selection= page.getSelection();

		if (convertSelectionToProject(selection) == null) {
			if (selection instanceof ITextSelection) {
				Object part= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
				if (part instanceof IEditorPart) {
					if (setSelectionFromEditor((IEditorPart)part) == true)
						return;
				}
			}
			IProject project = getConsole().getConsoleManager().getLastBuiltProject();
			if (project != null)
				selection = new StructuredSelection(project);
		}
		selectionChanged(null, selection);
	}

	boolean setSelectionFromEditor(IEditorPart part) {
		if (part == null)
			return false;
		IWorkbenchPartSite site= part.getSite();
		if (site == null)
			return false;
		ISelectionProvider provider= site.getSelectionProvider();
		if (provider != null ) {
			IEditorInput ei= part.getEditorInput();
			if (ei instanceof IFileEditorInput) {
				IFile file= ((IFileEditorInput)ei).getFile();
				selectionChanged(part, new StructuredSelection(file));
				return true;
			}
		}
		return false;
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		IProject newProject = convertSelectionToProject(selection);
		IProject oldProject = getProject();
		if (oldProject == null || (newProject != null && !newProject.equals(oldProject))) {
			setProject(newProject);
			setDocument();
			getConsole().setTitle(getProject());
		}
	}

	IProject convertSelectionToProject(ISelection selection) {
		IProject project = null;
		if (selection == null || ! (selection instanceof IStructuredSelection)) {
			return project;
		}
		IStructuredSelection ssel = (IStructuredSelection)selection;
		Object element = ssel.getFirstElement();
		if (element instanceof IAdaptable) {
			IAdaptable input = (IAdaptable)element;
			IResource resource = null;
			if (input instanceof IResource) {
				resource = (IResource)input;
			} else {
				resource = (IResource)input.getAdapter(IResource.class);
			}
			if (resource != null) {
				project = resource.getProject();
			}
		}
		return project;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.part.IPage#getControl()
	 */
	@Override
	public Control getControl() {
		if (fViewer != null) {
			return fViewer.getControl();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.part.IPage#setFocus()
	 */
	@Override
	public void setFocus() {
		Control control = getControl();
		if (control != null) {
			control.setFocus();
		}
		updateSelectionDependentActions();
	}

	/**
	 * Sets the font for this page.
	 *
	 * @param font
	 *            font
	 */
	protected void setFont(Font font) {
		getViewer().getTextWidget().setFont(font);
	}

	/**
	 * Sets the tab width for this page.
	 *
	 * @param tabs
	 *            tab width
	 */
	protected void setTabs(int tabs) {
		getViewer().getTextWidget().setTabs(tabs);
	}

	/**
	 * Refreshes this page
	 */
	protected void refresh() {
		getViewer().refresh();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class required) {
		if (IFindReplaceTarget.class.equals(required)) {
			return getViewer().getFindReplaceTarget();
		}
		if (Widget.class.equals(required)) {
			return getViewer().getTextWidget();
		}
//		if (IShowInSource.class.equals(required)) {
//			return this;
//		}
//		if (IShowInTargetList.class.equals(required)) {
//			return this;
//		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.text.ITextListener#textChanged(org.eclipse.jface.text.TextEvent)
	 */
	@Override
	public void textChanged(TextEvent event) {
		//		 update the find replace action if the document length is > 0
		IUpdate findReplace = (IUpdate)fGlobalActions.get(ActionFactory.FIND.getId());
		if (findReplace != null) {
			findReplace.update();
		}
	}

	/**
	 * Get the current CDT IConsole being displayed on the page
	 */
	private IConsole getCurrentConsole() {
		IBuildConsoleManager consoleManager = fConsole.getConsoleManager();
		return consoleManager.getProjectConsole(getProject());
	}

	/**
	 * Highlight next/previous error or error by console offset
	 * @param position POSITION_NEXT (-1), POSITION_PREV (-2), or offset
	 */
	void moveToError(int position) {
		IConsole console = getCurrentConsole();
		if (console == null) return;
		if (console instanceof BuildConsolePartitioner) {
			BuildConsolePartitioner par = (BuildConsolePartitioner)console;
			// Move to specified line in the model (BuildConsolePartitioner)
			if ( position == POSITION_NEXT ) {
				par.fDocumentMarkerManager.moveToNextError();
			} else if ( position == POSITION_PREV ) {
				par.fDocumentMarkerManager.moveToPreviousError();
			} else if ( position == POSITION_FIST ) {
				par.fDocumentMarkerManager.moveToFirstError();
			} else if ( position >= 0 ) {
				if ( ! par.fDocumentMarkerManager.moveToErrorByOffset(position) ) {
					// we haven't moved, because offset points to non-error partition
					return;
				}
			}
			showError(par, position > 0 || fShowErrorAction.isChecked() );
		}
	}

	/**
	 * Highlight current error and show it in editor
	 */
	public void showError(BuildConsolePartitioner par, boolean openInEditor) {
		// Highlight current error
		BuildConsolePartition p = par.fDocumentMarkerManager.getCurrentPartition();
		if ( p == null ) return;
		getViewer().selectPartition(par, p);
		// Show error in editor if necessary
		// (always show when absolute positioning, otherwise depends
		// on fShowErrorAction state)
		if ( openInEditor ) {
			openErrorInEditor(par.fDocumentMarkerManager.getCurrentErrorMarker());
		}
	}

	/**
	 * Open error specified by marker in editor
	 */
	public static void openErrorInEditor(ProblemMarkerInfo marker) {
		IWorkbenchWindow window = CUIPlugin.getActiveWorkbenchWindow();

		if ( marker == null || marker.file == null || window == null )  return;

		IWorkbenchPage page = window.getActivePage();
		if (page == null) return;

		IEditorPart editor = page.getActiveEditor();
		if (editor != null) {
			IEditorInput input = editor.getEditorInput();
			IFile file = ResourceUtil.getFile(input);
			if (file != null && file.equals(marker.file) && OpenStrategy.activateOnOpen()) {
				page.activate(editor);
			}
		}

		if ( marker.file instanceof IFile ) {
			try {
				// Find IMarker corresponding to ProblemMarkerInfo
				IMarker mrkrs[] = marker.file.findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_ONE);
				for (IMarker m: mrkrs) {
					if ( marker.lineNumber == ((Integer)m.getAttribute(IMarker.LINE_NUMBER)).intValue() &&
								marker.description.equals(m.getAttribute(IMarker.MESSAGE)) &&
								marker.severity == ((Integer)m.getAttribute(IMarker.SEVERITY)).intValue() ) {
						IDE.openEditor(page, m, OpenStrategy.activateOnOpen());
						return;
					}
				}
			} catch (PartInitException e) {
				CUIPlugin.log(e);
			} catch (CoreException e) {
				CUIPlugin.log(e);
			}
		}
	}

}
