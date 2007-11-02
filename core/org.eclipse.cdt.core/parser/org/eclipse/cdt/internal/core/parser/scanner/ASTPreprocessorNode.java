/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.parser.scanner;

import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionStyleMacroParameter;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElseStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorEndifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorErrorStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfdefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfndefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorObjectStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorPragmaStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorUndefStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit.IDependencyTree;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit.IDependencyTree.IASTInclusionNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * Models various AST-constructs obtained from the preprocessor.
 * @since 5.0
 */
abstract class ASTPreprocessorNode extends ASTNode {
	public ASTPreprocessorNode(IASTNode parent, ASTNodeProperty property, int startNumber, int endNumber) {
		setParent(parent);
		setPropertyInParent(property);
		setOffset(startNumber);
		setLength(endNumber-startNumber);
	}
	
	protected String getSource(int offset, int length) {
		final IASTTranslationUnit tu= getTranslationUnit();
		IASTNodeLocation[] loc=tu.getLocationInfo(offset, length);
		return tu.getUnpreprocessedSignature(loc);
	}

	public IASTNodeLocation[] getNodeLocations() {
		if (getLength() == 0) {
			return getTranslationUnit().getLocationInfo(getOffset(), 0);
		}
		return super.getNodeLocations();
	}

	public String getContainingFilename() {
		if (super.getOffset() == -1) {
			throw new UnsupportedOperationException();
		}
		return super.getContainingFilename();
	}

	public IASTFileLocation getFileLocation() {
		if (super.getOffset() == -1) {
			throw new UnsupportedOperationException();
		}
		return super.getFileLocation();
	}

	public int getLength() {
		if (super.getOffset() == -1) {
			throw new UnsupportedOperationException();
		}
		return super.getLength();
	}

	public int getOffset() {
		final int offset = super.getOffset();
		if (offset == -1) {
			throw new UnsupportedOperationException();
		}
		return offset;
	}

	public String getRawSignature() {
		if (super.getOffset() == -1) {
			throw new UnsupportedOperationException();
		}
		return super.getRawSignature();
	}
}



class ASTComment extends ASTPreprocessorNode implements IASTComment {
	private final boolean fIsBlockComment;
	public ASTComment(IASTTranslationUnit parent, int startNumber, int endNumber, boolean isBlockComment) {
		super(parent, IASTTranslationUnit.PREPROCESSOR_STATEMENT, startNumber, endNumber);
		fIsBlockComment= isBlockComment;
	}

	public char[] getComment() {
		return getSource(getOffset(), getLength()).toCharArray();
	}

	public boolean isBlockComment() {
		return fIsBlockComment;
	}

	public void setComment(char[] comment) {
		assert false;
	}
}


abstract class ASTDirectiveWithCondition extends ASTPreprocessorNode {
	private final int fConditionOffset;
    private final boolean fActive;
	public ASTDirectiveWithCondition(IASTTranslationUnit parent, int startNumber, int condNumber, int endNumber, boolean active) {
		super(parent, IASTTranslationUnit.PREPROCESSOR_STATEMENT, startNumber, endNumber);
		fConditionOffset= condNumber;
		fActive= active;
	}

    public boolean taken() {
        return fActive;
    }
        
    public String getConditionString() {
    	return getSource(fConditionOffset, getOffset() + getLength() - fConditionOffset);
    }
    
    public char[] getCondition() {
    	return getConditionString().toCharArray();
    }
}

class ASTEndif extends ASTPreprocessorNode implements IASTPreprocessorEndifStatement {
	public ASTEndif(IASTTranslationUnit parent, int startNumber, int endNumber) {
		super(parent, IASTTranslationUnit.PREPROCESSOR_STATEMENT, startNumber, endNumber);
	}
}

class ASTElif extends ASTDirectiveWithCondition implements IASTPreprocessorElifStatement {
	public ASTElif(IASTTranslationUnit parent, int startNumber, int condNumber, int condEndNumber, boolean active) {
		super(parent, startNumber, condNumber, condEndNumber, active);
    }
}

class ASTElse extends ASTPreprocessorNode implements IASTPreprocessorElseStatement {
	private final boolean fActive;
    public ASTElse(IASTTranslationUnit parent, int startNumber, int endNumber, boolean active) {
		super(parent, IASTTranslationUnit.PREPROCESSOR_STATEMENT, startNumber, endNumber);
		fActive= active;
	}
    public boolean taken() {
        return fActive;
    }
}

class ASTIfndef extends ASTDirectiveWithCondition implements IASTPreprocessorIfndefStatement {
	public ASTIfndef(IASTTranslationUnit parent, int startNumber, int condNumber, int condEndNumber, boolean active) {
		super(parent, startNumber, condNumber, condEndNumber, active);
	}
}

