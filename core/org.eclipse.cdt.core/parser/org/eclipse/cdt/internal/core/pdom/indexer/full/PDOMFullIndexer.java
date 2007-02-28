/*******************************************************************************
 * Copyright (c) 2006, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.indexer.full;

import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.pdom.indexer.AbstractPDOMIndexer;

/**
 * The Full indexer does full parsing in order to gather index information.
 * It has good accuracy but is relatively slow.
 * 
 * @author Doug Schaefer
 *
 */
public class PDOMFullIndexer extends AbstractPDOMIndexer {
	public static final String ID = IPDOMManager.ID_FULL_INDEXER;
	
	public String getID() {
		return ID;
	}
	
	public IPDOMIndexerTask createTask(ITranslationUnit[] added,
			ITranslationUnit[] changed, ITranslationUnit[] removed) {
		return new PDOMFullIndexerTask(this, added, changed, removed);
	}
}
