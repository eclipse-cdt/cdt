/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Andrew Ferguson (Symbian)
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core.pdom;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFile;
import org.eclipse.cdt.internal.core.index.IWritableIndexFragment;
import org.eclipse.cdt.internal.core.pdom.db.ChunkCache;
import org.eclipse.cdt.internal.core.pdom.db.DBProperties;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.core.runtime.CoreException;

public class WritablePDOM extends PDOM implements IWritableIndexFragment {

	public WritablePDOM(File dbPath, IIndexLocationConverter locationConverter) throws CoreException {
		this(dbPath, locationConverter, ChunkCache.getSharedInstance());
	}
	
	public WritablePDOM(File dbPath, IIndexLocationConverter locationConverter, ChunkCache cache) throws CoreException {
		super(dbPath, locationConverter, cache);
	}

	public IIndexFragmentFile addFile(IIndexFileLocation location) throws CoreException {
		return super.addFile(location);
	}

	public void addFileContent(IIndexFragmentFile sourceFile, 
			IASTPreprocessorIncludeStatement[] includes, IIndexFragmentFile[] destFiles,
			IASTPreprocessorMacroDefinition[] macros, IASTName[][] names) throws CoreException {
		assert sourceFile.getIndexFragment() == this;
		assert includes.length == destFiles.length;
		
		PDOMFile pdomFile = (PDOMFile) sourceFile;
		pdomFile.addIncludesTo(destFiles, includes);
		pdomFile.addMacros(macros);
		pdomFile.addNames(names);
	}

	public void clearFile(IIndexFragmentFile file) throws CoreException {
		assert file.getIndexFragment() == this;
		((PDOMFile) file).clear();		
	}
	
	public void clear() throws CoreException {
		super.clear();
	}
	
	public PDOMBinding addBinding(IASTName name) throws CoreException {
		PDOMBinding result= null;
		PDOMLinkage linkage= createLinkage(name.getLinkage().getID());
		if (linkage == null) {
			CCorePlugin.log(MessageFormat.format(Messages.WritablePDOM_error_unknownLinkage, new Object[]{name.getLinkage()}));
		}
		else {
			result= linkage.addBinding(name);
		}
		return result;
	}
	
	public void setProperty(String propertyName, String value) throws CoreException {
		new DBProperties(db, PROPERTIES).setProperty(propertyName, value);
	}
	

	/**
	 * Use the specified location converter to update each internal representation of a file location.
	 * The file index is rebuilt with the new representations. Individual PDOMFile records are unmoved so
	 * as to maintain referential integrity with other PDOM records.
	 * 
	 * <b>A write-lock must be obtained before calling this method</b>
	 * 
	 * @param newConverter the converter to use to update internal file representations
	 * @return a list of PDOMFiles for which the location converter returned null when queried for the new internal representation
	 * @throws CoreException
	 */
	public List/*<PDOMFile>*/ rewriteLocations(final IIndexLocationConverter newConverter) throws CoreException {
		final List pdomfiles = new ArrayList();
		getFileIndex().accept(new IBTreeVisitor(){
			public int compare(int record) throws CoreException {
				return 0;
			}
			public boolean visit(int record) throws CoreException {
				PDOMFile file = new PDOMFile(WritablePDOM.this, record);
				pdomfiles.add(file);
				return true;
			}
		});

		clearFileIndex();
		final List notConverted = new ArrayList();
		for(Iterator i= pdomfiles.iterator(); i.hasNext(); ) {
			PDOMFile file= (PDOMFile) i.next();
			String internalFormat = newConverter.toInternalFormat(file.getLocation());
			if(internalFormat!=null) {
				file.setInternalLocation(internalFormat);
				getFileIndex().insert(file.getRecord());
			} else {
				notConverted.add(file);
			}
		}

		return notConverted;
	}
}
