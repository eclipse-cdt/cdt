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

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.cdt.ui.tests.DOMAST.DOMASTPluginImages;
import org.eclipse.cdt.ui.tests.DOMAST.CPPPopulateASTViewAction;
import org.eclipse.cdt.ui.tests.DOMAST.CPopulateASTViewAction;
import org.eclipse.cdt.ui.tests.DOMAST.IPopulateDOMASTAction;
import org.eclipse.cdt.ui.tests.DOMAST.TreeObject;
import org.eclipse.cdt.ui.tests.DOMAST.TreeParent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.NullSourceElementRequestor;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.ast.IASTEnumerator;
import org.eclipse.cdt.core.dom.CDOM;
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
import org.eclipse.cdt.internal.core.dom.parser.AbstractGNUSourceCodeParser;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction;
import org.eclipse.cdt.internal.core.dom.parser.c.GCCParserExtensionConfiguration;
import org.eclipse.cdt.internal.core.dom.parser.c.GNUCSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor.CPPBaseVisitorAction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GNUCPPSourceParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GPPParserExtensionConfiguration;
import org.eclipse.cdt.internal.core.parser.InternalParserUtil;
import org.eclipse.cdt.internal.core.parser.scanner2.DOMScanner;
import org.eclipse.cdt.internal.core.parser.scanner2.GCCScannerExtensionConfiguration;
import org.eclipse.cdt.internal.core.parser.scanner2.GPPScannerExtensionConfiguration;
import org.eclipse.cdt.internal.core.parser.scanner2.IScannerExtensionConfiguration;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;