class ASTIfdef extends ASTDirectiveWithCondition implements IASTPreprocessorIfdefStatement {
	public ASTIfdef(IASTTranslationUnit parent, int startNumber, int condNumber, int condEndNumber, boolean active) {
		super(parent, startNumber, condNumber, condEndNumber, active);
	}
}

class ASTIf extends ASTDirectiveWithCondition implements IASTPreprocessorIfStatement {
	public ASTIf(IASTTranslationUnit parent, int startNumber, int condNumber, int condEndNumber, boolean active) {
		super(parent, startNumber, condNumber, condEndNumber, active);
	}
}

class ASTError extends ASTDirectiveWithCondition implements IASTPreprocessorErrorStatement {
	public ASTError(IASTTranslationUnit parent, int startNumber, int condNumber, int condEndNumber) {
		super(parent, startNumber, condNumber, condEndNumber, true);
	}

	public char[] getMessage() {
		return getCondition();
	}
}

class ASTPragma extends ASTDirectiveWithCondition implements IASTPreprocessorPragmaStatement {
	public ASTPragma(IASTTranslationUnit parent, int startNumber, int condNumber, int condEndNumber) {
		super(parent, startNumber, condNumber, condEndNumber, true);
	}

	public char[] getMessage() {
		return getCondition();
	}
}

class ASTInclusionStatement extends ASTPreprocessorNode implements IASTPreprocessorIncludeStatement {
	private final IASTName fName;
	private final String fPath;
	private final boolean fIsActive;
	private final boolean fIsResolved;
	private final boolean fIsSystemInclude;

	public ASTInclusionStatement(IASTTranslationUnit parent, int startNumber, int nameStartNumber, int nameEndNumber, 
			char[] headerName, String filePath, boolean userInclude, boolean active) {
		super(parent, IASTTranslationUnit.PREPROCESSOR_STATEMENT, startNumber, nameEndNumber);
		fName= new ASTPreprocessorName(this, IASTPreprocessorIncludeStatement.INCLUDE_NAME, nameStartNumber, nameEndNumber, headerName, null);
		fPath= filePath == null ? "" : filePath; //$NON-NLS-1$
		fIsActive= active;
		fIsResolved= filePath != null;
		fIsSystemInclude= !userInclude;
	}

	public IASTName getName() {
		return fName;
	}

	public String getPath() {
		return fPath;
	}

	public boolean isActive() {
		return fIsActive;
	}

	public boolean isResolved() {
		return fIsResolved;
	}

	public boolean isSystemInclude() {
		return fIsSystemInclude;
	}
}

class ASTMacro extends ASTPreprocessorNode implements IASTPreprocessorObjectStyleMacroDefinition {
	private final ASTPreprocessorName fName;
	
	/**
	 * Regular constructor.
	 */
	public ASTMacro(IASTTranslationUnit parent, IMacroBinding macro, 
			int startNumber, int nameNumber, int nameEndNumber, int expansionNumber, int endNumber) {
		super(parent, IASTTranslationUnit.PREPROCESSOR_STATEMENT, startNumber, endNumber);
		fName= new ASTPreprocessorDefinition(this, IASTPreprocessorMacroDefinition.MACRO_NAME, nameNumber, nameEndNumber, macro.getNameCharArray(), macro);
	}

	/**
	 * Constructor for built-in macros
	 * @param expansionOffset 
	 */
	public ASTMacro(IASTTranslationUnit parent, IMacroBinding macro, String filename, int nameOffset, int nameEndOffset, int expansionOffset) {
		super(parent, IASTTranslationUnit.PREPROCESSOR_STATEMENT, -1, -1);
		fName= new ASTBuiltinName(this, IASTPreprocessorMacroDefinition.MACRO_NAME, filename, nameOffset, nameEndOffset, macro.getNameCharArray(), macro);
	}

	protected IMacroBinding getMacro() {
		return (IMacroBinding) fName.getBinding();
	}
	
	public String getExpansion() {
		return new String(getMacro().getExpansion());
	}

	public IASTName getName() {
		return fName;
	}

	public int getRoleForName(IASTName n) {
		return (fName == n) ? r_definition : r_unclear;
	}

	public void setExpansion(String exp) {assert false;}
	public void setName(IASTName name) {assert false;}
}

class ASTMacroParameter extends ASTPreprocessorNode implements IASTFunctionStyleMacroParameter  {
	private final String fParameter;
	
	public ASTMacroParameter(IASTPreprocessorFunctionStyleMacroDefinition parent, char[] param, int offset, int endOffset) {
		super(parent, IASTPreprocessorFunctionStyleMacroDefinition.PARAMETER, offset, endOffset);
		fParameter= new String(param);
	}

