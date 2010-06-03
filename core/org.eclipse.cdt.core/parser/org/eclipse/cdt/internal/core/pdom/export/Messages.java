/*******************************************************************************
 * Copyright (c) 2007, 2008 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.export;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.core.pdom.export.messages"; //$NON-NLS-1$
	public static String GeneratePDOM_GenericGenerationFailed;
	public static String GeneratePDOM_Incomplete;
	public static String GeneratePDOM_NullLocationConverter;
	public static String GeneratePDOM_ProjectProviderReturnedNullCProject;
	public static String GeneratePDOM_Success;
	public static String GeneratePDOMApplication_CouldNotFindInitializer;
	public static String GeneratePDOMApplication_GenerationEnds;
	public static String GeneratePDOMApplication_GenerationStarts;
	public static String GeneratePDOMApplication_Initializing;
	public static String GeneratePDOMApplication_InvalidIndexerID;
	public static String GeneratePDOMApplication_UsingDefaultProjectProvider;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
