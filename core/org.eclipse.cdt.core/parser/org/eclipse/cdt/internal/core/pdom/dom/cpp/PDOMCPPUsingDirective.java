/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDirective;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Stores using directives for global or namespace scope. Directives for block-scopes 
 * are not persisted in the index.
 * For performance reasons the directives are not stored with their container. Rather
 * than that they are stored with the file, in which they are encountered. 
 * When parsing a file the directives from headers that are skipped are collected.
 */
public class PDOMCPPUsingDirective implements ICPPUsingDirective, IPDOMNode {
	private static final int CONTAINER_NAMESPACE 	= 0;
	private static final int NOMINATED_NAMESPACE    = 4;
	private static final int PREV_DIRECTIVE_OF_FILE	= 8;
	private static final int FILE_OFFSET	        = 12;
	private static final int RECORD_SIZE 			= 16;

	private final PDOMCPPLinkage fLinkage;
	private final long fRecord;

	PDOMCPPUsingDirective(PDOMCPPLinkage pdom, long record) {
		fLinkage= pdom;
		fRecord= record;
	}

	public PDOMCPPUsingDirective(PDOMCPPLinkage linkage, long prevRecInFile, PDOMCPPNamespace containerNS, 
			PDOMBinding nominated, int fileOffset) throws CoreException {
		final Database db= linkage.getDB();
		final long containerRec= containerNS == null ? 0 : containerNS.getRecord();
		final long nominatedRec= nominated.getRecord();
		
		fLinkage= linkage;
		fRecord= db.malloc(RECORD_SIZE);
		db.putRecPtr(fRecord + CONTAINER_NAMESPACE, containerRec);
		db.putRecPtr(fRecord + NOMINATED_NAMESPACE, nominatedRec);
		db.putRecPtr(fRecord + PREV_DIRECTIVE_OF_FILE, prevRecInFile);
		db.putInt(fRecord + FILE_OFFSET, fileOffset);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDirective#getNamespace()
	 */
	@Override
	public ICPPNamespaceScope getNominatedScope() {
		try {
			long rec = fLinkage.getDB().getRecPtr(fRecord + NOMINATED_NAMESPACE);
			PDOMNode node= fLinkage.getNode(rec);
			if (node instanceof ICPPNamespace) {
				return ((ICPPNamespace) node).getNamespaceScope();
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDirective#getContainingScope()
	 */
	@Override
	public IScope getContainingScope() {
		try {
			long rec = fLinkage.getDB().getRecPtr(fRecord + CONTAINER_NAMESPACE);
			if (rec != 0) {
				PDOMNode node= fLinkage.getNode(rec);
				if (node instanceof PDOMCPPNamespace) {
					return (PDOMCPPNamespace) node;
				}
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDirective#getPointOfDeclaration()
	 */
	@Override
	public int getPointOfDeclaration() {
		final Database db= fLinkage.getDB();
		try {
			return db.getInt(fRecord + FILE_OFFSET);
		} catch (CoreException e) {
			return 0;
		}
	}

	public long getRecord() {
		return fRecord;
	}

	public long getPreviousRec() throws CoreException {
		final Database db= fLinkage.getDB();
		return db.getRecPtr(fRecord + PREV_DIRECTIVE_OF_FILE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.IPDOMNode#accept(org.eclipse.cdt.core.dom.IPDOMVisitor)
	 */
	@Override
	public void accept(IPDOMVisitor visitor) throws CoreException {
	}

	@Override
	public void delete(PDOMLinkage linkage) throws CoreException {
		fLinkage.getDB().free(fRecord);
	}
}
