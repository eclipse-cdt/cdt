package org.eclipse.cdt.core.pdom;

import org.eclipse.cdt.core.dom.IASTServiceProvider;
import org.eclipse.cdt.core.dom.ICodeReaderFactory;
import org.eclipse.cdt.core.dom.IParserConfiguration;
import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;

public class PDOMASTServiceProvider implements IASTServiceProvider {

	public IASTTranslationUnit getTranslationUnit(IFile fileToParse)
			throws UnsupportedDialectException {
		// TODO Auto-generated method stub
		return null;
	}

	public IASTTranslationUnit getTranslationUnit(IStorage fileToParse,
			IProject project, ICodeReaderFactory fileCreator)
			throws UnsupportedDialectException {
		// TODO Auto-generated method stub
		return null;
	}

	public IASTTranslationUnit getTranslationUnit(IStorage fileToParse,
			IProject project) throws UnsupportedDialectException {
		// TODO Auto-generated method stub
		return null;
	}

	public IASTTranslationUnit getTranslationUnit(IFile fileToParse,
			ICodeReaderFactory fileCreator) throws UnsupportedDialectException {
		// TODO Auto-generated method stub
		return null;
	}

	public IASTTranslationUnit getTranslationUnit(IFile fileToParse,
			ICodeReaderFactory fileCreator, IParserConfiguration configuration)
			throws UnsupportedDialectException {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTCompletionNode getCompletionNode(IFile fileToParse, int offset,
			ICodeReaderFactory fileCreator) throws UnsupportedDialectException {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTCompletionNode getCompletionNode(IStorage fileToParse,
			IProject project, int offset, ICodeReaderFactory fileCreator)
			throws UnsupportedDialectException {
		// TODO Auto-generated method stub
		return null;
	}

}
