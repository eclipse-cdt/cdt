/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.indexer.ctags;

import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCFunction;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCVariable;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class CtagsCName implements IASTName, IASTFileLocation {
	
	private final PDOMLinkage linkage;
	private final String fileName;
	private final int lineNum;
	private final String elementName;
	private final Map fields;
	private PDOMBinding binding;
	private int kind; // Enum from below
	
	private final static int K_UNKNOWN = 0;
	private final static int K_CLASS = 1;
	private final static int K_MACRO = 2;
	private final static int K_ENUMERATOR = 3;
	private final static int K_FUNCTION = 4;
	private final static int K_ENUM = 5;
	private final static int K_MEMBER = 6;
	private final static int K_NAMESPACE = 7;
	private final static int K_PROTOTYPE = 8;
	private final static int K_STRUCT = 9;
	private final static int K_TYPEDEF = 10;
	private final static int K_UNION = 11;
	private final static int K_VARIABLE = 12;
	private final static int K_EXTERNALVAR = 13;

	private final static String[] kinds = { // Order must match value of enum above
			null, // unknown kinds
			"class", //$NON-NLS-1$
			"macro", //$NON-NLS-1$
			"enumerator", //$NON-NLS-1$
			"function", //$NON-NLS-1$
			"enum", //$NON-NLS-1$
			"member", //$NON-NLS-1$
			"namespace", //$NON-NLS-1$
			"prototype", //$NON-NLS-1$
			"struct", //$NON-NLS-1$
			"typedef", //$NON-NLS-1$
			"union", //$NON-NLS-1$
			"variable", //$NON-NLS-1$
			"externvar", //$NON-NLS-1$
	};

    public CtagsCName(PDOMLinkage linkage, String fileName, int lineNum, String elementName, Map fields) throws CoreException {
    	this.linkage = linkage;
		this.fileName = fileName;
		this.lineNum = lineNum;
		this.elementName = elementName;
		this.fields = fields;
		
		kind = K_UNKNOWN;
		String kindField = (String)fields.get("kind"); //$NON-NLS-1$
		if (kindField != null) {
			for (int i = 1; i < kinds.length; ++i) {
				if (kindField.equals(kinds[i])) {
					kind = i;
					break;
				}
			}
		}
		
	}

    public void addToPDOM() throws CoreException {
		linkage.addName(this);
    }
    
	public IBinding getBinding() {
		return binding;
	}

	public boolean isDeclaration() {
		throw new PDOMNotImplementedError();
	}

	public boolean isDefinition() {
		// TODO base on kind
		return true;
	}

	public boolean isReference() {
		// We're never a reference
		return false;
	}

	private PDOMBinding makeNewBinding(IPDOMNode scope) throws CoreException {
		switch (kind) {
		case K_VARIABLE:
			return new PDOMCVariable(linkage.getPDOM(), (PDOMNode)scope, this);
		case K_FUNCTION:
			return new PDOMCFunction(linkage.getPDOM(), (PDOMNode)scope, this);
		default:
			return null;
		}
	}
	
	public IBinding resolveBinding() {
		try {
			IPDOMNode scope = linkage;
			int[] types = null;
			switch (kind) {
			case K_VARIABLE:
				types = new int[] { PDOMCLinkage.CVARIABLE };
				break;
			case K_FUNCTION:
				types = new int[] { PDOMCLinkage.CFUNCTION };
				break;
			default:
				return null;
			}
			
			CtagsBindingFinder finder = new CtagsBindingFinder(elementName, types);
			scope.accept(finder);
			PDOMBinding[] bindings = finder.getBindings();
			if (bindings.length == 0)
				binding = makeNewBinding(scope);
			else if (bindings.length == 1)
				binding = bindings[0];
			else
				// TODO resolve overloads
				binding = bindings[0];
			return binding;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	public IBinding[] resolvePrefix() {
		throw new PDOMNotImplementedError();
	}

	public void setBinding(IBinding binding) {
		throw new PDOMNotImplementedError();
	}

	public char[] toCharArray() {
		return elementName.toCharArray();
	}

	public boolean accept(ASTVisitor visitor) {
		throw new PDOMNotImplementedError();
	}

	public String getContainingFilename() {
		return fileName;
	}

	public IASTFileLocation getFileLocation() {
		return this;
	}

	public IASTNodeLocation[] getNodeLocations() {
		throw new PDOMNotImplementedError();
	}

	public IASTNode getParent() {
		throw new PDOMNotImplementedError();
	}

	public ASTNodeProperty getPropertyInParent() {
		throw new PDOMNotImplementedError();
	}

	public String getRawSignature() {
		throw new PDOMNotImplementedError();
	}

	public IASTTranslationUnit getTranslationUnit() {
		throw new PDOMNotImplementedError();
	}

	public void setParent(IASTNode node) {
		throw new PDOMNotImplementedError();
	}

	public void setPropertyInParent(ASTNodeProperty property) {
		throw new PDOMNotImplementedError();
	}

	public int getEndingLineNumber() {
		throw new PDOMNotImplementedError();
	}

	public String getFileName() {
		return fileName;
	}

	public int getStartingLineNumber() {
		return lineNum;
	}

	public IASTFileLocation asFileLocation() {
		throw new PDOMNotImplementedError();
	}

	public int getNodeLength() {
		// -1 means we have a line num as the offset
		return -1;
	}

	public int getNodeOffset() {
		// since node length is -1, we can return the line number here
		return lineNum;
	}

}
