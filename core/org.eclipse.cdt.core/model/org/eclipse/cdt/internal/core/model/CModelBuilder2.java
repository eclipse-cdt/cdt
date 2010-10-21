/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTASMDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTStaticAssertDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IContributedModelBuilder;
import org.eclipse.cdt.core.model.INamespace;
import org.eclipse.cdt.core.model.IProblemRequestor;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.ParseError;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * Build TranslationUnit structure from an <code>IASTTranslationUnit</code>.
 *
 * @since 4.0
 */
public class CModelBuilder2 implements IContributedModelBuilder {
	private final static boolean DEBUG= Util.isActive(DebugLogConstants.MODEL);

	private final TranslationUnit fTranslationUnit;
	private final Map<ICElement, CElementInfo> fNewElements;
	private final IProgressMonitor fProgressMonitor;

	private ASTAccessVisibility fCurrentVisibility;
	private Stack<ASTAccessVisibility> fVisibilityStack;
	private HashMap<ISourceReference, int[]> fEqualElements;

	/**
	 * Create a model builder for the given translation unit.
	 * 
	 * @param tu  the translation unit (must be a {@link TranslationUnit}
	 * @param newElements  element cache
	 * @param monitor the progress monitor
	 */
	public CModelBuilder2(ITranslationUnit tu, Map<ICElement, CElementInfo> newElements, IProgressMonitor monitor) {
		fTranslationUnit= (TranslationUnit)tu;
		fNewElements= newElements;
		fProgressMonitor= monitor;
	}

	/*
	 * @see org.eclipse.cdt.core.model.IContributedModelBuilder#parse(boolean)
	 */
	public void parse(boolean quickParseMode) throws Exception {
		final IIndexManager indexManager= CCorePlugin.getIndexManager();
		IIndex index= indexManager.getIndex(fTranslationUnit.getCProject(), IIndexManager.ADD_DEPENDENCIES);
		
		try {
			if (index != null) {
				try {
					index.acquireReadLock();
				} catch (InterruptedException ie) {
					index= null;
				}
			}
			checkCanceled();
			long startTime= System.currentTimeMillis();
			final CElementInfo elementInfo= getElementInfo(fTranslationUnit);
			int parseFlags= quickParseMode ? ITranslationUnit.AST_SKIP_ALL_HEADERS : ITranslationUnit.AST_SKIP_INDEXED_HEADERS;
			if (!(elementInfo instanceof ASTHolderTUInfo)) {
				parseFlags |= ITranslationUnit.AST_SKIP_FUNCTION_BODIES;
			} else {
				parseFlags |= ITranslationUnit.AST_CONFIGURE_USING_SOURCE_CONTEXT;
			}
			parseFlags |= ITranslationUnit.AST_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS;
			parseFlags |= ITranslationUnit.AST_PARSE_INACTIVE_CODE;
			final IASTTranslationUnit ast;
			try {
				ast= fTranslationUnit.getAST(index, parseFlags, fProgressMonitor);
				if (DEBUG) {
					Util.debugLog("CModelBuilder2: parsing " //$NON-NLS-1$
							+ fTranslationUnit.getElementName()
							+ " mode="+ (quickParseMode ? "skip all " : "skip indexed ") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							+ " time="+ ( System.currentTimeMillis() - startTime ) + "ms", //$NON-NLS-1$ //$NON-NLS-2$
							DebugLogConstants.MODEL, false);
				}
			} catch (ParseError e) {
				checkCanceled();
				throw e;
			}
			if (ast == null) {
				return;
			}

			checkCanceled();
			startTime= System.currentTimeMillis();
			buildModel(ast);
			elementInfo.setIsStructureKnown(true);
			if (DEBUG) {
				Util.debugLog("CModelBuilder2: building " //$NON-NLS-1$
						+ "children=" + elementInfo.internalGetChildren().size() //$NON-NLS-1$
						+ " time=" + (System.currentTimeMillis() - startTime) + "ms", //$NON-NLS-1$ //$NON-NLS-2$
						DebugLogConstants.MODEL, false);
			}

			if (elementInfo instanceof ASTHolderTUInfo) {
				((ASTHolderTUInfo) elementInfo).fAST= ast;
				// preserve index lock for AST receiver
				index= null;
			}
		} finally {
			if (index != null) {
				index.releaseReadLock();
			}
		}
	}

	private boolean isCanceled() {
		return fProgressMonitor != null && fProgressMonitor.isCanceled();
	}

	private void checkCanceled() {
		if (fProgressMonitor != null && fProgressMonitor.isCanceled()) {
			if (DEBUG) Util.debugLog("CModelBuilder2: cancelled ", DebugLogConstants.MODEL, false); //$NON-NLS-1$
			throw new OperationCanceledException();
		}
	}

	/**
	 * Build the model from the given AST.
	 * @param ast
	 * @throws CModelException
	 * @throws DOMException
	 */
	private void buildModel(IASTTranslationUnit ast) throws CModelException, DOMException {
		fVisibilityStack= new Stack<ASTAccessVisibility>();
		fEqualElements= new HashMap<ISourceReference, int[]>();

		// includes
		final IASTPreprocessorIncludeStatement[] includeDirectives= ast.getIncludeDirectives();
		for (IASTPreprocessorIncludeStatement includeDirective : includeDirectives) {
			if (isLocalToFile(includeDirective)) {
				createInclusion(fTranslationUnit, includeDirective);
			}
		}
		// macros
		final IASTPreprocessorMacroDefinition[] macroDefinitions= ast.getMacroDefinitions();
		for (IASTPreprocessorMacroDefinition macroDefinition : macroDefinitions) {
			if (isLocalToFile(macroDefinition)) {
				createMacro(fTranslationUnit, macroDefinition);
			}
		}
		// declarations
		final IASTDeclaration[] declarations= ast.getDeclarations(true);
		for (IASTDeclaration declaration : declarations) {
			if (isLocalToFile(declaration)) {
				createDeclaration(fTranslationUnit, declaration);
			}
		}
		fEqualElements.clear();

		// sort by offset
		final List<ICElement> children= getElementInfo(fTranslationUnit).internalGetChildren();
		Collections.sort(children, new Comparator<ICElement>() {
			public int compare(ICElement o1, ICElement o2) {
				final SourceManipulationInfo info1= getSourceManipulationInfo((SourceManipulation) o1);
				final SourceManipulationInfo info2= getSourceManipulationInfo((SourceManipulation) o2);
				int delta= info1.getStartPos() - info2.getStartPos();
				if (delta == 0) {
					delta= info1.getIdStartPos() - info2.getIdStartPos();
				}
				return delta;
			}});

		if (isCanceled()) {
			return;
		}

		// report problems
		IProblemRequestor problemRequestor= fTranslationUnit.getProblemRequestor();
		if (problemRequestor != null && problemRequestor.isActive()) {
			problemRequestor.beginReporting();
			final IASTProblem[] ppProblems= ast.getPreprocessorProblems();
			IASTProblem[] problems= ppProblems;
			for (IASTProblem problem : problems) {
				if (isLocalToFile(problem)) {
					problemRequestor.acceptProblem(problem);
				}
			}
			problems= CPPVisitor.getProblems(ast);
			for (IASTProblem problem : problems) {
				if (isLocalToFile(problem)) {
					problemRequestor.acceptProblem(problem);
				}
			}
			problemRequestor.endReporting();
		}
	}

