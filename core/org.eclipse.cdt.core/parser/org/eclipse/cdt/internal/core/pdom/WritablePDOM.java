/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core.pdom;

import java.text.MessageFormat;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.internal.core.index.IIndexFragmentFile;
import org.eclipse.cdt.internal.core.index.IWritableIndexFragment;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public class WritablePDOM extends PDOM implements IWritableIndexFragment {

	public WritablePDOM(IPath dbPath) throws CoreException {
		super(dbPath);
	}
	public IIndexFragmentFile addFile(String filename) throws CoreException {
		return super.addFile(filename);
	}

	public void addFileContent(IIndexFragmentFile sourceFile, 
			IASTPreprocessorIncludeStatement[] includes, IIndexFragmentFile[] destFiles,
			IASTPreprocessorMacroDefinition[] macros, IASTName[] names) throws CoreException {
		assert sourceFile.getIndexFragment() == this;
		
		PDOMFile pdomFile = (PDOMFile) sourceFile;
		pdomFile.addIncludesTo(destFiles, includes);
		pdomFile.addMacros(macros);
		for (int i = 0; i < names.length; i++) {
			IASTName name= names[i];
			PDOMLinkage linkage= createLinkage(name.getLinkage().getID());
			if (linkage == null) {
				CCorePlugin.log(MessageFormat.format(Messages.WritablePDOM_error_unknownLinkage, new Object[]{name.getLinkage()}));
			}
			else {
				linkage.addName(name, pdomFile);
			}
		}
	}

	public void clearFile(IIndexFragmentFile file) throws CoreException {
		assert file.getIndexFragment() == this;
		((PDOMFile) file).clear();		
	}
	
	public void clear() throws CoreException {
		super.clear();
	}
}
