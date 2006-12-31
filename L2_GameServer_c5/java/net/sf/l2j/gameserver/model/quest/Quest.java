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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.NpcTable;
import net.sf.l2j.gameserver.instancemanager.QuestManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * @author Luis Arias
 *
 */
public abstract class Quest
{
	protected static Logger _log = Logger.getLogger(Quest.class.getName());

	/** HashMap containing events from String value of the event */
	private static Map<String, Quest> allEventsS = new FastMap<String, Quest>();

	private final int _questId;
	private final String _name;
	private final String _descr;
	private final boolean _party;
    private State initialState;
    private Map<String, State> states;
	
	/**
	 * Return collection view of the values contains in the allEventS
	 * @return Collection<Quest>
	 */
	public static Collection<Quest> findAllEvents() {
		return allEventsS.values();
	}
	
    /**
     * (Constructor)Add values to class variables and put the quest in HashMaps. 
     * @param questId : int pointing out the ID of the quest
     * @param name : String corresponding to the name of the quest
     * @param descr : String for the description of the quest
     */
	public Quest(int questId, String name, String descr)
	{
		this(questId, name, descr, false);
	}
	
	public Quest(int questId, String name, String descr, boolean party)
    {
		_questId = questId;
		_name = name;
		_descr = descr;
		_party = party;
        states = new FastMap<String, State>();
		if (questId != 0) {
            QuestManager.getInstance().getQuests().add(Quest.this);
		} else {
			allEventsS.put(name, this);
		}
    }
	
	/**
	 * Return ID of the quest
	 * @return int
	 */
	public int getQuestIntId() {
		return _questId;
	}
	
	/**
	 * Set the initial state of the quest with parameter "state"
	 * @param state
	 */
	public void setInitialState(State state) {
		this.initialState = state;
	}
	
	/**
	 * Add a new QuestState to the database and return it.
	 * @param player
	 * @return QuestState : QuestState created
	 */
	public QuestState newQuestState(L2PcInstance player) {
		QuestState qs = new QuestState(this, player, getInitialState(), false);
		Quest.createQuestInDb(qs);
		return qs;
	}
	
	/**
	 * Return initial state of the quest
	 * @return State
	 */
	public State getInitialState() {
		return initialState;
	}
    
	/**
	 * Return name of the quest
	 * @return String
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Return description of the quest
	 * @return String
	 */
	public String getDescr() {
		return _descr;
	}
	
	public boolean isParty()
	{
		return _party;
	}
    
	/**
	 * Add a state to the quest
	 * @param state
	 * @return state added
	 */
    public State addState(State state)
    {
        states.put(state.getName(), state);
		return state;
    }
    
    /**
     * Add the quest to the NPC's startQuest
     * @param npcId
     * @return L2NpcTemplate : Start NPC
     */
    public L2NpcTemplate addStartNpc(int npcId)
    {
		L2NpcTemplate t = NpcTable.getInstance().getTemplate(npcId);
		if (t != null) {
			t.addStartQuests(this);
		}
		return t;
    }
    
	// these are methods to call from java
    public final boolean notifyAttack(L2NpcInstance npc, QuestState qs) {
        String res = null;
        try { res = onAttack(npc, qs); } catch (Exception e) { return showError(qs, e); }
        return showResult(qs, res);
    } 
    public final boolean notifyDeath(L2NpcInstance npc, L2Character character, QuestState qs) {
        String res = null;
        try { res = onDeath(npc, character, qs); } catch (Exception e) { return showError(qs, e); }
        return showResult(qs, res);
    } 
    public final boolean notifyEvent(String event, QuestState qs) {
        String res = null;
        try { res = onEvent(event, qs); } catch (Exception e) { return showError(qs, e); }
        return showResult(qs, res);
    } 
	public final boolean notifyKill (L2NpcInstance npc, QuestState qs) {
		String res = null;
		try { res = onKill(npc, qs); } catch (Exception e) { return showError(qs, e); }
		return showResult(qs, res);
	}
	public final boolean notifyTalk (L2NpcInstance npc, QuestState qs) {
		String res = null;
		try { res = onTalk(npc, qs); } catch (Exception e) { return showError(qs, e); }
        qs.getPlayer().setLastQuestNpcObject(npc.getObjectId());
		return showResult(qs, res);
	}

	// these are methods that java calls to invoke scripts
    @SuppressWarnings("unused") public String onAttack(L2NpcInstance npc, QuestState qs) { return onEvent("", qs); } 
    @SuppressWarnings("unused") public String onDeath (L2NpcInstance npc, L2Character character, QuestState qs) { return onEvent("", qs); }
    @SuppressWarnings("unused") public String onEvent(String event, QuestState qs) { return null; } 
    @SuppressWarnings("unused") public String onKill (L2NpcInstance npc, QuestState qs) { return onEvent("", qs); }
    @SuppressWarnings("unused") public String onTalk (L2NpcInstance npc, QuestState qs) { return onEvent("", qs); }
	
