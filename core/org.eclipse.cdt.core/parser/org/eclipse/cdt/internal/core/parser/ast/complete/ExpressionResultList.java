/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.ast.complete;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.internal.core.parser.pst.ITypeInfo;

/**
 * @author hamer
 *
 */
public class ExpressionResultList extends ExpressionResult {
	private List resultList = new ArrayList();
	ExpressionResultList(){
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ExpressionResult#getResult()
	 */
	public ITypeInfo getResult() {
		// TODO Auto-generated method stub
		return (ITypeInfo)resultList.get(0);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.complete.ExpressionResult#setResult(org.eclipse.cdt.internal.core.parser.pst.TypeInfo)
	 */
	public void setResult(ITypeInfo info) {
		// TODO Auto-generated method stub
		resultList.add(info);
	}

	public void setResult( ExpressionResultList result ){
	    List list = result.getResultList();
	    int size = list.size();
	    for( int i = 0; i < size; i++ ){
	        resultList.add( list.get( i ) );    
	    }
	}
	
	/**
	 * @return
	 */
	public List getResultList() {
		return resultList;
	}

	/**
	 * @param list
	 */
	public void setResultList(List list) {
		resultList = list;
	}

}
