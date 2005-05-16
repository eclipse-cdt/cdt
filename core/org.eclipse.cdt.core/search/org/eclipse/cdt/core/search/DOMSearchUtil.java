/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.search;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICLogConstants;
import org.eclipse.cdt.core.dom.CDOM;
import org.eclipse.cdt.core.dom.IASTServiceProvider;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.core.dom.ast.c.ICExternalBinding;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceAlias;
import org.eclipse.cdt.core.filetype.ICFileType;
import org.eclipse.cdt.core.filetype.ICFileTypeConstants;
import org.eclipse.cdt.core.parser.ParseError;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.search.ICSearchConstants.LimitTo;
import org.eclipse.cdt.core.search.ICSearchConstants.SearchFor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTQualifiedName;
import org.eclipse.cdt.internal.core.search.matching.CSearchPattern;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

/**
 * Utility class to have commonly used algorithms in one place for searching with the DOM. 
 * 
 * @author dsteffle
 */
public class DOMSearchUtil {
    private static final IASTName[] BLANK_NAME_ARRAY = new IASTName[0];
    private static final IASTName[] EMPTY_NAME_LIST = BLANK_NAME_ARRAY;
    private static final Set EMPTY_MATCHES = new HashSet(0);

    /**
     * This is a convenience method that uses the SearchEngine to find declarations, references, or both that correspond
     * to the IASTName searchName found in the index.
     * 
     * @param scope is used to limit the scope that SearchEngine searches the index against
     * @param searchName is the IASTName whose delcarations/references are sought after
     * @param limitTo used to specify whether to get declarations, references, or both, one of: 
     * ( CSearchPattern.DECLARATION | CSearchPattern.REFERENCES | CSearchPattern.ALL_OCCURRENCES ) 
     * @return
     */
    public static Set getMatchesFromSearchEngine(ICSearchScope scope, IASTName searchName, LimitTo limitTo) {
        SearchEngine engine = new SearchEngine();
        BasicSearchResultCollector results = new BasicSearchResultCollector();
            
        ICSearchPattern pattern = createPattern(searchName, limitTo, true);
            
        try {
            engine.search(CCorePlugin.getWorkspace(), pattern, scope, results, false);
        } catch (InterruptedException e) {
            return EMPTY_MATCHES;
        }
            
        return results.getSearchResults();
    }
    
    private static CSearchPattern createPattern( IASTName searchName, LimitTo limitTo, boolean caseSensitive) {
		IBinding binding = searchName.resolveBinding();
		if (binding == null)
			return null;
		
        // build the SearchFor/pattern based on the IBinding
        SearchFor searchFor = createSearchFor(binding);
        if (binding instanceof IFunction) {
            searchFor = ICSearchConstants.FUNCTION;
        } else if (binding instanceof ICPPNamespace || binding instanceof ICPPNamespaceAlias) {
            searchFor = ICSearchConstants.NAMESPACE;
        } else if (binding instanceof ICPPField) {
            searchFor = ICSearchConstants.FIELD;
        } else if (binding instanceof IEnumerator) {
            searchFor = ICSearchConstants.ENUMTOR;
        } else if (binding instanceof ICPPMethod) {
            searchFor = ICSearchConstants.METHOD;
        } else if (binding instanceof IMacroBinding) {
            searchFor = ICSearchConstants.MACRO;
        } else if (binding instanceof ITypedef) {
            searchFor = ICSearchConstants.TYPEDEF;
        } else if (binding instanceof IVariable) {
            searchFor = ICSearchConstants.VAR;
        } else if (binding instanceof ICPPClassType) {
            searchFor = ICSearchConstants.CLASS;
        } else if (binding instanceof ICompositeType) {
            try {
                switch(((ICompositeType)binding).getKey()) {
                case ICompositeType.k_struct:
                    searchFor = ICSearchConstants.CLASS_STRUCT;
                    break;
                case ICompositeType.k_union:
                    searchFor = ICSearchConstants.UNION;
                    break;
                }
            } catch (DOMException e) {
                searchFor = ICSearchConstants.UNKNOWN_SEARCH_FOR;
            }
        } else if (binding instanceof IEnumeration) {
            searchFor = ICSearchConstants.ENUM;
        } else {
            searchFor = ICSearchConstants.UNKNOWN_SEARCH_FOR;
        }
        
        return CSearchPattern.createPattern(DOMSearchUtil.getSearchPattern(searchName), searchFor, limitTo, ICSearchConstants.EXACT_MATCH, caseSensitive);
    }
    
