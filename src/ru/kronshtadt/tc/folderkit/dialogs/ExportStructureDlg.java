package ru.kronshtadt.tc.folderkit.dialogs;

import java.awt.Frame;

import org.apache.log4j.Logger;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCComponentRevisionRuleType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

import ru.kronshtadt.tc.folderkit.Messages;
import ru.kronshtadt.tc.folderkit.base.BaseFolderkitDialog;

public class ExportStructureDlg extends BaseFolderkitDialog 
{
	private static final long serialVersionUID = 1L;
	public final static Logger logger = Logger.getLogger(CreateItemsDlg.class);

	private final static String CONFIG_RULE = "Выпущено, последняя";
	private final static String EXPORT_TYPE = "Bs7_Doc";
	private TCComponentRevisionRule revRule = null;
	
	/**
	 * Initialises class with Item factory Parameters are AIF standard
	 * 
	 * @param cc
	 * @param parent
	 * @throws TCException 
	 */	
	public ExportStructureDlg(AIFComponentContext[] cc, Frame parent) throws TCException {
		super(cc, parent, false);
		setTitle(Messages.ExportStructureDlg_Title);
			TCComponentRevisionRuleType ruleType = (TCComponentRevisionRuleType) session.getTypeComponent("RevisionRule");
			TCComponent[] revrules = ruleType.extent();
			for (TCComponent arule : revrules) {
				String name = arule.getStringProperty("object_name");
				if (CONFIG_RULE.equals(name)) {
					revRule = (TCComponentRevisionRule) arule;
					break;
				}
			}
			
		if (revRule == null)
			throw new TCException("Правило ревизий ["+CONFIG_RULE+"] не найдено!");
		showLog = true;
		okButton.setText("Экспортировать");
	}
	
	@Override
	protected boolean processItem(String path) {
		if(!path.equals("")) {
			exportFolder(parentFolder, path);
		} else MessageBox.post("Папка не была выбрана", "Ошибка", MessageBox.ERROR);
		return true;
	}
	
	private void exportFolder(TCComponent parentFolder, String path) {
		String folderName = "";
		try {
			folderName = parentFolder.getStringProperty("object_name");
			TCComponent[] entries = parentFolder.getReferenceListProperty("contents");
			for(TCComponent entry:entries) {
				if (entry instanceof TCComponentFolder) {
					exportFolder(entry, path);
					continue;
				}
				if (entry instanceof TCComponentItem) {
					exportItem((TCComponentItem)entry, path);
					continue;
				}
			}
		} catch (TCException e) {
			log.add(folderName);
			log.add(e.getError());
		}
	}

	private void exportItem(TCComponentItem item, String path) {
		String itemName = item.getObjectString();
		try {
			String itemType = item.getStringProperty("object_type");
			if (!EXPORT_TYPE.equals(itemType)) return;
			
			TCComponentItemRevision rev = item.getConfiguredItemRevision(revRule);
			if (rev==null) throw new TCException("Нет сконфигурированной ревизии");
			
			TCComponent[] datasets = rev.getRelatedComponents("IMAN_specification");
			for (TCComponent ds : datasets) {
				String typeDS = ds.getType();
				if ("PDF".equals(typeDS)) {
					TCComponent ref = ((TCComponentDataset)ds).getNamedRefComponent("PDF_Reference");
					if (ref == null)
						return;
					((TCComponentDataset)ds).getFiles("PDF_Reference", path);
			}
		}	

		
			
		} catch (TCException e) {
			log.add(itemName);
			log.add(e.getError());
		}
		
	}
	

}