	private boolean isLocalToFile(IASTNode node) {
		return node.isPartOfTranslationUnitFile();
	}

	private Include createInclusion(Parent parent, IASTPreprocessorIncludeStatement inclusion) throws CModelException{
		// create element
		final IASTName name= inclusion.getName();
		Include element= new Include(parent, ASTStringUtil.getSimpleName(name), inclusion.isSystemInclude());
		element.setFullPathName(inclusion.getPath());
		setIndex(element);

		element.setActive(inclusion.isActive());
		element.setResolved(inclusion.isResolved());
		// add to parent
		parent.addChild(element);
		// set positions
		setIdentifierPosition(element, name);
		setBodyPosition(element, inclusion);
		return element;
	}

	private void setIndex(SourceManipulation element) {
		int[] idx= fEqualElements.get(element);
		if (idx == null) {
			idx= new int[] {0};
			fEqualElements.put(element, idx);
		} else {
			element.setIndex(++idx[0]);
		}
	}

	private Macro createMacro(Parent parent, IASTPreprocessorMacroDefinition macro) throws CModelException{
		// create element
		final IASTName name= macro.getName();
		Macro element= new  Macro(parent, ASTStringUtil.getSimpleName(name));
		setIndex(element);
		element.setActive(macro.isActive());
		// add to parent
		parent.addChild(element);
		// set positions
		setIdentifierPosition(element, name);
		setBodyPosition(element, macro);
		if (macro instanceof IASTPreprocessorFunctionStyleMacroDefinition) {
			element.setFunctionStyle(true);
		}
		return element;
	}

	private void createDeclaration(Parent parent, IASTDeclaration declaration) throws CModelException, DOMException {
		if (declaration instanceof IASTFunctionDefinition) {
			createFunctionDefinition(parent, (IASTFunctionDefinition)declaration, false);
		} else if (declaration instanceof IASTSimpleDeclaration) {
			createSimpleDeclarations(parent, (IASTSimpleDeclaration)declaration, false);
		} else if (declaration instanceof ICPPASTVisibilityLabel) {
			handleVisibilityLabel((ICPPASTVisibilityLabel)declaration);
		} else if (declaration instanceof ICPPASTNamespaceDefinition) {
			createNamespace(parent, (ICPPASTNamespaceDefinition) declaration);
		} else if (declaration instanceof ICPPASTNamespaceAlias) {
			// TODO [cmodel] namespace alias?
		} else if (declaration instanceof ICPPASTTemplateDeclaration) {
			createTemplateDeclaration(parent, (ICPPASTTemplateDeclaration)declaration);
		} else if (declaration instanceof ICPPASTTemplateSpecialization) {
			// TODO [cmodel] template specialization?
		} else if (declaration instanceof ICPPASTExplicitTemplateInstantiation) {
			// TODO [cmodel] explicit template instantiation?
		} else if (declaration instanceof ICPPASTUsingDeclaration) {
			createUsingDeclaration(parent, (ICPPASTUsingDeclaration)declaration);
		} else if (declaration instanceof ICPPASTUsingDirective) {
			createUsingDirective(parent, (ICPPASTUsingDirective)declaration);
		} else if (declaration instanceof ICPPASTLinkageSpecification) {
			createLinkageSpecification(parent, (ICPPASTLinkageSpecification)declaration);
		} else if (declaration instanceof IASTASMDeclaration) {
			// TODO [cmodel] asm declaration?
		} else if (declaration instanceof IASTProblemDeclaration) {
			// TODO [cmodel] problem declaration?
		} else if (declaration instanceof ICPPASTStaticAssertDeclaration) {
			// ignore
		} else {
			assert false : "TODO: " + declaration.getClass().getName(); //$NON-NLS-1$
		}
	}

