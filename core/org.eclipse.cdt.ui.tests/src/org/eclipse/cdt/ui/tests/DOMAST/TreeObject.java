/**********************************************************************
 * Copyright (c) 2005 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation 
 **********************************************************************/
package org.eclipse.cdt.ui.tests.DOMAST;

import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorObjectStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.dom.ast.c.ICASTFieldDesignator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.gnu.c.IGCCASTArrayRangeDesignator;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTBinaryExpression;
import org.eclipse.core.runtime.IAdaptable;

/**
 * @author dsteffle
 */
public class TreeObject implements IAdaptable {
	private static final String OP_BRACKETEDPRIMARY = "( )"; //$NON-NLS-1$
	private static final String OP_TILDE = "~"; //$NON-NLS-1$
	private static final String OP_SIZEOF = "sizeof"; //$NON-NLS-1$
	private static final String OP_INCR = "++"; //$NON-NLS-1$
	private static final String OP_DECR = "--"; //$NON-NLS-1$
	private static final String OP_NOT = "!"; //$NON-NLS-1$
	private static final String OP_AMPER = "&"; //$NON-NLS-1$
	private static final String OP_TYPEOF = "typeof"; //$NON-NLS-1$
	private static final String OP_ALIGNOF = "alignof"; //$NON-NLS-1$
	private static final String OP_TYPEID = "typeid"; //$NON-NLS-1$
	private static final String OP_THROW = "throw"; //$NON-NLS-1$
	private static final String VARIABLE_SIZED_ = "* "; //$NON-NLS-1$
	private static final String VOLATILE_ = "volatile "; //$NON-NLS-1$
	private static final String STATIC_ = "static "; //$NON-NLS-1$
	private static final String RESTRICT_ = "restrict "; //$NON-NLS-1$
	private static final String CONST_ = "const "; //$NON-NLS-1$
	private static final String DASH = "-"; //$NON-NLS-1$
	private static final String OP_NOTEQUALS = "!="; //$NON-NLS-1$
	private static final String OP_EQUALS = "=="; //$NON-NLS-1$
	private static final String OP_BINARYXORASSIGN = "^="; //$NON-NLS-1$
	private static final String OP_BINARYANDASSIGN = "&="; //$NON-NLS-1$
	private static final String OP_BINARYORASSIGN = "|="; //$NON-NLS-1$
	private static final String OP_SHIFTRIGHTASSIGN = ">>="; //$NON-NLS-1$
	private static final String OP_SHIFTLEFTASSIGN = "<<="; //$NON-NLS-1$
	private static final String OP_MINUSASSIGN = "-="; //$NON-NLS-1$
	private static final String OP_PLUSASSIGN = "+="; //$NON-NLS-1$
	private static final String OP_MODULOASSIGN = "%="; //$NON-NLS-1$
	private static final String OP_DIVIDEASSIGN = "/="; //$NON-NLS-1$
	private static final String OP_MULTIPLYASSIGN = "*="; //$NON-NLS-1$
	private static final String OP_ASSIGN = "="; //$NON-NLS-1$
	private static final String OP_LOGICALOR = "||"; //$NON-NLS-1$
	private static final String OP_LOGICALAND = "&&"; //$NON-NLS-1$
	private static final String OP_BINARYOR = "|"; //$NON-NLS-1$
	private static final String OP_BINARYXOR = "^"; //$NON-NLS-1$
	private static final String OP_BINARYAND = "&"; //$NON-NLS-1$
	private static final String OP_GREATEREQUAL = ">="; //$NON-NLS-1$
	private static final String OP_LESSEQUAL = "<="; //$NON-NLS-1$
	private static final String OP_GREATERTHAN = ">"; //$NON-NLS-1$
	private static final String OP_LESSTHAN = "<"; //$NON-NLS-1$
	private static final String OP_SHIFTRIGHT = ">>"; //$NON-NLS-1$
	private static final String OP_SHIFTLEFT = "<<"; //$NON-NLS-1$
	private static final String OP_MINUS = DASH; //$NON-NLS-1$
	private static final String OP_PLUS = "+"; //$NON-NLS-1$
	private static final String OP_MODULO = "%"; //$NON-NLS-1$
	private static final String OP_DIVIDE = "/"; //$NON-NLS-1$
	private static final String OP_STAR = "*"; //$NON-NLS-1$
	private static final String OP_MIN = "<?"; //$NON-NLS-1$
	private static final String OP_MAX = ">?"; //$NON-NLS-1$
	private static final String OP_PMDOT = "."; //$NON-NLS-1$
	private static final String OP_PMARROW = "->"; //$NON-NLS-1$
	private static final String FILE_SEPARATOR = "\\"; //$NON-NLS-1$
	public static final String BLANK_STRING = ""; //$NON-NLS-1$
	private static final String IGCCAST_PREFIX = "IGCCAST"; //$NON-NLS-1$
	private static final String IGNUAST_PREFIX = "IGNUAST"; //$NON-NLS-1$
	private static final String IGPPAST_PREFIX = "IGPPAST"; //$NON-NLS-1$
	private static final String ICPPAST_PREFIX = "ICPPAST"; //$NON-NLS-1$
	private static final String ICAST_PREFIX = "ICAST"; //$NON-NLS-1$
	private static final String IAST_PREFIX = "IAST"; //$NON-NLS-1$
	private static final String START_OF_LIST = ": "; //$NON-NLS-1$
	private static final String LIST_SEPARATOR = ", "; //$NON-NLS-1$
	private static final String FILENAME_SEPARATOR = OP_PMDOT; //$NON-NLS-1$
	private IASTNode node = null;
	private TreeParent parent;
	
