package net.sf.l2j.gameserver.model.quest;

import java.util.List;

import javolution.util.FastList;
import net.sf.l2j.gameserver.model.L2DropData;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author XaKa
 *
 */
public class QuestDropInfo
{
	public class DropInfo
	{
		public int	dropItemID;
		public int	dropItemObjID;
		public int	dropItemCount;

		/**
		 * Constructor of DropInfo that initialzes class variables
		 * @param itemID : int designating the ID of the item
		 * @param itemCount : int designating the quantity of items needed for quest
		 * @param itemObjID : int designating the ID of the item in the inventory of the player
		 */
		public DropInfo(int itemID, int itemCount, int itemObjID)
		{
			dropItemID		= itemID;
			dropItemObjID	= itemObjID;
			dropItemCount	= itemCount;
		}
	}
	
	public List<DropInfo>	 dropList;
	
	/**
	 * Add informations for dropped items in the inventory of the player.
	 * @param pcInstance : L2PcInstance designating the player
	 * @param questName : String designating the name of the quest
	 */
	public QuestDropInfo(L2PcInstance pcInstance, String questName)
	{
		// Get the QuestState and the State from the name of the quest
		QuestState	questState		= pcInstance.getQuestState(questName);
		dropList = new FastList<DropInfo>();
		if (questState == null)
		    return;

		if (questState.getDrops() != null)
        {
			for(List<L2DropData> questDrop : questState.getDrops().values())
			{
				// Get drops given by the mob
                if(questDrop == null)
                    continue;
                
                // Go through all drops of the mob 
    			for(L2DropData dropInfo : questDrop)
    			{
    				int dropID 		= dropInfo.getItemId();
    				int dropObjID	= 0;
    				int dropCount	= questState.getQuestItemsCount(dropID);
    				//If player doesn't have this quest item(doesn't kill need npc? or other) then skip it
    				if(pcInstance.getInventory().getItemByItemId(dropID) == null)
    					continue;
    				
    				dropObjID = pcInstance.getInventory().getItemByItemId(dropID).getObjectId();
    				// Add info for the dropped item in the player's inventory 
    				dropList.add(new DropInfo(dropID, dropCount, dropObjID));
    			}
            }
		}
	}
}
