/**********************************************************************
 * Copyright (c) 2002,2003 Timesys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Timesys - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.ui.builder.internal;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.builder.BuilderPlugin;
import org.eclipse.cdt.core.builder.model.ICBuildConfig;
import org.eclipse.cdt.core.builder.model.ICBuildConfigManager;
import org.eclipse.cdt.core.builder.model.ICBuildConfigWorkingCopy;
import org.eclipse.cdt.core.resources.MakeUtil;
import org.eclipse.core.internal.runtime.Assert;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;


/**
 * Shows the build configuration wizard
 */
public class CBuildConfigView extends ViewPart implements IResourceChangeListener { 
	private TreeViewer viewer;
	
	/**
	 * Configuration life cycle actions
	 */
	private Action actionEditConfig;
	private Action actionNewConfig;
	private Action actionMakePrimary;
	private Action actionDeleteConfig;
	private Action doubleClickAction;
	//private Action actionBuild;
	
	/** 
	 * Message constants
	 */
	private final String MSG_CANDELETE = "Remove build configuration";
	private final String MSG_NODELETE = "Cannot delete primary build configuration";
	private final String MSG_MAKEPRIMARY = "Make this build configuration the primary configuration";
	private final String MSG_ALREADYPRIMARY = "This build configuration is the primary configuration";
	private final String KEY_LASTPROJECT = "LastProject";

	/**
	 * Menu entry for the collection of CBuildConfigurationAction
	 * objects
	 */
	class CBuildAction extends Action {
		private IProject m_project;
		private ICBuildConfig m_config;
		
		public CBuildAction(IProject prj, ICBuildConfig config) {
			m_project = prj;
			m_config = config;
			setText("Build");
			setToolTipText("Performing Build");
			setImageDescriptor(CBuilderImages.DESC_IMG_ACTION_BUILD_CONFIG);
			setMenuCreator(new CTargetsMenuCreator(m_project, m_config));
		}
	}

	class CTargetsMenuCreator implements IMenuCreator {
			
		private IProject m_project;
		private ICBuildConfig m_config;

		public CTargetsMenuCreator(IProject prj, ICBuildConfig config) {
			m_project = prj;
			m_config = config;	
		}
		
		private Menu createContents(Menu targets) {

			// walk the list of targets in the project and add them to the target menu
			String strTargets[] = MakeUtil.getPersistentTargets(m_project);
			for (int nIndex = 0; nIndex < strTargets.length; nIndex++) {
				MenuItem newItem = new MenuItem(targets, SWT.NONE);
				newItem.setText(strTargets[nIndex]);
				newItem.addSelectionListener(new SelectionAdapter() {

					public void widgetSelected(SelectionEvent selEvent) {
						// cheasy, but good enough for this, the name of the menu is the build target
						String strConfig = ((MenuItem) selEvent.getSource()).getText();
						// System.out.println("Building configuration " + strConfig + " on project " + m_project.getName() + " using configuration " + m_config.getName());
						IRunnableWithProgress builder = new ConfigurationBuilder(m_project, m_config, strConfig);
						try {
							new ProgressMonitorDialog(getSite().getShell()).run(true, true, builder);
						}
						catch (InterruptedException e) {}
						catch (InvocationTargetException e) {}
					}
				});
			}
			
			return targets;
		}

		/**
		 * @see org.eclipse.jface.action.IMenuCreator#dispose()
		 */
		public void dispose() {
			m_project = null;
		}
				
		/**
		 * @see org.eclipse.jface.action.IMenuCreator#getMenu(Control)
		 */
		public Menu getMenu(Control parent) {
			Menu theMenu = new Menu(parent);
			return createContents(theMenu);
		}

		/**
		 * @see org.eclipse.jface.action.IMenuCreator#getMenu(Menu)
		 */
		public Menu getMenu(Menu parent) {
			Menu theMenu = new Menu(parent);			
			return createContents(theMenu);
		}

	}
	
	/**
	 * this class builds the selected configuration
	 */
	class ConfigurationBuilder implements IRunnableWithProgress {
		private IProject m_prj;		
		private ICBuildConfig m_config;
		private String m_target;