	public TreeObject(IASTNode node) {
		this.node = node;
	}
	public IASTNode getNode() {
		return node;
	}
	public void setParent(TreeParent parent) {
		this.parent = parent;
	}
	public TreeParent getParent() {
		return parent;
	}
	
	private boolean hasProperPrefix(String string) {
		if (string.startsWith(IAST_PREFIX) ||
				string.startsWith(ICAST_PREFIX) ||
				string.startsWith(ICPPAST_PREFIX) ||
				string.startsWith(IGPPAST_PREFIX) ||
				string.startsWith(IGNUAST_PREFIX) ||
				string.startsWith(IGCCAST_PREFIX))
			return true;

		return false;
	}
	
	public String toString() {
	    if( node == null ) return BLANK_STRING; //$NON-NLS-1$
		StringBuffer buffer = new StringBuffer();
		
		Class[] classes = node.getClass().getInterfaces();
		for(int i=0; i<classes.length; i++) {
			String interfaceName = classes[i].getName().substring(classes[i].getName().lastIndexOf(FILENAME_SEPARATOR) + 1);
			if (hasProperPrefix(interfaceName)) {
				buffer.append(interfaceName);
				if (i+1 < classes.length && hasProperPrefix(classes[i+1].getName().substring(classes[i+1].getName().lastIndexOf(FILENAME_SEPARATOR) + 1)))
					buffer.append(LIST_SEPARATOR);
			}
		}
		
		if ( node instanceof IASTSimpleDeclaration ) {
			String name = null;
			IASTDeclarator[] decltors = ((IASTSimpleDeclaration)node).getDeclarators();
			
			if ( decltors.length > 0 ) {
				buffer.append(START_OF_LIST);
				for (int i=0; i<decltors.length; i++) {
					name = getDeclaratorName(decltors[i]);
					buffer.append(name);
					
					if (i+1<decltors.length)
						buffer.append(LIST_SEPARATOR);
				}
			}
			return buffer.toString();
		} else if ( node instanceof IASTFunctionDefinition ) {
			String name = getDeclaratorName( ((IASTFunctionDefinition)node).getDeclarator() );
			if (name != null) {
				buffer.append(START_OF_LIST);
				buffer.append(name);
			}
			return buffer.toString();
		} else if ( node instanceof IASTName ) {
			buffer.append(START_OF_LIST);
			buffer.append(node);
			return buffer.toString();
		} else if ( node instanceof IASTTranslationUnit ) {
			String fileName = getFilename();
			int lastSlash = fileName.lastIndexOf(FILE_SEPARATOR);
			
			if (lastSlash > 0) {
				buffer.append(START_OF_LIST);
				buffer.append(fileName.substring(lastSlash+1)); // TODO make path relative to project, i.e. /projectName/path/file.c
			}
			
			return buffer.toString();
		} else if( node instanceof IASTDeclSpecifier )
		{
		    buffer.append( START_OF_LIST );
		    buffer.append( ((IASTDeclSpecifier)node).getUnpreprocessedSignature() );
		    return buffer.toString();
		} else if ( node instanceof IASTPreprocessorIncludeStatement ) {
			String path = ((IASTPreprocessorIncludeStatement)node).getPath();
			int lastSlash = path.lastIndexOf(FILE_SEPARATOR) + 1;
			buffer.append( START_OF_LIST );
			buffer.append( path.substring(lastSlash) );
		} else if ( node instanceof IASTPreprocessorObjectStyleMacroDefinition ) {
			String name = ((IASTPreprocessorObjectStyleMacroDefinition)node).getName().toString();
			if (name != null) {
				buffer.append( START_OF_LIST );
				buffer.append( name );
			}
		} else if ( node instanceof IASTLiteralExpression ) {
			buffer.append(START_OF_LIST);
			buffer.append(node.toString());
		} else if ( node instanceof IASTUnaryExpression ) {
			buffer.append(START_OF_LIST);
			buffer.append( getUnaryOperatorString( (IASTUnaryExpression)node ) );
		} else if ( node instanceof IASTBinaryExpression ) {
			buffer.append(START_OF_LIST);
			buffer.append( getBinaryOperatorString( (IASTBinaryExpression)node ) );
		} else if ( node instanceof ICASTDesignator ) {
			if ( node instanceof ICASTArrayDesignator && ((ICASTArrayDesignator)node).getSubscriptExpression() != null ) {
				buffer.append(START_OF_LIST);
				buffer.append(((ICASTArrayDesignator)node).getSubscriptExpression());
			} else if ( node instanceof ICASTFieldDesignator && ((ICASTFieldDesignator)node).getName() != null ) {
				buffer.append(START_OF_LIST);
				buffer.append(((ICASTFieldDesignator)node).getName());
			} else if ( node instanceof IGCCASTArrayRangeDesignator && ((IGCCASTArrayRangeDesignator)node).getRangeCeiling() != null && ((IGCCASTArrayRangeDesignator)node).getRangeFloor() != null ) {
				buffer.append(START_OF_LIST);
				buffer.append(((IGCCASTArrayRangeDesignator)node).getRangeCeiling());
				buffer.append(DASH);
				buffer.append(((IGCCASTArrayRangeDesignator)node).getRangeFloor());
			}
		} else if ( node instanceof IASTArrayModifier ) {
			boolean started = false;
			if ( node instanceof ICASTArrayModifier ) {
				started = true;
				buffer.append(START_OF_LIST);
				if (((ICASTArrayModifier)node).isConst()) buffer.append(CONST_);
				if (((ICASTArrayModifier)node).isRestrict()) buffer.append(RESTRICT_);
				if (((ICASTArrayModifier)node).isStatic()) buffer.append(STATIC_);
				if (((ICASTArrayModifier)node).isVolatile()) buffer.append(VOLATILE_);
				if (((ICASTArrayModifier)node).isVariableSized()) buffer.append(VARIABLE_SIZED_);
			}			

			IASTExpression constantExpression = ((IASTArrayModifier)node).getConstantExpression();
			if ( constantExpression != null && constantExpression instanceof IASTIdExpression ) { 
				if (!started) buffer.append(START_OF_LIST);
				buffer.append(((IASTIdExpression)constantExpression).getName().toString());
			}
		}
		
		return buffer.toString();
	}
	
