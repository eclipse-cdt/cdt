/*******************************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.enablement;

import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;

public abstract class CompositeExpression implements IBooleanExpression {
	private IBooleanExpression fChildren[];
	protected CompositeExpression(IManagedConfigElement element){
		IManagedConfigElement childElement[] = element.getChildren();
		IBooleanExpression children[] = new IBooleanExpression[childElement.length];
		int num = 0;
		for(int i = 0; i < childElement.length; i++){
			IBooleanExpression child = createExpression(childElement[i]);
			if(child != null)
				children[num++] = child;
		}
		
		if(num < children.length){
			IBooleanExpression tmp[] = new IBooleanExpression[num];
			System.arraycopy(children,0,tmp,0,num);
			children = tmp;
		}
		fChildren = children;
	}
	
	protected IBooleanExpression createExpression(IManagedConfigElement element){
		String name = element.getName();
		if(AndExpression.NAME.equals(name))
			return new AndExpression(element);
		else if(OrExpression.NAME.equals(name))
			return new OrExpression(element);
		else if(NotExpression.NAME.equals(name))
			return new NotExpression(element);
		else if(CheckOptionExpression.NAME.equals(name))
			return new CheckOptionExpression(element);
		else if(CheckStringExpression.NAME.equals(name))
				return new CheckStringExpression(element);
		else if(FalseExpression.NAME.equals(name))
			return new FalseExpression(element);
		else if(CheckHolderExpression.NAME.equals(name))
			return new CheckHolderExpression(element);
		return null;
	}
	
	public IBooleanExpression[] getChildren(){
		return fChildren;
	}
}
