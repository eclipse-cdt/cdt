/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.browser.cbrowsing;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.browser.AllTypesCache;
import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.browser.ITypeReference;
import org.eclipse.cdt.core.browser.TypeSearchScope;
import org.eclipse.cdt.core.browser.TypeUtil;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementVisitor;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.internal.ui.browser.opentype.OpenTypeMessages;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;
import org.eclipse.cdt.internal.ui.util.ProblemTableViewer;
import org.eclipse.cdt.internal.ui.viewsupport.CElementLabels;
import org.eclipse.cdt.internal.ui.viewsupport.IViewPartInputProvider;
import org.eclipse.cdt.internal.ui.workingsets.WorkingSetFilterActionGroup;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IWorkingCopyManager;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.texteditor.ITextEditor;

public abstract class CBrowsingPart extends ViewPart implements IMenuListener, ISelectionListener, IViewPartInputProvider {

	private static final String TAG_SELECTED_ELEMENTS= "selectedElements"; //$NON-NLS-1$
	private static final String TAG_SELECTED_ELEMENT= "selectedElement"; //$NON-NLS-1$
	private static final String TAG_LOGICAL_PACKAGE= "logicalPackage"; //$NON-NLS-1$
	private static final String TAG_SELECTED_ELEMENT_PATH= "selectedElementPath"; //$NON-NLS-1$

	private LabelProvider fLabelProvider;
	private ILabelProvider fTitleProvider;
	private StructuredViewer fViewer;
	private IMemento fMemento;
	private TypeInfoComparator fTypeComparator;
	
	// Actions
	private WorkingSetFilterActionGroup fWorkingSetFilterActionGroup;
	private boolean fHasWorkingSetFilter= true;
	private boolean fHasCustomFilter= true;
//	private OpenEditorActionGroup fOpenEditorGroup;
//	private CCPActionGroup fCCPActionGroup;
//	private BuildActionGroup fBuildActionGroup;
	private ToggleLinkingAction fToggleLinkingAction;
//	protected CompositeActionGroup fActionGroups;


//	// Filters
//	private CustomFiltersActionGroup fCustomFiltersActionGroup;
	
	protected IWorkbenchPart fPreviousSelectionProvider;
	protected Object fPreviousSelectedElement;
		
	// Linking
	private boolean fLinkingEnabled;
		
	/*
	 * Ensure selection changed events being processed only if
	 * initiated by user interaction with this part.
	 */
	private boolean fProcessSelectionEvents= true;

	private IPartListener2 fPartListener= new IPartListener2() {
		public void partActivated(IWorkbenchPartReference ref) {
			setSelectionFromEditor(ref);
		}
		public void partBroughtToTop(IWorkbenchPartReference ref) {
			setSelectionFromEditor(ref);
		}
	 	public void partInputChanged(IWorkbenchPartReference ref) {
			setSelectionFromEditor(ref);
	 	}
		public void partClosed(IWorkbenchPartReference ref) {
		}
		public void partDeactivated(IWorkbenchPartReference ref) {
		}
		public void partOpened(IWorkbenchPartReference ref) {
		}
		public void partVisible(IWorkbenchPartReference ref) {
			if (ref != null && ref.getId() == getSite().getId()){
				fProcessSelectionEvents= true;
				IWorkbenchPage page= getSite().getWorkbenchWindow().getActivePage();
				if (page != null)
					selectionChanged(page.getActivePart(), page.getSelection());
		}
		}
		public void partHidden(IWorkbenchPartReference ref) {
			if (ref != null && ref.getId() == getSite().getId())
				fProcessSelectionEvents= false;
		}
	};

	public CBrowsingPart() {
		super();
		initLinkingEnabled();
	}

	/*
	 * Implements method from IViewPart.
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		fMemento= memento;
	}

	/*
	 * Implements method from IViewPart.
	 */
	public void saveState(IMemento memento) {
		if (fViewer == null) {
			// part has not been created
			if (fMemento != null) //Keep the old state;
				memento.putMemento(fMemento);
			return;
		}
		if (fHasWorkingSetFilter)
			fWorkingSetFilterActionGroup.saveState(memento);
//		if (fHasCustomFilter)
//			fCustomFiltersActionGroup.saveState(memento);
		saveSelectionState(memento);
		saveLinkingEnabled(memento);
	}	

	private void saveLinkingEnabled(IMemento memento) {
		memento.putInteger(getLinkToEditorKey(), fLinkingEnabled ? 1 : 0);
	}

	private void saveSelectionState(IMemento memento) {
/*		Object elements[]= ((IStructuredSelection) fViewer.getSelection()).toArray();
		if (elements.length > 0) {
			IMemento selectionMem= memento.createChild(TAG_SELECTED_ELEMENTS);
			for (int i= 0; i < elements.length; i++) {
				IMemento elementMem= selectionMem.createChild(TAG_SELECTED_ELEMENT);
				Object o= elements[i];
				if (o instanceof ICElement)
					elementMem.putString(TAG_SELECTED_ELEMENT_PATH, ((ICElement) elements[i]).getHandleIdentifier());
				else if (o instanceof LogicalPackage) {
					IPackageFragment[] packages=((LogicalPackage)o).getFragments();
					for (int j= 0; j < packages.length; j++) {
						IMemento packageMem= elementMem.createChild(TAG_LOGICAL_PACKAGE);
						packageMem.putString(TAG_SELECTED_ELEMENT_PATH, packages[j].getHandleIdentifier());
					}
				}
			}
		}
*/
	}

	protected void restoreState(IMemento memento) {
		if (fHasWorkingSetFilter)
			fWorkingSetFilterActionGroup.restoreState(memento);
//		if (fHasCustomFilter)
//			fCustomFiltersActionGroup.restoreState(memento);
			
		if (fHasCustomFilter || fHasWorkingSetFilter) {
			fViewer.getControl().setRedraw(false);
			fViewer.refresh();
			fViewer.getControl().setRedraw(true);
		}
	}	

