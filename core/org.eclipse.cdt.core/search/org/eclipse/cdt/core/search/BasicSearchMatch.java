/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *******************************************************************************/
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
		locatable 	= basicMatch.locatable;
		referringElement = basicMatch.referringElement;
	}
	
	final static private String HASH_SEPERATOR = ":"; //$NON-NLS-1$
	public int hashCode(){
		if( hashCode == 0 )
		{			
			StringBuffer hashBuffer = new StringBuffer();
			hashBuffer.append( name );
			hashBuffer.append( HASH_SEPERATOR );			
			hashBuffer.append( parentName );
			hashBuffer.append( HASH_SEPERATOR );
			hashBuffer.append( returnType );
			if( getLocation() != null){
				hashBuffer.append( HASH_SEPERATOR );
				hashBuffer.append( getLocation().toString() );
			}
			hashBuffer.append( HASH_SEPERATOR );
			hashBuffer.append( locatable instanceof IOffsetLocatable ? ((IOffsetLocatable)locatable).getNameStartOffset():((ILineLocatable)locatable).getStartLine());
			hashBuffer.append( HASH_SEPERATOR ); 
			hashBuffer.append( locatable instanceof IOffsetLocatable ? ((IOffsetLocatable)locatable).getNameEndOffset(): ((ILineLocatable)locatable).getEndLine());
			hashBuffer.append( HASH_SEPERATOR ); 
			hashBuffer.append( type );
			hashBuffer.append( HASH_SEPERATOR ); 
			hashBuffer.append( visibility ); 
			
			hashCode = hashBuffer.toString().hashCode();
		}
		return hashCode;
	}
	
	public boolean equals(Object obj){
		if( !(obj instanceof BasicSearchMatch ) ){
			return false;	
		}
		BasicSearchMatch match = (BasicSearchMatch)obj;
		
		if ((locatable != null && match.locatable==null) ||
			(locatable==null && match.locatable!=null)){
			return false;
		}
		
		if (locatable!=null && match.locatable !=null){
			if  (!(locatable instanceof IOffsetLocatable && match.locatable instanceof IOffsetLocatable) ||
				 !(locatable instanceof ILineLocatable && match.locatable instanceof ILineLocatable))
				return false;
		
			if (locatable instanceof IOffsetLocatable){
				if (((IOffsetLocatable)locatable).getNameStartOffset() != ((IOffsetLocatable)match.locatable).getNameStartOffset() ||
					((IOffsetLocatable)locatable).getNameEndOffset() != ((IOffsetLocatable)match.locatable).getNameEndOffset())
					return false;
				
			} else {
				if (((ILineLocatable)locatable).getStartLine() != ((ILineLocatable)match.locatable).getStartLine())
					return false;
			}
		}
		
		
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
		
		int result = getLocation().toString().compareTo( match.getLocation().toString() );
		if( result != 0 ) return result;
		
		
		if (locatable instanceof IOffsetLocatable && match.getLocatable() instanceof IOffsetLocatable){
			result = ((IOffsetLocatable)locatable).getNameStartOffset() - ((IOffsetLocatable)match.locatable).getNameStartOffset();
			if( result != 0 ) return result;
			
			result = ((IOffsetLocatable)locatable).getNameEndOffset() - ((IOffsetLocatable)match.locatable).getNameEndOffset();
			if( result != 0 ) return result;
		}
		else if (locatable instanceof ILineLocatable && match.getLocatable() instanceof ILineLocatable){
			result = ((ILineLocatable)locatable).getStartLine() - ((ILineLocatable)match.locatable).getStartLine();
			if( result != 0 ) return result;
			
			result = ((ILineLocatable)locatable).getEndLine() - ((ILineLocatable)match.locatable).getEndLine();
			if( result != 0 ) return result;
		}
		
		result = getName().compareTo( match.getName() );
		if( result != 0 ) return result;
		
		result = getParentName().compareTo( match.getParentName() );
		if( result != 0 ) return result;
		
		result = getReturnType().compareTo( match.getReturnType() );
		return result;
	}
	
	public String name 		  = null;
	public String parentName  = null;
	public String returnType;
	
	public IResource resource = null;
	public IPath     path 	  = null;

	public int type 		  = 0;
	public int visibility 	  = 0;
	
	boolean isConst			  = false;
	boolean isVolatile		  = false;		
	boolean isStatic		  = false;
	
	private int hashCode = 0;

	public IPath referringElement = null;
	
	public IMatchLocatable locatable = null;
	
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
	
	public IPath getReferenceLocation() {
		return referringElement;
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
	public void setType(int i) {
		type = i;
	}

	/**
	 * @param i
	 */
	public void setVisibility(int i) {
		visibility = i;
	}
	
	public IMatchLocatable getLocatable() {
		return locatable;
	}

}
