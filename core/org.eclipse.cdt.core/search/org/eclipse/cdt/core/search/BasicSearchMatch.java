/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/
/*
 * Created on Jul 29, 2003
 */
package org.eclipse.cdt.core.search;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class BasicSearchMatch implements IMatch, Comparable {

	public BasicSearchMatch() {
	}

	public BasicSearchMatch(BasicSearchMatch basicMatch) {
		name 		= basicMatch.name;
		parentName 	= basicMatch.parentName;
		returnType  = basicMatch.returnType;
		resource 	= basicMatch.resource;
		path 		= basicMatch.path;
		startOffset = basicMatch.startOffset;
		endOffset 	= basicMatch.endOffset;
	}
	
	public int hashCode(){
		String hashString = "";
		
		hashString += name;
		hashString += ":" + parentName;
		hashString += ":" + returnType;
		
		if( getLocation() != null)
			hashString += ":" + getLocation().toString();
			
		hashString += ":" + startOffset + ":" + endOffset;
		hashString += ":" + type + ":" + visibility;
		
		return hashString.hashCode();
	}
	
	public boolean equals(Object obj){
		if( !(obj instanceof BasicSearchMatch ) ){
			return false;	
		}
		BasicSearchMatch match = (BasicSearchMatch)obj;
		
		IPath path = getLocation();
		
		if( startOffset != match.getStartOffset() || endOffset != match.getEndOffset()  )
			return false;
		
		if( type != match.getElementType() || visibility != match.getVisibility() )
			return false;
			
		if( name != null && match.getName() != null){
			if( !name.equals( match.getName() ) ) 
		 		return false; 
		} else if( name != match.getName() ){
			return false;
		}
		 	
		if( parentName != null && match.getParentName() != null){
			if( !parentName.equals( match.getParentName() ) ) 
				return false; 
		} else if( parentName != match.getParentName() ){
			return false;
		}
		
		if( returnType != null && match.getReturnType() != null){
			if( !returnType.equals( match.getReturnType() ) ) 
				return false; 
		} else if( returnType != match.getReturnType() ){
			return false;
		}
		
		IPath thisPath = getLocation();
		IPath matchPath = match.getLocation();
		if( thisPath != null && matchPath != null ){
			if( !thisPath.equals( matchPath ) )
				return false;
		} else if( thisPath != matchPath ){
			return false;
		}

		return true;
	}
	
	public int compareTo( Object o ){
		if( !( o instanceof BasicSearchMatch ) ){
			throw new ClassCastException();
		}
		
		BasicSearchMatch match = (BasicSearchMatch) o;
		
		String str1 = getLocation().toString();
		String str2 = match.getLocation().toString();
		
		str1 += " " + getStartOffset()+ " ";
		str2 += " " + match.getStartOffset()+ " ";
		
		str1 += getName() + " " + getParentName()+ " " + getReturnType();
		str2 += match.getName() + " " + match.getParentName()+ " " + getReturnType();
		
		return str1.compareTo( str2 );
	}
	
	public String name 		  = null;
	public String parentName  = null;
	public String returnType;
	
	public IResource resource = null;
	public IPath     path 	  = null;
	
	public int startOffset 	  = 0;
	public int endOffset 	  = 0;
	
	public int type 		  = 0;
	public int visibility 	  = 0;
	
	boolean isConst			  = false;
	boolean isVolatile		  = false;		
	boolean isStatic		  = false;

	public int getElementType() {
		return type;
	}

	public int getVisibility() {
		return visibility;
	}

	public String getName() {
		return name;
	}

	public String getParentName() {
		return parentName;
	}

	public String getReturnType() {
		return returnType;
	}

	public IResource getResource() {
		return resource;
	}
	
	public IPath getLocation() {
		if(resource != null)
			return resource.getLocation();
		else if (path != null)
			return path;
		else return null;
	}

	public int getStartOffset() {
		return startOffset;
	}

	public int getEndOffset() {
		return endOffset;
	}

	public boolean isStatic() {
		return isStatic;
	}
	public boolean isConst() {
		return isConst;
	}

	public boolean isVolatile() {
		return isVolatile;
	}
	/**
	 * @return
	 */
	public int getType() {
		return type;
	}

	/**
	 * @param i
	 */
	public void setEndOffset(int i) {
		endOffset = i;
	}

	/**
	 * @param b
	 */
	public void setConst(boolean b) {
		isConst = b;
	}

	/**
	 * @param b
	 */
	public void setStatic(boolean b) {
		isStatic = b;
	}

	/**
	 * @param b
	 */
	public void setVolatile(boolean b) {
		isVolatile = b;
	}

	/**
	 * @param string
	 */
	public void setName(String string) {
		name = string;
	}

	/**
	 * @param string
	 */
	public void setParentName(String string) {
		parentName = string;
	}

	/**
	 * @param string
	 */
	public void setReturnType(String string) {
		returnType = string;
	}

	/**
	 * @param i
	 */
	public void setStartOffset(int i) {
		startOffset = i;
	}

	/**
	 * @param i
	 */
	public void setType(int i) {
		type = i;
	}

	/**
	 * @param i
	 */
	public void setVisibility(int i) {
		visibility = i;
	}

}
