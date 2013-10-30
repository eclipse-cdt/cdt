/*******************************************************************************
 * Copyright (c) 2002, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * Kevin Doyle (IBM) - [180875] - Added double click listener that handles opening of files
 * David Dykstal (IBM) - moved SystemPreferencesManager to a new package
 * Martin Oberhuber (Wind River) - Replace SystemRegistry by ISystemRegistry
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 * Martin Oberhuber (Wind River) - [185552] Remove remoteSystemsViewPreferencesActions extension point
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Martin Oberhuber (Wind River) - [186779] Fix IRSESystemType.getAdapter()
 * Martin Oberhuber (Wind River) - [190195] Cannot enable new connection prompt in system view
 * Martin Oberhuber (Wind River) - [190271] Move ISystemViewInputProvider to Core
 * David Dykstal (IBM) - [191311] enable global properties action
 * Martin Oberhuber (Wind River) - [196936] Hide disabled system types
 * Martin Oberhuber (Wind River) - [197025] Wait for model complete before restoring initial state
 * Martin Oberhuber (Wind River) - [197025][197167] Improved wait for model complete
 * David McKnight   (IBM)        - [199424] restoring memento state asynchronously
 * David McKnight   (IBM)        - [187711] Link with Editor handled by extension
 * David Dykstal (IBM) - [226728] NPE during init with clean workspace
 * David McKnight (IBM) 		 - [225747] [dstore] Trying to connect to an "Offline" system throws an NPE
 * David Dykstal (IBM) - [216858] Need the ability to Import/Export RSE connections for sharing
 * Kevin Doyle 	 (IBM) - [186769] Enable Contributions to Drop Down menu of Remote Systems view -> Preferences
 * David McKnight (IBM)  - [244807] System view does not handle restore from cache
 * David McKnight (IBM)  - [247544] [performance] Restoring Selection on Restart can cause the UI to freeze
 * Kevin Doyle 		(IBM)		 - [242431] Register a new unique context menu id, so contributions can be made to all our views
 * Li Ding          (IBM)        - [256135] Subsystem not restored in system view tree if subsystem configuration does not support filter
 * David McKnight   (IBM)        - [257721] Doubleclick doing special handling and expanding
 * David McKnight   (IBM)        - [250417] Restore from memento flag set to false during restore on startup
 * Martin Oberhuber (Wind River) - [286122] Avoid NPE when restoring memento
 * David McKnight   (IBM)        - [286670] TVT35:TCT586: CHS: English Strings Found
 * Martin Oberhuber (Wind River) - [326910] RSE looses selection when creating a project
 * David McKnight   (IBM)        - [330386] RSE SystemView has Focus Problems with Eclipse SDK 4.1M3
 * David McKnight   (IBM)        - [238365] Collapsing tree in new window collapses tree in Original window
 * David McKnight   (IBM)        - [330398] RSE leaks SWT resources
 * David McKnight   (IBM)        - [251654] System View Restore doesn't take into account Expand To Filter
 * David McKnight   (IBM)        - [420749] Unhandled event loop exception rse.internal.ui.view.SystemViewPart.restoreInitialState
 *******************************************************************************/

package org.eclipse.rse.internal.ui.view;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemPreferenceChangeEvent;
import org.eclipse.rse.core.events.ISystemPreferenceChangeEvents;
import org.eclipse.rse.core.events.ISystemPreferenceChangeListener;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.core.filters.ISystemFilterPoolReference;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.filters.ISystemFilterStringReference;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.ISystemViewInputProvider;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.core.RSEInitJob;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.internal.ui.actions.SystemCollapseAllAction;
import org.eclipse.rse.internal.ui.actions.SystemImportConnectionAction;
import org.eclipse.rse.internal.ui.actions.SystemPreferenceQualifyConnectionNamesAction;
import org.eclipse.rse.internal.ui.actions.SystemPreferenceRestoreStateAction;
import org.eclipse.rse.internal.ui.actions.SystemPreferenceShowFilterPoolsAction;
import org.eclipse.rse.internal.ui.actions.SystemShowPreferencesPageAction;
import org.eclipse.rse.internal.ui.actions.SystemWorkWithProfilesAction;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.RSESystemTypeAdapter;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.SystemPreferencesManager;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.actions.SystemCopyToClipboardAction;
import org.eclipse.rse.ui.actions.SystemNewConnectionAction;
import org.eclipse.rse.ui.actions.SystemPasteFromClipboardAction;
import org.eclipse.rse.ui.actions.SystemRefreshAction;
import org.eclipse.rse.ui.actions.SystemRefreshAllAction;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.view.ContextObject;
import org.eclipse.rse.ui.view.IContextObject;
import org.eclipse.rse.ui.view.IRSEViewPart;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.rse.ui.view.IViewLinker;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.CellEditorActionHandler;
import org.eclipse.ui.part.ISetSelectionTarget;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.views.framelist.FrameList;
/**
 * This is the desktop view wrapper of the System View viewer.
 */
