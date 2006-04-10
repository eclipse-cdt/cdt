/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.ui.view;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
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
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.SystemElapsedTimer;
import org.eclipse.rse.core.SystemPlugin;
import org.eclipse.rse.core.SystemPreferencesManager;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.filters.ISystemFilterPoolReference;
import org.eclipse.rse.filters.ISystemFilterReference;
import org.eclipse.rse.filters.ISystemFilterStringReference;
import org.eclipse.rse.model.IHost;
import org.eclipse.rse.model.ISystemPreferenceChangeEvent;
import org.eclipse.rse.model.ISystemPreferenceChangeEvents;
import org.eclipse.rse.model.ISystemPreferenceChangeListener;
import org.eclipse.rse.model.ISystemProfile;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.model.ISystemResourceChangeEvents;
import org.eclipse.rse.model.SystemResourceChangeEvent;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.ui.ISystemIconConstants;
import org.eclipse.rse.ui.ISystemPreferencesConstants;
import org.eclipse.rse.ui.SystemResources;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.actions.SystemCascadingPreferencesAction;
import org.eclipse.rse.ui.actions.SystemCollapseAllAction;
import org.eclipse.rse.ui.actions.SystemCopyToClipboardAction;
import org.eclipse.rse.ui.actions.SystemNewConnectionAction;
import org.eclipse.rse.ui.actions.SystemPasteFromClipboardAction;
import org.eclipse.rse.ui.actions.SystemPreferenceQualifyConnectionNamesAction;
import org.eclipse.rse.ui.actions.SystemPreferenceRestoreStateAction;
import org.eclipse.rse.ui.actions.SystemPreferenceShowFilterPoolsAction;
import org.eclipse.rse.ui.actions.SystemRefreshAction;
import org.eclipse.rse.ui.actions.SystemRefreshAllAction;
import org.eclipse.rse.ui.actions.SystemStartCommunicationsDaemonAction;
import org.eclipse.rse.ui.actions.SystemWorkWithProfilesAction;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.CellEditorActionHandler;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ISetSelectionTarget;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.views.framelist.FrameList;


/**
 * This is the desktop view wrapper of the System View viewer.
 * ViewPart is from com.ibm.itp.ui.support.parts
 */
