/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * Visitor to find macros in a BTree.
 * @since 4.0.2
 */
public final class MacroContainerPatternCollector implements IBTreeVisitor {
	private final PDOMLinkage fLinkage;
	
	private final List<PDOMMacroContainer> macros = new ArrayList<PDOMMacroContainer>();
	private final Pattern fPattern;
	private final IProgressMonitor fMonitor;
	private int fMonitorCheckCounter= 0;

		
	public MacroContainerPatternCollector(PDOMLinkage linkage, Pattern pattern, IProgressMonitor monitor) {
		fLinkage= linkage;
		fPattern= pattern;
		fMonitor= monitor;
	}

	
	@Override
	final public int compare(long record) throws CoreException {
		if (fMonitor != null)
			checkCancelled();
		return 0;
	}
	
	@Override
	final public boolean visit(long record) throws CoreException {
		if (record == 0)
			return true;

		String name= PDOMNamedNode.getDBName(fLinkage.getDB(), record).getString();
		if (fPattern.matcher(name).matches()) {
			macros.add(new PDOMMacroContainer(fLinkage, record));
		}
		return true; // look for more
	}
	
	final public PDOMMacroContainer[] getMacroContainers() {
		return macros.toArray(new PDOMMacroContainer[macros.size()]);
	}
	
	private void checkCancelled() {
		if (++fMonitorCheckCounter % 0x1000 == 0 && fMonitor.isCanceled()) {
			throw new OperationCanceledException();
		}
	}
}
