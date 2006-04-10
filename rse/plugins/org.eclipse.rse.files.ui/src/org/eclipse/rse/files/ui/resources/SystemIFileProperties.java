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

package org.eclipse.rse.files.ui.resources;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.rse.core.SystemPlugin;



/**
 * Class that encapsulates ISeries IResource persistent properties.
 */
public class SystemIFileProperties implements ISystemTextEditorConstants, ISystemRemoteEditConstants {

  
	private static final String STRING_EMPTY = "";
	private static final String EXPORT_KEY = "export";

	private static QualifiedName _nameDirty               = new QualifiedName( STRING_EMPTY, TEMP_FILE_DIRTY            );
	private static QualifiedName _nameReadOnly            = new QualifiedName( STRING_EMPTY, TEMP_FILE_READONLY         );
	private static QualifiedName _nameEditorProfileType   = new QualifiedName( STRING_EMPTY, EDITOR_PROFILE_TYPE        );
	private static QualifiedName _nameEncoding            = new QualifiedName( STRING_EMPTY, SOURCE_ENCODING_KEY        );
	private static QualifiedName _nameHasSequenceNumbers  = new QualifiedName( STRING_EMPTY, SEQUENCE_NUMBERS_KEY       );
	private static QualifiedName _nameRecordLength        = new QualifiedName( STRING_EMPTY, MAX_LINE_LENGTH_KEY        );
	private static QualifiedName _nameRemoteCCSID         = new QualifiedName( STRING_EMPTY, CCSID_KEY                  );
	private static QualifiedName _nameRemoteFileObject    = new QualifiedName( STRING_EMPTY, REMOTE_FILE_OBJECT_KEY     );
	private static QualifiedName _nameRemoteFilePath      = new QualifiedName( STRING_EMPTY, REMOTE_FILE_PATH_KEY       );
	private static QualifiedName _nameRemoteFileSubSystem = new QualifiedName( STRING_EMPTY, REMOTE_FILE_SUBSYSTEM_KEY  );
	private static QualifiedName _nameRemoteFileTimeStamp = new QualifiedName( STRING_EMPTY, REMOTE_FILE_MODIFIED_STAMP );
	private static QualifiedName _nameDownloadFileTimeStamp  = new QualifiedName( STRING_EMPTY, DOWNLOAD_FILE_MODIFIED_STAMP  );
	private static QualifiedName _nameUsedBinaryTransfer  = new QualifiedName( STRING_EMPTY, REMOTE_FILE_BINARY_TRANSFER );
	private static QualifiedName _nameTempCCSID           = new QualifiedName( STRING_EMPTY, TEMP_CCSID_KEY             );
	private static QualifiedName _nameRemoteBIDILogical   = new QualifiedName( STRING_EMPTY, BIDI_LOGICAL_KEY           );

	// for path mapping
	private static QualifiedName _nameRemoteFileMounted = new QualifiedName( STRING_EMPTY, REMOTE_FILE_MOUNTED);
	private static QualifiedName _nameResolvedMountedRemoteFileHost = new QualifiedName( STRING_EMPTY, RESOLVED_MOUNTED_REMOTE_FILE_HOST_KEY);
	private static QualifiedName _nameResolvedMountedRemoteFilePath = new QualifiedName( STRING_EMPTY, RESOLVED_MOUNTED_REMOTE_FILE_PATH_KEY);
	
	// local encoding qualified name
	// NOTE: DO NOT CHANGE THIS!! This exact qualified name is used by the IBM debugger.
	private static QualifiedName _nameLocalEncoding		  = new QualifiedName(SystemPlugin.getDefault().getSymbolicName(), LOCAL_ENCODING_KEY);
		
	protected IResource _resource = null;
	
	/**
	 * 
	 */
	public SystemIFileProperties( IResource file ){
		_resource = file;
		
		if( file == null )
			throw new NullPointerException();
	}

	/**
	 * Returns the temp file dirty persistent property value.
	 */
	public boolean getDirty(){
		return getPropertyBoolean( _nameDirty );
	}
	
	/**
	 * Returns the temp file readonly persistent property value.
	 */
	public boolean getReadOnly(){
		return getPropertyBoolean( _nameReadOnly);
	}

