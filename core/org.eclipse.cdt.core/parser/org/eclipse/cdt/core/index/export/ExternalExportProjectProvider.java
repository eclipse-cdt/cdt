/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.core.index.export;

import java.io.File;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.index.ResourceContainerRelativeLocationConverter;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * An IExportProjectProvider suitable for indexing an external folder. The arguments understood by this provider
 * are
 * <ul>
 * <li>-source what will become the root of the indexed content
 * <li>-include any preinclude files to configure the parser with
 * <li>-id the id to write to the produce fragment
 * </ul>
 */
public class ExternalExportProjectProvider extends AbstractExportProjectProvider implements IExportProjectProvider {
	private static final String ORG_ECLIPSE_CDT_CORE_INDEX_EXPORT_DATESTAMP = "org.eclipse.cdt.core.index.export.datestamp"; //$NON-NLS-1$
	private static final String CONTENT = "content"; //$NON-NLS-1$
	protected static final String ARG_SOURCE = "-source"; //$NON-NLS-1$
	protected static final String ARG_INCLUDE = "-include"; //$NON-NLS-1$
	protected static final String ARG_FRAGMENT_ID = "-id"; //$NON-NLS-1$
	
	private IFolder content;
	private String fragmentId;
	
	public ExternalExportProjectProvider() {
		super();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.core.index.export.IProjectForExportManager#createProject(java.util.Map)
	 */
	public ICProject createProject() throws CoreException {
			// -source
			File source= new File(getSingleString(ARG_SOURCE));
			if(!source.exists()) {
				fail(MessageFormat.format(Messages.ExternalContentPEM_LocationToIndexNonExistent, new Object[] {source}));
			}
			
			// -include
			List includeFiles= new ArrayList();
			if(isPresent(ARG_INCLUDE)) {
				includeFiles.addAll(getParameters(ARG_INCLUDE));				
			}
			
			// -id
			fragmentId= getSingleString(ARG_FRAGMENT_ID);
			
			return createCProject("__"+System.currentTimeMillis(), source, IPDOMManager.ID_FAST_INDEXER, includeFiles); //$NON-NLS-1$			
	}
	
	/**
	 * Returns the project folder the external content is stored in
	 * @return the project folder the external content is stored in
	 */
	protected IFolder getContentFolder() {
		return content;
	}
	
	/**
	 * Convenience method for creating a cproject
	 * @param projectName the name for the new project
	 * @param location the absolute path of some external content
	 * @param indexerID the indexer to use
	 * @param includeFiles a list of include paths to add to the project scanner
	 * @return a new project
	 * @throws CoreException
	 */
	private ICProject createCProject(
			final String projectName,
			final File location,
			final String indexerID,
			final List includeFiles
			) throws CoreException {
		final IWorkspace ws = ResourcesPlugin.getWorkspace();
		final ICProject newProject[] = new ICProject[1];
		
		ws.run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				IWorkspaceRoot root = ws.getRoot();
				
				IProject project = root.getProject(projectName);
				if (!project.exists()) {
					project.create(NPM);
				} else {
					project.refreshLocal(IResource.DEPTH_INFINITE, NPM);
				}
				if (!project.isOpen()) {
					project.open(NPM);
				}
				if (!project.hasNature(CProjectNature.C_NATURE_ID)) {
					addNatureToProject(project, CProjectNature.C_NATURE_ID, NPM);
				}
				if (!project.hasNature(CCProjectNature.CC_NATURE_ID)) {
					addNatureToProject(project, CCProjectNature.CC_NATURE_ID, NPM);
				}
				
				ICProject cproject = CCorePlugin.getDefault().getCoreModel().create(project);
				
				// External content appears under a linked folder
				content= cproject.getProject().getFolder(CONTENT);
				content.createLink(new Path(location.getAbsolutePath()), IResource.NONE, null);
				
				// Setup path entries
				List entries= new ArrayList(Arrays.asList(CoreModel.getRawPathEntries(cproject)));
				for(Iterator j= includeFiles.iterator(); j.hasNext(); ) {
					entries.add(
							CoreModel.newIncludeFileEntry(
									cproject.getProject().getFullPath(),
									new Path((String) j.next())
					));
				}
				entries.add(CoreModel.newSourceEntry(content.getProjectRelativePath()));
				cproject.setRawPathEntries(
					(IPathEntry[]) entries.toArray(new IPathEntry[includeFiles.size()]),
					new NullProgressMonitor()
				);
				
				newProject[0]= cproject;
			}
		}, null);

		if (indexerID != null) {
			IndexerPreferences.set(newProject[0].getProject(), IndexerPreferences.KEY_INDEX_ALL_FILES, Boolean.TRUE.toString());
			IndexerPreferences.set(newProject[0].getProject(), IndexerPreferences.KEY_INDEXER_ID, indexerID);
		}
		
		return newProject[0];
	}
	
	/*
	 * This should be a platform/CDT API
	 */
	private static void addNatureToProject(IProject proj, String natureId, IProgressMonitor monitor) throws CoreException {
		IProjectDescription description = proj.getDescription();
		String[] prevNatures = description.getNatureIds();
		String[] newNatures = new String[prevNatures.length + 1];
		System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
		newNatures[prevNatures.length] = natureId;
		description.setNatureIds(newNatures);
		proj.setDescription(description, monitor);
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.core.index.export.IExportProjectProvider#getLocationConverter(org.eclipse.cdt.core.model.ICProject)
	 */
	public IIndexLocationConverter getLocationConverter(final ICProject cproject) {
		return new ResourceContainerRelativeLocationConverter(content);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.core.index.export.IExportProjectProvider#getExportProperties()
	 */
	public Map getExportProperties() {
		Map properties= new HashMap();
		Date now= Calendar.getInstance().getTime();
		properties.put(ORG_ECLIPSE_CDT_CORE_INDEX_EXPORT_DATESTAMP,
				DateFormat.getDateInstance().format(now)
				+" "+DateFormat.getTimeInstance().format(now)); //$NON-NLS-1$
		properties.put(IIndexFragment.PROPERTY_FRAGMENT_ID, fragmentId);
		return properties;
	}
}
