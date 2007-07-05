/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
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
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.ui.testplugin.CTestPlugin;

public class BaseUITestCase extends BaseTestCase {
	
	public BaseUITestCase() {
		super();
	}
	
	public BaseUITestCase(String name) {
		super(name);
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
	
    protected IFile createFile(IContainer container, String fileName, String contents) throws Exception {
    	return TestSourceReader.createFile(container, new Path(fileName), contents);
    }
    
    protected IASTTranslationUnit createIndexBasedAST(IIndex index, ICProject project, IFile file) throws CModelException, CoreException {
    	return TestSourceReader.createIndexBasedAST(index, project, file);
    }

	protected void waitForIndexer(IIndex index, IFile file, int maxmillis) throws Exception {
		boolean firstTime= true;
		long endTime= System.currentTimeMillis() + maxmillis;
		while (firstTime || System.currentTimeMillis() < endTime) {
			if (!firstTime) 
				Thread.sleep(50);
			firstTime= false;
			
			index.acquireReadLock();
			try {
				IIndexFile pfile= index.getFile(IndexLocationFactory.getWorkspaceIFL(file));
				if (pfile != null && pfile.getTimestamp() >= file.getLocalTimeStamp()) {
					return;
				}
			}
			finally {
				index.releaseReadLock();
				int time= (int) (endTime- System.currentTimeMillis());
				if (time > 0) {
					CCorePlugin.getIndexManager().joinIndexer(time, NPM);
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
		for (int i= 0; i < windows.length; i++) {
			IWorkbenchPage[] pages= windows[i].getPages();
			for (int j= 0; j < pages.length; j++) {
				IWorkbenchPage page= pages[j];
				page.closeAllEditors(false);
			}
		}
	}
	
	protected void restoreAllParts() throws WorkbenchException {
		IWorkbenchPage page= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		page.zoomOut();
		runEventQueue(0);

		IViewReference[] viewRefs= page.getViewReferences();
		for (int i = 0; i < viewRefs.length; i++) {
			IViewReference ref = viewRefs[i];
			page.setPartState(ref, IWorkbenchPage.STATE_RESTORED);
		}
		IEditorReference[] editorRefs= page.getEditorReferences();
		for (int i = 0; i < editorRefs.length; i++) {
			IEditorReference ref = editorRefs[i];
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

	private Control[] findControls(Control w, Class clazz) {
		ArrayList result= new ArrayList();
		findControls(w, clazz, result);
		return (Control[]) result.toArray(new Control[result.size()]);
	}
	
	private void findControls(Control w, Class clazz, List result) {	
		if (clazz.isInstance(w)) {
			result.add(w);
		}
		if (w instanceof Composite) {
			Composite comp= (Composite) w;
			Control[] children= comp.getChildren();
			for (int i = 0; i < children.length; i++) {
				findControls(children[i], clazz, result);
			}
		}
	}

	final protected TreeItem checkTreeNode(IViewPart part, int i0, String label) {
		Tree tree= null;
		TreeItem root= null;
		for (int i=0; i<400; i++) {
			Control[] trees= findControls(part.getSite().getShell(), Tree.class);
			for (int j = 0; j < trees.length; j++) {
				try {
					tree= (Tree) trees[j];
					root= tree.getItem(i0);
					if (label.equals(root.getText())) {
						return root;
					}
				} 
				catch (SWTException e) {
					// in case widget was disposed, item may be replaced
				}
				catch (IllegalArgumentException e) {
					// item does not yet exist.
				}
			}
			runEventQueue(10);
		}
		assertNotNull("No tree in viewpart", tree);
		assertNotNull("Tree node " + label + "{" + i0 + "} does not exist!", root);
		assertEquals(label, root.getText());
		return root;
	}

	final protected TreeItem checkTreeNode(Tree tree, int i0, String label) {
		TreeItem root= null;
		for (int i=0; i<400; i++) {
			try {
				root= tree.getItem(i0);
				if (label.equals(root.getText())) {
					return root;
				}
			} 
			catch (SWTException e) {
				// in case widget was disposed, item may be replaced
			}
			catch (IllegalArgumentException e) {
				// item does not yet exist.
			}
			runEventQueue(10);
		}
		assertNotNull("Tree node " + label + "{" + i0 + "} does not exist!", root);
		assertEquals(label, root.getText());
		return root;
	}
	
	final protected TreeItem checkTreeNode(Tree tree, int i0, int i1, String label) {
		TreeItem item= null;
		TreeItem root= tree.getItem(i0);
		for (int i=0; i<400; i++) {
			try {
				item= null;
				item= root.getItem(i1);
				if (!"...".equals(item.getText())) {
					break;
				}
			} catch (SWTException e) {
				// in case widget was disposed, item may be replaced
			}
			catch (IllegalArgumentException e) {
				if (label == null) {
					return null;
				}
			}
			runEventQueue(10);
		}
		assertNotNull("Tree node " + label + "{" + i0 + "," + i1 + "} does not exist!", item);
		assertNotNull("Unexpected tree node " + item.getText(), label);
		assertEquals(label, item.getText());
		return item;
	}
}
