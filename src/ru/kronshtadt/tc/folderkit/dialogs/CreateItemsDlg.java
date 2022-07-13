package ru.kronshtadt.tc.folderkit.dialogs;

import java.awt.Frame;
import java.io.File;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ru.kronshtadt.tc.folderkit.DatasetFactory;
import ru.kronshtadt.tc.folderkit.DatasetFactory.PreferenceMap;
import ru.kronshtadt.tc.folderkit.Messages;
import ru.kronshtadt.tc.folderkit.base.BaseFolderkitDialog;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.SoaUtil;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.HorizontalLayout;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement;

/**
 * Create Items functionality user interface
 * 
 * @author Dmitriy.Sychev
 * 
 */
public class CreateItemsDlg extends BaseFolderkitDialog {

	private static final long serialVersionUID = -6316274552256419300L;
	public final static Logger logger = Logger.getLogger(CreateItemsDlg.class);
	private static final int FIELD_COUNT = 3;
	private DataManagementService service;
	private DatasetFactory dst = new DatasetFactory(session);
	private JTextField txtRevId;

	/**
	 * Initialises class with Item factory Parameters are AIF standard
	 * 
	 * @param cc
	 * @param parent
	 */
	public CreateItemsDlg(AIFComponentContext[] cc, Frame parent) {
		super(cc, parent);
		setTitle(Messages.CreateItemsDlg_Title);
		showLog = true;
		service = DataManagementService.getService(session);
	}

	@Override
	protected JPanel getAdditionalPanel() {
		JPanel addPanel = new JPanel();
		addPanel.setLayout(new HorizontalLayout());
		JLabel lblRev = new JLabel();
		lblRev.setText("Идентификатор ревизии:");
		addPanel.add("left.nobind.left.center", lblRev);
		txtRevId = new JTextField(3);
		txtRevId.setText("01");
		addPanel.add("left.nobind.left.center", txtRevId);
		return addPanel;
	}

	/**
	 * Processes the input string. Each string must have four tab-separated field,
	 * or an Exception will be raised 0 -- item type 1 -- item id 2 -- description 3
	 * -- name 4 -- file to upload (optional)
	 */
	@Override
	protected boolean processItem(String item) {
		try {
			String[] fields = item.split("\t"); //$NON-NLS-1$
			if (fields.length < FIELD_COUNT) {
				log.add(item);
				log.add(String.format(Messages.CreateItemsDlg_ErrFields, fields.length));
				return true;
			}
			if (hasCyr(fields[0], fields[1]))
				throw new TCException(Messages.CreateItemsDlg_CyrillicChars);

			if (hasEmptyField(fields))
				throw new TCException(Messages.CreateItemsDlg_EmptyFourthField);

			if (hasInvalidFile(fields))
				throw new TCException(Messages.CreateItemsDlg_InvalidFilePath);

			if (txtRevId.getText().isEmpty())
				throw new TCException("Обозначение ревизии должно быть указано!");

			if (!PreferenceMap.setType(fields))
				throw new TCException(String.format(Messages.CreateItemsDlg_InvalidFileExtension,
						DatasetFactory.getFileExtension(fields[3])));

			TCComponentItem newItem = makeItem(fields);
			parentFolder.add("contents", newItem); //$NON-NLS-1$

			if (fields.length > FIELD_COUNT) {
				dst.addDataSet(newItem.getLatestItemRevision(), fields[3]);
			}

		} catch (TCException e) {
			log.add(item);
			log.add(String.format("\t%s", e.getError())); //$NON-NLS-1$
		}
		return true;
	}

	private TCComponentItem makeItem(final String[] fields) throws TCException {
		final DataManagementService.CreateIn objectCreIn = new DataManagementService.CreateIn();
		objectCreIn.clientId = "1"; //$NON-NLS-1$
		final DataManagement.CreateInput itemCreInput = new DataManagement.CreateInput();
		itemCreInput.boName = fields[0];

		final DataManagement.CreateInput revCreInput = new DataManagement.CreateInput();
		revCreInput.boName = fields[0] + "Revision"; //$NON-NLS-1$
		revCreInput.stringProps.put("item_revision_id", txtRevId.getText());
		itemCreInput.compoundCreateInput.put("revision", new DataManagementService.CreateInput[] { revCreInput }); //$NON-NLS-1$

		itemCreInput.stringProps.put("item_id", fields[1]); //$NON-NLS-1$
		itemCreInput.stringProps.put("object_name", fields[2]); //$NON-NLS-1$
		// itemCreInput.stringProps.put("object_desc", fields[3]); //$NON-NLS-1$

		objectCreIn.data = itemCreInput;
		DataManagementService.CreateResponse resp;
		try {
			resp = service.createObjects(new DataManagementService.CreateIn[] { objectCreIn });
		} catch (ServiceException e) {
			logger.error(e.getMessage());
			throw new TCException(e.getLocalizedMessage());
		}
		if (resp.serviceData.sizeOfPartialErrors() == 0) {
			return (TCComponentItem) resp.output[0].objects[0];
		} else {
			SoaUtil.checkPartialErrors(resp.serviceData);
		}
		return null;
	}

	private boolean hasInvalidFile(String[] fields) {

		if (fields.length > FIELD_COUNT) {
			fields[3] = fields[3].replaceAll("\"", "");
			if (new File(fields[3]).exists())
				return false;

			return true;
		}
		return false;
	}

	private boolean hasEmptyField(String[] fields) {

		if (fields.length > FIELD_COUNT) {
			if (fields[3].length() <= 4)
				return true;

			return false;
		}
		return false;
	}

	/**
	 * Checks if the given id for certain type has cyrillic symbols
	 * 
	 * @param type type of the object
	 * @param id   item id
	 * @return true when cyrillic symbol found, false otherwise
	 */
	private boolean hasCyr(String type, String id) {
		// Will disable because of ItemID Naming Rules;
		return false;
		/*
		 * // The list of type forbidden to have cyrillics in ids if
		 * (!type.equals("Bs7_Assy") && //$NON-NLS-1$ !type.equals("Bs7_Detail") &&
		 * //$NON-NLS-1$ !type.equals("Bs7_PKI")) //$NON-NLS-1$ return false; for (char
		 * c : id.toCharArray()) { //Cyrillic range, as per The Unicode Standard,
		 * Version 6.2 final boolean isCyrillic = 0x400 <= c && c <= 0x4ff; if
		 * (isCyrillic) return true; } return false;
		 */
	}

}
