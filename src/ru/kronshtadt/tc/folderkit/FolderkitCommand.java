package ru.kronshtadt.tc.folderkit;

import java.awt.Frame;

import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentPseudoFolder;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

import ru.kronshtadt.tc.folderkit.dialogs.CompareFoldersDlg;
import ru.kronshtadt.tc.folderkit.dialogs.CreateFoldersDlg;
import ru.kronshtadt.tc.folderkit.dialogs.CreateItemsDlg;
import ru.kronshtadt.tc.folderkit.dialogs.ExportStructureDlg;
import ru.kronshtadt.tc.folderkit.dialogs.ImportStructureDlg;
import ru.kronshtadt.tc.folderkit.dialogs.LinkObjectsDlg;
import ru.kronshtadt.tc.folderkit.dialogs.LoadItemsDlg;
import ru.kronshtadt.tc.folderkit.dialogs.NormaliseItemsDlg;
import ru.kronshtadt.tc.folderkit.dialogs.RenameItemDlg;
import ru.kronshtadt.tc.folderkit.dialogs.RenameProcessDlg;
import ru.kronshtadt.tc.folderkit.dialogs.SetAttrsDlg;
import ru.kronshtadt.tc.folderkit.utils.DDInserter;
import ru.kronshtadt.tc.folderkit.utils.FldUtil;
import ru.kronshtadt.tc.folderkit.utils.ProcessHelper;

/**
 * Standard AIF class to handle an interface action
 * 
 * @author Dmitriy.Sychev
 * 
 */
public class FolderkitCommand extends AbstractAIFCommand {
	protected static final int CREATE_COUNT = 1;
	protected static final int COMPARE_COUNT = 2;

	TCComponentDataset pdfDD;

	/**
	 * Standard constructor
	 * 
	 * @param ccs
	 * @param frame
	 */
	public FolderkitCommand(final AIFComponentContext ccs[], Frame frame) {
		this(ccs, frame, null);
	}

	/**
	 * Checks the input selection and runs corresponding user interface
	 * 
	 * @param ccs   selected components
	 * @param frame a host frame
	 * @param key   Command action key, as defined in plugin.xml
	 */
	public FolderkitCommand(AIFComponentContext ccs[], final Frame frame, final String key) {
		super();
		parent = frame;

		if (isEmpty(ccs)) return;
		
		if (key.equals("ru.kronshtadt.tc.explorer.createFolders")) {//$NON-NLS-1$
			com.ktgroup.tc.sauron.Eye.watch("FolderKit.createFolders"); //$NON-NLS-1$
			if (isFolders(ccs, false)&& isCorrectCount(ccs, CREATE_COUNT)) {
				setRunnable(new CreateFoldersDlg(ccs, parent));
			}
		}
		if (key.equals("ru.kronshtadt.tc.explorer.createItems")) {//$NON-NLS-1$
			com.ktgroup.tc.sauron.Eye.watch("FolderKit.createItems"); //$NON-NLS-1$
			if (isFolders(ccs, false)&& isCorrectCount(ccs, CREATE_COUNT)) {
				setRunnable(new CreateItemsDlg(ccs, parent));
			}
		}
		if (key.equals("ru.kronshtadt.tc.explorer.loadItems")) {//$NON-NLS-1$
			com.ktgroup.tc.sauron.Eye.watch("FolderKit.loadItems"); //$NON-NLS-1$
			if (isFolders(ccs, true)&& isCorrectCount(ccs, CREATE_COUNT)) {
				setRunnable(new LoadItemsDlg(ccs, parent));
			}
		}
		if (key.equals("ru.kronshtadt.tc.explorer.linkObjects")) {//$NON-NLS-1$
			com.ktgroup.tc.sauron.Eye.watch("FolderKit.linkObjects"); //$NON-NLS-1$
			setRunnable(new LinkObjectsDlg(ccs, parent));
		}
		if (key.equals("ru.kronshtadt.tc.explorer.setAttrs")) {//$NON-NLS-1$
			com.ktgroup.tc.sauron.Eye.watch("FolderKit.setAttrs"); //$NON-NLS-1$
			setRunnable(new SetAttrsDlg(ccs, parent));
		}

		if (key.equals("ru.kronshtadt.tc.explorer.compareFolders")) {//$NON-NLS-1$
			com.ktgroup.tc.sauron.Eye.watch("FolderKit.compareFolders"); //$NON-NLS-1$
			if (isFolders(ccs, true)&& isCorrectCount(ccs, COMPARE_COUNT)) {
				setRunnable(new CompareFoldersDlg(ccs, parent));
			}
		}		
		if (key.equals("ru.kronshtadt.tc.explorer.renameItem")) {//$NON-NLS-1$
			com.ktgroup.tc.sauron.Eye.watch("FolderKit.renameItem"); //$NON-NLS-1$
			if (isItem(ccs)&& isSingle(ccs)) {
				setRunnable(new RenameItemDlg(ccs, parent));
			}
		}
		if (key.equals("ru.kronshtadt.tc.explorer.renameProcess")) {//$NON-NLS-1$
			com.ktgroup.tc.sauron.Eye.watch("FolderKit.renameProcess"); //$NON-NLS-1$
			if (isSingle(ccs)) {
				ProcessHelper ph = new ProcessHelper(ccs);
				ph.acquireProcess();
				if (!ph.gotProcess()) {
					MessageBox.post(ph.getErrorString(), Messages.DataCheck_ErrorTitle, MessageBox.ERROR);
					return;
				}
				setRunnable(new RenameProcessDlg(ph.getProcess(), parent));
			}
		}

		if (key.equals("ru.kronshtadt.tc.explorer.normaliseItem")) {//$NON-NLS-1$
			com.ktgroup.tc.sauron.Eye.watch("FolderKit.normaliseItem"); //$NON-NLS-1$
			AIFComponentContext[] expanded  = expandFolder(ccs);
			setRunnable(new NormaliseItemsDlg(expanded, parent));
		}
		
		if (key.equals("ru.kronshtadt.tc.explorer.importStructure")) {//$NON-NLS-1$
			com.ktgroup.tc.sauron.Eye.watch("FolderKit.importStructure"); //$NON-NLS-1$
			if (isFolders(ccs, false) && isCorrectCount(ccs, CREATE_COUNT)) {
				setRunnable(new ImportStructureDlg(ccs, parent));
			} 
		}
		
		if (key.equals("ru.kronshtadt.tc.explorer.exportStructure")) {//$NON-NLS-1$
			com.ktgroup.tc.sauron.Eye.watch("FolderKit.exportStructure"); //$NON-NLS-1$
			if (isFolders(ccs, false) && isCorrectCount(ccs, CREATE_COUNT)) {
				try {
					setRunnable(new ExportStructureDlg(ccs, parent));
				} catch (TCException e) {
					MessageBox.post(e);
				}
			} 
		}


	/*	if (key.equals("ru.kronshtadt.tc.explorer.aboutFolderKit")) {//$NON-NLS-1$
			Version version = FrameworkUtil.getBundle(getClass()).getVersion();
			String ver = String.format(Messages.FolderkitCommand_AboutMsg, version.getMajor(), version.getMinor(),
					version.getMicro());
			MessageBox.post(frame, ver, Messages.FolderkitCommand_AboutTitle, MessageBox.INFORMATION);
		}
*/
	}

