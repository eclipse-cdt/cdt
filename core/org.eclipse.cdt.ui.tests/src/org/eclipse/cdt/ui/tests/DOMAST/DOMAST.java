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
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
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
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
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
   private Action              refreshAction;
   private Action			   expandAllAction;
   private Action			   collapseAllAction;
   private IFile               file              = null;
   private IEditorPart         part              = null;
   private ParserLanguage              lang              = null;

   /*
    * The content provider class is responsible for providing objects to the
    * view. It can wrap existing objects in adapters or simply return objects
    * as-is. These objects may be sensitive to the current input of the view, or
    * ignore it and always show the same content (like Task List, for example).
    */

   public class ViewContentProvider implements IStructuredContentProvider,
         ITreeContentProvider {
      private TreeParent invisibleRoot;
      private IFile      aFile = null;

      /**
       *  
       */
      public ViewContentProvider() {
      }

      /**
       *  
       */
      public ViewContentProvider(IFile file) {
         this.aFile = file;
      }

      public void inputChanged(Viewer v, Object oldInput, Object newInput) {
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

      private void initialize() {
         if (aFile == null || lang == null)
            return;

         IPopulateDOMASTAction action = null;
         IASTTranslationUnit tu = null;
         try {
            tu = CDOM.getInstance().getDefaultASTService().getTranslationUnit(
                  aFile,
                  CDOM.getInstance().getCodeReaderFactory(
                        CDOM.PARSE_SAVED_RESOURCES));
         } catch (IASTServiceProvider.UnsupportedDialectException e) {
            return;
         }
         if (lang == ParserLanguage.CPP) {
            action = new CPPPopulateASTViewAction(tu);
            CPPVisitor.visitTranslationUnit(tu, (CPPBaseVisitorAction) action);
         } else {
            action = new CPopulateASTViewAction(tu);
            CVisitor.visitTranslationUnit(tu, (CBaseVisitorAction) action);
         }
         // display roots
         invisibleRoot = new TreeParent(null); //$NON-NLS-1$
         invisibleRoot.addChild(action.getTree());
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
         IWorkbenchPage[] pages = PlatformUI.getWorkbench()
               .getActiveWorkbenchWindow().getPages();

         if (pages.length == 0) {
            // TODO determine how to hide view if no pages found and part==null
         }

         outerLoop: for (int i = 0; i < pages.length; i++) {
            IEditorReference[] editorRefs = pages[i].getEditorReferences();
            for (int j = 0; j < editorRefs.length; j++) {
               part = editorRefs[j].getEditor(false);
               if (part instanceof CEditor) {
                  // TODO set the language properly if implement the above TODO
                  lang = ParserLanguage.CPP;
                  break outerLoop;
               }
            }
         }

         if (PlatformUI.getWorkbench().getActiveWorkbenchWindow()
               .getActivePage() != null
               && PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                     .getActivePage().getActiveEditor() != null)
            part = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                  .getActivePage().getActiveEditor();
      }

      viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
      drillDownAdapter = new DrillDownAdapter(viewer);

      if (part instanceof CEditor)
         viewer.setContentProvider(new ViewContentProvider(((CEditor) part)
               .getInputFile()));
      else
         viewer.setContentProvider(new ViewContentProvider(file));

      viewer.setLabelProvider(new ViewLabelProvider());
      viewer.setInput(getViewSite());
      makeActions();
      hookContextMenu();
      hookSingleClickAction();
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
   }

   private void fillLocalPullDown(IMenuManager manager) {
      //		manager.add(action1); // TODO determine the groups/filters to use
      //		manager.add(new Separator());
      //		manager.add(action2);
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
   	  manager.add(refreshAction);
      manager.add(new Separator());
      drillDownAdapter.addNavigationActions(manager);
   }

   private void makeActions() {
   	  refreshAction = new Action() {
         public void run() {
            // TODO take a snapshot of the tree expansion
         	
         	
         	// set the new content provider
         	setContentProvider(new ViewContentProvider(file));
            
         	// TODO set the expansion of the view based on the original snapshot (educated guess)
         	
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
      
      openDeclarationsAction = new Action() {
         public void run() {
            showMessage("Action 1 executed"); // TODO open declarations action //$NON-NLS-1$
                                              // ... use annotations
         }
      };
      openDeclarationsAction.setText(OPEN_DECLARATIONS);
      openDeclarationsAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

      openReferencesAction = new Action() {
         public void run() {
            showMessage("Action 2 executed"); // TODO open references action ... //$NON-NLS-1$
                                              // use annotations
         }
      };
      openReferencesAction.setText(OPEN_REFERENCES);
      openReferencesAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

      singleClickAction = new ASTHighlighterAction(part);
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
            
            if (filename.equals(TreeObject.BLANK_FILENAME))
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

   // TODO need to create a new action with the following for annotations (get
   // declarations/references)
   //	ISelection selection = viewer.getSelection();
   //	Object obj = ((IStructuredSelection)selection).getFirstElement();
   //	
   //	if (aPart instanceof CEditor) {
   //		IAnnotationModel aModel =
   // ((CEditor)aPart).getDocumentProvider().getAnnotationModel(aPart.getEditorInput());
   //		if ( aModel != null && obj instanceof TreeObject &&
   // !(((TreeObject)obj).getNode() instanceof IASTTranslationUnit) ) {
   //			Iterator itr = aModel.getAnnotationIterator();
   //			while (itr.hasNext()) {
   //				aModel.removeAnnotation((Annotation)itr.next());
   //			}
   //			
   //			ASTViewAnnotation annotation = new ASTViewAnnotation();
   //			annotation.setType(CMarkerAnnotation.WARNING_ANNOTATION_TYPE);
   //			aModel.addAnnotation(annotation, new
   // Position(((TreeObject)obj).getOffset(), ((TreeObject)obj).getLength()));
   //		}
   //	}

   // TODO implement annotation for things like open declarations/references
   //	private class ASTViewAnnotation extends Annotation implements
   // IAnnotationPresentation {
   //
   //		/* (non-Javadoc)
   //		 * @see org.eclipse.jface.text.source.IAnnotationPresentation#getLayer()
   //		 */
   //		public int getLayer() {
   //			return IAnnotationAccessExtension.DEFAULT_LAYER;
   //		}
   //
   //		/* (non-Javadoc)
   //		 * @see
   // org.eclipse.jface.text.source.IAnnotationPresentation#paint(org.eclipse.swt.graphics.GC,
   // org.eclipse.swt.widgets.Canvas, org.eclipse.swt.graphics.Rectangle)
   //		 */
   //		public void paint(GC gc, Canvas canvas, Rectangle r) {
   //		    // TODO implement this annotation image for
   //			ImageUtilities.drawImage(ASTViewPluginImages.get(ASTViewPluginImages.IMG_TO_DRAW),
   // gc, canvas, r, SWT.CENTER, SWT.TOP);
   //		}
   //	}

   private void hookSingleClickAction() {
      viewer.addSelectionChangedListener(new ISelectionChangedListener() {
         public void selectionChanged(SelectionChangedEvent event) {
            // TODO Auto-generated method stub
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

   public void setFile(IFile file) {
      this.file = file;
      viewer.setContentProvider(new ViewContentProvider(file));
   }

   public void setPart(IEditorPart part) {
      this.part = part;

      if (singleClickAction instanceof ASTHighlighterAction)
         ((ASTHighlighterAction) singleClickAction).setPart(part);
   }

   public void setLang(ParserLanguage lang) {
      this.lang = lang;
   }

}