/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core;

import org.eclipse.cdt.codan.core.model.ICheckersRegistry;
import org.eclipse.cdt.codan.core.model.ICodanAstReconciler;
import org.eclipse.cdt.codan.core.model.ICodanBuilder;
import org.eclipse.cdt.codan.core.model.IProblemReporter;
import org.eclipse.cdt.codan.internal.core.CheckersRegisry;
import org.eclipse.cdt.codan.internal.core.CodanBuilder;
import org.eclipse.cdt.codan.internal.core.model.CodanMarkerProblemReporter;

/**
 * Runtime singleton class to get access to Codan framework parts
 * 
 */
public class CodanRuntime {
	private static CodanRuntime instance = new CodanRuntime();
	private IProblemReporter problemReporter = new CodanMarkerProblemReporter();
	private CodanBuilder builder = new CodanBuilder();
	private CheckersRegisry checkers = CheckersRegisry.getInstance();

	public IProblemReporter getProblemReporter() {
		return problemReporter;
	}

	public void setProblemReporter(IProblemReporter reporter) {
		problemReporter = reporter;
	}

	public static CodanRuntime getInstance() {
		return instance;
	}

	public ICodanBuilder getBuilder() {
		return builder;
	}

	public ICodanAstReconciler getAstQuickBuilder() {
		return builder;
	}

	public ICheckersRegistry getChechersRegistry() {
		return checkers;
	}
}
