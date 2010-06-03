/*******************************************************************************
 * Copyright (c) 2007, 2009 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.export;

import java.io.File;
import com.ibm.icu.text.MessageFormat;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.export.IExportProjectProvider;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.pdom.WritablePDOM;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;

/**
 * An ISafeRunnable which
 * <ul>
 * <li>Creates a project for export
 * <li>Exports the PDOM
 * <li>Writes new properties to the PDOM
 * <ul>
 */
public class GeneratePDOM {
	protected IExportProjectProvider pm;
	protected String[] applicationArguments;
	protected File targetLocation;
	protected String indexerID;
	protected boolean deleteOnExit;
	
	public GeneratePDOM(IExportProjectProvider pm, String[] applicationArguments, File targetLocation, String indexerID) {
		this.pm= pm;
		this.applicationArguments= applicationArguments;
		this.targetLocation= targetLocation;
		this.indexerID= indexerID;
	}
	
	/**
	 * When set, the project created by the associated {@link IExportProjectProvider} will
	 * be deleted after {@link #run()} completes. By default this is not set.
	 * @param deleteOnExit
	 */
	public void setDeleteOnExit(boolean deleteOnExit) {
		this.deleteOnExit= deleteOnExit;
	}

	/**
	 * Executes the PDOM generation 
	 * @return {@link IStatus#OK} if the generated content is complete, {@link IStatus#ERROR} otherwise.
	 * @throws CoreException if an internal or invalid configuration error occurs
	 */
	public final IStatus run() throws CoreException {
		boolean isContentSynced= false;
		
		// create the project
		pm.setApplicationArguments(applicationArguments);
		final ICProject cproject = pm.createProject();
		if(cproject==null) {
			fail(MessageFormat.format(Messages.GeneratePDOM_ProjectProviderReturnedNullCProject,
					new Object [] {pm.getClass().getName()}));
			return null; // cannot be reached, inform the compiler
		}
		
		IIndexLocationConverter converter= pm.getLocationConverter(cproject);
		if(converter==null) {
			fail(MessageFormat.format(Messages.GeneratePDOM_NullLocationConverter,
					new Object [] {pm.getClass().getName()}));
		}
		
		// index the project
		IndexerPreferences.set(cproject.getProject(), IndexerPreferences.KEY_INDEXER_ID, indexerID);
		
		try {
			final IIndexManager im = CCorePlugin.getIndexManager();
			for (int i = 0; i < 20; i++) {
				if(CCoreInternals.getPDOMManager().isProjectRegistered(cproject)) {
					im.joinIndexer(Integer.MAX_VALUE, new NullProgressMonitor());
					if (!im.isIndexerSetupPostponed(cproject)) {
						break;
					}
				}
				Thread.sleep(200);
			}
		
			// check status
			isContentSynced= CCoreInternals.getPDOMManager().isProjectContentSynced(cproject);
			
			if(isContentSynced) {
				// export a .pdom file
				CCoreInternals.getPDOMManager().exportProjectPDOM(cproject, targetLocation, converter);

				// write properties to exported PDOM
				WritablePDOM exportedPDOM= new WritablePDOM(targetLocation, converter, LanguageManager.getInstance().getPDOMLinkageFactoryMappings());
				exportedPDOM.acquireWriteLock(0);
				try {
					Map<String,String> exportProperties= pm.getExportProperties();
					if(exportProperties!=null) {
						for(Map.Entry<String,String> entry : exportProperties.entrySet()) {
							exportedPDOM.setProperty(entry.getKey(), entry.getValue());
						}
					}
					exportedPDOM.close();
				}
				finally {
					exportedPDOM.releaseWriteLock();
				}
			}
			
		} catch(InterruptedException ie) {
			String msg= MessageFormat.format(Messages.GeneratePDOM_GenericGenerationFailed, new Object[] {ie.getMessage()});
			throw new CoreException(CCorePlugin.createStatus(msg, ie));
		} finally {
			if(deleteOnExit) {
				cproject.getProject().delete(true, new NullProgressMonitor());
			}
		}
		
		return isContentSynced ?
			  new Status(IStatus.OK, CCorePlugin.PLUGIN_ID, Messages.GeneratePDOM_Success)
			: new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, Messages.GeneratePDOM_Incomplete);
	}
	
	private void fail(String message) throws CoreException {
		GeneratePDOMApplication.fail(message);
	}
}
