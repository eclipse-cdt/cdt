/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Martin Oberhuber (Wind River) - initial API and implementation
 * David Dykstal (IBM) - fixing bug 162510: correctly process filter strings
 * Kushal Munir (IBM) - for API bug   
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Martin Oberhuber (Wind River) - [192724] Fixed logic to filter folders if FILE_TYPE_FOLDERS
 * Martin Oberhuber (Wind River) - [199548] Avoid touching files on setReadOnly() if unnecessary
 * Benjamin Muskalla (b.muskalla@gmx.net) - [174690][ssh] cannot delete symbolic links on remote systems
 * Martin Oberhuber (Wind River) - [203490] Fix NPE in SftpService.getUserHome()
 * Martin Oberhuber (Wind River) - [203500] Support encodings for SSH Sftp paths
 * David McKnight   (IBM)        - [207178] changing list APIs for file service and subsystems
 * Martin Oberhuber (Wind River) - [208912] Cannot expand /C on a VxWorks SSH Server
 * David McKnight   (IBM)        - [210109] store constants in IFileService rather than IFileServiceConstants
 *******************************************************************************/

package org.eclipse.rse.internal.services.ssh.files;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.NLS;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;

import org.eclipse.rse.internal.services.ssh.Activator;
import org.eclipse.rse.internal.services.ssh.ISshService;
import org.eclipse.rse.internal.services.ssh.ISshSessionProvider;
import org.eclipse.rse.internal.services.ssh.SshServiceResources;
import org.eclipse.rse.services.Mutex;
import org.eclipse.rse.services.clientserver.FileTypeMatcher;
import org.eclipse.rse.services.clientserver.IMatcher;
import org.eclipse.rse.services.clientserver.NamePatternMatcher;
import org.eclipse.rse.services.clientserver.PathUtility;
import org.eclipse.rse.services.clientserver.messages.IndicatorException;
import org.eclipse.rse.services.clientserver.messages.SystemMessage;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.AbstractFileService;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.services.files.RemoteFileCancelledException;
import org.eclipse.rse.services.files.RemoteFileIOException;
import org.eclipse.rse.services.files.RemoteFileSecurityException;

public class SftpFileService extends AbstractFileService implements IFileService, ISshService
{

	private static class SftpBufferedInputStream extends BufferedInputStream {
		
		private ChannelSftp channel;
		
		/**
		 * Creates a BufferedInputStream  and saves its argument, the input stream, for later use. An internal buffer array is created.
		 * @param in the underlying input stream.
		 * @param channel the associated channel.
		 */
		public SftpBufferedInputStream(InputStream in, ChannelSftp channel) {
			super(in);
			this.channel = channel;
		}

		/**
		 * Creates a BufferedInputStream  and saves its argument, the input stream, for later use. An internal buffer array of the given size is created.
		 * @param in the underlying input stream.
		 * @param size the buffer size.
		 * @param channel the associated channel.
		 */
		public SftpBufferedInputStream(InputStream in, int size, ChannelSftp channel) {
			super(in, size);
			this.channel = channel;
		}

		/**
		 * Closes the underlying input stream and channel.
		 * @see java.io.BufferedInputStream#close()
		 */
		public void close() throws IOException {
			super.close();
			channel.disconnect();
		}
	}
	
	private static class SftpBufferedOutputStream extends BufferedOutputStream {
		
		private ChannelSftp channel;
		
		/**
		 * Creates a new buffered output stream to write data to the specified underlying output stream with a default 512-byte buffer size.
		 * @param out the underlying output stream.
		 * @param channel the associated channel.
		 */
		public SftpBufferedOutputStream(OutputStream out, ChannelSftp channel) {
			super(out);
			this.channel = channel;
		}

		/**
		 * Creates a new buffered output stream to write data to the specified underlying output stream with the specified buffer size.
		 * @param out the underlying output stream.
		 * @param size the buffer size.
		 * @param channel the associated channel.
		 */
		public SftpBufferedOutputStream(OutputStream out, int size, ChannelSftp channel) {
			super(out, size);
			this.channel = channel;
		}

		/**
		 * Closes the underlying output stream and the channel.
		 * @see java.io.FilterOutputStream#close()
		 */
		public void close() throws IOException {
			super.close();
			channel.disconnect();
		}
	}
	
	//private SshConnectorService fConnector;
	private ISshSessionProvider fSessionProvider;
	private ChannelSftp fChannelSftp;
	private String fUserHome;
	private Mutex fDirChannelMutex = new Mutex();
	private long fDirChannelTimeout = 5000; //max.5 seconds to obtain dir channel
	/** Client-desired encoding for file and path names */
	private String fControlEncoding = null;
	/** Indicates the default string encoding on this platform */
	private static String defaultEncoding = new java.io.InputStreamReader(new java.io.ByteArrayInputStream(new byte[0])).getEncoding();
	
//	public SftpFileService(SshConnectorService conn) {
//		fConnector = conn;
//	}

	public SftpFileService(ISshSessionProvider sessionProvider) {
		fSessionProvider = sessionProvider;
	}
	
	public void setControlEncoding(String encoding) {
		fControlEncoding = encoding;
	}
	
	/**
	 * Encode String with requested user encoding, in case it differs from Platform default encoding.
	 * @param s String to encode
	 * @return encoded String
	 * @throws SystemMessageException
	 */
	protected String recode(String s) throws SystemMessageException {
		if (fControlEncoding==null) {
			return s;
		} else if (fControlEncoding.equals(defaultEncoding)) {
			return s;
		}
		try {
			byte[] bytes = s.getBytes(fControlEncoding); //what we want on the wire
			return new String(bytes);     //what we need to tell Jsch to get this on the wire
		} catch(UnsupportedEncodingException e) {
			throw makeSystemMessageException(e);
		}
	}
	