	private ISelection restoreSelectionState(IMemento memento) {
/*
 		if (memento == null)
 			return null;

		IMemento childMem;
		childMem= memento.getChild(TAG_SELECTED_ELEMENTS);
		if (childMem != null) {
			ArrayList list= new ArrayList();
			IMemento[] elementMem= childMem.getChildren(TAG_SELECTED_ELEMENT);
			for (int i= 0; i < elementMem.length; i++) {
				String javaElementHandle= elementMem[i].getString(TAG_SELECTED_ELEMENT_PATH);
				if (javaElementHandle == null) {
					// logical package
					IMemento[] packagesMem= elementMem[i].getChildren(TAG_LOGICAL_PACKAGE);
					LogicalPackage lp= null;
					for (int j= 0; j < packagesMem.length; j++) {
						javaElementHandle= packagesMem[j].getString(TAG_SELECTED_ELEMENT_PATH);
						Object pack= (IPackageFragment)JavaCore.create(javaElementHandle);
						if (pack instanceof IPackageFragment && ((IPackageFragment)pack).exists()) {
							if (lp == null)
								lp= new LogicalPackage((IPackageFragment)pack);								
							else
								lp.add((IPackageFragment)pack);
						}
					}
					if (lp != null)
						list.add(lp);
				} else {
					ICElement element= JavaCore.create(javaElementHandle);
					if (element != null && element.exists())
						list.add(element);
				}
			}
			return new StructuredSelection(list);
		}
*/
		return null;
	}

	private void restoreLinkingEnabled(IMemento memento) {
		Integer val= memento.getInteger(getLinkToEditorKey());
		if (val != null) {
			fLinkingEnabled= val.intValue() != 0;
		}
	}

	/**
	 * Creates the search list inner viewer.
	 */
	public void createPartControl(Composite parent) {
		Assert.isTrue(fViewer == null);
		

		fTypeComparator= new TypeInfoComparator();

		// Setup viewer
		fViewer= createViewer(parent);

		initDragAndDrop();

		fLabelProvider= createLabelProvider();
		fViewer.setLabelProvider(fLabelProvider);
		
		fViewer.setSorter(createTypeInfoSorter());
		fViewer.setUseHashlookup(true);
		fTitleProvider= createTitleProvider();
		
		createContextMenu();
		getSite().setSelectionProvider(fViewer);

		if (fMemento != null) { // initialize linking state before creating the actions
			restoreLinkingEnabled(fMemento);
		}

		createActions(); // call before registering for selection changes
		addKeyListener();

		if (fMemento != null)
			restoreState(fMemento);

		getSite().setSelectionProvider(fViewer);
		
		// Status line
		IStatusLineManager slManager= getViewSite().getActionBars().getStatusLineManager();
		fViewer.addSelectionChangedListener(createStatusBarUpdater(slManager));
	
		
		hookViewerListeners();

		// Filters
		addFilters();

		// Initialize viewer input
		fViewer.setContentProvider(createContentProvider());
		setInitialInput();
				
		// Initialize selecton
		setInitialSelection();
		fMemento= null;		
		
		// Listen to page changes
		getViewSite().getPage().addPostSelectionListener(this);
		getViewSite().getPage().addPartListener(fPartListener);

		fillActionBars(getViewSite().getActionBars());
		
		setHelp();
	}
	
	/**
	 * Answer the property defined by key.
	 */
	public Object getAdapter(Class key) {
		if (key == IShowInSource.class) {
			return getShowInSource();
		}
		return super.getAdapter(key);
	}

	/**
	 * Returns the <code>IShowInSource</code> for this view.
	 */
	protected IShowInSource getShowInSource() {
		return new IShowInSource() {
			public ShowInContext getShowInContext() {
				return new ShowInContext(
					null,
				getSite().getSelectionProvider().getSelection());
			}
		};
	}

//	protected DecoratingLabelProvider createDecoratingLabelProvider(CUILabelProvider provider) {
////		XXX: Work in progress for problem decorator being a workbench decorator//
////		return new ExcludingDecoratingLabelProvider(provider, decorationMgr, "org.eclipse.jdt.ui.problem.decorator"); //$NON-NLS-1$
//		return new DecoratingCLabelProvider(provider);
//	}
	
	protected TypeInfoSorter createTypeInfoSorter() {
		return new TypeInfoSorter();
	}
	
	protected StatusBarUpdater createStatusBarUpdater(IStatusLineManager slManager) {
		return new StatusBarUpdater(slManager);
	}
	
	protected void createContextMenu() {
		MenuManager menuManager= new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuManager.setRemoveAllWhenShown(true);
		menuManager.addMenuListener(this);
		Menu contextMenu= menuManager.createContextMenu(fViewer.getControl());
		fViewer.getControl().setMenu(contextMenu);
		getSite().registerContextMenu(menuManager, fViewer);
	}

	protected void initDragAndDrop() {
/*		int ops= DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK;
		// drop
		Transfer[] dropTransfers= new Transfer[] {
			LocalSelectionTransfer.getInstance()
		};
		TransferDropTargetListener[] dropListeners= new TransferDropTargetListener[] {
			new SelectionTransferDropAdapter(fViewer)
		};
		fViewer.addDropSupport(ops | DND.DROP_DEFAULT, dropTransfers, new DelegatingDropAdapter(dropListeners));
		
		// Drag 
		Transfer[] dragTransfers= new Transfer[] {
			LocalSelectionTransfer.getInstance(), 
			ResourceTransfer.getInstance()};
		TransferDragSourceListener[] dragListeners= new TransferDragSourceListener[] {
			new SelectionTransferDragAdapter(fViewer),
			new ResourceTransferDragAdapter(fViewer)
		};
		fViewer.addDragSupport(ops, dragTransfers, new JdtViewerDragAdapter(fViewer, dragListeners));
*/	}
	
	protected void fillActionBars(IActionBars actionBars) {
		IToolBarManager toolBar= actionBars.getToolBarManager();
		fillToolBar(toolBar);
		

		if (fHasWorkingSetFilter)
			fWorkingSetFilterActionGroup.fillActionBars(getViewSite().getActionBars());		

		actionBars.updateActionBars();
	
//		fActionGroups.fillActionBars(actionBars);
//		
//		if (fHasCustomFilter)
//			fCustomFiltersActionGroup.fillActionBars(actionBars);
//			
		IMenuManager menu= actionBars.getMenuManager();
		menu.add(fToggleLinkingAction);
	}
	
	//---- IWorkbenchPart ------------------------------------------------------


	public void setFocus() {
		fViewer.getControl().setFocus();
	}
	
	public void dispose() {
		if (fViewer != null) {
			getViewSite().getPage().removePostSelectionListener(this);
			getViewSite().getPage().removePartListener(fPartListener);
			fViewer= null;
		}
//		if (fActionGroups != null)
//			fActionGroups.dispose();
		
		if (fWorkingSetFilterActionGroup != null) {
			fWorkingSetFilterActionGroup.dispose();
		}
		
		super.dispose();
	}
	
