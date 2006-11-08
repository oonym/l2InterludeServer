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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;
import javolution.util.FastTable;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.Olympiad;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.MapRegionTable;
import net.sf.l2j.gameserver.SkillTable;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.MapRegionTable.TeleportWhereType;
import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2AttackableAI;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.handler.SkillHandler;
import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.appearance.CharAppearance;
import net.sf.l2j.gameserver.model.actor.instance.L2ArtefactInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2BoatInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance.SkillDat;
import net.sf.l2j.gameserver.model.actor.knownlist.CharKnownList;
import net.sf.l2j.gameserver.model.actor.knownlist.ObjectKnownList.KnownListAsynchronousUpdateTask;
import net.sf.l2j.gameserver.model.actor.stat.CharStat;
import net.sf.l2j.gameserver.model.actor.status.CharStatus;
import net.sf.l2j.gameserver.model.entity.Zone;
import net.sf.l2j.gameserver.model.entity.ZoneType;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.Attack;
import net.sf.l2j.gameserver.serverpackets.ChangeMoveType;
import net.sf.l2j.gameserver.serverpackets.ChangeWaitType;
import net.sf.l2j.gameserver.serverpackets.ExOlympiadSpelledInfo;
import net.sf.l2j.gameserver.serverpackets.MagicEffectIcons;
import net.sf.l2j.gameserver.serverpackets.MagicSkillCanceld;
import net.sf.l2j.gameserver.serverpackets.MagicSkillLaunched;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.NpcInfo;
import net.sf.l2j.gameserver.serverpackets.PartySpelled;
import net.sf.l2j.gameserver.serverpackets.PetInfo;
import net.sf.l2j.gameserver.serverpackets.Revive;
import net.sf.l2j.gameserver.serverpackets.ServerBasePacket;
import net.sf.l2j.gameserver.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.serverpackets.SocialAction;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.StopMove;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.TargetUnselected;
import net.sf.l2j.gameserver.serverpackets.TeleportToLocation;
import net.sf.l2j.gameserver.skills.Calculator;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.Func;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.templates.L2CharTemplate;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.gameserver.util.Util;
/**
 * Mother class of all character objects of the world (PC, NPC...)<BR><BR>
 *
 * L2Character :<BR><BR>
 * <li>L2CastleGuardInstance</li>
 * <li>L2DoorInstance</li>
 * <li>L2NpcInstance</li>
 * <li>L2PlayableInstance </li><BR><BR>
 *
 *
 * <B><U> Concept of L2CharTemplate</U> :</B><BR><BR>
 * Each L2Character owns generic and static properties (ex : all Keltir have the same number of HP...).
 * All of those properties are stored in a different template for each type of L2Character.
 * Each template is loaded once in the server cache memory (reduce memory use).
 * When a new instance of L2Character is spawned, server just create a link between the instance and the template.
 * This link is stored in <B>_template</B><BR><BR>
 *
 *
 * @version $Revision: 1.53.2.45.2.34 $ $Date: 2005/04/11 10:06:08 $
 */
public abstract class L2Character extends L2Object
{
	protected static final Logger _log = Logger.getLogger(L2Character.class.getName());
	
	// =========================================================
	// Data Field
	private CharAppearance _Appearance;
	private List<L2Character> _AttackByList;
	private L2Character _AttackingChar;
	private L2Skill _AttackingCharSkill;
	private boolean _IsAffraid                              = false; // Flee in a random direction
	private boolean _IsConfused                             = false; // Attack anyone randomly
	private boolean _IsFakeDeath                            = false; // Fake death
	private boolean _IsFlying                               = false; //Is flying Wyvern?
	private boolean _IsMuted                                = false; // Cannot use magic
	private boolean _IsKilledAlready                        = false;
	private boolean _IsImobilised                           = false;
	private boolean _IsOverloaded                           = false; // the char is carrying too much
	private boolean _IsParalyzed                            = false;
	private boolean _IsRiding                               = false; //Is Riding strider?
	private boolean _IsPendingRevive                        = false;
	private boolean _IsRooted                               = false; // Cannot move until root timed out
	private boolean _IsRunning                              = false;
	private boolean _IsSleeping                             = false; // Cannot move/attack until sleep timed out or monster is attacked
	private boolean _IsStunned                              = false; // Cannot move/attack until stun timed out
	private boolean _IsTeleporting                          = false;
	private L2Character _LastBuffer							= null;
	private int _LastHealAmount								= 0;
	private CharStat _Stat;
	private CharStatus _Status;
	private L2CharTemplate _Template;                       // The link on the L2CharTemplate object containing generic and static properties of this L2Character type (ex : Max HP, Speed...)
	private String _Title;
	private String _aiClass = "default";
	
	/** Table of Calculators containing all used calculator */
	private Calculator[] _Calculators;
	
	/** FastMap(Integer, L2Skill) containing all skills of the L2Character */
	protected final Map<Integer, L2Skill> _Skills;
	
	// =========================================================
	// Constructor
	/**
	 * Constructor of L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * Each L2Character owns generic and static properties (ex : all Keltir have the same number of HP...).
	 * All of those properties are stored in a different template for each type of L2Character.
	 * Each template is loaded once in the server cache memory (reduce memory use).
	 * When a new instance of L2Character is spawned, server just create a link between the instance and the template
	 * This link is stored in <B>_template</B><BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set the _template of the L2Character </li>
	 * <li>Set _overloaded to false (the charcater can take more items)</li><BR><BR>
	 *
	 * <li>If L2Character is a L2NPCInstance, copy skills from template to object</li>
	 * <li>If L2Character is a L2NPCInstance, link _calculators to NPC_STD_CALCULATOR</li><BR><BR>
	 *
	 * <li>If L2Character is NOT a L2NPCInstance, create an empty _skills slot</li>
	 * <li>If L2Character is a L2PcInstance or L2Summon, copy basic Calculator set to object</li><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 * @param template The L2CharTemplate to apply to the object
	 */
	public L2Character(int objectId, L2CharTemplate template)
	{
		super(objectId);
		super.setKnownList(new CharKnownList(new L2Character[]{this}));
		
		// Set its template to the new L2Character
		_Template = template;
		
		if (template != null && this instanceof L2NpcInstance)
		{
			// Copy the Standard Calcultors of the L2NPCInstance in _calculators
			_Calculators = NPC_STD_CALCULATOR;
			
			// Copy the skills of the L2NPCInstance from its template to the L2Character Instance
			// The skills list can be affected by spell effects so it's necessary to make a copy
			// to avoid that a spell affecting a L2NPCInstance, affects others L2NPCInstance of the same type too.
			_Skills = ((L2NpcTemplate)template).getSkills();
			if (_Skills != null)
			{
				for(Map.Entry<Integer, L2Skill> skill : _Skills.entrySet())
					addStatFuncs(skill.getValue().getStatFuncs(null, this));
			}
		}
		else
		{
			// Initialize the FastMap _skills to null
			_Skills = new FastMap<Integer,L2Skill>();
			
			// If L2Character is a L2PcInstance or a L2Summon, create the basic calculator set
			_Calculators = new Calculator[Stats.NUM_STATS];
			Formulas.getInstance().addFuncsToNewCharacter(this);
		}
	}
	
	// =========================================================
	// Event - Public
	/**
	 * Remove the L2Character from the world when the decay task is launched.<BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from _allObjects of L2World </B></FONT><BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packets to players</B></FONT><BR><BR>
	 *
	 */
	public void onDecay()
	{
		decayMe();
	}
	
	public void onTeleported()
	{
		setIsTeleporting(false);
		
		spawnMe(getPosition().getX(), getPosition().getY(), getPosition().getZ());
		
		if (_IsPendingRevive) doRevive();
		
		// Modify the position of the pet if necessary
		if(getPet() != null) getPet().teleToLocation(getPosition().getX() + Rnd.get(-100,100), getPosition().getY() + Rnd.get(-100,100), getPosition().getZ());
	}
	
	// =========================================================
	// Method - Public
	/**
	 * Add L2Character instance that is attacking to the attacker list.<BR><BR>
	 * @param player The L2Character that attcks this one
	 */
	public void addAttackerToAttackByList (L2Character player)
	{
		if (player == null || player == this || getAttackByList() == null || getAttackByList().contains(player)) return;
		getAttackByList().add(player);
	}
	
	/**
	 * Send a packet to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>.
	 * In order to inform other players of state modification on the L2Character, server just need to go through _knownPlayers to send Server->Client Packet<BR><BR>
	 *
	 */
	public final void broadcastPacket(ServerBasePacket mov)
	{
		sendPacket(mov);
		
		if (getKnownList().getKnownPlayers() == null) return;
		
		Collection<L2PcInstance> knownPlayers = getKnownList().getKnownPlayers();
		
		if (knownPlayers == null) return;
		
		if (Config.DEBUG) _log.fine("players to notify:" + knownPlayers.size() + " packet:"+mov.getType());
		
        try {
        	for (L2PcInstance player : knownPlayers)
        	{
        		if (player == null || player == this) continue;
        		player.sendPacket(mov);
        		//if(Config.DEVELOPER && !isInsideRadius(player, 3500, false, false)) _log.warning("broadcastPacket: Too far player see event!");
        	}
        } catch (Exception e) { } // this catches occasional NPEs while knownlist is unsynchronized
	}
	
	/**
	 * Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Create the Server->Client packet StatusUpdate with current HP and MP </li>
	 * <li>Send the Server->Client packet StatusUpdate with current HP and MP to all
	 * L2Character called _statusListener that must be informed of HP/MP updates of this L2Character </li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND CP information</B></FONT><BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance : Send current HP,MP and CP to the L2PcInstance and only current HP, MP and Level to all other L2PcInstance of the Party</li><BR><BR>
	 *
	 */
	public void broadcastStatusUpdate()
	{
		if (getStatus().getStatusListener() == null || getStatus().getStatusListener().isEmpty()) return;
		
		// Create the Server->Client packet StatusUpdate with current HP and MP
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_HP, (int)getStatus().getCurrentHp());
		su.addAttribute(StatusUpdate.CUR_MP, (int)getStatus().getCurrentMp());
		
		// Go through the StatusListener
		// Send the Server->Client packet StatusUpdate with current HP and MP
		
