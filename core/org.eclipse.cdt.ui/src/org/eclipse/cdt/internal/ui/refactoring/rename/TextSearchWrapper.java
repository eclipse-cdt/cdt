/*******************************************************************************
 * Copyright (c) 2004, 2008 Wind River Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.rename;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.search.core.text.TextSearchEngine;
import org.eclipse.search.core.text.TextSearchMatchAccess;
import org.eclipse.search.core.text.TextSearchRequestor;
import org.eclipse.search.core.text.TextSearchScope;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.utils.PathUtil;

import org.eclipse.cdt.internal.formatter.scanner.SimpleScanner;
import org.eclipse.cdt.internal.formatter.scanner.Token;

/**
 * Wraps the platform text search and uses a scanner to categorize the text-matches
 * by location (comments, string-literals, etc.).
 */
public class TextSearchWrapper {
    public final static int SCOPE_FILE = 1;
    public final static int SCOPE_WORKSPACE = 2;
    public final static int SCOPE_RELATED_PROJECTS = 3;
    public final static int SCOPE_SINGLE_PROJECT = 4;
    public final static int SCOPE_WORKING_SET = 5;

    private static class SearchScope extends TextSearchScope {
        public static SearchScope newSearchScope(IFile[] files, IWorkingSet ws) {
            IAdaptable[] adaptables= ws.getElements();
            ArrayList<IResource> resources = new ArrayList<IResource>();
            for (int i = 0; i < adaptables.length; i++) {
                IAdaptable adaptable = adaptables[i];
                IResource resource= (IResource) adaptable.getAdapter(IResource.class);
                if (resource != null) {
                    resources.add(resource);
                }
            }
            return newSearchScope(files, resources.toArray(new IResource[resources.size()]));
		}
        
		public static SearchScope newSearchScope(IFile[] files, IResource[] roots) {
			if (files != null) {
				ArrayList<IResource> resources = new ArrayList<IResource>(files.length + roots.length);
				for (IFile file : files) {
					if (!isInForest(file, roots)) {
						resources.add(file);
					}
				}
				Collections.addAll(resources, roots);
				roots = resources.toArray(new IResource[resources.size()]);
			}
			return new SearchScope(roots);
		}

		/**
		 * Checks is a file belongs to one of the given containers.
		 */
        private static boolean isInForest(IResource file, IResource[] roots) {
        	IPath filePath = file.getFullPath();
        	for (IResource root : roots) {
        		if (PathUtil.isPrefix(root.getFullPath(), filePath)) {
        			return true;
        		}
        	}
			return false;
		}

		private IResource[] fRootResources;
        private ArrayList<Matcher> fFileMatcher= new ArrayList<Matcher>();

        private SearchScope(IResource[] roots) {
            fRootResources= roots;
        }

		@Override
		public IResource[] getRoots() {
            return fRootResources;
        }

        @Override
		public boolean contains(IResourceProxy proxy) {
            if (proxy.isDerived()) {
                return false;
            }
            if (proxy.getType() == IResource.FILE) {
                return containsFile(proxy.getName());
            }
            return true;
		}

		private boolean containsFile(String name) {
            for (Iterator<Matcher> iter = fFileMatcher.iterator(); iter.hasNext();) {
                Matcher matcher = iter.next();
                matcher.reset(name);
                if (matcher.matches()) {
                    return true;
                }
            }
            return false;
        }

        public void addFileNamePattern(String filePattern) {
            Pattern p= Pattern.compile(filePatternToRegex(filePattern));
            fFileMatcher.add(p.matcher("")); //$NON-NLS-1$
		}

