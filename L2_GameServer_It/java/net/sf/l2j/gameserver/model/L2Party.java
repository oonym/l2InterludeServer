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
package net.sf.l2j.gameserver.model;

import java.util.List;

import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.SevenSignsFestival;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SummonInstance;
import net.sf.l2j.gameserver.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.serverpackets.PartySmallWindowAdd;
import net.sf.l2j.gameserver.serverpackets.PartySmallWindowAll;
import net.sf.l2j.gameserver.serverpackets.PartySmallWindowDelete;
import net.sf.l2j.gameserver.serverpackets.PartySmallWindowDeleteAll;
import net.sf.l2j.gameserver.serverpackets.PartySmallWindowUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.util.Rnd;

/**
 * This class ...
 * 
 * @author nuocnam
 * @version $Revision: 1.6.2.2.2.6 $ $Date: 2005/04/11 19:12:16 $
 */
public class L2Party {
	static double[] _bonusExpSp = {1, 1.30, 1.39, 1.50, 1.54, 1.58, 1.63, 1.67, 1.71};
	
	//private static Logger _log = Logger.getLogger(L2Party.class.getName());
	
	private List<L2PcInstance> _members = null;
	private List<L2PcInstance> _validMembers = null;
    private int _pendingInvitation = 0;       // Number of players that already have been invited (but not replied yet)
	private int _partyLvl = 0;
	private int _itemDistribution = 0;
	private int _itemLastLoot = 0;
	
	public static final int ITEM_LOOTER = 0;
	public static final int ITEM_RANDOM = 1;
	public static final int ITEM_RANDOM_SPOIL = 2;
	public static final int ITEM_ORDER = 3;
	public static final int ITEM_ORDER_SPOIL = 4;
	
	/**
	 * constructor ensures party has always one member - leader
	 * @param leader
	 * @param itemDistributionMode
	 */
	public L2Party(L2PcInstance leader, int itemDistribution) 
	{
		_validMembers = new FastList<L2PcInstance>();
		_itemDistribution = itemDistribution;
		getPartyMembers().add(leader);
		_validMembers.add(leader);
		_partyLvl = leader.getLevel();
	}
	
	/**
	 * returns number of party members
	 * @return
	 */
	public int getMemberCount() { return getPartyMembers().size(); }
	
    /**
     * returns number of players that already been invited, but not replied yet
     * @return
     */
    public int getPendingInvitationNumber() { return _pendingInvitation; }
    
    /**
     * decrease number of players that already been invited but not replied yet
     * happens when: player join party or player decline to join
     */
    public void decreasePendingInvitationNumber() { _pendingInvitation--; }
    
    /**
     * increase number of players that already been invite but not replied yet
     */
    public void increasePendingInvitationNumber() { _pendingInvitation++; }
    
	/**
	 * returns number of party members
	 * @return
	 */
	public int getValidMemberCount() { return _validMembers.size(); }
	
	/**
	 * returns all party members
	 * @return
	 */
	public List<L2PcInstance> getPartyMembers()
	{
		if (_members == null) _members = new FastList<L2PcInstance>();
		return _members;
	}
	
	/**
	 * get random member from party
	 * @return
	 */
	//private L2PcInstance getRandomMember() { return getPartyMembers().get(Rnd.get(getPartyMembers().size())); }
	private L2PcInstance getCheckedRandomMember(int ItemId, L2Character target)
	{
		List<L2PcInstance> availableMembers = new FastList<L2PcInstance>();
		for (L2PcInstance member : getPartyMembers())
		{
			if (member.getInventory().validateCapacityByItemId(ItemId) &&
                    Util.checkIfInRange(1150, target, member, true)) availableMembers.add(member);
		}
		if (availableMembers.size() > 0) return availableMembers.get(Rnd.get(availableMembers.size()));
		else return null;
	}
	
	/**
	 * get next item looter
	 * @return
	 */
	/*private L2PcInstance getNextLooter()
	{
		_itemLastLoot++;
		if (_itemLastLoot > getPartyMembers().size() -1) _itemLastLoot = 0;
		
		return (getPartyMembers().size() > 0) ? getPartyMembers().get(_itemLastLoot) : null;
	}*/
	private L2PcInstance getCheckedNextLooter(int ItemId, L2Character target)
	{
		for (int i = 0; i < getMemberCount(); i++)
		{
			_itemLastLoot++;
			if (_itemLastLoot >= getMemberCount()) _itemLastLoot = 0;
			L2PcInstance member = getPartyMembers().get(_itemLastLoot);
			
			if (member.getInventory().validateCapacityByItemId(ItemId) &&
                    Util.checkIfInRange(1150, target, member, true)) return member;
		}
		
		return null;
	}
	
