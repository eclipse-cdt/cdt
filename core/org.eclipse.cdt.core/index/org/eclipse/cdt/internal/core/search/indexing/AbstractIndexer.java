/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.filetype.ICFileType;
import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.cdt.core.parser.ast.IASTBaseSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTEnumerator;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTInclusion;
import org.eclipse.cdt.core.parser.ast.IASTMacro;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.internal.core.CharOperation;
import org.eclipse.cdt.internal.core.index.IDocument;
import org.eclipse.cdt.internal.core.index.IIndexer;
import org.eclipse.cdt.internal.core.index.IIndexerOutput;
import org.eclipse.core.resources.IFile;

public abstract class AbstractIndexer implements IIndexer, IIndexConstants, ICSearchConstants {
	
	IIndexerOutput output;
	final static int CLASS = 1;
	final static int STRUCT = 2;
	final static int UNION = 3;
	final static int ENUM = 4;
	final static int VAR = 5;
	final static int TYPEDEF = 6;
	final static int DERIVED = 7;
	final static int FRIEND = 8;
	
	public static boolean VERBOSE = false;
	
	//IDs defined in plugin.xml for file types
	private final static String C_SOURCE_ID = "org.eclipse.cdt.core.fileType.c_source"; //$NON-NLS-1$
	private final static String C_HEADER_ID = "org.eclipse.cdt.core.fileType.c_header"; //$NON-NLS-1$
	private final static String CPP_SOURCE_ID = "org.eclipse.cdt.core.fileType.cxx_source"; //$NON-NLS-1$
	private final static String CPP_HEADER_ID = "org.eclipse.cdt.core.fileType.cxx_header"; //$NON-NLS-1$
	
	public AbstractIndexer() {
		super();
	}
	
	public static void verbose(String log) {
	  System.out.println("(" + Thread.currentThread() + ") " + log); //$NON-NLS-1$//$NON-NLS-2$
	}
	
	public void addClassSpecifier(IASTClassSpecifier classSpecification){

		if (classSpecification.getClassKind().equals(ASTClassKind.CLASS))
		{
			//Get base clauses
			Iterator baseClauses = classSpecification.getBaseClauses();
			while (baseClauses.hasNext()){
				IASTBaseSpecifier baseSpec = (IASTBaseSpecifier) baseClauses.next();
				try {
					IASTTypeSpecifier typeSpec =  baseSpec.getParentClassSpecifier();
					if (typeSpec instanceof IASTClassSpecifier){
						IASTClassSpecifier baseClassSpec = (IASTClassSpecifier) typeSpec;
						String[] baseFullyQualifiedName = baseClassSpec.getFullyQualifiedName();
						this.output.addRef(encodeTypeEntry(baseFullyQualifiedName,DERIVED,ICSearchConstants.DECLARATIONS));
					}
				} catch (ASTNotImplementedException e) {}
			}
			
			//Get friends
			Iterator friends = classSpecification.getFriends();
			while (friends.hasNext()){
				Object decl = friends.next();
				if (decl instanceof IASTClassSpecifier){
					IASTClassSpecifier friendClassSpec = (IASTClassSpecifier) decl;
					String[] baseFullyQualifiedName = friendClassSpec.getFullyQualifiedName();
					this.output.addRef(encodeTypeEntry(baseFullyQualifiedName,FRIEND,ICSearchConstants.DECLARATIONS));
				}
				else if (decl instanceof IASTElaboratedTypeSpecifier){
					IASTElaboratedTypeSpecifier friendClassSpec = (IASTElaboratedTypeSpecifier) decl;
					String[] baseFullyQualifiedName = friendClassSpec.getFullyQualifiedName();
					this.output.addRef(encodeTypeEntry(baseFullyQualifiedName,FRIEND,ICSearchConstants.DECLARATIONS));
				}
				else if (decl instanceof IASTFunction){
					
				}
				else if (decl instanceof IASTMethod){
					//
				}
				
			}
			
			this.output.addRef(encodeTypeEntry(classSpecification.getFullyQualifiedName(),CLASS, ICSearchConstants.DECLARATIONS));
		}		
		else if (classSpecification.getClassKind().equals(ASTClassKind.STRUCT))
		{
			//Get base clauses
			Iterator i = classSpecification.getBaseClauses();
			while (i.hasNext()){
				IASTBaseSpecifier baseSpec = (IASTBaseSpecifier) i.next();
				try {
					IASTTypeSpecifier typeSpec =  baseSpec.getParentClassSpecifier();
					if (typeSpec instanceof IASTClassSpecifier){
						IASTClassSpecifier baseClassSpec = (IASTClassSpecifier) typeSpec;
						String[] baseFullyQualifiedName = baseClassSpec.getFullyQualifiedName();
						this.output.addRef(encodeTypeEntry(baseFullyQualifiedName,DERIVED,ICSearchConstants.DECLARATIONS));
					}
				} catch (ASTNotImplementedException e) {}
			}
			
//			Get friends
			Iterator friends = classSpecification.getFriends();
			while (friends.hasNext()){
				Object decl = friends.next();
				if (decl instanceof IASTClassSpecifier){
					IASTClassSpecifier friendClassSpec = (IASTClassSpecifier) decl;
					String[] baseFullyQualifiedName = friendClassSpec.getFullyQualifiedName();
					this.output.addRef(encodeTypeEntry(baseFullyQualifiedName,FRIEND,ICSearchConstants.DECLARATIONS));
				}
				else if (decl instanceof IASTElaboratedTypeSpecifier){
					IASTElaboratedTypeSpecifier friendClassSpec = (IASTElaboratedTypeSpecifier) decl;
					String[] baseFullyQualifiedName = friendClassSpec.getFullyQualifiedName();
					this.output.addRef(encodeTypeEntry(baseFullyQualifiedName,FRIEND,ICSearchConstants.DECLARATIONS));
				}
				else if (decl instanceof IASTFunction){
					
				}
				else if (decl instanceof IASTMethod){
					//
				}
			}
			
			this.output.addRef(encodeTypeEntry(classSpecification.getFullyQualifiedName(),STRUCT, ICSearchConstants.DECLARATIONS));
		}
		else if (classSpecification.getClassKind().equals(ASTClassKind.UNION))
		{
			this.output.addRef(encodeTypeEntry(classSpecification.getFullyQualifiedName(),UNION, ICSearchConstants.DECLARATIONS));			
		}
	}
	