    private static SearchFor createSearchFor( IBinding binding ) {
        SearchFor searchFor = null;
        if (binding instanceof IFunction) {
            searchFor = ICSearchConstants.FUNCTION;
        } else if (binding instanceof ICPPNamespace || binding instanceof ICPPNamespaceAlias) {
            searchFor = ICSearchConstants.NAMESPACE;
        } else if (binding instanceof ICPPField) {
            searchFor = ICSearchConstants.FIELD;
        } else if (binding instanceof IEnumerator) {
            searchFor = ICSearchConstants.ENUMTOR;
        } else if (binding instanceof ICPPMethod) {
            searchFor = ICSearchConstants.METHOD;
        } else if (binding instanceof IMacroBinding) {
            searchFor = ICSearchConstants.MACRO;
        } else if (binding instanceof ITypedef) {
            searchFor = ICSearchConstants.TYPEDEF;
        } else if (binding instanceof IVariable) {
            searchFor = ICSearchConstants.VAR;
        } else if (binding instanceof ICPPClassType) {
            searchFor = ICSearchConstants.CLASS;
        } else if (binding instanceof ICompositeType) {
            try {
                switch(((ICompositeType)binding).getKey()) {
                case ICompositeType.k_struct:
                    searchFor = ICSearchConstants.CLASS_STRUCT;
                    break;
                case ICompositeType.k_union:
                    searchFor = ICSearchConstants.UNION;
                    break;
                }
            } catch (DOMException e) {
                searchFor = ICSearchConstants.UNKNOWN_SEARCH_FOR;
            }
        } else if (binding instanceof IEnumeration) {
            searchFor = ICSearchConstants.ENUM;
        } else {
            searchFor = ICSearchConstants.UNKNOWN_SEARCH_FOR;
        }
        return searchFor;
    }
    