	private AIFComponentContext[] expandFolder(AIFComponentContext[] ccs) {
		TCComponent c = (TCComponent) ccs[0].getComponent();
		if (FldUtil.isFolder(c, true))
			try {
				return FldUtil.getChildrenContex(c);
			} catch (TCException e) {
				e.printStackTrace();
				return new AIFComponentContext[0];
			}
		return ccs;
	}

	public DDInserter makeInserter(AIFComponentContext[] ccs) {
		DDInserter di;
		TCComponentItem item = (TCComponentItem) ccs[0].getComponent();
		try {
			di = new DDInserter(item.getLatestItemRevision());
		} catch (TCException e) {
			MessageBox.post(e);
			return null;
		}
		if (!di.loadPDF()) {
			MessageBox.post("Набор данных PDF не найден в ИО ДД", Messages.DataCheck_ErrorTitle, MessageBox.ERROR);
			return null;
		}
		return di;
	}

	
	private boolean isEmpty(final AIFComponentContext aaifcomponentcontext[]) 
	{
		if (aaifcomponentcontext == null || aaifcomponentcontext.length == 0) {
			MessageBox.post(Messages.DataCheck_SelectionEmpty, Messages.DataCheck_ErrorTitle, MessageBox.ERROR);
			return true;
		}
		return false;
	}
	
	
	private boolean isSingle(final AIFComponentContext aaifcomponentcontext[])
	{
		if (aaifcomponentcontext.length != 1) {
			MessageBox.post("Выберите только один объект!", Messages.FolderkitCommand_Error, MessageBox.ERROR);
			return false;
		}
		return true;
	}

	private boolean isFolders(final AIFComponentContext aaifcomponentcontext[], boolean usePseudo) {
		for (AIFComponentContext aic : aaifcomponentcontext) {
			if (aic.getComponent() == null) {
				MessageBox.post(Messages.DataCheck_SelectionInvalid, Messages.DataCheck_ErrorTitle, MessageBox.ERROR);
				return false;
			}
			try {
				TCComponent ic = (TCComponent) aic.getComponent();
				boolean isFolder;
				if (usePseudo) {
					isFolder = ic instanceof TCComponentFolder || ic instanceof TCComponentPseudoFolder;
				} else {
					isFolder = ic instanceof TCComponentFolder;
				}
				if (!isFolder) {
					MessageBox.post(Messages.DataCheck_FolderdOnly, Messages.DataCheck_WarningTitle, MessageBox.WARNING);
					return false;
				}
			} catch (Exception ex) {
				MessageBox.post(ex);
				return false;
			}
		}
		return true;
	}
	
	private boolean isItem(final AIFComponentContext aaifcomponentcontext[]) {
		for (AIFComponentContext aic : aaifcomponentcontext) {
			if (aic.getComponent() == null) {
				MessageBox.post(Messages.DataCheck_SelectionInvalid, Messages.DataCheck_ErrorTitle, MessageBox.ERROR);
				return false;
			}
			boolean isItem;
			try {
				TCComponent ic = (TCComponent) aic.getComponent();
				isItem = ic instanceof TCComponentItem;
				
				if (!isItem) {
					MessageBox.post("Выберите сборку или деталь!", Messages.DataCheck_WarningTitle, MessageBox.WARNING);
					return false;
				}
			} catch (Exception ex) {
				MessageBox.post(ex);
				return false;
			}
		}
		return true;
	}
	
	
	private boolean isCorrectCount(final AIFComponentContext aaifcomponentcontext[], int count) 
	{
		if (aaifcomponentcontext.length != count) {
			MessageBox.post(
					String.format(Messages.DataCheck_FolderCount, count, aaifcomponentcontext.length),
					Messages.DataCheck_ErrorTitle, MessageBox.ERROR);
			return false;
		}	
		return true;
	}

	private Frame parent;
}
