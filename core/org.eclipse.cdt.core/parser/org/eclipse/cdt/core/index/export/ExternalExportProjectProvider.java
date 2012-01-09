/*******************************************************************************
 * Copyright (c) 2007, 2009 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.core.index.export;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.MessageFormat;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.index.ResourceContainerRelativeLocationConverter;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.extension.impl.CDefaultConfigurationData;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
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
public class ExternalExportProjectProvider extends AbstractExportProjectProvider {
	private static final String PREBUILT_PROJECT_OWNER = "org.eclipse.cdt.core.index.export.prebuiltOwner"; //$NON-NLS-1$
	private static final String ORG_ECLIPSE_CDT_CORE_INDEX_EXPORT_DATESTAMP = "org.eclipse.cdt.core.index.export.datestamp"; //$NON-NLS-1$
	private static final String CONTENT = "content"; //$NON-NLS-1$
	public static final String OPT_SOURCE = "-source"; //$NON-NLS-1$
	public static final String OPT_INCLUDE = "-include"; //$NON-NLS-1$
	public static final String OPT_FRAGMENT_ID = "-id"; //$NON-NLS-1$

	private IFolder content;
	private String fragmentId;

	public ExternalExportProjectProvider() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.core.index.export.IProjectForExportManager#createProject(java.util.Map)
	 */
	@Override
	public ICProject createProject() throws CoreException {
		// -source
		File source= new File(getSingleString(OPT_SOURCE));
		if(!source.exists()) {
			fail(MessageFormat.format(Messages.ExternalContentPEM_LocationToIndexNonExistent, new Object[] {source}));
		}

		// -include
		List<String> includeFiles= new ArrayList<String>();
		if(isPresent(OPT_INCLUDE)) {
			includeFiles.addAll(getParameters(OPT_INCLUDE));				
		}

		// -id
		fragmentId= getSingleString(OPT_FRAGMENT_ID);

		return createCCProject("__" + System.currentTimeMillis(), source, includeFiles); //$NON-NLS-1$
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
	 * @param includeFiles a list of include paths to add to the project scanner
	 * @return a new project
	 * @throws CoreException
	 */
	private ICProject createCCProject(final String projectName, final File location,
			final List<String> includeFiles) throws CoreException {
		final IWorkspace ws = ResourcesPlugin.getWorkspace();
		final ICProject newProject[] = new ICProject[1];

		ws.run(new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				IWorkspace workspace= ResourcesPlugin.getWorkspace();
				IProject project= workspace.getRoot().getProject("__prebuilt_index_temp__" + System.currentTimeMillis()); //$NON-NLS-1$
				IProjectDescription description = workspace.newProjectDescription(project.getName());
				CCorePlugin.getDefault().createCProject(description, project, NPM, PREBUILT_PROJECT_OWNER);
				CCorePlugin.getDefault().convertProjectFromCtoCC(project, NPM);
				ICProjectDescription pd= CCorePlugin.getDefault().getProjectDescription(project, true); 
				newCfg(pd, project.getName(), "config"); //$NON-NLS-1$
								
				CoreModel.getDefault().setProjectDescription(project, pd, true, new NullProgressMonitor());
				
				ICProject cproject= CCorePlugin.getDefault().getCoreModel().create(project);
				
				// External content appears under a linked folder
				content= cproject.getProject().getFolder(CONTENT);
				content.createLink(new Path(location.getAbsolutePath()), IResource.NONE, null);

				// Setup path entries
				List<IPathEntry> entries= new ArrayList<IPathEntry>(Arrays.asList(CoreModel.getRawPathEntries(cproject)));

				// pre-include files
				for(String path : includeFiles) {
					entries.add(CoreModel.newIncludeFileEntry(project.getFullPath(), new Path(path)));
				}
				
				// content directory is a source root
				entries.add(CoreModel.newSourceEntry(content.getProjectRelativePath()));
				
				// any additional entries
				entries.addAll(getAdditionalRawEntries());
				
				cproject.setRawPathEntries(entries.toArray(new IPathEntry[entries.size()]),
						new NullProgressMonitor()
				);
			
				newProject[0]= cproject;
				
				IndexerPreferences.set(newProject[0].getProject(), IndexerPreferences.KEY_INDEXER_ID, IPDOMManager.ID_NO_INDEXER);
				IndexerPreferences.set(newProject[0].getProject(), IndexerPreferences.KEY_INDEX_ALL_FILES, Boolean.TRUE.toString());
				IndexerPreferences.set(newProject[0].getProject(), IndexerPreferences.KEY_INDEX_UNUSED_HEADERS_WITH_DEFAULT_LANG, Boolean.TRUE.toString());
			}
		}, null);

		return newProject[0];
	}
	
	/**
	 * Get additional raw entries (above those added as part of the ExternalExportProjectProvider functionality)
	 * @return a list of additional entries to add to the project
	 */
	protected List<IPathEntry> getAdditionalRawEntries() {
		List<IPathEntry> entries= new ArrayList<IPathEntry>();
		entries.add(CoreModel.newIncludeEntry(content.getProjectRelativePath(), null, content.getLocation(), true));
		return entries;
	}

	private ICConfigurationDescription newCfg(ICProjectDescription des, String project, String config) throws CoreException {
		CDefaultConfigurationData data= new CDefaultConfigurationData(project + "." + config, //$NON-NLS-1$
				project + " " + config + " name", null); //$NON-NLS-1$ //$NON-NLS-2$
		data.initEmptyData();
		return des.createConfiguration(CCorePlugin.DEFAULT_PROVIDER_ID, data);		
	}

	/*
	 * @see org.eclipse.cdt.core.index.export.IExportProjectProvider#getLocationConverter(org.eclipse.cdt.core.model.ICProject)
	 */
	@Override
	public IIndexLocationConverter getLocationConverter(final ICProject cproject) {
		return new ResourceContainerRelativeLocationConverter(content);
	}

	/*
	 * @see org.eclipse.cdt.core.index.export.IExportProjectProvider#getExportProperties()
	 */
	@Override
	public Map<String, String> getExportProperties() {
		Map<String, String> properties= new HashMap<String, String>();
		Date now= Calendar.getInstance().getTime();
		properties.put(ORG_ECLIPSE_CDT_CORE_INDEX_EXPORT_DATESTAMP,
				DateFormat.getDateInstance().format(now)
				+ " " + DateFormat.getTimeInstance().format(now)); //$NON-NLS-1$
		properties.put(IIndexFragment.PROPERTY_FRAGMENT_ID, fragmentId);
		return properties;
	}
}