	/**
	 * Adds the KeyListener
	 */
	protected void addKeyListener() {
		fViewer.getControl().addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent event) {
				handleKeyReleased(event);
			}
		});
	}

	protected void handleKeyReleased(KeyEvent event) {
		if (event.stateMask != 0) 
			return;		
		
		int key= event.keyCode;
		if (key == SWT.F5) {
//			IAction action= fBuildActionGroup.getRefreshAction();
//			if (action.isEnabled())
//				action.run();
		}
	}
	
	//---- Adding Action to Toolbar -------------------------------------------
	
	protected void fillToolBar(IToolBarManager tbm) {
	}	

	/**
	 * Called when the context menu is about to open.
	 * Override to add your own context dependent menu contributions.
	 */
	public void menuAboutToShow(IMenuManager menu) {
		CUIPlugin.createStandardGroups(menu);
		
		IStructuredSelection selection= (IStructuredSelection) fViewer.getSelection();
		int size= selection.size();		
		Object element= selection.getFirstElement();
		
		if (size == 1)
			addOpenNewWindowAction(menu, element);
//		fActionGroups.setContext(new ActionContext(selection));
//		fActionGroups.fillContextMenu(menu);
//		fActionGroups.setContext(null);
	}
	
	private void addOpenNewWindowAction(IMenuManager menu, Object element) {
		if (element instanceof ICElement) {
			element= ((ICElement)element).getResource();
		}
		if (!(element instanceof IContainer))
			return;
//		menu.appendToGroup(
//			IContextMenuConstants.GROUP_OPEN, 
//			new PatchedOpenInNewWindowAction(getSite().getWorkbenchWindow(), (IContainer)element));
	}

	protected void createActions() {		
//		fActionGroups= new CompositeActionGroup(new ActionGroup[] {
//				new NewWizardsActionGroup(this.getSite()),
//				fOpenEditorGroup= new OpenEditorActionGroup(this), 
//				new OpenViewActionGroup(this), 
//				fCCPActionGroup= new CCPActionGroup(this), 
//				new GenerateActionGroup(this),
//				new RefactorActionGroup(this),
//				new ImportActionGroup(this),
//				fBuildActionGroup= new BuildActionGroup(this),
//				new JavaSearchActionGroup(this)});

		
		if (fHasWorkingSetFilter) {
			String viewId= getConfigurationElement().getAttribute("id"); //$NON-NLS-1$
			Assert.isNotNull(viewId);
			IPropertyChangeListener workingSetListener= new IPropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					doWorkingSetChanged(event);
				}
			};
			fWorkingSetFilterActionGroup= new WorkingSetFilterActionGroup(viewId, getShell(), workingSetListener);
			fViewer.addFilter(fWorkingSetFilterActionGroup.getWorkingSetFilter());
		}
	
