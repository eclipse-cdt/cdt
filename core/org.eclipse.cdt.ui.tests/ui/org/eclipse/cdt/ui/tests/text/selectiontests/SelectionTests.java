package org.eclipse.cdt.ui.tests.text.selectiontests;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.search.DOMSearchUtil;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.FileManager;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.index.sourceindexer.SourceIndexer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;

public class SelectionTests extends TestCase {
    
    static NullProgressMonitor      monitor;
    static IWorkspace               workspace;
    static IProject                 project;
    static FileManager              fileManager;
    static boolean                  disabledHelpContributions = false;
    {
        //(CCorePlugin.getDefault().getCoreModel().getIndexManager()).reset();
        monitor = new NullProgressMonitor();
        
        workspace = ResourcesPlugin.getWorkspace();
        
        ICProject cPrj; 
        try {
            cPrj = CProjectHelper.createCCProject("SelectionTestProject", "bin"); //$NON-NLS-1$ //$NON-NLS-2$
        
            project = cPrj.getProject();
            project.setSessionProperty(SourceIndexer.activationKey,new Boolean(false));
        } catch ( CoreException e ) {
            /*boo*/
        }
        if (project == null)
            fail("Unable to create project"); //$NON-NLS-1$

        //Create file manager
        fileManager = new FileManager();
    }
    public SelectionTests()
    {
        super();
    }
    /**
     * @param name
     */
    public SelectionTests(String name)
    {
        super(name);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite( SelectionTests.class );
        suite.addTest( new SelectionTests("cleanupProject") );    //$NON-NLS-1$
        return suite;
    }
    
    public void cleanupProject() throws Exception {
        try{
            project.delete( true, false, monitor );
            project = null;
        } catch( Throwable e ){
            /*boo*/
        }
    }
    
    protected void tearDown() throws Exception {
        if( project == null || !project.exists() ) 
            return;
        
        IResource [] members = project.members();
        for( int i = 0; i < members.length; i++ ){
            if( members[i].getName().equals( ".project" ) || members[i].getName().equals( ".cdtproject" ) ) //$NON-NLS-1$ //$NON-NLS-2$
                continue;
            try{
                members[i].delete( false, monitor );
            } catch( Throwable e ){
                /*boo*/
            }
        }
    }
    
    protected IFile importFile(String fileName, String contents ) throws Exception{
        //Obtain file handle
        IFile file = project.getProject().getFile(fileName);
        
        InputStream stream = new ByteArrayInputStream( contents.getBytes() ); 
        //Create file input stream
        if( file.exists() )
            file.setContents( stream, false, false, monitor );
        else
            file.create( stream, false, monitor );
        
        fileManager.addFile(file);
        
        return file;
    }
    
    protected IASTNode testF3(IFile file, int offset) {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IEditorPart part = null;
        try {
            part = page.openEditor(new FileEditorInput(file), "org.eclipse.cdt.ui.editor.CEditor"); //$NON-NLS-1$
        } catch (PartInitException e) {
            assertFalse(true);
        }
        
        if (part instanceof AbstractTextEditor) {
            ((AbstractTextEditor)part).getSelectionProvider().setSelection(new TextSelection(offset,0));
            
            final IAction action = ((AbstractTextEditor)part).getAction("OpenDeclarations"); //$NON-NLS-1$
            action.run();
        
            // the action above should highlight the declaration, so now retrieve it and use that selection to get the IASTName selected on the TU
            ISelection sel = ((AbstractTextEditor)part).getSelectionProvider().getSelection();
            
            if (sel instanceof TextSelection) {
                IASTName[] names = DOMSearchUtil.getSelectedNamesFrom(file, ((TextSelection)sel).getOffset(), ((TextSelection)sel).getLength());
                
                if (names.length == 0) {
                    assertFalse(true);
                } else {
                    return names[0];
                }
            }
        }
        
        return null;
    }
    
    protected IASTNode testF2(IFile file, int offset) {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IEditorPart part = null;
        try {
            part = page.openEditor(new FileEditorInput(file), "org.eclipse.cdt.ui.editor.CEditor"); //$NON-NLS-1$
        } catch (PartInitException e) {
            assertFalse(true);
        }
        
        if (part instanceof AbstractTextEditor) {
            ((AbstractTextEditor)part).getSelectionProvider().setSelection(new TextSelection(offset,0));
            
            final IAction action = ((AbstractTextEditor)part).getAction("OpenDefinition"); //$NON-NLS-1$
            action.run();
        
            // the action above should highlight the declaration, so now retrieve it and use that selection to get the IASTName selected on the TU
            ISelection sel = ((AbstractTextEditor)part).getSelectionProvider().getSelection();
            
            if (sel instanceof TextSelection) {
                IASTName[] names = DOMSearchUtil.getSelectedNamesFrom(file, ((TextSelection)sel).getOffset(), ((TextSelection)sel).getLength());
                
                if (names.length == 0) {
                    assertFalse(true);
                } else {
                    return names[0];
                }
            }
        }
        
        return null;
    }
    
