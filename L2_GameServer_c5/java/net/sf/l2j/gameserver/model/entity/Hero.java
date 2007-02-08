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

/**
 * @author godson
 */

package net.sf.l2j.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.Olympiad;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.serverpackets.UserInfo;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.StatsSet;


public class Hero 
{
    private static Logger _log = Logger.getLogger(Hero.class.getName());
    
    private static Hero _instance;
    private static final String GET_HEROES = "SELECT * FROM heroes WHERE played = 1";
    private static final String GET_ALL_HEROES = "SELECT * FROM heroes";
    private static final String UPDATE_ALL = "UPDATE heroes SET played = 0";
    private static final String INSERT_HERO = "INSERT INTO heroes VALUES (?,?,?,?,?)";
    private static final String UPDATE_HERO = "UPDATE heroes SET count = ?, played = ?" +
            " WHERE char_id = ?";
    private static final String GET_CLAN_ALLY = "SELECT clanid, allyId FROM characters" +
            " WHERE obj_Id = ?";
    private static final String DELETE_ITEMS = "DELETE FROM items WHERE item_id IN " +
            "(6842, 6611, 6612, 6613, 6614, 6615, 6616, 6617, 6618, 6619, 6620, 6621) " +
            "AND owner_id NOT IN (SELECT obj_id FROM characters WHERE accesslevel > 0)";
    
    private static final int[] _heroItems = {6842, 6611, 6612, 6613, 6614, 6615, 6616,
                                             6617, 6618, 6619, 6620, 6621
    };
    private static Map<Integer, StatsSet> _heroes;
    private static Map<Integer, StatsSet> _completeHeroes;
    
    public static final String COUNT = "count";
    public static final String PLAYED = "played";
    public static final String CLAN_NAME = "clan_name";
    public static final String CLAN_CREST = "clan_crest";
    public static final String ALLY_NAME = "ally_name";
    public static final String ALLY_CREST = "ally_crest";
    
    public static Hero getInstance()
    {
        if (_instance == null)
            _instance = new Hero();
        return _instance;       
    }
    
    public Hero()
    {
        init();
    }
    
    private void init()
    {
        _heroes = new FastMap<Integer, StatsSet>();
        _completeHeroes = new FastMap<Integer, StatsSet>();
        
        PreparedStatement statement;
        PreparedStatement statement2;
        
        ResultSet rset;
        ResultSet rset2;
        
        try
        {
            Connection con = L2DatabaseFactory.getInstance().getConnection();
            Connection con2 = L2DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(GET_HEROES);
            rset = statement.executeQuery();
            
            while (rset.next())
            {
                StatsSet hero = new StatsSet();
                int charId = rset.getInt(Olympiad.CHAR_ID);
                hero.set(Olympiad.CHAR_NAME, rset.getString(Olympiad.CHAR_NAME));
                hero.set(Olympiad.CLASS_ID, rset.getInt(Olympiad.CLASS_ID));
                hero.set(COUNT, rset.getInt(COUNT));
                hero.set(PLAYED, rset.getInt(PLAYED));
                
                statement2 = con2.prepareStatement(GET_CLAN_ALLY);
                statement2.setInt(1, charId);
                rset2 = statement2.executeQuery();
                
                if (rset2.next())
                {
                    int clanId = rset2.getInt("clanid");
                    int allyId = rset2.getInt("allyId");
                    
                    String clanName = "";
                    String allyName = "";
                    int clanCrest = 0;
                    int allyCrest = 0;
                    
                    if (clanId > 0)
                    {
                        clanName = ClanTable.getInstance().getClan(clanId).getName();
                        clanCrest = ClanTable.getInstance().getClan(clanId).getCrestId();
                        
                        if (allyId > 0)
                        {
                            allyName = ClanTable.getInstance().getClan(clanId).getAllyName();
                            allyCrest = ClanTable.getInstance().getClan(clanId).getAllyCrestId();
                        }
                    }
                    
                    hero.set(CLAN_CREST, clanCrest);
                    hero.set(CLAN_NAME, clanName);
                    hero.set(ALLY_CREST, allyCrest);
                    hero.set(ALLY_NAME, allyName);
                }
                
                rset2.close();
                statement2.close();
                
                _heroes.put(charId, hero);
            }
            
            rset.close();
            statement.close();
            
            statement = con.prepareStatement(GET_ALL_HEROES);
            rset = statement.executeQuery();
            
            while (rset.next())
            {
                StatsSet hero = new StatsSet();
                int charId = rset.getInt(Olympiad.CHAR_ID);
                hero.set(Olympiad.CHAR_NAME, rset.getString(Olympiad.CHAR_NAME));
                hero.set(Olympiad.CLASS_ID, rset.getInt(Olympiad.CLASS_ID));
                hero.set(COUNT, rset.getInt(COUNT));
                hero.set(PLAYED, rset.getInt(PLAYED));
                
                statement2 = con2.prepareStatement(GET_CLAN_ALLY);
                statement2.setInt(1, charId);
                rset2 = statement2.executeQuery();
                
                if (rset2.next())
                {
                    int clanId = rset2.getInt("clanid");
                    int allyId = rset2.getInt("allyId");
                    
                    String clanName = "";
                    String allyName = "";
                    int clanCrest = 0;
                    int allyCrest = 0;
                    
                    if (clanId > 0)
                    {
                        clanName = ClanTable.getInstance().getClan(clanId).getName();
                        clanCrest = ClanTable.getInstance().getClan(clanId).getCrestId();
                        
                        if (allyId > 0)
                        {
                            allyName = ClanTable.getInstance().getClan(clanId).getAllyName();
                            allyCrest = ClanTable.getInstance().getClan(clanId).getAllyCrestId();
                        }
                    }
                    
                    hero.set(CLAN_CREST, clanCrest);
                    hero.set(CLAN_NAME, clanName);
                    hero.set(ALLY_CREST, allyCrest);
                    hero.set(ALLY_NAME, allyName);
                }
                
                rset2.close();
                statement2.close();
                
                _completeHeroes.put(charId, hero);
            }
            
            rset.close();
            statement.close();
            
            con.close();
            con2.close();
        } catch(SQLException e)
        {
        	_log.warning("Hero System: Couldnt load Heroes");
        	if (Config.DEBUG) e.printStackTrace();
        }
        
        _log.info("Hero System: Loaded " + _heroes.size() + " Heroes.");
        _log.info("Hero System: Loaded " + _completeHeroes.size() + " all time Heroes.");
    }
    