	/**
	 * Recode String, and check that no information is lost.
	 * Throw an exception in case the desired Unicode String can not be expressed
	 * by the current encodings. Also enquotes result characters '?' and '*' for
	 * Jsch if necessary. 
	 * @param s String to recode
	 * @return recoded String
	 * @throws SystemMessageException if information is lost
	 */
	protected String recodeSafe(String s) throws SystemMessageException {
		try {
			String recoded = recode(s);
			byte[] bytes = recoded.getBytes(defaultEncoding);
			String decoded = decode(new String(bytes));
			if (!s.equals(decoded)) {
				int i=0;
				int lmax = Math.min(s.length(), decoded.length()); 
				while( (i<lmax) && (s.charAt(i)==decoded.charAt(i))) {
					i++;
				}
				//String sbad=s.substring(Math.max(i-2,0), Math.min(i+2,lmax));
				char sbad = s.charAt(i);
				String msg = "Cannot express character \'"+sbad+"\'(0x"+Integer.toHexString(sbad)  +") with "; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				if (fControlEncoding==null || fControlEncoding.equals(defaultEncoding)) {
					msg += "default encoding \""+defaultEncoding+"\". "; //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					msg += "encoding \""+fControlEncoding+"\" over local default encoding \""+defaultEncoding+"\". "; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
				}
				msg += "Please specify a different encoding in host properties.";  //$NON-NLS-1$
				throw new UnsupportedEncodingException(msg);
			}
			//Quote ? and * characters for Jsch
			//FIXME bug 204705: this does not work properly for commands like ls(), due to a Jsch bug 
			return quoteForJsch(recoded);
		} catch(UnsupportedEncodingException e) {
			try {
				//SystemMessage msg = new SystemMessage("RSE","F","9999",'E',e.getMessage(),"Please specify a different encoding in host properties."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				SystemMessage msg = new SystemMessage("RSE","F","9999",'E',e.getMessage(),""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				//throw new RemoteFileIOException(new SystemMessageException(msg));
				throw new SystemMessageException(msg);
			} catch(IndicatorException ind) {
				throw makeSystemMessageException(e);
			}
		}
	}
	
	/**
	 * Decode String (sftp result) with requested user encoding, in case it differs from Platform default encoding.
	 * @param s String to decode
	 * @return decoded String
	 * @throws SystemMessageException
	 */
	protected String decode(String s) throws SystemMessageException {
		if (fControlEncoding==null) {
			return s;
		} else if (fControlEncoding.equals(defaultEncoding)) {
			return s;
		}
		try {
			byte[] bytes = s.getBytes(); //original bytes sent by SSH
			return new String(bytes, fControlEncoding);
		} catch(UnsupportedEncodingException e) {
			throw makeSystemMessageException(e);
		}
	}

	/** Regular expression pattern to know when Jsch needs quoting. */
	private static Pattern quoteForJschPattern = Pattern.compile("[*?\\\\]"); //$NON-NLS-1$
	
	/**
	 * Quote characters '?' and '*' for Jsch because it would otherwise
	 * use them as patterns for globbing.
	 * @param s String to enquote
	 * @return String with '?' and '*' quoted.
	 */
	protected String quoteForJsch(String s) {
		if(quoteForJschPattern.matcher(s).find()) {
			StringBuffer buf = new StringBuffer(s.length()+8);
			for(int i=0; i<s.length(); i++) {
				char c = s.charAt(i);
//				if(c=='?' || c=='*' || c=='\\') {
				if(c=='?' || c=='*') {
					buf.append('\\');
				}
				buf.append(c);
			}
			s = buf.toString();
		}
		return s;
	}
	
	public String getName() {
		return SshServiceResources.SftpFileService_Name;
	}
	
	public String getDescription() {
		return SshServiceResources.SftpFileService_Description;
	}
	
	public void connect() throws SystemMessageException {
		Activator.trace("SftpFileService.connecting..."); //$NON-NLS-1$
		try {
			Session session = fSessionProvider.getSession();
		    Channel channel=session.openChannel("sftp"); //$NON-NLS-1$
		    channel.connect();
		    fChannelSftp=(ChannelSftp)channel;
		    setControlEncoding(fSessionProvider.getControlEncoding());
			fUserHome = decode(fChannelSftp.pwd());
			Activator.trace("SftpFileService.connected"); //$NON-NLS-1$
		} catch(Exception e) {
			Activator.trace("SftpFileService.connecting failed: "+e.toString()); //$NON-NLS-1$
			throw makeSystemMessageException(e);
		}
	}
	
	/**
	 * Check if the main ssh session is still connected.
	 * Notify ConnectorService of lost session if necessary.
	 * @return <code>true</code> if the session is still healthy.
	 */
	protected boolean checkSessionConnected() {
		Session session = fSessionProvider.getSession();
		if (session==null) {
			// ConnectorService has disconnected already. Nothing to do.
			return false;
		} else if (session.isConnected()) {
			// Session still healthy.
			return true;
		} else {
			// Session was lost, but ConnectorService doesn't know yet.
			// notify of lost session. May reconnect asynchronously later.
			fSessionProvider.handleSessionLost();
			return false;
		}
	}
	
