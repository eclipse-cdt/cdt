/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.indexer;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.core.pdom.indexer.messages"; //$NON-NLS-1$
	public static String PDOMImportTask_errorInvalidArchive;
	public static String PDOMImportTask_errorInvalidPDOMVersion;
	public static String PDOMIndexerTask_collectingFilesTask;
	public static String PDOMIndexerTask_indexerInfo;
	public static String TodoTaskUpdater_DeleteJob;
	public static String TodoTaskUpdater_taskFormat;
	public static String TodoTaskUpdater_UpdateJob;
	public static String PDOMImportTask_readingIndexJob_Name;
	public static String PDOMImportTask_readingChecksumsJob_Name;
	public static String PDOMImportTask_checkingFilesJob_Name;
	public static String PDOMImportTask_verifyingChecksumsJob_Name;
	public static String PDOMImportTask_updatingFileListJob_Name;
	public static String PDOMImportTask_importIndexJob_Name;
	public static String PDOMRebuildTask_0;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