	private String getUnaryOperatorString(IASTUnaryExpression be) {
		int op = be.getOperator();
		String opString = BLANK_STRING;
		
		if (be instanceof ICPPASTUnaryExpression) {
			switch(op) {
				case ICPPASTUnaryExpression.op_throw:
					opString = OP_THROW;
					break;
				case ICPPASTUnaryExpression.op_typeid:
					opString = OP_TYPEID;
					break;
			}
		} else if (be instanceof IGNUASTUnaryExpression) {
			switch(op) {
				case IGNUASTUnaryExpression.op_alignOf:
					opString = OP_ALIGNOF;
					break;
				case IGNUASTUnaryExpression.op_typeof:
					opString = OP_TYPEOF;
					break;
			}
		}
		
		if (!opString.equals(BLANK_STRING)) return opString;
		
		switch(op) {
			case IASTUnaryExpression.op_amper:
				opString = OP_AMPER;
				break;
			case IASTUnaryExpression.op_bracketedPrimary:
				opString = OP_BRACKETEDPRIMARY;
				break;
			case IASTUnaryExpression.op_minus:
				opString = OP_MINUS;
				break;
			case IASTUnaryExpression.op_not:
				opString = OP_NOT;
				break;
			case IASTUnaryExpression.op_plus:
				opString = OP_PLUS;
				break;
			case IASTUnaryExpression.op_postFixDecr:
				opString = OP_DECR;
				break;
			case IASTUnaryExpression.op_postFixIncr:
				opString = OP_INCR;
				break;
			case IASTUnaryExpression.op_prefixDecr:
				opString = OP_DECR;
				break;
			case IASTUnaryExpression.op_prefixIncr:
				opString = OP_INCR;
				break;
			case IASTUnaryExpression.op_sizeof:
				opString = OP_SIZEOF;
				break;
			case IASTUnaryExpression.op_star:
				opString = OP_STAR;
				break;
			case IASTUnaryExpression.op_tilde:
				opString = OP_TILDE; 
				break;
		}
		
		return opString;
	}
	
