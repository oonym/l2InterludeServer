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
package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.HennaTable;
import net.sf.l2j.gameserver.ItemTable;
import net.sf.l2j.gameserver.SkillTable;
import net.sf.l2j.gameserver.SkillTreeTable;
import net.sf.l2j.gameserver.clientpackets.Say2;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2HennaInstance;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2SkillLearn;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.base.SubClass;
import net.sf.l2j.gameserver.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.templates.L2Henna;

/**
 * Admin Subclass Restoration
 * 
 * 1. Populate the MatrixList with all Matrixies of each subclassed player.
 * 2. Purge all subclass assets.
 * 3. Write all subclasses to database from each PlayersMatrix.
 * 4. Re-skill every subclass in each PlayersMatrix.
 * 5. Write all final skills into database.
 * 6. Reset all players to log into their original baseclass incase
 * 	  they logged off from a subclass that now could not be restored.
 * 
 * @author Yesod
 */
public class AdminSubClass implements IAdminCommandHandler
{
    private static final int REQUIRED_LEVEL = 100;
    public static final String[] ADMIN_SUBCLASS_COMMANDS =
    {
     "admin_subclass_repair", "admin_subclass_henna_repair"
    };
    
    private static final String GET_SUBCLASSES = "SELECT character_subclasses.char_obj_id, character_subclasses.class_id, character_subclasses.exp, character_subclasses.sp, character_subclasses.level, characters.char_name FROM character_subclasses , characters WHERE character_subclasses.char_obj_id = characters.obj_Id ORDER BY character_subclasses.char_obj_id ASC";
    private static final String GET_CHAR_HENNAS = "SELECT char_obj_id, symbol_id FROM character_hennas WHERE class_index > 0";
    private static final String GET_DYE_COUNT = "SELECT count FROM items WHERE owner_id=? AND item_id=?";
    
    private static final String EMPTY_CHARACTER_SUBCLASS = "DELETE FROM character_subclasses";
    private static final String EMPTY_CHARACTER_EFFECTS  = "DELETE FROM character_skills_save";
    
    private static final String DELETE_SUBCLASS_SKILLS = "DELETE FROM character_skills WHERE class_index > 0";
    private static final String DELETE_SUBCLASS_SHORTCUTS = "DELETE FROM character_shortcuts WHERE class_index > 0";
    private static final String DELETE_SUBCLASS_QUESTS = "DELETE FROM character_quests WHERE class_index > 0";
    private static final String DELETE_SUBCLASS_HENNA = "DELETE FROM character_hennas WHERE class_index > 0";
    
    private static final String ADD_SUBCLASS = "INSERT INTO character_subclasses (char_obj_id,class_id,exp,sp,level,class_index) VALUES (?,?,?,?,?,?)";    
    private static final String ADD_SUBCLASS_SKILL = "INSERT INTO character_skills (char_obj_id,skill_id,skill_level,skill_name,class_index) VALUES (?,?,?,?,?)";
    private static final String ADD_INVENTORY_ITEM = "INSERT INTO items (owner_id, object_id, item_id, count, enchant_level, loc, loc_data, price_sell, price_buy, custom_type1, custom_type2) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
    
    private static final String UPDATE_CHARACTER_CLASSID = "UPDATE characters SET classid=base_class WHERE classid!=base_class";
    private static final String UPDATE_CHAR_DYE_COUNT = "UPDATE items SET object_id=?, count=? WHERE owner_id=? AND item_id=?";
    
    private static Logger _log = Logger.getLogger(L2PcInstance.class.getName());
    
    private L2PcInstance _activeChar;
    private int	_goodCount = 0;
    private int _badCount = 0;
    private Map<Integer, Matrix> _matrices;
    
    private class Matrix
    {
        private Map<Integer,Map<Integer,L2Skill>> _subClassSkillSets;
        List<SubClass> _subClasses; 
        
        private String _playerName;
        private int _objectId;
        
        protected Matrix(int objectId, String playerName)
        {
            _objectId = objectId;
            _playerName = playerName;
        }
        
        protected int getObjectId()
        {
            return _objectId;
        }
        
        protected String getPlayerName()
        {
            return _playerName;
        }
        
        protected int getNumSubClasses()
        {
            return getSubClassesList().size();
        }
        
