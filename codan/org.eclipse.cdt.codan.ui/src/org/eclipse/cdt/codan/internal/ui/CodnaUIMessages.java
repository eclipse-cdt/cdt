/*******************************************************************************
 * Copyright (c) 2009,2010 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui;

import org.eclipse.osgi.util.NLS;

/**
 * UI Messages
 */
public class CodnaUIMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.codan.internal.ui"; //$NON-NLS-1$
	public static String CustomizeProblemComposite_TabParameters;
	public static String CustomizeProblemComposite_TabScope;
	public static String CustomizeProblemDialog_Message;
	public static String CustomizeProblemDialog_Title;
	public static String Job_TitleRunningAnalysis;
	public static String ParametersComposite_None;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, CodnaUIMessages.class);
	}

	private CodnaUIMessages() {
	}
}
