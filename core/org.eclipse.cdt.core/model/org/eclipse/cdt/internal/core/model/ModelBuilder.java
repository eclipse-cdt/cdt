package org.eclipse.cdt.internal.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.internal.parser.IStructurizerCallback;

public class ModelBuilder implements IStructurizerCallback {
	
	private TranslationUnit fCurrFile;
	private CElement fCurrElement;
	
	public ModelBuilder(TranslationUnit file) {
		fCurrFile = file;
		fCurrElement = file;		
	}
	
	private final int fixLength(int startPos, int endPos) {
		if (endPos < startPos) {
			return 0;
		} else {
			return endPos - startPos + 1;
		}
	}	
	
	public void includeDecl(String name, int startPos, int endPos, int startLine, int endLine) {
		Include elem= new Include(fCurrFile, name, true ); // assume standard inclusion
		elem.setPos(startPos, fixLength(startPos, endPos));
		elem.setIdPos(startPos, fixLength(startPos, endPos));
		elem.setLines(startLine, endLine);
		
		fCurrFile.addChild(elem);
	}
		
	public void defineDecl(String name, int startPos, int endPos, int startLine, int endLine) {
		Macro elem= new Macro(fCurrFile, name);
		elem.setPos(startPos, fixLength(startPos, endPos));
		elem.setIdPos(startPos, fixLength(startPos, endPos));
		elem.setLines(startLine, endLine);
		fCurrFile.addChild(elem);
	}		
	
	public void functionDeclBegin(String name, int nameStartPos, int nameEndPos,
		int declStartPos, int startPos, int type, int modifiers) {
		//if (!assertCurrElement( new int[] { CElement.C_FILE, CElement.C_STRUCTURE, CElement.C_UNION, CElement.C_CLASS})) {
		//	return;
		//}

		CElement elem;
		if (fCurrElement instanceof IStructure) {
			elem = new Method(fCurrElement, name);
		} else {
			if(type == ICElement.C_FUNCTION_DECLARATION) {
				elem = new FunctionDeclaration(fCurrElement, name);
			} else {
				elem= new Function(fCurrElement, name);
			}
		}
		elem.setPos(declStartPos, 0);
		elem.setIdPos(nameStartPos, fixLength(nameStartPos, nameEndPos));
		elem.setLines(startPos, -1);
		
		fCurrElement.addChild(elem);
		fCurrElement= elem;
	}
		
	public void functionDeclEnd(int declEndPos, int endLine, boolean prototype) {
		//if (!assertCurrElement( new int[] { CElement.C_FUNCTION  })) {
		//	return;
		//}
		if(prototype == true && fCurrElement.getParent() instanceof Parent) {
			// Need to delete the current function and create a new object
			CElement elem, oldElem = fCurrElement;
			elem = new FunctionDeclaration(fCurrElement.getParent(), fCurrElement.getElementName());
			elem.setPos(oldElem.getStartPos(), 0);
			elem.setIdPos(oldElem.getIdStartPos(), oldElem.getIdLength());
			elem.setLines(oldElem.getStartLine(), -1);
			((Parent)fCurrElement.getParent()).addChild(elem);
			((Parent)fCurrElement.getParent()).removeChild(oldElem);
			fCurrElement = elem;
		}
		int declStartPos= fCurrElement.getStartPos();
		fCurrElement.setPos(declStartPos, fixLength(declStartPos, declEndPos));
		int startLine = fCurrElement.getStartLine();
		fCurrElement.setLines(startLine, endLine);
		fCurrElement= (CElement)fCurrElement.getParent();
	}
	
	public void fieldDecl(String name, int nameStartPos, int nameEndPos, int declStartPos,
		int declEndPos, int startLine, int endLine, int modifiers) {

		CElement elem;
		if (fCurrElement instanceof IStructure) {
			elem = new Field(fCurrElement, name);
		} else {
			elem = new Variable(fCurrElement, name);
		}
//System.out.println(elem.toDebugString() + " --> " + fCurrElement.toDebugString());
		elem.setPos(declStartPos, fixLength(declStartPos, declEndPos));
		elem.setIdPos(nameStartPos, fixLength(nameStartPos, nameEndPos));		
		elem.setLines(startLine, endLine);
	
		fCurrElement.addChild(elem);		
	}	
	
	public void structDeclBegin(String name, int kind, int nameStartPos, int nameEndPos,
		int declStartPos, int startLine, int modifiers) {
		//if (!assertCurrElement( new int[] { CElement.C_FILE, CElement.C_STRUCTURE, CElement.C_UNION, CElement.C_CLASS })) {
		//	return;
		//}

		if(isAnonymousStructure(name)) {
			name = new String("[anonymous]");
		}
		
		Structure elem= new Structure(fCurrElement, kind, name);
		elem.setPos(declStartPos, 0);
		elem.setIdPos(nameStartPos, fixLength(nameStartPos, nameEndPos));		
		elem.setLines(startLine, -1);
		
		fCurrElement.addChild(elem);
		fCurrElement= elem;
//System.out.println(elem.toDebugString() + " --> " + fCurrElement.toDebugString());
	}		
		
		
	public void structDeclEnd(int declEndPos, int endLine) {
		//assertCurrElement( new int[] { CElement.C_STRUCTURE, CElement.C_UNION, CElement.C_CLASS  });
		int declStartPos= fCurrElement.getStartPos();
		fCurrElement.setPos(declStartPos, fixLength(declStartPos, declEndPos));
		int startLine= fCurrElement.getStartLine();
		fCurrElement.setLines(startLine, endLine);
		fCurrElement= (CElement)fCurrElement.getParent();
	}			
	
	public void superDecl(String name) {
		//assertCurrElement( new int[] { CElement.C_STRUCTURE, CElement.C_UNION, CElement.C_CLASS  });
		if (fCurrElement instanceof IStructure) {
			((Structure)fCurrElement).addSuperClass(name);
		}		
	}
	
	public void reportError(Throwable throwable) {
		// System.out.println("ModelBuilder: error " + throwable.getMessage());
	}
	
	private boolean assertCurrElement(int[] acceptedTypes) {
		boolean isOk= false;
		int currType= fCurrElement.getElementType();
		for (int i= 0; i < acceptedTypes.length; i++) {
			if (currType == acceptedTypes[i]) {
				isOk= true;
			}
		}
		
		if (!isOk) {
			StringBuffer buf= new StringBuffer();
			buf.append("ModelBuilder: type check failed, is: ");
			buf.append(CElement.getTypeString(currType));
			buf.append(", should be [ ");
			for (int i= 0; i < acceptedTypes.length; i++) {
				buf.append(CElement.getTypeString(acceptedTypes[i]));
				buf.append(" ");
			}
			buf.append("]");
			
			//CPlugin.getPlugin().logErrorStatus(buf.toString(), null);
		}
		return isOk;
	}
	
	private boolean isAnonymousStructure(String name) {
		if (Character.isJavaIdentifierStart(name.charAt(0))) {
			return false;
		} else {
			return true;
		}
	}
}
