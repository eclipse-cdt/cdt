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

package org.eclipse.cdt.internal.core.index.sourceindexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.filetype.ICFileType;
import org.eclipse.cdt.core.filetype.ICFileTypeConstants;
import org.eclipse.cdt.core.model.ICModelMarker;
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
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.index.IDocument;
import org.eclipse.cdt.internal.core.index.IIndexer;
import org.eclipse.cdt.internal.core.index.IIndexerOutput;
import org.eclipse.cdt.internal.core.search.indexing.IIndexConstants;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public abstract class AbstractIndexer implements IIndexer,IIndexConstants, ICSearchConstants {
	
	final static int CLASS = 1;
	final static int STRUCT = 2;
	final static int UNION = 3;
	final static int ENUM = 4;
	final static int VAR = 5;
	final static int TYPEDEF = 6;
	final static int DERIVED = 7;
	final static int FRIEND = 8;
	final static int FWD_CLASS = 9;
	final static int FWD_STRUCT = 10;
	final static int FWD_UNION = 11;
	
	public static boolean VERBOSE = false;
	public static boolean TIMING = false;
	
	protected IIndexerOutput output;

	//Index Markers
	private int problemMarkersEnabled = 0;
	private Map problemsMap = null;
	protected static final String INDEXER_MARKER_PREFIX = Util.bind("indexerMarker.prefix" ) + " "; //$NON-NLS-1$ //$NON-NLS-2$
    protected static final String INDEXER_MARKER_ORIGINATOR =  ICModelMarker.INDEXER_MARKER + ".originator";  //$NON-NLS-1$
    private static final String INDEXER_MARKER_PROCESSING = Util.bind( "indexerMarker.processing" ); //$NON-NLS-1$
	
	public AbstractIndexer() {
		super();
	}
	
	public static void verbose(String log) {
	  System.out.println("(" + Thread.currentThread() + ") " + log); //$NON-NLS-1$//$NON-NLS-2$
	}
	
	public IIndexerOutput getOutput() {
	    return output;
	}
	    
	  
	public void addClassSpecifier(IASTClassSpecifier classSpecification, int indexFlag){

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
						char[][] baseFullyQualifiedName = baseClassSpec.getFullyQualifiedNameCharArrays();
						this.output.addRef(encodeTypeEntry(baseFullyQualifiedName,DERIVED,ICSearchConstants.DECLARATIONS),indexFlag);
					}
				} catch (ASTNotImplementedException e) {}
			}
			
			//Get friends
			Iterator friends = classSpecification.getFriends();
			while (friends.hasNext()){
				Object decl = friends.next();
				if (decl instanceof IASTClassSpecifier){
					IASTClassSpecifier friendClassSpec = (IASTClassSpecifier) decl;
					char[][] baseFullyQualifiedName = friendClassSpec.getFullyQualifiedNameCharArrays();
					this.output.addRef(encodeTypeEntry(baseFullyQualifiedName,FRIEND,ICSearchConstants.DECLARATIONS),indexFlag);
				}
				else if (decl instanceof IASTElaboratedTypeSpecifier){
					IASTElaboratedTypeSpecifier friendClassSpec = (IASTElaboratedTypeSpecifier) decl;
					char[][] baseFullyQualifiedName = friendClassSpec.getFullyQualifiedNameCharArrays();
					this.output.addRef(encodeTypeEntry(baseFullyQualifiedName,FRIEND,ICSearchConstants.DECLARATIONS),indexFlag);
				}
				else if (decl instanceof IASTFunction){
					
				}
				else if (decl instanceof IASTMethod){
					//
				}
				
			}
			
			this.output.addRef(encodeTypeEntry(classSpecification.getFullyQualifiedNameCharArrays(),CLASS, ICSearchConstants.DECLARATIONS),indexFlag);
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
						char[][] baseFullyQualifiedName = baseClassSpec.getFullyQualifiedNameCharArrays();
						this.output.addRef(encodeTypeEntry(baseFullyQualifiedName,DERIVED,ICSearchConstants.DECLARATIONS),indexFlag);
					}
				} catch (ASTNotImplementedException e) {}
			}
			