	private void createTemplateDeclaration(Parent parent, ICPPASTTemplateDeclaration templateDeclaration) throws CModelException, DOMException {
		IASTDeclaration declaration= templateDeclaration.getDeclaration();
		if (declaration instanceof IASTFunctionDefinition) {
			CElement element= createFunctionDefinition(parent, (IASTFunctionDefinition)declaration, true);
			String[] parameterTypes= ASTStringUtil.getTemplateParameterArray(templateDeclaration.getTemplateParameters());
			// set the template parameters
			if (element instanceof FunctionTemplate) {
				FunctionTemplate functionTemplate= (FunctionTemplate) element;
				functionTemplate.setTemplateParameterTypes(parameterTypes);
			} else if (element instanceof MethodTemplate) {
				MethodTemplate methodTemplate= (MethodTemplate) element;
				methodTemplate.setTemplateParameterTypes(parameterTypes);
			}
			// set the body position
			if (element instanceof SourceManipulation) {
				setBodyPosition((SourceManipulation)element, templateDeclaration);
			}
		} else if (declaration instanceof IASTSimpleDeclaration) {
			CElement[] elements= createSimpleDeclarations(parent, (IASTSimpleDeclaration)declaration, true);
			String[] parameterTypes= ASTStringUtil.getTemplateParameterArray(templateDeclaration.getTemplateParameters());
			for (CElement element : elements) {
				// set the template parameters
				if (element instanceof StructureTemplate) {
					StructureTemplate classTemplate= (StructureTemplate) element;
					classTemplate.setTemplateParameterTypes(parameterTypes);
				} else if (element instanceof StructureTemplateDeclaration) {
					StructureTemplateDeclaration classTemplate= (StructureTemplateDeclaration) element;
					classTemplate.setTemplateParameterTypes(parameterTypes);
				} else if (element instanceof VariableTemplate) {
					VariableTemplate varTemplate= (VariableTemplate) element;
					varTemplate.setTemplateParameterTypes(parameterTypes);
				} else if (element instanceof FunctionTemplateDeclaration) {
					FunctionTemplateDeclaration functionTemplate= (FunctionTemplateDeclaration) element;
					functionTemplate.setTemplateParameterTypes(parameterTypes);
				} else if (element instanceof MethodTemplateDeclaration) {
					MethodTemplateDeclaration methodTemplate= (MethodTemplateDeclaration) element;
					methodTemplate.setTemplateParameterTypes(parameterTypes);
				} else if (element instanceof FunctionTemplate) {
					FunctionTemplate functionTemplate= (FunctionTemplate) element;
					functionTemplate.setTemplateParameterTypes(parameterTypes);
				} else if (element instanceof MethodTemplate) {
					MethodTemplate methodTemplate= (MethodTemplate) element;
					methodTemplate.setTemplateParameterTypes(parameterTypes);
				}
				// set the body position
				if (element instanceof SourceManipulation) {
					setBodyPosition((SourceManipulation)element, templateDeclaration);
				}
			}
		} else if (declaration instanceof ICPPASTTemplateDeclaration) {
			// strange: template decl inside template decl
			createTemplateDeclaration(parent, (ICPPASTTemplateDeclaration)declaration);
		} else if (declaration instanceof IASTProblemDeclaration) {
			// ignore problem declarations (or create special elements for debugging?)
		} else {
			assert false : "TODO: " + declaration.getClass().getName(); //$NON-NLS-1$
		}
	}

	/**
	 * Handle extern "C" and related kinds.
	 *
	 * @param parent
	 * @param declaration
	 * @throws CModelException
	 * @throws DOMException
	 */
	private void createLinkageSpecification(Parent parent, ICPPASTLinkageSpecification linkageDeclaration) throws CModelException, DOMException {
		IASTDeclaration[] declarations= linkageDeclaration.getDeclarations(true);
		for (IASTDeclaration declaration : declarations) {
			if (linkageDeclaration.getFileLocation() != null || isLocalToFile(declaration)) {
				createDeclaration(parent, declaration);
			}
		}
	}

	private CElement[] createSimpleDeclarations(Parent parent, IASTSimpleDeclaration declaration, boolean isTemplate) throws CModelException, DOMException {
		final IASTDeclSpecifier declSpecifier= declaration.getDeclSpecifier();
		final IASTDeclarator[] declarators= declaration.getDeclarators();
		final CElement[] elements;
		boolean isCompositeType= declSpecifier instanceof IASTCompositeTypeSpecifier || declSpecifier instanceof IASTEnumerationSpecifier;
		if (declarators.length == 0) {
			elements= new CElement[1];
			final CElement element= createSimpleDeclaration(parent, declSpecifier, null, isTemplate);
			elements[0]= element;
		} else {
			if (isCompositeType) {
				createSimpleDeclaration(parent, declSpecifier, null, isTemplate);
			}
			elements= new CElement[declarators.length];
			for (int i= 0; i < declarators.length; i++) {
				final IASTDeclarator declarator= declarators[i];
				final CElement element= createSimpleDeclaration(parent, declSpecifier, declarator, isTemplate);
				if (!isTemplate && element instanceof SourceManipulation && declarators.length > 1) {
					setBodyPosition((SourceManipulation)element, declarator);
				}
				elements[i]= element;
			}
		}
		return elements;
	}

	private CElement createSimpleDeclaration(Parent parent, IASTDeclSpecifier declSpecifier, IASTDeclarator declarator, boolean isTemplate) throws CModelException, DOMException {
		if (declSpecifier instanceof IASTCompositeTypeSpecifier) {
			if (declarator != null) {
				return createTypedefOrFunctionOrVariable(parent, declSpecifier, declarator, isTemplate);
			}
			return createCompositeType(parent, (IASTCompositeTypeSpecifier)declSpecifier, isTemplate);
		} else if (declSpecifier instanceof IASTElaboratedTypeSpecifier) {
			if (declarator != null) {
				return createTypedefOrFunctionOrVariable(parent, declSpecifier, declarator, isTemplate);
			}
			return createElaboratedTypeDeclaration(parent, (IASTElaboratedTypeSpecifier)declSpecifier, isTemplate);
		} else if (declSpecifier instanceof IASTEnumerationSpecifier) {
			if (declarator != null) {
				return createTypedefOrFunctionOrVariable(parent, declSpecifier, declarator, isTemplate);
			}
			return createEnumeration(parent, (IASTEnumerationSpecifier)declSpecifier);
		} else if (declSpecifier instanceof IASTNamedTypeSpecifier) {
			if (declarator != null) {
				return createTypedefOrFunctionOrVariable(parent, declSpecifier, declarator, isTemplate);
			}
		} else if (declSpecifier instanceof IASTSimpleDeclSpecifier) {
			if (declarator != null) {
				return createTypedefOrFunctionOrVariable(parent, declSpecifier, declarator, isTemplate);
			}
		} else {
			assert false : "TODO: " + declSpecifier.getClass().getName(); //$NON-NLS-1$
		}
		return null;
	}

	private CElement createTypedefOrFunctionOrVariable(Parent parent, IASTDeclSpecifier declSpecifier,
			IASTDeclarator declarator, boolean isTemplate) throws CModelException {
		assert declarator != null;
		if (declSpecifier.getStorageClass() == IASTDeclSpecifier.sc_typedef) {
			return createTypeDef(parent, declSpecifier, declarator);
		}
		IASTDeclarator typeRelevant= ASTQueries.findTypeRelevantDeclarator(declarator);
		if (typeRelevant instanceof IASTFunctionDeclarator) {
			return createFunctionDeclaration(parent, declSpecifier, (IASTFunctionDeclarator)typeRelevant, isTemplate);
		}
		return createVariable(parent, declSpecifier, declarator, isTemplate);
	}