	/**
	 * get next item looter
	 * @return
	 */
	private L2PcInstance getActualLooter(L2PcInstance player, int ItemId, boolean spoil, L2Character target)
	{
		L2PcInstance looter = player;
        
        switch (_itemDistribution)
        {
            case ITEM_RANDOM:
                if (!spoil) looter = getCheckedRandomMember(ItemId, target);
                break;
            case ITEM_RANDOM_SPOIL:
                looter = getCheckedRandomMember(ItemId, target);
                break;
            case ITEM_ORDER:
                if (!spoil) looter = getCheckedNextLooter(ItemId, target);
                break;
            case ITEM_ORDER_SPOIL:
                looter = getCheckedNextLooter(ItemId, target);
                break;
        }
        
        if (looter == null) looter = player;
        return looter;
	}

	/**
	 * true if player is party leader
	 * @param player
	 * @return
	 */
	public boolean isLeader(L2PcInstance player) { return (getPartyMembers().get(0).equals(player)); }
	
	/**
	 * Returns the Object ID for the party leader to be used as a unique identifier of this party
	 * @return int 
	 */
	public int getPartyLeaderOID() { return getPartyMembers().get(0).getObjectId(); }
	
	/**
	 * Broadcasts packet to every party member 
	 * @param msg
	 */
	public void broadcastToPartyMembers(L2GameServerPacket msg) 
	{
		for (L2PcInstance member : getPartyMembers())
		{
			member.sendPacket(msg);
		}
	}
	
	
	/**
	 * Send a Server->Client packet to all other L2PcInstance of the Party.<BR><BR>
	 */
	public void broadcastToPartyMembers(L2PcInstance player, L2GameServerPacket msg) 
	{
		for(L2PcInstance member : getPartyMembers())
		{
			if (member != null && !member.equals(player)) member.sendPacket(msg);
		}
	}
	
	/**
	 * adds new member to party
	 * @param player
	 */
	public void addPartyMember(L2PcInstance player) 
	{		
		//sends new member party window for all members
		//we do all actions before adding member to a list, this speeds things up a little
		PartySmallWindowAll window = new PartySmallWindowAll();
		window.setPartyList(getPartyMembers());
		player.sendPacket(window);
		
		SystemMessage msg = new SystemMessage(SystemMessage.YOU_JOINED_S1_PARTY);
		msg.addString(getPartyMembers().get(0).getName());
		player.sendPacket(msg);
		
		msg = new SystemMessage(SystemMessage.S1_JOINED_PARTY);
		msg.addString(player.getName());
		broadcastToPartyMembers(msg);
		broadcastToPartyMembers(new PartySmallWindowAdd(player));
		
		//add player to party, adjust party level
		getPartyMembers().add(player);
		if (checkMemberValidity(player)) _validMembers.add(player);
		if (player.getLevel() > _partyLvl)
		{
			_partyLvl = player.getLevel();
			recalculateValidMembers();
		}
        
		// update partySpelled
		for(L2PcInstance member : getPartyMembers())
			member.updateEffectIcons(true); // update party icons only
	}
	
	/**
	 * removes player from party
	 * @param player
	 */
	public void removePartyMember(L2PcInstance player) 
	{
		if (getPartyMembers().contains(player)) 
		{
			getPartyMembers().remove(player);
			if (_validMembers.contains(player)) _validMembers.remove(player);
			recalculatePartyLevel();
			
			if (player.isFestivalParticipant())
				SevenSignsFestival.getInstance().updateParticipants(player, this);
			
			SystemMessage msg = new SystemMessage(SystemMessage.YOU_LEFT_PARTY);
			player.sendPacket(msg);
			player.sendPacket(new PartySmallWindowDeleteAll());
			player.setParty(null);
			
			msg = new SystemMessage(SystemMessage.S1_LEFT_PARTY);
			msg.addString(player.getName());
			broadcastToPartyMembers(msg);
			broadcastToPartyMembers(new PartySmallWindowDelete(player));
			
			if (getPartyMembers().size() == 1) 
			{
				getPartyMembers().get(0).setParty(null);
			}
		}
	}
	
	/**
	 * Change party leader (used for string arguments)
	 * @param name
	 */
	
