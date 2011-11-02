/*******************************************************************************
 * Copyright (c) 2008, 2009 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.astwriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.cdt.core.dom.ast.IASTCopyLocation;
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
	private int lastMacroExpOffset;
	private final Scribe scribe;
	private IASTTranslationUnit tu;
	private Map<String, List<IIndexName>> macroExpansion = new TreeMap<String, List<IIndexName>>();

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
					return true;
				}
			}
		}
		return false;
	}

	protected boolean macroExpansionAlreadyPrinted(IASTNode node) {
		IASTNodeLocation[] locs = node.getNodeLocations();
		if (locs.length ==1) {
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
		if (tu == null || !tu.equals(unit)) {
			initEmptyMacros(unit);
		}
		IASTNodeLocation[] locs = getNodeLocations(node);
		if (locs != null && locs.length ==1) {
			if (locs[0] instanceof IASTMacroExpansionLocation) {
				IASTMacroExpansionLocation macroNode = (IASTMacroExpansionLocation) locs[0];

				if (macroNode.asFileLocation().getNodeOffset() == lastMacroExpOffset) {
					return true;
				}
				if (write) {
					lastMacroExpOffset = macroNode.asFileLocation().getNodeOffset();
					node = getOriginalNode(node);
					scribe.print(node.getRawSignature());
				}
				return true;

			}
		}
		handleEmptyMacroExpansion(node);
		return false;
	}

	private IASTNode getOriginalNode(IASTNode node) {
		IASTNodeLocation[] locs = node.getNodeLocations();
		if (locs != null && locs.length == 1 && locs[0] instanceof IASTCopyLocation) {
			node = ((IASTCopyLocation) locs[0]).getOriginalNode();
		}
		return node;
	}

	private IASTNodeLocation[] getNodeLocations(IASTNode node) {
		IASTNodeLocation[] locs = node.getNodeLocations();
		if (locs != null && locs.length == 1 && locs[0] instanceof IASTCopyLocation) {
			locs = ((IASTCopyLocation) locs[0]).getOriginalNode().getNodeLocations();
		}
		return locs;
	}

	private void handleEmptyMacroExpansion(IASTNode node) {
		if (node.getTranslationUnit() == null)return;
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
		IASTFileLocation fileLocation = node.getFileLocation();
		if (fileLocation == null) {
			IASTNodeLocation[] locs = node.getNodeLocations();
			if (locs != null && locs.length > 0 && locs[0] instanceof IASTCopyLocation) {
				fileLocation = ((IASTCopyLocation) locs[0]).getOriginalNode().getFileLocation();
			}
		}
		return fileLocation;
	}

	private void initEmptyMacros(IASTTranslationUnit unit) {
		if (unit != null) {
			tu = unit;
			IIndex index = tu.getIndex();
			if (index != null) {
				macroExpansion = new TreeMap<String, List<IIndexName>>();
				IASTPreprocessorMacroDefinition[] md = tu.getMacroDefinitions();

				TreeSet<String>paths = new TreeSet<String>();
				for (IASTPreprocessorIncludeStatement is :tu.getIncludeDirectives()) {
					if (!is.isSystemInclude()) {
						paths.add(is.getContainingFilename());
					}
				}
				paths.add(tu.getContainingFilename());

				for (IASTPreprocessorMacroDefinition iastPreprocessorMacroDefinition : md) {
					if (iastPreprocessorMacroDefinition.getExpansion().length() == 0) {
						try {
							IIndexMacro[] macroBinding = index.findMacros(iastPreprocessorMacroDefinition.getName().toCharArray(),
									IndexFilter.ALL, null);
							if (macroBinding.length > 0) {
								IIndexName[] refs = index.findReferences(macroBinding[0]);
								for (IIndexName iIndexName : refs) {
									String filename2 = iIndexName.getFileLocation().getFileName();
									List<IIndexName> fileList = macroExpansion.get(filename2);
									if (paths.contains(filename2)) {
										if (fileList == null) {
											fileList = new ArrayList<IIndexName>();
											macroExpansion.put(filename2, fileList);
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
