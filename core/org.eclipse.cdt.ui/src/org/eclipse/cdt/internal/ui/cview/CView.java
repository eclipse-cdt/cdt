package org.eclipse.cdt.internal.ui.cview;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IArchive;
import org.eclipse.cdt.core.model.IArchiveContainer;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.IBinaryContainer;
import org.eclipse.cdt.core.model.IBinaryModule;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.resources.MakeUtil;
import org.eclipse.cdt.internal.ui.StandardCElementLabelProvider;
import org.eclipse.cdt.internal.ui.editor.OpenIncludeAction;
import org.eclipse.cdt.internal.ui.makeview.MakeAction;
import org.eclipse.cdt.internal.ui.makeview.MakeTarget;
import org.eclipse.cdt.internal.ui.makeview.MakeTargetAction;
import org.eclipse.cdt.internal.ui.preferences.CPluginPreferencePage;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.internal.ui.util.ProblemTreeViewer;
import org.eclipse.cdt.ui.CElementContentProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.AddBookmarkAction;
import org.eclipse.ui.actions.BuildAction;
import org.eclipse.ui.actions.CloseResourceAction;
import org.eclipse.ui.actions.CopyResourceAction;
import org.eclipse.ui.actions.DeleteResourceAction;
import org.eclipse.ui.actions.MoveResourceAction;
import org.eclipse.ui.actions.NewWizardMenu;
import org.eclipse.ui.actions.OpenFileAction;
import org.eclipse.ui.actions.OpenInNewWindowAction;
import org.eclipse.ui.actions.OpenResourceAction;
import org.eclipse.ui.actions.OpenSystemEditorAction;
import org.eclipse.ui.actions.OpenWithMenu;
import org.eclipse.ui.actions.RefreshAction;
import org.eclipse.ui.actions.RenameResourceAction;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.part.ISetSelectionTarget;
import org.eclipse.ui.part.PluginTransfer;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.framelist.BackAction;
import org.eclipse.ui.views.framelist.ForwardAction;
import org.eclipse.ui.views.framelist.FrameList;
import org.eclipse.ui.views.framelist.GoIntoAction;
import org.eclipse.ui.views.framelist.UpAction;




