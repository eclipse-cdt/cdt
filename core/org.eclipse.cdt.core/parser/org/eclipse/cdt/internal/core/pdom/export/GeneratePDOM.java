/*******************************************************************************
 * Copyright (c) 2007, 2014 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *     Markus Schorn (Wind River Systems)
 *     Martin Oberhuber (Wind River) - [397652] fix up-to-date check for PDOM
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.export;

import java.io.File;
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
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;

import com.ibm.icu.text.MessageFormat;

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
	protected boolean checkIndexStatus;

	/**
	 * Runnable to export a PDOM.
	 * @param checkIndexStatus <code>true</code> to check index completeness before exporting, or
	 *     <code>false</code> to export the index without checking anything
	 * @since 5.5
	 */
	public GeneratePDOM(IExportProjectProvider pm, String[] applicationArguments, File targetLocation, String indexerID,
			boolean checkIndexStatus) {
		this.pm = pm;
		this.applicationArguments = applicationArguments;
		this.targetLocation = targetLocation;
		this.indexerID = indexerID;
		this.checkIndexStatus = checkIndexStatus;
	}

	public GeneratePDOM(IExportProjectProvider pm, String[] applicationArguments, File targetLocation,
			String indexerID) {
		this(pm, applicationArguments, targetLocation, indexerID, true);
	}

	/**
	 * When set, the project created by the associated {@link IExportProjectProvider} will
	 * be deleted after {@link #run()} completes. By default this is not set.
	 * @param deleteOnExit
	 */
	public void setDeleteOnExit(boolean deleteOnExit) {
		this.deleteOnExit = deleteOnExit;
	}

	/**
	 * Executes the PDOM generation
	 * @return {@link IStatus#OK} if the generated content is complete, {@link IStatus#ERROR} otherwise.
	 * @throws CoreException if an internal or invalid configuration error occurs
	 */
	public final IStatus run() throws CoreException {
		// Create the project
		pm.setApplicationArguments(applicationArguments);
		final ICProject cproject = pm.createProject();
		if (cproject == null) {
			fail(MessageFormat.format(Messages.GeneratePDOM_ProjectProviderReturnedNullCProject,
					new Object[] { pm.getClass().getName() }));
			return null; // Cannot be reached, inform the compiler
		}

		IIndexLocationConverter converter = pm.getLocationConverter(cproject);
		if (converter == null) {
			fail(MessageFormat.format(Messages.GeneratePDOM_NullLocationConverter,
					new Object[] { pm.getClass().getName() }));
		}

		// Index the project
		IndexerPreferences.set(cproject.getProject(), IndexerPreferences.KEY_INDEXER_ID, indexerID);

		try {
			final IIndexManager manager = CCorePlugin.getIndexManager();
			for (int i = 0; i < 20; i++) {
				if (CCoreInternals.getPDOMManager().isProjectRegistered(cproject)) {
					manager.joinIndexer(Integer.MAX_VALUE, new NullProgressMonitor());
					if (!manager.isIndexerSetupPostponed(cproject)) {
						break;
					}
				}
				Thread.sleep(200);
			}

			if (checkIndexStatus) {
				// Check status
				IStatus syncStatus = CCoreInternals.getPDOMManager().getProjectContentSyncState(cproject);
				if (syncStatus != null) {
					// Add message and error severity
					IStatus myStatus = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID,
							Messages.GeneratePDOM_Incomplete);
					MultiStatus m = new MultiStatus(CCorePlugin.PLUGIN_ID, 1, new IStatus[] { myStatus, syncStatus },
							Messages.GeneratePDOM_Incomplete, null);
					// Log the status right away since legacy clients did not return any status details
					CCorePlugin.log(m);
					return m;
				}
			}
			// Export a .pdom file
			CCoreInternals.getPDOMManager().exportProjectPDOM(cproject, targetLocation, converter, null);

			// Write properties to exported PDOM
			WritablePDOM exportedPDOM = new WritablePDOM(targetLocation, converter,
					LanguageManager.getInstance().getPDOMLinkageFactoryMappings());
			exportedPDOM.acquireWriteLock(0, null);
			try {
				Map<String, String> exportProperties = pm.getExportProperties();
				if (exportProperties != null) {
					for (Map.Entry<String, String> entry : exportProperties.entrySet()) {
						exportedPDOM.setProperty(entry.getKey(), entry.getValue());
					}
				}
				exportedPDOM.close();
			} finally {
				exportedPDOM.releaseWriteLock();
			}
		} catch (InterruptedException ie) {
			String msg = MessageFormat.format(Messages.GeneratePDOM_GenericGenerationFailed,
					new Object[] { ie.getMessage() });
			throw new CoreException(CCorePlugin.createStatus(msg, ie));
		} finally {
			if (deleteOnExit) {
				cproject.getProject().delete(true, new NullProgressMonitor());
			}
		}

		return new Status(IStatus.OK, CCorePlugin.PLUGIN_ID, Messages.GeneratePDOM_Success);
	}

	private void fail(String message) throws CoreException {
		GeneratePDOMApplication.fail(message);
	}
}