		Set<L2Character> listeners = getStatus().getStatusListener();
		if (listeners != null)
			for (L2Character temp : listeners)
				temp.sendPacket(su);
	}
	
	/**
	 * Not Implemented.<BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 */
	public void sendPacket(@SuppressWarnings("unused") ServerBasePacket mov)
	{
		// default implementation
	}
	
	/**
	 * Teleport a L2Character and its pet if necessary.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Stop the movement of the L2Character</li>
	 * <li>Set the x,y,z position of the L2Object and if necessary modify its _worldRegion</li>
	 * <li>Send a Server->Client packet TeleportToLocationt to the L2Character AND to all L2PcInstance in its _KnownPlayers</li>
	 * <li>Modify the position of the pet if necessary</li><BR><BR>
	 *
	 */
	public void teleToLocation(int x, int y, int z)
	{
		// Stop movement
		stopMove(null, false);
		this.abortAttack();
		this.abortCast();
		
		setIsTeleporting(true);
		setTarget(null);
		
        if (Config.RESPAWN_RANDOM_ENABLED)
        {
            x += Rnd.get(-70, 70);
            y += Rnd.get(-70, 70);
        }
        
        z += 5;
        
		if (Config.DEBUG) 
            _log.fine("Teleporting to: " + x + ", " + y + ", " + z);
		
		// Send a Server->Client packet TeleportToLocationt to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character
		broadcastPacket(new TeleportToLocation(this, x, y, z));
		
		// Set the x,y,z position of the L2Object and if necessary modify its _worldRegion
		getPosition().setXYZ(x, y, z);
		
		decayMe();
		
		if (!(this instanceof L2PcInstance)) 
            onTeleported();
	}
	
	public void teleToLocation(Location loc) { teleToLocation(loc.getX(), loc.getY(), loc.getZ()); }
	
	public void teleToLocation(TeleportWhereType teleportWhere) { teleToLocation(MapRegionTable.getInstance().getTeleToLocation(this, teleportWhere)); }
	
	// =========================================================
	// Method - Private
	/**
	 * Launch a physical attack against a target (Simple, Bow, Pole or Dual).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get the active weapon (always equiped in the right hand) </li><BR><BR>
	 * <li>If weapon is a bow, check for arrows, MP and bow re-use delay (if necessary, equip the L2PcInstance with arrows in left hand)</li>
	 * <li>If weapon is a bow, consumme MP and set the new period of bow non re-use </li><BR><BR>
	 * <li>Get the Attack Speed of the L2Character (delay (in milliseconds) before next attack) </li>
	 * <li>Select the type of attack to start (Simple, Bow, Pole or Dual) and verify if SoulShot are charged then start calculation</li>
	 * <li>If the Server->Client packet Attack contains at least 1 hit, send the Server->Client packet Attack to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character</li>
	 * <li>Notify AI with EVT_READY_TO_ACT</li><BR><BR>
	 *
	 * @param target The L2Character targeted
	 *
	 */
	protected void doAttack(L2Character target)
	{
		if (Config.DEBUG) 
            _log.fine(this.getName()+" doAttack: target="+target);
		
		if (isAlikeDead() || target == null || (this instanceof L2NpcInstance && target.isAlikeDead()) 
                || (this instanceof L2PcInstance && target.isDead() && !target.isFakeDeath()) 
                || !getKnownList().knowsObject(target)
                || (this instanceof L2PcInstance && isDead()))
		{
			// If L2PcInstance is dead or the target is dead, the action is stoped
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			
			sendPacket(new ActionFailed());
			return;
		}
		
		if (isAttackingDisabled()) 
            return;
        
        if ((this instanceof L2PcInstance) && (((L2PcInstance)this).inObserverMode()))
        {
            ((L2PcInstance)this).sendMessage("Cant attack in observer mode");
            sendPacket(new ActionFailed());
            return;
        }
		
		// Get the active weapon instance (always equiped in the right hand)
		L2ItemInstance weaponInst = getActiveWeaponInstance();
		
		// Get the active weapon item corresponding to the active weapon instance (always equiped in the right hand)
		L2Weapon weaponItem = getActiveWeaponItem();
		
		if ((weaponItem!=null && weaponItem.getItemType() == L2WeaponType.ROD))
		{
			//	You can't make an attack with a fishing pole.
			((L2PcInstance)this).sendPacket(new SystemMessage(1472));
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

            ActionFailed af = new ActionFailed();
            sendPacket(af);
			return;
		}
        
		// Check for a bow
		if ((weaponItem != null && weaponItem.getItemType() == L2WeaponType.BOW))
		{
			//Check for arrows and MP
			if (this instanceof L2PcInstance)
			{
				// Verify if the bow can be use
				if (_disableBowAttackEndTime <= GameTimeController.getGameTicks())
				{
				    // Verify if L2PcInstance owns enough MP
					int saMpConsume = (int)getStat().calcStat(Stats.MP_CONSUME, 0, null, null);
					int mpConsume = saMpConsume == 0 ? weaponItem.getMpConsume() : saMpConsume; 

				    if (getCurrentMp() < mpConsume)
				    {
				        // If L2PcInstance doesn't have enough MP, stop the attack
				        
				        ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), 1000);
				        
				        sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_MP));
				        sendPacket(new ActionFailed());
				        return;
				    }
				    // If L2PcInstance have enough MP, the bow consummes it
				    getStatus().reduceMp(mpConsume);
				    
					// Set the period of bow non re-use
					_disableBowAttackEndTime = 5 * GameTimeController.TICKS_PER_SECOND + GameTimeController.getGameTicks();
				}
				else
				{
					// Cancel the action because the bow can't be re-use at this moment
					ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), 1000);
					
					sendPacket(new ActionFailed());
					return;
				}
				
				// Equip arrows needed in left hand and send a Server->Client packet ItemList to the L2PcINstance then return True
				if (!checkAndEquipArrows())
				{
					// Cancel the action because the L2PcInstance have no arrow
					getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
					
					sendPacket(new ActionFailed());
					sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_ARROWS));
					return;
				}
			}
			else if (this instanceof L2NpcInstance)
			{
				if (_disableBowAttackEndTime > GameTimeController.getGameTicks()) 
				    return;
			}
		}
		
		// Add the L2PcInstance to _knownObjects and _knownPlayer of the target
		target.getKnownList().addKnownObject(this);
		
		// Reduce the current CP if TIREDNESS configuration is activated
		if (Config.ALT_GAME_TIREDNESS) 
            setCurrentCp(getCurrentCp() - 10);
		
        // Recharge any active auto soulshot tasks for player (or player's summon if one exists).
		if (this instanceof L2PcInstance) 
            ((L2PcInstance)this).rechargeAutoSoulShot(true, false, false);
        else if (this instanceof L2Summon)
            ((L2Summon)this).getOwner().rechargeAutoSoulShot(true, false, true);
		
        // Verify if soulshots are charged.
        boolean wasSSCharged;
        
        if (this instanceof L2Summon && !(this instanceof L2PetInstance))
            wasSSCharged = (((L2Summon)this).getChargedSoulShot() != L2ItemInstance.CHARGED_NONE);
        else
            wasSSCharged = (weaponInst != null && weaponInst.getChargedSoulshot() != L2ItemInstance.CHARGED_NONE);

		// Get the Attack Speed of the L2Character (delay (in milliseconds) before next attack)
		int sAtk = calculateAttackSpeed(target, weaponInst);
		_attackEndTime = GameTimeController.getGameTicks();
		_attackEndTime += (sAtk / GameTimeController.MILLIS_IN_TICK);
		_attackEndTime -= 1;
        
        int ssGrade = 0;
        
        if (weaponItem != null)
            ssGrade = weaponItem.getCrystalType();
        
        // Create a Server->Client packet Attack
		Attack attack = new Attack(this, wasSSCharged, ssGrade);
		
		boolean hitted;
		
		// Set the Attacking Body part to CHEST
		setAttackingBodypart();
		
		// Select the type of attack to start
		if (weaponItem == null)
			hitted = doAttackHitSimple(attack, target);
		else if (weaponItem.getItemType() == L2WeaponType.BOW)
			hitted = doAttackHitByBow(attack, target, sAtk);
		else if (weaponItem.getItemType() == L2WeaponType.POLE)
			hitted = doAttackHitByPole(attack);
		else if (isUsingDualWeapon())
			hitted = doAttackHitByDual(attack, target);
		else
			hitted = doAttackHitSimple(attack, target);

        // Flag the attacker if it's a L2PcInstance outside a PvP area
        L2PcInstance player = null;
        
        if (this instanceof L2PcInstance)
            player = (L2PcInstance)this;
        else if (this instanceof L2Summon)
            player = ((L2Summon)this).getOwner();
        
        if (player != null) 
            player.updatePvPStatus(target);
		
		// Check if hit isn't missed
		if (!hitted)
			// Abort the attack of the L2Character and send Server->Client ActionFailed packet
			abortAttack();
        else
        {
            /* ADDED BY nexus - 2006-08-17
             * 
             * As soon as we know that our hit landed, we must discharge any active soulshots.
             * This must be done so to avoid unwanted soulshot consumption.
             */
            
            // If we didn't miss the hit, discharge the shoulshots, if any
            if (this instanceof L2Summon && !(this instanceof L2PetInstance))
                ((L2Summon)this).setChargedSoulShot(L2ItemInstance.CHARGED_NONE);
            else
                if (weaponInst != null)
                    weaponInst.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);

            
        	if (player != null)
        	{
        		if (player.isCursedWeaponEquiped())
        		{
                	// If hitted by a cursed weapon, Cp is reduced to 0
        			target.setCurrentCp(0);
        		} else if (player.isHero())
        		{
        			if (target instanceof L2PcInstance && ((L2PcInstance)target).isCursedWeaponEquiped())
                    	// If a cursed weapon is hitted by a Hero, Cp is reduced to 0
                		target.setCurrentCp(0);
        		}
        	}
        }
		
		// If the Server->Client packet Attack contains at least 1 hit, send the Server->Client packet Attack
		// to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character
		if (attack.hasHits()) 
			broadcastPacket(attack);
		
		// Get the Attack Reuse Delay of the L2Weapon
		int reuse = (weaponItem == null) ? 0 : weaponItem.getAttackReuseDelay();
		
		// Notify AI with EVT_READY_TO_ACT
		ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), sAtk+reuse);
	}
	
	/**
	 * Launch a Bow attack.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Calculate if hit is missed or not </li>
	 * <li>Consumme arrows </li>
	 * <li>If hit isn't missed, calculate if shield defense is efficient </li>
	 * <li>If hit isn't missed, calculate if hit is critical </li>
	 * <li>If hit isn't missed, calculate physical damages </li>
	 * <li>If the L2Character is a L2PcInstance, Send a Server->Client packet SetupGauge </li>
	 * <li>Create a new hit task with Medium priority</li>
	 * <li>Calculate and set the disable delay of the bow in function of the Attack Speed</li>
	 * <li>Add this hit to the Server-Client packet Attack </li><BR><BR>
	 *
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target The L2Character targeted
	 * @param sAtk The Attack Speed of the attacker
	 *
	 * @return True if the hit isn't missed
	 *
	 */
	private boolean doAttackHitByBow(Attack attack, L2Character target, int sAtk)
	{
		int damage1 = 0;
		boolean shld1 = false;
		boolean crit1 = false;
		
		// Calculate if hit is missed or not
		boolean miss1 = Formulas.getInstance().calcHitMiss(this, target);
		
		// Consumme arrows
		reduceArrowCount();
		
		_move = null;
		
		// Check if hit isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient
			shld1 = Formulas.getInstance().calcShldUse(this, target);
			
			// Calculate if hit is critical
			crit1 = Formulas.getInstance().calcCrit(getStat().getCriticalHit(target, null));
			
			// Calculate physical damages
			damage1 = (int)Formulas.getInstance().calcPhysDam(this, target, null, shld1, crit1, false, attack._soulshot);
		}
		
		// Calculate the bow re-use delay
		int reuse;
		
		L2Weapon bow = getActiveWeaponItem();
		
		if (bow == null)
			reuse = 1500;
		else
			reuse = bow.getAttackReuseDelay();
		reuse *= getStat().getReuseModifier(target);
		
		// Check if the L2Character is a L2PcInstance
		if (this instanceof L2PcInstance)
		{
			// Send a system message
			sendPacket(new SystemMessage(SystemMessage.GETTING_READY_TO_SHOOT_AN_ARROW));
			
			// Send a Server->Client packet SetupGauge
			SetupGauge sg = new SetupGauge(SetupGauge.RED, sAtk+reuse);
			sendPacket(sg);
		}
		
		// Create a new hit task with Medium priority
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, attack._soulshot, shld1), sAtk);
		
		// Calculate and set the disable delay of the bow in function of the Attack Speed
		_disableBowAttackEndTime = (sAtk+reuse)/GameTimeController.MILLIS_IN_TICK + GameTimeController.getGameTicks();
		
		// Add this hit to the Server-Client packet Attack
		attack.addHit(target, damage1, miss1, crit1, shld1);
		
		// Return true if hit isn't missed
		return !miss1;
	}
	
	/**
	 * Launch a Dual attack.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Calculate if hits are missed or not </li>
	 * <li>If hits aren't missed, calculate if shield defense is efficient </li>
	 * <li>If hits aren't missed, calculate if hit is critical </li>
	 * <li>If hits aren't missed, calculate physical damages </li>
	 * <li>Create 2 new hit tasks with Medium priority</li>
	 * <li>Add those hits to the Server-Client packet Attack </li><BR><BR>
	 *
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target The L2Character targeted
	 *
	 * @return True if hit 1 or hit 2 isn't missed
	 *
	 */
	private boolean doAttackHitByDual(Attack attack, L2Character target)
	{
		int damage1 = 0;
		int damage2 = 0;
		boolean shld1 = false;
		boolean shld2 = false;
		boolean crit1 = false;
		boolean crit2 = false;
		
		// Calculate if hits are missed or not
		boolean miss1 = Formulas.getInstance().calcHitMiss(this, target);
		boolean miss2 = Formulas.getInstance().calcHitMiss(this, target);
		
		// Check if hit 1 isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient against hit 1
			shld1 = Formulas.getInstance().calcShldUse(this, target);
			
			// Calculate if hit 1 is critical
			crit1 = Formulas.getInstance().calcCrit(getStat().getCriticalHit(target, null));
			
			// Calculate physical damages of hit 1
			damage1 = (int)Formulas.getInstance().calcPhysDam(this, target, null, shld1, crit1, true, attack._soulshot);
			damage1 /= 2;
		}
		
		// Check if hit 2 isn't missed
		if (!miss2)
		{
			// Calculate if shield defense is efficient against hit 2
			shld2 = Formulas.getInstance().calcShldUse(this, target);
			
			// Calculate if hit 2 is critical
			crit2 = Formulas.getInstance().calcCrit(getStat().getCriticalHit(target, null));
			
			// Calculate physical damages of hit 2
			damage2 = (int)Formulas.getInstance().calcPhysDam(this, target, null, shld2, crit2, true, attack._soulshot);
			damage2 /= 2;
		}
		
		// Create a new hit task with Medium priority for hit 1
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, attack._soulshot, shld1), 650);
		
		// Create a new hit task with Medium priority for hit 2 with a higher delay
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage2, crit2, miss2, attack._soulshot, shld2), 1250);
		
		// Add those hits to the Server-Client packet Attack
		attack.addHit(target, damage1, miss1, crit1, shld1);
		attack.addHit(target, damage2, miss2, crit2, shld2);
		
		// Return true if hit 1 or hit 2 isn't missed
		return (!miss1 || !miss2);
	}
	
	/**
	 * Launch a Pole attack.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get all visible objects in a spheric area near the L2Character to obtain possible targets </li>
	 * <li>If possible target is the L2Character targeted, launch a simple attack against it </li>
	 * <li>If possible target isn't the L2Character targeted but is attakable, launch a simple attack against it </li><BR><BR>
	 *
	 * @param attack Server->Client packet Attack in which the hit will be added
	 *
	 * @return True if one hit isn't missed
	 *
	 */
	private boolean doAttackHitByPole(Attack attack)
	{
		boolean hitted = false;
		
		double angleChar, angleTarget;
		int maxRadius = 80;
		int maxAngleDiff = 45;
		
		if(getTarget() == null) 
            return false;
		
		// o1 x: 83420 y: 148158 (Giran)
		// o2 x: 83379 y: 148081 (Giran)
		// dx = -41
		// dy = -77
		// distance between o1 and o2 = 87.24
		// arctan2 = -120 (240) degree (excel arctan2(dx, dy); java arctan2(dy, dx))
		//
		// o2
		//
		//          o1 ----- (heading)
		// In the diagram above:
		// o1 has a heading of 0/360 degree from horizontal (facing East)
		// Degree of o2 in respect to o1 = -120 (240) degree
		//
		// o2          / (heading)
		//            /
		//          o1
		// In the diagram above
		// o1 has a heading of -80 (280) degree from horizontal (facing north east)
		// Degree of o2 in respect to 01 = -40 (320) degree
		
		// ===========================================================
		// Make sure that char is facing selected target
		angleTarget = Util.calculateAngleFrom(this, getTarget());
		this.setHeading((int)((angleTarget / 9.0) * 1610.0)); // = this.setHeading((int)((angleTarget / 360.0) * 64400.0));
		
		// Update char's heading degree
		angleChar = Util.convertHeadingToDegree(this.getHeading());
		double attackpercent = 85;
		int attackcountmax = 13;
		int attackcount = 0;
		if (this instanceof L2PcInstance)
		{
			if (getKnownSkill(216) == null) //216 = pole mastery skill id  
			{  
				attackcountmax = 3; //Can attack 3 monster dont have pole mastery   
			}  
			else if (getSkillLevel(216) < 9)  
			{  
				attackcountmax = 8; //Can attack 8 monster pole mastery lvl is to low   
			}  
		}   
		
		if (angleChar <= 0) 
            angleChar += 360;
		// ===========================================================
		
		L2Character target;
		for (L2Object obj : getKnownList().getKnownObjects())
		{
			//Check if the L2Object is a L2Character
			if(obj instanceof L2Character)
			{
				if (obj instanceof L2PetInstance &&
						this instanceof L2PcInstance &&
						((L2PetInstance)obj).getOwner() == ((L2PcInstance)this)) continue;
				
				if (!Util.checkIfInRange(maxRadius, this, obj, false)) continue;
                
				//otherwise hit too high/low. 650 because mob z coord sometimes wrong on hills
                if(Math.abs(obj.getZ() - this.getZ()) > 650) continue;
				angleTarget = Util.calculateAngleFrom(this, obj);
				if (
						Math.abs(angleChar - angleTarget) > maxAngleDiff &&
						Math.abs((angleChar + 360) - angleTarget) > maxAngleDiff &&     // Example: char is at 1 degree and target is at 359 degree
						Math.abs(angleChar - (angleTarget + 360)) > maxAngleDiff        // Example: target is at 1 degree and char is at 359 degree
				) continue;
				
				target = (L2Character) obj;
				
				// Launch a simple attack against the L2Character targeted
				if(!target.isAlikeDead())
				{
					attackcount += 1;
					if (attackcount <= attackcountmax)
					{
						if (target == getAI().getAttackTarget() || target.isAutoAttackable(this))
						{
							
							hitted |= doAttackHitSimple(attack, target, attackpercent);
							attackpercent /= 1.15;
						}	
					}
				}
			}
		}
        
		// Return true if one hit isn't missed
		return hitted;
	}
	
	/**
	 * Launch a simple attack.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Calculate if hit is missed or not </li>
	 * <li>If hit isn't missed, calculate if shield defense is efficient </li>
	 * <li>If hit isn't missed, calculate if hit is critical </li>
	 * <li>If hit isn't missed, calculate physical damages </li>
	 * <li>Create a new hit task with Medium priority</li>
	 * <li>Add this hit to the Server-Client packet Attack </li><BR><BR>
	 *
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target The L2Character targeted
	 *
	 * @return True if the hit isn't missed
	 *
	 */
	private boolean doAttackHitSimple(Attack attack, L2Character target)
	{
		return doAttackHitSimple(attack, target, 100);
	}
    
	private boolean doAttackHitSimple(Attack attack, L2Character target, double attackpercent)
	{
		int damage1 = 0;
		boolean shld1 = false;
		boolean crit1 = false;
		
		// Calculate if hit is missed or not
		boolean miss1 = Formulas.getInstance().calcHitMiss(this, target);
		
		// Check if hit isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient
			shld1 = Formulas.getInstance().calcShldUse(this, target);
			
			// Calculate if hit is critical
			crit1 = Formulas.getInstance().calcCrit(getStat().getCriticalHit(target, null));
			
			// Calculate physical damages
			damage1 = (int)Formulas.getInstance().calcPhysDam(this, target, null, shld1, crit1, false, attack._soulshot);
            
			if (attackpercent != 100)
				damage1 = (int)(damage1*attackpercent/100);
		}
		
		// Create a new hit task with Medium priority
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, attack._soulshot, shld1), 980);
		
		// Add this hit to the Server-Client packet Attack
		attack.addHit(target, damage1, miss1, crit1, shld1);
		
		// Return true if hit isn't missed
		return !miss1;
	}
	
	/**
	 * Manage the casting task (casting and interrupt time, re-use delay...) and display the casting bar and animation on client.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Verify the possibilty of the the cast : skill is a spell, caster isn't muted... </li>
	 * <li>Get the list of all targets (ex : area effects) and define the L2Charcater targeted (its stats will be used in calculation)</li>
	 * <li>Calculate the casting time (base + modifier of MAtkSpd), interrupt time and re-use delay</li>
	 * <li>Send a Server->Client packet MagicSkillUser (to diplay casting animation), a packet SetupGauge (to display casting bar) and a system message </li>
	 * <li>Disable all skills during the casting time (create a task EnableAllSkills)</li>
	 * <li>Disable the skill during the re-use delay (create a task EnableSkill)</li>
	 * <li>Create a task MagicUseTask (that will call method onMagicUseTimer) to launch the Magic Skill at the end of the casting time</li><BR><BR>
	 *
	 * @param skill The L2Skill to use
	 *
	 */
	public void doCast(L2Skill skill)
	{
		if (skill == null)
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
		if (isSkillDisabled(skill.getId()))
		{
			if (this instanceof L2PcInstance) 
            {
				SystemMessage sm = new SystemMessage(SystemMessage.S1_PREPARED_FOR_REUSE);
				sm.addSkillName(skill.getId());
				sendPacket(sm);
			}
            
			return;
		}
		
		// Check if the skill is a magic spell and if the L2Character is not muted
		if (skill.isMagic() && isMuted())
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
        //Recharge AutoSoulShot
        if (skill.useSoulShot())
        {
            if (this instanceof L2PcInstance) 
                ((L2PcInstance)this).rechargeAutoSoulShot(true, false, false);
            else if (this instanceof L2Summon)
                ((L2Summon)this).getOwner().rechargeAutoSoulShot(true, false, true);
        }
        else if (skill.useSpiritShot())
        {
            if (this instanceof L2PcInstance) 
                ((L2PcInstance)this).rechargeAutoSoulShot(false, true, false);
            else if (this instanceof L2Summon)
                ((L2Summon)this).getOwner().rechargeAutoSoulShot(false, true, true);
        }
        else if (skill.useFishShot())
        {
        	if (this instanceof L2PcInstance) 
        	  ((L2PcInstance)this).rechargeAutoSoulShot(true, false, false);
        }
        
		// Get all possible targets of the skill in a table in function of the skill target type
		L2Object[] targets = skill.getTargetList(this);
        
		if (targets == null || targets.length == 0)
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
		// Set the target of the skill in function of Skill Type and Target Type
		L2Character target = null;
        
		if(     skill.getSkillType() == SkillType.BUFF ||
				skill.getSkillType() == SkillType.HEAL ||
				skill.getSkillType() == SkillType.COMBATPOINTHEAL ||
				skill.getSkillType() == SkillType.MANAHEAL ||
				skill.getSkillType() == SkillType.REFLECT ||
				skill.getSkillType() == SkillType.SEED ||
				skill.getTargetType() == L2Skill.SkillTargetType.TARGET_SELF ||
				skill.getTargetType() == L2Skill.SkillTargetType.TARGET_PET ||
				skill.getTargetType() == L2Skill.SkillTargetType.TARGET_PARTY ||
				skill.getTargetType() == L2Skill.SkillTargetType.TARGET_CLAN || 
				skill.getTargetType() == L2Skill.SkillTargetType.TARGET_ALLY)
		{
			target = (L2Character) targets[0];
			
			if (this instanceof L2PcInstance && target instanceof L2PcInstance && target.getAI().getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
			{
				if(skill.getSkillType() == SkillType.BUFF || skill.getSkillType() == SkillType.HOT || skill.getSkillType() == SkillType.HEAL || skill.getSkillType() == SkillType.HEAL_PERCENT || skill.getSkillType() == SkillType.MANAHEAL || skill.getSkillType() == SkillType.MANAHEAL_PERCENT)
					target.setLastBuffer(this);
				
				if (((L2PcInstance)this).isInParty() && skill.getTargetType() == L2Skill.SkillTargetType.TARGET_PARTY)
				{
					for (L2PcInstance member : ((L2PcInstance)this).getParty().getPartyMembers())
						 member.setLastBuffer(this);
				}
			}
		} else
			target = (L2Character) getTarget();
		
		// AURA skills should always be using caster as target
		if (skill.getTargetType() == SkillTargetType.TARGET_AURA)
			target = this;
		
		if (target == null)
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
        setAttackingChar(this);
        setAttackingCharSkill(skill);
        
		// Get the Identifier of the skill
		int magicId = skill.getId();
		
		// Get the Display Identifier for a skill that client can't display
		int displayId = skill.getDisplayId();
		
		// Get the level of the skill
		int level = getSkillLevel(magicId);
        
		if (level < 1) 
            level = 1;
		
		// Get the casting time of the skill (base)
		int skillTime = skill.getSkillTime();
		
		// Get the delay under wich the cast can be aborted (base)
		int skillInterruptTime = skill.getSkillInterruptTime();
		
		// Calculate the casting time of the skill (base + modifier of MAtkSpd)
		skillTime = Formulas.getInstance().calcMAtkSpd(this, skill, skillTime);
		
		// Calculate the Interrupt Time of the skill (base + modifier) if the skill is a spell else 0
		if (skill.isMagic())
			skillInterruptTime = Formulas.getInstance().calcMAtkSpd(this, skill, skillInterruptTime);
		else
			skillInterruptTime = 0;
		
		// Calculate altered Cast Speed due to BSpS/SpS
		L2ItemInstance weaponInst = this.getActiveWeaponInstance();
        
		if ((weaponInst != null)&&(skill.isMagic())&&((skill.getTargetType())!=(SkillTargetType.TARGET_SELF))) {
			if ((weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
					|| (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT))
			{
				//Only takes 70% of the time to cast a BSpS/SpS cast
				skillTime = (int)(0.70 * skillTime);
				skillInterruptTime = (int)(0.70 * skillInterruptTime);
				
				//Because the following are magic skills that do not actively 'eat' BSpS/SpS,
				//I must 'eat' them here so players don't take advantage of infinite speed increase
				if ((skill.getSkillType() == SkillType.BUFF)||
						(skill.getSkillType() == SkillType.MANAHEAL)||
						(skill.getSkillType() == SkillType.RESURRECT)||
						(skill.getSkillType() == SkillType.RECALL)||
						(skill.getSkillType() == SkillType.POISON)||
						(skill.getSkillType() == SkillType.CANCEL)||
						(skill.getSkillType() == SkillType.DEBUFF)||
						(skill.getSkillType() == SkillType.PARALYZE)||
						(skill.getSkillType() == SkillType.ROOT)||
						(skill.getSkillType() == SkillType.SLEEP)||
						(skill.getSkillType() == SkillType.DOT))
				{
					weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
				}
			}
		}
		
		// Set the _castEndTime and _castInterruptTim
		_castEndTime = 10 + GameTimeController.getGameTicks() + skillTime / GameTimeController.MILLIS_IN_TICK;
		_castInterruptTime = GameTimeController.getGameTicks() + skillInterruptTime / GameTimeController.MILLIS_IN_TICK;
		
		// Init the reuse time of the skill
		int reuseDelay = (int)(skill.getReuseDelay() * getStat().getMReuseRate(skill));
        if (skill.isMagic())
            reuseDelay *= 333.0 / getMAtkSpd();
        else
            reuseDelay *= 333.0 / getPAtkSpd();
		
		// Send a Server->Client packet MagicSkillUser with target, displayId, level, skillTime, reuseDelay
		// to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character
		broadcastPacket(new MagicSkillUser(this, target, displayId, level, skillTime, reuseDelay));
		
		// Send a system message USE_S1 to the L2Character
		if (this instanceof L2PcInstance) 
        {
			SystemMessage sm = new SystemMessage(SystemMessage.USE_S1);
			sm.addSkillName(magicId);
			sendPacket(sm);
		}
		
		// Check if this skill consume mp on start casting
		int initmpcons = getStat().getMpInitialConsume(skill);
		if (initmpcons > 0)
		{
			StatusUpdate su = new StatusUpdate(getObjectId());
			getStatus().reduceMp(calcStat(Stats.MP_CONSUME_RATE,initmpcons,null,null));
			su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
			sendPacket(su);
		}
		
		// Disable the skill during the re-use delay and create a task EnableSkill with Medium priority to enable it at the end of the re-use delay
		if (reuseDelay > 10)
		{
			disableSkill(skill.getId());
			ThreadPoolManager.getInstance().scheduleAi(new EnableSkill(skill.getId()), reuseDelay);
		}
		
		// launch the magic in skillTime milliseconds
		if (skillTime > 50)
		{
			// Send a Server->Client packet SetupGauge with the color of the gauge and the casting time
			if (this instanceof L2PcInstance)
			{
				SetupGauge sg = new SetupGauge(SetupGauge.BLUE, skillTime);
				sendPacket(sg);
			}
			
			// Disable all skills during the casting time and create a task EnableAllSkills with Medium priority to enable all skills at the end of the cating time
			disableAllSkills();
			ThreadPoolManager.getInstance().scheduleAi(new EnableAllSkills(skill), skillTime);
			
			if (_skillCast != null)
			{
				_skillCast.cancel(true);
				_skillCast = null;
			}
			
			// Create a task MagicUseTask with Medium priority to launch the MagicSkill at the end of the casting time
			_skillCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill), skillTime);
		}
		else
		{
			onMagicUseTimer(targets, skill);
		}
	}
	
	/**
	 * Kill the L2Character.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set target to null and cancel Attack or Cast </li>
	 * <li>Stop movement </li>
	 * <li>Stop HP/MP/CP Regeneration task </li>
	 * <li>Stop all active skills effects in progress on the L2Character </li>
	 * <li>Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform </li>
	 * <li>Notify L2Character AI </li><BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2NpcInstance : Create a DecayTask to remove the corpse of the L2NpcInstance after 7 seconds </li>
	 * <li> L2Attackable : Distribute rewards (EXP, SP, Drops...) and notify Quest Engine </li>
	 * <li> L2PcInstance : Apply Death Penalty, Manage gain/loss Karma and Item Drop </li><BR><BR>
	 *
	 * @param killer The L2Character who killed it
	 *
	 */
	public void doDie(L2Character killer)
	{
		// Set target to null and cancel Attack or Cast
		setTarget(null);
		
		// Stop movement
		stopMove(null);
		
		// Stop HP/MP/CP Regeneration task
		getStatus().stopHpMpRegeneration();
		
		// Stop all active skills effects in progress on the L2Character
		stopAllEffects();
		
		// Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
		broadcastStatusUpdate();
		
		// Notify L2Character AI
		getAI().notifyEvent(CtrlEvent.EVT_DEAD, null);
		
		if (killer != null)
		{
			// Notify Quest of character's death
			L2NpcInstance npc = null;
			if (killer instanceof L2NpcInstance) npc = (L2NpcInstance)killer;
			for (QuestState qs: getNotifyQuestOfDeath())
			{
				qs.getQuest().notifyDeath(npc, this, qs);
			}
			getNotifyQuestOfDeath().clear();
		}
		getAttackByList().clear();
	}
	
	/** Sets HP, MP and CP and revives the L2Character. */
	public void doRevive()
	{
		if (!isTeleporting())
		{
			setIsPendingRevive(false);
			
			_Status.setCurrentCp(getMaxCp() * Config.RESPAWN_RESTORE_CP);
			_Status.setCurrentHp(getMaxHp() * Config.RESPAWN_RESTORE_HP);
			//_Status.setCurrentMp(getMaxMp() * Config.RESPAWN_RESTORE_MP);
			
			// Start broadcast status 
			broadcastPacket(new SocialAction(getObjectId(), 15)); 
			broadcastPacket(new Revive(this));
		}
		else
			setIsPendingRevive(true);
	}
	
	/** Revives the L2Character using skill. */
	public void doRevive(L2Skill skill) { doRevive(); }
	
	/**
	 * Check if the active L2Skill can be casted.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Check if the L2Character can cast (ex : not sleeping...) </li>
     * <li>Check if the target is correct </li>
	 * <li>Notify the AI with AI_INTENTION_CAST and target</li><BR><BR>
	 *
	 * @param skill The L2Skill to use
	 *
	 */
	protected void useMagic(L2Skill skill)
	{
		if (skill == null || isDead())
			return;
		
		// Check if the L2Character can cast
		if (isAllSkillsDisabled())
		{
			// must be checked by caller
			return;
		}
		
		// Ignore the passive skill request. why does the client send it anyway ??
		if (skill.isPassive())
			return;
		
		// Get the target for the skill
		L2Object target = null;
        
		switch (skill.getTargetType())
		{
			case TARGET_AURA:    // AURA, SELF should be cast even if no target has been found
			case TARGET_SELF:
				target = this;
				break;
			default:

                // Get the first target of the list
			    target = skill.getFirstOfTargetList(this);
			    break;
		}
		
		// Notify the AI with AI_INTENTION_CAST and target
		getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
        
	}
	
    
	// =========================================================
	// Property - Public
	/** 
     * Return the L2CharacterAI of the L2Character and if its null create a new one. 
     */
	public L2CharacterAI getAI()
	{
		if (_ai == null)
		{
			synchronized(this)
			{
				if (_ai == null) _ai = new L2CharacterAI(new AIAccessor());
			}
		}
		
		return _ai;
	}
	
    
	public void setAI(L2CharacterAI newAI)
	{
        L2CharacterAI oldAI = getAI(); 
        if(oldAI != null && oldAI != newAI && oldAI instanceof L2AttackableAI)
            ((L2AttackableAI)oldAI).stopAITask();
        _ai = newAI; 
    }
	
	/** Return True if the L2Character has a L2CharacterAI. */
	public boolean hasAI() { return _ai != null; }
	
	public final CharAppearance getAppearance()
	{
		if (_Appearance == null) _Appearance = new CharAppearance(new L2Character[]{this});
		return _Appearance;
	}
	
	/** Return True if the L2Character is RaidBoss or his minion. */
	public boolean isRaid()
	{
		return false; 
	}
	
	/** Return a list of L2Character that attacked. */
	public final List<L2Character> getAttackByList ()
	{
		if (_AttackByList == null) _AttackByList = new FastList<L2Character>();
		return _AttackByList;
	}
	
	public final L2Character getAttackingChar() { return _AttackingChar; }
	/**
	 * Set _attackingChar to the L2Character that attacks this one.<BR><BR>
	 * @param player The L2Character that attcks this one
	 */
	public final void setAttackingChar (L2Character player)
	{
		if (player == null || player == this) return;
		_AttackingChar = player;
		addAttackerToAttackByList(player);
	}
	
	public final L2Skill getAttackingCharSkill() { return _AttackingCharSkill; }
	/**
	 * Set _attackingCharSkill to the L2Skill used against this L2Character.<BR><BR>
	 * @param skill The L2Skill used against this L2Character
	 */
	public void setAttackingCharSkill (L2Skill skill) { _AttackingCharSkill = skill; }
	
	public final boolean isAffraid() { return _IsAffraid; }
	public final void setIsAffraid(boolean value) { _IsAffraid = value; }
	
	/** Return True if the L2Character is dead or use fake death.  */
	public final boolean isAlikeDead() { return isFakeDeath() || !(getCurrentHp() > 0.5); }
	
	/** Return True if the L2Character can't use its skills (ex : stun, sleep...). */
	public final boolean isAllSkillsDisabled() { return _allSkillsDisabled || isStunned() || isSleeping() || isParalyzed(); }
	
	/** Return True if the L2Character can't attack (stun, sleep, attackEndTime, fakeDeath, paralyse). */
	public final boolean isAttackingDisabled() { return isStunned() || isSleeping() || _attackEndTime > GameTimeController.getGameTicks() || isFakeDeath() || isParalyzed(); }
	
	public final Calculator[] getCalculators() { return _Calculators; }
	
	public final boolean isConfused() { return _IsConfused; }
	public final void setIsConfused(boolean value) { _IsConfused = value; }
	
	/** Return True if the L2Character is dead. */
	public final boolean isDead() { return !(isFakeDeath()) && !(getCurrentHp() > 0.5); }
	
	public final boolean isFakeDeath() { return _IsFakeDeath; }
	public final void setIsFakeDeath(boolean value) { _IsFakeDeath = value; }
	
	/** Return True if the L2Character is flying. */
	public final boolean isFlying() { return _IsFlying; }
	/** Set the L2Character flying mode to True. */
	public final void setIsFlying(boolean mode) { _IsFlying = mode; }
	
	public boolean isImobilised() { return _IsImobilised; }
	public void setIsImobilised(boolean value){ _IsImobilised = value; }
	
	public final boolean isKilledAlready() { return _IsKilledAlready; }
	public final void setIsKilledAlready(boolean value) { _IsKilledAlready = value; }
	
	public final boolean isMuted() { return _IsMuted; }
	public final void setIsMuted(boolean value) { _IsMuted = value; }
	
	/** Return True if the L2Character can't move (stun, root, sleep, overload, paralyzed). */
	public final boolean isMovementDisabled() { return isStunned() || isRooted() || isSleeping() || isOverloaded() || isParalyzed() || isImobilised() || isFakeDeath(); }
	
	/** Return True if the L2Character can be controlled by the player (confused, affraid). */
	public final boolean isOutOfControl() { return isConfused() || isAffraid(); }
	
	public final boolean isOverloaded() { return _IsOverloaded; }
	/** Set the overloaded status of the L2Character is overloaded (if True, the L2PcInstance can't take more item). */
	public final void setIsOverloaded(boolean value) { _IsOverloaded = value; }
	
	public final boolean isParalyzed() { return _IsParalyzed; }
	public final void setIsParalyzed(boolean value) { _IsParalyzed = value; }
	
	public final boolean isPendingRevive() { return isDead() && _IsPendingRevive; }
	public final void setIsPendingRevive(boolean value) { _IsPendingRevive = value; }
	
	/**
	 * Return the L2Summon of the L2Character.<BR><BR>
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 */
	public L2Summon getPet() { return null; }
	
	/** Return True if the L2Character is ridding. */
	public final boolean isRiding() { return _IsRiding; }
	/** Set the L2Character riding mode to True. */
	public final void setIsRiding(boolean mode) { _IsRiding = mode; }
	
	public final boolean isRooted() { return _IsRooted; }
	public final void setIsRooted(boolean value) { _IsRooted = value; }
	
	/** Return True if the L2Character is running. */
	public final boolean isRunning() { return _IsRunning; }
	public final void setIsRunning(boolean value)
	{
		_IsRunning = value;
		broadcastPacket(new ChangeMoveType(this));
	}
	/** Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance. */
	public final void setRunning() { if (!isRunning()) setIsRunning(true); }
	
	public final boolean isSleeping() { return _IsSleeping; }
	public final void setIsSleeping(boolean value) { _IsSleeping = value; }
	
	public final boolean isStunned() { return _IsStunned; }
	public final void setIsStunned(boolean value) { _IsStunned = value; }
	
	public final boolean isTeleporting() { return _IsTeleporting; }
	public final void setIsTeleporting(boolean value) { _IsTeleporting = value; }
	
	public boolean isUndead() { return false; }
	
	public CharKnownList getKnownList() { return ((CharKnownList)super.getKnownList()); }
	
	public CharStat getStat()
	{
		if (_Stat == null) _Stat = new CharStat(new L2Character[]{this});
		return _Stat;
	}
	public final void setStat(CharStat value) { _Stat = value; }
	
	public CharStatus getStatus()
	{
		if (_Status == null) _Status = new CharStatus(new L2Character[]{this});
		return _Status;
	}
	public final void setStatus(CharStatus value) { _Status = value; }
	
	public L2CharTemplate getTemplate() { return _Template; }
	/**
	 * Set the template of the L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * Each L2Character owns generic and static properties (ex : all Keltir have the same number of HP...).
	 * All of those properties are stored in a different template for each type of L2Character.
	 * Each template is loaded once in the server cache memory (reduce memory use).
	 * When a new instance of L2Character is spawned, server just create a link between the instance and the template
	 * This link is stored in <B>_template</B><BR><BR>
	 *
	 * <B><U> Assert </U> :</B><BR><BR>
	 * <li> this instanceof L2Character</li><BR><BR
	 */
	protected final void setTemplate(L2CharTemplate template) { _Template = template; }
	
	/** Return the Title of the L2Character. */
	public final String getTitle() { return _Title; }
	/** Set the Title of the L2Character. */
	public final void setTitle(String value) { _Title = value; }
	
	/** Set the L2Character movement type to walk and send Server->Client packet ChangeMoveType to all others L2PcInstance. */
	public final void setWalking() { if (isRunning()) setIsRunning(false); }
	
	/** Task lauching the function enableAllSkills() */
	class EnableAllSkills implements Runnable
	{
		L2Skill _skill;
		
		public EnableAllSkills(L2Skill skill)
		{
			_skill = skill;
		}
		
		public void run()
		{
			try
			{
				enableAllSkills();
			} catch (Throwable e) {
				_log.log(Level.SEVERE, "", e);
			}
		}
	}
	
	/** Task lauching the function enableSkill() */
	class EnableSkill implements Runnable
	{
		int _skillId;
		
		public EnableSkill(int skillId)
		{
			_skillId = skillId;
		}
		
		public void run()
		{
			try
			{
				enableSkill(_skillId);
			} catch (Throwable e) {
				_log.log(Level.SEVERE, "", e);
			}
		}
	}
	
	/**
	 * Task lauching the function onHitTimer().<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL and send a Server->Client packet ActionFailed (if attacker is a L2PcInstance)</li>
	 * <li>If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are L2PcInstance </li>
	 * <li>If attack isn't aborted and hit isn't missed, reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary </li>
	 * <li>if attack isn't aborted and hit isn't missed, manage attack or cast break of the target (calculating rate, sending message...) </li><BR><BR>
	 *
	 */
	class HitTask implements Runnable
	{
		L2Character _hitTarget;
		int _damage;
		boolean _crit;
		boolean _miss;
		boolean _shld;
		boolean _soulshot;
		
		public HitTask(L2Character target, int damage, boolean crit, boolean miss, boolean soulshot, boolean shld)
		{
			_hitTarget = target;
			_damage = damage;
			_crit = crit;
			_shld = shld;
			_miss = miss;
			_soulshot = soulshot;
		}
		
		public void run()
		{
			try
			{
				onHitTimer(_hitTarget, _damage, _crit, _miss, _soulshot, _shld);
			}
			catch (Throwable e)
			{
				_log.severe(e.toString());
			}
		}
	}
	
	/** Task lauching the function onMagicUseTimer() */
	class MagicUseTask implements Runnable
	{
		L2Object[] _targets;
		L2Skill _skill;
		
		public MagicUseTask(L2Object[] targets, L2Skill skill)
		{
			_targets = targets;
			_skill = skill;
		}
		
		public void run()
		{
			try
			{
				onMagicUseTimer(_targets, _skill);
				_skillCast = null;
			}
			catch (Throwable e)
			{
				_log.log(Level.SEVERE, "", e);
			}
		}
	}
    
    /** Task lauching the function useMagic() */
    class QueuedMagicUseTask implements Runnable
    {
        L2PcInstance _currPlayer;
        L2Skill _queuedSkill;
        boolean _isCtrlPressed;
        boolean _isShiftPressed;
        
        public QueuedMagicUseTask(L2PcInstance currPlayer, L2Skill queuedSkill, boolean isCtrlPressed, boolean isShiftPressed)
        {
            _currPlayer = currPlayer;
            _queuedSkill = queuedSkill;
            _isCtrlPressed = isCtrlPressed;
            _isShiftPressed = isShiftPressed;
        }
        
        public void run()
        {
            try
            {
                _currPlayer.useMagic(_queuedSkill, _isCtrlPressed, _isShiftPressed);
            }
            catch (Throwable e)
            {
                _log.log(Level.SEVERE, "", e);
            }
        }
    }
	
	/** Task of AI notification */
	public class NotifyAITask implements Runnable
	{
		private final CtrlEvent _evt;
		
		NotifyAITask(CtrlEvent evt)
		{
			this._evt = evt;
		}
		
		public void run()
		{
			try
			{
				getAI().notifyEvent(_evt, null);
			}
			catch (Throwable t)
			{
				_log.log(Level.WARNING, "", t);
			}
		}
	}
	
	/** Task lauching the function stopPvPFlag() */
	class PvPFlag implements Runnable
	{
		public void run()
		{
			try
			{
				// _log.fine("Checking pvp time: " + getlastPvpAttack());
				// "lastattack: " _lastAttackTime "currenttime: "
				// System.currentTimeMillis());
				if (Math.abs(System.currentTimeMillis()-getlastPvpAttack()) > Config.PVP_TIME)
				{
					//  _log.fine("Stopping PvP");
					stopPvPFlag();
				}
				else if (Math.abs(System.currentTimeMillis()-getlastPvpAttack()) > Config.PVP_TIME - 5000)
				{
					updatePvPFlag(2);
				}
				else
				{
					updatePvPFlag(1);
					// Start a new PvP timer check
					//checkPvPFlag();
				}
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "error in pvp flag task:", e);
			}
		}
	}
	// =========================================================
	
	
	
	
	
	
	
	
	
	
	
	// =========================================================
	// Abnormal Effect - NEED TO REMOVE ONCE L2CHARABNORMALEFFECT IS COMPLETE
	// Data Field
	/** Map 16 bits (0x0000) containing all abnormal effect in progress */
	private short _AbnormalEffects;
	
	/**
	 * FastTable containing all active skills effects in progress of a L2Character.
	 * The Integer key of this FastMap is the L2Skill Identifier that has created the effect.
	 */
    class EffectItem {
        public int _skill;
        public L2Effect _effect;
        EffectItem(int skill, L2Effect effect) {_skill=skill; _effect=effect;}
        int getSkill() { return _skill; }
        L2Effect getEffect() { return _effect; }
    }
	private FastTable<EffectItem> _effects;
	
	/** The table containing the List of all stacked effect in progress for each Stack group Identifier */
	protected Map<String, List<Integer>> _stackedEffects;
	
	/** Table EMPTY_EFFECTS shared by all L2Character without effects in progress */
	private static final L2Effect[] EMPTY_EFFECTS = new L2Effect[0];
	
	public static final int ABNORMAL_EFFECT_BLEEDING   = 0x0001; // not sure
	public static final int ABNORMAL_EFFECT_POISON     = 0x0002;
	public static final int ABNORMAL_EFFECT_UNKNOWN_3  = 0x0004;
	public static final int ABNORMAL_EFFECT_UNKNOWN_4  = 0x0008;
	public static final int ABNORMAL_EFFECT_UNKNOWN_5  = 0x0010;
	public static final int ABNORMAL_EFFECT_UNKNOWN_6  = 0x0020;
	public static final int ABNORMAL_EFFECT_STUN       = 0x0040;
	public static final int ABNORMAL_EFFECT_SLEEP      = 0x0080;
	public static final int ABNORMAL_EFFECT_MUTED      = 0x0100; // not sure
	public static final int ABNORMAL_EFFECT_ROOT       = 0x0200;
	public static final int ABNORMAL_EFFECT_HOLD_1     = 0x0400;
	public static final int ABNORMAL_EFFECT_HOLD_2     = 0x0800;
	public static final int ABNORMAL_EFFECT_UNKNOWN_13 = 0x1000;
	public static final int ABNORMAL_EFFECT_BIG_HEAD   = 0x2000;
	public static final int ABNORMAL_EFFECT_FLAME      = 0x4000;
	public static final int ABNORMAL_EFFECT_UNKNOWN_16 = 0x8000;
	// FIXME : correct the value of the abnormal effect confused
	public static final int ABNORMAL_EFFECT_CONFUSED   = 0x0020;
	// FIXME : correct the value of the abnormal effect affraid
	public static final int ABNORMAL_EFFECT_AFFRAID    = 0x0010;
	
	// Method - Public
	/**
	 * Launch and add L2Effect (including Stack Group management) to L2Character and update client magic icone.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>.
	 * The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR><BR>
	 *
	 * Several same effect can't be used on a L2Character at the same time.
	 * Indeed, effects are not stackable and the last cast will replace the previous in progress.
	 * More, some effects belong to the same Stack Group (ex WindWald and Haste Potion).
	 * If 2 effects of a same group are used at the same time on a L2Character, only the more efficient (identified by its priority order) will be preserve.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Add the L2Effect to the L2Character _effects</li>
	 * <li>If this effect doesn't belong to a Stack Group, add its Funcs to the Calculator set of the L2Character (remove the old one if necessary)</li>
	 * <li>If this effect has higher priority in its Stack Group, add its Funcs to the Calculator set of the L2Character (remove previous stacked effect Funcs if necessary)</li>
	 * <li>If this effect has NOT higher priority in its Stack Group, set the effect to Not In Use</li>
	 * <li>Update active skills in progress icones on player client</li><BR>
	 *
	 */
	public final void addEffect(L2Effect newEffect)
	{
		if(newEffect == null) return;
		
		synchronized (this)
		{
			if (_effects == null)
				_effects = new FastTable<EffectItem>();
			
			if (_stackedEffects == null)
				_stackedEffects = new FastMap<String, List<Integer>>();
		}
		synchronized(_effects) 
		{
			L2Effect tempEffect = null;
		
			// Remove first Buff if number of buffs > 19
			L2Skill tempskill = newEffect.getSkill(); 
			if (getBuffCount() > 19 && !doesStack(tempskill) && ((
				tempskill.getSkillType() == L2Skill.SkillType.BUFF ||
                tempskill.getSkillType() == L2Skill.SkillType.DEBUFF ||
                tempskill.getSkillType() == L2Skill.SkillType.REFLECT ||
                tempskill.getSkillType() == L2Skill.SkillType.HEAL_PERCENT ||
                tempskill.getSkillType() == L2Skill.SkillType.MANAHEAL_PERCENT)&& 
                tempskill.getId() != 4267 &&
                tempskill.getId() != 4270 &&
                !(tempskill.getId() > 4360  && tempskill.getId() < 4367))
        	) removeFirstBuff(tempskill.getId());

			// Add the L2Effect to all effect in progress on the L2Character
			if (!newEffect.getSkill().isToggle()) 
			{
				int pos=0;
            	for (int i=0; i<_effects.size(); i++) 
            	{
            		if (_effects.get(i) != null) 
            		{
            			int skillid = _effects.get(i).getSkill();
            			if (!_effects.get(i).getEffect().getSkill().isToggle() && 
            				(skillid != 4267 && skillid != 4270 && !(skillid > 4360  && skillid < 4367))
            				) pos++;
            		}
            		else break;
            	}
            	_effects.add(pos, new EffectItem(newEffect.getSkill().getId(), newEffect));
			}
			else _effects.addLast(new EffectItem(newEffect.getSkill().getId(), newEffect));

			// Check if a stack group is defined for this effect
			if (newEffect.getStackType().equals("none"))
			{
				tempEffect = null;
				// Get the effect added to the L2Character _effects
				for (int i=0; i<_effects.size(); i++) 
				{
					if (_effects.get(i).getSkill() == newEffect.getSkill().getId()) 
					{
						tempEffect = _effects.get(i).getEffect();
						break;
					}
				}
			
				// Set this L2Effect to In Use
				if (tempEffect == null) return; 
				
				tempEffect.setInUse(true);
			
				// Add Funcs of this effect to the Calculator set of the L2Character
				addStatFuncs(newEffect.getStatFuncs());
			
				// Update active skills in progress icones on player client
				updateEffectIcons();
				return;
			}
		
			// Get the list of all stacked effects corresponding to the stack type of the L2Effect to add
			List<Integer> stackQueue = _stackedEffects.get(newEffect.getStackType());
		
			if (stackQueue == null)
				stackQueue = new FastList<Integer>();
		
			if (stackQueue.size() > 0)
			{
				// Get the first stacked effect of the Stack group selected
				tempEffect = null;
				for (int i=0; i<_effects.size(); i++) 
				{
					if (_effects.get(i).getSkill() == stackQueue.get(0)) 
					{
						tempEffect = _effects.get(i).getEffect();
						break;
					}
				}
			
				if (tempEffect != null)
				{
					// Remove all Func objects corresponding to this stacked effect from the Calculator set of the L2Character
					removeStatsOwner(tempEffect);
				
					// Set the L2Effect to Not In Use
					tempEffect.setInUse(false);
				}
			}
		
			// Add the new effect to the stack group selected at its position
			stackQueue = effectQueueInsert(newEffect.getSkill().getId(), newEffect.getStackOrder(), stackQueue);
        
			if (stackQueue == null) return;
		
			// Update the Stack Group table _stackedEffects of the L2Character
			_stackedEffects.put(newEffect.getStackType(), stackQueue);
		
			// Get the first stacked effect of the Stack group selected
			tempEffect = null;
			for (int i=0; i<_effects.size(); i++) 
			{
				if (_effects.get(i).getSkill() == stackQueue.get(0)) 
				{
					tempEffect = _effects.get(i).getEffect();
					break;
				}
			}
		
			// Set this L2Effect to In Use
			tempEffect.setInUse(true);
		
			// Add all Func objects corresponding to this stacked effect to the Calculator set of the L2Character
			addStatFuncs(tempEffect.getStatFuncs());
		}
		// Update active skills in progress (In Use and Not In Use because stacked) icones on client
		updateEffectIcons();
	}
	
	/**
	 * Insert an effect at the specified position in a Stack Group.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * Several same effect can't be used on a L2Character at the same time.
	 * Indeed, effects are not stackable and the last cast will replace the previous in progress.
	 * More, some effects belong to the same Stack Group (ex WindWald and Haste Potion).
	 * If 2 effects of a same group are used at the same time on a L2Character, only the more efficient (identified by its priority order) will be preserve.<BR><BR>
	 *
	 * @param id The identifier of the stacked effect to add to the Stack Group
	 * @param stackOrder The position of the effect in the Stack Group
	 * @param stackQueue The Stack Group in wich the effect must be added
	 *
	 */
	private List<Integer> effectQueueInsert(int id, @SuppressWarnings("unused") float stackOrder, List<Integer> stackQueue)
	{
		// Get the L2Effect corresponding to the Effect Identifier from the L2Character _effects
		if(_effects == null) 
            return null;

        L2Effect tempEffect = null;
        for (int i=0; i<_effects.size(); i++) {
            if (_effects.get(i).getSkill() == id) {
                tempEffect = _effects.get(i).getEffect();
                break;
            }
        }

		// Create an Iterator to go through the list of stacked effects in progress on the L2Character
		Iterator<Integer> queueIterator = stackQueue.iterator();

		int i = 0;
		while (queueIterator.hasNext()) {
            int cur = queueIterator.next();
            for (int z=0; z<_effects.size(); z++) {
                if (_effects.get(z).getSkill() == cur) {
                    if (tempEffect.getStackOrder() < _effects.get(z).getEffect().getStackOrder())
                        i++;
                    else break;
                }
            }
        }
		
		// Add the new effect to the Stack list in function of its position in the Stack group
		stackQueue.add(i, id);
		
		// skill.exit() could be used, if the users don't wish to see "effect 
		// removed" always when a timer goes off, even if the buff isn't active 
		// any more (has been replaced). but then check e.g. npc hold and raid petrify.
		if (Config.EFFECT_CANCELING && stackQueue.size() > 1) 
		{
			// only keep the current effect, cancel other effects
			for (int n=0; n<_effects.size(); n++) 
			{
				if (_effects.get(n).getSkill() == stackQueue.get(1)) 
				{
					_effects.remove(n);
					break;
				}
			}
			stackQueue.remove(1); 
		}
		
		return stackQueue;
	}
	
	/**
	 * Stop and remove L2Effect (including Stack Group management) from L2Character and update client magic icone.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>.
	 * The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR><BR>
	 *
	 * Several same effect can't be used on a L2Character at the same time.
	 * Indeed, effects are not stackable and the last cast will replace the previous in progress.
	 * More, some effects belong to the same Stack Group (ex WindWald and Haste Potion).
	 * If 2 effects of a same group are used at the same time on a L2Character, only the more efficient (identified by its priority order) will be preserve.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove Func added by this effect from the L2Character Calculator (Stop L2Effect)</li>
	 * <li>If the L2Effect belongs to a not empty Stack Group, replace theses Funcs by next stacked effect Funcs</li>
	 * <li>Remove the L2Effect from _effects of the L2Character</li>
	 * <li>Update active skills in progress icones on player client</li><BR>
	 *
	 */
	public final void removeEffect(L2Effect effect)
	{
		if(effect == null)
			return;

		synchronized(_effects) 
		{
			if(!effect.getInUse()) return;
			effect.setInUse(false);
			
			if (effect.getStackType() == "none")
			{
				// Remove Func added by this effect from the L2Character Calculator
				removeStatsOwner(effect);
			}
			else
			{
				if(_stackedEffects == null)
					return;
			
				// Get the list of all stacked effects corresponding to the stack type of the L2Effect to add
				List<Integer> stackQueue = _stackedEffects.get(effect.getStackType());
			
				if (stackQueue == null || stackQueue.size() < 1)
					return;
			
				// Get the Identifier of the first stacked effect of the Stack group selected
				int frontEffect = stackQueue.get(0);
			
				// Remove the effect from the Stack Group
				boolean removed = stackQueue.remove((Integer)effect.getSkill().getId());
			
				if (removed)
				{
					// Check if the first stacked effect was the effect to remove
					if (frontEffect == effect.getSkill().getId())
					{
						// Remove all its Func objects from the L2Character calculator set
						removeStatsOwner(effect);
					
						// Check if there's another effect in the Stack Group
						if (stackQueue.size() > 0)
						{
							// Add its list of Funcs to the Calculator set of the L2Character
							for (int i=0; i<_effects.size(); i++) 
							{
								if (_effects.get(i).getSkill() == stackQueue.get(0)) 
								{
									// Add its list of Funcs to the Calculator set of the L2Character
									addStatFuncs(_effects.get(i).getEffect().getStatFuncs());
									// Set the effect to In Use
									_effects.get(i).getEffect().setInUse(true);
									break;
								}
							}
						}
					}
					if (stackQueue.isEmpty())
						_stackedEffects.remove(effect.getStackType());
					else
						// Update the Stack Group table _stackedEffects of the L2Character
						_stackedEffects.put(effect.getStackType(), stackQueue);
				}
			}
		

			// Remove the active skill L2effect from _effects of the L2Character
			// The Integer key of _effects is the L2Skill Identifier that has created the effect
			for (int i=0; i<_effects.size(); i++) 
			{
				if (_effects.get(i).getSkill() == effect.getSkill().getId()) 
				{
					_effects.remove(i);
					break;
				}
			}
			
		}
		// Update active skills in progress (In Use and Not In Use because stacked) icones on client
		updateEffectIcons();
	}
	
	/**
	 * Active abnormal effects flags in the binary mask and send Server->Client UserInfo/CharInfo packet.<BR><BR>
	 */
	public final void startAbnormalEffect(short mask)
	{
		_AbnormalEffects |= mask;
		updateAbnormalEffect();
	}
	
	/**
	 * Active the abnormal effect Confused flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR><BR>
	 */
	public final void startConfused()
	{
		setIsConfused(true);
		getAI().notifyEvent(CtrlEvent.EVT_CONFUSED);
		updateAbnormalEffect();
	}
	
	/**
	 * Active the abnormal effect Fake Death flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR><BR>
	 */
	public final void startFakeDeath()
	{
		setIsFakeDeath(true);
        /* Aborts any attacks/casts if fake dead */
		abortAttack();
        abortCast();
		getAI().notifyEvent(CtrlEvent.EVT_FAKE_DEATH, null);
		broadcastPacket(new ChangeWaitType(this,ChangeWaitType.WT_START_FAKEDEATH));
	}
	
	/**
	 * Active the abnormal effect Fear flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR><BR>
	 */
	public final void startFear()
	{
		setIsAffraid(true);
		getAI().notifyEvent(CtrlEvent.EVT_AFFRAID);
		updateAbnormalEffect();
	}
	
	/**
	 * Active the abnormal effect Muted flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR><BR>
	 */
	public final void startMuted()
	{
		setIsMuted(true);
        /* Aborts any casts if muted */
        abortCast();
		getAI().notifyEvent(CtrlEvent.EVT_MUTED);
		updateAbnormalEffect();
	}
	
	/**
	 * Active the abnormal effect Root flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR><BR>
	 */
	public final void startRooted()
	{
		setIsRooted(true);
        getAI().notifyEvent(CtrlEvent.EVT_ROOTED, null);
		updateAbnormalEffect();
	}
	
	/**
	 * Active the abnormal effect Sleep flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR><BR>
	 */
	public final void startSleeping()
	{
		setIsSleeping(true);
		/* Aborts any attacks/casts if sleeped */
        abortAttack();
		abortCast();
		getAI().notifyEvent(CtrlEvent.EVT_SLEEPING, null);
		updateAbnormalEffect();
	}
	
	/**
	 * Launch a Stun Abnormal Effect on the L2Character.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Calculate the success rate of the Stun Abnormal Effect on this L2Character</li>
	 * <li>If Stun succeed, active the abnormal effect Stun flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet</li>
	 * <li>If Stun NOT succeed, send a system message Failed to the L2PcInstance attacker</li><BR><BR>
	 *
	 */
	public final void startStunning()
	{
		setIsStunned(true);
		/* Aborts any attacks/casts if stunned */
		abortAttack();
		abortCast();
		getAI().notifyEvent(CtrlEvent.EVT_STUNNED, null);
		updateAbnormalEffect();
	}
	
	/**
	 * Modify the abnormal effect map according to the mask.<BR><BR>
	 */
	public final void stopAbnormalEffect(short mask)
	{
		_AbnormalEffects &= ~mask;
		updateAbnormalEffect();
	}
	
	/**
	 * Stop all active skills effects in progress on the L2Character.<BR><BR>
	 */
	public final void stopAllEffects()
	{
		// Get all active skills effects in progress on the L2Character
		L2Effect[] effects = getAllEffects();
		if (effects == null) return;
		
		// Go through all active skills effects
		for(L2Effect e : effects)
			e.exit();
	}
	
	/**
	 * Stop a specified/all Confused abnormal L2Effect.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Delete a specified/all (if effect=null) Confused abnormal L2Effect from L2Character and update client magic icone </li>
	 * <li>Set the abnormal effect flag _confused to False </li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR><BR>
	 *
	 */
	public final void stopConfused(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2Effect.EffectType.CONFUSION);
		else
			removeEffect(effect);
		
		setIsConfused(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}
	
	/**
	 * Stop and remove the L2Effect corresponding to the L2Skill Identifier and update client magic icone.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>.
	 * The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR><BR>
	 *
	 * @param effectId The L2Skill Identifier of the L2Effect to remove from _effects
	 *
	 */
	public final void stopEffect(int effectId)
	{
		L2Effect effect = getEffect(effectId);
		if(effect != null) effect.exit();
	}
	
	/**
	 * Stop and remove all L2Effect of the selected type (ex : BUFF, DMG_OVER_TIME...) from the L2Character and update client magic icone.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>.
	 * The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove Func added by this effect from the L2Character Calculator (Stop L2Effect)</li>
	 * <li>Remove the L2Effect from _effects of the L2Character</li>
	 * <li>Update active skills in progress icones on player client</li><BR><BR>
	 *
	 * @param type The type of effect to stop ((ex : BUFF, DMG_OVER_TIME...)
	 *
	 */
	public final void stopEffects(L2Effect.EffectType type)
	{
		// Get all active skills effects in progress on the L2Character
		L2Effect[] effects = getAllEffects();
		
		if (effects == null) return;
		
		// Go through all active skills effects
		for(L2Effect e : effects)
		{
			// Stop active skills effects of the selected type
			if (e.getEffectType() == type) e.exit();
		}
	}
	
	/**
	 * Stop a specified/all Fake Death abnormal L2Effect.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Delete a specified/all (if effect=null) Fake Death abnormal L2Effect from L2Character and update client magic icone </li>
	 * <li>Set the abnormal effect flag _fake_death to False </li>
	 * <li>Notify the L2Character AI</li><BR><BR>
	 *
	 */
	public final void stopFakeDeath(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2Effect.EffectType.FAKE_DEATH);
		else
			removeEffect(effect);
		
		setIsFakeDeath(false);
		ChangeWaitType revive = new ChangeWaitType(this,ChangeWaitType.WT_STOP_FAKEDEATH);
		broadcastPacket(revive); 
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
	}
	
	/**
	 * Stop a specified/all Fear abnormal L2Effect.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Delete a specified/all (if effect=null) Fear abnormal L2Effect from L2Character and update client magic icone </li>
	 * <li>Set the abnormal effect flag _affraid to False </li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR><BR>
	 *
	 */
	public final void stopFear(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2Effect.EffectType.FEAR);
		else
			removeEffect(effect);
		
		setIsAffraid(false);
		updateAbnormalEffect();
	}
	
	/**
	 * Stop a specified/all Muted abnormal L2Effect.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Delete a specified/all (if effect=null) Muted abnormal L2Effect from L2Character and update client magic icone </li>
	 * <li>Set the abnormal effect flag _muted to False </li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR><BR>
	 *
	 */
	public final void stopMuted(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2Effect.EffectType.MUTE);
		else
			removeEffect(effect);
		
		setIsMuted(false);
		updateAbnormalEffect();
	}
	
	/**
	 * Stop a specified/all Root abnormal L2Effect.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Delete a specified/all (if effect=null) Root abnormal L2Effect from L2Character and update client magic icone </li>
	 * <li>Set the abnormal effect flag _rooted to False </li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR><BR>
	 *
	 */
	public final void stopRooting(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2Effect.EffectType.ROOT);
		else
			removeEffect(effect);
		
		setIsRooted(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}
	
	/**
	 * Stop a specified/all Sleep abnormal L2Effect.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Delete a specified/all (if effect=null) Sleep abnormal L2Effect from L2Character and update client magic icone </li>
	 * <li>Set the abnormal effect flag _sleeping to False </li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR><BR>
	 *
	 */
	public final void stopSleeping(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2Effect.EffectType.SLEEP);
		else
			removeEffect(effect);
		
		setIsSleeping(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}
	
	/**
	 * Stop a specified/all Stun abnormal L2Effect.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Delete a specified/all (if effect=null) Stun abnormal L2Effect from L2Character and update client magic icone </li>
	 * <li>Set the abnormal effect flag _stuned to False </li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR><BR>
	 *
	 */
	public final void stopStunning(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2Effect.EffectType.STUN);
		else
			removeEffect(effect);
		
		setIsStunned(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}
	
	/**
	 * Not Implemented.<BR><BR>
	 *
	 * <B><U> Overridden in</U> :</B><BR><BR>
	 * <li>L2NPCInstance</li>
	 * <li>L2PcInstance</li>
	 * <li>L2Summon</li>
	 * <li>L2DoorInstance</li><BR><BR>
	 *
	 */
	public abstract void updateAbnormalEffect();
	
	/**
	 * Update active skills in progress (In Use and Not In Use because stacked) icones on client.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress (In Use and Not In Use because stacked) are represented by an icone on the client.<BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method ONLY UPDATE the client of the player and not clients of all players in the party.</B></FONT><BR><BR>
	 *
	 */
    public final void updateEffectIcons()
    {
        updateEffectIcons(false);
    }
	public final void updateEffectIcons(boolean partyOnly)
	{
        // Create a L2PcInstance of this if needed
        L2PcInstance player = null;
        if (this instanceof L2PcInstance)
            player = (L2PcInstance)this;

        // Create a L2Summon of this if needed
        L2Summon summon = null;
        if (this instanceof L2Summon)
        {
            summon = (L2Summon)this;
            player = summon.getOwner();
        }
        
        // Create the main packet if needed
		MagicEffectIcons mi = null;
        if (!partyOnly)
            mi = new MagicEffectIcons();
        
        // Create the party packet if needed
        PartySpelled ps = null;
        if (summon != null)
            ps = new PartySpelled(summon);
        else if (player != null && player.isInParty())
            ps = new PartySpelled(player);
        
        // Create the olympiad spectator packet if needed
        ExOlympiadSpelledInfo os = null;
        if (player != null && player.isInOlympiadMode())
            os = new ExOlympiadSpelledInfo(player);
        
        if (mi == null && ps ==null && os == null)
            return; // nothing to do (should not happen) 
		
        // Add special effects
		if (player != null && mi != null)
		{
			if (player.getWeightPenalty() > 0)
				mi.addEffect(4270, player.getWeightPenalty(), -1);
			if (player.getexpertisePenalty() > 0)
				mi.addEffect(4267, 1, -1);
			if (player.getMessageRefusal())
				mi.addEffect(4269, 1, -1);
		}

        // Go through all effects if any
        L2Effect[] effects = getAllEffects();
		if (effects != null && effects.length > 0)
			for (int i = 0; i < effects.length; i++)
			{
				L2Effect effect = effects[i];
                
				if (effect == null)
					continue;
				
				if (effect.getInUse())
				{
                    if (mi != null)
                        effect.addIcon(mi);
					if (ps != null)
						effect.addPartySpelledIcon(ps);
                    if (os != null)
                        effect.addOlympiadSpelledIcon(os);
				}
			}
        
        
        // Send the packets if needed
        if (mi != null)
            sendPacket(mi);
        if (ps != null && player != null)
        {
            if (player.isInParty())
                player.getParty().broadcastToPartyMembers(player, ps);
            else
                player.sendPacket(ps);
        }
        if (os != null)
        {
            if (Olympiad.getInstance().getSpectators(player.getOlympiadGameId()) != null)
            {
                for (L2PcInstance spectator : Olympiad.getInstance().getSpectators(player.getOlympiadGameId()))
                {
                    if (spectator == null) continue;
                    spectator.sendPacket(os);
                }
            }
        }
	}
	
	// Property - Public
	/**
	 * Return a map of 16 bits (0x0000) containing all abnormal effect in progress for this L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * In Server->Client packet, each effect is represented by 1 bit of the map (ex : BLEEDING = 0x0001 (bit 1), SLEEP = 0x0080 (bit 8)...).
	 * The map is calculated by applying a BINARY OR operation on each effect.<BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Server Packet : CharInfo, NpcInfo, NpcInfoPoly, UserInfo...</li><BR><BR>
	 */
	public short getAbnormalEffect()
	{
		short ae = _AbnormalEffects;
		if (isStunned())  ae |= ABNORMAL_EFFECT_STUN;
		if (isRooted())   ae |= ABNORMAL_EFFECT_ROOT;
		if (isSleeping()) ae |= ABNORMAL_EFFECT_SLEEP;
		if (isConfused()) ae |= ABNORMAL_EFFECT_CONFUSED;
		if (isMuted())    ae |= ABNORMAL_EFFECT_MUTED;
		if (isAffraid())  ae |= ABNORMAL_EFFECT_AFFRAID;
		return ae;
	}
	
	/**
	 * Return all active skills effects in progress on the L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in <B>_effects</B>.
	 * The Integer key of _effects is the L2Skill Identifier that has created the effect.<BR><BR>
	 *
	 * @return A table containing all active skills effect in progress on the L2Character
	 *
	 */
	public final L2Effect[] getAllEffects()
	{
		// Create a copy of the effects set
		FastTable<EffectItem> effects = _effects;
		
		// If no effect found, return EMPTY_EFFECTS
		if (effects == null || effects.isEmpty()) return EMPTY_EFFECTS;
		
		// Return all effects in progress in a table
        int ArraySize = effects.size();
        L2Effect[] effectArray = new L2Effect[ArraySize];
        for (int i=0; i<ArraySize; i++) {
            if (i > effects.size() || effects.get(i) == null) break;
            effectArray[i] = effects.get(i).getEffect();
        }
		return effectArray;
	}
	
	/**
	 * Return L2Effect in progress on the L2Character corresponding to the L2Skill Identifier.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>.
	 * The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR><BR>
	 *
	 * @param index The L2Skill Identifier of the L2Effect to return from the _effects
	 *
	 * @return The L2Effect corresponding to the L2Skill Identifier
	 *
	 */
	public final L2Effect getEffect(int index)
	{
		FastTable<EffectItem> effects = _effects;
		if (effects == null) return null;

        L2Effect e;
        for (int i=0; i<effects.size(); i++) {
            e = effects.get(i).getEffect();
            if (effects.get(i).getSkill() == index) return e;
        }
        return null;
	}
	
	/**
	 * Return the first L2Effect in progress on the L2Character created by the L2Skill.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>.
	 * The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR><BR>
	 *
	 * @param skill The L2Skill whose effect must be returned
	 *
	 * @return The first L2Effect created by the L2Skill
	 *
	 */
	public final L2Effect getEffect(L2Skill skill)
	{
		FastTable<EffectItem> effects = _effects;
		if (effects == null) return null;
        
        L2Effect e;
        for (int i=0; i<effects.size(); i++) {
            e = effects.get(i).getEffect();
            if (e.getSkill() == skill) return e;
        }
		return null;
	}
	
	/**
	 * Return the first L2Effect in progress on the L2Character corresponding to the Effect Type (ex : BUFF, STUN, ROOT...).<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>.
	 * The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR><BR>
	 *
	 * @param tp The Effect Type of skills whose effect must be returned
	 *
	 * @return The first L2Effect corresponding to the Effect Type
	 *
	 */
	public final L2Effect getEffect(L2Effect.EffectType tp)
	{
		FastTable<EffectItem> effects = _effects;
		if (effects == null) return null;
		
        L2Effect e;
        for (int i=0; i<effects.size(); i++) {
            e = effects.get(i).getEffect();
            if (e.getEffectType() == tp) return e;
        }
        return null;
	}
	// =========================================================
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	// =========================================================
	// NEED TO ORGANIZE AND MOVE TO PROPER PLACE
	/** This class permit to the L2Character AI to obtain informations and uses L2Character method */
	public class AIAccessor
	{
		public AIAccessor() {}
		
		/**
		 * Return the L2Character managed by this Accessor AI.<BR><BR>
		 */
		public L2Character getActor()
		{
			return L2Character.this;
		}
		
		/**
		 * Accessor to L2Character moveToLocation() method with an interaction area.<BR><BR>
		 */
		public void moveTo(int x, int y, int z, int offset)
		{
			L2Character.this.moveToLocation(x, y, z, offset);
		}
		
		/**
		 * Accessor to L2Character moveToLocation() method without interaction area.<BR><BR>
		 */
		public void moveTo(int x, int y, int z)
		{
			L2Character.this.moveToLocation(x, y, z, 0);
		}
		
		/**
		 * Accessor to L2Character stopMove() method.<BR><BR>
		 */
		public void stopMove(L2CharPosition pos)
		{
			L2Character.this.stopMove(pos);
		}
		
		/**
		 * Accessor to L2Character doAttack() method.<BR><BR>
		 */
		public void doAttack(L2Character target)
		{
			L2Character.this.doAttack(target);
		}
		
		/**
		 * Accessor to L2Character doCast() method.<BR><BR>
		 */
		public void doCast(L2Skill skill)
		{
			L2Character.this.doCast(skill);
		}
		
		/**
		 * Create a NotifyAITask.<BR><BR>
		 */
		public NotifyAITask newNotifyTask(CtrlEvent evt)
		{
			return new NotifyAITask(evt);
		}
		
		/**
		 * Cancel the AI.<BR><BR>
		 */
		public void detachAI()
		{
			L2Character.this._ai = null;
		}
	}
	
	
	/**
	 * This class group all mouvement data.<BR><BR>
	 *
	 * <B><U> Data</U> :</B><BR><BR>
	 * <li>_moveTimestamp : Last time position update</li>
	 * <li>_xDestination, _yDestination, _zDestination : Position of the destination</li>
	 * <li>_xMoveFrom, _yMoveFrom, _zMoveFrom  : Position of the origin</li>
	 * <li>_moveStartTime : Start time of the movement</li>
	 * <li>_ticksToMove : Nb of ticks between the start and the destination</li>
	 * <li>_xSpeedTicks, _ySpeedTicks : Speed in unit/ticks</li><BR><BR>
	 *
	 * */
	public static class MoveData
	{
		// when we retrieve x/y/z we use GameTimeControl.getGameTicks()
		// if we are moving, but move timestamp==gameticks, we don't need
		// to recalculate position
		public int _moveTimestamp;
		public int _xDestination;
		public int _yDestination;
		public int _zDestination;
		public int _xMoveFrom;
		public int _yMoveFrom;
		public int _zMoveFrom;
		public int _heading;
		public int _moveStartTime;
		public int _ticksToMove;
		public float _xSpeedTicks;
		public float _ySpeedTicks;
	}
	
	
	/** Table containing all skillId that are disabled */
	protected List<Integer> _disabledSkills;
	private boolean _allSkillsDisabled;
	
