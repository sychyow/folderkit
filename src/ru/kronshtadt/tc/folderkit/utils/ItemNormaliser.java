package ru.kronshtadt.tc.folderkit.utils;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;

public class ItemNormaliser extends ItemRenamer {

	public ItemNormaliser(TCComponentItem item) throws TCException {
		super(item);
		name = item.getStringProperty("object_name");
		desc = item.getStringProperty("object_name");
		id = oldId;
	}
	
	public void normalise() throws TCException	{
		parent = item;
		TCComponent mForm = item.getRelatedComponent("IMAN_master_form");
		updateComponent(mForm);
		updateComponentArray(item, "bom_view_tags");
		TCComponent[] revs = item.getReferenceListProperty("revision_list");
		for (TCComponent rev:revs) {
			renameRev((TCComponentItemRevision) rev);
		}
	}

	@Override
	protected String makeComponentName(TCComponent c) throws TCException {
		NormalNameGenerator nng = new NormalNameGenerator(c);
		return nng.getName(parent);
		/*String compName = "";
		String baseId;
		if (parent instanceof TCComponentItem) {
			baseId = parent.getStringProperty("item_id");
		} else {
			String itemId = parent.getStringProperty("item_id");
			String revId = parent.getStringProperty("item_revision_id");
			baseId = String.format("%s/%s", itemId, revId);
		}
		String compType = c.getType();
		if (compType.startsWith("BOM"))
			compName = String.format("%s-Состав", baseId);
		else
			compName = baseId;
		return compName;
		*/
	}
	
	

}
