/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *******************************************************************************/
/*
 * Created on Jun 12, 2003
 */
package org.eclipse.cdt.internal.ui.search;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerator;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.SearchFor;
import org.eclipse.core.resources.IMarker;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkingSet;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class CSearchUtil {

	public static int LRU_WORKINGSET_LIST_SIZE= 3;
	private static LRUWorkingSets workingSetsCache;
	
	/**
	 * 
	 */
	public CSearchUtil() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param sets
	 */
	public static void updateLRUWorkingSets(IWorkingSet[] workingSets) {
		if (workingSets == null || workingSets.length < 1)
			return;
		
		CSearchUtil.getLRUWorkingSets().add(workingSets);
	}

	public static LRUWorkingSets getLRUWorkingSets() {
		if (CSearchUtil.workingSetsCache == null) {
			CSearchUtil.workingSetsCache = new LRUWorkingSets(CSearchUtil.LRU_WORKINGSET_LIST_SIZE);
		}
		return CSearchUtil.workingSetsCache;
	}
	
	/**
	 * @param object
	 * @param shell
	 */
	public static void warnIfBinaryConstant( ICElement element, Shell shell) {
		// TODO Auto-generated method stub
		
	}
	/**
	 * @param workingSets
	 * @return
	 */
	public static String toString(IWorkingSet[] workingSets) {
		if( workingSets != null & workingSets.length > 0 ){
			String string = new String();
			for( int i = 0; i < workingSets.length; i++ ){
				if( i > 0 )
					string += ", ";  //$NON-NLS-1$
				string += workingSets[i].getName();
			}
			
			return string;
		}
		
		return null;
	}


	/**
	 * @param marker
	 * @return
	 */
	public static ICElement getCElement(IMarker marker) {
		// TODO Auto-generated method stub
		return null;
	}

	static public SearchFor getSearchForFromElement( ICElement element ) {
		if( element == null )
			return null;

		SearchFor searchFor = null; 
		//outline view will confuse methods with functions, so if the 
		//name contains a "::", treat it as a method
		String pattern = element.getElementName();
		boolean forceMethod = ( pattern.indexOf("::") != -1 ); //$NON-NLS-1$
		
		switch ( element.getElementType() ){
			case ICElement.C_TEMPLATE_FUNCTION:	   /*fall through to function */
			case ICElement.C_FUNCTION_DECLARATION: /*fall through to function */
			case ICElement.C_FUNCTION:	if( forceMethod ) searchFor = ICSearchConstants.METHOD; 
										else 			  searchFor = ICSearchConstants.FUNCTION;		
										break;
										
			case ICElement.C_VARIABLE:	searchFor = ICSearchConstants.VAR;			break;
			
			case ICElement.C_TEMPLATE_CLASS:/*   fall through to CLASS   */ 
			case ICElement.C_STRUCT:		/*   fall through to CLASS   */	 
			case ICElement.C_CLASS:		searchFor = ICSearchConstants.CLASS_STRUCT;	break;
			
			case ICElement.C_UNION:		searchFor = ICSearchConstants.UNION;			break;
			
			case ICElement.C_ENUMERATOR: 
										searchFor = ICSearchConstants.ENUMTOR;			break;
			case ICElement.C_FIELD:		searchFor = ICSearchConstants.FIELD;			break;
			
			case ICElement.C_TEMPLATE_METHOD : 	  /*fall through to METHOD */
			case ICElement.C_METHOD_DECLARATION : /*fall through to METHOD */
			case ICElement.C_METHOD:	searchFor = ICSearchConstants.METHOD;		break;
			
			case ICElement.C_NAMESPACE: searchFor = ICSearchConstants.NAMESPACE;	break;
			
			case ICElement.C_ENUMERATION: searchFor = ICSearchConstants.ENUM;		break;
			
			default: searchFor = ICSearchConstants.UNKNOWN_SEARCH_FOR; break;
		}
		return searchFor;
	}
	
	static public SearchFor getSearchForFromNode(IASTOffsetableNamedElement node){
		SearchFor searchFor = null;
		
		if (node instanceof IASTClassSpecifier){
			//Find out if class, struct, union
		   IASTClassSpecifier tempNode = (IASTClassSpecifier) node;
		   if(tempNode.getClassKind().equals(ASTClassKind.CLASS)){
		   	searchFor = ICSearchConstants.CLASS;
		   }
		   else if (tempNode.getClassKind().equals(ASTClassKind.STRUCT)){
		   	searchFor = ICSearchConstants.STRUCT;
		   }
		   else if (tempNode.getClassKind().equals(ASTClassKind.UNION)){
		   	searchFor = ICSearchConstants.UNION;
		   }
		}
		else if (node instanceof IASTMethod){
			searchFor = ICSearchConstants.METHOD;
		}
		else if (node instanceof IASTFunction){
			searchFor = ICSearchConstants.FUNCTION;
		}
		else if (node instanceof IASTField){
			searchFor = ICSearchConstants.FIELD;
		}
		else if (node instanceof IASTVariable){
			searchFor = ICSearchConstants.VAR;
		}
		else if (node instanceof IASTEnumerationSpecifier){
			searchFor = ICSearchConstants.ENUM;
		}
		else if (node instanceof IASTNamespaceDefinition){
			searchFor = ICSearchConstants.NAMESPACE;
		}
		else if( node instanceof IASTTypedefDeclaration)
			searchFor = ICSearchConstants.TYPEDEF;
		else if( node instanceof IASTEnumerator )
			searchFor = ICSearchConstants.ENUMTOR;
		
		return searchFor;
	}
}
