/*******************************************************************************
 * Copyright (c) 2008, 2015 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.astwriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansionLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMMacroReferenceName;
import org.eclipse.core.runtime.CoreException;

/**
 * Recognizes nodes that are the result of an macro expansion and replaces them
 * with a suitable macro call.
 *
 * @author Emanuel Graf IFS
 */
public class MacroExpansionHandler {
	private final Scribe scribe;
	private int lastMacroExpOffset;
	private IASTTranslationUnit ast;
	private Map<String, List<IIndexName>> macroExpansion = new TreeMap<>();

	public MacroExpansionHandler(Scribe scribe) {
		this.scribe = scribe;
	}

	protected boolean checkisMacroExpansionNode(IASTNode node) {
		return checkisMacroExpansionNode(node, true);
	}

	protected boolean isStatementWithMixedLocation(IASTStatement node) {
		IASTNodeLocation[] nodeLocations = getNodeLocations(node);
		if (nodeLocations != null && nodeLocations.length > 1) {
			for (IASTNodeLocation loc : nodeLocations) {
				if (loc instanceof IASTMacroExpansionLocation) {
					if (!hasChildEnclosingMacroLocation(node, (IASTMacroExpansionLocation) loc)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean hasChildEnclosingMacroLocation(IASTNode node, IASTMacroExpansionLocation loc) {
		IASTNode[] children = node.getChildren();
		for (IASTNode child : children) {
			if (childEnclosesMacroLocation(child, loc)) {
				return true;
			}
		}
		return false;
	}

	private boolean childEnclosesMacroLocation(IASTNode child, IASTMacroExpansionLocation loc) {
		IASTNodeLocation[] childLocations = child.getNodeLocations();
		if (childLocations.length > 0 && hasMacroExpansionLocation(child, loc)) {
			if (childLocations[0] instanceof IASTMacroExpansionLocation) {
				IASTMacroExpansionLocation childMacroExpansionLocation = (IASTMacroExpansionLocation) childLocations[0];
				return macroContainsOnlyPartsOfChild(loc, childMacroExpansionLocation);
			} else if (childLocations[childLocations.length - 1] instanceof IASTMacroExpansionLocation) {
				IASTMacroExpansionLocation childMacroExpansionLocation = (IASTMacroExpansionLocation) childLocations[childLocations.length
						- 1];
				return macroContainsOnlyPartsOfChild(loc, childMacroExpansionLocation);
			}
			return true;
		}
		return false;
	}

	private boolean macroContainsOnlyPartsOfChild(IASTMacroExpansionLocation macroLocation,
			IASTMacroExpansionLocation childMacroLocation) {
		return childMacroLocation.getExpansion().getMacroDefinition()
				.equals(macroLocation.getExpansion().getMacroDefinition())
				&& childMacroLocation.getNodeOffset() == macroLocation.getNodeOffset()
				&& childMacroLocation.getNodeLength() == macroLocation.getNodeLength();
	}

	private boolean hasMacroExpansionLocation(IASTNode child, IASTMacroExpansionLocation macroLocation) {
		for (IASTNodeLocation childLocation : child.getNodeLocations()) {
			if (childLocation instanceof IASTMacroExpansionLocation) {
				return true;
			}
		}
		return false;
	}

	protected boolean macroExpansionAlreadyPrinted(IASTNode node) {
		IASTNodeLocation[] locs = node.getNodeLocations();
		if (locs.length == 1) {
			if (locs[0] instanceof IASTMacroExpansionLocation) {
				IASTMacroExpansionLocation macroNode = (IASTMacroExpansionLocation) locs[0];
				if (macroNode.asFileLocation().getNodeOffset() == lastMacroExpOffset) {
					return true;
				}
			}
		}
		return false;
	}

	protected boolean checkisMacroExpansionNode(IASTNode node, boolean write) {
		IASTTranslationUnit unit = node.getTranslationUnit();
		if (ast == null || !ast.equals(unit)) {
			initEmptyMacros(unit);
		}
		IASTNodeLocation[] locs = getNodeLocations(node);
		if (locs != null && locs.length == 1) {
			if (locs[0] instanceof IASTMacroExpansionLocation) {
				IASTMacroExpansionLocation macroNode = (IASTMacroExpansionLocation) locs[0];

				if (macroNode.asFileLocation().getNodeOffset() == lastMacroExpOffset) {
					return true;
				}
				if (write) {
					lastMacroExpOffset = macroNode.asFileLocation().getNodeOffset();
					node = node.getOriginalNode();
					scribe.print(node.getRawSignature());
				}
				return true;

			}
		}
		handleEmptyMacroExpansion(node);
		return false;
	}

	private IASTNodeLocation[] getNodeLocations(IASTNode node) {
		return node.getOriginalNode().getNodeLocations();
	}

	private void handleEmptyMacroExpansion(IASTNode node) {
		if (node.getTranslationUnit() == null)
			return;
		String file = node.getContainingFilename();
		List<IIndexName> exps = macroExpansion.get(file);
		if (exps != null && !exps.isEmpty()) {
			IASTFileLocation fileLocation = getFileLocation(node);
			if (fileLocation != null) {
				int nOff = fileLocation.getNodeOffset();
				for (IIndexName iIndexName : exps) {
					if (iIndexName instanceof PDOMMacroReferenceName) {
						PDOMMacroReferenceName mName = (PDOMMacroReferenceName) iIndexName;
						int eOff = mName.getFileLocation().getNodeOffset();
						int eLength = mName.getFileLocation().getNodeLength();
						if (eOff < nOff && Math.abs((eOff + eLength - nOff)) < 3) {
							scribe.print(mName.toString() + " "); //$NON-NLS-1$
						}
					}
				}
			}
		}
	}

	private IASTFileLocation getFileLocation(IASTNode node) {
		return node.getOriginalNode().getFileLocation();
	}

	private void initEmptyMacros(IASTTranslationUnit unit) {
		if (unit != null) {
			ast = unit;
			IIndex index = ast.getIndex();
			if (index != null) {
				macroExpansion = new TreeMap<>();
				IASTPreprocessorMacroDefinition[] md = ast.getMacroDefinitions();

				TreeSet<String> paths = new TreeSet<>();
				for (IASTPreprocessorIncludeStatement is : ast.getIncludeDirectives()) {
					if (!is.isSystemInclude()) {
						paths.add(is.getContainingFilename());
					}
				}
				paths.add(ast.getContainingFilename());

				for (IASTPreprocessorMacroDefinition iastPreprocessorMacroDefinition : md) {
					if (iastPreprocessorMacroDefinition.getExpansion().length() == 0) {
						try {
							IIndexMacro[] macroBinding = index.findMacros(
									iastPreprocessorMacroDefinition.getName().toCharArray(), IndexFilter.ALL, null);
							if (macroBinding.length > 0) {
								IIndexName[] refs = index.findReferences(macroBinding[0]);
								for (IIndexName iIndexName : refs) {
									String filename = iIndexName.getFileLocation().getFileName();
									List<IIndexName> fileList = macroExpansion.get(filename);
									if (paths.contains(filename)) {
										if (fileList == null) {
											fileList = new ArrayList<>();
											macroExpansion.put(filename, fileList);
										}
										fileList.add(iIndexName);
									}
								}
							}
						} catch (CoreException e) {
							e.printStackTrace();
						}
					}
				}
			} else {
				macroExpansion = Collections.emptyMap();
			}
		}
	}

	public void reset() {
		lastMacroExpOffset = -1;
	}
}