public class CView extends ViewPart implements IMenuListener, ISetSelectionTarget,
	IPropertyChangeListener {

	ProblemTreeViewer viewer;
	IMemento memento;

	// Actions for Menu context.
	AddBookmarkAction addBookmarkAction;
	//BuildAction buildAction;
	//BuildAction rebuildAction;
	MakeTargetAction makeTargetAction;
	CopyResourceAction copyResourceAction;
	DeleteResourceAction deleteResourceAction;
	OpenFileAction openFileAction;
	OpenSystemEditorAction openSystemEditorAction;
	PropertyDialogAction propertyDialogAction;
	RefreshAction refreshAction;
	RenameResourceAction renameResourceAction;
	MoveResourceAction moveResourceAction;

	CloseResourceAction closeProjectAction;
	OpenResourceAction openProjectAction;
	BuildAction buildAction;
	BuildAction rebuildAction;

	// CElement action
	OpenIncludeAction openIncludeAction;

	BackAction backAction;
	ForwardAction forwardAction;
	GoIntoAction goIntoAction;
	UpAction upAction;
	NewWorkingSetFilterAction wsFilterAction;
	AdjustWorkingSetFilterAction wsClearFilterAction;
	
	FrameList frameList;
	CViewFrameSource frameSource;

	CPatternFilter patternFilter = new CPatternFilter ();
	FilterSelectionAction patternFilterAction;

	CLibFilter clibFilter = new CLibFilter ();
	ShowLibrariesAction clibFilterAction;

	CWorkingSetFilter workingSetFilter = new CWorkingSetFilter ();
	ActionContributionItem adjustWorkingSetContributions [] = new ActionContributionItem[5];

	// Collapsing
	CollapseAllAction collapseAllAction;

	// Persistance tags.
	static final String TAG_SELECTION= "selection"; //$NON-NLS-1$
	static final String TAG_EXPANDED= "expanded"; //$NON-NLS-1$
	static final String TAG_ELEMENT= "element"; //$NON-NLS-1$
	static final String TAG_PATH= "path"; //$NON-NLS-1$
	static final String TAG_VERTICAL_POSITION= "verticalPosition"; //$NON-NLS-1$
	static final String TAG_HORIZONTAL_POSITION= "horizontalPosition"; //$NON-NLS-1$
	static final String TAG_FILTERS = "filters"; //$NON-NLS-1$
	static final String TAG_FILTER = "filter"; //$NON-NLS-1$
	static final String TAG_SHOWLIBRARIES = "showLibraries"; //$NON-NLS-1$
	static final String TAG_WORKINGSET = "workingSet"; //$NON-NLS-1$

	//Menu tags
	final String WORKING_GROUP_MARKER = "workingSetGroup";
	final String WORKING_GROUP_MARKER_END = "end-workingSetGroup";

	private IPartListener partListener = new IPartListener() {
		public void partActivated(IWorkbenchPart part) {
			if (part instanceof IEditorPart) {
				editorActivated((IEditorPart) part);
			}
		}
		public void partBroughtToTop(IWorkbenchPart part) {}
		public void partClosed(IWorkbenchPart part) {}
		public void partDeactivated(IWorkbenchPart part) {}
		public void partOpened(IWorkbenchPart part) {}
	};
	
	private IPropertyChangeListener workingSetListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent ev) {
			String prop = ev.getProperty();
			if(prop == null) {
				return;
			}

			if(prop.equals(CWorkingSetFilter.WORKING_SET_ACTIVE_CHANGED)) {
				updateWorkingSetMenu();
				viewer.getControl().setRedraw(false);
				viewer.refresh();
				viewer.getControl().setRedraw(true);
			} else if(prop.equals(IWorkingSetManager.CHANGE_WORKING_SET_ADD)) {
				updateWorkingSetMenu();
			} else if(prop.equals(IWorkingSetManager.CHANGE_WORKING_SET_NAME_CHANGE)) {
				updateWorkingSetMenu();
			} else if(prop.equals(IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE)){
				if(ev.getOldValue() instanceof IWorkingSet) {
					String name = ((IWorkingSet)(ev.getOldValue())).getName();
					String wsName = workingSetFilter.getWorkingSetName();
					if(wsName != null && name.equals(wsName)) {
						viewer.getControl().setRedraw(false);
						viewer.refresh();
						viewer.getControl().setRedraw(true);
					}
				}
			} else if(prop.equals(IWorkingSetManager.CHANGE_WORKING_SET_REMOVE)) {
				if(ev.getOldValue() instanceof IWorkingSet) {
					String name = ((IWorkingSet)(ev.getOldValue())).getName();
					String wsName = workingSetFilter.getWorkingSetName();
					if(wsName != null && name.equals(wsName)) {
						workingSetFilter.setWorkingSetName(null);
					}
				}
				updateWorkingSetMenu();
			}
		}
	};
	
	public CView() {
		super();
	}

	/**
	 * @see IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		viewer.getTree().setFocus();
		//composite.setFocus ();
	}

	/**
	 *      Reveal and select the passed element selection in self's visual component
	 * @see ISetSelectionTarget#selectReveal()
	 */
	public void selectReveal(ISelection selection) {
		IStructuredSelection ssel = convertSelectionToCElement(selection);
		if (!ssel.isEmpty()) {
			getResourceViewer().setSelection(ssel, true);
		}
	}

	private ITreeViewerListener expansionListener= new ITreeViewerListener() {
		public void treeCollapsed(TreeExpansionEvent event) {
		}
                
		public void treeExpanded(TreeExpansionEvent event) {
			final Object element= event.getElement();
			if (element instanceof IParent) {
				//viewer.refresh (element);
				Control ctrl= viewer.getControl();
				if (ctrl != null && !ctrl.isDisposed()) {
					ctrl.getDisplay().asyncExec(new Runnable() {
						public void run() {
							Control ctrl= viewer.getControl();
							if (ctrl != null && !ctrl.isDisposed()) {
								viewer.expandToLevel(element, 1);
							}
						}
					});
				}
			}
		}
	};

	/**
	* Handles double clicks in viewer.
	* Opens editor if file double-clicked.
	*/
	protected void handleDoubleClick(DoubleClickEvent event) {
		IStructuredSelection s = (IStructuredSelection)event.getSelection();
		IAdaptable element = (IAdaptable)s.getFirstElement();
		IEditorPart part = null;
		//System.out.println ("Double click on " + element);

		try {
			part = EditorUtility.openInEditor(element);
			if (part != null) {
				IWorkbenchPage page = getSite().getPage();
				page.bringToTop(part);
				if (element instanceof ISourceReference) {
					EditorUtility.revealInEditor(part, (ICElement)element);
				}
			}
		} catch (Exception e) {
		}		
		if (part == null && viewer.isExpandable(element)) {
			viewer.setExpandedState(element, !viewer.getExpandedState(element));
		}
	}

	/**
	* Handles key events in viewer.
	*/
	void handleKeyPressed(KeyEvent event) {
		if (event.character == SWT.DEL
			&& event.stateMask == 0 && deleteResourceAction.isEnabled()) {
			IStructuredSelection isel = deleteResourceAction.getStructuredSelection();
			Object[] array = isel.toArray();
			for (int i = 0; i < array.length; i++){
				if (array[i] instanceof IBinaryContainer
					|| array[i] instanceof IArchiveContainer) {
					return;
				}
			}
			deleteResourceAction.run();
		}
	}


	/* (non-Javadoc)
	 * Method declared on IViewPart.
	 */
	public void init(IViewSite site,IMemento memento) throws PartInitException {
		super.init(site,memento);
		this.memento = memento;
	}

	void initFrameList() {
		frameSource = new CViewFrameSource(this);
		frameList = new FrameList(frameSource);
		frameSource.connectTo(frameList);
	}

	/**
	* Answer the property defined by key.
	*/
	public Object getAdapter(Class key) {
		if (key.equals(ISelectionProvider.class))
			return viewer;
		return super.getAdapter(key);
	}


	/**
	 * Adds drag and drop support to the navigator.
	 */
	void initDragAndDrop() {
		int ops = DND.DROP_COPY | DND.DROP_MOVE;
		Transfer[] transfers = new Transfer[] {ResourceTransfer.getInstance(),
		FileTransfer.getInstance(), PluginTransfer.getInstance()};
		viewer.addDragSupport(ops, transfers, new CViewDragAdapter((ISelectionProvider)viewer));
		viewer.addDropSupport(ops, transfers, new CViewDropAdapter(viewer));
	}

	/** 
	 * Initializes the default preferences
	 */
	public static void initDefaults(IPreferenceStore store) {
		store.setDefault(TAG_SHOWLIBRARIES, true);
	}

	void initFilterFromPreferences() {
		CUIPlugin plugin = CUIPlugin.getDefault();
		boolean show = plugin.getPreferenceStore().getBoolean(TAG_SHOWLIBRARIES);
		getLibraryFilter().setShowLibraries(show);
	}

	/**
	* Create the KeyListener for doing the refresh on the viewer.
	*/
	void initRefreshKey() {
		viewer.getControl().addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent event) {
				if (event.keyCode == SWT.F5) {
					refreshAction.selectionChanged(
						(IStructuredSelection)viewer.getSelection());
						if (refreshAction.isEnabled())
							refreshAction.run();
				}
			}
		});
	}

	/**
	* Handles selection changed in viewer.
	* Updates global actions.
	* Links to editor (if option enabled)
	*/
	void handleSelectionChanged(SelectionChangedEvent event) {
		IStructuredSelection sel = (IStructuredSelection) event.getSelection();
		updateStatusLine(sel);
		updateActions(sel);
		updateGlobalActions(sel);
		goIntoAction.update();
		linkToEditor(sel);
	}


	/**
	* @see ContentOutlinePage#createControl
	*/
	public void createPartControl (Composite parent) {

		viewer = createViewer(parent);
		viewer.setUseHashlookup (true);
		CElementContentProvider provider = createContentProvider();
		viewer.setContentProvider(provider);
		setLabelDecorator(PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator());
		CUIPlugin.getDefault().getProblemMarkerManager().addListener(viewer);
		CUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);

		IWorkingSetManager wsmanager = getViewSite().getWorkbenchWindow().getWorkbench().getWorkingSetManager();
		workingSetFilter.setWorkingSetManager(wsmanager);


		// FIXME: Add Drag and Drop support.
		initFrameList();
		initRefreshKey();
		updateTitle();
		initDragAndDrop();
		viewer.addFilter(patternFilter);
		viewer.addFilter(workingSetFilter);
		//viewer.addFilter(clibFilter);
		viewer.setSorter(new CViewSorter ());
		// FIXME: Add different Sorting.
		if(memento != null)
			restoreFilters();
		else
			initFilterFromPreferences();

		viewer.setInput (CoreModel.getDefault().getCModel());

		MenuManager menuMgr= new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(this);

		Control control = viewer.getControl();
		Menu menu = menuMgr.createContextMenu(viewer.getTree ());
		control.setMenu (menu);

		// Make the Actions for the Context Menu
		makeActions();

		//Add the property changes after all of the UI work has been done.
		wsmanager.addPropertyChangeListener(workingSetListener);
		workingSetFilter.addChangeListener(workingSetListener);

		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
			handleDoubleClick(event);
			}
		});

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleSelectionChanged(event);
			}
		});

		viewer.getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				handleKeyPressed(e);
			}
		});

		viewer.addTreeListener(expansionListener);

		getSite().registerContextMenu(menuMgr, viewer);
		getSite().setSelectionProvider(viewer);
		getSite().getPage().addPartListener(partListener);

		if (memento != null)
			restoreState (memento);
		memento = null;

		fillActionBars();

	}

	protected ProblemTreeViewer createViewer(Composite parent) {
		return new ProblemTreeViewer (parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
	}

	protected CElementContentProvider createContentProvider() {
		boolean showCUChildren= CPluginPreferencePage.showCompilationUnitChildren();
		return new CElementContentProvider(showCUChildren, true);
	}

	protected StandardCElementLabelProvider createLabelProvider () {
		return new StandardCElementLabelProvider();
	}

	/* (non-Javadoc)
	 * Method declared on IWorkbenchPart.
	 */
	public void dispose() {
		getSite().getPage().removePartListener(partListener);
		CUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
		if (viewer != null) {
			viewer.removeTreeListener(expansionListener);
			CUIPlugin.getDefault().getProblemMarkerManager().removeListener(viewer);
		}
		IWorkspace workspace = CUIPlugin.getWorkspace();
		workspace.removeResourceChangeListener(closeProjectAction);
		workspace.removeResourceChangeListener(openProjectAction);

		IWorkingSetManager wsmanager = getViewSite().getWorkbenchWindow().getWorkbench().getWorkingSetManager();
		wsmanager.removePropertyChangeListener(workingSetListener);

		super.dispose();
	}

	/**
	 * An editor has been activated.  Set the selection in this navigator
	 * to be the editor's input, if linking is enabled.
	 */
	void editorActivated(IEditorPart editor) {
		if (!CPluginPreferencePage.isLinkToEditor()) {
			return;
		}

		IEditorInput input = editor.getEditorInput();
		if (input instanceof IFileEditorInput) {
			CoreModel factory = CoreModel.getDefault();
			IFileEditorInput fileInput = (IFileEditorInput) input;
			IFile file = fileInput.getFile();
			ICElement celement = factory.create(file);
			if (celement != null) {
				ISelection newSelection = new StructuredSelection(celement);
				if (!viewer.getSelection().equals(newSelection)) {
					viewer.setSelection(newSelection);
				}
			}
		}
	}

	CLibFilter getLibraryFilter () {
		return clibFilter;
	}

    /**
     * Returns the pattern filter for this view.
     * @return the pattern filter
     */
	CPatternFilter getPatternFilter() {
		return patternFilter;
	}
	
	/**
	 * Returns the working set filter for this view.
	 * @return the working set filter
	 */
	CWorkingSetFilter getWorkingSetFilter() {
		return workingSetFilter;
	}

	TreeViewer getViewer () {
		return viewer;
	}

	/**
	*      Create self's action objects
	*/
	void makeActions() {
		Shell shell = getViewSite().getShell();

		openIncludeAction = new OpenIncludeAction (viewer);
		openFileAction = new OpenFileAction(getSite().getPage());
		openSystemEditorAction = new OpenSystemEditorAction(getSite().getPage());
		refreshAction = new RefreshAction(shell);
		buildAction = new BuildAction(shell, IncrementalProjectBuilder.INCREMENTAL_BUILD);
		rebuildAction = new BuildAction(shell, IncrementalProjectBuilder.FULL_BUILD);
		makeTargetAction = new MakeTargetAction(shell);
		moveResourceAction = new MoveResourceAction (shell);
		copyResourceAction = new CopyResourceAction(shell);
		renameResourceAction = new RenameResourceAction(shell, viewer.getTree());
		deleteResourceAction = new DeleteResourceAction(shell);
		
		IWorkspace workspace = CUIPlugin.getWorkspace();

		openProjectAction = new OpenResourceAction(shell);
		workspace.addResourceChangeListener(openProjectAction, IResourceChangeEvent.POST_CHANGE);
		closeProjectAction = new CloseResourceAction(shell);
		workspace.addResourceChangeListener(closeProjectAction, IResourceChangeEvent.POST_CHANGE);

		//sortByNameAction = new SortViewAction(this, false);
		//sortByTypeAction = new SortViewAction(this, true);
		patternFilterAction = new FilterSelectionAction(shell, this, "Filters...");
		clibFilterAction = new ShowLibrariesAction(shell, this, "Show Referenced Libs");

		wsFilterAction = new NewWorkingSetFilterAction(getViewSite().getShell(), this, "Select Working Set...");
		wsClearFilterAction = new AdjustWorkingSetFilterAction("Deselect Working Set", null, workingSetFilter);

		goIntoAction = new GoIntoAction(frameList);
		backAction = new BackAction(frameList);
		forwardAction = new ForwardAction(frameList);
		upAction = new UpAction(frameList);

		addBookmarkAction = new AddBookmarkAction(shell);
		//propertyDialogAction = new PropertyDialogAction(shell, viewer);
		propertyDialogAction = new PropertyDialogAction(shell,
		new ISelectionProvider () {
			public void addSelectionChangedListener(ISelectionChangedListener listener)  {
				viewer.addSelectionChangedListener (listener);
			}
			public ISelection getSelection()  {
				return convertSelection (viewer.getSelection ());
			}
			public void removeSelectionChangedListener(ISelectionChangedListener listener) {
				viewer.removeSelectionChangedListener (listener);
			}
			public void setSelection(ISelection selection)  {
				viewer.setSelection (selection);
			}
		});

		IActionBars actionBars = getViewSite().getActionBars();
		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.DELETE, deleteResourceAction);
		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.BOOKMARK, addBookmarkAction);
		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.REFRESH, refreshAction);
		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.BUILD_PROJECT, buildAction);
		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.REBUILD_PROJECT, rebuildAction);
		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.OPEN_PROJECT, openProjectAction);
		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.CLOSE_PROJECT, closeProjectAction);

		collapseAllAction = new CollapseAllAction(this);
	}

	/**
	 * Updates all actions with the given selection.
	 * Necessary when popping up a menu, because some of the enablement criteria
	 * may have changed, even if the selection in the viewer hasn't.
	 * E.g. A project was opened or closed.
	 */
	void updateActions(IStructuredSelection selection) {
		makeTargetAction.selectionChanged(selection);
		copyResourceAction.selectionChanged(selection);
		refreshAction.selectionChanged(selection);
		moveResourceAction.selectionChanged(selection);
		openFileAction.selectionChanged(selection);
		openSystemEditorAction.selectionChanged(selection);
		propertyDialogAction.selectionChanged(selection);
		renameResourceAction.selectionChanged(selection);
		//sortByTypeAction.selectionChanged(selection);
		//sortByNameAction.selectionChanged(selection); 
	}
 
	/**
	 * Updates the global actions with the given selection.
	 * Be sure to invoke after actions objects have updated, since can* methods delegate to action objects.
	 */
	void updateGlobalActions(IStructuredSelection selection) {
		deleteResourceAction.selectionChanged(selection);
		addBookmarkAction.selectionChanged(selection);

		// Ensure Copy global action targets correct action,
		// either copyProjectAction or copyResourceAction,
		// depending on selection.
		copyResourceAction.selectionChanged(selection);
		IActionBars actionBars = getViewSite().getActionBars();
		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.COPY, copyResourceAction);
		actionBars.updateActionBars();
		renameResourceAction.selectionChanged(selection);
		
		refreshAction.selectionChanged(selection);
		buildAction.selectionChanged(selection);
		rebuildAction.selectionChanged(selection);
		openProjectAction.selectionChanged(selection);
		closeProjectAction.selectionChanged(selection);

	}


	//---- Action handling ----------------------------------------------------------

	IStructuredSelection convertSelection(ISelection s) {
		List converted = new ArrayList();
		if (s instanceof StructuredSelection) {
			Object[] elements= ((StructuredSelection)s).toArray();
			for (int i= 0; i < elements.length; i++) {
				Object e = elements[i];
				if (e instanceof IAdaptable) {
					IResource r = (IResource)((IAdaptable)e).getAdapter(IResource.class);
					if (r != null)
						converted.add(r);
				}
			}
		}
		return new StructuredSelection(converted.toArray());
	}

	IStructuredSelection convertSelectionToCElement(ISelection s) {
		List converted = new ArrayList();
		if (s instanceof StructuredSelection) {
			Object[] elements= ((StructuredSelection)s).toArray();
			for (int i= 0; i < elements.length; i++) {
				Object e = elements[i];
				if (e instanceof IAdaptable) {
					ICElement c = (ICElement)((IAdaptable)e).getAdapter(ICElement.class);
					if (c != null)
						converted.add(c);
				}
			}
		}
		return new StructuredSelection(converted.toArray());
	}

	/**
	* Called when the context menu is about to open.
	* Override to add your own context dependent menu contributions.
	*/
	public void menuAboutToShow(IMenuManager menu) {
		IStructuredSelection selection= (IStructuredSelection) viewer.getSelection();
		if (selection.isEmpty()) {
			new NewWizardMenu(menu, getSite().getWorkbenchWindow(), false);
			menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
			menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS+"-end"));//$NON-NLS-1$
			return;
		}

		updateActions (convertSelection(selection));
		//updateActions (selection);
		addNewMenu(menu, selection);
		menu.add(new Separator());
		addOpenMenu(menu, selection);
		menu.add(new Separator());
		addBuildMenu(menu, selection);
		menu.add(new Separator ());
		addRefreshMenu (menu, selection);
		menu.add(new Separator());
		addIOMenu(menu, selection);
		menu.add(new Separator());
		addBookMarkMenu (menu, selection);
		//menu.add(new Separator());
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS+"-end"));//$NON-NLS-1$
		addPropertyMenu(menu, selection);
	}

	void addNewMenu (IMenuManager menu, IStructuredSelection selection) {

		MenuManager newMenu = new MenuManager("New");
		IAdaptable element = (IAdaptable)selection.getFirstElement();
		IResource resource = (IResource)element.getAdapter(IResource.class);

		new NewWizardMenu(newMenu, getSite().getWorkbenchWindow(), false);

		menu.add(newMenu);

		if (resource == null)
			return;

		menu.add (new Separator ());
		if (selection.size() == 1 && resource instanceof IContainer) {
			menu.add(goIntoAction);
		}

		MenuManager gotoMenu = new MenuManager("GoTo");
		menu.add(gotoMenu);
		if (viewer.isExpandable(element)) {
			gotoMenu.add(backAction);
			gotoMenu.add(forwardAction);
			gotoMenu.add(upAction);
		}

	}

	void addOpenMenu(IMenuManager menu, IStructuredSelection selection) {
		IAdaptable element = (IAdaptable)selection.getFirstElement();
		IResource resource = (IResource)element.getAdapter(IResource.class);
		if (resource == null)
			return;

		// Create a menu flyout.
		//MenuManager submenu= new MenuManager("Open With"); //$NON-NLS-1$
		//submenu.add(new OpenWithMenu(getSite().getPage(), (IFile) resource));
		//menu.add(submenu);
		if (resource instanceof IFile)
			menu.add(openFileAction);

		fillOpenWithMenu(menu, selection);
		fillOpenToMenu(menu, selection);
	}


	void addBuildMenu(IMenuManager menu, IStructuredSelection selection) {
		IAdaptable element = (IAdaptable)selection.getFirstElement();
		IResource resource = (IResource)element.getAdapter(IResource.class);
		if (resource == null)
			return;

		//if (resource instanceof IProject) {
			// Allow manual incremental build only if auto build is off.
			//if (!ResourcesPlugin.getWorkspace().isAutoBuilding()) {
				//menu.add(buildAction);
			//}
		//}
		boolean isContainer = (resource instanceof IContainer);
		if (isContainer) {
			MakeTarget[] aBuild = new MakeTarget[1];
			aBuild[0] = new MakeTarget(resource, "all");
			menu.add(new MakeAction(aBuild, getViewSite().getShell(), "Build"));

			MakeTarget[] aReBuild = new MakeTarget[1];
			aReBuild[0] = new MakeTarget(resource, "clean all");
			menu.add(new MakeAction(aReBuild, getViewSite().getShell(), "Rebuild"));
			MakeTarget[] aClean = new MakeTarget[1];
			aClean[0] = new MakeTarget(resource, "clean");
			menu.add(new MakeAction(aClean, getViewSite().getShell(), "Clean"));
		
			MenuManager submenu= new MenuManager("Make");
			String [] directives = MakeUtil.getPersistentTargets(resource);
			if (directives.length > 0) {
				for (int i = 0; i < directives.length; i++) {
					MakeTarget[] a = new MakeTarget[1];
					a[0] = new MakeTarget(resource, directives[i]);
					submenu.add(new MakeAction(a, getViewSite().getShell(), directives[i]));
				}
			}
			menu.add(submenu);
		
			menu.add(makeTargetAction);
		}
	}

	void addRefreshMenu (IMenuManager menu, IStructuredSelection selection) {
		menu.add(refreshAction);
	}

	void addIOMenu (IMenuManager menu, IStructuredSelection selection) {
		IAdaptable element = (IAdaptable)selection.getFirstElement();
		IResource resource = (IResource)element.getAdapter(IResource.class);
		if (resource == null)
			return;

		menu.add(new Separator ());

		if (resource instanceof IProject) {
			menu.add(closeProjectAction);
		}

		if (resource instanceof IFile || resource instanceof IFolder) {
			menu.add(copyResourceAction);
			menu.add(moveResourceAction);
		}

		if (!(element instanceof IArchiveContainer || element instanceof IBinaryContainer)) {
			menu.add(renameResourceAction);
			menu.add(deleteResourceAction);
		}

	}

	void addBookMarkMenu (IMenuManager menu, IStructuredSelection selection) {
		IAdaptable element = (IAdaptable)selection.getFirstElement();
		IResource resource = (IResource)element.getAdapter(IResource.class);
		if (resource == null)
			return;
		if (resource instanceof IFile) {
			menu.add(addBookmarkAction);
		}
	}

	void addPropertyMenu (IMenuManager menu, IStructuredSelection selection) {
		propertyDialogAction.selectionChanged(convertSelection(selection));
		if (propertyDialogAction.isApplicableForSelection()) {
			menu.add(propertyDialogAction);
		}
	}


	/**
	 * Add "open with" actions to the context sensitive menu.
	 * @param menu the context sensitive menu
	 * @param selection the current selection in the project explorer
	 */
	void fillOpenWithMenu(IMenuManager menu, IStructuredSelection selection) {
		IAdaptable element = (IAdaptable)selection.getFirstElement();
		IResource resource = (IResource)element.getAdapter(IResource.class);
		if (resource == null)
			return;

		// If one file is selected get it.
		// Otherwise, do not show the "open with" menu.
		if (selection.size() != 1)
			return;

		if (!(resource instanceof IFile))
			return;

		// Create a menu flyout.
		MenuManager submenu = new MenuManager("Open With"); //$NON-NLS-1$
		submenu.add(new OpenWithMenu(getSite().getPage(), (IFile) resource));

		// Add the submenu.
		menu.add(submenu);
	}

	/**
	 * Add "open to" actions to the context sensitive menu.
	 * @param menu the context sensitive menu
	 * @param selection the current selection in the project explorer
	 */
	void fillOpenToMenu(IMenuManager menu, IStructuredSelection selection)
	{
		IAdaptable element = (IAdaptable)selection.getFirstElement();
		IResource resource = (IResource)element.getAdapter(IResource.class);
		if (resource == null)
			return;

		// If one file is selected get it.
		// Otherwise, do not show the "open with" menu.
		if (selection.size() != 1)
			return;

		if (!(resource instanceof IContainer))
			return;

		menu.add(new OpenInNewWindowAction(getSite().getWorkbenchWindow(), resource));
	}


	/**
	* Returns the tool tip text for the given element.
	*/
	String getToolTipText(Object element) {
		if (element instanceof IResource) {
			IPath path = ((IResource) element).getFullPath();
			if (path.isRoot()) {
				return "CVIEW"; //$NON-NLS-1$
			} else {
				return path.makeRelative().toString();
			}
		} else {
			return ((ILabelProvider) viewer.getLabelProvider()).getText(element);
													}
	}

	/**
	* Returns the message to show in the status line.
	*
	* @param selection the current selection
	* @return the status line message
	*/
	String getStatusLineMessage(IStructuredSelection selection) {
		if (selection.size() == 1) {
			Object o = selection.getFirstElement();
			if (o instanceof IResource) {
				return ((IResource) o).getFullPath().makeRelative().toString();
			} else if (o instanceof ICElement) {
				ICElement celement = (ICElement)o;
				IResource res  = (IResource)celement.getAdapter(IResource.class);
				if (res != null) {
					return res.getFullPath().toString();
				} else if (celement.getElementType() == ICElement.C_VCONTAINER) {					   
					if (celement instanceof IBinaryContainer) {
						ICProject cproj = celement.getCProject();
						if (cproj != null) {
							return cproj.getPath() + " - binaries";
						}
					} else if (celement instanceof IArchiveContainer) {
						ICProject cproj = celement.getCProject();
						if (cproj != null) {
							return cproj.getPath() + " - archives";
						}
					} else if (celement instanceof IBinaryModule) {
						IBinary bin = ((IBinaryModule)celement).getBinary();
						return bin.getPath() + ":" + celement.getElementName();
					}
				} else if (celement.getElementType() > ICElement.C_UNIT) {
					return celement.getPath().toString() + " - [" + celement.getElementName() +"]";
				}
				return celement.getElementName();
			} else {
				return "ItemSelected"; //$NON-NLS-1$
			}
		}
		if (selection.size() > 1) {
			return "StatusLine";
		}
		return "";//$NON-NLS-1$
	}

	void updateTitle () {
		Object input= getViewer().getInput();
		String viewName= getConfigurationElement().getAttribute("name"); //$NON-NLS-1$
		if (input == null || (input instanceof ICModel)) {
			setTitle(viewName);
			setTitleToolTip(""); //$NON-NLS-1$
		} else {
			ILabelProvider labelProvider = (ILabelProvider) getViewer().getLabelProvider();
			String inputText= labelProvider.getText(input);
			setTitle(inputText);
			setTitleToolTip(getToolTipText(input));
		}
	}

	/**
	 * Returns the tree viewer which shows the resource hierarchy.
	 */
	TreeViewer getResourceViewer() {
	        return viewer;
	}

	/**
	* Updates the message shown in the status line.
	*
	* @param selection the current selection
	*/
	void updateStatusLine(IStructuredSelection selection) {
		String msg = getStatusLineMessage(selection);
		getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
	}


	void fillActionBars() { 
		IActionBars actionBars= getViewSite().getActionBars();
		IToolBarManager toolBar = actionBars.getToolBarManager();
		toolBar.add(backAction);
		toolBar.add(forwardAction);
		toolBar.add(upAction);
		toolBar.add(new Separator());
		toolBar.add(collapseAllAction);
		actionBars.updateActionBars();

		IMenuManager menu = actionBars.getMenuManager();
		menu.add(wsFilterAction);
		menu.add(wsClearFilterAction);

		menu.add(new Separator());
		menu.add(new GroupMarker(WORKING_GROUP_MARKER));
		menu.add(new GroupMarker(WORKING_GROUP_MARKER_END));
		menu.add(new Separator());
		
		updateWorkingSetMenu();
				
		//menu.add (clibFilterAction);
		menu.add (patternFilterAction);
	}
	
	void updateWorkingSetMenu() {
		IMenuManager menu = getViewSite().getActionBars().getMenuManager();

		//Remove the previous entries
		for(int i = 0; i < adjustWorkingSetContributions.length; i++) {
			if(adjustWorkingSetContributions[i] != null) {
				menu.remove(adjustWorkingSetContributions[i]);
			}
		}

		//Find out what we are currently using
		String currentWorkingSetName = workingSetFilter.getWorkingSetName();

		//If we have no working set, then we can't disable it
		if(wsClearFilterAction != null) {
			wsClearFilterAction.setEnabled((currentWorkingSetName != null));
		}
		
		IWorkingSetManager manager = getViewSite().getWorkbenchWindow().getWorkbench().getWorkingSetManager();
		IWorkingSet recentsets [] = manager.getWorkingSets();
		for(int i = 0; i < adjustWorkingSetContributions.length; i++) {
			if(i < recentsets.length) {
				Action action = new AdjustWorkingSetFilterAction(recentsets[i].getName(),
															      recentsets[i].getName(),
																  workingSetFilter);
				adjustWorkingSetContributions[i] = new ActionContributionItem(action);
				if(currentWorkingSetName != null && 
				   currentWorkingSetName.equals(recentsets[i].getName())) {
					adjustWorkingSetContributions[i].getAction().setChecked(true);
				}
			} else {
				adjustWorkingSetContributions[i] = null;
			}
		}
		
		//Put the new entries in
		for(int i = 0; i < adjustWorkingSetContributions.length; i++) {
			if(adjustWorkingSetContributions[i] != null) {
				menu.appendToGroup(WORKING_GROUP_MARKER, adjustWorkingSetContributions[i]);
			}
		}
	}
	

	/**
	 * Sets the decorator for the package explorer.
	 *
	 * @param decorator a label decorator or <code>null</code> for no decorations.
	 */
	public void setLabelDecorator(ILabelDecorator decorator) {
		ILabelProvider cProvider= createLabelProvider();
		if (decorator == null) {
			viewer.setLabelProvider(cProvider);
		} else {
			viewer.setLabelProvider(new DecoratingLabelProvider(cProvider, decorator));
		}
	}

	public void propertyChange(PropertyChangeEvent event) {
		if (viewer == null)
			return;

		boolean refreshViewer= false;

		if (event.getProperty() == PreferenceConstants.PREF_SHOW_CU_CHILDREN) {
			boolean showCUChildren= CPluginPreferencePage.showCompilationUnitChildren();
			((CElementContentProvider)viewer.getContentProvider()).setProvideMembers(showCUChildren);
			refreshViewer= true;
		}

		if (refreshViewer)
			viewer.refresh();
	}

	/**
	 * Links to editor (if option enabled)
	 */
	void linkToEditor(IStructuredSelection selection) {
		if (!CPluginPreferencePage.isLinkToEditor()) {
			return;
		}
		// ignore selection changes if the package explorer is not the active part.
		// In this case the selection change isn't triggered by a user.
		if (!isActivePart())
			return;
		Object obj= selection.getFirstElement();

		if (selection.size() == 1) {
			if (obj instanceof ISourceReference) {
				ITranslationUnit tu = ((ISourceReference)obj).getTranslationUnit();
				if (tu != null) {
					IEditorPart part= EditorUtility.isOpenInEditor(obj);
					if (part != null) {
						IWorkbenchPage page= getSite().getPage();
						page.bringToTop(part);
						if (obj instanceof ICElement) {
							EditorUtility.revealInEditor(part, (ICElement) obj);
						}
					}
				}
			}
		}
		
	}

	private boolean isActivePart() {
		return this == getSite().getPage().getActivePart();
	}

	/* (non-Javadoc)
	 * @see IViewPartInputProvider#getViewPartInput()
	 */
	public Object getViewPartInput() {
		if (viewer != null) {
			return viewer.getInput();
		}
		return null;
	}

	public void collapseAll() {
		viewer.getControl().setRedraw(false);          
		viewer.collapseToLevel(getViewPartInput(), TreeViewer.ALL_LEVELS);
		viewer.getControl().setRedraw(true);
	}

	void restoreFilters() {
		// restore pattern filters
		IMemento filtersMem = memento.getChild(TAG_FILTERS);
		if (filtersMem != null) {
			IMemento children[]= filtersMem.getChildren(TAG_FILTER);
			String filters[]= new String[children.length];
			for (int i = 0; i < children.length; i++) {
				filters[i]= children[i].getString(TAG_ELEMENT);
			}
			getPatternFilter().setPatterns(filters);
		} else {
			getPatternFilter().setPatterns(new String[0]);
		}

		//restore library
		String show = memento.getString(TAG_SHOWLIBRARIES);
		if (show != null) {
			getLibraryFilter().setShowLibraries(show.equals("true")); //$NON-NLS-1$
		}
		else
			initFilterFromPreferences();

		//restore working set
		String wsname = memento.getString(TAG_WORKINGSET);
		if(wsname != null && workingSetFilter != null) {
			IWorkingSet set = getViewSite().getWorkbenchWindow().getWorkbench().getWorkingSetManager().getWorkingSet(wsname);
			if(set != null) {
				workingSetFilter.setWorkingSetName(wsname);
			}
		}
	}

	void restoreState(IMemento memento) {
		//ICelement container = CElementFactory.getDefault().getRoot();
		CoreModel factory = CoreModel.getDefault();
		IMemento childMem = memento.getChild(TAG_EXPANDED);
		if(childMem != null) {
			ArrayList elements = new ArrayList();
			IMemento[] elementMem = childMem.getChildren(TAG_ELEMENT);
			for (int i = 0; i < elementMem.length; i++){
				String p =  elementMem[i].getString(TAG_PATH);
				if (p != null) {
					IPath path = new Path(p);
					ICElement element = factory.create(path);
					if (element != null) {
						elements.add(element);
					}
				}
			}
			viewer.setExpandedElements(elements.toArray());
		}
		childMem = memento.getChild(TAG_SELECTION);
		if(childMem != null) {
			ArrayList list = new ArrayList();
			IMemento[] elementMem = childMem.getChildren(TAG_ELEMENT);
			for (int i = 0; i < elementMem.length; i++){
				String p = elementMem[i].getString(TAG_PATH);
				if (p != null) {
					IPath path = new Path(p);
					ICElement element = factory.create(path);
					if (element != null) {
						list.add(element);
					}
				}
			}
			viewer.setSelection(new StructuredSelection(list));
		}

		Tree tree = viewer.getTree();
		//save vertical position
		ScrollBar bar = tree.getVerticalBar();
		if (bar != null) {
			try {
				String posStr = memento.getString(TAG_VERTICAL_POSITION);
				int position;
				position = new Integer(posStr).intValue();
				bar.setSelection(position);
				position = new Integer(posStr).intValue();
				bar.setSelection(position);
			} catch (NumberFormatException e){}
		}
		bar = tree.getHorizontalBar();
		if (bar != null) {
			try {
				String posStr = memento.getString(TAG_HORIZONTAL_POSITION);
				int position;
				position = new Integer(posStr).intValue();
				bar.setSelection(position);
			} catch (NumberFormatException e){}
		}
	}

	public void saveState(IMemento memento) {
		if (viewer == null) {
			if (this.memento != null) { //Keep the old state;
				memento.putMemento(this.memento);
			}
			return;
		}

		//save expanded elements 
		Tree tree = viewer.getTree();
		Object expandedElements[] = viewer.getExpandedElements();
		if (expandedElements.length > 0) {
			IMemento expandedMem = memento.createChild(TAG_EXPANDED);
			for (int i = 0; i < expandedElements.length; i++) {
				Object o = expandedElements[i];
				// Do not save expanded binary files are libraries.
				if (o instanceof IParent
					&& ! (o instanceof IArchiveContainer || o instanceof IBinaryContainer
						|| o instanceof IBinary || o instanceof IArchive)) {
					IMemento elementMem = expandedMem.createChild(TAG_ELEMENT);
					ICElement e = (ICElement)o;
					IResource res = e.getResource();
					if (res != null) {
						elementMem.putString(TAG_PATH, res.getLocation().toOSString());
					}
				}
			}
		}

		//save selection
		Object elements[] = ((IStructuredSelection)viewer.getSelection()).toArray();
		if(elements.length > 0) {
			IMemento selectionMem = memento.createChild(TAG_SELECTION);
			for (int i = 0; i < elements.length; i++) {
				if (elements[i] instanceof ICElement) {
					ICElement e  = (ICElement)elements[i];
					IResource r  = e.getResource();
					if (r != null) {
						IMemento elementMem = selectionMem.createChild(TAG_ELEMENT);
						elementMem.putString(TAG_PATH,r.getLocation().toString());
					}
				}
			}
		}

		//save vertical position
		ScrollBar bar = tree.getVerticalBar();
		int position = bar != null ? bar.getSelection():0;
		memento.putString(TAG_VERTICAL_POSITION,String.valueOf(position));
		//save horizontal position
		bar = tree.getHorizontalBar(); 
		position = bar != null ? bar.getSelection():0;
		memento.putString(TAG_HORIZONTAL_POSITION,String.valueOf(position));

		//save filters
		String filters[] = getPatternFilter().getPatterns();
		if(filters.length > 0) {
			IMemento filtersMem = memento.createChild(TAG_FILTERS);
			for (int i = 0; i < filters.length; i++){
				IMemento child = filtersMem.createChild(TAG_FILTER);
				child.putString(TAG_ELEMENT,filters[i]);
			}
		}

		//save library filter
		boolean showLibraries= getLibraryFilter().getShowLibraries();
		String show= "true"; //$NON-NLS-1$
		if (!showLibraries)
			show= "false"; //$NON-NLS-1$
		memento.putString(TAG_SHOWLIBRARIES, show);

		String wsname = workingSetFilter.getWorkingSetName();
		if(wsname != null) {
			memento.putString(TAG_WORKINGSET, wsname);
		}
	}
}