    public Map<Integer, StatsSet> getHeroes()
    {
        return _heroes;
    }
    
    public synchronized void computeNewHeroes(List<StatsSet> newHeroes)
    {
        updateHeroes(true);
        
        List heroItems = Arrays.asList(_heroItems);
        L2ItemInstance[] items;
        InventoryUpdate iu;
        
        if (_heroes.size() != 0)
        {
            for (StatsSet hero : _heroes.values())
            {
                String name = hero.getString(Olympiad.CHAR_NAME);
                
                L2PcInstance player = L2World.getInstance().getPlayer(name);
                
                if (player != null)
                {
                    player.setHero(false);
                    
                    items = player.getInventory().unEquipItemInBodySlotAndRecord(L2Item.SLOT_LR_HAND);
                    iu = new InventoryUpdate();
                    for (L2ItemInstance item : items)
                    {
                        iu.addModifiedItem(item);
                    }
                    player.sendPacket(iu);
                    
                    items = player.getInventory().unEquipItemInBodySlotAndRecord(L2Item.SLOT_R_HAND);
                    iu = new InventoryUpdate();
                    for (L2ItemInstance item : items)
                    {
                        iu.addModifiedItem(item);
                    }
                    player.sendPacket(iu);
                    
                    items = player.getInventory().unEquipItemInBodySlotAndRecord(L2Item.SLOT_HAIR);
                    iu = new InventoryUpdate();
                    for (L2ItemInstance item : items)
                    {
                        iu.addModifiedItem(item);
                    }
                    player.sendPacket(iu);
                    
                    items = player.getInventory().unEquipItemInBodySlotAndRecord(L2Item.SLOT_FACE);
                    iu = new InventoryUpdate();
                    for (L2ItemInstance item : items)
                    {
                        iu.addModifiedItem(item);
                    }
                    player.sendPacket(iu);
                                         
                    items = player.getInventory().unEquipItemInBodySlotAndRecord(L2Item.SLOT_DHAIR);
                     iu = new InventoryUpdate();
                    for (L2ItemInstance item : items)
                    {
                        iu.addModifiedItem(item);
                    }
                    player.sendPacket(iu);
                    
                    for(L2ItemInstance item : player.getInventory().getAvailableItems(false))
                    {
                        if (item == null) continue;
                        if (!heroItems.contains(item.getItemId())) continue;
                        
                        player.destroyItem("Hero", item, null, true);
                        iu = new InventoryUpdate();
                        iu.addRemovedItem(item);
                        player.sendPacket(iu);
                    }
                    
                    player.sendPacket(new UserInfo(player));
                    player.broadcastUserInfo();
                }
            }
        }
        
        if (newHeroes.size() == 0)
        {
            _heroes.clear();
            return;
        }
        
        Map<Integer, StatsSet> heroes = new FastMap<Integer, StatsSet>();
        
        for (StatsSet hero : newHeroes)
        {
            int charId = hero.getInteger(Olympiad.CHAR_ID);
            
            if (_completeHeroes != null && _completeHeroes.containsKey(charId))
            {
                StatsSet oldHero = _completeHeroes.get(charId);
                int count = oldHero.getInteger(COUNT);
                oldHero.set(COUNT, count + 1);
                oldHero.set(PLAYED, 1);
                
                heroes.put(charId, oldHero);
            }
            else
            {
                StatsSet newHero = new StatsSet();
                newHero.set(Olympiad.CHAR_NAME, hero.getString(Olympiad.CHAR_NAME));
                newHero.set(Olympiad.CLASS_ID, hero.getInteger(Olympiad.CLASS_ID));
                newHero.set(COUNT, 1);
                newHero.set(PLAYED, 1);
                
                heroes.put(charId, newHero);
            }
        }
        
        deleteItemsInDb();
        
        _heroes.clear();
        _heroes.putAll(heroes);
        heroes.clear();
        
        updateHeroes(false);
        
        for (StatsSet hero : _heroes.values())
        {
            String name = hero.getString(Olympiad.CHAR_NAME);
            
            L2PcInstance player = L2World.getInstance().getPlayer(name);
            
            if (player != null)
            {
                player.setHero(true);
                L2Clan clan = player.getClan();
                if (clan != null)
                    clan.setReputationScore(clan.getReputationScore()+1000, true);
                player.sendPacket(new UserInfo(player));
                player.broadcastUserInfo();
            }
        }
    }
    
