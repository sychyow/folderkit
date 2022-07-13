package ru.kronshtadt.tc.folderkit.utils;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateOrUpdateRelationsInfo;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateOrUpdateRelationsResponse;
import com.teamcenter.services.rac.core._2008_06.DataManagement.SecondaryData;
import com.teamcenter.soa.client.model.ErrorStack;

public class ItemLinker extends BaseUtil {
	public static final int MODE_CHECK = 1;
	public static final int MODE_CREATE = 2;
	public static final int MODE_DELETE = 3;
	private int mode;
	

	public ItemLinker(int aMode) {
		super();
		mode = aMode;
	}
	
	public void link(String anItem) {
		item = anItem;
		try {
			switch (mode) {
			case MODE_CHECK:
				check();
				break;
			case MODE_CREATE:
				create();
				break;
			case MODE_DELETE:
				delete();
				break;
			}
		} catch (TCException e) {
			addLog(item +"\t"+ e.getError());
		}

	}

	private void delete() throws TCException {
		final String[] fields = item.split("\t");
		if (fields.length != 3) {
			addLog("неверное количество элементов");
			return;
		}
		TCComponentItemRevision rev1 = getRev(fields[0]);
		if (rev1==null) {
			addLog("Ревизия "+fields[0]+" не найдена");
			return;
		}
		TCComponentItemRevision rev2 = getRev(fields[1]);
		if (rev2==null) {
			addLog("Ревизия "+fields[1]+" не найдена");
			return;
		}
		final TCComponent[] comps = rev1.getReferenceListProperty(fields[2]);
		if (comps==null) {
			addLog("Отношение "+fields[2]+" не найдено");
			return;
		}
		for (TCComponent c:comps) {
			if (c.equals(rev2)) {
				rev1.remove(fields[2], c);
				return;
			}
		}
	}

	private void create() throws TCException {
		final String[] fields = item.split("\t");
		if (fields.length != 3) {
			addLog("неверное количество элементов");
			return;
		}
		TCComponentItemRevision rev1 = getRev(fields[0]);
		if (rev1==null) {
			addLog("Ревизия "+fields[0]+" не найдена");
			return;
		}
		TCComponentItemRevision rev2 = getRev(fields[1]);
		if (rev2==null) {
			addLog("Ревизия "+fields[1]+" не найдена");
			return;
		}
		
		final CreateOrUpdateRelationsInfo ri = new CreateOrUpdateRelationsInfo();
		ri.primaryObject = rev1;
		ri.secondaryData = new SecondaryData[1];
		ri.secondaryData[0] = new SecondaryData();
		ri.secondaryData[0].secondary = rev2;
		ri.relationType = fields[2];
		final CreateOrUpdateRelationsResponse res = getDMS().createOrUpdateRelations(new CreateOrUpdateRelationsInfo[]{ri}, false);
		
		if (res.serviceData.sizeOfPartialErrors()>0) {
			ErrorStack err = res.serviceData.getPartialError(0);
			throw new TCException(err.getErrorValues()[0].getMessage());
		}

	}

	private void check() throws TCException {
		final String[] fields = item.split("\t");
		if (fields.length != 3) {
			addLog("неверное количество элементов");
			return;
		}
		TCComponentItemRevision rev1 = getRev(fields[0]);
		if (rev1==null) {
			addLog("Ревизия "+fields[0]+" не найдена");
			return;
		}
		TCComponentItemRevision rev2 = getRev(fields[1]);
		if (rev2==null) {
			addLog("Ревизия "+fields[1]+" не найдена");
			return;
		}
		final TCComponent[] comps = rev1.getReferenceListProperty(fields[2]);
		if (comps==null) {
			addLog("Отношение "+fields[2]+" не найдено");
			return;
		}
		for (TCComponent c:comps) {
			if (c.equals(rev2)) {
				addLog("Объекты связаны");
				return;
			}
		}
		addLog("Связь отсутствует");
	}
	
	TCComponentItemRevision getRev(String revId) throws TCException {
		int revDivInd = revId.lastIndexOf('/');
		TCComponentItemRevision newRev = null;
		if (revDivInd != -1) {
			TCComponentItemRevision[] revs = irt.findRevisions(revId.substring(0, revDivInd),
					revId.substring(revDivInd + 1));
			if (revs.length > 0)
				newRev = revs[0];
		}
		return newRev;
	}

}