    public void testBug93281() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("class Point{                         \n"); //$NON-NLS-1$
        buffer.append("public:                              \n"); //$NON-NLS-1$
        buffer.append("Point(): xCoord(0){}                 \n"); //$NON-NLS-1$
        buffer.append("Point& operator=(const Point &rhs){return *this;}    // line A\n"); //$NON-NLS-1$
        buffer.append("void* operator new [ ] (unsigned int);\n"); //$NON-NLS-1$
        buffer.append("private:                             \n"); //$NON-NLS-1$
        buffer.append("int xCoord;                          \n"); //$NON-NLS-1$
        buffer.append("};                                   \n"); //$NON-NLS-1$
        buffer.append("static const Point zero;\n"); //$NON-NLS-1$
        buffer.append("int main(int argc, char **argv) {        \n"); //$NON-NLS-1$
        buffer.append("Point *p2 = new Point();         \n"); //$NON-NLS-1$
        buffer.append("p2->    operator // /* operator */ // F3 in the middle \n"); //$NON-NLS-1$
        buffer.append("//of \"operator\" should work\n"); //$NON-NLS-1$
        buffer.append("// \\n"); //$NON-NLS-1$
        buffer.append("/* */\n"); //$NON-NLS-1$
        buffer.append("=(zero);           // line B\n"); //$NON-NLS-1$
        buffer.append("p2->operator /* oh yeah */ new // F3 in the middle of \"operator\"\n"); //$NON-NLS-1$
        buffer.append("// should work\n"); //$NON-NLS-1$
        buffer.append("//\n"); //$NON-NLS-1$
        buffer.append("[ /* sweet */ ] //\n"); //$NON-NLS-1$
        buffer.append("(2);\n"); //$NON-NLS-1$
        buffer.append("return (0);                          \n"); //$NON-NLS-1$
        buffer.append("}\n"); //$NON-NLS-1$
        
        String code = buffer.toString();
        IFile file = importFile("test93281.cpp", code); //$NON-NLS-1$
        
        int offset = code.indexOf("p2->operator") + 6; //$NON-NLS-1$
        IASTNode node = testF3(file, offset);
        
        assertTrue(node instanceof IASTName);
        assertEquals(((IASTName)node).toString(), "operator new[]"); //$NON-NLS-1$
        assertEquals(((ASTNode)node).getOffset(), 183);
        assertEquals(((ASTNode)node).getLength(), 16);
        
        offset = code.indexOf("p2->    operator") + 11; //$NON-NLS-1$
        node = testF3(file, offset);
        
        assertTrue(node instanceof IASTName);
        assertEquals(((IASTName)node).toString(), "operator ="); //$NON-NLS-1$
        assertEquals(((ASTNode)node).getOffset(), 121);
        assertEquals(((ASTNode)node).getLength(), 9);
        
    }
        
    public void testBasicDefinition() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("extern int MyInt;       // MyInt is in another file\n"); //$NON-NLS-1$
        buffer.append("extern const int MyConst;   // MyConst is in another file\n"); //$NON-NLS-1$
        buffer.append("void MyFunc(int);       // often used in header files\n"); //$NON-NLS-1$
        buffer.append("struct MyStruct;        // often used in header files\n"); //$NON-NLS-1$
        buffer.append("typedef int NewInt;     // a normal typedef statement\n"); //$NON-NLS-1$
        buffer.append("class MyClass;          // often used in header files\n"); //$NON-NLS-1$
        buffer.append("int MyInt;\n"); //$NON-NLS-1$
        buffer.append("extern const int MyConst = 42;\n"); //$NON-NLS-1$
        buffer.append("void MyFunc(int a) { cout << a << endl; }\n"); //$NON-NLS-1$
        buffer.append("struct MyStruct { int Member1; int Member2; };\n"); //$NON-NLS-1$
        buffer.append("class MyClass { int MemberVar; };\n"); //$NON-NLS-1$
        
        String code = buffer.toString();
        IFile file = importFile("testBasicDefinition.cpp", code); //$NON-NLS-1$
        
        int offset = code.indexOf("MyInt;\n") + 2; //$NON-NLS-1$
        IASTNode def = testF2(file, offset);
        IASTNode decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "MyInt"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 11);
        assertEquals(((ASTNode)decl).getLength(), 5);
        assertEquals(((IASTName)def).toString(), "MyInt"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 330);
        assertEquals(((ASTNode)def).getLength(), 5);
        
        offset = code.indexOf("MyConst = 42") + 2; //$NON-NLS-1$
        def = testF2(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "MyConst"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 69);
        assertEquals(((ASTNode)decl).getLength(), 7);
        assertEquals(((IASTName)def).toString(), "MyConst"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 354);
        assertEquals(((ASTNode)def).getLength(), 7);
        
        offset = code.indexOf("MyFunc(int a)") + 2; //$NON-NLS-1$
        def = testF2(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "MyFunc"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 115);
        assertEquals(((ASTNode)decl).getLength(), 6);
        assertEquals(((IASTName)def).toString(), "MyFunc"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 373);
        assertEquals(((ASTNode)def).getLength(), 6);
        
        offset = code.indexOf("MyStruct {") + 2; //$NON-NLS-1$
        def = testF2(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "MyStruct"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 171);
        assertEquals(((ASTNode)decl).getLength(), 8);
        assertEquals(((IASTName)def).toString(), "MyStruct"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 417);
        assertEquals(((ASTNode)def).getLength(), 8);
        
        offset = code.indexOf("MyClass {") + 2; //$NON-NLS-1$
        def = testF2(file, offset);
        decl = testF3(file, offset);
        assertTrue(def instanceof IASTName);
        assertTrue(decl instanceof IASTName);
        assertEquals(((IASTName)decl).toString(), "MyClass"); //$NON-NLS-1$
        assertEquals(((ASTNode)decl).getOffset(), 278);
        assertEquals(((ASTNode)decl).getLength(), 7);
        assertEquals(((IASTName)def).toString(), "MyClass"); //$NON-NLS-1$
        assertEquals(((ASTNode)def).getOffset(), 463);
        assertEquals(((ASTNode)def).getLength(), 7);
    }
    
}