//		// Custom filter group
//		if (fHasCustomFilter)
//			fCustomFiltersActionGroup= new CustomFiltersActionGroup(this, fViewer);
//	
		fToggleLinkingAction= new ToggleLinkingAction(this);
	}
	
	private void doWorkingSetChanged(PropertyChangeEvent event) {
		String property= event.getProperty();
		if (IWorkingSetManager.CHANGE_WORKING_SET_NAME_CHANGE.equals(property))
			updateTitle();
		else	if (IWorkingSetManager.CHANGE_WORKING_SET_CONTENT_CHANGE.equals(property)) {
			updateTitle();
			fViewer.getControl().setRedraw(false);
			fViewer.refresh();
			fViewer.getControl().setRedraw(true);
		}
			
	}
	
	
	/**
	 * Returns the shell to use for opening dialogs.
	 * Used in this class, and in the actions.
	 */
	Shell getShell() {
		return fViewer.getControl().getShell();
	}

	protected final Display getDisplay() {
		return fViewer.getControl().getDisplay();
	}	

	/**
	 * Returns the selection provider.
	 */
	ISelectionProvider getSelectionProvider() {
		return fViewer;
	}

	/**
	 * Answers if the given <code>element</code> is a valid
	 * input for this part.
	 * 
	 * @param 	element	the object to test
	 * @return	<true> if the given element is a valid input
	 */
	abstract protected boolean isValidInput(Object element);
	
	protected boolean exists(Object element) {
		if (element == null)
			return false;
		if (element instanceof ICElement)
			return ((ICElement)element).exists();
		if (element instanceof ITypeInfo)
			return ((ITypeInfo)element).exists();
		return false;
	}
	
	protected ICProject findCProject(ICElement element) {
		return element.getCProject();
	}

	protected ICProject findCProject(ITypeInfo info) {
		IProject project = info.getEnclosingProject();
		if (project != null) {
			try {
				ICProject[] cProjects = CoreModel.getDefault().getCModel().getCProjects();
				if (cProjects != null) {
					for (int i = 0; i < cProjects.length; ++i) {
						ICProject cProject = cProjects[i];
						if (cProject.getProject().equals(project))
							return cProject;
					}
				}
			} catch (CModelException e) {
			}
		}
		return null;
	}
	
	protected ISourceRoot findSourceRoot(ICElement element) {
		while (element != null) {
			if (element instanceof ISourceRoot)
				return (ISourceRoot)element;
			if (element instanceof ICProject)
				return null;
			element = element.getParent();
		}
		return null;
	}

	protected ISourceRoot findSourceRoot(ITypeInfo info) {
		ICProject cProject = findCProject(info);
		if (cProject != null) {
			try {
				ISourceRoot[] roots = cProject.getAllSourceRoots();
				if (roots != null) {
					for (int i = 0; i < roots.length; ++i) {
						ISourceRoot root = roots[i];
						if (!isProjectSourceRoot(root)) {
							TypeSearchScope scope = new TypeSearchScope();
							scope.add(root);
							if (info.isEnclosed(scope))
								return root;
						}
					}
				}
			} catch (CModelException e) {
			}
		}
		return null;
	}

	protected boolean isProjectSourceRoot(ISourceRoot root) {
		IResource resource= root.getResource();
		return (resource instanceof IProject);
	}

	protected boolean isValidNamespace(Object element) {
		if (element instanceof ITypeInfo) {
			ITypeInfo info = (ITypeInfo)element;
			if (info.exists() && info.getCElementType() == ICElement.C_NAMESPACE) {
				// make sure it has types other than namespaces
				ITypeInfo[] types = info.getEnclosedTypes();
				if (types != null) {
					for (int i = 0; i < types.length; ++i) {
						if (types[i].getCElementType() != ICElement.C_NAMESPACE) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	protected Object getNamespaceInput(Object element) {
		if (element instanceof ICModel) {
			return null;
		}

		if (element instanceof ICProject || element instanceof ISourceRoot) {
			if (exists(element))
				return element;
		}
		
		if (element instanceof ITypeInfo) {
			ITypeInfo info = (ITypeInfo)element;
			ISourceRoot root = findSourceRoot(info);
			if (exists(root))
				return root;
			ICProject cProject = findCProject(info);
			if (exists(cProject))
				return cProject;
		}
		
		if (element instanceof ICElement) {
			ICElement cElem = (ICElement)element;
			ISourceRoot root = findSourceRoot(cElem);
			if (exists(root))
				return root;
			ICProject cProject = findCProject(cElem);
			if (exists(cProject))
				return cProject;
		}
		
		return null;
	}
	
	private static boolean hasChild(final ICElement parent, final ICElement child) {
		final boolean foundChild[] = { false };
		final ICElementVisitor visitor = new ICElementVisitor() {
			public boolean visit(ICElement element) throws CoreException {
				if (foundChild[0])
					return false;
				if (element.equals(child)) {
					foundChild[0] = true;
					return false;
				}
				return true;
			}
		};
		try {
			parent.accept(visitor);
		} catch (CoreException e) {
		}
		return foundChild[0];
	}
	
	protected Object getTypesInput(Object element) {
		if (element instanceof ICModel || element instanceof ICProject || element instanceof ISourceRoot) {
			return null;
		}

		if (element instanceof ICElement) {
			//TODO optimization needed here - how do we get back parent ITypeInfo
			TypeSearchScope scope = new TypeSearchScope();
			ICElement cElem = ((ICElement)element).getParent();
			ISourceRoot root = findSourceRoot(cElem);
			if (root != null) {
				scope.add(root);
			} else {
				ICProject cProject = findCProject(cElem);
				if (cProject != null) {
					scope.add(cProject);
				}
			}
			ITypeInfo[] namespaces = AllTypesCache.getNamespaces(scope, true);
			if (namespaces != null) {
				for (int i = 0; i < namespaces.length; ++i) {
					ITypeInfo[] enclosedTypes = namespaces[i].getEnclosedTypes();
					for (int j = 0; j < enclosedTypes.length; ++j) {
						ITypeInfo enclosedType = enclosedTypes[j];
						if (enclosedType.getResolvedReference() != null) {
							ICElement typeElem = TypeUtil.getElementForType(enclosedType);
							if (typeElem != null && (typeElem.equals(cElem) || (typeElem instanceof IParent && hasChild(typeElem, cElem)))) {
								return namespaces[i];
							}
						}
					}
				}
			}
			return null;
		}

		if (element instanceof ITypeInfo) {
			ITypeInfo info = (ITypeInfo) element;
			if (info.getCElementType() == ICElement.C_NAMESPACE) {
				if (exists(info))
					return info;
			}
			ITypeInfo namespace = info.getEnclosingType(new int[]{ICElement.C_NAMESPACE});
			if (namespace == null) {
				namespace = info.getRootNamespace(true);
			}
			if (exists(namespace))
				return namespace;
		}

		return null;
	}

	protected Object getMembersInput(Object element) {
		if (element instanceof ICModel || element instanceof ICProject || element instanceof ISourceRoot) {
			return null;
		}

		if (element instanceof ITypeInfo) {
			ITypeInfo info = (ITypeInfo) element;
			if (info.getCElementType() != ICElement.C_NAMESPACE) {
				if (exists(info))
					return info;
			}
		}

		if (element instanceof ICElement) {
			//TODO optimization needed here - how do we get back parent ITypeInfo
			TypeSearchScope scope = new TypeSearchScope();
			ICElement cElem = ((ICElement)element).getParent();
			ISourceRoot root = findSourceRoot(cElem);
			if (root != null) {
				scope.add(root);
			} else {
				ICProject cProject = findCProject(cElem);
				if (cProject != null) {
					scope.add(cProject);
				}
			}
			ITypeInfo[] namespaces = AllTypesCache.getNamespaces(scope, true);
			if (namespaces != null) {
				for (int i = 0; i < namespaces.length; ++i) {
					ITypeInfo[] enclosedTypes = namespaces[i].getEnclosedTypes();
					for (int j = 0; j < enclosedTypes.length; ++j) {
						ITypeInfo enclosedType = enclosedTypes[j];
						if (enclosedType.getResolvedReference() != null) {
							ICElement typeElem = TypeUtil.getElementForType(enclosedType);
							if (typeElem != null && (typeElem.equals(cElem) || (typeElem instanceof IParent && hasChild(typeElem, cElem)))) {
								return enclosedType;
							}
							
						}
					}
				}
			}
			return null;
		}

		return null;
	}

	/**
	 * Answers if the given <code>element</code> is a valid
	 * element for this part.
	 * 
	 * @param 	element	the object to test
	 * @return	<true> if the given element is a valid element
	 */
	abstract protected boolean isValidElement(Object element);
//		if (element == null)
//			return false;
////		element= getSuitableCElement(element);
////		if (element == null)
////			return false;
//		Object input= getViewer().getInput();
//		if (input == null)
//			return false;
//		if (input instanceof Collection)
//			return ((Collection)input).contains(element);
//		else
//			return input.equals(element);
//
//	}

	private boolean isInputResetBy(Object newInput, Object input, IWorkbenchPart part) {
		if (newInput == null)
			return part == fPreviousSelectionProvider;
			
		if (input instanceof ICElement && newInput instanceof ICElement)
			return getTypeComparator().compare(newInput, input)  > 0;

		if((newInput instanceof List) && (part instanceof NamespacesView))
			return true;
		else
			return false;
	}

	private boolean isInputResetBy(IWorkbenchPart part) {
		if (!(part instanceof CBrowsingPart))
			return true;
		Object thisInput= getViewer().getInput();
		Object partInput= ((CBrowsingPart)part).getViewer().getInput();
		
		if(thisInput instanceof Collection)
			thisInput= ((Collection)thisInput).iterator().next();

		if(partInput instanceof Collection)
			partInput= ((Collection)partInput).iterator().next();
			
		if ((thisInput instanceof ICElement || thisInput instanceof ITypeInfo)
			&& (partInput instanceof ICElement || partInput instanceof ITypeInfo))
			return getTypeComparator().compare(partInput, thisInput) > 0;
		else
			return true;
	}

	protected boolean isAncestorOf(Object ancestor, Object element) {
		if (element instanceof ICElement && ancestor instanceof ICElement)
			return !element.equals(ancestor) && internalIsAncestorOf((ICElement)ancestor, (ICElement)element);
		if (element instanceof ITypeInfo) {
			if (ancestor instanceof ISourceRoot || ancestor instanceof ICProject || ancestor instanceof ICModel) {
				ICProject cProject = ((ICElement)ancestor).getCProject();
				if (cProject != null) {
					IProject proj = (((ITypeInfo)element).getEnclosingProject());
					return (proj != null && proj.equals(cProject.getProject()));
				}
			}
			if (ancestor instanceof ITypeInfo) {
				return ((ITypeInfo)ancestor).encloses(((ITypeInfo)element));
			}
		}
		return false;
	}
	
	private boolean internalIsAncestorOf(ICElement ancestor, ICElement element) {
		if (element != null)
			return element.equals(ancestor) || internalIsAncestorOf(ancestor, element.getParent());
		else
			return false;
	}

	private boolean isSearchResultView(IWorkbenchPart part) {
//		return SearchUtil.isSearchPlugInActivated() && (part instanceof ISearchResultView);
		return false;
	}

	protected boolean needsToProcessSelectionChanged(IWorkbenchPart part, ISelection selection) {
		if (!fProcessSelectionEvents || part == this || isSearchResultView(part)){
			if (part == this)
				fPreviousSelectionProvider= part;
			return false;
		}
		return true;
	}
	
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (!needsToProcessSelectionChanged(part, selection))
			return;
		
		if (fToggleLinkingAction.isChecked() && (part instanceof ITextEditor)) {
			setSelectionFromEditor(part, selection);
			return;
		}

		if (!(selection instanceof IStructuredSelection))
			return;
	
		// Set selection
		Object selectedElement= getSingleElementFromSelection(selection);
		
		if (selectedElement != null && (part == null || part.equals(fPreviousSelectionProvider)) && selectedElement.equals(fPreviousSelectedElement))
			return;

		fPreviousSelectedElement= selectedElement;

		Object currentInput= getViewer().getInput();		
		if (selectedElement != null && selectedElement.equals(currentInput)) {
			Object elementToSelect= findElementToSelect(selectedElement);
			if (elementToSelect != null && getTypeComparator().compare(selectedElement, elementToSelect) < 0)
				setSelection(new StructuredSelection(elementToSelect), true);
			else if (elementToSelect == null && (this instanceof MembersView)) {
				setSelection(StructuredSelection.EMPTY, true);
				fPreviousSelectedElement= StructuredSelection.EMPTY;
			}
			fPreviousSelectionProvider= part;
			return;
		}
		
		// Clear input if needed
		if (part != fPreviousSelectionProvider && selectedElement != null && !selectedElement.equals(currentInput) && isInputResetBy(selectedElement, currentInput, part)) {
			if (!isAncestorOf(selectedElement, currentInput))
				setInput(null);
			fPreviousSelectionProvider= part;
			return;
		} else	if (selection.isEmpty() && !isInputResetBy(part)) {
			fPreviousSelectionProvider= part;
			return;
		} else if (selectedElement == null && part == fPreviousSelectionProvider) {
			setInput(null);
			fPreviousSelectionProvider= part;
			return;
		}
		fPreviousSelectionProvider= part;
		
		// Adjust input and set selection and 
		adjustInputAndSetSelection(selectedElement);
	}


	void setHasWorkingSetFilter(boolean state) {
		fHasWorkingSetFilter= state;
	}

	void setHasCustomSetFilter(boolean state) {
		fHasCustomFilter= state;
	}
	
	protected Object getInput() {
		return fViewer.getInput();
	}

	protected void setInput(Object input) {
		setViewerInput(input);
		updateTitle();
	}

	boolean isLinkingEnabled() {
		return fLinkingEnabled;
	}

	private void initLinkingEnabled() {
		fLinkingEnabled= PreferenceConstants.getPreferenceStore().getBoolean(getLinkToEditorKey());
	}

	private void setViewerInput(Object input) {
		fProcessSelectionEvents= false;
		fViewer.setInput(input);
		fProcessSelectionEvents= true;
	}

	protected void updateTitle() {
		setTitleToolTip(getToolTipText(fViewer.getInput()));
	}

	/**
	 * Returns the tool tip text for the given element.
	 */
	String getToolTipText(Object element) {
		String result;
		if (!(element instanceof IResource)) {
			result= CElementLabels.getTextLabel(element, AppearanceAwareLabelProvider.DEFAULT_TEXTFLAGS);		
		} else {
			IPath path= ((IResource) element).getFullPath();
			if (path.isRoot()) {
				result= getConfigurationElement().getAttribute("name"); //$NON-NLS-1$
			} else {
				result= path.makeRelative().toString();
			}
		}
		
		if (fWorkingSetFilterActionGroup == null || fWorkingSetFilterActionGroup.getWorkingSet() == null)
			return result;

		IWorkingSet ws= fWorkingSetFilterActionGroup.getWorkingSet();
		String wsstr= CBrowsingMessages.getFormattedString("CBrowsingPart.toolTip", new String[] { ws.getName() }); //$NON-NLS-1$
		if (result.length() == 0)
			return wsstr;
		return CBrowsingMessages.getFormattedString("CBrowsingPart.toolTip2", new String[] { result, ws.getName() }); //$NON-NLS-1$
	}
	
	public String getTitleToolTip() {
		if (fViewer == null)
			return super.getTitleToolTip();
		return getToolTipText(fViewer.getInput());
	}

	protected final StructuredViewer getViewer() {
		return fViewer;
	}

	protected final void setViewer(StructuredViewer viewer){
		fViewer= viewer; 
	}

	protected abstract LabelProvider createLabelProvider();

	protected ILabelProvider createTitleProvider() {
		return new CElementLabelProvider(CElementLabelProvider.SHOW_BASICS | CElementLabelProvider.SHOW_SMALL_ICONS);
	}

	protected final ILabelProvider getLabelProvider() {
		return fLabelProvider;
	}

	protected final ILabelProvider getTitleProvider() {
		return fTitleProvider;
	}

	/**
	 * Creates the the viewer of this part.
	 * 
	 * @param parent	the parent for the viewer
	 */
	protected StructuredViewer createViewer(Composite parent) {
		return new ProblemTableViewer(parent, SWT.MULTI);
	}
	
	protected int getLabelProviderFlags() {
		return CElementLabelProvider.SHOW_BASICS | CElementLabelProvider.SHOW_OVERLAY_ICONS |
				CElementLabelProvider.SHOW_SMALL_ICONS | /*CElementLabelProvider.SHOW_VARIABLE |*/ CElementLabelProvider.SHOW_PARAMETERS;
	}

	/**
	 * Adds filters the viewer of this part.
	 */
	protected void addFilters() {
		// default is to have no filters
	}

//	/**
//	 * Creates the the content provider of this part.
//	 */
	protected abstract IContentProvider createContentProvider();

	protected void setInitialInput() {
		// Use the selection, if any
		ISelection selection= getSite().getPage().getSelection();
		Object input= getSingleElementFromSelection(selection);
		if (!(input instanceof ICElement) && !(input instanceof ITypeInfo)) {
			// Use the input of the page
			input= getSite().getPage().getInput();
			if (!(input instanceof ICElement) && input instanceof IAdaptable)
				input= ((IAdaptable)input).getAdapter(ICElement.class);
		}
		setInput(findInputForElement(input));		
	}

	protected void setInitialSelection() {
		// Use the selection, if any
		Object input;
		IWorkbenchPage page= getSite().getPage();
		ISelection selection= null;
		if (page != null)
			selection= page.getSelection();
		if (selection instanceof ITextSelection) {
			Object part= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
			if (part instanceof IEditorPart) {
				setSelectionFromEditor((IEditorPart)part);
				if (fViewer.getSelection() != null)
					return;
			}
		}

		// Use saved selection from memento
		if (selection == null || selection.isEmpty())
			selection= restoreSelectionState(fMemento);
			
		if (selection == null || selection.isEmpty()) { 
			// Use the input of the page
			input= getSite().getPage().getInput();
			if (!(input instanceof ICElement)) {
				if (input instanceof IAdaptable)
					input= ((IAdaptable)input).getAdapter(ICElement.class);
				else
					return;
			}
			selection= new StructuredSelection(input);
		}
		selectionChanged(null, selection);
	}

	protected final void setHelp() {
//		CUIHelp.setHelp(fViewer, getHelpContextId());
	}

	/**
	 * Returns the context ID for the Help system
	 * 
	 * @return	the string used as ID for the Help context
	 */
	abstract protected String getHelpContextId();

	/**
	 * Returns the preference key for the link to editor setting.
	 * 
	 * @return	the string used as key into the preference store
	 */
	abstract protected String getLinkToEditorKey();

	/**
	 * Adds additional listeners to this view.
	 * This method can be overridden but should
	 * call super.
	 */
	protected void hookViewerListeners() {
		fViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (!fProcessSelectionEvents)
					return;

				fPreviousSelectedElement= getSingleElementFromSelection(event.getSelection());

				IWorkbenchPage page= getSite().getPage();
				if (page == null)
					return;

				if (page.equals(CUIPlugin.getActivePage()) && CBrowsingPart.this.equals(page.getActivePart())) {
					linkToEditor((IStructuredSelection)event.getSelection());
				}
			}
		});

		fViewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				ISelection selection = event.getSelection();
				if (selection != null) {
					Object element = getSingleElementFromSelection(selection);
					if (element instanceof ICElement) {
						openInEditor((ICElement)element);
					} else if (element instanceof ITypeInfo) {
						openTypeInEditor((ITypeInfo)element);
					}
				}
//				IAction open= fOpenEditorGroup.getOpenAction();
//				if (open.isEnabled()) {
//					open.run();
//					restoreSelection();
//				}
			}
		});
	}

	protected void openTypeInEditor(ITypeInfo info) {
		if (info == info.getCache().getGlobalNamespace()) {
			return; // nothing to open
		}
		ITypeReference location = info.getResolvedReference();
		if (location == null) {
			final ITypeInfo[] typesToResolve = new ITypeInfo[] { info };
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					AllTypesCache.resolveTypeLocation(typesToResolve[0], monitor);
					if (monitor.isCanceled()) {
						throw new InterruptedException();
					}
				}
			};
			
			IProgressService service = PlatformUI.getWorkbench().getProgressService();
			try {
				service.busyCursorWhile(runnable);
			} catch (InvocationTargetException e) {
				String title = OpenTypeMessages.getString("OpenTypeAction.exception.title"); //$NON-NLS-1$
				String message = OpenTypeMessages.getString("OpenTypeAction.exception.message"); //$NON-NLS-1$
				ExceptionHandler.handle(e, title, message);
				return;
			} catch (InterruptedException e) {
				// cancelled by user
				return;
			}

			location = info.getResolvedReference();
		}

		if (location == null) {
			// could not resolve location
			String title = OpenTypeMessages.getString("OpenTypeAction.errorTitle"); //$NON-NLS-1$
			String message = OpenTypeMessages.getFormattedString("OpenTypeAction.errorTypeNotFound", info.getQualifiedTypeName().toString()); //$NON-NLS-1$
			MessageDialog.openError(getShell(), title, message);
		} else if (!openInEditor(location)) {
			// error opening editor
			String title = OpenTypeMessages.getString("OpenTypeAction.errorTitle"); //$NON-NLS-1$
			String message = OpenTypeMessages.getFormattedString("OpenTypeAction.errorOpenEditor", location.getPath().toString()); //$NON-NLS-1$
			MessageDialog.openError(getShell(), title, message);
		}
	}

	private boolean openInEditor(ITypeReference location) {
		ITranslationUnit unit = location.getTranslationUnit();
		IEditorPart editorPart = null;
		
		try {
			if (unit != null)
				editorPart = EditorUtility.openInEditor(unit);
			if (editorPart == null) {
				// open as external file
				IPath path = location.getLocation();
				if (path != null) {
					IStorage storage = new FileStorage(path);
					editorPart = EditorUtility.openInEditor(storage);
				}
			}

			// highlight the type in the editor
			if (editorPart != null && editorPart instanceof ITextEditor) {
				ITextEditor editor = (ITextEditor) editorPart;
				editor.selectAndReveal(location.getOffset(), location.getLength());
				return true;
			}
		} catch (CModelException ex) {
			ex.printStackTrace();
			return false;
		} catch (PartInitException ex) {
			ex.printStackTrace();
			return false;
		}
		
		return false;
	}

	protected boolean openInEditor(ICElement cElement) {
		IEditorPart editorPart = null;
		
		try {
			if (cElement != null)
				editorPart = EditorUtility.openInEditor(cElement);
			if (editorPart == null)
				return false;
		} catch (CModelException ex) {
			ex.printStackTrace();
			return false;
		} catch (PartInitException ex) {
			ex.printStackTrace();
			return false;
		}
		
		// highlight the type in the editor
		if (cElement != null && editorPart instanceof CEditor) {
			CEditor editor = (CEditor) editorPart;
			editor.setSelection(cElement);
			return true;
		}
		return false;
	}

	void restoreSelection() {
		// Default is to do nothing
	}

	void adjustInputAndSetSelection(Object o) {
		if (!(o instanceof ICElement) && !(o instanceof ITypeInfo)) {
			setSelection(StructuredSelection.EMPTY, true);
			return;
		}
		
		Object elementToSelect= getSuitableElement(findElementToSelect(o));
		Object newInput= findInputForElement(o);
		Object oldInput= null;
		if (getInput() instanceof ICElement || getInput() instanceof ITypeInfo)
			oldInput = getInput();

		if (elementToSelect == null && !isValidInput(newInput) && (newInput == null && !isAncestorOf(o, oldInput)))
			// Clear input
			setInput(null);
		else if (mustSetNewInput(elementToSelect, oldInput, newInput)) {
			// Adjust input to selection
			setInput(newInput);
			// Recompute suitable element since it depends on the viewer's input
			elementToSelect= getSuitableElement(elementToSelect);
		}
		
		if (elementToSelect != null /*&& elementToSelect.exists()*/)
			setSelection(new StructuredSelection(elementToSelect), true);
		else
			setSelection(StructuredSelection.EMPTY, true);
	}

	/**
	 * Compute if a new input must be set.
	 * 
	 * @return	<code>true</code> if the input has to be set
	 * @since 3.0
	 */	
	private boolean mustSetNewInput(Object elementToSelect, Object oldInput, Object newInput) {
		return (newInput == null || !newInput.equals(oldInput))
		&& (elementToSelect == null
			|| oldInput == null);
//		return (newInput == null || !newInput.equals(oldInput))
//			&& (elementToSelect == null
//				|| oldInput == null
//				|| (!((elementToSelect instanceof IDeclaration)
//					&& (elementToSelect.getParent().equals(oldInput.getParent()))
//					&& (!isAncestorOf(getViewPartInput(), elementToSelect)))));
	}
	
	/**
	 * Finds the closest Java element which can be used as input for
	 * this part and has the given Java element as child
	 * 
	 * @param 	je 	the Java element for which to search the closest input
	 * @return	the closest Java element used as input for this part
	 */
	abstract protected Object findInputForElement(Object element);
	
	/**
	 * Finds the element which has to be selected in this part.
	 * 
	 * @param je	the Java element which has the focus
	 */
	abstract protected Object findElementToSelect(Object obj);
	
	/**
	 * Converts the given Java element to one which is suitable for this
	 * view. It takes into account wether the view shows working copies or not.
	 *
	 * @param	element the Java element to be converted
	 * @return	an element suitable for this view
	 */
	Object getSuitableElement(Object obj) {
		if (!(obj instanceof ICElement) && !(obj instanceof ITypeInfo))
			return null;
		if (fTypeComparator.compare(obj, ICElement.C_UNIT) > 0)
			return obj;
		return obj;
//		if (element.getElementType() == IJavaElement.CLASS_FILE)
//			return element;
//		if (isInputAWorkingCopy()) {
//			IJavaElement wc= getWorkingCopy(element);
//			if (wc != null)
//				element= wc;
//			return element;
//		}
//		else {
//			return element.getPrimaryElement();
//		}
	}

	protected final Object getSingleElementFromSelection(ISelection selection) {
		if (!(selection instanceof StructuredSelection) || selection.isEmpty())
			return null;
		
		Iterator iter= ((StructuredSelection)selection).iterator();
		Object firstElement= iter.next();
		if (!(firstElement instanceof ICElement) && !(firstElement instanceof ITypeInfo)) {
//			if (SearchUtil.isISearchResultViewEntry(firstElement)) {
//				ICElement je= SearchUtil.getJavaElement(firstElement);
//				if (je != null)
//					return je;
//				firstElement= SearchUtil.getResource(firstElement);
//			}
			if (firstElement instanceof IAdaptable) {
				ICElement je= (ICElement)((IAdaptable)firstElement).getAdapter(ICElement.class);
				if (je == null && firstElement instanceof IFile) { 
					IContainer parent= ((IFile)firstElement).getParent();
					if (parent != null)
						return (ICElement)parent.getAdapter(ICElement.class);
					else return null;
				} else 
					return je;
				
			} else
				return firstElement;
		}
		Object currentInput= getViewer().getInput();
		if (currentInput == null || !currentInput.equals(findInputForElement(firstElement)))
			if (iter.hasNext())
				// multi selection and view is empty
				return null;
			else
				// ok: single selection and view is empty 
				return firstElement;

		// be nice to multi selection
		while (iter.hasNext()) {
			Object element= iter.next();
			if (!(element instanceof ICElement) && !(element instanceof ITypeInfo))
//			if (!(element instanceof ICElement))
				return null;
			if (!currentInput.equals(findInputForElement(element)))
				return null;
		}
		return firstElement;
	}

	/**
	 * Gets the typeComparator.
	 * @return Returns a JavaElementTypeComparator
	 */
	protected Comparator getTypeComparator() {
		return fTypeComparator;
	}

	/**
	 * Links to editor (if option enabled)
	 */
	private void linkToEditor(IStructuredSelection selection) {	
		Object obj= selection.getFirstElement();

		if (selection.size() == 1) {
			IEditorPart part= EditorUtility.isOpenInEditor(obj);
			if (part != null) {
				IWorkbenchPage page= getSite().getPage();
				page.bringToTop(part);
				if (obj instanceof ICElement) 
					EditorUtility.revealInEditor(part, (ICElement) obj);
			}
		}
	}

	private void setSelectionFromEditor(IWorkbenchPartReference ref) {
			IWorkbenchPart part= ref.getPart(false);
			setSelectionFromEditor(part);
	}

	void setSelectionFromEditor(IWorkbenchPart part) {
		if (!linkBrowsingViewSelectionToEditor())
			return;
		
		if (part == null)
			return;
		IWorkbenchPartSite site= part.getSite();
		if (site == null)
			return;
		ISelectionProvider provider= site.getSelectionProvider();
		if (provider != null)
			setSelectionFromEditor(part, provider.getSelection());
	}

	private void setSelectionFromEditor(IWorkbenchPart part, ISelection selection) {
		if (part instanceof IEditorPart) {
			ICElement element= null;
			if (selection instanceof IStructuredSelection) {
				Object obj= getSingleElementFromSelection(selection);
				if (obj instanceof ICElement)
					element= (ICElement)obj;
			}
			IEditorInput ei= ((IEditorPart)part).getEditorInput();
			if (selection instanceof ITextSelection) {
				int offset= ((ITextSelection)selection).getOffset();
				element= getElementAt(ei, offset);
			}
			if (element != null) {
				adjustInputAndSetSelection(element);
				return;
			}
			if (ei instanceof IFileEditorInput) {
				IFile file= ((IFileEditorInput)ei).getFile();
				ICElement je= (ICElement)file.getAdapter(ICElement.class);
				if (je == null) {
					IContainer container= ((IFileEditorInput)ei).getFile().getParent();
					if (container != null)
						je= (ICElement)container.getAdapter(ICElement.class);
				}
				if (je == null) {					
					setSelection(null, false);
					return;
				}
				adjustInputAndSetSelection(je);
//			} else if (ei instanceof IClassFileEditorInput) {
//				IClassFile cf= ((IClassFileEditorInput)ei).getClassFile();
//				adjustInputAndSetSelection(cf);
			}
		}
	}

	/**
	 * Returns the element contained in the EditorInput
	 */
	Object getElementOfInput(IEditorInput input) {
//		if (input instanceof IClassFileEditorInput)
//			return ((IClassFileEditorInput)input).getClassFile();
//		else 
		if (input instanceof IFileEditorInput)
			return ((IFileEditorInput)input).getFile();
//		else if (input instanceof JarEntryEditorInput)
//			return ((JarEntryEditorInput)input).getStorage();
		return null;
	}
	
	protected void setSelection(ISelection selection, boolean reveal) {
		if (selection != null && selection.equals(fViewer.getSelection()))
			return;
		fProcessSelectionEvents= false;
		fViewer.setSelection(selection, reveal);
		fProcessSelectionEvents= true;
	}

	/**
	 * Tries to find the given element in a workingcopy.
	 */
	protected static ICElement getWorkingCopy(ICElement input) {
		// MA: with new working copy story original == working copy
		return input;
	}

