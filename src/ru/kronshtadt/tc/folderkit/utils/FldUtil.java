package ru.kronshtadt.tc.folderkit.utils;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentPseudoFolder;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

public class FldUtil {

	public static TCComponent[] getChildren(final TCComponent folder) throws TCException {
		TCComponent children[] = new TCComponent[0];
		if (folder instanceof TCComponentFolder) {
			children = folder.getReferenceListProperty("contents"); //$NON-NLS-1$
		}
		if (folder instanceof TCComponentPseudoFolder) {
			String prop = ((TCComponentPseudoFolder) folder).getDefaultPasteRelation();
			children = ((TCComponentPseudoFolder) folder).getOwningComponent().getReferenceListProperty(prop); // $NON-NLS-1$
		}
		return children;
	}

	public static AIFComponentContext[] getChildrenContex(final TCComponent folder) throws TCException {
		AIFComponentContext[] children = new AIFComponentContext[0];
		if (folder instanceof TCComponentFolder) {
			children = folder.getChildren("contents"); //$NON-NLS-1$
		}
		if (folder instanceof TCComponentPseudoFolder) {
			String prop = ((TCComponentPseudoFolder) folder).getFolderContext(false);
			children = ((TCComponentPseudoFolder) folder).getOwningComponent().getRelated(prop); // $NON-NLS-1$
		}
		return children;
	}

	/**
	 * Universal routine to get name from both a Folder and a PseudoFolder
	 * 
	 * @param Folder or PseudoFolder
	 * @return name of the object
	 * @throws TCException
	 */
	public static String getName(TCComponent comp) throws TCException {
		if (comp instanceof TCComponentPseudoFolder) {
			return ((TCComponentPseudoFolder) comp).getFolderContext(true);
		}
		return comp.getStringProperty("object_name"); //$NON-NLS-1$
	}

	public static boolean isFolder(final TCComponent ic, boolean usePseudo) {
		try {
			boolean isFolder;
			if (usePseudo) {
				isFolder = ic instanceof TCComponentFolder || ic instanceof TCComponentPseudoFolder;
			} else {
				isFolder = ic instanceof TCComponentFolder;
			}
			if (!isFolder) {
				return false;
			}
		} catch (Exception ex) {
			MessageBox.post(ex);
			return false;
		}
		return true;
	}

}
