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
import org.eclipse.cdt.core.dom.ast.ASTSignatureUtil;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
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
import org.eclipse.cdt.core.dom.ast.IASTProblemExpression;
import org.eclipse.cdt.core.dom.ast.IASTProblemStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.model.CProject;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.parser.ast.IASTEnumerator;
import org.eclipse.cdt.core.resources.FileStorage;
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
import org.eclipse.core.runtime.content.IContentType;
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
import org.eclipse.jface.viewers.IContentProvider;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.views.properties.PropertySheet;

/**
 * This is a simple DOM AST View used for development testing.
 */

public class DOMAST extends ViewPart {
   public static final String VIEW_ID = "org.eclipse.cdt.ui.tests.DOMAST.DOMAST"; //$NON-NLS-1$
   private static final String PROPERTIES_VIEW = "org.eclipse.ui.views.PropertySheet"; //$NON-NLS-1$
   private static final String ASTUTIL_MENU_LABEL = "ASTUtil#"; //$NON-NLS-1$
   private static final String DISPLAY_TYPE = "getNodeType(IASTNode)"; //$NON-NLS-1$
   private static final String DISPLAY_SIGNATURE = "getNodeSignature(IASTNode)"; //$NON-NLS-1$
   private static final String DISPLAY_EXPRESSION = "getExpressionString(IASTExpression)"; //$NON-NLS-1$
   private static final String DISPLAY_INITIALIZER = "getInitializerString(IASTInitializer)"; //$NON-NLS-1$
   private static final String NOT_VALID_COMPILATION_UNIT = "The active editor does not contain a valid compilation unit."; //$NON-NLS-1$
   private static final String EXTENSION_CXX = "CXX"; //$NON-NLS-1$
   private static final String EXTENSION_CPP = "CPP"; //$NON-NLS-1$
   private static final String EXTENSION_CC = "CC"; //$NON-NLS-1$
   private static final String EXTENSION_C = "C"; //$NON-NLS-1$
   private static final String DOM_AST_HAS_NO_CONTENT = "DOM AST has no content"; //$NON-NLS-1$
   private static final String SEARCH_FOR_IASTNAME = "Search for IASTName"; //$NON-NLS-1$
   private static final String CLEAR = "Clear"; //$NON-NLS-1$
   private static final String DOMAST_FILTER_GROUP_ID = "org.eclipse.cdt.ui.tests.DOMAST.DOMASTFilterGroup"; //$NON-NLS-1$
   private static final String LOAD_ACTIVE_EDITOR = "Load Active Editor"; //$NON-NLS-1$
   private static final String COLLAPSE_ALL = "Collapse ALL"; //$NON-NLS-1$
   private static final String EXPAND_ALL = "Expand All"; //$NON-NLS-1$
   private static final String REFRESH_DOM_AST   = "Refresh DOM AST";  //$NON-NLS-1$
   public static final String VIEW_NAME         = "DOM View";         //$NON-NLS-1$
   private static final String POPUPMENU         = "#PopupMenu";       //$NON-NLS-1$
   private static final String OPEN_DECLARATIONS = "Open Declarations"; //$NON-NLS-1$
   private static final String OPEN_REFERENCES   = "Open References";  //$NON-NLS-1$
   private static final String DISPLAY_PROBLEMS   = "Display Problems";  //$NON-NLS-1$
   TreeViewer          viewer;
   private DrillDownAdapter    drillDownAdapter;
   private Action              openDeclarationsAction;
   private Action              openReferencesAction;
   private Action              displayProblemsAction;
   private Action			   displayNodeTypeAction;
   private Action			   displayNodeSignatureAction;
   private Action			   displayExpressionAction;
   private Action			   displayInitializerAction;
   protected Action            singleClickAction;
   private Action              loadActiveEditorAction;
   private Action              refreshAction;
   private Action			   expandAllAction;
   private Action			   collapseAllAction;
   private Action			   clearAction;
   private Action			   searchNamesAction;
   protected IFile             file              = null;
   private IEditorPart         part              = null;
   protected ParserLanguage    lang              = null;
   
   private CustomFiltersActionGroup customFiltersActionGroup;
   
   protected static ViewContentProvider.StartInitializingASTView initializeASTViewJob = null;

   /*
    * The content provider class is responsible for providing objects to the
    * view. It can wrap existing objects in adapters or simply return objects
    * as-is. These objects may be sensitive to the current input of the view, or
    * ignore it and always show the same content (like Task List, for example).
    */