//	private int _flyingRunSpeed;
//	private int _floatingWalkSpeed;
//	private int _flyingWalkSpeed;
//	private int _floatingRunSpeed;
	
	/** Movement data of this L2Character */
	protected MoveData _move;
	
	/** Orientation of the L2Character */
	private int _heading;
	
	/** L2Charcater targeted by the L2Character */
	private L2Object _target;
	
	// setted by the start of casting, in game ticks
	private int     _castEndTime;
	private int     _castInterruptTime;
	
	private boolean _inCombat;
	
	// setted by the start of attack, in game ticks
	private int     _attackEndTime;
	private int     _attacking;
	private int     _disableBowAttackEndTime;
	
	
	/** Table of calculators containing all standard NPC calculator (ex : ACCURACY_COMBAT, EVASION_RATE */
	private static final Calculator[] NPC_STD_CALCULATOR;
	static {NPC_STD_CALCULATOR = Formulas.getInstance().getStdNPCCalculators();}
	
	protected L2CharacterAI _ai;
	
	/** Future Skill Cast */
	protected Future _skillCast;
	
	/** Char Coords from Client */
	private int _clientX;
	private int _clientY;
	private int _clientZ;
	private int _clientHeading;
	
	
	
	
	
	
	/** List of all QuestState instance that needs to be notified of this character's death */
	private List<QuestState> _NotifyQuestOfDeathList = new FastList<QuestState>();
	
	/**
	 * Add QuestState instance that is to be notified of character's death.<BR><BR>
	 *
	 * @param qs The QuestState that subscribe to this event
	 *
	 */
	public void addNotifyQuestOfDeath (QuestState qs)
	{
		if (qs == null || _NotifyQuestOfDeathList.contains(qs))
			return;
		
		_NotifyQuestOfDeathList.add(qs);
	}
	
	/**
	 * Return a list of L2Character that attacked.<BR><BR>
	 */
	public final List<QuestState> getNotifyQuestOfDeath ()
	{
		if (_NotifyQuestOfDeathList == null)
			_NotifyQuestOfDeathList = new FastList<QuestState>();
		
		return _NotifyQuestOfDeathList;
	}
	
	
	
	/**
	 * Add a Func to the Calculator set of the L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>.
	 * Each Calculator (a calculator per state) own a table of Func object.
	 * A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...).
	 * To reduce cache memory use, L2NPCInstances who don't have skills share the same Calculator set called <B>NPC_STD_CALCULATOR</B>.<BR><BR>
	 *
	 * That's why, if a L2NPCInstance is under a skill/spell effect that modify one of its state, a copy of the NPC_STD_CALCULATOR
	 * must be create in its _calculators before addind new Func object.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>If _calculators is linked to NPC_STD_CALCULATOR, create a copy of NPC_STD_CALCULATOR in _calculators</li>
	 * <li>Add the Func object to _calculators</li><BR><BR>
	 *
	 * @param f The Func object to add to the Calculator corresponding to the state affected
	 */
	public final synchronized void addStatFunc(Func f)
	{
		if (f == null)
			return;
		
		
		// Check if Calculator set is linked to the standard Calculator set of NPC
		if (_Calculators == NPC_STD_CALCULATOR)
		{
			// Create a copy of the standard NPC Calculator set
			_Calculators = new Calculator[Stats.NUM_STATS];
			
			for (int i=0; i < Stats.NUM_STATS; i++)
			{
				if (NPC_STD_CALCULATOR[i] != null)
					_Calculators[i] = new Calculator(NPC_STD_CALCULATOR[i]);
			}
		}
		
		// Select the Calculator of the affected state in the Calculator set
		int stat = f._stat.ordinal();
		
		if (_Calculators[stat] == null)
			_Calculators[stat] = new Calculator();
		
		// Add the Func to the calculator corresponding to the state
		_Calculators[stat].addFunc(f);
		
	}
	
	
	/**
	 * Add a list of Funcs to the Calculator set of the L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>.
	 * Each Calculator (a calculator per state) own a table of Func object.
	 * A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...). <BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method is ONLY for L2PcInstance</B></FONT><BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Equip an item from inventory</li>
	 * <li> Learn a new passive skill</li>
	 * <li> Use an active skill</li><BR><BR>
	 *
	 * @param funcs The list of Func objects to add to the Calculator corresponding to the state affected
	 */
	public final synchronized void addStatFuncs(Func[] funcs)
	{
		for (Func f : funcs)
			addStatFunc(f);
		if (funcs.length > 0) updateStats();
	}
	
	
	/**
	 * Remove a Func from the Calculator set of the L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>.
	 * Each Calculator (a calculator per state) own a table of Func object.
	 * A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...).
	 * To reduce cache memory use, L2NPCInstances who don't have skills share the same Calculator set called <B>NPC_STD_CALCULATOR</B>.<BR><BR>
	 *
	 * That's why, if a L2NPCInstance is under a skill/spell effect that modify one of its state, a copy of the NPC_STD_CALCULATOR
	 * must be create in its _calculators before addind new Func object.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove the Func object from _calculators</li><BR><BR>
	 * <li>If L2Character is a L2NPCInstance and _calculators is equal to NPC_STD_CALCULATOR,
	 * free cache memory and just create a link on NPC_STD_CALCULATOR in _calculators</li><BR><BR>
	 *
	 * @param f The Func object to remove from the Calculator corresponding to the state affected
	 */
	public final synchronized void removeStatFunc(Func f)
	{
		if (f == null)
			return;
		
		// Select the Calculator of the affected state in the Calculator set
		int stat = f._stat.ordinal();
		
		if (_Calculators[stat] == null)
			return;
		
		// Remove the Func object from the Calculator
		_Calculators[stat].removeFunc(f);
		
		if (_Calculators[stat].size() == 0)
			_Calculators[stat] = null;
		
		
		// If possible, free the memory and just create a link on NPC_STD_CALCULATOR
		if (this instanceof L2NpcInstance)
		{
			int i = 0;
			for (; i < Stats.NUM_STATS; i++)
			{
				if (!Calculator.equalsCals(_Calculators[i], NPC_STD_CALCULATOR[i]))
					break;
			}
			
			if (i >= Stats.NUM_STATS)
				_Calculators = NPC_STD_CALCULATOR;
		}
	}
	
	
	/**
	 * Remove a list of Funcs from the Calculator set of the L2PcInstance.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>.
	 * Each Calculator (a calculator per state) own a table of Func object.
	 * A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...). <BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method is ONLY for L2PcInstance</B></FONT><BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Unequip an item from inventory</li>
	 * <li> Stop an active skill</li><BR><BR>
	 *
	 * @param funcs The list of Func objects to add to the Calculator corresponding to the state affected
	 */
	public final synchronized void removeStatFuncs(Func[] funcs)
	{
		for (Func f : funcs)
			removeStatFunc(f);
		if (funcs.length > 0) updateStats();
	}
	
	
	/**
	 * Remove all Func objects with the selected owner from the Calculator set of the L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>.
	 * Each Calculator (a calculator per state) own a table of Func object.
	 * A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...).
	 * To reduce cache memory use, L2NPCInstances who don't have skills share the same Calculator set called <B>NPC_STD_CALCULATOR</B>.<BR><BR>
	 *
	 * That's why, if a L2NPCInstance is under a skill/spell effect that modify one of its state, a copy of the NPC_STD_CALCULATOR
	 * must be create in its _calculators before addind new Func object.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove all Func objects of the selected owner from _calculators</li><BR><BR>
	 * <li>If L2Character is a L2NPCInstance and _calculators is equal to NPC_STD_CALCULATOR,
	 * free cache memory and just create a link on NPC_STD_CALCULATOR in _calculators</li><BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Unequip an item from inventory</li>
	 * <li> Stop an active skill</li><BR><BR>
	 *
	 * @param owner The Object(Skill, Item...) that has created the effect
	 */
	public final synchronized void removeStatsOwner(Object owner)
	{
		
		// Go through the Calculator set
		for (int i=0; i < _Calculators.length; i++)
		{
			if (_Calculators[i] != null)
			{
				// Delete all Func objects of the selected owner
				_Calculators[i].removeOwner(owner);
				
				if (_Calculators[i].size() == 0)
					_Calculators[i] = null;
			}
		}
		
		// If possible, free the memory and just create a link on NPC_STD_CALCULATOR
		if (this instanceof L2NpcInstance)
		{
			int i = 0;
			for (; i < Stats.NUM_STATS; i++)
			{
				if (!Calculator.equalsCals(_Calculators[i], NPC_STD_CALCULATOR[i]))
					break;
			}
			
			if (i >= Stats.NUM_STATS)
				_Calculators = NPC_STD_CALCULATOR;
		}
	}
	
	/**
	 * Return the orientation of the L2Character.<BR><BR>
	 */
	public final int getHeading()
	{
		return _heading;
	}
	
	/**
	 * Set the orientation of the L2Character.<BR><BR>
	 */
	public final void setHeading(int heading)
	{
		_heading = heading;
	}
	
	/**
	 * Get zone L2Character is currently located in
	 */
	public Zone getZone()
	{
		for (Zone zone: ZoneManager.getInstance().getZones(ZoneType.getZoneTypeName(ZoneType.ZoneTypeEnum.Town)))
			if (zone.checkIfInZone(this))return zone;
		return null;
	}
	
	/**
	 * Return the X destination of the L2Character or the X position if not in movement.<BR><BR>
	 */
	public final int getClientX()
	{
		return _clientX;
	}
	public final int getClientY()
	{
		return _clientY;
	}
	public final int getClientZ()
	{
		return _clientZ;
	}
	public final int getClientHeading()
	{
		return _clientHeading;
	}
	public final void setClientX(int val)
	{
		_clientX=val;
	}
	public final void setClientY(int val)
	{
		_clientY=val;
	}
	public final void setClientZ(int val)
	{
		_clientZ=val;
	}
	public final void setClientHeading(int val)
	{
		_clientHeading=val;
	}
	public final int getXdestination()
	{
		MoveData m = _move;
		
		if (m != null)
			return m._xDestination;
		
		return getX();
	}
	
	/**
	 * Return the Y destination of the L2Character or the Y position if not in movement.<BR><BR>
	 */
	public final int getYdestination()
	{
		MoveData m = _move;
		
		if (m != null)
			return m._yDestination;
		
		return getY();
	}
	
	/**
	 * Return the Z destination of the L2Character or the Z position if not in movement.<BR><BR>
	 */
	public final int getZdestination()
	{
		MoveData m = _move;
		
		if (m != null)
			return m._zDestination;
		
		return getZ();
	}
	
	/**
	 * Return True if the L2Character is in combat.<BR><BR>
	 */
	public final boolean isInCombat()
	{
		return _inCombat;
	}
	
	/**
	 * Return True if the L2Character is moving.<BR><BR>
	 */
	public final boolean isMoving()
	{
		return _move != null;
	}
	
	/**
	 * Return True if the L2Character is casting.<BR><BR>
	 */
	public final boolean isCastingNow()
	{
		return _castEndTime > GameTimeController.getGameTicks();
	}
	
	/**
	 * Return True if the cast of the L2Character can be aborted.<BR><BR>
	 */
	public final boolean canAbortCast()
	{
		return _castInterruptTime > GameTimeController.getGameTicks();
	}
	
	/**
	 * Return True if the L2Character is attacking.<BR><BR>
	 */
	public final boolean isAttackingNow()
	{
		return _attackEndTime > GameTimeController.getGameTicks();
	}
	
	/**
	 * Return True if the L2Character has aborted its attack.<BR><BR>
	 */
	public final boolean isAttackAborted()
	{
		return _attacking <= 0;
	}
	
	
	/**
	 * Abort the attack of the L2Character and send Server->Client ActionFailed packet.<BR><BR>
	 */
	public final void abortAttack()
	{
		if (isAttackingNow())
		{
			_attacking = 0;
			sendPacket(new ActionFailed());
		}
	}
	
	/**
	 * Returns body part (paperdoll slot) we are targeting right now
	 */
	public final int getAttackingBodyPart()
	{
		return _attacking;
	}
	
	/**
	 * Abort the cast of the L2Character and send Server->Client MagicSkillCanceld/ActionFailed packet.<BR><BR>
	 */
	public final void abortCast()
	{
		if (isCastingNow())
		{
			_castEndTime = 0;
			_castInterruptTime = 0;
			if (_skillCast != null)
			{
				_skillCast.cancel(true);
				_skillCast = null;
			}
			// cancels the skill hit scheduled task
			enableAllSkills();                                      // re-enables the skills
			broadcastPacket(new MagicSkillCanceld(getObjectId()));  // broadcast packet to stop animations client-side
			sendPacket(new ActionFailed());                         // send an "action failed" packet to the caster
		}
	}
	
	/**
	 * Update the position of the L2Character during a movement and return True if the movement is finished.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * At the beginning of the move action, all properties of the movement are stored in the MoveData object called <B>_move</B> of the L2Character.
	 * The position of the start point and of the destination permit to estimated in function of the movement speed the time to achieve the destination.<BR><BR>
	 *
	 * When the movement is started (ex : by MovetoLocation), this method will be called each 0.1 sec to estimate and update the L2Character position on the server.
	 * Note, that the current server position can differe from the current client position even if each movement is straight foward.
	 * That's why, client send regularly a Client->Server ValidatePosition packet to eventually correct the gap on the server.
	 * But, it's always the server position that is used in range calculation.<BR><BR>
	 *
	 * At the end of the estimated movement time, the L2Character position is automatically set to the destination position even if the movement is not finished.<BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : The current Z position is obtained FROM THE CLIENT by the Client->Server ValidatePosition Packet.
	 * But x and y positions must be calculated to avoid that players try to modify their movement speed.</B></FONT><BR><BR>
	 *
	 * @param gameTicks Nb of ticks since the server start
	 * @return True if the movement is finished
	 */
	public boolean updatePosition(int gameTicks)
	{
		// Get movement data
		MoveData m = _move;
		
		if (m == null)
			return true;
		
		if (!isVisible())
		{
			_move = null;
			return true;
		}
		
		// Check if the position has alreday be calculated
		if (m._moveTimestamp == gameTicks)
			return false;
		
		// Calculate the time between the beginning of the deplacement and now
		int elapsed = gameTicks - m._moveStartTime;
		
		// If estimated time needed to achieve the destination is passed,
		// the L2Character is positionned to the destination position
		if (elapsed >= m._ticksToMove)
		{
			// Set the timer of last position update to now
			m._moveTimestamp = gameTicks;
			
			// Set the position of the L2Character to the destination
			if(this instanceof L2BoatInstance )
			{        	   
				super.getPosition().setXYZ(m._xDestination, m._yDestination, m._zDestination);
				((L2BoatInstance)this).updatePeopleInTheBoat(m._xDestination, m._yDestination, m._zDestination);
			}
			else
			{
				super.getPosition().setXYZ(m._xDestination, m._yDestination, m._zDestination);
			}
			
			// Cancel the move action
			_move = null;
			
			return true;
		}
		
		// Estimate the position of the L2Character dureing the movement according to its _xSpeedTicks and _ySpeedTicks
		// The Z position is obtained from the client
		if(this instanceof L2BoatInstance )
		{    	   
			super.getPosition().setXYZ(m._xMoveFrom + (int)(elapsed * m._xSpeedTicks),m._yMoveFrom + (int)(elapsed * m._ySpeedTicks),super.getZ());
			((L2BoatInstance)this).updatePeopleInTheBoat(m._xMoveFrom + (int)(elapsed * m._xSpeedTicks),m._yMoveFrom + (int)(elapsed * m._ySpeedTicks),super.getZ());
		}
		else
		{
			super.getPosition().setXYZ(m._xMoveFrom + (int)(elapsed * m._xSpeedTicks),m._yMoveFrom + (int)(elapsed * m._ySpeedTicks),super.getZ());
		}
		
		
		// Set the timer of last position update to now
		m._moveTimestamp = gameTicks;
		
		return false;
		
	}
	
	
	/**
	 * Stop movement of the L2Character (Called by AI Accessor only).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Delete movement data of the L2Character </li>
	 * <li>Set the current position (x,y,z), its current L2WorldRegion if necessary and its heading </li>
	 * <li>Remove the L2Object object from _gmList** of GmListTable </li>
	 * <li>Remove object from _knownObjects and _knownPlayer* of all surrounding L2WorldRegion L2Characters </li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T send Server->Client packet StopMove/StopRotation </B></FONT><BR><BR>
	 *
	 */
	public void stopMove(L2CharPosition pos) { stopMove(pos, true); }
	public void stopMove(L2CharPosition pos, boolean updateKnownObjects)
	{
		// Delete movement data of the L2Character
		_move = null;
		
		//if (getAI() != null)
		//  getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		
		// Set the current position (x,y,z), its current L2WorldRegion if necessary and its heading
		// All data are contained in a L2CharPosition object
		if (pos != null)
		{
			getPosition().setXYZ(pos.x, pos.y, pos.z);
			setHeading(pos.heading);
		}
		sendPacket(new StopMove(this));
		if (updateKnownObjects) ThreadPoolManager.getInstance().executeTask(new KnownListAsynchronousUpdateTask(this));
	}
	
	
	
	
	
	
	/**
	 * Target a L2Object (add the target to the L2Character _target, _knownObject and L2Character to _KnownObject of the L2Object).<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * The L2Object (including L2Character) targeted is identified in <B>_target</B> of the L2Character<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set the _target of L2Character to L2Object </li>
	 * <li>If necessary, add L2Object to _knownObject of the L2Character </li>
	 * <li>If necessary, add L2Character to _KnownObject of the L2Object </li>
	 * <li>If object==null, cancel Attak or Cast </li><BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance : Remove the L2PcInstance from the old target _statusListener and add it to the new target if it was a L2Character</li><BR><BR>
	 *
	 * @param object L2object to target
	 *
	 */
	public void setTarget(L2Object object)
	{
		if (object != null && !object.isVisible())
			object = null;
		
		if (object != null && object != _target)
		{
			this.getKnownList().addKnownObject(object);
			object.getKnownList().addKnownObject(this);
		}
		
		// If object==null, Cancel Attak or Cast
		if (object == null)
		{
			if(_target != null)
			{
				broadcastPacket(new TargetUnselected(this));
			}
			if (isAttackingNow() && getAI().getAttackTarget() == _target)
			{
				abortAttack();
				
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				
				if (this instanceof L2PcInstance) {
					sendPacket(new ActionFailed());
					SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
					sm.addString("Attack is aborted");
					sendPacket(sm);
				}
			}
			
			if (isCastingNow() && canAbortCast() && getAI().getCastTarget() == _target)
			{
				abortCast();
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				if (this instanceof L2PcInstance) {
					sendPacket(new ActionFailed());
					SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
					sm.addString("Casting is aborted");
					sendPacket(sm);
				}
			}
		}
		
		_target = object;
	}
	
	/**
	 * Return the identifier of the L2Object targeted or -1.<BR><BR>
	 */
	public final int getTargetId()
	{
		if (_target != null)
		{
			return _target.getObjectId();
		}
		
		return -1;
	}
	
	/**
	 * Return the L2Object targeted or null.<BR><BR>
	 */
	public final L2Object getTarget()
	{
		return _target;
	}
	
	// called from AIAccessor only
	/**
	 * Calculate movement data for a move to location action and add the L2Character to movingObjects of GameTimeController (only called by AI Accessor).<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * At the beginning of the move action, all properties of the movement are stored in the MoveData object called <B>_move</B> of the L2Character.
	 * The position of the start point and of the destination permit to estimated in function of the movement speed the time to achieve the destination.<BR><BR>
	 * All L2Character in movement are identified in <B>movingObjects</B> of GameTimeController that will call the updatePosition method of those L2Character each 0.1s.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get current position of the L2Character </li>
	 * <li>Calculate distance (dx,dy) between current position and destination including offset </li>
	 * <li>Create and Init a MoveData object </li>
	 * <li>Set the L2Character _move object to MoveData object </li>
	 * <li>Add the L2Character to movingObjects of the GameTimeController </li>
	 * <li>Create a task to notify the AI that L2Character arrives at a check point of the movement </li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T send Server->Client packet MoveToPawn/CharMoveToLocation </B></FONT><BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> AI : onIntentionMoveTo(L2CharPosition), onIntentionPickUp(L2Object), onIntentionInteract(L2Object) </li>
	 * <li> FollowTask </li><BR><BR>
	 *
	 * @param x The X position of the destination
	 * @param y The Y position of the destination
	 * @param z The Y position of the destination
	 * @param offset The size of the interaction area of the L2Character targeted
	 *
	 */
	protected void moveToLocation(int x, int y, int z, int offset)
	{
		// Get current position of the L2Character
		final int curX = super.getX();
		final int curY = super.getY();
		final int curZ = super.getZ();
		
		// Calculate distance (dx,dy) between current position and destination
		final double dx = (x - curX);
		final double dy = (y - curY);
		double distance = Math.sqrt(dx*dx + dy*dy);
		
		if (Config.DEBUG) _log.fine("distance to target:" + distance);
		
		// Define movement angles needed
		// ^
		// |     X (x,y)
		// |   /
		// |  /distance
		// | /
		// |/ angle
		// X ---------->
		// (curx,cury)
		
		double cos;
		double sin;
		
		
		// Check if a movement offset is defined or no distance to go through
		if (offset > 0 || distance < 1)
		{
			// If no distance to go through, the movement is canceled
			if (distance < 1 || distance - offset  <= 0)
			{
				sin = 0;
				cos = 1;
				distance = 0;
				x = curX;
				y = curY;
				
				if (Config.DEBUG) _log.fine("already in range, no movement needed.");
				
				// Notify the AI that the L2Character is arrived at destination
				getAI().notifyEvent(CtrlEvent.EVT_ARRIVED, null);
				
				return;
			}
			// Calculate movement angles needed
			sin = dy/distance;
			cos = dx/distance;
			
			distance -= (offset-5); // due to rounding error, we have to move a bit closer to be in range
			
			// Calculate the new destination with offset included
			x = curX + (int)(distance * cos);
			y = curY + (int)(distance * sin);
			
		}
		else
		{
			// Calculate movement angles needed
			sin = dy/distance;
			cos = dx/distance;
		}
		
		// Get the Move Speed of the L2Charcater
		float speed = getStat().getMoveSpeed();
		
		// Create and Init a MoveData object
		MoveData m = new MoveData();
		
		// Caclulate the Nb of ticks between the current position and the destination
		m._ticksToMove = (int)(GameTimeController.TICKS_PER_SECOND * (distance + 25) / speed);
		
		// Calculate the xspeed and yspeed in unit/ticks in function of the movement speed
		m._xSpeedTicks = (float)(cos * speed / GameTimeController.TICKS_PER_SECOND);
		m._ySpeedTicks = (float)(sin * speed / GameTimeController.TICKS_PER_SECOND);
		
		// Calculate and set the heading of the L2Character
		int heading = (int) (Math.atan2(-sin, -cos) * 10430.378350470452724949566316381);
		heading += 32768;
		setHeading(heading);
		
		if (Config.DEBUG)
			_log.fine("dist:"+ distance +"speed:" + speed + " ttt:" +m._ticksToMove +
			          " dx:"+(int)m._xSpeedTicks + " dy:"+(int)m._ySpeedTicks + " heading:" + heading);
		
		m._xDestination = x;
		m._yDestination = y;
		m._zDestination = z; // this is what was requested from client
		m._heading = 0;
		
		m._moveStartTime = GameTimeController.getGameTicks();
		m._xMoveFrom = curX;
		m._yMoveFrom = curY;
		m._zMoveFrom = curZ;
		
		// If necessary set Nb ticks needed to a min value to ensure small distancies movements
		if (m._ticksToMove < 1 )
			m._ticksToMove = 1;
		
		if (Config.DEBUG) _log.fine("time to target:" + m._ticksToMove);
		
		// Set the L2Character _move object to MoveData object
		_move = m;
		
		// Add the L2Character to movingObjects of the GameTimeController
		// The GameTimeController manage objects movement
		GameTimeController.getInstance().registerMovingObject(this);
		
		int tm = m._ticksToMove*GameTimeController.MILLIS_IN_TICK;
		
		// Create a task to notify the AI that L2Character arrives at a check point of the movement
		if (tm > 3000)
			ThreadPoolManager.getInstance().scheduleAi( new NotifyAITask(CtrlEvent.EVT_ARRIVED_REVALIDATE), 2000);
		
		// the CtrlEvent.EVT_ARRIVED will be sent when the character will actually arrive
		// to destination by GameTimeController
	}
	
	public boolean validateMovementHeading(int heading)
	{
		if (_move == null) return true;
		
		boolean result = true;
//		if (_move._heading < heading - 5 || _move._heading > heading  5)
		if (_move._heading != heading)
		{
			result = (_move._heading == 0);
			_move._heading = heading;
		}
		
		return result;
	}
	
	/**
	 * Return the distance between the current position of the L2Character and the target (x,y).<BR><BR>
	 *
	 * @param x   X position of the target
	 * @param y   Y position of the target
     * @return the plan distance
	 *
     * @deprecated use getPlanDistanceSq(int x, int y, int z)
	 */
	@Deprecated
    public final double getDistance(int x, int y)
    {
       double dx = x - getX();
       double dy = y - getY();
      
        return Math.sqrt(dx*dx + dy*dy);
    }

    /**
     * Return the distance between the current position of the L2Character and the target (x,y).<BR><BR>
     *
     * @param x   X position of the target
     * @param y   Y position of the target
     * @return the plan distance
     * 
     * @deprecated use getPlanDistanceSq(int x, int y, int z)
     */
	@Deprecated
    public final double getDistance(int x, int y, int z) 
    {
        double dx = x - getX();
        double dy = y - getY();
        double dz = z - getZ();
            
        return Math.sqrt(dx*dx + dy*dy + dz*dz);
    }

    /**
     * Return the squared distance between the current position of the L2Character and the given object.<BR><BR>
     * 
     * @param object   L2Object
     * @return the squared distance
     */
    public final double getDistanceSq(L2Object object) 
    { 
        return getDistanceSq(object.getX(), object.getY(), object.getZ());
    }

    /**
     * Return the squared distance between the current position of the L2Character and the given x, y, z.<BR><BR>
     * 
     * @param x   X position of the target
     * @param y   Y position of the target
     * @param z   Z position of the target
     * @return the squared distance
     */
    public final double getDistanceSq(int x, int y, int z) 
    {
        double dx = x - getX();
        double dy = y - getY();
        double dz = z - getZ();
       
        return (dx*dx + dy*dy + dz*dz);
    }

    /**
     * Return the squared plan distance between the current position of the L2Character and the given object.<BR>
     * (check only x and y, not z)<BR><BR>
     * 
     * @param object   L2Object
     * @return the squared plan distance
     */
    public final double getPlanDistanceSq(L2Object object) 
    {
        return getPlanDistanceSq(object.getX(), object.getY());
    }
    /**
     * Return the squared plan distance between the current position of the L2Character and the given x, y, z.<BR>
     * (check only x and y, not z)<BR><BR>
     * 
     * @param x   X position of the target
     * @param y   Y position of the target
     * @return the squared plan distance
     */
    public final double getPlanDistanceSq(int x, int y) 
    { 
        double dx = x - getX();
        double dy = y - getY();
            
        return (dx*dx + dy*dy);
    }

    /**
     * Check if this object is inside the given radius around the given object.<BR><BR>
     * 
     * @param object   the target
     * @param radius  the radius around the target
     * @param checkZ  should we check Z axis also
     * @param strictCheck  true if (distance < radius), false if (distance <= radius)
     * @return true is the L2Character is inside the radius.
     * 
     * @see net.sf.l2j.gameserver.model.L2Character.isInsideRadius(int x, int y, int z, int radius, boolean checkZ, boolean strictCheck)
     */
    public final boolean isInsideRadius(L2Object object, int radius, boolean checkZ, boolean strictCheck)
    {
        return isInsideRadius(object.getX(), object.getY(), object.getZ(), radius, checkZ, strictCheck);
    }
    /**
     * Check if this object is inside the given plan radius around the given point.<BR><BR>
     * 
     * @param x   X position of the target
     * @param y   Y position of the target
     * @param radius  the radius around the target
     * @param strictCheck  true if (distance < radius), false if (distance <= radius)
     * @return true is the L2Character is inside the radius.
     */
    public final boolean isInsideRadius(int x, int y, int radius, boolean strictCheck)
    {
        return isInsideRadius(x, y, 0, radius, false, strictCheck);
    }
    /**
     * Check if this object is inside the given radius around the given point.<BR><BR>
     * 
     * @param x   X position of the target
     * @param y   Y position of the target
     * @param z   Z position of the target
     * @param radius  the radius around the target
     * @param checkZ  should we check Z axis also
     * @param strictCheck  true if (distance < radius), false if (distance <= radius)
     * @return true is the L2Character is inside the radius.
     */
    public final boolean isInsideRadius(int x, int y, int z, int radius, boolean checkZ, boolean strictCheck)
    {
        double dx = x - getX();
        double dy = y - getY();
        double dz = z - getZ();
        
        if (strictCheck)
        {
            if (checkZ)
                return (dx*dx + dy*dy + dz*dz) < radius * radius;
            else
                return (dx*dx + dy*dy) < radius * radius;
        } else
        {
            if (checkZ)
                return (dx*dx + dy*dy + dz*dz) <= radius * radius;
            else
                return (dx*dx + dy*dy) <= radius * radius;
        }
    }
       