    /**
     * This is used to get an array of selected names in an IASTTranslationUnit based on the offset 
     * and length into that IASTTranslationUnit.
     * 
     * ex: IASTTranslationUnit contains: int foo;
     * then getSelectedNamesFrom(file, 4, 3) will return the IASTName corresponding to foo 
     * 
     * @param tu 
     * @param offset
     * @param length
     * @param lang
     * @return
     */
    public static IASTName[] getSelectedNamesFrom(IASTTranslationUnit tu, int offset, int length, ParserLanguage lang) {
        IASTNode node = null;
        try{
            node = tu.selectNodeForLocation(tu.getFilePath(), offset, length);
        } 
        catch (ParseError er){}
        catch ( VirtualMachineError vmErr){
            if (vmErr instanceof OutOfMemoryError){
                org.eclipse.cdt.internal.core.model.Util.log(null, "Open Declarations Out Of Memory error: " + vmErr.getMessage() + " on File: " + tu.getContainingFilename(), ICLogConstants.CDT); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        catch (Exception ex){}
        
        finally{
            if (node == null){
                return EMPTY_NAME_LIST;
            }
        }
    
        if (node instanceof IASTName) {
            IASTName[] results = new IASTName[1];
            results[0] = (IASTName)node;
            return results;
        }
        
        ASTVisitor collector = null;
        if (lang == ParserLanguage.CPP) {
            collector = new CPPNameCollector();
        } else {
            collector = new CNameCollector();
        }
        
        node.accept( collector );
        
        List names = null;
        if (collector instanceof CPPNameCollector) {
            names = ((CPPNameCollector)collector).nameList;
        } else {
            names = ((CNameCollector)collector).nameList;
        }
        
        IASTName[] results = new IASTName[names.size()];
        for(int i=0; i<names.size(); i++) {
            if (names.get(i) instanceof IASTName)
                results[i] = (IASTName)names.get(i);
        }
        
        return results;
    }

    
    /**
     * This is used to get an array of selected names in an IFile based on the offset and length 
     * into that IFile.
     * 
     * NOTE:  Invoking this method causes a parse, if an IASTTranslationUnit is already obtained then
     * invoke getSelectedNamesFrom(IASTTranslationUnit, int, int, ParserLanguage) instead.
     * 
     * ex: IFile contains: int foo;
     * then getSelectedNamesFrom(file, 4, 3) will return the IASTName corresponding to foo 
     * 
     * @param file the IFile whose selection 
     * @param offset
     * @param length
     * @return
     */
    public static IASTName[] getSelectedNamesFrom(IFile file, int offset, int length) {
        IASTNode node = null;
        IASTTranslationUnit tu = null;
        try {
            tu = CDOM.getInstance().getASTService().getTranslationUnit(
                    file,
                    CDOM.getInstance().getCodeReaderFactory(
                            CDOM.PARSE_WORKING_COPY_WHENEVER_POSSIBLE));
        } catch (IASTServiceProvider.UnsupportedDialectException e) {
            return EMPTY_NAME_LIST;
        }
        try{
            node = tu.selectNodeForLocation(file.getRawLocation().toOSString(), offset, length);
        } 
        catch (ParseError er){}
        catch ( VirtualMachineError vmErr){
            if (vmErr instanceof OutOfMemoryError){
                org.eclipse.cdt.internal.core.model.Util.log(null, "Open Declarations Out Of Memory error: " + vmErr.getMessage() + " on File: " + file.getName(), ICLogConstants.CDT); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        catch (Exception ex){}
        
        finally{
            if (node == null){
                return EMPTY_NAME_LIST;
            }
        }
    
        if (node instanceof IASTName) {
            IASTName[] results = new IASTName[1];
            results[0] = (IASTName)node;
            return results;
        }
        
        ASTVisitor collector = null;
        if (getLanguageFromFile(file) == ParserLanguage.CPP) {
            collector = new CPPNameCollector();
        } else {
            collector = new CNameCollector();
        }
        
        node.accept( collector );
        
        List names = null;
        if (collector instanceof CPPNameCollector) {
            names = ((CPPNameCollector)collector).nameList;
        } else {
            names = ((CNameCollector)collector).nameList;
        }
        
        IASTName[] results = new IASTName[names.size()];
        for(int i=0; i<names.size(); i++) {
            if (names.get(i) instanceof IASTName)
                results[i] = (IASTName)names.get(i);
        }
        
        return results;
    }
    
    /**
    * This retrieves the ParserLanguage from an IFile.
    *
    * @param file
    * @return
    */
    public static ParserLanguage getLanguageFromFile(IFile file) {
        IProject project = file.getProject();
        ICFileType type = CCorePlugin.getDefault().getFileType(project, file.getFullPath().lastSegment());
        String lid = type.getLanguage().getId();
        if ( lid != null && lid.equals(ICFileTypeConstants.LANG_CXX) ) {
            return ParserLanguage.CPP;
        }
        
        return ParserLanguage.C;
    }

    /**
     * This is used to get the names from the TU that the IASTName searchName belongs to.
     * 
     * @param searchName the IASTName whose references/delcarations are to be retrieved
     * @param limitTo used to specify whether to get declarations, references, or both, one of: 
     * ( CSearchPattern.DECLARATION | CSearchPattern.REFERENCES | CSearchPattern.ALL_OCCURRENCES ) 
     * @return IASTName[] declarations, references, or both depending on limitTo that correspond to the IASTName searchName searched for
     */
    public static IASTName[] getNamesFromDOM(IASTName searchName, LimitTo limitTo) {
		IASTName[] names = null;
		IASTTranslationUnit tu = searchName.getTranslationUnit();
		
		if (tu == null) {
			return BLANK_NAME_ARRAY;
		}
		
		IBinding binding = searchName.resolveBinding();
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
		
		return names;
    }
	
	private static IASTName[] getNames(IASTTranslationUnit tu, IBinding binding, LimitTo limitTo) {
        IASTName[] names = null;
		if (limitTo == ICSearchConstants.DECLARATIONS) {
            names = tu.getDeclarations(binding);
        } else if (limitTo == ICSearchConstants.REFERENCES) {
            names = tu.getReferences(binding);
        } else if (limitTo == ICSearchConstants.DEFINITIONS) {
            names = tu.getDefinitions(binding);
        } else if (limitTo == ICSearchConstants.ALL_OCCURRENCES){
            names = tu.getDeclarations(binding);
            names = (IASTName[])ArrayUtil.addAll(IASTName.class, names, tu.getReferences(binding));
        } else {  // assume ALL
            names = tu.getDeclarations(binding);
            names = (IASTName[])ArrayUtil.addAll(IASTName.class, names, tu.getReferences(binding));
        }
		
		return names;
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
        ICFileType type = CCorePlugin.getDefault().getFileType(project, path.lastSegment());
        boolean isHeader= type.isHeader();
        if( isHeader ) 
            return ParserLanguage.CPP; // assumption
        String lid = type.getLanguage().getId();
        if( lid.equals(ICFileTypeConstants.LANG_CXX))
            return ParserLanguage.CPP;
        if( lid.equals( ICFileTypeConstants.LANG_C ) )
            return ParserLanguage.C;
        return ParserLanguage.CPP;
    }
	
	/**
	 * Generates a search pattern String based on the IASTName passed as a parameter.
	 * 
	 * Used to generate a string to present to the user as well as a string used by
	 * the SearchEngine to parse for qualified names and parameters.
	 * 
	 * @param name
	 * @return
	 */
	public static String getSearchPattern(IASTName name) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("::"); //$NON-NLS-1$
        
        String[] namespaces = null;
        
        IASTNode parent = name.getParent();
        while(!(parent instanceof IASTTranslationUnit) && parent != null) {
            if (parent instanceof ICPPASTNamespaceDefinition) {
                namespaces = (String[])ArrayUtil.append(String.class, namespaces, ((ICPPASTNamespaceDefinition)parent).getName().toString());
            }
            parent = parent.getParent();
        }
        
        if (namespaces != null && namespaces.length > 0) {
            for( int i=namespaces.length-1; i>=0; i-- ) {
                if (namespaces[i] != null) {
                    buffer.append(namespaces[i]);
                    buffer.append("::"); //$NON-NLS-1$
                }
            }
        }
        
		if (name instanceof CPPASTName && name.getParent() instanceof CPPASTQualifiedName) {
			IASTName[] names = ((CPPASTQualifiedName)name.getParent()).getNames();
			for(int i=0; i<names.length; i++) {
				if (i != 0) buffer.append("::"); //$NON-NLS-1$
				buffer.append(names[i].toString());
			}
		} else {
			buffer.append(name.toString());
		}
		
	 	if( name.resolveBinding() instanceof IFunction ){
			try {
				IBinding binding = name.resolveBinding();
				IFunctionType type = ((IFunction)binding).getType();
				
				buffer.append("("); //$NON-NLS-1$
				if (binding instanceof ICExternalBinding) {
					buffer.append("..."); //$NON-NLS-1$
				} else {
					IType[] parms = type.getParameterTypes();
					for( int i = 0; i < parms.length; i++ ){
						if( i != 0 )
							buffer.append(", "); //$NON-NLS-1$
						buffer.append(ASTTypeUtil.getType(parms[i]));
					}
				}
				buffer.append(")"); //$NON-NLS-1$
			} catch (DOMException e) {
				buffer = new StringBuffer();
				buffer.append(name.toString());
			}
	 	}

		return buffer.toString();
	}
}
