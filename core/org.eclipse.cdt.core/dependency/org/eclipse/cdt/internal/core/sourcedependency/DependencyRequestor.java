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
package org.eclipse.cdt.internal.core.sourcedependency;

import java.util.LinkedList;

import org.eclipse.cdt.core.parser.ast.IASTInclusion;
import org.eclipse.cdt.internal.core.index.IDocument;
import org.eclipse.cdt.internal.core.parser.NullSourceElementRequestor;

public class DependencyRequestor extends NullSourceElementRequestor {
	PreprocessorOutput preprocessor;
	IDocument document;
	private IASTInclusion currentInclude = null;
	private LinkedList includeStack = new LinkedList();

    public DependencyRequestor(PreprocessorOutput p, IDocument doc){
    	this.preprocessor = p;
    	this.document = doc;
    }
	
	public void enterInclusion(IASTInclusion inclusion) {
		//System.out.println("enterInclusion " + inclusion.getName());
		//Get parent
		IASTInclusion parent = peekInclude();
		preprocessor.addInclude(inclusion, parent);
		//Push on stack
		pushInclude(inclusion);
	}
	
	public void exitInclusion(IASTInclusion inclusion) {
		// TODO Auto-generated method stub
		//System.out.println("Exit inclusion " + inclusion.getFullFileName());
		//Pop
		popInclude();
	}
	
	private void pushInclude( IASTInclusion inclusion ){
			includeStack.addFirst( currentInclude );
			currentInclude = inclusion;
	}
	
	private IASTInclusion popInclude(){
		IASTInclusion oldInclude = currentInclude;
		currentInclude = (includeStack.size() > 0 ) ? (IASTInclusion) includeStack.removeFirst() : null;
		return oldInclude;
	}
	
	private IASTInclusion peekInclude(){
		return currentInclude;
	}
}