   public class ViewContentProvider implements IStructuredContentProvider,
         ITreeContentProvider {
      private static final String POPULATING_AST_VIEW = "Populating AST View"; //$NON-NLS-1$
	  private DOMASTNodeParent invisibleRoot;
      private DOMASTNodeParent tuTreeParent = null;
      private IASTTranslationUnit tu = null;
	  protected IASTProblem[] astProblems = null;

      public ViewContentProvider() {
      }

      public ViewContentProvider(IFile file) {
      	this(file, null);
      }
      
      public ViewContentProvider(IFile file, Object[] expanded) {
       	initializeASTViewJob = new StartInitializingASTView(new InitializeView(POPULATING_AST_VIEW, this, viewer, file), expanded);
   		initializeASTViewJob.schedule();

     }
      
      public DOMASTNodeParent getTUTreeParent() {
      	if (tuTreeParent == null && invisibleRoot != null) {
      		for(int i=0; i<invisibleRoot.getChildren().length; i++) {
      			if (invisibleRoot.getChildren()[i] instanceof DOMASTNodeParent && invisibleRoot.getChildren()[i].getNode() instanceof IASTTranslationUnit){
      	      		tuTreeParent = (DOMASTNodeParent)invisibleRoot.getChildren()[i];
      	      		return tuTreeParent;
      			}
      		}
      	}
      	
      	return tuTreeParent;
      }
      
      public IASTTranslationUnit getTU() {
      	if (tu == null && invisibleRoot != null) {
      		for(int i=0; i<invisibleRoot.getChildren().length; i++) {
      			if (invisibleRoot.getChildren()[i] instanceof DOMASTNodeParent && invisibleRoot.getChildren()[i].getNode() instanceof IASTTranslationUnit){
      	      		tu = (IASTTranslationUnit)invisibleRoot.getChildren()[i].getNode();
      			}
      		}
      	}
      	
      	return tu;
      }
	  
	  public ParserLanguage getTULanguage() {
		  return lang;
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
         if (child instanceof DOMASTNodeLeaf) {
            return ((DOMASTNodeLeaf) child).getParent();
         }
         return null;
      }

      public Object[] getChildren(Object parent) {
         if (parent instanceof DOMASTNodeParent) {
            return ((DOMASTNodeParent) parent).getChildren();
         }
         return new Object[0];
      }

      public boolean hasChildren(Object parent) {
         if (parent instanceof DOMASTNodeParent)
            return ((DOMASTNodeParent) parent).hasChildren();
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
					
					private void expandTreeIfNecessary(TreeItem[] tree, Object[] theExpanded) {
			     		for( int i=0; i<tree.length; i++) {
			     			for( int j=0; j<theExpanded.length; j++) {
				     			if (theExpanded[j] instanceof DOMASTNodeLeaf &&
				     					tree[i].getData() instanceof DOMASTNodeLeaf &&
				     					((DOMASTNodeLeaf)theExpanded[j]).toString().equals(((DOMASTNodeLeaf)tree[i].getData()).toString()) && 
				     					((DOMASTNodeLeaf)theExpanded[j]).getOffset() == (((DOMASTNodeLeaf)tree[i].getData()).getOffset())) {
				     				tree[i].setExpanded(true);
				     				viewer.refresh();
				     				expandTreeIfNecessary(tree[i].getItems(), theExpanded);
				     			}
			     			}
			     		}
			     	}});
	    	}
	    	
			return job.getResult();
		}
      	
      }
      
      private class InitializeView extends Job {

		private static final String COLON_SPACE = ": "; //$NON-NLS-1$
        private static final String DOM_AST_VIEW_DONE = "[DOM AST View] done "; //$NON-NLS-1$
        private static final String DOM_AST_VIEW_FINISHED = "[DOM AST View] finished: "; //$NON-NLS-1$
        private static final String RETRIEVING_PREPROCESSOR_PROBLEMS = "Retrieving all preprocessor problems from TU"; //$NON-NLS-1$
		private static final String RETRIEVING_PREPROCESSOR_STATEMENTS = "Retrieving all preprocessor statements from TU"; //$NON-NLS-1$
		private static final String _PREPROCESSOR_PROBLEMS_ = " preprocessor problems"; //$NON-NLS-1$
		private static final String _PREPROCESSOR_STATEMENTS_ = " preprocessor statements"; //$NON-NLS-1$
		private static final String MERGING_ = "Merging "; //$NON-NLS-1$
		private static final String GROUPING_AST = "Grouping AST View according to includes"; //$NON-NLS-1$
		private static final String GENERATING_INITIAL_TREE = "Generating initial AST Tree for the View"; //$NON-NLS-1$
		private static final String PARSING_TRANSLATION_UNIT = "Parsing Translation Unit"; //$NON-NLS-1$
		String name = null;
      	DOMASTNodeParent root = null;
      	ViewContentProvider provider = null;
      	TreeViewer view = null;
      	IFile aFile = null;
      	
    	/**
		 * @param name
		 */
		public InitializeView(String name, ViewContentProvider provider, TreeViewer view, IFile file) {
			super(name);
			this.name = name;
			setUser(true);
			this.provider = provider;
			this.view = view;
			this.aFile = file;
		}
		
	    public DOMASTNodeParent getInvisibleRoot() {
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
			
    		if (aFile == null || lang == null || monitor == null)
	            return Status.CANCEL_STATUS;

			if (monitor.isCanceled()) return Status.CANCEL_STATUS;
			monitor.beginTask(name, 100);
			start=System.currentTimeMillis();
			
	         IPopulateDOMASTAction action = null;
	         IASTTranslationUnit aTu = null;
	         try {
	         	monitor.subTask(PARSING_TRANSLATION_UNIT);
	         	start=System.currentTimeMillis();
	            aTu = CDOM.getInstance().getASTService().getTranslationUnit(
	                  aFile,
	                  CDOM.getInstance().getCodeReaderFactory(
	                        CDOM.PARSE_SAVED_RESOURCES));
	            monitor.worked(30);
	            System.out.println(DOM_AST_VIEW_DONE + PARSING_TRANSLATION_UNIT + COLON_SPACE + (System.currentTimeMillis()- start) );
	         } catch (IASTServiceProvider.UnsupportedDialectException e) {
	         	return Status.CANCEL_STATUS;
	         }
	         
	         if (monitor.isCanceled()) return Status.CANCEL_STATUS;
	         monitor.subTask(GENERATING_INITIAL_TREE);
	         start=System.currentTimeMillis();
	         if (lang == ParserLanguage.CPP) {
	            action = new CPPPopulateASTViewAction(aTu, monitor);
	            aTu.accept( (CPPASTVisitor) action);
	         } else {
	            action = new CPopulateASTViewAction(aTu, monitor);
	            aTu.accept( (CASTVisitor) action);
	         }
	         monitor.worked(30);
             System.out.println(DOM_AST_VIEW_DONE + GENERATING_INITIAL_TREE + COLON_SPACE + (System.currentTimeMillis()- start) );
	         
	         // display roots
	         root = new DOMASTNodeParent(null); //$NON-NLS-1$
	         
	         if (monitor.isCanceled()) return Status.CANCEL_STATUS;
	         monitor.subTask(RETRIEVING_PREPROCESSOR_STATEMENTS);
	         start=System.currentTimeMillis();
	         IASTPreprocessorStatement[] statements = aTu.getAllPreprocessorStatements();
	         monitor.worked(5);
	         System.out.println(DOM_AST_VIEW_DONE + RETRIEVING_PREPROCESSOR_STATEMENTS + COLON_SPACE + (System.currentTimeMillis()- start) );
	         
	         monitor.subTask(MERGING_ + statements.length + _PREPROCESSOR_STATEMENTS_);
	         start=System.currentTimeMillis();
	         // merge preprocessor statements to the tree
	         action.mergePreprocessorStatements(statements);
	         monitor.worked(2);
	         System.out.println(DOM_AST_VIEW_DONE + MERGING_ + statements.length + _PREPROCESSOR_STATEMENTS_ + COLON_SPACE + (System.currentTimeMillis()- start) );
	         
	         if (monitor.isCanceled()) return Status.CANCEL_STATUS;
	         monitor.subTask(RETRIEVING_PREPROCESSOR_PROBLEMS);
	         start=System.currentTimeMillis();
	         IASTProblem[] problems = aTu.getPreprocessorProblems();
	         monitor.worked(2);
	         System.out.println(DOM_AST_VIEW_DONE + RETRIEVING_PREPROCESSOR_PROBLEMS + COLON_SPACE + (System.currentTimeMillis()- start) );
	         	         
	         monitor.subTask(MERGING_ + problems.length + _PREPROCESSOR_PROBLEMS_);
	         start=System.currentTimeMillis();
	         // merge preprocessor problems to the tree
	         action.mergePreprocessorProblems(problems);
	         monitor.worked(1);
	         System.out.println(DOM_AST_VIEW_DONE + MERGING_ + problems.length + _PREPROCESSOR_PROBLEMS_ + COLON_SPACE + (System.currentTimeMillis()- start) );

	         if (monitor.isCanceled()) return Status.CANCEL_STATUS;
	         monitor.subTask(GROUPING_AST);
	         start=System.currentTimeMillis();
	         // group #includes
	         action.groupIncludes(statements);
	         monitor.worked(30);
	         System.out.println(DOM_AST_VIEW_DONE + GROUPING_AST + COLON_SPACE + (System.currentTimeMillis()- start) );

	         root.addChild(action.getTree());
			 
			 // get the IASTProblems from the action
			 if (action instanceof CPopulateASTViewAction)
				 astProblems = ((CPopulateASTViewAction)action).getASTProblems();
			 else if (action instanceof CPPPopulateASTViewAction)
				 astProblems = ((CPPPopulateASTViewAction)action).getASTProblems();
	         
	         provider.setInvisibleRoot(root);
	         
	         monitor.done();
			
	         System.out.println(DOM_AST_VIEW_FINISHED + (System.currentTimeMillis()- overallStart) );
	         
			return Status.OK_STATUS;
		}
		
      }
	  
	  public IASTProblem[] getASTProblems() {
		  return astProblems;
	  }
      
      private void initialize() {
      	invisibleRoot = new DOMASTNodeParent(); // blank the AST View, when the job above is complete it will update the AST View with the proper tree
      }
      
      protected void setInvisibleRoot(DOMASTNodeParent root) {
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
				if (view == null) return;
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
		public void inputChanged(Viewer aViewer, Object oldInput, Object newInput) {
		}
		
		private TreeItem expandTreeToTreeObject(TreeItem[] treeItems, DOMASTNodeLeaf treeObj) {
			for (int i=0; i<treeItems.length; i++) {
				if (treeItems[i].getData() == treeObj) {
	 				return treeItems[i];
	 			}
	 			
	 			DOMASTNodeParent parent = treeObj.getParent();
	 			
	 			if (parent == null) return null; 

	 			while (parent != treeItems[i].getData()) {
	 				parent = parent.getParent();
	 				if (parent == null) break;
	 			}
	 			
	 			if (parent == treeItems[i].getData()) {
	 				treeItems[i].setExpanded(true);
	 				viewer.refresh();

	 				return expandTreeToTreeObject(treeItems[i].getItems(), treeObj);
	 			}
	 		}
	 		
	 		return null; // nothing found
		}
		
	 	private TreeItem expandTreeToTreeObject(DOMASTNodeLeaf treeObj) {
	 		return expandTreeToTreeObject(viewer.getTree().getItems(), treeObj);
	 	}
		
		/**
		 * Find an ASTNode in the tree and expand the tree to that node.
		 * Returns true if successful, false otherwise.
		 * 
		 * @param offset
		 * @param findString
		 * @param searchForward
		 * @param caseSensitive
		 * @param wholeWord
		 * @param regExSearch
		 * @return
		 */
		public boolean findAndSelect(IASTNode node, boolean useOffset) {
			// get the DOMASTNodeLeaf from the AST View's model corresponding to the IASTNode
			DOMASTNodeLeaf treeNode = null;
			TreeItem treeItem = null;
			
			treeNode =  getTUTreeParent().findTreeObject(node, useOffset);

			if (treeNode != null && treeNode.getParent() != null) {
				// found a matching DOMASTNodeLeaf, so expand the tree to that object
				treeItem = expandTreeToTreeObject(treeNode);
			}
			
			// select the node that was found (and is now displayed)
			if (treeItem != null) {
				TreeItem[] items = new TreeItem[1];
				items[0] = treeItem;
				treeItem.getParent().setSelection(items);
					
				return true;
			}

			return false;
		}
   }

   class ViewLabelProvider extends LabelProvider {

      private static final String BLANK_STRING = ""; //$NON-NLS-1$

    public String getText(Object obj) {
		  if (obj == null) return BLANK_STRING;
         return obj.toString();
      }

      public Image getImage(Object obj) {
         String imageKey = DOMASTPluginImages.IMG_DEFAULT;

         IASTNode node = null;
         if (obj instanceof DOMASTNodeLeaf) {
            node = ((DOMASTNodeLeaf) obj).getNode();
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
         	if (node instanceof IASTProblemExpression)
         		imageKey = DOMASTPluginImages.IMG_IASTProblem;
         	else
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
	  
	  viewer.addSelectionChangedListener(new UpdatePropertiesListener());
   }
   
   private class UpdatePropertiesListener implements ISelectionChangedListener {

	public void selectionChanged(SelectionChangedEvent event) {
		ISelection selection = viewer.getSelection();
		IViewPart propertyPart = getSite().getPage().findView(PROPERTIES_VIEW);
		if (propertyPart instanceof PropertySheet) {
			((PropertySheet)propertyPart).selectionChanged(getSite().getPart(), selection);
		}
	}
	   
   }

   public void setContentProvider(ViewContentProvider vcp) {
	  if (viewer == null) return;
      viewer.setContentProvider(vcp);
   }
   
   public IContentProvider getContentProvider() {
   		return viewer.getContentProvider();
   }

   private void hookContextMenu() {
      MenuManager menuMgr = new MenuManager(POPUPMENU);
      menuMgr.setRemoveAllWhenShown(true);
      menuMgr.addMenuListener(new IMenuListener() {
		  private void hideMenuItems(IMenuManager manager) {
			  IContributionItem[] items = manager.getItems();
			  
			  for (int i = 0; i < items.length; i++) {
				  if (items[i] instanceof IMenuManager) {
					  hideMenuItems((IMenuManager)items[i]);
				  }
				  
				  if (items[i] instanceof ActionContributionItem) {
					  String text = ((ActionContributionItem) items[i]).getAction().getText();
					  IASTNode selectedNode = null;
					  if (viewer.getSelection() instanceof StructuredSelection
							  && ((StructuredSelection) viewer.getSelection())
							  .getFirstElement() instanceof DOMASTNodeLeaf) {
						  selectedNode = ((DOMASTNodeLeaf) ((StructuredSelection) viewer
								  .getSelection()).getFirstElement()).getNode(); 
					  }
					  
					  if (text.equals(OPEN_REFERENCES) || text.equals(OPEN_DECLARATIONS)) {
						  if (selectedNode instanceof IASTName) {
							  items[i].setVisible(true);
						  } else {
							  items[i].setVisible(false);
						  }
					  }
					  
					  if (text.equals(DISPLAY_SIGNATURE)) {
						  if (selectedNode instanceof IASTDeclarator || 
								  selectedNode instanceof IASTDeclSpecifier ||
								  selectedNode instanceof IASTTypeId) {
							  items[i].setVisible(true);
						  } else {
							  items[i].setVisible(false);
						  }
					  } else if (text.equals(DISPLAY_TYPE)) {
						  if (selectedNode instanceof IASTDeclarator ||
								  selectedNode instanceof IASTTypeId ||
								  (selectedNode instanceof IASTName && (
										  ((IASTName)selectedNode).resolveBinding() instanceof IVariable ||
										  ((IASTName)selectedNode).resolveBinding() instanceof IFunction ||
										  ((IASTName)selectedNode).resolveBinding() instanceof IType))) {
							  items[i].setVisible(true);
						  } else {
							  items[i].setVisible(false);
						  }
					  } else if (text.equals(DISPLAY_EXPRESSION)) {
						  if (selectedNode instanceof IASTExpression) {
							  items[i].setVisible(true);
						  } else {
							  items[i].setVisible(false);
						  }
					  } else if (text.equals(DISPLAY_INITIALIZER)) {
						  if (selectedNode instanceof IASTInitializer) {
							  items[i].setVisible(true);
						  } else {
							  items[i].setVisible(false);
						  }
					  }
				  }
			  }
		  }
		  
         public void menuAboutToShow(IMenuManager manager) {
            DOMAST.this.fillContextMenu(manager);
            hideMenuItems(manager);
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
	  // ASTUtil#... menu
	  MenuManager astMenu = new MenuManager(ASTUTIL_MENU_LABEL);
	  astMenu.add(displayNodeTypeAction);
	  astMenu.add(displayNodeSignatureAction);
	  astMenu.add(displayExpressionAction);
	  astMenu.add(displayInitializerAction);
	  manager.add(astMenu);
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
	  manager.add(displayProblemsAction);
      manager.add(new Separator());
      drillDownAdapter.addNavigationActions(manager);
   }

   private void makeActions() {
 	  loadActiveEditorAction = new Action() {
        public void run() {
        	openDOMASTView(getActiveEditor());
        }
     };
     loadActiveEditorAction.setText(LOAD_ACTIVE_EDITOR);
     loadActiveEditorAction.setToolTipText(LOAD_ACTIVE_EDITOR);
     loadActiveEditorAction.setImageDescriptor(DOMASTPluginImages.DESC_RELOAD_VIEW);
   	
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
      refreshAction.setImageDescriptor(DOMASTPluginImages.DESC_REFRESH_VIEW);

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
	  
	  displayProblemsAction = new DisplayProblemsResultAction();
	  displayProblemsAction.setText(DISPLAY_PROBLEMS);
      displayProblemsAction.setImageDescriptor(DOMASTPluginImages.DESC_IASTProblem);
	  
	  displayNodeTypeAction = new Action() { 
		  public void run() {
			  ISelection selection = viewer.getSelection();
		     	if (selection instanceof IStructuredSelection &&
		     			((IStructuredSelection)selection).getFirstElement() instanceof DOMASTNodeLeaf &&
		     			((DOMASTNodeLeaf)((IStructuredSelection)selection).getFirstElement()).getNode() != null) {
					showMessage("ASTUtil#getNodeType(IASTNode): \"" + ASTTypeUtil.getNodeType(((DOMASTNodeLeaf)((IStructuredSelection)selection).getFirstElement()).getNode()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$
		     	}
		  } };
	  displayNodeTypeAction.setText(DISPLAY_TYPE);
      displayNodeTypeAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

	  displayNodeSignatureAction = new Action() { 
		  public void run() {
			  ISelection selection = viewer.getSelection();
		     	if (selection instanceof IStructuredSelection &&
		     			((IStructuredSelection)selection).getFirstElement() instanceof DOMASTNodeLeaf &&
		     			((DOMASTNodeLeaf)((IStructuredSelection)selection).getFirstElement()).getNode() != null) {
					showMessage("ASTSignatureUtil#getNodeSignature(IASTNode): \"" + ASTSignatureUtil.getNodeSignature(((DOMASTNodeLeaf)((IStructuredSelection)selection).getFirstElement()).getNode()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$
		     	}
		  } };
      displayNodeSignatureAction.setText(DISPLAY_SIGNATURE);
	  displayNodeSignatureAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
		  .getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
	  
	  displayExpressionAction = new Action() { 
		  public void run() {
			  ISelection selection = viewer.getSelection();
		     	if (selection instanceof IStructuredSelection &&
		     			((IStructuredSelection)selection).getFirstElement() instanceof DOMASTNodeLeaf &&
		     			((DOMASTNodeLeaf)((IStructuredSelection)selection).getFirstElement()).getNode() instanceof IASTExpression) {
					showMessage("ASTSignatureUtil#getExpressionString(IASTExpression): \"" + ASTSignatureUtil.getExpressionString((IASTExpression)((DOMASTNodeLeaf)((IStructuredSelection)selection).getFirstElement()).getNode()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$
		     	}
		  } };
	  displayExpressionAction.setText(DISPLAY_EXPRESSION);
	  displayExpressionAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
		  .getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
	  
	  displayInitializerAction = new Action() { 
		  public void run() {
			  ISelection selection = viewer.getSelection();
		     	if (selection instanceof IStructuredSelection &&
		     			((IStructuredSelection)selection).getFirstElement() instanceof DOMASTNodeLeaf &&
		     			((DOMASTNodeLeaf)((IStructuredSelection)selection).getFirstElement()).getNode() instanceof IASTInitializer) {
					showMessage("ASTSignatureUtil#getInitializerString(IASTInitializer): \"" + ASTSignatureUtil.getInitializerString((IASTInitializer)((DOMASTNodeLeaf)((IStructuredSelection)selection).getFirstElement()).getNode()) + "\""); //$NON-NLS-1$ //$NON-NLS-2$
		     	}
		  } };
	  displayInitializerAction.setText(DISPLAY_INITIALIZER);
	  displayInitializerAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
		  .getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
	  
      singleClickAction = new ASTHighlighterAction(part);
   }

   protected IEditorPart getActiveEditor() {
   	IEditorPart editor = null;
   	
   	if (getSite().getPage().isEditorAreaVisible() &&
	   	getSite().getPage().getActiveEditor() != null) {
	   	editor = getSite().getPage().getActiveEditor();
	    part = editor;
	    lang = getLanguageFromFile(((CEditor)editor).getInputFile());
   	}

   	return editor;
   }
      
   private class ASTHighlighterAction extends Action {
       private static final String A_PART_INSTANCEOF = "aPart instanceof "; //$NON-NLS-1$
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
          if (obj instanceof DOMASTNodeLeaf) {
             if (((DOMASTNodeLeaf)obj).getNode() instanceof IASTTranslationUnit) // don't do anything for TU
                 return;
             
             String filename = ((DOMASTNodeLeaf) obj).getFilename();
             
             if (filename.equals(DOMASTNodeLeaf.BLANK_STRING))
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

                ICProject cproject = new CProject(null, file.getProject());
                ITranslationUnit unit = CoreModel.getDefault().createTranslationUnitFrom(cproject, path);
                if (unit != null) 
                    try {
                     aPart = EditorUtility.openInEditor(unit);
                } catch (PartInitException e) {
                   return;
                } catch (CModelException e) {
                   return;
                }
             }
             
             if( aPart instanceof AbstractTextEditor )
             {
                 ((AbstractTextEditor) aPart).selectAndReveal(((DOMASTNodeLeaf) obj).getOffset(),
                       ((DOMASTNodeLeaf) obj).getLength());
             }
             else
                 System.out.println( A_PART_INSTANCEOF + aPart.getClass().getName() );
     
             aPart.getSite().getPage().activate(aPart.getSite().getPage().findView(VIEW_ID));
             
          }
       }

   }

   private class DisplayDeclarationsAction extends DisplaySearchResultAction {
    private static final String STRING_QUOTE = "\""; //$NON-NLS-1$
	public void run() {
     	ISelection selection = viewer.getSelection();
     	if (selection instanceof IStructuredSelection &&
     			((IStructuredSelection)selection).getFirstElement() instanceof DOMASTNodeLeaf &&
     			((DOMASTNodeLeaf)((IStructuredSelection)selection).getFirstElement()).getNode() instanceof IASTName) {
     		IASTName name = (IASTName)((DOMASTNodeLeaf)((IStructuredSelection)selection).getFirstElement()).getNode();
     		StringBuffer pattern = new StringBuffer(STRING_QUOTE);
     		if (name.toString() != null)
     			pattern.append(name.toString());
     		pattern.append(STRING_QUOTE);
     		
     		if (lang == ParserLanguage.CPP) {
     			IASTName[] names = ((DOMASTNodeLeaf)((IStructuredSelection)selection).getFirstElement()).getNode().getTranslationUnit().getDeclarations(name.resolveBinding());
     			displayNames(names, OPEN_DECLARATIONS, pattern.toString());
     		} else {
     			IASTName[] names = ((DOMASTNodeLeaf)((IStructuredSelection)selection).getFirstElement()).getNode().getTranslationUnit().getDeclarations(name.resolveBinding());
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
     			((IStructuredSelection)selection).getFirstElement() instanceof DOMASTNodeLeaf &&
     			((DOMASTNodeLeaf)((IStructuredSelection)selection).getFirstElement()).getNode() instanceof IASTName) {
     		IASTName name = (IASTName)((DOMASTNodeLeaf)((IStructuredSelection)selection).getFirstElement()).getNode();
     		StringBuffer pattern = new StringBuffer(STRING_QUOTE);
     		if (name.toString() != null)
     			pattern.append(name.toString());
     		pattern.append(STRING_QUOTE);
     		
     		if (lang == ParserLanguage.CPP) {
     			IASTName[] names = ((DOMASTNodeLeaf)((IStructuredSelection)selection).getFirstElement()).getNode().getTranslationUnit().getReferences(name.resolveBinding());
     			displayNames(names, OPEN_REFERENCES, pattern.toString());
     		} else {
     			IASTName[] names = ((DOMASTNodeLeaf)((IStructuredSelection)selection).getFirstElement()).getNode().getTranslationUnit().getReferences(name.resolveBinding());
     			displayNames(names, OPEN_REFERENCES, pattern.toString());
     		}
     	}
     }
   }
   
   private class DisplaySearchResultAction extends Action {
	   	protected void displayNames(IASTName[] names, String queryLabel, String pattern) {
	        DOMDisplaySearchNames job = new DOMDisplaySearchNames(names, queryLabel, pattern);
	        NewSearchUI.activateSearchResultView();
	        NewSearchUI.runQueryInBackground(job);
	     }
   }
   
   private class DisplayProblemsResultAction extends Action {
	   	private static final String IASTPROBLEM = "IASTProblem"; //$NON-NLS-1$
		private static final String PROBLEMS_FOUND = "Problems Found"; //$NON-NLS-1$
		protected void displayProblems(IASTProblem[] problems, String queryLabel, String pattern) {
	        DOMDisplaySearchNames job = new DOMDisplaySearchNames(problems, queryLabel, pattern);
	        NewSearchUI.activateSearchResultView();
	        NewSearchUI.runQueryInBackground(job);
	     }
		
		public void run() {
			if (viewer.getTree().getItems().length == 0) {
        		showMessage(DOM_AST_HAS_NO_CONTENT);
        	}
        	
        	if (viewer.getContentProvider() instanceof ViewContentProvider) {
				displayProblems(((ViewContentProvider)viewer.getContentProvider()).getASTProblems(), PROBLEMS_FOUND, IASTPROBLEM);
        	}
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
            message.replaceAll("&", "&&")); //$NON-NLS-1$ //$NON-NLS-2$
   }

   /**
    * Passing the focus request to the viewer's control.
    */
   public void setFocus() {
       if (viewer==null) return;
      viewer.getControl().setFocus();
	  
	  ISelection selection = viewer.getSelection();
      IViewPart propertyPart = getSite().getPage().findView(PROPERTIES_VIEW);
	  if (propertyPart instanceof PropertySheet) {
		  ((PropertySheet)propertyPart).selectionChanged(getSite().getPart(), selection);
	  }
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

   private class RunAfterViewOpenJob extends Job {
   	
   	Runnable runner = null;
   	
   	/**
	 * 
	 */
	public RunAfterViewOpenJob(String name, Runnable runner) {
		super(name);
		this.runner = runner;
	}
   	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IStatus run(IProgressMonitor monitor) {
		try {
			initializeASTViewJob.join();
			CTestPlugin.getStandardDisplay().asyncExec(runner);
		} catch (InterruptedException ie) {}
		
		return Status.OK_STATUS;

	}
   }
   
   /**
    * Open the DOM AST View and return a reference to it.  This helper method can also be used to run an
    * Action after the DOM AST View has been fully loaded (like find/select nodes in the view).
    * 
    * Note:  The action is not guaranteed to run.  An example would be if loading the view is canceled.
    * 
    * @param editor
    * @param action
    * @return
    */
    public static IViewPart openDOMASTViewRunAction(IEditorPart editor, Runnable runnable, String nameOfJob) {
    	IViewPart view = openDOMASTView(editor);
    	
    	if (view == null) return null;
    	
   		RunAfterViewOpenJob job = ((DOMAST)view).new RunAfterViewOpenJob(nameOfJob, runnable);
   		job.schedule();
    	
    	return view;
    }
   
    /**
     * Open the DOM AST View and return a reference to it.
     * 
     * @param editor
     * @return
     */
	public static IViewPart openDOMASTView(IEditorPart editor) {
		IWorkbenchPartSite site = editor.getSite();
		Shell shell = site.getShell();
		IViewPart tempView = null;
		
		// get the active editor
    	if (editor instanceof CEditor) {

    		try {
    			tempView = site.getPage().showView(VIEW_ID);
    		} catch (PartInitException pie) {}
    		
    		if (tempView != null) {
    			if (tempView instanceof DOMAST) {
    				IFile aFile = ((CEditor)editor).getInputFile();
    				
    				// check if the file is a valid "compilation unit" (based on file extension)
                    if (aFile == null) {
                        MessageDialog.openInformation(shell, DOMAST.VIEW_NAME, NOT_VALID_COMPILATION_UNIT);
                        return null;
                    }
                    
    				String ext = aFile.getFileExtension().toUpperCase();
    				if (!(ext.equals(EXTENSION_C) || ext.equals(EXTENSION_CC) || ext.equals(EXTENSION_CPP) || ext.equals(EXTENSION_CXX))) {
    					MessageDialog.openInformation(shell, DOMAST.VIEW_NAME, NOT_VALID_COMPILATION_UNIT);
    					return null;
    				}
    				
    				((DOMAST)tempView).setFile(aFile);
    				((DOMAST)tempView).setPart(editor);
    				((DOMAST)tempView).setLang(getLanguageFromFile(aFile));
    				((DOMAST)tempView).setContentProvider(((DOMAST)tempView).new ViewContentProvider(((CEditor)editor).getInputFile()));
    			}
    		}

    		site.getPage().activate(tempView);
    		
    	}
    	
    	return tempView;
	}
    	
    public static ParserLanguage getLanguageFromFile(IFile file) {
        if (file == null) { // assume CPP
            return ParserLanguage.CPP;
        }
        
       	IProject project = file.getProject();
       	String lid = null;
    	IContentType type = CCorePlugin.getContentType(project, file.getFullPath().lastSegment());
    	if (type != null) {
    		lid = type.getId();
    	}
    	if ( lid != null 
    			&& ( lid.equals(CCorePlugin.CONTENT_TYPE_CXXSOURCE) || lid.equals(CCorePlugin.CONTENT_TYPE_CXXHEADER)) ) {
    		return ParserLanguage.CPP;
    	}
    	
    	return ParserLanguage.C;
    }
   
}