	private void createNamespace(Parent parent, ICPPASTNamespaceDefinition declaration) throws CModelException, DOMException{
		// create element
		final String type= Keywords.NAMESPACE;
		final IASTName name= declaration.getName();
		final String nsName= ASTStringUtil.getQualifiedName(name);
		final Namespace element= new Namespace(parent, nsName);
		setIndex(element);
		element.setActive(declaration.isActive());

		// add to parent
		parent.addChild(element);
		// set positions
		if (name != null && nsName.length() > 0) {
			setIdentifierPosition(element, name);
		} else {
			final IASTFileLocation nsLocation= declaration.getFileLocation();
			if (nsLocation != null) {
				element.setIdPos(nsLocation.getNodeOffset(), type.length());
			}
		}
		setBodyPosition(element, declaration);

		element.setTypeName(type);

		IASTDeclaration[] nsDeclarations= declaration.getDeclarations(true);
		for (IASTDeclaration nsDeclaration : nsDeclarations) {
			if (declaration.getFileLocation() != null || isLocalToFile(nsDeclaration)) {
				createDeclaration(element, nsDeclaration);
			}
		}
	}

	private StructureDeclaration createElaboratedTypeDeclaration(Parent parent, IASTElaboratedTypeSpecifier elaboratedTypeSpecifier, boolean isTemplate) throws CModelException{
		// create element
		final String type;
		final int kind;
		switch (elaboratedTypeSpecifier.getKind()) {
		case IASTElaboratedTypeSpecifier.k_struct:
			kind= (isTemplate) ? ICElement.C_TEMPLATE_STRUCT_DECLARATION : ICElement.C_STRUCT_DECLARATION;
			type= Keywords.STRUCT;
			break;
		case IASTElaboratedTypeSpecifier.k_union:
			kind= (isTemplate) ? ICElement.C_TEMPLATE_UNION_DECLARATION : ICElement.C_UNION_DECLARATION;
			type= Keywords.UNION;
			break;
		case ICPPASTElaboratedTypeSpecifier.k_class:
			kind= (isTemplate) ? ICElement.C_TEMPLATE_CLASS_DECLARATION : ICElement.C_CLASS_DECLARATION;
			type= Keywords.CLASS;
			break;
		case IASTElaboratedTypeSpecifier.k_enum:
			// do we need a C_ENUM_DECLARATION?
			kind= ICElement.C_CLASS_DECLARATION;
			type= Keywords.ENUM;
			break;
		default:
			kind= ICElement.C_CLASS_DECLARATION;
			type= ""; //$NON-NLS-1$
			break;
		}

		final IASTName astClassName= elaboratedTypeSpecifier.getName();
		final String className= ASTStringUtil.getSimpleName(astClassName);

		StructureDeclaration element;
		if (isTemplate) {
			element= new StructureTemplateDeclaration(parent, kind, className);
		} else {
			element= new StructureDeclaration(parent, className, kind);
		}
		setIndex(element);
		element.setActive(elaboratedTypeSpecifier.isActive());
		StructureInfo info= (StructureInfo) getElementInfo(element);
		info.setTypeName(type);

		// add to parent
		parent.addChild(element);

		// set positions
		if (className.length() > 0) {
			setIdentifierPosition(info, astClassName);
		} else {
			final IASTFileLocation classLocation= getMinFileLocation(elaboratedTypeSpecifier.getNodeLocations());
			if (classLocation != null) {
				info.setIdPos(classLocation.getNodeOffset(), type.length());
			}
		}
		setBodyPosition(info, elaboratedTypeSpecifier);
		return element;
	}

	private Enumeration createEnumeration(Parent parent, IASTEnumerationSpecifier enumSpecifier) throws CModelException{
		// create element
		final String type= Keywords.ENUM;
		final IASTName astEnumName= enumSpecifier.getName();
		final String enumName= ASTStringUtil.getSimpleName(astEnumName);
		final Enumeration element= new Enumeration (parent, enumName);
		setIndex(element);
		element.setActive(enumSpecifier.isActive());
		
		// add to parent
		parent.addChild(element);
		final IASTEnumerator[] enumerators= enumSpecifier.getEnumerators();
		for (final IASTEnumerator enumerator : enumerators) {
			createEnumerator(element, enumerator);
		}
		EnumerationInfo info= (EnumerationInfo) getElementInfo(element);
		// set enumeration position
		if (astEnumName != null && enumName.length() > 0) {
			setIdentifierPosition(info, astEnumName);
		} else {
			final IASTFileLocation enumLocation= enumSpecifier.getFileLocation();
			info.setIdPos(enumLocation.getNodeOffset(), type.length());
		}
		setBodyPosition(info, enumSpecifier);
		info.setTypeName(type);
		return element;
	}

	private Enumerator createEnumerator(Parent enumarator, IASTEnumerator enumDef) throws CModelException{
		final IASTName astEnumName= enumDef.getName();
		final Enumerator element= new Enumerator (enumarator, ASTStringUtil.getSimpleName(astEnumName));
		setIndex(element);
		element.setActive(enumDef.isActive());

		IASTExpression initialValue= enumDef.getValue();
		if (initialValue != null){
			element.setConstantExpression(ASTStringUtil.getExpressionString(initialValue));
		}
		// add to parent
		enumarator.addChild(element);
		// set positions
		setIdentifierPosition(element, astEnumName);
		setBodyPosition(element, enumDef);
		return element;
	}