	protected ChannelSftp getChannel(String task) throws SystemMessageException
	{
		Activator.trace(task);
		if (fChannelSftp==null || !fChannelSftp.isConnected()) {
			Activator.trace(task + ": channel not connected: "+fChannelSftp); //$NON-NLS-1$
			if (checkSessionConnected()) {
				//session connected but channel not: try to reconnect
				//(may throw Exception)
				connect();
			} else {
				//session was lost: returned channelSftp will be invalid.
				//This will lead to jsch exceptions (NPE, or disconnected)
				//which are ignored for now since the connection is about
				//to be disconnected anyways.
				throw makeSystemMessageException(new IOException(SshServiceResources.SftpFileService_Error_JschSessionLost));
			}
		}
		return fChannelSftp;
	}
	
	public void disconnect() {
		//disconnect-service may be called after the session is already
		//disconnected (due to event handling). Therefore, don't try to
		//check the session and notify.
		Activator.trace("SftpFileService.disconnect"); //$NON-NLS-1$
		if (fChannelSftp!=null && fChannelSftp.isConnected()) {
			fChannelSftp.disconnect();
		}
		fDirChannelMutex.interruptAll();
		fChannelSftp = null;
	}
	
	
	private SystemMessageException makeSystemMessageException(Exception e) {
		if (e instanceof SystemMessageException) {
			//dont wrap SystemMessageException again
			return (SystemMessageException)e;
		}
		else if (e instanceof SftpException) {
			//Some extra handling to keep Sftp messages
			//TODO more user-friendly messages for more Sftp exception types
			SystemMessageException messageException;
			SftpException sftpe = (SftpException)e;
			if (sftpe.id == ChannelSftp.SSH_FX_PERMISSION_DENIED) {
				messageException = new RemoteFileSecurityException(e);
			} else {
				messageException = new RemoteFileIOException(e);
			}
			messageException.getSystemMessage().makeSubstitution("Sftp: "+sftpe.toString()); //$NON-NLS-1$ //Dont translate since the exception isnt translated either
			return messageException;
		}
		return new RemoteFileIOException(e);
	}
	
	/**
	 * Concatenate a parent directory with a file name to form a new proper path name.
	 * @param parentDir path name of the parent directory.
	 * @param fileName file name to concatenate.
	 * @return path name concatenated from parent directory and file name.
	 * 
	 */
	protected String concat(String parentDir, String fileName) {
		// See also {@link SftpHostFile#getAbsolutePath()}
		StringBuffer path = new StringBuffer(parentDir);
		if (!parentDir.endsWith("/")) //$NON-NLS-1$
		{
			path.append('/');
		}
		path.append(fileName);
		return path.toString();
	}
	
	public IHostFile getFile(String remoteParent, String fileName, IProgressMonitor monitor) throws SystemMessageException
	{
		//getFile() must return a dummy even for non-existent files,
		//or the move() operation will fail. This is described in 
		//the API docs.
		SftpHostFile node = null;
		SftpATTRS attrs = null;
		String fullPath = concat(remoteParent, fileName);
		if (fDirChannelMutex.waitForLock(monitor, fDirChannelTimeout)) {
			try {
				attrs = getChannel("SftpFileService.getFile: "+fullPath).stat(recodeSafe(fullPath)); //$NON-NLS-1$
				Activator.trace("SftpFileService.getFile <--"); //$NON-NLS-1$
				node = makeHostFile(remoteParent, fileName, attrs);
			} catch(Exception e) {
				Activator.trace("SftpFileService.getFile failed: "+e.toString()); //$NON-NLS-1$
				if ( (e instanceof SftpException) && ((SftpException)e).id==ChannelSftp.SSH_FX_NO_SUCH_FILE) {
					//We MUST NOT throw an exception here. API requires that an empty IHostFile
					//is returned in this case.
				} else {
					throw makeSystemMessageException(e);
				}
			} finally {
				fDirChannelMutex.release();
			}
		} else {
			throw new RemoteFileCancelledException();
		}
		if (node==null) {
			node = new SftpHostFile(remoteParent, fileName, false, false, false, 0, 0);
			node.setExists(false);
		}
		return node;
	}
	
	public boolean isConnected() {
		try {
			return getChannel("SftpFileService.isConnected()").isConnected(); //$NON-NLS-1$
		} catch(Exception e) {
			/*cannot be connected when we cannot get a channel*/
		}
		return false;
	}
	