	/**
	 * Show message error to player who has an access level greater than 0
	 * @param qs : QuestState
	 * @param t : Throwable
	 * @return boolean
	 */
	private boolean showError(QuestState qs, Throwable t) {
		_log.log(Level.WARNING, "", t);
		if (qs.getPlayer().getAccessLevel() > 0) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			pw.close();
			String res = "<html><body><title>Script error</title>"+sw.toString()+"</body></html>";
			return showResult(qs, res);
		}
		return false;
	}
	
	/**
	 * Show a message to player.<BR><BR>
	 * <U><I>Concept : </I></U><BR>
	 * 3 cases are managed according to the value of the parameter "res" :<BR>
	 * <LI><U>"res" ends with string ".html" :</U> an HTML is opened in order to be shown in a dialog box</LI>
	 * <LI><U>"res" starts with "<html>" :</U> the message hold in "res" is shown in a dialog box</LI>
	 * <LI><U>otherwise :</U> the message hold in "res" is shown in chat box</LI>
	 * @param qs : QuestState 
	 * @param res : String pointing out the message to show at the player
	 * @return boolean
	 */
	private boolean showResult(QuestState qs, String res) {
		if (res == null)
			return true;
		if (res.endsWith(".htm")) {
			qs.showHtmlFile(res);
		}
		else if (res.startsWith("<html>")) {
			NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
			npcReply.setHtml(res);
			qs.getPlayer().sendPacket(npcReply);
		}
		else {
			SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
			sm.addString(res);
			qs.getPlayer().sendPacket(sm);
		}
		return false;
	}
	
	/**
	 * Add quests to the L2PCInstance of the player.<BR><BR>
	 * <U><I>Action : </U></I><BR>
	 * Add state of quests, drops and variables for quests in the HashMap _quest of L2PcInstance
	 * @param player : Player who is entering the world
	 */
	public static void playerEnter(L2PcInstance player) {

        java.sql.Connection con = null;
        try
        {
	    // Get list of quests owned by the player from database
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;
            
            PreparedStatement invalidQuestData      = con.prepareStatement("DELETE FROM character_quests WHERE char_id=? and name=?");
            PreparedStatement invalidQuestDataVar   = con.prepareStatement("delete FROM character_quests WHERE char_id=? and name=? and var=?");
            
            statement = con.prepareStatement("SELECT name,value FROM character_quests WHERE char_id=? AND var=?");
            statement.setInt(1, player.getObjectId());
            statement.setString(2, "<state>");
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				
				// Get ID of the quest and ID of its state
				String questId = rs.getString("name");
				String stateId = rs.getString("value");
				
				// Search quest associated with the ID
				Quest q = QuestManager.getInstance().getQuest(questId);
				if (q == null) {
					_log.finer("Unknown quest "+questId+" for player "+player.getName());
					if (Config.AUTODELETE_INVALID_QUEST_DATA){
                        invalidQuestData.setInt(1, player.getObjectId());
                        invalidQuestData.setString(2, questId);
                        invalidQuestData.executeUpdate();
					}
					continue;
				}
				
				// Identify the state of the quest for the player
				boolean completed = false;
				if (stateId.length() > 0 && stateId.charAt(0) == '*') { // probably obsolete check 
					completed = true;
					stateId = stateId.substring(1);
				}
				if(stateId.equals("Completed")) completed = true;
				
				// Create an object State containing the state of the quest
				State state = q.states.get(stateId);
				if (state == null) {
					_log.finer("Unknown state "+state+" in quest "+questId+" for player "+player.getName());
					if (Config.AUTODELETE_INVALID_QUEST_DATA){
					    invalidQuestData.setInt(1, player.getObjectId());
                        invalidQuestData.setString(2, questId);
                        invalidQuestData.executeUpdate();
					}
					continue;
				}
				// Create a new QuestState for the player that will be added to the player's list of quests
				new QuestState(q, player, state, completed);
			}
			rs.close();
            invalidQuestData.close();
            statement.close();

            // Get list of quests owned by the player from the DB in order to add variables used in the quest.
            statement = con.prepareStatement("SELECT name,var,value FROM character_quests WHERE char_id=?");
            statement.setInt(1,player.getObjectId());
			rs = statement.executeQuery();
			while (rs.next()) {
				String questId = rs.getString("name");
				String var     = rs.getString("var");
				String value   = rs.getString("value");
				// Get the QuestState saved in the loop before
				QuestState qs = player.getQuestState(questId);
				if (qs == null) {
					_log.finer("Lost variable "+var+" in quest "+questId+" for player "+player.getName());
					if (Config.AUTODELETE_INVALID_QUEST_DATA){
					    invalidQuestDataVar.setInt   (1,player.getObjectId());
                        invalidQuestDataVar.setString(2,questId);
                        invalidQuestDataVar.setString(3,var);
                        invalidQuestDataVar.executeUpdate();
					}
					continue;
				}
				// Add parameter to the quest
				qs.setInternal(var, value);
			}
			rs.close();
            invalidQuestDataVar.close();
            statement.close();
			
		} catch (Exception e) {
			_log.log(Level.WARNING, "could not insert char quest:", e);
        } finally {
            try { con.close(); } catch (Exception e) {}
        }
		
		// events
		for (String name : allEventsS.keySet()) {
			player.processQuestEvent(name, "enter");
		}
	}


	/**
	 * Insert in the database the quest for the player.
	 * @param qs : QuestState pointing out the state of the quest
	 * @param var : String designating the name of the variable for the quest
	 * @param value : String designating the value of the variable for the quest
	 */
	public static void createQuestVarInDb(QuestState qs, String var, String value) {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;
            statement = con.prepareStatement("INSERT INTO character_quests (char_id,name,var,value) VALUES (?,?,?,?)");
            statement.setInt   (1, qs.getPlayer().getObjectId());
            statement.setString(2, qs.getQuest().getName());
            statement.setString(3, var);
            statement.setString(4, value);
	    statement.executeUpdate();
            statement.close();
        } catch (Exception e) {
			_log.log(Level.WARNING, "could not insert char quest:", e);
        } finally {
            try { con.close(); } catch (Exception e) {}
        }
	}
	
	/**
	 * Update the value of the variable "var" for the quest.<BR><BR>
	 * <U><I>Actions :</I></U><BR>
	 * The selection of the right record is made with :
	 * <LI>char_id = qs.getPlayer().getObjectID()</LI>
	 * <LI>name = qs.getQuest().getName()</LI>
	 * <LI>var = var</LI>
	 * <BR><BR>
	 * The modification made is :
	 * <LI>value = parameter value</LI>
	 * @param qs : Quest State
	 * @param var : String designating the name of the variable for quest
	 * @param value : String designating the value of the variable for quest
	 */
    public static void updateQuestVarInDb(QuestState qs, String var, String value) {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;
            statement = con.prepareStatement("UPDATE character_quests SET value=? WHERE char_id=? AND name=? AND var = ?");
            statement.setString(1, value);
            statement.setInt   (2, qs.getPlayer().getObjectId());
            statement.setString(3, qs.getQuest().getName());
            statement.setString(4, var);
			statement.executeUpdate();
            statement.close();
        } catch (Exception e) {
			_log.log(Level.WARNING, "could not update char quest:", e);
        } finally {
            try { con.close(); } catch (Exception e) {}
        }
	}
	
    /**
     * Delete a variable of player's quest from the database.
     * @param qs : object QuestState pointing out the player's quest
     * @param var : String designating the variable characterizing the quest
     */
	public static void deleteQuestVarInDb(QuestState qs, String var) {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;
            statement = con.prepareStatement("DELETE FROM character_quests WHERE char_id=? AND name=? AND var=?");
            statement.setInt   (1, qs.getPlayer().getObjectId());
            statement.setString(2, qs.getQuest().getName());
            statement.setString(3, var);
	    statement.executeUpdate();
            statement.close();
        } catch (Exception e) {
			_log.log(Level.WARNING, "could not delete char quest:", e);
        } finally {
            try { con.close(); } catch (Exception e) {}
        }
	}
	
	/**
	 * Delete the player's quest from database.
	 * @param qs : QuestState pointing out the player's quest
	 */
	public static void deleteQuestInDb(QuestState qs) {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;
            statement = con.prepareStatement("DELETE FROM character_quests WHERE char_id=? AND name=?");
            statement.setInt   (1, qs.getPlayer().getObjectId());
            statement.setString(2, qs.getQuest().getName());
			statement.executeUpdate();
            statement.close();
        } catch (Exception e) {
			_log.log(Level.WARNING, "could not delete char quest:", e);
        } finally {
            try { con.close(); } catch (Exception e) {}
        }
	}
	
	/**
	 * Create a record in database for quest.<BR><BR>
	 * <U><I>Actions :</I></U><BR>
	 * Use fucntion createQuestVarInDb() with following parameters :<BR>
	 * <LI>QuestState : parameter sq that puts in fields of database :
	 * 	 <UL type="square">
	 *     <LI>char_id : ID of the player</LI>
	 *     <LI>name : name of the quest</LI>
	 *   </UL>
	 * </LI>
	 * <LI>var : string "&lt;state&gt;" as the name of the variable for the quest</LI>
	 * <LI>val : string corresponding at the ID of the state (in fact, initial state)</LI>
	 * @param qs : QuestState
	 */
	public static void createQuestInDb(QuestState qs) {
		createQuestVarInDb(qs, "<state>", qs.getStateId());
	}
	
	/**
	 * Update informations regarding quest in database.<BR>
	 * <U><I>Actions :</I></U><BR>
	 * <LI>Get ID state of the quest recorded in object qs</LI>
	 * <LI>Test if quest is completed. If true, add a star (*) before the ID state</LI>
	 * <LI>Save in database the ID state (with or without the star) for the variable called "&lt;state&gt;" of the quest</LI>
	 * @param qs : QuestState
	 */
	public static void updateQuestInDb(QuestState qs) {
		String val = qs.getStateId();
		//if (qs.isCompleted())
		//	val = "*" + val;
		updateQuestVarInDb(qs, "<state>", val);
	}
	
}