	private Structure createCompositeType(Parent parent, IASTCompositeTypeSpecifier compositeTypeSpecifier, boolean isTemplate) throws CModelException, DOMException{
		// create element
		final String type;
		final int kind;
		final ASTAccessVisibility defaultVisibility;
		switch (compositeTypeSpecifier.getKey()) {
		case IASTCompositeTypeSpecifier.k_struct:
			kind= (isTemplate) ? ICElement.C_TEMPLATE_STRUCT : ICElement.C_STRUCT;
			type= Keywords.STRUCT;
			defaultVisibility= ASTAccessVisibility.PUBLIC;
			break;
		case IASTCompositeTypeSpecifier.k_union:
			kind= (isTemplate) ? ICElement.C_TEMPLATE_UNION : ICElement.C_UNION;
			type= Keywords.UNION;
			defaultVisibility= ASTAccessVisibility.PUBLIC;
			break;
		case ICPPASTCompositeTypeSpecifier.k_class:
			kind= (isTemplate) ? ICElement.C_TEMPLATE_CLASS : ICElement.C_CLASS;
			type= Keywords.CLASS;
			defaultVisibility= ASTAccessVisibility.PRIVATE;
			break;
		default:
			kind= ICElement.C_CLASS;
			type= ""; //$NON-NLS-1$
			defaultVisibility= ASTAccessVisibility.PUBLIC;
			break;
		}

		final IASTName astClassName= compositeTypeSpecifier.getName();
		final String className= ASTStringUtil.getSimpleName(astClassName);

		final Structure element;
		if (!isTemplate) {
			Structure classElement= new Structure(parent, kind, className);
			element= classElement;
		} else {
			StructureTemplate classTemplate= new StructureTemplate(parent, kind, className);
			element= classTemplate;
		}
		setIndex(element);
		element.setActive(compositeTypeSpecifier.isActive());

		if (compositeTypeSpecifier instanceof ICPPASTCompositeTypeSpecifier) {
			// store super classes names
			final ICPPASTCompositeTypeSpecifier cppCompositeTypeSpecifier= (ICPPASTCompositeTypeSpecifier)compositeTypeSpecifier;
			ICPPASTBaseSpecifier[] baseSpecifiers= cppCompositeTypeSpecifier.getBaseSpecifiers();
			for (final ICPPASTBaseSpecifier baseSpecifier : baseSpecifiers) {
				final IASTName baseName= baseSpecifier.getName();
				final ASTAccessVisibility visibility;
				switch (baseSpecifier.getVisibility()) {
				case ICPPASTBaseSpecifier.v_public:
					visibility= ASTAccessVisibility.PUBLIC;
					break;
				case ICPPASTBaseSpecifier.v_protected:
					visibility= ASTAccessVisibility.PROTECTED;
					break;
				case ICPPASTBaseSpecifier.v_private:
					visibility= ASTAccessVisibility.PRIVATE;
					break;
				default:
					visibility= ASTAccessVisibility.PUBLIC;
				}
				element.addSuperClass(ASTStringUtil.getSimpleName(baseName), visibility);
			}
		}

		StructureInfo info= (StructureInfo) getElementInfo(element);
		info.setTypeName(type);

		// add to parent
		parent.addChild(element);
		// set positions
		if (!isTemplate){
			setBodyPosition(info, compositeTypeSpecifier);
		}
		if (className.length() > 0) {
			setIdentifierPosition(info, astClassName);
		} else {
			final IASTFileLocation classLocation= getMinFileLocation(compositeTypeSpecifier.getNodeLocations());
			if (classLocation != null) {
				info.setIdPos(classLocation.getNodeOffset(), type.length());
			}
		}
		// add members
		pushDefaultVisibility(defaultVisibility);
		try {
			final IASTDeclaration[] memberDeclarations= compositeTypeSpecifier.getDeclarations(true);
			for (IASTDeclaration member : memberDeclarations) {
				if (compositeTypeSpecifier.getFileLocation() != null || isLocalToFile(member)) {
					createDeclaration(element, member);
				}
			}
		} finally {
			popDefaultVisibility();
		}
		return element;
	}

	private TypeDef createTypeDef(Parent parent, IASTDeclSpecifier declSpecifier, IASTDeclarator declarator) throws CModelException{
		IASTDeclarator nestedDeclarator= declarator;
		while (nestedDeclarator.getNestedDeclarator() != null) {
			nestedDeclarator= nestedDeclarator.getNestedDeclarator();
		}
		final IASTName astTypedefName= nestedDeclarator.getName();
		if (astTypedefName == null) {
			return null;
		}
		// create the element
		String name= ASTStringUtil.getSimpleName(astTypedefName);

        final TypeDef element= new TypeDef(parent, name);
		setIndex(element);
		element.setActive(declarator.isActive());

        String typeName= ASTStringUtil.getSignatureString(declSpecifier, declarator);
		element.setTypeName(typeName);

		// add to parent
		parent.addChild(element);

		// set positions
		final SourceManipulationInfo info= getSourceManipulationInfo(element);
		if (name.length() > 0) {
			setIdentifierPosition(info, astTypedefName);
		} else {
			setIdentifierPosition(info, declSpecifier);
		}
		if (declSpecifier instanceof IASTCompositeTypeSpecifier) {
			setBodyPosition(info, astTypedefName);
		} else {
			setBodyPosition(info, declSpecifier.getParent());
		}
		return element;
	}

	private VariableDeclaration createVariable(Parent parent, IASTDeclSpecifier specifier, IASTDeclarator declarator, boolean isTemplate) throws CModelException {
		IASTDeclarator nestedDeclarator= declarator;
		while (nestedDeclarator.getNestedDeclarator() != null) {
			nestedDeclarator= nestedDeclarator.getNestedDeclarator();
		}
		final IASTName astVariableName= nestedDeclarator.getName();
		if (astVariableName == null) {
			return null;
		}
		final String variableName= ASTStringUtil.getQualifiedName(astVariableName);

		final VariableDeclaration element;
		final SourceManipulationInfo info;

		if (declarator instanceof IASTFieldDeclarator || parent instanceof IStructure
				|| CModelBuilder2.getScope(astVariableName) instanceof ICPPClassScope) {
			// field
			Field newElement= new Field(parent, variableName);
			setIndex(newElement);
			final FieldInfo fieldInfo= (FieldInfo)getElementInfo(newElement);
			if (specifier instanceof ICPPASTDeclSpecifier) {
				final ICPPASTDeclSpecifier cppSpecifier= (ICPPASTDeclSpecifier)specifier;
				fieldInfo.setMutable(cppSpecifier.getStorageClass() == IASTDeclSpecifier.sc_mutable);
			}
			fieldInfo.setTypeName(ASTStringUtil.getSignatureString(specifier, declarator));
			fieldInfo.setVisibility(getCurrentVisibility());
			fieldInfo.setConst(specifier.isConst());
			fieldInfo.setVolatile(specifier.isVolatile());
			element= newElement;
			info= fieldInfo;
		} else {
			if (isTemplate) {
				// template variable
				VariableTemplate newElement= new VariableTemplate(parent, variableName);
				element= newElement;
			} else {
				if (specifier.getStorageClass() == IASTDeclSpecifier.sc_extern) {
					// variable declaration
					VariableDeclaration newElement= new VariableDeclaration(parent, variableName);
					element= newElement;
				} else {
					// variable
					Variable newElement= new Variable(parent, variableName);
					element= newElement;
				}
			}
			setIndex(element);
			VariableInfo varInfo= (VariableInfo) getElementInfo(element);
			varInfo.setTypeName(ASTStringUtil.getSignatureString(specifier, declarator));
			varInfo.setConst(specifier.isConst());
			varInfo.setVolatile(specifier.isVolatile());
			info= varInfo;
		}
		element.setActive(declarator.isActive());
		// TODO [cmodel] correctly resolve isStatic
		element.setStatic(specifier.getStorageClass() == IASTDeclSpecifier.sc_static);
		// add to parent
		parent.addChild(element);

		// set positions
		setIdentifierPosition(info, astVariableName);
		if (!isTemplate) {
			if (specifier instanceof IASTCompositeTypeSpecifier) {
				setBodyPosition(info, astVariableName);
			} else {
				setBodyPosition(info, specifier.getParent());
			}
		}
		return element;
	}

