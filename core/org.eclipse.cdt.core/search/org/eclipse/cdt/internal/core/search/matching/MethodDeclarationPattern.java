/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *******************************************************************************/
/*
 * Created on Jul 11, 2003
 */
package org.eclipse.cdt.internal.core.search.matching;

import java.io.IOException;

import org.eclipse.cdt.core.browser.PathUtil;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;
import org.eclipse.cdt.core.parser.ast.ASTUtil;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTQualifiedNameElement;
import org.eclipse.cdt.core.search.BasicSearchMatch;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.internal.core.CharOperation;
import org.eclipse.cdt.internal.core.index.IEntryResult;
import org.eclipse.cdt.internal.core.index.cindexstorage.Index;
import org.eclipse.cdt.internal.core.index.cindexstorage.IndexedFileEntry;
import org.eclipse.cdt.internal.core.index.cindexstorage.Util;
import org.eclipse.cdt.internal.core.index.cindexstorage.io.IndexInput;
import org.eclipse.cdt.internal.core.search.IIndexSearchRequestor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MethodDeclarationPattern extends CSearchPattern {

	private SearchFor searchFor;
	
	private char[][] parameterNames;
	private char[]   simpleName;
	private char[][] qualifications;
	private char[]   returnTypes;
	
	private char[]   decodedSimpleName;
	private char[][] decodedQualifications;
	private char[][] decodedParameters;
	private char[]   decodedReturnTypes;
	
	public MethodDeclarationPattern(char[] name, char[][] qual, char [][] params, char[] returnTypes, int matchMode, SearchFor search, LimitTo limitTo, boolean caseSensitive) {
		//super( name, params, matchMode, limitTo, caseSensitive );
		super( matchMode, caseSensitive, limitTo );

		qualifications = qual;
		simpleName = name;
		parameterNames = params;
		this.returnTypes = returnTypes;
		
		searchFor = search;
	}

	public char [] getSimpleName(){
		return simpleName;
	}
	
	public int matchLevel(ISourceElementCallbackDelegate node, LimitTo limit ) {
		if( node instanceof IASTMethod ){
			if( searchFor != METHOD || !canAccept( limit ) ){
				return IMPOSSIBLE_MATCH;
			}
		} else if ( node instanceof IASTFunction ){
			if( searchFor != FUNCTION || !canAccept( limit ) ){
				return IMPOSSIBLE_MATCH;
			}
		} else {
			return IMPOSSIBLE_MATCH;
		}

		IASTFunction function = (IASTFunction) node;
		char[] nodeName = function.getNameCharArray();

		//check name, if simpleName == null, its treated the same as "*"	
		if( simpleName != null && !matchesName( simpleName, nodeName ) ){
			return IMPOSSIBLE_MATCH;
		}

		if( node instanceof IASTQualifiedNameElement ){
			//create char[][] out of full name, 
			char [][] qualName = ((IASTQualifiedNameElement) node).getFullyQualifiedNameCharArrays();
			//check containing scopes
			if( !matchQualifications( qualifications, qualName, true ) ){
				return IMPOSSIBLE_MATCH;
			}
		}
		
		
		//parameters
		if( parameterNames != null && parameterNames.length > 0  &&	parameterNames[0].length > 0 ){
			String [] paramTypes = ASTUtil.getFunctionParameterTypes(function);
			
			if ( paramTypes.length == 0 && CharOperation.equals(parameterNames[0], "void".toCharArray())){ //$NON-NLS-1$
				//All empty lists have transformed to void, this function has no parms
				return ACCURATE_MATCH;
			}
			
			if( parameterNames.length != paramTypes.length )
				return IMPOSSIBLE_MATCH;
			
			for( int i = 0; i < parameterNames.length; i++ ){
			
				//if this function doesn't have this many parameters, it is not a match.
				//or if this function has a parameter, but parameterNames only has null.
				if( parameterNames[ i ] == null )
					return IMPOSSIBLE_MATCH;
					
				char[] param = paramTypes[ i ].toCharArray();
				
				//no wildcards in parameters strings
				if( !CharOperation.equals( parameterNames[i], param, _caseSensitive ) )
					return IMPOSSIBLE_MATCH;
			}
		}
		
		return ACCURATE_MATCH;
	}
	
	public char[] indexEntryPrefix() {
		if( searchFor == FUNCTION )
			return Index.bestFunctionPrefix( _limitTo, simpleName, _matchMode, _caseSensitive );
		else if( searchFor == METHOD )
			return Index.bestMethodPrefix( _limitTo, simpleName, qualifications, _matchMode, _caseSensitive );
		else return null;
	}
	
	protected void resetIndexInfo(){
		decodedSimpleName = null;
		decodedQualifications = null;
	}
	
	protected void decodeIndexEntry(IEntryResult entryResult) {
		this.decodedSimpleName = entryResult.extractSimpleName().toCharArray();	
		String []missmatch = entryResult.getEnclosingNames();
		if(missmatch != null) {
			
			//Find the first opening braces
			int start=0;
			int end=0;
			boolean parmsExist=false;
			for (int i=0; i<missmatch.length; i++){
				if (missmatch[i].equals("(")){ //$NON-NLS-1$
					start=i;
					parmsExist=true;
				}
				
				if (missmatch[i].equals(")")){ //$NON-NLS-1$
					end=i;
					break;
				}
			}
			
			//Check for return type
			boolean returnTypeExists=false;
			int returnStart=0;
			int returnEnd=0;
			if (end != 0 && 
				(end+1)<missmatch.length){
				//Make sure that we have a parameter string and that there is still something left
				//to be decoded
				
				for (int j=end; j<missmatch.length; j++){
				    if (missmatch[j].equals("R(")){	 //$NON-NLS-1$
				        returnStart=j;
				        returnTypeExists=true;
				    }
				  
				    if (missmatch[j].equals(")R")){	 //$NON-NLS-1$
				        returnEnd=j;
				        break;
				    }
				}
			}
			
			if (parmsExist){
				this.decodedParameters = new char[end - (start + 1)][];
				
				int counter=0;
				for (int i=start+1; i<end; i++){
					decodedParameters[counter++]=missmatch[i].toCharArray();
				}
				
				if (returnEnd != 0){
					this.decodedQualifications = new char[missmatch.length - (returnEnd + 1)][];
					counter=0;
					for (int i = returnEnd + 1; i < missmatch.length; i++)
						this.decodedQualifications[counter++] = missmatch[i].toCharArray();
				} else {
					this.decodedQualifications = new char[missmatch.length - (end + 1)][];
					counter=0;
					for (int i = end + 1; i < missmatch.length; i++)
						this.decodedQualifications[counter++] = missmatch[i].toCharArray();
				}
			} else {
				this.decodedParameters = new char[0][];
				this.decodedQualifications = new char[missmatch.length][];
				for (int i = 0; i < missmatch.length; i++)
					this.decodedQualifications[i] = missmatch[i].toCharArray();
			}
			
			if (returnTypeExists){
				this.returnTypes = missmatch[returnStart + 1].toCharArray();
			}
		}
			
	}

	protected boolean matchIndexEntry() {
		/* check simple name matches */
		if (simpleName != null){
			if( ! matchesName( simpleName, decodedSimpleName ) ){
				return false; 
			}
		}
		
		if( !matchQualifications( qualifications, decodedQualifications ) ){
			return false;
		}
		
		if( !matchParameters( parameterNames, decodedParameters ) ){
			return false;
		}
		
		if (!matchReturnType(returnTypes, decodedReturnTypes)){
			return false;
		}
		return true;
	}
	
	/**
	 * @param returnTypes
	 * @param decodedReturnTypes
	 * @return
	 */
	private boolean matchReturnType(char[] returnTypes, char[] decodedReturnTypes) {
		if( returnTypes == null || decodedReturnTypes == null ){
			return true;  //treat null as "*"
		}
	
		return CharOperation.equals( returnTypes, decodedReturnTypes, true);
	}

	private boolean matchParameters(char[][] parameterNames2, char[][] decodedParameters2) {
		
		if (parameterNames2.length == 0)
			return true;
		
		//Check lengths of decoded
		if (decodedParameters2.length != parameterNames2.length)
			return false;
		
		for (int i=0; i<parameterNames2.length; i++){
			boolean matchFound=false;
			for (int j=0; j<decodedParameters2.length; j++){
				if (Util.compare(parameterNames2[i],decodedParameters[j])==0){
					matchFound=true;
					break;
				}
			}
			
			if (!matchFound)
				 return false;
			
		}
		return true;
	}

	public void feedIndexRequestor(IIndexSearchRequestor requestor, int detailLevel, int[] fileRefs, int[][] offsets, int[][] offsetLengths,IndexInput input, ICSearchScope scope) throws IOException {

		for (int i = 0, max = fileRefs.length; i < max; i++) {
			IndexedFileEntry file = input.getIndexedFile(fileRefs[i]);
			String path = null;
			if (file != null && scope.encloses(path =file.getPath())) {
				for (int j=0; j<offsets[i].length; j++){
					BasicSearchMatch match = new BasicSearchMatch();
					match.setName(new String(this.decodedSimpleName));
					//Decode the offsetse
					//Offsets can either be IIndex.LINE or IIndex.OFFSET 
					match.setLocatable(getMatchLocatable(offsets[i][j],offsetLengths[i][j]));
					match.setParentName(""); //$NON-NLS-1$
					if (searchFor == METHOD){
						match.setType(ICElement.C_METHOD);
					} else if (searchFor == FUNCTION ){
						match.setType(ICElement.C_FUNCTION);
					}
					
				    IFile tempFile = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(path));
					if (tempFile != null && tempFile.exists())
						match.setResource(tempFile);
					else {
						IPath tempPath = PathUtil.getWorkspaceRelativePath(file.getPath());
						match.setPath(tempPath);
						match.setReferringElement(tempPath);
					}
					requestor.acceptSearchMatch(match);
				}
			}
		}
	}
}
