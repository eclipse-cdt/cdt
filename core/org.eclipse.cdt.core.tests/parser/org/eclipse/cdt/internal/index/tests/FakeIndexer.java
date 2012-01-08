/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.index.tests;

import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.pdom.indexer.AbstractPDOMIndexer;

public class FakeIndexer extends AbstractPDOMIndexer {
	static final String ID = "org.eclipse.cdt.core.tests.FakeIndexer";

	@Override
	public IPDOMIndexerTask createTask(ITranslationUnit[] added,
			ITranslationUnit[] changed, ITranslationUnit[] removed) {
		return null;
	}

	@Override
	public String getID() {
		return ID;
	}

}
