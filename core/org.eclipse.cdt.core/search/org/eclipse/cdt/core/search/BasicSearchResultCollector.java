/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *******************************************************************************/
/*
 * Created on Jul 29, 2003
 */
package org.eclipse.cdt.core.search;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.ASTUtil;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerator;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTMacro;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTOffsetableElement;
import org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement;
import org.eclipse.cdt.core.parser.ast.IASTQualifiedNameElement;
import org.eclipse.cdt.core.parser.ast.IASTReference;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class BasicSearchResultCollector implements ICSearchResultCollector {
	 IProgressMonitor fProgressMonitor = null;

	 public BasicSearchResultCollector() {
	 }
	 
	 public BasicSearchResultCollector(IProgressMonitor monitor) {
	 		 fProgressMonitor = monitor;
	 }

	public void aboutToStart() {
		results = new HashSet();
	}

	public void done() {
	}
	
	public IProgressMonitor getProgressMonitor() {
		 		 return fProgressMonitor;
	}

	 public void setProgressMonitor(IProgressMonitor monitor) {
	 		 fProgressMonitor = monitor;
	 }

	public IMatch createMatch(Object fileResource, int start, int end, ISourceElementCallbackDelegate node, IPath referringElement)
	{
		BasicSearchMatch result = new BasicSearchMatch();
		return createMatch( result, fileResource, start, end, node, referringElement);
	}
	
	 public IMatch createMatch( BasicSearchMatch result, Object fileResource, int start, int end, ISourceElementCallbackDelegate node, IPath referringElement ) {
		if( fileResource instanceof IResource )
			result.resource = (IResource) fileResource;
		else if( fileResource instanceof IPath )
			result.path = (IPath) fileResource;
			
		result.startOffset = start;
		result.endOffset = end;
		result.parentName = ""; //$NON-NLS-1$
		result.referringElement = referringElement;
		
		IASTOffsetableNamedElement offsetable = null;
	
		if( node instanceof IASTReference ){
			offsetable = (IASTOffsetableNamedElement) ((IASTReference)node).getReferencedElement();
			result.name = ((IASTReference)node).getName();
		} else if( node instanceof IASTOffsetableNamedElement ){
			offsetable = (IASTOffsetableNamedElement)node;
			result.name = offsetable.getName();
		}
		
		result.parentName = ""; //$NON-NLS-1$
		String [] names = null;
		if( offsetable instanceof IASTEnumerator ){
			IASTEnumerator enumerator = (IASTEnumerator) offsetable;
			names = enumerator.getOwnerEnumerationSpecifier().getFullyQualifiedName();
		} else if( offsetable instanceof IASTQualifiedNameElement ) {
			names = ((IASTQualifiedNameElement) offsetable).getFullyQualifiedName();
		}
	
		if( names != null ){
			for( int i = 0; i < names.length - 1; i++ ){
				if( i > 0 )
					result.parentName += "::"; //$NON-NLS-1$
			
				result.parentName += names[ i ];
			}
		}
		if (offsetable instanceof IASTVariable){
			result.returnType = ASTUtil.getType(((IASTVariable)offsetable).getAbstractDeclaration());
		}
		if( offsetable instanceof IASTFunction ){
			result.name += getParameterString( (IASTFunction) offsetable );
			result.returnType = ASTUtil.getType(((IASTFunction)offsetable).getReturnType());
		}
		
		setElementInfo( result, offsetable );
		
		return result;
	}


	/**
	 * @param function
	 * @return
	 */
	private String getParameterString(IASTFunction function) {
		if( function == null )
			return ""; //$NON-NLS-1$
		
		String paramString = "("; //$NON-NLS-1$
		
		String [] paramTypes = ASTUtil.getFunctionParameterTypes( function );
		
		for( int i = 0; i < paramTypes.length; i++ ){
			if( i != 0 )
				paramString += ", "; //$NON-NLS-1$
			paramString += paramTypes[i];
		}

		paramString += ")"; //$NON-NLS-1$
		return paramString;
	}

	public boolean acceptMatch(IMatch match) throws CoreException {
		if( !results.contains( match ) ){
			results.add( match );
			return true;
		}
		return false;
	}
	
	public Set getSearchResults(){
		return results;
	}
	
	private void setElementInfo( BasicSearchMatch match, IASTOffsetableElement node ){
		//ImageDescriptor imageDescriptor = null;
		if( node instanceof IASTClassSpecifier ||
			node instanceof IASTElaboratedTypeSpecifier ){
			
			ASTClassKind kind = null;
			if (node instanceof IASTClassSpecifier){
				kind = ((IASTClassSpecifier)node).getClassKind();
			}
			else{
				kind = ((IASTElaboratedTypeSpecifier)node).getClassKind();
			}
			
			if( kind == ASTClassKind.CLASS ){
				match.type = ICElement.C_CLASS;
			} else if ( kind == ASTClassKind.STRUCT ){
				match.type = ICElement.C_STRUCT;
			} else if ( kind == ASTClassKind.UNION ){
				match.type = ICElement.C_UNION;
			}
		} else if ( node instanceof IASTNamespaceDefinition ){
			match.type = ICElement.C_NAMESPACE;
		} else if ( node instanceof IASTEnumerationSpecifier ){
			match.type = ICElement.C_ENUMERATION;
		} else if ( node instanceof IASTMacro ){
			match.type = ICElement.C_MACRO;
		} else if ( node instanceof IASTField ){
			match.type = ICElement.C_FIELD;
			IASTField  field = (IASTField)node;
			ASTAccessVisibility visibility = field.getVisiblity();
			if( visibility == ASTAccessVisibility.PUBLIC ){
				match.visibility = ICElement.CPP_PUBLIC;
			} else if ( visibility == ASTAccessVisibility.PRIVATE ) {
				match.visibility = ICElement.CPP_PRIVATE;
			} // else protected, there is no ICElement.CPP_PROTECTED
			match.isConst = field.getAbstractDeclaration().isConst();
			match.isStatic = field.isStatic();
		} else if ( node instanceof IASTVariable ){
			match.type = ICElement.C_VARIABLE;
			IASTVariable variable = (IASTVariable)node;
			match.isConst  = variable.getAbstractDeclaration().isConst();
		} else if ( node instanceof IASTEnumerator ){
			match.type = ICElement.C_ENUMERATOR;
		} else if ( node instanceof IASTMethod ){
			match.type = ICElement.C_METHOD;
			IASTMethod method = (IASTMethod) node;
			ASTAccessVisibility visibility = method.getVisiblity();
			if( visibility == ASTAccessVisibility.PUBLIC ){
				match.visibility = ICElement.CPP_PUBLIC;
			} else if ( visibility == ASTAccessVisibility.PRIVATE ) {
				match.visibility = ICElement.CPP_PRIVATE;
			} // else protected, there is no ICElement.CPP_PROTECTED
			match.isConst = method.isConst();
			match.isVolatile = method.isVolatile();
			match.isStatic = method.isStatic();
		} else if ( node instanceof IASTFunction ){
			match.type = ICElement.C_FUNCTION;
			IASTFunction function = (IASTFunction)node;
			match.isStatic = function.isStatic();
		} else if ( node instanceof IASTTypedefDeclaration ){
			match.type = ICElement.C_TYPEDEF;
		}
	}
	
	private Set results;
}
