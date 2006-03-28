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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.internal.core.pdom.db.BTree;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMLinkageFactory;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * The PDOM Database.
 * 
 * @author Doug Schaefer
 */
public class PDOMDatabase implements IPDOM {

	private final IProject project;
	
	private final IPath dbPath;
	private Database db;
	
	private static final int VERSION = 0;

	public static final int LINKAGES = Database.DATA_AREA;
	public static final int FILE_INDEX = Database.DATA_AREA + 4;
	
	private BTree fileIndex;

	private static final QualifiedName dbNameProperty
		= new QualifiedName(CCorePlugin.PLUGIN_ID, "dbName"); //$NON-NLS-1$

	public PDOMDatabase(IProject project, PDOMManager manager) throws CoreException {
		this.project = project;
		String dbName = project.getPersistentProperty(dbNameProperty);
		if (dbName == null) {
			dbName = project.getName() + "_"
					+ System.currentTimeMillis() + ".pdom";
			project.setPersistentProperty(dbNameProperty, dbName);
		}
		
		dbPath = CCorePlugin.getDefault().getStateLocation().append(dbName);
		db = new Database(dbPath.toOSString(), VERSION);
	}

	public IProject getProject() {
		return project;
	}
	
	public static interface IListener {
		public void handleChange(PDOMDatabase pdom);
	}
	
	private List listeners;
	
	public void addListener(IListener listener) {
		if (listeners == null)
			listeners = new LinkedList();
		listeners.add(listener);
	}
	
	public void removeListener(IListener listener) {
		if (listeners == null)
			return;
		listeners.remove(listener);
	}
	
	private void fireChange() {
		if (listeners == null)
			return;
		Iterator i = listeners.iterator();
		while (i.hasNext())
			((IListener)i.next()).handleChange(this);
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
	
	public void addSymbols(ITranslationUnit tu) throws CoreException {
		final ILanguage language = tu.getLanguage();
		if (language == null)
			return;
		
		final PDOMLinkage linkage = getLinkage(language);
		if (linkage == null)
			return;

		IASTTranslationUnit ast = language.getTranslationUnit((IFile)tu.getResource(),
				ILanguage.AST_USE_INDEX |
				ILanguage.AST_SKIP_INDEXED_HEADERS |
				ILanguage.AST_SKIP_IF_NO_BUILD_INFO);
		if (ast == null)
			return;

		ast.accept(new ASTVisitor() {
				{
					shouldVisitNames = true;
					shouldVisitDeclarations = true;
				}

				public int visit(IASTName name) {
					try {
						linkage.addName(name);
						return PROCESS_CONTINUE;
					} catch (CoreException e) {
						CCorePlugin.log(e);
						return PROCESS_ABORT;
					}
				};
			});;
		
		fireChange();
	}
	
	public void removeSymbols(ITranslationUnit tu) throws CoreException {
		String filename = ((IFile)tu.getResource()).getLocation().toOSString();
		PDOMFile file = PDOMFile.find(this, filename);
		if (file == null)
			return;
		file.clear();
	}
	
	public void delete() throws CoreException {
		getDB().clear();
		fileIndex = null;
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
		try {
			ILanguage language = name.getTranslationUnit().getLanguage();
			return getLinkage(language).resolveBinding(name);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		
		return null;
	}

	public PDOMBinding[] findBindings(String pattern) throws CoreException {
		List bindings = new ArrayList();
		PDOMLinkage linkage = getFirstLinkage();
		while (linkage != null) {
			linkage.findBindings(pattern, bindings);
			linkage = linkage.getNextLinkage();
		}
		return (PDOMBinding[])bindings.toArray(new PDOMBinding[bindings.size()]);
	}
	
	public PDOMLinkage getLinkage(ILanguage language) throws CoreException {
		IPDOMLinkageFactory factory = (IPDOMLinkageFactory)language.getAdapter(IPDOMLinkageFactory.class);
		String id = language.getId();
		int linkrec = db.getInt(LINKAGES);
		while (linkrec != 0) {
			if (id.equals(PDOMLinkage.getId(this, linkrec)))
				return factory.getLinkage(this, linkrec);
			else
				linkrec = PDOMLinkage.getNextLinkageRecord(this, linkrec);
		}
		
		return factory.createLinkage(this);
	}

	public PDOMLinkage getLinkage(int record) throws CoreException {
		if (record == 0)
			return null;
		
		String id = PDOMLinkage.getId(this, record);
		ILanguage language = LanguageManager.getInstance().getLanguage(id);
		return getLinkage(language);
	}
	
	public PDOMLinkage getFirstLinkage() throws CoreException {
		return getLinkage(db.getInt(LINKAGES));
	}
	
	public void insertLinkage(PDOMLinkage linkage) throws CoreException {
		linkage.setNext(db.getInt(LINKAGES));
		db.putInt(LINKAGES, linkage.getRecord());
	}
	
	public PDOMBinding getBinding(int record) throws CoreException {
		if (record == 0)
			return null;
		else
			return PDOMLinkage.getLinkage(this, record).getBinding(record);
	}

	// Read-write lock rules. Readers don't conflict with other readers,
	// Writers conflict with readers, and everyone conflicts with writers.
	private class ReaderLockRule implements ISchedulingRule {
		public boolean isConflicting(ISchedulingRule rule) {
			if (rule == this)
				return false;
			else if (rule == getWriterLockRule())
				return true;
			else
				return false;
		}
		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}
	}

	private class WriterLockRule implements ISchedulingRule {
		public boolean isConflicting(ISchedulingRule rule) {
			if (rule == this || rule == getReaderLockRule())
				return true;
			else
				return false;
		}
		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}
	}
	
	private ReaderLockRule readerLockRule = new ReaderLockRule();
	private WriterLockRule writerLockRule = new WriterLockRule();
	
	public ISchedulingRule getReaderLockRule() {
		return readerLockRule;
	}
	
	public ISchedulingRule getWriterLockRule() {
		return writerLockRule;
	}
	
}
