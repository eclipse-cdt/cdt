/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.search.indexing;

import java.io.IOException;

import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.internal.core.index.IDocument;
import org.eclipse.cdt.internal.core.index.IIndexer;
import org.eclipse.cdt.internal.core.index.IIndexerOutput;
import org.eclipse.cdt.internal.core.search.CharOperation;

public abstract class AbstractIndexer implements IIndexer, IIndexConstants, ICSearchConstants {
	
	IIndexerOutput output;
	final static int CLASS = 1;
	final static int STRUCT = 2;
	final static int UNION = 3;
	
	public AbstractIndexer() {
		super();
	}
	
	public void addClassSpecifier(IASTClassSpecifier classSpecification){
		if (classSpecification.getClassKind().equals(ASTClassKind.CLASS))
		{
			this.output.addRef(encodeTypeEntry(classSpecification.getName().toCharArray(),CLASS));
		}		
		else if (classSpecification.getClassKind().equals(ASTClassKind.STRUCT))
		{
			this.output.addRef(encodeTypeEntry(classSpecification.getName().toCharArray(),STRUCT));
		}
		else if (classSpecification.getClassKind().equals(ASTClassKind.UNION))
		{
			this.output.addRef(encodeTypeEntry(classSpecification.getName().toCharArray(),UNION));			
		}
	}
	
	public void addConstructorDeclaration(){
		
	}
	public void addConstructorReference(){

	}
	
	public void addMemberDeclaration(){
	
	}
	public void addMemberReference(){
	
	}

	public void addFunctionDeclaration(){
	
	}
	
	public void addFunctionReference(){
	
	}
	
	public void addNameReference(){
		
	}
	
	private void addSuperTypeReference(int modifiers, char[] packageName, char[] typeName, char[][] enclosingTypeNames, char classOrInterface, char[] superTypeName, char superClassOrInterface){

	}
	
	public void addTypeReference(char[] typeName){
		this.output.addRef(CharOperation.concat(TYPE_REF, CharOperation.lastSegment(typeName, '.')));
	}
	/**
	 * Type entries are encoded as follow: 'typeDecl/' ('C' | 'S' | 'U' ) '/'  TypeName '/' 
	 */
	 protected static final char[] encodeTypeEntry( char[] typeName, int classType) {
		
		int pos;
		//3 - 1 for SUFFIX letter, 2 Separators
		char[] result = new char[TYPE_DECL_LENGTH + typeName.length + 3];
		System.arraycopy(TYPE_DECL, 0, result, 0, pos = TYPE_DECL_LENGTH);
		switch (classType)
		{
			case(CLASS):
			result[pos++] = CLASS_SUFFIX;
			break;
			
			case(STRUCT):
			result[pos++] = STRUCT_SUFFIX;
			break;
			
			case(UNION):
			result[pos++] = UNION_SUFFIX;
			break;
		}
		
		result[pos++] = SEPARATOR;
		System.arraycopy(typeName, 0, result, pos, typeName.length);
		pos += typeName.length;
		result[pos++] = SEPARATOR;
		
		return result;
	}
	/**
	 * Returns the file types the <code>IIndexer</code> handles.
	 */
	public abstract String[] getFileTypes();
	/**
	 * @see IIndexer#index(IDocument document, IIndexerOutput output)
	 */
	public void index(IDocument document, IIndexerOutput output) throws IOException {
		this.output = output;
		if (shouldIndex(document)) indexFile(document);
	}
	
	protected abstract void indexFile(IDocument document) throws IOException;
	/**
	 * @see IIndexer#shouldIndex(IDocument document)
	 */
	public boolean shouldIndex(IDocument document) {
		String type = document.getType();
		String[] supportedTypes = this.getFileTypes();
		for (int i = 0; i < supportedTypes.length; ++i) {
			if (supportedTypes[i].equals(type))
				return true;
		}
		return false;
	}
}