	public String getParameter() {
		return fParameter;
	}

	public void setParameter(String value) {assert false;}
}

class ASTFunctionMacro extends ASTMacro implements IASTPreprocessorFunctionStyleMacroDefinition {
	/**
	 * Regular constructor.
	 */
	public ASTFunctionMacro(IASTTranslationUnit parent, IMacroBinding macro, 
			int startNumber, int nameNumber, int nameEndNumber, int expansionNumber, int endNumber) {
		super(parent, macro, startNumber, nameNumber, nameEndNumber, expansionNumber, endNumber);
	}

	/**
	 * Constructor for builtins
	 */
	public ASTFunctionMacro(IASTTranslationUnit parent, IMacroBinding macro, 
			String filename, int nameOffset, int nameEndOffset, int expansionOffset) {
		super(parent, macro, filename, nameOffset, nameEndOffset, expansionOffset);
	}

	public IASTFunctionStyleMacroParameter[] getParameters() {
    	IMacroBinding macro= getMacro();
    	char[][] paramList= macro.getParameterList();
    	IASTFunctionStyleMacroParameter[] result= new IASTFunctionStyleMacroParameter[paramList.length];
    	for (int i = 0; i < result.length; i++) {
			result[i]= new ASTMacroParameter(this, paramList[i], -1, -1);
		}
        return result;
    }

	public void addParameter(IASTFunctionStyleMacroParameter parm) {assert false;}
}


class ASTUndef extends ASTPreprocessorNode implements IASTPreprocessorUndefStatement {
	private final IASTName fName;
	public ASTUndef(IASTTranslationUnit parent, char[] name, int startNumber, int nameNumber, int nameEndNumber, IBinding binding) {
		super(parent, IASTTranslationUnit.PREPROCESSOR_STATEMENT, startNumber, nameEndNumber);
		fName= new ASTPreprocessorName(this, IASTPreprocessorUndefStatement.MACRO_NAME, nameNumber, nameEndNumber, name, binding);
	}

	public IASTName getMacroName() {
		return fName;
	}
}

class ASTInclusionNode implements IASTInclusionNode {
	protected LocationCtx fLocationCtx;
	private IASTInclusionNode[] fInclusions;

	public ASTInclusionNode(LocationCtx ctx) {
		fLocationCtx= ctx;
	}

	public IASTPreprocessorIncludeStatement getIncludeDirective() {
		return fLocationCtx.getInclusionStatement();
	}

	public IASTInclusionNode[] getNestedInclusions() {
		if (fInclusions == null) {
			ArrayList result= new ArrayList();
			fLocationCtx.getInclusions(result);
			fInclusions= (IASTInclusionNode[]) result.toArray(new IASTInclusionNode[result.size()]);
		}
		return fInclusions;
	}
}

class DependencyTree extends ASTInclusionNode implements IDependencyTree {
	public DependencyTree(LocationCtx ctx) {
		super(ctx);
	}

	public IASTInclusionNode[] getInclusions() {
		return getNestedInclusions();
	}

	public String getTranslationUnitPath() {
		return fLocationCtx.getFilePath();
	}
}

class ASTFileLocation implements IASTFileLocation {
	private FileLocationCtx fLocationCtx;
	private int fOffset;
	private int fLength;

	public ASTFileLocation(FileLocationCtx fileLocationCtx, int startOffset, int length) {
		fLocationCtx= fileLocationCtx;
		fOffset= startOffset;
		fLength= length;
	}

	public String getFileName() {
		return fLocationCtx.getFilePath();
	}

	public IASTFileLocation asFileLocation() {
		return this;
	}

	public int getNodeLength() {
		return fLength;
	}

	public int getNodeOffset() {
		return fOffset;
	}

	public int getEndingLineNumber() {
		int end= fLength > 0 ? fOffset+fLength-1 : fOffset;
		return fLocationCtx.getLineNumber(end);
	}

	public int getStartingLineNumber() {
		return fLocationCtx.getLineNumber(fOffset);
	}

	public char[] getSource() {
		return fLocationCtx.getSource(fOffset, fLength);
	}
}

class ASTFileLocationForBuiltins implements IASTFileLocation {
	private String fFile;
	private int fOffset;
	private int fLength;

	public ASTFileLocationForBuiltins(String file, int startOffset, int length) {
		fFile= file;
		fOffset= startOffset;
		fLength= length;
	}

	public String getFileName() {
		return fFile;
	}

	public IASTFileLocation asFileLocation() {
		return this;
	}

	public int getNodeLength() {
		return fLength;
	}

	public int getNodeOffset() {
		return fOffset;
	}

	public int getEndingLineNumber() {
		return 0;
	}

	public int getStartingLineNumber() {
		return 0;
	}
}


