/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems) - Adapted for CDT
 *******************************************************************************/
package org.eclipse.cdt.ui.testplugin;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IWidgetTokenKeeper;
import org.eclipse.jface.text.IWidgetTokenOwner;
import org.eclipse.jface.text.reconciler.AbstractReconciler;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;

import org.eclipse.cdt.internal.ui.text.CReconcilingStrategy;
import org.eclipse.cdt.internal.ui.text.CompositeReconcilingStrategy;


/**
 * Copied from org.eclipse.jdt.text.tests.performance.
 * 
 * @since 4.0
 */
public class EditorTestHelper {
	
	private static class ImportOverwriteQuery implements IOverwriteQuery {
		@Override
		public String queryOverwrite(String file) {
			return ALL;
		}	
	}		
	
	public static final String TEXT_EDITOR_ID= "org.eclipse.ui.DefaultTextEditor";
	
	public static final String C_EDITOR_ID= "org.eclipse.cdt.ui.editor.CEditor";
	
	public static final String RESOURCE_PERSPECTIVE_ID= "org.eclipse.ui.resourcePerspective";
	
	public static final String C_PERSPECTIVE_ID= "org.eclipse.cdt.ui.CPerspective";
	
	public static final String OUTLINE_VIEW_ID= "org.eclipse.ui.views.ContentOutline";
	
	public static final String C_VIEW_ID= "org.eclipse.cdt.ui.CView";
	
	public static final String NAVIGATOR_VIEW_ID= "org.eclipse.ui.views.ResourceNavigator";
	
	public static final String INTRO_VIEW_ID= "org.eclipse.ui.internal.introview";

	private static final long MAX_WAIT_TIME = 60000; // don't wait longer than 60 seconds
	
	public static IEditorPart openInEditor(IFile file, boolean runEventLoop) throws PartInitException {
		IEditorPart part= IDE.openEditor(getActivePage(), file);
		if (runEventLoop)
			runEventQueue(part);
		return part;
	}
	
	public static IEditorPart openInEditor(IFile file, String editorId, boolean runEventLoop) throws PartInitException {
		IEditorPart part= IDE.openEditor(getActivePage(), file, editorId);
		if (runEventLoop)
			runEventQueue(part);
		return part;
	}
	
	public static AbstractTextEditor[] openInEditor(IFile[] files, String editorId) throws PartInitException {
		AbstractTextEditor editors[]= new AbstractTextEditor[files.length];
		for (int i= 0; i < files.length; i++) {
			editors[i]= (AbstractTextEditor) openInEditor(files[i], editorId, true);
			joinReconciler(getSourceViewer(editors[i]), 100, 10000, 100);
		}
		return editors;
	}
	
	public static IDocument getDocument(ITextEditor editor) {
		IDocumentProvider provider= editor.getDocumentProvider();
		IEditorInput input= editor.getEditorInput();
		return provider.getDocument(input);
	}
	
	public static void revertEditor(ITextEditor editor, boolean runEventQueue) {
		editor.doRevertToSaved();
		if (runEventQueue)
			runEventQueue(editor);
	}
	
	public static void closeEditor(IEditorPart editor) {
		IWorkbenchPartSite site;
		IWorkbenchPage page;
		if (editor != null && (site= editor.getSite()) != null && (page= site.getPage()) != null)
			page.closeEditor(editor, false);
	}
	
	public static void closeAllEditors() {
		IWorkbenchWindow[] windows= PlatformUI.getWorkbench().getWorkbenchWindows();
		for (int i= 0; i < windows.length; i++) {
			IWorkbenchPage[] pages= windows[i].getPages();
			for (int j= 0; j < pages.length; j++) {
				pages[j].closeAllEditors(false);
			}
		}
	}
	
	/**
	 * Runs the event queue on the current display until it is empty.
	 */
	public static void runEventQueue() {
		IWorkbenchWindow window= getActiveWorkbenchWindow();
		if (window != null)
			runEventQueue(window.getShell());
	}
	
