/*******************************************************************************
 * Copyright (c) 2006, 2015 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Patrick Hofer [bug 345872] 
 *******************************************************************************/
package org.eclipse.cdt.ui.tests;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
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
import org.eclipse.ui.internal.WorkbenchPartReference;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.FileManager;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.ui.testplugin.CTestPlugin;
import org.eclipse.cdt.ui.testplugin.util.StringAsserts;

public class BaseUITestCase extends BaseTestCase {

	ArrayList<File> tempFiles = new ArrayList<File>();
	protected File tmpDir;
	protected ICProject cproject;
	protected File currentFile;
	protected ICElement currentCElem;
	protected IFile currentIFile;
	IProgressMonitor monitor = new NullProgressMonitor();
	static FileManager fileManager = new FileManager();
	
	public BaseUITestCase() {
		super();
	}

	public BaseUITestCase(String name) {
		super(name);
	}
	
	/**
	 * Override for c++ (i.e. at least one c++ test)
	 * 
	 * @return is c++ tests
	 */
	public boolean isCpp() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.testplugin.util.BaseTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		removeLeftOverProjects();
		cproject = createProject(isCpp());
		tmpDir = cproject.getProject().getLocation().makeAbsolute().toFile();
	
		
		final IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IViewPart view= activePage.findView("org.eclipse.cdt.ui.tests.DOMAST.DOMAST");
		if (view != null) {
			activePage.hideView(view);
		}
	}

	@Override
	protected void tearDown() throws Exception {
		if (cproject != null) {
			try {
				cproject.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
			} catch (CoreException e) {
				throw e;
			}
		}
		runEventQueue(0);
		super.tearDown();
	}
	
	
	/**
	 * @throws CoreException
	 */
	private void removeLeftOverProjects() throws CoreException {
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject[] projects = workspace.getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			IProject p = projects[i];
			if (p.getName().startsWith("Codan")) {
				p.delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
			}
		}
	}

	protected ICProject createProject(final boolean cpp) throws CoreException {
		final ICProject cprojects[] = new ICProject[1];
		ModelJoiner mj = new ModelJoiner();
		try {
			// Create the cproject
			final String projectName = "CDTUIProjTest_" + System.currentTimeMillis();
			final IWorkspace workspace = ResourcesPlugin.getWorkspace();
			workspace.run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					// Create the cproject
					ICProject cproject = cpp ? CProjectHelper.createCCProject(projectName, null, IPDOMManager.ID_NO_INDEXER)
							: CProjectHelper.createCProject(projectName, null, IPDOMManager.ID_NO_INDEXER);
					cprojects[0] = cproject;
				}
			}, null);
			mj.join();
		} finally {
			mj.dispose();
		}
		return cprojects[0];
	}

	protected void indexFiles() throws CoreException {
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				cproject.getProject().refreshLocal(1, monitor);
			}
		}, null);
		// Index the cproject
		CCorePlugin.getIndexManager().setIndexerId(cproject, IPDOMManager.ID_FAST_INDEXER);
		CCorePlugin.getIndexManager().reindex(cproject);
		// wait until the indexer is done
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(1000 * 60, // 1 min
				new NullProgressMonitor()));
		return;
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
	public StringBuilder[] getContentsForTest(int sections) throws IOException {
		return TestSourceReader.getContentsForTest(CTestPlugin.getDefault().getBundle(), "ui",
				getClass(), getName(), sections);
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

	protected void runEventQueue(int time) {
		final long endTime= System.currentTimeMillis() + time;
		while (true) {
			while (Display.getCurrent().readAndDispatch())
				;
			long diff= endTime - System.currentTimeMillis();
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
		expandTreeItem(tree, new int[] { idx });
	}

	protected void expandTreeItem(Tree tree, int idx1, int idx2) {
		expandTreeItem(tree, new int[] { idx1, idx2 });
	}

	protected void expandTreeItem(Tree tree, int[] idxs) {
		TreeItem item= tree.getItem(idxs[0]);
		assertNotNull(item);
		expandTreeItem(item);
		for (int i= 1; i < idxs.length; i++) {
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
		for (int i= 1; i < idxs.length; i++) {
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
		assertNotNull(label); // we don't handle testing for a base node to not appear; can be added if/when needed
		IViewReference viewRef = part.getViewSite().getPage().findViewReference(part.getViewSite().getId());
		Control viewControl = ((WorkbenchPartReference) viewRef).getPane().getControl();

		Tree tree= null;
		TreeItem root= null;
		StringBuilder cands= new StringBuilder();
		for (int i= 0; i < 400; i++) {
			cands.setLength(0);
			Control[] trees= findControls(viewControl, Tree.class);
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
		assertNotNull(label); // we don't handle testing for a base node to not appear; can be added if/when needed
		TreeItem root= null;
		for (int millis= 0; millis < 5000; millis= millis == 0 ? 1 : millis * 2) {
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
		fail("Tree node " + label + "{" + i0 + "} does not exist!");
		return null;
	}

	/**
	 * Pass label=null to test that the {i0,i1} node doesn't exist
	 */
	final protected TreeItem checkTreeNode(Tree tree, int i0, int i1, String label) {
		String firstItemText= null;
		int timeout = (label == null) ? 1000 : 5000; // see footnote[0]

		// If {i0,i1} exists, whether or not it matches label (when label != null)
		boolean nodePresent = false;

		for (int millis= 0; millis < timeout; millis= millis == 0 ? 1 : millis * 2) {
			nodePresent = false;
			runEventQueue(millis);
			TreeItem i0Node= tree.getItem(i0);
			if (!i0Node.getExpanded()) {
				expandTreeItem(i0Node);
			}
			try {
				TreeItem firstItem= i0Node.getItem(0);
				firstItemText= firstItem.getText();
				if (firstItemText.length() > 0 && !firstItemText.equals("...")) {
					TreeItem item = i0Node.getItem(i1);
					nodePresent = true;
					if (label != null && label.equals(item.getText())) {
						return item;
					}
				}
			} catch (SWTException e) {
				// in case widget was disposed, item may be replaced
			} catch (IllegalArgumentException e) {
				// item does not yet exist.
			}
		}

		if (label == null) {
			assertFalse("Tree node {" + i0 + "," + i1 + "} exists but shouldn't!", nodePresent);
		} else {
			fail("Tree node " + label + "{" + i0 + "," + i1 + "} does not exist!");
		}
		return null;
	}

	public static void assertEqualString(String actual, String expected) {
		StringAsserts.assertEqualString(actual, expected);
	}
	
	protected int pos2line(int pos) throws IOException {
		FileInputStream st = new FileInputStream(currentFile);
		try {
			int c;
			int line = 1;
			int cur = 0;
			while ((c = st.read()) != -1) {
				if (c == '\n')
					line++;
				if (cur >= pos)
					return line;
				cur++;
			}
		} finally {
			st.close();
		}
		return 0;
	}
	
	public File loadcode(String code, boolean cpp) {
		String fileKey = "@file:";
		int indf = code.indexOf(fileKey);
		if (indf >= 0) {
			int sep = code.indexOf('\n');
			if (sep != -1) {
				String line = code.substring(0, sep);
				code = code.substring(sep + 1);
				String fileName = line.substring(indf + fileKey.length()).trim();
				return loadcode(code, new File(tmpDir, fileName));
			}
		}
		String ext = cpp ? ".cpp" : ".c";
		File testFile = null;
		try {
			testFile = File.createTempFile("test", ext, tmpDir); //$NON-NLS-1$
		} catch (IOException e1) {
			fail(e1.getMessage());
			return null;
		}
		return loadcode(code, testFile);
	}

	public File loadcode(String code, String filename) {
		File testFile = new File(tmpDir, filename);
		return loadcode(code, testFile);
	}

	private File loadcode(String code, File testFile) {
		try {
			tempFiles.add(testFile);
			TestUtils.saveFile(new ByteArrayInputStream(code.trim().getBytes()), testFile);
			currentFile = testFile;
			try {
				cproject.getProject().refreshLocal(1, null);
			} catch (CoreException e) {
				fail(e.getMessage());
			}
			currentCElem = cproject.findElement(new Path(currentFile.toString()));
			currentIFile = (IFile) currentCElem.getResource();
			return testFile;
		} catch (IOException e) {
			fail("Cannot save test: " + testFile + ": " + e.getMessage());
			return null;
		} catch (CModelException e) {
			fail("Cannot find file: " + testFile + ": " + e.getMessage());
			return null;
		}
	}

	public File loadcode_c(String code) {
		return loadcode(code, true);
	}

	public File loadcode_cpp(String code) {
		return loadcode(code, false);
	}

	public File loadcode(String code) {
		return loadcode(code, isCpp());
	}
}

// Footnotes
// [0] Waiting for something to appear is very efficient; waiting for it to not
// appear is very inefficient. In the former case, regardless of how much time
// is alloted, we stop waiting as soon as the item appears, whereas in the
// latter we have to wait the entire timeout. In test suites with thousands of
// tests, efficiency is critical. Thus, in testing that a tree node doesn't have
// an Nth child, we shoot for efficiency and accept the risk of a false
// negative. More specifically, we wait only one second for the item TO NOT
// appear, whereas we give an item up to five seconds TO appear. This compromise
// is better than not having that sort of test at all, which some would argue is
// the better approach. In practice, it takes about 60-150 ms for the item to
// appear (on my machine), but we give it up to five seconds. Waiting one second
// for it to not appear should be more than adequate
