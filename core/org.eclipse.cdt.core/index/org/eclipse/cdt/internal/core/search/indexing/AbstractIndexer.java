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
import java.util.Iterator;

import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerator;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
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
	final static int ENUM = 4;
	final static int VAR = 5;

	public AbstractIndexer() {
		super();
	}
	
	public void addClassSpecifier(IASTClassSpecifier classSpecification){

		if (classSpecification.getClassKind().equals(ASTClassKind.CLASS))
		{
			this.output.addRef(encodeTypeEntry(classSpecification.getFullyQualifiedName(),CLASS));
		}		
		else if (classSpecification.getClassKind().equals(ASTClassKind.STRUCT))
		{
			this.output.addRef(encodeTypeEntry(classSpecification.getFullyQualifiedName(),STRUCT));
		}
		else if (classSpecification.getClassKind().equals(ASTClassKind.UNION))
		{
			this.output.addRef(encodeTypeEntry(classSpecification.getFullyQualifiedName(),UNION));			
		}
	}
	
	public void addEnumerationSpecifier(IASTEnumerationSpecifier enumeration) {
		this.output.addRef(encodeTypeEntry(enumeration.getFullyQualifiedName(), ENUM));
		
	Iterator i = enumeration.getEnumerators();
		while (i.hasNext())
		{
			IASTEnumerator en = (IASTEnumerator) i.next(); 	
			String name = en.getName();
			IASTEnumerationSpecifier parent = en.getOwnerEnumerationSpecifier();
			String[] parentName = parent.getFullyQualifiedName();
			String[] enumeratorFullName = new String[parentName.length + 1];
			int pos;
			System.arraycopy(parentName, 0, enumeratorFullName, 0, pos = parentName.length);
			enumeratorFullName[pos++] = name;
			this.output.addRef(encodeEntry(enumeratorFullName,FIELD_DECL,FIELD_DECL_LENGTH));

		}
	}
	
	
	public void addVariable(IASTVariable variable) {

		this.output.addRef(encodeTypeEntry(variable.getFullyQualifiedName(), VAR));
	}
	
	public void addTypedefDeclaration(IASTTypedefDeclaration typedef) {
		this.output.addRef(encodeEntry(typedef.getFullyQualifiedName(), TYPEDEF_DECL, TYPEDEF_DECL_LENGTH));
	}
	
	public void addFieldDeclaration(IASTField field) {
		this.output.addRef(encodeEntry(field.getFullyQualifiedName(),FIELD_DECL,FIELD_DECL_LENGTH));
	}
	
	public void addMethodDeclaration(IASTMethod method) {
		this.output.addRef(encodeEntry(method.getFullyQualifiedName(),METHOD_DECL,METHOD_DECL_LENGTH));
	}
	
	public void addConstructorDeclaration(){
		
	}
	public void addConstructorReference(){

	}
	
	public void addMemberDeclaration(){
	
	}
	public void addMemberReference(){
	
	}

	public void addFunctionDeclaration(IASTFunction function){
		this.output.addRef(encodeEntry(function.getFullyQualifiedName(),FUNCTION_DECL,FUNCTION_DECL_LENGTH));
	}
	
	public void addFunctionReference(){
	
	}
	
	public void addNameReference(){
		
	}
	
	public void addNamespaceDefinition(IASTNamespaceDefinition namespace){
		this.output.addRef(encodeEntry(namespace.getFullyQualifiedName(),NAMESPACE_DECL,NAMESPACE_DECL_LENGTH));
	}

	private void addSuperTypeReference(int modifiers, char[] packageName, char[] typeName, char[][] enclosingTypeNames, char classOrInterface, char[] superTypeName, char superClassOrInterface){

	}
	
	public void addTypeReference(char[] typeName){
		//this.output.addRef(CharOperation.concat(TYPE_REF, CharOperation.lastSegment(typeName, '.')));
	}
	/**
	 * Type entries are encoded as follow: 'typeDecl/' ('C' | 'S' | 'U' | 'E' ) '/'  TypeName ['/' Qualifier]* 
	 */
	 protected static final char[] encodeTypeEntry( String [] fullTypeName, int typeType) {
		int pos, nameLength = 0;
		for (int i=0; i<fullTypeName.length; i++){
			String namePart = fullTypeName[i];
			nameLength+= namePart.length();
		}
		//char[] has to be of size - [type decl length + length of the name + separators + letter]
		char[] result = new char[TYPE_DECL_LENGTH + nameLength + fullTypeName.length + 1 ];
		System.arraycopy(TYPE_DECL, 0, result, 0, pos = TYPE_DECL_LENGTH);
		switch (typeType)
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
			
			case(ENUM):
			result[pos++] = ENUM_SUFFIX;
			break;
			
			case (VAR):
			result[pos++] = VAR_SUFFIX;
			break;
		}
		result[pos++] = SEPARATOR;
		//Encode in the following manner
		//	[typeDecl info]/[typeName]/[qualifiers]
		if (fullTypeName.length > 0){
		//Extract the name first
			char [] tempName = fullTypeName[fullTypeName.length-1].toCharArray();
			System.arraycopy(tempName, 0, result, pos, tempName.length);
			pos += tempName.length;
		}
		//Extract the qualifiers
		for (int i=0; i<(fullTypeName.length - 1); i++){
			result[pos++] = SEPARATOR;
			char [] tempName = fullTypeName[i].toCharArray();
			System.arraycopy(tempName, 0, result, pos, tempName.length);
			pos+=tempName.length;				
		}
		return result;
	}
	/**
	 * Namespace entries are encoded as follow: '[prefix]/' TypeName ['/' Qualifier]*
	 */
	protected static final char[] encodeEntry(String[] elementName, char[] prefix, int prefixSize) {
		int pos, nameLength = 0;
		for (int i=0; i<elementName.length; i++){
			String namePart = elementName[i];
			nameLength+= namePart.length();
		}
		//char[] has to be of size - [type length + length of the name (including qualifiers) + 
		//separators (need one less than fully qualified name length)
		char[] result = new char[prefixSize + nameLength + elementName.length - 1 ];
		System.arraycopy(prefix, 0, result, 0, pos = prefix.length);
		if (elementName.length > 0){
		//Extract the name first
			char [] tempName = elementName[elementName.length-1].toCharArray();
			System.arraycopy(tempName, 0, result, pos, tempName.length);
			pos += tempName.length;
		}
		//Extract the qualifiers
		for (int i=0; i<(elementName.length - 1); i++){
			result[pos++] = SEPARATOR;
			char [] tempName = elementName[i].toCharArray();
			System.arraycopy(tempName, 0, result, pos, tempName.length);
			pos+=tempName.length;				
		}
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
	/**
	 * Type entries are encoded as follow: 'typeDecl/' ('C' | 'S' | 'U' ) '/'  TypeName '/' 
	 * Current encoding is optimized for queries: all classes
	 */
	 public static final char[] bestTypeDeclarationPrefix(char[] typeName, char[][] containingTypes, ASTClassKind classKind, int matchMode, boolean isCaseSensitive) {
		// index is case sensitive, thus in case attempting case insensitive search, cannot consider
		// type name.
		if (!isCaseSensitive){
			typeName = null;
		}
		//Class kind not provided, best we can do
		if (classKind == null){
			return TYPE_DECL;
		}
		
		char classType=CLASS_SUFFIX;
		if (classKind == ASTClassKind.STRUCT){
			classType = STRUCT_SUFFIX;
		}
		else if (classKind == ASTClassKind.UNION){
			classType = UNION_SUFFIX;
		}
		
		switch(matchMode){
			case EXACT_MATCH :
			case PREFIX_MATCH :
				break;
			case PATTERN_MATCH :
				if (typeName != null){
					int starPos = CharOperation.indexOf('*', typeName);
					switch(starPos) {
						case -1 :
							break;
						case 0 :
							typeName = null;
							break;
						default : 
							typeName = CharOperation.subarray(typeName, 0, starPos);
					}
				}
		}
		
		int containingTypesLength=0; 
		int typeLength = typeName == null ? 0 : typeName.length;
		int pos;
		//figure out the total length of the qualifiers
		for (int i=0; i<containingTypes.length; i++){
			containingTypesLength+= containingTypes[i].length;
		}
		//typed decl length + length of qualifiers + qualifier separators + name length + 2 (1 for name separator, 1 for letter)
		char[] result = new char[TYPE_DECL_LENGTH + containingTypesLength + containingTypes.length + typeLength + 2 ];
		System.arraycopy(TYPE_DECL, 0, result, 0, pos = TYPE_DECL_LENGTH);
		result[pos++] = classType;
		result[pos++] = SEPARATOR;
		
		if (typeLength > 0){
			System.arraycopy(typeName, 0, result, pos, typeName.length);
			pos += typeName.length;
		}
				
		for (int i=0; i<containingTypes.length; i++){
			result[pos++] = SEPARATOR;
			char[] tempName = containingTypes[i];
			System.arraycopy(tempName, 0, result, pos, tempName.length);
			pos += tempName.length;
		}
		
		return result;
	}
	
	public static final char[] bestNamespacePrefix(char[] namespaceName, char[][] containingTypes, int matchMode, boolean isCaseSensitive) {
		   // index is case sensitive, thus in case attempting case insensitive search, cannot consider
		   // type name.
		   if (!isCaseSensitive){
			   namespaceName = null;
		   }
		
		   switch(matchMode){
			   case EXACT_MATCH :
			   case PREFIX_MATCH :
				   break;
			   case PATTERN_MATCH :
				   if (namespaceName != null){
					   int starPos = CharOperation.indexOf('*', namespaceName);
					   switch(starPos) {
						   case -1 :
							   break;
						   case 0 :
							   namespaceName = null;
							   break;
						   default : 
							   namespaceName = CharOperation.subarray(namespaceName, 0, starPos);
					   }
				   }
		   }
		
		   int containingTypesLength=0; 
		   int typeLength = namespaceName == null ? 0 : namespaceName.length;
		   int pos;
		   //figure out the total length of the qualifiers
		   for (int i=0; i<containingTypes.length; i++){
			   containingTypesLength+= containingTypes[i].length;
		   }
		   //typed decl length + length of qualifiers + qualifier separators + name length + 2 (1 for name separator, 1 for letter)
		   char[] result = new char[TYPE_DECL_LENGTH + containingTypesLength + containingTypes.length + typeLength + 2 ];
		   System.arraycopy(TYPE_DECL, 0, result, 0, pos = TYPE_DECL_LENGTH);
		   //result[pos++] = classType;
		   result[pos++] = SEPARATOR;
		
		   if (typeLength > 0){
			   System.arraycopy(namespaceName, 0, result, pos, namespaceName.length);
			   pos += namespaceName.length;
		   }
				
		   for (int i=0; i<containingTypes.length; i++){
			   result[pos++] = SEPARATOR;
			   char[] tempName = containingTypes[i];
			   System.arraycopy(tempName, 0, result, pos, tempName.length);
			   pos += tempName.length;
		   }
		
		   return result;
	   }

}