	public void addEnumerationSpecifier(IASTEnumerationSpecifier enumeration) {
		this.output.addRef(encodeTypeEntry(enumeration.getFullyQualifiedName(), ENUM, ICSearchConstants.DECLARATIONS));
		
		Iterator i = enumeration.getEnumerators();
		while (i.hasNext())
		{
			IASTEnumerator en = (IASTEnumerator) i.next(); 	
			String[] enumeratorFullName =
				createEnumeratorFullyQualifiedName(en);
			
			this.output.addRef(encodeEntry( enumeratorFullName, ENUMTOR_DECL, ENUMTOR_DECL_LENGTH ));

		}
	}

	protected String[] createEnumeratorFullyQualifiedName(IASTEnumerator en) {
		String name = en.getName();
		IASTEnumerationSpecifier parent = en.getOwnerEnumerationSpecifier();
		String[] parentName = parent.getFullyQualifiedName();
		
		//See spec 7.2-10, the the scope of the enumerator is the same level as the enumeration
		String[] enumeratorFullName = new String[ parentName.length ];
		
		System.arraycopy( parentName, 0, enumeratorFullName, 0, parentName.length);
		enumeratorFullName[ parentName.length - 1 ] = name;
		return enumeratorFullName;
	}

	public void addEnumeratorReference(IASTEnumerator enumerator) {
		this.output.addRef(encodeEntry(createEnumeratorFullyQualifiedName(enumerator),ENUMTOR_REF,ENUMTOR_REF_LENGTH));	
	}
		
	public void addMacro(IASTMacro macro) {
		String[] macroName = new String[1];
		macroName[0] = macro.getName();
		this.output.addRef(encodeEntry(macroName,MACRO_DECL,MACRO_DECL_LENGTH));
	}
		
	public void addEnumerationReference(IASTEnumerationSpecifier enumeration) {
		this.output.addRef(encodeTypeEntry(enumeration.getFullyQualifiedName(), ENUM, ICSearchConstants.REFERENCES));
	}
	public void addVariable(IASTVariable variable) {
		this.output.addRef(encodeTypeEntry(variable.getFullyQualifiedName(), VAR, ICSearchConstants.DECLARATIONS));
	}
	