public class SystemViewPart 
	extends ViewPart
	implements ISetSelectionTarget, ISystemShellProvider, ISystemMessageLine, IElementFactory, IPersistableElement, IAdapterFactory, ISystemPreferenceChangeListener, ISelectionChangedListener, IRSEViewPart
{

	public class ToggleLinkingAction extends Action
	{
		public ToggleLinkingAction(SystemViewPart viewPart, String label)
		{
			super(label);
			setChecked(isLinkingEnabled);
		}

		public void run()
		{
			toggleLinkingEnabled();
			setChecked(isLinkingEnabled);
		}
	}

	protected SystemView systemView;
	protected ISystemViewInputProvider input = null;
	protected String message, errorMessage;
	protected SystemMessage sysErrorMessage;
	protected IStatusLineManager statusLine = null;
	protected boolean inputIsRoot = true;
	protected boolean doTimings = false;
	protected boolean isLinkingEnabled = false;

	protected SystemElapsedTimer timer;
	protected FrameList frameList;
	protected SystemViewPartGotoActionGroup gotoActionGroup;
	protected ToggleLinkingAction toggleLinkingAction;

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

	// Persistance tags.
	static final String TAG_RELEASE = "release";
	static final String TAG_SELECTION = "selection";
	static final String TAG_EXPANDED_TO = "expandedTo";
	static final String TAG_EXPANDED = "expanded";
	static final String TAG_ELEMENT = "element";
	static final String TAG_PATH = "path";
	static final String TAG_FILTER = "filter";
	static final String TAG_INPUT = "svInput";
	static final String TAG_VERTICAL_POSITION = "verticalPosition";
	static final String TAG_HORIZONTAL_POSITION = "horizontalPosition";
	static final String TAG_SHOWFILTERPOOLS = "showFilterPools";
	static final String TAG_SHOWFILTERSTRINGS = "showFilterStrings";
	static final String TAG_LINKWITHEDITOR = "linkWithEditor";

	public static final String MEMENTO_DELIM = "///";

	// constants			
	public static final String ID = "org.eclipse.rse.ui.view.systemView"; // matches id in plugin.xml, view tag	

	/**
	 * SystemViewPart constructor.
	 */
	public SystemViewPart()
	{
		super();
		//SystemPlugin.logDebugMessage(this.getClass().getName(),"INSIDE CTOR FOR SYSTEMVIEWPART.");
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
		systemView.setSelection(selection, true);
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

	public void toggleLinkingEnabled()
	{
		isLinkingEnabled = !isLinkingEnabled;
		if (isLinkingEnabled)
		{
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
		  * @since 2.0
		  */
	protected void editorActivated(IEditorPart editor)
	{
		if (!isLinkingEnabled)
			return;

		IEditorInput input = editor.getEditorInput();
		if (input instanceof IFileEditorInput)
		{
			IFileEditorInput fileInput = (IFileEditorInput) input;
			IFile file = fileInput.getFile();
			/* FIXME - can't couple this view to files ui
			SystemIFileProperties properties = new SystemIFileProperties(file);
			Object rmtEditable = properties.getRemoteFileObject();
			Object remoteObj = null;
			if (rmtEditable != null && rmtEditable instanceof ISystemEditableRemoteObject)
			{
				ISystemEditableRemoteObject editable = (ISystemEditableRemoteObject) rmtEditable;
				remoteObj = editable.getRemoteObject();

			}
			else
			{
				String subsystemId = properties.getRemoteFileSubSystem();
				String path = properties.getRemoteFilePath();
				if (subsystemId != null && path != null)
				{
					ISubSystem subSystem = SystemPlugin.getTheSystemRegistry().getSubSystem(subsystemId);
					if (subSystem != null)
					{
						if (subSystem.isConnected())
						{
							try
							{
								remoteObj = subSystem.getObjectWithAbsoluteName(path);
							}
							catch (Exception e)
							{
								return;
							}
						}
					}
				}
			}
			

			if (remoteObj != null)
			{
				// DKM - causes editor to loose focus
				//systemView.refreshRemoteObject(path, remoteObj, true);

				SystemResourceChangeEvent event = new SystemResourceChangeEvent(remoteObj, ISystemResourceChangeEvents.EVENT_SELECT_REMOTE, null);
				systemView.systemResourceChanged(event);
			}
			*/
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
		SystemBasePlugin.logInfo("Inside updateTitle. inputObject class type: " + inputObj.getClass().getName());
		if (inputObj != null)
		{
			setTitleToolTip(getFrameToolTipText(input));
			String viewName = getConfigurationElement().getAttribute("name"); //$NON-NLS-1$
			if (inputObj instanceof IHost)
			{
				IHost conn = (IHost) inputObj;
				setPartName(viewName + " : " + conn.getAliasName());
			}
			else if (inputObj instanceof ISubSystem)
			{
				ISubSystem ss = (ISubSystem) inputObj;
				setPartName(viewName + " : " + ss.getName());
			}
			else if (inputObj instanceof ISystemFilterPoolReference)
			{
				ISystemFilterPoolReference sfpr = (ISystemFilterPoolReference) inputObj;
				setPartName(viewName + " : " + sfpr.getName());
			}
			else if (inputObj instanceof ISystemFilterReference)
			{
				ISystemFilterReference sfr = (ISystemFilterReference) inputObj;
				setPartName(viewName + " : " + sfr.getName());
			}
			else if (inputObj instanceof ISystemFilterStringReference)
			{
				ISystemFilterStringReference sfsr = (ISystemFilterStringReference) inputObj;
				setPartName(viewName + " : " + sfsr.getString());
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
		SystemPlugin.logDebugMessage(this.getClass().getName(),"INSIDE SETINPUTPROVIDER FOR SYSTEMVIEWPART.");
		this.input = input;
	}*/
	/**
	 * Creates the SWT controls for a part.  
	 * Called by Eclipse framework.
	 */
	public void createPartControl(Composite parent)
	{
		//SystemPlugin.logInfo("INSIDE CREATEPARTCONTROL FOR SYSTEMVIEWPART.");
		if (input == null)
			//input = SystemPlugin.getTheSystemRegistry();
			input = getInputProvider();
		systemView = new SystemView(getShell(), parent, input, this);
		frameList = createFrameList();

		gotoActionGroup = new SystemViewPartGotoActionGroup(this);
		IActionBars actionBars = getActionBars();
		if (actionBars != null)
		{
			actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(), systemView.getDeleteAction());
			//SystemCommonSelectAllAction selAllAction = new SystemCommonSelectAllAction(getShell(), systemView, systemView);
			actionBars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(), systemView.getSelectAllAction());
			// added by Phil in 3.0 ...
			//actionBars.setGlobalActionHandler(IWorkbenchActionConstants.PROPERTIES, systemView.getPropertyDialogAction(); hmm, different one for local vs remote objects
			actionBars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), systemView.getRefreshAction());

			statusLine = actionBars.getStatusLineManager();
		}

		// register global edit actions 		
		ISystemRegistry registry = SystemPlugin.getTheSystemRegistry();

		Clipboard clipboard = registry.getSystemClipboard();

		CellEditorActionHandler editorActionHandler = new CellEditorActionHandler(getViewSite().getActionBars());

		_copyAction = new SystemCopyToClipboardAction(systemView.getShell(), clipboard);
		_pasteAction = new SystemPasteFromClipboardAction(systemView.getShell(), clipboard);

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

		// ----------------------------------------------------------------------
		// Enable property sheet updates when tree items are selected.
		// Note for this to work each item in the tree must either implement
		//   IPropertySource, or support IPropertySource.class as an adapter type
		//   in its AdapterFactory.
		// ----------------------------------------------------------------------
		getSite().setSelectionProvider(systemView);
		// listen to editor events for linking
		getSite().getPage().addPartListener(partListener);

		SystemWidgetHelpers.setHelp(parent, SystemPlugin.HELPPREFIX + "sysv0000");

		// ----------------------
		// Restore previous state
		// ----------------------		
		if ((fMemento != null) && (input instanceof ISystemRegistry))
			restoreState(fMemento);
		//fMemento = null;

		// Register for preference change events
		registry.addSystemPreferenceChangeListener(this);

		// if this is the primary RSE view, and there are no user-defined
		// connections, auto-expand the New Connection prompt...
		if ((input == SystemPlugin.getTheSystemRegistry()) && (SystemPlugin.getTheSystemRegistry().getHosts().length == 1))
		{
			// assume this is the primary RSE view

			// WE GET ALL THE WAY HERE, BUT THESE LINES OF CODE ARE INEFFECTIVE FOR SOME REASON!!           
			TreeItem firstItem = systemView.getTree().getItems()[0];
			systemView.setSelection(new StructuredSelection(firstItem.getData()));
			systemView.setExpandedState(firstItem.getData(), true);
		}
	}

	/**
	 * Creates the frame source and frame list, and connects them.
	 * 
	 * @since 2.0
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
	 * @since 2.0
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
		SystemPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell().setFocus();
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
		if (isLinkingEnabled)
		{
			linkToEditor(sel);
		}
	}

	
	// link back to editor
	protected void linkToEditor(IStructuredSelection selection) 
	{
		Object obj = selection.getFirstElement();
		if (obj instanceof IAdaptable)
		{
			try
			{
				ISystemRemoteElementAdapter adapter = (ISystemRemoteElementAdapter)((IAdaptable)obj).getAdapter(ISystemRemoteElementAdapter.class);
				if (adapter != null)
				{
					
					if (adapter.canEdit(obj))
					{
						IWorkbenchPage page = getSite().getPage();
						IEditorReference[] editorRefs = page.getEditorReferences();
						for (int i = 0; i < editorRefs.length; i++)
						{
							IEditorReference editorRef = editorRefs[i];
						
							IEditorPart editor = editorRef.getEditor(false);
							if (editor != null)
							{
							IEditorInput input = editor.getEditorInput();
							if (input instanceof FileEditorInput)
							{
								IFile file = ((FileEditorInput)input).getFile();				
								/** FIXME - can't couple this view to files ui
								if (file.getProject().getName().equals(SystemRemoteEditManager.REMOTE_EDIT_PROJECT_NAME))
								{
									SystemIFileProperties properties = new SystemIFileProperties(file);
									String path = properties.getRemoteFilePath();
									if (path != null && path.equals(adapter.getAbsoluteName(obj)))
									{
										page.bringToTop(editor);
										return;
									}
								}
								*/
							}
							}											
						}
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}			
		}		
	}


	/**
	 * Fills the local tool bar with actions.
	 */
	protected void fillLocalToolBar(boolean showConnectionActions)
	{
		IActionBars actionBars = getViewSite().getActionBars();
		SystemRefreshAction refreshAction = new SystemRefreshAction(getShell());
		refreshAction.setSelectionProvider(systemView);
		actionBars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), refreshAction);

		IToolBarManager toolBarMgr = actionBars.getToolBarManager();
		if (showConnectionActions)
		{
			SystemNewConnectionAction newConnAction = new SystemNewConnectionAction(getShell(), false, systemView); // false implies not from popup menu
			toolBarMgr.add(newConnAction);
		}

		refreshAction.setSelectionProvider(systemView);
		toolBarMgr.add(refreshAction);

		toolBarMgr.add(new Separator("Navigate"));
		SystemViewPartGotoActionGroup gotoActions = new SystemViewPartGotoActionGroup(this);
		gotoActions.fillActionBars(actionBars);

		// defect 41203
		toolBarMgr.add(new Separator());
		
		// DKM - changing hover image to the elcl16 one since the navigator no long has clcl16 icons
		SystemCollapseAllAction collapseAllAction = new SystemCollapseAllAction(getShell());
		collapseAllAction.setSelectionProvider(systemView);
		// PSC ... better to encapsulate this in the SystemCollapseAllAction class
		//collapseAllAction.setImageDescriptor(getNavigatorImageDescriptor("elcl16/collapseall.gif")); //$NON-NLS-1$	
		//collapseAllAction.setHoverImageDescriptor(getNavigatorImageDescriptor("elcl16/collapseall.gif")); //$NON-NLS-1$
		
		toolBarMgr.add(collapseAllAction);

		toggleLinkingAction = new ToggleLinkingAction(this, org.eclipse.ui.internal.views.navigator.ResourceNavigatorMessages.ToggleLinkingAction_text); 
		
		toggleLinkingAction.setToolTipText(org.eclipse.ui.internal.views.navigator.ResourceNavigatorMessages.ToggleLinkingAction_toolTip); 
		toggleLinkingAction.setImageDescriptor(getNavigatorImageDescriptor(ISystemIconConstants.ICON_IDE_LINKTOEDITOR_ID)); 
		toggleLinkingAction.setHoverImageDescriptor(getNavigatorImageDescriptor(ISystemIconConstants.ICON_IDE_LINKTOEDITOR_ID)); 
		toolBarMgr.add(toggleLinkingAction);
		
	

		IMenuManager menuMgr = actionBars.getMenuManager();
		populateSystemViewPulldownMenu(menuMgr, getShell(), showConnectionActions, this, systemView);
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
			SystemWorkWithProfilesAction wwProfilesAction = new SystemWorkWithProfilesAction(shell);
			menuMgr.add(newConnectionAction);
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

		// Now query our remoteSystemsViewPreferencesActions for extenders who wish to appear in the
		//  preferences cascading menu...
		SystemCascadingPreferencesAction preferencesAction = new SystemCascadingPreferencesAction(shell);
		menuMgr.add(preferencesAction.getSubMenu());

		menuMgr.add(new Separator());
		menuMgr.add(new SystemStartCommunicationsDaemonAction(shell));

		if (viewPart != null)
		{
			/*
			SystemCascadingTeamAction teamAction = new SystemCascadingTeamAction(shell, viewPart);		
			menuMgr.add(new Separator());
			menuMgr.add(teamAction.getSubMenu());
			*/
		}
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
		SystemPlugin.getTheSystemRegistry().removeSystemPreferenceChangeListener(this);
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
		ISystemViewInputProvider inputProvider = SystemPlugin.getTheSystemRegistry();
		if (inputObj != null)
		{
			platformManager = Platform.getAdapterManager();
			if (inputObj instanceof IHost)
			{
				IHost conn = (IHost) inputObj;
				inputProvider = new SystemViewAPIProviderForConnections(conn);
				setPartName(getTitle() + " : " + conn.getAliasName());
			}
			else if (inputObj instanceof ISubSystem)
			{
				ISubSystem ss = (ISubSystem) inputObj;
				inputProvider = new SystemViewAPIProviderForSubSystems(ss);
				setPartName(getTitle() + " : " + ss.getName());
			}
			else if (inputObj instanceof ISystemFilterPoolReference)
			{
				ISystemFilterPoolReference sfpr = (ISystemFilterPoolReference) inputObj;
				inputProvider = new SystemViewAPIProviderForFilterPools(sfpr);
				setPartName(getTitle() + " : " + sfpr.getName());
			}
			else if (inputObj instanceof ISystemFilterReference)
			{
				ISystemFilterReference sfr = (ISystemFilterReference) inputObj;
				inputProvider = new SystemViewAPIProviderForFilters(sfr);
				setPartName(getTitle() + " : " + sfr.getName());
			}
			else if (inputObj instanceof ISystemFilterStringReference)
			{
				ISystemFilterStringReference sfsr = (ISystemFilterStringReference) inputObj;
				inputProvider = new SystemViewAPIProviderForFilterStrings(sfsr);
				setPartName(getTitle() + " : " + sfsr.getString());
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
		//SystemPlugin.logDebugMessage(this.getClass().getName(),msg);
		//System.out.println("INSIDE getInputProvider. inputProvider = "+inputProvider);
		return inputProvider;
	}

	// --------------------------------------------
	// ISystemPreferenceChangeListener interface...
	// --------------------------------------------
	public void systemPreferenceChanged(ISystemPreferenceChangeEvent event)
	{
		if ((event.getType() == ISystemPreferenceChangeEvents.EVENT_QUALIFYCONNECTIONNAMES) && (qualifyConnectionNamesAction != null))
			qualifyConnectionNamesAction.setChecked(SystemPreferencesManager.getPreferencesManager().getQualifyConnectionNames());
		else if ((event.getType() == ISystemPreferenceChangeEvents.EVENT_SHOWFILTERPOOLS) && (showFilterPoolsAction != null))
			showFilterPoolsAction.setChecked(SystemPreferencesManager.getPreferencesManager().getShowFilterPools());
		else if ((event.getType() == ISystemPreferenceChangeEvents.EVENT_RESTORESTATE) && (restoreStateAction != null))
			restoreStateAction.setChecked(SystemPreferencesManager.getPreferencesManager().getRememberState());

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
		return SystemPlugin.getDefault().getImageDescriptorFromIDE(relativePath); // more reusable
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
		if (!SystemPreferencesManager.getPreferencesManager().getRememberState())
			return;
		if (systemView == null)
		{
			// part has not been created
			if (fMemento != null) //Keep the old state;
				memento.putMemento(fMemento);
			return;
		}

		if (isLinkingEnabled)
		{
			memento.putString(TAG_LINKWITHEDITOR, "t");
		}
		else
		{
			memento.putString(TAG_LINKWITHEDITOR, "f");
		}

		// We record the current release for future in case anything significant changes from release to release
		memento.putString(TAG_RELEASE, SystemResources.CURRENT_RELEASE_NAME);

		// We record the current preferences for show filter string and show filter pools.
		// We do this to ensure the states match on restore. If they don't we will be in trouble
		//  restoring expansion state and hence will abandon it.

		memento.putString(TAG_SHOWFILTERPOOLS, SystemPreferencesManager.getPreferencesManager().getShowFilterPools() ? "t" : "f");
		//memento.putString(TAG_SHOWFILTERSTRINGS, SystemPreferencesManager.getPreferencesManager().getShowFilterStrings() ? "t" : "f");       

		String inputMemento = memento.getString("factoryID"); // see IWorkbenchWindow ... this is only clue I can figure out!
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
				ISystemViewElementAdapter adapter = systemView.getAdapter(o);
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
			IMemento selectionMem = memento.createChild(TAG_SELECTION);
			for (int i = 0; i < elements.length; i++)
			{
				Object o = elements[i];
				ISystemViewElementAdapter adapter = systemView.getAdapter(o);
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
				adapter = systemView.getAdapter(o);
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
		restoreAction.setRule(SystemPlugin.getTheSystemRegistry());
		restoreAction.schedule();

		/* DKM - Moved to RestoreStateRunnable
		 *     - resolves invalid shell problem at startup
		 * *
		//System.out.println("SYSTEMVIEWPART: restoreState");
		if (!SystemPreferencesManager.getPreferencesManager().getRememberState())
		  return;
		  
		if (doTimings)
		  timer = new SystemElapsedTimer();
		
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
					if (doTimings)
					  timer.setStartTime();
					//event = new SystemResourceChangeEvent(ro.name,ISystemResourceChangeEvents.EVENT_REFRESH_REMOTE,
					//                                      SystemViewDummyObject.getSingleton()); // This tells SystemView to expand this remote object, but don't select a child
					//systemView.systemResourceChanged(event);
		   	        systemView.refreshRemoteObject(ro.name, SystemViewDummyObject.getSingleton(), true);
					if (doTimings)
					{
						timer.setEndTime();
						System.out.println("Time to restore "+ro.name+": " + timer);
					}
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
				if (doTimings)
				  timer.setStartTime();
			    SystemResourceChangeEvent event = new SystemResourceChangeEvent(v,ISystemResourceChangeEvents.EVENT_SELECT_REMOTE,null);
			    systemView.systemResourceChanged(event);								
				if (doTimings)
				{
					timer.setEndTime();
					System.out.println("Time to select "+v.size()+" elements: " + timer);
				}
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
		ISystemRegistry sr = SystemPlugin.getTheSystemRegistry();

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

		String elementType = "";
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
					ssfId = token.substring(0, index);
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
					if (showFilterPools) // definitely a filter
					{
						index = token.indexOf('=');
						String filterName = token.substring(index + 1);
						String poolName = token.substring(0, index);
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
			return "Remote object: " + name;
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
			super("Restore RSE Tree");
			_memento = memento;
			
		}

		public IStatus runInUIThread(IProgressMonitor monitor)		
		{
			IMemento memento = _memento;
			//System.out.println("SYSTEMVIEWPART: restoreState");
			if (!SystemPreferencesManager.getPreferencesManager().getRememberState())
				return Status.CANCEL_STATUS;

			if (doTimings)
				timer = new SystemElapsedTimer();

			// restore the show filter pools and show filter strings settings as they were when this was saved
			boolean showFilterPools = false;
			boolean showFilterStrings = false;
			String linkWithEditor = memento.getString(TAG_LINKWITHEDITOR);
			if (linkWithEditor != null)
			{
				if (linkWithEditor.equals("t"))
				{
					isLinkingEnabled = true;
					toggleLinkingAction.setChecked(true);
				}
				else
					isLinkingEnabled = false;
			}
			else
			{
				isLinkingEnabled = false;
			}

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
			if (childMem != null)
			{
				ArrayList elements = new ArrayList();
				Vector remoteElements = new Vector();
				IMemento[] elementMem = childMem.getChildren(TAG_ELEMENT);

				// yantzi: artemis6.0, keep track subsystems which have their memento flag set in order
				// to restore system view from cache (if the subsystem supports this)
				List cacheSubSystemList = new ArrayList();
				ISubSystem cacheSubSystem;
				boolean restoreFromCache = SystemPlugin.getDefault().getPreferenceStore().getBoolean(ISystemPreferencesConstants.RESTORE_STATE_FROM_CACHE);

				// walk through list of expanded nodes, breaking into 2 lists: non-remote and remote
				for (int i = 0; i < elementMem.length; i++)
				{
					Object element = getObjectFromMemento(showFilterPools, showFilterStrings, elementMem[i].getString(TAG_PATH));
					if (element != null)
						if (element instanceof RemoteObject) // this is a remote object
						{
							remoteElements.add(element);
							//System.out.println("Added to remote expansion list: " + element);
						}
						else if (element instanceof ISystemFilterReference)
						{
							elements.add(element);

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
				// expand remote...
				if (remoteElements.size() > 0)
				{
					for (int idx = 0; idx < remoteElements.size(); idx++)
					{
						RemoteObject ro = (RemoteObject) remoteElements.elementAt(idx);
						if (doTimings)
							timer.setStartTime();
						//event = new SystemResourceChangeEvent(ro.name,ISystemResourceChangeEvents.EVENT_REFRESH_REMOTE,
						//                                      SystemViewDummyObject.getSingleton()); // This tells SystemView to expand this remote object, but don't select a child
						//systemView.systemResourceChanged(event);
						
						// yantzi: artemis 6.0:  notify subsystems that this is a restore from memento so they 
						// can optionally use the cache if desired
						if (ro.subsystem != null && ro.subsystem.supportsCaching())
						{
							ro.subsystem.getCacheManager().setRestoreFromMemento(true);
						}
						
						systemView.refreshRemoteObject(ro.name, SystemViewDummyObject.getSingleton(), true);
						
						// yantzi: artemis 6.0:  reset restore from memento flag
						if (ro.subsystem != null && ro.subsystem.supportsCaching())
						{
							ro.subsystem.getCacheManager().setRestoreFromMemento(false);
						}						
						
						if (doTimings)
						{
							timer.setEndTime();
							System.out.println("Time to restore " + ro.name + ": " + timer);
						}
					}
				}
				
				// yantzi: artemis 6.0, restore memento flag for affected subsystems
				if (restoreFromCache)
				{
					for (int i = 0; i < cacheSubSystemList.size(); i++)
					{
						((ISubSystem) cacheSubSystemList.get(i)).getCacheManager().setRestoreFromMemento(false);
					}
				}
			}

			// restoreSelection
			childMem = memento.getChild(TAG_SELECTION);
			if (childMem != null)
			{
				ArrayList list = new ArrayList();
				Vector remoteElements = new Vector();
				IMemento[] elementMem = childMem.getChildren(TAG_ELEMENT);
				for (int i = 0; i < elementMem.length; i++)
				{
					Object element = getObjectFromMemento(showFilterPools, showFilterStrings, elementMem[i].getString(TAG_PATH));
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
					for (int idx = 0; idx < remoteElements.size(); idx++)
					{
						RemoteObject ro = (RemoteObject) remoteElements.elementAt(idx);
						v.addElement(ro.name);
					}
					if (doTimings)
						timer.setStartTime();
					SystemResourceChangeEvent event = new SystemResourceChangeEvent(v, ISystemResourceChangeEvents.EVENT_SELECT_REMOTE, null);
					systemView.systemResourceChanged(event);
					if (doTimings)
					{
						timer.setEndTime();
						System.out.println("Time to select " + v.size() + " elements: " + timer);
					}
				}
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
			showFilterPools = savedValue.equals("t");
		else
			showFilterPools = SystemPreferencesManager.getPreferencesManager().getShowFilterPools();

		savedValue = memento.getString(TAG_SHOWFILTERSTRINGS);
		if (savedValue != null)
			showFilterStrings = savedValue.equals("t");
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
		return "com.ibm.etools.systems.systemview.elementfactory";
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
		ISystemViewElementAdapter adapter = systemView.getAdapter(inputObj);
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
		if (!SystemPreferencesManager.getPreferencesManager().getRememberState())
			return null;
		else
			return this;
	}
}