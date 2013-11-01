/********************************************************************************
 * Copyright (c) 2002, 2013 IBM Corporation and others. All rights reserved.
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
 * Kevin Doyle (IBM) - [180875] - Removed part of double click listener that opens files
 * Michael Berger (IBM) - Patch to remove non-standard expand/collapse from menu.
 * Tobias Schwarz (Wind River) - Fix 166343 getChildCount() counts invalid items
 * Martin Oberhuber (Wind River) - Improve fix for 166343 getChildCount()
 * Uwe Stieber (Wind River) - [172492] Use SafeTreeViewer
 *                          - [177537] [api] Dynamic system type provider need a hook to add dynamic system type specific menu groups
 *                          - Several bugfixes.
 * David Dykstal (IBM) - moved SystemPreferencesManager to a new package
 * Martin Oberhuber (Wind River) - [168975] Move RSE Events API to Core
 * Martin Oberhuber (Wind River) - [182454] improve getAbsoluteName() documentation
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [186773] split ISystemRegistryUI from ISystemRegistry
 * Martin Oberhuber (Wind River) - [186779] Fix IRSESystemType.getAdapter()
 * Martin Oberhuber (Wind River) - [186964] Fix adapter actions for multiselect, and and NPE
 * Martin Oberhuber (Wind River) - [186991] Avoid remote refresh if no element is remote
 * Martin Oberhuber (Wind River) - [190271] Move ISystemViewInputProvider to Core
 * Kevin Doyle (IBM) - [194602] handleDoubleClick does expand/collapse on treepath instead of element
 * David McKnight   (IBM)        - [194897] Should not remote refresh objects above subsystem.
 * Kevin Doyle - [193380] Deleting connection Refresh's Entire Remote Systems view
 * Kevin Doyle - [195537] Move ElementComparer to Separate File
 * Martin Oberhuber (Wind River) - [196936] Hide disabled system types
 * David McKnight   (IBM)        - [187205] Prevented expansion of non-expanded on remote refresh
 * David McKnight   (IBM)        - [196930] Don't add the connection when it's not supposed to be shown
 * Tobias Schwarz (Wind River)   - [197484] Provide ContextObject for queries on all levels
 * David McKnight   (IBM)        - [196662] Avoid main thread query to check exists when remote refreshing
 * Kevin Doyle  (IBM)            - [198576] Renaming a folder directly under a Filter doesn't update children
 * Kevin Doyle (IBM) 			 - [196582] Deprecated getRemoteObjectIdentifier
 * Martin Oberhuber (Wind River) - [198650] Fix assertion when restoring workbench state
 * Martin Oberhuber (Wind River) - [183176] Fix "widget is disposed" during Platform shutdown
 * David McKnight (IBM)          - [204684] CheckExistsJob used for determining if a remote object exists after a query of it's children
 * David McKnight (IBM)          - [205592] CheckExistsJob should use the context model object to get adapter
 * David McKnight   (IBM)        - [205819] Need to use input stream copy when EFS files are the src
 * Xuan Chen      (IBM)          - [160775] [api] rename (at least within a zip) blocks UI thread
 * David McKnight   (IBM)        - [199424] api to create tree items after query complete
 * David McKnight   (IBM)        - [187711] expandTo to handle filters specially
 * Martin Oberhuber (Wind River) - [218524][api] Remove deprecated ISystemViewInputProvider#getShell()
 * David Dykstal (IBM) - [222376] NPE if starting on a workspace with an old mark and a renamed default profile
 * David McKnight   (IBM)        - [224380] system view should only fire property sheet update event when in focus
 * David McKnight   (IBM)        - [224313] [api] Create RSE Events for MOVE and COPY holding both source and destination fields
 * David Dykstal (IBM) - [225911] Exception received after deleting a profile containing a connection
 * David Dykstal (IBM) - [216858] Need the ability to Import/Export RSE connections for sharing
 * David McKnight  (IBM)         - [231903] TVT34:TCT198: PLK: problems with "Show prompt" checkbox and "New connection prompt"
 * David McKnight  (IBM)         - [233530] Not Prompted on Promptable Filters after using once by double click
 * David McKnight  (IBM)         - [233570] ClassCastException when moving filter after "go into" action
 * David Dykstal (IBM)           - [233530] Backing out previous change for this bug
 * David McKnight   (IBM)        - [223461] [Refresh][api] Refresh expanded folder under filter refreshes Filter
 * David McKnight   (IBM)        - [236874] NPE upon selecting an item that is not associated with subsystem
 * David McKnight   (IBM)        - [238363] Performance improvement for refresh in system view.
 * David McKnight   (IBM)        - [241722] New -> File doesn't select the newly created file
 * David McKnight   (IBM)        - [187739] [refresh] Sub Directories are collapsed when Parent Directory is Refreshed on Remote Systems
 * David Dykstal (IBM) - [233530] Not Prompted on Promptable Filters after using once by double click
 * David McKnight   (IBM)        - [241744] Refresh collapse low level nodes which is expended before.
 * David McKnight   (IBM)        - [249245] not showing inappropriate popup actions for: Refresh, Show In Table, Go Into, etc.
 * David McKnight   (IBM)        - [251625] Widget disposed exception when renaming/pasting a folder
 * David McKnight   (IBM)        - [257721] Doubleclick doing special handling and expanding
 * David McKnight   (IBM)        - [190805] [performance][dstore] Right-click > Disconnect on a dstore connection is slow and spawns many Jobs
 * David McKnight   (IBM)        - [190001] [refresh] Avoid unnecessary duplicate queries during drag&drop to filter
 * Martin Oberhuber (Wind River) - [276195] Avoid unnecessary selectionChanged when restoring connections
 * David McKnight   (IBM)        - [277328] Unhandled Event Loop Exception When Right-Clicking on "Pending..." Message
 * David McKnight   (IBM)        - [283793] [dstore] Expansion indicator(+) does not reset after no connect
 * Uwe Stieber      (Wind River) - [238519] [usability][api] Adapt RSE view(s) to follow decoration style of the Eclipse platform common navigator
 * David McKnight   (IBM)        - [330973] Drag/drop a local file generates an error message in the Remote system view
 * David McKnight   (IBM)        - [308783] Value in Properties view remains "Pending..."
 * David McKnight   (IBM)        - [241726] Move doesn't select the moved items
 * David McKnight   (IBM)        - [333196] New member filter dialogue keep popping up when creating a shared member filter.
 * David McKnight   (IBM)        - [341281] amendment to fix for bug 308983
 * David McKnight   (IBM)        - [342208] potential NPE in SystemView$ExpandRemoteObjects.execute()
 * David McKnight   (IBM)        - [342095] Properties in Properties view remain "Pending..." in some cases
 * David McKnight   (IBM)        - [372976] ClassCastException when SystemView assumes widget a TreeItem when it's a Tree
 * David Dykstal    (IBM)        - [257110] Prompting filter called twice on double click rather than just once
 * David McKnight   (IBM)        - [380613] Problem in SystemView with disposed TreeItem when Link With Editor toolbar icon is used
 * David McKnight   (IBM)        - [385774] select folder dialog doesn't update enablement properly after new folder created
 * David McKnight   (IBM)        - [388364] RDz property view flickers when a user disconnects from zOS system
 * David Mcknight   (IBM)        - [374681] Incorrect number of children on the properties page of a directory
 * David McKnight   (IBM)        - [404396] delete doesn't always properly unmap tree items in SystemView
 * David McKnight   (IBM)        - [411398] SystemView event handling for icon changes needs to handle multi-source
 * Yang Yang        (IBM)        - [420578] Refresh action on connection level after a file deletion results in a problem occurred pop-up
 * David McKnight   (IBM)        - [420837] EVENT_ICON_CHANGE should not be handled with a refresh()
 ********************************************************************************/
   
package org.eclipse.rse.internal.ui.view;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreePathContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.SameShellProvider;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemRemoteChangeEvent;
import org.eclipse.rse.core.events.ISystemRemoteChangeEvents;
import org.eclipse.rse.core.events.ISystemRemoteChangeListener;
import org.eclipse.rse.core.events.ISystemResourceChangeEvent;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.ISystemResourceChangeListener;
import org.eclipse.rse.core.events.SystemRemoteChangeEvent;
import org.eclipse.rse.core.events.SystemResourceChangeEvent;
import org.eclipse.rse.core.filters.ISystemFilter;
import org.eclipse.rse.core.filters.ISystemFilterContainer;
import org.eclipse.rse.core.filters.ISystemFilterContainerReference;
import org.eclipse.rse.core.filters.ISystemFilterPool;
import org.eclipse.rse.core.filters.ISystemFilterPoolReference;
import org.eclipse.rse.core.filters.ISystemFilterPoolReferenceManager;
import org.eclipse.rse.core.filters.ISystemFilterPoolReferenceManagerProvider;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.filters.ISystemFilterString;
import org.eclipse.rse.core.filters.ISystemFilterStringReference;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.IRSECallback;
import org.eclipse.rse.core.model.ISystemContainer;
import org.eclipse.rse.core.model.ISystemMessageObject;
import org.eclipse.rse.core.model.ISystemRegistry;
import org.eclipse.rse.core.model.ISystemViewInputProvider;
import org.eclipse.rse.core.model.SystemMessageObject;
import org.eclipse.rse.core.references.IRSEBaseReferencingObject;
import org.eclipse.rse.core.subsystems.IRemoteObjectIdentifier;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.internal.ui.SystemResources;
import org.eclipse.rse.internal.ui.actions.SystemCascadingGoToAction;
import org.eclipse.rse.internal.ui.actions.SystemCollapseAction;
import org.eclipse.rse.internal.ui.actions.SystemCommonDeleteAction;
import org.eclipse.rse.internal.ui.actions.SystemCommonRenameAction;
import org.eclipse.rse.internal.ui.actions.SystemCommonSelectAllAction;
import org.eclipse.rse.internal.ui.actions.SystemExpandAction;
import org.eclipse.rse.internal.ui.actions.SystemImportConnectionAction;
import org.eclipse.rse.internal.ui.actions.SystemOpenExplorerPerspectiveAction;
import org.eclipse.rse.internal.ui.actions.SystemShowInMonitorAction;
import org.eclipse.rse.internal.ui.actions.SystemShowInTableAction;
import org.eclipse.rse.internal.ui.actions.SystemSubMenuManager;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.ui.ISystemContextMenuConstants;
import org.eclipse.rse.ui.ISystemDeleteTarget;
import org.eclipse.rse.ui.ISystemMessages;
import org.eclipse.rse.ui.ISystemRenameTarget;
import org.eclipse.rse.ui.RSESystemTypeAdapter;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.SystemBasePlugin;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.SystemPreferencesManager;
import org.eclipse.rse.ui.SystemWidgetHelpers;
import org.eclipse.rse.ui.actions.ISystemAction;
import org.eclipse.rse.ui.actions.SystemNewConnectionAction;
import org.eclipse.rse.ui.actions.SystemRefreshAction;
import org.eclipse.rse.ui.dialogs.SystemPromptDialog;
import org.eclipse.rse.ui.messages.ISystemMessageLine;
import org.eclipse.rse.ui.messages.SystemMessageDialog;
import org.eclipse.rse.ui.model.ISystemPromptableObject;
import org.eclipse.rse.ui.model.ISystemShellProvider;
import org.eclipse.rse.ui.model.SystemRemoteElementResourceSet;
import org.eclipse.rse.ui.model.SystemResourceChangeEventUI;
import org.eclipse.rse.ui.view.AbstractSystemViewAdapter;
import org.eclipse.rse.ui.view.IContextObject;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.rse.ui.view.ISystemSelectAllTarget;
import org.eclipse.rse.ui.view.ISystemTree;
import org.eclipse.rse.ui.view.ISystemViewElementAdapter;
import org.eclipse.rse.ui.view.SystemAdapterHelpers;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.internal.progress.ProgressMessages;
import org.eclipse.ui.part.EditorInputTransfer;
import org.eclipse.ui.part.PluginTransfer;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.progress.PendingUpdateAdapter;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;
import org.eclipse.ui.views.framelist.GoIntoAction;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

/**
 * This subclass of the standard JFace tree viewer is used to show a tree
 * view of connections to remote systems, which can be manipulated and expanded
 * to access remote objects in the remote system.
 */
/*
 * At one time implemented the following as well: MenuListener, IDoubleClickListener, ArmListener, IWireEventTarget
 */
