package ru.kronshtadt.tc.folderkit.dialogs;

import java.awt.Frame;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;

import ru.kronshtadt.tc.folderkit.base.BaseFolderkitDialog;
import ru.kronshtadt.tc.folderkit.utils.AttrSetter;

public class SetAttrsDlg extends BaseFolderkitDialog {

	private static final long serialVersionUID = -4458575094274251268L;
	private final AttrSetter setter = new AttrSetter();

	public SetAttrsDlg(AIFComponentContext[] cc, Frame frame) {
		super(cc, frame);
		setTitle("Заполнить атрибуты");
		okButton.setText("Заполнить");
		showLog = true;
		setter.setLog(log);
	}

	@Override
	protected boolean processItem(String item) {
		setter.set(item);
		return true;
	}

}
