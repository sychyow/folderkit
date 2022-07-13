package ru.kronshtadt.tc.folderkit.dialogs;

import java.awt.Frame;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.ButtonLayout;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.PropertyLayout;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.VerticalLayout;

import ru.kronshtadt.tc.folderkit.utils.ItemRenamer;

public final class RenameItemDlg extends AbstractAIFDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5236788500122555910L;
	private static final int FIELD_LEN = 32;
	private TCComponentItem item;
	private JTextField tfItemId;
	private JTextField tfItemName;
	private JTextField tfItemDesc;
	

	RenameItemDlg(Frame frame, TCComponentItem inputItem){
		super(frame);
		item = inputItem;
		initDialog();
	}

	public RenameItemDlg(AIFComponentContext[] ccs, Frame parent) {
		super(parent);
		item = (TCComponentItem) ccs[0].getComponent();
		initDialog();
	}

	private void initDialog() {
		JPanel parentPanel = new JPanel(new VerticalLayout(5,2,2,2,2));
		this.setTitle("Переименование");
		this.getContentPane().add(parentPanel);
		parentPanel.add("top", getPropsPanel());
		parentPanel.add("top", getButtonPanel(false));
		fillText();
	}
	
	private void setProp(JTextField field, String prop) throws TCException {
		String strVal = item.getStringProperty(prop);
		field.setText(strVal);
		if (strVal.length()<=FIELD_LEN)
			field.setColumns(FIELD_LEN);
	}
	
	private void fillText() {
		try {
			setProp(tfItemId, "item_id");
			setProp(tfItemName, "object_name");
			setProp(tfItemDesc, "object_desc");
		} catch (TCException e) {
			e.printStackTrace();
			tfItemId.setText(e.getLocalizedMessage());
		}		
	}

	private JPanel getPropsPanel() {
		JPanel propsPanel = new JPanel(new PropertyLayout());
		tfItemId = new JTextField();
		tfItemName = new JTextField();
		tfItemDesc = new JTextField();
		propsPanel.add("1.1", new JLabel("Идентификатор"));
		propsPanel.add("1.2", tfItemId);
		propsPanel.add("2.1", new JLabel("Наименование"));
		propsPanel.add("2.2", tfItemName);
		propsPanel.add("3.1", new JLabel("Описание"));
		propsPanel.add("3.2", tfItemDesc);
		return propsPanel;
	}
	
	
	

	@Override
	protected JPanel getButtonPanel(final boolean hasApplyButton) {
		    Registry registry = Registry.getRegistry(this);
		    JPanel jPanel = new JPanel((LayoutManager)new ButtonLayout());
		    final JButton okButton = new JButton(registry.getString("ok"));
		    final JButton applyButton = new JButton(registry.getString("apply"));
		    JButton jButton3 = new JButton(registry.getString("close"));
      	    jPanel.add(okButton);
		    if (hasApplyButton) {
		        jPanel.add(applyButton); 
		    } 
		    jPanel.add(jButton3);
		    okButton.setEnabled(true);
		    
		    okButton.addActionListener(new ActionListener() {
		          public void actionPerformed(ActionEvent param1ActionEvent) {
		        	  okAction();
		          }
		        });
		    if (hasApplyButton)
		      applyButton.addActionListener(new ActionListener() {
		            public void actionPerformed(ActionEvent param1ActionEvent) {
		              applyAction();
		              okApplyAction();
		            }
		          }); 
		    jButton3.addActionListener(new ActionListener() {
		          public void actionPerformed(ActionEvent param1ActionEvent) {
		        	setVisible(false);
		            dispose();
		          }
		        });
	 
		    return jPanel;
	}

	@Override
	protected void okAction() {
		try {
			ItemRenamer renamer = new ItemRenamer(item);
			String newId = tfItemId.getText();
			if (renamer.isExists(newId)) {
				MessageBox.post("Этот идентификатор уже используется!", "Ошибка", MessageBox.ERROR);
				return;
			}
			if (renamer.hasStatus()) {
				MessageBox.post("Нельзя переименовывать: есть выпущенные ревизии!", "Ошибка", MessageBox.ERROR);
				return;
			}
			renamer.rename(newId, tfItemName.getText(), tfItemDesc.getText());
		} catch (TCException e) {
			MessageBox.post(e);
			return;
		}
		dispose();
	}
	
	
}