	/**
	 * Returns whether the original file is marked as a mounted file
	 */
	public boolean getRemoteFileMounted(){
		return getPropertyBoolean( _nameRemoteFileMounted);
	}

	/**
	 * Returns the editor profile persistent property value.
	 */
	public String getEditorProfileType(){
		return getPropertyString( _nameEditorProfileType );		
	}

	/**
	 * Returns the source encoding persistent property value.
	 */
	public String getEncoding(){
		return getPropertyString( _nameEncoding );
	}
	
	/**
	 * Gets the local encoding persistent property value.
	 * @return the local encoding.
	 */
	public String getLocalEncoding() {
		return getPropertyString(_nameLocalEncoding);
	}

	/**
	 * Returns the file that this instance is associated with.
	 */
	public IResource getFile() {
		return _resource;
	}

	/**
	 * Returns the sequence numbers flag persistent property value.
	 */
	public boolean getHasSequenceNumbers(){
		return getPropertyBoolean( _nameHasSequenceNumbers );
	}	

	/**
	 * Returns the session property value of the given property.
	 */
	protected Object getParentSessionObject( QualifiedName name ){
		try{
			return _resource.getParent().getSessionProperty( name );
		}
		catch( CoreException ex ){
			return null;
		}
	}

	/**
	 * Returns the value of a boolean property.
	 */
	protected boolean getPropertyBoolean( QualifiedName name ){
		try{
			String strValue = _resource.getPersistentProperty( name );
			if( strValue == null )
				return false;
				
			return strValue.equals( "true" );
		}
		catch( CoreException ex ){
			return false;
		}		
	}
	
	/**
	 * Returns the value of an integer property.
	 */
	protected int getPropertyInteger( QualifiedName name ){
		try{
			String strValue = _resource.getPersistentProperty( name );
			
			if( strValue == null )
				return 0;
				
			return Integer.parseInt( strValue );
		}
		catch( CoreException ex ){
			return 0;
		}
		catch( NumberFormatException ex ){
			return 0;
		}
	}

	/**
	 * Returns the value of an integer property.
	 */
	protected long getPropertyLong( QualifiedName name ){
		try{
			String strValue = _resource.getPersistentProperty( name );				

			if( strValue == null )
				return 0;

			return Long.parseLong( strValue );
		}
		catch( CoreException ex ){
			return 0;
		}		
		catch( NumberFormatException ex ){
			return 0;
		}
	}
	
	/**
	 * Returns the value of a string persistent or session property.
	 */
	protected String getPropertyString( QualifiedName name ){
		try{
			return _resource.getPersistentProperty( name );
		}
		catch( CoreException ex ){
			return STRING_EMPTY;
		}
	}
	
	/**
	 * Returns the remote file object session property value.
	 */
	public int getRecordLength() {
		return getPropertyInteger( _nameRecordLength );
	}

	/**
	 * Returns the CCSID persistent property value.
	 */
	public int getRemoteCCSID(){
		return getPropertyInteger( _nameRemoteCCSID );
	}
	
	/**
	 * Returns the remote file object session property value.
	 */
	public Object getRemoteFileObject(){
		return getSessionObject( _nameRemoteFileObject );
	}

	/**
	 * Returns the remote file object session property value.
	 */
	public Object getTempCCSID(){
		return getParentSessionObject( _nameTempCCSID );
	}


	/**
	 * Returns the actual file (member) path persistent property value on the originating host.
	 */
	public String getResolvedMountedRemoteFilePath(){
		return getPropertyString( _nameResolvedMountedRemoteFilePath);
	}
	
	/**
	 * Returns the actual file (member) host persistent property value for a mounted file.
	 */
	public String getResolvedMountedRemoteFileHost(){
		return getPropertyString( _nameResolvedMountedRemoteFileHost);
	}
	
	/**
	 * Returns the full file (member) path persistent property value.
	 */
	public String getRemoteFilePath(){
		return getPropertyString( _nameRemoteFilePath );
	}

	/**
	 * Returns the sub-system name persistent property value.
	 */
	public String getRemoteFileSubSystem(){
		return getPropertyString( _nameRemoteFileSubSystem );
	}

