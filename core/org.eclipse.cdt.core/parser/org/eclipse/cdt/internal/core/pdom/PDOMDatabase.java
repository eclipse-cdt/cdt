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

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ILanguage;
import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.pdom.db.BTree;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.pdom.dom.PDOMName;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;


/**
 * The PDOM Database.
 * 
 * @author Doug Schaefer
 */
public class PDOMDatabase implements IPDOM {

	private final IPath dbPath;
	private final Database db;
	
	private static final int VERSION = 0;
	
	public static final int STRING_INDEX = Database.DATA_AREA + 0 * Database.INT_SIZE;
	private BTree stringIndex;
	
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
		
		try {
			db = new Database(dbPath.toOSString(), VERSION);
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					CCorePlugin.PLUGIN_ID, 0, "Failed to create database", e));
		}
	}

	public Database getDB() {
		return db;
	}

	public BTree getStringIndex() {
		if (stringIndex == null)
			stringIndex = new BTree(db, STRING_INDEX);
		return stringIndex;
	}
	
	public BTree getFileIndex() {
		if (fileIndex == null)
			fileIndex = new BTree(db, FILE_INDEX);
		return fileIndex;
	}
	
	public BTree getBindingIndex() {
		if (bindingIndex == null)
			bindingIndex = new BTree(db, BINDING_INDEX);
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
						if (name.toCharArray().length > 0)
							language.getPDOMBinding(PDOMDatabase.this, name);
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
		// TODO Auto-generated method stub
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
		} catch (IOException e) {
			CCorePlugin.log(e);
		}
		return new IASTName[0];
	}

	public IBinding resolveBinding(IASTName name) {
		try {
			return new PDOMBinding(this, name, null);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	public IBinding[] resolvePrefix(IASTName name) {
//		try {
			final String prefix = new String(name.toCharArray());
			final ArrayList bindings = new ArrayList();
			
//			getStringIndex().visit(new PDOMString.Visitor(db, prefix) {
//				public boolean visit(int record) throws IOException {
//					String value = new String(new PDOMString(PDOMDatabase.this, record).getString());
//					if (value.startsWith(prefix)) {
//						PDOMBinding pdomBinding = PDOMBinding.find(PDOMDatabase.this, record);
//						if (pdomBinding != null)
//							bindings.add(pdomBinding);
//						return true;
//					} else
//						return false;
//				}
//			});
			
			return (IBinding[])bindings.toArray(new IBinding[bindings.size()]);
//		} catch (IOException e) {
//			PDOMCorePlugin.log(new CoreException(new Status(IStatus.ERROR,
//					PDOMCorePlugin.ID, 0, "resolvePrefix", e)));
//			return null;
//		}
	}
	
}