        private String filePatternToRegex(String filePattern) {
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < filePattern.length(); i++) {
                char c = filePattern.charAt(i);
                switch (c) {
                case '\\':
                case '(':
                case ')':
                case '{':
                case '}':
                case '.':
                case '[':
                case ']':
                case '$':
                case '^':
                case '+':
                case '|':
                    result.append('\\');
                    result.append(c);
                    break;
                case '?':
                    result.append('.');
                    break;
                case '*':
                    result.append(".*"); //$NON-NLS-1$
                    break;
                default:
                    result.append(c);
                    break;
                }
            }
            return result.toString();
        }
    }

    public TextSearchWrapper() {
    }
    
    private TextSearchScope createSearchScope(IFile[] files, int scope, IFile file,
    		String workingSetName, String[] patterns) {
        switch (scope) {
        	case SCOPE_WORKSPACE:
        	    return defineSearchScope(files, file.getWorkspace().getRoot(), patterns);
        	case SCOPE_SINGLE_PROJECT:
        	    return defineSearchScope(files, file.getProject(), patterns);
        	case SCOPE_FILE:
        	    return defineSearchScope(files, file, patterns);
        	case SCOPE_WORKING_SET: {
        	    return defineWorkingSetAsSearchScope(files, workingSetName, patterns);
        	}
        }
	    return defineRelatedProjectsAsSearchScope(files, file.getProject(), patterns);
    }
    
    private TextSearchScope defineRelatedProjectsAsSearchScope(IFile[] files, IProject project, String[] patterns) {
        HashSet<IProject> projects= new HashSet<IProject>();
        LinkedList<IProject> workThrough= new LinkedList<IProject>();
        workThrough.add(project);
        while (!workThrough.isEmpty()) {
            IProject proj= workThrough.removeLast();
            if (projects.add(proj)) {
                try {
                    workThrough.addAll(Arrays.asList(proj.getReferencedProjects()));
                    workThrough.addAll(Arrays.asList(proj.getReferencingProjects()));
                } catch (CoreException e) {
                    // need to ignore
                }
            }
        }
        IResource[] roots= projects.toArray(new IResource[projects.size()]);
        return defineSearchScope(files, roots, patterns);
    }

    private TextSearchScope defineWorkingSetAsSearchScope(IFile[] files, String workingSetName, String[] patterns) {
    	IWorkingSet workingSet = workingSetName != null ?
        	PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSet(workingSetName) :
        	null;	
		SearchScope result= workingSet != null ?
				SearchScope.newSearchScope(files, workingSet) :
				SearchScope.newSearchScope(files, new IResource[0]);
		applyFilePatterns(result, patterns);
		return result;
    }

    private void applyFilePatterns(SearchScope scope, String[] patterns) {
        for (String pattern : patterns) {
            scope.addFileNamePattern(pattern);
        }
    }

    private TextSearchScope defineSearchScope(IFile[] files, IResource root, String[] patterns) {
    	SearchScope result= SearchScope.newSearchScope(files, new IResource[] { root }); 
        applyFilePatterns(result, patterns);
        return result;
    }
    
    private TextSearchScope defineSearchScope(IFile[] files, IResource[] roots, String[] patterns) {
    	SearchScope result= SearchScope.newSearchScope(files, roots);           
        applyFilePatterns(result, patterns);
        return result;
    }
    
    /**
     * Searches for a given word.
     *
     * @param filesToSearch The files to search.
     * @param scope Together with {@code file} and {@code workingSet} defines set of additional
     *     file to search. One of SCOPE_FILE, SCOPE_WORKSPACE, SCOPE_RELATED_PROJECTS,
     *     SCOPE_SINGLE_PROJECT, or SCOPE_WORKING_SET.
     * @param scopeAnchor The file used as an anchor for the scope.
     * @param workingSet The name of a working set. Ignored if {@code scope} is not
     *     SCOPE_WORKING_SET.
     * @param patterns File name patterns.
     * @param word The word to search for.
     * @param monitor A progress monitor.
     * @param target The list that gets populated with search results.
     */
    public IStatus searchWord(IFile[] filesToSearch, int scope, IFile scopeAnchor, String workingSet,
    		String[] patterns, String word, IProgressMonitor monitor,
    		final List<CRefactoringMatch> target) {
        int startPos= target.size();
        TextSearchEngine engine= TextSearchEngine.create();
        StringBuilder searchPattern= new StringBuilder(word.length() + 8);
        searchPattern.append("\\b"); //$NON-NLS-1$
        searchPattern.append("\\Q"); //$NON-NLS-1$
        searchPattern.append(word);
        searchPattern.append("\\E"); //$NON-NLS-1$
        searchPattern.append("\\b"); //$NON-NLS-1$

        Pattern pattern= Pattern.compile(searchPattern.toString());
        
        TextSearchScope searchscope= createSearchScope(filesToSearch, scope, scopeAnchor, workingSet, patterns);
        TextSearchRequestor requestor= new TextSearchRequestor() {
            @Override
			public boolean acceptPatternMatch(TextSearchMatchAccess access) {
            	IFile file= access.getFile();
            	ICElement elem= CoreModel.getDefault().create(file);
            	if (elem instanceof ITranslationUnit) {
            		target.add(new CRefactoringMatch(file, 
            				access.getMatchOffset(), access.getMatchLength(), 0));
            	}
            	return true;
            }
        };
        IStatus result= engine.search(searchscope, requestor, pattern, 
        		new SubProgressMonitor(monitor, 95));
        categorizeMatches(target.subList(startPos, target.size()), 
                new SubProgressMonitor(monitor, 5));

        return result;
    }
    
    public void categorizeMatches(List<CRefactoringMatch> matches, IProgressMonitor monitor) {
        monitor.beginTask(RenameMessages.TextSearch_monitor_categorizeMatches, matches.size());
        IFile file= null;
        ArrayList<int[]> locations= null;
        for (Iterator<CRefactoringMatch> iter = matches.iterator(); iter.hasNext();) {
            CRefactoringMatch match = iter.next();
            IFile tfile= match.getFile();
            if (file == null || !file.equals(tfile)) {
                file= tfile;
                locations= new ArrayList<int[]>(); 
                computeLocations(file, locations);                
            }
            match.setLocation(findLocation(match, locations));            
            monitor.worked(1);
        }
    }

    final static Comparator<int[]> COMPARE_FIRST_INTEGER= new Comparator<int[]>() {
        public int compare(int[] o1, int[] o2) {
            return (o1)[0] - (o2)[0];
        }
    };

    private int findLocation(CRefactoringMatch match, ArrayList<int[]> states) {
        int pos = Collections.binarySearch(states, new int[] {match.getOffset()}, COMPARE_FIRST_INTEGER);
        if (pos < 0) {
            pos= -pos - 2;
            if (pos < 0) {
                pos = 0;
            }
        }
        int endOffset= match.getOffset() + match.getLength();
        int location= 0;
        while (pos < states.size()) {
            int[] info= states.get(pos);
            if (info[0] >= endOffset) {
                break;
            }
            location |= info[1];
            pos++;
        }
        return location;
    }

    private void computeLocations(IFile file, ArrayList<int[]> locations) {
        Reader reader;
        SimpleScanner scanner= new SimpleScanner();
        try {
            reader = new BufferedReader(new InputStreamReader(file.getContents(), file.getCharset()));
        } catch (CoreException e) {
            return;
        } catch (UnsupportedEncodingException e) {
            return;
        }
        try {
            scanner.initialize(reader, null);
            scanner.setReuseToken(true);
            Token token;
            int lastState= 0;
            while ((token= scanner.nextToken()) != null) {
                int state= CRefactory.OPTION_IN_CODE_REFERENCES;
                switch (token.getType()) {
                	case Token.tLINECOMMENT:
                    case Token.tBLOCKCOMMENT:
                        state= CRefactory.OPTION_IN_COMMENT;
                		break;
                    case Token.tSTRING:
                    case Token.tLSTRING:
                    case Token.tCHAR:
                        state= CRefactory.OPTION_IN_STRING_LITERAL;
                    	break;
                    case Token.tPREPROCESSOR:
                        state= CRefactory.OPTION_IN_PREPROCESSOR_DIRECTIVE;
                        break;
                    case Token.tPREPROCESSOR_DEFINE:
                        state= CRefactory.OPTION_IN_MACRO_DEFINITION;
                        break;
                    case Token.tPREPROCESSOR_INCLUDE:
                        state= CRefactory.OPTION_IN_INCLUDE_DIRECTIVE;
                        break;
                }
                if (state != lastState) {
                    locations.add(new int[] { token.getOffset(), state });
                    lastState= state;
                }
            }
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
            }
        }
    }
}
