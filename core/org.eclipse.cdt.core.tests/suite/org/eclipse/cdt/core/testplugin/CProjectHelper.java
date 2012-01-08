/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Andrew Ferguson (Symbian)
 *     Markus Schorn (Wind River Systems)
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.testplugin;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.zip.ZipFile;

import junit.framework.Assert;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IArchive;
import org.eclipse.cdt.core.model.IArchiveContainer;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.IBinaryContainer;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.settings.model.ICConfigExtensionReference;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.TestCfgDataProvider;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.eclipse.ui.wizards.datatransfer.ZipFileStructureProvider;
import org.osgi.framework.Bundle;
/**
 * Helper methods to set up a ICProject.
 */
public class CProjectHelper {

	private final static IOverwriteQuery OVERWRITE_QUERY= new IOverwriteQuery() {
		@Override
		public String queryOverwrite(String file) {
			return ALL;
		}
	};

	public static ICProject createCProject(final String projectName, String binFolderName) throws CoreException {
		return createCCProject(projectName, binFolderName, null);
	}
	
	/**
	 * Creates a ICProject.
	 */
	public static ICProject createCProject(final String projectName, String binFolderName, final String indexerID) throws CoreException {
		final IWorkspace ws = ResourcesPlugin.getWorkspace();
		final ICProject newProject[] = new ICProject[1];
		ws.run(new IWorkspaceRunnable() {

			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				IWorkspaceRoot root = ws.getRoot();
				IProject project = root.getProject(projectName);
				if (indexerID != null) {
					IndexerPreferences.set(project, IndexerPreferences.KEY_INDEX_UNUSED_HEADERS_WITH_DEFAULT_LANG, "true");
					IndexerPreferences.set(project, IndexerPreferences.KEY_INDEXER_ID, indexerID);
				}
				if (!project.exists()) {
					project.create(null);
				} else {
					project.refreshLocal(IResource.DEPTH_INFINITE, null);
				}
				if (!project.isOpen()) {
					project.open(null);
				}
				if (!project.hasNature(CProjectNature.C_NATURE_ID)) {
					String projectId = CTestPlugin.PLUGIN_ID + ".TestProject";
					addNatureToProject(project, CProjectNature.C_NATURE_ID, null);
					CCorePlugin.getDefault().mapCProjectOwner(project, projectId, false);
				}
				addDefaultBinaryParser(project);
				newProject[0] = CCorePlugin.getDefault().getCoreModel().create(project);
			}
		}, null);