	/**
	 * Returns the remote file time stamp persistent property value.
	 */
	public long getRemoteFileTimeStamp(){
		return getPropertyLong( _nameRemoteFileTimeStamp );
	}
	
	

	/**
	 * Returns the timestamp of the Eclipse resource after download
	 */
	public long getDownloadFileTimeStamp(){
		return getPropertyLong( _nameDownloadFileTimeStamp );
	}

	/**
	 * Returns the session property value of the given property.
	 */
	protected Object getSessionObject( QualifiedName name ){
		try{
			return _resource.getSessionProperty( name );
		}
		catch( CoreException ex ){
			return null;
		}
	}

	/**
	 * Returns the binary transfer flag persistent property value.
	 */
	public boolean getUsedBinaryTransfer(){
		return getPropertyBoolean( _nameUsedBinaryTransfer );
	}	

	/**
	 * Sets the temp file dirty persistent property value.
	 */
	public void setDirty( boolean bDirty ){
		setPropertyBoolean( _nameDirty, bDirty );
	}

	/**
	 * Sets the temp file readonly persistent property value.
	 */
	public void setReadOnly( boolean bReadOnly ){
		setPropertyBoolean( _nameReadOnly, bReadOnly );
	}
	
	/**
	 * Sets the remote file mounted indicator property value
	 */
	public void setRemoteFileMounted( boolean bMounted){
		setPropertyBoolean( _nameRemoteFileMounted, bMounted);
	}

	/**
	 * Sets the editor profile type persistent property value.
	 */
	public void setEditorProfileType( String strType ){
		setPropertyString( _nameEditorProfileType, strType );
	}
	
	/**
	 * Sets the source encoding persistent property value.
	 */
	public void setEncoding( String strEncoding ){
		setPropertyString( _nameEncoding, strEncoding );
	}
	
	/**
	 * Sets the local encoding persistent property value.
	 * @param strLocalEncoding the local encoding.
	 */
	public void setLocalEncoding(String strLocalEncoding) {
		setPropertyString(_nameLocalEncoding, strLocalEncoding);
	}
	
	/**
	 * Returns whether the file is stored in BIDI logical format.
	 * @return <code>true</code> if the file is stored in BIDI logical format, <code>false</code> if the file is stored in BIDI
	 * visual format.
	 */
	public boolean getBIDILogical() {
		return getPropertyBoolean(_nameRemoteBIDILogical);
	}

	/**
	 * Sets whether the file is stored in BIDI logical format.
	 * @param logical <code>true</code> if the file is stored in BIDI logical format, <code>false</code> if the file is stored in BIDI
	 * visual format. 
	 */
	public void setBIDILogical(boolean logical) {
		setPropertyBoolean(_nameRemoteBIDILogical, logical);
	}
	
	/**
	 * Sets the sequence numbers flag persistent property value.
	 */
	public void setHasSequenceNumbers( boolean bSequenceNumbers ){
		setPropertyBoolean( _nameHasSequenceNumbers, bSequenceNumbers );
	}	

	/**
	 * Sets the session property to the given object.
	 */
	protected void setParentSessionObject( QualifiedName name, Object objValue ){
		try{
			_resource.getParent().setSessionProperty( name, objValue );
		}
		catch( CoreException ex ){
		}
	}	

	/**
	 * Sets a boolean property given a property name, and its value.
	 */
	protected void setPropertyBoolean( QualifiedName name, boolean bValue ){
		setPropertyString( name, bValue == true ? "true" : "false" );
	}	

	/**
	 * Sets a boolean property given a property name, and its value.
	 */
	protected void setPropertyInteger( QualifiedName name, int iValue ){
		setPropertyString( name, String.valueOf( iValue ) );
	}	

	/**
	 * Sets a boolean property given a property name, and its value.
	 */
	protected void setPropertyLong( QualifiedName name, long lValue ){
		setPropertyString( name, String.valueOf( lValue ) );
	}	