		public ConfigurationBuilder(IProject prj, ICBuildConfig bldConfig, String strTarget) {
			m_prj  = prj;
			m_config = bldConfig;
			m_target = strTarget;
		}
		
		/**
		 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(IProgressMonitor)
		 */
		public void run(IProgressMonitor monitor) {
			Assert.isNotNull(m_prj, "Project reference cannot be null");
			Assert.isNotNull(m_config, "Configuration reference cannot be null");
			try {
				MakeUtil.setSessionBuildDir(m_prj, m_prj.getLocation().toOSString());
				MakeUtil.setSessionTarget(m_prj, m_target);
				MakeUtil.setSessionConsoleMode(m_prj, true);
				m_prj.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
			} 
			catch (CoreException e) { }
		}
	}
	
	/*
	 * The content provider class is responsible for
	 * providing objects to the view. It can wrap
	 * existing objects in adapters or simply return
	 * objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore
	 * it and always show the same content 
	 * (like Task List, for example).
	 */	 
	abstract class TreeObject implements IAdaptable {
		private String 	fName;
		private TreeObject fParent;
		private ArrayList	fChildren;
		
		public TreeObject(String name) {
			this(name, null);
		}

		abstract public void doRefreshChildren();
		
		public TreeObject(String name, TreeObject parent) {
			setName(name);
			setParent(parent);
			fChildren = new ArrayList();
		}

		public void refreshChildren() {
			fChildren.clear();
			doRefreshChildren();
			for (Iterator iter = fChildren.iterator(); iter.hasNext();) {
				TreeObject element = ((TreeObject) iter.next());
				element.refreshChildren();
			}
		}

		public void addChild(TreeObject child) {
			fChildren.add(child);
			child.setParent(this);
		}

		public void removeChild(TreeObject child) {
			fChildren.remove(child);
			child.setParent(null);
		}

		public TreeObject[] getChildren() {
			return (TreeObject[]) fChildren.toArray(new TreeObject[fChildren.size()]);
		}
		
		public boolean hasChildren() {
			return (fChildren.size() > 0);
		}

		public void setName(String name) {
			fName = ((name != null) ? name : "");
		}

		public void setParent(TreeObject parent) {
			fParent = parent;
		}

		public String getName() {
			return fName;
		}
		
		public TreeObject getParent() {
			return fParent;
		}
		
		public Object getAdapter(Class key) {
			return null;
		}
		
		public Image getImage() {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}

		public String toString() {
			return getName();
		}
	}
	
	/**
	 * The tree parent class contains a list of projects
	 */
	class TreeRoot extends TreeObject {

		public TreeRoot() {
			super("");
		}
		
		/**
		 * Return a list of all child objects (projects)
		 * that support the C project nature.
		 */
		public void doRefreshChildren() {
			IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
			if (wsRoot != null) {
				IProject[] projects = wsRoot.getProjects();
				if (projects != null) {
					for (int i = 0; i < projects.length; i++) {
						try {
							if (projects[i].isOpen()) {
								if (projects[i].hasNature(CProjectNature.C_NATURE_ID)) {
									addChild(new TreeProject(projects[i], this));
								}
							}
						} 
						catch (CoreException e) {
						}
					}
				}
			}
		}

		public Image getImage() {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
		}
		
	}
		
	class TreeProject extends TreeObject {
		private IProject fProject;

		public TreeProject(IProject project, TreeRoot parent) {
			super(project.getName(), parent);
			setProject(project);
		}

		private void setProject(IProject project) {
			fProject = project;
		}
		
		public IProject getProject() {
			return fProject;
		}

		public void doRefreshChildren() {
			try {
				ICBuildConfig[] configs = getBuildConfigurationManager().getConfigurations(fProject);
				for (int i = 0; i < configs.length; i++) {
					addChild(new TreeConfiguration(configs[i]));
				}
			} catch (CoreException e) {
			}
		}
		
		public Image getImage() {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
		}
	}
	
	class TreeConfiguration extends TreeObject {
		private ICBuildConfig fConfig;
		
		public TreeConfiguration(ICBuildConfig config) {
			super(config.getName());
			fConfig = config;
		}

