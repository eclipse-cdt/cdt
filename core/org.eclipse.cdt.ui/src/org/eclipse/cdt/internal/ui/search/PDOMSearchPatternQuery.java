/*******************************************************************************
 * Copyright (c) 2006, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     IBM Corporation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.search;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceAlias;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.ICElement;


/**
 * @author Doug Schaefer
 */
public class PDOMSearchPatternQuery extends PDOMSearchQuery {

	// First bit after the FINDs in PDOMSearchQuery.
	public static final int FIND_CLASS_STRUCT = 0x10;
	public static final int FIND_FUNCTION = 0x20;
	public static final int FIND_VARIABLE = 0x40;
	public static final int FIND_UNION = 0x100;
	public static final int FIND_METHOD = 0x200;
	public static final int FIND_FIELD = 0x400;
	public static final int FIND_ENUM = 0x1000;
	public static final int FIND_ENUMERATOR = 0x2000;
	public static final int FIND_NAMESPACE = 0x4000;
	public static final int FIND_TYPEDEF = 0x10000;
	public static final int FIND_MACRO = 0x20000;
	public static final int FIND_ALL_TYPES
		= FIND_CLASS_STRUCT | FIND_FUNCTION | FIND_VARIABLE
		| FIND_UNION | FIND_METHOD | FIND_FIELD | FIND_ENUM
		| FIND_ENUMERATOR | FIND_NAMESPACE | FIND_TYPEDEF | FIND_MACRO;
	
	private String scopeDesc;
	private String patternStr;
	private Pattern[] pattern;
	
	public PDOMSearchPatternQuery(
			ICElement[] scope,
			String scopeDesc,
			String patternStr,
			boolean isCaseSensitive,
			int flags) throws PatternSyntaxException {
		super(scope, flags);
		this.scopeDesc = scopeDesc;
		
		// remove spurious whitespace, which will make the search fail 100% of the time
		this.patternStr = patternStr.trim();
		
		// Parse the pattern string
		List<Pattern> patternList = new ArrayList<Pattern>();
    	StringBuffer buff = new StringBuffer();
    	int n = patternStr.length();
    	for (int i = 0; i < n; ++i) {
    		char c = patternStr.charAt(i);
    		switch (c) {
    		case '\\':
    			if (i+1 < n) {
    				switch(patternStr.charAt(i+1)) {
    				case '?':
    					buff.append("\\?"); //$NON-NLS-1$
    					break;
    				case '*':
    					buff.append("\\*"); //$NON-NLS-1$
    					break;
    				default:
    					buff.append('\\');
    				}
    			} else {
    				buff.append('\\');
    			}
    			break;
    		case '*':
    			buff.append(".*"); //$NON-NLS-1$
    			break;
    		case '?':
    			buff.append('.');
    			break;
    		case ':':
    			if (buff.length() > 0) {
    				if (isCaseSensitive)
    					patternList.add(Pattern.compile(buff.toString()));
    				else
    					patternList.add(Pattern.compile(buff.toString(),Pattern.CASE_INSENSITIVE));
    				buff = new StringBuffer();
    			}
    			break;
			case '|': case '+': case '^': case '(': case ')': case '[': case ']': 
				buff.append('\\').append(c);
				break;
   			default:
    			buff.append(c);
    		}
    	}
    	
    	if (buff.length() > 0) {
			if (isCaseSensitive)
				patternList.add(Pattern.compile(buff.toString()));
			else
				patternList.add(Pattern.compile(buff.toString(),Pattern.CASE_INSENSITIVE));
    	}
	    
    	pattern = patternList.toArray(new Pattern[patternList.size()]); 
	}
	
	@Override
	public IStatus runWithIndex(IIndex index, IProgressMonitor monitor) throws OperationCanceledException {
		try {
			IndexFilter filter= IndexFilter.ALL;
			IIndexBinding[] bindings = index.findBindings(pattern, false, filter, monitor);
			ArrayList<IIndexBinding> matchedBindings = new ArrayList<IIndexBinding>();
			for (int i = 0; i < bindings.length; ++i) {
				IIndexBinding pdomBinding = bindings[i];

				//check for the element type of this binding and create matches if 
				//the element type checkbox is checked in the C/C++ Search Page

				//TODO search for macro

				boolean matches= false;
				if ((flags & FIND_ALL_TYPES) == FIND_ALL_TYPES) {
					matches= true;
				} else if (pdomBinding instanceof ICompositeType) {
					ICompositeType ct= (ICompositeType) pdomBinding;
					switch (ct.getKey()) {
					case ICompositeType.k_struct:
					case ICPPClassType.k_class:
						matches= (flags & FIND_CLASS_STRUCT) != 0;
						break;
					case ICompositeType.k_union:
						matches= (flags & FIND_UNION) != 0;
						break;
					}
				} else if (pdomBinding instanceof IEnumeration) {
					matches= (flags & FIND_ENUM) != 0;
				} else if (pdomBinding instanceof IEnumerator) {
					matches= (flags & FIND_ENUMERATOR) != 0;
				} else if (pdomBinding instanceof IField) {
					matches= (flags & FIND_FIELD) != 0;
				} else if (pdomBinding instanceof ICPPMethod) {
					matches= (flags & FIND_METHOD) != 0;
				} else if (pdomBinding instanceof IVariable) {
					matches= (flags & FIND_VARIABLE) != 0;
				} else if (pdomBinding instanceof IFunction) {
					matches= (flags & FIND_FUNCTION) != 0;
				} else if (pdomBinding instanceof ICPPNamespace || pdomBinding instanceof ICPPNamespaceAlias) {
					matches= (flags & FIND_NAMESPACE) != 0;
				} else if (pdomBinding instanceof ITypedef) {
					matches= (flags & FIND_TYPEDEF) != 0;
				}
				if (matches) {
					matchedBindings.add(pdomBinding);
				}
			}
			if ((flags & FIND_MACRO) != 0 && pattern.length == 1) {
				bindings = index.findMacroContainers(pattern[0], filter, monitor);
				for (IIndexBinding indexBinding : bindings) {
					matchedBindings.add(indexBinding);
				}
			}
			createMatches(index, matchedBindings.toArray(new IIndexBinding[matchedBindings.size()]));
		} catch (CoreException e) {
			return e.getStatus();
		}
		
		return Status.OK_STATUS;
	}

	@Override
	public String getResultLabel(int numMatches) {
		String patternInScope = NLS.bind(
				CSearchMessages.PDOMSearchPatternQuery_PatternQuery_labelPatternInScope, patternStr, scopeDesc);
		return getResultLabel(patternInScope, numMatches); 
	}
}
