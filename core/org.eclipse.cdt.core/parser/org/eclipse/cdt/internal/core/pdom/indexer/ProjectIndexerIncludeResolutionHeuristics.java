/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.pdom.indexer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.internal.core.dom.IIncludeFileResolutionHeuristics;
import org.eclipse.cdt.internal.core.pdom.ASTFilePathResolver;
import org.eclipse.cdt.internal.core.resources.ResourceLookup;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Heuristics for picking up includes from the project
 */
public class ProjectIndexerIncludeResolutionHeuristics implements IIncludeFileResolutionHeuristics {
	private static final String TRUE = "true"; //$NON-NLS-1$

	private IProject fProject;
	private IProject[] fProjects;
	private final ASTFilePathResolver fResolver;
	private final boolean fIgnoreCase;

	public ProjectIndexerIncludeResolutionHeuristics(IProject project, ASTFilePathResolver resolver) {
		fProject= project;
		fResolver= resolver;
		fIgnoreCase= resolver.isCaseInsensitiveFileSystem();
	}

	@Override
	public String findInclusion(String include, String currentFile) {
		final IIndexFileLocation ifl= fResolver.resolveASTPath(currentFile);
		if (ifl == null || ifl.getFullPath() == null) {
			return null;
		}
		
		if (fProject == null)
			return null;
		
		
		if (fProjects == null) {
			if (fProject.isOpen()) {
				String val= IndexerPreferences.get(fProject, IndexerPreferences.KEY_INCLUDE_HEURISTICS, TRUE);
				if (TRUE.equals(val)) {
					fProjects= getOpenReferencedProjects(fProject);
				}
			} 
			if (fProjects == null) { 
				fProject= null;
				return null;
			}
		}
		
		IFile[] files= ResourceLookup.findFilesByName(new Path(include), fProjects, fIgnoreCase);
		if (files.length == 0)
			return null;
		
		final IPath bestLocation = selectBest(files, ifl.getFullPath().toCharArray()).getLocation();
		if (bestLocation == null)
			return null;
		
		return bestLocation.toString();
	}
	
	
	private IResource selectBest(IFile[] files, char[] currentFullPath) {
		IFile best= files[0];
		int bestScore= computeScore(best.getFullPath().toString().toCharArray(), currentFullPath);
		
		for (int i = 1; i < files.length; i++) {
			IFile file= files[i];
			int score= computeScore(file.getFullPath().toString().toCharArray(), currentFullPath);
			if (score > bestScore) {
				bestScore= score;
				best= file;
			}
		}
		return best;
	}

	private int computeScore(char[] path1, char[] path2) {
		final int limit= Math.min(path1.length, path2.length);
		int match=0;
		for (int i = 0; i < limit; i++) {
			if (path1[i] != path2[i])
				break;
			if (path1[i] == '/')
				match= i;
		}
		// prefer shortest path with longest matches with 
		return (match << 16) - path1.length; 
	}

	private IProject[] getOpenReferencedProjects(IProject prj) {
		Set<IProject> result= new HashSet<IProject>();
		
		if (prj.isOpen()) {
			result.add(prj);

			List<IProject> projectsToSearch= new ArrayList<IProject>();
			projectsToSearch.add(prj);
			for (int i=0; i<projectsToSearch.size(); i++) {
				IProject project= projectsToSearch.get(i);
				IProject[] nextLevel;
				try {
					nextLevel= project.getReferencedProjects();
					for (IProject prjNextLevel : nextLevel) {
						if (prjNextLevel.isOpen() && result.add(prjNextLevel)) {
							projectsToSearch.add(prjNextLevel);
						}
					}
				} catch (CoreException e) {
				}
			}
		}
		return result.toArray(new IProject[result.size()]);
	}
}