		public void doRefreshChildren() {
		}
		
		public ICBuildConfig getConfiguration() {
			return fConfig;
		}

		public IProject getProject() {
			return fConfig.getProject();
		}
	}

	class ViewContentProvider implements IStructuredContentProvider, 
										   ITreeContentProvider {
		private TreeRoot invisibleRoot;

		public ViewContentProvider() {
			invisibleRoot = new TreeRoot();
			invisibleRoot.refreshChildren();
		}

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			invisibleRoot.refreshChildren();
		}
		
		public void dispose() {
		}
		
		public Object[] getElements(Object parent) {
			if (parent.equals(ResourcesPlugin.getWorkspace())) {
				return getChildren(invisibleRoot);
			}
			return getChildren(parent);
		}

		public Object getParent(Object child) {
			if (child instanceof TreeObject) {
				return ((TreeObject) child).getParent();
			}
			return null;
		}

		public Object[] getChildren(Object parent) {
			if (parent instanceof TreeObject) {
				return ((TreeObject) parent).getChildren();
			}
			return new Object[0];
		}

		public boolean hasChildren(Object parent) {			
			if (parent instanceof TreeObject) {
				return ((TreeObject) parent).hasChildren();
			}
			return false;
		}
	}
	
	class ViewLabelProvider extends LabelProvider {

		public String getText(Object obj) {
			return obj.toString();
		}

		public Image getImage(Object obj) {
			Image image = null;			// this is the image that will represent the obj parameter			

			// if this is a build configuration, get the image for the build configuration 
			if (obj instanceof TreeObject) {
				image = ((TreeObject) obj).getImage();
			} else {
				// er, this is a bit odd, means we're not looking at one of our own classes
				image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
			}
			
			return image;
		}
	}
	
	/**
	 * The constructor.
	 */
	public CBuildConfigView() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 * 
	 * @param	parent					the owner of the control 
	 */
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new ViewerSorter(){});
		viewer.setInput(ResourcesPlugin.getWorkspace());
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		
		// register the listener for changes to the tree
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		
		// Set this button to disable by default since there is no valid selection at this point.
		actionEditConfig.setEnabled(false);
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("_#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				CBuildConfigView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				ISelection selItem = viewer.getSelection();
				Object objItem = ((IStructuredSelection) selItem).getFirstElement();
				if (objItem.getClass().equals(TreeObject.class)) {
					actionEditConfig.run();
				}
				else {
				// be nice and expand or contract the tree when the user double clicks on a node
					for (Iterator iter = ((IStructuredSelection) selItem).iterator(); iter.hasNext(); ) {
						Object objNode = iter.next();
						if (viewer.getExpandedState(objNode)) {
							viewer.collapseToLevel(objNode, 1);
						}
						else {
							viewer.expandToLevel(objNode, 1);
						}
					}
				}
			}
		});
	}
		
	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(actionEditConfig);
		manager.add(new Separator());
		//manager.add(actionBuild);
	}

	/**
	 * this method populates the right-click menu
	 */
	private void fillContextMenu(IMenuManager manager) {
		
		ISelection selItem = viewer.getSelection();
		Object objItem = ((IStructuredSelection)selItem).getFirstElement();
		
		if (objItem instanceof TreeProject) {
			manager.add(actionNewConfig);
			// manager.add(actionBuild);
		} else if (objItem instanceof TreeConfiguration) {
			TreeConfiguration treeConf = (TreeConfiguration) objItem;		
			manager.add(actionEditConfig);
			manager.add(actionNewConfig);
			CBuildAction build = new CBuildAction(treeConf.getProject(), treeConf.getConfiguration());
			build.setImageDescriptor(CBuilderImages.DESC_IMG_ACTION_BUILD_CONFIG);
			manager.add(build);
			
			ICBuildConfig item = treeConf.getConfiguration();

			if (item != null) {
				// TODO: fix this
				// actionMakePrimary.setEnabled(!item.getIsPrimary());
				actionMakePrimary.setDescription(actionMakePrimary.isEnabled() ? MSG_MAKEPRIMARY : MSG_ALREADYPRIMARY);
				actionMakePrimary.setToolTipText(actionMakePrimary.getDescription());
				// TODO: fix this
				// actionDeleteConfig.setEnabled(!item.getIsPrimary());
				actionDeleteConfig.setDescription(actionDeleteConfig.isEnabled() ? MSG_CANDELETE : MSG_NODELETE);
				actionDeleteConfig.setToolTipText(actionDeleteConfig.getDescription());
				manager.add(actionMakePrimary);
				manager.add(actionDeleteConfig);
			}
		}

		// Other plug-ins can contribute there actions here
		manager.add(new Separator("Additions"));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(actionEditConfig);
		//manager.add(actionBuild);
		manager.add(new Separator());
	}

	private void updateActionEnabled () {
		IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
		TreeObject child = (TreeObject) sel.getFirstElement();
		boolean enabled = ((sel.size() > 0) && (child instanceof TreeConfiguration));
		
		actionEditConfig.setEnabled(enabled);
		//actionBuild.setEnabled(true);
	}

	private ICBuildConfigManager getBuildConfigurationManager() {
		return BuilderPlugin.getDefault().getBuildConfigurationManager();
	}

	private Shell getShell() {
		return viewer.getControl().getShell();
	}

	private Object getElementFromSelection(ISelection sel) {
		Object element = null;
		if (sel instanceof IStructuredSelection) {
			element = ((IStructuredSelection) sel).getFirstElement();
		}
		return element;
	}
	
	private void makeActions() {

		//////////////////////////////////////////////
		// Action: EDIT
		//////////////////////////////////////////////

		actionEditConfig = new Action() {
			public void run() {
				Object element = getElementFromSelection(viewer.getSelection());
				if ((element != null) && (element instanceof TreeConfiguration)) {
					TreeConfiguration treeConf = (TreeConfiguration) element;
					CBuildConfigDialog dlg = null;
					
					dlg = new CBuildConfigDialog(getShell(), treeConf.getConfiguration()); 

					if (dlg.open() == dlg.OK) {
						viewer.refresh();					
					}										
				}
			}
		};
		
		actionEditConfig.setText("Edit...");
		actionEditConfig.setToolTipText("Edit Configuration");
		actionEditConfig.setImageDescriptor(CBuilderImages.DESC_IMG_ACTION_EDIT_CONFIG);

		//////////////////////////////////////////////
		// Action: BUILD
		//////////////////////////////////////////////
		
//		actionBuild = new Action() {
//		public void run() {
//				IStructuredSelection selList = (IStructuredSelection) viewer.getSelection();
//				for (Iterator iter = selList.iterator(); iter.hasNext();) {
//					Object selItem = (Object) iter.next();
//					if (selItem instanceof TreeObject) {
//						TreeObject selConfig = (TreeObject) selItem;
//						IRunnableWithProgress builder = new CBuildConfig(selConfig.getAssocProject(), selConfig.getBuildConfig());
//						try {
//							new ProgressMonitorDialog(getSite().getShell()).run(true, true, builder);
//						}
//						catch (InterruptedException e) {}
//						catch (InvocationTargetException e) {}
//					}
//				}
//			}
//		};
//
//		actionBuild.setText("Build");
//		actionBuild.setToolTipText("Performing Build");
//		actionBuild.setImageDescriptor(CBuilderImages.DESC_IMG_ACTION_BUILD_CONFIG);
//
//		doubleClickAction = new Action() {
//			public void run() {
//				ISelection selection = viewer.getSelection();
//				Object obj = ((IStructuredSelection)selection).getFirstElement();
//				showMessage("Try to perform a build here" + obj.toString());
//			}
//		};

		//////////////////////////////////////////////
		// Action: NEW
		//////////////////////////////////////////////
		
		actionNewConfig = new Action() {
			public void run() {
				Object element = getElementFromSelection(viewer.getSelection());
				if ((element != null) && (element instanceof TreeProject)) {
					TreeProject treeProject = (TreeProject) element;
					ICBuildConfigWorkingCopy cfg = null;
					CBuildConfigDialog dlg = null;

					cfg = getBuildConfigurationManager().getConfiguration(treeProject.getProject(), null);
					dlg = new CBuildConfigDialog(getShell(), cfg); 

					if (dlg.open() == dlg.OK) {
						viewer.refresh();
					}
				}
			}	
		};
			
		actionNewConfig.setText("New...");
		actionNewConfig.setToolTipText("Add new configuration");
		actionNewConfig.setImageDescriptor(CBuilderImages.DESC_IMG_ACTION_NEW_CONFIG);

		//////////////////////////////////////////////
		// Action: MAKE PRIMARY
		//////////////////////////////////////////////

		actionMakePrimary = new Action() {
			public void run() {
				Object element = getElementFromSelection(viewer.getSelection());
				if ((element != null) && (element instanceof TreeConfiguration)) {
					TreeConfiguration treeConfig = (TreeConfiguration) element;
					// TODO: Use getBuildConfigurationManager(), 
					// treeConfig.getBuildConfig() to set primary config
					viewer.refresh();
				}
			}
		};
			
		actionMakePrimary.setText("Make Primary");
		actionMakePrimary.setToolTipText("Make this the primary configuration");
//		actionMakePrimary.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
//			getImageDescriptor(ISharedImages.IMG_OBJS_TASK_TSK));


		//////////////////////////////////////////////
		// Action: DELETE
		//////////////////////////////////////////////

		actionDeleteConfig = new Action() {
			public void run() {
				Object element = getElementFromSelection(viewer.getSelection());
				if ((element != null) && (element instanceof TreeConfiguration)) {
					TreeConfiguration treeConfig = (TreeConfiguration) element;
					if (MessageDialog.openConfirm(getShell(), "Removing build configuration", "Are you sure you want to remove this build configuration?")) {
						viewer.refresh();
					}
				}
			}
		};
		
		actionDeleteConfig.setText("Remove");
		actionDeleteConfig.setToolTipText("Remove this configuration");
		actionMakePrimary.setImageDescriptor(CBuilderImages.DESC_IMG_ACTION_DELETE_CONFIG);	
	
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged (SelectionChangedEvent event) {
				updateActionEnabled();
			}
		});	
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(getShell(), "Build Configuration", message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	/**
	 * clean-up
	 */	
	public void dispose()
	{
		// remove from the workspace our listener
		//getViewSite().getPage().removePostSelectionListener(this);
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}
	
	/**
	 * given a project, updates the view with the contents of
	 * the build configuration for that project
	 * 
	 * @param prjTarget the new project that this view should adjust to
	 */
	private void updateView(IProject prjTarget) 
	{
		Assert.isNotNull(prjTarget);
	}
		
	class CResourceDeltaVisitor implements IResourceDeltaVisitor {
		
		private boolean bRefreshView = false;
		
	    public boolean visit(IResourceDelta delta) {
	     	
	     	IResource resource = delta.getResource();
	     	boolean bVisitChildren = true;
	     	int nKind;
	     	
	     	if (resource instanceof IProject) {
		        nKind = delta.getKind();
	        	if (nKind == IResourceDelta.OPEN || nKind == IResourceDelta.ADDED || nKind == IResourceDelta.REMOVED || nKind == IResourceDelta.CHANGED) {
	        		bRefreshView = true;
	        	}
	        	// don't recurse past the project level
			    bVisitChildren = false;
	     	}
	     	
	     	return bVisitChildren;
	    }
	    
		public boolean getRefreshView() {
			return bRefreshView;
		}
		
	 }
	

	/**
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {

		CResourceDeltaVisitor changeVisitor = new CResourceDeltaVisitor();
		
		try {
			event.getDelta().accept(changeVisitor);
		} 
		catch (CoreException e) {
			System.err.println(e.toString());
		}

		// refresh the view so the user can see the changes.	
		if (changeVisitor.getRefreshView()) {
			this.refresh();
		}
		
	}
	
	/**
	 * Wrapper for users of this object to force a view refresh
	 */
	public void refresh() {
		Display.getDefault().syncExec(new Runnable()
			{
				public void run() { 
					viewer.refresh();
				}
			}
		);
	}
}