//			Get friends
			Iterator friends = classSpecification.getFriends();
			while (friends.hasNext()){
				Object decl = friends.next();
				if (decl instanceof IASTClassSpecifier){
					IASTClassSpecifier friendClassSpec = (IASTClassSpecifier) decl;
					char[][] baseFullyQualifiedName = friendClassSpec.getFullyQualifiedNameCharArrays();
					this.output.addRef(encodeTypeEntry(baseFullyQualifiedName,FRIEND,ICSearchConstants.DECLARATIONS),indexFlag);
				}
				else if (decl instanceof IASTElaboratedTypeSpecifier){
					IASTElaboratedTypeSpecifier friendClassSpec = (IASTElaboratedTypeSpecifier) decl;
					char[][] baseFullyQualifiedName = friendClassSpec.getFullyQualifiedNameCharArrays();
					this.output.addRef(encodeTypeEntry(baseFullyQualifiedName,FRIEND,ICSearchConstants.DECLARATIONS),indexFlag);
				}
				else if (decl instanceof IASTFunction){
					
				}
				else if (decl instanceof IASTMethod){
					//
				}
			}
			
			this.output.addRef(encodeTypeEntry(classSpecification.getFullyQualifiedNameCharArrays(),STRUCT, ICSearchConstants.DECLARATIONS),indexFlag);
		}
		else if (classSpecification.getClassKind().equals(ASTClassKind.UNION))
		{
			this.output.addRef(encodeTypeEntry(classSpecification.getFullyQualifiedNameCharArrays(),UNION, ICSearchConstants.DECLARATIONS),indexFlag);			
		}
	}
	
	public void addEnumerationSpecifier(IASTEnumerationSpecifier enumeration, int indexFlag) {
		this.output.addRef(encodeTypeEntry(enumeration.getFullyQualifiedNameCharArrays(), ENUM, ICSearchConstants.DECLARATIONS),indexFlag);
		
		Iterator i = enumeration.getEnumerators();
		while (i.hasNext())
		{
			IASTEnumerator en = (IASTEnumerator) i.next(); 	
			char[][] enumeratorFullName =
				createEnumeratorFullyQualifiedName(en);

			this.output.addRef(encodeEntry( enumeratorFullName, ENUMTOR_DECL, ENUMTOR_DECL_LENGTH ),indexFlag);

		}
	}

	protected char[][] createEnumeratorFullyQualifiedName(IASTEnumerator en) {
		char[] name = en.getNameCharArray();
		IASTEnumerationSpecifier parent = en.getOwnerEnumerationSpecifier();
		char[][] parentName = parent.getFullyQualifiedNameCharArrays();
		
		//See spec 7.2-10, the the scope of the enumerator is the same level as the enumeration
		char[][] enumeratorFullName = new char[parentName.length][];
		
		System.arraycopy( parentName, 0, enumeratorFullName, 0, parentName.length);
		enumeratorFullName[ parentName.length - 1 ] = name;
		return enumeratorFullName;
	}

	public void addEnumeratorReference(IASTEnumerator enumerator, int indexFlag) {
		this.output.addRef(encodeEntry(createEnumeratorFullyQualifiedName(enumerator),ENUMTOR_REF,ENUMTOR_REF_LENGTH),indexFlag);	
	}
		
	public void addMacro(IASTMacro macro, int indexFlag) {
		char[][] macroName = new char[][] { macro.getNameCharArray() };
		this.output.addRef(encodeEntry(macroName,MACRO_DECL,MACRO_DECL_LENGTH),indexFlag);
	}
		
	public void addEnumerationReference(IASTEnumerationSpecifier enumeration, int indexFlag) {
		this.output.addRef(encodeTypeEntry(enumeration.getFullyQualifiedNameCharArrays(), ENUM, ICSearchConstants.REFERENCES),indexFlag);
	}
	public void addVariable(IASTVariable variable, int indexFlag) {
		this.output.addRef(encodeTypeEntry(variable.getFullyQualifiedNameCharArrays(), VAR, ICSearchConstants.DECLARATIONS),indexFlag);
	}
	
	public void addVariableReference(IASTVariable variable, int indexFlag) {
		this.output.addRef(encodeTypeEntry(variable.getFullyQualifiedNameCharArrays(), VAR, ICSearchConstants.REFERENCES),indexFlag);
	}	
	
	public void addParameterReference( IASTParameterDeclaration parameter, int indexFlag ){
		this.output.addRef( encodeTypeEntry( new char[][] { parameter.getNameCharArray() }, VAR, ICSearchConstants.REFERENCES),indexFlag);
	}
	
	public void addTypedefDeclaration(IASTTypedefDeclaration typedef, int indexFlag) {
		this.output.addRef(encodeEntry(typedef.getFullyQualifiedNameCharArrays(), TYPEDEF_DECL, TYPEDEF_DECL_LENGTH),indexFlag);
	}
	
	public void addFieldDeclaration(IASTField field, int indexFlag) {
		this.output.addRef(encodeEntry(field.getFullyQualifiedNameCharArrays(),FIELD_DECL,FIELD_DECL_LENGTH),indexFlag);
	}
	
	public void addFieldReference(IASTField field, int indexFlag) {
		this.output.addRef(encodeEntry(field.getFullyQualifiedNameCharArrays(),FIELD_REF,FIELD_REF_LENGTH),indexFlag);
	}
	
	public void addMethodDeclaration(IASTMethod method, int indexFlag) {
		this.output.addRef(encodeEntry(method.getFullyQualifiedNameCharArrays(),METHOD_DECL,METHOD_DECL_LENGTH),indexFlag);
	
		Iterator i=method.getParameters();
		while (i.hasNext()){
			Object parm = i.next();
			if (parm instanceof IASTParameterDeclaration){
				IASTParameterDeclaration parmDecl = (IASTParameterDeclaration) parm;
				this.output.addRef(encodeTypeEntry(new char[][]{parmDecl.getNameCharArray()}, VAR, ICSearchConstants.DECLARATIONS),indexFlag);
			}
		}
	}
	
	public void addMethodReference(IASTMethod method, int indexFlag) {
		this.output.addRef(encodeEntry(method.getFullyQualifiedNameCharArrays(),METHOD_REF,METHOD_REF_LENGTH),indexFlag);
	}

	public void addElaboratedForwardDeclaration(IASTElaboratedTypeSpecifier elaboratedType, int indexFlag) {
		if (elaboratedType.getClassKind().equals(ASTClassKind.CLASS))
		{
			this.output.addRef(encodeTypeEntry(elaboratedType.getFullyQualifiedNameCharArrays(),FWD_CLASS, ICSearchConstants.DECLARATIONS),indexFlag);
		}		
		else if (elaboratedType.getClassKind().equals(ASTClassKind.STRUCT))
		{
			this.output.addRef(encodeTypeEntry(elaboratedType.getFullyQualifiedNameCharArrays(),FWD_STRUCT, ICSearchConstants.DECLARATIONS),indexFlag);
		}
		else if (elaboratedType.getClassKind().equals(ASTClassKind.UNION))
		{
			this.output.addRef(encodeTypeEntry(elaboratedType.getFullyQualifiedNameCharArrays(),FWD_UNION, ICSearchConstants.DECLARATIONS),indexFlag);			
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

	public void addFunctionDeclaration(IASTFunction function, int indexFlag){
		this.output.addRef(encodeEntry(function.getFullyQualifiedNameCharArrays(),FUNCTION_DECL,FUNCTION_DECL_LENGTH),indexFlag);
		
		Iterator i=function.getParameters();
		while (i.hasNext()){
			Object parm = i.next();
			if (parm instanceof IASTParameterDeclaration){
				IASTParameterDeclaration parmDecl = (IASTParameterDeclaration) parm;
				this.output.addRef(encodeTypeEntry(new char[][]{parmDecl.getNameCharArray()}, VAR, ICSearchConstants.DECLARATIONS),indexFlag);
			}
		}
	}
	
	public void addFunctionReference(IASTFunction function, int indexFlag){
		this.output.addRef(encodeEntry(function.getFullyQualifiedNameCharArrays(),FUNCTION_REF,FUNCTION_REF_LENGTH),indexFlag);
	}
	
	public void addNameReference(){
		
	}
	
	public void addNamespaceDefinition(IASTNamespaceDefinition namespace, int indexFlag){
		this.output.addRef(encodeEntry(namespace.getFullyQualifiedNameCharArrays(),NAMESPACE_DECL,NAMESPACE_DECL_LENGTH),indexFlag);
	}
	
	public void addNamespaceReference(IASTNamespaceDefinition namespace, int indexFlag) {
		this.output.addRef(encodeEntry(namespace.getFullyQualifiedNameCharArrays(),NAMESPACE_REF,NAMESPACE_REF_LENGTH),indexFlag);
	}
	
	public void addTypedefReference( IASTTypedefDeclaration typedef, int indexFlag ){
		this.output.addRef( encodeTypeEntry( typedef.getFullyQualifiedNameCharArrays(), TYPEDEF, ICSearchConstants.REFERENCES),indexFlag );
	}

	private void addSuperTypeReference(int modifiers, char[] packageName, char[] typeName, char[][] enclosingTypeNames, char classOrInterface, char[] superTypeName, char superClassOrInterface){

	}
	
	public void addTypeReference(char[] typeName){
		//this.output.addRef(CharOperation.concat(TYPE_REF, CharOperation.lastSegment(typeName, '.')));
	}
	
	public void addClassReference(IASTTypeSpecifier reference, int indexFlag){
		char[][] fullyQualifiedName = null;
		ASTClassKind classKind = null;
		
		if (reference instanceof IASTClassSpecifier){
		  IASTClassSpecifier classRef = (IASTClassSpecifier) reference;
		  fullyQualifiedName = classRef.getFullyQualifiedNameCharArrays();
		  classKind = classRef.getClassKind();
		}
		else if (reference instanceof IASTElaboratedTypeSpecifier){
		  IASTElaboratedTypeSpecifier typeRef = (IASTElaboratedTypeSpecifier) reference;
		  fullyQualifiedName = typeRef.getFullyQualifiedNameCharArrays();
		  classKind = typeRef.getClassKind();
		}
	
		if (classKind.equals(ASTClassKind.CLASS))
		{  
			this.output.addRef(encodeTypeEntry(fullyQualifiedName,CLASS, ICSearchConstants.REFERENCES),indexFlag);
		}		
		else if (classKind.equals(ASTClassKind.STRUCT))
		{
			this.output.addRef(encodeTypeEntry(fullyQualifiedName,STRUCT,ICSearchConstants.REFERENCES),indexFlag);
		}
		else if (classKind.equals(ASTClassKind.UNION))
		{
			this.output.addRef(encodeTypeEntry(fullyQualifiedName,UNION,ICSearchConstants.REFERENCES),indexFlag);			
		}
	}
	public void addForwardClassReference(IASTTypeSpecifier reference, int indexFlag){
		char[][] fullyQualifiedName = null;
		ASTClassKind classKind = null;
		
		if (reference instanceof IASTElaboratedTypeSpecifier){
		  IASTElaboratedTypeSpecifier typeRef = (IASTElaboratedTypeSpecifier) reference;
		  fullyQualifiedName = typeRef.getFullyQualifiedNameCharArrays();
		  classKind = typeRef.getClassKind();
		}
	
		if (classKind == null)
			return;
		
		if (classKind.equals(ASTClassKind.CLASS))
		{  
			this.output.addRef(encodeTypeEntry(fullyQualifiedName,FWD_CLASS, ICSearchConstants.REFERENCES),indexFlag);
		}		
		else if (classKind.equals(ASTClassKind.STRUCT))
		{
			this.output.addRef(encodeTypeEntry(fullyQualifiedName,FWD_STRUCT,ICSearchConstants.REFERENCES),indexFlag);
		}
		else if (classKind.equals(ASTClassKind.UNION))
		{
			this.output.addRef(encodeTypeEntry(fullyQualifiedName,FWD_UNION,ICSearchConstants.REFERENCES),indexFlag);			
		}
	}
	/**
	 * Type entries are encoded as follow: 'typeDecl/' ('C' | 'S' | 'U' | 'E' ) '/'  TypeName ['/' Qualifier]* 
	 */
	 protected static final char[] encodeTypeEntry( char[][] fullTypeName, int typeType, LimitTo encodeType){ 

		int pos = 0, nameLength = 0;
		for (int i=0; i<fullTypeName.length; i++){
			char[] namePart = fullTypeName[i];
			nameLength+= namePart.length;
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
			
			case(FWD_CLASS):
			result[pos++]=FWD_CLASS_SUFFIX;
			break;
			
			case (FWD_STRUCT):
			result[pos++]=FWD_STRUCT_SUFFIX;
			break;
			
			case (FWD_UNION):
			result[pos++]=FWD_UNION_SUFFIX;
			break;
		}
		result[pos++] = SEPARATOR;
		//Encode in the following manner
		//	[typeDecl info]/[typeName]/[qualifiers]
		if (fullTypeName.length > 0){
		//Extract the name first
			char [] tempName = fullTypeName[fullTypeName.length-1];
			System.arraycopy(tempName, 0, result, pos, tempName.length);
			pos += tempName.length;
		}
		//Extract the qualifiers
		for (int i=fullTypeName.length - 2; i >= 0; i--){
			result[pos++] = SEPARATOR;
			char [] tempName = fullTypeName[i];
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
	protected static final char[] encodeEntry(char[][] elementName, char[] prefix, int prefixSize){ 
		int pos, nameLength = 0;
		for (int i=0; i<elementName.length; i++){
			char[] namePart = elementName[i];
			nameLength+= namePart.length;
		}
		//char[] has to be of size - [type length + length of the name (including qualifiers) + 
		//separators (need one less than fully qualified name length)
		char[] result = new char[prefixSize + nameLength + elementName.length - 1 ];
		System.arraycopy(prefix, 0, result, 0, pos = prefix.length);
		if (elementName.length > 0){
		//Extract the name first
			char [] tempName = elementName[elementName.length-1];
			System.arraycopy(tempName, 0, result, pos, tempName.length);
			pos += tempName.length;
		}
		//Extract the qualifiers
		for (int i=elementName.length - 2; i>=0; i--){
			result[pos++] = SEPARATOR;
			char [] tempName = elementName[i];
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
			  if (id.equals(ICFileTypeConstants.FT_C_SOURCE) ||
			  	  id.equals(ICFileTypeConstants.FT_CXX_SOURCE) ||
				  id.equals(ICFileTypeConstants.FT_C_HEADER) ||
				  id.equals(ICFileTypeConstants.FT_CXX_HEADER))
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
		} else if ( searchFor == ICSearchConstants.FWD_CLASS) {
			classType = FWD_CLASS_SUFFIX;
		} else if ( searchFor == ICSearchConstants.FWD_STRUCT) {
			classType = FWD_STRUCT_SUFFIX;
		} else if ( searchFor == ICSearchConstants.FWD_UNION) {
			classType = FWD_UNION_SUFFIX;
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
	
	public void addInclude(IASTInclusion inclusion, IASTInclusion parent, int fileNumber){
		this.output.addIncludeRef(inclusion.getFullFileName());
		this.output.addRelatives(inclusion.getFullFileName(),(parent != null ) ? parent.getFullFileName() : null);
		
		//Add Dep Table entry
		char[][] incName = new char[1][];
		incName[0] = inclusion.getFullFileName().toCharArray();
		//TODO: Kludge! Get rid of BOGUS entry - need to restructure Dep Tree to use reference indexes
		int BOGUS_ENTRY = 1;
		this.output.addRef(encodeEntry(incName, INCLUDE_REF, INCLUDE_REF_LENGTH),fileNumber);
	}
	
	

    abstract private class Problem {
        public IFile file;
        public IFile originator;
        public Problem( IFile file, IFile orig ){
            this.file = file;
            this.originator = orig;
        }
        
        abstract public boolean isAddProblem();
        abstract public Object getProblem();
    }

    private class AddMarkerProblem extends Problem {
        private Object problem;
        public AddMarkerProblem(IFile file, IFile orig, Object problem) {
            super( file, orig );
            this.problem = problem;
        }
        public boolean isAddProblem(){
            return true;
        }
        public Object getProblem(){
            return problem;
        }
    }

    private class RemoveMarkerProblem extends Problem {
        public RemoveMarkerProblem(IFile file, IFile orig) {
            super(file, orig);
        }
        public boolean isAddProblem() {
            return false;
        }
        public Object getProblem() {
            return null;
        }
    }

    // Problem markers ******************************
    
    
    public boolean areProblemMarkersEnabled(){
        return problemMarkersEnabled != 0;
    }
    public int getProblemMarkersEnabled() {
        return problemMarkersEnabled;
    }
    
    public void setProblemMarkersEnabled(int value) {
        if (value != 0) {
            problemsMap = new HashMap();
        }
        this.problemMarkersEnabled = value;
    }
    
    /**
     * @param tempFile - not null
     * @param resourceFile
     * @param problem
     */
    public void generateMarkerProblem(IFile tempFile, IFile resourceFile, Object problem) {
        Problem tempProblem = new AddMarkerProblem(tempFile, resourceFile, problem);
        if (problemsMap.containsKey(tempFile)) {
            List list = (List) problemsMap.get(tempFile);
            list.add(tempProblem);
        } else {
            List list = new ArrayList();
            list.add(new RemoveMarkerProblem(tempFile, resourceFile));  //remove existing markers
            list.add(tempProblem);
            problemsMap.put(tempFile, list);
        }
    }

    public void requestRemoveMarkers(IFile resource, IFile originator ){
        if (!areProblemMarkersEnabled())
            return;
        
        Problem prob = new RemoveMarkerProblem(resource, originator);
        
        //a remove request will erase any previous requests for this resource
        if( problemsMap.containsKey(resource) ){
            List list = (List) problemsMap.get(resource);
            list.clear();
            list.add(prob);
        } else {
            List list = new ArrayList();
            list.add(prob);
            problemsMap.put(resource, list);
        }
    }
    
    public void reportProblems() {
        if (!areProblemMarkersEnabled())
            return;
        
        Iterator i = problemsMap.keySet().iterator();
        
        while (i.hasNext()){
            IFile resource = (IFile) i.next();
            List problemList = (List) problemsMap.get(resource);

            //only bother scheduling a job if we have problems to add or remove
            if (problemList.size() <= 1) {
                IMarker [] marker;
                try {
                    marker = resource.findMarkers( ICModelMarker.INDEXER_MARKER, true, IResource.DEPTH_ZERO);
                } catch (CoreException e) {
                    continue;
                }
                if( marker.length == 0 )
                    continue;
            }
            String jobName = INDEXER_MARKER_PROCESSING;
            jobName += " ("; //$NON-NLS-1$
            jobName += resource.getFullPath();
            jobName += ')';
            
            ProcessMarkersJob job = new ProcessMarkersJob(resource, problemList, jobName);
            
            IndexManager indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();
            IProgressMonitor group = indexManager.getIndexJobProgressGroup();
            
            job.setRule(resource);
            if (group != null)
                job.setProgressGroup(group, 0);
            job.setPriority(Job.DECORATE);
            job.schedule();
        }
    }
    
    private class ProcessMarkersJob extends Job {
        protected final List problems;
        private final IFile resource;
        public ProcessMarkersJob(IFile resource, List problems, String name) {
            super(name);
            this.problems = problems;
            this.resource = resource;
        }

        protected IStatus run(IProgressMonitor monitor) {
            IWorkspaceRunnable job = new IWorkspaceRunnable( ) {
                public void run(IProgressMonitor monitor) {
                    processMarkers( problems );
                }
            };
            try {
                CCorePlugin.getWorkspace().run(job, resource, 0, null);
            } catch (CoreException e) {
            }
            return Status.OK_STATUS;
        }
    }
    
    protected void processMarkers(List problemsList) {
        Iterator i = problemsList.iterator();
        while (i.hasNext()) {
            Problem prob = (Problem) i.next();
            if (prob.isAddProblem()) {
                addMarkers(prob.file, prob.originator, prob.getProblem());
            } else {
                removeMarkers(prob.file, prob.originator);
            }
        }
    }

    abstract protected void addMarkers(IFile tempFile, IFile originator, Object problem);
    
    public void removeMarkers(IFile resource, IFile originator) {
        if (originator == null) {
            //remove all markers
            try {
                resource.deleteMarkers(ICModelMarker.INDEXER_MARKER, true, IResource.DEPTH_INFINITE);
            } catch (CoreException e) {
            }
            return;
        }
        // else remove only those markers with matching originator
        IMarker[] markers;
        try {
            markers = resource.findMarkers(ICModelMarker.INDEXER_MARKER, true, IResource.DEPTH_INFINITE);
        } catch (CoreException e1) {
            return;
        }
        String origPath = originator.getFullPath().toString();
        IMarker mark = null;
        String orig = null;
        for (int i = 0; i < markers.length; i++) {
            mark = markers[ i ];
            try {
                orig = (String) mark.getAttribute(INDEXER_MARKER_ORIGINATOR);
                if( orig != null && orig.equals(origPath )) {
                    mark.delete();
                }
            } catch (CoreException e) {
            }
        }
    }
    

}

