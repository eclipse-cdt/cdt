/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.cview;


import java.util.ArrayList;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IArchive;
import org.eclipse.cdt.core.model.IArchiveContainer;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.IBinaryContainer;
import org.eclipse.cdt.core.model.IBinaryModule;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementVisitor;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.model.IBufferFactory;
import org.eclipse.cdt.internal.ui.drag.DelegatingDragAdapter;
import org.eclipse.cdt.internal.ui.drag.FileTransferDragAdapter;
import org.eclipse.cdt.internal.ui.drag.LocalSelectionTransferDragAdapter;
import org.eclipse.cdt.internal.ui.drag.ResourceTransferDragAdapter;
import org.eclipse.cdt.internal.ui.drag.TransferDragSourceListener;
import org.eclipse.cdt.internal.ui.preferences.CPluginPreferencePage;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.internal.ui.util.ProblemTreeViewer;
import org.eclipse.cdt.ui.CElementContentProvider;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.cdt.ui.CElementSorter;
import org.eclipse.cdt.ui.CLocalSelectionTransfer;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
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
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.OpenEvent;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
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
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.part.ISetSelectionTarget;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.PluginTransfer;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.eclipse.ui.views.framelist.FrameList;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

/**
 * 
 * CView
 *  
 */
public class CView extends ViewPart implements ISetSelectionTarget, IPropertyChangeListener, IShowInTarget {

	ProblemTreeViewer viewer;
	IMemento memento;

	CViewActionGroup actionGroup;

	FrameList frameList;
	CViewFrameSource frameSource;

	ResourceWorkingSetFilter workingSetFilter = new ResourceWorkingSetFilter();

	private boolean dragDetected;
	private Listener dragDetectListener;

	private ICElement workingCopyTranslation = null;
	
	// Persistance tags.
	static final String TAG_SELECTION = "selection"; //$NON-NLS-1$
	static final String TAG_EXPANDED = "expanded"; //$NON-NLS-1$
	static final String TAG_ELEMENT = "element"; //$NON-NLS-1$
	static final String TAG_PATH = "path"; //$NON-NLS-1$
	static final String TAG_VERTICAL_POSITION = "verticalPosition"; //$NON-NLS-1$
	static final String TAG_HORIZONTAL_POSITION = "horizontalPosition"; //$NON-NLS-1$
	static final String TAG_WORKINGSET = "workingSet"; //$NON-NLS-1$

	//Menu tags
	final String WORKING_GROUP_MARKER = "workingSetGroup"; //$NON-NLS-1$
	final String WORKING_GROUP_MARKER_END = "end-workingSetGroup"; //$NON-NLS-1$

	private IPartListener partListener = new IPartListener() {

		public void partActivated(IWorkbenchPart part) {
			if (part instanceof IEditorPart) {
				editorActivated((IEditorPart) part);
			}
		}

		public void partBroughtToTop(IWorkbenchPart part) {
		}

		public void partClosed(IWorkbenchPart part) {
		}

		public void partDeactivated(IWorkbenchPart part) {
		}

		public void partOpened(IWorkbenchPart part) {
		}
	};