//	/**
//	* event that is called when the destination coordinates are reached
//	*/
//	public void onTargetReached()
//	{
//	L2Character pawn = getPawnTarget();
//	
//	if (pawn != null)
//	{
//	int x = pawn.getX(), y=pawn.getY(),z = pawn.getZ();
//	
//	double distance = getDistance(x,y);
//	if (getCurrentState() == STATE_FOLLOW)
//	{
//	calculateMovement(x,y,z,distance);
//	return;
//	}
//	
//	//          takes care of moving away but distance is 0 so i won't follow problem
//	
//	
//	if (((distance > getAttackRange()) && (getCurrentState() == STATE_ATTACKING)) || (pawn.isMoving() && getCurrentState() != STATE_ATTACKING))
//	{
//	calculateMovement(x,y,z,distance);
//	return;
//	}
//	
//	}
//	//       update x,y,z with the current calculated position
//	stopMove();
//	
//	if (Config.DEBUG)
//	_log.fine(this.getName() +":: target reached at: x "+getX()+" y "+getY()+ " z:" + getZ());
//	
//	if (getPawnTarget() != null)
//	{
//	
//	setPawnTarget(null);
//	setMovingToPawn(false);
//	}
//	}
//	
//	public void setTo(int x, int y, int z, int heading)
//	{
//	setX(x);
//	setY(y);
//	setZ(z);
//	setHeading(heading);
//	updateCurrentWorldRegion(); //TODO: maybe not needed here
//	if (isMoving())
//	{
//	setCurrentState(STATE_IDLE);
//	StopMove setto = new StopMove(this);
//	broadcastPacket(setto);
//	}
//	else
//	{
//	ValidateLocation setto = new ValidateLocation(this);
//	broadcastPacket(setto);
//	}
//	
//	FinishRotation fr = new FinishRotation(this);
//	broadcastPacket(fr);
//	}
	
	
//	protected void startCombat()
//	{
//	if (_currentAttackTask == null )//&& !isInCombat())
//	{
//	_currentAttackTask = ThreadPoolManager.getInstance().scheduleMed(new AttackTask(), 0);
//	}
//	else
//	{
//	_log.info("multiple attacks want to start in parallel. prevented.");
//	}
//	}
//	
	
	/**
	 * Return the Weapon Expertise Penalty of the L2Character.<BR><BR>
	 */
	public float getWeaponExpertisePenalty()
	{
		return 1.f;
	}
	
	/**
	 * Return the Armour Expertise Penalty of the L2Character.<BR><BR>
	 */
	public float getArmourExpertisePenalty()
	{
		return 1.f;
	}
	
	
	/**
	 * Set _attacking corresponding to Attacking Body part to CHEST.<BR><BR>
	 */
	public void setAttackingBodypart()
	{
		_attacking = Inventory.PAPERDOLL_CHEST;
	}
	
	/**
	 * Retun True if arrows are available.<BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 *
	 */
	protected boolean checkAndEquipArrows()
	{
		return true;
	}
	
	/**
	 * Add Exp and Sp to the L2Character.<BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li>
	 * <li> L2PetInstance</li><BR><BR>
	 *
	 */
	public void addExpAndSp(@SuppressWarnings("unused") long addToExp, @SuppressWarnings("unused") int addToSp)
	{
		// Dummy method (overridden by players and pets)
	}
	
	/**
	 * Return the active weapon instance (always equiped in the right hand).<BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 *
	 */
	public abstract L2ItemInstance getActiveWeaponInstance();
	
	/**
	 * Return the active weapon item (always equiped in the right hand).<BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 *
	 */
	public abstract L2Weapon getActiveWeaponItem();
	
	/**
	 * Return the secondary weapon instance (always equiped in the left hand).<BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 *
	 */
	public abstract L2ItemInstance getSecondaryWeaponInstance();
	
	/**
	 * Return the secondary weapon item (always equiped in the left hand).<BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 *
	 */
	public abstract L2Weapon getSecondaryWeaponItem();
	
	
	/**
	 * Manage hit process (called by Hit Task).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL and send a Server->Client packet ActionFailed (if attacker is a L2PcInstance)</li>
	 * <li>If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are L2PcInstance </li>
	 * <li>If attack isn't aborted and hit isn't missed, reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary </li>
	 * <li>if attack isn't aborted and hit isn't missed, manage attack or cast break of the target (calculating rate, sending message...) </li><BR><BR>
	 *
	 * @param target The L2Character targeted
	 * @param damage Nb of HP to reduce
	 * @param crit True if hit is critical
	 * @param miss True if hit is missed
	 * @param soulshot True if SoulShot are charged
	 * @param shld True if shield is efficient
	 *
	 */
	protected void onHitTimer(L2Character target, int damage, boolean crit, boolean miss, boolean soulshot, boolean shld)
	{
		// If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL
		// and send a Server->Client packet ActionFailed (if attacker is a L2PcInstance)
		if (target == null || isAlikeDead() ||(this instanceof L2NpcInstance && ((L2NpcInstance) this).isEventMob))
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
		if ((this instanceof L2NpcInstance && target.isAlikeDead()) || target.isDead() 
                || (!getKnownList().knowsObject(target) && !(this instanceof L2DoorInstance)))
		{
			//getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			
			sendPacket(new ActionFailed());
			return;
		}
		
        if (miss)
        {
            if (target instanceof L2PcInstance)
            {
                SystemMessage sm = new SystemMessage(SystemMessage.AVOIDED_S1s_ATTACK);
    
                if (this instanceof L2Summon)
                {
                    int mobId = ((L2Summon)this).getTemplate().npcId;
                    sm.addNpcName(mobId);
                }
                else
                {
                    sm.addString(getName());
                }
                
                ((L2PcInstance)target).sendPacket(sm);
            }
        }

		// If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are L2PcInstance
		if (!isAttackAborted())
		{
			// If L2Character attacker is a L2PcInstance, send a system message
			if (this instanceof L2PcInstance)
			{
				// Check if hit is critical
				if (crit)
					sendPacket(new SystemMessage(SystemMessage.CRITICAL_HIT));
				
				// Check if hit is missed
				if (miss)
				{
					sendPacket(new SystemMessage(SystemMessage.MISSED_TARGET));
				}
				else if (damage < 1)
				{
					//((L2PcInstance)this).sendMessage("You hit the target's armor.");
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessage.YOU_DID_S1_DMG);
					sm.addNumber(damage);
					sendPacket(sm);
				}
			}
            else if (this instanceof L2Summon)
            {
                L2Summon activeSummon = (L2Summon)this;
                
                // Prevents the double spam of system messages, if the target is the owning player.
                if (target.getObjectId() != activeSummon.getOwner().getObjectId())
                {
                    if (crit)
                        activeSummon.getOwner().sendPacket(new SystemMessage(SystemMessage.PET_CRITICAL_HIT));
                    
                    if (miss)
                    {
                        activeSummon.getOwner().sendMessage("The pet missed the target.");
                    }
                    else
                    {
                        SystemMessage sm = new SystemMessage(SystemMessage.PET_DID_S1_DMG);
                        sm.addNumber(damage);
                        activeSummon.getOwner().sendPacket(sm);
                    }
                }
            }
			
			// If L2Character target is a L2PcInstance, send a system message
			if (target instanceof L2PcInstance)
			{
				L2PcInstance enemy = (L2PcInstance)target;
				
				// Check if shield is efficient
				if (shld)
					enemy.sendPacket(new SystemMessage(SystemMessage.SHIELD_DEFENCE_SUCCESSFULL));
				//else if (!miss && damage < 1)
					//enemy.sendMessage("You hit the target's armor.");
			}
            else if (target instanceof L2Summon)
            {
                L2Summon activeSummon = (L2Summon)target;
                
                SystemMessage sm = new SystemMessage(SystemMessage.PET_RECEIVED_DAMAGE_OF_S2_BY_S1);
                sm.addString(getName());
                sm.addNumber(damage);
                activeSummon.getOwner().sendPacket(sm);
            }
            
			if (!miss && damage > 0)
			{
				L2Weapon weapon = getActiveWeaponItem();
				boolean isBow = (weapon != null && weapon.getItemType().toString().equalsIgnoreCase("Bow"));
                
				if (!isBow) // Do not reflect or absorb if weapon is of type bow
				{
					// Reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary
					double reflectPercent = target.getStat().calcStat(Stats.REFLECT_DAMAGE_PERCENT,0,null,null);
                    
					if (reflectPercent > 0 && target.getTarget()==this)
					{
						int reflectedDamage = (int)(reflectPercent / 100. * damage);
						damage -= reflectedDamage;
						getStatus().reduceHp(reflectedDamage, null, true);
                        
						if (target instanceof L2PcInstance) 
                            ((L2PcInstance)target).sendMessage("You reflected " + reflectedDamage + " damage.");
					}
					
					// Absorb HP from the damage inflicted
					double absorbPercent = this.getStat().calcStat(Stats.ABSORB_DAMAGE_PERCENT,0, null,null);
                    
					if (absorbPercent > 0)
					{
						int maxCanAbsorb = (int)(this.getMaxHp() - this.getCurrentHp());
						int absorbDamage = (int)(absorbPercent / 100. * damage);
                        
						if (absorbDamage > maxCanAbsorb) 
                            absorbDamage = maxCanAbsorb; // Can't absord more than max hp
                        
						setCurrentHp(getCurrentHp() + absorbDamage);
					}
				}
				
				target.reduceCurrentHp(damage, this);

                // Notify AI with EVT_ATTACKED
                target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this);
                getAI().clientStartAutoAttack();

                // Calculate if attack is broken
				boolean abort = Formulas.getInstance().calcAtkBreak(target, target.getStat().getAtkCancel());
				
                // Manage attack or cast break of the target (calculating rate, sending message...)
				if (abort)
				{
					target.breakAttack();
					target.breakCast();
				}
			}
        
			// Launch weapon Special ability effect if available
			L2Weapon activeWeapon = getActiveWeaponItem();
            
			if (activeWeapon != null)
				activeWeapon.getSkillEffects(this, target, crit);
			
			// Check Raidboss attack
			if (target.isRaid() && getLevel() > target.getLevel() + 8)
			{
				L2Skill skill = SkillTable.getInstance().getInfo(4515, 99);
                
				if (skill != null)
					skill.getEffects(target, this);
				else
					_log.warning("Skill 4515 at level 99 is missing in DP.");
			}
            
            /* COMMENTED OUT BY nexus - 2006-08-17
             * 
             * We must not discharge the soulshouts at the onHitTimer method,
             * as this can cause unwanted soulshout consumption if the attacker
             * recharges the soulshot right after an attack request but before
             * his hit actually lands on the target.
             * 
             * The soulshot discharging has been moved to the doAttack method:
             * As soon as we know that we didn't missed the hit there, then we
             * must discharge any charged soulshots.
             */
            /*
            L2ItemInstance weapon = getActiveWeaponInstance();

            if (!miss)
            {
                if (this instanceof L2Summon && !(this instanceof L2PetInstance))
                {
                    if (((L2Summon)this).getChargedSoulShot() != L2ItemInstance.CHARGED_NONE)
                        ((L2Summon)this).setChargedSoulShot(L2ItemInstance.CHARGED_NONE);
                }
                else
                {
                    if (weapon != null && weapon.getChargedSoulshot() != L2ItemInstance.CHARGED_NONE)
                        weapon.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
                }
            }
            */
            
            return;
		}

		getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
	}
	
	/**
	 * Break an attack and send Server->Client ActionFailed packet and a System Message to the L2Character.<BR><BR>
	 */
	public void breakAttack()
	{
		if (isAttackingNow())
		{
			// Abort the attack of the L2Character and send Server->Client ActionFailed packet
			abortAttack();
			
			if (this instanceof L2PcInstance)
			{
				//TODO Remove sendPacket because it's always done in abortAttack
				sendPacket(new ActionFailed());
				
				// Send a system message
				sendPacket(new SystemMessage(SystemMessage.ATTACK_FAILED));
			}
		}
	}
	
	
	/**
	 * Break a cast and send Server->Client ActionFailed packet and a System Message to the L2Character.<BR><BR>
	 */
	public void breakCast()
	{
		if (isCastingNow() && canAbortCast())
		{
			// Abort the cast of the L2Character and send Server->Client MagicSkillCanceld/ActionFailed packet.
			abortCast();
			
			if (this instanceof L2PcInstance)
			{
				// Send a system message
				sendPacket(new SystemMessage(SystemMessage.CASTING_INTERRUPTED));
			}
		}
	}
	
	/**
	 * Reduce the arrow number of the L2Character.<BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 *
	 */
	protected void reduceArrowCount()
	{
		// default is to do nothin
	}
	
	/**
	 * Manage Forced attack (shift + select target).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>If L2Character or target is in a town area, send a system message TARGET_IN_PEACEZONE a Server->Client packet ActionFailed </li>
	 * <li>If target is confused, send a Server->Client packet ActionFailed </li>
	 * <li>If L2Character is a L2ArtefactInstance, send a Server->Client packet ActionFailed </li>
	 * <li>Send a Server->Client packet MyTargetSelected to start attack and Notify AI with AI_INTENTION_ATTACK </li><BR><BR>
	 *
	 * @param player The L2PcInstance to attack
	 *
	 */
	public void onForcedAttack(L2PcInstance player)
	{
        if (isInsidePeaceZone(player))
		{
			// If L2Character or target is in a peace zone, send a system message TARGET_IN_PEACEZONE a Server->Client packet ActionFailed
			player.sendPacket(new SystemMessage(SystemMessage.TARGET_IN_PEACEZONE));
			player.sendPacket(new ActionFailed());
		}
		else if (player.getTarget() != null && !player.getTarget().isAttackable() && (player.getAccessLevel() < Config.GM_PEACEATTACK))
		{
			// If target is not attackable, send a Server->Client packet ActionFailed
			player.sendPacket(new ActionFailed());
			return;
		}
		else if (player.isConfused())
		{
			// If target is confused, send a Server->Client packet ActionFailed
			player.sendPacket(new ActionFailed());
		}
		else if (this instanceof L2ArtefactInstance)
		{
			// If L2Character is a L2ArtefactInstance, send a Server->Client packet ActionFailed
			player.sendPacket(new ActionFailed());
		}
		else
		{
			//_log.config("Not within a zone");
			//player.startAttack(this);
			
			// Send a Server->Client packet MyTargetSelected to start attack
			player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
			
			// Notify AI with AI_INTENTION_ATTACK
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
		}
	}
	
	/**
	 * Return True if inside peace zone.<BR><BR>
	 */
	public boolean isInsidePeaceZone(L2PcInstance attacker)
	{
		return isInsidePeaceZone(attacker, this);
	}
	
	public boolean isInsidePeaceZone(L2PcInstance attacker, L2Object target)
	{
		return  (
				(attacker.getAccessLevel() < Config.GM_PEACEATTACK) &&
				isInsidePeaceZone((L2Object)attacker, target)
		);
	}
	
	public boolean isInsidePeaceZone(L2Object attacker, L2Object target)
	{
		return (
				target != null &&
				!(target instanceof L2MonsterInstance) &&                                   // Target is not a monster and
				(                                                                           // (
						ZoneManager.getInstance().checkIfInZonePeace(attacker) ||           //   Player is inside peace zone or
						ZoneManager.getInstance().checkIfInZonePeace(target)                //   Target is inside peace zone
				) &&                                                                        // ) and
				(                                                                           // (
						!(target instanceof L2PcInstance) ||                                //   Target is not a player or
						(!Config.ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE) ||       //   Target is a player and killable in peace zone is false or
						(((L2PcInstance)target).getKarma() <= 0)                            //   Target is a player, killable in peace zone is true, and has no karma
				)                                                                           // )
		);
	}
   
    /**
     * return true if this character is inside an active grid.
     */
    public Boolean isInActiveRegion()
    {
        L2WorldRegion region = L2World.getInstance().getRegion(getX(),getY());
        return  ((region !=null) && (region.isActive()));
    }

	/**
	 * Return True if the L2Character has a Party in progress.<BR><BR>
	 */
    public boolean isInParty()
	{
		return false;
	}
    
	/**
	 * Return the L2Party object of the L2Character.<BR><BR>
	 */
	public L2Party getParty()
	{
		return null;
	}
    
	/**
	 * Return the Attack Speed of the L2Character (delay (in milliseconds) before next attack).<BR><BR>
	 */
	public int calculateAttackSpeed(L2Character target, @SuppressWarnings("unused") L2ItemInstance weaponInst)
	{
		double atkSpd = 0;
        if (this instanceof L2Summon)
        {
            atkSpd = getPAtkSpd();
        } else
        {
    		L2Weapon weapon = this.getActiveWeaponItem();
    		if (weapon !=null)
    		{
    			switch (weapon.getItemType())
    			{
    				case DAGGER:
    					atkSpd = getStat().getPAtkSpd();
    					atkSpd /= 1.15;
    					break;
    				default:
    					atkSpd = getStat().getPAtkSpd();
				}
			}
        }

		return Formulas.getInstance().calcPAtkSpd(this, target, atkSpd);
	}
	
	/**
	 * Return True if the L2Character use a dual weapon.<BR><BR>
	 */
	public boolean isUsingDualWeapon()
	{
		return false;
	}
	
	/**
	 * Add a skill to the L2Character _skills and its Func objects to the calculator set of the L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills own by a L2Character are identified in <B>_skills</B><BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Replace oldSkill by newSkill or Add the newSkill </li>
	 * <li>If an old skill has been replaced, remove all its Func objects of L2Character calculator set</li>
	 * <li>Add Func objects of newSkill to the calculator set of the L2Character </li><BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance : Save update in the character_skills table of the database</li><BR><BR>
	 *
	 * @param newSkill The L2Skill to add to the L2Character
	 *
	 * @return The L2Skill replaced or null if just added a new L2Skill
	 *
	 */
	public L2Skill addSkill(L2Skill newSkill)
	{
		L2Skill oldSkill    = null;
		
		if (newSkill != null)
		{
			// Replace oldSkill by newSkill or Add the newSkill
			oldSkill = _Skills.put(newSkill.getId(), newSkill);
			
			// If an old skill has been replaced, remove all its Func objects
			if (oldSkill != null)
				removeStatsOwner(oldSkill);
			
			// Add Func objects of newSkill to the calculator set of the L2Character
			addStatFuncs(newSkill.getStatFuncs(null, this));
		}
		
		return oldSkill;
	}
	
	
	/**
	 * Remove a skill from the L2Character and its Func objects from calculator set of the L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills own by a L2Character are identified in <B>_skills</B><BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove the skill from the L2Character _skills </li>
	 * <li>Remove all its Func objects from the L2Character calculator set</li><BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance : Save update in the character_skills table of the database</li><BR><BR>
	 *
	 * @param skill The L2Skill to remove from the L2Character
	 *
	 * @return The L2Skill removed
	 *
	 */
	public L2Skill removeSkill(L2Skill skill)
	{
		if (skill == null) return null;
		
		// Remove the skill from the L2Character _skills
		L2Skill oldSkill = _Skills.remove(skill.getId());
		
		// Remove all its Func objects from the L2Character calculator set
		if (oldSkill != null)
			removeStatsOwner(oldSkill);
		
		return oldSkill;
	}
	
	/**
	 * Return all skills own by the L2Character in a table of L2Skill.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills own by a L2Character are identified in <B>_skills</B> the L2Character <BR><BR>
	 *
	 */
	public final L2Skill[] getAllSkills()
	{
		if (_Skills == null)
			return new L2Skill[0];
		
		return _Skills.values().toArray(new L2Skill[_Skills.values().size()]);
	}
	
	/**
	 * Return the level of a skill owned by the L2Character.<BR><BR>
	 *
	 * @param skillId The identifier of the L2Skill whose level must be returned
	 *
	 * @return The level of the L2Skill identified by skillId
	 *
	 */
	public int getSkillLevel(int skillId)
	{
		if (_Skills == null)
			return -1;
		
		L2Skill skill = _Skills.get(skillId);
		
		if (skill == null)
			return -1;
		return skill.getLevel();
	}
	
	/**
	 * Return True if the skill is known by the L2Character.<BR><BR>
	 *
	 * @param skillId The identifier of the L2Skill to check the knowledge
	 *
	 */
	public final L2Skill getKnownSkill(int skillId)
	{
		if (_Skills == null)
			return null;
		
		return _Skills.get(skillId);
	}


    /**
     * Return the number of skills of type(Buff, Debuff, HEAL_PERCENT, MANAHEAL_PERCENT) affecting this L2Character.<BR><BR>
     *
     * @return The number of Buffs affecting this L2Character
     */
    public int getBuffCount() {
        L2Effect[] effects = this.getAllEffects();
        int numBuffs=0;
        if (effects != null) {
            for (L2Effect e : effects) {
                if (e != null) {
                    if ((e.getSkill().getSkillType() == L2Skill.SkillType.BUFF ||
                        e.getSkill().getSkillType() == L2Skill.SkillType.DEBUFF ||
                        e.getSkill().getSkillType() == L2Skill.SkillType.REFLECT ||
                        e.getSkill().getSkillType() == L2Skill.SkillType.HEAL_PERCENT ||
                        e.getSkill().getSkillType() == L2Skill.SkillType.MANAHEAL_PERCENT) && 
                        e.getSkill().getId() != 4267 &&
                        e.getSkill().getId() != 4270 &&
                        !(e.getSkill().getId() > 4360  && e.getSkill().getId() < 4367)) { // 7s buffs
                        numBuffs++;
                    }
                }
            }
        }
        return numBuffs;
    }
    
    /**
     * Removes the first Buff of this L2Character.<BR><BR>
     *
     * @param preferSkill If != 0 the given skill Id will be removed instead of first
     */
    public void removeFirstBuff(int preferSkill) {
        L2Effect[] effects = this.getAllEffects();
        L2Effect removeMe=null;
        if (effects != null) {
            for (L2Effect e : effects) {
                if (e != null) {
                    if ((e.getSkill().getSkillType() == L2Skill.SkillType.BUFF ||
                        e.getSkill().getSkillType() == L2Skill.SkillType.DEBUFF ||
                        e.getSkill().getSkillType() == L2Skill.SkillType.REFLECT ||
                        e.getSkill().getSkillType() == L2Skill.SkillType.HEAL_PERCENT ||
                        e.getSkill().getSkillType() == L2Skill.SkillType.MANAHEAL_PERCENT) && 
                        e.getSkill().getId() != 4267 &&
                        e.getSkill().getId() != 4270 &&
                        !(e.getSkill().getId() > 4360  && e.getSkill().getId() < 4367)) {
                        if (preferSkill == 0) { removeMe=e; break; }
                        else if (e.getSkill().getId() == preferSkill) { removeMe=e; break; }
                        else if (removeMe==null) removeMe=e;
                    }
                }
            }
        }
        if (removeMe != null) removeMe.exit();
    }
    
    /**
     * Checks if the given skill stacks with an existing one.<BR><BR>
     *
     * @param checkSkill the skill to be checked
     * 
     * @return Returns whether or not this skill will stack
     */
    public boolean doesStack(L2Skill checkSkill) {
        if (_effects == null || _effects.size() < 1 ||
                checkSkill._effectTemplates == null || 
                checkSkill._effectTemplates.length < 1 ||
                checkSkill._effectTemplates[0]._stackType == null) return false;
        String stackType=checkSkill._effectTemplates[0]._stackType;
        if (stackType.equals("none")) return false;

        for (int i=0; i<_effects.size(); i++) {
            if (_effects.get(i).getEffect().getStackType() != null &&
                    _effects.get(i).getEffect().getStackType().equals(stackType)) return true;
        }
        return false;
    }
	
	/**
	 * Manage the magic skill launching task (MP, HP, Item consummation...) and display the magic skill animation on client.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Send a Server->Client packet MagicSkillLaunched (to display magic skill animation) to all L2PcInstance of L2Charcater _knownPlayers</li>
	 * <li>Consumme MP, HP and Item if necessary</li>
	 * <li>Send a Server->Client packet StatusUpdate with MP modification to the L2PcInstance</li>
	 * <li>Launch the magic skill in order to calculate its effects</li>
	 * <li>If the skill type is PDAM, notify the AI of the target with AI_INTENTION_ATTACK</li>
	 * <li>Notify the AI of the L2Character with EVT_FINISH_CASTING</li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : A magic skill casting MUST BE in progress</B></FONT><BR><BR>
	 *
	 * @param skill The L2Skill to use
	 *
	 */
	public void onMagicUseTimer(L2Object[] targets, L2Skill skill)
	{
		if (skill == null || targets == null || targets.length <= 0)
        {
            setAttackingChar(null);
            setAttackingCharSkill(null);
            getAI().notifyEvent(CtrlEvent.EVT_CANCEL);

            return;
        }
		
		int escapeRange = 0;
		if(skill.getEffectRange() > escapeRange) escapeRange = skill.getEffectRange();
		else if(skill.getCastRange() < 0 && skill.getSkillRadius() > 80) escapeRange = skill.getSkillRadius();
			
		if(escapeRange > 0) 
		{
			List<L2Character> targetList = new FastList<L2Character>();
			for (int i = 0; i < targets.length; i++)
			{
				if (targets[i] instanceof L2Character)
				{
					if(!this.isInsideRadius(targets[0],escapeRange,true,false)) continue;
					else targetList.add((L2Character)targets[i]);
				}
				//else
				//{
				//	if (Config.DEBUG) 
				//        _log.warning("Class cast bad: "+targets[i].getClass().toString());
				//}
			}
			if(targetList.isEmpty()) 
			{
				abortCast();	
				return;
			}
			else targets = targetList.toArray(new L2Character[targetList.size()]);
		}
        
		// Check if player is using fake death.
        if (isAlikeDead())
		{
            setAttackingChar(null);
            setAttackingCharSkill(null);
            getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
            
			_castEndTime = 0;
			_castInterruptTime = 0;
			return;
		}
		
		// Check if a cast is in progress
		if (isCastingNow())
		{
			// Get the display identifier of the skill
			int magicId = skill.getDisplayId();
			
			// Get the level of the skill
			int level = getSkillLevel(skill.getId());
            
			if (level < 1) 
                level = 1;
			
			// Go through targets table
			for (int i = 0;i < targets.length;i++)
			{
				if (targets[i] instanceof L2Character)
				{
					L2Character target = (L2Character) targets[i];

					if (skill.getSkillType() == L2Skill.SkillType.BUFF || skill.getSkillType() == L2Skill.SkillType.SEED)
					{
						SystemMessage smsg = new SystemMessage(SystemMessage.YOU_FEEL_S1_EFFECT);
						smsg.addString(skill.getName());
						target.sendPacket(smsg);
					}
					
					if (Config.DEBUG) 
                        _log.fine("msl: "+getName()+" "+magicId+" "+level+" "+target.getTitle());
					
					// Send a Server->Client packet MagicSkillLaunched to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character
					broadcastPacket(new MagicSkillLaunched(this, magicId, level, target));
                    
					if (this instanceof L2PcInstance && target instanceof L2Summon)
					{
						if (this.equals(((L2Summon)target).getOwner()))
							this.sendPacket(new PetInfo((L2Summon)target));
						else
							this.sendPacket(new NpcInfo((L2Summon)target, this));
					}
				}
			}
			
			StatusUpdate su = new StatusUpdate(getObjectId());
			boolean isSendStatus = false;
			
			// Consume MP of the L2Character and Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
			if (getStat().getMpConsume(skill) > 0)
			{
				getStatus().reduceMp(calcStat(Stats.MP_CONSUME_RATE,getStat().getMpConsume(skill),null,null));
				su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
				isSendStatus = true;
			}
			
			// Consume HP if necessary and Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
			if (skill.getHpConsume() > 0)
			{
				double consumeHp;
				
				consumeHp = calcStat(Stats.HP_CONSUME_RATE,skill.getHpConsume(),null,null);
				if(consumeHp+1 >= getCurrentHp())
					consumeHp = getCurrentHp()-1.0;
				
				getStatus().reduceHp(consumeHp, this);
				
				su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
				isSendStatus = true;
			}
			
			// Send a Server->Client packet StatusUpdate with MP modification to the L2PcInstance
			if (isSendStatus) sendPacket(su);
			
			// Consume Items if necessary and Send the Server->Client packet InventoryUpdate with Item modification to all the L2Character
			if (skill.getItemConsume() > 0)
				consumeItem(skill.getItemConsumeId(), skill.getItemConsume());
			
            _castEndTime = 0;
            _castInterruptTime = 0;
            _skillCast = null;
            
			// Launch the magic skill in order to calculate its effects
			callSkill(skill, targets);

			//if the skill has changed the character's state to something other than STATE_CASTING
			//then just leave it that way, otherwise switch back to STATE_IDLE.
			//if(isCastingNow())
			//  getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
			
			// If the skill type is PDAM or DRAIN_SOUL, notify the AI of the target with AI_INTENTION_ATTACK
			if (skill.getSkillType() == SkillType.PDAM || skill.getSkillType() == SkillType.DRAIN_SOUL)
			{
				if ((getTarget() != null) && (getTarget() instanceof L2Character))
					getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getTarget());
			}
			
            if (skill.isOffensive())
                getAI().clientStartAutoAttack();

            // Notify the AI of the L2Character with EVT_FINISH_CASTING
			getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING);
            
            /* 
             * If character is a player, then wipe their current cast state and  
             * check if a skill is queued.
             * 
             * If there is a queued skill, launch it and wipe the queue.
             */
            if (this instanceof L2PcInstance)
            {
                L2PcInstance currPlayer = (L2PcInstance)this;
                SkillDat queuedSkill = currPlayer.getQueuedSkill();
                
                currPlayer.setCurrentSkill(null, false, false);
                
                if (queuedSkill != null)
                {                     
                    currPlayer.setQueuedSkill(null, false, false);
                    
                    // DON'T USE : Recursive call to useMagic() method
                    // currPlayer.useMagic(queuedSkill.getSkill(), queuedSkill.isCtrlPressed(), queuedSkill.isShiftPressed());
                    ThreadPoolManager.getInstance().executeTask(new QueuedMagicUseTask(currPlayer, queuedSkill.getSkill(), queuedSkill.isCtrlPressed(), queuedSkill.isShiftPressed()) );
                }
            }
		}
	}
	
	
	/**
	 * Reduce the item number of the L2Character.<BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 *
	 */
	public void consumeItem(@SuppressWarnings("unused") int itemConsumeId, @SuppressWarnings("unused") int itemCount)
	{
	}
	
	/**
	 * Enable a skill (remove it from _disabledSkills of the L2Character).<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills disabled are identified by their skillId in <B>_disabledSkills</B> of the L2Character <BR><BR>
	 *
	 * @param skillId The identifier of the L2Skill to enable
	 *
	 */
	public synchronized void enableSkill(int skillId)
	{
		if (_disabledSkills == null)
			return;
		
		_disabledSkills.remove(new Integer(skillId));
		
	}
	
	/**
	 * Disable a skill (add it to _disabledSkills of the L2Character).<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills disabled are identified by their skillId in <B>_disabledSkills</B> of the L2Character <BR><BR>
	 *
	 * @param skillId The identifier of the L2Skill to disable
	 *
	 */
	public synchronized void disableSkill(int skillId)
	{
		if (_disabledSkills == null)
		{
			_disabledSkills = new FastList<Integer>();
			
		}
		_disabledSkills.add(skillId);
	}
	
	/**
	 * Check if a skill is disabled.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills disabled are identified by their skillId in <B>_disabledSkills</B> of the L2Character <BR><BR>
	 *
	 * @param skillId The identifier of the L2Skill to disable
	 *
	 */
	public boolean isSkillDisabled(int skillId)
	{
		if (isAllSkillsDisabled())
			return true;
		
		if (_disabledSkills == null)
			return false;
        
        try {
            return _disabledSkills.contains(skillId);
        } catch (Exception e) {return false;}
		
	}
	
	/**
	 * Disable all skills (set _allSkillsDisabled to True).<BR><BR>
	 */
	public void disableAllSkills()
	{
		if (Config.DEBUG) _log.fine("all skills disabled");
		_allSkillsDisabled = true;
	}
	
	/**
	 * Enable all skills (set _allSkillsDisabled to False).<BR><BR>
	 */
	public void enableAllSkills()
	{
		if (Config.DEBUG) _log.fine("all skills enabled");
		_allSkillsDisabled = false;
	}
	
	/**
	 * Launch the magic skill and calculate its effects on each target contained in the targets table.<BR><BR>
	 *
	 * @param skill The L2Skill to use
	 * @param targets The table of L2Object targets
	 *
	 */
	public void callSkill(L2Skill skill, L2Object[] targets)
	{
		try
		{
            
			// Do initial checkings for skills and set pvp flag/draw aggro when needed
			for (L2Object target : targets)
			{
				if (target instanceof L2Character)
				{
					// Set some values inside target's instance for later use
					L2Character player = (L2Character) target;
					
                    L2Weapon activeWeapon = getActiveWeaponItem();
					// Launch weapon Special ability skill effect if available
					if (activeWeapon != null && !((L2Character)target).isDead())
					{
						if (activeWeapon.getSkillEffects(this, player, skill).length > 0 && this instanceof L2PcInstance)
							this.sendPacket(SystemMessage.sendString("Target affected by weapon special ability!"));
					}
					
					// Check Raidboss attack
					if (player.isRaid() && getLevel() > player.getLevel() + 8)
					{
						L2Skill tempSkill = SkillTable.getInstance().getInfo(4515, 99);
						if(tempSkill != null)
						{
							tempSkill.getEffects(player, this);
						}	
						else
						{
							_log.warning("Skill 4515 at level 99 is missing in DP.");
						}
					}

					L2PcInstance activeChar = null;

					if (this instanceof L2PcInstance)
						activeChar = (L2PcInstance)this;
					else if (this instanceof L2Summon)
						activeChar = ((L2Summon)this).getOwner();

					if (activeChar != null)
					{
						if (skill.isOffensive())
						{
							if ((player instanceof L2PcInstance || player instanceof L2Summon) && activeChar.checkIfPvP(player))
                            {
                                player.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar);
                                activeChar.updatePvPStatus(player);
                            }else if (player instanceof L2Attackable)
                            {
                            	// notify the AI that she is attacked
                            	player.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar);
                            }
						}
						else
						{
							if (player instanceof L2PcInstance)
							{
								// Casting non offensive skill on player with pvp flag set or with karma
								if (!player.equals(this) &&
										(((L2PcInstance)player).getPvpFlag() > 0 ||
												((L2PcInstance)player).getKarma() > 0)) activeChar.updatePvPStatus();
							}
							else if (player instanceof L2Attackable && !(skill.getSkillType() == L2Skill.SkillType.SUMMON))
								activeChar.updatePvPStatus();
						}
					}
				}
			}
			
			ISkillHandler handler = null;
			
			// TODO Remove this useless section
			if(skill.isToggle())
			{
				// Check if the skill effects are already in progress on the L2Character
				if(getEffect(skill.getId()) != null)
				{
					handler = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());
					
					if (handler != null)
						handler.useSkill(this, skill, targets);
					else
						skill.useSkill(this, targets);
					
					return;
				}
			}
            
            // Check if over-hit is possible
            if(skill.isOverhit())
            {
                // Set the "over-hit enabled" flag on each of the possible targets
                for (L2Object target : targets)
                {
                    L2Character player = (L2Character) target;
                    if(player instanceof L2Attackable)
                    {
                        ((L2Attackable)player).overhitEnabled(true);
                    }
                }
            }
			
			// Get the skill handler corresponding to the skill type (PDAM, MDAM, SWEEP...) started in gameserver
			handler = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());
			
			// Launch the magic skill and calculate its effects
			if (handler != null)
				handler.useSkill(this, skill, targets);
			else
				skill.useSkill(this, targets);
			
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "", e);
		}
	}
	
	/**
	 * Return True if the L2Character is behind the target and can't be seen.<BR><BR>
	 */
	public boolean isBehindTarget()
	{
		if(getTarget() == null)
			return false;
		
		if (getTarget() instanceof L2Character)
		{
			L2Character target = (L2Character) getTarget();
			int tarheading = target.getHeading();
			int heading = getHeading();
			int dx = target.getX() - getX();
			int dy = target.getY() - getY();
			double dist = Math.sqrt(dx*dx + dy*dy);
			if(dist == 0)
			{
				dist = 0.01;
			}
			double sin = dy/dist;
			double cos = dx/dist;
			heading = (int) (Math.atan2(-sin,-cos) * 10430.378350470452724949566316381);
			heading += 32768;
			if((tarheading - heading) <= 12000 && (tarheading - heading) >= -12000)
			{
				return true;
			}
			if((tarheading - heading) >= 53536 || (tarheading - heading) <= -53536)
			{
				return true;
			}
		}
		else
		{
			_log.fine("isBehindTarget's target not an L2 Character.");
		}
		return false;
	}
	
	
	
	/**
	 * Return 1.<BR><BR>
	 */
	public double getLevelMod()
	{
		return 1;
	}
	
	public final void setSkillCast(Future newSkillCast)
	{
		_skillCast = newSkillCast;
	}
	public final void setSkillCastEndTime(int newSkillCastEndTime)
	{
		_castEndTime = newSkillCastEndTime;
	}
	
	/**
	 * Not Implemented.<BR><BR>
	 */
	public void updateStats()
	{
	}
	
	private Future _PvPRegTask;
	
	private long _lastPvpAttack;
	
	public void setlastPvpAttack(long time)
	{
		_lastPvpAttack = time;
	}
	
	public long getlastPvpAttack()
	{
		return _lastPvpAttack;
	}
	
	public void startPvPFlag()
	{
		updatePvPFlag(1);
		
		_PvPRegTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(
		                                                                   new PvPFlag(), 1000, 1000);
	}
	
	public void stopPvPFlag() {
		if (_PvPRegTask != null) _PvPRegTask.cancel(false);
		
		updatePvPFlag(0);
		
		_PvPRegTask = null;
	}
	
	public void updatePvPFlag(int value) {
		if (!(this instanceof L2PcInstance))
			return;
		L2PcInstance player = (L2PcInstance)this;
		if (player.getPvpFlag() == value)
			return;
		player.setPvpFlag(value);
		
		if (player.getKarma() < 1)
		{
			StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.PVP_FLAG, value);
			broadcastPacket(su);
		}
		else return;
	}
	