//
//	boolean isInputAWorkingCopy() {
//		return ((BaseCElementContentProvider)getViewer().getContentProvider()).getProvideWorkingCopy();
//	}

	/**
	 * @see org.eclipse.jdt.internal.ui.javaeditor.JavaEditor#getElementAt(int)
	 */
	protected ICElement getElementAt(IEditorInput input, int offset) {
//		if (input instanceof IClassFileEditorInput) {
//			try {
//				return ((IClassFileEditorInput)input).getClassFile().getElementAt(offset);
//			} catch (CModelException ex) {
//				return null;
//			}
//		}

		IWorkingCopyManager manager= CUIPlugin.getDefault().getWorkingCopyManager();
		ITranslationUnit unit= manager.getWorkingCopy(input);
		if (unit != null)
			try {
				if (unit.isConsistent())
					return unit.getElementAtOffset(offset);
				else {
					/*
					 * XXX: We should set the selection later when the
					 *      CU is reconciled.
					 *      see https://bugs.eclipse.org/bugs/show_bug.cgi?id=51290 
					 */
				}
			} catch (CModelException ex) {
				// fall through
			}
		return null;
	}
	
/*	protected ICElement getTypeForCU(ITranslationUnit cu) {
		cu= (ITranslationUnit)getSuitableCElement(cu);
		
//		// Use primary type if possible
//		ICElement primaryType= cu.findPrimaryType();
//		if (primaryType != null)
//			return primaryType;

		// Use first top-level type
		try {
			final ICElement[] fTypes = new ICElement[]{ null };
			cu.accept(new ICElementVisitor() {
				public boolean visit(ICElement element) throws CoreException {
					// TODO Auto-generated method stub
					switch(element.getElementType()) {
						case ICElement.C_NAMESPACE:
						case ICElement.C_TEMPLATE_CLASS:
						case ICElement.C_CLASS:
						case ICElement.C_STRUCT:
						case ICElement.C_UNION:
						case ICElement.C_ENUMERATION:
						case ICElement.C_TYPEDEF:
							fTypes[0] = element;
							return false;
					}
					return true;
				}
			});
//			ICElement[] types= cu.getTypes();
//			if (types.length > 0)
//				return types[0];
//			else
//				return null;
			return fTypes[0];
		} catch (CoreException ex) {
			return null;
		}
	}	
*/
	void setProcessSelectionEvents(boolean state) {
		fProcessSelectionEvents= state;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.viewsupport.IViewPartInputProvider#getViewPartInput()
	 */
	public Object getViewPartInput() {
		if (fViewer != null) {
			return fViewer.getInput();
		}
		return null;
	}

//	protected void setActionGroups(CompositeActionGroup actionGroups) {
//		fActionGroups= actionGroups;
//	}
//
//	protected void setBuildActionGroup(BuildActionGroup actionGroup) {
//		fBuildActionGroup= actionGroup;
//	}
//
//	protected void setCCPActionGroup(CCPActionGroup actionGroup) {
//		fCCPActionGroup= actionGroup;
//	}
//
//	protected void setCustomFiltersActionGroup(CustomFiltersActionGroup customFiltersActionGroup) {
//		fCustomFiltersActionGroup= customFiltersActionGroup;
//	}

	protected boolean hasCustomFilter() {
		return fHasCustomFilter;
	}

	protected boolean hasWorkingSetFilter() {
		return fHasWorkingSetFilter;
	}

//	protected void setOpenEditorGroup(OpenEditorActionGroup openEditorGroup) {
//		fOpenEditorGroup= openEditorGroup;
//	}
//
//	protected OpenEditorActionGroup getOpenEditorGroup() {
//		return fOpenEditorGroup;
//	}
//
//	protected BuildActionGroup getBuildActionGroup() {
//		return fBuildActionGroup;
//	}
//
//	protected CCPActionGroup getCCPActionGroup() {
//		return fCCPActionGroup;
//	}
	
	private boolean linkBrowsingViewSelectionToEditor() {
		return isLinkingEnabled();
	}

	public void setLinkingEnabled(boolean enabled) {
		fLinkingEnabled= enabled;
		PreferenceConstants.getPreferenceStore().setValue(getLinkToEditorKey(), enabled);
		if (enabled) {
			IEditorPart editor = getSite().getPage().getActiveEditor();
			if (editor != null) {
				setSelectionFromEditor(editor);
			}
		}
	}

}
