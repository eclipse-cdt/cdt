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

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;

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
		item.setExpanded(true);
		Event event = new Event();
		event.item = item;
		item.getParent().notifyListeners(SWT.Expand, event);	
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
	
	protected Control getFocusControl(Class clazz, int wait) {
		return getFocusControl(clazz, null, wait);
	}
	
	protected Control getFocusControl(Class clazz, Control differentTo, int wait) {
		for (int i = 0; i <= wait/10; i++) {
			Control fc= Display.getCurrent().getFocusControl();
			if (clazz.isInstance(fc) && fc != differentTo) {
				return fc;
			}
			runEventQueue(10);
		}
		fail();
		return null;
	}

}
