/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ILanguage;
import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.pdom.db.BTree;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.QualifiedName;


/**
 * The PDOM Database.
 * 
 * @author Doug Schaefer
 */
public class PDOMDatabase implements IPDOM {

	private final IPath dbPath;
	private Database db;
	
	private static final int VERSION = 0;
	
	public static final int FILE_INDEX = Database.DATA_AREA + 1 * Database.INT_SIZE;
	private BTree fileIndex;

	public static final int BINDING_INDEX = Database.DATA_AREA + 2 * Database.INT_SIZE;
	private BTree bindingIndex;

	private static final QualifiedName dbNameProperty
		= new QualifiedName(CCorePlugin.PLUGIN_ID, "dbName"); //$NON-NLS-1$

	public PDOMDatabase(IProject project, PDOMManager manager) throws CoreException {
		String dbName = project.getPersistentProperty(dbNameProperty);
		if (dbName == null) {
			dbName = project.getName() + "_"
					+ System.currentTimeMillis() + ".pdom";
			project.setPersistentProperty(dbNameProperty, dbName);
		}
		
		dbPath = CCorePlugin.getDefault().getStateLocation().append(dbName);
		db = new Database(dbPath.toOSString(), VERSION);
	}

	public Database getDB() throws CoreException {
		if (db == null)
			db = new Database(dbPath.toOSString(), VERSION);

		return db;
	}

	public BTree getFileIndex() throws CoreException {
		if (fileIndex == null)
			fileIndex = new BTree(getDB(), FILE_INDEX);
		return fileIndex;
	}
	
	public BTree getBindingIndex() throws CoreException {
		if (bindingIndex == null)
			bindingIndex = new BTree(getDB(), BINDING_INDEX);
		return bindingIndex;
	}
	
	public void addSymbols(ITranslationUnit tu) throws CoreException {
		final ILanguage language = tu.getLanguage();
		if (language == null)
			return;
		
		IASTTranslationUnit ast = language.getTranslationUnit(tu,
				ILanguage.AST_USE_INDEX |
				ILanguage.AST_SKIP_INDEXED_HEADERS);

		ast.accept(new ASTVisitor() {
				{
					shouldVisitNames = true;
					shouldVisitDeclarations = true;
				}

				public int visit(IASTName name) {
					try {
						if (name.toCharArray().length > 0) {
							PDOMBinding binding = language.getPDOMBinding(PDOMDatabase.this, name);
							if (binding != null)
								new PDOMName(PDOMDatabase.this, name, binding);
						}
						return PROCESS_CONTINUE;
					} catch (CoreException e) {
						CCorePlugin.log(e);
						return PROCESS_ABORT;
					}
				};
			});;
	}
	
	public void removeSymbols(ITranslationUnit ast) {
		
	}
	
	public void delete() throws CoreException {
		db = null;
		bindingIndex = null;
		fileIndex = null;
		System.gc();
		dbPath.toFile().delete();
	}

	public ICodeReaderFactory getCodeReaderFactory() {
		return new PDOMCodeReaderFactory(this);
	}

	public ICodeReaderFactory getCodeReaderFactory(IWorkingCopy root) {
		return new PDOMCodeReaderFactory(this, root);
	}

	public IASTName[] getDeclarations(IBinding binding) {
		try {
			if (binding instanceof PDOMBinding) {
				PDOMName name = ((PDOMBinding)binding).getFirstDeclaration();
				if (name == null)
					return new IASTName[0];
				return new IASTName[] { name }; 
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return new IASTName[0];
	}

	public IBinding resolveBinding(IASTName name) {
		return null;
	}

	public IBinding[] resolvePrefix(IASTName name) {
		return new IBinding[0];
	}
	
}
