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

import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.model.L2DropData;

/**
 * @author Luis Arias
 *
 * Functions in this class are used in python files 
 */
public class State
{
	// TODO - Begins
	/** Prototype of empty String list */
	private static final String[] emptyStrList = new String[0];
	/** Prototype of empty int list */
	private static final int[] emptyIntList = new int[0];
	// TODO - End
	
	/** Name of the quest */
    /** Quest object associated to the state */
	private final Quest _quest;
	private Map<Integer, List<L2DropData>> _Drops;
	private String[] _Events = emptyStrList;
    private String _Name;
    private int[] _AttackIds = emptyIntList;
	private int[] _KillIds = emptyIntList;
	private int[] _TalkIds = emptyIntList;


	/**
	 * Constructor for the state of the quest. 
	 * @param name : String pointing out the name of the quest
	 * @param quest : Quest
	 */
    public State(String name, Quest quest)
    {
        _Name = name;
		this._quest = quest;
		quest.addState(this);
    }
    
    // =========================================================
    // Method - Public
    /**
     * Add mob to be attacked in the list at start of the quest.<BR><BR>
     * @param attackId
     * @return int : attackId
     */
    public int addAttackId(int attackId) {
        _AttackIds = addToIntArray(_AttackIds, attackId);
        return attackId;
    }
    
	/**
	 * Add mob to be killed in the list at start of the quest.<BR><BR>
	 * @param killId
	 * @return int : killId
	 */
	public int addKillId(int killId) {
        _KillIds = addToIntArray(_KillIds, killId);
		return killId;
	}
    
    /**
     * Add drop for the quest at this state of the quest 
     * @param npcId : int designating the ID of the NPC
     * @param itemId : int designating the ID of the item dropped
     * @param chance : int designating the chance the item dropped 
     */
    public void addQuestDrop(int npcId, int itemId, int chance) {
        try {
            if (_Drops == null)
                _Drops = new FastMap<Integer, List<L2DropData>>();
            L2DropData d = new L2DropData();
            d.setItemId((short)itemId);
            d.setChance(chance);
            d.setQuestID(_quest.getName());
            d.addStates(new String[]{_Name});
            List<L2DropData> lst = _Drops.get(npcId);
            if (lst != null) {
                lst.add(d);
            } else {
                lst = new FastList<L2DropData>();
                lst.add(d);
                _Drops.put(npcId, lst);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Add NPC in the list of NPC to be talked
     * @param talkId : ID of the NPC
     * @return int : ID of the NPC
     */
    public int addTalkId(int talkId) {
        _TalkIds = addToIntArray(_TalkIds, talkId);
        return talkId;
    }
    
    // =========================================================
    // Method - Private
    private int[] addToIntArray(int[] intArray, int value) {
        int len = intArray.length;
        int[] tmp = new int[len+1];
        for (int i=0; i < len; i++)
            tmp[i] = intArray[i];
        tmp[len] = value;
        return tmp;
    }
    
    // =========================================================
    // Proeprty
    /**
     * Return table of all NPC's ID to attack for quest.
     * @return int[]
     */
    int[] getAttackIds() {
        return _AttackIds;
    }

    /**
     * Return list of drops at this step/state of the quest.
     * @return HashMap
     */
    Map<Integer, List<L2DropData>> getDrops() {
        return _Drops;
    }
    
    /**
     * Return list of events
     * @return String[]
     */
    String[] getEvents() {
        return _Events;
    }

    /**
     * Return table of all NPC's ID to kill for quest.
     * @return int[]
     */
    int[] getKillIds() {
        return _KillIds;
    }
    
    /**
     * Return name of the quest
     * @return String
     */
    public String getName()
    {
        return _Name;
    }
	
	/**
	 * Return IDs of NPC to be talked for the quest
	 * @return int[]
	 */
	int[] getTalkIds() {
		return _TalkIds;
	}
    
    /**
     * Return name of the quest
     * @return String
     */
    public String toString() {
        return _Name;
    }

    public Quest getQuest()
    {
        return _quest;
    }
}