    public void updateHeroes(boolean setDefault)
    {
        Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            if(setDefault)
            {
                PreparedStatement statement = con.prepareStatement(UPDATE_ALL);
                statement.execute();
                statement.close();
            }
            else
            {
                PreparedStatement statement;
                
                for (Integer heroId : _heroes.keySet())
                {
                    StatsSet hero = _heroes.get(heroId);
                    
                    if (_completeHeroes == null || !_completeHeroes.containsKey(heroId))
                    {
                        statement = con.prepareStatement(INSERT_HERO);
                        statement.setInt(1, heroId);
                        statement.setString(2, hero.getString(Olympiad.CHAR_NAME));
                        statement.setInt(3, hero.getInteger(Olympiad.CLASS_ID));
                        statement.setInt(4, hero.getInteger(COUNT));
                        statement.setInt(5, hero.getInteger(PLAYED));
                        statement.execute();
                        
                        Connection con2 = L2DatabaseFactory.getInstance().getConnection();
                        PreparedStatement statement2 = con2.prepareStatement(GET_CLAN_ALLY);
                        statement2.setInt(1, heroId);
                        ResultSet rset2 = statement2.executeQuery();
                        
                        if (rset2.next())
                        {
                            int clanId = rset2.getInt("clanid");
                            int allyId = rset2.getInt("allyId");
                            
                            String clanName = "";
                            String allyName = "";
                            int clanCrest = 0;
                            int allyCrest = 0;
                            
                            if (clanId > 0)
                            {
                                clanName = ClanTable.getInstance().getClan(clanId).getName();
                                clanCrest = ClanTable.getInstance().getClan(clanId).getCrestId();
                                
                                if (allyId > 0)
                                {
                                    allyName = ClanTable.getInstance().getClan(clanId).getAllyName();
                                    allyCrest = ClanTable.getInstance().getClan(clanId).getAllyCrestId();
                                }
                            }
                            
                            hero.set(CLAN_CREST, clanCrest);
                            hero.set(CLAN_NAME, clanName);
                            hero.set(ALLY_CREST, allyCrest);
                            hero.set(ALLY_NAME, allyName);
                        }
                        
                        rset2.close();
                        statement2.close();
                        con2.close();
                        
                        _heroes.remove(hero);
                        _heroes.put(heroId, hero);
                        
                        _completeHeroes.put(heroId, hero);
                    }
                    else
                    {
                        statement = con.prepareStatement(UPDATE_HERO);
                        statement.setInt(1, hero.getInteger(COUNT));
                        statement.setInt(2, hero.getInteger(PLAYED));
                        statement.setInt(3, heroId);
                        statement.execute();
                    }
                    
                    statement.close();
                }
            }
        } catch(SQLException e)
        {
        	_log.warning("Hero System: Couldnt update Heroes");
        	if (Config.DEBUG) e.printStackTrace();
        } finally
        {
            try{con.close();}catch(Exception e){e.printStackTrace();}
        }
    }
    
    public int[] getHeroItems()
    {
        return _heroItems;
    }
    
    private void deleteItemsInDb()
    {
        Connection con = null;
        
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(DELETE_ITEMS);
            statement.execute();
            statement.close();
        }
        catch(SQLException e){e.printStackTrace();}
        finally{
            try{con.close();}catch(SQLException e){e.printStackTrace();}
        }
    }
}