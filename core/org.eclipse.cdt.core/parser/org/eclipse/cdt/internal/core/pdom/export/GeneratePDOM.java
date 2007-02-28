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
package org.eclipse.cdt.internal.core.pdom.export;

import java.io.File;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.index.export.IExportProjectProvider;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.pdom.WritablePDOM;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * An ISafeRunnable which
 * <ul>
 * <li>Creates a project for export
 * <li>Exports the pdom
 * <li>Writes new properties to the pdom
 * <ul>
 */
public class GeneratePDOM implements ISafeRunnable {
	protected IExportProjectProvider pm;
	protected String[] applicationArguments;
	protected File targetLocation;

	public GeneratePDOM(IExportProjectProvider pm, String[] applicationArguments, File targetLocation) {
		this.pm = pm;
		this.applicationArguments= applicationArguments;
		this.targetLocation = targetLocation;
	}

	public final void run() throws CoreException {
		pm.setApplicationArguments(applicationArguments);
		final ICProject cproject = pm.createProject();
		if(cproject==null) {
			throw new CoreException(CCorePlugin.createStatus(Messages.GeneratePDOM_ProjectProviderReturnedNullCProject));
		}
		CCorePlugin.getIndexManager().joinIndexer(Integer.MAX_VALUE, new NullProgressMonitor());
		IIndexLocationConverter converter= pm.getLocationConverter(cproject);
		try {
			CCoreInternals.getPDOMManager().exportProjectPDOM(cproject, targetLocation, converter);
			WritablePDOM exportedPDOM= new WritablePDOM(targetLocation, converter);

			exportedPDOM.acquireWriteLock(0);
			try {
				Map exportProperties= pm.getExportProperties();
				if(exportProperties!=null) {
					for(Iterator i = exportProperties.entrySet().iterator(); i.hasNext(); ) {
						Map.Entry entry = (Map.Entry) i.next();
						exportedPDOM.setProperty((String) entry.getKey(), (String) entry.getValue());
					}
				}
			} finally {
				exportedPDOM.releaseWriteLock(0);
			}
		} catch(InterruptedException ie) {
			String msg= MessageFormat.format(Messages.GeneratePDOM_GenericGenerationFailed, new Object[] {ie.getMessage()});
			throw new CoreException(CCorePlugin.createStatus(msg, ie));
		}
	}

	public void handleException(Throwable exception) {
		// subclass for custom behaviour
		CCorePlugin.log(exception);
	}
}
