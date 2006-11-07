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

	public void addInclude(IIndexFragmentFile sourceFile, IIndexFragmentFile destFile,
			IASTPreprocessorIncludeStatement include) throws CoreException {
		assert sourceFile.getIndexFragment() == this;
		assert destFile.getIndexFragment() == this;
		((PDOMFile) sourceFile).addIncludeTo((PDOMFile) destFile, include);
	}

	public void addMacro(IIndexFragmentFile sourceFile, IASTPreprocessorMacroDefinition macro) throws CoreException {
		assert sourceFile.getIndexFragment() == this;
		((PDOMFile) sourceFile).addMacro(macro);
	}

	public void addName(IIndexFragmentFile sourceFile, IASTName name) throws CoreException {
		assert sourceFile.getIndexFragment() == this;
		PDOMLinkage linkage= createLinkage(name.getLinkage().getID());
		if (linkage == null) {
			CCorePlugin.log(MessageFormat.format(Messages.WritablePDOM_error_unknownLinkage, new Object[]{name.getLinkage()}));
		}
		else {
			linkage.addName(name, (PDOMFile) sourceFile);
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