	private FunctionDeclaration createFunctionDefinition(Parent parent, IASTFunctionDefinition functionDeclaration, boolean isTemplate) throws CModelException, DOMException {
		final IASTFunctionDeclarator declarator= functionDeclaration.getDeclarator();
		IASTDeclarator nestedDeclarator= declarator;
		while (nestedDeclarator.getNestedDeclarator() != null) {
			nestedDeclarator= nestedDeclarator.getNestedDeclarator();
		}
		final IASTName name= nestedDeclarator.getName();
        if (name == null) {
            // Something is wrong, skip this element
            return null;
        }

        final IASTDeclSpecifier declSpecifier= functionDeclaration.getDeclSpecifier();

		final String simpleName= ASTStringUtil.getSimpleName(name);
		final String[] parameterTypes= ASTStringUtil.getParameterSignatureArray(declarator);
		final String returnType= ASTStringUtil.getReturnTypeString(declSpecifier, declarator);

		final FunctionDeclaration element;
		final FunctionInfo info;

		if (declarator instanceof ICPPASTFunctionDeclarator) {

			final ICPPASTFunctionDeclarator cppFunctionDeclarator= (ICPPASTFunctionDeclarator)declarator;
			final IASTName simpleAstName;
			if (name instanceof ICPPASTQualifiedName) {
				final ICPPASTQualifiedName quName= (ICPPASTQualifiedName)name;
				simpleAstName= quName.getLastName();
			} else {
				simpleAstName= name;
			}
			IScope scope= null;
			// try to avoid expensive resolution of scope and binding
			boolean isMethod= parent instanceof IStructure;
			if (!isMethod && name instanceof ICPPASTQualifiedName) {
				final IASTName[] names= ((ICPPASTQualifiedName)name).getNames();
				if (isTemplate) {
					for (IASTName name2 : names) {
						if (name2 instanceof ICPPASTTemplateId) {
							isMethod= true;
							break;
						}
					}
				}
				if (!isMethod) {
					scope= CPPVisitor.getContainingScope(simpleAstName);
			        isMethod= scope instanceof ICPPClassScope || simpleAstName.resolveBinding() instanceof ICPPMethod;
				}
			}
			if (isMethod) {
				// method
				final MethodDeclaration methodElement;
				final String methodName;
				if (parent instanceof IStructure) {
					methodName= ASTStringUtil.getSimpleName(name);
				} else {
					methodName= ASTStringUtil.getQualifiedName(name);
				}
				if (isTemplate) {
					methodElement= new MethodTemplate(parent, methodName);
				} else {
					methodElement= new Method(parent, methodName);
				}
				element= methodElement;
				// establish identity attributes before getElementInfo()
				methodElement.setParameterTypes(parameterTypes);
				methodElement.setReturnType(returnType);
				methodElement.setConst(cppFunctionDeclarator.isConst());
				setIndex(element);

				final MethodInfo methodInfo= (MethodInfo) getElementInfo(methodElement);
				info= methodInfo;
				ICPPMethod methodBinding= null;
				if (scope != null) {
					final IBinding binding= simpleAstName.resolveBinding();
					if (binding instanceof ICPPMethod) {
						methodBinding= (ICPPMethod)binding;
					}
				}
				if (methodBinding != null) {
					methodInfo.setVirtual(methodBinding.isVirtual());
					methodInfo.setInline(methodBinding.isInline());
					methodInfo.setFriend(((ICPPASTDeclSpecifier)declSpecifier).isFriend());
					methodInfo.setVolatile(cppFunctionDeclarator.isVolatile());
					methodInfo.setVisibility(adaptVisibilityConstant(methodBinding.getVisibility()));
					methodInfo.setPureVirtual(false);
					methodElement.setConstructor(methodBinding instanceof ICPPConstructor);
					methodElement.setDestructor(methodBinding.isDestructor());
				} else {
					if (declSpecifier instanceof ICPPASTDeclSpecifier) {
						final ICPPASTDeclSpecifier cppDeclSpecifier= (ICPPASTDeclSpecifier)declSpecifier;
						methodInfo.setVirtual(cppDeclSpecifier.isVirtual());
						methodInfo.setInline(cppDeclSpecifier.isInline());
						methodInfo.setFriend(cppDeclSpecifier.isFriend());
					}
					methodInfo.setVolatile(cppFunctionDeclarator.isVolatile());
					methodInfo.setVisibility(getCurrentVisibility());
					methodInfo.setPureVirtual(false);
					final boolean isConstructor;
					if (scope != null) {
						isConstructor= CPPVisitor.isConstructor(scope, declarator);
					} else if (parent instanceof IStructure) {
						isConstructor= parent.getElementName().equals(simpleName);
					} else if (name instanceof ICPPASTQualifiedName) {
						final ICPPASTQualifiedName quName= (ICPPASTQualifiedName)name;
						final IASTName[] names= quName.getNames();
						isConstructor= names.length >= 2 && simpleName.equals(ASTStringUtil.getSimpleName(names[names.length-2]));
					} else {
						isConstructor= false;
					}
					methodElement.setConstructor(isConstructor);
					methodElement.setDestructor(simpleName.charAt(0) == '~');
				}
			} else {
				String functionName= ASTStringUtil.getQualifiedName(name);
				// strip namespace qualifier if parent is same namespace
				if (name instanceof ICPPASTQualifiedName && parent instanceof INamespace) {
					final ICPPASTQualifiedName quName= (ICPPASTQualifiedName)name;
					final IASTName[] names= quName.getNames();
				 	if (names.length >= 2 && parent.getElementName().equals(ASTStringUtil.getSimpleName(names[names.length-2]))) {
				 		functionName= simpleName;
				 	}
				}
				if (isTemplate) {
					// template function
					element= new FunctionTemplate(parent, functionName);
				} else {
					// function
					element= new Function(parent, functionName);
				}
				element.setParameterTypes(parameterTypes);
				element.setReturnType(returnType);
				setIndex(element);
				
				info= (FunctionInfo) getElementInfo(element);
				info.setConst(cppFunctionDeclarator.isConst());
			}

		} else {
			final String functionName= ASTStringUtil.getQualifiedName(name);
			element= new Function(parent, functionName);
			element.setParameterTypes(parameterTypes);
			element.setReturnType(returnType);
			setIndex(element);
			
			info= (FunctionInfo) getElementInfo(element);
		}
		element.setActive(functionDeclaration.isActive());

		// TODO [cmodel] correctly resolve isStatic
		info.setStatic(declSpecifier.getStorageClass() == IASTDeclSpecifier.sc_static);

		// add to parent
		parent.addChild(element);

		// set positions
		setIdentifierPosition(info, name);
		if (!isTemplate) {
			setBodyPosition(info, functionDeclaration);
		}
		return element;
	}