//	public void checkPvPFlag()
//	{
//	if (Config.DEBUG) _log.fine("Checking PvpFlag");
//	_PvPRegTask = ThreadPoolManager.getInstance().scheduleLowAtFixedRate(
//	new PvPFlag(), 1000, 5000);
//	_PvPRegActive = true;
//	//  _log.fine("PvP recheck");
//	}
//	
	
	/**
	 * Return a Random Damage in function of the weapon.<BR><BR>
	 */
	public final int getRandomDamage(@SuppressWarnings("unused") L2Character target)
	{
		L2Weapon weaponItem = getActiveWeaponItem();
		
		if (weaponItem == null)
			return 5+(int)Math.sqrt(getLevel());
		
		return weaponItem.getRandomDamage();
	}
	
	public String toString()
	{
		return "mob "+this.getObjectId();
	}
	
	public int getAttackEndTime()
	{
		return _attackEndTime;
	}
	
	/**
	 * Not Implemented.<BR><BR>
	 */
	public abstract int getLevel();
	// =========================================================
	
	
	
	
	
	
	// =========================================================
	// Stat - NEED TO REMOVE ONCE L2CHARSTAT IS COMPLETE
	// Property - Public
	public final double calcStat(Stats stat, double init, L2Character target, L2Skill skill) { return getStat().calcStat(stat, init, target, skill); }
	
	// Property - Public
	public int getAccuracy() { return getStat().getAccuracy(); }
	public final float getAttackSpeedMultiplier() { return getStat().getAttackSpeedMultiplier(); }
	public final int getAtkCancel() { return getStat().getAtkCancel(); }
	public int getCON() { return getStat().getCON(); }
	public int getDEX() { return getStat().getDEX(); }
	public final double getCriticalDmg(L2Character target, double init) { return getStat().getCriticalDmg(target, init); }
	public int getCriticalHit(L2Character target, L2Skill skill) { return getStat().getCriticalHit(target, skill); }
	public int getEvasionRate(L2Character target) { return getStat().getEvasionRate(target); }
	public int getINT() { return getStat().getINT(); }
	public final int getMagicalAttackRange(L2Skill skill) { return getStat().getMagicalAttackRange(skill); }
	public final int getMaxCp() { return getStat().getMaxCp(); }
	public int getMAtk(L2Character target, L2Skill skill) { return getStat().getMAtk(target, skill); }
	public int getMAtkSpd() { return getStat().getMAtkSpd(); }
	public int getMaxMp() { return getStat().getMaxMp(); }
	public int getMaxHp() { return getStat().getMaxHp(); }
	public final int getMCriticalHit(L2Character target, L2Skill skill) { return getStat().getMCriticalHit(target, skill); }
	public int getMDef(L2Character target, L2Skill skill) { return getStat().getMDef(target, skill); }
	public int getMEN() { return getStat().getMEN(); }
	public double getMReuseRate(L2Skill skill) { return getStat().getMReuseRate(skill); }
	public float getMovementSpeedMultiplier() { return getStat().getMovementSpeedMultiplier(); }
	public int getPAtk(L2Character target) { return getStat().getPAtk(target); }
	public double getPAtkAnimals(L2Character target) { return getStat().getPAtkAnimals(target); }
	public double getPAtkDragons(L2Character target) { return getStat().getPAtkDragons(target); }
	public double getPAtkInsects(L2Character target) { return getStat().getPAtkInsects(target); }
	public double getPAtkMonsters(L2Character target) { return getStat().getPAtkMonsters(target); }
    public double getPAtkPlants(L2Character target) { return getStat().getPAtkPlants(target); }
    public int getPAtkSpd() { return getStat().getPAtkSpd(); }
	public double getPAtkUndead(L2Character target) { return getStat().getPAtkUndead(target); }
	public double getPDefUndead(L2Character target) { return getStat().getPDefUndead(target); }
	public int getPDef(L2Character target) { return getStat().getPDef(target); }
	public final int getPhysicalAttackRange() { return getStat().getPhysicalAttackRange(); }
	public int getRunSpeed() { return getStat().getRunSpeed(); }
	public final int getShldDef() { return getStat().getShldDef(); }
	public int getSTR() { return getStat().getSTR(); }
	public final int getWalkSpeed() { return getStat().getWalkSpeed(); }
	public int getWIT() { return getStat().getWIT(); }
	// =========================================================
	
	
	// =========================================================
	// Status - NEED TO REMOVE ONCE L2CHARTATUS IS COMPLETE
	// Method - Public
	public void addStatusListener(L2Character object) { getStatus().addStatusListener(object); }
	public void reduceCurrentHp(double i, L2Character attacker) { reduceCurrentHp(i, attacker, true); }
	public void reduceCurrentHp(double i, L2Character attacker, boolean awake) { getStatus().reduceHp(i, attacker, awake); }
	public void reduceCurrentMp(double i) { getStatus().reduceMp(i); }
	public void removeStatusListener(L2Character object) { getStatus().removeStatusListener(object); }
	protected synchronized void stopHpMpRegeneration() { getStatus().stopHpMpRegeneration(); }
	
	// Property - Public
	public final double getCurrentCp() { return getStatus().getCurrentCp(); }
	public final void setCurrentCp(Double newCp) { setCurrentCp((double) newCp); }
	public final void setCurrentCp(double newCp) { getStatus().setCurrentCp(newCp); }
	public final double getCurrentHp() { return getStatus().getCurrentHp(); }
	public final void setCurrentHp(double newHp) { getStatus().setCurrentHp(newHp); }
	public final void setCurrentHpMp(double newHp, double newMp){ getStatus().setCurrentHpMp(newHp, newMp); }
	public final double getCurrentMp() { return getStatus().getCurrentMp(); }
	public final void setCurrentMp(Double newMp) { setCurrentMp((double)newMp); }
	public final void setCurrentMp(double newMp) { getStatus().setCurrentMp(newMp); }
	// =========================================================
	
	public void setAiClass(String aiClass)
	{
		_aiClass = aiClass;
	}
	
	public String getAiClass()
	{
		return _aiClass;
	}

	public L2Character getLastBuffer()
	{
		return _LastBuffer;
	}

	public int getLastHealAmount()
	{
		return _LastHealAmount;
	}

	public void setLastBuffer(L2Character buffer)
	{
		_LastBuffer = buffer;
	}

	public void setLastHealAmount(int hp)
	{
		_LastHealAmount = hp;
	}
}