	protected IHostFile[] internalFetch(String parentPath, String fileFilter, int fileType, IProgressMonitor monitor) throws SystemMessageException
	{
		if (fileFilter == null) {
			fileFilter = "*"; //$NON-NLS-1$
		}
		IMatcher filematcher = null;
		if (fileFilter.endsWith(",")) { //$NON-NLS-1$
			String[] types = fileFilter.split(","); //$NON-NLS-1$
			filematcher = new FileTypeMatcher(types, true);
		} else {
			filematcher = new NamePatternMatcher(fileFilter, true, true);
		}
		List results = new ArrayList();
		if (fDirChannelMutex.waitForLock(monitor, fDirChannelTimeout)) {
			try {
			    Vector vv=getChannel("SftpFileService.internalFetch: "+parentPath).ls(recodeSafe(parentPath)); //$NON-NLS-1$
			    for(int ii=0; ii<vv.size(); ii++) {
			    	Object obj=vv.elementAt(ii);
			    	if(obj instanceof ChannelSftp.LsEntry){
			    		ChannelSftp.LsEntry lsEntry = (ChannelSftp.LsEntry)obj;
			    		String fileName = decode(lsEntry.getFilename());
			    		if (".".equals(fileName) || "..".equals(fileName)) { //$NON-NLS-1$ //$NON-NLS-2$
			    			//don't show the trivial names
			    			continue;
			    		}
			    		if (filematcher.matches(fileName) || (lsEntry.getAttrs().isDir() && fileType!=IFileService.FILE_TYPE_FOLDERS)) {
							//get ALL directory names (unless looking for folders only)
			    			SftpHostFile node = makeHostFile(parentPath, fileName, lsEntry.getAttrs());
			    			if (isRightType(fileType, node)) {
			    				results.add(node);
			    			}
			    		}
			    	}
			    }
				Activator.trace("SftpFileService.internalFetch <--"); //$NON-NLS-1$
			} catch(Exception e) {
				//TODO throw new SystemMessageException.
				//We get a "2: No such file" exception when we try to get contents
				//of a symbolic link that turns out to point to a file rather than
				//a directory. In this case, the result is probably expected.
				//We should try to classify symbolic links as "file" or "dir" correctly.
				if (checkSessionConnected()) {
					Activator.trace("SftpFileService.internalFetch failed: "+e.toString()); //$NON-NLS-1$
					throw makeSystemMessageException(e);
				}
				//TODO if not session connected, do we need to throw?
				//Probably not, since the session is going down anyways.  
			} finally {
				fDirChannelMutex.release();
			}
		} else {
			throw new RemoteFileCancelledException();
		}
		return (IHostFile[])results.toArray(new IHostFile[results.size()]);
	}

	private SftpHostFile makeHostFile(String parentPath, String fileName, SftpATTRS attrs) {
		SftpATTRS attrsTarget = attrs;
		String linkTarget=null;
		if (attrs.isLink()) {
			//TODO remove comments as soon as jsch-0.1.29 is available 
			//	try {
			//		//Note: readlink() is supported only with jsch-0.1.29 or higher.
			//		//By catching the exception we remain backward compatible.
			//		linkTarget=getChannel("makeHostFile.readlink").readlink(recode(node.getAbsolutePath())); //$NON-NLS-1$
			//		//TODO: Classify the type of resource linked to as file, folder or broken link
			//	} catch(Exception e) {}
			//check if the link points to a directory
			try {
				getChannel("makeHostFile.chdir").cd(recode(concat(parentPath, fileName))); //$NON-NLS-1$
				linkTarget=decode(getChannel("makeHostFile.chdir").pwd()); //$NON-NLS-1$
				if (linkTarget!=null && !linkTarget.equals(concat(parentPath, fileName))) {
					attrsTarget = getChannel("SftpFileService.getFile").stat(recode(linkTarget)); //$NON-NLS-1$
				} else {
					linkTarget=null;
				}
			} catch(Exception e) {
				//dangling link?
				if (e instanceof SftpException && ((SftpException)e).id==ChannelSftp.SSH_FX_NO_SUCH_FILE) {
					linkTarget=":dangling link"; //$NON-NLS-1$
				}
			}
		}
		SftpHostFile node = new SftpHostFile(parentPath, fileName, attrsTarget.isDir(), false, attrs.isLink(), 1000L * attrs.getMTime(), attrs.getSize());
		if (linkTarget!=null) {
			node.setLinkTarget(linkTarget);
		}
		//Permissions: expect the current user to be the owner
		String perms = attrsTarget.getPermissionsString();
		if (perms.indexOf('r',1)<=0) {
			node.setReadable(false); //not readable by anyone
		}
		if (perms.indexOf('w',1)<=0) {
			node.setWritable(false); //not writable by anyone
		}
		if (node.isDirectory()) {
			if (perms.indexOf('x',1)<=0) {
				node.setWritable(false); //directories that are not executable are also not readable
			}
		} else {
			if (perms.indexOf('x',1)>0) {
				node.setExecutable(true); //executable by someone
			}
		}
		if (attrs.getExtended()!=null) {
			node.setExtendedData(attrs.getExtended());
		}
		return node;
	}
	
	public String getSeparator() {
		return "/"; //$NON-NLS-1$
	}
	