	private String getBinaryOperatorString(IASTBinaryExpression be) {
		int op = be.getOperator();
		String opString = BLANK_STRING;
		
		if (be instanceof ICPPASTBinaryExpression) {
			switch(op) {
				case ICPPASTBinaryExpression.op_pmarrow:
					opString = OP_PMARROW;
					break;
				case ICPPASTBinaryExpression.op_pmdot:
					opString = OP_PMDOT;
					break;
			}
		} else if (be instanceof IGPPASTBinaryExpression) {
			switch(op) {
				case IGPPASTBinaryExpression.op_max:
					opString = OP_MAX;
					break;
				case IGPPASTBinaryExpression.op_min:
					opString = OP_MIN;
					break;
			}
		}
		
		if (!opString.equals(BLANK_STRING)) return opString;
		
		switch(op) {
			case IASTBinaryExpression.op_multiply:
				opString = OP_STAR;
				break;
			case IASTBinaryExpression.op_divide:
				opString = OP_DIVIDE;
				break;
			case IASTBinaryExpression.op_modulo:
				opString = OP_MODULO;
				break;
			case IASTBinaryExpression.op_plus:
				opString = OP_PLUS;
				break;
			case IASTBinaryExpression.op_minus:
				opString = OP_MINUS;
				break;
			case IASTBinaryExpression.op_shiftLeft:
				opString = OP_SHIFTLEFT;
				break;
			case IASTBinaryExpression.op_shiftRight:
				opString = OP_SHIFTRIGHT;
				break;
			case IASTBinaryExpression.op_lessThan:
				opString = OP_LESSTHAN;
				break;
			case IASTBinaryExpression.op_greaterThan:
				opString = OP_GREATERTHAN;
				break;
			case IASTBinaryExpression.op_lessEqual:
				opString = OP_LESSEQUAL;
				break;
			case IASTBinaryExpression.op_greaterEqual:
				opString = OP_GREATEREQUAL;
				break;
			case IASTBinaryExpression.op_binaryAnd:
				opString = OP_BINARYAND;
				break;
			case IASTBinaryExpression.op_binaryXor:
				opString = OP_BINARYXOR;
				break;
			case IASTBinaryExpression.op_binaryOr:
				opString = OP_BINARYOR;
				break;
			case IASTBinaryExpression.op_logicalAnd:
				opString = OP_LOGICALAND;
				break;
			case IASTBinaryExpression.op_logicalOr:
				opString = OP_LOGICALOR;
				break;
			case IASTBinaryExpression.op_assign:
				opString = OP_ASSIGN;
				break;
			case IASTBinaryExpression.op_multiplyAssign:
				opString = OP_MULTIPLYASSIGN;
				break;
			case IASTBinaryExpression.op_divideAssign:
				opString = OP_DIVIDEASSIGN;
				break;
			case IASTBinaryExpression.op_moduloAssign:
				opString = OP_MODULOASSIGN;
				break;
			case IASTBinaryExpression.op_plusAssign:
				opString = OP_PLUSASSIGN;
				break;
			case IASTBinaryExpression.op_minusAssign:
				opString = OP_MINUSASSIGN;
				break;
			case IASTBinaryExpression.op_shiftLeftAssign:
				opString = OP_SHIFTLEFTASSIGN;
				break;
			case IASTBinaryExpression.op_shiftRightAssign:
				opString = OP_SHIFTRIGHTASSIGN;
				break;
			case IASTBinaryExpression.op_binaryAndAssign:
				opString = OP_BINARYANDASSIGN;
				break;
			case IASTBinaryExpression.op_binaryXorAssign:
				opString = OP_BINARYXORASSIGN;
				break;
			case IASTBinaryExpression.op_binaryOrAssign:
				opString = OP_BINARYORASSIGN;
				break;
			case IASTBinaryExpression.op_equals:
				opString = OP_EQUALS;
				break;
			case IASTBinaryExpression.op_notequals:
				opString = OP_NOTEQUALS;
				break;
		}
		
		return opString;
	}
	
	private String getDeclaratorName(IASTDeclarator decltor) {
		String name = BLANK_STRING;
		while (decltor != null && decltor.getName() != null && decltor.getName().toString() == null) {
			decltor = decltor.getNestedDeclarator();
		}
		if (decltor != null && decltor.getName() != null) {
			name = decltor.getName().toString();
		}
		return name;
	}
	
	public Object getAdapter(Class key) {
		return null;
	}
	
	public String getFilename()
	{
		if ( node == null ) return BLANK_STRING;
	   IASTNodeLocation [] location = node.getNodeLocations();
	   if( location.length > 0 && location[0] instanceof IASTFileLocation )
	      return ((IASTFileLocation)location[0]).getFileName();
	   return BLANK_STRING; //$NON-NLS-1$
	}
	
	public int getOffset() {
	   IASTNodeLocation [] location = node.getNodeLocations();
	   if( location.length == 1 )
	      return location[0].getNodeOffset();
	   return 0;
	}
	
	public int getLength() {
	   IASTNodeLocation [] location = node.getNodeLocations();
	   if( location.length == 1 )
	      return location[0].getNodeLength();
	   return 0;
	}
}
