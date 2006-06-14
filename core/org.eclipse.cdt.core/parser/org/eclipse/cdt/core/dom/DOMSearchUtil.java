/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.content.IContentType;

/**
 * Utility class to have commonly used algorithms in one place for searching with the DOM. 
 * 
 * @author dsteffle
 */
public class DOMSearchUtil {
	private static final IASTName[] BLANK_NAME_ARRAY = new IASTName[0];
    private static final IASTName[] EMPTY_NAME_LIST = BLANK_NAME_ARRAY;

    public static final int DECLARATIONS = 1;
    public static final int DEFINITIONS = 2;
    public static final int DECLARATIONS_DEFINITIONS = 3;
    public static final int REFERENCES = 4;
    public static final int ALL_OCCURRENCES = 5;
    /**
    * This retrieves the ParserLanguage from an IFile.
    *
    * @param file
    * @return
    */
    public static ParserLanguage getLanguageFromFile(IFile file) {
        IProject project = file.getProject();
        IContentType contentType = CCorePlugin.getContentType(project, file.getFullPath().lastSegment());
        if (contentType != null) {
        	String lid = contentType.getId();
        	if (CCorePlugin.CONTENT_TYPE_CXXSOURCE.equals(lid)) {
        		return ParserLanguage.CPP;
        	}
        }        
        return ParserLanguage.C;
    }

    /**
     * The CPPNameCollector used to get IASTNames from an IASTNode.
     * 
     * @author dsteffle
     */
    static public class CPPNameCollector extends CPPASTVisitor {
        {
            shouldVisitNames = true;
        }
        public List nameList = new ArrayList();
        public int visit( IASTName name ){
            nameList.add( name );
            return PROCESS_CONTINUE;
        }
        public IASTName getName( int idx ){
            if( idx < 0 || idx >= nameList.size() )
                return null;
            return (IASTName) nameList.get( idx );
        }
        public int size() { return nameList.size(); } 
    }

    /**
     * The CNameCollector used to get IASTNames from an IASTNode.
     * 
     * @author dsteffle
     */
    static public class CNameCollector extends CASTVisitor {
        {
            shouldVisitNames = true;
        }
        public List nameList = new ArrayList();
        public int visit( IASTName name ){
            nameList.add( name );
            return PROCESS_CONTINUE;
        }
        public IASTName getName( int idx ){
            if( idx < 0 || idx >= nameList.size() )
                return null;
            return (IASTName) nameList.get( idx );
        }
        public int size() { return nameList.size(); } 
    }
    
	/**
	 * Returns the ParserLanguage corresponding to the IPath and IProject.  Returns ParserLanguage.CPP if the file type is a header.
	 * 
	 * @param path
	 * @param project
	 * @return
	 */
    public static ParserLanguage getLanguage( IPath path, IProject project )
    {  
    	//FIXME: ALAIN, for headers should we assume CPP ??
    	// The problem is that it really depends on how the header was included.
    	String id = null;
    	IContentType contentType = CCorePlugin.getContentType(project, path.lastSegment());
    	if (contentType != null) {
    		id = contentType.getId();
    	}
    	if (id != null) {
    		if (CCorePlugin.CONTENT_TYPE_CXXHEADER.equals(id)) {
    			return ParserLanguage.CPP;
    		} else if (CCorePlugin.CONTENT_TYPE_CXXSOURCE.equals(id)) {
    			return ParserLanguage.CPP;
    		} else if (CCorePlugin.CONTENT_TYPE_CHEADER.equals(id)) {
    			return ParserLanguage.CPP; 				// <============== is that right ? should not this be C ?
    		} else if (CCorePlugin.CONTENT_TYPE_CSOURCE.equals(id)) {
    			return ParserLanguage.C;
    		} else if (CCorePlugin.CONTENT_TYPE_ASMSOURCE.equals(id)) {
    			// ???
    			// What do we do here ?
    		}
    	}
		return ParserLanguage.CPP;
    }

    /**
     * This is used to get the names from the TU that the IASTName searchName belongs to.
     * 
     * @param searchName the IASTName whose references/delcarations are to be retrieved
     * @param limitTo used to specify whether to get declarations, references, or both, one of: 
     * ( CSearchPattern.DECLARATION | CSearchPattern.REFERENCES | CSearchPattern.ALL_OCCURRENCES ) 
     * @return IASTName[] declarations, references, or both depending on limitTo that correspond to the IASTName searchName searched for
     */
    public static IASTName[] getNamesFromDOM(IASTName searchName, int limitTo) {
		IASTName[] names = null;
		IASTTranslationUnit tu = searchName.getTranslationUnit();
		
		if (tu == null) {
			return BLANK_NAME_ARRAY;
		}
		
		IBinding binding = searchName.resolveBinding();
		if (binding instanceof PDOMBinding) {
			try { 
				ArrayList pdomNames = new ArrayList();
				// First decls
				PDOMName name = ((PDOMBinding)binding).getFirstDeclaration();
				while (name != null) {
					pdomNames.add(name);
					name = name.getNextInBinding();
				}
				// Next defs
				name = ((PDOMBinding)binding).getFirstDefinition();
				while (name != null) {
					pdomNames.add(name);
					name = name.getNextInBinding();
				}
				names = (IASTName[])pdomNames.toArray(new IASTName[pdomNames.size()]);
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		} else {
			names = getNames(tu, binding, limitTo);
			
			if (names == null || names.length == 0) { // try alternate strategies		
				try {
					// fix for 86829, 95224
					if ((binding instanceof ICPPConstructor || (binding instanceof ICPPMethod && ((ICPPMethod)binding).isDestructor())) 
							&& binding.getScope() instanceof ICPPClassScope) {
						binding =  ((ICPPClassScope)binding.getScope()).getClassType();
						names = getNames(tu, binding, limitTo);
					}
				} catch (DOMException e) {}
			}
		}

		return names;
    }
	
	private static IASTName[] getNames(IASTTranslationUnit tu, IBinding binding, int limitTo) {
        IASTName[] names = null;
		if (limitTo == DECLARATIONS ||
			limitTo == DECLARATIONS_DEFINITIONS) {
            names = tu.getDeclarations(binding);
        } else if (limitTo == REFERENCES) {
            names = tu.getReferences(binding);
        } else if (limitTo == DEFINITIONS) {
            names = tu.getDefinitions(binding);
        } else if (limitTo == ALL_OCCURRENCES){
            names = tu.getDeclarations(binding);
            names = (IASTName[])ArrayUtil.addAll(IASTName.class, names, tu.getReferences(binding));
        } else {  // assume ALL
            names = tu.getDeclarations(binding);
            names = (IASTName[])ArrayUtil.addAll(IASTName.class, names, tu.getReferences(binding));
        }
		
		return names;
	}

}
