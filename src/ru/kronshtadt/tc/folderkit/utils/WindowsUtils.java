package ru.kronshtadt.tc.folderkit.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * Class to get a path to desktop folder. The source is taken from
 * http://www.rgagnon.com/javadetails/java-0652.html as is.
 * 
 * @author Real's HowTo
 * 
 */
public class WindowsUtils
{

	private static final String REGQUERY_UTIL = "reg query ";
	private static final String REGSTR_TOKEN = "REG_SZ";
	private static final String DESKTOP_FOLDER_CMD = REGQUERY_UTIL
			+ "\"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\"
			+ "Explorer\\Shell Folders\" /v DESKTOP";

	private WindowsUtils()
	{
	}

	public static String getCurrentUserDesktopPath()
	{
		try
		{
			final Process process = Runtime.getRuntime().exec(
					DESKTOP_FOLDER_CMD);
			final StreamReader reader = new StreamReader(process
					.getInputStream());
			reader.start();
			process.waitFor();
			reader.join();
			final String result = reader.getResult();
			final int tokenIndex = result.indexOf(REGSTR_TOKEN);
			if (tokenIndex == -1)
			{
				return null;
			}
			return result.substring(tokenIndex + REGSTR_TOKEN.length()).trim();
		} catch (final Exception e)
		{
			return null;
		}
	}

	public static class StreamReader extends Thread
	{

		private final InputStream inputStream;
		private final StringWriter stringWriter;

		public StreamReader(final InputStream is)
		{
			super();
			this.inputStream = is;
			stringWriter = new StringWriter();
		}

		public String getResult()
		{
			return stringWriter.toString();
		}

		@Override
		public void run()
		{
			try
			{
				int c;
				while ((c = inputStream.read()) != -1)
				{
					stringWriter.write(c);
				}
			} catch (final IOException e)
			{
				// Do nothing
			}
		}
	}
}
