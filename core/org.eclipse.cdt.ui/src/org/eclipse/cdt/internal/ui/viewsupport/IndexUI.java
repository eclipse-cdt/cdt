/*******************************************************************************
 * Copyright (c) 2007, 2016 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Ed Swartz (Nokia)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.viewsupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IPositionConverter;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IIndexMacro;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInstanceCache;
import org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable;
import org.eclipse.cdt.internal.core.model.ext.CElementHandleFactory;
import org.eclipse.cdt.internal.core.model.ext.ICElementHandle;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;
import org.eclipse.cdt.internal.core.resources.ResourceLookup;
import org.eclipse.cdt.internal.ui.editor.ASTProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorInput;

public class IndexUI {
	private static final ICElementHandle[] EMPTY_ELEMENT_ARRAY = {};

	public static IIndexBinding elementToBinding(IIndex index, ICElement element) throws CoreException {
		return elementToBinding(index, element, -1);
	}

	public static IIndexBinding elementToBinding(IIndex index, ICElement element, int linkageID) throws CoreException {
		if (element instanceof ISourceReference) {
			ISourceReference sf = ((ISourceReference) element);
			ISourceRange range = sf.getSourceRange();
			if (range.getIdLength() != 0) {
				IIndexName name = elementToName(index, element, linkageID);
				if (name != null) {
					return index.findBinding(name);
				}
			} else {
				String name = element.getElementName();
				name = name.substring(name.lastIndexOf(':') + 1);
				IIndexBinding[] bindings = index.findBindings(name.toCharArray(), IndexFilter.ALL,
						new NullProgressMonitor());
				for (IIndexBinding binding : bindings) {
					if (checkBinding(binding, element)) {
						return binding;
					}
				}
			}
		}
		return null;
	}

	private static boolean checkBinding(IIndexBinding binding, ICElement element) {
		switch (element.getElementType()) {
		case ICElement.C_ENUMERATION:
			return binding instanceof IEnumeration;
		case ICElement.C_NAMESPACE:
			return binding instanceof ICPPNamespace;
		case ICElement.C_STRUCT_DECLARATION:
		case ICElement.C_STRUCT:
			return binding instanceof ICompositeType && ((ICompositeType) binding).getKey() == ICompositeType.k_struct;
		case ICElement.C_CLASS:
		case ICElement.C_CLASS_DECLARATION:
			return binding instanceof ICPPClassType && ((ICompositeType) binding).getKey() == ICPPClassType.k_class;
		case ICElement.C_UNION:
		case ICElement.C_UNION_DECLARATION:
			return binding instanceof ICompositeType && ((ICompositeType) binding).getKey() == ICompositeType.k_union;
		case ICElement.C_TYPEDEF:
			return binding instanceof ITypedef;
		case ICElement.C_METHOD:
		case ICElement.C_METHOD_DECLARATION:
			return binding instanceof ICPPMethod;
		case ICElement.C_FIELD:
			return binding instanceof IField;
		case ICElement.C_FUNCTION:
		case ICElement.C_FUNCTION_DECLARATION:
			return binding instanceof ICPPFunction && !(binding instanceof ICPPMethod);
		case ICElement.C_VARIABLE:
		case ICElement.C_VARIABLE_DECLARATION:
			return binding instanceof IVariable;
		case ICElement.C_ENUMERATOR:
			return binding instanceof IEnumerator;
		case ICElement.C_TEMPLATE_CLASS:
		case ICElement.C_TEMPLATE_CLASS_DECLARATION:
		case ICElement.C_TEMPLATE_STRUCT:
		case ICElement.C_TEMPLATE_STRUCT_DECLARATION:
		case ICElement.C_TEMPLATE_UNION:
		case ICElement.C_TEMPLATE_UNION_DECLARATION:
			return binding instanceof ICPPClassTemplate;
		case ICElement.C_TEMPLATE_FUNCTION:
		case ICElement.C_TEMPLATE_FUNCTION_DECLARATION:
			return binding instanceof ICPPFunctionTemplate && !(binding instanceof ICPPMethod);
		case ICElement.C_TEMPLATE_METHOD_DECLARATION:
		case ICElement.C_TEMPLATE_METHOD:
			return binding instanceof ICPPFunctionTemplate && binding instanceof ICPPMethod;
		case ICElement.C_TEMPLATE_VARIABLE:
			return binding instanceof ICPPTemplateParameter;
		}
		return false;
	}

	public static IIndexName elementToName(IIndex index, ICElement element) throws CoreException {
		return elementToName(index, element, -1);
	}

	public static IIndexName elementToName(IIndex index, ICElement element, int linkageID) throws CoreException {
		if (element instanceof ISourceReference) {
			ISourceReference sf = ((ISourceReference) element);
			ITranslationUnit tu = sf.getTranslationUnit();
			if (tu != null) {
				IIndexFileLocation location = IndexLocationFactory.getIFL(tu);
				if (location != null) {
					IIndexFile[] files = index.getFiles(location);
					for (IIndexFile file : files) {
						if (linkageID == -1 || file.getLinkageID() == linkageID) {
							String elementName = element.getElementName();
							int idx = elementName.lastIndexOf(":") + 1; //$NON-NLS-1$
							ISourceRange pos = sf.getSourceRange();
							IRegion region = getConvertedRegion(tu, file, pos.getIdStartPos() + idx,
									pos.getIdLength() - idx);
							IIndexName[] names = file.findNames(region.getOffset(), region.getLength());
							for (IIndexName name2 : names) {
								IIndexName name = name2;
								if (!name.isReference() && elementName.endsWith(new String(name.getSimpleID()))) {
									return name;
								}
							}
						}
					}
				}
			}
		}
		return null;
	}

	public static boolean isIndexed(IIndex index, ICElement element) throws CoreException {
		if (element instanceof ISourceReference) {
			ISourceReference sf = ((ISourceReference) element);
			ITranslationUnit tu = sf.getTranslationUnit();
			if (tu != null) {
				IIndexFileLocation location = IndexLocationFactory.getIFL(tu);
				if (location != null) {
					IIndexFile[] files = index.getFiles(location);
					return files.length > 0;
				}
			}
		}
		return false;
	}

	private static IRegion getConvertedRegion(ITranslationUnit tu, IIndexFile file, int pos, int length)
			throws CoreException {
		IRegion region = new Region(pos, length);
		IPositionConverter converter = CCorePlugin.getPositionTrackerManager().findPositionConverter(tu,
				file.getTimestamp());
		if (converter != null) {
			region = converter.actualToHistoric(region);
		}
		return region;
	}

	public static IIndexInclude elementToInclude(IIndex index, IInclude include) throws CoreException {
		if (include != null) {
			ITranslationUnit tu = include.getTranslationUnit();
			if (tu != null) {
				IIndexFileLocation location = IndexLocationFactory.getIFL(tu);
				if (location != null) {
					IIndexFile[] files = index.getFiles(location);
					for (IIndexFile file : files) {
						String elementName = include.getElementName();
						ISourceRange pos = include.getSourceRange();
						IRegion region = getConvertedRegion(tu, file, pos.getIdStartPos(), pos.getIdLength());

						IIndexInclude[] includes = index.findIncludes(file);
						int bestDiff = Integer.MAX_VALUE;
						IIndexInclude best = null;
						for (IIndexInclude candidate : includes) {
							int diff = Math.abs(candidate.getNameOffset() - region.getOffset());
							if (diff > bestDiff) {
								break;
							}
							if (candidate.getFullName().endsWith(elementName)) {
								bestDiff = diff;
								best = candidate;
							}
						}
						if (best != null)
							return best;
					}
				}
			}
		}
		return null;
	}

	public static ICElementHandle[] findRepresentative(IIndex index, IBinding binding) throws CoreException {
		ICElementHandle[] defs;
		while (true) {
			defs = findAllDefinitions(index, binding);
			if (defs.length == 0) {
				ICElementHandle elem = findAnyDeclaration(index, null, binding);
				if (elem != null) {
					defs = new ICElementHandle[] { elem };
				}
			}
			if (defs.length != 0 || !(binding instanceof ICPPSpecialization))
				break;
			binding = ((ICPPSpecialization) binding).getSpecializedBinding();
		}
		return defs;
	}

	public static ICElementHandle[] findAllDefinitions(IIndex index, IBinding binding) throws CoreException {
		if (binding != null) {
			IIndexName[] defs = index.findNames(binding,
					IIndex.FIND_DEFINITIONS | IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES);

			Set<ICElementHandle> result = new LinkedHashSet<>();
			for (IIndexName in : defs) {
				ICElementHandle definition = getCElementForName((ICProject) null, index, in);
				if (definition != null) {
					result.add(definition);
				}
			}
			return result.toArray(new ICElementHandle[result.size()]);
		}
		return EMPTY_ELEMENT_ARRAY;
	}

	/**
	 * Creates CElementHandles for definitions or declarations when you expect to find those
	 * in the index.
	 * @param preferProject
	 * @param index
	 * @param declName
	 * @return the ICElementHandle or <code>null</code>.
	 */
	public static ICElementHandle getCElementForName(ICProject preferProject, IIndex index, IASTName declName)
			throws CoreException {
		assert !declName.isReference();
		IBinding binding = declName.resolveBinding();
		if (binding != null) {
			ITranslationUnit tu = getTranslationUnit(declName);
			if (tu != null) {
				if (tu instanceof IWorkingCopy)
					tu = ((IWorkingCopy) tu).getOriginalElement();
				IFile file = (IFile) tu.getResource();
				long timestamp = file != null ? file.getLocalTimeStamp() : 0;
				IASTFileLocation loc = declName.getFileLocation();
				if (loc == null) {
					return null;
				}
				IRegion region = new Region(loc.getNodeOffset(), loc.getNodeLength());
				IPositionConverter converter = CCorePlugin.getPositionTrackerManager().findPositionConverter(tu,
						timestamp);
				if (converter != null) {
					region = converter.actualToHistoric(region);
				}
				return CElementHandleFactory.create(tu, binding, declName.isDefinition(), region, timestamp);
			}
		}
		return null;
	}

	public static ITranslationUnit getTranslationUnit(IASTName name) {
		IASTTranslationUnit astTranslationUnit = name.getTranslationUnit();
		return astTranslationUnit == null ? null : astTranslationUnit.getOriginatingTranslationUnit();
	}

	public static ITranslationUnit getTranslationUnit(ICProject cproject, IIndexName name) {
		try {
			return CoreModelUtil.findTranslationUnitForLocation(name.getFile().getLocation(), cproject);
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
		return null;
	}

	private static ITranslationUnit getTranslationUnit(ICProject cproject, final IASTFileLocation fileLocation) {
		if (fileLocation != null) {
			IPath path = Path.fromOSString(fileLocation.getFileName());
			try {
				return CoreModelUtil.findTranslationUnitForLocation(path, cproject);
			} catch (CModelException e) {
				CUIPlugin.log(e);
			}
		}
		return null;
	}

	/**
	 * Given a 'source' and a 'target' translation unit, return a translation unit
	 * that resolves to the same file as 'target' and has a workspace path that
	 * matches the workspace path of 'source' as closely as possible.
	 *
	 * Most commonly, 'target' will be the only trasnlation unit that resolves to
	 * the file in question, and 'target' is returned. In the presence of linked
	 * folders, however, multiple workspace paths can refer to the same file, and
	 * this function chooses the one that's closest to 'source'.
	 */
	public static ITranslationUnit getPreferredTranslationUnit(ITranslationUnit target, ITranslationUnit source) {
		// Get the files corresponding to the source and target translation units.
		// These files encode the workspace paths.
		IFile sourceFile = source.getFile();
		IFile targetFile = target.getFile();
		if (sourceFile == null || targetFile == null) {
			return target;
		}

		// Resolve the location of the target in the filesystem.
		IPath targetLocation = targetFile.getLocation();
		if (targetLocation == null) {
			return target;
		}

		// Find all files that resolve to the same location.
		IFile[] candidates = ResourceLookup.findFilesForLocation(targetLocation);

		// In the common case that there is one only file that resolves to that
		// location, or if the search found no results, return the original target.
		if (candidates.length <= 1) {
			return target;
		}

		// Get the workspace path of the source translation unit's file.
		final IPath sourcePath = sourceFile.getFullPath();

		// Sort the candidates files by how closely they match 'sourcePath'.
		Arrays.sort(candidates, new Comparator<IFile>() {
			@Override
			public int compare(IFile f1, IFile f2) {
				// Get the workspace paths of the files being compared.
				IPath p1 = f1.getFullPath();
				IPath p2 = f2.getFullPath();

				// Closeness of the match is defined by how many segments of
				// the candidate's workspace path match 'sourcePath'.
				int s1 = p1.matchingFirstSegments(sourcePath);
				int s2 = p2.matchingFirstSegments(sourcePath);
				if (s1 > s2)
					return -1;
				if (s1 < s2)
					return 1;

				// Fall back on alphabetical comparison.
				return p1.toString().compareTo(p2.toString());
			}
		});

		// Processing in the sorted order, return the first file for which
		// a translation unit can be found.
		for (IFile candidate : candidates) {
			ITranslationUnit tu = CoreModelUtil.findTranslationUnit(candidate);
			if (tu != null) {
				return tu;
			}
		}

		// Fall back on returning the original target.
		return target;
	}

	public static ICElementHandle getCElementForName(ICProject preferProject, IIndex index, IIndexName declName)
			throws CoreException {
		assert !declName.isReference();
		ITranslationUnit tu = getTranslationUnit(preferProject, declName);
		if (tu != null) {
			return getCElementForName(tu, index, declName);
		}
		return null;
	}

	public static ICElementHandle getCElementForName(ITranslationUnit tu, IIndex index, IIndexName declName)
			throws CoreException {
		IRegion region = new Region(declName.getNodeOffset(), declName.getNodeLength());
		long timestamp = declName.getFile().getTimestamp();
		return CElementHandleFactory.create(tu, index.findBinding(declName), declName.isDefinition(), region,
				timestamp);
	}

	public static ICElementHandle getCElementForMacro(ICProject preferProject, IIndex index, IIndexMacro macro)
			throws CoreException {
		ITranslationUnit tu = getTranslationUnit(preferProject, macro.getFileLocation());
		if (tu != null) {
			IIndexName name = macro.getDefinition();
			if (name != null) {
				IRegion region = new Region(name.getNodeOffset(), name.getNodeLength());
				long timestamp = macro.getFile().getTimestamp();
				return CElementHandleFactory.create(tu, macro, region, timestamp);
			}
		}
		return null;
	}

	public static ICElementHandle findAnyDeclaration(IIndex index, ICProject preferProject, IBinding binding)
			throws CoreException {
		if (binding != null) {
			IIndexName[] names = index.findNames(binding, IIndex.FIND_DECLARATIONS);
			for (IIndexName name : names) {
				ICElementHandle elem = getCElementForName(preferProject, index, name);
				if (elem != null) {
					return elem;
				}
			}
		}
		return null;
	}

	public static IASTName getSelectedName(IEditorInput editorInput, ITextSelection selection) throws CoreException {
		return getSelectedName(editorInput, selection.getOffset(), selection.getLength());
	}

	public static IASTName getSelectedName(IEditorInput editorInput, IRegion selection) throws CoreException {
		return getSelectedName(editorInput, selection.getOffset(), selection.getLength());
	}

	private static IASTName getSelectedName(IEditorInput editorInput, final int offset, final int length) {
		IWorkingCopy workingCopy = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(editorInput);
		if (workingCopy == null)
			return null;

		final IASTName[] result = { null };
		ASTProvider.getASTProvider().runOnAST(workingCopy, ASTProvider.WAIT_ACTIVE_ONLY, null, new ASTRunnable() {
			@Override
			public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) {
				if (ast != null) {
					final IASTNodeSelector nodeSelector = ast.getNodeSelector(null);
					IASTName name = nodeSelector.findEnclosingName(offset, length);
					if (name == null) {
						name = nodeSelector.findImplicitName(offset, length);
					}
					if (name != null && name.getParent() instanceof IASTPreprocessorMacroExpansion) {
						IASTFileLocation floc = name.getParent().getFileLocation();
						IASTNode node = nodeSelector.findEnclosingNodeInExpansion(floc.getNodeOffset(),
								floc.getNodeLength());
						if (node instanceof IASTName) {
							name = (IASTName) node;
						} else if (node instanceof IASTFunctionCallExpression) {
							IASTExpression expr = ((IASTFunctionCallExpression) node).getFunctionNameExpression();
							if (expr instanceof IASTIdExpression) {
								name = ((IASTIdExpression) expr).getName();
							}
						} else {
							if (node instanceof IASTSimpleDeclaration) {
								IASTNode[] dtors = ((IASTSimpleDeclaration) node).getDeclarators();
								if (dtors != null && dtors.length > 0) {
									node = dtors[0];
								}
							} else if (node instanceof IASTFunctionDefinition) {
								node = ((IASTFunctionDefinition) node).getDeclarator();
							}
							if (node instanceof IASTDeclarator) {
								IASTDeclarator dtor = ASTQueries.findTypeRelevantDeclarator((IASTDeclarator) node);
								name = dtor.getName();
							}
						}
					}
					result[0] = name;
				}
				return Status.OK_STATUS;
			}
		});
		return result[0];
	}

	public static String getFileNotIndexedMessage(ICElement input) {
		ITranslationUnit tu = null;
		if (input instanceof ISourceReference) {
			ISourceReference ref = (ISourceReference) input;
			tu = ref.getTranslationUnit();
		}
		if (tu == null) {
			return NLS.bind(Messages.IndexUI_infoNotInSource, input.getElementName());
		}

		String msg = NLS.bind(Messages.IndexUI_infoNotInIndex, tu.getElementName());

		IResource res = tu.getResource();
		if (res != null) {
			Properties props = IndexerPreferences.getProperties(res.getProject());
			if (props == null || !Boolean.parseBoolean((String) props.get(IndexerPreferences.KEY_INDEX_ALL_FILES))
					|| (!Boolean.parseBoolean(
							(String) props.get(IndexerPreferences.KEY_INDEX_UNUSED_HEADERS_WITH_DEFAULT_LANG))
							&& !Boolean.parseBoolean((String) props
									.get(IndexerPreferences.KEY_INDEX_UNUSED_HEADERS_WITH_ALTERNATE_LANG)))) {
				msg = msg + " " + Messages.IndexUI_infoSelectIndexAllFiles; //$NON-NLS-1$
			}
		}
		return msg;
	}

	public static ICElement attemptConvertionToHandle(IIndex index, ICElement input) throws CoreException {
		if (input instanceof ICElementHandle) {
			return input;
		}
		IIndexName name = elementToName(index, input);
		if (name != null) {
			ICElement handle = getCElementForName(input.getCProject(), index, name);
			if (handle != null) {
				return handle;
			}
		}
		return input;
	}

	/**
	 * Searches for all specializations that depend on the definition of the given binding.
	 */
	public static List<? extends IBinding> findSpecializations(IIndex index, IBinding binding) throws CoreException {
		List<IBinding> result = null;

		// Check for instances of the given binding.
		if (binding instanceof ICPPInstanceCache) {
			ICPPTemplateInstance[] instances = ((ICPPInstanceCache) binding).getAllInstances();
			for (ICPPTemplateInstance inst : instances) {
				if (!ASTInternal.hasDeclaration(inst)) {
					if (result == null)
						result = new ArrayList<>(instances.length);
					result.add(inst);
				}
			}
		}

		// Check for specializations of the owner.
		IBinding owner = binding.getOwner();
		if (owner != null) {
			List<? extends IBinding> specializations = findSpecializations(index, owner);
			for (IBinding specOwner : specializations) {
				if (specOwner instanceof ICPPClassSpecialization) {
					// Add the specialized member.
					IBinding specializedMember = ((ICPPClassSpecialization) specOwner).specializeMember(binding);
					specializedMember = index.adaptBinding(specializedMember);
					if (specializedMember != null) {
						if (result == null)
							result = new ArrayList<>(specializations.size());
						result.add(specializedMember);
						// Also add instances of the specialized member.
						if (specializedMember instanceof ICPPInstanceCache) {
							ICPPTemplateInstance[] instances = ((ICPPInstanceCache) specializedMember)
									.getAllInstances();
							for (ICPPTemplateInstance inst : instances) {
								if (!ASTInternal.hasDeclaration(inst)) {
									result.add(inst);
								}
							}
						}
					}
				}
			}
		}

		if (result != null) {
			return result;
		}
		return Collections.emptyList();
	}
}
