package net.sf.l2j.gameserver.model.quest.jython;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.quest.Quest;

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;

public abstract class QuestJython extends Quest {

	/**
	 * Initialize the engine for scripts of quests, luxury shops and blacksmith
	 */
	public static void init() {
		try
		{
			// Initialize the engine for loading Jython scripts
			BSFManager bsf = new BSFManager();
			// Execution of all the scripts placed in data/jscript
			// inside the DataPack directory

			String dataPackDirForwardSlashes = Config.DATAPACK_ROOT.getPath().replaceAll("\\\\","/");
			String loadingScript = 
			    "import sys;"
			  + "sys.path.insert(0,'" + dataPackDirForwardSlashes + "');"
			  + "import data";

			bsf.exec("jython", "quest", 0, 0, loadingScript);
		} catch (BSFException e) { e.printStackTrace(); }
	}

	/**
	 * Constructor used in jython files.
	 * @param questId : int designating the ID of the quest
	 * @param name : String designating the name of the quest
	 * @param descr : String designating the description of the quest
	 */
	public QuestJython(int questId, String name, String descr) {
		super(questId, name, descr);
	}
}