/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class DOMAST extends ViewPart {
	private static final String REFRESH_DOM_AST = "Refresh DOM AST"; //$NON-NLS-1$
	private static final String VIEW_NAME = "DOM View"; //$NON-NLS-1$
	private static final String POPUPMENU = "#PopupMenu"; //$NON-NLS-1$
	private static final String OPEN_DECLARATIONS = "Open Declarations"; //$NON-NLS-1$
	private static final String OPEN_REFERENCES = "Open References"; //$NON-NLS-1$
	private TreeViewer viewer;
	private DrillDownAdapter drillDownAdapter;
	private Action action1;
	private Action action2;
	private Action singleClickAction;
	private Action refreshAction;
	private IFile file = null;
	private IEditorPart part = null;
	ParserLanguage lang = null;
	
	/*
	 * The content provider class is responsible for
	 * providing objects to the view. It can wrap
	 * existing objects in adapters or simply return
	 * objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore
	 * it and always show the same content 
	 * (like Task List, for example).
	 */
	 
	public class ViewContentProvider implements IStructuredContentProvider, 
										   ITreeContentProvider {
		private TreeParent invisibleRoot;
		private IFile aFile = null;
		
		/**
		 * 
		 */
		public ViewContentProvider() {}
		
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
				if (invisibleRoot==null) initialize();
				return getChildren(invisibleRoot);
			}
			return getChildren(parent);
		}
		public Object getParent(Object child) {
			if (child instanceof TreeObject) {
				return ((TreeObject)child).getParent();
			}
			return null;
		}
		public Object [] getChildren(Object parent) {
			if (parent instanceof TreeParent) {
				return ((TreeParent)parent).getChildren();
			}
			return new Object[0];
		}
		public boolean hasChildren(Object parent) {
			if (parent instanceof TreeParent)
				return ((TreeParent)parent).hasChildren();
			return false;
		}
	
		private void initialize() {
			if ( aFile == null || lang == null) return;
			
			IPopulateDOMASTAction action = null;

			// TODO could use something like below to work with working copy... i.e. refresh button
//			IProject currentProject = aFile.getProject();
//			IWorkingCopy workingCopy = null;
//			if( editor.isDirty() ){
//				IWorkingCopy [] workingCopies = CUIPlugin.getSharedWorkingCopies();
//				if( workingCopies != null ){
//					for( int i = 0; i < workingCopies.length; i++ ){
//						if( workingCopies[i].getUnderlyingResource().equals( file ) ){
//							workingCopy = workingCopies[i];
//							break;
//						}
//					}
//				}
//			}
			
			CodeReader reader = null;
			try {
//				if( workingCopy == null )
					reader = new CodeReader(aFile.getLocation().toOSString(), aFile.getCharset() );
//				else 
//					reader = new CodeReader(aFile.getLocation().toOSString(), workingCopy.getContents());
			} catch (IOException e) {
				e.printStackTrace();
			} catch ( CoreException e ) {
	            e.printStackTrace();
	        }
			
			// get IFile
//			IWorkspace workspace= ResourcesPlugin.getWorkspace();
//			workspace.setDescription(desc);
//			getWorkbench().getActiveWorkbenchWindow();
			
			// parse IFile
			ParserMode mode = ParserMode.COMPLETE_PARSE;
			Map definitions = new Hashtable();
			
			String [] includePaths = new String[0];
			IScannerInfo scannerInfo = new ScannerInfo( definitions, includePaths );
			IScannerExtensionConfiguration configuration = null;
			if( lang == ParserLanguage.CPP )
			   configuration = new GPPScannerExtensionConfiguration();
			else
			   configuration = new GCCScannerExtensionConfiguration();
			
			IScanner scanner = new DOMScanner( reader, scannerInfo, mode, lang, ParserFactory.createDefaultLogService(), configuration, CDOM.getInstance().getCodeReaderFactory( CDOM.PARSE_SAVED_RESOURCES) );
			
//					reader,
//					scannerInfo, 		
//					mode, 
//					lang,
//					new NullSourceElementRequestor(), 			 
//					null, 
//					null  );
			AbstractGNUSourceCodeParser parser = null;
			if ( lang == ParserLanguage.C ) {
				parser = new GNUCSourceParser(
						scanner,
						mode, 
						new NullLogService(),
						new GCCParserExtensionConfiguration()
						);
			
				IASTTranslationUnit tu = parser.parse();
				
				action = new CPopulateASTViewAction(tu);
				CVisitor.visitTranslationUnit(tu, (CBaseVisitorAction)action);
			} else if ( lang == ParserLanguage.CPP ){
				parser = new GNUCPPSourceParser(
				scanner,
				mode, 
				new NullLogService(),
				new GPPParserExtensionConfiguration()
				);
				
				IASTTranslationUnit tu = parser.parse();
				
				action = new CPPPopulateASTViewAction(tu);
				CPPVisitor.visitTranslationUnit(tu, (CPPBaseVisitorAction)action);
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
				node = ((TreeObject)obj).getNode();
			}
			
			if ( node instanceof IASTArrayModifier ) {
				imageKey = DOMASTPluginImages.IMG_IASTArrayModifier;
			} else if ( node instanceof IASTDeclaration ) {
				imageKey = DOMASTPluginImages.IMG_IASTDeclaration;
			} else if ( node instanceof IASTDeclarator ) {
				imageKey = DOMASTPluginImages.IMG_IASTDeclarator;
			} else if ( node instanceof IASTDeclSpecifier ) {
				imageKey = DOMASTPluginImages.IMG_IASTDeclSpecifier;
			} else if ( node instanceof IASTEnumerator ) {
				imageKey = DOMASTPluginImages.IMG_IASTEnumerator;
			} else if ( node instanceof IASTExpression ) {
				imageKey = DOMASTPluginImages.IMG_IASTExpression;
			} else if ( node instanceof IASTInitializer ) {
				imageKey = DOMASTPluginImages.IMG_IASTInitializer;
			} else if ( node instanceof IASTName ) {
				imageKey = DOMASTPluginImages.IMG_IASTName;
			} else if ( node instanceof IASTParameterDeclaration ) {
				imageKey = DOMASTPluginImages.IMG_IASTParameterDeclaration;
			} else if ( node instanceof IASTPointerOperator ) {
				imageKey = DOMASTPluginImages.IMG_IASTPointerOperator;
			} else if ( node instanceof IASTPreprocessorStatement ) {
				imageKey = DOMASTPluginImages.IMG_IASTPreprocessorStatement;
			} else if ( node instanceof IASTProblem ) {
				imageKey = DOMASTPluginImages.IMG_IASTProblem;
			} else if ( node instanceof IASTSimpleDeclaration ) {
				imageKey = DOMASTPluginImages.IMG_IASTSimpleDeclaration;
			} else if ( node instanceof IASTStatement ) {
				imageKey = DOMASTPluginImages.IMG_IASTStatement;
			} else if ( node instanceof IASTTranslationUnit ) {
				imageKey = DOMASTPluginImages.IMG_IASTTranslationUnit;
			} else if ( node instanceof IASTTypeId ) {
				imageKey = DOMASTPluginImages.IMG_IASTTypeId;
			} else if ( node instanceof ICASTDesignator ) {
				imageKey = DOMASTPluginImages.IMG_ICASTDesignator;
			} else if ( node instanceof ICPPASTConstructorChainInitializer ) {
				imageKey = DOMASTPluginImages.IMG_ICPPASTConstructorChainInitializer;
			} else if ( node instanceof ICPPASTTemplateParameter ) {
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
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		
		if (part == null) {
			IWorkbenchPage[] pages = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPages();
			
			if (pages.length == 0) {
				// TODO determine how to hide view if no pages found and part==null
			}
			
			outerLoop: for(int i=0; i<pages.length; i++) {
				IEditorReference[] editorRefs = pages[i].getEditorReferences();
				for (int j=0; j<editorRefs.length; j++) {
					part = editorRefs[j].getEditor(false);
					if (part instanceof CEditor) {
						// TODO set the language properly if implement the above TODO
						lang = ParserLanguage.CPP;
						break outerLoop;
					}
				}
			}
			
			if (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() != null &&
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor() != null)			
				part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		}
		
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		drillDownAdapter = new DrillDownAdapter(viewer);
		
		if (part instanceof CEditor)
			viewer.setContentProvider( new ViewContentProvider(((CEditor)part).getInputFile()) );
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
				for (int i=0; i<items.length; i++) {
					if (items[i] instanceof ActionContributionItem &&
							(((ActionContributionItem)items[i]).getAction().getText().equals(OPEN_REFERENCES) ||
									((ActionContributionItem)items[i]).getAction().getText().equals(OPEN_DECLARATIONS) )) {
						if (viewer.getSelection() instanceof StructuredSelection &&
								((StructuredSelection)viewer.getSelection()).getFirstElement() instanceof TreeObject &&
								((TreeObject)((StructuredSelection)viewer.getSelection()).getFirstElement()).getNode() instanceof IASTName) {
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

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		manager.add(action2);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(refreshAction);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}

	private void makeActions() {
		refreshAction = new Action() {
			public void run() {
				setContentProvider(new ViewContentProvider(file));
			}
		};
		refreshAction.setText(REFRESH_DOM_AST);
		refreshAction.setToolTipText(REFRESH_DOM_AST);
		refreshAction.setImageDescriptor(DOMASTPluginImages.DESC_IASTInitializer);
		
		action1 = new Action() {
			public void run() {
				showMessage("Action 1 executed"); // TODO open declarations action ... use annotations
			}
		};
		action1.setText(OPEN_DECLARATIONS);
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
		action2 = new Action() {
			public void run() {
				showMessage("Action 2 executed"); // TODO open references action ... use annotations
			}
		};
		action2.setText(OPEN_REFERENCES);
		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
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
		
		public void run() {
			ISelection selection = viewer.getSelection();
			Object obj = ((IStructuredSelection)selection).getFirstElement();
			if (aPart instanceof CEditor && obj instanceof TreeObject) {
				((CEditor)aPart).selectAndReveal(((TreeObject)obj).getOffset(), ((TreeObject)obj).getLength());
			}
		}
	}

	// TODO need to create a new action with the following for annotations (get declarations/references)
//	ISelection selection = viewer.getSelection();
//	Object obj = ((IStructuredSelection)selection).getFirstElement();
//	
//	if (aPart instanceof CEditor) {
//		IAnnotationModel aModel = ((CEditor)aPart).getDocumentProvider().getAnnotationModel(aPart.getEditorInput());
//		if ( aModel != null && obj instanceof TreeObject && !(((TreeObject)obj).getNode() instanceof IASTTranslationUnit) ) {
//			Iterator itr = aModel.getAnnotationIterator();
//			while (itr.hasNext()) {
//				aModel.removeAnnotation((Annotation)itr.next());
//			}
//			
//			ASTViewAnnotation annotation = new ASTViewAnnotation();
//			annotation.setType(CMarkerAnnotation.WARNING_ANNOTATION_TYPE);
//			aModel.addAnnotation(annotation, new Position(((TreeObject)obj).getOffset(), ((TreeObject)obj).getLength()));
//		}
//	}

	
	// TODO implement annotation for things like open declarations/references
//	private class ASTViewAnnotation extends Annotation implements IAnnotationPresentation {
//
//		/* (non-Javadoc)
//		 * @see org.eclipse.jface.text.source.IAnnotationPresentation#getLayer()
//		 */
//		public int getLayer() {
//			return IAnnotationAccessExtension.DEFAULT_LAYER;
//		}
//
//		/* (non-Javadoc)
//		 * @see org.eclipse.jface.text.source.IAnnotationPresentation#paint(org.eclipse.swt.graphics.GC, org.eclipse.swt.widgets.Canvas, org.eclipse.swt.graphics.Rectangle)
//		 */
//		public void paint(GC gc, Canvas canvas, Rectangle r) {
//		    // TODO implement this annotation image for 
//			ImageUtilities.drawImage(ASTViewPluginImages.get(ASTViewPluginImages.IMG_TO_DRAW), gc, canvas, r, SWT.CENTER, SWT.TOP);
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
	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			VIEW_NAME,
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
			((ASTHighlighterAction)singleClickAction).setPart(part);
	}
	
	public void setLang(ParserLanguage lang) {
		this.lang = lang;
	}
	
}
