/*
 *HooMESearch.java
 *Jan 31, 2013
 */
package hoome.message;

import mvn.Consts;

/**
 * represent the results of a search
 * @author Sola Adekunle
 *
 */
public class Result {

	private long fileID;  //file id
	private long fileSize; //filesize
	private String fileName; //file name



	/**
	 * Constructs a Result from given input
	 * @param fileID file ID
	 * @param fileSize file size
	 * @param fileName file name
	 * @return
	 * @throws BadAttributeValueException file name
	 */
	public Result(long fileID, long fileSize, String fileName) throws BadAttributeValueException {
		this.setFileId(fileID);
		this.setFileSize(fileSize);
		this.setFileName(fileName);
	}

	/**
	 * Constructs a single Result instance from given input stream
	 * @param in  input stream to parse
	 * @throws java.io.IOException if bad attribute value
	 * @throws BadAttributeValueException if problem parsing Result instance
	 */
	public Result(MessageInput in) throws java.io.IOException, BadAttributeValueException {
		int fileId = in.getScanner().readInt();
		int size = in.getScanner().readInt();
		String fileName = in.readString();
		this.setFileId(HooMEUtilities.getUnsignedint(fileId));
		this.setFileSize(HooMEUtilities.getUnsignedint(size));
		this.setFileName(fileName);
		
		
	}

	/**
	 * Serialize Result to given output stream
	 * @param out  output stream to serialize to
	 * @throws java.io.IOException if unable to serialize Result instance
	 */
	public void encode(MessageOutput out) throws java.io.IOException {
		out.getDataOutputStream().writeInt((int)this.fileID);
		out.getDataOutputStream().writeInt((int)this.fileSize);
		out.getDataOutputStream().write(this.fileName.getBytes(HooMEMessage.CHARENCODING));
		out.getDataOutputStream().write(Consts.EOLN.getBytes(HooMEMessage.CHARENCODING));
	}
	/**
	 * overides hash code in java.lang.object
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (fileID ^ (fileID >>> 32));
		result = prime * result
				+ ((fileName == null) ? 0 : fileName.hashCode());
		result = prime * result + (int) (fileSize ^ (fileSize >>> 32));
		return result;
	}

	/**
	 * Overides equals in java.lang.object
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Result other = (Result) obj;
		if (fileID != other.fileID)
			return false;
		if (fileName == null) {
			if (other.fileName != null)
				return false;
		} else if (!fileName.equals(other.fileName))
			return false;
		if (fileSize != other.fileSize)
			return false;
		return true;
	}

	/**
	 * gets the file Id
	 * @return file Id
	 */
	public long getFileId() {
		return fileID;
	}

	/**
	 * sets the id of the file
	 * @param fileID the id of the file
	 * @throws BadAttributeValueException if bad file Id
	 */
	public void setFileId(long fileID)   throws BadAttributeValueException {
		if(fileID<0 ) {
			throw new BadAttributeValueException(Consts.ILL_ARG);
		}
		this.fileID = fileID;
	}

	/**
	 * gets the size of the file
	 * @return the file zise
	 */
	public long getFileSize() {
		return fileSize;
	}

	/**
	 * sets the size of the file
	 * @param fileSize  size of the file
	 * @throws BadAttributeValueException if bad attribute value
	 */
	public void setFileSize(long fileSize)  throws BadAttributeValueException {
		if(fileSize>Consts.MAX_UNSIGNED_INT || fileSize<0 ) {
			throw new BadAttributeValueException(Consts.ILL_ARG);
		}
		this.fileSize = fileSize;
	}

	/**
	 * gets the file name of the result
	 * @return the file name
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * sets filename
	 * @param fileName name of the file
	 * @throws BadAttributeValueException if bad attribute value
	 */
	public void setFileName(String fileName)  throws BadAttributeValueException {
		if(null==fileName || !fileName.matches(Consts.FILE_NAME_REGEX) || fileName.isEmpty() ||
				fileName.length() >= Consts.MAX_UNSIGNED_SHORT) {
			throw new BadAttributeValueException(Consts.ILL_ARG);
		}
		this.fileName = fileName;
	}

	/**
	 * overides to string in Java.lang.object
	 */
	@Override
	public String toString() {
		return super.toString() + "Result [fileID=" + fileID + ", fileSize=" + fileSize
				+ ", fileName=" + fileName + "]";
	}
	
	
	
}



