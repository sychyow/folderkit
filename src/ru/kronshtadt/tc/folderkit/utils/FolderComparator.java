package ru.kronshtadt.tc.folderkit.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import ru.kronshtadt.tc.folderkit.dialogs.InsertDDDlg;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;

/**
 * The class compares two folders according to specification: Инструмент должен
 * проверять наличие каждого элемента из первой папки во второй указанной папке
 * или любой её подпапке.
 * 
 * @author Dmitriy.Sychev
 * 
 */
public class FolderComparator
{
	/*
	 * The contents are stored in maps, the key is UID, just because it's more
	 * universal object-wise, so we'd able to compare not only items, but any
	 * object if a need would arise
	 */
	private Map<String, TCComponent> baseContent;
	private Map<String, TCComponent> refContent;
	private Set<String> visited = new HashSet<>();
	
	private final static Logger logger = Logger.getLogger(InsertDDDlg.class);

	/**
	 * Initialises the class with the folders to compare: gets related contents
	 * 
	 * @param fldFirst
	 *            First folder object
	 * @param fldSecond
	 *            Second folder object
	 */
	public FolderComparator(final TCComponent fldFirst,
			final TCComponent fldSecond)
	{
		baseContent = new HashMap<>();
		refContent = new HashMap<>();
		extractContent(fldFirst, baseContent, true);
		visited.clear();
		extractContent(fldSecond, refContent, true);
	}

	/**
	 * The method to receive a list of objects belong to the given folder
	 * 
	 * @param folder
	 *            Folder to start the traverse from
	 * @param mapContent
	 *            A container to store the results
	 * @param goDeep
	 *            If true, the method will recursively visit the child folders
	 *            all the way down
	 */
	private void extractContent(final TCComponent folder,
			Map<String, TCComponent> mapContent, final boolean goDeep)
	{
		if (visited.contains(folder.getUid()))
		{ // no vicious circles or redundant visits, please
			return;
		} else
		{
			visited.add(folder.getUid());
		}
		try
		{
			TCComponent[] children = FldUtil.getChildren(folder);
			for (final TCComponent child : children)
			{ // Classes other than Item, ItemRevision and Folder are to be ignored
				if (child instanceof TCComponentItem || child instanceof TCComponentItemRevision)
				{
					mapContent.put(child.getUid(), child);
					continue;
				}
				if (child instanceof TCComponentFolder && goDeep)
				{
					extractContent((TCComponentFolder) child, mapContent, goDeep);
				}
			}
		} catch (TCException e)
		{
			logger.error(e.getStackTrace());
		}
	}

	/**
	 * The actual comparison is implemented here. OK, could have used a Set
	 * here, decided against it, first, because with a map one could do some
	 * object-based comparison, second, HashSet is backed by the same HashMap,
	 * so why bother?
	 * 
	 * @return An array of items that make difference
	 */
	public TCComponent[] compareContent()
	{
		ArrayList<TCComponent> diffItems = new ArrayList<>();
		
		for (Entry<String, TCComponent> e: baseContent.entrySet()) {
			if (!refContent.containsKey(e.getKey())) {
				diffItems.add(e.getValue());
			}
		}
		return diffItems.toArray(new TCComponent[0]);
	}

}
