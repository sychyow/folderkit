package ru.kronshtadt.tc.folderkit.dialogs;

import java.awt.Frame;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.apache.log4j.Logger;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.HorizontalLayout;
import com.teamcenter.rac.util.MessageBox;

import ru.kronshtadt.tc.folderkit.Messages;
import ru.kronshtadt.tc.folderkit.base.BaseFolderkitDialog;
import ru.kronshtadt.tc.folderkit.utils.ItemLinker;

public class LinkObjectsDlg extends BaseFolderkitDialog {

	private class ActTypeListener implements ItemListener {

		@Override
		public void itemStateChanged(ItemEvent e) {
			int state = e.getStateChange();
			if (state == ItemEvent.SELECTED) {
				JRadioButton btn = (JRadioButton) e.getItem();
				okButton.setText(btn.getText());
			}
		}
	}

	private static final String DELETE = "Удалить";
	private static final String CREATE = "Создать";
	private static final String CHECK = "Проверить";
	/**
	 * 
	 */
	private static final long serialVersionUID = -1732647958924589831L;
	public final static Logger logger = Logger.getLogger(LinkObjectsDlg.class);
	private JRadioButton optCheck;
	private JRadioButton optCreate;
	private JRadioButton optDelete;
	private ItemLinker linker;

	public LinkObjectsDlg(AIFComponentContext[] cc, Frame frame) {
		super(cc, frame);
		setTitle("Связать объекты");
		showLog = true;
		okButton.setText(CHECK);
	}

	@Override
	protected JPanel getAdditionalPanel() {
		JPanel addPanel = new JPanel(new HorizontalLayout());
		ButtonGroup group = new ButtonGroup();
		optCheck = new JRadioButton(CHECK);
		optCreate = new JRadioButton(CREATE);
		optDelete = new JRadioButton(DELETE);
		optCheck.addItemListener(new ActTypeListener());
		optCreate.addItemListener(new ActTypeListener());
		optDelete.addItemListener(new ActTypeListener());
		group.add(optCheck);
		group.add(optCreate);
		group.add(optDelete);
		addPanel.add("left.nobind.left.center", optCheck);
		addPanel.add("left.nobind.left.center", optCreate);
		addPanel.add("left.nobind.left.center", optDelete);
		optCheck.setSelected(true);
		return addPanel;
	}

	@Override
	protected boolean processItem(String item) {
		linker.link(item);
		return true;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onOK()
	{
		if (Items.isEmpty()) return;
		final int len = Items.size();
		if (okButton.getText().equals(strClose))
		{
			dispose();
			return;
		}
		if (optCheck.isSelected())
			linker = new ItemLinker(ItemLinker.MODE_CHECK);
		else
			if (optCreate.isSelected())
				linker = new ItemLinker(ItemLinker.MODE_CREATE);
			else
				if (optDelete.isSelected())
					linker = new ItemLinker(ItemLinker.MODE_DELETE);
		linker.setLog(log);
		
		pbBar.setMinimum(0);
		pbBar.setMaximum(len);
		try
		{
			parentFolder.lock();
		} catch (TCException e)
		{
			MessageBox.post(e);
			okButton.setText(strClose);
			pbBar.setString(Messages.BaseDlg_Failed);
			return;
		}
		Thread t = new Thread(new onOKAction());
		t.start();
	}
	

}
