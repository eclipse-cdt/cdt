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
import org.eclipse.cdt.internal.ui.StandardCElementLabelProvider;
import org.eclipse.cdt.internal.ui.drag.DelegatingDragAdapter;
import org.eclipse.cdt.internal.ui.drag.FileTransferDragAdapter;
import org.eclipse.cdt.internal.ui.drag.LocalSelectionTransferDragAdapter;
import org.eclipse.cdt.internal.ui.drag.ResourceTransferDragAdapter;
import org.eclipse.cdt.internal.ui.drag.TransferDragSourceListener;
import org.eclipse.cdt.internal.ui.preferences.CPluginPreferencePage;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.internal.ui.util.ProblemTreeViewer;
import org.eclipse.cdt.ui.CElementContentProvider;
import org.eclipse.cdt.ui.CElementSorter;
import org.eclipse.cdt.ui.CLocalSelectionTransfer;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
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
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ResourceWorkingSetFilter;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.WorkingSetFilterActionGroup;
import org.eclipse.ui.part.ISetSelectionTarget;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.PluginTransfer;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.framelist.FrameList;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

/**
 * 
 * CView
 *
 */
public class CView extends ViewPart implements ISetSelectionTarget,
	IPropertyChangeListener, IShowInTarget {

	ProblemTreeViewer viewer;
	IMemento memento;

	CViewActionGroup actionGroup;
	WorkingSetFilterActionGroup wsFilterActionGroup;
	
	FrameList frameList;
	CViewFrameSource frameSource;

	CLibFilter clibFilter = new CLibFilter ();
	CPatternFilter patternFilter = new CPatternFilter ();
	
	ResourceWorkingSetFilter workingSetFilter = new ResourceWorkingSetFilter();
	
	ActionContributionItem adjustWorkingSetContributions [] = new ActionContributionItem[5];

	
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
	static final String TAG_SORTER = "sorter"; //$NON-NLS-1$

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
		private void doViewerUpdate() {
			viewer.getControl().setRedraw(false);
			viewer.refresh();
			viewer.getControl().setRedraw(true);
		}

		public void propertyChange(PropertyChangeEvent ev) {
			String prop = ev.getProperty();
			if(prop == null) {
				return;
			}
			if(prop.equals(WorkingSetFilterActionGroup.CHANGE_WORKING_SET)) {
				workingSetFilter.setWorkingSet((IWorkingSet)ev.getNewValue());
				doViewerUpdate();
			} else if(prop.equals(IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE)){
				if(ev.getOldValue() instanceof IWorkingSet && workingSetFilter.getWorkingSet() != null) {
					if(workingSetFilter.getWorkingSet().equals(ev.getOldValue())) {
						doViewerUpdate();
					}
				}
			} else if(prop.equals(IWorkingSetManager.CHANGE_WORKING_SET_REMOVE)) {
				if(ev.getOldValue() instanceof IWorkingSet && workingSetFilter.getWorkingSet() != null) {
					if(workingSetFilter.getWorkingSet().equals(ev.getOldValue())) {
						workingSetFilter.setWorkingSet(null);
						doViewerUpdate();
					}
				}
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
			getViewer().setSelection(ssel, true);
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
		if (getActionGroup() != null) {
			getActionGroup().handleKeyPressed(event);
		}
	}

        /**
         * Handles a key release in the viewer.  Does nothing by default.
         *
         */
        protected void handleKeyReleased(KeyEvent event) {
        }

	/**
	 * Handles selection changed in viewer.
	 * Updates global actions.
	 * Links to editor (if option enabled)
	 */
	void handleSelectionChanged(SelectionChangedEvent event) {
		IStructuredSelection sel = (IStructuredSelection) event.getSelection();
		updateStatusLine(sel);
		if (getActionGroup() != null) {
			getActionGroup().runDefaultAction(sel);
		}
		linkToEditor(sel);
	}

	/**
	 * Returns the action group.
	 *
	 * @return the action group
	 */
	protected CViewActionGroup getActionGroup() {
		return actionGroup;
	}

	/**
	 * Sets the action group.
	 *
	 * @param actionGroup the action group
	 */
	protected void setActionGroup(CViewActionGroup actionGroup) {
		this.actionGroup = actionGroup;
	}
	
	/**
	* Answer the property defined by key.
	*/
	public Object getAdapter(Class key) {
		if (key.equals(ISelectionProvider.class))
			return viewer;
		return super.getAdapter(key);
	}

	/* (non-Javadoc)
	 * Method declared on IViewPart.
	 */
	public void init(IViewSite site,IMemento memento) throws PartInitException {
		super.init(site,memento);
		this.memento = memento;
	}

	/**
	 * init the frame source and the framelist.
	 */
	void initFrameList() {
		frameSource = new CViewFrameSource(this);
		frameList = new FrameList(frameSource);
		frameSource.connectTo(frameList);
	}

	/**
	 * Initializes the sorter.
	 */
	void initCElementSorter() {
		viewer.setSorter(new CElementSorter());
	}

	/**
	 * Adds the filters to the viewer.
	 *
	 * @param viewer the viewer
	 */
	void initFilters(TreeViewer viewer) {
		viewer.addFilter(patternFilter);
		viewer.addFilter(workingSetFilter);
		//viewer.addFilter(clibFilter);
	}

	/**
	 * Adds drag and drop support to the navigator.
	 */
	void initDragAndDrop() {
		int ops = DND.DROP_COPY | DND.DROP_MOVE;

		Transfer[] dragTransfers =
			new Transfer[] {
				ResourceTransfer.getInstance(),
				FileTransfer.getInstance(),
				CLocalSelectionTransfer.getInstance(),
				PluginTransfer.getInstance()};

		TransferDragSourceListener[] dragListeners =
			new TransferDragSourceListener[] {
				new ResourceTransferDragAdapter(viewer),
				new LocalSelectionTransferDragAdapter(viewer),
				new FileTransferDragAdapter(viewer)};

		viewer.addDragSupport(ops, dragTransfers, new DelegatingDragAdapter(viewer, dragListeners));

		Transfer[] dropTransfers =
			new Transfer[] {
				ResourceTransfer.getInstance(),
				FileTransfer.getInstance(),
				LocalSelectionTransfer.getInstance(),
				PluginTransfer.getInstance()};

		viewer.addDropSupport(ops, dropTransfers, new CViewDropAdapter(viewer));

	}

	/** 
	 * Initializes the default preferences
	 */
	public static void initDefaults(IPreferenceStore store) {
		store.setDefault(TAG_SHOWLIBRARIES, true);
	}

	/**
	 * get the default preferences.
	 */
	void initFilterFromPreferences() {
		CUIPlugin plugin = CUIPlugin.getDefault();
		boolean show = plugin.getPreferenceStore().getBoolean(TAG_SHOWLIBRARIES);
		getLibraryFilter().setShowLibraries(show);
	}

	/**
	 * Sets the content provider for the viewer.
	 */
	void initContentProvider(TreeViewer viewer) {
		CElementContentProvider provider = createContentProvider();
		viewer.setContentProvider(provider);
	}
	
	/**
	 * Sets the label provider for the viewer.
	 */
	void initLabelProvider(TreeViewer viewer) {
		ILabelProvider cProvider= createLabelProvider();
		ILabelDecorator decorator = CUIPlugin.getDefault().getWorkbench().getDecoratorManager().getLabelDecorator();
		viewer.setLabelProvider(new DecoratingLabelProvider(cProvider, decorator));
	}
	
	

	/**
	 * Initializes and registers the context menu.
	 */
	protected void initContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				CView.this.fillContextMenu(manager);
			}
		});
		TreeViewer viewer = getViewer();
		Menu menu = menuMgr.createContextMenu(viewer.getTree());
		viewer.getTree().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	/**
	 * Add listeners to the viewer.
	 */	
	protected void initListeners(TreeViewer viewer) {
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
			public void keyReleased(KeyEvent e) {
				handleKeyReleased(e);
			}
		});
	}

	/**
	* @see ContentOutlinePage#createControl
	*/
	public void createPartControl (Composite parent) {

		viewer = createViewer(parent);
		viewer.setUseHashlookup (true);
		initContentProvider(viewer);
		initLabelProvider(viewer);
		CUIPlugin.getDefault().getProblemMarkerManager().addListener(viewer);
		CUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);

		initFilters(viewer);
		initListeners(viewer);
		initCElementSorter();
		initFrameList();
		initDragAndDrop();
		updateTitle();

		if (memento != null) {
			restoreFilters();
		} else {
			initFilterFromPreferences();
		}

		viewer.setInput (CoreModel.getDefault().getCModel());

		initContextMenu();

		// Make the Actions for the Context Menu
		makeActions();
		getActionGroup().fillActionBars(getViewSite().getActionBars());

		
		//Add the property changes after all of the UI work has been done.
		IWorkingSetManager wsmanager = getViewSite().getWorkbenchWindow().getWorkbench().getWorkingSetManager();
		wsmanager.addPropertyChangeListener(workingSetListener);

		viewer.addTreeListener(expansionListener);

		getSite().setSelectionProvider(viewer);
		getSite().getPage().addPartListener(partListener);

		if (memento != null)
			restoreState (memento);
		memento = null;

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
		if (getActionGroup() != null) {
			getActionGroup().dispose();
		}
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
	 * @return the working set
	 */
	public IWorkingSet getWorkingSet() {
		return workingSetFilter.getWorkingSet();
	}

        /**
         * Returns the sorter.
         */
	public CElementSorter getSorter() {
		return (CElementSorter) getViewer().getSorter();
	}

	/**
	 * Returns the tree viewer which shows the resource hierarchy.
	 */
	public TreeViewer getViewer () {
		return viewer;
	}

	/*
	 */
	public FrameList getFrameList() {
		return frameList;
	}	

	/**
	 *      Create self's action objects
	 */
	void makeActions() {
		wsFilterActionGroup = new WorkingSetFilterActionGroup(getViewSite().getShell(), workingSetListener);
		setActionGroup(new MainActionGroup(this));
	}

	/**
	 * Called when the context menu is about to open.
	 * Delegates to the action group using the viewer's selection as the action context.
	 * @since 2.0
	 */
	protected void fillContextMenu(IMenuManager menu) {
		IStructuredSelection selection =
			(IStructuredSelection) getViewer().getSelection();
		getActionGroup().setContext(new ActionContext(selection));
		getActionGroup().fillContextMenu(menu);
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
	 * Updates the message shown in the status line.
	 *
	 * @param selection the current selection
	 */
	void updateStatusLine(IStructuredSelection selection) {
		String msg = getStatusLineMessage(selection);
		getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
	}

	/**
	 * Sets the decorator for the package explorer.
	 *
	 * @param decorator a label decorator or <code>null</code> for no decorations.
	 */
	public void setLabelDecorator(ILabelDecorator decorator) {
		ILabelProvider cProvider= createLabelProvider();
		viewer.setLabelProvider(new DecoratingLabelProvider(cProvider, decorator));
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
	}

	void restoreState(IMemento memento) {
		//Restore the working set before we re-build the tree
		String wsname = memento.getString(TAG_WORKINGSET);
		if(wsname != null) {
			IWorkingSetManager wsmanager = getViewSite().getWorkbenchWindow().getWorkbench().getWorkingSetManager();
			IWorkingSet set = wsmanager.getWorkingSet(wsname);
			wsFilterActionGroup.setWorkingSet(set);
		} else {
			wsFilterActionGroup.setWorkingSet(null);
		}

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
			} catch (NumberFormatException e){
			}
		}
		bar = tree.getHorizontalBar();
		if (bar != null) {
			try {
				String posStr = memento.getString(TAG_HORIZONTAL_POSITION);
				int position;
				position = new Integer(posStr).intValue();
				bar.setSelection(position);
			} catch (NumberFormatException e){
			}
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

		//Save the working set away
		if(workingSetFilter.getWorkingSet() != null) {
			String wsname = workingSetFilter.getWorkingSet().getName();
			if(wsname != null) {
				memento.putString(TAG_WORKINGSET, wsname);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IShowInTarget#show(org.eclipse.ui.part.ShowInContext)
	 */
	public boolean show(ShowInContext context) {
		//@@@ Do something with the selection later?
		//ISelection selection = context.getSelection();
		try {
			IEditorInput input = (IEditorInput)context.getInput();
			if(input != null) {
				IResource res = (IResource)input.getAdapter(IResource.class);
				if(res != null) {
					selectReveal(new StructuredSelection(res));
				}
			}
		} catch(Exception ex) {
			/* Ignore */
		}
		return false;
	}

}