public class SystemViewPart
	extends ViewPart
	implements ISetSelectionTarget, IShellProvider, ISystemMessageLine, IElementFactory, IPersistableElement, IAdapterFactory, ISystemPreferenceChangeListener, ISelectionChangedListener, IRSEViewPart
{



	protected SystemView systemView;
	protected ISystemViewInputProvider input = null;
	protected String message, errorMessage;
	protected SystemMessage sysErrorMessage;
	protected IStatusLineManager statusLine = null;
	protected boolean inputIsRoot = true;


	protected FrameList frameList;
	protected SystemViewPartGotoActionGroup gotoActionGroup;

	// link with editor stuff
	protected boolean _isLinkingEnabled = false;

	// view linker is used when a link with editor is required
	protected IViewLinker _viewLinker;

	// remember-state variables...

	protected IMemento fMemento;
	protected IAdapterManager platformManager;
	// preference toggle actions that need to be updated when preferences change
	protected SystemPreferenceQualifyConnectionNamesAction qualifyConnectionNamesAction;
	protected SystemPreferenceShowFilterPoolsAction showFilterPoolsAction;
	protected SystemPreferenceRestoreStateAction restoreStateAction;
	//protected SystemPreferenceShowFilterStringsAction  showFilterStringsAction;
	protected static SystemWorkWithProfilesAction wwProfilesAction;
	// copy and paste actions
	protected SystemCopyToClipboardAction _copyAction;
	protected SystemPasteFromClipboardAction _pasteAction;

	// Persistence tags.
	static final String TAG_RELEASE = "release"; //$NON-NLS-1$
	static final String TAG_SELECTION = "selection"; //$NON-NLS-1$
	static final String TAG_EXPANDED_TO = "expandedTo"; //$NON-NLS-1$
	static final String TAG_EXPANDED = "expanded"; //$NON-NLS-1$
	static final String TAG_ELEMENT = "element"; //$NON-NLS-1$
	static final String TAG_PATH = "path"; //$NON-NLS-1$
	static final String TAG_FILTER = "filter"; //$NON-NLS-1$
	static final String TAG_INPUT = "svInput"; //$NON-NLS-1$
	static final String TAG_VERTICAL_POSITION = "verticalPosition"; //$NON-NLS-1$
	static final String TAG_HORIZONTAL_POSITION = "horizontalPosition"; //$NON-NLS-1$
	static final String TAG_SHOWFILTERPOOLS = "showFilterPools"; //$NON-NLS-1$
	static final String TAG_SHOWFILTERSTRINGS = "showFilterStrings"; //$NON-NLS-1$
	static final String TAG_LINKWITHEDITOR = "linkWithEditor"; //$NON-NLS-1$

	public static final String MEMENTO_DELIM = "///"; //$NON-NLS-1$

	// constants
	public static final String ID = "org.eclipse.rse.ui.view.systemView"; // matches id in plugin.xml, view tag //$NON-NLS-1$

	/**
	 * SystemViewPart constructor.
	 */
	public SystemViewPart()
	{
		super();
		//RSEUIPlugin.logDebugMessage(this.getClass().getName(),"INSIDE CTOR FOR SYSTEMVIEWPART.");
	}
	/**
	 * Easy access to the TreeViewer object
	 */
	public SystemView getSystemView()
	{
		return systemView;
	}

	public Viewer getRSEViewer()
	{
		return systemView;
	}

	/**
	 * When an element is added/deleted/changed/etc and we have focus, this
	 * method is called. See SystemStaticHelpers.selectReveal method.
	 */
	public void selectReveal(ISelection selection)
	{
		ISelection origSel = systemView.getSelection();
		if (origSel.isEmpty()) {
			systemView.setSelection(selection, true);
		} else {
			// bug check whether the new selection can be set,
			// before actually setting it. Restore old selection
			// if the new one does not work.
			systemView.setSelection(selection, false);
			ISelection newSel = systemView.getSelection();
			if (newSel.isEmpty()) {
				systemView.setSelection(origSel, false);
			} else {
				systemView.setSelection(newSel, true);
			}
		}
	}

	/**
	 * Returns the name for the given element.
	 * Used as the name for the current frame.
	 */
	protected String getFrameName(Object element)
	{
		return ((ILabelProvider) getSystemView().getLabelProvider()).getText(element);
	}

	/**
	 * Returns the tool tip text for the given element.
	 * Used as the tool tip text for the current frame, and for the view title tooltip.
	 */
	protected String getFrameToolTipText(Object element)
	{
		return ((ILabelProvider) getSystemView().getLabelProvider()).getText(element);
	}

	public boolean isLinkingEnabled()
	{
		return _isLinkingEnabled;
	}

	public void setLinkingEnabled(boolean flag, IViewLinker viewLinker)
	{
		_isLinkingEnabled = flag;
		if (_isLinkingEnabled)
		{
			_viewLinker = viewLinker;
			IWorkbenchWindow activeWindow = SystemBasePlugin.getActiveWorkbenchWindow();
			IWorkbenchPage activePage = activeWindow.getActivePage();
			IEditorPart editor = activePage.getActiveEditor();
			if (editor != null)
			{
				editorActivated(editor);
			}
		}
	}

	/**
		  * An editor has been activated.  Sets the selection in this navigator
		  * to be the editor's input, if linking is enabled.
		  *
		  * @param editor the active editor
		  */
	protected void editorActivated(IEditorPart editor)
	{
		if (!_isLinkingEnabled)
			return;

		if (_viewLinker != null){
			_viewLinker.linkEditorToView(editor, systemView);
		}
	}
	/**
	 * Updates the title text and title tool tip.
	 * Called whenever the input of the viewer changes.
	 */
	protected void updateTitle()
	{
		//IAdaptable inputObj = getSite().getPage().getInput();
		Object inputObj = getSystemView().getInput();
		SystemBasePlugin.logInfo("Inside updateTitle. inputObject class type: " + inputObj.getClass().getName()); //$NON-NLS-1$

		{
			setTitleToolTip(getFrameToolTipText(input));
			String viewName = getConfigurationElement().getAttribute("name"); //$NON-NLS-1$
			if (inputObj instanceof IHost)
			{
				IHost conn = (IHost) inputObj;
				setPartName(viewName + " : " + conn.getAliasName()); //$NON-NLS-1$
			}
			else if (inputObj instanceof ISubSystem)
			{
				ISubSystem ss = (ISubSystem) inputObj;
				setPartName(viewName + " : " + ss.getName()); //$NON-NLS-1$
			}
			else if (inputObj instanceof ISystemFilterPoolReference)
			{
				ISystemFilterPoolReference sfpr = (ISystemFilterPoolReference) inputObj;
				setPartName(viewName + " : " + sfpr.getName()); //$NON-NLS-1$
			}
			else if (inputObj instanceof ISystemFilterReference)
			{
				ISystemFilterReference sfr = (ISystemFilterReference) inputObj;
				setPartName(viewName + " : " + sfr.getName()); //$NON-NLS-1$
			}
			else if (inputObj instanceof ISystemFilterStringReference)
			{
				ISystemFilterStringReference sfsr = (ISystemFilterStringReference) inputObj;
				setPartName(viewName + " : " + sfsr.getString()); //$NON-NLS-1$
			}
			else
			{
				setPartName(viewName);
				setTitleToolTip(""); //$NON-NLS-1$
			}
		}
	}

	/*
	 * Set our input provider that will be used to populate the tree view
	 *
	public void setInputProvider(ISystemViewInputProvider input)
	{
		RSEUIPlugin.logDebugMessage(this.getClass().getName(),"INSIDE SETINPUTPROVIDER FOR SYSTEMVIEWPART.");
		this.input = input;
	}*/
	/**
	 * Creates the SWT controls for a part.
	 * Called by Eclipse framework.
	 */
	public void createPartControl(Composite parent)
	{
		//RSEUIPlugin.logInfo("INSIDE CREATEPARTCONTROL FOR SYSTEMVIEWPART.");
		if (input == null)
			//input = RSECorePlugin.getTheSystemRegistry();
			input = getInputProvider();
		systemView = new SystemView(getShell(), parent, input, this);
		frameList = createFrameList();

		gotoActionGroup = new SystemViewPartGotoActionGroup(this);
		IActionBars actionBars = getActionBars();
		if (actionBars != null)
		{
			actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(), systemView.getDeleteAction());
			actionBars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(), systemView.getSelectAllAction());
			actionBars.setGlobalActionHandler(ActionFactory.PROPERTIES.getId(), systemView.getPropertyDialogAction());
			actionBars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), systemView.getRefreshAction());
			actionBars.setGlobalActionHandler(ActionFactory.RENAME.getId(), systemView.getRenameAction());

			statusLine = actionBars.getStatusLineManager();
		}

		// register global edit actions
		CellEditorActionHandler editorActionHandler = new CellEditorActionHandler(getViewSite().getActionBars());

		_copyAction = new SystemCopyToClipboardAction(systemView.getShell(), null);
		_pasteAction = new SystemPasteFromClipboardAction(systemView.getShell(), null);

		editorActionHandler.setCopyAction(_copyAction);
		editorActionHandler.setPasteAction(_pasteAction);
		editorActionHandler.setDeleteAction(systemView.getDeleteAction());
		editorActionHandler.setSelectAllAction(systemView.getSelectAllAction());

		systemView.addSelectionChangedListener(this);
		//hook the part focus to the viewer's control focus.
		//hookFocus(systemView.getControl());


		//prime the selection
		//selectionChanged(null, getSite().getDesktopWindow().getSelectionService().getSelection());

		boolean showConnectionActions = true;
		fillLocalToolBar(showConnectionActions);

		// -----------------------------
		// Enable right-click popup menu
		// -----------------------------
		getSite().registerContextMenu(systemView.getContextMenuManager(), systemView);
		getSite().registerContextMenu(ISystemContextMenuConstants.RSE_CONTEXT_MENU, systemView.getContextMenuManager(), systemView);

		// ----------------------------------------------------------------------
		// Enable property sheet updates when tree items are selected.
		// Note for this to work each item in the tree must either implement
		//   IPropertySource, or support IPropertySource.class as an adapter type
		//   in its AdapterFactory.
		// ----------------------------------------------------------------------
		getSite().setSelectionProvider(systemView);
		// listen to editor events for linking
		getSite().getPage().addPartListener(partListener);

		SystemWidgetHelpers.setHelp(parent, RSEUIPlugin.HELPPREFIX + "sysv0000"); //$NON-NLS-1$

		// ----------------------
		// Restore previous state
		// ----------------------
		final RSEInitJob initRSEJob = RSEInitJob.getInstance();
		if (initRSEJob == null) {
			//Already initialized - Profiles are loaded, we can restore state right away without blocking
			restoreInitialState();
		} else {
			//Wait until model fully restored, then fire a callback to restore state.
			//Remember current display, since we're definitely on the display thread here
			final Display display = Display.getCurrent();
			Job waitForRestoreCompleteJob = new Job("WaitForRestoreComplete") { //$NON-NLS-1$
				protected IStatus run(IProgressMonitor monitor) {
					try {
						//Wait for initRSEJob.
						initRSEJob.waitForCompletion();
						//callback
						display.asyncExec(new Runnable() {
							public void run() {
								restoreInitialState();
							}
						});
					} catch(InterruptedException e) {
						return Status.CANCEL_STATUS;
					}
					return Status.OK_STATUS;
				}
			};
			waitForRestoreCompleteJob.setSystem(true);
			waitForRestoreCompleteJob.schedule();
		}
	}

	/**
	 * Restore initial state of the SystemView. Can only be done
	 * once the RSE Model has been fully restored, so this may
	 * need to run in a callback.
	 */
	private void restoreInitialState() {
		if ((fMemento != null) && (input instanceof ISystemRegistry))
			restoreState(fMemento);
		//fMemento = null;

		// Register for preference change events
		ISystemRegistry registry = RSECorePlugin.getTheSystemRegistry();
		registry.addSystemPreferenceChangeListener(this);

		// if this is the primary RSE view, and there are no user-defined
		// connections, auto-expand the New Connection prompt...
		if ((input == RSECorePlugin.getTheSystemRegistry()) && (RSECorePlugin.getTheSystemRegistry().getHosts().length == 1))
		{
			// assume this is the primary RSE view

			// WE GET ALL THE WAY HERE, BUT THESE LINES OF CODE ARE INEFFECTIVE FOR SOME REASON!!
			Tree t = systemView.getTree();
			
			if (!t.isDisposed()  && t.getItemCount() > 0) {
				TreeItem firstItem = t.getItems()[0];
				systemView.setSelection(new StructuredSelection(firstItem.getData()));
				systemView.setExpandedState(firstItem.getData(), true);
			}
		}
	}


	/**
	 * Creates the frame source and frame list, and connects them.
	 */
	protected FrameList createFrameList()
	{
		SystemViewPartFrameSource frameSource = new SystemViewPartFrameSource(this);
		FrameList frameList = new FrameList(frameSource);
		frameSource.connectTo(frameList);
		return frameList;
	}
	/**
	 * Return the FrameList object for this view part
	 */
	public FrameList getFrameList()
	{
		return frameList;
	}

	/**
	 * Return the Goto action group
	 */
	public SystemViewPartGotoActionGroup getGotoActionGroup()
	{
		return gotoActionGroup;
	}

	/**
	 * Return the shell for this view part
	 */
	public Shell getShell()
	{
		if (systemView != null)
			return systemView.getTree().getShell();
		else
			return getSite().getShell();
	}
	/**
	 * Return the action bars for this view part
	 */
	public IActionBars getActionBars()
	{
		return getViewSite().getActionBars();
	}

	/**
	 * @see IWorkbenchPart#setFocus()
	 */
	public void setFocus()
	{
		//System.out.println("INSIDE SETFOCUS FOR SYSTEMVIEWPART. SYSTEMVIEW NULL? " + (systemView==null));
		systemView.getControl().setFocus();
		/* the following was an attempt to fix problem with scrollbar needing two clicks to activate. didn't help.
		if (!SystemPreferencesGlobal.getGlobalSystemPreferences().getRememberState())
		{
			TreeItem[] roots = systemView.getTree().getItems();
			if ((roots != null) && (roots.length>0))
			  systemView.setSelection(new StructuredSelection(roots[0].getData()));
		}
		*/

	}

	public void selectionChanged(SelectionChangedEvent e)
	{
		IStructuredSelection sel = (IStructuredSelection) e.getSelection();
		_copyAction.setEnabled(_copyAction.updateSelection(sel));
		_pasteAction.setEnabled(_pasteAction.updateSelection(sel));
		//systemView.getPropertyDialogAction();
		if (_isLinkingEnabled)
		{
			linkToEditor(sel);
		}
	}


	// link back to editor
	protected void linkToEditor(IStructuredSelection selection)
	{
		if (_viewLinker != null)
		{
			Object obj = selection.getFirstElement();
			IWorkbenchPage page = getSite().getPage();
			_viewLinker.linkViewToEditor(obj, page);
		}
	}


	/**
	 * Fills the local tool bar with actions.
	 */
	protected void fillLocalToolBar(boolean showConnectionActions)
	{
		IActionBars actionBars = getViewSite().getActionBars();
		SystemRefreshAction refreshAction = new SystemRefreshAction(getShell());
		refreshAction.setId(ActionFactory.REFRESH.getId());
		refreshAction.setActionDefinitionId("org.eclipse.ui.file.refresh"); //$NON-NLS-1$
		refreshAction.setSelectionProvider(systemView);
		actionBars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), refreshAction);

		// Note: Keep the group marker and separators in place! ISV's needs
		//       them to find their places within the toolbar. Re-use as
		//       much constants from the remote system view context menu as possible.
		//       This in particular makes orientating and contributing via plugin.xml
		//       much easier.

		IToolBarManager toolBarMgr = actionBars.getToolBarManager();
		if (showConnectionActions)
		{
			toolBarMgr.add(new GroupMarker(ISystemContextMenuConstants.GROUP_NEW_NONCASCADING));
			SystemNewConnectionAction newConnAction = new SystemNewConnectionAction(getShell(), false, systemView); // false implies not from popup menu
			toolBarMgr.add(newConnAction);
		}

		toolBarMgr.add(new GroupMarker(ISystemContextMenuConstants.GROUP_BUILD));
		refreshAction.setSelectionProvider(systemView);
		toolBarMgr.add(refreshAction);

		toolBarMgr.add(new Separator(ISystemContextMenuConstants.GROUP_GOTO));
		SystemViewPartGotoActionGroup gotoActions = new SystemViewPartGotoActionGroup(this);
		gotoActions.fillActionBars(actionBars);

		// defect 41203
		toolBarMgr.add(new Separator(ISystemContextMenuConstants.GROUP_EXPAND));
		SystemCollapseAllAction collapseAllAction = new SystemCollapseAllAction(getShell());
		collapseAllAction.setSelectionProvider(systemView);
		collapseAllAction.setViewer(systemView); // fix for bug 238365 - action needs to know the viewer
		toolBarMgr.add(collapseAllAction);

		IMenuManager menuMgr = actionBars.getMenuManager();
		populateSystemViewPulldownMenu(menuMgr, getShell(), showConnectionActions, this, systemView);

		// [179181] [api] Dynamic system type provider need a hook to add dynamic system type specific toolbar groups.
		IRSESystemType[] systemTypes = SystemWidgetHelpers.getValidSystemTypes(null);
		for (int i = 0; i < systemTypes.length; i++) {
			IRSESystemType systemType = systemTypes[i];
			Object adapter = systemType.getAdapter(RSESystemTypeAdapter.class);
			if (adapter instanceof RSESystemTypeAdapter) {
				((RSESystemTypeAdapter)adapter).addCustomToolbarGroups(this);
			}
		}
	}

	/**
	 * Pulldown the local toolbar menu with actions
	 */
	public static void populateSystemViewPulldownMenu(IMenuManager menuMgr, Shell shell, boolean showConnectionActions, IWorkbenchPart viewPart, ISelectionProvider sp)
	{
		SystemRefreshAllAction refreshAllAction = new SystemRefreshAllAction(shell);
		//SystemCascadingUserIdPerSystemTypeAction userIdPerSystemTypeAction = new SystemCascadingUserIdPerSystemTypeAction(shell); d51541
		SystemPreferenceShowFilterPoolsAction showFilterPoolsAction = new SystemPreferenceShowFilterPoolsAction(shell);
		SystemPreferenceQualifyConnectionNamesAction qualifyConnectionNamesAction = null;
		SystemPreferenceRestoreStateAction restoreStateAction = new SystemPreferenceRestoreStateAction(shell);

		if (viewPart instanceof SystemViewPart)
		{
			((SystemViewPart) viewPart).showFilterPoolsAction = showFilterPoolsAction; // set non-static field
			 ((SystemViewPart) viewPart).restoreStateAction = restoreStateAction; // set non-static field
		}

		if (showConnectionActions)
		{
			boolean fromPopup = false;
			boolean wantIcon = false;
			SystemNewConnectionAction newConnectionAction = new SystemNewConnectionAction(shell, fromPopup, wantIcon, sp);
			SystemImportConnectionAction importConnectionAction = new SystemImportConnectionAction();
			importConnectionAction.setShell(shell);
			importConnectionAction.setText(SystemResources.RESID_IMPORT_CONNECTION_LABEL_LONG);
			SystemWorkWithProfilesAction wwProfilesAction = new SystemWorkWithProfilesAction(shell);
			menuMgr.add(newConnectionAction);
			menuMgr.add(importConnectionAction);
			menuMgr.add(new Separator());
			menuMgr.add(wwProfilesAction);
			menuMgr.add(new Separator());
			// moved Qualify Connection Names from here for d51541
			//menuMgr.add(new Separator()); d51541
		}
		menuMgr.add(refreshAllAction);
		menuMgr.add(new Separator());
		if (showConnectionActions)
		{
			qualifyConnectionNamesAction = new SystemPreferenceQualifyConnectionNamesAction(shell);
			if (viewPart instanceof SystemViewPart)
				 ((SystemViewPart) viewPart).qualifyConnectionNamesAction = qualifyConnectionNamesAction;
			menuMgr.add(qualifyConnectionNamesAction); // moved here for d51541
		}
		//menuMgr.add(userIdPerSystemTypeAction.getSubMenu()); d51541
		menuMgr.add(showFilterPoolsAction);
		menuMgr.add(restoreStateAction); // d51541

		// Now display any contributed action shortcuts for bringing up
		// a particular preference page...
		MenuManager subMenu = new MenuManager(SystemResources.ACTION_CASCADING_PREFERENCES_LABEL, ISystemContextMenuConstants.MENU_PREFERENCES);
		menuMgr.add(subMenu);

		// The initial "RSE" Preference page action is added hardcoded.
		// This comes from the former SystemCascadingPreferencesAction.
		// FIXME will be moved to using command/hander extension point as per
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=186769
		SystemShowPreferencesPageAction action = new SystemShowPreferencesPageAction();
		action.setPreferencePageID("org.eclipse.rse.ui.preferences.RemoteSystemsPreferencePage"); //$NON-NLS-1$
		action.setText(SystemResources.ACTION_SHOW_PREFERENCEPAGE_LABEL);
		action.setToolTipText(SystemResources.ACTION_SHOW_PREFERENCEPAGE_TOOLTIP);
		action.setHelp("org.eclipse.rse.ui.aprefrse"); //$NON-NLS-1$
		subMenu.add(action);
		subMenu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_ADDITIONS));

		SystemViewMenuListener menuListener = new SystemViewMenuListener(true); // true says this is a persistent menu
		if (viewPart instanceof ISystemMessageLine)
			menuListener.setShowToolTipText(true, (ISystemMessageLine) viewPart);
		menuMgr.addMenuListener(menuListener);

	}

	/**
	 *
	 */
	public void dispose()
	{
		super.dispose();
		if (platformManager != null)
			unregisterWithManager(platformManager);
		RSECorePlugin.getTheSystemRegistry().removeSystemPreferenceChangeListener(this);
		getSite().getPage().removePartListener(partListener);
		//System.out.println("INSIDE DISPOSE FOR SYSTEMVIEWPART.");
	}

	/**
	 * Returns the initial input provider for the viewer.
	 * Tries to deduce the appropriate input provider based on current input.
	 */
	protected ISystemViewInputProvider getInputProvider()
	{
		IAdaptable inputObj = getSite().getPage().getInput();
		inputIsRoot = false;
		ISystemViewInputProvider inputProvider = RSECorePlugin.getTheSystemRegistry();
		if (inputObj != null)
		{
			platformManager = Platform.getAdapterManager();
			if (inputObj instanceof IHost)
			{
				IHost conn = (IHost) inputObj;
				inputProvider = new SystemViewAPIProviderForConnections(conn);
				setPartName(getTitle() + " : " + conn.getAliasName()); //$NON-NLS-1$
			}
			else if (inputObj instanceof ISubSystem)
			{
				ISubSystem ss = (ISubSystem) inputObj;
				inputProvider = new SystemViewAPIProviderForSubSystems(ss);
				setPartName(getTitle() + " : " + ss.getName()); //$NON-NLS-1$
			}
			else if (inputObj instanceof ISystemFilterPoolReference)
			{
				ISystemFilterPoolReference sfpr = (ISystemFilterPoolReference) inputObj;
				inputProvider = new SystemViewAPIProviderForFilterPools(sfpr);
				setPartName(getTitle() + " : " + sfpr.getName()); //$NON-NLS-1$
			}
			else if (inputObj instanceof ISystemFilterReference)
			{
				ISystemFilterReference sfr = (ISystemFilterReference) inputObj;
				inputProvider = new SystemViewAPIProviderForFilters(sfr);
				setPartName(getTitle() + " : " + sfr.getName()); //$NON-NLS-1$
			}
			else if (inputObj instanceof ISystemFilterStringReference)
			{
				ISystemFilterStringReference sfsr = (ISystemFilterStringReference) inputObj;
				inputProvider = new SystemViewAPIProviderForFilterStrings(sfsr);
				setPartName(getTitle() + " : " + sfsr.getString()); //$NON-NLS-1$
			}
			else
			{
				platformManager = null;
				inputIsRoot = true;
			}

			if (platformManager != null)
				registerWithManager(platformManager, inputObj);
			//msg = "INSIDE GETINPUTPROVIDER FOR SYSTEMVIEWPART: inputObj="+inputObj+", input class="+inputObj.getClass().getName()+", inputProvider="+inputProvider;
		}
		else
		{
			//msg = "INSIDE GETINPUTPROVIDER FOR SYSTEMVIEWPART: inputObj is null, inputProvider="+inputProvider;
		}
		//RSEUIPlugin.logDebugMessage(this.getClass().getName(),msg);
		//System.out.println("INSIDE getInputProvider. inputProvider = "+inputProvider);
		return inputProvider;
	}

	// --------------------------------------------
	// ISystemPreferenceChangeListener interface...
	// --------------------------------------------
	public void systemPreferenceChanged(ISystemPreferenceChangeEvent event)
	{
		if ((event.getType() == ISystemPreferenceChangeEvents.EVENT_QUALIFYCONNECTIONNAMES) && (qualifyConnectionNamesAction != null))
			qualifyConnectionNamesAction.setChecked(SystemPreferencesManager.getQualifyConnectionNames());
		else if ((event.getType() == ISystemPreferenceChangeEvents.EVENT_SHOWFILTERPOOLS) && (showFilterPoolsAction != null))
			showFilterPoolsAction.setChecked(SystemPreferencesManager.getShowFilterPools());
		else if ((event.getType() == ISystemPreferenceChangeEvents.EVENT_RESTORESTATE) && (restoreStateAction != null))
			restoreStateAction.setChecked(SystemPreferencesManager.getRememberState());

		//else if ((event.getType() == ISystemPreferenceChangeEvents.EVENT_SHOWFILTERSTRINGS) &&
		//    (showFilterStringsAction != null))
		//  showFilterStringsAction.setChecked(SystemPreferencesManager.getPreferencesManager().getShowFilterStrings());

	}

	// -------------------------------
	// ISystemMessageLine interface...
	// -------------------------------
	/**
	 * Clears the currently displayed error message and redisplayes
	 * the message which was active before the error message was set.
	 */
	public void clearErrorMessage()
	{
		errorMessage = null;
		sysErrorMessage = null;
		if (statusLine != null)
			statusLine.setErrorMessage(errorMessage);
	}
	/**
	 * Clears the currently displayed message.
	 */
	public void clearMessage()
	{
		message = null;
		if (statusLine != null)
			statusLine.setMessage(message);
	}
	/**
	 * Get the currently displayed error text.
	 * @return The error message. If no error message is displayed <code>null</code> is returned.
	 */
	public String getErrorMessage()
	{
		return errorMessage;
	}
	/**
	 * Get the currently displayed message.
	 * @return The message. If no message is displayed <code>null<code> is returned.
	 */
	public String getMessage()
	{
		return message;
	}
	/**
	 * Display the given error message. A currently displayed message
	 * is saved and will be redisplayed when the error message is cleared.
	 */
	public void setErrorMessage(String message)
	{
		this.errorMessage = message;
		if (statusLine != null)
			statusLine.setErrorMessage(message);
	}
	/**
	 * Get the currently displayed error text.
	 * @return The error message. If no error message is displayed <code>null</code> is returned.
	 */
	public SystemMessage getSystemErrorMessage()
	{
		return sysErrorMessage;
	}

	/**
	 * Display the given error message. A currently displayed message
	 * is saved and will be redisplayed when the error message is cleared.
	 */
	public void setErrorMessage(SystemMessage message)
	{
		sysErrorMessage = message;
		setErrorMessage(message.getLevelOneText());
	}
	/**
	 * Display the given error message. A currently displayed message
	 * is saved and will be redisplayed when the error message is cleared.
	 */
	public void setErrorMessage(Throwable exc)
	{
		setErrorMessage(exc.getMessage());
	}

	/**
	 * Set the message text. If the message line currently displays an error,
	 * the message is stored and will be shown after a call to clearErrorMessage
	 */
	public void setMessage(String message)
	{
		this.message = message;
		if (statusLine != null)
			statusLine.setMessage(message);
	}
	/**
	 *If the message line currently displays an error,
	 * the message is stored and will be shown after a call to clearErrorMessage
	 */
	public void setMessage(SystemMessage message)
	{
		setMessage(message.getLevelOneText());
	}

	// -------------------------------------------
	// MEMENTO SUPPORT (SAVING/RESTORING STATE)...
	// -------------------------------------------
	/**
	 * Initializes this view with the given view site.  A memento is passed to
	 * the view which contains a snapshot of the views state from a previous
	 * session.  Where possible, the view should try to recreate that state
	 * within the part controls.
	 * <p>
	 * The parent's default implementation will ignore the memento and initialize
	 * the view in a fresh state.  Subclasses may override the implementation to
	 * perform any state restoration as needed.
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException
	{
		init(site);
		fMemento = memento;
		//System.out.println("INSIDE INIT");
	}

	/**
	 * Returns the image descriptor with the given relative path.
	 */
	protected ImageDescriptor getNavigatorImageDescriptor(String relativePath)
	{
		return RSEUIPlugin.getDefault().getImageDescriptorFromIDE(relativePath); // more reusable
		/*
		String iconPath = "icons/full/"; //$NON-NLS-1$
		try
		{
			AbstractUIPlugin plugin = (AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
			URL installURL = plugin.getDescriptor().getInstallURL();

			URL url = new URL(installURL, iconPath + relativePath);
			ImageDescriptor descriptor = ImageDescriptor.createFromURL(url);
			return descriptor;
		}
		catch (MalformedURLException e)
		{
			// should not happen
			return ImageDescriptor.getMissingImageDescriptor();
		}*/
	}

	/**
	 * Method declared on IViewPart.
	 */
	public void saveState(IMemento memento)
	{
		//System.out.println("INSIDE SAVESTATE");
		if (!SystemPreferencesManager.getRememberState())
			return;
		if (systemView == null)
		{
			// part has not been created
			if (fMemento != null) //Keep the old state;
				memento.putMemento(fMemento);
			return;
		}

		if (_isLinkingEnabled)
		{
			memento.putString(TAG_LINKWITHEDITOR, "t"); //$NON-NLS-1$
		}
		else
		{
			memento.putString(TAG_LINKWITHEDITOR, "f"); //$NON-NLS-1$
		}

		// We record the current release for future in case anything significant changes from release to release
		memento.putString(TAG_RELEASE, RSECorePlugin.CURRENT_RELEASE_NAME);

		// We record the current preferences for show filter string and show filter pools.
		// We do this to ensure the states match on restore. If they don't we will be in trouble
		//  restoring expansion state and hence will abandon it.

		memento.putString(TAG_SHOWFILTERPOOLS, SystemPreferencesManager.getShowFilterPools() ? "t" : "f"); //$NON-NLS-1$ //$NON-NLS-2$
		//memento.putString(TAG_SHOWFILTERSTRINGS, SystemPreferencesManager.getPreferencesManager().getShowFilterStrings() ? "t" : "f");

		String inputMemento = memento.getString("factoryID"); // see IWorkbenchWindow ... this is only clue I can figure out!  //$NON-NLS-1$
		if (inputMemento != null)
		{
			saveInputState(memento);
			return;
		}

		Tree tree = systemView.getTree();

		// SAVE EXPAND-TO HASHTABLE
		Hashtable expandToFilters = systemView.getExpandToFilterTable();
		if ((expandToFilters != null) && (expandToFilters.size() > 0))
		{
			IMemento expandedMem = memento.createChild(TAG_EXPANDED_TO);
			Enumeration keys = expandToFilters.keys();
			while (keys.hasMoreElements())
			{
				Object key = keys.nextElement();
				Object value = expandToFilters.get(key);
				if (value != null)
				{
					IMemento elementMem = expandedMem.createChild(TAG_ELEMENT);
					elementMem.putString(TAG_PATH, (String) key);
					elementMem.putString(TAG_FILTER, (String) value);
				}
			}
		}

		// SAVE EXPANSION STATE
		//Object expandedElements[]= systemView.getExpandedElements();
		Object expandedElements[] = systemView.getVisibleExpandedElements();
		if ((expandedElements != null) && (expandedElements.length > 0))
		{
			IMemento expandedMem = memento.createChild(TAG_EXPANDED);
			for (int i = 0; i < expandedElements.length; i++)
			{
				Object o = expandedElements[i];
				ISystemViewElementAdapter adapter = systemView.getViewAdapter(o);
				//ISystemRemoteElementAdapter radapter = systemView.getRemoteAdapter(o);
				//if (adapter.saveExpansionState(o) && (radapter==null))
				if (adapter.saveExpansionState(o))
				{
					IMemento elementMem = expandedMem.createChild(TAG_ELEMENT);
					elementMem.putString(TAG_PATH, getMementoHandle(o, adapter));
					//System.out.println("Added to saved expansion list: " + getMementoHandle(o, adapter));
				}
			}
		}

		// SAVE SELECTION STATE
		Object elements[] = ((IStructuredSelection) systemView.getSelection()).toArray();
		if ((elements != null) && (elements.length > 0))
		{
			int MAX_SELECTION = 1;
			IMemento selectionMem = memento.createChild(TAG_SELECTION);
			for (int i = 0; i < elements.length && i < MAX_SELECTION; i++)
			{
				Object o = elements[i];
				ISystemViewElementAdapter adapter = systemView.getViewAdapter(o);
				//ISystemRemoteElementAdapter radapter = systemView.getRemoteAdapter(o);
				//if (adapter.saveExpansionState(o) && (radapter==null))
				if (adapter.saveExpansionState(o))
				{
					IMemento elementMem = selectionMem.createChild(TAG_ELEMENT);
					elementMem.putString(TAG_PATH, getMementoHandle(o, adapter));
				}
			}
		}

		//save vertical position
		ScrollBar bar = tree.getVerticalBar();
		int position = bar != null ? bar.getSelection() : 0;
		memento.putString(TAG_VERTICAL_POSITION, String.valueOf(position));
		//save horizontal position
		bar = tree.getHorizontalBar();
		position = bar != null ? bar.getSelection() : 0;
		memento.putString(TAG_HORIZONTAL_POSITION, String.valueOf(position));

	}

	/**
	 * Defer to the adapter to get the memento handle key plus the memento handle for
	 *  each part leading up to the current object.
	 */
	protected String getMementoHandle(Object o, ISystemViewElementAdapter adapter)
	{
		StringBuffer idBuffer = new StringBuffer(adapter.getMementoHandleKey(o));
		Object[] elementNodes = systemView.getElementNodes(o);
		if (elementNodes != null)
		{
			for (int idx = elementNodes.length - 1; idx >= 0; idx--)
			{
				o = elementNodes[idx];
				adapter = systemView.getViewAdapter(o);
				idBuffer.append(MEMENTO_DELIM + adapter.getMementoHandle(o));
			}
		}
		//System.out.println("MEMENTO HANDLE: " + idBuffer.toString());
		return idBuffer.toString();
	}

	/**
	 * Our own method for restoring state
	 */
	protected void restoreState(IMemento memento)
	{
		RestoreStateRunnable restoreAction = new RestoreStateRunnable(memento);
		restoreAction.setRule(RSECorePlugin.getTheSystemRegistry());
		restoreAction.schedule();

		/* DKM - Moved to RestoreStateRunnable
		 *     - resolves invalid shell problem at startup
		 * *
		//System.out.println("SYSTEMVIEWPART: restoreState");
		if (!SystemPreferencesManager.getPreferencesManager().getRememberState())
		  return;

		// restore the show filter pools and show filter strings settings as they were when this was saved
		boolean showFilterPools = false;
		boolean showFilterStrings = false;
		String savedValue = memento.getString(TAG_SHOWFILTERPOOLS);
		if (savedValue != null)
		  showFilterPools = savedValue.equals("t");
		else
		  showFilterPools = SystemPreferencesManager.getPreferencesManager().getShowFilterPools();

		savedValue = memento.getString(TAG_SHOWFILTERSTRINGS); // historical
		if (savedValue != null)
		  showFilterStrings = savedValue.equals("t");
		//else
		  //showFilterStrings = SystemPreferencesManager.getPreferencesManager().getShowFilterStrings();

		IMemento childMem = null;

		// restore expand-to hashtable state
			childMem= memento.getChild(TAG_EXPANDED_TO);
		if (childMem != null)
		{
			IMemento[] elementMem= childMem.getChildren(TAG_ELEMENT);
			Hashtable ht = new Hashtable();
			for (int i= 0; i < elementMem.length; i++)
			{
				String key   = elementMem[i].getString(TAG_PATH);
				String value = elementMem[i].getString(TAG_FILTER);
				if ((key != null) && (value != null))
				  ht.put(key, value);
			}
			if (ht.size() > 0)
			  systemView.setExpandToFilterTable(ht);
		}

		// restore expansion state
			childMem= memento.getChild(TAG_EXPANDED);
		if (childMem != null)
		{
		    ArrayList elements= new ArrayList();
		    Vector remoteElements = new Vector();
			IMemento[] elementMem= childMem.getChildren(TAG_ELEMENT);
			// walk through list of expanded nodes, breaking into 2 lists: non-remote and remote
			for (int i= 0; i < elementMem.length; i++)
			{
				Object element= getObjectFromMemento(showFilterPools, showFilterStrings, elementMem[i].getString(TAG_PATH));
				if (element != null)
				  if (element instanceof RemoteObject) // this is a remote object
				  {
				    remoteElements.add(element);
				    //System.out.println("Added to remote expansion list: " + element);
				  }
				  else
				  {
				    elements.add(element);
				    //System.out.println("Added to non-remote expansion list: " + element);
				  }
			}
			// expand non-remote...
			systemView.setExpandedElements(elements.toArray());
			// expand remote...
			if (remoteElements.size() > 0)
			{
				SystemResourceChangeEvent event = null;
				for (int idx=0; idx<remoteElements.size(); idx++)
				{
					RemoteObject ro = (RemoteObject)remoteElements.elementAt(idx);
					//event = new SystemResourceChangeEvent(ro.name,ISystemResourceChangeEvents.EVENT_REFRESH_REMOTE,
					//                                      SystemViewDummyObject.getInstance()); // This tells SystemView to expand this remote object, but don't select a child
					//systemView.systemResourceChanged(event);
		   	        systemView.refreshRemoteObject(ro.name, SystemViewDummyObject.getInstance(), true);
				}
			}
		}

		// restoreSelection
		childMem= memento.getChild(TAG_SELECTION);
		if (childMem != null)
		{
			ArrayList list= new ArrayList();
		    Vector remoteElements = new Vector();
			IMemento[] elementMem= childMem.getChildren(TAG_ELEMENT);
			for (int i= 0; i < elementMem.length; i++)
			{
				Object element= getObjectFromMemento(showFilterPools, showFilterStrings, elementMem[i].getString(TAG_PATH));
				if (element != null)
				  if (element instanceof RemoteObject) // this is a remote object
				    remoteElements.add(element);
				  else
				    list.add(element);
				//System.out.println("Added to selection list: " + element);
			}
			systemView.setSelection(new StructuredSelection(list));
			if (remoteElements.size() > 0)
			{
				Vector v = new Vector();
				for (int idx=0; idx<remoteElements.size(); idx++)
				{
					RemoteObject ro = (RemoteObject)remoteElements.elementAt(idx);
					v.addElement(ro.name);
				}
			    SystemResourceChangeEvent event = new SystemResourceChangeEvent(v,ISystemResourceChangeEvents.EVENT_SELECT_REMOTE,null);
			    systemView.systemResourceChanged(event);
			}
		}
		Tree tree= systemView.getTree();
		//restore vertical position
		ScrollBar bar= tree.getVerticalBar();
		if (bar != null)
		 {
			try
			{
				String posStr= memento.getString(TAG_VERTICAL_POSITION);
				if (posStr != null)
				{
				  int position;
				  position= new Integer(posStr).intValue();
				  bar.setSelection(position);
				}
			} catch (NumberFormatException e)
			{
			}
		}
		//restore vertical position
		bar= tree.getHorizontalBar();
		if (bar != null)
		{
			try
			{
				String posStr= memento.getString(TAG_HORIZONTAL_POSITION);
				if (posStr != null)
				{
			  	  int position;
				  position= new Integer(posStr).intValue();
				  bar.setSelection(position);
				}
			} catch (NumberFormatException e)
			{
			}
		}
		*/
	}

	/**
	 * protected method to deconstruct an expanded/selected memento into an actual object
	 */
	protected Object getObjectFromMemento(boolean showFilterPools, boolean showFilterStrings, String memento)
	{
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();

		ISystemProfile profile = null;
		IHost conn = null;
		String ssfId = null;
		ISubSystemConfiguration ssf = null;
		ISubSystem subsystem = null;
		ISystemFilterPoolReference fpRef = null;
		ISystemFilterReference fRef = null;
		ISystemFilterStringReference fsRef = null;
		RemoteObject remoteObject = null;

		//System.out.println("PARSING MEMENTO: " + memento);

		if (memento == null)
			return null;

		Vector v = tokenize(memento, MEMENTO_DELIM);

		String elementType = ""; //$NON-NLS-1$
		String ssName = null;
		//String connName = null;
		//String subsystemName = null;
		// 0. element type
		// 1. connections
		//    2. subsystems
		//       3. filter pools (optional)
		//          4. filters
		//             5. filter strings (optional)
		//                6. remote objects
		// 1. connections
		//    2. subsystems
		//          3. filters
		//             4. filter strings (optional)
		//                5. remote objects
		// 1. connections
		//    2. subsystems
		//       3. filter pools (optional)
		//          4. filters
		//                5. remote objects
		// 1. connections
		//    2. subsystems
		//          3. filters
		//                4. remote objects

		int index = 0;
		for (int idx = 0; idx < v.size(); idx++)
		{
			String token = (String) v.elementAt(idx);
			switch (idx)
			{
				// type of element
				case 0 :
					elementType = token;
					break;
					// connection
				case 1 :
					index = token.indexOf('.');
					if (index > 0)
					{
						profile = sr.getSystemProfile(token.substring(0, index));
						if (profile != null)
							conn = sr.getHost(profile, token.substring(index + 1));
					}
					break;
					// subsystem
				case 2 :
					index = token.indexOf('=');
					ssfId = index != -1 ? token.substring(0, index) : null;
					ssName = token.substring(index + 1);
					if (ssfId != null)
						ssf = sr.getSubSystemConfiguration(ssfId);
					if ((ssf != null) && (conn != null))
					{
						ISubSystem[] subsystems = ssf.getSubSystems(conn, true); // force to life
						if (subsystems != null)
							for (int ssidx = 0;(subsystem == null) && (ssidx < subsystems.length); ssidx++)
								if (subsystems[ssidx].getName().equals(ssName))
									subsystem = subsystems[ssidx];
					}
					break;
					// filter pool or filter (depends on showFilterPools)
				case 3 :

					if (subsystem != null && !(subsystem.getSubSystemConfiguration().supportsFilters())) {
						remoteObject = new RemoteObject(token, subsystem, null, null);
						break;
					}

					if (showFilterPools)
					{
						if (subsystem != null)
						{
							String poolName = token;
							ISystemFilterPoolReference[] refs = subsystem.getFilterPoolReferenceManager().getSystemFilterPoolReferences();
							if (refs != null)
								for (int refidx = 0;(fpRef == null) && (refidx < refs.length); refidx++)
								{
									if (refs[refidx].getFullName().equals(poolName))
										fpRef = refs[refidx];
								}
						}
					}
					else
					{
						index = token.indexOf('=');
						if (index != -1)
						{
							String filterName = token.substring(index + 1);
							String poolName = token.substring(0, index);
							if (subsystem != null)
							{
								ISystemFilterPoolReference[] refs = subsystem.getFilterPoolReferenceManager().getSystemFilterPoolReferences();
								if (refs != null)
									for (int refidx = 0;(fpRef == null) && (refidx < refs.length); refidx++)
										if (refs[refidx].getFullName().equals(poolName))
											fpRef = refs[refidx];
							}

							// TODO: handle nested filters. in this case they are separated by ';'. See SystemFilterReferenceAdapter's getMementoHandle()
							if (fpRef != null)
							{
								ISystemFilterReference[] refs = fpRef.getSystemFilterReferences(subsystem);
								if (refs != null)
									for (int refidx = 0;(fRef == null) && (refidx < refs.length); refidx++)
										if (refs[refidx].getName().equals(filterName))
											fRef = refs[refidx];
							}
						}
					}
					break;
					// filter or filter string (depends on showFilterPools) or remote object (depends on showFilterStrings)
				case 4 :

					if (subsystem != null && !(subsystem.getSubSystemConfiguration().supportsFilters())) {
						remoteObject = new RemoteObject(token, subsystem, null, null);
						break;
					}

					if (showFilterPools) // definitely a filter
					{
						index = token.indexOf('=');
						String filterName = token.substring(index + 1);
//						String poolName = token.substring(0, index);
						// TODO: handle nested filters. in this case they are separated by ';'. See SystemFilterReferenceAdapter's getMementoHandle()
						if (fpRef != null) // should have already been parsed in case 3
						{
							ISystemFilterReference[] refs = fpRef.getSystemFilterReferences(subsystem);
							if (refs != null)
								for (int refidx = 0;(fRef == null) && (refidx < refs.length); refidx++)
									if (refs[refidx].getName().equals(filterName))
										fRef = refs[refidx];
						}
					}
					else if (showFilterStrings) // children of filters are filter strings or resolved remote objects
					{
						// at this point we know the parent filter reference as that was parsed in case 3
						if (fRef != null)
						{
							ISystemFilterStringReference[] refs = fRef.getSystemFilterStringReferences();
							if (refs != null)
								for (int refidx = 0;(fsRef == null) && (refidx < refs.length); refidx++)
									if (refs[refidx].getString().equals(token))
										fsRef = refs[refidx];
						}
					}
					else // if both pools and strings are turned off, then at level four we are definitely dealing with remote objects
						{
						if ((subsystem != null) && (fRef != null))
							remoteObject = new RemoteObject(token, subsystem, fRef, fsRef);
					}

					break;
					// filter string (depends on showFilterStrings) or remote object
				case 5 :

					if (subsystem!=null && !(subsystem.getSubSystemConfiguration().supportsFilters())) {
						remoteObject = new RemoteObject(token, subsystem, null, null);
						break;
					}

					if (showFilterPools && showFilterStrings) // definitely a filter string
					{
						// at this point we know the parent filter reference as that was parsed in case 4
						if (fRef != null)
						{
							ISystemFilterStringReference[] refs = fRef.getSystemFilterStringReferences();
							if (refs != null)
								for (int refidx = 0;(fsRef == null) && (refidx < refs.length); refidx++)
									if (refs[refidx].getString().equals(token))
										fsRef = refs[refidx];
						}
					}
					else // definitely remote
						{
						if ((subsystem != null) && (fRef != null))
							remoteObject = new RemoteObject(token, subsystem, fRef, fsRef);
					}

					break;
				default : // definitely a remote object

					if (subsystem!=null && !(subsystem.getSubSystemConfiguration().supportsFilters())) {
						remoteObject = new RemoteObject(token, subsystem, null, null);
						break;
					}

					if ((subsystem != null) && (fRef != null))
						remoteObject = new RemoteObject(token, subsystem, fRef, fsRef);
			}
		}

		if (elementType.equals(ISystemMementoConstants.MEMENTO_KEY_CONNECTION))
			return conn;
		else if (elementType.equals(ISystemMementoConstants.MEMENTO_KEY_SUBSYSTEM))
			return subsystem;
		else if (elementType.equals(ISystemMementoConstants.MEMENTO_KEY_FILTERPOOLREFERENCE))
			return fpRef;
		else if (elementType.equals(ISystemMementoConstants.MEMENTO_KEY_FILTERREFERENCE))
			return fRef;
		else if (elementType.equals(ISystemMementoConstants.MEMENTO_KEY_FILTERSTRINGREFERENCE))
			return fsRef;
		else
			return remoteObject;

		//return null;
	}

	/**
	 * Parse a string into tokens. Unlike StringTokenizer, this supports delimiter strings versus
	 *  only delimiter characters
	 */
	public static Vector tokenize(String inputString, String delimiter)
	{
		Vector v = new Vector();
		StringBuffer token = new StringBuffer();
		String lastToken = null;
		int inpLen = inputString.length();
		int delimLen = delimiter.length();
		char delimChar1 = delimiter.charAt(0);
		for (int idx = 0; idx < inpLen; idx++)
		{
			int remLen = inpLen - idx;
			char currChar = inputString.charAt(idx);
			if ((currChar == delimChar1) && (remLen >= delimLen) && inputString.substring(idx, idx + delimLen).equals(delimiter))
			{
				lastToken = token.toString();
				v.addElement(lastToken);
				//System.out.println("...token: " + token);
				token.setLength(0);
				idx += delimLen - 1;
			}
			else
				token.append(currChar);
		}
		if (token.length() > 0)
		{
			lastToken = token.toString();
			v.addElement(lastToken);
			//System.out.println("...token: " + token);
		}
		return v;
	}

	protected class ShowRestoredRemoteObject implements Runnable
	{
		private Object _restoredObject;
		private Object[] _children;
		public ShowRestoredRemoteObject(Object restoredObject, Object[] children)
		{
			_restoredObject = restoredObject;
			_children = children;
		}

		public void run()
		{
			Vector matches = new Vector();
			systemView.findAllRemoteItemReferences(_restoredObject, _restoredObject, matches);
			if (matches.size() > 0){
				TreeItem item = (TreeItem)matches.get(0);
				systemView.createTreeItems(item, _children);
				item.setExpanded(true);
			}

		}
	}


	protected class RestoreRemoteObjects extends Job
	{
	  		class GetExpandToFilter implements Runnable
  	  		{
	  			private IWorkbenchPart _part;
  	  			private String _expandToFilter = null;
  	  			private Object _remoteObject = null;
  	  			
  	  			public GetExpandToFilter(Object remoteObject){
  	  				_remoteObject = remoteObject;
  	  			}
  	  			
  	  			public void run()
  	  			{
  	  				IWorkbenchPart activePart = _part;
  	  				if (activePart==null) {
  	  					IWorkbenchWindow win = SystemBasePlugin.getActiveWorkbenchWindow();
  		  				if (win != null){
	  	  					IWorkbenchPage page = win.getActivePage();
  	  						if (page != null){
  	  							activePart = page.getActivePart();
  	  							if (activePart != null){
  	  								_part = activePart;
  	  							}
  		  					}
	  	  				}
  	  				}

  	  				if (activePart instanceof SystemViewPart){
  	  					SystemView viewer = ((SystemViewPart)activePart).getSystemView();
  	  					if (_remoteObject instanceof IContextObject){
  	  						_expandToFilter = viewer.getExpandToFilter(((IContextObject)_remoteObject).getModelObject());
  	  					}
  	  					else {
  	  						_expandToFilter = viewer.getExpandToFilter(_remoteObject);
  	  					}
  	  				}
  	  			}

  	  			public String getExpandToFilter()
  	  			{
  	  				return _expandToFilter;
  	  			}
  	  		}

  	
  	  		
		private Vector _remoteObjectsToRestore;
		private Vector _remoteObjectsToSelect;

		public RestoreRemoteObjects(Vector remoteObjects, Vector remoteObjectsToSelect)
		{
			super("Restore Remote Objects"); //$NON-NLS-1$
			setSystem(true);
			_remoteObjectsToRestore = remoteObjects;
			_remoteObjectsToSelect = remoteObjectsToSelect;
		}

		protected IStatus run(IProgressMonitor monitor)
		{
			IStatus status = doRestore(monitor);
			if (status.isOK()){
				status = doSelect(monitor);
			}
			return status;
		}

		protected IStatus doSelect(IProgressMonitor monitor)
		{
			Vector v = new Vector();
			int MAX_SELECT = 1;
			for (int i = 0; i < _remoteObjectsToSelect.size() && i < MAX_SELECT; i++){

				Object object = _remoteObjectsToSelect.get(i);
				if (object instanceof RemoteObject)
				{
					RemoteObject robject = (RemoteObject)object;
					v.addElement(robject.name);
				}
			}
			SystemResourceChangeEvent event = new SystemResourceChangeEvent(v, ISystemResourceChangeEvents.EVENT_SELECT_REMOTE, null);
			systemView.systemResourceChanged(event);

			return Status.OK_STATUS;
		}


		protected IStatus doRestore(IProgressMonitor monitor)
		{
			boolean restoreFromCache = RSEUIPlugin.getDefault().getPreferenceStore().getBoolean(ISystemPreferencesConstants.RESTORE_STATE_FROM_CACHE);

			for (int i = 0; i < _remoteObjectsToRestore.size(); i++){

				if (monitor.isCanceled()){
					return Status.CANCEL_STATUS;
				}

				Object object = _remoteObjectsToRestore.get(i);
				if (object instanceof RemoteObject)
				{
					RemoteObject robject = (RemoteObject)object;

					ISubSystem ss = robject.subsystem;

					// yantzi: artemis 6.0:  notify subsystems that this is a restore from memento so they
					// can optionally use the cache if desired
					if (ss != null && restoreFromCache && ss.supportsCaching()){
						ss.getCacheManager().setRestoreFromMemento(true);
					}

					if (!ss.isOffline()){
						String path = robject.name;
						ISystemFilterReference fref = robject.fRef;

						try
						{
							Object actualObject = ss.getObjectWithAbsoluteName(path, monitor);

							if (actualObject instanceof IAdaptable)
							{
								// get the adapter
								ISystemViewElementAdapter adapter = (ISystemViewElementAdapter)((IAdaptable)actualObject).getAdapter(ISystemViewElementAdapter.class);

								// get the context
								ContextObject contextObject = new ContextObject(actualObject, ss, fref);

								Object[] children = null;
								
								Display dis = Display.getDefault();
					  	  		GetExpandToFilter getExpandTo = new GetExpandToFilter(contextObject);
					  	  		dis.syncExec(getExpandTo);
					  	  		String expandToFilter = getExpandTo.getExpandToFilter();
					    	  	if (expandToFilter != null){
					    	  		children = adapter.getChildrenUsingExpandToFilter(actualObject, expandToFilter);
					    	  	}
					    	  	else {	  
					    	  		// get the children
					    	  		children = adapter.getChildren(contextObject, monitor);
					    	  	}
					    	  	
					    	  	ShowRestoredRemoteObject showRunnable = new ShowRestoredRemoteObject(actualObject, children);
					    	  	Display.getDefault().asyncExec(showRunnable);					    	  	
							}

						}
						catch (Exception e){
							// unexpected
						}
					}

					// yantzi: artemis 6.0:  reset restore from memento flag
					if (ss != null && restoreFromCache && ss.supportsCaching()){
						ss.getCacheManager().setRestoreFromMemento(false);
					}
				}
				else if (object instanceof ISystemFilterReference)
				{

					ISystemFilterReference fref = (ISystemFilterReference)object;
					ISubSystem ss = fref.getSubSystem();

					// yantzi: artemis 6.0:  notify subsystems that this is a restore from memento so they
					// can optionally use the cache if desired
					if (ss != null && restoreFromCache && ss.supportsCaching()){
						ss.getCacheManager().setRestoreFromMemento(true);
					}
					boolean isRestoringCache = ss.getCacheManager() != null && ss.getCacheManager().isRestoreFromMemento();

					if (!ss.isOffline()){
						if (!ss.isConnected() && !isRestoringCache){
							try
							{
								ss.connect(monitor, false);
							}
							catch (Exception e){
								return Status.CANCEL_STATUS;
							}
						}
						if (ss.isConnected() || isRestoringCache)
						{
							// get the adapter
							ISystemViewElementAdapter adapter = (ISystemViewElementAdapter)((IAdaptable)object).getAdapter(ISystemViewElementAdapter.class);

							// get the context
							ContextObject contextObject = new ContextObject(fref, ss, fref);

							// get the children
							Object[] children = adapter.getChildren(contextObject, monitor);

							ShowRestoredRemoteObject showRunnable = new ShowRestoredRemoteObject(fref, children);
							Display.getDefault().asyncExec(showRunnable);
						}
					}

					// yantzi: artemis 6.0:  reset restore from memento flag
					if (ss != null && restoreFromCache && ss.supportsCaching()){
						ss.getCacheManager().setRestoreFromMemento(false);
					}
				}
			}


			return Status.OK_STATUS;
		}
	}

	protected class RemoteObject
	{
		public String name;
		public ISubSystem subsystem;
		public ISystemFilterReference fRef;
		public ISystemFilterStringReference fsRef;

		public RemoteObject(String name, ISubSystem ss, ISystemFilterReference fRef, ISystemFilterStringReference fsRef)
		{
			this.name = name;
			this.subsystem = ss;
			this.fRef = fRef;
			this.fsRef = fsRef;
		}

		public String toString()
		{
			return "Remote object: " + name; //$NON-NLS-1$
		}

		public boolean equals(RemoteObject compared)
		{
			if (name.equals(compared.name) &&
				subsystem == compared.subsystem &&
				fRef == compared.fRef)
				return true;

			return false;
		}
	}

	protected IPartListener partListener = new IPartListener()
	{
		public void partActivated(IWorkbenchPart part)
		{
			if (part instanceof IEditorPart)
				editorActivated((IEditorPart) part);
		}
		public void partBroughtToTop(IWorkbenchPart part)
		{
		}
		public void partClosed(IWorkbenchPart part)
		{
		}
		public void partDeactivated(IWorkbenchPart part)
		{
		}
		public void partOpened(IWorkbenchPart part)
		{
		}
	};

	public class RestoreStateRunnable extends UIJob
	{
		protected IMemento _memento;
		public RestoreStateRunnable(IMemento memento)
		{
			super("Restore RSE Tree"); //$NON-NLS-1$
			_memento = memento;

		}

		public IStatus runInUIThread(IProgressMonitor monitor)
		{
			IMemento memento = _memento;
			//System.out.println("SYSTEMVIEWPART: restoreState");
			if (!SystemPreferencesManager.getRememberState())
				return Status.CANCEL_STATUS;

			// restore the show filter pools and show filter strings settings as they were when this was saved
			boolean showFilterPools = false;
			boolean showFilterStrings = false;


			String savedValue = memento.getString(TAG_SHOWFILTERPOOLS);
			if (savedValue != null)
				showFilterPools = savedValue.equals("t"); //$NON-NLS-1$
			else
				showFilterPools = SystemPreferencesManager.getShowFilterPools();
			savedValue = memento.getString(TAG_SHOWFILTERSTRINGS); // historical
			if (savedValue != null)
				showFilterStrings = savedValue.equals("t"); //$NON-NLS-1$
			//else
			//showFilterStrings = SystemPreferencesManager.getPreferencesManager().getShowFilterStrings();

			IMemento childMem = null;

			// restore expand-to hashtable state
			childMem = memento.getChild(TAG_EXPANDED_TO);
			if (childMem != null)
			{
				IMemento[] elementMem = childMem.getChildren(TAG_ELEMENT);
				Hashtable ht = new Hashtable();
				for (int i = 0; i < elementMem.length; i++)
				{
					String key = elementMem[i].getString(TAG_PATH);
					String value = elementMem[i].getString(TAG_FILTER);
					if ((key != null) && (value != null))
						ht.put(key, value);
				}
				if (ht.size() > 0)
					systemView.setExpandToFilterTable(ht);
			}
			// restore expansion state
			childMem = memento.getChild(TAG_EXPANDED);
			Vector remoteElementsToRestore = new Vector();
			List cacheSubSystemList = new ArrayList();
			if (childMem != null)
			{
				ArrayList elements = new ArrayList();

				IMemento[] elementMem = childMem.getChildren(TAG_ELEMENT);

				// yantzi: artemis6.0, keep track subsystems which have their memento flag set in order
				// to restore system view from cache (if the subsystem supports this)

				ISubSystem cacheSubSystem;
				boolean restoreFromCache = RSEUIPlugin.getDefault().getPreferenceStore().getBoolean(ISystemPreferencesConstants.RESTORE_STATE_FROM_CACHE);

				// walk through list of expanded nodes, breaking into 2 lists: non-remote and remote
				for (int i = 0; i < elementMem.length; i++)
				{
					Object element = getObjectFromMemento(showFilterPools, showFilterStrings, elementMem[i].getString(TAG_PATH));
					if (element != null)
						if (element instanceof RemoteObject) // this is a remote object
						{
							remoteElementsToRestore.add(element);
							//System.out.println("Added to remote expansion list: " + element);
						}
						else if (element instanceof ISystemFilterReference)
						{
							remoteElementsToRestore.add(element); // filters trigger asynchronous queries, so best to expand this with remote items

							if (restoreFromCache)
							{
								// yantzi: artemis 6.0, see comment above
								cacheSubSystem = ((ISystemFilterReference)element).getSubSystem();
								if (cacheSubSystem.supportsCaching() && cacheSubSystem.getCacheManager() != null)
								{
									cacheSubSystem.getCacheManager().setRestoreFromMemento(true);
									cacheSubSystemList.add(cacheSubSystem);
								}
							}
						}
						else
						{
							elements.add(element);
							//System.out.println("Added to non-remote expansion list: " + element);
						}
				}
				// expand non-remote...
				systemView.setExpandedElements(elements.toArray());
			}

			// restoreSelection
			childMem = memento.getChild(TAG_SELECTION);

			Vector remoteElementsToSelect = new Vector();
			if (childMem != null)
			{
				ArrayList list = new ArrayList();

				IMemento[] elementMem = childMem.getChildren(TAG_ELEMENT);
				for (int i = 0; i < elementMem.length; i++)
				{
					Object element = getObjectFromMemento(showFilterPools, showFilterStrings, elementMem[i].getString(TAG_PATH));
					if (element != null)
						if (element instanceof RemoteObject) // this is a remote object
							remoteElementsToSelect.add(element);
						else
							list.add(element);
					//System.out.println("Added to selection list: " + element);
				}
				if (list.size()>0) {
					systemView.setSelection(new StructuredSelection(list));
				}
			}

			if (remoteElementsToRestore.size() > 0)
			{
				RestoreRemoteObjects restoreRemoteJob = new RestoreRemoteObjects(remoteElementsToRestore, remoteElementsToSelect);
				restoreRemoteJob.schedule();
			}

			Tree tree = systemView.getTree();
			//restore vertical position
			ScrollBar bar = tree.getVerticalBar();
			if (bar != null)
			{
				try
				{
					String posStr = memento.getString(TAG_VERTICAL_POSITION);
					if (posStr != null)
					{
						int position;
						position = new Integer(posStr).intValue();
						bar.setSelection(position);
					}
				}
				catch (NumberFormatException e)
				{
				}
			}
			//restore vertical position
			bar = tree.getHorizontalBar();
			if (bar != null)
			{
				try
				{
					String posStr = memento.getString(TAG_HORIZONTAL_POSITION);
					if (posStr != null)
					{
						int position;
						position = new Integer(posStr).intValue();
						bar.setSelection(position);
					}
				}
				catch (NumberFormatException e)
				{
				}
			}


			String linkWithEditor = memento.getString(TAG_LINKWITHEDITOR);
			if (linkWithEditor != null)
			{
				if (linkWithEditor.equals("t")) //$NON-NLS-1$
					_isLinkingEnabled = true;
				else
					_isLinkingEnabled = false;
			}
			else
			{
				_isLinkingEnabled = false;
			}



			return Status.OK_STATUS;
		}

	}

	// THE FOLLOWING ARE TO ENABLE PERSISTENCE OF NON-PRIMARY REMOTE SYSTEM EXPLORER VIEWS OPENED UP BY THE
	// USER RIGHT CLICKING AND SELECTING "OPEN IN NEW PERSPECTIVE".

	// --------------------------
	// IElementFactory methods...
	// --------------------------
	/**
	 * Given the memento created by saveState, for the input to the perspective, recreate the input object
	 *  at startup time.
	 * See the extension point for "org.eclipse.ui.elementFactories" in plugin.xml
	 */
	public IAdaptable createElement(IMemento memento)
	{
		//System.out.println("INSIDE CREATEELEMENT IN SYSTEMVIEWPART. TAG_INPUT STRING = "+memento.getString(TAG_INPUT));
		IAdaptable element = null;
		Object object = null;

		// restore the show filter pools and show filter strings settings as they were when this was saved
		boolean showFilterPools = false;
		boolean showFilterStrings = false;
		String savedValue = memento.getString(TAG_SHOWFILTERPOOLS);
		if (savedValue != null)
			showFilterPools = savedValue.equals("t"); //$NON-NLS-1$
		else
			showFilterPools = SystemPreferencesManager.getShowFilterPools();

		savedValue = memento.getString(TAG_SHOWFILTERSTRINGS);
		if (savedValue != null)
			showFilterStrings = savedValue.equals("t"); //$NON-NLS-1$
		//else
		//  showFilterStrings = SystemPreferencesManager.getPreferencesManager().getShowFilterStrings();

		object = getObjectFromMemento(showFilterPools, showFilterStrings, memento.getString(TAG_INPUT));

		// For now we don't allow secondary perspectives to be opened on remote objects, so we are lucky!
		if (object instanceof RemoteObject)
		{
		}
		else if (object instanceof IAdaptable)
			element = (IAdaptable) object;
		//System.out.println("... RETURNING "+element);
		return element;
	}

	// ------------------------------
	// IPersistableElement methods...
	// ------------------------------
	/**
	 * Return the element factory ID as declared in the extension point for "org.eclipse.ui.elementFactories" in plugin.xml
	 */
	public String getFactoryId()
	{
		//System.out.println("INSIDE GETFACTORYID IN SYSTEMVIEWPART");
		return "org.eclipse.rse.systemview.elementfactory"; //$NON-NLS-1$
	}

	/*
	 * The workbench is closing, and we are being asked to save the state of the input object in one of the secondary perspectives
	 *
	public void saveState(IMemento memento)
	{

	}*/

	/**
	 * The workbench is closing, and we are being asked to save the state of the input object in one of the secondary perspectives.
	 * This method is called by our saveState when it detects this is a save for the input vs a page save.
	 */
	protected void saveInputState(IMemento memento)
	{
		//System.out.println("INSIDE SAVEINPUTSTATE IN SYSTEMVIEWPART");
		IAdaptable inputObj = getSite().getPage().getInput();
		ISystemViewElementAdapter adapter = systemView.getViewAdapter(inputObj);
		if ((adapter != null) && (adapter.saveExpansionState(inputObj)))
		{
			String handle = getInputMementoHandle(inputObj, adapter);
			if (handle != null)
			{
				//System.out.println("... saving memento string: "+handle);
				memento.putString(TAG_INPUT, handle);
			}
		}
	}
	protected String getInputMementoHandle(Object o, ISystemViewElementAdapter adapter)
	{
		StringBuffer idBuffer = new StringBuffer(adapter.getMementoHandleKey(o));
		idBuffer.append(MEMENTO_DELIM + adapter.getInputMementoHandle(o));
		return idBuffer.toString();
	}

	// --------------------------------
	// IAdapterFactory methods...
	// --------------------------------
	/**
	 * @see IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList()
	{
		return new Class[] { IPersistableElement.class };
	}

	/**
	 * Called by our plugin's startup method to register our adaptable object types
	 * with the platform. We prefer to do it here to isolate/encapsulate all factory
	 * logic in this one place.
	 */
	public void registerWithManager(IAdapterManager manager, Object input)
	{
		//System.out.println("INSIDE REGISTERWITHMANAGER IN SYSTEMVIEWPART");
		// these are all the object types we allow the user to select "Open in New Perspective" on...
		manager.registerAdapters(this, input.getClass());
	}
	/**
	 * Called by our plugin's startup method to register our adaptable object types
	 * with the platform. We prefer to do it here to isolate/encapsulate all factory
	 * logic in this one place.
	 */
	public void unregisterWithManager(IAdapterManager manager)
	{
		//System.out.println("INSIDE UNREGISTERWITHMANAGER IN SYSTEMVIEWPART");
		// these are all the object types we allow the user to select "Open in New Perspective" on...
		manager.unregisterAdapters(this);
	}

	/**
	 * @see IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType)
	{
		//System.out.println("INSIDE GETADAPTER IN SYSTEMVIEWPART. adaptableObject = "+adaptableObject+", adapterType = "+adapterType.getName());
		// we don't try to restore these secondary perspectives unless user has elected to do so...
		if (!SystemPreferencesManager.getRememberState())
			return null;
		else
			return this;
	}
}