	public void changePartyLeader(String name)
	{
		L2PcInstance player = getPlayerByName(name);
		
		if (player != null)
		{
			if (getPartyMembers().contains(player))
			{
				if (isLeader(player))
				{
					player.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_TRANSFER_RIGHTS_TO_YOURSELF));
				}
				else
				{
					//Swap party members
					L2PcInstance temp;
					int p1 = getPartyMembers().indexOf(player);
					temp = getPartyMembers().get(0);
					getPartyMembers().set(0,getPartyMembers().get(p1));
					getPartyMembers().set(p1,temp);
					
					SystemMessage msg = new SystemMessage(SystemMessage.S1_HAS_BECOME_A_PARTY_LEADER);
					msg.addString(getPartyMembers().get(0).getName());
					broadcastToPartyMembers(msg);
					broadcastToPartyMembers(new PartySmallWindowUpdate(getPartyMembers().get(0)));
				}
			}
			else
			{
				player.sendPacket(new SystemMessage(SystemMessage.YOU_CAN_TRANSFER_RIGHTS_ONLY_TO_ANOTHER_PARTY_MEMBER));
			}
		}
		
	}
	
	/**
	 * finds a player in the party by name
	 * @param name
	 * @return
	 */
	private L2PcInstance getPlayerByName(String name) 
	{
		for(L2PcInstance member : getPartyMembers())
		{
			if (member.getName().equals(name)) return member;
		}
		return null;
	}
	
	/**
	 * Oust player from party
	 * @param player
	 */
	public void oustPartyMember(L2PcInstance player) 
	{
		if (getPartyMembers().contains(player)) 
		{
			if (isLeader(player)) 
			{
				removePartyMember(player);
				if (getPartyMembers().size() > 1)
				{
					SystemMessage msg = new SystemMessage(SystemMessage.S1_HAS_BECOME_A_PARTY_LEADER);
					msg.addString(getPartyMembers().get(0).getName());
					broadcastToPartyMembers(msg);
					broadcastToPartyMembers(new PartySmallWindowUpdate(getPartyMembers().get(0)));
				}
			} 
			else 
			{
				removePartyMember(player);
			}
            
            if (getPartyMembers().size() == 1)
            {
                // No more party needed
                _members = null;
                _validMembers = null;
            }
		}
	}
	
	/**
	 * Oust player from party
	 * Overloaded method that takes player's name as parameter
	 * @param name
	 */
	public void oustPartyMember(String name) 
	{
		L2PcInstance player = getPlayerByName(name);
		
		if (player != null) 
		{
			if (isLeader(player)) 
			{
				removePartyMember(player);
                if (getPartyMembers().size() > 1)
                {
    				SystemMessage msg = new SystemMessage(SystemMessage.S1_HAS_BECOME_A_PARTY_LEADER);
    				msg.addString(getPartyMembers().get(0).getName());
    				broadcastToPartyMembers(msg);
    				broadcastToPartyMembers(new PartySmallWindowUpdate(getPartyMembers().get(0)));
                }
			} 
			else 
			{
				removePartyMember(player);
			}
            
            if (getPartyMembers().size() == 1)
            {
                // No more party needed
                _members = null;
                _validMembers = null;
            }
		}
	}
	
	/**
	 * dissolves entire party
	 *
	 */
	/*  [DEPRECATED]
	 private void dissolveParty() 
	 {
	 	SystemMessage msg = new SystemMessage(SystemMessage.PARTY_DISPERSED);
	 	for(int i = 0; i < _members.size(); i++) 
	 	{
	 		L2PcInstance temp = _members.get(i);
	 		temp.sendPacket(msg);
	 		temp.sendPacket(new PartySmallWindowDeleteAll());
	 		temp.setParty(null);
	 	}
	 }
	 */
	
	
	/**
	 * distribute item(s) to party members
	 * @param player
	 * @param item
	 */
	public void distributeItem(L2PcInstance player, L2ItemInstance item) 
	{
		if (item.getItemId() == 57)
		{
			distributeAdena(player, item.getCount(), player);
			ItemTable.getInstance().destroyItem("Party", item, player, null);
			return;
		}
		
		L2PcInstance target = getActualLooter(player, item.getItemId(), false, player);
		target.addItem("Party", item, player, true);
		
		// Send messages to other party members about reward
		if (item.getCount() > 1) 
		{
			SystemMessage msg = new SystemMessage(SystemMessage.S1_PICKED_UP_S2_S3);
			msg.addString(target.getName());
			msg.addItemName(item.getItemId());
			msg.addNumber(item.getCount());
			broadcastToPartyMembers(target, msg);
		}
		else
		{
			SystemMessage msg = new SystemMessage(SystemMessage.S1_PICKED_UP_S2);
			msg.addString(target.getName());
			msg.addItemName(item.getItemId());
			broadcastToPartyMembers(target, msg);
		}
	}
	
	/**
	 * distribute item(s) to party members
	 * @param player
	 * @param item
	 */
	public void distributeItem(L2PcInstance player, L2Attackable.RewardItem item, boolean spoil, L2Attackable target) 
	{
		if (item == null) return;
		
		if (item.getItemId() == 57)
		{
			distributeAdena(player, item.getCount(), target);
			return;
		}
		
		L2PcInstance looter = getActualLooter(player, item.getItemId(), spoil, target);
		
		looter.addItem(spoil?"Sweep":"Party", item.getItemId(), item.getCount(), player, true);
		
		// Send messages to other aprty members about reward
		if (item.getCount() > 1) 
		{
			SystemMessage msg = spoil ?  new SystemMessage(SystemMessage.S1_SWEEPED_UP_S2_S3) 
			                          : new SystemMessage(SystemMessage.S1_PICKED_UP_S2_S3);
			msg.addString(looter.getName());
			msg.addItemName(item.getItemId());
			msg.addNumber(item.getCount());
			broadcastToPartyMembers(looter, msg);
		}
		else
		{
			SystemMessage msg = spoil ?  new SystemMessage(SystemMessage.S1_SWEEPED_UP_S2) 
			                          : new SystemMessage(SystemMessage.S1_PICKED_UP_S2);
			msg.addString(looter.getName());
			msg.addItemName(item.getItemId());
			broadcastToPartyMembers(looter, msg);
		}
	}
	
	/**
	 * distribute adena to party members
	 * @param adena
	 */
	public void distributeAdena(L2PcInstance player, int adena, L2Character target) 
	{
        // Get all the party members
        List<L2PcInstance> membersList = getPartyMembers();
        
        // Check the number of party members that must be rewarded
        // (The party member must be in range to receive its reward)
        List<L2PcInstance> ToReward = new FastList<L2PcInstance>();
		for(L2PcInstance member : membersList)
		{
            if (!Util.checkIfInRange(1150, target, member, true)) continue;
            ToReward.add(member);
		}
        
        // Avoid null exceptions, if any
        if (ToReward == null || ToReward.isEmpty()) return;
        
        // Now we can actually distribute the adena reward
        // (Total adena splitted by the number of party members that are in range and must be rewarded)
        int count = adena / ToReward.size();
        for (L2PcInstance member : ToReward)
            member.addAdena("Party", count, player, true);
	}
	
	/**
	 * Distribute Experience and SP rewards to L2PcInstance Party members in the known area of the last attacker.<BR><BR>
	 * 
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get the L2PcInstance owner of the L2SummonInstance (if necessary) </li>
	 * <li>Calculate the Experience and SP reward distribution rate </li>
	 * <li>Add Experience and SP to the L2PcInstance </li><BR><BR>
	 * 
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T GIVE rewards to L2PetInstance</B></FONT><BR><BR>
	 * 
	 * @param xpReward The Experience reward to distribute
	 * @param spReward The SP reward to distribute
	 * @param rewardedMembers The list of L2PcInstance to reward and LSummonInstance whose owner must be reward
	 * @param lastAttacker The L2Character that has killed the L2Attackable
	 * 
	 */
	public void distributeXpAndSp(long xpReward, int spReward, List<L2Character> rewardedMembers, L2Character lastAttacker) 
	{
		L2SummonInstance summon = null;
		L2PcInstance owner      = null;
		
		float penalty;
		double sqLevel;
		double preCalculation;
		
		xpReward *= getExpBonus();
		spReward *= getSpBonus();
		
		double sqLevelSum = 0;
		for (L2PcInstance character : _validMembers)
			sqLevelSum += (character.getLevel() * character.getLevel());
		
		List<L2Character> ToRemove = new FastList<L2Character>();
		
		// Go through the members that must be rewarded
		synchronized(rewardedMembers)
		{
			for (L2Character member : rewardedMembers)
			{
				if(ToRemove != null && ToRemove.contains(member)) continue;
				
				// Check if members are near the last Attacker
				if ((member instanceof L2PlayableInstance && lastAttacker.getKnownList().knowsObject(member)) || lastAttacker.getKnownList().knowsThePlayer((L2PcInstance)member))
				{
					penalty = 0;
					
					// The reward can only be given to a L2PcInstance that owns the L2SummonInstance
					// The Summon and the L2PcInstance are in the rewarded members table, but it's impossible to know which one is first
					// That's why only one of them must be used (remove the other during the calculation of the first)
					if (member instanceof L2SummonInstance)
					{
						summon  = (L2SummonInstance)member;
						penalty = summon.getExpPenalty();
						owner   = summon.getOwner();
						
						// Remove the owner from the rewarded members
						if (rewardedMembers.contains(owner)) ToRemove.add(owner);
					}
					else if (member instanceof L2PcInstance)
					{ 
						owner   = (L2PcInstance)member;
						
						// The L2SummonInstance penalty
						if (owner.getPet() instanceof L2SummonInstance)
						{
							summon     = (L2SummonInstance)owner.getPet();
							penalty    = summon.getExpPenalty();
							
							// Remove the L2SummonInstance from the rewarded members
							if (rewardedMembers.contains(owner.getPet()))
							{
								ToRemove.add(summon);
							}
						}
					}
					
					// Calculate and add the EXP and SP reward to the member
					if (_validMembers.contains(member))
					{
						sqLevel = member.getLevel() * member.getLevel();
						preCalculation = (sqLevel / sqLevelSum) * (1 - penalty);
						
						// Add the XP/SP points to the requested party member
						member.addExpAndSp(Math.round(member.calcStat(Stats.EXPSP_RATE, xpReward * preCalculation, null, null)), 
						                   (int)member.calcStat(Stats.EXPSP_RATE, spReward * preCalculation, null, null));
					}
				}
			}
		}
	}
	
	/**
	 * Calculates and gives final XP and SP rewards to the party member.<BR>
	 * This method takes in consideration number of members, members' levels, rewarder's level and bonus modifier for the actual party.<BR><BR>
	 * 
	 * @param member is the L2Character to be rewarded
	 * @param xpReward is the total amount of XP to be "splited" and given to the member
	 * @param spReward is the total amount of SP to be "splited" and given to the member
	 * @param penalty is the penalty that must be applied to the XP rewards of the requested member
	 */
	
	/**
	 * refresh party level
	 *
	 */
	public void recalculatePartyLevel() 
	{
		int newLevel = 0;
		for (L2PcInstance member : getPartyMembers())
		{
			if (member.getLevel() > newLevel)
				newLevel = member.getLevel();
		}
		_partyLvl = newLevel;
		
		recalculateValidMembers();
	}
	
	/**
	 * refresh party valid members
	 *
	 */
	public void recalculateValidMembers()
	{
		_validMembers.clear();
		for (L2PcInstance member : getPartyMembers())
		{
			if (checkMemberValidity(member)) _validMembers.add(member);
		}
	}
	
	private boolean checkMemberValidity(L2PcInstance member)
	{
//		Fixed LevelDiff cutoff point
		if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("level"))
		{
			return (getLevel() - member.getLevel() <= Config.PARTY_XP_CUTOFF_LEVEL);
		}
