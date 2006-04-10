/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.dstore.universal.miners.filesystem;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.rse.dstore.universal.miners.IUniversalDataStoreConstants;
import org.eclipse.rse.services.clientserver.IServiceConstants;
import org.eclipse.rse.services.clientserver.archiveutils.AbsoluteVirtualPath;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.clientserver.archiveutils.VirtualChild;
import org.eclipse.rse.services.clientserver.java.BasicClassFileParser;


/*
 * This utility class is for determing file types
 */
public class FileClassifier extends Thread
{
    protected class Pair
    {
        private String _fileName;

        private String _canonicalName;

        private DataElement _element;

        public Pair(String fileName, DataElement element)
        {
            _fileName = fileName;
            _element = element;
        }

        public DataElement getElement()
        {
            return _element;
        }

        public String getFileName()
        {
            return _fileName;
        }

        public String getCanonicalName()
        {
            return _canonicalName;
        }

        public void setCanonicalName(String canonicalName)
        {
            _canonicalName = canonicalName;
        }
    }

    public static final String symbolicLinkStr = "symbolic link to";
    
    public static final String fileSep = System.getProperty("file.separator");
    public static final String defaultType = "file";
    
    public static final String STR_SYMBOLIC_LINK = "symbolic link";
    public static final String STR_SHARED_OBJECT="shared object";
    public static final String STR_OBJECT_MODULE="object module";
    public static final String STR_MODULE="module";
    public static final String STR_ARCHIVE="archive";
    public static final String STR_EXECUTABLE="executable";
    public static final String STR_SCRIPT="script";
    public static final String STR_EXECUTABLE_SCRIPT="executable(script)";
    public static final String STR_EXECUTABLE_BINARY="executable(binary)";
    public static final String STR_DOT_A=".a";
    public static final String STR_DOT_SO=".so";
    public static final String STR_DOT_SO_DOT=".so.";
    public static final String STR_DIRECTORY="diectory";

    
    private DataElement _subject;

    private DataStore _dataStore;

    private String _specialEncoding = null;

    protected String _systemShell = null;

    private List _fileMap;

    private boolean _classifyChildren = true;

    private boolean _canResolveLinks = false;

    private boolean _classifyVirtual = false;

    private boolean _systemSupportsClassify = true;
    
    private boolean _systemSupportsClassFilesOnly = false;
    
    private List _lines;

    public FileClassifier(DataElement subject)
    {
        _lines = new ArrayList();
        // special encoding passed in when starting server
        _specialEncoding = System.getProperty("dstore.stdin.encoding");

        _subject = subject;
        _dataStore = subject.getDataStore();
        _fileMap = new ArrayList();

        // we can resolve links on Linux
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.startsWith("win"))
        {
            _systemSupportsClassify = false;
        }
        else if (osName.equals("z/OS")) {
        	_systemSupportsClassFilesOnly = true;
        }
        
        _systemShell = "sh";
        _canResolveLinks = osName.startsWith("linux");

