package ru.kronshtadt.tc.folderkit.utils;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

public class ItemRenamer {
	private static final int FULL = 0;
	private static final int NAME = 1;
	protected TCComponentItem item;
	protected TCSession session;
	protected String oldId;
	protected String id;
	protected String name;
	protected String desc;
	protected TCComponent parent;
	private int mode = FULL;

	public ItemRenamer(TCComponentItem item) throws TCException {
		this.item = item;
		oldId = item.getStringProperty("item_id");
		session = item.getSession();
	}

	@SuppressWarnings("deprecation")
	public boolean isExists(String newId) {
		boolean res = true;
		if (oldId.equals(newId))
			return false;
		try {
			TCComponentItemType itt = (TCComponentItemType) session.getTypeComponent("Item");
			TCComponentItem itm = itt.find(newId);
			if (itm == null)
				res = false;
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}

	public boolean hasStatus() {
		boolean res = false;
		try {
			TCComponentItemRevision[] revs = item.getReleasedItemRevisions();
			if (revs != null && revs.length > 0)
				res = true;
		} catch (TCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}

	public void rename(String newId, String newName, String newDesc) throws TCException {
		id = newId;
		name = newName;
		desc = newDesc;
		if (oldId.equals(id))
			mode = NAME;
		renameItem();
	}

	protected void renameItem() throws TCException {

		renameComponent(item);
		if (mode == FULL) {
			parent = item;
			item.setStringProperty("item_id", id);
			TCComponent mForm = item.getRelatedComponent("IMAN_master_form");
			updateComponent(mForm);
			updateComponentArray(item, "bom_view_tags");
		}

		TCComponentItemRevision[] revs = item.getWorkingItemRevisions();
		for (TCComponentItemRevision rev : revs) {
			renameRev(rev);
		}
	}

	protected void renameRev(TCComponentItemRevision rev) throws TCException {
		renameComponent(rev);
		if (mode == FULL) {
			parent = rev;
			TCComponent mForm = rev.getRelatedComponent("IMAN_master_form_rev");
			updateComponent(mForm);
			updateComponentArray(rev, "structure_revisions");
			updateComponentArray(rev, "IMAN_specification");
			updateComponentArray(rev, "IMAN_Rendering");
			updateComponentArray(rev, "IMAN_UG_altrep");
		}

	}

	private void renameComponent(TCComponent c) throws TCException {
		c.setStringProperty("object_name", name);
		c.setStringProperty("object_desc", desc);
	}

	protected void updateComponent(TCComponent c) throws TCException {
		String compName = makeComponentName(c);
		c.setStringProperty("object_name", compName);
		if ("UGMASTER".equals(c.getType())) {
			updateFile(c);
		}
	}

	protected String makeComponentName(TCComponent c) throws TCException {
		String compName = c.getStringProperty("object_name");
		compName = compName.replace(oldId, id);
		return compName;
	}

	private void updateFile(TCComponent c) throws TCException {
		TCComponentDataset ug = (TCComponentDataset) c;
		TCComponentTcFile[] files = ug.getTcFiles();
		String fileName = c.getStringProperty("object_name").replace('.', '_').replace('/', '_').concat(".prt");
		for (TCComponentTcFile file : files) {
			String origName = file.getStringProperty(TCComponentTcFile.PROP_TCFILE_NAME);
			if (origName.endsWith(".prt")) {
				file.setStringProperty(TCComponentTcFile.PROP_TCFILE_NAME, fileName);
				break;
			}
		}
	}

	protected void updateComponentArray(TCComponent c, String attrName) throws TCException {
		TCComponent[] bvs = c.getReferenceListProperty(attrName);
		if (bvs == null)
			return;
		for (TCComponent bv : bvs) {
			updateComponent(bv);
		}
	}

}