        protected List<SubClass> getSubClassesList()
        {
            if (_subClasses == null)
                _subClasses = new FastList<SubClass>();
            
            return _subClasses;
        }
        
        protected L2Skill getSubSkill(int classIndex, int skillId)
        {
            if (getSubSkillSet().get(classIndex) == null)
                return null;
            
            return getSubSkillSet().get(classIndex).get(skillId);
        }
        
        protected void addSubSkill(int classIndex, L2Skill skill)
        {
            Map<Integer, L2Skill> skillList;
            
            if (getSubSkillSet().containsKey(classIndex))
            {
                skillList = getSubSkillSet().get(classIndex);
                skillList.put(skill.getId(), skill);
            } 
            else 
            {
                skillList = new FastMap<Integer, L2Skill>();
                skillList.put(skill.getId(), skill);
            }
            
            getSubSkillSet().put(classIndex, skillList);
            return;
        }
        
        protected Map<Integer,Map<Integer,L2Skill>> getSubSkillSet()
        {
            if (_subClassSkillSets == null)
                _subClassSkillSets = new FastMap<Integer,Map<Integer,L2Skill>>();
            
            return _subClassSkillSets;
        }
    }
    
    private Map<Integer, Matrix> getMatrices()
    {
        if (_matrices == null)
            _matrices = new FastMap<Integer, Matrix>();
        
        return _matrices;
    }
    
