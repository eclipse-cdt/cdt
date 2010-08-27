/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Ed Swartz (Nokia)
 *******************************************************************************/ 
package org.eclipse.cdt.internal.ui.viewsupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

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
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInstanceCache;
import org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable;
import org.eclipse.cdt.internal.core.model.ext.CElementHandleFactory;
import org.eclipse.cdt.internal.core.model.ext.ICElementHandle;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;

import org.eclipse.cdt.internal.ui.editor.ASTProvider;

public class IndexUI {
	private static final ICElementHandle[] EMPTY_ELEMENTS = new ICElementHandle[0];

	public static IIndexBinding elementToBinding(IIndex index, ICElement element) throws CoreException {
		return elementToBinding(index, element, -1);
	}

	public static IIndexBinding elementToBinding(IIndex index, ICElement element, int linkageID) throws CoreException {
		if (element instanceof ISourceReference) {
			ISourceReference sf = ((ISourceReference)element);
			ISourceRange range= sf.getSourceRange();
			if (range.getIdLength() != 0) {
				IIndexName name= elementToName(index, element, linkageID);
				if (name != null) {
					return index.findBinding(name);
				}
			}
			else {
				String name= element.getElementName();
				name= name.substring(name.lastIndexOf(':')+1);
				IIndexBinding[] bindings= index.findBindings(name.toCharArray(), IndexFilter.ALL, new NullProgressMonitor());
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
		switch(element.getElementType()) {
		case ICElement.C_ENUMERATION:
			return binding instanceof IEnumeration;
		case ICElement.C_NAMESPACE:
			return binding instanceof ICPPNamespace;
		case ICElement.C_STRUCT_DECLARATION:
		case ICElement.C_STRUCT:
			return binding instanceof ICompositeType && 
				((ICompositeType) binding).getKey() == ICompositeType.k_struct;
		case ICElement.C_CLASS:
		case ICElement.C_CLASS_DECLARATION:
			return binding instanceof ICPPClassType && 
				((ICompositeType) binding).getKey() == ICPPClassType.k_class;
		case ICElement.C_UNION:
		case ICElement.C_UNION_DECLARATION:
			return binding instanceof ICompositeType && 
				((ICompositeType) binding).getKey() == ICompositeType.k_union;
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
			ISourceReference sf = ((ISourceReference)element);
			ITranslationUnit tu= sf.getTranslationUnit();
			if (tu != null) {
				IIndexFileLocation location= IndexLocationFactory.getIFL(tu);
				if (location != null) {
					IIndexFile[] files= index.getFiles(location);
					for (IIndexFile file : files) {
						if (linkageID == -1 || file.getLinkageID() == linkageID) {
							String elementName= element.getElementName();
							int idx= elementName.lastIndexOf(":")+1; //$NON-NLS-1$
							ISourceRange pos= sf.getSourceRange();
							IRegion region = getConvertedRegion(tu, file, pos.getIdStartPos()+idx, pos.getIdLength()-idx);
							IIndexName[] names= file.findNames(region.getOffset(), region.getLength());
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
			ISourceReference sf = ((ISourceReference)element);
			ITranslationUnit tu= sf.getTranslationUnit();
			if (tu != null) {
				IIndexFileLocation location= IndexLocationFactory.getIFL(tu);
				if (location != null) {
					IIndexFile[] files= index.getFiles(location);
					return files.length > 0;
				}
			}
		}
		return false;
	}
	
	private static IRegion getConvertedRegion(ITranslationUnit tu, IIndexFile file, int pos, int length) throws CoreException {
		IRegion region= new Region(pos, length);
		IPositionConverter converter= CCorePlugin.getPositionTrackerManager().findPositionConverter(tu, file.getTimestamp());
		if (converter != null) {
			region= converter.actualToHistoric(region);
		}
		return region;
	}
	
	public static IIndexInclude elementToInclude(IIndex index, IInclude include) throws CoreException {
		if (include != null) {
			ITranslationUnit tu= include.getTranslationUnit();
			if (tu != null) {
				IIndexFileLocation location= IndexLocationFactory.getIFL(tu);
				if (location != null) {
					IIndexFile[] files= index.getFiles(location);
					for (IIndexFile file : files) {
						String elementName= include.getElementName();
						ISourceRange pos= include.getSourceRange();
						IRegion region= getConvertedRegion(tu, file, pos.getIdStartPos(), pos.getIdLength());

						IIndexInclude[] includes= index.findIncludes(file);
						int bestDiff= Integer.MAX_VALUE;
						IIndexInclude best= null;
						for (IIndexInclude candidate : includes) {
							int diff= Math.abs(candidate.getNameOffset()- region.getOffset());
							if (diff > bestDiff) {
								break;
							}
							if (candidate.getFullName().endsWith(elementName)) {
								bestDiff= diff;
								best= candidate;
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
		ICElementHandle[] defs = IndexUI.findAllDefinitions(index, binding);
		if (defs.length == 0) {
			ICElementHandle elem = IndexUI.findAnyDeclaration(index, null, binding);
			if (elem != null) {
				defs = new ICElementHandle[] { elem };
			}
		}
		return defs;
	}

	public static ICElementHandle[] findAllDefinitions(IIndex index, IBinding binding) throws CoreException {
		if (binding != null) {
			IIndexName[] defs= index.findNames(binding, IIndex.FIND_DEFINITIONS | IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES);

			ArrayList<ICElementHandle> result= new ArrayList<ICElementHandle>();
			for (IIndexName in : defs) {
				ICElementHandle definition= getCElementForName((ICProject) null, index, in);
				if (definition != null) {
					result.add(definition);
				}
				
			}
			return result.toArray(new ICElementHandle[result.size()]);
		}
		return EMPTY_ELEMENTS;
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
		IBinding binding= declName.resolveBinding();
		if (binding != null) {
			ITranslationUnit tu= getTranslationUnit(preferProject, declName);
			if (tu != null) {
				IFile file= (IFile) tu.getResource();
				long timestamp= file != null ? file.getLocalTimeStamp() : 0;
				IASTFileLocation loc= declName.getFileLocation();
				IRegion region= new Region(loc.getNodeOffset(), loc.getNodeLength());
				IPositionConverter converter= CCorePlugin.getPositionTrackerManager().findPositionConverter(tu, timestamp);
				if (converter != null) {
					region= converter.actualToHistoric(region);
				}
				return CElementHandleFactory.create(tu, binding, declName.isDefinition(), region, timestamp);
			}
		}
		return null;
	}
	
	public static ITranslationUnit getTranslationUnit(ICProject cproject, IASTName name) {
		return getTranslationUnit(cproject, name.getFileLocation());
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
			IPath path= Path.fromOSString(fileLocation.getFileName());
			try {
				return CoreModelUtil.findTranslationUnitForLocation(path, cproject);
			} catch (CModelException e) {
				CUIPlugin.log(e);
			}
		}
		return null;
	}

	public static ICElementHandle getCElementForName(ICProject preferProject, IIndex index, IIndexName declName) 
			throws CoreException {
		assert !declName.isReference();
		ITranslationUnit tu= getTranslationUnit(preferProject, declName);
		if (tu != null) {
			return getCElementForName(tu, index, declName);
		}
		return null;
	}

	public static ICElementHandle getCElementForName(ITranslationUnit tu, IIndex index, IIndexName declName) 
			throws CoreException {
		IRegion region= new Region(declName.getNodeOffset(), declName.getNodeLength());
		long timestamp= declName.getFile().getTimestamp();
		return CElementHandleFactory.create(tu, index.findBinding(declName), declName.isDefinition(), region, timestamp);
	}

	public static ICElementHandle getCElementForMacro(ICProject preferProject, IIndex index, IIndexMacro macro) 
			throws CoreException {
		ITranslationUnit tu= getTranslationUnit(preferProject, macro.getFileLocation());
		if (tu != null) {
			IIndexName name= macro.getDefinition();
			if (name != null) {
				IRegion region= new Region(name.getNodeOffset(), name.getNodeLength());
				long timestamp= macro.getFile().getTimestamp();
				return CElementHandleFactory.create(tu, macro, region, timestamp);
			}
		}
		return null;
	}

	public static ICElementHandle findAnyDeclaration(IIndex index, ICProject preferProject, IBinding binding) 
			throws CoreException {
		if (binding != null) {
			IIndexName[] names= index.findNames(binding, IIndex.FIND_DECLARATIONS);
			for (IIndexName name : names) {
				ICElementHandle elem= getCElementForName(preferProject, index, name);
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
		
		final IASTName[] result= {null};
		ASTProvider.getASTProvider().runOnAST(workingCopy, ASTProvider.WAIT_ACTIVE_ONLY, null, new ASTRunnable() {
			public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) {
				if (ast != null) {
					final IASTNodeSelector nodeSelector = ast.getNodeSelector(null);
					IASTName name= nodeSelector.findEnclosingName(offset, length);
					if (name == null) {
						name= nodeSelector.findImplicitName(offset, length);
					}
					if (name != null && name.getParent() instanceof IASTPreprocessorMacroExpansion) {
						IASTFileLocation floc= name.getParent().getFileLocation();
						IASTNode node= nodeSelector.findEnclosingNodeInExpansion(floc.getNodeOffset(), floc.getNodeLength());
						if (node instanceof IASTName) {
							name= (IASTName) node;
						} else if (node instanceof IASTFunctionCallExpression){
							IASTExpression expr= ((IASTFunctionCallExpression) node).getFunctionNameExpression();
							if (expr instanceof IASTIdExpression) {
								name= ((IASTIdExpression) expr).getName();
							}
						} else {
							if (node instanceof IASTSimpleDeclaration) {
								IASTNode[] dtors= ((IASTSimpleDeclaration) node).getDeclarators();
								if (dtors != null && dtors.length > 0) {
									node= dtors[0];
								}
							} else if (node instanceof IASTFunctionDefinition) {
								node= ((IASTFunctionDefinition) node).getDeclarator();
							}
							if (node instanceof IASTDeclarator) {
								IASTDeclarator dtor= ASTQueries.findTypeRelevantDeclarator((IASTDeclarator) node);
								name= dtor.getName();
							}
						}
					}
					result[0]= name;
				}
				return Status.OK_STATUS;
			}
		});
		return result[0];
	}

	public static String getFileNotIndexedMessage(ICElement input) {
		ITranslationUnit tu= null;
		if (input instanceof ISourceReference) {
			ISourceReference ref= (ISourceReference) input;
			tu= ref.getTranslationUnit();
		}
		if (tu == null) {
			return NLS.bind(Messages.IndexUI_infoNotInSource, input.getElementName());
		} 
		
		String msg= NLS.bind(Messages.IndexUI_infoNotInIndex, tu.getElementName());
		
		IResource res= tu.getResource();
		if (res != null) {
			Properties props= IndexerPreferences.getProperties(res.getProject());
			if (props == null || !"true".equals(props.get(IndexerPreferences.KEY_INDEX_ALL_FILES)) || //$NON-NLS-1$
					(!"true".equals(props.get(IndexerPreferences.KEY_INDEX_UNUSED_HEADERS_WITH_DEFAULT_LANG)) && //$NON-NLS-1$
					 !"true".equals(props.get(IndexerPreferences.KEY_INDEX_UNUSED_HEADERS_WITH_ALTERNATE_LANG)))) { //$NON-NLS-1$
				msg= msg+ " " + Messages.IndexUI_infoSelectIndexAllFiles; //$NON-NLS-1$
			} 
		}
		return msg;
	}

	public static ICElement attemptConvertionToHandle(IIndex index, ICElement input) throws CoreException {
		if (input instanceof ICElementHandle) {
			return input;
		}
		IIndexName name= IndexUI.elementToName(index, input);
		if (name != null) {
			ICElement handle= getCElementForName(input.getCProject(), index, name);
			if (handle != null) {
				return handle;
			}
		} 
		return input;
	}
	
	/**
	 * Searches for all specializations that depend on the definition of the given binding.
	 */
	public static List<? extends IBinding> findSpecializations(IBinding binding) throws CoreException {
		List<IBinding> result= null;

		IBinding owner = binding.getOwner();
		if (owner != null) {
			List<? extends IBinding> specializedOwners= findSpecializations(owner);
			if (!specializedOwners.isEmpty()) {
				result= new ArrayList<IBinding>(specializedOwners.size());

				for (IBinding specOwner : specializedOwners) {
					if (specOwner instanceof ICPPClassSpecialization) {
						result.add(((ICPPClassSpecialization) specOwner).specializeMember(binding));
					}
				}
			}
		}
		
		if (binding instanceof ICPPInstanceCache) {
			final List<ICPPTemplateInstance> instances= Arrays.asList(((ICPPInstanceCache) binding).getAllInstances());
			if (!instances.isEmpty()) {
				if (result == null)
					result= new ArrayList<IBinding>(instances.size());


				for (ICPPTemplateInstance inst : instances) {
					if (!IndexFilter.ALL_DECLARED.acceptBinding(inst)) {
						result.add(inst);
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