public class SystemView extends SafeTreeViewer
	implements ISystemTree, ISystemShellProvider,
		ISystemResourceChangeListener, ISystemRemoteChangeListener,
		IMenuListener, IPostSelectionProvider,
		ISystemDeleteTarget, ISystemRenameTarget, ISystemSelectAllTarget,
		ISelectionChangedListener,  ITreeViewerListener
{

	// for deferred queries
	class ExpandRemoteObjects implements IRSECallback {
		private List _toExpand;

		public ExpandRemoteObjects(List toExpand){
			_toExpand = toExpand;
		}

		public void done(IStatus status, Object result) {

			if (Display.getCurrent() != null){ // on main thread
				execute();
			}
			else {
				// need to run this code on main thread
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable()
				{
					public void run() {
						execute();
					}
				});
			}
		}

		private void execute()
		{
			// expand each previously expanded sub-node, recursively
			for (int idx = 0; idx < _toExpand.size(); idx++) {
				ExpandedItem itemToExpand = (ExpandedItem) _toExpand.get(idx);
				if (itemToExpand.isRemote()) {
					// find remote item based on its original name and unchanged root parent
					Item item = null;
					item = findFirstRemoteItemReference(itemToExpand.remoteName, itemToExpand.subsystem, itemToExpand.parentItem);

					// if found, re-expand it
					if (item != null && !item.isDisposed()) {
						IRSECallback callback = getCallbackForSubChildren(itemToExpand, _toExpand);
						if (callback != null){
							createChildren(item, callback);
							((TreeItem) item).setExpanded(true);
						}
					}
				} else if (itemToExpand.data!=null) {
					setExpandedState(itemToExpand.data, true);
				}
			}
		}

		private IRSECallback getCallbackForSubChildren(ExpandedItem remoteParent, List itemsToExpand){
			List subChildren = new ArrayList();


			String parentName = remoteParent.remoteName;
			Object parent = remoteParent.data;
			String absoluteParentName = remoteParent.remoteAdapter.getAbsoluteName(parent);

			for (int i = 0; i < itemsToExpand.size(); i++){
				ExpandedItem itemToExpand = (ExpandedItem) itemsToExpand.get(i);
				if (parentName.equals(itemToExpand.remoteName)){
					// same item
				}
				else if (itemToExpand.remoteName != null && itemToExpand.remoteName.startsWith(parentName)){
					// child item
					subChildren.add(itemToExpand);
				}
				else {
					// some objects might need explicit comparison					
					Object object = itemToExpand.data;
					String childParentName = null;
					ISystemRemoteElementAdapter elementAdapter = getRemoteAdapter(object);
					if (elementAdapter != null){
						childParentName = elementAdapter.getAbsoluteParentName(object);
					} 
					else {
						ISystemViewElementAdapter viewAdapter = getViewAdapter(object);
						childParentName = viewAdapter.getName(object);
					}
					if (absoluteParentName.equals(childParentName)){
						subChildren.add(itemToExpand);
					}
				}
			}

			if (subChildren.size() > 0){
				return new ExpandRemoteObjects(subChildren);
			}
			else {
				return null;
			}
		}
	}
	protected Shell shell; // shell hosting this viewer: TODO can be removed
	protected ISystemViewInputProvider inputProvider; // who is supplying our tree root elements?
	protected ISystemViewInputProvider previousInputProvider; // who is supplying our tree root elements?
	protected Object previousInput;
	protected IHost previousInputConnection;
	// protected actions initialized on demand:
	// should be accessed by getters only
	private SystemNewConnectionAction _newConnectionAction;
	private SystemImportConnectionAction _importConnectionAction;
	private SystemRefreshAction _refreshAction;
	private PropertyDialogAction _propertyDialogAction;
	private SystemCollapseAction _collapseAction; // defect 41203
	private SystemExpandAction _expandAction; // defect 41203
	private SystemOpenExplorerPerspectiveAction _openToPerspectiveAction;

	private SystemShowInTableAction _showInTableAction;
	private SystemShowInMonitorAction _showInMonitorAction;
	private GoIntoAction _goIntoAction;
	private SystemCascadingGoToAction _gotoActions;
	// global actions: to be accessed by getters only
	// Note the Edit menu actions are set in SystemViewPart. Here we use these
	//   actions from our own popup menu actions.
	private SystemCommonDeleteAction _deleteAction; // for global delete menu item
	private SystemCommonRenameAction _renameAction; // for common rename menu item
	private SystemCommonSelectAllAction _selectAllAction; // for common Ctrl+A select-all
	// special flags needed when building popup menu, set after examining selections
	protected boolean selectionShowPropertiesAction;
	protected boolean selectionShowRefreshAction;
	protected boolean selectionShowOpenViewActions;
	protected boolean selectionShowGenericShowInTableAction;
	protected boolean selectionShowDeleteAction;
	protected boolean selectionShowRenameAction;
	protected boolean selectionEnableDeleteAction;
	protected boolean selectionEnableRenameAction;
	protected boolean selectionIsRemoteObject;
	protected boolean selectionHasAncestorRelation;
	protected boolean selectionFlagsUpdated = false;
	// misc
	protected MenuManager menuMgr;
	protected boolean showActions = true;
	protected boolean hardCodedConnectionSelected = false;
	protected boolean mixedSelection = false;
	protected boolean specialMode = false;
	protected boolean menuListenerAdded = false;
	protected boolean fromSystemViewPart = false;
	protected boolean areAnyRemote = false;
	protected boolean enabledMode = true;
	protected Widget previousItem = null;
	protected int searchDepth = 0;
	//protected Vector      remoteItemsToSkip = null;
	protected Cursor busyCursor;
	protected TreeItem inputTreeItem = null;
	protected static final int SEARCH_INFINITE = 10; // that's far enough down to search!
	public boolean debug = false;
	public boolean debugRemote = false;
	public boolean debugProperties = debug && false;
	// for support of Expand To actions ... transient filters really.
	// we need to record these per tree node they are applied to.
	protected Hashtable expandToFiltersByObject; // most efficient way to find these is by binary object
	protected Hashtable expandToFiltersByTreePath; // however, we lose that after a refresh so we also record by tree path

	// message line
	protected ISystemMessageLine messageLine = null;
	// button pressed
	protected static final int LEFT_BUTTON = 1;
	protected int mouseButtonPressed = LEFT_BUTTON; //d40615
	protected boolean expandingTreeOnly = false; //d40615
	protected ViewerFilter[] initViewerFilters = null;

	protected List _setList;

	protected boolean _allowAdapterToHandleDoubleClick = true;
	
	private Object[] _lastPropertyValues = null; // to reduce duplicate property sheet updates

	/**
	 * Constructor
	 * @param shell The shell hosting this tree viewer widget
	 * @param parent The composite widget into which to place this widget
	 * @param inputProvider The input object which will supply the initial root objects in the tree.
	 *                      Can be null initially, but be sure to call #setInputProvider(ISystemViewInputProvider) later.
	 * @param msgLine Where to display messages and tooltip text
	 */
	public SystemView(Shell shell, Composite parent, ISystemViewInputProvider inputProvider, ISystemMessageLine msgLine) {
		super(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL); // DKM - putting style here to avoid SWT.BORDER (defect 168972)
		assert shell == parent.getShell();
		this.shell = shell;
		this.inputProvider = inputProvider;
		this.messageLine = msgLine;
		init();
	}

	/**
	 * Constructor to use when you want to specify styles for the tree widget
	 * @param shell The shell hosting this tree viewer widget
	 * @param parent The composite widget into which to place this widget
	 * @param style The style to give the tree widget
	 * @param inputProvider The input object which will supply the initial root objects in the tree.
	 *                      Can be null initially, but be sure to call #setInputProvider(ISystemViewInputProvider) later.
	 * @param msgLine Where to display messages and tooltip text
	 */
	public SystemView(Shell shell, Composite parent, int style, ISystemViewInputProvider inputProvider, ISystemMessageLine msgLine) {
		super(parent, style);
		assert shell == parent.getShell();
		this.shell = shell;
		this.inputProvider = inputProvider;
		this.messageLine = msgLine;
		init();
	}

	/**
	 * Constructor to use when you want to specify styles for the tree widget
	 * @param shell The shell hosting this tree viewer widget
	 * @param parent The composite widget into which to place this widget
	 * @param style The style to give the tree widget
	 * @param inputProvider The input object which will supply the initial root objects in the tree.
	 *                      Can be null initially, but be sure to call #setInputProvider(ISystemViewInputProvider) later.
	 * @param msgLine Where to display messages and tooltip text
	 * @param initViewerFilters the initial viewer filters to apply.
	 */
	public SystemView(Shell shell, Composite parent, int style, ISystemViewInputProvider inputProvider, ISystemMessageLine msgLine, ViewerFilter[] initViewerFilters) {
		super(parent, style);
		assert shell == parent.getShell();
		this.shell = shell;
		this.inputProvider = inputProvider;
		this.messageLine = msgLine;
		this.initViewerFilters = initViewerFilters;
		init();
	}

	/**
	 * Constructor to use when you create your own tree widget.
	 * @param shell The shell hosting this tree viewer widget
	 * @param tree The Tree widget you created.
	 * @param inputProvider The input object which will supply the initial root objects in the tree.
	 *                      Can be null initially, but be sure to call #setInputProvider(ISystemViewInputProvider) later.
	 * @param msgLine Where to display messages and tooltip text
	 */
	public SystemView(Shell shell, Tree tree, ISystemViewInputProvider inputProvider, ISystemMessageLine msgLine) {
		super(tree);
		assert shell == tree.getShell();
		this.shell = shell;
		this.inputProvider = inputProvider;
		this.messageLine = msgLine;
		init();
	}

	/**
	 * Set the input provider. Sometimes this is delayed, or can change.
	 * @param inputProvider the input provider for this view.
	 */
	public void setInputProvider(ISystemViewInputProvider inputProvider) {
		this.inputProvider = inputProvider;
		inputProvider.setViewer(this);
		setInput(inputProvider);
	}

	/**
	 * Get the SystemViewPart that encapsulates us.
	 * Will be null unless fromSystemViewPart is true.
	 */
	public SystemViewPart getSystemViewPart() {
		if (fromSystemViewPart)
			return ((SystemViewPart) messageLine);
		else
			return null;
	}

	/**
	 * Get the workbench window containing this view part. Will only be non-null for the explorer view part,
	 * not when used within, say, a dialog
	 */
	protected IWorkbenchWindow getWorkbenchWindow() {
		if (fromSystemViewPart)
			return getSystemViewPart().getSite().getWorkbenchWindow();
		else
			return null;
	}

	/**
	 * Get the workbench part containing this view. Will only be non-null for the explorer view part,
	 * not when used within, say, a dialog
	 */
	protected IWorkbenchPart getWorkbenchPart() {
		return getSystemViewPart();
	}

	/**
	 * Disable/Enable the viewer. We do this by blocking keystrokes without visually greying out
	 */
	public void setEnabled(boolean enabled) {
		enabledMode = enabled;
	}

	/**
	 * Sets the label and content provider for the system view.
	 * This can be called externally if a custom RSE label and content provider is desired
	 * @param lcProvider the provider
	 */
	public void setLabelAndContentProvider(SystemViewLabelAndContentProvider lcProvider) {
		ILabelDecorator decorator = null;
		if (PlatformUI.isWorkbenchRunning()) {
			IWorkbench wb = PlatformUI.getWorkbench();
			decorator = wb.getDecoratorManager().getLabelDecorator();
		}
		setLabelProvider(new SystemViewDecoratingStyledCellLabelProvider(lcProvider, decorator));
		setContentProvider(lcProvider);
	}

	protected void init() {
		_setList = new ArrayList();
		busyCursor = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);

		setUseHashlookup(true); // new for our 2nd release. Attempt to fix 38 minutes to refresh for 15K elements
		setComparer(new ElementComparer());



		// set content provider
		SystemViewLabelAndContentProvider lcProvider = new SystemViewLabelAndContentProvider();
		setLabelAndContentProvider(lcProvider);

		// set initial viewer filters
		if (initViewerFilters != null) {

			for (int i = 0; i < initViewerFilters.length; i++) {
				addFilter(initViewerFilters[i]);
			}
		}

		fromSystemViewPart = ((messageLine != null) && (messageLine instanceof SystemViewPart));

		// set the tree's input. Provides initial roots.
		if (inputProvider != null) {
			inputProvider.setViewer(this);
			setInput(inputProvider);
			if (fromSystemViewPart) {
				previousInputConnection = getInputConnection(getWorkbenchPart().getSite().getPage().getInput());
			}
		}
		//addDoubleClickListener(this);
		addSelectionChangedListener(this);
		addTreeListener(this);
		// ----------------------------------------
		// register with system registry for events
		// ----------------------------------------
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
		sr.addSystemResourceChangeListener(this);
		sr.addSystemRemoteChangeListener(this);
		// -----------------------------
		// Enable right-click popup menu
		// -----------------------------
		menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(this);
		Menu menu = menuMgr.createContextMenu(getTree());
		getTree().setMenu(menu);
		// -------------------------------------------
		// Enable specific keys: dbl-click, Delete, F5
		// -------------------------------------------
		addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				handleDoubleClick(event);
			}
		});
		getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				handleKeyPressed(e);
			}
		});
		getControl().addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				mouseButtonPressed = e.button; //d40615
				if (!enabledMode) {
					//e.doit = false;
					return;
				}
			}
		});

		initRefreshKey();

		// initialize drag and drop
		initDragAndDrop();
	}

	/**
	 * Create the KeyListener for doing the refresh on the viewer.
	 */
	protected void initRefreshKey() {
		/* DKM - no need for explicit key listener since we
		 * have global action
		 getControl().addKeyListener(new KeyAdapter()
		 {
		 public void keyReleased(KeyEvent event)
		 {
		 if (!enabledMode)
		 return;
		 if (event.keyCode == SWT.F5)
		 {
		 //if (debug)
		 //  System.out.println("F5 pressed");
		 refreshAll();
		 }
		 }
		 });
		 */
	}

	/**
	 * Handles double clicks in viewer.
	 * Expands/Collapses selected item if it can be expanded/collapsed
	 */
	protected void handleDoubleClick(DoubleClickEvent event) {
		if (!enabledMode) {
			//event.doit = false;
			return;
		}
		ITreeSelection s = (ITreeSelection) event.getSelection();
		Object element = s.getFirstElement();
		if (element == null) return;
		// Get the path for the element and use it for setting expanded state,
		// so the proper TreeItem is expanded/collapsed
		TreePath[] paths = s.getPathsFor(element);
		if (paths == null || paths.length == 0 || paths[0] == null) return;
		TreePath elementPath = paths[0];

		// bringing back handling at the adapter level here due to bug 257721
		ISystemViewElementAdapter adapter = getViewAdapter(element);
		boolean alreadyHandled = false;
		if (adapter != null && _allowAdapterToHandleDoubleClick)
			alreadyHandled = adapter.handleDoubleClick(element);

		if (!alreadyHandled && isExpandable(element)) {
			boolean expandedState = getExpandedState(elementPath);
			// TL: need postpone this ExpandState change to avoid an extra createfilter
			//setExpandedState(elementPath, !expandedState);
			// DWD:  fire collapse / expand event
			Event baseEvent = new Event();
			baseEvent.item = findItem(element);
			baseEvent.widget = baseEvent.item;
			TreeEvent treeEvent = new TreeEvent(baseEvent);
			if (expandedState) {
				handleTreeCollapse(treeEvent);
			} else {
				handleTreeExpand(treeEvent);
			}
			// TL: post-change ExpandState
			setExpandedState(elementPath, !expandedState);
			return;
		}
	}

	/**
	 * Handles key events in viewer.
	 */
	void handleKeyPressed(KeyEvent event) {
		if ((event.character == SWT.DEL) && (event.stateMask == 0) && (((IStructuredSelection) getSelection()).size() > 0)) {
			scanSelections("handleKeyPressed"); //$NON-NLS-1$
			/* DKM - 53694
			 if (showDelete() && canDelete())
			 {

			 SystemCommonDeleteAction dltAction = (SystemCommonDeleteAction)getDeleteAction();
			 dltAction.setShell(getShell());
			 dltAction.setSelection(getSelection());
			 dltAction.setViewer(this);
			 dltAction.run();

			 }
			 */
		} else if ((event.character == '-') && (event.stateMask == SWT.CTRL)) {
			collapseAll();
		} else if ((event.character == 1) && // for some reason Ctrl+A comes in as Ctrl plus the number 1!
				(event.stateMask == SWT.CTRL) && !fromSystemViewPart) {
			//System.out.println("Inside Ctrl+A processing");
			if (enableSelectAll(null)) doSelectAll(null);
		} else if ((event.character == '-') && (((IStructuredSelection) getSelection()).size() > 0)) {
			//System.out.println("Inside Ctrl+- processing");
			collapseSelected();
		} else if ((event.character == '+') && (((IStructuredSelection) getSelection()).size() > 0)) {
			//System.out.println("Inside Ctrl++ processing");
			expandSelected();
		}

	}

	/**
	 * Handles a collapse-selected request
	 */
	public void collapseSelected() {
		TreeItem[] selectedItems = ((Tree) getControl()).getSelection();
		if ((selectedItems != null) && (selectedItems.length > 0)) {
			for (int idx = 0; idx < selectedItems.length; idx++)
				selectedItems[idx].setExpanded(false);
		}
	}

	/**
	 * Handles an expand-selected request
	 */
	public void expandSelected() {
		TreeItem[] selectedItems = ((Tree) getControl()).getSelection();
		if ((selectedItems != null) && (selectedItems.length > 0)) {
			for (int idx = 0; idx < selectedItems.length; idx++) {
				if (!selectedItems[idx].getExpanded()) {
					createChildren(selectedItems[idx]);
				}
				selectedItems[idx].setExpanded(true);
			}
		}
	}

	/**
	 * Display a message/status on the message/status line
	 */
	public void displayMessage(String msg) {
		if (messageLine != null) messageLine.setMessage(msg);
	}

	/**
	 * Clear message/status shown on the message/status line
	 */
	public void clearMessage() {
		if (messageLine != null) messageLine.clearMessage();
	}

	/**
	 * Turn off right-click actions
	 */
	public void setShowActions(boolean show) {
		this.showActions = show;
	}

	/**
	 * Return the input provider
	 */
	public ISystemViewInputProvider getInputProvider() {
		inputProvider.setViewer(this); // just in case. Added by Phil in V5.0
		return inputProvider;
	}

	/**
	 * Return the popup menu for the tree
	 */
	public Menu getContextMenu() {
		return getTree().getMenu();
	}

	/**
	 * Return the popup menu for the tree
	 */
	public MenuManager getContextMenuManager() {
		return menuMgr;
	}

	/**
	 * Rather than pre-defining this common action we wait until it is first needed,
	 *  for performance reasons.
	 */
	public IAction getNewConnectionAction() {
		if (_newConnectionAction == null) _newConnectionAction = new SystemNewConnectionAction(getShell(), true, this); // true=>from popup menu
		return _newConnectionAction;
	}

	/**
	 * Rather than pre-defining this common action we wait until it is first needed,
	 * for performance reasons.
	 */
	private IAction getImportConnectionAction() {
		if (_importConnectionAction == null) {
			_importConnectionAction = new SystemImportConnectionAction(); // true=>from popup menu
			_importConnectionAction.setShell(getShell());
			_importConnectionAction.setText(SystemResources.RESID_IMPORT_CONNECTION_LABEL_LONG);
		}
		return _importConnectionAction;
	}

	/**
	 * Return the refresh action
	 */
	public IAction getRefreshAction() {
		if (_refreshAction == null) _refreshAction = new SystemRefreshAction(getShell());
		_refreshAction.setId(ActionFactory.REFRESH.getId());
		_refreshAction.setActionDefinitionId("org.eclipse.ui.file.refresh"); //$NON-NLS-1$
		return _refreshAction;
	}

	/**
	 * @return the collapse action. Lazily creates it.
	 */
	public IAction getCollapseAction() {
		if (_collapseAction == null) _collapseAction = new SystemCollapseAction(getShell());
		return _collapseAction;
	}

	/**
	 * @return the expand action. Lazily creates it.
	 */
	public IAction getExpandAction() {
		if (_expandAction == null) _expandAction = new SystemExpandAction(getShell());
		return _expandAction;
	}

	/**
	 * Rather than pre-defining this common action we wait until it is first needed,
	 *  for performance reasons.
	 */
	public PropertyDialogAction getPropertyDialogAction() {
		if (_propertyDialogAction == null) {
			_propertyDialogAction = new PropertyDialogAction(new SameShellProvider(getShell()), this);
			_propertyDialogAction.setId(ActionFactory.PROPERTIES.getId());
			_propertyDialogAction.setActionDefinitionId(IWorkbenchActionDefinitionIds.PROPERTIES);
		}


		_propertyDialogAction.selectionChanged(getSelection());

		return _propertyDialogAction;
	}


	/**
	 * Return the select All action
	 */
	public IAction getSelectAllAction() {
		if (_selectAllAction == null) _selectAllAction = new SystemCommonSelectAllAction(getShell(), this, this);
		return _selectAllAction;
	}

	/**
	 * Rather than pre-defined this common action we wait until it is first needed,
	 *  for performance reasons.
	 */
	public IAction getRenameAction() {
		if (_renameAction == null) _renameAction = new SystemCommonRenameAction(getShell(), this);
		_renameAction.setId(ActionFactory.RENAME.getId());
		_renameAction.setActionDefinitionId(IWorkbenchActionDefinitionIds.RENAME);
		return _renameAction;
	}

	/**
	 * Rather than pre-defined this common action we wait until it is first needed,
	 *  for performance reasons.
	 */
	public IAction getDeleteAction() {
		if (_deleteAction == null) _deleteAction = new SystemCommonDeleteAction(getShell(), this);
		_deleteAction.setId(ActionFactory.DELETE.getId());
		_deleteAction.setActionDefinitionId(IWorkbenchActionDefinitionIds.DELETE);
		return _deleteAction;
	}

	/*
	 * Get the common "Open to->" action for opening a new Remote System Explorer view,
	 *  scoped to the currently selected object.
	 *
	 protected SystemCascadingOpenToAction getOpenToAction()
	 {
	 if (openToAction == null)
	 openToAction = new SystemCascadingOpenToAction(getShell(),getWorkbenchWindow());
	 return openToAction;
	 } NOT USED YET */

	/**
	 * Get the common "Open to->" action for opening a new Remote System Explorer view,
	 *  scoped to the currently selected object.
	 */
	public SystemOpenExplorerPerspectiveAction getOpenToPerspectiveAction() {
		if (_openToPerspectiveAction == null) _openToPerspectiveAction = new SystemOpenExplorerPerspectiveAction(getShell(), getWorkbenchWindow());
		return _openToPerspectiveAction;
	}

	public SystemShowInTableAction getShowInTableAction() {
		if (_showInTableAction == null) _showInTableAction = new SystemShowInTableAction(getShell());
		return _showInTableAction;
	}

	public SystemShowInMonitorAction getShowInMonitorAction() {
		if (_showInMonitorAction == null) _showInMonitorAction = new SystemShowInMonitorAction(getShell());
		return _showInMonitorAction;
	}

	/**
	 * Get the common "Go Into" action for drilling down in the Remote System Explorer view,
	 *  scoped to the currently selected object.
	 */
	public GoIntoAction getGoIntoAction() {
		if (_goIntoAction == null) {
			_goIntoAction = new GoIntoAction(getSystemViewPart().getFrameList());
			_goIntoAction.setText(SystemResources.ACTION_CASCADING_GOINTO_LABEL);
			_goIntoAction.setToolTipText(SystemResources.ACTION_CASCADING_GOINTO_TOOLTIP);
		}
		return _goIntoAction;
	}

	/**
	 * Get the common "Go To->" cascading menu action for navigating the frame list.
	 */
	public SystemCascadingGoToAction getGoToActions() {
		if (_gotoActions == null) _gotoActions = new SystemCascadingGoToAction(getShell(), getSystemViewPart());
		return _gotoActions;
	}

	/**
	 * Helper method to collapse a node in the tree.
	 * Called when a currently expanded subsystem is disconnected.
	 * @param forceRefresh true if children should be deleted from memory so re-expand forces refresh.
	 */
	public void collapseNode(Object element, boolean forceRefresh) {
		// First, collapse this element and all its children.
		collapseToLevel(element, ALL_LEVELS);

		// Collapsed just changes expanded state but leaves existing child
		//  widgets in memory so they are re-shown on next expansion.
		// To force the next expand to re-get the children, we have to delete the
		//  children.
		if (forceRefresh) {
			refresh(element); // look at AbstractTreeViewer.updateChildren which this
			// will call. If the element is collapsed (which we just
			// did) then its children are simply disposed of, and
			// not re-queried. Just what we want!
		}
	}

	/**
	 * This is method is called to populate the popup menu
	 */
	public void fillContextMenu(IMenuManager menu) {

		if (!showActions) return;
		//SystemViewPlugin.getDefault().logMessage("inside fillContextMenu");
		IStructuredSelection selection = (IStructuredSelection) getSelection();
		boolean allSelectionsFromSameParent = true;
		int selectionCount = selection.size();

		if (selectionCount == 0) // nothing selected
		{
			menu.add(getNewConnectionAction());
			menu.add(getImportConnectionAction());
			menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_ADDITIONS)); // user or BP/ISV additions
		} else {
			if (selectionCount > 1) {
				allSelectionsFromSameParent = sameParent();
				if (!allSelectionsFromSameParent) {
					if (selectionHasAncestryRelationship()) {
						// don't show the menu because actions with
						//  multiple select on objects that are ancestors
						//  of each other is problematic
						// still create the standard groups
						createStandardGroups(menu);
						return;
					}
				}
			}
			// Partition into groups...
			createStandardGroups(menu);

			// PRESET INSTANCE VARIABLES ABOUT WHAT COMMON ACTIONS ARE TO BE SHOWN...
			// PERFORMANCE TWEAK: OUR GLOBAL DELETE ACTION LISTENS FOR SELECTION CHANGES, AND
			//  WHEN THAT CHANGES, WILL CALL CANDELETE() HERE. THAT IN TURN WILL CALL SCANSELECTIONS.
			//  THIS MEANS SCAN SELECTIONS GETS CALL TWICE ON MOST RIGHT CLICK ACTIONS.
			if (!selectionFlagsUpdated) // might already be called by the global delete action wh
				scanSelections("fillContextMenu"); //$NON-NLS-1$

			
			Object selectedObject = selection.getFirstElement();
			if (selectedObject instanceof PendingUpdateAdapter){
				return; // no menu for "Pending..."
			}
			
			// ADD COMMON ACTIONS...

			// COMMON REFRESH ACTION...
			if (showRefresh()) {
				menu.appendToGroup(ISystemContextMenuConstants.GROUP_BUILD, getRefreshAction());

				// MJB: Removed as per bugzilla entry # 145843
				//menu.appendToGroup(ISystemContextMenuConstants.GROUP_BUILD, getExpandAction());  // defect 41203
				//menu.appendToGroup(ISystemContextMenuConstants.GROUP_BUILD, getCollapseAction());  // defect 41203
			}

			// COMMON RENAME ACTION...
			if (showRename()) {
				menu.appendToGroup(ISystemContextMenuConstants.GROUP_REORGANIZE, getRenameAction());
				((ISystemAction) getRenameAction()).setInputs(getShell(), this, selection);
			}

		// COMMON DELETE ACTION...
			if (showDelete()) {
				//menu.add(getDeleteAction());
				menu.appendToGroup(ISystemContextMenuConstants.GROUP_REORGANIZE, getDeleteAction());
				((ISystemAction) getDeleteAction()).setInputs(getShell(), this, selection);
				menu.add(new Separator());
			}

			// PROPERTIES ACTION...
			// This is supplied by the system, so we pretty much get it for free. It finds the
			// registered propertyPages extension points registered for the selected object's class type.
			//_propertyDialogAction.selectionChanged(selection);
			if (showProperties()) {
				PropertyDialogAction pdAction = getPropertyDialogAction();
				if (pdAction.isApplicableForSelection()) menu.appendToGroup(ISystemContextMenuConstants.GROUP_PROPERTIES, pdAction);
			}

			// GO INTO ACTION...
			// OPEN IN NEW WINDOW ACTION...
			if (fromSystemViewPart) {

				
				ISystemViewElementAdapter adapter = getViewAdapter(selectedObject);

				boolean hasChildren = adapter.hasChildren((IAdaptable)selectedObject);
				if (!selectionIsRemoteObject)
				{
					GoIntoAction goIntoAction = getGoIntoAction();
					boolean singleSelection = selection.size() == 1;
					goIntoAction.setEnabled(singleSelection);
					if (singleSelection) {
						// dkm - first find out if the selection will have children
						//      only add this action if there are children
						if (hasChildren && showOpenViewActions())
						{
							menu.appendToGroup(ISystemContextMenuConstants.GROUP_GOTO, goIntoAction);
						}
					}


					if (showOpenViewActions()) {
						SystemOpenExplorerPerspectiveAction opa = getOpenToPerspectiveAction();
						opa.setSelection(selection);
						menu.appendToGroup(opa.getContextMenuGroup(), opa);
					}
				}

				if (showGenericShowInTableAction() && hasChildren) {
					SystemShowInTableAction showInTableAction = getShowInTableAction();
					showInTableAction.setSelection(selection);
					menu.appendToGroup(getOpenToPerspectiveAction().getContextMenuGroup(), showInTableAction);

					SystemShowInMonitorAction showInMonitorAction = getShowInMonitorAction();
					showInMonitorAction.setSelection(selection);
					menu.appendToGroup(getOpenToPerspectiveAction().getContextMenuGroup(), showInMonitorAction);
				}
			}

			// GO TO CASCADING ACTIONS...
			if (fromSystemViewPart && (selectionIsRemoteObject || showOpenViewActions())) {
				SystemCascadingGoToAction gotoActions = getGoToActions();
				gotoActions.setSelection(selection);
				menu.appendToGroup(gotoActions.getContextMenuGroup(), gotoActions.getSubMenu());
			}

			// ADAPTER SPECIFIC ACTIONS (Must be the last actions added to the menu!!!)
			SystemMenuManager ourMenu = new SystemMenuManager(menu);

			// yantzi:artemis 6.0 (defect 53970), do not show adapter specific actions when
			// there is not a common adapter for all selected elements (i.e. there are 2 or
			// more selected elements that have different adapters
			Iterator elements = selection.iterator();
			//Hashtable adapters = new Hashtable();
			ISystemViewElementAdapter adapter = null;
			boolean skipAdapterActions = false;

			// can we ask adapters to contribute menu items?
			// This can be done consistently only if all elements
			// in the (multi) selection adapt to the same adapter instance.
			// otherwise, adapters will not be allowed to contribute.
			while (elements.hasNext() && !skipAdapterActions) {
				Object element = elements.next();
				if (adapter == null) {
					adapter = getViewAdapter(element);
				} else if (adapter != getViewAdapter(element)) {
					// selected elements have different adapters
					skipAdapterActions = true;
				}
				//if (adapter != null)
				//	adapters.put(adapter,element); // want only unique adapters
			}

			//Enumeration uniqueAdapters = adapters.keys();
			if (adapter != null && !skipAdapterActions) {
				Shell shell = getShell();

				//while (uniqueAdapters.hasMoreElements())
				//{
				//	 ISystemViewElementAdapter nextAdapter = (ISystemViewElementAdapter)uniqueAdapters.nextElement();
				adapter.addActions(ourMenu, selection, shell, ISystemContextMenuConstants.GROUP_ADAPTERS);

			     if (adapter instanceof AbstractSystemViewAdapter)
			     {

						AbstractSystemViewAdapter aVA = (AbstractSystemViewAdapter)adapter;

						// add dynamic menu popups
						aVA.addDynamicPopupMenuActions(ourMenu, selection, shell,  ISystemContextMenuConstants.GROUP_ADDITIONS);

						// add remote actions
						aVA.addCommonRemoteActions(ourMenu, selection, shell, ISystemContextMenuConstants.GROUP_ADAPTERS);
			     }
				//}
			}


			// whale through all actions, updating shell and selection
			IContributionItem[] items = menu.getItems();
			for (int idx = 0; idx < items.length; idx++) {
				if ((items[idx] instanceof ActionContributionItem) && (((ActionContributionItem) items[idx]).getAction() instanceof ISystemAction)) {
					ISystemAction item = (ISystemAction) (((ActionContributionItem) items[idx]).getAction());
					try {
						item.setInputs(getShell(), this, selection);
					} catch (Exception e) {
						SystemBasePlugin.logError("Error configuring action " + item.getClass().getName(), e); //$NON-NLS-1$
					}
				} else if (items[idx] instanceof SystemSubMenuManager) {
					SystemSubMenuManager item = (SystemSubMenuManager) items[idx];
					item.setInputs(getShell(), this, selection);
				}
			}

			// ***** DO NOT ADD ANY ACTIONS AFTER HERE *****

		}

	}


	/**
	 * Called when the context menu is about to open.
	 * Calls {@link #fillContextMenu(IMenuManager)}
	 */
	public void menuAboutToShow(IMenuManager menu) {
		if (!enabledMode) return;
		fillContextMenu(menu);
		if (!menuListenerAdded) {
			if (menu instanceof MenuManager) {
				Menu m = ((MenuManager) menu).getMenu();
				if (m != null) {
					menuListenerAdded = true;
					SystemViewMenuListener ml = new SystemViewMenuListener();
					if (messageLine != null) ml.setShowToolTipText(true, messageLine);
					m.addMenuListener(ml);
				}
			}
		}
		//System.out.println("Inside menuAboutToShow: menu null? "+( ((MenuManager)menu).getMenu()==null));
	}

	/**
	 * Creates the Systems plugin standard groups in a context menu.
	 */
	public static IMenuManager createStandardGroups(IMenuManager menu) {
		if (!menu.isEmpty()) return menu;
		// simply sets partitions in the menu, into which actions can be directed.
		// Each partition can be delimited by a separator (new Separator) or not (new GroupMarker).
		// Deleted groups are not used yet.
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_NEW)); // new->
		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_GOTO)); // goto into, go->
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_EXPANDTO)); // expand to->
		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_EXPAND)); // expand, collapse
		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_OPEN)); // open xxx
		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_OPENWITH)); // open with->
		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_BROWSEWITH)); // open with->
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_WORKWITH)); // work with->
		//menu.add(new Separator(ISystemContextMenuConstants.GROUP_SHOW));         // show->type hierarchy, in-navigator
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_BUILD)); // build, rebuild, refresh
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_CHANGE)); // update, change
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_REORGANIZE)); // rename,move,copy,delete,bookmark,refactoring
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_REORDER)); // move up, move down
		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_GENERATE)); // getters/setters, etc. Typically in editor
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_SEARCH)); // search
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_CONNECTION)); // connection-related actions
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_STARTSERVER)); // start/stop remote server actions
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_IMPORTEXPORT)); // get or put actions
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_ADAPTERS)); // actions queried from adapters
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_ADDITIONS)); // user or BP/ISV additions
		//menu.add(new Separator(ISystemContextMenuConstants.GROUP_VIEWER_SETUP)); // ? Probably View->by xxx, yyy
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_TEAM)); // Team
		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_COMPAREWITH));
		menu.add(new GroupMarker(ISystemContextMenuConstants.GROUP_REPLACEWITH));
		menu.add(new Separator(ISystemContextMenuConstants.GROUP_PROPERTIES)); // Properties

		// [177537] [api] Dynamic system type provider need a hook to add dynamic system type specific menu groups.
		IRSESystemType[] systemTypes = SystemWidgetHelpers.getValidSystemTypes(null);
		for (int i = 0; i < systemTypes.length; i++) {
			IRSESystemType systemType = systemTypes[i];
			Object adapter = systemType.getAdapter(RSESystemTypeAdapter.class);
			if (adapter instanceof RSESystemTypeAdapter) {
				((RSESystemTypeAdapter)adapter).addCustomMenuGroups(menu);
			}
		}

		return menu;
	}

	/**
	 * protected helper method to add an Action to a given menu.
	 * To give the action the opportunity to grey out, we call selectionChanged, but
	 * only if the action implements ISelectionChangedListener
	 */
	protected void menuAdd(MenuManager menu, IAction action) {
		if (action instanceof ISelectionChangedListener) ((ISelectionChangedListener) action).selectionChanged(new SelectionChangedEvent(this, getSelection()));
	}

	/**
	 * Determines whether the view has an ancestor relation selection so
	 * that actions can be enable/disabled appropriately.
	 * For example, delete needs to be disabled when a parent and it's child
	 * are both selected.
	 * @return true if the selection has one or more ancestor relations
	 */
	protected boolean hasAncestorRelationSelection() {
		return selectionHasAncestryRelationship();
		/*
		TreeItem[] elements = getTree().getSelection();


		//Item[] elements = getSelection(getControl());
		for (int i = 0; i < elements.length; i++) {
			TreeItem parentItem = elements[i];
			//for (int j = 0; j < elements.length; j++) {
				//if (j != i) {
					if (isAncestorOf(parentItem, elements))
					{
						return true;
					}
				//}
		//	}
		}
		return false;
		*/
	}

	/**
	 * Handles selection changed in viewer.
	 * Updates global actions.
	 * Links to editor (if option enabled)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		selectionFlagsUpdated = false;
		_setList = new ArrayList();
		IStructuredSelection sel = (IStructuredSelection) event.getSelection();
		Object firstSelection = sel.getFirstElement();
		if (firstSelection == null) return;

		//	added by Phil. Noticed Edit->Delete not enabled when it should be
		boolean enableDelete = true;
		IStructuredSelection selection = (IStructuredSelection) getSelection();
		Iterator elements = selection.iterator();

		while (enableDelete && elements.hasNext()) {
			Object element = elements.next();
			ISystemViewElementAdapter adapter = getViewAdapter(element);
			if (adapter == null) continue;
			if (enableDelete) enableDelete = adapter.showDelete(element) && adapter.canDelete(element);
		}
		//System.out.println("Enabling delete action: "+enableDelete);
		//System.out.println("Enabling selectAll action: "+enableSelectAll(sel));
		((SystemCommonDeleteAction) getDeleteAction()).setEnabled(enableDelete);
		((SystemCommonSelectAllAction) getSelectAllAction()).setEnabled(enableSelectAll(sel)); // added by Phil. Noticed Edit->Select All not enabled when it should be

		ISystemViewElementAdapter adapter = getViewAdapter(firstSelection);
		if (adapter != null) {
			displayMessage(adapter.getStatusLineText(firstSelection));
			if ((mouseButtonPressed == LEFT_BUTTON) && (!expandingTreeOnly)) //d40615
				adapter.selectionChanged(firstSelection); //d40615
		} else
			clearMessage();
		//System.out.println("Inside selectionChanged in SystemView");
		expandingTreeOnly = false; //d40615
	}

	protected void logMyDebugMessage(String prefix, String msg) {
		if (!debugProperties) return;
		//RSEUIPlugin.logDebugMessage(prefix, msg);
		System.out.println(prefix + " " + msg); //$NON-NLS-1$
	}

	/**
	 * Convenience method for returning the shell of this viewer.
	 */
	public Shell getShell() {
		//getShell() can lead to "widget is disposed" errors, but avoiding them here does not really help
		if (!getTree().isDisposed()) {
			return getTree().getShell();
		}
		return shell;
	}

	/**
	 * Helper method to determine if a given object is currently selected.
	 * Does not consider if a child node of the given object is currently selected.
	 */
	protected boolean isSelected(Object element) {
		return isSelected(element, (IStructuredSelection) getSelection());
	}

	/**
	 * Helper method to determine if a given tree item is currently selected.
	 * Does not consider if a child node of the given object is currently selected.
	 */
	protected boolean isTreeItemSelected(Widget w) {
		boolean match = false;
		TreeItem[] items = getTree().getSelection();
		if ((items != null) && (items.length > 0)) {
			for (int idx = 0; !match && (idx < items.length); idx++)
				if (items[idx] == w) match = true;
		}
		return match;
	}

	/**
	 * Helper method to determine if any of a given array of objects is currently selected
	 * Does not consider if a child node of the given object is currently selected.
	 */
	protected boolean isSelected(Object[] elementArray) {
		return isSelected(elementArray, (IStructuredSelection) getSelection());
	}

	/**
	 * Helper method to determine if a given object is in given selection
	 * Does not consider if a child node of the given object is currently selected.
	 */
	protected boolean isSelected(Object element, IStructuredSelection selection) {
		boolean isSelected = false;
		Iterator elements = selection.iterator();
		while (!isSelected && elements.hasNext()) {
			if (element.equals(elements.next())) isSelected = true;
		}
		return isSelected;
	}

	/**
	 * Helper method to determine if any of a given array of objects is in given selection
	 * Does not consider if a child node of the given object is currently selected.
	 */
	protected boolean isSelected(Object[] elementArray, IStructuredSelection selection) {
		boolean isSelected = false;
		Iterator elements = selection.iterator();
		while (!isSelected && elements.hasNext()) {
			Object nextSelection = elements.next();
			for (int idx = 0; !isSelected && (idx < elementArray.length); idx++) {
				if (elementArray[idx].equals(nextSelection)) isSelected = true;
			}
		}
		return isSelected;
	}

	/**
	 * Helper method to determine if a given object is currently selected.
	 * Does consider if a child node of the given object is currently selected.
	 */
	protected boolean isSelectedOrChildSelected(Object[] parentElements) {
		boolean isSelected = false;
		if ((parentElements == null) || (parentElements.length == 0)) return false;
		for (int idx = 0; !isSelected && (idx < parentElements.length); idx++)
			isSelected = isSelectedOrChildSelected(parentElements[idx]);
		return isSelected;
	}

	protected boolean searchToRoot(TreeItem selectedItem, TreeItem searchItem) {
		boolean found = false;
		boolean done = false;
		while (!found && !done) {
			if (selectedItem == searchItem)
				found = true;
			else {
				if (selectedItem != null)
				{
					selectedItem = selectedItem.getParentItem();
					if (selectedItem == null) done = true;
				}
			}
		}
		return found;
	}

	/**
	 * Called after tree item collapsed. Updates the children of the tree item being
	 * collapsed by removing the widgets associated with any transient message objects
	 * that were in the tree.
	 * @param event the event that caused the collapse. The event data will include the
	 * tree element being collapsed.
	 */
	public void treeCollapsed(TreeExpansionEvent event) {
		final Object element = event.getElement(); // get parent node being collapsed
		// we always allow adapters opportunity to show a different icon depending on collapsed state
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				String[] allProps = { IBasicPropertyConstants.P_TEXT, IBasicPropertyConstants.P_IMAGE };
				update(element, allProps); // for refreshing non-structural properties in viewer when model changes
			}
		});
	}

	/**
	 *	Called after tree item expanded.
	 *  We need this hook to potentially undo user expand request.
	 *  @param event the SWT TreeExpansionEvent that caused the expansion.
	 */
	public void treeExpanded(TreeExpansionEvent event) {
		expandingTreeOnly = true;
		final Object element = event.getElement();
		// we always allow adapters opportunity to show a different icon depending on expanded state
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				updatePropertySheet();
				String[] allProps = { IBasicPropertyConstants.P_TEXT, IBasicPropertyConstants.P_IMAGE };
				update(element, allProps); // for refreshing non-structural properties in viewer when model changes
			}
		});
	}

	/* (non-Javadoc)
	 * Here only for observability.
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#handleTreeCollapse(org.eclipse.swt.events.TreeEvent)
	 */
	protected void handleTreeCollapse(TreeEvent event) {
		super.handleTreeCollapse(event);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#handleTreeExpand(org.eclipse.swt.events.TreeEvent)
	 */
	protected void handleTreeExpand(TreeEvent event) {
		TreeItem item = (TreeItem) event.item;
		removeTransientMessages(item);
		Shell shell = getShell();
		Object data = item.getData();
		boolean showBusy = (data instanceof IHost) && ((IHost)data).isOffline();
		if (showBusy) SystemPromptDialog.setDisplayCursor(shell, busyCursor);
		super.handleTreeExpand(event);
		if (showBusy) SystemPromptDialog.setDisplayCursor(shell, null);
	}

	/**
	 * Remove the transient messages from this item.
	 * @param item The tree item whose children should be examined for transient messages.
	 */
	private void removeTransientMessages(TreeItem item) {
		// Remove any transient messages prior to finding children. They will be regenerated if they are needed.
		Item[] children = getItems(item);
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				Item child = children[i];
				Object data = child.getData();
				if (data instanceof ISystemMessageObject) {
					ISystemMessageObject message = (ISystemMessageObject) data;
					if (message.isTransient()) {
						remove(message);
					}
				}
			}
		}
	}


	/**
	 * Clear current selection. Ignore widget disposed message.
	 */
	protected void clearSelection() {
		try {
			setSelection((ISelection) null);
		} catch (Exception exc) {
		}
	}



	/**
	 * Returns the implementation of ISystemViewElementAdapter for the given
	 * object.  Returns null if the adapter is not defined or the
	 * object is not adaptable.
	 */
	protected ISystemViewElementAdapter getViewAdapter(Object o) {

		ISystemViewInputProvider provider = getInputProvider();

		// should never be null, but we check just to be safe
		// the input provider should be set because for things like connections, the select
		// dialogs may set a different input provider for the connection adapter which is subsequently
		// not updated when selecting a connection in the Remote Systems view.
		// This ensures that the input provider for the Remote Systems view is set for the adapter.
		if (provider != null) {
			return SystemAdapterHelpers.getViewAdapter(o, this, provider);
		} else {
			return SystemAdapterHelpers.getViewAdapter(o, this);
		}
	}

	/**
	 * Returns the implementation of IRemoteObjectIdentifier for the given
	 * object.  Returns null if this object is not adaptable to this.
	 *
	 * @deprecated 	should use {@link #getViewAdapter(Object)} since IRemoteObjectIdentifier
	 * 		is not defined in the adapter factories
	 */
	protected IRemoteObjectIdentifier getRemoteObjectIdentifier(Object o)
	{
		return (IRemoteObjectIdentifier)((IAdaptable)o).getAdapter(IRemoteObjectIdentifier.class);
	}

	/**
	 * Returns the implementation of ISystemRemoteElementAdapter for the given
	 * object.  Returns null if this object is not adaptable to this.
	 */
	protected ISystemRemoteElementAdapter getRemoteAdapter(Object o)
	{
		if (o instanceof IAdaptable)
		{
			return (ISystemRemoteElementAdapter)((IAdaptable)o).getAdapter(ISystemRemoteElementAdapter.class);
		}
		return null;
	}


	/**
	 *
	 */
	public void handleDispose(DisposeEvent event) {
		//if (debug)
		//RSEUIPlugin.logDebugMessage(this.getClass().getName(),"Inside handleDispose for SystemView");
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
		sr.removeSystemResourceChangeListener(this);
		sr.removeSystemRemoteChangeListener(this);
		busyCursor.dispose();
		super.handleDispose(event);
	}

	/**
	 * Return the connection of the selected object, whatever it is.
	 */
	public IHost getSelectedConnection() {
		Object firstSelection = ((StructuredSelection) getSelection()).getFirstElement();
		if (firstSelection == null)
			return null;
		else if (firstSelection instanceof IHost)
			return (IHost) firstSelection;
		else if (firstSelection instanceof ISubSystem)
			return ((ISubSystem) firstSelection).getHost();
		else if (firstSelection instanceof ISystemFilterPoolReference)
			return ((ISubSystem) (((ISystemFilterPoolReference) firstSelection).getProvider())).getHost();
		else if (firstSelection instanceof ISystemFilterReference)
			return ((ISubSystem) (((ISystemFilterReference) firstSelection).getProvider())).getHost();
		else if (getViewAdapter(firstSelection) != null) {
			ISubSystem ss = getViewAdapter(firstSelection).getSubSystem(firstSelection);
			if (ss != null)
				return ss.getHost();
			else
				return null;
		} else
			return null;
	}

	/**
	 * We override getSelection(Control) so that a list of items
	 * under the same parent always gets returned in the order in which
	 * they appear in the tree view.  Otherwise, after a "move up" or
	 * "move down", the order of selection can come back wrong.
	 */
	protected Item[] getSelection(Control widget) {
		Tree tree = (Tree) widget;
		Item[] oldResult = tree.getSelection();
		if (oldResult != null && oldResult.length > 0) {
			if (oldResult[0] instanceof TreeItem) {
				Widget parentItem = ((TreeItem) oldResult[0]).getParentItem();
				if (parentItem == null) {
					parentItem = tree;
				}
				if (itemsShareParent(parentItem, oldResult)) {
					Item[] newResult = sortSelection(parentItem, oldResult);
					return newResult;
				}
			}
		}
		return oldResult;
	}

	protected boolean itemsShareParent(Widget parentItem, Item[] items) {
		for (int i = 0; i < items.length; i++) {
			Widget itemParent = ((TreeItem) items[i]).getParentItem();
			if (parentItem instanceof TreeItem) {
				if (itemParent != parentItem) {
					return false;
				}
			} else if (itemParent != null) {
				return false;
			}
		}

		return true;
	}

	protected Item[] sortSelection(Widget parentItem, Item[] oldResult) {
		Item[] children = null;
		Item[] newResult = new Item[oldResult.length];
		if (parentItem instanceof Item) {
				children = oldResult;
		} else
			children = getChildren(parentItem);

		for (int i = 0; i < oldResult.length; i++)
		{
			Item first = removeFirstItem(oldResult, children);
			newResult[i] = first;
		}

		return newResult;

	}


	protected Item removeFirstItem(Item[] items, Item[] children)
	{
		if (items != null)
		{
			for (int i = 0; i < items.length; i++)
			{
				if (items[i] != null)
				{
					Item current = items[i];
					items[i] = null;
					return current;
				}
			}
		}
		return null;
	}



	/**
	 * Move one tree item to a new location
	 */
	protected void moveTreeItem(Widget parentItem, Item item, Object src, int newPosition) {
		if (item==null) {
			//Null items during RSECombinedTestSuite - put debugBreak here to investigate
			assert item!=null;
			return;
		}
		if (getExpanded(item)) {
			setExpanded(item, false);
			refresh(src); // flush items from memory
		}

		createTreeItem(parentItem, src, newPosition);

		//createTreeItem(parentItem, (new String("New")), newPosition);
		//remove(src);

		disassociate(item);
		item.dispose();
		// TODO: make this work so the selection order doesn't get screwed up!
	}

	/**
	 * Move existing items a given number of positions within the same node.
	 * If the delta is negative, they are all moved up by the given amount. If
	 * positive, they are all moved down by the given amount.<p>
	 */
	protected void moveTreeItems(Widget parentItem, Object[] src, int delta) {
		int[] oldPositions = new int[src.length];
		Item[] oldItems = new Item[src.length];

		for (int idx = 0; idx < src.length; idx++)
			oldItems[idx] = (Item) internalFindRelativeItem(parentItem, src[idx], 1);

		Item[] children = null;
		if (parentItem instanceof Item) {
			children = getItems((Item) parentItem);
		} else
			children = getChildren(parentItem);

		for (int idx = 0; idx < src.length; idx++)
		{
			oldPositions[idx] = getTreeItemPosition(oldItems[idx], children) + 1;
		}

		if (delta > 0) // moving down, process backwards
		{
			for (int idx = src.length - 1; idx >= 0; idx--) {
				moveTreeItem(parentItem, oldItems[idx], src[idx], oldPositions[idx] + delta);
			}
		} else // moving up, process forewards
		{
			for (int idx = 0; idx < src.length; idx++) {
				moveTreeItem(parentItem, oldItems[idx], src[idx], oldPositions[idx] + delta - 1);
			}
		}
	}



	/**
	 * Get the position of a tree item within its parent
	 */
	/*
	protected int getTreeItemPosition(Widget parentItem, Item childItem, Item[] items) {
		int pos = -1;
		Item[] children = null;
		if (parentItem instanceof Item) {
			if (items == null)
				children = getItems((Item) parentItem);
			else
				children = items;
		} else
			children = getChildren(parentItem);
		for (int idx = 0; (pos == -1) && (idx < children.length); idx++) {
			if (children[idx] == childItem) pos = idx;
		}
		return pos;
	}
	*/


	/**
	 * Get the position of a tree item within its parent
	 */
	protected int getTreeItemPosition(Item childItem, Item[] children) {
		int pos = -1;
		for (int idx = 0; (pos == -1) && (idx < children.length); idx++) {
			if (children[idx] == childItem) pos = idx;
		}
		return pos;
	}


	/**
	 * Expand a given filter, given a subsystem that contains a reference to the filter's pool.
	 * This will expand down to the filter if needed
	 * @param parentSubSystem - the subsystem containing a reference to the filter's parent pool
	 * @param filter - the filter to find, reveal, and expand within the subsystem context
	 * @return the filter reference to the filter if found and expanded. This is a unique binary address
	 *   within the object's in this tree, so can be used in the viewer methods to affect this particular
	 *   node.
	 */
	public ISystemFilterReference revealAndExpand(ISubSystem parentSubSystem, ISystemFilter filter) {
		setExpandedState(parentSubSystem.getHost(), true); // expand the connection
		setExpandedState(parentSubSystem, true); // expand the subsystem
		Object filterParentInTree = parentSubSystem; // will be case unless in show filter pool mode
		// if showing filter pools, expand parent filter pool reference...
		if (SystemPreferencesManager.getShowFilterPools()) {
			ISystemFilterPoolReference poolRef = parentSubSystem.getFilterPoolReferenceManager().getReferenceToSystemFilterPool(filter.getParentFilterPool());
			setExpandedState(poolRef, true);
			filterParentInTree = poolRef;
		}
		// now, find the filter reference, and expand it...
		Widget parentItem = findItem(filterParentInTree); // find tree widget of parent
		if ((parentItem == null) || !(parentItem instanceof Item)) return null;
		TreeItem child = (TreeItem) internalFindReferencedItem(parentItem, filter, 1);
		if (child == null) return null;
		// found it! Now expand it...
		setExpandedState(child.getData(), true);
		return (ISystemFilterReference) child.getData();
	}

	// ------------------------------------
	// ISYSTEMRESOURCEChangeListener METHOD
	// ------------------------------------

	/**
	 * Called when something changes in the model
	 */
	public void systemResourceChanged(ISystemResourceChangeEvent event) {
		if (!getControl().isDisposed()) {
			ResourceChangedJob job = new ResourceChangedJob(event, this);
			job.setPriority(Job.INTERACTIVE);
			if (Display.getCurrent() != null) {
				job.runInUIThread(null);
			} else {
				// job.setUser(true);
				job.schedule();
			}
			/*
			Display display = Display.getCurrent();
			try {
				while (job.getResult() == null) {
					while (display != null && display.readAndDispatch()) {
						//Process everything on event queue
					}
					if (job.getResult() == null) Thread.sleep(200);
				}
			} catch (InterruptedException e) {
			}
			*/
		} else {
			trace("resource changed while shutting down"); //$NON-NLS-1$
		}
	}

	public void trace(String str) {
		String id = RSEUIPlugin.getDefault().getBundle().getSymbolicName();
		String val = Platform.getDebugOption(id + "/debug"); //$NON-NLS-1$
		if ("true".equals(val)) { //$NON-NLS-1$
			try {
				throw new IllegalStateException(str);
			} catch(IllegalStateException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Inner class which extends UIJob to connect this connection
	 * on the UI Thread when no Shell is available from
	 * the caller
	 */
	public class ResourceChangedJob extends UIJob {

		protected ISystemResourceChangeEvent _event;
		protected SystemView _originatingViewer;
		//public Exception _originatingThread; //for debugging

		public ResourceChangedJob(ISystemResourceChangeEvent event, SystemView originatingViewer) {
			super("Resource Changed..."); //$NON-NLS-1$
			//FIXME Shouldn't the originatingViewer be taken from the event if possible, if it is instanceof ISystemResourceChangeEventUI?
			//See also originatedHere, below
			_originatingViewer = originatingViewer;
			_event = event;
			//_originatingThread = new Exception();
			//_originatingThread.fillInStackTrace();
			////_originatingThread.printStackTrace();
			////System.out.println("<<<<<<<<<<<<<");
		}

		public IStatus runInUIThread(IProgressMonitor monitor) {
			if (getControl().isDisposed()) {
				trace("SystemView: refresh after disposed"); //$NON-NLS-1$
				return Status.CANCEL_STATUS;
			}
			int type = _event.getType();
			Object src = _event.getSource();
			Object parent = _event.getParent();

			String[] properties = new String[1];
			if (parent == RSECorePlugin.getTheSystemRegistry()) parent = inputProvider;
			ISubSystem ss = null;
			Widget item = null;
			Widget parentItem = null;
			Object[] multiSource = _event.getMultiSource();
			Object previous = null;
			boolean wasSelected = false;
			boolean originatedHere = true;
			if (_event instanceof SystemResourceChangeEventUI) {
				Viewer viewer = ((SystemResourceChangeEventUI)_event).getOriginatingViewer();
				if (viewer!=null && viewer!=_originatingViewer) {
					originatedHere = false;
				}
				Object viewerItem = ((SystemResourceChangeEventUI)_event).getViewerItem();
				if (viewerItem instanceof TreeItem) {
					inputTreeItem = (TreeItem)viewerItem;
				} else {
					inputTreeItem = null;
				}
			} else {
				inputTreeItem = null;
			}

			//logDebugMsg("INSIDE SYSRESCHGD: " + type + ", " + src + ", " + parent);
			switch (type) {
			// SPECIAL CASES: ANYTHING TO DO WITH FILTERS!!
			case ISystemResourceChangeEvents.EVENT_RENAME_FILTER_REFERENCE:
			case ISystemResourceChangeEvents.EVENT_CHANGE_FILTER_REFERENCE:
				findAndUpdateFilter(_event, type);
				break;
			case ISystemResourceChangeEvents.EVENT_CHANGE_FILTERSTRING_REFERENCE:
				findAndUpdateFilterString(_event, type);
				break;

			case ISystemResourceChangeEvents.EVENT_ADD_FILTERSTRING_REFERENCE:
			case ISystemResourceChangeEvents.EVENT_DELETE_FILTERSTRING_REFERENCE:
			case ISystemResourceChangeEvents.EVENT_MOVE_FILTERSTRING_REFERENCES:
				//findAndUpdateFilterStringParent(event, type);
				//break;
			case ISystemResourceChangeEvents.EVENT_ADD_FILTER_REFERENCE:
			case ISystemResourceChangeEvents.EVENT_DELETE_FILTER_REFERENCE:
			case ISystemResourceChangeEvents.EVENT_MOVE_FILTER_REFERENCES:
				// are we a secondary perspective, and our input or parent of our input was deleted?
				if ((
					   (type == ISystemResourceChangeEvents.EVENT_DELETE_FILTERSTRING_REFERENCE)
					|| (type == ISystemResourceChangeEvents.EVENT_DELETE_FILTER_REFERENCE)
					)
					&& affectsInput(src)
				) {
					close();
					return Status.OK_STATUS;
				}

				findAndUpdateFilterParent(_event, type);
				break;

			case ISystemResourceChangeEvents.EVENT_ADD:
			case ISystemResourceChangeEvents.EVENT_ADD_RELATIVE:
				if (debug) {
					logDebugMsg("SV event: EVENT_ADD "); //$NON-NLS-1$
				}
				// clearSelection();
				//refresh(parent);t
				parentItem = findItem(parent);
				if (parentItem == null) return Status.OK_STATUS;
				if ((parentItem instanceof Item) && !getExpanded((Item) parentItem)) {
					refresh(parent); // flush cached stuff so next call will show new item
				} else if ((parentItem instanceof Item) || // regular node
						(parent == inputProvider)) // root node. Hmm, hope this is going to work in all cases
				{
					boolean addingConnection = (src instanceof IHost);
					if (addingConnection)
					{
						// 196930 - don't add the connection when it's not supposed to be shown
						IHost con = (IHost)src;
						IRSESystemType sysType = con.getSystemType();
						if (sysType != null) { // sysType can be null if workspace contains a host that is no longer defined by the workbench
							RSESystemTypeAdapter adapter = (RSESystemTypeAdapter)(sysType.getAdapter(RSESystemTypeAdapter.class));

							if (adapter == null || !adapter.isEnabled(sysType))
							{
								// don't add this if our src is not enabled
								return Status.OK_STATUS;
							}
						}
						else {
							return Status.OK_STATUS;
						}
					}

					//System.out.println("ADDING CONNECTIONS.........................: " + addingConnection);
					//System.out.println("event.getParent() instanceof SystemRegistry: " + (event.getParent() instanceof SystemRegistry));
					//System.out.println("inputProvider.showingConnections().........: " + (inputProvider.showingConnections()));
					if ((parent == inputProvider) && addingConnection && (_event.getParent() instanceof ISystemRegistry) && !inputProvider.showingConnections()) return Status.OK_STATUS; // only reflect new connections in main perspective. pc42742
					int pos = -1;
					if (type == ISystemResourceChangeEvents.EVENT_ADD_RELATIVE) {
						previous = _event.getRelativePrevious();
						if (previous != null) pos = getItemIndex(parentItem, previous);
						if (pos >= 0) pos++; // want to add after previous
					} else
						pos = _event.getPosition();

					Item[] currentItems = null;
					if (parentItem instanceof Tree)
					{
						currentItems = ((Tree)parentItem).getItems();
					}
					else
					{
						currentItems = getItems((Item)parentItem);
					}
					boolean exists = false;
					// check for src
					for (int i = 0; i < currentItems.length && !exists; i++)
					{
						Item cur = currentItems[i];
						if (cur.getData() == src)
						{
							exists = true;
						}
					}

					//logDebugMsg("ADDING CONN? "+ addingConnection + ", position="+pos);
					if (!exists)
					{
						createTreeItem(parentItem, src, pos);
					}
					// setSelection(new StructuredSelection(src), true);
				}
				break;
			case ISystemResourceChangeEvents.EVENT_ADD_MANY:
				if (debug) {
					logDebugMsg("SV event: EVENT_ADD_MANY"); //$NON-NLS-1$
				}
				multiSource = _event.getMultiSource();
				clearSelection();
				parentItem = findItem(parent);
				if (parentItem == null) return Status.OK_STATUS;
				if ((parentItem instanceof Item) && !getExpanded((Item) parentItem)) {
					refresh(parent); // flush cached stuff so next call will show new items
				} else if (multiSource.length > 0) {
					boolean addingConnections = (multiSource[0] instanceof IHost);
					// are we restoring connections previously removed due to making a profile inactive,
					// and is one of these connections the one we were opened with?
					if (addingConnections && (_event.getParent() instanceof ISystemRegistry) && (inputProvider instanceof SystemEmptyListAPIProviderImpl)) {
						boolean done = false;
						for (int idx = 0; !done && (idx < multiSource.length); idx++) {
							if (multiSource[idx] == previousInputConnection) {
								done = true;
								setInputProvider(previousInputProvider);
								previousInput = null;
								previousInputProvider = null;
							}
						}
						if (done) return Status.OK_STATUS;
					}
					// are we adding connections and yet we are not a secondary perspective?
					// If so, this event does not apply to us.
					else if (addingConnections && (_event.getParent() instanceof ISystemRegistry) && !inputProvider.showingConnections()) return Status.OK_STATUS;

					for (int idx = 0; idx < multiSource.length; idx++) {
						if (debug && addingConnections) logDebugMsg("... new connection " + ((IHost) multiSource[idx]).getAliasName()); //$NON-NLS-1$
						createTreeItem(parentItem, multiSource[idx], -1);
					}
					setSelection(new StructuredSelection(multiSource), true);
				}
				break;
			case ISystemResourceChangeEvents.EVENT_REPLACE_CHILDREN:
				if (debug) {
					logDebugMsg("SV event: EVENT_REPLACE_CHILDREN"); //$NON-NLS-1$
				}
				multiSource = _event.getMultiSource();
				//logDebugMsg("MULTI-SRC LENGTH : " + multiSource.length);
				clearSelection();
				parentItem = findItem(parent);
				if (parentItem == null) return Status.OK_STATUS;
				if (multiSource.length > 0 && parentItem instanceof Item) {
					getControl().setRedraw(false);
					collapseNode(parent, true); // collapse and flush gui widgets from memory
					//setExpandedState(parent, true); // expand the parent
					setExpanded((Item) parentItem, true); // expand the parent without calling resolveFilterString
					TreeItem[] kids = ((TreeItem) parentItem).getItems(); // any kids? Like a dummy node?
					if (kids != null) for (int idx = 0; idx < kids.length; idx++){
						disassociate(kids[idx]);
						kids[idx].dispose();
					}
					//boolean addingConnections = (multiSource[0] instanceof SystemConnection);
					for (int idx = 0; idx < multiSource.length; idx++) {
						//if (debug && addingConnections)
						//  logDebugMsg("... new connection " + ((SystemConnection)multiSource[idx]).getAliasName());
						createTreeItem(parentItem, multiSource[idx], -1);
					}
					getControl().setRedraw(true);
					//setSelection(new StructuredSelection(multiSource),true);
				}
				break;
			case ISystemResourceChangeEvents.EVENT_CHANGE_CHILDREN:
				if (debug) {
					logDebugMsg("SV event: EVENT_CHANGE_CHILDREN. src=" + src + ", parent=" + parent); //$NON-NLS-1$ //$NON-NLS-2$
					//Exception e = new Exception();
					//e.fillInStackTrace();
					//e.printStackTrace();
				}
				// I HAVE DECIDED TO CHANGE THE SELECTION ALGO TO ONLY RESELECT IF THE CURRENT
				// SELECTION IS A CHILD OF THE PARENT... PHIL
				boolean wasSrcSelected = false;
				if (src != null) {
					wasSrcSelected = isSelectedOrChildSelected(src);
					//System.out.println("WAS SELECTED? " + wasSrcSelected);
				}
				item = findItem(parent);
				//logDebugMsg("  parent = " + parent);
				//logDebugMsg("  item = " + item);
				// INTERESTING BUG HERE. GETEXPANDED WILL RETURN TRUE IF THE TREE ITEM HAS EVER BEEN
				// EXPANDED BUT IS NOW COLLAPSED! I CANNOT FIND ANY API IN TreeItem or TreeViewer THAT
				// WILL TELL ME IF A TREE ITEM IS SHOWING OR NOT!
				if ((item != null) && (item instanceof TreeItem) && ((TreeItem) item).getExpanded()) {
					if (wasSrcSelected) {
						//System.out.println("...Clearing selection");
						clearSelection();
					}
					//refresh(parent);
					if (debug) System.out.println("Found item and it was expanded for " + parent); //$NON-NLS-1$
					getControl().setRedraw(false);
					collapseNode(parent, true); // collapse and flush gui widgets from memory
					setExpandedState(parent, true); // expand the parent
					getControl().setRedraw(true);
					if (wasSrcSelected) {
						//System.out.println("Setting selection to " + src);
						setSelection(new StructuredSelection(src), true);
					}
				} else
					collapseNode(parent, true);
				break;
			case ISystemResourceChangeEvents.EVENT_DELETE:
				if (debug) logDebugMsg("SV event: EVENT_DELETE "); //$NON-NLS-1$
				// are we a secondary perspective, and our input or parent of our input was deleted?
				if (affectsInput(src)) {
					close();
					return Status.OK_STATUS;
				}
				parentItem = findItem(parent);
				if (parentItem == null) return Status.OK_STATUS;
				if ((parentItem instanceof Item) && !getExpanded((Item) parentItem))
					refresh(parent); // flush memory
				else {
					wasSelected = isSelectedOrChildSelected(src);
					if (wasSelected) clearSelection();
					_originatingViewer.remove(src);
					if (wasSelected) setSelection(new StructuredSelection(parent), true);
				}
				break;

			case ISystemResourceChangeEvents.EVENT_DELETE_MANY:
				if (debug) {
					logDebugMsg("SV event: EVENT_DELETE_MANY "); //$NON-NLS-1$
				}
				multiSource = _event.getMultiSource();
				// are we a secondary perspective, and our input or parent of our input was deleted?
				if (affectsInput(multiSource)) {
					close();
					return Status.OK_STATUS;
				}
				if (parent != null) {
					parentItem = findItem(parent);
				} else {
					// find first parentItem for source
					if (multiSource != null && multiSource.length > 0) {
						Widget sitem = findItem(multiSource[0]);
						if (sitem instanceof TreeItem) {
							parentItem = ((TreeItem)sitem).getParentItem();
							if (parentItem == null) {
								parentItem = ((TreeItem)sitem).getParent();
							}
						}
					}
				}
				if (parentItem == null) {
					return Status.OK_STATUS;
				}
				if ((parentItem instanceof Item) && !getExpanded((Item) parentItem)) {
					refresh(parent); // flush memory
				} else if (parentItem instanceof Tree) {
					if (_originatingViewer != null) {
						_originatingViewer.remove(multiSource);
					}
				} else {
					wasSelected = isSelectedOrChildSelected(multiSource);
					if (wasSelected) {
						clearSelection();
					}
					if (_originatingViewer != null) {
						_originatingViewer.remove(multiSource);
					}
					if (wasSelected) {
						setSelection(parent != null ? new StructuredSelection(parent) : null, true);
					}
				}
				break;
			/* Now done below in systemRemoteResourceChanged
			 case EVENT_DELETE_REMOTE:
			 if (debug)
			 logDebugMsg("SV event: EVENT_DELETE_REMOTE ");
			 deleteRemoteObject(src);
			 break;

			 case EVENT_DELETE_REMOTE_MANY:
			 // multi-source: array of objects to delete
			 if (debug)
			 logDebugMsg("SV event: EVENT_DELETE_REMOTE_MANY ");
			 multiSource = event.getMultiSource();
			 //remoteItemsToSkip = null; // reset
			 if ((multiSource == null) || (multiSource.length==0))
			 return;
			 for (int idx=0; idx<multiSource.length; idx++)
			 deleteRemoteObject(multiSource[idx]);
			 break;
			 */
			case ISystemResourceChangeEvents.EVENT_RENAME:
				if (debug) logDebugMsg("SV event: EVENT_RENAME "); //$NON-NLS-1$
				properties[0] = IBasicPropertyConstants.P_TEXT;
				update(src, properties); // for refreshing non-structural properties in viewer when model changes
				updatePropertySheet();
				break;
			/* Now done below in systemRemoteResourceChanged
			 case EVENT_RENAME_REMOTE:
			 // SRC: the updated remote object, after the rename
			 // PARENT: the String from calling getAbsoluteName() on the remote adapter BEFORE updating the remote object's name
			 if (debug)
			 logDebugMsg("SV event: EVENT_RENAME_REMOTE ");

			 renameRemoteObject(src, (String)parent);
			 break;
			 */
			case ISystemResourceChangeEvents.EVENT_ICON_CHANGE:
				if (debug) logDebugMsg("SV event: EVENT_ICON_CHANGE "); //$NON-NLS-1$
				if (multiSource != null && multiSource.length > 1){
					src = multiSource; // use multi source instead
				}
				if (src instanceof Object[] && ((Object[])src).length < 1000){ // too expensive when there are tons of children
					Object[] srcs = (Object[])src;
					// only do this if there's an associated item
					Object src1 = srcs[0];
					Widget w = findItem(src1);
					if (w == null){ // can't find item in tree - so fall back to refresh
						refresh(parent);						
					}
					else {
						for (int s = 0; s < srcs.length; s++){
							Object srcObj = srcs[s];
							if (srcObj != null){
								if (initViewerFilters != null && initViewerFilters.length > 0) {
									w = findItem(srcs[s]);
									if (w == null) {
										// refresh(parent);		 - don't refresh since this can cause an infinite cycle of refreshes!				
									} else {
										properties[0] = IBasicPropertyConstants.P_IMAGE;
										update(srcObj, properties); // for refreshing non-structural properties in viewer when model changes
		
									}
								} else {
									properties[0] = IBasicPropertyConstants.P_IMAGE;
									update(srcObj, properties); // for refreshing non-structural properties in viewer when model changes
								}
							}
						}
					}
				}
				else {
					if (initViewerFilters != null && initViewerFilters.length > 0) {
						Widget w = findItem(src);
						if (w == null) {
							refresh(parent);
						} else {
							properties[0] = IBasicPropertyConstants.P_IMAGE;
							update(src, properties); // for refreshing non-structural properties in viewer when model changes

						}
					} else {
						properties[0] = IBasicPropertyConstants.P_IMAGE;
						update(src, properties); // for refreshing non-structural properties in viewer when model changes
					}
				}

				//updatePropertySheet();
				break;
			//case EVENT_CHANGE:
			//if (debug)
			//logDebugMsg("SV event: EVENT_CHANGE ");
			//refresh(src); THIS IS AN EVIL OPERATION: CAUSES ALL EXPANDED NODES TO RE-REQUEST THEIR CHILDREN. OUCH!
			//updatePropertySheet();
			//break;
			case ISystemResourceChangeEvents.EVENT_REFRESH:
				if (debug) logDebugMsg("SV event: EVENT_REFRESH "); //$NON-NLS-1$
				//if (src != null)
				//  refresh(src); // ONLY VALID WHEN USER TRULY WANTS TO REQUERY CHILDREN FROM HOST
				//else
				//  refresh(); // refresh entire tree
				if ((src == null) || (src == RSEUIPlugin.getTheSystemRegistryUI()))
					refreshAll();
				else {
					//FIXME Why do we forceRemote here? EVENT_REFRESH_SELECTED also does not do forceRemote.
					//smartRefresh(src, false);
					smartRefresh(src, true);
				}
				updatePropertySheet();
				break;
			// refresh the parent of the currently selected items.
			// todo: intelligently re-select previous selections
			case ISystemResourceChangeEvents.EVENT_REFRESH_SELECTED_PARENT:
				if (debug) logDebugMsg("SV event: EVENT_REFRESH_SELECTED_PARENT "); //$NON-NLS-1$
				TreeItem[] items = getTree().getSelection();
				if ((items != null) && (items.length > 0) && (items[0] != null)) {
					//System.out.println("Selection not empty");
					parentItem = getParentItem(items[0]); // get parent of first selection. Only allowed to select items of same parent.
					if ((parentItem != null) && (parentItem instanceof Item)) {
						//System.out.println("parent of selection not empty: "+parentItem.getData());
						smartRefresh(new TreeItem[] { (TreeItem) parentItem });
					}
					//else
					//System.out.println("parent of selection is empty");
				}
				//else
				//System.out.println("Selection is empty");
				break;
			case ISystemResourceChangeEvents.EVENT_REFRESH_SELECTED:
				if (debug) logDebugMsg("SV event: EVENT_REFRESH_SELECTED "); //$NON-NLS-1$
				IStructuredSelection selected = (IStructuredSelection) getSelection();
				Iterator i = selected.iterator();
				// the following is a tweak. Refresh only re-queries the children. If the selected item has no
				//  childen, then refresh does nothing. Instead of that outcome, we re-define it to mean refresh
				//  the parent. The tricky part is to prevent multiple refreshes on multiple selections so we have
				//  to pre-scan for this scenario.
				// We also want to re-select any remote objects currently selected. They lose their selection as their
				//  memory address changes.
				Item parentElementItem = null;
				List selectedRemoteObjects = new Vector();
				items = getTree().getSelection();
				int itemIdx = 0;
				//System.out.println("Inside EVENT_REFRESH_SELECTED. FIRST SELECTED OBJECT = " + items[0].handle);
				while (i.hasNext()) {

					Object element = i.next();
					ISystemViewElementAdapter adapter = _originatingViewer.getViewAdapter(element);
					if ((parentElementItem == null) && (adapter != null) && (!adapter.hasChildren((IAdaptable)element))) {
						//parentItem = getParentItem((Item)findItem(element));
						parentItem = getParentItem(items[itemIdx]);
						if ((parentItem != null) && (parentItem instanceof Item)) parentElementItem = (Item) parentItem; //.getData();
					}
					if (getViewAdapter(element) != null) {
						selectedRemoteObjects.add(element);
						if (ss == null) ss = getViewAdapter(element).getSubSystem(element);
					}
					itemIdx++;
				}
				if (parentElementItem != null) {
					//refresh(parentElement);
					//FIXME IF a multi-select contains elements with a different parent than the one found, they will be ignored.
					smartRefresh(new TreeItem[] { (TreeItem) parentElementItem });
					if (selectedRemoteObjects.size() > 0) {
						selectRemoteObjects(selectedRemoteObjects, ss, parentElementItem);
					}
				}
				// the following is another tweak. If an expanded object is selected for refresh, which has remote children,
				// and any of those children are expanded, then on refresh the resulting list may be in a different
				// order and the silly algorithm inside tree viewer will simply re-expand the children at the previous
				// relative position. If that position has changed, the wrong children are re-expanded!
				// How to fix this? Ugly code to get the query the list of expanded child elements prior to refresh,
				// collapse them, do the refresh, then re-expand them based on absolute name versus tree position.
				// Actually, to do this right we need to test if the children of the selected item are remote objects
				// versus just the selected items because they may have selected a filter!
				// We go straight the TreeItem level for performance and ease of programming.
				else {
					smartRefresh(getTree().getSelection());
				}
				//else
				//{
				//i = selected.iterator();
				//while (i.hasNext())
				//refresh(i.next());
				//}

				updatePropertySheet();
				break;
			case ISystemResourceChangeEvents.EVENT_REFRESH_SELECTED_FILTER:
				if (debug) logDebugMsg("SV event: EVENT_REFRESH_SELECTED_FILTER "); //$NON-NLS-1$
				IStructuredSelection selectedItems = (IStructuredSelection) getSelection();
				Iterator j = selectedItems.iterator();
				// We climb up the tree here until we find a SystemFilterReference data member in the tree.
				// If we do find a reference of SystemFilterReference we refresh on it.
				// If we do not find a reference of SystemFilterReference we.....TODO: WHAT DO WE DO???
				// We also want to re-select any remote objects currently selected. They lose their selection as their
				//  memory address changes.
				Item parentElemItem = null;
				List selRemoteObjects = new Vector();
				if (j.hasNext()) {
					Object element = j.next();
					ISystemViewElementAdapter adapter = _originatingViewer.getViewAdapter(element);
					if (adapter != null) {
						Item parItem = getParentItem((Item) findItem(element));

						if (parItem != null) parentElemItem = parItem; //.getData();

						while (parItem != null && !(parItem.getData() instanceof ISystemFilterReference)) {
							parItem = getParentItem(parItem);

							if (parItem != null) parentElemItem = parItem; //.getData();
						}
					}
					if (getViewAdapter(element) != null) {
						selRemoteObjects.add(element);
						ss = getViewAdapter(element).getSubSystem(element);
					}
				}

				if (parentElemItem != null && (parentElemItem.getData() instanceof ISystemFilterReference)) {
					smartRefresh(new TreeItem[] { (TreeItem) parentElemItem });
					if (selRemoteObjects.size() > 0) {
						selectRemoteObjects(selRemoteObjects, ss, parentElemItem);
					}

					updatePropertySheet();
				} else {
					// if we cannot find a parent element that has a system filter reference then we refresh
					// everything since the explorer must be within a filter
					_event.setType(ISystemResourceChangeEvents.EVENT_REFRESH);
					systemResourceChanged(_event);
				}
				break;
			case ISystemResourceChangeEvents.EVENT_REFRESH_REMOTE:
				if (debug) logDebugMsg("SV event: EVENT_REFRESH_REMOTE: src = " + src); //$NON-NLS-1$

				// Fake expanded is set to the item for the src object if the object is in a collapsed state and
				// resides directly under a filter.  The item is artificially expanded in order to allow
				// refreshRemoteObject() to go through with a query of the item.  After the query is kicked off,
				// fakeExpanded is contracted in order to retain the original tree expand state.
				TreeItem fakeExpanded = null;

				ISystemViewElementAdapter adapter = getViewAdapter(src);
				if (adapter != null)
				{
					// we need to refresh filters
					ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();

					// if this is a filter reference, we just need to refresh it
					if (src instanceof ISystemFilterReference)
					{
						refresh(src);
						break;
					}

					// need to find filter references contain this object
		        	List filterReferences = sr.findFilterReferencesFor(src, adapter.getSubSystem(src), false);

					// first, find out if src is a container or not
					// if it's a container, just pass into refreshRemoteObject
					// if it's NOT a container, pass in it's parent
					boolean hasChildren = adapter.hasChildren((IAdaptable)src);
					if (!hasChildren && !(src instanceof ISubSystem))
					{
						// make the src the parent of the src
						Object srcParent = adapter.getParent(src);
						if (srcParent != null)
						{
							if (filterReferences.size() > 0){
								for (int r = 0; r < filterReferences.size(); r++){
									ISystemFilterReference ref = (ISystemFilterReference)filterReferences.get(r);
									refresh(ref);
								}
								break;
							}
							else {
								src = srcParent;
							}

						}
					}
					else
					{
						// only do this if the object is "remote"
						if (adapter.isRemote(src))
						{
							// get up-to-date version of the container (need to make sure it still exists)
							if (ss == null)
							{
								ss = adapter.getSubSystem(src);
							}
							if (ss != null)
							{
								// for bug 196662
								// if we're refreshing a previously unexpanded node, then a query will not happen
								// so we should refresh it's parent in this case
								Widget w = findItem(src);
								if (w instanceof TreeItem)
								{
									TreeItem titem = (TreeItem)w;
									TreeItem[] titems = titem.getItems();
									if (titems.length >  0 && !titem.getExpanded())
									{
										// the item is artificially expanded in order to allow the query to go through in
										// refreshRemoteObject()
										titem.setExpanded(true);

										// we set this so that after calling refreshRemoteObject(), the item can be re-collapsed
										fakeExpanded = titem;
									}
								}
							}
						}
					}
				}

				refreshRemoteObject(src, parent, originatedHere);
				if (fakeExpanded != null){
					fakeExpanded.setExpanded(false);
				}

				break;
			case ISystemResourceChangeEvents.EVENT_SELECT_REMOTE:
				if (debug) logDebugMsg("SV event: EVENT_SELECT_REMOTE: src = " + src); //$NON-NLS-1$
				//remoteItemsToSkip = null; // reset
				selectRemoteObjects(src, (ISubSystem) null, parent);
				break;

			case ISystemResourceChangeEvents.EVENT_MOVE_MANY:
				if (debug) logDebugMsg("SV event: EVENT_MOVE_MANY "); //$NON-NLS-1$
				multiSource = _event.getMultiSource();
				if ((multiSource == null) || (multiSource.length == 0)) return Status.OK_STATUS;
				parentItem = findItem(parent);
				if (parentItem == null) return Status.OK_STATUS;
				if ((parentItem instanceof Item) && !getExpanded((Item) parentItem))
					refresh(parent); // flush memory
				else {
					clearSelection();
					moveTreeItems(parentItem, multiSource, _event.getPosition());
					setSelection(new StructuredSelection(multiSource), true);
				}
				break;
			case ISystemResourceChangeEvents.EVENT_PROPERTY_CHANGE:
				if (debug) logDebugMsg("SV event: EVENT_PROPERTY_CHANGE "); //$NON-NLS-1$
				String[] allProps = { IBasicPropertyConstants.P_TEXT, IBasicPropertyConstants.P_IMAGE };
				ISystemRemoteElementAdapter ra = (ISystemRemoteElementAdapter)((IAdaptable)src).getAdapter(ISystemRemoteElementAdapter.class);
				if (ra != null) {
					updateRemoteObjectProperties(src);
				} else
					update(src, allProps); // for refreshing non-structural properties in viewer when model changes
				updatePropertySheet();
				break;
			case ISystemResourceChangeEvents.EVENT_PROPERTYSHEET_UPDATE:
				if (debug) logDebugMsg("SV event: EVENT_PROPERTYSHEET_UPDATE "); //$NON-NLS-1$
				updatePropertySheet();
				break;
			case ISystemResourceChangeEvents.EVENT_MUST_COLLAPSE:
				if (debug) logDebugMsg("SV event: EVENT_MUST_COLLAPSE "); //$NON-NLS-1$
				collapseNode(src, true); // collapse and flush gui widgets from memory
				break;
			case ISystemResourceChangeEvents.EVENT_COLLAPSE_ALL:
				if (debug) logDebugMsg("SV event: EVENT_COLLAPSE_ALL "); //$NON-NLS-1$
				collapseAll(); // collapse all
				if ((src != null) && (src instanceof String) && ((String) src).equals("false")) // defect 41203 //$NON-NLS-1$
				{
				} else
					refresh(); // flush gui widgets from memory
				break;
			case ISystemResourceChangeEvents.EVENT_COLLAPSE_SELECTED: // defect 41203
				if (debug) logDebugMsg("SV event: EVENT_COLLAPSE_SELECTED "); //$NON-NLS-1$
				collapseSelected();
				break;
			case ISystemResourceChangeEvents.EVENT_EXPAND_SELECTED: // defect 41203
				if (debug) logDebugMsg("SV event: EVENT_EXPAND_SELECTED "); //$NON-NLS-1$
				expandSelected();
				break;

			case ISystemResourceChangeEvents.EVENT_REVEAL_AND_SELECT:
				if (debug) logDebugMsg("SV event: EVENT_REVEAL_AND_SELECT "); //$NON-NLS-1$
				parentItem = findItem(parent);
				if (parentItem == null) return Status.OK_STATUS;
				if ((parentItem instanceof Item) && !getExpanded((Item) parentItem)) {
					setExpandedState(parent, true);
					Object toSelect = src;
					//if (event.getMultiSource() != null)
					//toSelect = event.getMultiSource();
					//clearSelection();
					if (toSelect != null) {
						if (parent instanceof IRSEBaseReferencingObject) {
							TreeItem child = (TreeItem) internalFindReferencedItem(parentItem, toSelect, 1);
							if (child != null) toSelect = child.getData();
						} else if ((parent instanceof ISystemFilterPoolReferenceManagerProvider) && !(src instanceof IRSEBaseReferencingObject)) {
							// we are in "don't show filter pools" mode and a new filter was created
							//  (we get the actual filter, vs on pool ref creation when we get the pool ref)
							TreeItem child = (TreeItem) internalFindReferencedItem(parentItem, toSelect, 1);
							if (child != null) toSelect = child.getData();
						}
						setSelection(new StructuredSelection(toSelect), true);
					}
				}
				break;
			case ISystemResourceChangeEvents.EVENT_SELECT:
				if (debug) logDebugMsg("SV event: EVENT_SELECT "); //$NON-NLS-1$
				item = findItem(src);
				if (item == null) // if not showing item, this is a no-op
					return Status.OK_STATUS;
				setSelection(new StructuredSelection(src), true);
				break;
			case ISystemResourceChangeEvents.EVENT_SELECT_EXPAND:
				if (debug) logDebugMsg("SV event: EVENT_SELECT_EXPAND "); //$NON-NLS-1$
				item = findItem(src);
				if (item == null) // if not showing item, this is a no-op
					return Status.OK_STATUS;
				if (!getExpanded((Item) item)) setExpandedState(src, true);
				setSelection(new StructuredSelection(src), true);
				break;

			}
			return Status.OK_STATUS;
		}
	}

	// ------------------------------------
	// ISYSTEMREMOTEChangeListener METHOD
	// ------------------------------------


	private static class CheckPending implements Runnable
	{
		private boolean _notReady = true;
		private TreeItem _item;

		public CheckPending(TreeItem item)
		{
			_item = item;
		}

		public void run()
		{
			if (_item.isDisposed()) {
				// Parent was deleted in the meantime
				_notReady = false;
			} else {
				Item[] items = _item.getItems();
				// We know that a child must appear eventualy, because the
				// REMOTE_RESOURCE_CREATED event is only sent in case of
				// successful creation of the element.
				_notReady = (items.length <= 0 || ProgressMessages.PendingUpdateAdapter_PendingLabel.equals(items[0].getText()));
			}
		}

		public boolean isNotReady()
		{
			return _notReady;
		}
	}

	/**
	 * This is the method in your class that will be called when a remote resource
	 *  changes. You will be called after the resource is changed.
	 * @see org.eclipse.rse.core.events.ISystemRemoteChangeEvent
	 */
	public void systemRemoteResourceChanged(ISystemRemoteChangeEvent event) {
		int eventType = event.getEventType();
		Object remoteResourceParent = event.getResourceParent();
		Object remoteResource = event.getResource();
		boolean originatedHere;

		if (event instanceof SystemResourceChangeEventUI) {
			Viewer viewer = ((SystemResourceChangeEventUI)event).getOriginatingViewer();
			originatedHere = (viewer==this);
		}
		else if (event instanceof SystemRemoteChangeEvent){
			Object viewer = ((SystemRemoteChangeEvent)event).getOriginatingViewer();
			originatedHere = (viewer==this);
		}
		else {
			originatedHere = false;
		}

		List remoteResourceNames = null;
		if (remoteResource instanceof List) {
			remoteResourceNames = (List) remoteResource;
			remoteResource = remoteResourceNames.get(0);
		}
		// getRemoteResourceAbsoluteName(remoteResourceParent); // DWD may not be necessary
		String remoteResourceName = getRemoteResourceAbsoluteName(remoteResource);
		if (remoteResourceName == null) return;

		ISubSystem ss = getSubSystem(event, remoteResource, remoteResourceParent);

		List filterMatches = null;

		switch (eventType) {
		// --------------------------
		// REMOTE RESOURCE CHANGED...
		// --------------------------
		case ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_CHANGED:
			updatePropertySheet(); // just in case
			break;
		// --------------------------
		// REMOTE RESOURCE CREATED...
		// --------------------------
		case ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_CREATED:
			// we can easily lose our original selection so we need save and restore it if needed
			List prevSelection = null;
			TreeItem parentSelectionItem = null;
			if (originatedHere) {
				prevSelection = getRemoteSelection();
				parentSelectionItem = getSelectedParentItem();
			}

			// when a new remote resource is created, we need to interrogate all filters
			//  within connections to the same hostname, to see if the filter results are
			//  affected by this change. If so, we refresh the filter.
			filterMatches = findAllRemoteItemFilterReferences(remoteResourceName, ss, null);
			ArrayList selectedFilters = null;
			if (filterMatches != null) {
				for (int idx = 0; idx < filterMatches.size(); idx++) {
					FilterMatch match = (FilterMatch) filterMatches.get(idx);
					TreeItem filterItem = match.getTreeItem();
					if (isTreeItemSelected(filterItem)) // if this filter is currently selected, we will lose that selection!
					{
						if (selectedFilters == null) selectedFilters = new ArrayList();
						selectedFilters.add(filterItem);
					}
					
					Object filterRef = filterItem.getData();
					if (filterRef != remoteResourceParent){ // don't refresh it here if the filter reference is the object to refresh anyway
						smartRefresh(new TreeItem[] { filterItem }, null, true);
					}
				}
			}
			// now, refresh all occurrences of the remote parent object.
			refreshRemoteObject(remoteResourceParent, null, false);
			// restore selected filters...
			if (selectedFilters != null) setSelection(selectedFilters);
			// if the create event originated here, then expand the selected node and
			//  select the new resource under it.
			if (originatedHere){
				// first, restore previous selection...
				if (prevSelection != null) selectRemoteObjects(prevSelection, ss, parentSelectionItem);
				
				TreeItem selectedItem = null;
				if (remoteResourceParent instanceof String)
					selectedItem = (TreeItem)findFirstRemoteItemReference((String)remoteResourceParent, ss, parentSelectionItem);
				else
					selectedItem = (TreeItem)findFirstRemoteItemReference(remoteResourceParent, parentSelectionItem);
				
				if (selectedItem == null){
					selectedItem = getFirstSelectedTreeItem();
				}
				
				
				if (selectedItem != null)
				{
					Object data = selectedItem.getData();
					boolean allowExpand = true;
					ISystemViewElementAdapter adapter = getViewAdapter(data);

					if (adapter != null && data instanceof IAdaptable)
					{
						allowExpand = adapter.hasChildren((IAdaptable)data);
					}
					if (allowExpand && !selectedItem.getExpanded()) // if the filter is expanded, then we already refreshed it...
					{
						createChildren(selectedItem);
						selectedItem.setExpanded(true);
					}
					if (adapter.supportsDeferredQueries(ss) && allowExpand) // should not be waiting for a non-query - bug 330973
					{
						final List names = remoteResourceNames;
						final String name = remoteResourceName;
						final ISubSystem subsys = ss;
						final TreeItem item = selectedItem;
						final IWorkbench wb = RSEUIPlugin.getDefault().getWorkbench();

						// do the selection after the query triggered via refreshRemoteObject() completes
						Job job = new Job("select resource") //$NON-NLS-1$
						{
							public IStatus run(IProgressMonitor monitor) {

								boolean notReady = true;
								while (notReady && !wb.isClosing())
								{
									try {
										Thread.sleep(100);
									}
									catch (InterruptedException e){}

									CheckPending checkRunnable = new CheckPending(item);
									wb.getDisplay().syncExec(checkRunnable);
									notReady = checkRunnable.isNotReady();
								}

								wb.getDisplay().asyncExec(new Runnable()
								{
									public void run()
									{
										if (!wb.isClosing() && !item.isDisposed()) {
											if (names != null)
												selectRemoteObjects(names, subsys, item);
											else
												selectRemoteObjects(name, subsys, item);
										}
									}
								});

								return Status.OK_STATUS;
							}
						};
						job.setSystem(true);
						job.schedule();
					}
					else {
						if (remoteResourceNames != null)
							selectRemoteObjects(remoteResourceNames, ss, selectedItem);
						else
							selectRemoteObjects(remoteResourceName, ss, selectedItem);
					}
				}
				//else
				//System.out.println("Hmm, nothing selected");
			}
			break;
		// --------------------------
		// REMOTE RESOURCE DELETED...
		// --------------------------
		case ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_DELETED:
			// delete all existing references to the remote object(s)...
			if (remoteResourceNames != null) {
				for (int idx = 0; idx < remoteResourceNames.size(); idx++)
					deleteRemoteObject(remoteResourceNames.get(idx), ss);
			} else
				deleteRemoteObject(remoteResourceName, ss);

			// now, find all filters that either list this remote resource or list the contents of it,
			// if it is a container... for expediency we only test for the first resource, even if given
			// a list of them...
			filterMatches = findAllRemoteItemFilterReferences(remoteResourceName, ss, null);
			if (filterMatches != null) {
				for (int idx = 0; idx < filterMatches.size(); idx++) {
					FilterMatch match = (FilterMatch) filterMatches.get(idx);
					TreeItem filterItem = match.getTreeItem();
					if (match.listsElement()) {
						// if the filter is expanded, we are ok. If not, we need to flush its memory...
						if (!getExpanded(filterItem)) refresh(filterItem.getData());
					} else // else this filter lists the contents of the deleted container element, so refresh it:
					{
						// if the filter is not expanded, we need to flush its memory...
						if (!getExpanded(filterItem))
							refresh(filterItem.getData());
						else
							// if the filter is expanded, we need to refresh it
							smartRefresh(new TreeItem[] { filterItem }, null, true);
					}
				}
			}

			break;

		// --------------------------
		// REMOTE RESOURCE RENAMED...
		// --------------------------
		case ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_RENAMED:
			// we can easily lose our original selection so we need save and restore it if needed
			prevSelection = null;
			parentSelectionItem = null;
			if (originatedHere) {
				prevSelection = getRemoteSelection();
				parentSelectionItem = getSelectedParentItem();
			}

			// rename all existing references to the remote object...
			renameRemoteObject(remoteResource, event.getOldNames()[0], ss); // assuming only one resource renamed

			// refresh remoteResource if it's a directory
			ISystemViewElementAdapter adapter = getViewAdapter(remoteResource);
			if (remoteResource instanceof IAdaptable && adapter.hasChildren((IAdaptable) remoteResource)) {
				refreshRemoteObject(remoteResource, remoteResource, originatedHere);
			}

			// now, find all filters that list the contents of the OLD name container.
			filterMatches = findAllRemoteItemFilterReferences(event.getOldNames()[0], ss, null); // assuming only one resource renamed
			if (filterMatches != null) {
				for (int idx = 0; idx < filterMatches.size(); idx++) {
					FilterMatch match = (FilterMatch) filterMatches.get(idx);
					TreeItem filterItem = match.getTreeItem();
					if (match.listsElementContents()) // this filter lists the contents of the renamed container element, so refresh it:
					{
						// if the filter is not expanded, we need only flush its memory...
						if (!getExpanded(filterItem))
							refresh(filterItem.getData());
						else
							// the filter is expanded, so refresh its contents. This will likely result in an empty list
							smartRefresh(new TreeItem[] { filterItem }, null, true);
					}
				}
			}
			// now, find all filters that list the contents of the NEW name container.
			filterMatches = findAllRemoteItemFilterReferences(remoteResourceName, ss, null);
			if (filterMatches != null) {
				for (int idx = 0; idx < filterMatches.size(); idx++) {
					FilterMatch match = (FilterMatch) filterMatches.get(idx);
					TreeItem filterItem = match.getTreeItem();
					if (match.listsElementContents()) // this filter lists the contents of the renamed container element, so refresh it:
					{
						// if the filter is not expanded, we need only flush its memory...
						if (!getExpanded(filterItem))
							refresh(filterItem.getData());
						else
							// the filter is expanded, so refresh its contents. This will likely result in an empty list
							smartRefresh(new TreeItem[] { filterItem }, null, true);
					}
				}
			}

			// restore selection
			if (originatedHere && (prevSelection != null)) {
				selectRemoteObjects(prevSelection, ss, parentSelectionItem);
				updatePropertySheet(); // just in case
			}
			break;
		}
	}

	/**
	 * Turn selection into an array of remote object names
	 */
	protected List getRemoteSelection() {
		List prevSelection = null;
		IStructuredSelection selection = (IStructuredSelection) getSelection();
		Iterator i = selection.iterator();
		while (i.hasNext()) {
			Object element = i.next();
			ISystemViewElementAdapter ra = getViewAdapter(element);
			if (ra != null) {
				if (prevSelection == null) prevSelection = new Vector();
				prevSelection.add(ra.getAbsoluteName(element));
			}
		}
		return prevSelection;
	}

	/**
	 * Turn a given remote object reference into a fully qualified absolute name
	 */
	protected String getRemoteResourceAbsoluteName(Object remoteResource) {
		if (remoteResource == null) return null;
		String remoteResourceName = null;
		if (remoteResource instanceof String)
			remoteResourceName = (String) remoteResource;
		else {
			ISystemViewElementAdapter ra = getViewAdapter(remoteResource);
			if (ra == null) return null;
			remoteResourceName = ra.getAbsoluteName(remoteResource);
		}
		return remoteResourceName;
	}

	/**
	 * Deduce the subsystem from the event or remote object
	 */
	protected ISubSystem getSubSystem(ISystemRemoteChangeEvent event, Object remoteResource, Object remoteParent) {
		if (event.getSubSystem() != null) return event.getSubSystem();
		ISubSystem ss = null;
		if ((remoteResource != null) && !(remoteResource instanceof String)) {
			if (remoteResource instanceof List) {
				List v = (List) remoteResource;
				if (v.size() > 0) ss = getSubSystem(event, v.get(0), null);
			} else {
				ISystemViewElementAdapter ra = getViewAdapter(remoteResource);
				if (ra != null) ss = ra.getSubSystem(remoteResource);
			}
		}
		if ((ss == null) && (remoteParent != null) && !(remoteParent instanceof String)) {
			if (remoteParent instanceof List) {
				List v = (List) remoteParent;
				if (v.size() > 0) ss = getSubSystem(event, null, v.get(0));
			} else {
				ISystemViewElementAdapter ra = getViewAdapter(remoteParent);
				if (ra != null) ss = ra.getSubSystem(remoteParent);
			}
		}
		return ss;
	}

	// ------------------------------------
	// MISCELLANEOUS METHODS...
	// ------------------------------------

	/**
	 * Close us!
	 */
	protected void close() {
		previousInputProvider = inputProvider;
		previousInput = getWorkbenchPart().getSite().getPage().getInput();
		ISystemViewInputProvider ip = new SystemEmptyListAPIProviderImpl();
		setInputProvider(ip);
	}

	/**
	 * Test if the given input is our view's input object. This is designed to only
	 * consider instances of ourself in non-primary perspectives.
	 */
	protected boolean affectsInput(Object[] elements) {
		boolean affected = false;
		IWorkbenchPart viewPart = getWorkbenchPart();
		if ((viewPart != null) && (getInput() != RSECorePlugin.getTheSystemRegistry()) && !(getInput() instanceof SystemEmptyListAPIProviderImpl)) {
			for (int idx = 0; !affected && (idx < elements.length); idx++)
				affected = affectsInput(elements[idx]);
		}
		return affected;
	}

	/**
	 * Test if the given input is our view's input object. This is designed to only
	 * consider instances of ourself in non-primary perspectives.
	 */
	protected boolean affectsInput(Object element) {
		boolean affected = false;
		IWorkbenchPart viewPart = getWorkbenchPart();
		if ((viewPart != null) && (getInput() != RSECorePlugin.getTheSystemRegistry()) && !(getInput() instanceof SystemEmptyListAPIProviderImpl)) {

			Object input = viewPart.getSite().getPage().getInput();
			if (input != null) {
				Object referencedElement = getReferencedObject(element);
				Object referencedInput = getReferencedObject(input);
				//System.out.println("TESTING1 " + input + " vs " + element);
				//System.out.println("TESTING2 " + referencedInput + " vs " + referencedElement);
				if ((input == element) || (referencedInput == referencedElement))
					affected = true;
				else {
					while (!affected && (input != null)) {
						input = getElementParent(input);
						if (input != null) {
							referencedInput = getReferencedObject(input);
							//System.out.println("...TESTING1 " + input + " vs " + element);
							//System.out.println("...TESTING2 " + referencedInput + " vs " + referencedElement);
							affected = ((input == element) || (referencedInput == referencedElement));
						}
					}
				}
			}
		}
		return affected;
	}

	protected Object getReferencedObject(Object inputObj) {
		if (inputObj instanceof ISystemFilterPoolReference)
			return ((ISystemFilterPoolReference) inputObj).getReferencedFilterPool();
		else if (inputObj instanceof ISystemFilterReference)
			return ((ISystemFilterReference) inputObj).getReferencedFilter();
		else if (inputObj instanceof ISystemFilterStringReference)
			return ((ISystemFilterStringReference) inputObj).getReferencedFilterString();
		else
			return inputObj;
	}

	protected Object getElementParent(Object inputObj) {
		if (inputObj instanceof IHost)
			return ((IHost) inputObj).getSystemProfile();
		else if (inputObj instanceof ISubSystem)
			return ((ISubSystem) inputObj).getHost();
		else if (inputObj instanceof ISystemFilterPoolReference)
			return ((ISystemFilterPoolReference) inputObj).getProvider(); // will be a subsystem
		else if (inputObj instanceof ISystemFilterPool)
			return ((ISystemFilterPool) inputObj).getProvider(); // will be a subsystem factory. Hmm!
		else if (inputObj instanceof ISystemFilterReference)
			return ((ISystemFilterReference) inputObj).getParent(); // will be filter reference or filter pool reference
		else if (inputObj instanceof ISystemFilter) {
			ISystemFilter filter = (ISystemFilter) inputObj;
			if (filter.getParentFilter() != null)
				return filter.getParentFilter();
			else
				return filter.getParentFilterPool();
		} else if (inputObj instanceof ISystemFilterStringReference)
			return ((ISystemFilterStringReference) inputObj).getParent(); // will be a SystemFilterReference
		else if (inputObj instanceof ISystemFilterString)
			return ((ISystemFilterString) inputObj).getParentSystemFilter();
		else
			return null;
	}

	protected IHost getInputConnection(Object inputObj) {
		if (inputObj instanceof IHost)
			return (IHost) inputObj;
		else if (inputObj instanceof ISubSystem)
			return ((ISubSystem) inputObj).getHost();
		else if (inputObj instanceof ISystemFilterPoolReference) {
			ISubSystem ss = (ISubSystem) ((ISystemFilterPoolReference) inputObj).getProvider(); // will be a subsystem
			if (ss == null) return null;
			return ss.getHost();
		} else if (inputObj instanceof ISystemFilterReference) {
			ISubSystem ss = (ISubSystem) ((ISystemFilterReference) inputObj).getProvider(); // will be a subsystem
			if (ss == null) return null;
			return ss.getHost();
		} else if (inputObj instanceof ISystemFilterStringReference) {
			ISubSystem ss = (ISubSystem) ((ISystemFilterStringReference) inputObj).getProvider(); // will be a subsystem
			if (ss == null) return null;
			return ss.getHost();
		} else
			return null;
	}

	/**
	 * Handy debug method to print a tree item
	 */
	protected String printTreeItem(Item item) {
		if (item == null)
			return ""; //$NON-NLS-1$
		else if (item instanceof TreeItem) {
			TreeItem ti = (TreeItem) item;
			return printTreeItem(ti.getParentItem()) + "/" + ti.getText(); //$NON-NLS-1$
		} else
			return item.toString();
	}

	/**
	 * Delete all occurrences of a given remote object
	 */
	protected void deleteRemoteObject(Object deleteObject, ISubSystem subsystem) {
		List matches = null;
		String oldElementName = null;

		// STEP 1: get the object's remote adapter and subsystem
		if (deleteObject instanceof String)
			oldElementName = (String) deleteObject;
		else {
			ISystemViewElementAdapter rmtAdapter = getViewAdapter(deleteObject);
			if (rmtAdapter == null) return;
			oldElementName = rmtAdapter.getAbsoluteName(deleteObject);
			subsystem = rmtAdapter.getSubSystem(deleteObject);
		}
		// STEP 2: find all references to the object
		matches = findAllRemoteItemReferences(oldElementName, deleteObject, subsystem, matches);
		if (matches == null) {
			//System.out.println("matches is null");
			return;
		}

		boolean wasSelected = false;
		Item parentItem = null;

		boolean dupes = false;
		Object prevData = null;
		for (int idx = 0; !dupes && (idx < matches.size()); idx++) {
			Item match = (Item) matches.get(idx);
			if ((match instanceof TreeItem) && !((TreeItem) match).isDisposed()) {
				if (match.getData() == prevData)
					dupes = true;
				else
					prevData = match.getData();
			}
		}
		//System.out.println("matches size = " + matches.size() + ", any binary duplicates? " + dupes);

		List toRemove = new ArrayList();
		// STEP 3: process all references to the object
		for (int idx = 0; idx < matches.size(); idx++) {
			Item match = (Item) matches.get(idx);
			//System.out.println("...match " + idx + ": TreeItem? " + (match instanceof TreeItem) + ", disposed? " + ((TreeItem)match).isDisposed());
			// a reference to this remote object
			if ((match instanceof TreeItem) && !((TreeItem) match).isDisposed()) {
				((TreeItem) match).getParentItem(); // DWD may not be necessary
				//Object data = match.getData();
				if (!wasSelected) {
					//wasSelected = isSelectedOrChildSelected(data);
					wasSelected = isTreeItemSelectedOrChildSelected(match);
					if (wasSelected) {
						clearSelection();
						parentItem = ((TreeItem) match).getParentItem();
						//System.out.println("...current item was selected");
					}
				}
				if (dupes) // defect 46818
				{ // if there are multiple references to the same binary object, ...
					//System.out.println(".....calling refresh(data) on this match");
					/*
					 if (pItem!=null)
					 smartRefresh(new TreeItem[] {pItem}); // just refresh the parent node
					 else
					 refreshAll();
					 */
					disassociate(match);
					match.dispose();
				} else {
					toRemove.add(match);
					//System.out.println(".....calling remove(data) on this match");
					//remove(data); // remove this item from the tree
				}
			}
		}

		// do the remove now
		for (int i = 0; i < toRemove.size(); i++)
		{
			Item childItem = (Item)toRemove.get(i);
			disassociate(childItem);	
			Widget wid = findItem(deleteObject);
			if (wid != null){
				// make sure all associated items are unmapped
				unmapElement(deleteObject);				
			}
			
			childItem.dispose();
		}

		// STEP 4: if we removed a selected item, select its parent
		if (wasSelected && (parentItem != null) && (parentItem instanceof TreeItem) && (parentItem.getData() != null)) {
			//System.out.println("Resetting selection to parent");
			setSelection(new StructuredSelection(parentItem.getData()), true);
		}
		return;
	}

	/**
	 * Rename a remote object. Renames all references to it currently displayed in this tree.
	 */
	protected void renameRemoteObject(Object renameObject, String oldElementName, ISubSystem subsystem) {
		String[] properties = new String[1];
		properties[0] = IBasicPropertyConstants.P_TEXT;

		// STEP 0: do we have the physical remote object that has been renamed? If so, update it directly
		/*
		 Item item = (Item)findItem(renameObject);
		 if (item != null)
		 {
		 update(renameObject, properties); // for refreshing non-structural properties in viewer when model changes
		 if (item instanceof TreeItem)
		 smartRefresh(new TreeItem[] {(TreeItem)item}); // we update the kids because they typically store references to their parent
		 }
		 */

		List matches = null;

		// STEP 1: get the object's remote adapter and subsystem
		String newElementName = null;
		ISystemViewElementAdapter rmtAdapter = null;
		if (renameObject instanceof String) {
			//FIXME How to get the adapter based on the String name?
			newElementName = (String)renameObject;
		} else {
			rmtAdapter = getViewAdapter(renameObject);
			subsystem = rmtAdapter.getSubSystem(renameObject);
			newElementName = rmtAdapter.getName(renameObject);
		}

		// STEP 2: find all references to the old name object
		matches = findAllRemoteItemReferences(oldElementName, renameObject, subsystem, matches);
		if (matches == null) return;

		TreeItem[] selected = getTree().getSelection();
		getTree().deselectAll();

		boolean refresh = false;
		// STEP 3: process all references to the old name object
		for (int idx = 0; idx < matches.size(); idx++) {
			Item match = (Item) matches.get(idx);
			// a reference to this remote object
			if ((match instanceof TreeItem) && !((TreeItem) match).isDisposed())
			{
				Object data = match.getData();

				ISystemRemoteElementAdapter	remoteAdapter = (ISystemRemoteElementAdapter)((IAdaptable)data).getAdapter(ISystemRemoteElementAdapter.class);

				if (data != renameObject) // not a binary match
				{
					if (remoteAdapter != null)
					{
						// DKM - would be nice to eventually get rid of remote element adapter
						refresh = remoteAdapter.refreshRemoteObject(data, renameObject); // old, new
					}
				} else {
					refresh = true;
				}

				// rename explicitly here (since internalUpdate doesn't seem to have an effect
				match.setText(newElementName);
				//updateItem(match, renameObject);
				internalUpdate(match, data, properties);

				//update(data, properties); // for refreshing non-structural properties in viewer when model changes
				//System.out.println("Match found. refresh required? " + refresh);
//				if (refresh)
				//refreshRemoteObject(data,null,false);
	//				smartRefresh(new TreeItem[] { (TreeItem) match });
			}
		}

		if (refresh)
		{
			// causes duplicates to appear when there are more than one rename objects
			//smartRefresh((TreeItem[])matches.toArray(new TreeItem[matches.size()]));
			getTree().setSelection(selected);
		}

		// STEP 4: update property sheet, just in case.
		updatePropertySheet();

		return;
	}

	/**
	 * Update properties of remote object. Update all references to this object
	 */
	protected void updateRemoteObjectProperties(Object remoteObject) {
		List matches = new Vector();

		// STEP 1: get the object's remote adapter and subsystem
		ISystemRemoteElementAdapter	rmtAdapter = (ISystemRemoteElementAdapter)((IAdaptable)remoteObject).getAdapter(ISystemRemoteElementAdapter.class);

		ISubSystem subsystem = rmtAdapter.getSubSystem(remoteObject);

		// STEP 2: find all references to the object
		String oldElementName = rmtAdapter.getAbsoluteName(remoteObject);
		findAllRemoteItemReferences(oldElementName, remoteObject, subsystem, matches);

		// STEP 3: process all references to the object
		String[] allProps = { IBasicPropertyConstants.P_TEXT, IBasicPropertyConstants.P_IMAGE };
		for (int idx = 0; idx < matches.size(); idx++) {
			Item match = (Item) matches.get(idx);
			// a reference to this remote object
			if ((match instanceof TreeItem) && !((TreeItem) match).isDisposed()) {
				Object data = match.getData();
				if (data == remoteObject) // same binary object as given?
					update(data, allProps); // for refreshing non-structural properties in viewer when model changes
				else // match by name
				{


					rmtAdapter.refreshRemoteObject(data, remoteObject); // old, new
					update(data, allProps);
				}
			}
		}

		// STEP 4: update the property sheet in case we changed properties of first selected item
		ISelection selection = getSelection();
		if (selection instanceof IStructuredSelection){
			Object sel = ((IStructuredSelection)selection).getFirstElement();
			if (remoteObject.equals(sel)){
				updatePropertySheet(true);
			}
		}
		return;
	}

	/**
	 * Refresh contents of remote container. Refreshes all references to this container including filters that
	 *  display the contents of this container.
	 * @param remoteObject - either an actual remote object, or the absolute name of a remote object
	 * @param toSelect - the child object to select after refreshing the given object. This will force the
	 *   object to be expanded, and then select this object which can be a remote object or absolute name of a
	 *   remote object. To simply force an expand of the remote object, without bothering to select a child,
	 *   pass an instance of SystemViewDummyObject.
	 * @return true if a refresh done, false if given a non-remote object.
	 */
	protected boolean refreshRemoteObject(Object remoteObject, Object toSelect, boolean originatedHere) {
		if (remoteObject == null) return false;

		// STEP 1: get the object's remote adapter and subsystem, or use its name if only given that
		ISystemViewElementAdapter rmtAdapter = null;
		ISubSystem subsystem = null;
		String oldElementName = null;
		boolean doesDeferredQueries = false;
		if (!(remoteObject instanceof String)) {
			rmtAdapter = getViewAdapter(remoteObject);
			if (rmtAdapter == null) return false;
			subsystem = rmtAdapter.getSubSystem(remoteObject);
			assert subsystem!=null : "EVENT_REFRESH_REMOTE outside subsystem"; //$NON-NLS-1$
			oldElementName = rmtAdapter.getAbsoluteName(remoteObject);
			doesDeferredQueries = rmtAdapter.supportsDeferredQueries(subsystem);
		} else
			oldElementName = (String) remoteObject;

		List matches = new Vector();
		// STEP 2: find all references to the object
		findAllRemoteItemReferences(oldElementName, remoteObject, subsystem, matches);
		if (matches.size()>0 && remoteObject instanceof String) {
			//TODO one String may reference multiple different context objects, so we should really iterate over all matches here
			//See javadoc of findAllRemoteItemReferences
			remoteObject = getFirstRemoteObject(matches);
			rmtAdapter = getViewAdapter(remoteObject);
			assert rmtAdapter!=null; //cannot happen because matches were result of String query
			if (rmtAdapter!=null) {
				subsystem = rmtAdapter.getSubSystem(remoteObject);
				assert subsystem!=null : "EVENT_REFRESH_REMOTE outside subsystem"; //$NON-NLS-1$
				doesDeferredQueries = rmtAdapter.supportsDeferredQueries(subsystem);
			}
		}

		if (remoteObject instanceof ISystemContainer) {
			((ISystemContainer) remoteObject).markStale(true);
		}

		// STEP 3: process all references to the object
		boolean firstSelection = true;
		for (int idx = 0; idx < matches.size(); idx++) {
			Widget match = (Widget) matches.get(idx);
			// a reference to this remote object
			if ((match instanceof TreeItem) && !((TreeItem) match).isDisposed()) {
				TreeItem matchedItem = (TreeItem)match;
				Object data = matchedItem.getData();
				boolean wasExpanded = matchedItem.getExpanded();



				smartRefresh(new TreeItem[] { matchedItem }); // refresh the remote object
				if (firstSelection && // for now, we just select the first binary occurrence we find
						(data == remoteObject)) // same binary object as given?
				{
					firstSelection = false;
					if ((toSelect != null) && originatedHere)
					{
						boolean allowExpand = true;
						if (rmtAdapter != null && data instanceof IAdaptable)
						{
							allowExpand = rmtAdapter.hasChildren((IAdaptable)data);
						}
						if (allowExpand && wasExpanded && !getExpanded(matchedItem)) // assume if callers wants to select kids that they want to expand parent
						{
							createChildren(matchedItem);
							setExpanded(matchedItem, true);
						}

						// todo: handle cumulative selections.
						// STEP 4: If requested, select the kids in the newly refreshed object.
						// If the same binary object appears multiple times, select the kids in the first occurrence.
						//  ... what else to do?
						// DKM - added wasExpanded check since we don't want to expand something that isn't already expanded
						if (!doesDeferredQueries && wasExpanded && !(toSelect instanceof SystemViewDummyObject)) {
							//selecting remote objects makes only sense if not deferred, because
							//in the deferred case the objects will be retrieved in a separate job.
							selectRemoteObjects(toSelect, null, match); // select the given kids in this parent
						}
					}
				}
			}
		}
		return true;
	}

	protected void doUpdateItem(final Item item, Object element)
	{
		// adding this because base eclipse version isn't renaming properly on duplicates
		ISystemViewElementAdapter adapter = getViewAdapter(element);
		if (adapter != null && item != null && !item.isDisposed())
		{
			String oldText = item.getText();
			String newText = adapter.getText(element);
			if (oldText == null || !oldText.equals(newText))
			{
				if (newText != null){
					item.setText(newText);
				}
				else {
					SystemBasePlugin.logInfo("SystemView.doUpdateItem() - text for " + element + " is null!");  //$NON-NLS-1$//$NON-NLS-2$
				}
			}
		}

		super.doUpdateItem(item, element);
	}

	/**
	 * Given the result TreeItems of findAllRemoteItemReferences,
	 * return the Data of the first Item.
	 */
	protected Object getFirstRemoteObject(List matches) {
		if ((matches == null) || (matches.size() == 0)) return null;
		Object firstRemote = matches.get(0);
		if (firstRemote != null) firstRemote = ((Item) firstRemote).getData();
		return firstRemote;
	}

	/**
	 * Refreshes the tree starting at the given widget.
	 *
	 * @param widget the widget
	 * @param element the element
	 * @param doStruct <code>true</code> if structural changes are to be picked up,
	 *   and <code>false</code> if only label provider changes are of interest
	 */
	protected void ourInternalRefresh(Widget widget, Object element, boolean doStruct, boolean forceRemote) {
		final Widget fWidget = widget;
		final Object fElement = element;
		final boolean fDoStruct = doStruct;

		// we have to take special care if one of our kids are selected and it is a remote object...
		if (forceRemote || (isSelectionRemote() && isTreeItemSelectedOrChildSelected(widget))) {
			if (!isTreeItemSelected(widget)) // it is one of our kids that is selected
			{
				//MOB cannot see why the selection is cleared here
				//clearSelection(); // there is nothing much else we can do. Calling code will restore it anyway hopefully
				doOurInternalRefresh(fWidget, fElement, fDoStruct, true);
			} else // it is us that is selected. This might be a refresh selected operation. TreeItem address won't change
			{
				doOurInternalRefresh(fWidget, fElement, fDoStruct, true);
			}
		} else {
			preservingSelection(new Runnable() {
				public void run() {
					doOurInternalRefresh(fWidget, fElement, fDoStruct, true);
				}
			});
		}
	}

	protected boolean isSelectionRemote() {
		ISelection s = getSelection();
		if (s instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) s;
			Iterator it = ss.iterator();
			while (it.hasNext()) {
				if (getRemoteAdapter(it.next()) != null) {
					return true;
				}
			}
		}
		return false;
	}

	protected void doOurInternalRefresh(Widget widget, Object element, boolean doStruct, boolean firstCall) {
		if (widget instanceof Item) {
		    Object data = element;
            if (data instanceof IContextObject) {
                data = ((IContextObject)data).getModelObject();
            }
			if (doStruct) {
				updatePlus((Item) widget, data);
			}
			updateItem(widget, data);
		}

		/* may cause performance issue in bug 238363
		 * calling refresh on each child item means that we'll be doing update on it
		 * which ends up making it a pending decoration change since it's an update on an
		 * item that already has text
		// recurse
		Item[] children = getChildren(widget);
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				Widget item = children[i];
				Object data = item.getData();
				if (data instanceof IAdaptable && item instanceof TreeItem) {
					data = getContextObject((TreeItem)item);
				}
				if (data != null) doOurInternalRefresh(item, data, doStruct, false);
			}
		}
*/

		if (firstCall) {

			internalRefresh(element);
		}
	}

	/**
	 * Override of internalRefreshStruct so that we can account for context
	 */
	protected void internalRefreshStruct(Widget widget, Object element, boolean updateLabels) {
		if (widget instanceof TreeItem)
		{
				ContextObjectWithViewer contextObject = getContextObject((TreeItem)widget);
				IRSECallback callback = null;

				ArrayList expandedChildren = new ArrayList();
				if (widget instanceof TreeItem){
					TreeItem currItem = (TreeItem)widget;
					gatherExpandedChildren(currItem, currItem, expandedChildren);
				}

				if (expandedChildren.size() > 0){
					callback = new ExpandRemoteObjects(expandedChildren);
					contextObject.setCallback(callback);
				}

				internalRSERefreshStruct(widget, contextObject, updateLabels);
		}
		else
		{
			internalRSERefreshStruct(widget, element, updateLabels);
		}
	}

	/**
	 * This is used during RSE refresh - otherwise filters aren't applied during refresh
	 * @param widget the widget to refresh
	 * @param element the element to refresh
	 * @param updateLabels whether to update labels (ends up being ignored and having the value of true)
	 */
	private void internalRSERefreshStruct(Widget widget, Object element, boolean updateLabels)
	{
		updateChildren(widget, element, null); // DKM - using deprecated API because it's the only way to call updateChildren
											   // need a better solution for this in the future (the proper updateChildren is private)
		Item[] children = getChildren(widget);
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				Widget item = children[i];
				Object data = item.getData();
                if (data instanceof IAdaptable && item instanceof TreeItem) {
					data = getContextObject((TreeItem)item);
                }
				if (data != null) {
					internalRSERefreshStruct(item, data, updateLabels);
				}
			}
		}

	}

	protected Object[] getRawChildren(Widget w) {
		Object parent = w.getData();

		if (parent.equals(getRoot())) return super.getRawChildren(parent);
		Object[] result = ((ITreeContentProvider) getContentProvider()).getChildren(parent);
		if (result != null) return result;

		return new Object[0];
	}

	/*
	 protected void preservingSelection(Runnable updateCode)
	 {
	 super.preservingSelection(updateCode);
	 System.out.println("After preservingSelection: new selection = "+getFirstSelectionName(getSelection()));
	 }
	 protected void handleInvalidSelection(ISelection invalidSelection, ISelection newSelection)
	 {
	 System.out.println("Inside handleInvalidSelection: old = "+getFirstSelectionName(invalidSelection)+", new = "+getFirstSelectionName(newSelection));
	 updateSelection(newSelection);
	 }
	 */
	protected String getFirstSelectionName(ISelection s) {
		if ((s != null) && (s instanceof IStructuredSelection)) {
			IStructuredSelection ss = (IStructuredSelection) s;
			Object firstSel = ss.getFirstElement();
			String name = null;
			if (firstSel != null) {
				ISystemRemoteElementAdapter ra = getRemoteAdapter(firstSel);
				if (ra != null)
					name = ra.getAbsoluteName(firstSel);
				else
					name = getViewAdapter(firstSel).getName(firstSel);
			}
			return name;
		} else
			return null;
	}

	/**
	 * Expand a remote object within the tree. Must be given its parent element within the tree,
	 *   in order to uniquely find it. If not given this, we expand the first occurrence we find!
	 * @param remoteObject - either a remote object or a remote object absolute name
	 * @param subsystem - the subsystem that owns the remote objects, to optimize searches.
	 * @param parentObject - the parent that owns the remote objects, to optimize searches. Can
	 *          be an object or the absolute name of a remote object.
	 * @return the tree item of the remote object if found and expanded, else null
	 */
	public Item expandRemoteObject(Object remoteObject, ISubSystem subsystem, Object parentObject) {
		// given the parent? Should be easy
		Item remoteItem = null;
		if (parentObject != null) {
			Item parentItem = null;
			if (parentObject instanceof Item)
				parentItem = (Item) parentObject;
			else if (parentObject instanceof String) // given absolute name of remote object
				parentItem = findFirstRemoteItemReference((String) parentObject, subsystem, (Item) null); // search all roots for the parent
			else // given actual remote object
			{
				ISystemViewElementAdapter ra = getViewAdapter(parentObject);
				if (ra != null) {
					if (subsystem == null) subsystem = ra.getSubSystem(parentObject);
					parentItem = findFirstRemoteItemReference(ra.getAbsoluteName(parentObject), subsystem, (Item) null); // search all roots for the parent
				} else // else parent is not a remote object. Probably its a filter
				{
					Widget parentWidget = findItem(parentObject);
					if (parentWidget instanceof Item) parentItem = (Item) parentWidget;
				}
			}
			// ok, we have the parent item! Hopefully!
			if (remoteObject instanceof String)
				remoteItem = findFirstRemoteItemReference((String) remoteObject, subsystem, parentItem);
			else
				remoteItem = findFirstRemoteItemReference(remoteObject, parentItem);
			if (remoteItem == null) return null;
			setExpandedState(remoteItem.getData(), true);
		} else // not given a parent to refine search with. Better have a subsystem!!
		{
			if (remoteObject instanceof String)
				remoteItem = findFirstRemoteItemReference((String) remoteObject, subsystem, (Item) null);
			else {
				ISystemViewElementAdapter ra = getViewAdapter(remoteObject);
				if (ra != null) {
					if (subsystem == null) subsystem = ra.getSubSystem(remoteObject);
					remoteItem = findFirstRemoteItemReference(ra.getAbsoluteName(remoteObject), subsystem, (Item) null);
				}
			}
			if (remoteItem == null) return null;
			setExpandedState(remoteItem.getData(), true);
		}
		return remoteItem;
	}

	/**
	 * Select a remote object or objects given the parent remote object (can be null) and subsystem (can be null)
	 * @param src - either a remote object, a remote object absolute name, or a List of remote objects or remote object absolute names
	 * @param subsystem - the subsystem that owns the remote objects, to optimize searches.
	 * @param parentObject - the parent that owns the remote objects, to optimize searches.
	 * @return true if found and selected
	 */
	public boolean selectRemoteObjects(Object src, ISubSystem subsystem, Object parentObject) {
		//String parentName = null;
		// given a parent object? That makes it easy...
		if (parentObject != null) {
			ISystemViewElementAdapter ra = getViewAdapter(parentObject);
			if (ra != null) {
				//parentName = ra.getAbsoluteName(parentObject);
				if (subsystem == null) subsystem = ra.getSubSystem(parentObject);
				Item parentItem = findFirstRemoteItemReference(parentObject, (Item) null); // search all roots for the parent
				return selectRemoteObjects(src, subsystem, parentItem);
			} else // else parent is not a remote object. Probably its a filter
			{
				Item parentItem = null;
				if (parentObject instanceof Item)
					parentItem = (Item) parentObject;
				else {
					Widget parentWidget = findItem(parentObject);
					if (parentWidget instanceof Item) parentItem = (Item) parentWidget;
				}
				if (parentItem != null)
					return selectRemoteObjects(src, (ISubSystem) null, parentItem);
				else
					return false;
			}
		} else
			//return selectRemoteObjects(src, (SubSystem)null, (Item)null); // Phil test
			return selectRemoteObjects(src, subsystem, (Item) null);
	}

	/**
	 * Select a remote object or objects given the parent remote object (can be null) and subsystem (can be null) and parent TreeItem to
	 *  start the search at (can be null)
	 * @param src - either a remote object, a remote object absolute name, or a List of remote objects or remote object absolute names
	 * @param subsystem - the subsystem that owns the remote objects, to optimize searches.
	 * @param parentItem - the parent at which to start the search to find the remote objects. Else, starts at the roots.
	 * @return true if found and selected
	 */
	protected boolean selectRemoteObjects(Object src, ISubSystem subsystem, Item parentItem) {
		clearSelection();
		Item selItem = null;

		if (parentItem != null && parentItem.isDisposed()) {
			return false;
		}

		if ((parentItem != null) && !getExpanded(parentItem)){
			// don't expand objects that don't have children - bug 330973
			Object parentData = parentItem.getData();
			boolean expandable = getViewAdapter(parentData).hasChildren((IAdaptable)parentData);
			if (expandable)
				setExpandedState(parentItem.getData(), true);				
		}

		//System.out.println("SELECT_REMOTE: PARENT = " + parent + ", PARENTITEM = " + parentItem);
		if (src instanceof List) {
			//String elementName = null;
			List setList = (List)src;
			ArrayList selItems = new ArrayList();
			// our goal here is to turn the List of names or remote objects into a collection of
			// actual TreeItems we matched them on...
			for (int idx = 0; idx < setList.size(); idx++) {
				Object o = setList.get(idx);
				//elementName = null;
				if (o instanceof String)
					selItem = findFirstRemoteItemReference((String) o, subsystem, parentItem);
				else
					selItem = findFirstRemoteItemReference(o, parentItem);

				if (selItem != null) {
					selItems.add(selItem);
					// when selecting multiple items, we optimize by assuming they have the same parent...
					if ((parentItem == null) && (selItem instanceof TreeItem)) parentItem = ((TreeItem) selItem).getParentItem();
				}
			}
			if (selItems.size() > 0) {		
				List dataList = new ArrayList();
				for (int i = 0; i < selItems.size(); i++){
					Item item = (Item)selItems.get(i);
					if (item != null){
						dataList.add(item.getData());
					}
				}
				IStructuredSelection sel = new StructuredSelection(dataList);				
				setSelection(sel);
				updatePropertySheet();
				return true;
			}
		} else {
			if (src instanceof String)
				//selItem = (Item)findFirstRemoteItemReference((String)src, (SubSystem)null, parentItem); Phil test
				selItem = findFirstRemoteItemReference((String) src, subsystem, parentItem);
			else
				selItem = findFirstRemoteItemReference(src, parentItem);

			if (selItem != null) {

				IStructuredSelection sel = new StructuredSelection(selItem.getData());
				setSelection(sel);
				updatePropertySheet();
				return true;
			}
		}
		return false;
	}

	/**
	 * Refresh the whole tree. We have special code to reselect remote objects after the refresh
	 */
	public void refreshAll() {
		IStructuredSelection selected = (IStructuredSelection) getSelection();
		Iterator i = selected.iterator();
		Object parentElement = null;
		List selectedRemoteObjects = new Vector();
		Widget parentItem = null;
		ISubSystem ss = null;
		while (i.hasNext()) {
			Object element = i.next();
			if (parentElement == null) {
				Item item = (Item) findItem(element);
				if (item != null) {
					parentItem = getParentItem(item);
				}
				if ((parentItem != null) && (parentItem instanceof Item)) parentElement = ((Item) parentItem).getData();
			}
			if (getViewAdapter(element) != null) {
				selectedRemoteObjects.add(element);
				if (ss == null) ss = getViewAdapter(element).getSubSystem(element);
			}
		}

		//super.refresh();
		smartRefresh((Object) null, (selectedRemoteObjects.size() > 0));

		if (selectedRemoteObjects.size() > 0) {
			selectRemoteObjects(selectedRemoteObjects, ss, parentElement);
		}
	}

	/**
	 * Do an intelligent refresh of an expanded item. The inherited algorithm for refresh is stupid,
	 * in that it re-expands children based on their original ordinal position which can change after a
	 * refresh, resulting in the wrong children being expanded. Currently this only truly comes to light
	 * for remote objects, where refresh really can change the resulting list and hence each child's
	 * ordinal position. So, to be safe we only override the inherited algorithm if any nested child
	 * is a remote object
	 */
	protected void smartRefresh(TreeItem[] itemsToRefresh) {
		smartRefresh(itemsToRefresh, null, false);
	}

	protected void smartRefresh(TreeItem[] itemsToRefresh, ArrayList expandedChildren, boolean forceRemote) {
		areAnyRemote = false; // set in ExpandedItem constructor
		boolean fullRefresh = false;
		// for each selected tree item gather a list of expanded child nodes...
		if (expandedChildren == null)
			expandedChildren = new ArrayList();
		else
			fullRefresh = true;
		boolean[] wasExpanded = new boolean[itemsToRefresh.length];
		for (int idx = 0; idx < itemsToRefresh.length; idx++) {
			TreeItem currItem = itemsToRefresh[idx];
			if (currItem.getExpanded()) {
				// ...if this selected item is expanded, recursively gather up all its expanded descendents
				Object data = currItem.getData();
				ISystemViewElementAdapter adapter = null;
				if (data != null) adapter = getViewAdapter(data);
				if (adapter != null && adapter.isPromptable(data))
					setExpandedState(data, false); // collapse temp expansion of prompts
				else {
					//expandedChildren.add(new ExpandedItem(currItem)); we don't need special processing for given items themselves as they will not be refreshed, only their kids
					gatherExpandedChildren((fullRefresh ? null : currItem), currItem, expandedChildren);
					wasExpanded[idx] = true;
				}
			} else {
				wasExpanded[idx] = false;
			}
		}
		// ok, we have found all expanded descendants of all selected items.
		// If none of the expanded sub-nodes are remote simply use the inherited algorithm for refresh
		if (!areAnyRemote) {
			for (int idx = 0; idx < itemsToRefresh.length; idx++)
				//ourInternalRefresh(itemsToRefresh[idx], itemsToRefresh[idx].getData(), wasExpanded[idx]);
				ourInternalRefresh(itemsToRefresh[idx], itemsToRefresh[idx].getData(), true, forceRemote); // defect 42321
			return;
		}
		getControl().setRedraw(false);
		// If any selected nodes are remote use our own algorithm:
		// 1. collapse each given node and refresh it to remove the children from memory, then
		//    expand it again. It doesn't matter if it is remote or not since its own memory
		//    address (absolute name) won't change, only that of its children.
		for (int idx = 0; idx < itemsToRefresh.length; idx++) {
			TreeItem currItem = itemsToRefresh[idx];
			setExpanded(currItem, false); // collapse node
			ourInternalRefresh(currItem, currItem.getData(), true, true); // dispose of children, update plus

			if (wasExpanded[idx]) {



				IRSECallback callback = new ExpandRemoteObjects(expandedChildren);

				createChildren(currItem, callback); // re-expand
				currItem.setExpanded(true);
			} else // hmm, item was not expanded so just flush its memory
			{

			}
		}

		// for non-deferred queries

		// 2. expand each previously expanded sub-node, recursively
		for (int idx = 0; idx < expandedChildren.size(); idx++) {
			ExpandedItem itemToExpand = (ExpandedItem) expandedChildren.get(idx);
			if (itemToExpand.isRemote()) {
				// find remote item based on its original name and unchanged root parent
				Item item = null;

				// for deferred queries, we handle this via a callback
				item = findFirstRemoteItemReference(itemToExpand.remoteName, itemToExpand.subsystem, itemToExpand.parentItem);

				// if found, re-expand it
				if (item != null) {
					//setExpanded(item, true);
					createChildren(item);
					((TreeItem) item).setExpanded(true);
					if (debug) System.out.println("Re-Expanded RemoteItem: " + itemToExpand.remoteName); //$NON-NLS-1$
				} else if (debug) System.out.println("Re-Expand of RemoteItem '" + itemToExpand.remoteName + "' failed. Not found"); //$NON-NLS-1$ //$NON-NLS-2$
			} else if (itemToExpand.data!=null) {
				ExpandedItem nextItemToExpand = null;
				if (idx + 1< expandedChildren.size()){
					nextItemToExpand = (ExpandedItem)expandedChildren.get(idx + 1);
				}
				// this is to address bug 241744
				if (nextItemToExpand != null && nextItemToExpand.isRemote()){	// if the next item to expand is remote
					List remainingExpandedChildren = new ArrayList(expandedChildren.size() - idx);
					for (int e = idx + 1; e < expandedChildren.size(); e++){
						remainingExpandedChildren.add(expandedChildren.get(e));
					}

					Item item = findFirstRemoteItemReference(itemToExpand.remoteName, itemToExpand.subsystem, itemToExpand.parentItem);
					if (item != null){
						IRSECallback callback = new ExpandRemoteObjects(remainingExpandedChildren);
						createChildren(item, callback);
						((TreeItem) item).setExpanded(true);
					}
				}
				else { // original approach
					setExpandedState(itemToExpand.data, true);
				}
				if (debug) System.out.println("Re-Expanded non-remote Item: " + itemToExpand.data); //$NON-NLS-1$
			}
		}

		getControl().setRedraw(true);
	}

	public void refreshRemote(Object element)
	{
		smartRefresh(element, true);
	}



	protected ArrayList getExpandedChildren(TreeItem[] roots){
		ArrayList expandedChildren = new ArrayList();
		for (int idx = 0; idx < roots.length; idx++) {
			TreeItem currItem = roots[idx];
			if (currItem.getExpanded()) {
				Object data = currItem.getData();
				ISystemViewElementAdapter adapter = null;
				if (data != null) adapter = getViewAdapter(data);
				if(adapter != null && adapter.isPromptable(data)) {
					setExpandedState(data, false);
				}
				else {
					expandedChildren.add(new ExpandedItem(null, currItem));
				}
			}
		}
		return expandedChildren;
	}

	/**
	 * Do an intelligent refresh of the given element. Can be null for full refresh
	 */
	protected void smartRefresh(Object element, boolean forceRemote) {
		if ((element == null) || (element == getInput())) {
			// fullRefresh
			Tree tree = getTree();
			TreeItem[] roots = tree.getItems();
			boolean anyExpanded = false;
			areAnyRemote = false; // set in ExpandedItem constructor
			ArrayList expandedChildren = getExpandedChildren(roots);
			if (expandedChildren.size() > 0)
				anyExpanded = true;

			if (!anyExpanded)
				super.refresh();
			else {
				internalRefresh(getInput());
				roots = tree.getItems(); // re-query roots
				smartRefresh(roots, expandedChildren, forceRemote);
			}
		} else if (getViewAdapter(element) != null) {

			Item item = null;
			if (element instanceof String) {
				item = findFirstRemoteItemReference((String) element, (ISubSystem) null, (Item) null);
				if (item != null) {
					smartRefresh(new TreeItem[] { (TreeItem) item });
				}
			} else {
				ISystemViewElementAdapter adapter = getViewAdapter(element);
				// DKM - taken out as per defect 174295
				//String elementName = adapter.getName(element);

				String searchString = adapter.getAbsoluteName(element);
				ISubSystem subSystem = adapter.getSubSystem(element);

				List matches = new Vector();
				findAllRemoteItemReferences(searchString, element, subSystem, matches);

				// DKM - taken out as per defect 174295
				//findAllRemoteItemReferences(elementName, element, subSystem, matches);
				if (matches.size() > 0) {
					for (int i = 0; i < matches.size(); i++) {
						Item match = (Item) matches.get(i);
						if ((match instanceof TreeItem) && !((TreeItem) match).isDisposed()) {
							smartRefresh(new TreeItem[] { (TreeItem) match });
						}
					}
				}
			}

			/*
			 Item item = null;
			 if (element instanceof String)
			 item = findFirstRemoteItemReference((String)element, (SubSystem)null, (Item)null);
			 else
			 item = findFirstRemoteItemReference(element, (Item)null);
			 if (item != null)
			 smartRefresh(new TreeItem[] {(TreeItem)item});

			 */
		} else {
			Item item = (Item) findItem(element);
			//System.out.println("Inside SV smartRefresh for "+element+". Item found? " + (item!=null));
			if (item != null) smartRefresh(new TreeItem[] { (TreeItem) item });
		}
	}

	class ExpandedItem {
		//private TreeItem item; //not needed since we'll get the item by absolute name
		                         //For mixed remote/non-remote selections we may want a TreePath
		TreeItem parentItem;
		Object data;
		String remoteName;
		ISubSystem subsystem;
		private ISystemRemoteElementAdapter remoteAdapter;

		ExpandedItem(TreeItem parentItem, TreeItem item) {
			this.parentItem = parentItem;
			this.data = item.getData();
			if (data != null) {
				remoteAdapter = getRemoteAdapter(data);
				if (remoteAdapter != null) {
					remoteName = remoteAdapter.getAbsoluteName(data);
					subsystem = remoteAdapter.getSubSystem(data);
					areAnyRemote = true;
					if (debug) System.out.println("ExpandedRemoteItem added. remoteName = " + remoteName); //$NON-NLS-1$
				} else {
					ISystemViewElementAdapter adapter = getViewAdapter(data);
					if (adapter != null) {
						remoteName = adapter.getAbsoluteName(data);
						subsystem = adapter.getSubSystem(data);
					}
					if (debug) System.out.println("ExpandedItem added. Data = " + data); //$NON-NLS-1$
				}
			} else if (debug) System.out.println("ExpandedItem added. Data = null"); //$NON-NLS-1$
		}

		boolean isRemote() {
			return (remoteAdapter != null);
		}
	}

	/**
	 * Gather up all expanded children of the given tree item into a list
	 * that can be used later to reexpand.
	 * @param parentItem The root parent which will not be refreshed itself
	 *    (only its kids) and hence will remain valid after refresh.
	 *    In a full refresh this will be null.
	 * @param startingItem The starting item for this search.
	 *    Usually same as parentItem, but changes via recursion
	 * @param listToPopulate An array list that will be populated
	 *    with instances of our inner class ExpandedItem
	 */
	protected void gatherExpandedChildren(TreeItem parentItem, TreeItem startingItem, ArrayList listToPopulate) {
		TreeItem[] itemChildren = startingItem.getItems();
		for (int idx = 0; idx < itemChildren.length; idx++) {
			TreeItem currChild = itemChildren[idx];
			if (currChild.getExpanded()) {
				Object data = currChild.getData();
				ISystemViewElementAdapter adapter = null;
				if (data != null) adapter = getViewAdapter(data);
				if (adapter != null && adapter.isPromptable(data)) {
					setExpandedState(data, false);
				} else {
					listToPopulate.add(new ExpandedItem(parentItem, currChild));
					gatherExpandedChildren(parentItem, currChild, listToPopulate);
				}
			}
		}
	}

	/**
	 * Get index of item given its data element
	 */
	protected int getItemIndex(Widget parent, Object element) {
		int index = -1;
		Item[] kids = getChildren(parent);
		if (kids != null) for (int idx = 0; idx < kids.length; idx++)
			if (kids[idx].getData() == element) index = idx;
		return index;
	}

	/**
	 * We don't show actual filters, only filter references that are unique generated
	 *  for each subtree of each subsystem. Yet, each event is relative to the filter,
	 *  not our special filter references. Hence, all this code!!
	 * <p>
	 * Special case handling for updates to filters which affect the filter
	 *  but not the filter parent:
	 *   1. Existing filter renamed (RENAME)
	 *   2. Existing filter's filter strings changed (CHANGE)
	 * <p>
	 * Assumption:
	 *   1. event.getGrandParent() == subsystem (one event fired per affected subsystem)
	 *   2. event.getSource() == filter or filter string (not the reference, the real filter or string)
	 *   3. event.getParent() == parent of filter or filter string. One of:
	 *      a. filterPool reference or filter reference (nested)
	 *      b. filterPool for non-nested filters when showing filter pools
	 *      c. subsystem for non-nested filters when not showing filter pools
	 *      d. filter for nested filters
	 * <p>
	 * Our job here:
	 *   1. Determine if we are even showing the given subsystem
	 *   2. Find the reference to the updated filter in that subsystem's subtree
	 *   3. Ask that parent to either update its name or collapse and refresh its children
	 *   4. Forget selecting something ... the original item remains selected!
	 */
	protected void findAndUpdateFilter(ISystemResourceChangeEvent event, int type) {
		ISystemFilter filter = (ISystemFilter) event.getSource();
		//Object parent = event.getParent();
		if (debug) {
			String eventType = null;
			switch (type) {
			case ISystemResourceChangeEvents.EVENT_RENAME_FILTER_REFERENCE:
				eventType = "EVENT_RENAME_FILTER_REFERENCE"; //$NON-NLS-1$
				break;
			case ISystemResourceChangeEvents.EVENT_CHANGE_FILTER_REFERENCE:
				eventType = "EVENT_CHANGE_FILTER_REFERENCE"; //$NON-NLS-1$
				break;
			}
			logDebugMsg("SV event: " + eventType); //$NON-NLS-1$
		}

		// STEP 1. ARE WE EVEN SHOWING THE GIVEN SUBSYSTEM?
		ISubSystem ss = (ISubSystem) event.getGrandParent();
		Widget widget = findItem(ss);

		if (widget != null) {

			// STEP 2: ARE WE SHOWING A REFERENCE TO RENAMED OR UPDATED FILTER?
			Widget item = null;

			Control c = getControl();

			// KM: defect 53008.
			// Yes we are showing the subsystem, so widget is the subsystem item
			if (widget != c && widget instanceof Item) {

				if (debug) logDebugMsg("...Found ss " + ss); //$NON-NLS-1$

				item = internalFindReferencedItem(widget, filter, SEARCH_INFINITE);
			}
			// No, we are not showing the subsystem, so widget is the control
			else if (widget == c) {

				if (debug) logDebugMsg("...Din not find ss " + ss); //$NON-NLS-1$

				item = internalFindReferencedItem(widget, filter, SEARCH_INFINITE);
			}

			if (item == null)
				logDebugMsg("......didn't find renamed/updated filter's reference!"); //$NON-NLS-1$
			else {
				// STEP 3: UPDATE THAT FILTER...
				if (type == ISystemResourceChangeEvents.EVENT_RENAME_FILTER_REFERENCE) {
					String[] rproperties = { IBasicPropertyConstants.P_TEXT };
					update(item.getData(), rproperties); // for refreshing non-structural properties in viewer when model changes
				} else if (type == ISystemResourceChangeEvents.EVENT_CHANGE_FILTER_REFERENCE) {
					//if (((TreeItem)item).getExpanded())
					//refresh(item.getData());
					smartRefresh(new TreeItem[] { (TreeItem) item });
					/*
					 Object data = item.getData();
					 boolean wasExpanded = getExpanded((Item)item);
					 setExpandedState(data, false); // collapse node
					 refresh(data); // clear all cached widgets
					 if (wasExpanded)
					 setExpandedState(data, true); // by doing this all subnodes that were expanded are now collapsed
					 */
				}
				updatePropertySheet();
			}
		}
	}

	protected void findAndUpdateFilterString(ISystemResourceChangeEvent event, int type) {
		ISystemFilterString filterString = (ISystemFilterString) event.getSource();
		// STEP 1. ARE WE EVEN SHOWING THE GIVEN SUBSYSTEM?
		ISubSystem ss = (ISubSystem) event.getGrandParent();
		Widget item = findItem(ss);
		if (item != null && item != getControl()) {
			Item ssItem = (Item) item;
			if (debug) logDebugMsg("...Found ss " + ss); //$NON-NLS-1$
			// STEP 2: ARE WE SHOWING A REFERENCE TO THE UPDATED FILTER STRING?
			item = internalFindReferencedItem(ssItem, filterString, SEARCH_INFINITE);
			if (item == null)
				logDebugMsg("......didn't find updated filter string's reference!"); //$NON-NLS-1$
			else {
				// STEP 3: UPDATE THAT FILTER STRING...
				if (type == ISystemResourceChangeEvents.EVENT_CHANGE_FILTERSTRING_REFERENCE) // HAD BETTER!
				{
					//if (((TreeItem)item).getExpanded())
					//refresh(item.getData());
					// boolean wasExpanded = getExpanded((Item)item);
					Object data = item.getData();
					setExpandedState(data, false); // collapse node
					refresh(data); // clear all cached widgets
					//if (wasExpanded)
					//setExpandedState(data, true); // hmm, should we?
					String properties[] = { IBasicPropertyConstants.P_TEXT };
					update(item.getData(), properties); // for refreshing non-structural properties in viewer when model changes
					updatePropertySheet();
				}
			}
		}
	}

	/**
	 * We don't show actual filters, only filter references that are unique generated
	 *  for each subtree of each subsystem. Yet, each event is relative to the filter,
	 *  not our special filter references. Hence, all this code!!
	 * <p>
	 * Special case handling for updates to filters which affect the parent of the
	 *  filter, such that the parent's children must be re-generated:
	 *   1. New filter created (ADD)
	 *   2. Existing filter deleted (DELETE)
	 *   3. Existing filters reordered (MOVE)
	 * <p>
	 * Assumption:
	 *   1. event.getGrandParent() == subsystem (one event fired per affected subsystem)
	 *   2. event.getSource() == filter (not the reference, the real filter)
	 *   3. event.getParent() == parent of filter. One of:
	 *      a. filterPool reference or filter reference (nested)
	 *      b. filterPool for non-nested filters when showing filter pools
	 *      c. subsystem for non-nested filters when not showing filter pools
	 *      d. filter for nested filters
	 * <p>
	 * Our job here:
	 *   1. Determine if we are even showing the given subsystem
	 *   2. Find the parent to the given filter: filterPool or subsystem
	 *   3. Ask that parent to refresh its children (causes re-gen of filter references)
	 *   4. Select something: QUESTION: is this subsystem the origin of this action??
	 *      a. For ADD, select the newly created filter reference for the new filter
	 *           ANSWER: IF PARENT OF NEW FILTER IS WITHIN THIS SUBSYSTEM, AND WAS SELECTED PREVIOUSLY
	 *      b. For DELETE, select the parent of the filter?
	 *           ANSWER: IF DELETED FILTER IS WITHING THIS SUBSYSTEM AND WAS SELECTED PREVIOUSLY
	 *      c. For MOVE, select the moved filters
	 *           ANSWER: IF MOVED FILTERS ARE WITHIN THIS SUBSYSTEM, AND WERE SELECTED PREVIOUSLY
	 */
	protected void findAndUpdateFilterParent(ISystemResourceChangeEvent event, int type) {
		ISubSystem ss = (ISubSystem) event.getGrandParent();


		boolean add = false, move = false, delete = false;
		boolean afilterstring = false;
		//if (debug)
		//{
		String eventType = null;
		switch (type) {
		case ISystemResourceChangeEvents.EVENT_ADD_FILTER_REFERENCE:
			add = true;
			if (debug) eventType = "EVENT_ADD_FILTER_REFERENCE"; //$NON-NLS-1$
			break;
		case ISystemResourceChangeEvents.EVENT_DELETE_FILTER_REFERENCE:
			delete = true;
			if (debug) eventType = "EVENT_DELETE_FILTER_REFERENCE"; //$NON-NLS-1$
			break;
		case ISystemResourceChangeEvents.EVENT_MOVE_FILTER_REFERENCES:
			move = true;
			if (debug) eventType = "EVENT_MOVE_FILTER_REFERENCES"; //$NON-NLS-1$
			break;
		case ISystemResourceChangeEvents.EVENT_ADD_FILTERSTRING_REFERENCE:
			add = true;
			afilterstring = true;
			if (debug) eventType = "EVENT_ADD_FILTERSTRING_REFERENCE"; //$NON-NLS-1$
			break;
		case ISystemResourceChangeEvents.EVENT_DELETE_FILTERSTRING_REFERENCE:
			delete = true;
			afilterstring = true;
			if (debug) eventType = "EVENT_DELETE_FILTERSTRING_REFERENCE"; //$NON-NLS-1$
			break;
		case ISystemResourceChangeEvents.EVENT_MOVE_FILTERSTRING_REFERENCES:
			move = true;
			afilterstring = true;
			if (debug) eventType = "EVENT_MOVE_FILTERSTRING_REFERENCES"; //$NON-NLS-1$
			break;

		}
		if (debug) logDebugMsg("SV event: " + eventType); //$NON-NLS-1$
		//}
		//clearSelection();

		ISystemFilter filter = null;
		ISystemFilterString filterstring = null;
		if (!afilterstring)
			filter = (ISystemFilter) event.getSource(); // for multi-source move, gets first filter
		else
			filterstring = (ISystemFilterString) event.getSource();

		boolean multiSource = move;
		// STEP 1: ARE WE SHOWING THE SUBSYSTEM GRANDPARENT OF CURRENT REFRESH?
		Widget item = findItem(ss);

		if (item == null) {
			if (debug) logDebugMsg("...Did not find ss " + ss.getName()); //$NON-NLS-1$
			return;
		}

		boolean wasSelected = false;
		IStructuredSelection oldSelections = (IStructuredSelection) getSelection();

		Object parent = event.getParent();
		if (debug) logDebugMsg("...Found ss " + ss); //$NON-NLS-1$

		// STEP 2: ARE WE SHOWING A REFERENCE TO THE FILTER's PARENT POOL?
		Widget parentRefItem = null;
		ISystemFilterContainer refdParent = null;
		// 3a (reference to filter pool or filter)
		if (parent instanceof ISystemFilterContainerReference) // given a reference to parent?
		{
			refdParent = ((ISystemFilterContainerReference) parent).getReferencedSystemFilterContainer();
			parentRefItem = internalFindReferencedItem(item, refdParent, SEARCH_INFINITE);
		}
		// 3b and 3d. (filter pool or filter)
		else if (parent instanceof ISystemFilterContainer) {
			refdParent = (ISystemFilterContainer) parent;
			parentRefItem = internalFindReferencedItem(item, refdParent, SEARCH_INFINITE);
		}
		// 3c (subsystem)
		else {
			parentRefItem = item;
		}
		if (parentRefItem != null) {
			if (debug) logDebugMsg("......We are showing reference to parent"); //$NON-NLS-1$
			// STEP 3... YES, SO REFRESH PARENT... IT WILL RE-GEN THE FILTER REFERENCES FOR EACH CHILD FILTER
			//  ... actually, call off the whole show if that parent is currently not expanded!!
			// HMMM... WE NEED TO REFRESH EVEN IF NOT EXPANDED IF ADDING FIRST CHILD
			if (!add) // move or delete
			{
				// Widgets can only be Tree or TreeItem here
				if (parentRefItem instanceof Item){
					if (!(((TreeItem) parentRefItem).getExpanded())) {
						refresh(parentRefItem.getData()); // flush cached widgets so next expand is fresh
						return;
					}
				}


				// move or delete and parent is expanded...
				Item oldItem = (Item) internalFindReferencedItem(parentRefItem, afilterstring ? (Object) filterstring : (Object) filter, 1);
				//if (debug)
				//logDebugMsg("oldItem null? " + (oldItem==null));
				if (oldItem != null) // found moved or deleted filter in our subtree
				{
					wasSelected = isSelected(oldItem.getData(), oldSelections); // was it selected before?
					//if (debug)
					//logDebugMsg("was selected? " + wasSelected);
				} else {
					// else interesting case ... we are showing the parent, but can't find the child!
				}
				if (move) {
					Object[] srcObjects = null;
					if (multiSource)
						srcObjects = event.getMultiSource();
					else {
						srcObjects = new Object[1];
						srcObjects[0] = event.getSource();
					}
					moveReferencedTreeItems(parentRefItem, srcObjects, event.getPosition());
					//refresh(parentRefItem.getData());
				} else // remove
				{
					if (oldItem != null)
						remove(oldItem.getData());
				}
			} else // add operation
			{
				if (parentRefItem instanceof TreeItem && (!((TreeItem) parentRefItem).getExpanded())){
					refresh(parentRefItem.getData()); // delete cached GUIs
				}			
				else if (afilterstring) {
					ISystemFilterReference fr = (ISystemFilterReference) parentRefItem.getData();
					ISystemFilterStringReference fsr = fr.getSystemFilterStringReference(filterstring);
					createTreeItem(parentRefItem, fsr, event.getPosition());
					//setSelection(new StructuredSelection(fsr),true);
				} else {
					Object data = parentRefItem.getData();
					if (data instanceof ISystemFilterContainerReference) {
						ISystemFilterContainerReference sfcr = (ISystemFilterContainerReference) data;
						ISystemFilterReference sfr = sfcr.getSystemFilterReference(ss, filter);
						createTreeItem(parentRefItem, sfr, event.getPosition());
					} else // hmm, could be parent is a subsystem, child is a filter in no-show-filter-pools mode
					{
						if (data instanceof ISystemFilterPoolReferenceManagerProvider) // that's a subsystem!
						{
							ISystemFilterPoolReferenceManagerProvider sfprmp = (ISystemFilterPoolReferenceManagerProvider) data;
							ISystemFilterPoolReferenceManager sfprm = sfprmp.getSystemFilterPoolReferenceManager();
							ISystemFilterReference sfr = sfprm.getSystemFilterReference(ss, filter);
							createTreeItem(parentRefItem, sfr, sfprm.getSystemFilterReferencePosition(sfr));
						}
					}
				}
				//refresh(parentRefItem.getData());
			}

			// STEP 4: DECIDE WHAT TO SELECT:

			// 4a. ADD ... only select if parent of new filter was previously selected...
			if (add && isSelected(parentRefItem.getData(), oldSelections)) {
				if (debug) logDebugMsg(".........that parent was previously selected"); //$NON-NLS-1$
				// .... YES, SO SELECT NEW FILTER'S REFERENCE
				Item filterItem = (Item) internalFindReferencedItem(parentRefItem, afilterstring ? (Object) filterstring : (Object) filter, 1); // start at filter's parent, search for filter
				if (filterItem == null) {
					if (debug) logDebugMsg("Hmm, didn't find new filter's reference!"); //$NON-NLS-1$
				} else {
					if (debug) logDebugMsg(".........Trying to set selection to " + filterItem.getData()); //$NON-NLS-1$
					setSelection(new StructuredSelection(filterItem.getData()), true);
				}
			}
			// 4b. DELETE ... select parent if deleted filter was previously selected
			else if (delete && wasSelected) {
				setSelection(new StructuredSelection(parentRefItem.getData())); // select parent
			}
			// 4c. MOVE ... only select if any of moved references were previously selected...
			else if (move && wasSelected && !afilterstring) {
				ISystemFilter[] filters = (ISystemFilter[]) event.getMultiSource();
				if (filters != null) {
					ISystemFilterReference[] newRefs = new ISystemFilterReference[filters.length];
					for (int idx = 0; idx < newRefs.length; idx++) {
						Widget w = internalFindReferencedItem(parentRefItem, filters[idx], 1);
						newRefs[idx] = (ISystemFilterReference) ((Item) w).getData();
					}
					setSelection(new StructuredSelection(newRefs), true);
				}
			} else if (move && wasSelected && afilterstring) {
				ISystemFilterString[] filterStrings = (ISystemFilterString[]) event.getMultiSource();
				if (filterStrings != null) {
					ISystemFilterStringReference[] newRefs = new ISystemFilterStringReference[filterStrings.length];
					for (int idx = 0; idx < newRefs.length; idx++) {
						Widget w = internalFindReferencedItem(parentRefItem, filterStrings[idx], 1);
						newRefs[idx] = (ISystemFilterStringReference) ((Item) w).getData();
					}
					setSelection(new StructuredSelection(newRefs), true);
				}
			}

		} else if (debug) logDebugMsg("Did not find parent ref " + parent); //$NON-NLS-1$
	}

	/**
	 * Move existing items a given number of positions within the same node.
	 * If the delta is negative, they are all moved up by the given amount. If
	 * positive, they are all moved down by the given amount.<p>
	 */
	protected void moveReferencedTreeItems(Widget parentItem, Object[] masterSrc, int delta) {
		int[] oldPositions = new int[masterSrc.length];
		Item[] oldItems = new Item[masterSrc.length];
		Object[] src = new Object[masterSrc.length];

		for (int idx = 0; idx < src.length; idx++) {
			oldItems[idx] = (Item) internalFindReferencedItem(parentItem, masterSrc[idx], 1);
			src[idx] = oldItems[idx].getData();
		}
		Item[] children = null;
		if (parentItem instanceof Item) {
			children = getItems((Item) parentItem);
		} else
			children = getChildren(parentItem);

		for (int idx = 0; idx < src.length; idx++) {
			oldPositions[idx] = getTreeItemPosition(oldItems[idx], children) + 1;
			//logDebugMsg("::: Old position : " + oldPositions[idx]);
		}

		if (delta > 0) // moving down, process backwards
		{
			for (int idx = src.length - 1; idx >= 0; idx--) {
				//logDebugMsg("DN: Old position : " + oldPositions[idx] + ", new position : " + (oldPositions[idx]+delta));
				moveTreeItem(parentItem, oldItems[idx], src[idx], oldPositions[idx] + delta);
			}
		} else // moving up, process forewards
		{
			for (int idx = 0; idx < src.length; idx++) {
				//logDebugMsg("UP: Old position : " + oldPositions[idx] + ", new position : " + (oldPositions[idx]+delta-1));
				moveTreeItem(parentItem, oldItems[idx], src[idx], oldPositions[idx] + delta - 1);
			}
		}
	}

	/**
	 * Recursively tries to find a reference to the given referenced item
	 *
	 * @param parent the parent item at which to start the search.
	 * @param element the master element to which we want to find a tree item which references it
	 * @param searchLimit how deep to search
	 */
	protected Widget internalFindReferencedItem(Widget parent, Object element, int searchLimit) {
		previousItem = null;
		searchDepth = 0;

		Widget result = mappedFindFirstRemoteItemReference(element);
		if (result == null)
		{
			result = recursiveInternalFindReferencedItem(parent, element, searchLimit);
		}
		return result;
	}

	/**
	 * Recursively tries to find a reference the given filtercontainer
	 * Limits search depth to when we find an item that is not a connection,
	 *    subsystem, filter pool, filter or filter string.
	 * @param parent the parent item at which to start the search.
	 * @param element the master element to which we want to find a tree item which references it
	 * @param searchLimit how deep to search
	 */
	protected Widget recursiveInternalFindReferencedItem(Widget parent, Object element, int searchLimit) {
		// compare with node
		Object data = parent.getData();
		if ((data != null) && (data instanceof IRSEBaseReferencingObject)) {
			IRSEBaseReferencingObject refingData = (IRSEBaseReferencingObject) data;
			Object refedData = refingData.getReferencedObject();
			//logDebugMsg("data is a refing obj to " + refingData);
			if (refedData == element)
				//if (refedData.equals(element))
				return parent;
			else
				previousItem = parent;
		}
		// recurse over children if we are listing a subsystem or connection or
		// filter framework reference object, and nesting limit not reached.
		if (((data instanceof ISubSystem) || (data instanceof IHost) || (data instanceof ISystemFilterContainer) || (data instanceof ISystemFilterContainerReference) || (data instanceof ISystemFilterStringReference))
				&& (searchDepth < searchLimit)) {
			++searchDepth;
			int oldDepth = searchDepth;
			Item[] items = getChildren(parent);
			for (int i = 0; (i < items.length); i++)
			{
				Widget o = recursiveInternalFindReferencedItem(items[i], element, searchLimit);
				if (o != null) return o;
				searchDepth = oldDepth;
			}
		}
		return null;
	}

	/**
	 * Recursively tries to find an item starting at the given item.
	 * (base viewer classes do not offer a relative search!)
	 *
	 * @param parent the parent item at which to start the search.
	 * @param element the element to match on. Matches on "==" versus equals()
	 */
	protected Widget internalFindRelativeItem(Widget parent, Object element, int searchLimit) {
		searchDepth = 0;
		return recursiveInternalFindRelativeItem(parent, element, searchLimit);
	}

	/**
	 * Recursively tries to find an item starting at the given item.
	 * (base viewer classes do not offer a relative search!)
	 *
	 * @param parent the parent item at which to start the search.
	 * @param element the element to match on. Matches on "==" versus equals()
	 */
	protected Widget recursiveInternalFindRelativeItem(Widget parent, Object element, int searchLimit) {
		// compare with node
		Object data = parent.getData();
		if ((data != null) && (data == element)) return parent;
		// recurse over children
		if (searchDepth < searchLimit) {
			++searchDepth;
			int oldDepth = searchDepth;
			Item[] items = getChildren(parent);
			for (int i = 0; i < items.length; i++) {
				Widget o = recursiveInternalFindRelativeItem(items[i], element, searchLimit);
				if (o != null) return o;
				searchDepth = oldDepth;
			}
		}
		return null;
	}



	/**
	 * Find the first binary-match or name-match of remote object, given its absolute name.
	 * @param remoteObjectName The absolute name of the remote object to find.
	 * @param subsystem The subsystem of the remote object to find. Optional.
	 * @param parentItem The parent item at which to start the search. Optional.
	 * @return TreeItem hit if found
	 */
	public Item findFirstRemoteItemReference(String remoteObjectName, ISubSystem subsystem, Item parentItem) {
		//List matches = new Vector();
		Item match = null;
		if (parentItem == null)
			//findAllRemoteItemReferences(remoteObjectName, null, subsystem, matches);
			match = internalFindFirstRemoteItemReference(remoteObjectName, null, subsystem);
		else {

			//recursiveFindAllRemoteItemReferences(parentItem, remoteObjectName, null, subsystem, matches);
			match = recursiveFindFirstRemoteItemReference(parentItem, remoteObjectName, null, subsystem);
		}
		//if (matches.size() > 0)
		//  return (Item)matches.elementAt(0);
		//else
		//  return null;
		return match;
	}

	/**
	 * Find the first binary-match or name-match of a remote object, given its binary object.
	 * @param remoteObject - The remote object to find.
	 * @param parentItem - Optionally, the parent item to start the search at
	 * @return TreeItem hit if found
	 */
	public Item findFirstRemoteItemReference(Object remoteObject, Item parentItem) {

		Item match = mappedFindFirstRemoteItemReference(remoteObject);
		if (match != null && !match.isDisposed())
			return match;

		//List matches = new Vector();
		ISystemViewElementAdapter adapter = getViewAdapter(remoteObject);
		if (adapter == null) return null;

		ISubSystem subsystem = adapter.getSubSystem(remoteObject);
		String remoteObjectName = adapter.getAbsoluteName(remoteObject);
		if (parentItem == null)
			//findAllRemoteItemReferences(remoteObjectName, remoteObject, subsystem, matches);
			match = internalFindFirstRemoteItemReference(remoteObjectName, remoteObject, subsystem);
		else {
			//recursiveFindAllRemoteItemReferences(parentItem, remoteObjectName, remoteObject, subsystem, matches);
			//System.out.println("recursiveFindFirstRemoteItemReference(parentItem, remoteObjectName, remoteObject, subsystem)");
			match = recursiveFindFirstRemoteItemReference(parentItem, remoteObjectName, remoteObject, subsystem);
		}

		//if (matches.size() > 0)
		//  return (Item)matches.elementAt(0);
		//else
		//  return null;
		return match;
	}

	/**
	 * Recursively tries to find a given remote object. Since the object memory object
	 *  for a remote object is not dependable we call getAbsoluteName() on the adapter to
	 *  do the comparisons. Note this does not take into account the parent connection or
	 *  subsystem or filter, hence you must know where to start the search, else you risk
	 *  finding the wrong one.
	 *
	 * @param element the remote object to which we want to find a tree item which references it. Can be a string or an object
	 * @param elementObject the actual remote element to find, for binary matching, optionally for cases when element is a string
	 * @param matches the List to populate with hits, or <code>null</code> to
	 *    get a new List created and returned with the hits.
	 * @return the List populated with hits, or <code>null</code> if <code>null</code>
	 *    was passed in as the List to populate and no hits were found.
	 */
	public List findAllRemoteItemReferences(Object element, Object elementObject, List matches) {
		String searchString = null;
		ISubSystem subsystem = null;
		if (element instanceof String)
			searchString = (String) element;
		else {
			if (elementObject == null)
				elementObject = element;
			ISystemViewElementAdapter adapter = getViewAdapter(element);
			if (adapter == null)
				return matches;
			subsystem = adapter.getSubSystem(element);
			searchString = adapter.getAbsoluteName(element);
		}
		Tree tree = getTree();
		Item[] roots = tree.getItems();
		if (roots == null)
			return matches;
		if (matches == null)
			matches = new Vector();

		// try new map lookup method - won't work in cases of rename
		if (!mappedFindAllRemoteItemReferences(elementObject, matches)){
			for (int idx = 0; idx < roots.length; idx++) {
				//System.out.println("recursiveFindAllRemoteItemReferences(roots[idx], searchString, elementObject, subsystem, matches);");
				matches = recursiveFindAllRemoteItemReferences(roots[idx], searchString, elementObject, subsystem, matches);
			}
		}

		return matches;
	}

	/**
	 * Recursively tries to find all occurrences of a given remote object, starting at the tree root.
	 * Since the object memory object for a remote object is not dependable we call getAbsoluteName()
	 * on the adapter to do the comparisons.
	 * <p>
	 * TODO: This method should not return any invalid matches, i.e. remote objects
	 * that do match the String identifier but have been deleted already. Because the
	 * same remote object can appear in multiple contexts in the RSE Tree, a single
	 * remote object identifier String may evaluate to multiple different matches
	 * to fill into the matches argument. All those context object matches, however,
	 * reference the same real-world model objects due to the constraint that
	 * {@link IRemoteObjectIdentifier} uniquely identifies a remote object.
	 * <p>
	 * This overload takes a string and a subsystem.
	 *
	 * @param searchString the absolute name of the remote object to which
	 *    we want to find a tree item which references it.
	 * @param elementObject the actual remote element to find, for binary matching
	 * @param subsystem optional subsystem to search within
	 * @param matches the List to populate with hits (TreeItem objects),
	 *     or <code>null</code> to get a new List created and returned
	 * @return the List populated with hits, or <code>null</code> if
	 *     <code>null</code> was passed as matches to populate and no matches
	 *     were found.
	 */
	protected List findAllRemoteItemReferences(String searchString, Object elementObject, ISubSystem subsystem, List matches) {
		Tree tree = getTree();
		Item[] roots = tree.getItems();
		if (roots == null) return matches;
		if (matches == null)
			matches = new Vector();

		// try new map lookup method - won't work in cases of rename
		if (!mappedFindAllRemoteItemReferences(elementObject, matches)){

			boolean foundExact = false;
			for (int idx = 0; idx < roots.length; idx++){
				if (recursiveFindExactMatches((TreeItem)roots[idx], elementObject, subsystem, matches)){
					foundExact = true;
				}
			}

			if (!foundExact)
			{
				for (int idx = 0; idx < roots.length; idx++){
					matches = recursiveFindAllRemoteItemReferences(roots[idx], searchString, elementObject, subsystem, matches);
				}
			}
		}
		return matches;
	}


	private boolean recursiveFindExactMatches(TreeItem root, Object elementObject, ISubSystem subsystem, List matches)
	{
		boolean foundSomething = false;
		Object data = root.getData();
		if (data == elementObject)
		{
			matches.add(root);
			foundSomething = true;
		}
		if (subsystem != null){
			if (data instanceof ISubSystem){
				if (data != subsystem)
					return false;
			}
			else if (data instanceof IHost){
				if (subsystem.getHost() != data)
					return false;
			}
		}

		TreeItem[] children = root.getItems();
		for (int i = 0; i < children.length; i++)
		{
			if (recursiveFindExactMatches(children[i], elementObject, subsystem,  matches))
			{
				foundSomething = true;
			}
		}
		return foundSomething;
	}

	/**
	 * Recursively tries to find the first occurrence of a given remote object, starting at the tree root.
	 * Optionally scoped to a specific subsystem.
	 * Since the object memory object for a remote object is not dependable we call getAbsoluteName()
	 * on the adapter to do the comparisons.
	 * <p>
	 * This overload takes a string and a subsystem.
	 *
	 * @param searchString the absolute name of the remote object to which we want to find a tree item which references it.
	 * @param elementObject the actual remote element to find, for binary matching
	 * @param subsystem optional subsystem to search within
	 * @return TreeItem hit if found
	 */
	protected Item internalFindFirstRemoteItemReference(String searchString, Object elementObject, ISubSystem subsystem) {
		Item[] roots = getTree().getItems();
		if ((roots == null) || (roots.length == 0)) return null;

		// use map first
		Item match = mappedFindFirstRemoteItemReference(elementObject);

		for (int idx = 0; (match == null || match.isDisposed()) && (idx < roots.length); idx++) {
			//System.out.println("recursiveFindFirstRemoteItemReference(parentItem, remoteObjectName, remoteObject, subsystem)");
			match = recursiveFindFirstRemoteItemReference(roots[idx], searchString, elementObject, subsystem);
		}

		return match;
	}
	

	protected Item mappedFindFirstRemoteItemReference(Object elementObject)
	{
		Widget item = findItem(elementObject);
		if (item != null && !item.isDisposed()){
			return (Item)item;
		}
		return null;
	}

	protected boolean mappedFindAllRemoteItemReferences(Object elementObject, List occurrences)
	{
		int numFound = 0;
		Widget[] items = findItems(elementObject);
		if (items.length > 0)
		{
			for (int i = 0; i < items.length; i++)
			{
				Widget item = items[i];
				if (!item.isDisposed()){
					occurrences.add(item);
					numFound++;
				}
			}
			if (numFound > 0){
				return true;
			}
		}

		return false;
	}

	/**
	 * Recursively tries to find all references to a remote object.
	 * @param parent the parent item at which to start the search.
	 * @param elementName the absolute name of the remote element to find
	 * @param elementObject the actual remote element to find, for binary matching
	 * @param subsystem optional subsystem to search within
	 * @param occurrences the List to populate with hits. Must not be <code>null</code>
	 * @return the given List populated with hits
	 */
	protected List recursiveFindAllRemoteItemReferences(Item parent, String elementName, Object elementObject, ISubSystem subsystem, List occurrences) {
		Object rawData = parent.getData();
		ISystemViewElementAdapter remoteAdapter = null;
		// ----------------------------
		// what are we looking at here?
		// ----------------------------
		if (rawData != null) remoteAdapter = getViewAdapter(rawData);
		// -----------------------------------------------------------------------
		// if this is a remote object, test if it is the one we are looking for...
		// -----------------------------------------------------------------------
		if (remoteAdapter != null) {
			// first test for binary match
			if (elementObject == rawData) {
				occurrences.add(parent); // found a match!
				if (debugRemote) System.out.println("Find All: Remote item binary match found"); //$NON-NLS-1$
				return occurrences; // no point in checking the kids
			}
			// now test for absolute name match
			String fqn = remoteAdapter.getAbsoluteName(rawData);
			if (debugRemote) System.out.println("TESTING FINDALL: '" + fqn + "' vs '" + elementName + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if ((fqn != null) && fqn.equals(elementName)) {
				// make sure this is the right kind of match
				boolean correctSubsystem = false;
				if (subsystem == null){
					correctSubsystem = true;
				}
				else {
					Object data = parent.getData();
					if (data != null){
						ISystemViewElementAdapter adapter = getViewAdapter(data);
						if (adapter != null && adapter.getSubSystem(data).equals(subsystem)){
							correctSubsystem = true;
						}
					}
				}
				if (correctSubsystem){
					occurrences.add(parent); // found a match!
					if (debugRemote) System.out.println("...and remote item name match found"); //$NON-NLS-1$
					return occurrences; // no point in checking the kids
				}
			}
		}
		// -------------------------------------------------------------------------
		// if we have been given a subsystem to restrict to, that is a hint to us...
		// -------------------------------------------------------------------------
		else if ((rawData != null) && (subsystem != null)) // test for hints we are in the wrong place
		{
			// if we are currently visiting a subsystem, and that subsystem is not from the same
			//  factory, then we can assume the remote object occurrences we are looking for are
			//  not to be found within this branch...
			if ((rawData instanceof ISubSystem) && (((ISubSystem) rawData).getSubSystemConfiguration() != subsystem.getSubSystemConfiguration())) {
				return occurrences; // they don't match, so don't bother checking the kids
			}
			// if we are currently visiting a connection, and that connection's hostname is not the same
			//  as that of our given subsystem, then we can assume the remote object occurrences we are
			//  looking for are not to be found within this branch...
			else if ((rawData instanceof IHost) && (!((IHost) rawData).getHostName().equals(subsystem.getHost().getHostName()))) {
				return occurrences; // they don't match, so don't bother checking the kids
			}
		}
		// recurse over children
		Item[] items = getChildren(parent);
		for (int i = 0; (i < items.length); i++) {

			if (!items[i].isDisposed()) occurrences = recursiveFindAllRemoteItemReferences(items[i], elementName, elementObject, subsystem, occurrences);
		}
		return occurrences;
	}




	/**
	 * Recursively tries to find the first references to a remote object.
	 * This search is restricted to the given subsystem, if given.
	 * @param parent the parent item at which to start the search.
	 * @param elementName the absolute name of the remote element to find
	 * @param elementObject the actual remote element to find, for binary matching
	 * @param subsystem optional subsystem to search within
	 * @return TreeItem match if found, null if not found.
	 */
	protected Item recursiveFindFirstRemoteItemReference(Item parent, String elementName, Object elementObject, ISubSystem subsystem) {
		Object rawData = parent.getData();
		ISystemViewElementAdapter remoteAdapter = null;
		// ----------------------------
		// what are we looking at here?
		// ----------------------------
		if (rawData != null) remoteAdapter = getViewAdapter(rawData);
		// -----------------------------------------------------------------------
		// if this is a remote object, test if it is the one we are looking for...
		// -----------------------------------------------------------------------
		if (remoteAdapter != null) {
			// first test for binary match
			if (elementObject == rawData) {
				if (debugRemote) System.out.println("Remote item binary match found"); //$NON-NLS-1$
				return parent; // return the match
			}
			// now test for absolute name match
			String fqn = remoteAdapter.getAbsoluteName(rawData);
			if (debugRemote) System.out.println("TESTING FINDFIRST: '" + fqn + "' vs '" + elementName + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if ((fqn != null) && fqn.equals(elementName)) {
				if ((subsystem != null) && (subsystem == remoteAdapter.getSubSystem(rawData))) {
					if (debugRemote) System.out.println("Remote item name match found and subsystems matched"); //$NON-NLS-1$
					return parent; // return the match
				} else if (subsystem == null) {
					if (debugRemote) System.out.println("Remote item name match found and subsystem null"); //$NON-NLS-1$
					return parent;
				} else if (debugRemote) System.out.println("Remote item name match found but subsystem mismatch"); //$NON-NLS-1$
			}
		}
		// -------------------------------------------------------------------------
		// if we have been given a subsystem to restrict to, that is a hint to us...
		// -------------------------------------------------------------------------
		else if ((rawData != null) && (subsystem != null)) // test for hints we are in the wrong place
		{
			// if we are currently visiting a subsystem, and that subsystem is not from the same
			//  factory, then we can assume the remote object occurrences we are looking for are
			//  not to be found within this branch...
			if ((rawData instanceof ISubSystem) && (rawData != subsystem)) {
				return null; // they don't match, so don't bother checking the kids
			}
			// if we are currently visiting a connection, and that connection's hostname is not the same
			//  as that of our given subsystem, then we can assume the remote object occurrences we are
			//  looking for are not to be found within this branch...
			else if ((rawData instanceof IHost) && !((IHost) rawData).getHostName().equals(subsystem.getHost().getHostName())) {
				return null; // they don't match, so don't bother checking the kids
			}
		}
		// recurse over children
		Item[] items = getChildren(parent);
		Item match = null;
		for (int i = 0; (match == null) && (i < items.length); i++) {
			if (!items[i].isDisposed()) match = recursiveFindFirstRemoteItemReference(items[i], elementName, elementObject, subsystem);
		}
		return match;
	}

	/**
	 * Recursively tries to find all filters affected by a given remote object.
	 *
	 * @param elementName the absolute name of the remote object to which we want to find a filters which result in it.
	 * @param subsystem The subsystem which owns the remote resource. Necessary to scope the search for impacted filters.
	 * @param matches the List to populate with hits. Can be <code>null</code>,
	 *     in which case a new List is created and returned.
	 * @return List of FilterMatch objects for each affected filter, or
	 *     <code>null</code> if <code>null</code> was passed in as the List
	 *     to populate and no matches were found.
	 */
	protected List findAllRemoteItemFilterReferences(String elementName, ISubSystem subsystem, List matches) {
		Tree tree = getTree();
		Item[] roots = tree.getItems();
		if (roots == null) return matches;
		if (matches == null)
			matches = new Vector();

		for (int idx = 0; idx < roots.length; idx++){
			matches = recursiveFindAllRemoteItemFilterReferences(roots[idx], elementName, subsystem, matches);
		}
		return matches;

	}

	/**
	 * Recursively tries to find all filters which are affected by a given remote object, such that we can subsequently refresh that filter
	 *  after a remote resource change.
	 * @param parent the parent item at which to start the search.
	 * @param elementName the absolute name of the remote element that has been created, changed, deleted or renamed.
	 * @param subsystem The subsystem which owns the remote resource. Necessary to scope the search for impacted filters.
	 * @param occurrences the List to populate with hits. Must not be <code>null</code>.
	 *
	 * @return The given List of occurrences, populated with FilterMatch objects
	 *     for each affected filter.
	 */
	protected List recursiveFindAllRemoteItemFilterReferences(Item parent, String elementName, ISubSystem subsystem, List occurrences) {
		Object rawData = parent.getData();

		// ----------------------------
		// what are we looking at here?
		// ----------------------------

		// ---------------------------------------------------------------------
		// if this is a filter object, test for two things:
		//  #1. does this filter list this remote object if expanded/refreshed?
		//  #2. does this filter list the contents of this remote object?
		// ---------------------------------------------------------------------
		if (rawData instanceof ISystemFilterReference) {
			ISystemFilterReference filterRef = (ISystemFilterReference) rawData;
			if (filterRef.getReferencedFilter().isPromptable()) return occurrences;
			ISubSystem fss = (ISubSystem) filterRef.getProvider();
			if (fss != null) // should never happen!!
			{
				// #1
				if (fss.doesFilterMatch(filterRef.getReferencedFilter(), elementName)) {
					occurrences.add(new FilterMatch((TreeItem) parent, true)); // found a match!
					if (debugRemote) System.out.println("...Filter match found for " + elementName + ": " + filterRef.getReferencedFilter().getName()); //$NON-NLS-1$ //$NON-NLS-2$
					return occurrences; // no point in checking the kids
				}
				// #2
				else if (fss.doesFilterListContentsOf(filterRef.getReferencedFilter(), elementName)) {
					occurrences.add(new FilterMatch((TreeItem) parent, false)); // found a match!
					if (debugRemote) System.out.println("...Filter content match found for " + elementName + ": " + filterRef.getReferencedFilter().getName()); //$NON-NLS-1$ //$NON-NLS-2$
					return occurrences; // no point in checking the kids
				} else if (debugRemote) System.out.println("... no match on the filter for element name " + elementName); //$NON-NLS-1$
			}
		}
		// ----------------------------------------------------------------------
		// if this is not a filter, then before recursing on its kids, check for
		//  hints that such recursion is a waste of time, for performance reasons
		// ----------------------------------------------------------------------
		else if (rawData != null) {
			// ---------------------------------------------------------------------------------
			// if we are currently visiting a subsystem, and that subsystem is not from the same
			//  factory, then we can assume the remote object occurrences we are looking for are
			//  not to be found within this branch...
			// ---------------------------------------------------------------------------------
			if ((rawData instanceof ISubSystem) && (subsystem != null)) {
				ISubSystem currSS = (ISubSystem) rawData;
				if (currSS.getSubSystemConfiguration() != subsystem.getSubSystemConfiguration()) return occurrences; // they don't match, so don't bother checking the kids
			}
			// -----------------------------------------------------------------------------------------
			// if we are currently visiting a connection, and that connection's hostname is not the same
			//  as that of our given subsystem, then we can assume the remote object occurrences we are
			//  looking for are not to be found within this branch...
			// -----------------------------------------------------------------------------------------
			else if (rawData instanceof IHost) {
				if (subsystem==null) {
					return occurrences; //bug 187061: renaming a host has no subsystem associated, therefore no other matches to rename
				}
				IHost currConn = (IHost) rawData;
				if (!currConn.getHostName().equals(subsystem.getHost().getHostName()))
					return occurrences; // they don't match, so don't bother checking the kids
			}
			// skip the new connection prompts...
			else if (rawData instanceof ISystemPromptableObject)
				return occurrences;
			// ------------------------------------------------------------------------
			// if this is a remote object, we are too deep into this branch of the tree
			//  for filters, so stop here
			// ------------------------------------------------------------------------
			else if (getRemoteAdapter(rawData) != null) return occurrences;
		}
		// recurse over children
		Item[] items = getChildren(parent);
		for (int i = 0; (i < items.length); i++) {
			occurrences = recursiveFindAllRemoteItemFilterReferences(items[i], elementName, subsystem, occurrences);
		}
		return occurrences;
	}

	/**
	 * Inner class to encapsulate what is put in the List for the recursiveFindAllRemoteItemFilterReferences() method.
	 */
	protected class FilterMatch {
		protected boolean filterListsElement;
		protected boolean filterListsElementContents;
		protected TreeItem match;

		FilterMatch(TreeItem match, boolean filterListsElement) {
			this.match = match;
			this.filterListsElement = filterListsElement;
			this.filterListsElementContents = !filterListsElement;
		}

		boolean listsElement() {
			return filterListsElement;
		}

		boolean listsElementContents() {
			return filterListsElementContents;
		}

		TreeItem getTreeItem() {
			return match;
		}
	}

	/**
	 * --------------------------------------------------------------------------------
	 * For many actions we have to walk the selection list and examine each selected
	 *  object to decide if a given common action is supported or not.
	 * <p>
	 * Walking this list multiple times while building the popup menu is a performance
	 *  hit, so we have this common method that does it only once, setting instance
	 *  variables for all of the decisions we are in interested in.
	 * --------------------------------------------------------------------------------
	 */
	protected void scanSelections(String whereFrom) {
		//System.out.println("inside scanSelections. Called from " + whereFrom);
		// here are the instance variables we set...
		// protected boolean selectionShowRefreshAction;
		// protected boolean selectionShowOpenViewActions;
		// protected boolean selectionShowDeleteAction;
		// protected boolean selectionShowRenameAction;
		// protected boolean selectionIsRemoteObject;
		// protected boolean selectionEnableDeleteAction;
		// protected boolean selectionEnableRenameAction;


		// initial these variables to true. Then if set to false even once, leave as false always...
		selectionShowPropertiesAction = true;
		selectionShowRefreshAction = true;
		selectionShowOpenViewActions = true;
		selectionShowGenericShowInTableAction = true;
		selectionShowDeleteAction = true;
		selectionShowRenameAction = true;
		selectionEnableDeleteAction = true;
		selectionEnableRenameAction = true;
		selectionIsRemoteObject = true;

		selectionHasAncestorRelation = hasAncestorRelationSelection();


		IStructuredSelection selection = (IStructuredSelection) getSelection();
		Iterator elements = selection.iterator();
		SystemRemoteElementResourceSet lastSet = null;
		while (elements.hasNext()) {

			Object element = elements.next();
			ISystemViewElementAdapter adapter = getViewAdapter(element);
			if (adapter == null) continue;

			if (selectionShowPropertiesAction) selectionShowPropertiesAction = adapter.showProperties(element);

			if (selectionShowRefreshAction) selectionShowRefreshAction = adapter.showRefresh(element);

			if (selectionShowOpenViewActions) selectionShowOpenViewActions = adapter.showOpenViewActions(element);

			if (selectionShowGenericShowInTableAction) selectionShowGenericShowInTableAction = adapter.showGenericShowInTableAction(element);

			if (selectionShowDeleteAction) selectionShowDeleteAction = adapter.showDelete(element);

			if (selectionShowRenameAction) selectionShowRenameAction = adapter.showRename(element);

			if (selectionEnableDeleteAction) selectionEnableDeleteAction = selectionShowDeleteAction && adapter.canDelete(element) && !selectionHasAncestorRelation;
			//System.out.println("ENABLE DELETE SET TO " + selectionEnableDeleteAction);

			if (selectionEnableRenameAction) selectionEnableRenameAction = selectionShowRenameAction && adapter.canRename(element);

			if (selectionIsRemoteObject) selectionIsRemoteObject = adapter.isRemote(element);

			if (selectionIsRemoteObject && !selectionFlagsUpdated) {
				ISubSystem srcSubSystem = adapter.getSubSystem(element);
				if (srcSubSystem != null &&
						(srcSubSystem.isConnected() || element instanceof ISystemFilterReference || element instanceof ISubSystem))
				{
					SystemRemoteElementResourceSet set = null;
					if (lastSet != null)
					{
						if (lastSet.getViewAdapter() == adapter && lastSet.getSubSystem() == srcSubSystem)
						{
							set = lastSet;
						}
					}
					if (set == null)
					{
						set = getSetFor(srcSubSystem, adapter);
						lastSet = set;
					}
					set.addResource(element);
				}
			}

		}


		selectionFlagsUpdated = true;
		//System.out.println("Inside scan selections: selectionShowOpenViewActions = " + selectionShowOpenViewActions);

	}

	/**
	 * Decides whether to even show the properties menu item.
	 * Assumes scanSelections() has already been called
	 */
	protected boolean showProperties() {
		return selectionShowPropertiesAction;
	}

	/**
	 * Decides whether to even show the refresh menu item.
	 * Assumes scanSelections() has already been called
	 */
	protected boolean showRefresh() {
		return selectionShowRefreshAction;
		/*
		 boolean ok = true;
		 IStructuredSelection selection= (IStructuredSelection)getSelection();
		 Iterator elements= selection.iterator();
		 int count = 0;
		 while (ok && elements.hasNext())
		 {
		 Object element= elements.next();
		 ISystemViewElementAdapter adapter = getAdapter(element);
		 if (!adapter.showRefresh(element))
		 ok = false;
		 }
		 return ok;
		 */
	}

	/**
	 * Decides whether to even show the "open in new perspective" menu item.
	 * Assumes scanSelections() has already been called
	 */
	protected boolean showOpenViewActions() {
		return selectionShowOpenViewActions;
	}

	/**
	 * Decides whether to even show the generic "show in table" menu item.
	 * Assumes scanSelections() has already been called
	 */
	protected boolean showGenericShowInTableAction() {
		return selectionShowGenericShowInTableAction;
	}

	/**
	 * Decides whether all the selected objects are remote objects or not
	 * Assumes scanSelections() has already been called
	 */
	protected boolean areSelectionsRemote() {
		return selectionIsRemoteObject;
	}

	// ---------------------------
	// ISYSTEMDELETETARGET METHODS
	// ---------------------------

	/**
	 * Required method from ISystemDeleteTarget.
	 * Decides whether to even show the delete menu item.
	 * Assumes scanSelections() has already been called
	 */
	public boolean showDelete() {
		if (!selectionFlagsUpdated) {
			//System.out.println("Inside showDelete. selectFlagsUpdated = false");
			scanSelections("showDelete"); //$NON-NLS-1$
		}
		return selectionShowDeleteAction;
	}

	/**
	 * Required method from ISystemDeleteTarget
	 * Decides whether to enable the delete menu item.
	 * Assumes scanSelections() has already been called
	 */
	public boolean canDelete() {
		if (!selectionFlagsUpdated) {
			//System.out.println("Inside canDelete. selectFlagsUpdated = false");
			scanSelections("canDelete"); //$NON-NLS-1$
		}
		return selectionEnableDeleteAction;
	}

	/**
	 * Required method from ISystemDeleteTarget
	 *
	 * @deprecated all deletion should now occur independently of the view and the
	 *       view should only deal with the handling of refresh events
	 */
	public boolean doDelete(IProgressMonitor monitor) {
		ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
		IStructuredSelection selection = (IStructuredSelection) getSelection();
		Iterator elements = selection.iterator();
		//int selectedCount = selection.size();
		//Object multiSource[] = new Object[selectedCount];
		//int idx = 0;
		Object element = null;
		//Object parentElement = getSelectedParent();
		ISystemViewElementAdapter adapter = null;
		boolean ok = true;
		boolean anyOk = false;
		List deletedVector = new Vector();

		// keep track of the current set
		SystemRemoteElementResourceSet set = null;

		try {
			while (ok && elements.hasNext()) {
				element = elements.next();
				//multiSource[idx++] = element;
				adapter = getViewAdapter(element);
				if (getRemoteAdapter(element) != null) continue;
				ok = adapter.doDelete(getShell(), element, monitor);
				if (ok) {
					anyOk = true;
					deletedVector.add(element);
				}
			}
			// now we have things divided into sets
			// delete 1 set at a time
			for (int s = 0; s < _setList.size() && ok; s++) {
				set = (SystemRemoteElementResourceSet) _setList.get(s);
				ISubSystem srcSubSystem = set.getSubSystem();
				ISystemViewElementAdapter srcAdapter = set.getViewAdapter();

				if (srcSubSystem != null) {

					// this call can throw an exception
					ok = srcAdapter.doDeleteBatch(getShell(), set.getResourceSet(), monitor);

					if (ok) {
						anyOk = true;
						deletedVector.addAll(set.getResourceSet());
					}
				}
			}
		} catch (SystemMessageException exc) {
			SystemMessageDialog.displayErrorMessage(getShell(), exc.getSystemMessage());
			ok = false;
		} catch (Exception exc) {
			String msg = exc.getMessage();
			if ((msg == null) || (exc instanceof ClassCastException)) msg = exc.getClass().getName();
			SystemMessageDialog.displayErrorMessage(getShell(), RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_EXCEPTION_DELETING).makeSubstitution(element, msg));

			// refresh all parents if selection is remote objects
			if (selectionIsRemoteObject) {

				// we only need to iterate over the last set in the list of sets since presumably the sets before did not cause any exceptions
				// if elements in the list before were deleted successfully, then the code after this catch block will handle them (by firing delete events)
				// for the current set that caused the exception, we refresh the parents of the elements in the set (since we don't know which
				// elements in the set may have been deleted successfully before the exception occurred).
				if (set != null) {
					List list = set.getResourceSet();

					if (list != null && list.size() > 0) {

						Iterator iter = list.iterator();

						List refreshedList = new Vector();

						while (iter.hasNext()) {
							Object obj = iter.next();
							ISystemViewElementAdapter adp = getViewAdapter(obj);
							Object parent = adp.getParent(obj);

							if ((parent != null) && !(refreshedList.contains(parent))) {
								SystemResourceChangeEvent event = new SystemResourceChangeEvent(parent, ISystemResourceChangeEvents.EVENT_REFRESH_REMOTE, null);
								sr.fireEvent(event);
								refreshedList.add(parent);
							}
						}
					}
				}
			}

			ok = false;
		}
		//System.out.println("in doDelete. Any ok? " + anyOk + ", selectionIsRemoteObject? " + selectionIsRemoteObject);
		if (anyOk) {
			if (selectionIsRemoteObject)
				sr.fireRemoteResourceChangeEvent(ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_DELETED, deletedVector, null, null, null, this);
			else {
				Object[] deleted = new Object[deletedVector.size()];
				for (int idx = 0; idx < deleted.length; idx++)
					deleted[idx] = deletedVector.get(idx);
				sr.fireEvent(new org.eclipse.rse.core.events.SystemResourceChangeEvent(deleted, ISystemResourceChangeEvents.EVENT_DELETE_MANY, getSelectedParent()));
			}
		}
		return ok;
	}

	// ---------------------------
	// ISYSTEMRENAMETARGET METHODS
	// ---------------------------

	private class RenameJob extends WorkspaceJob
	{
		String[] newNames = null;
		Object[] elements = null;
		Object[] elementAdapters = null;
		Object parentElement = null;
		String renameMessage = null;

		/**
		 * RenameJob job.
		 * @param newNames array of new names of all the elements need to be renamed
		 * @param elements array of all the elements need to be renamed
		 * @param elementAdapters array of all the view adapters of the elements need to be renamed
		 * @param parentElement the parent object of the list of objects to be renamed
		 * @param renameMessage the title of the Rename job.
		 */
		public RenameJob(String[] newNames, Object[] elements, Object[] elementAdapters, Object parentElement, String renameMessage)
		{
			super(renameMessage);
			this.newNames = newNames;
			this.elements = elements;
			this.elementAdapters = elementAdapters;
			this.parentElement = parentElement;
			setUser(true);
		}

		public IStatus runInWorkspace(IProgressMonitor monitor)
		{
			ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
			Object element = null;
			ISystemViewElementAdapter adapter = null;
			ISystemRemoteElementAdapter remoteAdapter = null;
			String oldFullName = "";  //$NON-NLS-1$
			String oldName = "";      //$NON-NLS-1$
			Vector fileNamesRenamed = new Vector();

			boolean ok = true;
			try {
				int steps = elements.length;
			       monitor.beginTask(renameMessage, steps);
				for (int i=0; i < elements.length; i++)
				{
					element = elements[i];
					adapter = (ISystemViewElementAdapter)elementAdapters[i];
					remoteAdapter = getRemoteAdapter(element);
					if (remoteAdapter != null)
					{
						oldName = remoteAdapter.getName(element);
						oldFullName = remoteAdapter.getAbsoluteName(element); // pre-rename
						monitor.subTask(getRenamingMessage(oldName).getLevelOneText());
					}
					ok = adapter.doRename(null, element, newNames[i], monitor);
					if (ok)
					{
						fileNamesRenamed.add(oldName);
						if (remoteAdapter != null)
						{
							ISubSystem ss = adapter.getSubSystem(element);
							sr.fireRemoteResourceChangeEvent(ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_RENAMED, element, parentElement, ss, new String[] {oldFullName}, this);
						}

						else
							sr.fireEvent(new org.eclipse.rse.core.events.SystemResourceChangeEvent(element, ISystemResourceChangeEvents.EVENT_RENAME, parentElement));
					}
					 monitor.worked(1);
				}
			}
			catch (SystemMessageException exc)
			{
				ok = false;
				//If this operation is cancelled, need to display a proper message to the user.
				if (monitor.isCanceled() && fileNamesRenamed.size() > 0)
				{
					//Get the renamed file names
					String renamedFileNames = (String)(fileNamesRenamed.get(0));
					for (int i=1; i<(fileNamesRenamed.size()); i++)
					{
						renamedFileNames = renamedFileNames + "\n" + fileNamesRenamed.get(i); //$NON-NLS-1$
					}
					//getMessage("RSEG1125").makeSubstitution(movedFileName));
					SystemMessage thisMessage = RSEUIPlugin.getPluginMessage(ISystemMessages.FILEMSG_RENAME_INTERRUPTED);
					thisMessage.makeSubstitution(renamedFileNames);
					SystemMessageDialog.displayErrorMessage(shell, thisMessage);
				}
				else
				{
					SystemMessageDialog.displayErrorMessage(shell, exc.getSystemMessage());
				}
			} catch (Exception exc) {
				//String msg = exc.getMessage();
				//if ((msg == null) || (exc instanceof ClassCastException))
				//  msg = exc.getClass().getName();
				exc.printStackTrace();
				SystemMessageDialog.displayErrorMessage(null, RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_EXCEPTION_RENAMING).makeSubstitution(element, exc), //msg),
						exc);
				ok = false;
			}

			return Status.OK_STATUS;
		}
	}
	/**
	 * Required method from ISystemRenameTarget.
	 * Decides whether to even show the rename menu item.
	 * Assumes scanSelections() has already been called
	 */
	public boolean showRename() {
		return selectionShowRenameAction;
	}

	/**
	 * Required method from ISystemRenameTarget
	 * Decides whether to enable the rename menu item.
	 * Assumes scanSelections() has already been called
	 */
	public boolean canRename() {
		if (!selectionFlagsUpdated) scanSelections("canRename"); //$NON-NLS-1$
		return selectionEnableRenameAction;
	}

	/**
	 * Get the specific "Renaming %1..."
	 */
    protected SystemMessage getRenamingMessage(String oldName)
    {
    	SystemMessage msg = RSEUIPlugin.getPluginMessage(ISystemMessages.MSG_RENAMEGENERIC_PROGRESS);
		msg.makeSubstitution(oldName);
		return msg;
    }

	/**
	 * Required method from ISystemRenameTarget
	 */
	public boolean doRename(String[] newNames) {
		IStructuredSelection selection = (IStructuredSelection) getSelection();
		Iterator elements = selection.iterator();
		Object parentElement = getSelectedParent();

		Object[] renameElements = new Object[newNames.length];
		Object[] elementAdapters = new Object[newNames.length];
		int i = 0;
		while (elements.hasNext()) {
			renameElements[i] = elements.next();
			elementAdapters[i] = getViewAdapter(renameElements[i]);
			i++;
			//remoteAdapter = getRemoteAdapter(element);
		}
		SystemMessage renameMessage = getRenamingMessage(""); //$NON-NLS-1$
		String renameMessageText = renameMessage.getLevelOneText();
		RenameJob renameJob = new RenameJob(newNames, renameElements, elementAdapters, parentElement, renameMessageText);
		renameJob.schedule();
		return true;
	}

	protected void logDebugMsg(String msg) {
		//RSEUIPlugin.logDebugMessage(this.getClass().getName(),msg);
		msg = this.getClass().getName() + ": " + msg; //$NON-NLS-1$
		SystemBasePlugin.logInfo(msg);
		System.out.println(msg);
	}

	// -----------------------------------------------------------------
	// ISystemSelectAllTarget methods to facilitate the global action...
	// -----------------------------------------------------------------
	/**
	 * Return true if select all should be enabled for the given object.
	 * For a tree view, you should return true if and only if the selected object has children.
	 * You can use the passed in selection or ignore it and query your own selection.
	 */
	public boolean enableSelectAll(IStructuredSelection selection) {
		Tree tree = getTree();
		TreeItem[] items = tree.getSelection();
		if ((items == null) || (items.length != 1)) // only allow for single selections
			return false;

		TreeItem ti = items[0];
		int count = getItemCount(ti);
		if (count == 1) // is it a dummy?
		{
			if ((getItems(ti)[0]).getData() == null) count = 0; // assume a dummy
		}
		return (count > 0);
	}

	/**
	 * When this action is run via Edit->Select All or via Ctrl+A, perform the
	 * select all action. For a tree view, this should select all the children
	 * of the given selected object. You can use the passed in selected object
	 * or ignore it and query the selected object yourself.
	 */
	public void doSelectAll(IStructuredSelection selection) {
		Tree tree = getTree();
		TreeItem[] currSel = tree.getSelection();
		TreeItem[] childItems = currSel[0].getItems();
		if (childItems.length == 0) return;
		tree.setSelection(childItems);
		Object[] childObjects = new Object[childItems.length];
		for (int idx = 0; idx < childObjects.length; idx++)
			childObjects[idx] = childItems[idx].getData();
		fireSelectionChanged(new SelectionChangedEvent(this, new StructuredSelection(childObjects)));
	}

	// --------------------------------------------
	// ISystemTree methods to facilitate our GUI...
	// --------------------------------------------
	/**
	 * This is called to ensure all elements in a multiple-selection have the same parent in the
	 *  tree viewer. If they don't we automatically disable all actions.
	 * <p>
	 * Designed to be as fast as possible by going directly to the SWT widgets
	 */
	public boolean sameParent() {
		boolean same = true;
		Tree tree = getTree();
		TreeItem[] items = tree.getSelection();
		if ((items == null) || (items.length == 0)) return true;
		TreeItem prevParent = null;
		TreeItem currParent = null;
		for (int idx = 0; same && (idx < items.length); idx++) {
			currParent = items[idx].getParentItem();
			if ((idx > 0) && (currParent != prevParent))
				same = false;
			else {
				prevParent = currParent;
			}
		}
		return same;
	}

	protected boolean selectionHasAncestryRelationship() {
		if (selectionFlagsUpdated) return selectionHasAncestorRelation;

		Tree tree = getTree();
		TreeItem[] items = tree.getSelection();
		return hasSelectedAncestor(items);
		/*
		Tree tree = getTree();
		TreeItem[] items = tree.getSelection();

		for (int idx = 0; idx < items.length; idx++) {
			TreeItem item = items[idx];
	//		for (int c = 0; c < items.length; c++) {
		//		if (item != items[c]) {
					if (isAncestorOf(item, items)) {
						return true;
					}
//				}
	//		}
	}
		return false;
		*/
	}

	protected boolean isAncestorOf(TreeItem container, TreeItem[] items) {
		TreeItem[] children = container.getItems();
		for (int i = 0; i < children.length; i++) {
			TreeItem child = children[i];
			for (int j = 0; j < items.length; j++)
			{
				TreeItem item = items[j];
				if (child == item) {
					return true;
				}
				else if (child.getItemCount() > 0)
				{
					if (isAncestorOf(child, items))
					{
						return true;
					}
				}
			}
		}
		return false;
	}


	protected boolean hasSelectedAncestor(TreeItem[] items) {

		List cleanParents = new ArrayList();

		for (int j = 0; j < items.length; j++)
		{
			TreeItem item = items[j];
			TreeItem parent = item.getParentItem();
			while (parent != null && !cleanParents.contains(parent))
			{
				if (isTreeItemSelected(parent))
				{
					return true;
				}
				else
				{
					cleanParents.add(parent);
					parent = parent.getParentItem();
				}
			}
		}
		return false;
	}