	private FunctionDeclaration createFunctionDeclaration(Parent parent, IASTDeclSpecifier declSpecifier, IASTFunctionDeclarator declarator, boolean isTemplate) throws CModelException {
		IASTDeclarator nestedDeclarator= declarator;
		while (nestedDeclarator.getNestedDeclarator() != null) {
			nestedDeclarator= nestedDeclarator.getNestedDeclarator();
		}
		final IASTName name= nestedDeclarator.getName();
        if (name == null) {
            // Something is wrong, skip this element
            return null;
        }

		final String functionName= ASTStringUtil.getSimpleName(name);
		final String[] parameterTypes= ASTStringUtil.getParameterSignatureArray(declarator);
		final String returnType= ASTStringUtil.getReturnTypeString(declSpecifier, declarator);

		final FunctionDeclaration element;
		final FunctionInfo info;

		if (declarator instanceof ICPPASTFunctionDeclarator) {
			final ICPPASTFunctionDeclarator cppFunctionDeclarator= (ICPPASTFunctionDeclarator)declarator;
			if (parent instanceof IStructure) {
				// method
				final MethodDeclaration methodElement;
				if (isTemplate) {
					methodElement= new MethodTemplateDeclaration(parent, functionName);
				} else {
					methodElement= new MethodDeclaration(parent, functionName);
				}
				element= methodElement;
				// establish identity attributes before getElementInfo()
				methodElement.setParameterTypes(parameterTypes);
				methodElement.setReturnType(returnType);
				methodElement.setConst(cppFunctionDeclarator.isConst());
				setIndex(element);
				final MethodInfo methodInfo= (MethodInfo) getElementInfo(methodElement);
				info= methodInfo;
				if (declSpecifier instanceof ICPPASTDeclSpecifier) {
					final ICPPASTDeclSpecifier cppDeclSpecifier= (ICPPASTDeclSpecifier)declSpecifier;
					methodInfo.setVirtual(cppDeclSpecifier.isVirtual());
					methodInfo.setInline(cppDeclSpecifier.isInline());
					methodInfo.setFriend(cppDeclSpecifier.isFriend());
				}
				methodInfo.setVolatile(cppFunctionDeclarator.isVolatile());
				methodInfo.setVisibility(getCurrentVisibility());
				methodInfo.setPureVirtual(cppFunctionDeclarator.isPureVirtual());
				methodElement.setConstructor(functionName.equals(parent.getElementName()));
				methodElement.setDestructor(functionName.charAt(0) == '~');
			} else {
				if (isTemplate) {
					element= new FunctionTemplateDeclaration(parent, functionName);
				} else {
					element= new FunctionDeclaration(parent, functionName);
				}
				element.setParameterTypes(parameterTypes);
				element.setReturnType(returnType);
				setIndex(element);
				
				info= (FunctionInfo)getElementInfo(element);
				info.setConst(cppFunctionDeclarator.isConst());
			}
		} else if (declarator instanceof IASTStandardFunctionDeclarator) {
			if (isTemplate) {
				element= new FunctionTemplateDeclaration(parent, functionName);
			} else {
				element= new FunctionDeclaration(parent, functionName);
			}
			element.setParameterTypes(parameterTypes);
			element.setReturnType(returnType);
			setIndex(element);

			info= (FunctionInfo)getElementInfo(element);
		} else {
			assert false;
			return null;
		}
		element.setActive(declarator.isActive());
		
		// TODO [cmodel] correctly resolve isStatic
		info.setStatic(declSpecifier.getStorageClass() == IASTDeclSpecifier.sc_static);

		// add to parent
		parent.addChild(element);

		// hook up the offsets
		setIdentifierPosition(info, name);
		if (!isTemplate) {
			setBodyPosition(info, declarator.getParent());
		}
		return element;
	}

	private Using createUsingDirective(Parent parent, ICPPASTUsingDirective usingDirDeclaration) throws CModelException{
		// create the element
		IASTName name= usingDirDeclaration.getQualifiedName();
        Using element= new Using(parent, ASTStringUtil.getQualifiedName(name), true);
		setIndex(element);
		element.setActive(usingDirDeclaration.isActive());

		// add to parent
		parent.addChild(element);

		// set positions
		setIdentifierPosition(element, name);
		setBodyPosition(element, usingDirDeclaration);
		return element;
	}

