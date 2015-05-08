/*******************************************************************************
 * Copyright (c) 2010, 2015 Tomasz Wesolowski and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tomasz Wesolowski - initial API and implementation
 *     Sergey Prigogin (Google)
 *     Patrick Hofer [bug 345872]
 *     Nathan Ridge [bug 345872]
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.ui.CDTUITools;

import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.model.ASTStringUtil;
import org.eclipse.cdt.internal.core.model.ext.ICElementHandle;

import org.eclipse.cdt.internal.ui.text.ICReconcilingListener;
import org.eclipse.cdt.internal.ui.viewsupport.IndexUI;

public class OverrideIndicatorManager implements ICReconcilingListener {
	public static final String ANNOTATION_TYPE = "org.eclipse.cdt.ui.overrideIndicator"; //$NON-NLS-1$
	private final HashMap<ICPPClassType, ICPPMethod[]> methodsCache = new HashMap<>(); 

	public static final int ANNOTATION_IMPLEMENTS = 0;
	public static final int ANNOTATION_OVERRIDES = 1;
	public static final int ANNOTATION_SHADOWS = 2;

	public class OverrideIndicator extends Annotation {
		public static final String ANNOTATION_TYPE_ID = "org.eclipse.cdt.ui.overrideIndicator"; //$NON-NLS-1$
		private final int type;
		private final ICElementHandle elementHandle;

		public OverrideIndicator(int resultType, String message, ICElementHandle elementHandle) {
			super(ANNOTATION_TYPE_ID, false, message);
			this.type = resultType;
			this.elementHandle = elementHandle;
		}

		public int getIndicationType() {
			return type;
		}

		public void open() {
			try {
				CDTUITools.openInEditor(elementHandle, true, true);
			} catch (CoreException e) {
			}
		}
	}

	private final IAnnotationModel fAnnotationModel;
	private Annotation[] fOverrideAnnotations;
	
	private final Object fAnnotationModelLockObject;
	private int annotationKind;
	private String annotationMessage;
	
	public OverrideIndicatorManager(IAnnotationModel annotationModel, IASTTranslationUnit ast) {
		fAnnotationModel = annotationModel;
		fAnnotationModelLockObject = getLockObject(fAnnotationModel);
		updateAnnotations(ast, new NullProgressMonitor());
	}

	/**
	 * Returns the lock object for the given annotation model.
	 *
	 * @param annotationModel the annotation model
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
	
	protected void updateAnnotations(IASTTranslationUnit ast, IProgressMonitor progressMonitor) {
		if (ast == null || progressMonitor.isCanceled())
			return;
		
		final IIndex index = ast.getIndex();
		final Map<Annotation, Position> annotationMap= new HashMap<Annotation, Position>(50);
		
		class MethodFinder extends ASTVisitor {
			{
				shouldVisitDeclarators = true;
			}

			@Override
			public int visit(IASTDeclarator declarator) {
				if (!(declarator instanceof ICPPASTFunctionDeclarator)) {
					return PROCESS_CONTINUE;
				}
				IASTDeclarator decl = ASTQueries.findInnermostDeclarator(declarator);
				IASTName name = decl.getName();
				if (name != null) {
					IBinding binding = name.resolveBinding();
					if (binding instanceof ICPPMethod) {
						ICPPMethod method = (ICPPMethod) binding;
						try {
							ICPPMethod overriddenMethod = testForOverride(method, declarator);
							if (overriddenMethod != null) {
								try {
									ICElementHandle baseDeclaration = IndexUI.findAnyDeclaration(index, null, overriddenMethod);
									if (baseDeclaration == null) {
										ICElementHandle[] allDefinitions = IndexUI.findAllDefinitions(index, overriddenMethod);
										if (allDefinitions.length > 0) {
											baseDeclaration = allDefinitions[0];
										}
									}
									
									OverrideIndicator indicator = new OverrideIndicator(annotationKind, annotationMessage, baseDeclaration);
									
									IASTFileLocation fileLocation = declarator.getFileLocation();
									Position position = new Position(fileLocation.getNodeOffset(), fileLocation.getNodeLength());
									annotationMap.put(indicator, position);
								} catch (CoreException e) {
								}
							}
						} catch (DOMException e) {
						}
					}
				}
				return PROCESS_CONTINUE;
			}
		}
			
		try {
			ast.accept(new MethodFinder());
		} finally {
			methodsCache.clear();
		}
		
		if (progressMonitor.isCanceled())
			return;

		synchronized (fAnnotationModelLockObject) {
			if (fAnnotationModel instanceof IAnnotationModelExtension) {
				((IAnnotationModelExtension)fAnnotationModel).replaceAnnotations(fOverrideAnnotations, annotationMap);
			} else {
				removeAnnotations();
				for (Map.Entry<Annotation, Position> entry : annotationMap.entrySet()) {
					fAnnotationModel.addAnnotation(entry.getKey(), entry.getValue());
				}
			}
			fOverrideAnnotations= annotationMap.keySet().toArray(new Annotation[annotationMap.keySet().size()]);
		}
	}
	
	private ICPPMethod testForOverride(ICPPMethod method, IASTNode point) throws DOMException {
		if (method.isDestructor() || method.isPureVirtual()) {
			return null;
		}
		
		ICPPBase[] bases = ClassTypeHelper.getBases(method.getClassOwner(), point);
		if (bases.length == 0) {
			return null;
		}
				
		ICPPClassType owningClass = method.getClassOwner();
		ICPPMethod overriddenMethod = getOverriddenMethodInBaseClass(owningClass, method, point);
		
		if (overriddenMethod != null) {
			StringBuilder sb = new StringBuilder();
			if (annotationKind == ANNOTATION_IMPLEMENTS) {
				sb.append(CEditorMessages.OverrideIndicatorManager_implements);
			} else if (annotationKind == ANNOTATION_OVERRIDES){
				sb.append(CEditorMessages.OverrideIndicatorManager_overrides);
			} else if (annotationKind == ANNOTATION_SHADOWS) {
				sb.append(CEditorMessages.OverrideIndicatorManager_shadows);
			}
			sb.append(' ');
			sb.append(ASTStringUtil.join(overriddenMethod.getQualifiedName(), "::")); //$NON-NLS-1$
			
			if (bases.length > 1) {
				boolean foundInDirectlyDerivedBaseClass = false;
				ICPPClassType matchedMethodOwner = overriddenMethod.getClassOwner();
				for (ICPPBase base : bases) {
					if (base.getBaseClass() == matchedMethodOwner) {
						foundInDirectlyDerivedBaseClass = true;
						break;
					}
				}
				if (!foundInDirectlyDerivedBaseClass) {
					ICPPClassType indirectingClass = null;
					for (ICPPBase base : bases) {
						indirectingClass = (ICPPClassType)base.getBaseClass();
						if (getOverriddenMethodInBaseClass(indirectingClass, method, point) != null)
							break;
					}
					if (indirectingClass != null) {
						sb.append(' ');
						sb.append(CEditorMessages.OverrideIndicatorManager_via);
						sb.append(' ');
						sb.append(ASTStringUtil.join(indirectingClass.getQualifiedName(), "::")); //$NON-NLS-1$
					}
				}
			}
			
			annotationMessage = sb.toString();
			return overriddenMethod;
		}
		return null;
	}

	private ICPPMethod getOverriddenMethodInBaseClass(ICPPClassType aClass, ICPPMethod testedMethod,
			IASTNode point) throws DOMException {
		final String testedMethodName = testedMethod.getName();

		ICPPMethod[] allInheritedMethods;
		if (methodsCache.containsKey(aClass)) {
			allInheritedMethods = methodsCache.get(aClass);
		} else {
			ICPPMethod[] inheritedMethods = null;
			ICPPClassType[] bases= ClassTypeHelper.getAllBases(aClass, point);
			for (ICPPClassType base : bases) {
				inheritedMethods = ArrayUtil.addAll(ICPPMethod.class, inheritedMethods, 
						ClassTypeHelper.getDeclaredMethods(base, point));
			}
			allInheritedMethods = ArrayUtil.trim(ICPPMethod.class, inheritedMethods);
			methodsCache.put(aClass, allInheritedMethods);
		}

		for (ICPPMethod method : allInheritedMethods) {
			if (method.getName().equals(testedMethodName)) {
				if (method.isVirtual()) {
					if (ClassTypeHelper.isOverrider(testedMethod, method)) {
						if (method.isPureVirtual()) {
							annotationKind = ANNOTATION_IMPLEMENTS;
						} else {
							annotationKind = ANNOTATION_OVERRIDES;
						}
					} else {
						// The method has same name as virtual method in base, but does not override
						// it (e.g. because it has a different signature), it shadows it.
						annotationKind = ANNOTATION_SHADOWS;
					}
				} else {
					// The method has same name and is not virtual, it hides/shadows the method
					// in the base class.
					annotationKind = ANNOTATION_SHADOWS;
				}
				return method;
			}
		}
		return null;
	}

	/**
	 * Removes all override indicators from this manager's annotation model.
	 */
	void removeAnnotations() {
		if (fOverrideAnnotations == null)
			return;

		synchronized (fAnnotationModelLockObject) {
			if (fAnnotationModel instanceof IAnnotationModelExtension) {
				((IAnnotationModelExtension)fAnnotationModel).replaceAnnotations(fOverrideAnnotations, null);
			} else {
				for (int i= 0, length= fOverrideAnnotations.length; i < length; i++)
					fAnnotationModel.removeAnnotation(fOverrideAnnotations[i]);
			}
			fOverrideAnnotations= null;
		}
	}

	@Override
	public void aboutToBeReconciled() {
	}

	@Override
	public void reconciled(IASTTranslationUnit ast, boolean force, IProgressMonitor progressMonitor) {
		updateAnnotations(ast, progressMonitor);
	}
}
