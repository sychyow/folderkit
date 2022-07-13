/**
 * 
 */
package ru.kronshtadt.tc.folderkit;

import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCException;

/**
 * Rename routines encapsulation
 * 
 * @author Dmitriy.Sychev
 *
 */
public class FolderkitRenamer {
	private String mPrefix;
	private String mSuffix;
	private String mWhat;
	private String mWith;
	private boolean mDoName;
	private boolean mDoDesc;
	
	/**
	 * Sets a flag to process object_name attribute
	 * @param mDoName the mDoName to set
	 */
	public void setDoName(boolean mDoName) {
		this.mDoName = mDoName;
	}

	/**
	 * Sets a flag to process object_desc attribute
	 * @param mDoDesc the mDoDesc to set
	 */
	public void setDoDesc(boolean mDoDesc) {
		this.mDoDesc = mDoDesc;
	}

	/**
	 * Sets a prefix to add
	 */
	public void setPrefix(String pref) {
		mPrefix = pref;
	}
	
	/**
	 * Sets a suffix to add
	 * @param suff
	 */
	public void setSuffix(String suff) {
		mSuffix = suff;
	}
	
	/**
	 * Sets replace parameters
	 * 
	 * @param src Template for what to replace
	 * @param dst String to replace with
	 * @throws Exception when src is empty and dst is set
	 */
	public void setReplace(String src, String dst) throws Exception {
		if (src.isEmpty()&&!dst.isEmpty()) throw new Exception(Messages.RenameFoldersDlg_ReplaceParamError);
		mWhat = src;
		mWith = dst;
	}
	
	/**
	 * Performs changes on a given string
	 * @param src
	 * @return
	 */
	private String renameString(String src) {
		StringBuilder sb = new StringBuilder();
		sb.append(mPrefix);
		if (!mWhat.isEmpty()) sb.append(src.replaceAll(mWhat, mWith));
		else sb.append(src);
		sb.append(mSuffix);
		return sb.toString();
	}
	
	/**
	 * Performs rename actions on the attributes of an object given
	 * 
	 * @param comp An object to change its attributes
	 * @throws TCException
	 */
	public void rename(TCComponentFolder comp) throws TCException{
		if (mDoName) {
			renameProperty(comp, "object_name"); //$NON-NLS-1$
		}
		if (mDoDesc) {
			renameProperty(comp, "object_desc"); //$NON-NLS-1$
		}	
	}

	/**
	 * Renames an attribute specified for an object
	 * 
	 * @param comp An object to changes
	 * @param propName Attribute name to change
	 * @throws TCException
	 */
	private void renameProperty(TCComponentFolder comp, String propName)
			throws TCException {
		String val = comp.getStringProperty(propName);
		val = renameString(val);
		comp.setStringProperty(propName, val);
	}
	
}