	/**
	 * Sets a string property given a property name, and its value.
	 */
	protected void setPropertyString( QualifiedName name, String strValue ){
		
		// Setting is expensive, so do get and compare first
		//--------------------------------------------------
		String strValueCurrent = null;
		
		try{
			strValueCurrent = _resource.getPersistentProperty( name );
		}
		
		catch( CoreException ex ){
			strValueCurrent = STRING_EMPTY;
		}

		// If the value to be set is currently set, do nothing
		//----------------------------------------------------
		if( strValue != null && strValue.equals( strValueCurrent ) == true )
			return;

		// McCoy the new value
		//--------------------				
		try{
			_resource.setPersistentProperty( name, strValue );
		}
		catch( CoreException ex ){
		}
	}	

	/**
	 * Sets the record length persistent property value.
	 */
	public void setRecordLength( int iRecordLength ){
		setPropertyInteger( _nameRecordLength, iRecordLength );
	}

	/**
	 * Sets the codepage source encoding such as "Cp937" persistent property value.
	 */
	public void setRemoteCCSID( int iCCSID ){
		setPropertyInteger( _nameRemoteCCSID, iCCSID );
	}
	
	/**
	 * Sets the remote system member path persistent property value.
	 */
	public void setRemoteFileObject( Object object ) {
		setSessionObject( _nameRemoteFileObject, object );
	}
	
	
	/**
	  * Sets the actual remote system file (member) path persistent property value on the originating host.
	  */
	public void setResolvedMountedRemoteFilePath( String strPath ){
		setPropertyString( _nameResolvedMountedRemoteFilePath, strPath );
	}
	
	
	/**
	  * Sets the actual remote system file (member) path persistent property value on the originating host.
	  */
	public void setResolvedMountedRemoteFileHost( String strHost ){
		setPropertyString( _nameResolvedMountedRemoteFileHost, strHost );
	}
	
	/**
	 * Sets the remote system file (member) path persistent property value.
	 */
	public void setRemoteFilePath( String strPath ){
		setPropertyString( _nameRemoteFilePath, strPath );
	}
	
	/**
	 * Sets the sub system name persistent property value.
	 */
	public void setRemoteFileSubSystem( String strSubSystem ){
		setPropertyString( _nameRemoteFileSubSystem, strSubSystem );
	}

	/**
	 * Sets the remote file time stamp persistent property value.
	 */
	public void setRemoteFileTimeStamp( long lTimeStamp ){
		setPropertyLong( _nameRemoteFileTimeStamp, lTimeStamp );
	}
	
	/**
	 * Sets the local file time stamp property value of download.
	 */
	public void setDownloadFileTimeStamp( long lTimeStamp ){
		setPropertyLong( _nameDownloadFileTimeStamp, lTimeStamp );
	}
	
	/**
	 * Sets the session property to the given object.
	 */
	protected void setSessionObject( QualifiedName name, Object objValue ){
		try{
			_resource.setSessionProperty( name, objValue );
		}
		catch( CoreException ex ){
		}
	}

	/**
	 * Sets the remote system member path persistent property value.
	 */
	public void setTempCCSID( Object object ) {
		setParentSessionObject( _nameTempCCSID, object );
	}	

	/**
	 * Sets the binary transfer flag persistent property value.
	 */
	public void setUsedBinaryTransfer( boolean bBinaryTransfer ){
		setPropertyBoolean( _nameUsedBinaryTransfer, bBinaryTransfer );
	}
	
	public void setModificationStampAtExport(String hostName, String destination, long modificationStamp) {
	    QualifiedName key = new QualifiedName(STRING_EMPTY, EXPORT_KEY + ":" + hostName + ":" + destination);
	    setPropertyLong(key, modificationStamp);
	}
	
	public long getModificationStampAtExport(String hostName, String destination) {
	    QualifiedName key = new QualifiedName(STRING_EMPTY, EXPORT_KEY + ":" + hostName + ":" + destination);
	    return getPropertyLong(key);
	}
	
	public boolean hasModificationStampAtExport(String hostName, String destination) {
	    QualifiedName key = new QualifiedName(STRING_EMPTY, EXPORT_KEY + ":" + hostName + ":" + destination);
	    String val = getPropertyString(key);
	    
	    if (val != null) {
	        return true;
	    }
	    else {
	        return false;
	    }
	}
}