	private Using createUsingDeclaration(Parent parent, ICPPASTUsingDeclaration usingDeclaration) throws CModelException{
		// create the element
		IASTName name= usingDeclaration.getName();
		Using element= new Using(parent, ASTStringUtil.getSimpleName(name), false);
		setIndex(element);
		element.setActive(usingDeclaration.isActive());
		// add to parent
		parent.addChild(element);

		// set positions
		setIdentifierPosition(element, name);
		setBodyPosition(element, usingDeclaration);
		return element;
	}

	private CElementInfo getElementInfo(CElement cElement) {
		CElementInfo info = fNewElements.get(cElement);
		if (info == null) {
			info = cElement.createElementInfo();
			fNewElements.put(cElement, info);
		}
		return info;
	}

	private SourceManipulationInfo getSourceManipulationInfo(SourceManipulation cElement) {
		return (SourceManipulationInfo) getElementInfo(cElement);
	}

	/**
	 * Utility method to set the body position of an element from an AST node.
	 *
	 * @param element
	 * @param astNode
	 * @throws CModelException
	 */
	private void setBodyPosition(SourceManipulation element, IASTNode astNode) throws CModelException {
		setBodyPosition(getSourceManipulationInfo(element), astNode);
	}

	/**
	 * Utility method to set the body position of an element from an AST node.
	 *
	 * @param info
	 * @param astNode
	 */
	private void setBodyPosition(SourceManipulationInfo info, IASTNode astNode) {
		final IASTFileLocation location= astNode.getFileLocation();
		if (location != null) {
			info.setPos(location.getNodeOffset(), location.getNodeLength());
			info.setLines(location.getStartingLineNumber(), location.getEndingLineNumber());
		} else {
			final IASTNodeLocation[] locations= astNode.getNodeLocations();
			final IASTFileLocation minLocation= getMinFileLocation(locations);
			if (minLocation != null) {
				final IASTFileLocation maxLocation= getMaxFileLocation(locations);
				if (maxLocation != null) {
					final int startOffset= minLocation.getNodeOffset();
					final int endOffset= maxLocation.getNodeOffset() + maxLocation.getNodeLength();
					info.setPos(startOffset, endOffset - startOffset);
					final int startLine= minLocation.getStartingLineNumber();
					final int endLine= maxLocation.getEndingLineNumber();
					info.setLines(startLine, endLine);
				}
			}
		}
	}

	/**
	 * Utility method to set the identifier position of an element from an AST name.
	 *
	 * @param element
	 * @param astName
	 * @throws CModelException
	 */
	private void setIdentifierPosition(SourceManipulation element, IASTName astName) throws CModelException {
		setIdentifierPosition(getSourceManipulationInfo(element), astName);
	}

	/**
	 * Utility method to set the identifier position of an element from an AST name.
	 *
	 * @param info
	 * @param astName
	 */
	private void setIdentifierPosition(SourceManipulationInfo info, IASTNode astName) {
		final IASTFileLocation location= astName.getFileLocation();
		if (location != null) {
			info.setIdPos(location.getNodeOffset(), location.getNodeLength());
		} else {
			final IASTNodeLocation[] locations= astName.getNodeLocations();
			final IASTFileLocation minLocation= getMinFileLocation(locations);
			if (minLocation != null) {
				final IASTFileLocation maxLocation= getMaxFileLocation(locations);
				if (maxLocation != null) {
					final int startOffset= minLocation.getNodeOffset();
					final int endOffset= maxLocation.getNodeOffset() + maxLocation.getNodeLength();
					info.setIdPos(startOffset, endOffset - startOffset);
				}
			}
		}
	}

	private static IASTFileLocation getMaxFileLocation(IASTNodeLocation[] locations) {
		if (locations == null || locations.length == 0) {
			return null;
		}
		final IASTNodeLocation nodeLocation= locations[locations.length-1];
		return nodeLocation.asFileLocation();
	}

	private static IASTFileLocation getMinFileLocation(IASTNodeLocation[] locations) {
		if (locations == null || locations.length == 0) {
			return null;
		}
		final IASTNodeLocation nodeLocation= locations[0];
		return nodeLocation.asFileLocation();
	}

	/**
	 * Handle the special "declaration" visibility label
	 * @param visibilityLabel
	 */
	private void handleVisibilityLabel(ICPPASTVisibilityLabel visibilityLabel) {
		setCurrentVisibility(adaptVisibilityConstant(visibilityLabel.getVisibility()));
	}

	/**
	 * Convert the given <code>ICPPASTVisiblityLabel</code> visibility constant
	 * into an <code>ASTAccessVisibility</code>.
	 *
	 * @param visibility
	 * @return the corresponding <code>ASTAccessVisibility</code>
	 */
	private ASTAccessVisibility adaptVisibilityConstant(int visibility) {
		switch (visibility) {
		case ICPPASTVisibilityLabel.v_public:
			return ASTAccessVisibility.PUBLIC;
		case ICPPASTVisibilityLabel.v_protected:
			return ASTAccessVisibility.PROTECTED;
		case ICPPASTVisibilityLabel.v_private:
			return ASTAccessVisibility.PRIVATE;
		}
		assert false : "Unknown visibility"; //$NON-NLS-1$
		return ASTAccessVisibility.PUBLIC;
	}

	private void setCurrentVisibility(ASTAccessVisibility visibility) {
		fCurrentVisibility= visibility;
	}
	private ASTAccessVisibility getCurrentVisibility() {
		if (fCurrentVisibility == null) {
			return ASTAccessVisibility.PUBLIC;
		}
		return fCurrentVisibility;
	}

	/**
	 * Pop the default visibility from the outer scope.
	 */
	private void popDefaultVisibility() {
		if (!fVisibilityStack.isEmpty()) {
			setCurrentVisibility(fVisibilityStack.pop());
		}
	}

	/**
	 * Push given visibility as default class/struct/union visibility.
	 *
	 * @param visibility
	 */
	private void pushDefaultVisibility(ASTAccessVisibility visibility) {
		fVisibilityStack.push(getCurrentVisibility());
		setCurrentVisibility(visibility);
	}

	/**
	 * Determine the scope for given name.
	 * 
	 * @param astName
	 * @return the scope or <code>null</code>
	 */
	private static IScope getScope(IASTName astName) {
		IBinding binding= astName.resolveBinding();
		if (binding != null) {
			try {
				return binding.getScope();
			} catch (DOMException e) {
				return null;
			}
		}
		return null;
	}
}