        init();
    }

    private void init()
    {
        String objType = _subject.getType();

        // determine if we are classifying virtual files
        // we are if the subject is an archive, virtual folder or a virtual file
        if (objType.equals(IUniversalDataStoreConstants.UNIVERSAL_ARCHIVE_FILE_DESCRIPTOR) || objType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FOLDER_DESCRIPTOR)
                || objType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR))
        {
            _classifyVirtual = true;
        }
        else
        {
            _classifyVirtual = false;
        }

        // if the subject is a file or a virtual file (i.e. not a directory or
        // an archive), then
        // we do not classify children (since there are no children)
        if (objType.equals(IUniversalDataStoreConstants.UNIVERSAL_FILE_DESCRIPTOR) || objType.equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR))
        {
            _classifyChildren = false;
        }
        // otherwise, put children in hashmap
        else
        {
            // store all datalements in hashmap
            for (int i = 0; i < _subject.getNestedSize(); i++)
            {
                DataElement child = _subject.get(i);

                if (child.getType().equals(IUniversalDataStoreConstants.UNIVERSAL_FILE_DESCRIPTOR)
                        || child.getType().equals(IUniversalDataStoreConstants.UNIVERSAL_VIRTUAL_FILE_DESCRIPTOR)
                        || child.getType().equals(IUniversalDataStoreConstants.UNIVERSAL_FOLDER_DESCRIPTOR))
                {
                    String name = child.getName();
                    String properties = child.getSource();

                    // if this file has already been classified
                    // ignore it
                    String[] tokens = properties.split("\\" + IServiceConstants.TOKEN_SEPARATOR);

                    if (tokens.length < 12)
                    {
                        putElement(name, child);
                    }
                    /*
                     * StringTokenizer tokenizer = new
                     * StringTokenizer(properties,
                     * IUniversalDataStoreConstants.TOKEN_SEPARATOR); if
                     * (tokenizer.countTokens() < 12) { putElement(name, child); }
                     */
                }
            }

            _classifyChildren = true;
        }
    }

    private void putElement(String name, DataElement child)
    {
        _fileMap.add(new Pair(name, child));
    }

    /**
     * Gets the data element for the given name from the name to data element
     * mapping.
     * 
     * @param name
     *            the name.
     * @return the data element corresponding to that name.
     */
    private List getElementsFor(String fileName)
    {
        boolean matchedCanonical = false;
        ArrayList results = new ArrayList();
        for (int i = 0; i < _fileMap.size(); i++)
        {
            Pair apair = (Pair) _fileMap.get(i);
            String canonicalName = apair.getCanonicalName();
            if (canonicalName != null)
            {
                if (canonicalName.equals(fileName))
                {

                    if (!matchedCanonical)
                    {
                        results.clear();
                        matchedCanonical = true;
                    }
                    results.add(apair);
                }
            }
            else if (apair.getFileName().equals(fileName) && !matchedCanonical)
            {
                results.add(apair);
            }
        }
        return results;
    }

    public void run()
    {
        if (!_systemSupportsClassify)
            return;

        // get full path
    	StringBuffer fPathBuf = new StringBuffer(_subject.getValue());
    	fPathBuf.append(File.separatorChar);
    	fPathBuf.append(_subject.getName());
        String filePath = fPathBuf.toString();
        
        // if we have to classify children
        if (_classifyChildren)
        {
      

            // if it's not a classification of virtual files
            if (!_classifyVirtual)
            {
                File parentFile = new File(filePath);

                // if parent file is a directory, classify all its children, and
                // do not
                // resolve links by default
                if (parentFile.isDirectory() && parentFile.list().length > 0)
                {
                    classifyChildren(parentFile, "*", false);
                }
            }
            else
            {
                classifyVirtualChildren(filePath);
            }

            _dataStore.refresh(_subject);
        }
        else
        {
            File theFile = new File(filePath);

            if (theFile.exists())
            {
                try
                {
                    String type = classifyFile(theFile.getCanonicalFile());
                    StringBuffer classifiedProperties = new StringBuffer(_subject.getSource());
                    classifiedProperties.append('(');
                    classifiedProperties.append(type);
                    classifiedProperties.append(')');
                    _subject.setAttribute(DE.A_SOURCE, classifiedProperties.toString());
                    _dataStore.refresh(_subject);
                }
                catch (Exception e)
                {
                }
            }
        }
    }

    /**
     * Classifies from the given line of classification output.
     * 
     * @param parentFile
     *            the parent file.
     * @param line
     *            the line of output to parse.
     * @param specialEncoding
     *            a special encoding, if there is one.
     * @param resolveLink
     *            resolve link.
     * @return the classification.
     */
    protected String classify(File parentFile, String line, String specialEncoding, boolean resolveLink)
    {
        // this string should be contained in an output line that indicates a
        // symbolic link
     

        // default type
        String type = defaultType;

        int colon = line.indexOf(':');

        // name appears before colon
        String name = line.substring(0, colon);

        // the full type appears after the colon
        String fulltype = line.substring(colon + 1, line.length()).trim();

        // if it's a *.class file, then we look for main method and qulaified
        // class name
        // as part of the classification
        if (name.endsWith(".class"))
        {
            // get parent path
            String parentPath = parentFile.getAbsolutePath();

            // get file separator
           

            // if parent path does not end with separator, then add it
            if (!parentPath.endsWith(fileSep))
            {
                parentPath = parentPath + fileSep;
            }

            // add name to parent path to get file path
            String filePath = parentPath + name;

            // input stream to file
            FileInputStream stream = null;

            // class file parser
            BasicClassFileParser parser = null;

            boolean isExecutable = false;

            try
            {
                stream = new FileInputStream(filePath);

                // use class file parser to parse the class file
                parser = new BasicClassFileParser(stream);
                parser.parse();

                // query if it is executable, i.e. whether it has main method
                isExecutable = parser.isExecutable();
            }
            catch (IOException e)
            {
                // TODO: log it

                // we assume not executable
                isExecutable = false;
            }

            // if it is executable, then also get qualified class name
            if (isExecutable)
            {
                type = "executable(java";

                String qualifiedClassName = parser.getQualifiedClassName();

                if (qualifiedClassName != null)
                {
                    type = type + ":" + qualifiedClassName;
                }

                type = type + ")";
            }
            return type;
        }
        
        // if the system supports only classifying *.class files, then return generic type "file".
        if (_systemSupportsClassFilesOnly) {
        	return type;
        }

        boolean matchesLib = (fulltype.indexOf(STR_SHARED_OBJECT) > -1) || (fulltype.indexOf(STR_OBJECT_MODULE) > -1) || (fulltype.indexOf(STR_ARCHIVE) > -1);

        boolean matchesExe = (fulltype.indexOf(STR_EXECUTABLE) > -1);
        boolean matchesScript = (fulltype.indexOf(STR_SCRIPT) > -1);
        // shared
        if (matchesLib && (name.endsWith(STR_DOT_A) || name.endsWith(STR_DOT_SO) || name.indexOf(STR_DOT_SO_DOT) > 0))
        {
            type = STR_MODULE;
        }
        else if (matchesScript)
        {
            if (matchesExe)
            {
                type = STR_EXECUTABLE_SCRIPT;
            }
            else
            {
                type = STR_SCRIPT;
            }
        }
        // if the fullType contains "executable", then it is either a script
        // executable or a binary
        // executable
        else if (matchesExe)
        {
            type = STR_EXECUTABLE_BINARY;
        }
        else if (fulltype.indexOf(STR_DIRECTORY) > -1)
        {
            type = STR_DIRECTORY;
        }
        // finally, if the full type contains the symbolic link string, then we
        // know
        // we have a symbolic link
        else if (fulltype.startsWith(symbolicLinkStr))
        {
            type = resolveSymbolicLink(parentFile, name, fulltype, symbolicLinkStr, resolveLink, specialEncoding);
        }

        return type;
    }

    protected String resolveSymbolicLink(File parentFile, String originalName, String fulltype, String symbolicLinkStr, boolean resolveLink, String specialEncoding)
    {
        // type is "link"
        StringBuffer type = new StringBuffer(STR_SYMBOLIC_LINK);

        // find the target (i.e. referenced) file
        String referencedFile = fulltype.substring(symbolicLinkStr.length()).trim();
        File refFile = new File(referencedFile);
        if (refFile.isDirectory())
        {
        	type.append("(directory)");
            return type.toString();
        }

        try
        {
            // if we are supposed to resolve link, then do so by running "sh -c
            // file <filename>"
            // with <filename> being the reference file name
            if (resolveLink)
            {

                String args[] = new String[3];
                args[0] = _systemShell;
                args[1] = "-c";
                args[2] = "file " + referencedFile;

                Process childProcess = Runtime.getRuntime().exec(args, null, parentFile);
                BufferedReader childReader = null;

                if (specialEncoding != null && specialEncoding.length() > 0)
                {
                    childReader = new BufferedReader(new InputStreamReader(childProcess.getInputStream(), specialEncoding));
                }
                else
                {
                    childReader = new BufferedReader(new InputStreamReader(childProcess.getInputStream()));
                }

                String childLine = childReader.readLine().trim();

                type.append('(');
                type.append(classify(parentFile, childLine, specialEncoding, resolveLink));
                type.append(')');

                childReader.close();

            }
        }
        catch (Exception e)
        {
        }
        return type.toString();
    }

    /**
     * Classify a file. It classifies the file by running "sh -c file
     * <filename>".
     * 
     * @param aFile
     *            the file to classify.
     * @return the classification.
     */
    public String classifyFile(File aFile)
    {
        String type = defaultType;

        try
        {
            String referencedFile = aFile.getCanonicalPath();

            String specialEncoding = System.getProperty("dstore.stdin.encoding");
            /*
            if (specialEncoding == null)
            {
                specialEncoding = System.getProperty("file.encoding");
            }
*/
            specialEncoding = null;
            String args[] = new String[3];
            args[0] = "sh";
            args[1] = "-c";
            args[2] = "file " + referencedFile;

            Process childProcess = Runtime.getRuntime().exec(args);

            BufferedReader childReader = null;

            if (specialEncoding != null && specialEncoding.length() > 0)
            {
                childReader = new BufferedReader(new InputStreamReader(childProcess.getInputStream(), specialEncoding));
            }
            else
            {
                childReader = new BufferedReader(new InputStreamReader(childProcess.getInputStream()));
            }

            String childLine = null;
            try
            {
                childLine = childReader.readLine().trim();
            }
            catch (Exception e)
            {

            }

            type = classify(aFile.getParentFile(), childLine, specialEncoding, true);

            childReader.close();
        }
        catch (Exception e)
        {
        }

        return type;
    }

    protected List readLines(DataInputStream stream, String encoding) throws Exception
    {

        if (encoding == null)
        {
            encoding = System.getProperty("file.encoding");
        }
        
        
        try
        {

            // find out how many bytes are available to be read
            int available = stream.available();
            while (available > -1)
            {
	            // if there's none, wait a bit and return true to continue
	            if (available == 0)
	            {
	                sleep(100);
	                available = stream.available();
	                if (available == 0)
	                    return _lines;
	            }
	
	            byte[] readBytes = new byte[available];
	
	            // read the available bytes
	            int numRead = stream.read(readBytes, 0, available);
	
	            // if we've reached end of stream, quit
	            if (numRead == -1)
	            {
	                return null;
	            }
	
	
	            // get the output using the encoding
	            try
	            {
	           
	                String fullOutput = new String(readBytes, 0, numRead, encoding);
	
	                // if output is not null, we assume the encoding was correct and
	                // process the output
	                if (fullOutput != null /* && fullOutput.length() == numRead */)
	                {
	                    // tokenize the output so that we can get each line of
	                    // output
	                    // the delimiters are therefore set to "\n\r"
	                    String[] tokens = fullOutput.split("\n");
	                    if (tokens.length > 0)
	                    {
	                        if (_lines.size() > 0)
	                        {
	                            String lastLine = (String)_lines.remove(_lines.size() -1);
	                            tokens[0] = lastLine + tokens[0];
	                        }
	                        
	                        for (int i = 0; i< tokens.length; i++)
	                    	{
	                        _lines.add(tokens[i]);
	                    	}
	                    }
	                }
	            }
	            catch (Exception e)
	            {
	            }
	            available = stream.available();
            }
       
        }
        catch (Exception e)
        {

        }
       
        return _lines;

    }

    protected String readLine(DataInputStream stream, String encoding) throws Exception
    {
        if (_lines.size() == 0)
        {
           _lines = readLines(stream, encoding); 
        }
        if (_lines == null)
        {
            return null;
        }
        if (_lines.size() > 0)
        {
        	return (String)_lines.remove(0);
        }
        return null;
    }

    /**
     * Classifies the children of a given file.
     * 
     * @param parentFile
     *            the parent file.
     * @param files
     *            the files to classify. Specify "*" to classify all files.
     * @param resolveLinks
     *            resolve links if possible.
     */
    protected void classifyChildren(File parentFile, String files, boolean resolveLinks)
    {

        try
        {
            boolean hasLinks = false;

            String[] args = new String[3];
            args[0] = "sh";

            args[1] = "-c";

            // if we are asked to resolve children, and it is possible to do so
            // then use "file -L". This is slower than if we run without the
            // "-L".
            if (resolveLinks && _canResolveLinks)
            {
                args[2] = "file -L " + files;
            }
            // otherwise, don't use "-L"
            else
            {
                args[2] = "file " + files;

            }

            // run command with the working directory being the parent file
            Process theProcess = Runtime.getRuntime().exec(args, null, parentFile);

            BufferedReader reader = null;
            DataInputStream stream = null;

            if (_specialEncoding != null)
            {
                stream = new DataInputStream(theProcess.getInputStream());
                reader = new BufferedReader(new InputStreamReader(stream, _specialEncoding));
            }
            else
            {
                stream = new DataInputStream(theProcess.getInputStream());
            }

            // a list of files to be queried after
            List deferredQueries = new ArrayList();

            String line = null;
            try
            {
                if (reader != null)
                    line = reader.readLine();
                else
                    line = readLine(stream, _specialEncoding);//reader.readLine();
            }
            catch (Exception e)
            {
            }

            // read each line of output
            while (line != null)
            {
                if (line.length() > 0)
                {
                    line = line.trim();
                    if (line.indexOf("cannot open ") > 0)
                    {

                    }
                    else
                    {
                        int colon = line.indexOf(':');

                        // sometimes we get two lines of output for each file
                        // so ignore second line
                        if (colon != -1)
                        {

                            // name of classified file
                            String name = line.substring(0, colon);

                            // get classification from line, and do not resolve
                            // link
                            String type = classify(parentFile, line, _specialEncoding, false);

                            // get data elements- may be more than one (in case
                            // of link)
                            List pairs = getElementsFor(name);
                            for (int d = 0; d < pairs.size(); d++)
                            {
                                Pair pair = (Pair) pairs.get(d);

                                DataElement element = pair.getElement();

                                // if the element was in our list of mappings
                                if (element != null)
                                {

                                    // referencing file
                                	StringBuffer path = new StringBuffer(element.getValue());
                                	path.append(File.separatorChar);
                                	path.append(element.getName());
                                    File refFile = new File(path.toString());

                                    // canonical file path

                                    String canonicalPath = null;

                                    // if the file is a link resolve it
                                    // if it's a link and we're already trying
                                    // to resolve links, ignore it
                                    if (type.equals(STR_SYMBOLIC_LINK) && !resolveLinks)
                                    {

                                        // get canonical file path
                                        canonicalPath = refFile.getCanonicalPath();

                                        // if we can't resolve links, then get
                                        // the canonical file using Java
                                        if (!_canResolveLinks)
                                        {
                                            // get canonical file
                                            File canFile = refFile.getCanonicalFile();

                                            // put canonical file in our list of
                                            // mappings with
                                            // the current element (so the
                                            // canonical file name
                                            // corresponds to this element
                                            // subsequently)
                                            pair.setCanonicalName(canonicalPath);

                                            // get parent of canonical file
                                            File cP = canFile.getParentFile();

                                            // add canonical file parent to
                                            // deferred query list
                                            if (!deferredQueries.contains(cP))
                                            {
                                                deferredQueries.add(cP);
                                            }
                                        }

                                        // we can resolve links, so indicate it
                                        // with a flag
                                        hasLinks = true;
                                    }
                                    else
                                    {
                                        // remove from list
                                        _fileMap.remove(pair);

                                        if (type.equals(STR_SYMBOLIC_LINK))
                                        {
                                            canonicalPath = refFile.getCanonicalPath();
                                        }
                                    }

                                    // set current properties
                                    StringBuffer currentProperties = new StringBuffer(element.getAttribute(DE.A_SOURCE));

                                    // determine text to write
                                    StringBuffer textToWrite = new StringBuffer(type);

                                    // if type is "link", we write a string of
                                    // form "link:canonicalPath"
                                    if (type.equals(STR_SYMBOLIC_LINK))
                                    {
                                        if (type.indexOf(":") == -1)
                                        {
                                        	textToWrite.append(':');
                                        	textToWrite.append(canonicalPath);
                                        }
                                    }

                                    // determine text to check for
                                    String textToCheck = STR_SYMBOLIC_LINK;

                                    int linkIndex = currentProperties.lastIndexOf(textToCheck);

                                    if (linkIndex != -1)
                                    {
                                        int cutOffIndex = linkIndex + textToCheck.length();
                                        
                                        StringBuffer typeBuf = new StringBuffer();
                                        typeBuf.append('(');
                                        typeBuf.append(type);
                                        typeBuf.append(')');
                                        currentProperties.insert(cutOffIndex, typeBuf.toString());
                                        element.setAttribute(DE.A_SOURCE, currentProperties.toString());
                                    }
                                    else
                                    {
                                    	currentProperties.append('|');
                                    	currentProperties.append(textToWrite);
                                        element.setAttribute(DE.A_SOURCE, currentProperties.toString());
                                    }
                                }
                            }
                        }
                    }
                }

                try
                {
                    if (reader != null)
                        line = reader.readLine();
                    else
                        line = readLine(stream, _specialEncoding);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            if (reader != null)
                reader.close();
            else
                stream.close();
            // we have found links
            if (hasLinks)
            {
                // if we were told not to resolve them, but we are capable of
                // doing so, then
                // let's try again
                if (!resolveLinks && _canResolveLinks)
                {
                    // we pass true to indicate we want to resolve links this
                    // time
                    classifyChildren(parentFile, "*", true);
                }
                // otherwise, run deferred queries on parents of target files
                // and try to resolve link
                else
                {
                    // run deferred queries on canonical file parents
                    for (int i = 0; i < deferredQueries.size(); i++)
                    {
                        File aFile = (File) deferredQueries.get(i);

                        // we pass true to indicate we want to resolve links
                        // this time
                        StringBuffer newPathBuf = new StringBuffer(aFile.getAbsolutePath());
                        newPathBuf.append(File.separatorChar);
                        newPathBuf.append('*');
                        classifyChildren(parentFile, newPathBuf.toString(), true);
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    /**
     * Classify virtual children.
     * 
     * @param fullPath
     *            the full path of the parent file. The path could represent an
     *            archive or a virtual folder.
     */
    protected void classifyVirtualChildren(String parentPath)
    {
        ArchiveHandlerManager mgr = ArchiveHandlerManager.getInstance();

        // clean up path
        // DKM - this strips leading / so I'm putting it back
        parentPath = '/' + ArchiveHandlerManager.cleanUpVirtualPath(parentPath);

        // check if parent is an archive
        boolean isArchive = mgr.isRegisteredArchive(parentPath);

        // check if parent is virtual
        boolean isVirtual = ArchiveHandlerManager.isVirtual(parentPath);

        VirtualChild[] children = null;

        try
        {

            File archiveFile = null;
            String virtualPath = null;

            // if the parent is an archive and not a virtual
            if (isArchive && !isVirtual)
            {

                // archive file is simply the path passed in
                archiveFile = new File(parentPath);

                // virtual path is "" to indicate we want the top level entries
                // in the archive
                virtualPath = "";
            }
            // otherwise, if the parent is a virtual folder
            else
            {
                AbsoluteVirtualPath avp = new AbsoluteVirtualPath(parentPath);

                // get the path of the containing archive
                String archivePath = avp.getContainingArchiveString();

                // get the virtual part of the file path
                virtualPath = avp.getVirtualPart();

                // get archive file
                archiveFile = new File(archivePath);
            }

            // get the contents of the virtual path within the archive
            children = mgr.getContents(archiveFile, virtualPath);

            // go through each virtual child, and set the classification type
            for (int i = 0; i < children.length; i++)
            {
                VirtualChild child = children[i];

                String type = defaultType;

                // only classify if the virtual child is not a directory
                if (!child.isDirectory)
                {

                    // name of virtual child
                    String name = child.name;

                    // get classification
                    type = mgr.getClassification(archiveFile, child.fullName);

                    // get data element
                    List matches = getElementsFor(name);
                    for (int c = 0; c < matches.size(); c++)
                    {
                        Pair pair = (Pair) matches.get(c);
                        DataElement element = pair.getElement();

                        // if element is in our list of mappings
                        if (element != null)
                        {

                            // remove element from list of mappings
                            _fileMap.remove(pair);

                            // add type to current properties
                            StringBuffer currentProperties = new StringBuffer(element.getAttribute(DE.A_SOURCE));
                            currentProperties.append('|');
                            currentProperties.append(type);
                            
                            element.setAttribute(DE.A_SOURCE, currentProperties.toString());
                        }
                    }
                }
            }
        }
        catch (IOException e)
        {
            // TODO: log error
        }
    }
}