/*******************************************************************************
 * Copyright (c) 2009, 2011 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.codan.core.cxx.model;

import org.eclipse.cdt.codan.core.cxx.Activator;
import org.eclipse.cdt.codan.core.model.AbstractCheckerWithProblemPreferences;
import org.eclipse.cdt.codan.core.model.ICheckerInvocationContext;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemLocation;
import org.eclipse.cdt.codan.core.model.IProblemLocationFactory;
import org.eclipse.cdt.codan.core.model.IRunnableInEditorChecker;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTImageLocation;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansionLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * Convenience implementation of checker that works on index-based AST of a C/C++
 * program.
 * 
 * Clients may extend this class.
 */
public abstract class AbstractIndexAstChecker extends AbstractCheckerWithProblemPreferences
		implements ICAstChecker, IRunnableInEditorChecker {
	private CxxModelsCache modelCache;

	@Override
	public synchronized boolean processResource(IResource resource) throws OperationCanceledException {
		if (!shouldProduceProblems(resource))
			return false;
		if (!(resource instanceof IFile))
			return true;
			
		processFile((IFile) resource);
		return false;
	}

	private void processFile(IFile file) throws OperationCanceledException {
		ICheckerInvocationContext context = getContext();
		synchronized (context) {
			modelCache = context.get(CxxModelsCache.class);
			if (modelCache == null) {
				ICElement celement = CoreModel.getDefault().create(file);
				if (!(celement instanceof ITranslationUnit)) {
					return;
				}
				modelCache = new CxxModelsCache((ITranslationUnit) celement);
				context.add(modelCache);
			}
		}

		try {
			IASTTranslationUnit ast = modelCache.getAST();
			if (ast != null) {
				synchronized (ast) {
					processAst(ast);
				}
			}
		} catch (CoreException e) {
			Activator.log(e);
		} finally {
			modelCache = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see IRunnableInEditorChecker#processModel(Object, ICheckerInvocationContext)
	 */
	@Override
	public synchronized void processModel(Object model, ICheckerInvocationContext context) {
		if (model instanceof IASTTranslationUnit) {
			setContext(context);
			IASTTranslationUnit ast = (IASTTranslationUnit) model;
			synchronized (context) {
				modelCache = context.get(CxxModelsCache.class);
				if (modelCache == null) {
					modelCache = new CxxModelsCache(ast);
					context.add(modelCache);
				}
			}
			try {
				processAst(ast);
			} finally {
				modelCache = null; 
				setContext(null);
			}
		}
	}

	@Override
	public boolean runInEditor() {
		return true;
	}

	public void reportProblem(String id, IASTNode astNode, Object... args) {
		IProblemLocation loc = getProblemLocation(astNode);
		if (loc != null)
			reportProblem(id, loc, args);
	}

	public void reportProblem(IProblem problem, IASTNode astNode, Object... args) {
		IProblemLocation loc = getProblemLocation(astNode);
		if (loc != null)
			reportProblem(problem, loc, args);
	}

	protected IProblemLocation getProblemLocation(IASTNode astNode) {
		IASTFileLocation astLocation = astNode.getFileLocation();
		return getProblemLocation(astNode, astLocation);
	}

	private IProblemLocation getProblemLocation(IASTNode astNode, IASTFileLocation astLocation) {
		int line = astLocation.getStartingLineNumber();
		IProblemLocationFactory locFactory = getRuntime().getProblemLocationFactory();
		if (enclosedInMacroExpansion(astNode) && astNode instanceof IASTName) {
			IASTImageLocation imageLocation = ((IASTName) astNode).getImageLocation();
			if (imageLocation != null) {
				int start = imageLocation.getNodeOffset();
				int end = start + imageLocation.getNodeLength();
				return locFactory.createProblemLocation(getFile(), start, end, line);
			}
		}
		if (line == astLocation.getEndingLineNumber()) {
			return locFactory.createProblemLocation(getFile(), astLocation.getNodeOffset(),
					astLocation.getNodeOffset() + astLocation.getNodeLength(), line);
		}
		return locFactory.createProblemLocation(getFile(), line);
	}

	protected static boolean enclosedInMacroExpansion(IASTNode node) {
		IASTNodeLocation[] nodeLocations = node.getNodeLocations();
		return nodeLocations.length == 1 && nodeLocations[0] instanceof IASTMacroExpansionLocation;
	}

	protected static boolean includesMacroExpansion(IASTNode node) {
		for (IASTNodeLocation nodeLocation : node.getNodeLocations()) {
			if (nodeLocation instanceof IASTMacroExpansionLocation)
				return true;
		}
		return false;
	}

	protected IFile getFile() {
		return modelCache.getFile();
	}

	protected IProject getProject() {
		IFile file = getFile();
		return file == null ? null : file.getProject();
	}

	protected CxxModelsCache getModelCache() {
		return modelCache;
	}

	protected ICodanCommentMap getCommentMap() {
		return modelCache.getCommentedNodeMap();
	}
}
