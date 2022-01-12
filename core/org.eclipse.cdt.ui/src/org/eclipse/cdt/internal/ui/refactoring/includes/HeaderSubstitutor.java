/*******************************************************************************
 * Copyright (c) 2012, 2014 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.includes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.internal.corext.codemanipulation.IncludeInfo;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;

public class HeaderSubstitutor {
	private static int HEURISTIC_SCORE_NOT_AUTO_EXPORTED_HEADER = 0x1;
	private static int HEURISTIC_SCORE_NAME_MATCH = 0x2;
	private static int HEURISTIC_SCORE_NO_EXTENSION = 0x4;
	private static int HEURISTIC_SCORE_MAX = HEURISTIC_SCORE_NOT_AUTO_EXPORTED_HEADER + HEURISTIC_SCORE_NAME_MATCH
			+ HEURISTIC_SCORE_NO_EXTENSION;

	private final IncludeCreationContext fContext;
	private IncludeMap[] fIncludeMaps;
	private SymbolExportMap fSymbolExportMap;

	public HeaderSubstitutor(IncludeCreationContext context) {
		fContext = context;
		fIncludeMaps = new IncludeMap[] { new IncludeMap(true), new IncludeMap(false) };
		IPreferencesService preferences = Platform.getPreferencesService();
		IScopeContext[] scopes = PreferenceConstants.getPreferenceScopes(context.getProject());
		String str = preferences.getString(CUIPlugin.PLUGIN_ID, PreferenceConstants.INCLUDES_HEADER_SUBSTITUTION, null,
				scopes);
		if (str != null) {
			List<HeaderSubstitutionMap> maps = HeaderSubstitutionMap.deserializeMaps(str);
			for (HeaderSubstitutionMap map : maps) {
				if (!map.isCppOnly() || fContext.isCXXLanguage()) {
					fIncludeMaps[0].addAllMappings(map.getUnconditionalSubstitutionMap());
					fIncludeMaps[1].addAllMappings(map.getOptionalSubstitutionMap());
				}
			}
		}
		addHeaderDerivedMappings();
		fIncludeMaps[0].transitivelyClose();
		fIncludeMaps[1].transitivelyClose();

		fSymbolExportMap = new SymbolExportMap();
		str = preferences.getString(CUIPlugin.PLUGIN_ID, PreferenceConstants.INCLUDES_SYMBOL_EXPORTING_HEADERS, null,
				scopes);
		if (str != null) {
			List<SymbolExportMap> maps = SymbolExportMap.deserializeMaps(str);
			for (SymbolExportMap map : maps) {
				fSymbolExportMap.addAllMappings(map);
			}
		}
	}

	private void addHeaderDerivedMappings() {
		try {
			for (IIndexFile file : fContext.getIndex().getAllFiles()) {
				String replacement = file.getReplacementHeader();
				if (replacement != null) {
					IPath path = IndexLocationFactory.getAbsolutePath(file.getLocation());
					if (fContext.getCurrentDirectory().isPrefixOf(path)) {
						// "IWYU pragma: private" does not affect inclusion from files under
						// the directory where the header is located.
						continue;
					}

					IncludeInfo includeInfo = fContext.getIncludeForHeaderFile(path);
					if (includeInfo == null)
						continue;

					if (replacement.isEmpty()) {
						IIndexInclude[] includedBy = fContext.getIndex().findIncludedBy(file, IIndex.DEPTH_ZERO);
						for (IIndexInclude include : includedBy) {
							IPath includer = IndexLocationFactory.getAbsolutePath(include.getIncludedByLocation());
							IncludeInfo replacementInfo = fContext.getIncludeForHeaderFile(includer);
							if (replacementInfo != null) {
								fIncludeMaps[0].addMapping(includeInfo, replacementInfo);
							}
						}
					} else {
						String[] headers = replacement.split(","); //$NON-NLS-1$
						for (String header : headers) {
							if (!header.isEmpty()) {
								char firstChar = header.charAt(0);
								IncludeInfo replacementInfo;
								if (firstChar == '"' || firstChar == '<') {
									replacementInfo = new IncludeInfo(header);
								} else {
									replacementInfo = new IncludeInfo(header, includeInfo.isSystem());
								}
								fIncludeMaps[0].addMapping(includeInfo, replacementInfo);
							}
						}
					}
				}
			}
		} catch (CoreException e) {
			CUIPlugin.log(e);
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
		String filename = includeInfo.getName();
		if (!fContext.isHeaderFile(filename) || fContext.isAutoExportedFile(filename)) {
			return null;
		}
		return fContext.resolveInclude(includeInfo);
	}

	public IPath getPreferredRepresentativeHeader(IPath path) {
		IncludeInfo includeInfo = fContext.getIncludeForHeaderFile(path);
		if (includeInfo == null)
			return path;
		List<IncludeInfo> candidates = new ArrayList<>();
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
		IPath firstIncludedPreviously = null;
		for (IncludeInfo candidate : candidates) {
			IPath header = fContext.resolveInclude(candidate);
			if (header != null) {
				if (fContext.isIncluded(header))
					return header;
				if (firstResolved == null)
					firstResolved = header;
				if (firstIncludedPreviously == null && fContext.wasIncludedPreviously(header))
					firstIncludedPreviously = header;
			}
		}

		return firstIncludedPreviously != null ? firstIncludedPreviously : firstResolved != null ? firstResolved : path;
	}

	/**
	 * Performs heuristic header substitution.
	 *
	 * @param request the inclusion request
	 * @param preferredHeader the header selected using regular header substitution rules
	 */
	public IPath getPreferredRepresentativeHeaderByHeuristic(InclusionRequest request, IPath preferredHeader) {
		if (!fContext.isCXXLanguage())
			return preferredHeader;

		String symbolName = request.getBinding().getName();
		int bestScore = getScore(preferredHeader.toString(), symbolName);
		if (bestScore == HEURISTIC_SCORE_MAX)
			return preferredHeader; // Nothing can be better than preferredHeader.

		IIndexFile bestCandidate = null;
		Set<IIndexFile> indexFiles = request.getDeclaringFiles().keySet();
		ArrayDeque<IIndexFile> front = new ArrayDeque<>();
		HashSet<IIndexFile> processed = new HashSet<>();
		front.addAll(indexFiles);
		processed.addAll(indexFiles);

		try {
			// Look for headers matching by name and headers without an extension.
			while (!front.isEmpty()) {
				IIndexFile file = front.remove();
				String path = IncludeUtil.getPath(file);

				int score = getScore(path, symbolName);
				if (score > bestScore) {
					bestScore = score;
					bestCandidate = file;
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

			if (bestCandidate != null)
				return IndexLocationFactory.getAbsolutePath(bestCandidate.getLocation());
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}

		return preferredHeader;
	}

	private int getScore(String path, String symbolName) {
		int score = 0;
		if (fContext.isHeaderFile(path) && !fContext.isAutoExportedFile(path))
			score += HEURISTIC_SCORE_NOT_AUTO_EXPORTED_HEADER;
		if (getFilename(path).equalsIgnoreCase(symbolName))
			score += HEURISTIC_SCORE_NAME_MATCH;
		if (!hasExtension(path))
			score += HEURISTIC_SCORE_NO_EXTENSION;
		return score;
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
