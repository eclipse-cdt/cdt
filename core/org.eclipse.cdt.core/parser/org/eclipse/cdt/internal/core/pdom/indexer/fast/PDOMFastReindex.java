/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.indexer.fast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMFastReindex extends Job {

	private final PDOM pdom;
	
	public PDOMFastReindex(PDOM pdom) {
		super("Reindex");
		this.pdom = pdom;
	}
	
	protected IStatus run(IProgressMonitor monitor) {
		try {
			final List addedTUs = new ArrayList();
			
			// First clear out the DB
			pdom.delete();
			
			// Now repopulate it
			pdom.getProject().getProject().accept(new IResourceProxyVisitor() {
				public boolean visit(IResourceProxy proxy) throws CoreException {
					if (proxy.getType() == IResource.FILE) {
						String fileName = proxy.getName();
						IContentType contentType = Platform.getContentTypeManager().findContentTypeFor(fileName);
						if (contentType == null)
							return true;
						String contentTypeId = contentType.getId();
						
						if (CCorePlugin.CONTENT_TYPE_CXXSOURCE.equals(contentTypeId)
								|| CCorePlugin.CONTENT_TYPE_CSOURCE.equals(contentTypeId)) {
							// TODO handle header files
							addedTUs.add((ITranslationUnit)CoreModel.getDefault().create((IFile)proxy.requestResource()));
						}
						return false;
					} else {
						return true;
					}
				}
			}, 0);
			
			for (Iterator i = addedTUs.iterator(); i.hasNext();)
				addTU((ITranslationUnit)i.next());
			
			monitor.done();
			return Status.OK_STATUS;
		} catch (CoreException e) {
			return e.getStatus();
		} catch (InterruptedException e) {
			return Status.CANCEL_STATUS;
		}
	}

	protected void addTU(ITranslationUnit tu) throws InterruptedException, CoreException {
		ILanguage language = tu.getLanguage();
		if (language == null)
			return;

		// get the AST in a "Fast" way
		IASTTranslationUnit ast = language.getASTTranslationUnit(tu,
				ILanguage.AST_USE_INDEX
						| ILanguage.AST_SKIP_INDEXED_HEADERS
						| ILanguage.AST_SKIP_IF_NO_BUILD_INFO);
		if (ast == null)
			return;

		pdom.acquireWriteLock();
		try {
			pdom.addSymbols(language, ast);
		} finally {
			pdom.releaseWriteLock();
		}
	}

}
