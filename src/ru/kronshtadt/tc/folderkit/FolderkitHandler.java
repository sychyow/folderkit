package ru.kronshtadt.tc.folderkit;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.util.MessageBox;

/**
 * Common hander for all the commands in the folderkit package
 * 
 */
public class FolderkitHandler extends AbstractHandler
{
	/**
	 * The constructor.
	 */
	public FolderkitHandler()
	{
	}
	
	

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		String CmdID = event.getCommand().getId();
		/*
		 * //Just leaved it here as a sample of using a SWT component instead of
		 * Swing IWorkbenchWindow window =
		 * HandlerUtil.getActiveWorkbenchWindowChecked(event);
		 * MessageDialog.openInformation( window.getShell(), "Folderkit",
		 * CmdID);
		 */
		try
		{
			//putStats("FolderKit");
			(new FolderkitCommand(AIFUtility.getCurrentApplication().getTargetContexts(), AIFUtility
					.getActiveDesktop(), CmdID)).executeModal();
			
		} catch (Exception e)
		{
			MessageBox.post(e);
		}
		return null;
	}
} 