//		Fixed MinPercentage cutoff point
		else if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("percentage")) 
		{
			int sqLevel = member.getLevel() * member.getLevel();
			int sqLevelSum = 0;
			for (L2PcInstance character : getPartyMembers())
			{
				sqLevelSum += (character.getLevel() * character.getLevel());
			}
			return (sqLevel * 100 >= sqLevelSum * Config.PARTY_XP_CUTOFF_PERCENT);
		}
//		Automatic cutoff method
		else if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("auto")) 
		{
			int sqLevel = member.getLevel() * member.getLevel();
			int sqLevelSum = 0;
			for (L2PcInstance character : getPartyMembers())
			{
				sqLevelSum += (character.getLevel() * character.getLevel());
			}
			
			int i = getMemberCount() -1;
			if (i < 1 ) return true;
			if (i >= _bonusExpSp.length) i = _bonusExpSp.length -1;
			
			return (sqLevel >= sqLevelSum * (1-1/(1 +_bonusExpSp[i] -_bonusExpSp[i-1])));
		}
		return true;
	}
	
	private double getBaseExpSpBonus()
	{
		int i = getValidMemberCount() -1;
		if (i < 1 ) return 1;
		if (i >= _bonusExpSp.length) i = _bonusExpSp.length -1;
		
		return _bonusExpSp[i];
	}
	
	private double getExpBonus() 
	{
		if(_validMembers.size() < 2)
		{
			//not is a valid party		    
			return getBaseExpSpBonus(); 
		}
		else
		{
			return getBaseExpSpBonus() * Config.RATE_PARTY_XP;
		}
	}
	
	private double getSpBonus() 
	{ 
		if(_validMembers.size() < 2)
		{	
			//not is a valid party
			return getBaseExpSpBonus();
		}			    	 
		else
		{
			return getBaseExpSpBonus() * Config.RATE_PARTY_SP;
		}
	}
	
	public int getLevel() { return _partyLvl; }
	
	public int getLootDistribution() { return _itemDistribution; }
}
