/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.opentype;

import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.internal.core.search.matching.OrPattern;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.operation.IRunnableWithProgress;

public class TypeSearchOperation implements IRunnableWithProgress {

	private final TypeSearchResultCollector collector = new TypeSearchResultCollector();

	private ICSearchScope scope;
	private IWorkspace workspace;
	private OrPattern pattern;
	private SearchEngine engine;

	public TypeSearchOperation(IWorkspace workspace, ICSearchScope scope, SearchEngine engine) {
		this.workspace = workspace;
		this.scope = scope;
		this.engine = engine;

		// search for namespaces, classes, structs, unions, enums and typedefs
		pattern = new OrPattern();
		pattern.addPattern(SearchEngine.createSearchPattern("*", ICSearchConstants.TYPE, ICSearchConstants.DECLARATIONS, false));
		pattern.addPattern(
			SearchEngine.createSearchPattern("*", ICSearchConstants.NAMESPACE, ICSearchConstants.DECLARATIONS, false));
		pattern.addPattern(SearchEngine.createSearchPattern("*", ICSearchConstants.CLASS, ICSearchConstants.DECLARATIONS, false));
		pattern.addPattern(SearchEngine.createSearchPattern("*", ICSearchConstants.STRUCT, ICSearchConstants.DECLARATIONS, false));
		pattern.addPattern(
			SearchEngine.createSearchPattern("*", ICSearchConstants.CLASS_STRUCT, ICSearchConstants.DECLARATIONS, false));
		pattern.addPattern(SearchEngine.createSearchPattern("*", ICSearchConstants.UNION, ICSearchConstants.DECLARATIONS, false));
		pattern.addPattern(SearchEngine.createSearchPattern("*", ICSearchConstants.ENUM, ICSearchConstants.DECLARATIONS, false));
		pattern.addPattern(SearchEngine.createSearchPattern("*", ICSearchConstants.TYPEDEF, ICSearchConstants.DECLARATIONS, false));
	}

	public void run(IProgressMonitor monitor) throws InterruptedException {
		collector.setProgressMonitor(monitor);
		try {
			engine.search(workspace, pattern, scope, collector, true);
		} catch (OperationCanceledException e) {
			throw new InterruptedException();
		}
		if (monitor.isCanceled()) {
			throw new InterruptedException();
		}
	}

	public Object[] getResults() {
		return collector.getSearchResults().toArray();
	}
}
