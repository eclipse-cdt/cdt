/*******************************************************************************
 * Copyright (c) 2007, 2012 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.parser.scanner;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTImageLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.internal.core.dom.Linkage;

/**
 * Models IASTNames as needed for the preprocessor statements and macro expansions.
 * @since 5.0
 */
class ASTPreprocessorName extends ASTPreprocessorNode implements IASTName {
	private final char[] fName;
	private final IBinding fBinding;
	public ASTPreprocessorName(IASTNode parent, ASTNodeProperty property, int startNumber, int endNumber, char[] name, IBinding binding) {
		super(parent, property, startNumber, endNumber);
		fName= name;
		fBinding= binding;
	}

	@Override
	public IBinding resolveBinding() {
		return fBinding;
	}
	@Override
	public IBinding resolvePreBinding() {
		return fBinding;
	}
	@Override
	public IBinding getBinding() {
		return fBinding;
	}
	@Override
	public IBinding getPreBinding() {
		return fBinding;
	}
	@Override
	public ILinkage getLinkage() {
		final IASTTranslationUnit tu= getTranslationUnit();
		return tu == null ? Linkage.NO_LINKAGE : tu.getLinkage();
	}
	@Override
	public IASTCompletionContext getCompletionContext() {
		return null;
	}
	@Override
	public boolean isDeclaration() {
		return false;
	}
	@Override
	public boolean isDefinition() {
		return false;
	}
	@Override
	public boolean isReference() {
		return false;
	}
	@Override
	public char[] toCharArray() {
		return fName;
	}    	
	@Override
	public char[] getSimpleID() {
		return fName;
	}
	@Override
	public char[] getLookupKey() {
		return fName;
	}

	@Override
	public String toString() {
		return new String(fName);
	}
	@Override
	public void setBinding(IBinding binding) {assert false;}

	@Override
	public int getRoleOfName(boolean allowResolution) {
		return IASTNameOwner.r_unclear;
	}
	@Override
	public IASTName getLastName() {
		return this;
	}
	@Override
	public boolean isQualified() {
		return false;
	}
	@Override
	public IASTName copy() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IASTName copy(CopyStyle style) {
		throw new UnsupportedOperationException();
	}
}

class ASTPreprocessorDefinition extends ASTPreprocessorName {
	public ASTPreprocessorDefinition(IASTNode parent, ASTNodeProperty property, int startNumber,
			int endNumber, char[] name, IBinding binding) {
		super(parent, property, startNumber, endNumber, name, binding);
	}

	@Override
	public boolean isDefinition() {
		return true;
	}
	
	@Override
	public int getRoleOfName(boolean allowResolution) {
		return IASTNameOwner.r_definition;
	}
}


class ASTBuiltinName extends ASTPreprocessorDefinition {
	private final IASTFileLocation fFileLocation;

	public ASTBuiltinName(IASTNode parent, ASTNodeProperty property, IASTFileLocation floc, char[] name, IBinding binding) {
		super(parent, property, -1, -1, name, binding);
		fFileLocation= floc;
	}

	@Override
	public boolean contains(IASTNode node) {
		return node==this;
	}

	@Override
	public String getContainingFilename() {
		if (fFileLocation == null) {
			return ""; //$NON-NLS-1$
		}
		return fFileLocation.getFileName();
	}

	@Override
	public IASTFileLocation getFileLocation() {
		return fFileLocation;
	}

	@Override
	public IASTNodeLocation[] getNodeLocations() {
		if (fFileLocation == null) {
			return new IASTNodeLocation[0];
		}
		return new IASTNodeLocation[]{fFileLocation};
	}

	@Override
	public String getRawSignature() {
		if (fFileLocation == null) {
			return ""; //$NON-NLS-1$
		}
		return toString();
	}
}

class ASTMacroReferenceName extends ASTPreprocessorName {
	private ImageLocationInfo fImageLocationInfo;
	
	public ASTMacroReferenceName(IASTNode parent, ASTNodeProperty property, int offset, int endOffset, IMacroBinding macro, ImageLocationInfo imgLocationInfo) {
		super(parent, property, offset, endOffset, macro.getNameCharArray(), macro);
		fImageLocationInfo= imgLocationInfo;
	}

	@Override
	public int getRoleOfName(boolean allowResolution) {
		return IASTNameOwner.r_unclear;
	}

	@Override
	public boolean isReference() {
		return true;
	}

	@Override
	public IASTImageLocation getImageLocation() {
		if (fImageLocationInfo != null) {
			IASTTranslationUnit tu= getTranslationUnit();
			if (tu != null) {
				LocationMap lr= (LocationMap) tu.getAdapter(LocationMap.class);
				if (lr != null) {
					return fImageLocationInfo.createLocation(lr, fImageLocationInfo);
				}
			}
			return null;
		}
		return super.getImageLocation();
	}
}
