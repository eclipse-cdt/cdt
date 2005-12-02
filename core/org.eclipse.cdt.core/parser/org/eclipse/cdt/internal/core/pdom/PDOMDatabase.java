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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.ILanguage;
import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.LanguageManager;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.pdom.db.BTree;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.core.resources.IFile;
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

	public static final int LANGUAGES = Database.DATA_AREA;
	public static final int FILE_INDEX = Database.DATA_AREA + 4;
	public static final int BINDING_INDEX = Database.DATA_AREA + 8;
	
	private Map languageCache;
	private ILanguage[] languages;
	
	private BTree fileIndex;
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
	
	public BTree getBindingIndex() throws CoreException {
		if (bindingIndex == null)
			bindingIndex = new BTree(getDB(), BINDING_INDEX);
		return bindingIndex;
	}
	
	public void addSymbols(ITranslationUnit tu) throws CoreException {
		final ILanguage language = tu.getLanguage();
		if (language == null)
			return;
		
		final int languageId = getLanguageId(language);
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
							PDOMBinding binding = language.getPDOMBinding(PDOMDatabase.this, languageId, name);
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
		bindingIndex = null;
		fileIndex = null;
		languageCache = null;
		languages = null;
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
	
	private void initLanguageMap() throws CoreException {
		// load in the languages
		languageCache = new HashMap();
		int record = db.getInt(LANGUAGES);
		PDOMLanguage lang = null;
		if (record != 0)
			lang = new PDOMLanguage(this, record);
		while (lang != null) {
			languageCache.put(lang.getName(), lang);
			lang = lang.getNext();
		}
		
		// map language ids to ILanguage impls.
		languages = new ILanguage[languageCache.size() + 1]; // + 1 for the empty zero
		Iterator i = languageCache.values().iterator();
		while (i.hasNext()) {
			lang = (PDOMLanguage)i.next();
			languages[lang.getId()] = LanguageManager.getInstance().getLanguage(lang.getName());
		}
	}
	
	public int getLanguageId(ILanguage language) throws CoreException {
		if (languageCache == null)
			initLanguageMap();
		PDOMLanguage pdomLang = (PDOMLanguage)languageCache.get(language.getId());
		if (pdomLang == null) {
			// add it in
			int next = db.getInt(LANGUAGES);
			int id = next == 0 ? 1 : new PDOMLanguage(this, next).getId() + 1;
			pdomLang = new PDOMLanguage(this, language.getId(), id, next);
			db.putInt(LANGUAGES, pdomLang.getRecord());
			ILanguage[] oldlangs = languages;
			languages = new ILanguage[id + 1];
			System.arraycopy(oldlangs, 0, languages, 0, id);
			languages[id] = language;
			return id;
		} else
			return pdomLang.getId();
	}

	public ILanguage getLanguage(int id) throws CoreException {
		if (languages == null)
			initLanguageMap();
		return languages[id];
	}
}