    public boolean useAdminCommand(String command, L2PcInstance activeChar)
    {
        if (activeChar == null) 
            return false;
        
        if (activeChar.isSubClassActive()) 
        {
            activeChar.sendMessage("You can not be on a sub class while attempting to use this command.");
            return false;
        }
        
        if (!(activeChar.getAccessLevel() >= REQUIRED_LEVEL)) 
            return false; 
        
        _activeChar = activeChar;    	
        
        if (command.equals("admin_subclass_repair"))
        {
            /**
             * I. Populate the MatrixList with all Matrixies of each subclassed player.
             */
            java.sql.Connection con       = null;
            PreparedStatement 	statement = null;
            ResultSet         	rset	  = null;
            
            // KEEP THIS CONNECTION ALIVE DURING THE ENTIRE METHOD!
            try 
            {
                con = L2DatabaseFactory.getInstance().getConnection(); 
            } 
            catch (SQLException sqlx) 
            {
                _log.severe("//subclass_repair can't connect to the database: " + sqlx);
                return true;
            }
            
            sendReport("INFO", "Allocating Players");
            
            try
            {
                statement = con.prepareStatement(GET_SUBCLASSES);
                rset = statement.executeQuery();
                
                int objectId = -1;
                sendReport("INFO", "Processing...");
                
                while (rset.next())
                {
                    try
                    {                	                	
                        Matrix playerMatrix = null;              	
                        objectId = rset.getInt("char_obj_id");
                        
                        // EITHER CREATE OR RETRIEVE THE MATRIX INSTANCE FOR THE PLAYER.
                        if (getMatrices().containsKey(objectId))
                            playerMatrix = getMatrices().get(objectId);
                        else
                            playerMatrix = new Matrix(objectId, rset.getString("char_name"));
                        
                        if (playerMatrix.getNumSubClasses() == 3)
                        {
                            sendReport("WARN", "Warning | Player(" + playerMatrix.getPlayerName() + ") overcapped subclass count, all else will be ignored.");
                            continue;
                        }
                        else 
                        {
                            SubClass subClass = new SubClass();
                            subClass.setClassId(rset.getInt("class_id"));
                            subClass.setLevel(rset.getByte("level"));
                            subClass.setExp(rset.getLong("exp"));
                            subClass.setSp(rset.getInt("sp"));
                            subClass.setClassIndex(playerMatrix.getNumSubClasses() + 1);
                            
                            playerMatrix.getSubClassesList().add(subClass);
                            
                            try 
                            {
                                getMatrices().put(objectId, playerMatrix); 
                                sendReport("INFO", "Allocated Player: " + playerMatrix.getPlayerName());
                            } 
                            catch (Exception e) {
                                sendReport("WARN", "Error | Allocating Player: " + playerMatrix.getPlayerName());
                                continue; 
                            }
                        }
                    } 
                    catch (Exception e)
                    {
                        sendReport("BAD", " Severe Error | Info on server console. Attempting recovery..."); 
                        continue;
                    }
                }
            } 
            catch (Exception e)
            {
                sendReport("BAD", "Severe Error | Info on server console. " + e);
                return true;
            } 
            finally 
            {
                try
                { 
                    rset.close();
                    statement.close();
                }
                catch (SQLException sqlx) {}
            }
            
            sendReport("INFO", "Finished Allocating " + getMatrices().size() + " Subclassed Players.");
            
            /**
             * II. Purge all sub class assets.
             */       
            
            sendReport("INFO", "Purging sub class assets: Processing...");
            
            try
            {
                int numDeleted;
                
                // Remove all base data related to sub classes.
                statement = con.prepareStatement(EMPTY_CHARACTER_SUBCLASS);
                numDeleted = statement.executeUpdate();
                statement.close();
                sendReport("INFO", "Deleted " + numDeleted + " sub classes.");
                
                // Remove all skills related to sub classes.
                statement = con.prepareStatement(DELETE_SUBCLASS_SKILLS);
                numDeleted = statement.executeUpdate();
                statement.close();
                sendReport("INFO", "Deleted " + numDeleted + " sub class Skills.");
                
                // Remove all skill effects related to sub classes.
                statement = con.prepareStatement(EMPTY_CHARACTER_EFFECTS);
                numDeleted = statement.executeUpdate();
                statement.close();
                sendReport("INFO", "Deleted " + numDeleted + " sub class Stored Effects/Buffs");
                
                // Remove all shortcuts related to sub classes.
                statement = con.prepareStatement(DELETE_SUBCLASS_SHORTCUTS);
                numDeleted = statement.executeUpdate();
                statement.close();
                sendReport("INFO", "Deleted " + numDeleted + " sub class QuickBar Shortcuts.");
                
                // Remove all quests related to sub classes.
                statement = con.prepareStatement(DELETE_SUBCLASS_QUESTS);
                numDeleted = statement.executeUpdate();
                statement.close();
                sendReport("INFO", "Deleted " + numDeleted + " sub class Quest Variables.");
            } 
            catch (Exception e) 
            {
                sendReport("BAD", "Severe Error | When deleting sub class assets. This process can not continue. Restore the backed up database and restart server, afterwards run the command again.");
                return true;
            } 
            finally 
            {
                try
                { 
                    statement.close();
                }
                catch (SQLException sqlx) {}
            }
            
            /**
             * III. Write all subclasses to database from each PlayersMatrix.
             */
            setCounter(false, false);
            
            for (Iterator<Matrix> playerMatrices = getMatrices().values().iterator(); playerMatrices.hasNext();)
            {
                Matrix currMatrix = playerMatrices.next();
                
                sendReport("INFO", "Writing " + currMatrix.getNumSubClasses() +
                           " Subclasses for Player: " + currMatrix.getPlayerName());
                
                for (SubClass playerSub : currMatrix.getSubClassesList()) 
                {
                    try
                    {
                        statement = con.prepareStatement(ADD_SUBCLASS);
                        statement.setInt(1, currMatrix.getObjectId());
                        statement.setInt(2, playerSub.getClassId());
                        statement.setLong(3, playerSub.getExp());
                        statement.setInt(4, playerSub.getSp());
                        statement.setInt(5, playerSub.getLevel());
                        statement.setInt(6, playerSub.getClassIndex());
                        
                        statement.execute();
                        statement.close();
                        
                        sendReport("INFO", "Wrote | ObjectId(" + currMatrix.getObjectId() + 
                                   ") ClassIndex(" + playerSub.getClassIndex()+") into Database. Player: " + currMatrix.getPlayerName());
                        setCounter(true, false);
                    } 
                    catch (Exception e) 
                    {
                        currMatrix.getSubClassesList().remove(playerSub);
                        
                        sendReport("WARN", "Error | Writing Player(" + currMatrix.getPlayerName() + ") ObjectId(" + currMatrix.getObjectId() + 
                                   ") ClassIndex("+playerSub.getClassIndex()+") into Database. Subclass will not be restored. "+e);
                        setCounter(false, true);
                        continue;
                    } 
                    finally 
                    {
                        try
                        { 
                            statement.close();
                        }
                        catch (SQLException sqlx) {}
                    }
                }
            }
            
            sendReport("INFO", "Finished Writing " + getCount(true) + " Subclasses to Database. Errors " + getCount(false));
            
            /**
             * IV. Re-skill every subclass in each PlayersMatrix.
             */
            setCounter(false, false);
            
            for (Iterator<Matrix> playerMatrices = getMatrices().values().iterator(); playerMatrices.hasNext();)
            {
                Matrix currMatrix = playerMatrices.next(); 
                
                sendReport("INFO", "Processing " + currMatrix.getNumSubClasses() + " Subclasses for Player: " + currMatrix.getPlayerName());
                
                for (SubClass playerSub : currMatrix.getSubClassesList())
                {
                    sendReport("INFO", "---> Allocating Skills | ClassIndex: " + playerSub.getClassIndex() + " Level: " + playerSub.getLevel());
                    
                    ClassId subTemplate = ClassId.values()[playerSub.getClassId()];
                    
                    // IMPORTANT: RETRIVE ALL SKILLS ALLOWED FOR THIS TEMPLATE.
                    List<L2SkillLearn> SkillTree = SkillTreeTable.getInstance().getAllowedSkills(subTemplate);
                    
                    if (SkillTree == null)
                    {
                        sendReport("INFO", "<--- Skipping : ClassIndex(" + playerSub.getClassIndex() + ")");
                        sendReport("WARN", "Error | Player (" + currMatrix.getPlayerName() + ") | Skilltree for ClassId " + playerSub.getClassId() + 
                                   " on ClassIndex " + playerSub.getClassIndex() + " is not defined!");
                        
                        setCounter(false, true);
                        continue;
                    }
                    
                    for (Iterator<L2SkillLearn> validSkills = SkillTree.listIterator(); validSkills.hasNext();)
                    {
                        L2SkillLearn SkillInfo = validSkills.next();
                        
                        if (SkillInfo.getMinLevel() <= playerSub.getLevel())
                        {	
                            L2Skill prevSkill = currMatrix.getSubSkill(playerSub.getClassIndex(), SkillInfo.getId());
                            L2Skill newSkill = SkillTable.getInstance().getInfo(SkillInfo.getId(), SkillInfo.getLevel());
                            
                            if (prevSkill != null && (prevSkill.getLevel() > newSkill.getLevel()))
                                continue;
                            
                            try 
                            {
                                currMatrix.addSubSkill(playerSub.getClassIndex(), newSkill);
                                
                                if (Config.DEBUG)
                                    sendReport("INFO", "ObjectId(" + currMatrix.getObjectId() + ") Index: " + playerSub.getClassIndex() + " " + 
                                               newSkill.getName() + " Lv:" + newSkill.getLevel());
                                
                                setCounter(true,false);
                            } 
                            catch (Exception e) 
                            {
                                sendReport("WARN", "Error Allocating | ObjectId(" + currMatrix.getObjectId() + ") Index: " + 
                                           playerSub.getClassIndex() + " " + newSkill.getName() + " Lv:" + newSkill.getLevel());
                                
                                setCounter(false, true);
                                continue;
                            }
                        }
                    }
                }
            }
            
            sendReport("INFO", "Finished Allocating " + getCount(true) + " Skills Into Database: Errors " + getCount(false));
            
            /**
             * V. Skill Engine: Write all final skills into database.
             */
            setCounter(false, false);
            
            for (Iterator<Matrix> playerMatrices = getMatrices().values().iterator(); playerMatrices.hasNext();)
            {
                Matrix playerMatrix = playerMatrices.next();
                
                for (SubClass playerSub : playerMatrix.getSubClassesList())
                {
                    Map<Integer,L2Skill> skillList = playerMatrix.getSubSkillSet().get(playerSub.getClassIndex());
                    
                    if (skillList == null) 
                    {
                        sendReport("WARN", "<--- Skipping : ClassIndex: " + playerSub.getClassIndex() + " No Skills Defined.");
                        continue;
                    }
                    
                    sendReport("INFO", "---> Writing Skills : ClassIndex: " + playerSub.getClassIndex() + " Lv: " + playerSub.getLevel());
                    
                    for (Iterator<L2Skill> allSkills = skillList.values().iterator(); allSkills.hasNext();)
                    {
                        L2Skill currSkill = allSkills.next();
                        
                        try
                        {
                            statement = con.prepareStatement(ADD_SUBCLASS_SKILL);
                            statement.setInt(1, playerMatrix.getObjectId());
                            statement.setInt(2, currSkill.getId());
                            statement.setInt(3, currSkill.getLevel());
                            statement.setString(4, currSkill.getName());
                            statement.setInt(5, playerSub.getClassIndex());
                            statement.execute();
                            statement.close();
                            
                            if (Config.DEBUG)
                                sendReport("INFO", " Index(" + playerSub.getClassIndex() + ") SkId(" + 
                                           currSkill.getId() + ") SkLv(" + currSkill.getLevel() + ") - " + 
                                           currSkill.getName() + " - Player:" + playerMatrix.getPlayerName());
                            
                            setCounter(true, false);
                        } 
                        catch (Exception e) 
                        {
                            sendReport("WARN", "Error | Writing Skill(" + playerMatrix.getPlayerName() + 
                                       ") "+ currSkill.getName() + " SkId " + currSkill.getId() + " SkLv " + 
                                       currSkill.getLevel() + " ClassIndex " + playerSub.getClassIndex());
                            
                            setCounter(false, true);
                            //continue;
                        } 
                        finally
                        {
                            try
                            { 
                                statement.close();
                            }
                            catch (SQLException sqlx) {}       					
                        }
                    }
                }
            }
            
            sendReport("INFO", "Finished Writing " + getCount(true) + " Skills Into Database: Errors " + getCount(false));
            
            /**
             * VII. Reset all players to log into their original baseclass incase
             * they logged off from a subclass that now could not be restored.
             */
            try
            {
                // Reset all subclass active state data.
                statement = con.prepareStatement(UPDATE_CHARACTER_CLASSID);
                int numChanges = statement.executeUpdate();
                statement.close();
                
                sendReport("INFO", "Reverted " + numChanges + " Players to BaseClass.");
                
            } catch(Exception e) {}
            
            sendReport("INFO", "Done! Be sure to maintain all of your backups incase undesirable changes have been made!");
            
            // Perform various cleanup operations.
            try
            {
                rset.close();
                statement.close();
                con.close();
            }
            catch (SQLException sqlx) {}
            
            setCounter(false, false);
            _matrices = null;
        }
        
        /**
         * Repairing of character hennas.
         */
        else if(command.equals("admin_subclass_henna_repair"))
        {
            java.sql.Connection con        = null;
            PreparedStatement 	statement  = null;
            ResultSet         	rset	   = null;
            
            PreparedStatement 	statement2 = null;
            ResultSet         	rset2	   = null;
            
            sendReport("INFO", "Gathering Henna Information: Processing...");
            setCounter(false, false);
            
            try 
            {
                con = L2DatabaseFactory.getInstance().getConnection();
                statement = con.prepareStatement(GET_CHAR_HENNAS);
                rset = statement.executeQuery();
                
                int playerId = 0;
                int count = 0;
                boolean isUpdated = false;
                
                L2Henna hennaTemplate 		  = null;
                L2HennaInstance hennaInstance = null;
                L2ItemInstance dyeInstance	  = null;
                
                while (rset.next())
                {
                    try
                    {
                        hennaTemplate = HennaTable.getInstance().getTemplate(rset.getInt("symbol_id"));
                        
                        if (hennaTemplate != null)
                        {
                            hennaInstance = new L2HennaInstance(hennaTemplate);
                            dyeInstance = ItemTable.getInstance().createItem("Henna Restoration", hennaInstance.getItemIdDye(), hennaInstance.getAmountDyeRequire(), activeChar);
                            playerId = rset.getInt("char_obj_id");
                            
                            statement2 = con.prepareStatement(GET_DYE_COUNT);
                            statement2.setInt(1, playerId);
                            statement2.setInt(2, dyeInstance.getItemId());
                            rset2 = statement2.executeQuery();
                            
                            // UPDATE_CHAR_DYE_COUNT
                            while (rset2.next())
                            {
                                count = rset2.getInt("count");
                                
                                rset2.close();
                                statement2.close();
                                
                                statement2 = con.prepareStatement(UPDATE_CHAR_DYE_COUNT);
                                statement2.setInt(1, dyeInstance.getObjectId());
                                statement2.setInt(2, (dyeInstance.getCount() + count));
                                statement2.setInt(3, playerId);
                                statement2.setInt(4, dyeInstance.getItemId());
                                statement2.executeUpdate();
                                statement2.close();
                                
                                sendReport("INFO", "Henna | Updated DyeID(" + dyeInstance.getItemId() + ") for Player ID: " + playerId);
                                setCounter(true, false);
                                
                                isUpdated = true;
                                break;
                            }
                            
                            if (isUpdated)
                            {
                                isUpdated = false;
                                continue;
                            }
                            
                            // Reimburse the player with half the amount of dyes used to create the original henna.
                            statement2.close();
                            statement2 = con.prepareStatement(ADD_INVENTORY_ITEM);
                            statement2.setInt(1, playerId);
                            statement2.setInt(2, dyeInstance.getObjectId());
                            statement2.setInt(3, dyeInstance.getItemId());	        			
                            statement2.setInt(4, dyeInstance.getCount());
                            statement2.setInt(5, 0);
                            statement2.setString(6, "INVENTORY");
                            statement2.setInt(7, 0);
                            statement2.setInt(8, dyeInstance.getPriceToSell());
                            statement2.setInt(9, dyeInstance.getPriceToBuy());
                            statement2.setInt(10, dyeInstance.getCustomType1());
                            statement2.setInt(11, dyeInstance.getCustomType2());
                            statement2.executeUpdate();
                            statement2.close();		                    
                            //
                            
                            sendReport("INFO", "Henna | Wrote DyeID(" + dyeInstance.getItemId() + ") into inventory for Player Id: " + playerId);
                            setCounter(true, false);
                        }
                        else
                        {
                            _log.info("Null HennaTemplate");
                            continue;
                        }
                    }
                    catch(Exception e)
                    {
                        sendReport("WARN", "Internal Henna Error, Attempting Recover: "+e);
                        setCounter(false, true);
                        continue;
                    }
                    finally
                    {
                        try
                        {
                            rset2.close();
                            statement2.close();
                        }
                        catch (SQLException sqlx) {}
                    }
                }
                
                // Remove all hennas attributed to a sub class.
                statement = con.prepareStatement(DELETE_SUBCLASS_HENNA);
                int numDelete = statement.executeUpdate();
                statement.close();
                sendReport("INFO", "Deleted " + numDelete + " Henna Symbols.");
                
            }
            catch(Exception e)
            {
                sendReport("WARN", "Henna Error: " + e);
            }
            finally
            {
                try
                {
                    rset.close();
                    statement.close();
                    con.close();
                }
                catch (SQLException sqlx) {}
            }
            
            sendReport("INFO", "Finished Writing " + getCount(true) + " Henna Dyes to Inventories. Errors " + getCount(false));
        }
        
        return true;
    }
    
    private void sendConsole(int msgType, String msgContent)
    {
        _activeChar.sendPacket(new CreatureSay(_activeChar.getObjectId(), msgType, _activeChar.getName(), msgContent));
    }
    
    private void sendReport(String msgType, String msgContent)
    {
        if (msgType.equals("INFO")) 
        {
            _log.info(msgContent); 
            sendConsole(Say2.SHOUT, msgContent);
        }
        else if (msgType.equals("WARN"))
        {
            _log.warning(msgContent); 
            sendConsole(Say2.TRADE, msgContent);
        }
        else if (msgType.equals("BAD"))
        {
            _log.severe(msgContent); 
            sendConsole(Say2.TRADE, msgContent);
        }
    }
    
    private void setCounter(boolean isOK, boolean isError)
    {
        if (isOK)
            _goodCount++;
        else if (isError)
            _badCount++;
        else if (!(isOK && isError))
        {
            _goodCount = 0;
            _badCount = 0;
        }
    }
    
    private int getCount(boolean isOK)
    {
        if (isOK)
            return _goodCount;
        
        return _badCount;
    }
    
    public String[] getAdminCommandList()
    {
        return ADMIN_SUBCLASS_COMMANDS;
    }
}