		return newProject[0];
	}

	/**
	 * Add the default binary parser if no binary parser configured.
	 * 
	 * @param project
	 * @throws CoreException 
	 */
	public static boolean addDefaultBinaryParser(IProject project) throws CoreException {
		ICConfigExtensionReference[] binaryParsers= CCorePlugin.getDefault().getDefaultBinaryParserExtensions(project);
		if (binaryParsers == null || binaryParsers.length == 0) {
			ICProjectDescription desc= CCorePlugin.getDefault().getProjectDescription(project);
			if (desc == null) {
				return false;
			}
			
			desc.getDefaultSettingConfiguration().create(CCorePlugin.BINARY_PARSER_UNIQ_ID, CCorePlugin.DEFAULT_BINARY_PARSER_UNIQ_ID);
			CCorePlugin.getDefault().setProjectDescription(project, desc);
		}
		return true;
	}

	/**
	 * Creates a ICProject.
	 */
	public static ICProject createNewStileCProject(final String projectName, final String indexerID) throws CoreException {
		return createNewStileCProject(projectName, indexerID, false);
	}

	/**
	 * Creates a ICProject.
	 */
	public static ICProject createNewStileCProject(final String projectName, String providerId, final String indexerID) throws CoreException {
		return createNewStileCProject(projectName, providerId, indexerID, false);
	}

	/**
	 * Creates a ICProject.
	 */
	public static ICProject createNewStileCProject(final String projectName, final String indexerID, boolean markCreating) throws CoreException {
		return createNewStileCProject(projectName, null, indexerID, markCreating);
	}

	/**
	 * Creates a ICProject.
	 */
	public static ICProject createNewStileCProject(final String projectName, String cfgProviderId, final String indexerID, final boolean markCreating) throws CoreException {
		final IWorkspace ws = ResourcesPlugin.getWorkspace();
		final ICProject newProject[] = new ICProject[1];
		if(cfgProviderId == null)
			cfgProviderId = TestCfgDataProvider.PROVIDER_ID;
		
		final String finalCfgProviderId = cfgProviderId;
		ws.run(new IWorkspaceRunnable() {

			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				IWorkspaceRoot root = ws.getRoot();
				IProject project = root.getProject(projectName);
				if (indexerID != null) {
					IndexerPreferences.set(project, IndexerPreferences.KEY_INDEXER_ID, indexerID);
				}
				if (!project.exists()) {
					project.create(null);
				} else {
					project.refreshLocal(IResource.DEPTH_INFINITE, null);
				}
				if (!project.isOpen()) {
					project.open(null);
				}
				if (!project.hasNature(CProjectNature.C_NATURE_ID)) {
					addNatureToProject(project, CProjectNature.C_NATURE_ID, null);
					ICConfigurationDescription prefCfg = CCorePlugin.getDefault().getPreferenceConfiguration(finalCfgProviderId);
					ICProjectDescriptionManager mngr = CCorePlugin.getDefault().getProjectDescriptionManager();
					ICProjectDescription projDes = mngr.createProjectDescription(project, false, markCreating);
					projDes.createConfiguration(CDataUtil.genId(null), CDataUtil.genId("test"), prefCfg);
					mngr.setProjectDescription(project, projDes);
//					CCorePlugin.getDefault().mapCProjectOwner(project, projectId, false);
				}
				addDefaultBinaryParser(project);
				newProject[0] = CCorePlugin.getDefault().getCoreModel().create(project);
			}
		}, null);

		return newProject[0];
	}

	
	private static String getMessage(IStatus status) {
		StringBuffer message = new StringBuffer("[");
		message.append(status.getMessage());
		if (status.isMultiStatus()) {
			IStatus children[] = status.getChildren();
			for( int i = 0; i < children.length; i++) {
				message.append(getMessage(children[i]));
			}
		}
		message.append("]");
		return message.toString();
	}

	public static ICProject createCCProject(final String projectName, final String binFolderName) throws CoreException {
		return createCCProject(projectName, binFolderName, null);
	}

	public static ICProject createCCProject(final String projectName, final String binFolderName, final String indexerID) throws CoreException {
		final IWorkspace ws = ResourcesPlugin.getWorkspace();
		final ICProject newProject[] = new ICProject[1];
		ws.run(new IWorkspaceRunnable() {

			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				ICProject cproject = createCProject(projectName, binFolderName, indexerID);
				if (!cproject.getProject().hasNature(CCProjectNature.CC_NATURE_ID)) {
					addNatureToProject(cproject.getProject(), CCProjectNature.CC_NATURE_ID, null);
				}
				newProject[0] = cproject;
			}
		}, null);
		return newProject[0];
	}

	/**
	 * Removes a ICProject.
	 */
	public static void delete(ICProject cproject) {
		try {
			cproject.getProject().delete(true, true, null);
		} catch (CoreException e) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
			} finally {
				try {
					System.gc();
					System.runFinalization();
					cproject.getProject().delete(true, true, null);
				} catch (CoreException e2) {
					Assert.fail(getMessage(e2.getStatus()));
				}
			}
		}
	}

	/**
	 * Adds a folder container to a ICProject.
	 */
	public static ICContainer addCContainer(ICProject cproject, String containerName) throws CoreException {
		IProject project = cproject.getProject();
		ICContainer container = null;
		if (containerName == null || containerName.length() == 0) {
			ICContainer[] conts = cproject.getSourceRoots();
			if (conts.length > 0) {
				container = conts[0];
			}
		} else {
			IFolder folder = project.getFolder(containerName);
			if (!folder.exists()) {
				folder.create(false, true, null);
			}
			container = CoreModel.getDefault().create(folder);
		}
		return container;
	}

	/**
	 * Adds a folder container to a ICProject and imports all files contained in the given Zip file.
	 */
	public static ICContainer addCContainerWithImport(ICProject cproject, String containerName, ZipFile zipFile)
			throws InvocationTargetException, CoreException {
		ICContainer root = addCContainer(cproject, containerName);
		importFilesFromZip(zipFile, root.getPath(), null);
		return root;
	}

	/**
	 * Removes a folder from a ICProject.
	 */
	public static void removeCContainer(ICProject cproject, String containerName) throws CoreException {
		IFolder folder = cproject.getProject().getFolder(containerName);
		folder.delete(true, null);
	}

	/**
	 * Attempts to find an archive with the given name in the workspace
	 */
	public static IArchive findArchive(ICProject testProject, String name) throws CModelException {
		int x;
		IArchive[] myArchives;
		IArchiveContainer archCont;
		/***************************************************************************************************************************
		 * Since ArchiveContainer.getArchives does not wait until all the archives in the project have been parsed before returning
		 * the list, we have to do a sync ArchiveContainer.getChildren first to make sure we find all the archives.
		 */
		archCont = testProject.getArchiveContainer();
		myArchives = archCont.getArchives();
		if (myArchives.length < 1)
			return (null);
		for (x = 0; x < myArchives.length; x++) {
			if (myArchives[x].getElementName().equals(name))
				return (myArchives[x]);
		}
		return (null);
	}

	/**
	 * Attempts to find a binary with the given name in the workspace
	 */
	public static IBinary findBinary(ICProject testProject, String name) throws CModelException {
		IBinaryContainer binCont;
		int x;
		IBinary[] myBinaries;
		binCont = testProject.getBinaryContainer();
		myBinaries = binCont.getBinaries();
		if (myBinaries.length < 1)
			return (null);
		for (x = 0; x < myBinaries.length; x++) {
			if (myBinaries[x].getElementName().equals(name))
				return (myBinaries[x]);
		}
		return (null);
	}

	/**
	 * Attempts to find an object with the given name in the workspace
	 */
	public static IBinary findObject(ICProject testProject, String name) throws CModelException {
		ICElement[] sourceRoots = testProject.getChildren();
		for (int i = 0; i < sourceRoots.length; i++) {
			ISourceRoot root = (ISourceRoot) sourceRoots[i];
			ICElement[] myElements = root.getChildren();
			for (int x = 0; x < myElements.length; x++) {
				if (myElements[x].getElementName().equals(name)) {
					if (myElements[x] instanceof IBinary) {
						return ((IBinary) myElements[x]);
					}
				}
			}
		}
		return null;
	}

	/**
	 * Attempts to find a TranslationUnit with the given name in the workspace
	 * @throws InterruptedException 
	 */
	public static ITranslationUnit findTranslationUnit(ICProject testProject, String name) throws CModelException, InterruptedException {
		for (int j=0; j<20; j++) {
			ICElement[] sourceRoots = testProject.getChildren();
			for (int i = 0; i < sourceRoots.length; i++) {
				ISourceRoot root = (ISourceRoot) sourceRoots[i];
				ICElement[] myElements = root.getChildren();
				for (int x = 0; x < myElements.length; x++) {
					if (myElements[x].getElementName().equals(name)) {
						if (myElements[x] instanceof ITranslationUnit) {
							return ((ITranslationUnit) myElements[x]);
						}
					}
				}
			}
			Thread.sleep(100);
		}
		return null;
	}

	/**
	 * Attempts to find an element with the given name in the workspace
	 */
	public static ICElement findElement(ICProject testProject, String name) throws CModelException {
		ICElement[] sourceRoots = testProject.getChildren();
		for (int i = 0; i < sourceRoots.length; i++) {
			ISourceRoot root = (ISourceRoot) sourceRoots[i];
			ICElement[] myElements = root.getChildren();
			for (int x = 0; x < myElements.length; x++) {
				if (myElements[x].getElementName().equals(name)) {
					return myElements[x];
				}
			}
		}
		return null;
	}

	public static void addNatureToProject(IProject proj, String natureId, IProgressMonitor monitor) throws CoreException {
		IProjectDescription description = proj.getDescription();
		String[] prevNatures = description.getNatureIds();
		String[] newNatures = new String[prevNatures.length + 1];
		System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
		newNatures[prevNatures.length] = natureId;
		description.setNatureIds(newNatures);
		proj.setDescription(description, monitor);
	}

	private static void importFilesFromZip(ZipFile srcZipFile, IPath destPath, IProgressMonitor monitor)
			throws InvocationTargetException {
		ZipFileStructureProvider structureProvider = new ZipFileStructureProvider(srcZipFile);
		try {
			ImportOperation op = new ImportOperation(destPath, structureProvider.getRoot(), structureProvider,
					OVERWRITE_QUERY);
			op.run(monitor);
		} catch (InterruptedException e) {
			// should not happen
		}
	}

	
	public static void importSourcesFromPlugin(ICProject project, Bundle bundle, String sources) throws CoreException {
		try {
			String baseDir= FileLocator.toFileURL(FileLocator.find(bundle, new Path(sources), null)).getFile();
			ImportOperation importOp = new ImportOperation(project.getProject().getFullPath(),
					new File(baseDir), FileSystemStructureProvider.INSTANCE, OVERWRITE_QUERY);
			importOp.setCreateContainerStructure(false);
			importOp.run(new NullProgressMonitor());
		}
		catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, CTestPlugin.PLUGIN_ID, 0, "Import Interrupted", e));
		}
	}

	/**
	 * @return the location of a newly created directory in temporary area.
	 *    Note that cleanup should be done with {@link ResourceHelper#cleanUp()}.
	 * @throws IOException
	 * @throws CoreException 
	 */
	public static File freshDir() throws IOException, CoreException {
		IPath folderPath = ResourceHelper.createTemporaryFolder();
		File folder = new File(folderPath.toOSString());
		Assert.assertTrue(folder.exists());
		Assert.assertTrue(folder.isDirectory());
		Assert.assertTrue(folder.canWrite());
		
		return folder;
	}
}