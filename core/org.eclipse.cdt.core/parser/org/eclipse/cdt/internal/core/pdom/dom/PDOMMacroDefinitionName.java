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
package org.eclipse.cdt.internal.core.pdom.dom;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexFragmentName;
import org.eclipse.core.runtime.CoreException;

class PDOMMacroDefinitionName implements IIndexFragmentName {
	private final PDOMMacro fMacro;

	PDOMMacroDefinitionName(PDOMMacro macro) {
		fMacro = macro;
	}
	
	public PDOMMacro getMacro() {
		return fMacro;
	}
	
	@Override
	public boolean couldBePolymorphicMethodCall() throws CoreException {
		return false;
	}
	@Override
	public IIndexName[] getEnclosedNames() throws CoreException {
		return IIndexName.EMPTY_ARRAY;
	}
	@Override
	public IIndexName getEnclosingDefinition() throws CoreException {
		return null;
	}
	@Override
	public IIndexFile getFile() throws CoreException {
		return fMacro.getFile();
	}
	@Override
	public int getNodeLength() {
		return fMacro.getNodeLength();
	}
	@Override
	public int getNodeOffset() {
		return fMacro.getNodeOffset();
	}
	@Override
	public boolean isBaseSpecifier() throws CoreException {
		return false;
	}
	@Override
	public boolean isReadAccess() throws CoreException {
		return false;
	}
	@Override
	public boolean isWriteAccess() throws CoreException {
		return false;
	}
	@Override
	public IASTFileLocation getFileLocation() {
		return fMacro;
	}
	@Override
	public boolean isDeclaration() {
		return false;
	}
	@Override
	public boolean isDefinition() {
		return true;
	}
	@Override
	public boolean isReference() {
		return false;
	}
	@Override
	public boolean isInlineNamespaceDefinition() {
		return false;
	}

	@Override
	@Deprecated
	public char[] toCharArray() {
		return fMacro.getNameCharArray();
	}
	@Override
	public String toString() {
		return new String(getSimpleID());
	}
	
	@Override
	public char[] getSimpleID() {
		return fMacro.getNameCharArray();
	}
	@Override
	public IIndexFragmentBinding getBinding() {
		return fMacro;
	}
	@Override
	public IIndexFragment getIndexFragment() {
		return fMacro.getFragment();
	}
}