/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;

/**
 * @author jcamelon
 */
public class CPPASTQualifiedName extends CPPASTNode implements
      ICPPASTQualifiedName {

   /**
    * @param duple
    */
   public CPPASTQualifiedName() {
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.core.dom.ast.IASTName#resolveBinding()
    */
   public IBinding resolveBinding() {
      //The full qualified name resolves to the same thing as the last name
      removeNullNames();
      return names[names.length - 1].resolveBinding();
   }

   public IBinding[] resolvePrefix() {
	   removeNullNames();
	   return names[names.length - 1].resolvePrefix();
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#toString()
    */
   public String toString() {
   	  if( signature == null ) return ""; //$NON-NLS-1$
      return signature;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName#addName(org.eclipse.cdt.core.dom.ast.IASTName)
    */
   public void addName(IASTName name) {
      if (names == null) {
         names = new IASTName[DEFAULT_NAMES_LIST_SIZE];
         currentIndex = 0;
      }
      if (names.length == currentIndex) {
         IASTName[] old = names;
         names = new IASTName[old.length * 2];
         for (int i = 0; i < old.length; ++i)
            names[i] = old[i];
      }
      names[currentIndex++] = name;
   }

   /**
    * @param decls2
    */
   private void removeNullNames() {
      int nullCount = 0;
      for (int i = 0; i < names.length; ++i)
         if (names[i] == null)
            ++nullCount;
      if (nullCount == 0)
         return;
      IASTName[] old = names;
      int newSize = old.length - nullCount;
      names = new IASTName[newSize];
      for (int i = 0; i < newSize; ++i)
         names[i] = old[i];
      currentIndex = newSize;
   }

   private int              currentIndex            = 0;
   private IASTName[]       names                   = null;
   private static final int DEFAULT_NAMES_LIST_SIZE = 4;
   private boolean          value;
   private String           signature;

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName#getNames()
    */
   public IASTName[] getNames() {
      if (names == null)
         return IASTName.EMPTY_NAME_ARRAY;
      removeNullNames();
      return names;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.core.dom.ast.IASTName#toCharArray()
    */
   public char[] toCharArray() {
      if (names == null)
         return "".toCharArray();
      removeNullNames();

      //count first
      int len = 0;
      for (int i = 0; i < names.length; ++i) {
         char[] n = names[i].toCharArray();
         if (n == null)
            return null;
         len += n.length;
         if (i != names.length - 1)
            len += 2;
      }

      char[] nameArray = new char[len];
      int pos = 0;
      for (int i = 0; i < names.length; i++) {
         char[] n = names[i].toCharArray();
         System.arraycopy(n, 0, nameArray, pos, n.length);
         pos += n.length;
         if (i != names.length - 1) {
            nameArray[pos++] = ':';
            nameArray[pos++] = ':';
         }
      }
      return nameArray;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName#isFullyQualified()
    */
   public boolean isFullyQualified() {
      return value;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName#setFullyQualified(boolean)
    */
   public void setFullyQualified(boolean value) {
      this.value = value;
   }

   /**
    * @param string
    */
   public void setValue(String string) {
      this.signature = string;

   }

}