	public void addVariableReference(IASTVariable variable) {
		this.output.addRef(encodeTypeEntry(variable.getFullyQualifiedName(), VAR, ICSearchConstants.REFERENCES));
	}	
	
	public void addParameterReference( IASTParameterDeclaration parameter ){
		this.output.addRef( encodeTypeEntry( new String [] { parameter.getName() }, VAR, ICSearchConstants.REFERENCES));
	}
	
	public void addTypedefDeclaration(IASTTypedefDeclaration typedef) {
		this.output.addRef(encodeEntry(typedef.getFullyQualifiedName(), TYPEDEF_DECL, TYPEDEF_DECL_LENGTH));
	}
	
	public void addFieldDeclaration(IASTField field) {
		this.output.addRef(encodeEntry(field.getFullyQualifiedName(),FIELD_DECL,FIELD_DECL_LENGTH));
	}
	
	public void addFieldReference(IASTField field) {
		this.output.addRef(encodeEntry(field.getFullyQualifiedName(),FIELD_REF,FIELD_REF_LENGTH));
	}
	
	public void addMethodDeclaration(IASTMethod method) {
		this.output.addRef(encodeEntry(method.getFullyQualifiedName(),METHOD_DECL,METHOD_DECL_LENGTH));
	
		Iterator i=method.getParameters();
		while (i.hasNext()){
			Object parm = i.next();
			if (parm instanceof IASTParameterDeclaration){
				IASTParameterDeclaration parmDecl = (IASTParameterDeclaration) parm;
				this.output.addRef(encodeTypeEntry(new String[]{parmDecl.getName()}, VAR, ICSearchConstants.DECLARATIONS));
			}
		}
	}
	
	public void addMethodReference(IASTMethod method) {
		this.output.addRef(encodeEntry(method.getFullyQualifiedName(),METHOD_REF,METHOD_REF_LENGTH));
	}

