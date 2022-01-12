/*******************************************************************************
 * Copyright (c) 2009, 2016 Alena Laskavaia
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.codan.core.cxx.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.codan.core.cxx.Activator;
import org.eclipse.cdt.codan.core.model.AbstractCheckerWithProblemPreferences;
import org.eclipse.cdt.codan.core.model.ICheckerInvocationContext;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemLocation;
import org.eclipse.cdt.codan.core.model.IProblemLocationFactory;
import org.eclipse.cdt.codan.core.model.IRunnableInEditorChecker;
import org.eclipse.cdt.core.dom.ast.IASTComment;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
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
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * Convenience implementation of checker that works on index-based AST of a
 * C/C++
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
			// Run the checker only if the index is fully initialized. Otherwise it may produce
			// false positives.
			if (modelCache.getIndex().isFullyInitialized()) {
				IASTTranslationUnit ast = modelCache.getAST();
				if (ast != null) {
					synchronized (ast) {
						processAst(ast);
					}
				}
			}
		} catch (CoreException e) {
			Activator.log(e);
		} finally {
			modelCache = null;
		}
	}

	@Override
	public synchronized void processModel(Object model, ICheckerInvocationContext context) {
		if (model instanceof IASTTranslationUnit) {
			IASTTranslationUnit ast = (IASTTranslationUnit) model;
			// Run the checker only if the index was fully initialized when the file was parsed.
			// Otherwise the checker may produce false positives.
			if (ast.isBasedOnIncompleteIndex())
				return;
			setContext(context);
			synchronized (context) {
				modelCache = context.get(CxxModelsCache.class);
				if (modelCache == null) {
					modelCache = new CxxModelsCache(ast);
					context.add(modelCache);
				}
			}
			CPPSemantics.pushLookupPoint(ast);
			try {
				processAst(ast);
			} finally {
				modelCache = null;
				setContext(null);
				CPPSemantics.popLookupPoint();
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

	/**
	 * Checks if problem should be reported, in this case it will check line
	 * comments, later can add filters or what not.
	 *
	 * @param problem - problem kind
	 * @param loc - location
	 * @param args - arguments
	 * @since 3.4
	 */
	@Override
	protected boolean shouldProduceProblem(IProblem problem, IProblemLocation loc, Object... args) {
		String suppressionComment = (String) getSuppressionCommentPreference(problem).getValue();
		if (suppressionComment.isEmpty())
			return super.shouldProduceProblem(problem, loc, args);
		List<IASTComment> lineComments = getLineCommentsForLocation(loc);
		for (IASTComment astComment : lineComments) {
			if (astComment.getRawSignature().contains(suppressionComment))
				return false;
		}
		return super.shouldProduceProblem(problem, loc, args);
	}

	protected List<IASTComment> getLineCommentsForLocation(IProblemLocation loc) {
		ArrayList<IASTComment> lineComments = new ArrayList<>();
		try {
			IASTComment[] commentsArray = modelCache.getAST().getComments();
			for (IASTComment comm : commentsArray) {
				IASTFileLocation fileLocation = comm.getFileLocation();
				if (fileLocation.getStartingLineNumber() == loc.getLineNumber()) {
					String problemFile = loc.getFile().getLocation().toOSString();
					String commentFile = fileLocation.getFileName();
					if (problemFile.equals(commentFile)) {
						lineComments.add(comm);
					}
				}
			}
		} catch (OperationCanceledException | CoreException e) {
			Activator.log(e);
		}
		return lineComments;
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
		// If the raw signature has more than one line, we highlight only the code
		// related to the problem. However, if the problem is associated with a
		// node representing a class definition, do not highlight the entire class
		// definition, because that can result in many lines being highlighted.
		if (astNode instanceof IASTCompositeTypeSpecifier) {
			return locFactory.createProblemLocation(getFile(), line);
		}
		int start = astLocation.getNodeOffset();
		int end = start + astLocation.getNodeLength();
		return locFactory.createProblemLocation(getFile(), start, end, line);
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
