/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * IBM Corporation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.search;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCStructure;
import org.eclipse.cdt.internal.core.pdom.dom.cpp.PDOMCPPClassType;
import org.eclipse.cdt.internal.core.pdom.dom.cpp.PDOMCPPLinkage;

import org.eclipse.cdt.internal.ui.util.Messages;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;

/**
 * @author Doug Schaefer
 *
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
		
		this.patternStr = patternStr;
		
		// Parse the pattern string
		List patternList = new ArrayList();
    	StringBuffer buff = new StringBuffer();
    	int n = patternStr.length();
    	for (int i = 0; i < n; ++i) {
    		char c = patternStr.charAt(i);
    		switch (c) {
    		case '*':
    			buff.append(".*"); //$NON-NLS-1$
    			break;
    		case '?':
    			buff.append('.');
    			break;
    		case '.':
    		case ':':
    			if (buff.length() > 0) {
    				if (isCaseSensitive)
    					patternList.add(Pattern.compile(buff.toString()));
    				else
    					patternList.add(Pattern.compile(buff.toString(),Pattern.CASE_INSENSITIVE));
    				buff = new StringBuffer();
    			}
    			break;
   			default:
    			buff.append(c);
    		}
    	}
    	
    	if (buff.length() > 0)
    	{
			if (isCaseSensitive)
				patternList.add(Pattern.compile(buff.toString()));
			else
				patternList.add(Pattern.compile(buff.toString(),Pattern.CASE_INSENSITIVE));
    	}
	    
    	pattern = (Pattern[])patternList.toArray(new Pattern[patternList.size()]); 
	}
	
	public IStatus run(IProgressMonitor monitor) throws OperationCanceledException {
		try {
			for (int i = 0; i < projects.length; ++i)
				searchProject(projects[i], monitor);
			return Status.OK_STATUS;
		} catch (CoreException e) {
			return e.getStatus();
		}
	}
	
	private void searchProject(ICProject project, IProgressMonitor monitor) throws CoreException {
		IPDOM pdom = CCorePlugin.getPDOMManager().getPDOM(project);

		try {
			pdom.acquireReadLock();
		} catch (InterruptedException e) {
			return;
		}

		try {
			IBinding[] bindings = pdom.findBindings(pattern, monitor);
			for (int i = 0; i < bindings.length; ++i) {
				PDOMBinding pdomBinding = (PDOMBinding)bindings[i];
				
				//check for the element type of this binding and create matches if 
				//the element type checkbox is checked in the C/C++ Search Page
				
				//TODO search for macro
				
				if ((flags & FIND_ALL_TYPES) == FIND_ALL_TYPES)
				{
					createMatches(pdomBinding.getLinkage().getLanguage(), pdomBinding);
				}
				else
				{					
					//C++
					if (pdomBinding.getLinkage() instanceof PDOMCPPLinkage)
					{
						switch (pdomBinding.getNodeType()) {
						case PDOMCPPLinkage.CPPCLASSTYPE:
						{
							switch (((PDOMCPPClassType)pdomBinding).getKey())
							{
								case ICPPClassType.k_class:
								case ICompositeType.k_struct:
									if (((flags & FIND_CLASS_STRUCT) == FIND_CLASS_STRUCT))
									{
										createMatches(pdomBinding.getLinkage().getLanguage(), pdomBinding);
									}
									break;
								case ICompositeType.k_union:
									if ((flags & FIND_UNION) == FIND_UNION)
									{
										createMatches(pdomBinding.getLinkage().getLanguage(), pdomBinding);
									}
									break;
								default:
									break;
							}
							break;
						}
						case PDOMCPPLinkage.CPPENUMERATION:	
							if ((flags & FIND_ENUM) == FIND_ENUM)
							{
								createMatches(pdomBinding.getLinkage().getLanguage(), pdomBinding);
							}
							break;
						case PDOMCPPLinkage.CPPENUMERATOR:	
							if ((flags & FIND_ENUMERATOR) == FIND_ENUMERATOR)
							{
								createMatches(pdomBinding.getLinkage().getLanguage(), pdomBinding);
							}
							break;
						case PDOMCPPLinkage.CPPFIELD:	
							if ((flags & FIND_FIELD) == FIND_FIELD)
							{
								createMatches(pdomBinding.getLinkage().getLanguage(), pdomBinding);
							}
							break;
						case PDOMCPPLinkage.CPPFUNCTION:
							if ((flags & FIND_FUNCTION) == FIND_FUNCTION)
							{
								createMatches(pdomBinding.getLinkage().getLanguage(), pdomBinding);
							}
							break;
						case PDOMCPPLinkage.CPPMETHOD:	
							if ((flags & FIND_METHOD) == FIND_METHOD)
							{
								createMatches(pdomBinding.getLinkage().getLanguage(), pdomBinding);
							}
							break;
						case PDOMCPPLinkage.CPPNAMESPACE:
						case PDOMCPPLinkage.CPPNAMESPACEALIAS:	
							if ((flags & FIND_NAMESPACE) == FIND_NAMESPACE)
							{
								createMatches(pdomBinding.getLinkage().getLanguage(), pdomBinding);
							}
							break;
						case PDOMCPPLinkage.CPPTYPEDEF:	
							if ((flags & FIND_TYPEDEF) == FIND_TYPEDEF)
							{
								createMatches(pdomBinding.getLinkage().getLanguage(), pdomBinding);
							}
							break;
						case PDOMCPPLinkage.CPPVARIABLE:	
							if ((flags & FIND_VARIABLE) == FIND_VARIABLE)
							{
								createMatches(pdomBinding.getLinkage().getLanguage(), pdomBinding);
							}
							break;
						default:
							break;
						}
					}
					//C
					else if (pdomBinding.getLinkage() instanceof PDOMCLinkage)
					{
						switch (pdomBinding.getNodeType()) {
						case PDOMCLinkage.CSTRUCTURE:
							switch (((PDOMCStructure)pdomBinding).getKey())
							{
								case ICompositeType.k_struct:
									if (((flags & FIND_CLASS_STRUCT) == FIND_CLASS_STRUCT))
									{
										createMatches(pdomBinding.getLinkage().getLanguage(), pdomBinding);
									}
									break;
								case ICompositeType.k_union:
									if ((flags & FIND_UNION) == FIND_UNION)
									{
										createMatches(pdomBinding.getLinkage().getLanguage(), pdomBinding);
									}
									break;
							}
							break;
						case PDOMCLinkage.CENUMERATION:	
							if ((flags & FIND_ENUM) == FIND_ENUM)
							{
								createMatches(pdomBinding.getLinkage().getLanguage(), pdomBinding);
							}
							break;
						case PDOMCLinkage.CENUMERATOR:	
							if ((flags & FIND_ENUMERATOR) == FIND_ENUMERATOR)
							{
								createMatches(pdomBinding.getLinkage().getLanguage(), pdomBinding);
							}
							break;
						case PDOMCLinkage.CFIELD:	
							if ((flags & FIND_FIELD) == FIND_FIELD)
							{
								createMatches(pdomBinding.getLinkage().getLanguage(), pdomBinding);
							}
							break;
						case PDOMCLinkage.CFUNCTION:
							if ((flags & FIND_FUNCTION) == FIND_FUNCTION)
							{
								createMatches(pdomBinding.getLinkage().getLanguage(), pdomBinding);
							}
							break;
						case PDOMCLinkage.CTYPEDEF:	
							if ((flags & FIND_TYPEDEF) == FIND_TYPEDEF)
							{
								createMatches(pdomBinding.getLinkage().getLanguage(), pdomBinding);
							}
							break;
						case PDOMCLinkage.CVARIABLE:	
							if ((flags & FIND_VARIABLE) == FIND_VARIABLE)
							{
								createMatches(pdomBinding.getLinkage().getLanguage(), pdomBinding);
							}
							break;
						default:
							break;
						}
					}
				}
			}
		} finally {
			pdom.releaseReadLock();
		}
	}
	
	public String getLabel() {
		return Messages.format(CSearchMessages.getString("PDOMSearchPatternQuery.PatternQuery_labelPatternInScope"), super.getLabel(), patternStr, scopeDesc); //$NON-NLS-1$
	}
	
}