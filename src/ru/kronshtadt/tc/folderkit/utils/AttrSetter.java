package ru.kronshtadt.tc.folderkit.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;

public class AttrSetter extends BaseUtil {

	public void set(String anItem) {
		item = anItem;
		String[] fields = item.split("\t");
		if (fields.length % 2 ==0) {
			addLog("Неверный формат строки");
			return;
	    }
		String objID = fields[0];
		List<AttrValue> attrs = new ArrayList<>();
		for (int i=1; i<fields.length; i+=2) {
			AttrValue av = new AttrValue();
			av.name = fields[i];
			av.value = fields[i+1];
			attrs.add(av);	
		}

		List<TCComponent> objs = getObjects(objID);
		if (objs.size() == 0)
			addLog(objID + " не найден");

		for (TCComponent c : objs) {
			applyAttrs(c, attrs);
		}

	}

	@SuppressWarnings("deprecation")
	private void applyAttrs(TCComponent c, List<AttrValue> attrs) {
		List<TCProperty> props = new ArrayList<>();
		try {			
			for (AttrValue av : attrs) {
				TCProperty pr = c.getTCProperty(av.name);
				if (!pr.isModifiable()) {
					addLog("Аттрибут "+av.name+" закрыт от изменений");
					break;
				}
				if (pr.isNotArray())
					applyValue(pr,av);
				else
					applyArrayValue(pr,av);
				props.add(pr);
			}
			TCProperty[] toSave = props.toArray(new TCProperty[0]);
			c.lock();
			c.setTCProperties(toSave);
			c.save();
			c.unlock();
		} catch (TCException e) {
			addLog(e.getError());
		}

	}

	private void applyArrayValue(TCProperty pr, AttrValue av) throws TCException {
		String[] vals = av.value.split(",");
		switch(pr.getPropertyType()) {
		case TCProperty.PROP_string:
			pr.setStringValueArray(vals);
			break;
		case TCProperty.PROP_int:
			try {
				int[] iVals = new int[vals.length];
				for (int i=0;i<vals.length;i++)
					iVals[i] = Integer.parseInt(vals[i]);
				pr.setIntValueArray(iVals);
			} catch (NumberFormatException nfe) {
				throw new TCException(av+nfe.getMessage());
			}
			break;
		case TCProperty.PROP_short:
			try {
				short[] iVals = new short[vals.length];
				for (int i=0;i<vals.length;i++)
					iVals[i] = Short.parseShort(vals[i]);
				pr.setShortValueArray(iVals);
			} catch (NumberFormatException nfe) {
				throw new TCException(av+nfe.getMessage());
			}
			break;
		case TCProperty.PROP_double:
			try {
				double[] dVals = new double[vals.length];
				for (int i=0;i<vals.length;i++)
					dVals[i] = Double.parseDouble(vals[i]);
				pr.setDoubleValueArray(dVals);
			} catch (NumberFormatException nfe) {
				throw new TCException(av+nfe.getMessage());
			}
			break;
		case TCProperty.PROP_logical:
			try {
				boolean[] bVals = new boolean[vals.length];
				for (int i=0;i<vals.length;i++)
					bVals[i] = Boolean.parseBoolean(vals[i]);
				pr.setLogicalValueArray(bVals);
			} catch (NumberFormatException nfe) {
				throw new TCException(av+nfe.getMessage());
			}
			break;
		default: throw new TCException(av.name+": тип свойства не поддерживается");
		}
	}

	private void applyValue(TCProperty pr, AttrValue av) throws TCException {
		switch(pr.getPropertyType()) {
		case TCProperty.PROP_string:
			pr.setStringValue(av.value);
			break;
		case TCProperty.PROP_int:
			try {
				int iVal = Integer.parseInt(av.value);
				pr.setIntValue(iVal);
			} catch (NumberFormatException nfe) {
				throw new TCException(av+nfe.getMessage());
			}
			break;
		case TCProperty.PROP_short:
			try {
				short iVal = Short.parseShort(av.value);
				pr.setShortValue(iVal);
			} catch (NumberFormatException nfe) {
				throw new TCException(av+nfe.getMessage());
			}
			break;
		case TCProperty.PROP_double:
			try {
				double dVal = Double.parseDouble(av.value);
				pr.setDoubleValue(dVal);
			} catch (NumberFormatException nfe) {
				throw new TCException(av+nfe.getMessage());
			}
			break;
		case TCProperty.PROP_logical:
			try {
				boolean bVal = Boolean.parseBoolean(av.value);
				pr.setLogicalValue(bVal);
			} catch (NumberFormatException nfe) {
				throw new TCException(av+nfe.getMessage());
			}
			break;
		case TCProperty.PROP_date:
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
			try {
				Date dVal = sdf.parse(av.value);
				pr.setDateValue(dVal);
			} catch (ParseException e) {
				throw new TCException(av+e.getMessage());
			}
		default: throw new TCException(av.name+": тип свойства не поддерживается");
		}
		
	}

	private List<TCComponent> getObjects(String objId) {
		List<TCComponent> res = new ArrayList<>();
		try {
			int revDivInd = objId.lastIndexOf('/');
			if (revDivInd == -1) {
				TCComponentItem[] items = itt.findItems(objId);
				if (items.length > 0)
					res.add(items[0]);
			} else {
				res = getRevs(objId.substring(0, revDivInd), objId.substring(revDivInd + 1));
			}
		} catch (TCException e) {
			addLog(e.getError());
		}
		return res;
	}

	private List<TCComponent> getRevs(String itemId, String revId) throws TCException {
		List<TCComponent> res = new ArrayList<>();
		if (revId.equals("*")) {
			TCComponentItem[] items = itt.findItems(itemId);
			if (items.length == 0)
				return res;
			TCComponent[] revs = items[0].getReferenceListProperty("revisions");
			res = Arrays.asList(revs);
		} else {
			TCComponentItemRevision[] revs = irt.findRevisions(itemId, revId);
			if (revs.length > 0)
				res.add(revs[0]);
		}

		return res;
	}

	public class AttrValue {
		public String name;
		public String value;
		public String toString() {
			return String.format("%s:%s ",name, value);
		}
	}

}