	private IPropertyChangeListener workingSetListener = new IPropertyChangeListener() {

		public void propertyChange(PropertyChangeEvent ev) {
			String property = ev.getProperty();
			Object newValue = ev.getNewValue();
			Object oldValue = ev.getOldValue();
			IWorkingSet filterWorkingSet = workingSetFilter.getWorkingSet();

			if (property == null) {
				return;
			}
			if (IWorkingSetManager.CHANGE_WORKING_SET_REMOVE.equals(property) && oldValue == filterWorkingSet) {
				setWorkingSet(null);
			} else if (IWorkingSetManager.CHANGE_WORKING_SET_NAME_CHANGE.equals(property) && newValue == filterWorkingSet) {
				updateTitle();
			} else if (IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE.equals(property) && newValue == filterWorkingSet) {
				getViewer().refresh();
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
	 * Reveal and select the passed element selection in self's visual
	 * component
	 * 
	 * @see ISetSelectionTarget#selectReveal()
	 */
	public void selectReveal(ISelection selection) {
		IStructuredSelection ssel = SelectionConverter.convertSelectionToCElements(selection);
		
		Object tempElement = ssel.getFirstElement();
		ICElement celement = null;
		if (tempElement instanceof ICElement){
			celement = (ICElement) tempElement;
		}
		
		//Check to see if the translation unit for this CElement
		//currently has a working copy - if it does we need to find
		//the corresponding element in the working copy in order for 
		//selectReveal to work properly
		
		if (celement != null){
			if (celement instanceof ITranslationUnit){
				IWorkingCopy wcopy = checkForWorkingCopy((ITranslationUnit) celement);
				if (wcopy != null)
					ssel = new StructuredSelection(new Object[]{wcopy});
			} else {
				IStructuredSelection tempSSel=getCElementFromTranslationUnit(celement);
				if (tempSSel != null)
					ssel = tempSSel;
			}
		}
		
		if (!ssel.isEmpty()) {
			getViewer().setSelection(ssel, true);
		}
	}

	/**
	 * @param unit
	 */
	private IWorkingCopy checkForWorkingCopy(ITranslationUnit unit) {
		IBufferFactory bufferFactory = CUIPlugin.getDefault().getBufferFactory();
		
		if (bufferFactory != null)
			return unit.findSharedWorkingCopy(bufferFactory);
		
		return null;
	}

	private void setElementForStructuredSelection(ICElement element){
		this.workingCopyTranslation = element;
	}
	
	private IStructuredSelection getCElementFromTranslationUnit(ICElement celement){
	
		//Check to see if we have a working copy in the model 
		//for this translation unit1
		ArrayList parents = new ArrayList();
		//Get translation unit for this element - find the TranslationUnit parent
		
		ICElement parent =celement.getParent();
		parents.add(parent);
		while (!(parent instanceof ITranslationUnit)){
			parent = parent.getParent();
			parents.add(parent);
			if (parent == null)
				break;
		}
		
		if (parent != null){
			final ArrayList finalParents = new ArrayList(parents);
			final ICElement finalCElement = celement;
			
			IWorkingCopy wcopy = checkForWorkingCopy((ITranslationUnit) parent);
			
			if (wcopy != null){
				ICElement[] celements = null;
				final ICElementVisitor visitor = new ICElementVisitor() {
					public boolean visit(ICElement element) throws CoreException {
						if (element instanceof IWorkingCopy)
							return true;
						
						ICElement parentStruct = ((ICElement) finalParents.get(0));
						if (element.getElementName().equals(parentStruct.getElementName()) &&
							element.getElementType() == parentStruct.getElementType()){
							return true;
						}
						
						if (finalCElement.getElementName().equals(element.getElementName()) &&
							finalCElement.getElementType() == element.getElementType()){
							setElementForStructuredSelection(element);
						}
						
						return false;
					}
				};
				try {
					wcopy.accept(visitor);
				} catch (CoreException e) {}
				 return new StructuredSelection(new Object[]{workingCopyTranslation});
			}
		}
	
		return null;
	}
	
	private ITreeViewerListener expansionListener = new ITreeViewerListener() {

		public void treeCollapsed(TreeExpansionEvent event) {
		}

		public void treeExpanded(TreeExpansionEvent event) {
			final Object element = event.getElement();
			if (element instanceof IParent) {
				//viewer.refresh (element);
				Control ctrl = viewer.getControl();
				if (ctrl != null && !ctrl.isDisposed()) {
					ctrl.getDisplay().asyncExec(new Runnable() {

						public void run() {
							Control ctrl = viewer.getControl();
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
	 * Handles an open event from the viewer.
	 * Opens an editor on the selected file.
	 *
	 * @param event the open event
	 */
	protected void handleOpen(OpenEvent event) {
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		getActionGroup().runDefaultAction(selection);
	}

	/**
	 * Handles double clicks in viewer. Opens editor if file double-clicked.
	 */
	protected void handleDoubleClick(DoubleClickEvent event) {
		IStructuredSelection s = (IStructuredSelection) event.getSelection();
		Object o = s.getFirstElement();
		if (viewer.isExpandable(o)) {
			// Do not drill in to translation units of binaries.
			if (o instanceof ITranslationUnit || o instanceof IBinary || o instanceof IArchive) {
				return;
			}
			viewer.setExpandedState(o, !viewer.getExpandedState(o));
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
	 * Handles a key release in the viewer. Does nothing by default.
	 *  
	 */
	protected void handleKeyReleased(KeyEvent event) {
		if (getActionGroup() != null) {
			getActionGroup().handleKeyReleased(event);
		}
	}

	/**
	 * Handles selection changed in viewer. Updates global actions. Links to
	 * editor (if option enabled)
	 */
	void handleSelectionChanged(SelectionChangedEvent event) {
		final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		updateStatusLine(selection);
		updateActionBars(selection);
		dragDetected = false;
		if (isLinkingEnabled()) {
			getSite().getShell().getDisplay().asyncExec(new Runnable() {

				public void run() {
					if (dragDetected == false) {
						// only synchronize with editor when the selection is
						// not the result
						// of a drag. Fixes bug 22274.
						linkToEditor(selection);
					}
				}
			});
		}
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
	 * @param actionGroup
	 *            the action group
	 */
	protected void setActionGroup(CViewActionGroup actionGroup) {
		this.actionGroup = actionGroup;
	}

	/**
	 * Answer the property defined by key.
	 */
	public Object getAdapter(Class key) {
		if (key.equals(ISelectionProvider.class)) {
			return viewer;
		} else if (key == IShowInSource.class) {
			return getShowInSource();
		} else if (key == IShowInTarget.class) {
			return this;
		}
		return super.getAdapter(key);
	}

	/*
	 * (non-Javadoc) Method declared on IViewPart.
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
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
	 * @param viewer
	 *            the viewer
	 */
	void initFilters(TreeViewer viewer) {
		viewer.addFilter(workingSetFilter);
	}

	/**
	 * Adds drag and drop support to the navigator.
	 */
	void initDragAndDrop() {
		int ops = DND.DROP_COPY | DND.DROP_MOVE;

		Transfer[] dragTransfers = new Transfer[] { ResourceTransfer.getInstance(), FileTransfer.getInstance(),
				CLocalSelectionTransfer.getInstance(), PluginTransfer.getInstance()};

		TransferDragSourceListener[] dragListeners = new TransferDragSourceListener[] { new ResourceTransferDragAdapter(viewer),
				new LocalSelectionTransferDragAdapter(viewer), new FileTransferDragAdapter(viewer)};

		viewer.addDragSupport(ops, dragTransfers, new DelegatingDragAdapter(viewer, dragListeners));

		Transfer[] dropTransfers = new Transfer[] { ResourceTransfer.getInstance(), FileTransfer.getInstance(),
				LocalSelectionTransfer.getInstance(), PluginTransfer.getInstance()};

		viewer.addDropSupport(ops, dropTransfers, new CViewDropAdapter(viewer));
		dragDetectListener = new Listener() {

			public void handleEvent(Event event) {
				dragDetected = true;
			}
		};
		viewer.getControl().addListener(SWT.DragDetect, dragDetectListener);

	}

	/**
	 * Initializes the default preferences
	 */
	public static void initDefaults(IPreferenceStore store) {
	}

	/**
	 * get the default preferences.
	 */
	void initFilterFromPreferences() {
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
		ILabelProvider cProvider = createLabelProvider();
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
	 * Restores the working set filter from the persistence store.
	 */
	void initWorkingSetFilter() {
		// FIXME: the memento does not work if we close the view
		// and reopen we should save this somewhere else.
		// but it goes to pretty much all the settings 8-(
		if (memento == null) {
			return;
		}
		String wsname = memento.getString(TAG_WORKINGSET);

		if (wsname != null && wsname.equals("") == false) { //$NON-NLS-1$
			IWorkingSetManager wsmanager = getViewSite().getWorkbenchWindow().getWorkbench().getWorkingSetManager();
			IWorkingSet workingSet = wsmanager.getWorkingSet(wsname);
			if (workingSet != null) {
				// Only initialize filter. Don't set working set into viewer.
				// Working set is set via WorkingSetFilterActionGroup
				// during action creation.
				workingSetFilter.setWorkingSet(workingSet);
			}
		}
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

		viewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				handleOpen(event);
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
	public void createPartControl(Composite parent) {

		viewer = createViewer(parent);
		viewer.setUseHashlookup(true);
		viewer.setComparer(new CViewElementComparer());
		initContentProvider(viewer);
		initLabelProvider(viewer);
		CUIPlugin.getDefault().getProblemMarkerManager().addListener(viewer);
		CUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);

		initFilters(viewer);
		initWorkingSetFilter();
		initListeners(viewer);
		initCElementSorter();
		initFrameList();
		initDragAndDrop();
		updateTitle();

		viewer.setInput(CoreModel.getDefault().getCModel());

		initContextMenu();

		//Add the property changes after all of the UI work has been done.
		IWorkingSetManager wsmanager = getViewSite().getWorkbenchWindow().getWorkbench().getWorkingSetManager();
		wsmanager.addPropertyChangeListener(workingSetListener);

		viewer.addTreeListener(expansionListener);

		// Needs to be done before the actions
		getSite().setSelectionProvider(viewer);
		getSite().getPage().addPartListener(partListener);

		// Make the Actions for the Context Menu
		makeActions();
		getActionGroup().fillActionBars(getViewSite().getActionBars());
		updateActionBars((IStructuredSelection) viewer.getSelection());

		if (memento != null) {
			getActionGroup().restoreFilterAndSorterState(memento);
		} else {
			initFilterFromPreferences();
		}


		if (memento != null) {
			restoreState(memento);
		}
		memento = null;

	}

	protected ProblemTreeViewer createViewer(Composite parent) {
		return new ProblemTreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
	}

	protected CElementContentProvider createContentProvider() {
		boolean showCUChildren = CPluginPreferencePage.showCompilationUnitChildren();
		return new CViewContentProvider(showCUChildren, true);
	}

	protected CElementLabelProvider createLabelProvider() {
		return new CViewLabelProvider();
	}

	/*
	 * (non-Javadoc) Method declared on IWorkbenchPart.
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

		Control control = viewer.getControl();
		if (dragDetectListener != null && control != null && control.isDisposed() == false) {
			control.removeListener(SWT.DragDetect, dragDetectListener);
		}

		super.dispose();
	}

	/**
	 * An editor has been activated. Set the selection in this navigator to be
	 * the editor's input, if linking is enabled.
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

	/**
	 * Returns the working set filter for this view.
	 * 
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
	public TreeViewer getViewer() {
		return viewer;
	}

	/*
	 */
	public FrameList getFrameList() {
		return frameList;
	}

	/**
	 * Create self's action objects
	 */
	void makeActions() {
		setActionGroup(new MainActionGroup(this));
	}

	/**
	 * Called when the context menu is about to open. Delegates to the action
	 * group using the viewer's selection as the action context.
	 * 
	 * @since 2.0
	 */
	protected void fillContextMenu(IMenuManager menu) {
		IStructuredSelection selection = (IStructuredSelection) getViewer().getSelection();
		CViewActionGroup actionGroup = getActionGroup();
		if (actionGroup != null) {
			actionGroup.setContext(new ActionContext(selection));
			actionGroup.fillContextMenu(menu);
			actionGroup.setContext(null);
		}
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
	 * @param selection
	 *            the current selection
	 * @return the status line message
	 */
	String getStatusLineMessage(IStructuredSelection selection) {
		if (selection.size() == 1) {
			Object o = selection.getFirstElement();
			if (o instanceof IResource) {
				return ((IResource) o).getFullPath().makeRelative().toString();
			} else if (o instanceof ICElement) {
				ICElement celement = (ICElement) o;
				IResource res = (IResource) celement.getAdapter(IResource.class);
				if (res != null) {
					return res.getFullPath().toString();
				} else if (celement.getElementType() == ICElement.C_VCONTAINER) {
					if (celement instanceof IBinaryContainer) {
						ICProject cproj = celement.getCProject();
						if (cproj != null) {
							return cproj.getPath() + CViewMessages.getString("CView.binaries"); //$NON-NLS-1$
						}
					} else if (celement instanceof IArchiveContainer) {
						ICProject cproj = celement.getCProject();
						if (cproj != null) {
							return cproj.getPath() + CViewMessages.getString("CView.archives"); //$NON-NLS-1$
						}
					} else if (celement instanceof IBinaryModule) {
						IBinary bin = ((IBinaryModule) celement).getBinary();
						return bin.getPath() + ":" + celement.getElementName(); //$NON-NLS-1$
					}
				} else if (celement.getElementType() > ICElement.C_UNIT) {
					return celement.getPath().toString() + " - [" + celement.getElementName() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
				}
				return celement.getElementName();
			} else if (o instanceof IWorkbenchAdapter) {
				IWorkbenchAdapter wAdapter = (IWorkbenchAdapter)o;
				return wAdapter.getLabel(o);
			} else {
				return "ItemSelected"; //$NON-NLS-1$
			}
		}
		if (selection.size() > 1) {
			return "StatusLine"; //$NON-NLS-1$
		}
		return "";//$NON-NLS-1$
	}

	/**
	 * Updates the action bar actions.
	 * 
	 * @param selection
	 *            the current selection
	 */
	protected void updateActionBars(IStructuredSelection selection) {
		CViewActionGroup group = getActionGroup();
		if (group != null) {
			group.setContext(new ActionContext(selection));
			group.updateActionBars();
		}
	}

	void updateTitle() {
		Object input = getViewer().getInput();
		String viewName = getConfigurationElement().getAttribute("name"); //$NON-NLS-1$
		if (input == null || (input instanceof ICModel)) {
			setTitle(viewName);
			setTitleToolTip(""); //$NON-NLS-1$
		} else {
			ILabelProvider labelProvider = (ILabelProvider) getViewer().getLabelProvider();
			String inputText = labelProvider.getText(input);
			setTitle(inputText);
			setTitleToolTip(getToolTipText(input));
		}
	}

	/**
	 * Updates the message shown in the status line.
	 * 
	 * @param selection
	 *            the current selection
	 */
	void updateStatusLine(IStructuredSelection selection) {
		String msg = getStatusLineMessage(selection);
		getViewSite().getActionBars().getStatusLineManager().setMessage(msg);
	}

	/*
	 */
	public void setWorkingSet(IWorkingSet workingSet) {
		TreeViewer treeViewer = getViewer();
		Object[] expanded = treeViewer.getExpandedElements();
		ISelection selection = treeViewer.getSelection();

		workingSetFilter.setWorkingSet(workingSet);
		/*
		 * if (workingSet != null) { settings.put(STORE_WORKING_SET,
		 * workingSet.getName()); } else { settings.put(STORE_WORKING_SET, "");
		 * //$NON-NLS-1$ }
		 */
		updateTitle();
		treeViewer.refresh();
		treeViewer.setExpandedElements(expanded);
		if (selection.isEmpty() == false && selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			treeViewer.reveal(structuredSelection.getFirstElement());
		}
	}

	/**
	 * Sets the decorator for the package explorer.
	 * 
	 * @param decorator
	 *            a label decorator or <code>null</code> for no decorations.
	 */
	public void setLabelDecorator(ILabelDecorator decorator) {
		ILabelProvider cProvider = createLabelProvider();
		viewer.setLabelProvider(new DecoratingLabelProvider(cProvider, decorator));
	}

	public void propertyChange(PropertyChangeEvent event) {
		if (viewer == null) return;

		boolean refreshViewer = false;
		String property = event.getProperty();

		if (property.equals(PreferenceConstants.PREF_SHOW_CU_CHILDREN)) {
			boolean showCUChildren = CPluginPreferencePage.showCompilationUnitChildren();
			((CElementContentProvider) viewer.getContentProvider()).setProvideMembers(showCUChildren);
			refreshViewer = true;
		} else if (property.equals(PreferenceConstants.PREF_LINK_TO_EDITOR)) {
			CViewActionGroup group = getActionGroup();
			if (group instanceof MainActionGroup) {
				boolean enable = isLinkingEnabled();
				((MainActionGroup)group).toggleLinkingAction.setChecked(enable);
			}
		}

		if (refreshViewer) {
			viewer.refresh();
		}
	}

	/**
	 * Returns whether the navigator selection automatically tracks the active
	 * editor.
	 * 
	 * @return <code>true</code> if linking is enabled, <code>false</code>
	 *         if not
	 */
	public boolean isLinkingEnabled() {
		return CPluginPreferencePage.isLinkToEditor();
	}

	public void setLinkingEnabled(boolean enable) {
		CPluginPreferencePage.setLinkingEnabled(enable);
	}

	/**
	 * Links to editor (if option enabled)
	 */
	void linkToEditor(IStructuredSelection selection) {
		// ignore selection changes if the package explorer is not the active
		// part.
		// In this case the selection change isn't triggered by a user.
		if (!isActivePart()) {
			return;
		}
		if (selection.size() == 1) {
			Object obj = selection.getFirstElement();
			if (obj instanceof ISourceReference) {
				ITranslationUnit tu = ((ISourceReference) obj).getTranslationUnit();
				if (tu != null) {
					IEditorPart part = EditorUtility.isOpenInEditor(obj);
					if (part != null) {
						IWorkbenchPage page = getSite().getPage();
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

	/*
	 * (non-Javadoc)
	 * 
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

	void restoreState(IMemento memento) {
		
		CoreModel factory = CoreModel.getDefault();
		
		getActionGroup().restoreFilterAndSorterState(memento);

		IMemento childMem = memento.getChild(TAG_EXPANDED);
		if (childMem != null) {
			ArrayList elements = new ArrayList();
			IMemento[] elementMem = childMem.getChildren(TAG_ELEMENT);
			for (int i = 0; i < elementMem.length; i++) {
				String p = elementMem[i].getString(TAG_PATH);
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
		if (childMem != null) {
			ArrayList list = new ArrayList();
			IMemento[] elementMem = childMem.getChildren(TAG_ELEMENT);
			for (int i = 0; i < elementMem.length; i++) {
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
			} catch (NumberFormatException e) {
			}
		}
		bar = tree.getHorizontalBar();
		if (bar != null) {
			try {
				String posStr = memento.getString(TAG_HORIZONTAL_POSITION);
				int position;
				position = new Integer(posStr).intValue();
				bar.setSelection(position);
			} catch (NumberFormatException e) {
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
						&& !(o instanceof IArchiveContainer || o instanceof IBinaryContainer || o instanceof IBinary || o instanceof IArchive)) {
					IMemento elementMem = expandedMem.createChild(TAG_ELEMENT);
					ICElement e = (ICElement) o;
					IResource res = e.getResource();
					if (res != null) {
						elementMem.putString(TAG_PATH, res.getLocation().toOSString());
					}
				}
			}
		}

		//save selection
		Object elements[] = ((IStructuredSelection) viewer.getSelection()).toArray();
		if (elements.length > 0) {
			IMemento selectionMem = memento.createChild(TAG_SELECTION);
			for (int i = 0; i < elements.length; i++) {
				if (elements[i] instanceof ICElement) {
					ICElement e = (ICElement) elements[i];
					IResource r = e.getResource();
					if (r != null) {
						IMemento elementMem = selectionMem.createChild(TAG_ELEMENT);
						elementMem.putString(TAG_PATH, r.getLocation().toString());
					}
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

		getActionGroup().saveFilterAndSorterState(memento);
		//Save the working set away
		if (workingSetFilter.getWorkingSet() != null) {
			String wsname = workingSetFilter.getWorkingSet().getName();
			if (wsname != null) {
				memento.putString(TAG_WORKINGSET, wsname);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.IShowInTarget#show(org.eclipse.ui.part.ShowInContext)
	 */
	public boolean show(ShowInContext context) {
		IEditorInput input = (IEditorInput) context.getInput();
		if (input != null) {
			IResource res = (IResource) input.getAdapter(IResource.class);
			if (res != null) {
				selectReveal(new StructuredSelection(res));
				return true;
			}
		}
		ISelection selection= context.getSelection();
		if (selection != null) {
			selectReveal(selection);
			return true;
		}
		return false;
	}
 
	/**
	 * Returns the <code>IShowInSource</code> for this view.
	 */
	protected IShowInSource getShowInSource() {
		return new IShowInSource() {
			public ShowInContext getShowInContext() {
				return new ShowInContext(getViewer().getInput(), getViewer().getSelection());
			}
		};
	}

}
