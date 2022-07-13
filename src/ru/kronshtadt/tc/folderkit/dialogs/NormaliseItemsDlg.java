package ru.kronshtadt.tc.folderkit.dialogs;

import java.awt.Frame;

import javax.swing.ImageIcon;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.common.AbstractProgressDialog;
import com.teamcenter.rac.util.Registry;


public final class NormaliseItemsDlg  extends AbstractProgressDialog{

	private static final long serialVersionUID = -7186697592032702575L;
	private Registry registry;

	private AIFComponentContext[] target_comps;

	public NormaliseItemsDlg(AIFComponentContext aaifcomponentcontext[],
			Frame frame)
	{
		super(frame);
		target_comps = aaifcomponentcontext;
		initializeDialog();
	}

	private void initializeDialog() {
		setTitle("Нормализация объекта");
		setDisplaySuccessComponents(true);
		setCommandIcon(new ImageIcon(this.getClass().getResource("/icons/apply_markup_alllevels_16.png")));
		//setSuccessIcon(new ImageIcon(this.getClass().getResource("/icons/validation_16.png")));
		setConfirmationText("Нормализовать?");
		setShowParentFlag(false);
		setTCComponents(target_comps);	
		setConfirmationFlag(true);
	}

	@Override
	protected Registry getReg() {
		if (registry == null)
			registry = Registry.getRegistry(this);
		return registry;
	}

	@Override
	protected void getOperations(AIFComponentContext aContext) {
		try {
			addOperation(new NormailseItemOperation(aContext));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
