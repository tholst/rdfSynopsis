package rdfsynopsis.eval;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.log4j.Logger;

public abstract class AbstractEvaluator {

	protected String title;
	protected Logger logger;
	protected boolean timeStamp;
	
	public AbstractEvaluator(String title) {
		this(title,true);
	}
	
	public AbstractEvaluator(String title, boolean timeStamp) {
		super();
		this.title = title;
		this.timeStamp = timeStamp;
	}
	
	public static File makeTitleDir(String title, File outDir, boolean timestamp) {
		Logger logger = Logger.getLogger(AbstractEvaluator.class);
		try {
			String timeStamp = new SimpleDateFormat("yyyy-MM-dd__HH-mm-ss")
					.format(Calendar.getInstance().getTime());
			String dirName = title;
			if (timestamp)
				dirName +=  "__@" + timeStamp;
			dirName += File.separator;
			File titleDir = new File(outDir, dirName);
			if (!titleDir.mkdirs()) {
				logger.error(
						"Directory could not be created: " + titleDir.getPath());
				return null;
			}
			logger.debug("Directory created: "+titleDir.getPath());
			return titleDir;
		} catch (Exception e) {
			logger.error(e);
		}
		return null;
	}
	
	public static File makeTitleFile(String title, File outDir, String suffix, boolean timestamp) {
		Logger logger = Logger.getLogger(AbstractEvaluator.class);
		try {
			String timeStamp = new SimpleDateFormat("yyyy-MM-dd__HH-mm-ss")
					.format(Calendar.getInstance().getTime());
			String fileName = title;
			if (timestamp)
				fileName += "__@" + timeStamp; 
			fileName += suffix;
			File titleFile = new File(outDir, fileName);
			logger.debug("Trying to create file: "+titleFile.getPath());
			if (!titleFile.createNewFile()) {
				logger.error(
						"File could not be created: " + titleFile.getPath());
				return null;
			}
			logger.debug("File created: "+titleFile.getPath());
			return titleFile;
		} catch (Exception e) {
			logger.error(e);
		}
		return null;
	}


	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractEvaluator other = (AbstractEvaluator) obj;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}


	public void evaluate(File outDir) {
		evaluate(outDir, "");
	}
	public abstract void evaluate(File outDir, String titleAddition);
}
