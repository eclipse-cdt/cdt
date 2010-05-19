/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.ui.tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.handlers.IHandlerService;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.ui.testplugin.CTestPlugin;
import org.eclipse.cdt.ui.testplugin.util.StringAsserts;

public class BaseUITestCase extends BaseTestCase {
	
	public BaseUITestCase() {
		super();
	}
	
	public BaseUITestCase(String name) {
		super(name);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.testplugin.util.BaseTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		final IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IViewPart view= activePage.findView("org.eclipse.cdt.ui.tests.DOMAST.DOMAST");
		if (view != null) {
			activePage.hideView(view);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.testplugin.util.BaseTestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		runEventQueue(0);
		super.tearDown();
	}

	/**
	 * Reads a section in comments form the source of the given class. Fully 
	 * equivalent to <code>readTaggedComment(getClass(), tag)</code>
	 * @since 4.0
	 */
    protected String readTaggedComment(final String tag) throws IOException {
    	return TestSourceReader.readTaggedComment(CTestPlugin.getDefault().getBundle(), "ui", getClass(), tag);
    }
 
    /**
     * Reads multiple sections in comments from the source of the given class.
     * @since 4.0
     */
	public StringBuffer[] getContentsForTest(int sections) throws IOException {
		return TestSourceReader.getContentsForTest(CTestPlugin.getDefault().getBundle(), "ui", getClass(), getName(), sections);
	}
	
	public String getAboveComment() throws IOException {
		return getContentsForTest(1)[0].toString();
	}
	
    protected IFile createFile(IContainer container, String fileName, String contents) throws Exception {
    	return TestSourceReader.createFile(container, new Path(fileName), contents);
    }
    
    protected IASTTranslationUnit createIndexBasedAST(IIndex index, ICProject project, IFile file) throws CModelException, CoreException {
    	return TestSourceReader.createIndexBasedAST(index, project, file);
    }

	protected void waitForIndexer(IIndex index, IFile file, int maxmillis) throws Exception {
		boolean firstTime= true;
		long endTime= System.currentTimeMillis() + maxmillis;
		long sleep= 1;
		while (firstTime || System.currentTimeMillis() < endTime) {
			if (!firstTime) {
				Thread.sleep(sleep);
				sleep= Math.min(250, sleep*2);
			}
			firstTime= false;
			
			if (CCorePlugin.getIndexManager().isIndexerSetupPostponed(CoreModel.getDefault().create(file.getProject())))
				continue;
			index.acquireReadLock();
			try {
				IIndexFile pfile= index.getFile(ILinkage.CPP_LINKAGE_ID, IndexLocationFactory.getWorkspaceIFL(file));
				if (pfile != null && pfile.getTimestamp() >= file.getLocalTimeStamp()) {
					return;
				}
				pfile= index.getFile(ILinkage.C_LINKAGE_ID, IndexLocationFactory.getWorkspaceIFL(file));
				if (pfile != null && pfile.getTimestamp() >= file.getLocalTimeStamp()) {
					return;
				}
			} finally {
				index.releaseReadLock();
				int time= (int) (endTime- System.currentTimeMillis());
				if (time > 0) {
					CCorePlugin.getIndexManager().joinIndexer(time, npm());
				}
			}
		}
		throw new Exception("Indexer did not complete in time!");
	}
	
	protected void runEventQueue(int time) {
		final long endTime= System.currentTimeMillis()+time;
		while(true) {
			while (Display.getCurrent().readAndDispatch());
			long diff= endTime-System.currentTimeMillis();
			if (diff <= 0) {
				break;
			}
			try {
				Thread.sleep(Math.min(20, diff));
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	protected void expandTreeItem(Tree tree, int idx) {
		expandTreeItem(tree, new int[] {idx});
	}

	protected void expandTreeItem(Tree tree, int idx1, int idx2) {
		expandTreeItem(tree, new int[] {idx1, idx2});
	}

	protected void expandTreeItem(Tree tree, int[] idxs) {
		TreeItem item= tree.getItem(idxs[0]);
		assertNotNull(item);
		expandTreeItem(item);
		for (int i=1; i < idxs.length; i++) {
			item= item.getItem(idxs[i]);
			assertNotNull(item);
			expandTreeItem(item);
		}
	}
	
	protected void expandTreeItem(TreeItem item) {
		Event event = new Event();
		event.item = item;
		item.getParent().notifyListeners(SWT.Expand, event);	
		item.setExpanded(true);
		runEventQueue(0);
	}

	protected void selectTreeItem(Tree tree, int idx) {
		selectTreeItem(tree, new int[] {idx});
	}

	protected void selectTreeItem(Tree tree, int idx1, int idx2) {
		selectTreeItem(tree, new int[] {idx1, idx2});
	}

	protected void selectTreeItem(Tree tree, int[] idxs) {
		TreeItem item= tree.getItem(idxs[0]);
		assertNotNull(item);
		for (int i=1; i < idxs.length; i++) {
			item= item.getItem(idxs[i]);
			assertNotNull(item);
		}
		tree.setSelection(item);
		Event event = new Event();
		event.item = item;
		item.getParent().notifyListeners(SWT.Selection, event);	
		runEventQueue(0);
	}

	protected void closeEditor(IEditorPart editor) {
		IWorkbenchPartSite site;
		IWorkbenchPage page;
		if (editor != null && (site= editor.getSite()) != null && (page= site.getPage()) != null) {
			page.closeEditor(editor, false);
		}
	}
	
	protected void closeAllEditors() {
		IWorkbenchWindow[] windows= PlatformUI.getWorkbench().getWorkbenchWindows();
		for (IWorkbenchWindow window : windows) {
			IWorkbenchPage[] pages= window.getPages();
			for (IWorkbenchPage page : pages) {
				page.closeAllEditors(false);
			}
		}
	}
	
	protected void restoreAllParts() throws WorkbenchException {
		IWorkbenchPage page= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		page.zoomOut();
		runEventQueue(0);

		IViewReference[] viewRefs= page.getViewReferences();
		for (IViewReference ref : viewRefs) {
			page.setPartState(ref, IWorkbenchPage.STATE_RESTORED);
		}
		IEditorReference[] editorRefs= page.getEditorReferences();
		for (IEditorReference ref : editorRefs) {
			page.setPartState(ref, IWorkbenchPage.STATE_RESTORED);
		}
		runEventQueue(0);
	}
	
	protected IViewPart activateView(String id) throws PartInitException {
		IViewPart view= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(id);
		assertNotNull(view);
		runEventQueue(0);
		return  view;
	}
	
	protected void executeCommand(IViewPart viewPart, String commandID) throws ExecutionException, NotDefinedException, NotEnabledException, NotHandledException {
		IHandlerService hs= (IHandlerService)viewPart.getSite().getService(IHandlerService.class);
		assertNotNull(hs);
		hs.executeCommand(commandID, null);
	}

	private Control[] findControls(Control w, Class<?> clazz) {
		ArrayList<Control> result= new ArrayList<Control>();
		findControls(w, clazz, result);
		return result.toArray(new Control[result.size()]);
	}
	
	private void findControls(Control w, Class<?> clazz, List<Control> result) {	
		if (clazz.isInstance(w)) {
			result.add(w);
		}
		if (w instanceof Composite) {
			Composite comp= (Composite) w;
			Control[] children= comp.getChildren();
			for (Control element : children) {
				findControls(element, clazz, result);
			}
		}
	}

	final protected TreeItem checkTreeNode(IViewPart part, int i0, String label) {
		Tree tree= null;
		TreeItem root= null;
		StringBuilder cands= new StringBuilder();
		for (int i= 0; i < 400; i++) {
			cands.setLength(0);
			Control[] trees= findControls(part.getSite().getShell(), Tree.class);
			for (int j = 0; j < trees.length; j++) {
				try {
					tree= (Tree) trees[j];
					root= tree.getItem(i0);
					if (label.equals(root.getText())) {
						return root;
					}
					if (j > 0) {
						cands.append('|');
					}
					cands.append(root.getText());
				} catch (SWTException e) {
					// in case widget was disposed, item may be replaced
				} catch (IllegalArgumentException e) {
					// item does not yet exist.
				}
			}
			runEventQueue(10);
		}
		assertNotNull("No tree in viewpart", tree);
		assertNotNull("Tree node " + label + "{" + i0 + "} does not exist!", root);
		assertEquals(label, cands.toString());
		return root;
	}

	final protected TreeItem checkTreeNode(Tree tree, int i0, String label) {
		TreeItem root= null;
		for (int millis=0; millis < 5000; millis= millis==0 ? 1 : millis*2) {
			runEventQueue(millis);
			try {
				root= tree.getItem(i0);
				if (label.equals(root.getText())) {
					return root;
				}
			} catch (SWTException e) {
				// in case widget was disposed, item may be replaced
			} catch (IllegalArgumentException e) {
				// item does not yet exist.
			}
		}
		assertNotNull("Tree node " + label + "{" + i0 + "} does not exist!", root);
		assertEquals(label, root.getText());
		return root;
	}
	
	final protected TreeItem checkTreeNode(Tree tree, int i0, int i1, String label) {
		TreeItem item= null;
		String itemText= null;
		SWTException ex= null;
		String firstItemText= null;
		for (int millis=0; millis < 5000; millis= millis==0 ? 1 : millis*2) {
			runEventQueue(millis);
			TreeItem root= tree.getItem(i0);
			ex= null;
			try {
				TreeItem firstItem= root.getItem(0);
				firstItemText= firstItem.getText();
				if (firstItemText.length() > 0 && !firstItemText.equals("...")) {
					item= root.getItem(i1);
					itemText= item.getText();
					assertNotNull("Unexpected tree node " + itemText, label);
					if (label.equals(itemText)) {
						return item;
					}
					if (millis > 2000) {
						assertEquals(label, itemText);
						return item;
					}
				}
			} catch (IllegalArgumentException e) {
				if (label != null) {
					fail("Tree node " + label + "{" + i0 + "," + i1 + "} does not exist!");
				}
				return null;
			} catch (SWTException e) {
				// widget was disposed, try again.
				ex= e;
			}
		}
		if (ex != null)
			throw ex;
		
		assertEquals("Timeout expired waiting for tree node {" + i0 + "," + i1 + "}; firstItem=" + firstItemText, label, itemText);
		return null;
	}
	
	public static void assertEqualString(String actual, String expected) {
		StringAsserts.assertEqualString(actual, expected);
	}
}
