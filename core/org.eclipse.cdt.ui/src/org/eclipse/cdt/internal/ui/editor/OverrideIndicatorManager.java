/*******************************************************************************
 * Copyright (c) 2010 Tomasz Wesolowski and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Tomasz Wesolowski - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.ui.CDTUITools;

import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.model.ASTStringUtil;
import org.eclipse.cdt.internal.core.model.ext.ICElementHandle;

import org.eclipse.cdt.internal.ui.text.ICReconcilingListener;
import org.eclipse.cdt.internal.ui.viewsupport.IndexUI;

public class OverrideIndicatorManager implements ICReconcilingListener {

	private static final String MESSAGE_SEPARATOR = ";\n"; //$NON-NLS-1$

	public static class OverrideInfo {

		public int nodeOffset;
		public int resultType;
		public String message;
		public int nodeLength;
		
		public IBinding binding;

		public OverrideInfo(int nodeOffset, int nodeLength, int markerType, String message, IBinding binding) {
			this.nodeOffset = nodeOffset;
			this.resultType = markerType;
			this.message = message;
			this.binding = binding;
		}
	}

	public static final int RESULT_OVERRIDES = 0;
	public static final int RESULT_IMPLEMENTS = 1;
	public static final int RESULT_SHADOWS = 2;

	public class OverrideIndicator extends Annotation {

		public static final String ANNOTATION_TYPE_ID = "org.eclipse.cdt.ui.overrideIndicator"; //$NON-NLS-1$
		private int type;
		private ICElementHandle declaration;

		public OverrideIndicator(int resultType, String message, IBinding binding, IIndex index) {
			super(ANNOTATION_TYPE_ID, false, message);
			this.type = resultType;
			try {
				declaration = IndexUI.findAnyDeclaration(index, null, binding);
				if (declaration == null) {
					ICElementHandle[] allDefinitions = IndexUI.findAllDefinitions(index, binding);
					if (allDefinitions.length > 0) {
						declaration = allDefinitions[0];
					}
				}
			} catch (CoreException e) {
			}
		}

		public int getIndicationType() {
			return type;
		}

		public void open() {
			try {
				CDTUITools.openInEditor(declaration, true, true);
			} catch (CoreException e) {
			}

		}

	}

	private IAnnotationModel fAnnotationModel;
	private Vector<OverrideIndicator> fOverrideAnnotations = new Vector<OverrideIndicator>();
	private Object fAnnotationModelLockObject;

	public OverrideIndicatorManager(IAnnotationModel annotationModel) {
		fAnnotationModel = annotationModel;
		fAnnotationModelLockObject = getLockObject(fAnnotationModel);
	}

	private void handleResult(OverrideInfo info, IIndex index) {

		Position position = new Position(info.nodeOffset, info.nodeLength);

		OverrideIndicator indicator = new OverrideIndicator(info.resultType, info.message, info.binding, index);
		synchronized (fAnnotationModelLockObject) {
			fAnnotationModel.addAnnotation(indicator, position);
		}
		fOverrideAnnotations.add(indicator);
	}

	/**
	 * Removes all override indicators from this manager's annotation model.
	 */
	public void removeAnnotations() {
		if (fOverrideAnnotations == null)
			return;

		synchronized (fAnnotationModelLockObject) {
			for (Annotation i : fOverrideAnnotations)
				fAnnotationModel.removeAnnotation(i);
			fOverrideAnnotations.clear();
		}
	}

	public void generateAnnotations(IASTTranslationUnit ast, final IIndex index) {

		class MethodDeclarationFinder extends ASTVisitor {
			{
				shouldVisitDeclarations = true;
			}

			@Override
			public int visit(IASTDeclaration declaration) {
				try {
					IBinding binding = null;
					ICPPMethod method = null;
					if (isFunctionDeclaration(declaration)) {
						binding = getDeclarationBinding(declaration);
					} else if (declaration instanceof IASTFunctionDefinition) {
						binding = getDefinitionBinding((IASTFunctionDefinition) declaration);
					}
					if (binding instanceof ICPPMethod) {
						method = (ICPPMethod) binding;
						OverrideInfo overrideInfo = testForOverride(method, declaration.getFileLocation());
						if (overrideInfo != null) {
							handleResult(overrideInfo, index);
						}
					} 
				} catch (DOMException e) {
				}
				// go to next declaration
				return PROCESS_SKIP;
			}
		}

		class CompositeTypeFinder extends ASTVisitor {
			{
				shouldVisitDeclSpecifiers = true;
			}

			@Override
			public int visit(IASTDeclSpecifier declSpec) {
				if (declSpec instanceof ICPPASTCompositeTypeSpecifier) {
					declSpec.accept(new MethodDeclarationFinder());
				}
				return PROCESS_CONTINUE;
			}
		}

		class MethodDefinitionFinder extends ASTVisitor {
			{
				shouldVisitDeclarations = true;
			}

			@Override
			public int visit(IASTDeclaration declaration) {
				try {
					if (!(declaration instanceof IASTFunctionDefinition)) {
						return PROCESS_SKIP;
					}
					IASTFunctionDefinition definition = (IASTFunctionDefinition) declaration;
					IBinding definitionBinding = getDefinitionBinding(definition);
					if (!(definitionBinding instanceof ICPPMethod)) {
						return PROCESS_SKIP;
					}
					ICPPMethod method = (ICPPMethod) definitionBinding;
					OverrideInfo overrideInfo = testForOverride(method, definition.getFileLocation());
					if (overrideInfo != null) {
						handleResult(overrideInfo, index);
					}
				} catch (DOMException e) {
				}
				return PROCESS_SKIP;
			}
		}

		ast.accept(new CompositeTypeFinder());
		ast.accept(new MethodDefinitionFinder());
	}
	
	public static OverrideInfo testForOverride(ICPPMethod testedOverride, IASTFileLocation location) throws DOMException {

		testedOverride.getClassOwner().getBases();

		boolean onlyPureVirtual = true;
		StringBuilder sb = new StringBuilder();
		Set<ICPPMethod> overridenMethods = new HashSet<ICPPMethod>();
		Set<ICPPMethod> shadowedMethods = new HashSet<ICPPMethod>();
		
		Set<ICPPClassType> alreadyTestedBases = new HashSet<ICPPClassType>();

		ICPPBase[] bases = testedOverride.getClassOwner().getBases();
		
		// Don't override 'self' in cyclic inheritance
		alreadyTestedBases.add(testedOverride.getClassOwner());

		for (ICPPBase base : bases) {
			ICPPClassType testedClass;
			if (!(base.getBaseClass() instanceof ICPPClassType)) {
				continue;
			}
			testedClass = (ICPPClassType) base.getBaseClass();

			overridenMethods.clear();
			shadowedMethods.clear();
			handleBaseClass(testedClass, testedOverride, overridenMethods, shadowedMethods, alreadyTestedBases);

			for (ICPPMethod overriddenMethod : overridenMethods) {

				if (sb.length() > 0) {
					sb.append(MESSAGE_SEPARATOR);
				}
				if (overriddenMethod.isPureVirtual()) {
					sb.append(CEditorMessages.OverrideIndicatorManager_implements);
				} else {
					sb.append(CEditorMessages.OverrideIndicatorManager_overrides);
					onlyPureVirtual = false;
				}
				sb.append(' ');
				sb.append(getQualifiedNameString(overriddenMethod));

				if (bases.length > 1 && overriddenMethod.getClassOwner() != testedClass) {
					sb.append(' ');
					sb.append(CEditorMessages.OverrideIndicatorManager_via);
					sb.append(' ');
					sb.append(getQualifiedNameString(testedClass));
				}
			}
			for (ICPPMethod shadowedMethod : shadowedMethods) {
				if (sb.length() > 0) {
					sb.append(MESSAGE_SEPARATOR);
				}
				sb.append(CEditorMessages.OverrideIndicatorManager_shadows);
				sb.append(' ');
				sb.append(getQualifiedNameString(shadowedMethod));
			}
		}
		
		int markerType;
		if (overridenMethods.size() > 0) {
			markerType = onlyPureVirtual ? RESULT_IMPLEMENTS : RESULT_OVERRIDES;
		} else {
			markerType = RESULT_SHADOWS;
		}
		
		IBinding bindingToOpen = null;
		if (overridenMethods.size() > 0) {
			bindingToOpen = overridenMethods.iterator().next();
		} else if (shadowedMethods.size() > 0) {
			bindingToOpen = shadowedMethods.iterator().next();
		}
		
		if (sb.length() > 0) {
			OverrideInfo info = new OverrideInfo(location.getNodeOffset(), location.getNodeLength(), markerType,
					sb.toString(), bindingToOpen);
			return info;
		}
		return null;

	}

	/**
	 * If the class directly has a valid override for testedOverride, it is added to foundBindings. Otherwise
	 * each base class is added to handleBaseClass.
	 * 
	 * @param shadowedMethods
	 * @param alreadyTestedBases 
	 * 
	 * @throws DOMException
	 */
	private static void handleBaseClass(ICPPClassType aClass, ICPPMethod testedOverride,
			Set<ICPPMethod> foundMethods, Set<ICPPMethod> shadowedMethods, Set<ICPPClassType> alreadyTestedBases) throws DOMException {
		
		if (alreadyTestedBases.contains(aClass)) {
			return;
		} else {
			alreadyTestedBases.add(aClass);
		}

		Vector<ICPPMethod> validOverrides = new Vector<ICPPMethod>();
		for (ICPPMethod method : aClass.getDeclaredMethods()) {
			if (testedOverride.getName().equals(method.getName())) {
				if (ClassTypeHelper.isOverrider(testedOverride, method)) {
					validOverrides.add(method);
				} else if (sameParameters(testedOverride, method)) {
					shadowedMethods.add(method);
				}
			}
		}
		if (validOverrides.size() > 1) {
			/* System.err.println("Found many valid overrides"); */
		}
		if (validOverrides.size() >= 1) {
			foundMethods.addAll(validOverrides);
			return;
		}

		for (ICPPBase b : aClass.getBases()) {
			if (!(b.getBaseClass() instanceof ICPPClassType)) {
				continue;
			}
			ICPPClassType baseClass = (ICPPClassType) b.getBaseClass();
			handleBaseClass(baseClass, testedOverride, foundMethods, shadowedMethods, alreadyTestedBases);
		}
	}

	private static boolean sameParameters(ICPPMethod a, ICPPMethod b) throws DOMException {
		ICPPFunctionType aType = a.getType();
		ICPPFunctionType bType = b.getType();
		if (aType.getParameterTypes().length != bType.getParameterTypes().length) {
			return false;
		}
		for (int i = 0; i < aType.getParameterTypes().length; ++i) {
			IType overrideParamType = aType.getParameterTypes()[i];
			IType methodParamType = bType.getParameterTypes()[i];
			if (!overrideParamType.isSameType(methodParamType)) {
				return false;
			}
		}
		return true;
	}

	private static String getQualifiedNameString(ICPPBinding binding) throws DOMException {
		String methodQualifiedName = ASTStringUtil.join(binding.getQualifiedName(), "::"); //$NON-NLS-1$
		return methodQualifiedName;
	}

	private static boolean isFunctionDeclaration(IASTDeclaration declaration) {
		if (!(declaration instanceof IASTSimpleDeclaration)) {
			return false;
		}
		IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) declaration;
		IASTDeclarator[] declarators = simpleDecl.getDeclarators();
		if (declarators.length < 1) {
			return false;
		}
		IASTDeclarator declarator = ASTQueries.findInnermostDeclarator(declarators[0]);
		return (declarator instanceof IASTFunctionDeclarator);
	}

	private static IBinding getDefinitionBinding(IASTFunctionDefinition definition) {
		IASTDeclarator declarator = ASTQueries.findInnermostDeclarator(definition.getDeclarator());
		return declarator.getName().resolveBinding();
	}

	private static IBinding getDeclarationBinding(IASTDeclaration declaration) {
		for (IASTNode node : declaration.getChildren()) {
			if (node instanceof IASTDeclarator) {
				IASTDeclarator decl = ASTQueries.findInnermostDeclarator((IASTDeclarator) node);
				return decl.getName().resolveBinding();
			}
		}
		return null;
	}

	public void aboutToBeReconciled() {
	}

	public void reconciled(IASTTranslationUnit ast, boolean force, IProgressMonitor progressMonitor) {
		if (ast == null) {
			return;
		}
		IIndex index = ast.getIndex();
		removeAnnotations();
		generateAnnotations(ast, index);
	}

	/**
	 * Returns the lock object for the given annotation model.
	 * 
	 * @param annotationModel
	 *            the annotation model
	 * @return the annotation model's lock object
	 */
	private Object getLockObject(IAnnotationModel annotationModel) {
		if (annotationModel instanceof ISynchronizable) {
			Object lock = ((ISynchronizable) annotationModel).getLockObject();
			if (lock != null)
				return lock;
		}
		return annotationModel;
	}

}
