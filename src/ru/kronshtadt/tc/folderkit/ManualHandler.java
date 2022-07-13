package ru.kronshtadt.tc.folderkit;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class ManualHandler extends AbstractHandler {

	private static final String MANUAL_PATH = "x:\\Siemens\\Информация\\Инструкции\\Teamcenter\\";

	public ManualHandler() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		if (Desktop.isDesktopSupported()) {
			try {
				File dir = new File(MANUAL_PATH);
				File[] files = dir.listFiles();
				List<File> manuals = new ArrayList<>();
				if (files != null) {
					for (File f : files) {
						if (f.getName().startsWith("РП Утилиты папок")) {
							manuals.add(f);
						}
					}
					Collections.sort(manuals, new Comparator<File>() {
						  @Override
						  public int compare(File f1, File f2) {
						    return f2.getName().compareTo(f1.getName());
						  }
						});
					if (manuals.size()>0)
						Desktop.getDesktop().open(manuals.get(0));
				}

			} catch (IOException ex) {
				// no application registered for PDFs
				ex.printStackTrace();
			}
		}
		return null;
	}

}
