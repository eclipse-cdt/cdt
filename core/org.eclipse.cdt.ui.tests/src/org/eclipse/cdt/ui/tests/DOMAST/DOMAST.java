/**********************************************************************
 * Copyright (c) 2005 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation 
 **********************************************************************/
package org.eclipse.cdt.ui.tests.DOMAST;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.CDOM;
import org.eclipse.cdt.core.dom.IASTServiceProvider;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTProblemStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.filetype.ICFileType;
import org.eclipse.cdt.core.filetype.ICFileTypeConstants;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.parser.ast.IASTEnumerator;
import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor.CPPBaseVisitorAction;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.ui.actions.CustomFiltersActionGroup;
import org.eclipse.cdt.ui.testplugin.CTestPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class DOMAST extends ViewPart {
   private static final String DOM_AST_HAS_NO_CONTENT = "DOM AST has no content"; //$NON-NLS-1$
   private static final String SEARCH_FOR_IASTNAME = "Search for IASTName"; //$NON-NLS-1$
   private static final String CLEAR = "Clear"; //$NON-NLS-1$
   private static final String DOMAST_FILTER_GROUP_ID = "org.eclipse.cdt.ui.tests.DOMAST.DOMASTFilterGroup"; //$NON-NLS-1$
   private static final String EXTENSION_CXX = "CXX"; //$NON-NLS-1$
   private static final String EXTENSION_CPP = "CPP"; //$NON-NLS-1$
   private static final String EXTENSION_CC = "CC"; //$NON-NLS-1$
   private static final String EXTENSION_C = "C"; //$NON-NLS-1$
   private static final String NOT_VALID_COMPILATION_UNIT = "The active editor does not contain a valid compilation unit."; //$NON-NLS-1$
   private static final String LOAD_ACTIVE_EDITOR = "Load Active Editor"; //$NON-NLS-1$
   private static final String COLLAPSE_ALL = "Collapse ALL"; //$NON-NLS-1$
   private static final String EXPAND_ALL = "Expand All"; //$NON-NLS-1$
   private static final String REFRESH_DOM_AST   = "Refresh DOM AST";  //$NON-NLS-1$
   private static final String VIEW_NAME         = "DOM View";         //$NON-NLS-1$
   private static final String POPUPMENU         = "#PopupMenu";       //$NON-NLS-1$
   private static final String OPEN_DECLARATIONS = "Open Declarations"; //$NON-NLS-1$
   private static final String OPEN_REFERENCES   = "Open References";  //$NON-NLS-1$
   TreeViewer          viewer;
   private DrillDownAdapter    drillDownAdapter;
   private Action              openDeclarationsAction;
   private Action              openReferencesAction;
   private Action              singleClickAction;
   private Action              loadActiveEditorAction;
   private Action              refreshAction;
   private Action			   expandAllAction;
   private Action			   collapseAllAction;
   private Action			   clearAction;
   private Action			   searchNamesAction;
   private IFile               file              = null;
   private IEditorPart         part              = null;
   private ParserLanguage              lang              = null;
   
   private CustomFiltersActionGroup customFiltersActionGroup;

   /*
    * The content provider class is responsible for providing objects to the
    * view. It can wrap existing objects in adapters or simply return objects
    * as-is. These objects may be sensitive to the current input of the view, or
    * ignore it and always show the same content (like Task List, for example).
    */

   public class ViewContentProvider implements IStructuredContentProvider,
         ITreeContentProvider {
      private static final String POPULATING_AST_VIEW = "Populating AST View"; //$NON-NLS-1$
	private TreeParent invisibleRoot;
      private TreeParent tuTreeParent = null;
      private IASTTranslationUnit tu = null;

      public ViewContentProvider() {
      }

      public ViewContentProvider(IFile file) {
      	this(file, null);
      }
      
      public ViewContentProvider(IFile file, Object[] expanded) {
       	StartInitializingASTView job = new StartInitializingASTView(new InitializeView(POPULATING_AST_VIEW, this, viewer, file), expanded);
   		job.schedule();

     }
      
      public TreeParent getTUTreeParent() {
      	if (tuTreeParent == null && invisibleRoot != null) {
      		for(int i=0; i<invisibleRoot.getChildren().length; i++) {
      			if (invisibleRoot.getChildren()[i] instanceof TreeParent && invisibleRoot.getChildren()[i].getNode() instanceof IASTTranslationUnit){
      	      		tuTreeParent = (TreeParent)invisibleRoot.getChildren()[i];
      			}
      		}
      	}
      	
      	return tuTreeParent;
      }
      
      public IASTTranslationUnit getTU() {
      	if (tu == null && invisibleRoot != null) {
      		for(int i=0; i<invisibleRoot.getChildren().length; i++) {
      			if (invisibleRoot.getChildren()[i] instanceof TreeParent && invisibleRoot.getChildren()[i].getNode() instanceof IASTTranslationUnit){
      	      		tu = (IASTTranslationUnit)invisibleRoot.getChildren()[i].getNode();
      			}
      		}
      	}
      	
      	return tu;
      }

      public void dispose() {
      }

      public Object[] getElements(Object parent) {
         if (parent.equals(getViewSite())) {
            if (invisibleRoot == null)
               initialize();
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
         if (parent instanceof TreeParent) {
            return ((TreeParent) parent).getChildren();
         }
         return new Object[0];
      }

      public boolean hasChildren(Object parent) {
         if (parent instanceof TreeParent)
            return ((TreeParent) parent).hasChildren();
         return false;
      }

      private class StartInitializingASTView extends Job {
      	private static final String INITIALIZE_AST_VIEW = "Initialize AST View"; //$NON-NLS-1$
		InitializeView job = null;
      	Object[] expanded = null;
      	
      	public StartInitializingASTView(InitializeView job, Object[] expanded) {
      		super(INITIALIZE_AST_VIEW);
      		this.job = job;
      		this.expanded = expanded;
      	}
      	
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		protected IStatus run(IProgressMonitor monitor) {
		    job.schedule();
	      	
		    try {
		    	job.join();
		    } catch (InterruptedException ie) {
		    	return Status.CANCEL_STATUS;
		    }
	      		
	    	CTestPlugin.getStandardDisplay().asyncExec(new InitializeRunnable(viewer)); // update the view from the Display thread
	    	// if there are objects to expand then do so now
	    	if (expanded != null) {
	    		CTestPlugin.getStandardDisplay().asyncExec(new Runnable() {
					public void run() {
			         	// set the expansion of the view based on the original snapshot (educated guess)
			         	Tree tree = viewer.getTree();
			         	expandTreeIfNecessary(tree.getItems(), expanded);
						
					}
					
					private void expandTreeIfNecessary(TreeItem[] tree, Object[] expanded) {
			     		for( int i=0; i<tree.length; i++) {
			     			for( int j=0; j<expanded.length; j++) {
				     			if (expanded[j] instanceof TreeObject &&
				     					tree[i].getData() instanceof TreeObject &&
				     					((TreeObject)expanded[j]).toString().equals(((TreeObject)tree[i].getData()).toString()) && 
				     					((TreeObject)expanded[j]).getOffset() == (((TreeObject)tree[i].getData()).getOffset())) {
				     				tree[i].setExpanded(true);
				     				viewer.refresh();
				     				expandTreeIfNecessary(tree[i].getItems(), expanded);
				     			}
			     			}
			     		}
			     	}});
	    	}
	    	
			return job.getResult();
		}
      	
      }
      
      private class InitializeView extends Job {

      	private static final String RETRIEVING_PREPROCESSOR_PROBLEMS = "Retrieving all preprocessor problems from TU"; //$NON-NLS-1$
		private static final String RETRIEVING_PREPROCESSOR_STATEMENTS = "Retrieving all preprocessor statements from TU"; //$NON-NLS-1$
		private static final String _PREPROCESSOR_PROBLEMS_ = " preprocessor problems"; //$NON-NLS-1$
		private static final String _PREPROCESSOR_STATEMENTS_ = " preprocessor statements"; //$NON-NLS-1$
		private static final String MERGING_ = "Merging "; //$NON-NLS-1$
		private static final String GROUPING_AST = "Grouping AST View according to includes"; //$NON-NLS-1$
		private static final String GENERATING_INITIAL_TREE = "Generating initial AST Tree for the View"; //$NON-NLS-1$
		private static final String PARSING_TRANSLATION_UNIT = "Parsing Translation Unit"; //$NON-NLS-1$
		String name = null;
      	TreeParent root = null;
      	ViewContentProvider provider = null;
      	TreeViewer view = null;
      	IFile file = null;
      	
    	/**
		 * @param name
		 */
		public InitializeView(String name, ViewContentProvider provider, TreeViewer view, IFile file) {
			super(name);
			this.name = name;
			setUser(true);
			this.provider = provider;
			this.view = view;
			this.file = file;
		}
		
	    public TreeParent getInvisibleRoot() {
	      	return root;
	    }

		/**
    	 * @return Returns the scheduling rule for this operation
    	 */
    	public ISchedulingRule getScheduleRule() {
    		return ResourcesPlugin.getWorkspace().getRoot();
    	}
    	
		/* (non-Javadoc)
		 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
    	protected IStatus run(IProgressMonitor monitor) {
    		long start=0;
			long overallStart=System.currentTimeMillis();
			
    		if (file == null || lang == null || monitor == null)
	            return Status.CANCEL_STATUS;

			if (monitor.isCanceled()) return Status.CANCEL_STATUS;
			monitor.beginTask(name, 7);
			start=System.currentTimeMillis();
			
	         IPopulateDOMASTAction action = null;
	         IASTTranslationUnit tu = null;
	         try {
	         	monitor.subTask(PARSING_TRANSLATION_UNIT);
	         	start=System.currentTimeMillis();
	            tu = CDOM.getInstance().getASTService().getTranslationUnit(
	                  file,
	                  CDOM.getInstance().getCodeReaderFactory(
	                        CDOM.PARSE_SAVED_RESOURCES));
	            monitor.worked(1);
	            System.out.println("[DOM AST View] done " + PARSING_TRANSLATION_UNIT + ": " + (System.currentTimeMillis()- start) );
	         } catch (IASTServiceProvider.UnsupportedDialectException e) {
	         	return Status.CANCEL_STATUS;
	         }
	         
	         if (monitor.isCanceled()) return Status.CANCEL_STATUS;
	         monitor.subTask(GENERATING_INITIAL_TREE);
	         start=System.currentTimeMillis();
	         if (lang == ParserLanguage.CPP) {
	            action = new CPPPopulateASTViewAction(tu, monitor);
	            CPPVisitor.visitTranslationUnit(tu, (CPPBaseVisitorAction) action);
	         } else {
	            action = new CPopulateASTViewAction(tu, monitor);
	            CVisitor.visitTranslationUnit(tu, (CBaseVisitorAction) action);
	         }
	         monitor.worked(2);
             System.out.println("[DOM AST View] done " + GENERATING_INITIAL_TREE + ": " + (System.currentTimeMillis()- start) );
	         
	         // display roots
	         root = new TreeParent(null); //$NON-NLS-1$
	         
	         if (monitor.isCanceled()) return Status.CANCEL_STATUS;
	         monitor.subTask(RETRIEVING_PREPROCESSOR_STATEMENTS);
	         start=System.currentTimeMillis();
	         IASTPreprocessorStatement[] statements = tu.getAllPreprocessorStatements();
	         monitor.worked(3);
	         System.out.println("[DOM AST View] done " + RETRIEVING_PREPROCESSOR_STATEMENTS + ": " + (System.currentTimeMillis()- start) );
	         
	         monitor.subTask(MERGING_ + statements.length + _PREPROCESSOR_STATEMENTS_);
	         start=System.currentTimeMillis();
	         // merge preprocessor statements to the tree
	         action.mergePreprocessorStatements(statements);
	         monitor.worked(4);
	         System.out.println("[DOM AST View] done " + MERGING_ + statements.length + _PREPROCESSOR_STATEMENTS_ + ": " + (System.currentTimeMillis()- start) );
	         
	         if (monitor.isCanceled()) return Status.CANCEL_STATUS;
	         monitor.subTask(RETRIEVING_PREPROCESSOR_PROBLEMS);
	         start=System.currentTimeMillis();
	         IASTProblem[] problems = tu.getPreprocesorProblems();
	         monitor.worked(5);
	         System.out.println("[DOM AST View] done " + RETRIEVING_PREPROCESSOR_PROBLEMS + ": " + (System.currentTimeMillis()- start) );
	         	         
	         monitor.subTask(MERGING_ + problems.length + _PREPROCESSOR_PROBLEMS_);
	         start=System.currentTimeMillis();
	         // merge preprocessor problems to the tree
	         action.mergePreprocessorProblems(problems);
	         monitor.worked(6);
	         System.out.println("[DOM AST View] done " + MERGING_ + problems.length + _PREPROCESSOR_PROBLEMS_ + ": " + (System.currentTimeMillis()- start) );

	         if (monitor.isCanceled()) return Status.CANCEL_STATUS;
	         monitor.subTask(GROUPING_AST);
	         start=System.currentTimeMillis();
	         // group #includes
	         action.groupIncludes(statements);
	         monitor.worked(7);
	         System.out.println("[DOM AST View] done " + GROUPING_AST + ": " + (System.currentTimeMillis()- start) );

	         root.addChild(action.getTree());
	         
	         provider.setInvisibleRoot(root);
	         
	         monitor.done();
			
	         System.out.println("[DOM AST View] finished: " + (System.currentTimeMillis()- overallStart) );
	         
			return Status.OK_STATUS;
		}

      }
      
      private void initialize() {
      	invisibleRoot = new TreeParent(null); // blank the AST View, when the job above is complete it will update the AST View with the proper tree
      }
      
      protected void setInvisibleRoot(TreeParent root) {
      	invisibleRoot = root;
      }

	  	public class InitializeRunnable implements Runnable {
			TreeViewer view = null;
			
			public InitializeRunnable(TreeViewer view) {
				this.view = view;
			}
			
			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				view.refresh();
				
				if (view.getTree().getItems().length > 0) {
					TreeItem[] selection = new TreeItem[1];
					selection[0] = view.getTree().getItems()[0];
					
					// select the first item to prevent it from being selected accidentally (and possibly switching editors accidentally)
					view.getTree().setSelection(selection);
				}
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
   }

   class ViewLabelProvider extends LabelProvider {

      public String getText(Object obj) {
         return obj.toString();
      }

      public Image getImage(Object obj) {
         String imageKey = DOMASTPluginImages.IMG_DEFAULT;

         IASTNode node = null;
         if (obj instanceof TreeObject) {
            node = ((TreeObject) obj).getNode();
         }

         if (node instanceof IASTArrayModifier) {
            imageKey = DOMASTPluginImages.IMG_IASTArrayModifier;
         } else if (node instanceof IASTDeclaration) {
            if (node instanceof IASTProblemDeclaration)
         		imageKey = DOMASTPluginImages.IMG_IASTProblem;
         	else
         		imageKey = DOMASTPluginImages.IMG_IASTDeclaration;
         } else if (node instanceof IASTDeclarator) {
            imageKey = DOMASTPluginImages.IMG_IASTDeclarator;
         } else if (node instanceof IASTDeclSpecifier) {
            imageKey = DOMASTPluginImages.IMG_IASTDeclSpecifier;
         } else if (node instanceof IASTEnumerator) {
            imageKey = DOMASTPluginImages.IMG_IASTEnumerator;
         } else if (node instanceof IASTExpression) {
            imageKey = DOMASTPluginImages.IMG_IASTExpression;
         } else if (node instanceof IASTInitializer) {
            imageKey = DOMASTPluginImages.IMG_IASTInitializer;
         } else if (node instanceof IASTName) {
            imageKey = DOMASTPluginImages.IMG_IASTName;
         } else if (node instanceof IASTParameterDeclaration) {
            imageKey = DOMASTPluginImages.IMG_IASTParameterDeclaration;
         } else if (node instanceof IASTPointerOperator) {
            imageKey = DOMASTPluginImages.IMG_IASTPointerOperator;
         } else if (node instanceof IASTPreprocessorStatement) {
            imageKey = DOMASTPluginImages.IMG_IASTPreprocessorStatement;
         } else if (node instanceof IASTProblem) {
            imageKey = DOMASTPluginImages.IMG_IASTProblem;
         } else if (node instanceof IASTSimpleDeclaration) {
       		imageKey = DOMASTPluginImages.IMG_IASTSimpleDeclaration;
         } else if (node instanceof IASTStatement) {
         	if (node instanceof IASTProblemStatement)
         		imageKey = DOMASTPluginImages.IMG_IASTProblem;
         	else
         		imageKey = DOMASTPluginImages.IMG_IASTStatement;
         } else if (node instanceof IASTTranslationUnit) {
            imageKey = DOMASTPluginImages.IMG_IASTTranslationUnit;
         } else if (node instanceof IASTTypeId) {
            imageKey = DOMASTPluginImages.IMG_IASTTypeId;
         } else if (node instanceof ICASTDesignator) {
            imageKey = DOMASTPluginImages.IMG_ICASTDesignator;
         } else if (node instanceof ICPPASTConstructorChainInitializer) {
            imageKey = DOMASTPluginImages.IMG_ICPPASTConstructorChainInitializer;
         } else if (node instanceof ICPPASTTemplateParameter) {
            imageKey = DOMASTPluginImages.IMG_ICPPASTTemplateParameter;
         }

         return DOMASTPluginImages.get(imageKey);
      }
   }

   class NameSorter extends ViewerSorter {
   }

   public DOMAST() {

   }

   /**
    * This is a callback that will allow us to create the viewer and initialize
    * it.
    */
   public void createPartControl(Composite parent) {

      if (part == null) {
         part = getActiveEditor();
         
         if (!(part instanceof CEditor)) return;
      }

      viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
      drillDownAdapter = new DrillDownAdapter(viewer);

      if (part instanceof CEditor) {
         viewer.setContentProvider(new ViewContentProvider(((CEditor) part).getInputFile()));
      	 setFile(((CEditor) part).getInputFile());
      } else {
         viewer.setContentProvider(new ViewContentProvider(null)); // don't attempt to create a view based on old file info
      }

      viewer.setLabelProvider(new ViewLabelProvider());
      viewer.setInput(getViewSite());
      makeActions();
      hookContextMenu();
      hookSingleClickAction();

      customFiltersActionGroup = new CustomFiltersActionGroup(DOMAST_FILTER_GROUP_ID, viewer);
      contributeToActionBars();
   }

   public void setContentProvider(ViewContentProvider vcp) {
      viewer.setContentProvider(vcp);
      
   }

   private void hookContextMenu() {
      MenuManager menuMgr = new MenuManager(POPUPMENU);
      menuMgr.setRemoveAllWhenShown(true);
      menuMgr.addMenuListener(new IMenuListener() {
         public void menuAboutToShow(IMenuManager manager) {
            DOMAST.this.fillContextMenu(manager);
            IContributionItem[] items = manager.getItems();
            for (int i = 0; i < items.length; i++) {
               if (items[i] instanceof ActionContributionItem
                     && (((ActionContributionItem) items[i]).getAction()
                           .getText().equals(OPEN_REFERENCES) || ((ActionContributionItem) items[i])
                           .getAction().getText().equals(OPEN_DECLARATIONS))) {
                  if (viewer.getSelection() instanceof StructuredSelection
                        && ((StructuredSelection) viewer.getSelection())
                              .getFirstElement() instanceof TreeObject
                        && ((TreeObject) ((StructuredSelection) viewer
                              .getSelection()).getFirstElement()).getNode() instanceof IASTName) {
                     items[i].setVisible(true);
                  } else {
                     items[i].setVisible(false);
                  }
               }
            }
         }
      });
      Menu menu = menuMgr.createContextMenu(viewer.getControl());
      viewer.getControl().setMenu(menu);
      getSite().registerContextMenu(menuMgr, viewer);
   }

   private void contributeToActionBars() {
      IActionBars bars = getViewSite().getActionBars();
      fillLocalPullDown(bars.getMenuManager());
      fillLocalToolBar(bars.getToolBarManager());
      customFiltersActionGroup.fillActionBars(bars);
   }

   private void fillLocalPullDown(IMenuManager manager) {
   }

   void fillContextMenu(IMenuManager manager) {
      manager.add(openDeclarationsAction);
      manager.add(openReferencesAction);
      manager.add(new Separator());
      drillDownAdapter.addNavigationActions(manager);
      // Other plug-ins can contribute there actions here
      manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
   }

   private void fillLocalToolBar(IToolBarManager manager) {
   	  manager.add(expandAllAction);
      manager.add(collapseAllAction);
      manager.add(new Separator());
   	  manager.add(refreshAction);
   	  manager.add(loadActiveEditorAction);
   	  manager.add(new Separator());
   	  manager.add(clearAction);
      manager.add(new Separator());
      manager.add(searchNamesAction);
      manager.add(new Separator());
      drillDownAdapter.addNavigationActions(manager);
   }

   private void makeActions() {
 	  loadActiveEditorAction = new Action() {
        public void run() {
        	// get the active editor
        	IEditorPart editor = getActiveEditor();
        	if (editor instanceof CEditor) {
	    		IViewPart tempView = null;
	
	    		try {
	    			tempView = getSite().getPage().showView(OpenDOMViewAction.VIEW_ID);
	    		} catch (PartInitException pie) {}
	    		
	    		if (tempView != null) {
	    			if (tempView instanceof DOMAST) {
	    				IFile aFile = ((CEditor)editor).getInputFile();
	    				
	    				// check if the file is a valid "compilation unit" (based on file extension)
	    				String ext = aFile.getFileExtension().toUpperCase();
	    				if (!(ext.equals(EXTENSION_C) || ext.equals(EXTENSION_CC) || ext.equals(EXTENSION_CPP) || ext.equals(EXTENSION_CXX))) {
	    					showMessage(NOT_VALID_COMPILATION_UNIT);
	    					return;
	    				}
	    				
	    				((DOMAST)tempView).setFile(aFile);
	    				((DOMAST)tempView).setPart(editor);
	    				((DOMAST)tempView).setLang(getLanguageFromFile(aFile));
	    				((DOMAST)tempView).setContentProvider(((DOMAST)tempView).new ViewContentProvider(((CEditor)editor).getInputFile()));
	    			}
	    		}
	
	    		getSite().getPage().activate(tempView);
        	}
        }
     };
     loadActiveEditorAction.setText(LOAD_ACTIVE_EDITOR);
     loadActiveEditorAction.setToolTipText(LOAD_ACTIVE_EDITOR);
     loadActiveEditorAction.setImageDescriptor(DOMASTPluginImages.DESC_DEFAULT);
   	
     refreshAction = new Action() {
         public void run() {
            // take a snapshot of the tree expansion
         	Object[] expanded = viewer.getExpandedElements();

         	// set the new content provider
         	setContentProvider(new ViewContentProvider(file, expanded));
         }
      };
      refreshAction.setText(REFRESH_DOM_AST);
      refreshAction.setToolTipText(REFRESH_DOM_AST);
      refreshAction.setImageDescriptor(DOMASTPluginImages.DESC_IASTInitializer);

     expandAllAction = new Action() {
        public void run() {
        	viewer.expandAll();
        }
     };
     expandAllAction.setText(EXPAND_ALL);
     expandAllAction.setToolTipText(EXPAND_ALL);
     expandAllAction.setImageDescriptor(DOMASTPluginImages.DESC_EXPAND_ALL);
     
     collapseAllAction = new Action() {
        public void run() {
        	viewer.collapseAll();
        }
     };
     collapseAllAction.setText(COLLAPSE_ALL);
     collapseAllAction.setToolTipText(COLLAPSE_ALL);
     collapseAllAction.setImageDescriptor(DOMASTPluginImages.DESC_COLLAPSE_ALL);

     clearAction = new Action() {
        public void run() {
        	viewer.setContentProvider(new ViewContentProvider(null));
        	viewer.refresh();
        }
     };
     clearAction.setText(CLEAR);
     clearAction.setToolTipText(CLEAR);
     clearAction.setImageDescriptor(DOMASTPluginImages.DESC_CLEAR);
     
     searchNamesAction = new Action() {
        private void performSearch() {
        	if (viewer.getTree().getItems().length == 0) {
        		showMessage(DOM_AST_HAS_NO_CONTENT);
        	}
        	
        	FindIASTNameDialog dialog = new FindIASTNameDialog(getSite().getShell(), new FindIASTNameTarget(viewer, lang));
        	dialog.open();
       }
     	
     	public void run() {
     		performSearch();
        }
     };
     searchNamesAction.setText(SEARCH_FOR_IASTNAME);
     searchNamesAction.setToolTipText(SEARCH_FOR_IASTNAME);
     searchNamesAction.setImageDescriptor(DOMASTPluginImages.DESC_SEARCH_NAMES);
     
      openDeclarationsAction = new DisplayDeclarationsAction();
      openDeclarationsAction.setText(OPEN_DECLARATIONS);
      openDeclarationsAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

      openReferencesAction = new DisplayReferencesAction();
      openReferencesAction.setText(OPEN_REFERENCES);
      openReferencesAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

      singleClickAction = new ASTHighlighterAction(part);
   }

   private IEditorPart getActiveEditor() {
   	IEditorPart editor = null;
   	
   	if (getSite().getPage().isEditorAreaVisible() &&
	   	getSite().getPage().getActiveEditor() != null) {
	   	editor = getSite().getPage().getActiveEditor();
	    part = editor;
	    lang = getLanguageFromFile(((CEditor)editor).getInputFile());
   	}

   	return editor;
   }
   
   private ParserLanguage getLanguageFromFile(IFile file) {
   	IProject project = file.getProject();
	ICFileType type = CCorePlugin.getDefault().getFileType(project, file.getFullPath().lastSegment());
	String lid = type.getLanguage().getId();
	if ( lid != null && lid.equals(ICFileTypeConstants.LANG_CXX) ) {
		return ParserLanguage.CPP;
	}
	
	return ParserLanguage.C;
   }
   
   private class ASTHighlighterAction extends Action {
	IEditorPart aPart = null;

      public ASTHighlighterAction(IEditorPart part) {
         this.aPart = part;
      }

      public void setPart(IEditorPart part) {
         this.aPart = part;
      }

      protected boolean open(String filename) throws PartInitException,
            CModelException {
         IPath path = new Path(filename);
         IFile f = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(
               path);
         if (f != null) {
            EditorUtility.openInEditor(f);
            return true;
         }

         FileStorage storage = new FileStorage(null, path);
         EditorUtility.openInEditor(storage);
         return true;
      }

      public void run() {
         ISelection selection = viewer.getSelection();
         Object obj = ((IStructuredSelection) selection).getFirstElement();
         if (aPart instanceof CEditor && obj instanceof TreeObject) {
            String filename = ((TreeObject) obj).getFilename();
            
            if (filename.equals(TreeObject.BLANK_STRING))
            	return;
            
            IResource r = ParserUtil.getResourceForFilename(filename);
            if (r != null) {
               try {
                  aPart = EditorUtility.openInEditor(r);
               } catch (PartInitException pie) {
                  return;
               } catch (CModelException e) {
                  return;
               }
            } else {
               IPath path = new Path( filename );
       			FileStorage storage = new FileStorage(null, path);
       			try {
                  aPart = EditorUtility.openInEditor(storage);
               } catch (PartInitException e) {
                  return;
               } catch (CModelException e) {
                  return;
               }
            }
            ((CEditor) aPart).selectAndReveal(((TreeObject) obj).getOffset(),
                  ((TreeObject) obj).getLength());

            aPart.getSite().getPage().activate(aPart.getSite().getPage().findView(OpenDOMViewAction.VIEW_ID));
         }
      }
   }

   private class DisplayDeclarationsAction extends DisplaySearchResultAction {
    private static final String STRING_QUOTE = "\""; //$NON-NLS-1$
	public void run() {
     	ISelection selection = viewer.getSelection();
     	if (selection instanceof IStructuredSelection &&
     			((IStructuredSelection)selection).getFirstElement() instanceof TreeObject &&
     			((TreeObject)((IStructuredSelection)selection).getFirstElement()).getNode() instanceof IASTName) {
     		IASTName name = (IASTName)((TreeObject)((IStructuredSelection)selection).getFirstElement()).getNode();
     		StringBuffer pattern = new StringBuffer(STRING_QUOTE);
     		if (name.toString() != null)
     			pattern.append(name.toString());
     		pattern.append(STRING_QUOTE);
     		
     		if (lang == ParserLanguage.CPP) {
     			IASTName[] names = ((TreeObject)((IStructuredSelection)selection).getFirstElement()).getNode().getTranslationUnit().getDeclarations(name.resolveBinding());
     			displayNames(names, OPEN_DECLARATIONS, pattern.toString());
     		} else {
     			IASTName[] names = ((TreeObject)((IStructuredSelection)selection).getFirstElement()).getNode().getTranslationUnit().getDeclarations(name.resolveBinding());
     			displayNames(names, OPEN_DECLARATIONS, pattern.toString());
     		}
     	}
     }
   }
   
   private class DisplayReferencesAction extends DisplaySearchResultAction {
   	private static final String STRING_QUOTE = "\""; //$NON-NLS-1$
    public void run() {
     	ISelection selection = viewer.getSelection();
     	if (selection instanceof IStructuredSelection &&
     			((IStructuredSelection)selection).getFirstElement() instanceof TreeObject &&
     			((TreeObject)((IStructuredSelection)selection).getFirstElement()).getNode() instanceof IASTName) {
     		IASTName name = (IASTName)((TreeObject)((IStructuredSelection)selection).getFirstElement()).getNode();
     		StringBuffer pattern = new StringBuffer(STRING_QUOTE);
     		if (name.toString() != null)
     			pattern.append(name.toString());
     		pattern.append(STRING_QUOTE);
     		
     		if (lang == ParserLanguage.CPP) {
     			IASTName[] names = ((TreeObject)((IStructuredSelection)selection).getFirstElement()).getNode().getTranslationUnit().getReferences(name.resolveBinding());
     			displayNames(names, OPEN_REFERENCES, pattern.toString());
     		} else {
     			IASTName[] names = ((TreeObject)((IStructuredSelection)selection).getFirstElement()).getNode().getTranslationUnit().getReferences(name.resolveBinding());
     			displayNames(names, OPEN_REFERENCES, pattern.toString());
     		}
     	}
     }
   }
   
   private class DisplaySearchResultAction extends Action {
	   	protected void displayNames(IASTName[] names, String queryLabel, String pattern) {
	        DOMQuery job = new DOMQuery(names, queryLabel, pattern);
	        NewSearchUI.activateSearchResultView();
	        NewSearchUI.runQuery(job);
	     }
   }

   private void hookSingleClickAction() {
      viewer.addSelectionChangedListener(new ISelectionChangedListener() {
         public void selectionChanged(SelectionChangedEvent event) {
            singleClickAction.run();
         }
      });
   }

   void showMessage(String message) {
      MessageDialog.openInformation(viewer.getControl().getShell(), VIEW_NAME,
            message);
   }

   /**
    * Passing the focus request to the viewer's control.
    */
   public void setFocus() {
      viewer.getControl().setFocus();
   }

   public void setPart(IEditorPart part) {
      this.part = part;

      if (singleClickAction instanceof ASTHighlighterAction)
         ((ASTHighlighterAction) singleClickAction).setPart(part);
   }

   public void setLang(ParserLanguage lang) {
      this.lang = lang;
   }
   
   public void setFile(IFile file) {
    this.file = file;
   }

}