/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.model.quest;

import net.sf.l2j.gameserver.instancemanager.QuestManager;

/**
 * @author Luis Arias
 *
 * Functions in this class are used in python files
 */
public class State
{
	/** Name of the quest */
    private String _questName;
    private String _name;


	/**
	 * Constructor for the state of the quest.
	 * @param name : String pointing out the name of the quest
	 * @param quest : Quest
	 */
    public State(String name, Quest quest)
    {
        _name = name;
        _questName = quest.getName();
		quest.addState(this);
    }

    // =========================================================
    // Method - Public
    /**
     * Add drop for the quest at this state of the quest
     * @param npcId : int designating the ID of the NPC
     * @param itemId : int designating the ID of the item dropped
     * @param chance : int designating the chance the item dropped
     * 
     * DEPRECATING THIS...only the itemId is really needed, and even 
     * that is only here for backwards compatibility
     */
    public void addQuestDrop(int npcId, int itemId, int chance) {
    	QuestManager.getInstance().getQuest(_questName).registerItem(itemId);
    }

    // =========================================================
    // Property

    /**
     * Return name of the quest
     * @return String
     */
    public String getName()
    {
        return _name;
    }

    /**
     * Return name of the quest
     * @return String
     */
    @Override
	public String toString() {
        return _name;
    }
}