	public void addElaboratedForwardDeclaration(IASTElaboratedTypeSpecifier elaboratedType) {
		if (elaboratedType.getClassKind().equals(ASTClassKind.CLASS))
		{
			this.output.addRef(encodeTypeEntry(elaboratedType.getFullyQualifiedName(),CLASS, ICSearchConstants.DECLARATIONS));
		}		
		else if (elaboratedType.getClassKind().equals(ASTClassKind.STRUCT))
		{
			this.output.addRef(encodeTypeEntry(elaboratedType.getFullyQualifiedName(),STRUCT, ICSearchConstants.DECLARATIONS));
		}
		else if (elaboratedType.getClassKind().equals(ASTClassKind.UNION))
		{
			this.output.addRef(encodeTypeEntry(elaboratedType.getFullyQualifiedName(),UNION, ICSearchConstants.DECLARATIONS));			
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

	public void addFunctionDeclaration(IASTFunction function){
		this.output.addRef(encodeEntry(function.getFullyQualifiedName(),FUNCTION_DECL,FUNCTION_DECL_LENGTH));
		
		Iterator i=function.getParameters();
		while (i.hasNext()){
			Object parm = i.next();
			if (parm instanceof IASTParameterDeclaration){
				IASTParameterDeclaration parmDecl = (IASTParameterDeclaration) parm;
				this.output.addRef(encodeTypeEntry(new String[]{parmDecl.getName()}, VAR, ICSearchConstants.DECLARATIONS));
			}
		}
	}
	
	public void addFunctionReference(IASTFunction function){
		this.output.addRef(encodeEntry(function.getFullyQualifiedName(),FUNCTION_REF,FUNCTION_REF_LENGTH));
	}
	
	public void addNameReference(){
		
	}
	
	public void addNamespaceDefinition(IASTNamespaceDefinition namespace){
		this.output.addRef(encodeEntry(namespace.getFullyQualifiedName(),NAMESPACE_DECL,NAMESPACE_DECL_LENGTH));
	}
	
	public void addNamespaceReference(IASTNamespaceDefinition namespace) {
		this.output.addRef(encodeEntry(namespace.getFullyQualifiedName(),NAMESPACE_REF,NAMESPACE_REF_LENGTH));
	}
	
	public void addTypedefReference( IASTTypedefDeclaration typedef ){
		this.output.addRef( encodeTypeEntry( typedef.getFullyQualifiedName(), TYPEDEF, ICSearchConstants.REFERENCES) );
	}

	private void addSuperTypeReference(int modifiers, char[] packageName, char[] typeName, char[][] enclosingTypeNames, char classOrInterface, char[] superTypeName, char superClassOrInterface){

	}
	
	public void addTypeReference(char[] typeName){
		//this.output.addRef(CharOperation.concat(TYPE_REF, CharOperation.lastSegment(typeName, '.')));
	}
	
	public void addClassReference(IASTTypeSpecifier reference){
		String[] fullyQualifiedName = null;
		ASTClassKind classKind = null;
		
		if (reference instanceof IASTClassSpecifier){
		  IASTClassSpecifier classRef = (IASTClassSpecifier) reference;
		  fullyQualifiedName = classRef.getFullyQualifiedName();
		  classKind = classRef.getClassKind();
		}
		else if (reference instanceof IASTElaboratedTypeSpecifier){
		  IASTElaboratedTypeSpecifier typeRef = (IASTElaboratedTypeSpecifier) reference;
		  fullyQualifiedName = typeRef.getFullyQualifiedName();
		  classKind = typeRef.getClassKind();
		}
	
		if (classKind.equals(ASTClassKind.CLASS))
		{  
			this.output.addRef(encodeTypeEntry(fullyQualifiedName,CLASS, ICSearchConstants.REFERENCES));
		}		
		else if (classKind.equals(ASTClassKind.STRUCT))
		{
			this.output.addRef(encodeTypeEntry(fullyQualifiedName,STRUCT,ICSearchConstants.REFERENCES));
		}
		else if (classKind.equals(ASTClassKind.UNION))
		{
			this.output.addRef(encodeTypeEntry(fullyQualifiedName,UNION,ICSearchConstants.REFERENCES));			
		}
	}
	/**
	 * Type entries are encoded as follow: 'typeDecl/' ('C' | 'S' | 'U' | 'E' ) '/'  TypeName ['/' Qualifier]* 
	 */
	 protected static final char[] encodeTypeEntry( String [] fullTypeName, int typeType, LimitTo encodeType){ 

		int pos = 0, nameLength = 0;
		for (int i=0; i<fullTypeName.length; i++){
			String namePart = fullTypeName[i];
			nameLength+= namePart.length();
		}
		
		char [] result = null;
		if( encodeType == REFERENCES ){
			//char[] has to be of size - [type decl length + length of the name + separators + letter]
			result = new char[TYPE_REF_LENGTH + nameLength + fullTypeName.length + 1 ];
			System.arraycopy(TYPE_REF, 0, result, 0, pos = TYPE_REF_LENGTH);
		
		} else if( encodeType == DECLARATIONS ){
			//char[] has to be of size - [type decl length + length of the name + separators + letter]
			result = new char[TYPE_DECL_LENGTH + nameLength + fullTypeName.length + 1 ];
			System.arraycopy(TYPE_DECL, 0, result, 0, pos = TYPE_DECL_LENGTH);
		}
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
			
			case (TYPEDEF):
			result[pos++] = TYPEDEF_SUFFIX;
			break;
			
			case(DERIVED):
			result[pos++]= DERIVED_SUFFIX;
			break;
			
			case(FRIEND):
			result[pos++]=FRIEND_SUFFIX;
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
		for (int i=fullTypeName.length - 2; i >= 0; i--){
			result[pos++] = SEPARATOR;
			char [] tempName = fullTypeName[i].toCharArray();
			System.arraycopy(tempName, 0, result, pos, tempName.length);
			pos+=tempName.length;				
		}
		
		if (AbstractIndexer.VERBOSE)
			AbstractIndexer.verbose(new String(result));
			
		return result;
	}
	/**
	 * Namespace entries are encoded as follow: '[prefix]/' TypeName ['/' Qualifier]*
	 */
	protected static final char[] encodeEntry(String[] elementName, char[] prefix, int prefixSize){ 
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
		for (int i=elementName.length - 2; i>=0; i--){
			result[pos++] = SEPARATOR;
			char [] tempName = elementName[i].toCharArray();
			System.arraycopy(tempName, 0, result, pos, tempName.length);
			pos+=tempName.length;				
		}
		
		if (AbstractIndexer.VERBOSE)
			AbstractIndexer.verbose(new String(result));
			
		return result;
	}
	
	/**
	 * Returns the file types being indexed.
	 */
	public abstract IFile getResourceFile();
	/**
	 * @see IIndexer#index(IDocument document, IIndexerOutput output)
	 */
	public void index(IDocument document, IIndexerOutput output) throws IOException {
		this.output = output;
		if (shouldIndex(this.getResourceFile())) indexFile(document);
	} 
	
	protected abstract void indexFile(IDocument document) throws IOException;
	/**
	 * @param fileToBeIndexed
	 * @see IIndexer#shouldIndex(IFile file)
	 */
	public boolean shouldIndex(IFile fileToBeIndexed) {
		if (fileToBeIndexed != null){
			ICFileType type = CCorePlugin.getDefault().getFileType(fileToBeIndexed.getProject(),fileToBeIndexed.getName());
			if (type.isSource() || type.isHeader()){
			  String id = type.getId();
			  if (id.equals(AbstractIndexer.C_SOURCE_ID) ||
			  	  id.equals(AbstractIndexer.CPP_SOURCE_ID) ||
				  id.equals(AbstractIndexer.C_HEADER_ID) ||
				  id.equals(AbstractIndexer.CPP_HEADER_ID))
			  	return true;
		}
		}
		
		return false;
	}
	/**
	 * Type entries are encoded as follow: 'typeDecl/' ('C' | 'S' | 'U' ) '/'  TypeName '/' 
	 * Current encoding is optimized for queries: all classes
	 */
	public static final char[] bestTypePrefix( SearchFor searchFor, LimitTo limitTo, char[] typeName, char[][] containingTypes, int matchMode, boolean isCaseSensitive) {
		char [] prefix = null;
		if( limitTo == DECLARATIONS ){
			prefix = TYPE_DECL;
		} else if( limitTo == REFERENCES ){
			prefix = TYPE_REF;
		} else {
			return TYPE_ALL;
		}
					
		char classType = 0;
		
		if( searchFor == ICSearchConstants.CLASS ){
			classType = CLASS_SUFFIX;
		} else if ( searchFor == ICSearchConstants.STRUCT ){
			classType = STRUCT_SUFFIX;
		} else if ( searchFor == ICSearchConstants.UNION ){
			classType = UNION_SUFFIX;
		} else if ( searchFor == ICSearchConstants.ENUM ){
			classType = ENUM_SUFFIX;
		} else if ( searchFor == ICSearchConstants.TYPEDEF ){
			classType = TYPEDEF_SUFFIX;
		} else if ( searchFor == ICSearchConstants.DERIVED){
			classType = DERIVED_SUFFIX;
		} else if ( searchFor == ICSearchConstants.FRIEND){
			classType = FRIEND_SUFFIX;
		} else {
			//could be TYPE or CLASS_STRUCT, best we can do for these is the prefix
			return prefix;
		}
		
		return bestPrefix( prefix, classType, typeName, containingTypes, matchMode, isCaseSensitive );
	}
	
	public static final char[] bestNamespacePrefix(LimitTo limitTo, char[] namespaceName, char[][] containingTypes, int matchMode, boolean isCaseSensitive) {
		char [] prefix = null;
		if( limitTo == REFERENCES ){
			prefix = NAMESPACE_REF;
		} else if ( limitTo == DECLARATIONS ) {
			prefix = NAMESPACE_DECL;
		} else {
			return NAMESPACE_ALL;
		}
		
		return bestPrefix( prefix, (char) 0, namespaceName, containingTypes, matchMode, isCaseSensitive );
	}	
		
	public static final char[] bestVariablePrefix( LimitTo limitTo, char[] varName, char[][] containingTypes, int matchMode, boolean isCaseSenstive ){
		char [] prefix = null;
		if( limitTo == REFERENCES ){
			prefix = TYPE_REF;
		} else if( limitTo == DECLARATIONS ){
			prefix = TYPE_DECL;
		} else {
			return TYPE_ALL;
		}
		
		return bestPrefix( prefix, VAR_SUFFIX, varName, containingTypes, matchMode, isCaseSenstive );	
	}

	public static final char[] bestFieldPrefix( LimitTo limitTo, char[] fieldName,char[][] containingTypes, int matchMode, boolean isCaseSensitive) {
		char [] prefix = null;
		if( limitTo == REFERENCES ){
			prefix = FIELD_REF;
		} else if( limitTo == DECLARATIONS ){
			prefix = FIELD_DECL;
		} else {
			return FIELD_ALL;
		}
		
		return bestPrefix( prefix, (char)0, fieldName, containingTypes, matchMode, isCaseSensitive );
	}  
	
	public static final char[] bestEnumeratorPrefix( LimitTo limitTo, char[] enumeratorName,char[][] containingTypes, int matchMode, boolean isCaseSensitive) {
		char [] prefix = null;
		if( limitTo == REFERENCES ){
			prefix = ENUMTOR_REF;
		} else if( limitTo == DECLARATIONS ){
			prefix = ENUMTOR_DECL;
		} else if (limitTo == ALL_OCCURRENCES){
			return ENUMTOR_ALL;
		}
		else {
			//Definitions
			return "noEnumtorDefs".toCharArray(); //$NON-NLS-1$
		}
		
		return bestPrefix( prefix, (char)0, enumeratorName, containingTypes, matchMode, isCaseSensitive );
	}  

	public static final char[] bestMethodPrefix( LimitTo limitTo, char[] methodName,char[][] containingTypes, int matchMode, boolean isCaseSensitive) {
		char [] prefix = null;
		if( limitTo == REFERENCES ){
			prefix = METHOD_REF;
		} else if( limitTo == DECLARATIONS ){
			prefix = METHOD_DECL;
		} else if( limitTo == DEFINITIONS ){
			//TODO prefix = METHOD_DEF;
			return METHOD_ALL;	
		} else {
			return METHOD_ALL;
		}
		
		return bestPrefix( prefix, (char)0, methodName, containingTypes, matchMode, isCaseSensitive );
	}  
	
	public static final char[] bestFunctionPrefix( LimitTo limitTo, char[] functionName, int matchMode, boolean isCaseSensitive) {
		char [] prefix = null;
		if( limitTo == REFERENCES ){
			prefix = FUNCTION_REF;
		} else if( limitTo == DECLARATIONS ){
			prefix = FUNCTION_DECL;
		} else if ( limitTo == DEFINITIONS ){
			//TODO prefix = FUNCTION_DEF;
			return FUNCTION_ALL;
		} else {
			return FUNCTION_ALL;
		}
		return bestPrefix( prefix, (char)0, functionName, null, matchMode, isCaseSensitive );
	}  
		
	public static final char[] bestPrefix( char [] prefix, char optionalType, char[] name, char[][] containingTypes, int matchMode, boolean isCaseSensitive) {
		char[] 	result = null;
		int 	pos    = 0;
		
		int wildPos, starPos = -1, questionPos;
		
		//length of prefix + separator
		int length = prefix.length;
		
		//add length for optional type + another separator
		if( optionalType != 0 )
			length += 2;
		
		if (!isCaseSensitive){
			//index is case sensitive, thus in case attempting case insensitive search, cannot consider
			//type name.
			name = null;
		} else if( matchMode == PATTERN_MATCH && name != null ){
			int start = 0;

			char [] temp = new char [ name.length ];
			boolean isEscaped = false;
			int tmpIdx = 0;
			for( int i = 0; i < name.length; i++ ){
				if( name[i] == '\\' ){
					if( !isEscaped ){
						isEscaped = true;
						continue;
					} 
					isEscaped = false;		
				} else if( name[i] == '*' && !isEscaped ){
					starPos = i;
					break;
				} 
				temp[ tmpIdx++ ] = name[i];
			}
			
			name = new char [ tmpIdx ];
			System.arraycopy( temp, 0, name, 0, tmpIdx );				
		
			//starPos = CharOperation.indexOf( '*', name );
			questionPos = CharOperation.indexOf( '?', name );

			if( starPos >= 0 ){
				if( questionPos >= 0 )
					wildPos = ( starPos < questionPos ) ? starPos : questionPos;
				else 
					wildPos = starPos;
			} else {
				wildPos = questionPos;
			}
			 
			switch( wildPos ){
				case -1 : break;
				case 0  : name = null;	break;
				default : name = CharOperation.subarray( name, 0, wildPos ); break;
			}
		}
		//add length for name
		if( name != null ){
			length += name.length;
		} else {
			//name is null, don't even consider qualifications.
			result = new char [ length ];
			System.arraycopy( prefix, 0, result, 0, pos = prefix.length );
			if( optionalType != 0){
				result[ pos++ ] = optionalType;
				result[ pos++ ] = SEPARATOR; 
			}
			return result;
		}
		 		
		//add the total length of the qualifiers
		//we don't want to mess with the contents of this array (treat it as constant)
		//so check for wild cards later.
		if( containingTypes != null ){
			for( int i = 0; i < containingTypes.length; i++ ){
				if( containingTypes[i].length > 0 ){
					length += containingTypes[ i ].length;
					length++; //separator
				}
			}
		}
		
		//because we haven't checked qualifier wild cards yet, this array might turn out
		//to be too long. So fill a temp array, then check the length after
		char [] temp = new char [ length ];
		
		System.arraycopy( prefix, 0, temp, 0, pos = prefix.length );
		
		if( optionalType != 0 ){
			temp[ pos++ ] = optionalType;
			temp[ pos++ ] = SEPARATOR;
		}
		
		System.arraycopy( name, 0, temp, pos, name.length );
		pos += name.length;
		
		if( containingTypes != null ){
			for( int i = containingTypes.length - 1; i >= 0; i-- ){
				if( matchMode == PATTERN_MATCH ){
					starPos     = CharOperation.indexOf( '*', containingTypes[i] );
					questionPos = CharOperation.indexOf( '?', containingTypes[i] );

					if( starPos >= 0 ){
						if( questionPos >= 0 )
							wildPos = ( starPos < questionPos ) ? starPos : questionPos;
						else 
							wildPos = starPos;
					} else {
						wildPos = questionPos;
					}
					
					if( wildPos >= 0 ){
						temp[ pos++ ] = SEPARATOR;
						System.arraycopy( containingTypes[i], 0, temp, pos, wildPos );
						pos += starPos;
						break;
					}
				}
				
				if( containingTypes[i].length > 0 ){
					temp[ pos++ ] = SEPARATOR;
					System.arraycopy( containingTypes[i], 0, temp, pos, containingTypes[i].length );
					pos += containingTypes[i].length;
				}
			}
		}
	
		if( pos < length ){
			result = new char[ pos ];
			System.arraycopy( temp, 0, result, 0, pos );	
		} else {
			result = temp;
		}
		
		return result;
	}

	/**
	 * @param _limitTo
	 * @param simpleName
	 * @param _matchMode
	 * @param _caseSensitive
	 * @return
	 */
	public static final char[] bestMacroPrefix( LimitTo limitTo, char[] macroName, int matchMode, boolean isCaseSenstive ){
		//since we only index macro declarations we already know the prefix
		char [] prefix = null;
		if( limitTo == DECLARATIONS ){
			prefix = MACRO_DECL;
		} else {
			return null;
		}
		
		return bestPrefix( prefix,  (char)0, macroName, null, matchMode, isCaseSenstive );	
	}
	
	/**
	 * @param _limitTo
	 * @param simpleName
	 * @param _matchMode
	 * @param _caseSensitive
	 * @return
	 */
	public static final char[] bestIncludePrefix( LimitTo limitTo, char[] incName, int matchMode, boolean isCaseSenstive ){
		//since we only index macro declarations we already know the prefix
		char [] prefix = null;
		if( limitTo == REFERENCES ){
			prefix = INCLUDE_REF;
		} else {
			return null;
		}
		
		return bestPrefix( prefix,  (char)0, incName, null, matchMode, isCaseSenstive );	
	}
	
	public void addInclude(IASTInclusion inclusion, IASTInclusion parent){
		this.output.addIncludeRef(inclusion.getFullFileName());
		this.output.addRelatives(inclusion.getFullFileName(),(parent != null ) ? parent.getFullFileName() : null);
		//Add Dep Table entry
		String[] incName = new String[1];
		incName[0] = inclusion.getFullFileName();
		this.output.addRef(encodeEntry(incName, INCLUDE_REF, INCLUDE_REF_LENGTH));
	}
}

