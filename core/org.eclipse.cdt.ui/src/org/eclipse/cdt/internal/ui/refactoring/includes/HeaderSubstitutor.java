/*******************************************************************************
 * Copyright (c) 2012, 2013 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.includes;

import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;

import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;

import org.eclipse.cdt.internal.core.resources.ResourceLookup;

public class HeaderSubstitutor {
	private final InclusionContext fContext;
	private IncludeMap[] fIncludeMaps;
	private SymbolExportMap fSymbolExportMap;

	public HeaderSubstitutor(InclusionContext context) {
		fContext = context;
		fIncludeMaps = new IncludeMap[] { new IncludeMap(true), new IncludeMap(false) };
		IPreferencesService preferences = Platform.getPreferencesService();
		IScopeContext[] scopes = PreferenceConstants.getPreferenceScopes(context.getProject());
		String str = preferences.getString(CUIPlugin.PLUGIN_ID,
				IncludePreferences.INCLUDES_HEADER_SUBSTITUTION, null, scopes);
		if (str != null) {
			List<HeaderSubstitutionMap> maps = HeaderSubstitutionMap.deserializeMaps(str);
			for (HeaderSubstitutionMap map : maps) {
				if (!map.isCppOnly() || fContext.isCXXLanguage()) {
					fIncludeMaps[0].addAllMappings(map.getUnconditionalSubstitutionMap());
					fIncludeMaps[1].addAllMappings(map.getOptionalSubstitutionMap());
				}
			}
		}

		fSymbolExportMap = new SymbolExportMap();
		str = preferences.getString(CUIPlugin.PLUGIN_ID,
				IncludePreferences.INCLUDES_SYMBOL_EXPORTING_HEADERS, null, scopes);
		if (str != null) {
			List<SymbolExportMap> maps = SymbolExportMap.deserializeMaps(str);
			for (SymbolExportMap map : maps) {
				fSymbolExportMap.addAllMappings(map);
			}
		}
	}

	/**
	 * Selects the header file to be used in an {@code #include} statement given the header file
	 * that needs to be included. Returns absolute path of the header if it can be uniquely
	 * determined, or {@code null} otherwise. The header is determined uniquely if there are no
	 * optional replacement headers for it, and there is no more that one unconditional replacement.
	 *
	 * @param path absolute path of the header to be included directly or indirectly
	 * @return absolute path of the header to be included directly
	 */
	public IPath getUniqueRepresentativeHeader(IPath path) {
		IncludeInfo includeInfo = fContext.getIncludeForHeaderFile(path);
		if (includeInfo == null)
			return null;
		IncludeMap[] maps = fIncludeMaps;
		for (IncludeMap map : maps) {
			if (map.isUnconditionalSubstitution()) {
				List<IncludeInfo> replacements = map.getMapping(includeInfo);
				if (replacements.size() == 1) {
					includeInfo = replacements.get(0);
				} else if (replacements.size() > 1) {
					return null;
				}
			}
		}
		for (IncludeMap map : maps) {
			if (!map.isUnconditionalSubstitution()) {
				if (!map.getMapping(includeInfo).isEmpty()) {
					return null;
				}
			}
		}
		return fContext.resolveInclude(includeInfo);
	}

	public IPath getPreferredRepresentativeHeader(IPath path) {
		IncludeInfo includeInfo = fContext.getIncludeForHeaderFile(path);
		if (includeInfo == null)
			return path;
		// TODO(sprigogin): Take fSymbolExportMap into account.
		List<IncludeInfo> candidates = new ArrayList<IncludeInfo>();
		candidates.add(includeInfo);
		IncludeMap[] maps = fIncludeMaps;
		for (IncludeMap map : maps) {
			for (int i = 0; i < candidates.size();) {
				IncludeInfo candidate = candidates.get(i);
				List<IncludeInfo> replacements = map.getMapping(candidate);
				int increment = 1;
				if (!replacements.isEmpty()) {
					if (map.isUnconditionalSubstitution()) {
						candidates.remove(i);
						increment = 0;
					}
					candidates.addAll(i, replacements);
					increment += replacements.size();
				}
				i += increment;
			}
		}
		IPath firstResolved = null;
		for (IncludeInfo candidate : candidates) {
			IPath header = fContext.resolveInclude(candidate);
			if (header != null) {
				if (fContext.isIncluded(header))
					return header;
				if (firstResolved == null)
					firstResolved = header;
			}
		}

		return firstResolved != null ? firstResolved : path;
	}

	/**
	 * Performs heuristic header substitution.
	 */
	public IPath getPreferredRepresentativeHeaderByHeuristic(InclusionRequest request) {
		Set<IIndexFile> indexFiles = request.getDeclaringFiles().keySet();
		String symbolName = request.getBinding().getName();
		ArrayDeque<IIndexFile> front = new ArrayDeque<IIndexFile>();
		HashSet<IIndexFile> processed = new HashSet<IIndexFile>();

		try {
			// Look for headers without an extension and a matching name.
			if (fContext.isCXXLanguage()) {
				front.addAll(indexFiles);
				processed.addAll(indexFiles);

				while (!front.isEmpty()) {
					IIndexFile file = front.remove();

					String path = IncludeUtil.getPath(file);

					if (!hasExtension(path) && getFilename(path).equalsIgnoreCase(symbolName)) {
						// A C++ header without an extension and with a name which matches the name
						// of the symbol which should be declared is a perfect candidate for inclusion.
						return IndexLocationFactory.getAbsolutePath(file.getLocation());
					}

					// Process the next level of the include hierarchy.
					IIndexInclude[] includes = fContext.getIndex().findIncludedBy(file, 0);
					for (IIndexInclude include : includes) {
						IIndexFile includer = include.getIncludedBy();
						if (!processed.contains(includer)) {
							front.add(includer);
							processed.add(includer);
						}
					}
				}
			}

			// Repeat the process, this time only looking for headers without an extension.
			front.clear();
			front.addAll(indexFiles);
			processed.clear();
			processed.addAll(indexFiles);

			while (!front.isEmpty()) {
				IIndexFile file = front.remove();

				String path = IncludeUtil.getPath(file);

				if (fContext.isCXXLanguage() && !hasExtension(path)) {
					// A C++ header without an extension is still a very good candidate for inclusion.
					return IndexLocationFactory.getAbsolutePath(file.getLocation());
				}

				// Process the next level of the include hierarchy.
				IIndexInclude[] includes = fContext.getIndex().findIncludedBy(file, 0);
				for (IIndexInclude include : includes) {
					IIndexFile includer = include.getIncludedBy();
					if (!processed.contains(includer)) {
						URI uri = includer.getLocation().getURI();
						if (IncludeUtil.isSource(includer, fContext.getProject()) || isWorkspaceFile(uri)) {
							return IndexLocationFactory.getAbsolutePath(file.getLocation());
						}
						front.add(includer);
						processed.add(includer);
					}
				}
			}
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}

		return request.getCandidatePaths().iterator().next();
	}

	/**
	 * Returns the set of headers exporting the given symbol. 
	 */
	public Set<IncludeInfo> getExportingHeaders(String symbol) {
		Set<IncludeInfo> headers = fSymbolExportMap.getMapping(symbol);
		if (headers == null)
			return Collections.emptySet();
		return headers;
	}

	/**
	 * Returns whether the given URI points within the workspace.
	 */
	private static boolean isWorkspaceFile(URI uri) {
		for (IFile file : ResourceLookup.findFilesForLocationURI(uri)) {
			if (file.exists()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns whether the given path has a file suffix, or not.
	 */
	private static boolean hasExtension(String path) {
		return path.indexOf('.', path.lastIndexOf('/') + 1) >= 0;
	}

	/**
	 * Returns the filename of the given path, without extension.
	 */
	private static String getFilename(String path) {
		int startPos = path.lastIndexOf('/') + 1;
		int endPos = path.lastIndexOf('.');
		if (endPos > startPos) {
			return path.substring(startPos, endPos);
		} else {
			return path.substring(startPos);
		}
	}
}
