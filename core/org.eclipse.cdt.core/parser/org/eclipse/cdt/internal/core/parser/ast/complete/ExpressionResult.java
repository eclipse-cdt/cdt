/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.core.parser.ast.complete;

import org.eclipse.cdt.internal.core.parser.pst.TypeInfo;

/**
 * @author hamer
 *
 */
public class ExpressionResult {
	
 
 private TypeInfo result;
 private boolean failedToDereference = false;
 
 ExpressionResult(){
 	result = new TypeInfo();
 }
 ExpressionResult(TypeInfo result){
 	this.result = result;
 }
/**
 * @return
 */
public TypeInfo getResult() {
	return result;
}

/**
 * @param info
 */
public void setResult(TypeInfo info) {
	result = info;
}

/**
 * @return
 */
public boolean isFailedToDereference() {
	return failedToDereference;
}

/**
 * @param b
 */
public void setFailedToDereference(boolean b) {
	failedToDereference = b;
}

}
