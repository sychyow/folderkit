package ru.kronshtadt.tc.folderkit.dialogs;

import java.awt.Frame;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentFolderType;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

import ru.kronshtadt.tc.folderkit.Messages;
import ru.kronshtadt.tc.folderkit.base.BaseFolderkitDialog;

public class ImportStructureDlg extends BaseFolderkitDialog 
{
	private static final long serialVersionUID = 1001L;
	public final static Logger logger = Logger.getLogger(CreateItemsDlg.class);
	private  TCComponentFolderType folderType;
	private TCComponentItemType itemType;
	private static final String strFolderType = "Folder"; //$NON-NLS-1$
	private static final String regex = ".*\\.[0-9]{3}(( [�-�0-9]{1,3})?(-[�-�0-9]{1,3})?)?(\\.[A-Z0-9]{4})?";
	private Pattern txtPattern;
	
	/**
	 * Initialises class with Item factory Parameters are AIF standard
	 * 
	 * @param cc
	 * @param parent
	 */	
	public ImportStructureDlg(AIFComponentContext[] cc, Frame parent) {
		super(cc, parent, false);
		setTitle(Messages.ImportStructureDlg_Title);
		try
		{
			itemType = (TCComponentItemType)session.getTypeComponent("Item");
			folderType = (TCComponentFolderType)session.getTypeComponent(strFolderType);
		} catch (TCException e)
		{
			logger.error(e.getStackTrace());
		} 
		showLog = true;
		txtPattern = Pattern.compile(regex);
		okButton.setText("�������������");
	}
	

	@Override
	protected boolean processItem(String path) {
		if(!path.equals("")) {
			File itemFolder = new File(path);
			recursionByFile(path, createFolder(parentFolder, itemFolder.getName()));
		} else MessageBox.post("����� �� ���� �������", "������", MessageBox.ERROR);
		return true;
	}
	
	/**
	 * ������� ��������� ����������
	 * @param folderPath - ������� ���� ���������� � �������
	 * @param parentFolderForChild - ������� ���� ���������� � ��
	 * */
	
	private void recursionByFile (String folderPath, TCComponent parentFolderForChild) {
		File itemFolder = null;
		File[] folderEntries = null;
		String fileName = "";
		TCComponentItem[] items = null;
		
		try {
			itemFolder = new File(folderPath);
			folderEntries = sortFileByType(itemFolder.listFiles());
			
		    for (File entry : folderEntries)
		    {
		    	fileName = entry.getName();
		    	if (entry.isFile()) {
		    		if (fileName.toLowerCase().endsWith(".pdf")) 
		    			items = itemType.findItems(renamePDF(fileName));
		    		else if (fileName.toLowerCase().endsWith(".txt")) 
		    			items = itemType.findItems(renameTXT(fileName));
		    		
		    		if (items.length > 0)
		    			for(TCComponentItem item : items) 
		    				parentFolderForChild.add(addProperty, item);	
		    		else 
		    			log.add("������ " + fileName + " �� ������ � Teamcenter. ������ �� ��� ��������. \n������������ �������: " + folderPath);	
		    	}	
		    	else if (entry.isDirectory())
		    		recursionByFile(folderPath + "\\" + fileName, createFolder(parentFolderForChild, fileName));
		    }
		}
		catch (TCException e) {
			log.add(fileName);
			log.add(String.format("\t%s", e.getError()));
		}
	}

	/**
	 * ����� �������� ����� � TC
	 * 
	 * @param parentFolderForChild -���������� TC ��� ���������� �����
	 * @param nameFolder - ������������ ����������� �����
	 * @return TCComponentFolder - ��������� ����� � TC
	 * */
	private TCComponentFolder createFolder (TCComponent parentFolderForChild, String nameFolder) {
		TCComponentFolder newFolder = (TCComponentFolder) parentFolderForChild;	
		try {
			newFolder = folderType.create(nameFolder, "", strFolderType);
    		parentFolderForChild.add(addProperty, newFolder);    		
		} catch (Exception e) {
			MessageBox.post(e);
		}
		return newFolder;
	}

	
	/**
	 * ����� ���������� ������ ������ � ���������� �� ���� �����(pdf � txt � ������ ������, ����� � �����)
	 * 
	 * @param folderEntries - ������ ������
	 * 
	 * */
	private File[] sortFileByType (File[] folderEntries) {
		
		List<File> folders = new ArrayList<>(); 
		folders.addAll(Arrays.asList(folderEntries));

		Comparator<File> itemComp = new ItemComparator();
		Collections.sort(folders, itemComp);
        
        return folders.toArray(new File[0]);
     }
	
	
	/**
	 * ����� ��� ����������� ����������� pdf �����
	 * */
	private String renamePDF(String name) {
		String [] strSplit = name.split(" ���", 2);        
		return strSplit[0];
	}
	/**
	 * ����� ��� ����������� ����������� txt �����
	 * */
	private String renameTXT(String name) {
		Matcher matcher = txtPattern.matcher(name);
		
		while(matcher.find()) {
			return matcher.group();
		}	
		return "";		
	}
}

/**
 * ���������� ��� ��������� ����� �����
 * */

class ItemComparator implements Comparator<File>{
	@Override
	public int compare(File arg0, File arg1) {
		if (arg0.isFile() && arg1.isDirectory())
		    return 1;
		else if(arg0.isDirectory() && arg1.isFile())
		    return -1;
		return 0;
	}
}

//List<File> itemFiles = new ArrayList<>();
//List<File> itemFolders = new ArrayList<>();

//for (File entry : folderEntries) {
//	if(entry.getName().toLowerCase().endsWith(".pdf") || entry.getName().toLowerCase().endsWith(".txt"))
//		itemFiles.add(entry);
//	else if(entry.isDirectory())
//		itemFolders.add(entry);
//}

//folders.addAll(itemFiles);
//folders.addAll(itemFolders);