	public boolean upload(File localFile, String remoteParent, String remoteFile, boolean isBinary, String srcEncoding, String hostEncoding, IProgressMonitor monitor) throws SystemMessageException
	{
		String dst = remoteParent;
		if( remoteFile!=null ) {
			dst = concat(dst, remoteFile);
		}
		//TODO what to do with isBinary?
		ChannelSftp channel = null;
		//Fixing bug 158534. TODO remove when bug 162688 is fixed.
		if (monitor==null) {
			monitor = new NullProgressMonitor();
		}
		try {
			SftpProgressMonitor sftpMonitor=new MyProgressMonitor(monitor);
			int mode=ChannelSftp.OVERWRITE;
			dst = recodeSafe(dst);
			getChannel("SftpFileService.upload "+remoteFile); //check the session is healthy //$NON-NLS-1$ 
			channel=(ChannelSftp)fSessionProvider.getSession().openChannel("sftp"); //$NON-NLS-1$
		    channel.connect();
			channel.put(localFile.getAbsolutePath(), dst, sftpMonitor, mode); 
			Activator.trace("SftpFileService.upload "+remoteFile+ " ok"); //$NON-NLS-1$ //$NON-NLS-2$
			if (monitor.isCanceled()) {
				return false;
			} else {
				SftpATTRS attr = channel.stat(dst);
				attr.setACMODTIME(attr.getATime(), (int)(localFile.lastModified()/1000));
				////TODO check if we want to maintain permissions
				//if (!localFile.canWrite()) {
				//	attr.setPERMISSIONS( attr.getPermissions() & (~00400));
				//}
				channel.setStat(dst, attr);
				if (attr.getSize() != localFile.length()) {
					//Error: file truncated? - Inform the user!!
					//TODO test if this works
					throw makeSystemMessageException(new IOException(NLS.bind(SshServiceResources.SftpFileService_Error_upload_size,remoteFile)));
					//return false;
				}
			}
		}
		catch (Exception e) {
			Activator.trace("SftpFileService.upload "+dst+" failed: "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
			throw makeSystemMessageException(e);
			//return false;
		}
		finally {
			if (channel!=null) channel.disconnect();
		}
		return true;
	}

	  public static class MyProgressMonitor implements SftpProgressMonitor
	  {
		  private IProgressMonitor fMonitor;
		  private double fWorkPercentFactor;
		  private Long fMaxWorkKB;
		  private long fWorkToDate;
		  
		  public MyProgressMonitor(IProgressMonitor monitor) {
			  fMonitor = monitor;
		  }
		  public void init(int op, String src, String dest, long max){
			  fWorkPercentFactor = 1.0 / max;
			  fMaxWorkKB = new Long(max / 1024L);
			  fWorkToDate = 0;
			  String srcFile = new Path(src).lastSegment();
			  //String desc = ((op==SftpProgressMonitor.PUT)? 
              //        "Uploading " : "Downloading ")+srcFile;
			  String desc = srcFile;
			  //TODO avoid cast from long to int
			  fMonitor.beginTask(desc, (int)max);
		  }
		  public boolean count(long count){
			  fWorkToDate += count;
			  Long workToDateKB = new Long(fWorkToDate / 1024L);
			  Double workPercent = new Double(fWorkPercentFactor * fWorkToDate);
			  String subDesc = MessageFormat.format(
					  SshServiceResources.SftpFileService_Msg_Progress,
					  new Object[] {
						workToDateKB, fMaxWorkKB, workPercent	  
					  });
			  fMonitor.subTask(subDesc);
		      fMonitor.worked((int)count);
		      return !(fMonitor.isCanceled());
		  }
		  public void end(){
			  fMonitor.done();
		  }
	}

	public boolean upload(InputStream stream, String remoteParent, String remoteFile, boolean isBinary, String hostEncoding, IProgressMonitor monitor) throws SystemMessageException
	{
		//TODO hack for now
		try
		{
			BufferedInputStream bis = new BufferedInputStream(stream);
			File tempFile = File.createTempFile("sftp", "temp"); //$NON-NLS-1$ //$NON-NLS-2$
			FileOutputStream os = new FileOutputStream(tempFile);
			BufferedOutputStream bos = new BufferedOutputStream(os);
	
			 byte[] buffer = new byte[1024];
			 int readCount;
			 while( (readCount = bis.read(buffer)) > 0) 
			 {
			      bos.write(buffer, 0, readCount);
			 }
			 bos.close();
			 upload(tempFile, remoteParent, remoteFile, isBinary, "", hostEncoding, monitor); //$NON-NLS-1$
		}
		catch (Exception e) {
			throw makeSystemMessageException(e);
			//return false;
		}
		return true;
	}
	
	public boolean download(String remoteParent, String remoteFile, File localFile, boolean isBinary, String hostEncoding, IProgressMonitor monitor) throws SystemMessageException
	{
		ChannelSftp channel = null;
		String remotePath = concat(remoteParent, remoteFile);
		try {
			if (!localFile.exists()) {
				File localParentFile = localFile.getParentFile();
				if (!localParentFile.exists()) {
					localParentFile.mkdirs();
				}
				//localFile.createNewFile();
			}
			//TODO Ascii/binary?
			String remotePathRecoded = recode(remotePath);
			int mode=ChannelSftp.OVERWRITE;
			MyProgressMonitor sftpMonitor = new MyProgressMonitor(monitor);
			getChannel("SftpFileService.download "+remoteFile); //check the session is healthy //$NON-NLS-1$
			channel=(ChannelSftp)fSessionProvider.getSession().openChannel("sftp"); //$NON-NLS-1$
		    channel.connect();
			channel.get(remotePathRecoded, localFile.getAbsolutePath(), sftpMonitor, mode);
			Activator.trace("SftpFileService.download "+remoteFile+ " ok"); //$NON-NLS-1$ //$NON-NLS-2$
			if (monitor.isCanceled()) {
				return false;
			} else {
				SftpATTRS attr = channel.stat(remotePathRecoded);
				localFile.setLastModified(1000L * attr.getMTime());
				//TODO should we set the read-only status?
				//if (0==(attrs.getPermissions() & 00400)) localFile.setReadOnly();
				if (attr.getSize() != localFile.length()) {
					//Error: file truncated? - Inform the user!!
					//TODO test if this works
					throw makeSystemMessageException(new IOException(NLS.bind(SshServiceResources.SftpFileService_Error_download_size,remoteFile)));
					//return false;
				}
			}
		}
		catch (Exception e) {
			//TODO improve message and handling when trying to download a symlink
			Activator.trace("SftpFileService.download "+remotePath+" failed: "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
			throw makeSystemMessageException(e);
			//Note: In case of an exception, the caller needs to ensure that in case
			//we downloaded to a temp file, the temp file is deleted again, or a 
			//broken incorrect file might be synchronized back to the source, thus
			//destroying the original file!!
			//return false;
		}
		finally {
			if (channel!=null) {
				channel.disconnect();
			}
		}
		return true;
	}
	
	public IHostFile getUserHome() {
		//As per bug 204710, this may be called before we are connected
		if (fUserHome!=null) {
			int lastSlash = fUserHome.lastIndexOf('/');
			String name = fUserHome.substring(lastSlash + 1);	
			String parent = fUserHome.substring(0, lastSlash);
			try {
				return getFile(parent, name, null);
			} catch(SystemMessageException e) {
				//Error getting user home -> return a handle
				//Returning the home path as a Root is the safest we can do, since it will
				//let users know what the home path is, and the "My Home" filter will be
				//set to correct target. See also bug 204710.
				return new SftpHostFile("", fUserHome, true, true, false, 0, 0); //$NON-NLS-1$
			}
		}
		//Bug 203490, bug 204710: Could not determine user home
		return null;
	}

	public IHostFile[] getRoots(IProgressMonitor monitor) {
		IHostFile root = new SftpHostFile("/", "/", true, true, false, 0, 0); //$NON-NLS-1$ //$NON-NLS-2$
		return new IHostFile[] { root };
	}
	
	public IHostFile createFile(String remoteParent, String fileName, IProgressMonitor monitor) throws SystemMessageException 
	{
		IHostFile result = null;
		String fullPath = concat(remoteParent, fileName);
		if (fDirChannelMutex.waitForLock(monitor, fDirChannelTimeout)) {
			try {
				String fullPathRecoded = recodeSafe(concat(remoteParent, fileName));
				OutputStream os = getChannel("SftpFileService.createFile").put(fullPathRecoded); //$NON-NLS-1$
				//TODO workaround bug 153118: write a single space
				//since jsch hangs when trying to close the stream without writing
				os.write(32); 
				os.close();
				SftpATTRS attrs = getChannel("SftpFileService.createFile.stat").stat(fullPathRecoded); //$NON-NLS-1$
				result = makeHostFile(remoteParent, fileName, attrs);
				Activator.trace("SftpFileService.createFile ok"); //$NON-NLS-1$
			} catch (Exception e) {
				Activator.trace("SftpFileService.createFile "+fullPath+" failed: "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
				throw makeSystemMessageException(e);
			} finally {
				fDirChannelMutex.release();
			}
		} else {
			throw new RemoteFileCancelledException();
		}
		return result;
	}

	public IHostFile createFolder(String remoteParent, String folderName, IProgressMonitor monitor) throws SystemMessageException
	{
		IHostFile result = null;
		String fullPath = concat(remoteParent, folderName);
		if (fDirChannelMutex.waitForLock(monitor, fDirChannelTimeout)) {
			try {
				String fullPathRecoded = recodeSafe(fullPath);
				getChannel("SftpFileService.createFolder").mkdir(fullPathRecoded); //$NON-NLS-1$
				SftpATTRS attrs = getChannel("SftpFileService.createFolder.stat").stat(fullPathRecoded); //$NON-NLS-1$
				result = makeHostFile(remoteParent, folderName, attrs);
				Activator.trace("SftpFileService.createFolder ok"); //$NON-NLS-1$
			} catch (Exception e) {
				Activator.trace("SftpFileService.createFolder "+fullPath+" failed: "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
				throw makeSystemMessageException(e);
			} finally {
				fDirChannelMutex.release();
			}
		} else {
			throw new RemoteFileCancelledException();
		}
		return result;
	}

	public boolean delete(String remoteParent, String fileName, IProgressMonitor monitor) throws SystemMessageException
	{
		boolean ok=false;
		String fullPath = concat(remoteParent, fileName);
		Activator.trace("SftpFileService.delete.waitForLock"); //$NON-NLS-1$
		if (fDirChannelMutex.waitForLock(monitor, fDirChannelTimeout)) {
			try {
				String fullPathRecoded = recodeSafe(fullPath);
				SftpATTRS attrs = null;
				try {
					attrs = getChannel("SftpFileService.delete").lstat(fullPathRecoded); //$NON-NLS-1$
				} catch (SftpException e) {
					//bug 154419: test for dangling symbolic link 
					if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
						//simply try to delete --> if it really doesnt exist, this will throw an exception
						getChannel("SftpFileService.delete.rm").rm(fullPathRecoded); //$NON-NLS-1$
					} else {
						throw e;
					}
				}
				if (attrs==null) {
					//doesn't exist, nothing to do
					ok=true;
				} else if (attrs.isDir()) {
					try {
						getChannel("SftpFileService.delete.rmdir").rmdir(fullPathRecoded); //$NON-NLS-1$
						ok=true;
					} catch(SftpException e) {
						if(e.id==ChannelSftp.SSH_FX_FAILURE) {
							//Bug 153649: Recursive directory delete
							//throw new RemoteFolderNotEmptyException();
							String fullPathQuoted = PathUtility.enQuoteUnix(fullPathRecoded);
							int rv = runCommand("rm -rf "+fullPathQuoted, monitor); //$NON-NLS-1$
							ok = (rv==0);
						} else {
							throw e;
						}
					}
				} else {
					getChannel("SftpFileService.delete.rm").rm(fullPathRecoded); //$NON-NLS-1$
					ok=true;
				}
				Activator.trace("SftpFileService.delete ok"); //$NON-NLS-1$
			} catch (Exception e) {
				Activator.trace("SftpFileService.delete "+fullPath+" failed: "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
				throw makeSystemMessageException(e);
			} finally {
				fDirChannelMutex.release();
			}
		}
		return ok;
	}

	public boolean rename(String remoteParent, String oldName, String newName, IProgressMonitor monitor) throws SystemMessageException
	{
		boolean ok=false;
		String fullPathOld = concat(remoteParent, oldName);
		String fullPathNew = concat(remoteParent, newName);
		if (fDirChannelMutex.waitForLock(monitor, fDirChannelTimeout)) {
			try {
				getChannel("SftpFileService.rename").rename(recode(fullPathOld), recodeSafe(fullPathNew)); //$NON-NLS-1$
				ok=true;
				Activator.trace("SftpFileService.rename ok"); //$NON-NLS-1$
			} catch (Exception e) {
				Activator.trace("SftpFileService.rename "+fullPathOld+" -> "+fullPathNew+" failed: "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				throw makeSystemMessageException(e);
			} finally {
				fDirChannelMutex.release();
			}
		}
		return ok;
	}
	
	public boolean rename(String remoteParent, String oldName, String newName, IHostFile oldFile, IProgressMonitor monitor) throws SystemMessageException {
		// TODO dont know how to update
		return rename(remoteParent, oldName, newName, monitor);
	}

	private boolean progressWorked(IProgressMonitor monitor, int work) {
		boolean cancelRequested = false;
		if (monitor!=null) {
			monitor.worked(work);
			cancelRequested = monitor.isCanceled();
		}
		return cancelRequested;
	}
	
	public int runCommand(String command, IProgressMonitor monitor) throws SystemMessageException
	{
		Activator.trace("SftpFileService.runCommand "+command); //$NON-NLS-1$
		int result = -1;
		if (monitor!=null) {
			monitor.beginTask(command, 20);
		}
		Channel channel = null;
		try {
			channel=fSessionProvider.getSession().openChannel("exec"); //$NON-NLS-1$
			((ChannelExec)channel).setCommand(command);

			//No user input
			channel.setInputStream(null);
			//Capture error output for exception text
			ByteArrayOutputStream err = new ByteArrayOutputStream();
			((ChannelExec)channel).setErrStream(err);
			InputStream in=channel.getInputStream();
			channel.connect();
			byte[] tmp=new byte[1024];
			while(!channel.isClosed()){
				if( progressWorked(monitor,1) ) {
					break;
				}
				while(in.available()>0){
					int i=in.read(tmp, 0, 1024);
					if(i<0)break;
					//System.out.print(new String(tmp, 0, i));
				}
				try{Thread.sleep(1000);}catch(Exception ee){}
			}
			result = channel.getExitStatus();
			if (result!=0) {
				String errorMsg = err.toString();
				Activator.trace("SftpFileService.runCommand ok, error: "+result+", "+errorMsg); //$NON-NLS-1$ //$NON-NLS-2$
				if (errorMsg.length()>0) {
					throw makeSystemMessageException(new IOException(errorMsg));
				}
			} else {
				Activator.trace("SftpFileService.runCommand ok, result: "+result); //$NON-NLS-1$
			}
		} catch(Exception e) {
			Activator.trace(command);
			Activator.trace("SftpFileService.runCommand failed: "+e.toString()); //$NON-NLS-1$
			throw makeSystemMessageException(e);
		} finally {
			if (monitor!=null) {
				monitor.done();
			}
			if (channel!=null) {
				channel.disconnect();
			}
		}
		return result;
	}
	
	public boolean move(String srcParent, String srcName, String tgtParent, String tgtName, IProgressMonitor monitor) throws SystemMessageException
	{
		// move is not supported by sftp directly. Use the ssh shell instead.
		// TODO check if newer versions of sftp support move directly
		// TODO Interpret some error messages like "command not found" (use ren instead of mv on windows)
		// TODO mimic by copy if the remote does not support copying between file systems?
		Activator.trace("SftpFileService.move "+srcName); //$NON-NLS-1$
		String fullPathOld = PathUtility.enQuoteUnix(recode(concat(srcParent, srcName)));
		String fullPathNew = PathUtility.enQuoteUnix(recodeSafe(concat(tgtParent, tgtName)));
		int rv = runCommand("mv "+fullPathOld+' '+fullPathNew, monitor); //$NON-NLS-1$
		return (rv==0);
	}

	public boolean copy(String srcParent, String srcName, String tgtParent, String tgtName, IProgressMonitor monitor) throws SystemMessageException {
		// copy is not supported by sftp directly. Use the ssh shell instead.
		// TODO check if newer versions of sftp support copy directly
		// TODO Interpret some error messages like "command not found" (use (x)copy instead of cp on windows)
		Activator.trace("SftpFileService.copy "+srcName); //$NON-NLS-1$
		String fullPathOld = PathUtility.enQuoteUnix(recode(concat(srcParent, srcName)));
		String fullPathNew = PathUtility.enQuoteUnix(recodeSafe(concat(tgtParent, tgtName)));
		int rv = runCommand("cp -Rp "+fullPathOld+' '+fullPathNew, monitor); //$NON-NLS-1$
		return (rv==0);
	}
	
	public boolean copyBatch(String[] srcParents, String[] srcNames, String tgtParent, IProgressMonitor monitor) throws SystemMessageException 
	{
		Activator.trace("SftpFileService.copyBatch "+srcNames); //$NON-NLS-1$
		boolean ok = true;
		for (int i = 0; i < srcParents.length; i++)
		{
			//TODO check what should happen if one file throws an Exception 
			//should the batch job continue? 
			ok = ok && copy(srcParents[i], srcNames[i], tgtParent, srcNames[i], monitor);
		}
		return ok;
	}

	public void initService(IProgressMonitor monitor) {
		Activator.trace("SftpFileService.initService"); //$NON-NLS-1$
		try
		{
			connect();
		}
		catch (Exception e)
		{			
		}
	}
	
	public void uninitService(IProgressMonitor monitor) {
		Activator.trace("SftpFileService.uninitService"); //$NON-NLS-1$
		disconnect();
	}

	public boolean isCaseSensitive() {
		//TODO find out whether remote is case sensitive or not
		return true;
	}

	public boolean setLastModified(String parent, String name,
			long timestamp, IProgressMonitor monitor) throws SystemMessageException 
	{
		boolean ok=false;
		String path = concat(parent, name);
		if (fDirChannelMutex.waitForLock(monitor, fDirChannelTimeout)) {
			try {
				getChannel("SftpFileService.setLastModified").setMtime(recode(path), (int)(timestamp/1000)); //$NON-NLS-1$
				ok=true;
				Activator.trace("SftpFileService.setLastModified ok"); //$NON-NLS-1$
			} catch (Exception e) {
				Activator.trace("SftpFileService.setLastModified "+path+" failed: "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
				throw makeSystemMessageException(e);
			} finally {
				fDirChannelMutex.release();
			}
		}
		return ok;
	}

	public boolean setReadOnly(String parent, String name,
			boolean readOnly, IProgressMonitor monitor) throws SystemMessageException {
		boolean ok=false;
		String path = concat(parent, name);
		if (fDirChannelMutex.waitForLock(monitor, fDirChannelTimeout)) {
			try {
				SftpATTRS attr = getChannel("SftpFileService.setReadOnly").stat(recode(path)); //$NON-NLS-1$
				int permOld = attr.getPermissions();
				int permNew = permOld;
				if (readOnly) {
					permNew &= ~(128 | 16 | 2); //ugo-w
				} else {
					permNew |= 128; //u+w
				}
				if (permNew != permOld) {
					//getChannel("SftpFileService.setReadOnly").chmod(permNew, path); //$NON-NLS-1$
					attr.setPERMISSIONS(permNew); 
					getChannel("SftpFileService.setReadOnly").setStat(recode(path), attr); //$NON-NLS-1$
					ok=true;
					Activator.trace("SftpFileService.setReadOnly ok"); //$NON-NLS-1$
				} else {
					ok=true;
					Activator.trace("SftpFileService.setReadOnly nothing-to-do"); //$NON-NLS-1$
				}
			} catch (Exception e) {
				Activator.trace("SftpFileService.setReadOnly "+path+" failed: "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
				throw makeSystemMessageException(e);
			} finally {
				fDirChannelMutex.release();
			}
		}
		return ok;
	}

	/**
	 * Gets the input stream to access the contents of a remote file.
	 * @since 2.0
	 * @see org.eclipse.rse.services.files.AbstractFileService#getInputStream(String, String, boolean, IProgressMonitor) 
	 */
	public InputStream getInputStream(String remoteParent, String remoteFile, boolean isBinary, IProgressMonitor monitor) throws SystemMessageException {
		
		InputStream stream = null;
		
		String remotePath = concat(remoteParent, remoteFile);
		try {
			String remotePathRecoded = recode(remotePath);
			getChannel("SftpFileService.getInputStream " + remoteFile); //check the session is healthy //$NON-NLS-1$
			ChannelSftp channel = (ChannelSftp)fSessionProvider.getSession().openChannel("sftp"); //$NON-NLS-1$
		    channel.connect();
			stream = new SftpBufferedInputStream(channel.get(remotePathRecoded), channel);
			Activator.trace("SftpFileService.getInputStream " + remoteFile + " ok"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		catch (Exception e) {
			Activator.trace("SftpFileService.getInputStream " + remotePath + " failed: " + e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
			throw makeSystemMessageException(e);
		}
		
		return stream;
	}

	/**
	 * Gets the output stream to write to a remote file.
	 * @since 2.0
	 * @see org.eclipse.rse.services.files.AbstractFileService#getOutputStream(String, String, boolean, IProgressMonitor)
	 */
	public OutputStream getOutputStream(String remoteParent, String remoteFile, boolean isBinary, IProgressMonitor monitor) throws SystemMessageException {
		
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		OutputStream stream = null;
		String dst = remoteParent;
		if (remoteFile!=null) {
			dst = concat(remoteParent, remoteFile);
		}
		
		try {
			SftpProgressMonitor sftpMonitor = new MyProgressMonitor(monitor);
			int mode = ChannelSftp.OVERWRITE;
			getChannel("SftpFileService.getOutputStream " + remoteFile); //check the session is healthy //$NON-NLS-1$
			ChannelSftp channel = (ChannelSftp)fSessionProvider.getSession().openChannel("sftp"); //$NON-NLS-1$
		    channel.connect();
			stream = new SftpBufferedOutputStream(channel.put(recodeSafe(dst), sftpMonitor, mode), channel); 
			Activator.trace("SftpFileService.getOutputStream " + remoteFile + " ok"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		catch (Exception e) {
			Activator.trace("SftpFileService.getOutputStream " + dst + " failed: " + e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
			throw makeSystemMessageException(e);
		}
		if (monitor.isCanceled()) {
			throw new RemoteFileCancelledException();
		}
		return stream;
	}
}