/*
	protected boolean isAncestorOf(TreeItem container, TreeItem item) {
		TreeItem[] children = container.getItems();
		for (int i = 0; i < children.length; i++) {
			TreeItem child = children[i];
			if (child == item) {
				return true;
			} else if (child.getItemCount() > 0) {
				if (isAncestorOf(child, item)) {
					return true;
				}
			}
		}
		return false;
	}
*/

	/**
	 * This is called to accurately get the parent object for the current selection
	 *  for this viewer.
	 * <p>
	 * The getParent() method in the adapter is very unreliable... adapters can't be sure
	 * of the context which can change via filtering and view options.
	 */
	public Object getSelectedParent() {
		Tree tree = getTree();
		TreeItem[] items = tree.getSelection();
		if ((items == null) || (items.length == 0)) {
			return tree.getData();
		} else {
			TreeItem parentItem = items[0].getParentItem();
			if (parentItem != null)
				return parentItem.getData();
			else
				return tree.getData();
		}
	}

	/**
	 * Return the TreeItem of the parent of the selected node. Or null if a root is selected.
	 */
	public TreeItem getSelectedParentItem() {
		Tree tree = getTree();
		TreeItem[] items = tree.getSelection();
		if ((items == null) || (items.length == 0)) {
			return null;
		} else {
			return items[0].getParentItem();
		}
	}

	/**
	 * This returns the element immediately before the first selected element in this tree level.
	 * Often needed for enablement decisions for move up actions.
	 */
	public Object getPreviousElement() {
		Object prevElement = null;
		Tree tree = getTree();
		TreeItem[] items = tree.getSelection();
		if ((items != null) && (items.length > 0)) {
			TreeItem item1 = items[0];
			TreeItem[] parentItems = null;
			TreeItem parentItem = item1.getParentItem();
			if (parentItem != null)
				parentItems = parentItem.getItems();
			else
				parentItems = item1.getParent().getItems();
			if (parentItems != null) {
				TreeItem prevItem = null;
				for (int idx = 0; (prevItem == null) && (idx < parentItems.length); idx++)
					if ((parentItems[idx] == item1) && (idx > 0)) prevItem = parentItems[idx - 1];
				if (prevItem != null) prevElement = prevItem.getData();
			}
		}
		return prevElement;
	}

	/**
	 * This returns the element immediately after the last selected element in this tree level
	 * Often needed for enablement decisions for move down actions.
	 */
	public Object getNextElement() {
		Object nextElement = null;
		Tree tree = getTree();
		TreeItem[] items = tree.getSelection();
		if ((items != null) && (items.length > 0)) {
			TreeItem itemN = items[items.length - 1];
			TreeItem[] parentItems = null;
			TreeItem parentItem = itemN.getParentItem();
			if (parentItem != null)
				parentItems = parentItem.getItems();
			else
				parentItems = itemN.getParent().getItems();
			if (parentItems != null) {
				TreeItem nextItem = null;
				for (int idx = 0; (nextItem == null) && (idx < parentItems.length); idx++)
					if ((parentItems[idx] == itemN) && (idx < (parentItems.length - 1))) nextItem = parentItems[idx + 1];
				if (nextItem != null) nextElement = nextItem.getData();
			}
		}
		return nextElement;
	}

	/**
	 * This is called to walk the tree back up to the roots and return the visible root
	 *  node for the first selected object.
	 */
	public Object getRootParent() {
		Tree tree = getTree();
		TreeItem[] selectedItems = tree.getSelection();
		Object rootElement = null;
		if ((selectedItems != null) && (selectedItems.length > 0)) {
			TreeItem item = selectedItems[0];
			TreeItem parentItem = item.getParentItem();
			if (parentItem == null) // item is a root element
				rootElement = item.getData();
			else
				while (rootElement == null) {
					item = parentItem;
					parentItem = item.getParentItem();
					if (parentItem == null) // item is a root element
						rootElement = item.getData();
				}
		}
		//logDebugMsg("getRootParent returned: "+rootElement);
		return rootElement;
	}

	/**
	 * This returns an array containing each element in the tree, up to but not including the root.
	 * The array is in reverse order, starting at the leaf and going up.
	 */
	public Object[] getElementNodes(Object element) {
		Widget w = findItem(element);
		if ((w != null) && (!w.isDisposed()) && (w instanceof TreeItem)) return getElementNodes((TreeItem) w);
		return null;
	}

	/**
	 * This returns an array containing each element in the tree, up to but not including the root.
	 * The array is in reverse order, starting at the leaf and going up.
	 * This flavor is optimized for the case when you have the tree item directly.
	 * @return Array of Objects leading to the given TreeItem,
	 *    sorted from the leaf item up.
	 */
	public Object[] getElementNodes(TreeItem item) {
		List v = new Vector();
		v.add(item.getData());
		while (item != null) {
			item = item.getParentItem();
			if (item != null) v.add(item.getData());
		}
		Object[] nodes = new Object[v.size()];
		for (int idx = 0; idx < nodes.length; idx++)
			nodes[idx] = v.get(idx);
		return nodes;
	}

	/**
	 * This returns an array containing each element in the tree, up to but not including the root.
	 * The array is in reverse order, starting at the leaf and going up.
	 * This flavor returns an array of TreeItem objects versus element objects.
	 * @return Array of TreeItem objects leading to the given TreeItem,
	 *    sorted from the leaf item up.
	 */
	public TreeItem[] getItemNodes(TreeItem item) {
		List v = new Vector();
		v.add(item);
		while (item != null) {
			item = item.getParentItem();
			if (item != null) v.add(item);
		}
		TreeItem[] nodes = new TreeItem[v.size()];
		for (int idx = 0; idx < nodes.length; idx++)
			nodes[idx] = (TreeItem) v.get(idx);
		return nodes;
	}

	/**
	 * Helper method to determine if a given object is currently selected.
	 * Does consider if a child node of the given object is currently selected.
	 */
	public boolean isSelectedOrChildSelected(Object parentElement) {
		boolean isSelected = false;
		Item[] selectedItems = ((Tree) getControl()).getSelection();
		if ((selectedItems != null) && (selectedItems.length > 0)) {
			Widget w = findItem(parentElement);
			if (!(w instanceof TreeItem)) return true; // assume we have been given the root, which means any selection is a child
			TreeItem item = (TreeItem) w;
			// for every selected tree item, scan upwards to the root to see if
			// it or any of its parents are the given element.
			for (int idx = 0; !isSelected && (idx < selectedItems.length); idx++) {
				if (selectedItems[idx] instanceof TreeItem) {
					if (selectedItems[idx] == item)
						isSelected = true;
					else
						isSelected = searchToRoot((TreeItem) selectedItems[idx], item);
				}
			}
		}
		return isSelected;
		//return isSelected(element, (IStructuredSelection)getSelection());
	}

	/**
	 * Override that takes a widget.
	 */
	public boolean isTreeItemSelectedOrChildSelected(Widget w) {
		boolean isSelected = false;
		Item[] selectedItems = ((Tree) getControl()).getSelection();
		if ((selectedItems != null) && (selectedItems.length > 0)) {
			if (!(w instanceof TreeItem)) return true; // assume we have been given the root, which means any selection is a child
			TreeItem item = (TreeItem) w;
			// for every selected tree item, scan upwards to the root to see if
			// it or any of its parents are the given element.
			for (int idx = 0; !isSelected && (idx < selectedItems.length); idx++) {
				if (selectedItems[idx] instanceof TreeItem) {
					if (selectedItems[idx] == item)
						isSelected = true;
					else
						isSelected = searchToRoot((TreeItem) selectedItems[idx], item);
				}
			}
		}
		return isSelected;
		//return isSelected(element, (IStructuredSelection)getSelection());
	}

	/**
	 * Return the number of immediate children in the tree, for the given tree node
	 */
	public int getChildCount(Object element) {
		if (getTree().isDisposed()) return 0;
		Widget w = findItem(element);
		if (w == null)
			return 0;

		// modified patch initially provided by Tobias Schwarz
		if (w instanceof TreeItem) {
			TreeItem ti = (TreeItem) w;
			int count = getItemCount((Item) w);
			int ignoreItems = 0;
			if (count > 0) {
				Item[] items = getItems(ti);
				for (int i=0; i<count; i++) {
				    Object itemData = items[i].getData();
				    if (itemData==null
				        || itemData instanceof SystemMessageObject
				        || itemData instanceof PendingUpdateAdapter
				    ) {
				        ignoreItems++;
				    } else {
				        break;
				    }
				}
			}
			return count - ignoreItems;
		}
		return getItemCount((Control) w);
	}

	/**
	 * Return the tree item of the first selected object
	 */
	protected TreeItem getFirstSelectedTreeItem() {
		// find the selected tree item...
		Item[] selectedItems = ((Tree) getControl()).getSelection();
		if ((selectedItems == null) || (selectedItems.length == 0) || !(selectedItems[0] instanceof TreeItem)) return null;
		return (TreeItem) selectedItems[0];
	}

	/**
	 * Refresh the given tree item node
	 */
	protected void refreshTreeItem(TreeItem item) {
		// if we are already expanded, collapse and refresh to clear memory
		if (getExpanded(item)) {
			collapseNode(item.getData(), true);
			//setExpanded(selectedItem, false);
			//refreshItem(selectedItem, selectedItem.getData());
		}
		// ok, now time to force an expand...
		createChildren(item); // re-expand. this calls the content provider, which calls the getChildren() method in the adapter. That will call us back.
		item.setExpanded(true);
	}

	public void updatePropertySheet(){
		updatePropertySheet(false);
	}
	
	/**
	 * Called when a property is updated and we need to inform the Property Sheet viewer.
	 * There is no formal mechanism for this so we simulate a selection changed event as
	 *  this is the only event the property sheet listens for.
	 */
	private void updatePropertySheet(boolean force) {
		ISelection selection = getSelection();
		if (selection == null || !(selection instanceof IStructuredSelection)) return;

		// only fire this event if the view actually has focus
		if (force || getControl().isFocusControl())
		{		
			Object object = ((IStructuredSelection)selection).getFirstElement();
			if (object != null){
				ISystemViewElementAdapter adapter = getViewAdapter(object);
				if (adapter != null){	
					// figure out what properties this object has
					adapter.setPropertySourceInput(object);
					IPropertyDescriptor[] descriptors = adapter.getPropertyDescriptors();
					Object[] propertyValues = new Object[descriptors.length];
					for (int i = 0; i < descriptors.length; i++){
						IPropertyDescriptor descriptor = descriptors[i];
						propertyValues[i] = adapter.getPropertyValue(descriptor.getId());							
					}						
					
					if (_lastPropertyValues != null){
						if (_lastPropertyValues.length == propertyValues.length){
							boolean theSame = true;					
							// check to see if anything has changed
							for (int i = 0; i < _lastPropertyValues.length && theSame; i++){
								Object lastPropertyValue = _lastPropertyValues[i];
								Object propertyValue = propertyValues[i];
								if (lastPropertyValue != null && !lastPropertyValue.equals(propertyValue)){
									theSame = false;
								}
							}
							if (theSame){
								// no need to refresh anything
								return;
							}
						}
					}
					_lastPropertyValues = propertyValues;					
						
				}
														
				
				IWorkbenchPart ourPart = getWorkbenchPart();
				IWorkbenchPart activePart = null;
				IWorkbenchWindow win = getWorkbenchWindow(); // from dialog it's possible to not have an active part
				if (win != null){
					IWorkbenchPage page = win.getActivePage();
					if (page != null){
						activePart = page.getActivePart();
					}
				}
				if (activePart != null){
					if (activePart != ourPart){
						ourPart.setFocus(); // without part focus, there are no post selection change listeners
					}	
							
					// create events in order to update the property sheet
					 IStructuredSelection fakeSelection = new StructuredSelection(new Object());		
					
					if (fakeSelection != null){
						SelectionChangedEvent dummyEvent = new SelectionChangedEvent(this, fakeSelection);
						// first change the selection, then change it back (otherwise the property sheet ignores the event)
						fireSelectionChanged(dummyEvent);
						firePostSelectionChanged(dummyEvent);
					}
					SelectionChangedEvent event = new SelectionChangedEvent(this, selection);
					
					// fire the event
					fireSelectionChanged(event);
					firePostSelectionChanged(event);
					
					if (ourPart != activePart){
						activePart.setFocus();
					}
				}
			}
			
		}
	}

	/**
	 * Called to select an object within the tree, and optionally expand it
	 */
	public void select(Object element, boolean expand) {
		setSelection(new StructuredSelection(element), true); // true => reveal
		if (expand) setExpandedState(element, true);
	}

	/**
	 * Returns the tree item of the first selected object. Used for setViewerItem in a resource
	 *  change event.
	 */
	public Item getViewerItem() {
		TreeItem[] selectedItems = getTree().getSelection();
		if ((selectedItems != null) && (selectedItems.length > 0))
			return selectedItems[0];
		else
			return null;
	}

	/**
	 * Returns true if any of the selected items are currently expanded
	 */
	public boolean areAnySelectedItemsExpanded() {
		boolean expanded = false;
		Item[] selectedItems = ((Tree) getControl()).getSelection();
		if ((selectedItems != null) && (selectedItems.length > 0)) {
			// for every selected tree item, see if it is currently expanded...
			for (int idx = 0; !expanded && (idx < selectedItems.length); idx++) {
				if (selectedItems[idx] instanceof TreeItem) {
					if (((TreeItem) selectedItems[idx]).getExpanded()) expanded = true;
				}
			}
		}
		return expanded;
	}

	/**
	 * Returns true if any of the selected items are expandable but not yet expanded
	 */
	public boolean areAnySelectedItemsExpandable() {
		boolean expandable = false;
		Item[] selectedItems = ((Tree) getControl()).getSelection();
		if ((selectedItems != null) && (selectedItems.length > 0)) {
			// for every selected tree item, see if needs expanding...
			for (int idx = 0; !expandable && (idx < selectedItems.length); idx++) {
				if (selectedItems[idx] instanceof TreeItem) {
					if ((((TreeItem) selectedItems[idx]).getItemCount() > 0) && !((TreeItem) selectedItems[idx]).getExpanded()) expandable = true;
				}
			}
		}
		return expandable;
	}


    /**
     * Initialize drag and drop support for this view.
     *
     */
    protected void initDragAndDrop()
    {
        int ops = DND.DROP_COPY | DND.DROP_MOVE;
        Transfer[] dragtransfers = new Transfer[]
            { PluginTransfer.getInstance(),
        		EditorInputTransfer.getInstance()
            };

        Transfer[] droptransfers = new Transfer[]
            { PluginTransfer.getInstance(),
        		FileTransfer.getInstance(),
                EditorInputTransfer.getInstance(),
                ResourceTransfer.getInstance()
             };

        addDragSupport(ops | DND.DROP_DEFAULT, dragtransfers, new SystemViewDataDragAdapter(this));
        addDropSupport(ops | DND.DROP_DEFAULT, droptransfers, new SystemViewDataDropAdapter(this));
    }
	// ----------------------------------
	// Support for EXPAND TO-> ACTIONS...
	// ----------------------------------

	public void expandTo(Object parentObject, Object remoteObject)
	{
		SystemViewLabelAndContentProvider provider = (SystemViewLabelAndContentProvider)getContentProvider();
		provider.setEnableDeferredQueries(false);

		ISystemViewElementAdapter adapter = getViewAdapter(parentObject);
		ISystemViewElementAdapter targetAdapter = getViewAdapter(remoteObject);
		Assert.isNotNull(adapter, "adapter is null for " + parentObject); //$NON-NLS-1$
		ISubSystem ss = adapter.getSubSystem(parentObject);
		String parentName = adapter.getAbsoluteName(parentObject);
		String remoteObjectName = targetAdapter.getAbsoluteName(remoteObject);
		Item parentItem = findFirstRemoteItemReference(parentName, ss, null);
		if (parentItem != null)
		{
			createChildren(parentItem);
			Item[] children = getItems(parentItem);
			setExpanded(parentItem, true);
			for (int i = 0; i < children.length; i++)
			{

				Item child = children[i];
				Object data = child.getData();
				if (data.equals(remoteObject))
				{
					select(remoteObject, false);
					provider.setEnableDeferredQueries(true);
					return;
				}
				else if (data instanceof ISystemFilterReference)
				{
					ISystemFilterReference ref = (ISystemFilterReference)data;
					if (ss.doesFilterMatch(ref.getReferencedFilter(), remoteObjectName)){
						expandTo(data, remoteObject);
					}
					else if (ss.doesFilterListContentsOf(ref.getReferencedFilter(),remoteObjectName)){
						expandTo(data, remoteObject);
					}
				}
				else if (data instanceof ISystemFilterPoolReference)
				{
					expandTo(data, remoteObject);
				}
				else
				{
					ISystemViewElementAdapter dataAdapter = (ISystemViewElementAdapter)((IAdaptable)data).getAdapter(ISystemViewElementAdapter.class);
					String path = dataAdapter.getAbsoluteName(data);
					if (remoteObjectName.startsWith(path))
					{
						expandTo(data, remoteObject);
					}
				}
			}
		}
		provider.setEnableDeferredQueries(true);
	}


	/**
	 * Called when user selects an Expand To action to expand the selected remote object with a quick filter
	 */
	public void expandTo(String filterString) {
		SystemViewPart svp = getSystemViewPart();
		if (svp == null) return;
		// find the selected tree item...
		TreeItem selectedItem = getFirstSelectedTreeItem();
		if (selectedItem == null) return;
		Object element = selectedItem.getData();
		ISystemViewElementAdapter remoteAdapter = getViewAdapter(element);
		if (remoteAdapter == null) return;
		// update our hashtables, keyed by object address and tree path...
		if (expandToFiltersByObject == null) expandToFiltersByObject = new Hashtable();
		if (expandToFiltersByTreePath == null) expandToFiltersByTreePath = new Hashtable();
		if (filterString != null)
			expandToFiltersByObject.put(selectedItem.getData(), filterString);
		else
			expandToFiltersByObject.remove(selectedItem.getData());
		if (filterString != null)
			expandToFiltersByTreePath.put(getItemPath(selectedItem), filterString);
		else
			expandToFiltersByTreePath.remove(getItemPath(selectedItem));

		// now refresh this tree item node...
		refreshTreeItem(selectedItem);
	}

	/**
	 * Return the fully-qualified path up to the given item, expressible as a string
	 */
	protected String getItemPath(TreeItem item) {
		StringBuffer idBuffer = new StringBuffer(getItemNodeID(item));
		TreeItem[] elementNodes = getItemNodes(item);
		if (elementNodes != null) {
			for (int idx = elementNodes.length - 1; idx >= 0; idx--) {
				item = elementNodes[idx];
				idBuffer.append(SystemViewPart.MEMENTO_DELIM + getItemNodeID(item));
			}
		}
		//System.out.println("MEMENTO HANDLE: " + idBuffer.toString());
		return idBuffer.toString();
	}

	/**
	 * Return the string identifying this node in the tree
	 */
	protected String getItemNodeID(TreeItem item) {
		//ISystemViewElementAdapter adapter = getAdapter(item.getData());
		//return adapter.getMementoHandle(item.getData());
		return item.getText();
	}

	/**
	 * Callback from the input provider to test if the given node has expand-to filtering criteria
	 */
	public String getExpandToFilter(Object element) {
		String filter = null;
		// for performance reasons, we first test for a binary match...
		if (expandToFiltersByObject != null) {
			filter = (String) expandToFiltersByObject.get(element);
		}
		// if binary match fails, look for tree path match...
		if ((filter == null) && (expandToFiltersByTreePath != null)) {
			Widget item = findItem(element);
			if ((item != null) && (item instanceof TreeItem)) {
				filter = (String) expandToFiltersByTreePath.get(getItemPath((TreeItem) item));
				if (filter != null) {
					if (expandToFiltersByObject == null) expandToFiltersByObject = new Hashtable();
					expandToFiltersByObject.put(element, filter); // so next time it will be faster
				}
			}
		}
		return filter;
	}

	/**
	 * To support restoring state we need to write out to disk out current table that maps
	 *   tree items to their current expand-to filter. That means we need access to the table.
	 */
	public Hashtable getExpandToFilterTable() {
		return expandToFiltersByTreePath;
	}

	/**
	 * To support restoring state we need to write out to disk out current table that maps
	 *   tree items to their current expand-to filter. That means we need to be able to set the table.
	 */
	public void setExpandToFilterTable(Hashtable ht) {
		expandToFiltersByTreePath = ht;
	}

	protected SystemRemoteElementResourceSet getSetFor(ISubSystem subSystem, ISystemViewElementAdapter adapter) {
		for (int i = 0; i < _setList.size(); i++) {
			SystemRemoteElementResourceSet set = (SystemRemoteElementResourceSet) _setList.get(i);
			if (set.getViewAdapter() == adapter && set.getSubSystem() == subSystem) {
				return set;
			}
		}

		// no existing set - create one
		SystemRemoteElementResourceSet newSet = new SystemRemoteElementResourceSet(subSystem, adapter);
		_setList.add(newSet);
		return newSet;
	}
