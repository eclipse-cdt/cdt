/*******************************************************************************
 * Copyright (c) 2015 Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jonah Graham (Kichwa Coders) - initial API and implementation to Add support for gdb's "set substitute-path" (Bug 472765)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.launching;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.core.sourcelookup.IMappingSourceContainer;
import org.eclipse.cdt.debug.core.sourcelookup.MappingSourceContainer;
import org.eclipse.cdt.debug.internal.core.sourcelookup.MapEntrySourceContainer;
import org.eclipse.cdt.dsf.debug.sourcelookup.DsfSourceLookupDirector;
import org.eclipse.cdt.dsf.gdb.service.IGDBSourceLookup;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;
import org.eclipse.debug.core.sourcelookup.containers.DefaultSourceContainer;

/**
 * A Source Lookup director that extends the standard DSF version to support the
 * GDB backend handling path substitutions using gdb's "set substitute-path"
 * mechanism. This director works in tandem with {@link IGDBSourceLookup}
 * service to synchronise GDB's path substitutions.
 * 
 * @since 5.0
 */
public class GdbSourceLookupDirector extends DsfSourceLookupDirector {
	private final DsfSession fSession;

	public GdbSourceLookupDirector(DsfSession session) {
		super(session);
		fSession = session;
	}

	@Override
	public void initializeParticipants() {
		/*
		 * We don't call super as we don't want DsfSourceLookupParticipant to be
		 * added explicitly. Instead we use GdbSourceLookupParticipant which
		 * extends DsfSourceLookupParticipant and does not use the mappings
		 * directly but relies on GDB's "set substitute-path".
		 */
		addParticipants(new ISourceLookupParticipant[] { new GdbSourceLookupParticipant(fSession) });
	}

	/**
	 * Return a map of substitutions with the Key being the compilation path and
	 * the Value being the machine local path.
	 * 
	 * @return map of substitutions
	 */
	public Map<String, String> getSubstitutionsPaths() {
		Map<String, String> entries = new HashMap<>();
		collectSubstitutionsPaths(getSourceContainers(), entries);
		return entries;
	}

	protected void collectSubstitutionsPaths(ISourceContainer[] containers, Map<String, String> entries) {
		for (ISourceContainer container : containers) {
			if (container instanceof MapEntrySourceContainer) {
				MapEntrySourceContainer sourceSubContainer = (MapEntrySourceContainer) container;

				String from = sourceSubContainer.getBackendPathStr();
				IPath to = sourceSubContainer.getLocalPath();
				if (from != null && to != null) {
					entries.put(from, to.toOSString());
				}
			} else if (container.isComposite()) {
				ISourceContainer[] childContainers;
				try {
					childContainers = container.getSourceContainers();
				} catch (CoreException e) {
					/*
					 * Consistent with other uses of getSourceContainers, we
					 * silently ignore these children.
					 */
					childContainers = new ISourceContainer[0];
				}
				if (container instanceof MappingSourceContainer) {
					MappingSourceContainer mappingSourceContainer = (MappingSourceContainer) container;
					if (mappingSourceContainer.isMappingWithBackendEnabled()) {
						collectSubstitutionsPaths(childContainers, entries);
					} else {
						/*
						 * This mapping has explicitly requested *not* to do a
						 * substitute, so don't recurse on children here.
						 */
					}
				} else {
					/*
					 * There can be MappingSourceContainers in
					 * DefaultSourceContainer, but not in other types of
					 * composite containers (e.g. a DirectorySourceContainer
					 * cannot contain a MappingSourceContainer).
					 * 
					 * It is important we don't recurse across all composites
					 * containers for performance reasons. If a
					 * DirectorySourceContainer was recursed here, then it could
					 * means recursing through the entire directory structure
					 * under that container.
					 */
					if (container instanceof DefaultSourceContainer) {
						collectSubstitutionsPaths(childContainers, entries);
					}
				}
			}
		}
	}

	/**
	 * Get the compilation path for the given sourceName. Unlike super's
	 * version, for {@link MappingSourceContainer}s where backend mapping is
	 * enabled this method is a no-op as in those cases the backend already
	 * matches.
	 */
	@Override
	public IPath getCompilationPath(String sourceName) {
		return getCompilationPath(getSourceContainers(), sourceName);
	}

	/**
	 * This method mirrors the logic of
	 * {@link #collectSubstitutionsPaths(ISourceContainer[], Map)} on which
	 * containers are iterated and excluded.
	 */
	protected IPath getCompilationPath(ISourceContainer[] containers, String sourceName) {
		for (ISourceContainer container : containers) {

			if (container instanceof IMappingSourceContainer) {
				IPath mappedPath = ((IMappingSourceContainer) container).getCompilationPath(sourceName);
				if (mappedPath != null) {
					if (container instanceof MappingSourceContainer
							&& ((MappingSourceContainer) container).isMappingWithBackendEnabled()) {
						/*
						 * This mapping is being handled by GDB backend (i.e. it was
						 * collected by collectSubstitutionsPaths to pass to gdb's
						 * "set substitute-path"). Because GDB is doing the
						 * translation on it, pass the local name to GDB, not the
						 * translated name.
						 */
						return new Path(sourceName);
					}
					return mappedPath;
				}
			} else if (container.isComposite()) {
				ISourceContainer[] childContainers = null;
				try {
					childContainers = container.getSourceContainers();
				} catch (CoreException e) {
					/*
					 * Consistent with other uses of getSourceContainers, we
					 * silently ignore these children.
					 */
				}
				if (childContainers != null) {
					IPath path = getCompilationPath(childContainers, sourceName);
					if (path != null) {
						return path;
					}
				}
			}
		}
		return null;
	}
}
