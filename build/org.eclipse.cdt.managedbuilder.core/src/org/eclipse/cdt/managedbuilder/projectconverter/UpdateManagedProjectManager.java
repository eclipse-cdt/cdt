/*******************************************************************************
 * Copyright (c) 2004, 2006 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.projectconverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PluginVersionIdentifier;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IOverwriteQuery;

public class UpdateManagedProjectManager {
	static private HashMap fUpdateManagers = new HashMap();
	static private IOverwriteQuery fBackupFileOverwriteQuery = null;
	static private IOverwriteQuery fOpenQuestionQuery = null;
	static private IOverwriteQuery fUpdateProjectQuery = null;

	private ManagedBuildInfo fConvertedInfo= null;
	private boolean fIsInfoReadOnly = false;
	final private IProject fProject;
	
	private UpdateManagedProjectManager(IProject project){
		fProject = project;
	}	
	
	public static void setBackupFileOverwriteQuery(IOverwriteQuery backupFileOverwriteQuery){
		fBackupFileOverwriteQuery = backupFileOverwriteQuery;
	}
/*
	public static void setOpenQuestionQuery(IOverwriteQuery openQuestionQuery){
		fOpenQuestionQuery = openQuestionQuery;
	}
*/
	public static void setUpdateProjectQuery(IOverwriteQuery updateProjectQuery){
		fUpdateProjectQuery = updateProjectQuery;
	}
	
	private static boolean getBooleanFromQueryAnswer(String answer){
		if(IOverwriteQuery.ALL.equalsIgnoreCase(answer) ||
				IOverwriteQuery.YES.equalsIgnoreCase(answer))
			return true;
		return false;
	}

	synchronized static private UpdateManagedProjectManager getUpdateManager(IProject project){
		UpdateManagedProjectManager mngr = getExistingUpdateManager(project);
		if(mngr == null)
			mngr = createUpdateManager(project);
		return mngr;
	}
	
	static private UpdateManagedProjectManager getExistingUpdateManager(IProject project){
		return (UpdateManagedProjectManager)fUpdateManagers.get(project.getName());
	}

	static private UpdateManagedProjectManager createUpdateManager(IProject project){
		UpdateManagedProjectManager mngr = new UpdateManagedProjectManager(project);
		fUpdateManagers.put(project.getName(),mngr);
		return mngr;
	}

	static private void removeUpdateManager(IProject project){
		UpdateManagedProjectManager mngr = getExistingUpdateManager(project);
		if(mngr == null)
			return;
		fUpdateManagers.remove(project.getName());
	}
	
	static protected PluginVersionIdentifier getManagedBuildInfoVersion(String version){
		if(version == null)
			version = "1.2"; //$NON-NLS-1$
		return new PluginVersionIdentifier(version);
	}

	static public boolean isCompatibleProject(IManagedBuildInfo info) {
		if(info == null)
			return false;

		PluginVersionIdentifier projVersion = getManagedBuildInfoVersion(info.getVersion());

		PluginVersionIdentifier compVersion = ManagedBuildManager.getBuildInfoVersion();

		if(compVersion.isEquivalentTo(projVersion))
			return true;
		return false;
	}

	/* (non-Javadoc)
	 * Create a back-up file
	 *  
	 * @param settingsFile
	 * @param suffix
	 * @param monitor
	 * @param project
	 */
	static void backupFile(IFile settingsFile, String suffix, IProgressMonitor monitor, IProject project){
		UpdateManagedProjectManager mngr = getExistingUpdateManager(project);
		if(mngr == null || mngr.fIsInfoReadOnly)
			return;
		IContainer destFolder = project;
		IFile dstFile = destFolder.getFile(new Path(settingsFile.getName()+suffix)); 
		mngr.backupFile(settingsFile,  dstFile, monitor,  project, fBackupFileOverwriteQuery);
	}

	/*
	 *  Creates a back-up file. This method is useful for the vendor who wants
	 *  to create a backup of '.cdtbuild' file before project conversion.

 	 * @param settingsFile
	 * @param suffix
	 * @param monitor
	 * @param project
	 */
	
	public static void backupSettingsFile(IFile settingsFile, String suffix, IProgressMonitor monitor, IProject project){
		UpdateManagedProjectManager mngr = getUpdateManager(project);
		if(mngr == null || mngr.fIsInfoReadOnly)
			return;
		IContainer destFolder = project;
		IFile dstFile = destFolder.getFile(new Path(settingsFile.getName()+suffix)); 
		mngr.backupFile(settingsFile,  dstFile, monitor,  project, fBackupFileOverwriteQuery);
	}
	
	/* (non-Javadoc)
	 * Create a back-up file
	 * 
	 * @param srcFile
	 * @param dstFile
	 * @param monitor
	 * @param project
	 * @param query
	 */
	private void backupFile(IFile srcFile, IFile dstFile, IProgressMonitor monitor, IProject project, IOverwriteQuery query){
		File src = srcFile.getLocation().toFile();
		File dst = dstFile.getLocation().toFile();
		backupFile(src, dst, monitor, project, query);
	}

	private void backupFile(File srcFile, File dstFile, IProgressMonitor monitor, IProject project, IOverwriteQuery query){
		try {
			if (dstFile.exists()) {
				boolean shouldUpdate;
				if(query != null)
					shouldUpdate = getBooleanFromQueryAnswer(query.queryOverwrite(dstFile.getName()));
				else
					shouldUpdate = openQuestion(ConverterMessages.getResourceString("UpdateManagedProjectManager.0"), //$NON-NLS-1$
							ConverterMessages.getFormattedString("UpdateManagedProjectManager.1", new String[] {dstFile.getName(),project.getName()})); //$NON-NLS-1$

				if (shouldUpdate) {
					dstFile.delete();
				} else {
					throw new OperationCanceledException(ConverterMessages.getFormattedString("UpdateManagedProjectManager.2", project.getName())); //$NON-NLS-1$
				}
			}
			copyFile(srcFile, dstFile);
		} catch(Exception e){
			fIsInfoReadOnly = true;
		}
	}

	void copyFile(File src, File dst) throws IOException {
		FileInputStream fis = null;
		FileOutputStream fos = null;
		
		try {
			fis = new FileInputStream(src);
			fos = new FileOutputStream(dst);
			
			final int BUFSIZ = 1024;
			byte buf[] = new byte[BUFSIZ];
			int len = 0;
			
			while ((len = fis.read(buf)) > 0) {
				fos.write(buf, 0, len);
			}
		} finally {
			if (fis != null) {
				fis.close();
			}
			if (fos != null) {
				fos.close();
			}
		}
	}

	private void restoreFile(String backupFileName, String restoreFileName, IProgressMonitor monitor, IProject project){
		IContainer destFolder = project;
		File restoreFile = destFolder.getFile(new Path(restoreFileName)).getLocation().toFile();
		File backupFile = destFolder.getFile(new Path(backupFileName)).getLocation().toFile();
		
		try{
			if (restoreFile.exists()) {
				restoreFile.delete();
			}
			copyFile(backupFile, restoreFile);
		} catch(Exception e){
			fIsInfoReadOnly = true;
		}
	}
	
	static private boolean openQuestion(final String title, final String message){
		if(fOpenQuestionQuery != null)
			return getBooleanFromQueryAnswer(fOpenQuestionQuery.queryOverwrite(message));

		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if(window == null){
			IWorkbenchWindow windows[] = PlatformUI.getWorkbench().getWorkbenchWindows();
			window = windows[0];
		}

		final Shell shell = window.getShell();
		final boolean [] answer = new boolean[1];
		shell.getDisplay().syncExec(new Runnable() {
			public void run() {
				answer[0] = MessageDialog.openQuestion(shell,title,message); 
			}
		});	
		return answer[0];
	}
	
	/**
	 * returns ManagedBuildInfo for the current project
	 * if converter is currently running 
	 * @param project project for which ManagedBuildInfo is needed
	 * @return the pointer to the project ManagedBuildInfo or null
	 * if converter is no running
	 */
	static public ManagedBuildInfo getConvertedManagedBuildInfo(IProject project){
		UpdateManagedProjectManager mngr = getExistingUpdateManager(project);
		if(mngr == null)
			return null;
		return mngr.getConvertedManagedBuildInfo();
	}
	
	private ManagedBuildInfo getConvertedManagedBuildInfo(){
		return fConvertedInfo;
	}
	
	private void doProjectUpdate(ManagedBuildInfo info) 
						throws CoreException {
		fConvertedInfo = info;
		NullProgressMonitor monitor = new NullProgressMonitor();
		IFile settingsFile = fProject.getFile(ManagedBuildManager.SETTINGS_FILE_NAME);
		IFile backupFile = fProject.getFile(ManagedBuildManager.SETTINGS_FILE_NAME + "_initial"); //$NON-NLS-1$
		if(isCompatibleProject(info))
			return;

		try {
			if (!settingsFile.getLocation().toFile().exists()) 
				throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), -1,
					ConverterMessages.getResourceString("UpdateManagedProjectManager.6"),null)); //$NON-NLS-1$

				
			backupFile(settingsFile, backupFile, monitor, fProject, new IOverwriteQuery(){
					public String queryOverwrite(String file) {
						return ALL;
					}	
				});
	
			PluginVersionIdentifier version = getManagedBuildInfoVersion(info.getVersion());

			boolean shouldUpdate;
			if(fUpdateProjectQuery != null)
				shouldUpdate = getBooleanFromQueryAnswer(fUpdateProjectQuery.queryOverwrite(fProject.getFullPath().toString()));
			else
				shouldUpdate = openQuestion(ConverterMessages.getResourceString("UpdateManagedProjectManager.3"), //$NON-NLS-1$
					ConverterMessages.getFormattedString("UpdateManagedProjectManager.4", new String[]{fProject.getName(),version.toString(),ManagedBuildManager.getBuildInfoVersion().toString()}) //$NON-NLS-1$
					);
			
			if (!shouldUpdate) 
				fIsInfoReadOnly = true;
									
			if(version.isEquivalentTo(new PluginVersionIdentifier(1,2,0))){
				UpdateManagedProject12.doProjectUpdate(monitor, fProject);
				version = getManagedBuildInfoVersion(info.getVersion());
			}
			if(version.isEquivalentTo(new PluginVersionIdentifier(2,0,0))){
				UpdateManagedProject20.doProjectUpdate(monitor, fProject);
				version = getManagedBuildInfoVersion(info.getVersion());
			}
			if(version.isEquivalentTo(new PluginVersionIdentifier(2,1,0))){
				UpdateManagedProject21.doProjectUpdate(monitor, fProject);
				version = getManagedBuildInfoVersion(info.getVersion());
			}
			if(version.isEquivalentTo(new PluginVersionIdentifier(3,0,0))){
				UpdateManagedProject30.doProjectUpdate(monitor, fProject);
				version = getManagedBuildInfoVersion(info.getVersion());
			}
	
			if(!isCompatibleProject(info)){
				throw new CoreException(new Status(IStatus.ERROR, ManagedBuilderCorePlugin.getUniqueIdentifier(), -1,
						ConverterMessages.getFormattedString("UpdateManagedProjectManager.5",  //$NON-NLS-1$
								new String [] {
									ManagedBuildManager.getBuildInfoVersion().toString(),
									version.toString(),
									info.getManagedProject().getId()
								}
							),null));
			}
			
		} catch (CoreException e) {
			fIsInfoReadOnly = true;
			throw e; 
		} finally{
			if(fIsInfoReadOnly){
				restoreFile(backupFile.getName(), settingsFile.getName(), monitor, fProject);
				info.setReadOnly(true);
			}
		}
	}

	/**
	 * updates the managed project
	 * 
	 * @param project the project to be updated
	 * @param info the ManagedBuildInfo for the current project
	 * @throws CoreException if conversion failed
	 */
	static public void updateProject(final IProject project, ManagedBuildInfo info) 
						throws CoreException{
		try {
			getUpdateManager(project).doProjectUpdate(info);
		} finally {
			removeUpdateManager(project);
			// We have to this here since we use java.io.File to handle the update.
			IWorkspace workspace = project.getWorkspace();
			ISchedulingRule rule = workspace.getRuleFactory().refreshRule(project);
			WorkspaceJob job = new WorkspaceJob("Refresh Project") { //$NON-NLS-1$
				public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
					project.refreshLocal(IResource.DEPTH_ONE, null);
					return Status.OK_STATUS;
				}
			};
			job.setRule(rule);
			job.schedule();
		}
	}
}
