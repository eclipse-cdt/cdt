package org.eclipse.cdt.internal.ui.compare;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.swt.graphics.Image;

import org.eclipse.compare.IEditableContent;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.DocumentRangeNode;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.compare.structuremergeviewer.IStructureCreator;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.parser.CStructurizer;
import org.eclipse.cdt.internal.parser.IStructurizerCallback;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.DocumentInputStream;
import org.eclipse.cdt.ui.CUIPlugin;
/**
 * 
 */
public class CStructureCreator implements IStructureCreator {			

	private static final String NAME= "CStructureCreator.name";

	public CStructureCreator() {
	}

	/**
	 * @see IStructureCreator#getTitle
	 */		
	public String getName() {
		return CUIPlugin.getResourceString(NAME);
	}
	
	/**
	 * @see IStructureCreator#getStructure
	 */		
	public IStructureComparator getStructure(Object input) {
		
		String s= null;
		if (input instanceof IStreamContentAccessor) {
			try {
				s= readString(((IStreamContentAccessor) input).getContents());
			} catch(CoreException ex) {
			}
		}
			
		Document doc= new Document(s != null ? s : "");
				
		CNode root= new CNode(null, ICElement.C_FILE, "root", doc, 0, 0);		
				
		DocumentInputStream is= new DocumentInputStream(doc);
		IStructurizerCallback callback= new CNodeTreeConstructor(root, doc);
		try {
			CStructurizer.getCStructurizer().parse(callback, is);
		} catch (NodeConstructorError e) {
			System.out.println("Parse error: " + e);
			return null;
		} catch (IOException e) {
			return null;
		}				
		
		return root;
	}	

	/**
	 * @see IStructureCreator#canSave
	 */	
	public boolean canSave() {
		return true;
	}

	/**
	 * @see IStructureCreator#locate
	 */		
	public IStructureComparator locate(Object path, Object source) {
		return null;
	}

	/**
	 * @see IStructureCreator#canRewriteTree
	 */		
	public boolean canRewriteTree() {
		return false;
	}

	/**
	 * @see IStructureCreator#rewriteTree
	 */		
	public void rewriteTree(Differencer differencer, IDiffContainer root) {
	}	

	/**
	 * @see IStructureCreator#save
	 */	
	public void save(IStructureComparator structure, Object input) {
		if (input instanceof IEditableContent && structure instanceof CNode) {
			IDocument doc= ((CNode)structure).getDocument();
			IEditableContent bca= (IEditableContent) input;
			String c= doc.get();
			bca.setContent(c.getBytes());
		}
	}
	
	/**
	 * @see IStructureCreator#getContents
	 */
	public String getContents(Object node, boolean ignoreWhitespace) {
		if (node instanceof IStreamContentAccessor) {
			IStreamContentAccessor sca= (IStreamContentAccessor) node;
			try {
				return readString(sca.getContents());
			} catch (CoreException ex) {
			}
		}
		return null;
	}

	private static class CNode extends DocumentRangeNode implements ITypedElement {
		
		private String fImageKey;
		private CNode fParent;
		private int fCode;
		
		public CNode(CNode parent, int type, String id, IDocument doc, int start, int length) {
			super(type, id, doc, start, length);
			fCode = type;
			fImageKey= CPluginImages.IMG_OBJS_STRUCT;
			fParent= parent;
		}
			
		/**
		 * Returns the type code of this node.
		 * The type code is uninterpreted client data which can be set in the constructor.
		 *
		 * @return the type code of this node
		 */
		public int getTypeCode() {
			return fCode;
		}
		
		public void setTypeCode(int code) {
			fCode = code;
		}
		
		public CNode getParent() {
			return fParent;
		}
				
		/**
		 * @see ITypedInput#getName
		 */
		public String getName() {
			return getId();
		}

		/**
		 * @see ITypedInput#getType
		 */
		public String getType() {
			return "c2";
		}
		
		/**
		 * @see ITypedInput#getImage
		 */
		public Image getImage() {
			if (fImageKey != null) {
				return CPluginImages.get(fImageKey);
			}
			return null;
		}
	};			
	
	private static class NodeConstructorError extends Error {
	}
	
	private static class CNodeTreeConstructor implements IStructurizerCallback {
		
		private CNode fRoot;
		private CNode fCurrent;
		
		private IDocument fDocument;
		
		public CNodeTreeConstructor(CNode root, IDocument doc) {
			fRoot= root;
			fCurrent= root;
			fDocument= doc;
		}
		
		private CNode getParent(CNode node) {
			CNode parent= node.getParent();
			if (parent == null) {
				throw new NodeConstructorError();
			}
			return parent;
		}
		
		private final int fixLength(int startPos, int endPos) {
			if (endPos < startPos) {
				return 0;
			} else {
				return endPos - startPos + 1;
			}
		}
		
		
		public void includeDecl(String name, int startPos, int endPos, int startLine, int endLine) {
			CNode node= new CNode(fRoot, ICElement.C_INCLUDE, name, fDocument, startPos, fixLength(startPos, endPos));
			fRoot.addChild(node);
		}
		
		public void defineDecl(String name, int startPos, int endPos, int startLine, int endLine) {
			CNode node= new CNode(fRoot, ICElement.C_MACRO, name, fDocument, startPos, fixLength(startPos, endPos));
			fRoot.addChild(node);			
		}
		
		public void functionDeclBegin(String name, int nameStartPos, int nameEndPos,
			int declStartPos, int startLine, int type, int modifiers) {
			CNode node= new CNode(fCurrent, ICElement.C_FUNCTION, name, fDocument, declStartPos, 0);
			fCurrent.addChild(node);
			fCurrent= node;
		}
		
		public void functionDeclEnd(int declEndPos, int endLine, boolean prototype) {
			if(prototype) {
				fCurrent.setTypeCode(ICElement.C_FUNCTION_DECLARATION);
			}
			Position p= fCurrent.getRange();
			fCurrent.setLength(fixLength(p.getOffset(), declEndPos));
			fCurrent= getParent(fCurrent);
		}
		
		public void fieldDecl(String name, int nameStartPos, int nameEndPos, int declStartPos,
			int declEndPos, int startLine, int endLine, int modifiers) {
			CNode node= new CNode(fCurrent, ICElement.C_FIELD, name, fDocument, declStartPos, fixLength(declStartPos, declEndPos));
			fCurrent.addChild(node);
		}
		
		public void structDeclBegin(String name, int kind, int nameStartPos, int nameEndPos,
			int declStartPos, int startLine, int modifiers) {
			CNode node= new CNode(fCurrent, kind, name, fDocument, declStartPos, 0);
			fCurrent.addChild(node);
			fCurrent= node;
		}
		
		public void structDeclEnd(int declEndPos, int endLine) {
			Position p= fCurrent.getRange();
			fCurrent.setLength(fixLength(p.getOffset(), declEndPos));
			fCurrent= getParent(fCurrent);			
		}
		
		public void superDecl(String name) {
		}
		
		public void reportError(Throwable throwable) {
			throw new NodeConstructorError();
		}
	}

	/**
	 * Returns null if an error occurred.
	 */
	private static String readString(InputStream is) {
		if (is == null)
			return null;
		BufferedReader reader= null;
		try {
			StringBuffer buffer= new StringBuffer();
			char[] part= new char[2048];
			int read= 0;
			reader= new BufferedReader(new InputStreamReader(is));

			while ((read= reader.read(part)) != -1)
				buffer.append(part, 0, read);
			
			return buffer.toString();
			
		} catch (IOException ex) {
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ex) {
				}
			}
		}
		return null;
	}
	
}