	public static void runEventQueue(IWorkbenchPart part) {
		runEventQueue(part.getSite().getShell());
	}
	
	public static void runEventQueue(Shell shell) {
		runEventQueue(shell.getDisplay());
	}
	
	public static void runEventQueue(Display display) {
		while (display.readAndDispatch()) {
			// do nothing
		}
	}
	
	/**
	 * Runs the event queue on the current display and lets it sleep until the
	 * timeout elapses.
	 * 
	 * @param millis the timeout in milliseconds
	 */
	public static void runEventQueue(long millis) {
		runEventQueue(getActiveDisplay(), millis);
	}
	
	public static void runEventQueue(IWorkbenchPart part, long millis) {
		runEventQueue(part.getSite().getShell(), millis);
	}
	
	public static void runEventQueue(Shell shell, long millis) {
		runEventQueue(shell.getDisplay(), millis);
	}
	
	public static void runEventQueue(Display display, long minTime) {
		if (display != null) {
			DisplayHelper.sleep(display, minTime);
		} else {
			sleep((int) minTime);
		}
	}
	
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	}
	
	public static void forceFocus() {
		IWorkbenchWindow window= getActiveWorkbenchWindow();
		if (window == null) {
			IWorkbenchWindow[] wbWindows= PlatformUI.getWorkbench().getWorkbenchWindows();
			if (wbWindows.length == 0)
				return;
			window= wbWindows[0];
		}
		Shell shell= window.getShell();
		if (shell != null && !shell.isDisposed()) {
			shell.forceActive();
			shell.forceFocus();
		}
	}
	
	public static IWorkbenchPage getActivePage() {
		IWorkbenchWindow window= getActiveWorkbenchWindow();
		return window != null ? window.getActivePage() : null;
	}
	
	public static Display getActiveDisplay() {
		IWorkbenchWindow window= getActiveWorkbenchWindow();
		return window != null ? window.getShell().getDisplay() : null;
	}
	
	public static void joinBackgroundActivities(AbstractTextEditor editor) throws CoreException {
		joinBackgroundActivities(getSourceViewer(editor));
	}
	
	public static void joinBackgroundActivities(SourceViewer sourceViewer) throws CoreException {
		joinBackgroundActivities();
		joinReconciler(sourceViewer, 500, 0, 500);
	}
	
	public static void joinBackgroundActivities() throws CoreException {
		// Join Building
		Logger.global.entering("EditorTestHelper", "joinBackgroundActivities");
		Logger.global.finer("join builder");
		boolean interrupted= true;
		while (interrupted) {
			try {
				Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);
				interrupted= false;
			} catch (InterruptedException e) {
				interrupted= true;
			}
		}
		// Join indexing
		Logger.global.finer("join indexer");
		IIndexManager indexManager= CCorePlugin.getIndexManager();
		indexManager.joinIndexer(1000, new NullProgressMonitor());
		// Join jobs
		joinJobs(0, 1000, 500);
		Logger.global.exiting("EditorTestHelper", "joinBackgroundActivities");
	}
	
	public static boolean joinJobs(long minTime, long maxTime, long intervalTime) {
		Logger.global.entering("EditorTestHelper", "joinJobs");
		runEventQueue(minTime);
		
		DisplayHelper helper= new DisplayHelper() {
			@Override
			public boolean condition() {
				return allJobsQuiet();
			}
		};
		boolean quiet= helper.waitForCondition(getActiveDisplay(), maxTime > 0 ? maxTime : MAX_WAIT_TIME, intervalTime);
		Logger.global.exiting("EditorTestHelper", "joinJobs", new Boolean(quiet));
		return quiet;
	}
	
	public static void sleep(int intervalTime) {
		try {
			Thread.sleep(intervalTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean allJobsQuiet() {
		IJobManager jobManager= Job.getJobManager();
		Job[] jobs= jobManager.find(null);
		for (int i= 0; i < jobs.length; i++) {
			Job job= jobs[i];
			int state= job.getState();
			if (state == Job.RUNNING || state == Job.WAITING) {
				Logger.global.finest(job.toString());
				return false;
			}
		}
		return true;
	}
	
	public static boolean isViewShown(String viewId) {
		return getActivePage().findViewReference(viewId) != null;
	}
	
	public static boolean showView(String viewId, boolean show) throws PartInitException {
		IWorkbenchPage activePage= getActivePage();
		IViewReference view= activePage.findViewReference(viewId);
		boolean shown= view != null;
		if (shown != show)
			if (show)
				activePage.showView(viewId);
			else
				activePage.hideView(view);
		return shown;
	}
	
	public static void bringToTop() {
		getActiveWorkbenchWindow().getShell().forceActive();
	}
	
	public static void forceReconcile(SourceViewer sourceViewer) {
		Accessor reconcilerAccessor= new Accessor(getReconciler(sourceViewer), AbstractReconciler.class);
		reconcilerAccessor.invoke("forceReconciling", new Object[0]);
	}
	
	public static boolean joinReconciler(SourceViewer sourceViewer, long minTime, long maxTime, long intervalTime) {
		Logger.global.entering("EditorTestHelper", "joinReconciler");
		runEventQueue(minTime);
		
		AbstractReconciler reconciler= getReconciler(sourceViewer);
		if (reconciler == null)
			return true;
		final Accessor backgroundThreadAccessor= getBackgroundThreadAccessor(reconciler);
		Accessor reconcilerAccessor= null;
		if (reconciler instanceof MonoReconciler) {
			IReconcilingStrategy strategy= reconciler.getReconcilingStrategy(IDocument.DEFAULT_CONTENT_TYPE);
			if (strategy instanceof CReconcilingStrategy) {
				reconcilerAccessor= new Accessor(strategy, CReconcilingStrategy.class);
			} else if (strategy instanceof CompositeReconcilingStrategy) {
				IReconcilingStrategy[] strategies= ((CompositeReconcilingStrategy)strategy).getReconcilingStrategies();
				for (int i = 0; i < strategies.length; i++) {
					if (strategies[i] instanceof CReconcilingStrategy) {
						reconcilerAccessor= new Accessor(strategies[i], CReconcilingStrategy.class);
						break;
					}
				}
			}
		}
		final Accessor cReconcilerAccessor= reconcilerAccessor;
		DisplayHelper helper= new DisplayHelper() {
			@Override
			public boolean condition() {
				return !isRunning(cReconcilerAccessor, backgroundThreadAccessor);
			}
		};
		boolean finished= helper.waitForCondition(getActiveDisplay(), maxTime > 0 ? maxTime : MAX_WAIT_TIME, intervalTime);
		Logger.global.exiting("EditorTestHelper", "joinReconciler", new Boolean(finished));
		return finished;
	}
	
	public static AbstractReconciler getReconciler(SourceViewer sourceViewer) {
		return (AbstractReconciler) new Accessor(sourceViewer, SourceViewer.class).get("fReconciler");
	}
	
	public static SourceViewer getSourceViewer(AbstractTextEditor editor) {
		SourceViewer sourceViewer= (SourceViewer) new Accessor(editor, AbstractTextEditor.class).invoke("getSourceViewer", new Object[0]);
		return sourceViewer;
	}
	
	private static Accessor getBackgroundThreadAccessor(AbstractReconciler reconciler) {
		Object backgroundThread= new Accessor(reconciler, AbstractReconciler.class).get("fThread");
		return new Accessor(backgroundThread, backgroundThread.getClass());
	}
	
	private static boolean isRunning(Accessor cReconcilerAccessor, Accessor backgroundThreadAccessor) {
		return (cReconcilerAccessor != null ? !isInitialProcessDone(cReconcilerAccessor) : false) || isDirty(backgroundThreadAccessor) || isActive(backgroundThreadAccessor);
	}
	
	private static boolean isInitialProcessDone(Accessor cReconcilerAccessor) {
		return ((Boolean) cReconcilerAccessor.get("fInitialProcessDone")).booleanValue();
	}
	
	private static boolean isDirty(Accessor backgroundThreadAccessor) {
		return ((Boolean) backgroundThreadAccessor.invoke("isDirty", new Object[0])).booleanValue();
	}
	
	private static boolean isActive(Accessor backgroundThreadAccessor) {
		return ((Boolean) backgroundThreadAccessor.invoke("isActive", new Object[0])).booleanValue();
	}
	
	public static String showPerspective(String perspective) throws WorkbenchException {
		String shownPerspective= getActivePage().getPerspective().getId();
		if (!perspective.equals(shownPerspective)) {
			IWorkbench workbench= PlatformUI.getWorkbench();
			IWorkbenchWindow activeWindow= workbench.getActiveWorkbenchWindow();
			workbench.showPerspective(perspective, activeWindow);
		}
		return shownPerspective;
	}
	
	public static void closeAllPopUps(SourceViewer sourceViewer) {
		IWidgetTokenKeeper tokenKeeper= new IWidgetTokenKeeper() {
			@Override
			public boolean requestWidgetToken(IWidgetTokenOwner owner) {
				return true;
			}
		};
		sourceViewer.requestWidgetToken(tokenKeeper, Integer.MAX_VALUE);
		sourceViewer.releaseWidgetToken(tokenKeeper);
	}
	
	public static void resetFolding() {
		CUIPlugin.getDefault().getPreferenceStore().setToDefault(PreferenceConstants.EDITOR_FOLDING_ENABLED);
	}
	
	public static boolean enableFolding(boolean value) {
		IPreferenceStore preferenceStore= CUIPlugin.getDefault().getPreferenceStore();
		boolean oldValue= preferenceStore.getBoolean(PreferenceConstants.EDITOR_FOLDING_ENABLED);
		if (value != oldValue)
			preferenceStore.setValue(PreferenceConstants.EDITOR_FOLDING_ENABLED, value);
		return oldValue;
	}
	
	public static ICProject createCProject(String project, String externalSourceFolder) throws CoreException {
		return createCProject(project, externalSourceFolder, false, false);
	}
	
	public static ICProject createCProject(String project, String externalSourceFolder, boolean linkSourceFolder) throws CoreException {
		return createCProject(project, externalSourceFolder, linkSourceFolder, false);
	}

	public static ICProject createCProject(String project, String externalSourceFolder, boolean linkSourceFolder, boolean useIndexer) throws CoreException {
		ICProject cProject= CProjectHelper.createCCProject(project, "bin", useIndexer ? IPDOMManager.ID_FAST_INDEXER : IPDOMManager.ID_NO_INDEXER);
		IFolder folder;
		if (linkSourceFolder) {
			File file= FileTool.getFileInPlugin(CTestPlugin.getDefault(), new Path(externalSourceFolder));
			folder= ResourceHelper.createLinkedFolder((IProject) cProject.getUnderlyingResource(), "src", file.getAbsolutePath());
		} else {
			folder= ((IProject) cProject.getUnderlyingResource()).getFolder("src");
			importFilesFromDirectory(FileTool.getFileInPlugin(CTestPlugin.getDefault(), new Path(externalSourceFolder)), folder.getFullPath(), null);
		}
		Assert.assertNotNull(folder);
		Assert.assertTrue(folder.exists());
		CProjectHelper.addCContainer(cProject, "src");
		if (useIndexer) {
			IIndexManager indexManager= CCorePlugin.getIndexManager();
			indexManager.joinIndexer(5000, new NullProgressMonitor());
		}
		return cProject;
	}

	public static IProject createNonCProject(final String projectName, String externalSourceFolder, boolean linkSourceFolder) throws CoreException {
		final IWorkspace ws = ResourcesPlugin.getWorkspace();
		final IProject newProject[] = new IProject[1];
		ws.run(new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				IWorkspaceRoot root = ws.getRoot();
				IProject project = root.getProject(projectName);
				if (!project.exists()) {
					project.create(null);
				} else {
					project.refreshLocal(IResource.DEPTH_INFINITE, null);
				}
				if (!project.isOpen()) {
					project.open(null);
				}
				newProject[0] = project;
			}
		}, null);

		final IProject project= newProject[0];
		Assert.assertNotNull(project);
		final IFolder folder;
		if (linkSourceFolder) {
			File file= FileTool.getFileInPlugin(CTestPlugin.getDefault(), new Path(externalSourceFolder));
			folder= ResourceHelper.createLinkedFolder(project, "src", file.getAbsolutePath());
		} else {
			folder= project.getFolder("src");
			importFilesFromDirectory(FileTool.getFileInPlugin(CTestPlugin.getDefault(), new Path(externalSourceFolder)), folder.getFullPath(), null);
		}
		Assert.assertNotNull(folder);
		Assert.assertTrue(folder.exists());

		return project;
	}
	
    public static IFile createFile(IContainer container, String fileName, String contents, IProgressMonitor monitor) throws CoreException {
        //Obtain file handle
        IFile file = container.getFile(new Path(fileName));
        InputStream stream = new ByteArrayInputStream(contents.getBytes()); 
        //Create file input stream
        if(file.exists())
            file.setContents(stream, false, false, monitor);
        else
            file.create(stream, false, monitor);
        return file;
    }
    
	public static IFile[] findFiles(IResource resource) throws CoreException {
		List<IResource> files= new ArrayList<IResource>();
		findFiles(resource, files);
		return files.toArray(new IFile[files.size()]);
	}
	
	private static void findFiles(IResource resource, List<IResource> files) throws CoreException {
		if (resource instanceof IFile) {
			files.add(resource);
			return;
		}
		if (resource instanceof IContainer) {
			IResource[] resources= ((IContainer) resource).members();
			for (int i= 0; i < resources.length; i++)
				findFiles(resources[i], files);
		}
	}
	
	public static boolean setDialogEnabled(String id, boolean enabled) {
//		boolean wasEnabled= OptionalMessageDialog.isDialogEnabled(id);
//		if (wasEnabled != enabled)
//			OptionalMessageDialog.setDialogEnabled(id, enabled);
//		return wasEnabled;
		return false;
	}
	
	public static void importFilesFromDirectory(File rootDir, IPath destPath, IProgressMonitor monitor) throws CoreException {		
		try {
			IImportStructureProvider structureProvider= FileSystemStructureProvider.INSTANCE;
			List<File> files= new ArrayList<File>(100);
			addFiles(rootDir, files);
			ImportOperation op= new ImportOperation(destPath, rootDir, structureProvider, new ImportOverwriteQuery(), files);
			op.setCreateContainerStructure(false);
			op.run(monitor);
		} catch (Exception x) {
			throw newCoreException(x);
		}
	}	
	
	private static CoreException newCoreException(Throwable x) {
		return new CoreException(new Status(IStatus.ERROR, CTestPlugin.PLUGIN_ID, -1, "", x));
	}

	private static void addFiles(File dir, List<File> collection) throws IOException {
		File[] files= dir.listFiles();
		List<File> subDirs= new ArrayList<File>(2);
		for (int i= 0; i < files.length; i++) {
			if (files[i].isFile()) {
				collection.add(files[i]);
			} else if (files[i].isDirectory()) {
				subDirs.add(files[i]);
			}
		}
		Iterator<File> iter= subDirs.iterator();
		while (iter.hasNext()) {
			File subDir= iter.next();
			addFiles(subDir, collection);
		}
	}
}
