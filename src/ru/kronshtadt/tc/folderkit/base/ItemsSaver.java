package ru.kronshtadt.tc.folderkit.base;

/**
 * An interface with a handler from 'Save to folder' button. Because stupid Java
 * cannot into proper delegates.
 * 
 * @author Dmitriy.Sychev
 * 
 */
public interface ItemsSaver
{
	/**
	 * A hander for 'Save to folder' button
	 */
	void saveInFolder();
}