/*
	protected boolean usingElementMap() {
		return false;
	}
*/

		/**
		 * For bug 204684:
		 *
		 * Because we don't have an API for ISystemViewElementAdapter.exists()...
		 * This class is used to determine whether an object exists and consequently whether to remove it from the view
		 * after a query comes back with either no children or a SystemMessageObject.  We query the parent to determine
		 * whether the remote object exists - in that case we just leave the message as is in the view.  In the case where
		 * we detect that the object does not exist, we re-populate the parent node with the new children.
		 */
		public static class CheckExistenceJob extends Job
		{


			private IAdaptable _remoteObject;
			//private TreeItem _parentItem;
			private IContextObject _context;
			public CheckExistenceJob(IAdaptable remoteObject, TreeItem parentItem, IContextObject context)
			{
				super("Check existence"); //$NON-NLS-1$
				_remoteObject = remoteObject;
				//_parentItem = parentItem;
				_context = context;
			}

			public IStatus run(IProgressMonitor monitor)
			{
				// need to use the model object to get the adapter (since it could be a filter)
				ISystemViewElementAdapter adapter = (ISystemViewElementAdapter)_context.getModelObject().getAdapter(ISystemViewElementAdapter.class);
				if (adapter != null)
				{
					final Object[] children =  adapter.getChildren(_context, monitor);
					if (contains(children, _remoteObject))
					{
						// we want to end this so the user sees the error message
					}
					else
					{
						Display.getDefault().asyncExec(new Runnable(){
							public void run()
							{
								/*
								// first need to remove the old items
								TreeItem[] items = _parentItem.getItems();
								for (int i = 0; i < items.length; i++) {
									if (items[i].getData() != null) {
										disassociate(items[i]);
										items[i].dispose();
									} else {
										items[i].dispose();
										}
									}


								// we want to propagate the changes to the view
								add(_context.getModelObject(), children);
								*/
								// refresh using the event since other views may need updating
								IAdaptable par = _context.getModelObject();
								ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
								sr.fireEvent(new SystemResourceChangeEvent(par, ISystemResourceChangeEvents.EVENT_REFRESH_REMOTE, null));

							}
						});
					}
				}

				return Status.OK_STATUS;
			}

			public static boolean contains(Object[] children, IAdaptable remoteObject)
			{
				ISystemViewElementAdapter adapter1 = (ISystemViewElementAdapter)remoteObject.getAdapter(ISystemViewElementAdapter.class);
				String path1 = adapter1==null ? null : adapter1.getAbsoluteName(remoteObject);
				for (int i = 0; i < children.length; i++)
				{
					if (remoteObject==children[i] || remoteObject.equals(children[i]))
					{
						return true;
					}
					else if (children[i] instanceof IAdaptable)
					{
						IAdaptable remoteObject2 = (IAdaptable)children[i];
						ISystemViewElementAdapter adapter2 = (ISystemViewElementAdapter)remoteObject2.getAdapter(ISystemViewElementAdapter.class);
						if (adapter2 != null)
						{
							String path2 = adapter2.getAbsoluteName(remoteObject2);
							if (path1 != null && path2 != null && path1.equals(path2))
							{
								return true;
							}
						}
					}
				}
				return false;
			}
		}


	public void add(Object parentElementOrTreePath, Object[] childElements) {		
		assertElementsNotNull(childElements);

		IContextObject contextObject = null;
		ISystemFilterReference originalFilter = null;
		if (parentElementOrTreePath instanceof IContextObject)
		{
			contextObject = (IContextObject)parentElementOrTreePath;
			originalFilter = contextObject.getFilterReference();
			parentElementOrTreePath = contextObject.getModelObject();

		}

		
		List matches = new Vector();
		findAllRemoteItemReferences(parentElementOrTreePath, parentElementOrTreePath, matches);

		// get rid of references to items for different connection
		if (parentElementOrTreePath instanceof IAdaptable)
		{
			List invalidMatches = new ArrayList();
			ISystemViewElementAdapter adapter = (ISystemViewElementAdapter)((IAdaptable)parentElementOrTreePath).getAdapter(ISystemViewElementAdapter.class);
			if (adapter != null)
			{
				ISubSystem subSystem = adapter.getSubSystem(parentElementOrTreePath);

				boolean unexpandContainer = false;
				// if the subsystem is not connected, then need to keep the widget expandable
				if (childElements.length == 0 && !subSystem.isConnected()){
					unexpandContainer = true;
				}		
				
				
				IHost parentHost = subSystem.getHost();
				for (int i = 0; i < matches.size(); i++)
				{
					Widget match = (Widget) matches.get(i);
					
					// for bug 283793
					if (match instanceof TreeItem && unexpandContainer){
						TreeItem titem = ((TreeItem)match);
						if (titem.getExpanded()){
							setExpanded(titem, false);
						}
					}
					else {
						Object data = null;
						try {
							data = match.getData();
						}
						catch (SWTException e){
							// not sure why this occurs -logging it for now
							// this is reported in bug 251625
							SystemBasePlugin.logError("Exception in SystemView.add() with " + match); //$NON-NLS-1$
							SystemBasePlugin.logError(e.getMessage());
						}
	
						if (data instanceof IAdaptable)
						{
							ISystemViewElementAdapter madapter = (ISystemViewElementAdapter)((IAdaptable)data).getAdapter(ISystemViewElementAdapter.class);
							if (madapter != null)
							{
								IHost mHost = madapter.getSubSystem(data).getHost();
								if (mHost != parentHost)
								{
									invalidMatches.add(match);
								}
							}
						}
					}
				}
				if (unexpandContainer){
					// brings back the + icon
					refresh(parentElementOrTreePath);
					return;
				}
			}

			if (invalidMatches.size() > 0)
			{
				for (int m = invalidMatches.size() - 1; m >= 0 ; m--)
				{
					Object match = invalidMatches.get(m);
					matches.remove(match);
				}
			}
		}

		//Widget[] widgets = internalFindItems(parentElementOrTreePath);
		// If parent hasn't been realized yet, just ignore the add.
		if (matches.size() == 0) {
			super.add(parentElementOrTreePath, childElements);
		}
		else
		{
		for (int i = 0; i < matches.size(); i++) {
			Widget match = (Widget) matches.get(i);
			ISystemFilterReference ref = null;
			if (match instanceof TreeItem)
			{
				ref = getContainingFilterReference((TreeItem)match);
			}
			ISystemViewElementAdapter adapter = (ISystemViewElementAdapter)((IAdaptable)parentElementOrTreePath).getAdapter(ISystemViewElementAdapter.class);

			if (matches.size() > 1 && ref != null && ref != originalFilter)
			{
				// could have the same object under multiple filters
				// need to apply filter

				Object[] newChildren = null;
				if (match instanceof TreeItem)
				{
					ContextObjectWithViewer context = getContextObject((TreeItem)match);
					newChildren = adapter.getChildren(context, new NullProgressMonitor());
					internalAdd(match, parentElementOrTreePath, newChildren);
				}
			}
			else
			{							
				internalAdd(match, parentElementOrTreePath, childElements);

				// refresh parent in this case because the parentElementOrTreePath may no longer exist
				if (childElements.length == 0 || childElements[0] instanceof SystemMessageObject)
				{
					if (adapter.isRemote(parentElementOrTreePath) && !adapter.hasChildren((IAdaptable)parentElementOrTreePath))
					{
						/*
							// refresh the parent
							Object par = adapter.getParent(parentElementOrTreePath);
							ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
							sr.fireEvent(new SystemResourceChangeEvent(par, ISystemResourceChangeEvents.EVENT_REFRESH_REMOTE, null));

							*/

						// for bug 204684, using this job to determine whether or not the object exists before trying to update
						if (match instanceof TreeItem)
						{
							TreeItem parentItem = ((TreeItem)match).getParentItem();
							if (parentItem != null)
							{
								ContextObjectWithViewer context = getContextObject(parentItem);
								if (adapter.supportsDeferredQueries(context.getSubSystem())) {
									CheckExistenceJob job = new CheckExistenceJob((IAdaptable)parentElementOrTreePath, parentItem, context);
									job.schedule();
								} else {
									Object[] children =  adapter.getChildren(context, new NullProgressMonitor());
									if (!CheckExistenceJob.contains(children, (IAdaptable)parentElementOrTreePath)) {
										IAdaptable par = context.getModelObject();
										ISystemRegistry sr = RSECorePlugin.getTheSystemRegistry();
										sr.fireEvent(new SystemResourceChangeEvent(par, ISystemResourceChangeEvents.EVENT_REFRESH_REMOTE, null));
									}
								}
							}
						}
					}
				}

			}
		}
		}

		TreeItem item = getFirstSelectedTreeItem();
		if (item != null)
		{
			if (item.getData() == parentElementOrTreePath)
			{
				updatePropertySheet();
			}
		}

		// for bug 187739
		if (contextObject instanceof ContextObjectWithViewer) {
   	       ContextObjectWithViewer ctx = (ContextObjectWithViewer)contextObject;
   	       IRSECallback cb = ctx.getCallback();
   	       if (cb!=null) {
   	          cb.done(Status.OK_STATUS, childElements);
   	       }
   	   }
	}


	/**
	 * Get the containing filter reference for an item
	 * @param item the item to get the filter reference for
	 * @return the filter reference
	 */
	public ISystemFilterReference getContainingFilterReference(TreeItem item)
	{
		Object data = item.getData();
		if (data instanceof ISystemFilterReference)
		{
			return (ISystemFilterReference)data;
		}
		else
		{
			TreeItem parent = item.getParentItem();
			if (parent != null)
			{
				return getContainingFilterReference(parent);
			}
			else
			{
				Object input = getInput();
				if (input instanceof ISystemFilterReference)
				{
					return (ISystemFilterReference)input;
				}
				else
				{
					return null;
				}
			}
		}
	}

	/**
	 * Get the containing subsystem from an item
	 * @param item the item to get the subsystem for
	 * @return the subsystem
	 */
	public ISubSystem getContainingSubSystem(TreeItem item)
	{
		Object data = item.getData();
		if (data instanceof ISubSystem)
		{
			return (ISubSystem)data;
		}
		else
		{
			TreeItem parent = item.getParentItem();
			if (parent != null)
			{
				return getContainingSubSystem(parent);
			}
			else
			{
				Object input = getInput();
				if (input instanceof ISubSystem)
				{
					return (ISubSystem)input;
				}
				else
				{
					return null;
				}
			}
		}
	}


	/**
	 * Get the context object from a tree item
	 * @param item the item to get the context for
	 * @return the context object
	 */
	public ContextObjectWithViewer getContextObject(TreeItem item)
	{
		Object data = item.getData();
		ISystemFilterReference filterReference = getContainingFilterReference(item);
		if (filterReference != null)
		{
			return new ContextObjectWithViewer(data, filterReference.getSubSystem(), filterReference, this);
		}
		else
		{
			ISubSystem subSystem = getContainingSubSystem(item);
			if (subSystem != null)
			{
				return new ContextObjectWithViewer(data, subSystem, this);
			}
			else
			{
				return new ContextObjectWithViewer(data, this);
			}
		}
	}


	/**
	 * Create tree items for the specified children
	 *
	 * @param widget the parent item for the items to create
	 * @param children the children to create items for
	 */
	public void createTreeItems(TreeItem widget, Object[] children)
	{
		TreeItem[] tis = widget.getItems();

		// first dispose of dummies
		for (int i = 0; i < tis.length; i++) {
			if (tis[i].getData() != null) {
				disassociate(tis[i]);
				Assert.isTrue(tis[i].getData() == null,
						"Second or later child is non -null");//$NON-NLS-1$
			}
			tis[i].dispose();
		}

		// now create children
		for (int i = 0; i < children.length; i++)
		{
			createTreeItem(widget, children[i], -1);
		}
	}

	/**
	 * For bug 187739
	 */
	protected void createChildren(final Widget widget, IRSECallback callback)
	{
		if (widget instanceof TreeItem)
		{
		final Item[] tis = getChildren(widget);
		if (tis != null && tis.length > 0) {
			Object data = tis[0].getData();
			if (data != null) {
				return; // children already there!
			}
		}
		final IRSECallback cb = callback;

		BusyIndicator.showWhile(widget.getDisplay(), new Runnable() {
			public void run() {
				// fix for PR 1FW89L7:
				// don't complain and remove all "dummies" ...
				if (tis != null) {
					for (int i = 0; i < tis.length; i++) {
						if (tis[i].getData() != null) {
							disassociate(tis[i]);
							Assert.isTrue(tis[i].getData() == null,
									"Second or later child is non -null");//$NON-NLS-1$

						}
						tis[i].dispose();
					}
				}
				Object d = widget.getData();
				if (d != null)
				{
					ContextObjectWithViewer parentElement = getContextObject((TreeItem)widget);
					if (cb != null){
						parentElement.setCallback(cb);
					}

					Object[] children = getSortedChildren(parentElement);
					if (children != null)
					{
						for (int i = 0; i < children.length; i++)
						{
							createTreeItem(widget, children[i], -1);
						}
					}
				}
			}

		});
		}
		else
		{
			super.createChildren(widget);
		}
	}

	/**
	 * Overridden so that we can pass a wrapper IContextObject into the provider to get children instead
	 * of the model object, itself
	 */
	protected void createChildren(final Widget widget)
	{
		createChildren(widget, null);
	}

	/**
	 * Override to pass context into hasChildren()
	 *
	 */
	public boolean isExpandable(Object elementOrTreePath) {
		Object element;
		TreePath path;
		if (elementOrTreePath instanceof TreePath) {
			path = (TreePath) elementOrTreePath;
			element = path.getLastSegment();
		} else {
			element = elementOrTreePath;
			path = null;
		}
		IContentProvider cp = getContentProvider();
		if (cp instanceof ITreePathContentProvider) {
			ITreePathContentProvider tpcp = (ITreePathContentProvider) cp;
			if (path == null) {
				// A path was not provided so try and find one
				Widget w = findItem(element);
				if (w instanceof Item) {
					Item item = (Item) w;
					path = getTreePathFromItem(item);
				}
				if (path == null) {
					path = new TreePath(new Object[] { element });
				}
			}
			return tpcp.hasChildren(path);
		}
		if (cp instanceof ITreeContentProvider)
		{
			ITreeContentProvider tcp = (ITreeContentProvider) cp;
			if (elementOrTreePath instanceof TreeItem)
			{
				ContextObjectWithViewer context = getContextObject((TreeItem)elementOrTreePath);
				return tcp.hasChildren(context);
			}
			else
			{
				return tcp.hasChildren(element);
			}
		}
		return false;
	}

	public void update(Object element, String[] properties) {
		Assert.isNotNull(element);
		List matches = new Vector();
		findAllRemoteItemReferences(element, element, matches);

		for (int i = 0; i < matches.size(); i++) {

			internalUpdate((Widget)matches.get(i), element, properties);
		}
	}

	/**
	 * This method is used to set whether or not the tree viewer allows the view adapter
	 * for a selected object to handle a double-click.  If so, the adapter implements it's
	 * own handleDoubleClickMethod() and returns whether or not the operation is complete
	 * such that the view does or does not need to do additional processing (such as expansion).
	 * Typically the method is called with <code>false</code> when the SystemView is used in a
	 * dialog since, in that context, it makes no sense to respond to double-clicks by opening
	 * in an editor.  In contrast to this approach, SystemView.setEnabled(false) prevents any
	 * handling of double-click (such as the tree expand) and disables the context menu.
	 *
	 * @param flag whether to allow the adapter to handle the double click
	 */
	public void allowAdapterToHandleDoubleClick(boolean flag)
	{
		_allowAdapterToHandleDoubleClick = flag;
	}
}
