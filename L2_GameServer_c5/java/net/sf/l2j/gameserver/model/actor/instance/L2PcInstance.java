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
package net.sf.l2j.gameserver.model.actor.instance;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.CharTemplateTable;
import net.sf.l2j.gameserver.ClanTable;
import net.sf.l2j.gameserver.Connection;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.GmListTable;
import net.sf.l2j.gameserver.HennaTable;
import net.sf.l2j.gameserver.ItemTable;
import net.sf.l2j.gameserver.LoginServerThread;
import net.sf.l2j.gameserver.MapRegionTable;
import net.sf.l2j.gameserver.Olympiad;
import net.sf.l2j.gameserver.RecipeController;
import net.sf.l2j.gameserver.SevenSigns;
import net.sf.l2j.gameserver.SevenSignsFestival;
import net.sf.l2j.gameserver.SkillTable;
import net.sf.l2j.gameserver.SkillTreeTable;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.Universe;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.ai.L2PlayerAI;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.communitybbs.BB.Forum;
import net.sf.l2j.gameserver.communitybbs.Manager.ForumsBBSManager;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.handler.ItemHandler;
import net.sf.l2j.gameserver.handler.skillhandlers.SiegeFlag;
import net.sf.l2j.gameserver.handler.skillhandlers.TakeCastle;
import net.sf.l2j.gameserver.instancemanager.ArenaManager;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.QuestManager;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.instancemanager.CursedWeaponsManager;
import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.BlockList;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.ItemContainer;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.L2DropData;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Fishing;
import net.sf.l2j.gameserver.model.L2HennaInstance;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Macro;
import net.sf.l2j.gameserver.model.L2ManufactureList;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Radar;
import net.sf.l2j.gameserver.model.L2RecipeList;
import net.sf.l2j.gameserver.model.L2ShortCut;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2SkillLearn;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.MacroList;
import net.sf.l2j.gameserver.model.PcFreight;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.PcWarehouse;
import net.sf.l2j.gameserver.model.PetInventory;
import net.sf.l2j.gameserver.model.ShortCuts;
import net.sf.l2j.gameserver.model.TradeList;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.knownlist.PcKnownList;
import net.sf.l2j.gameserver.model.actor.stat.PcStat;
import net.sf.l2j.gameserver.model.actor.status.PcStatus;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.base.Experience;
import net.sf.l2j.gameserver.model.base.Race;
import net.sf.l2j.gameserver.model.base.SubClass;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.L2Event;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.entity.Zone;
import net.sf.l2j.gameserver.model.entity.ZoneType;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.model.waypoint.WayPointNode;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.ChangeWaitType;
import net.sf.l2j.gameserver.serverpackets.CharInfo;
import net.sf.l2j.gameserver.serverpackets.ExFishingEnd;
import net.sf.l2j.gameserver.serverpackets.ExFishingStart;
import net.sf.l2j.gameserver.serverpackets.ExOlympiadMode;
import net.sf.l2j.gameserver.serverpackets.ExOlympiadUserInfo;
import net.sf.l2j.gameserver.serverpackets.HennaInfo;
import net.sf.l2j.gameserver.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.MagicSkillCanceld;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.ObservationMode;
import net.sf.l2j.gameserver.serverpackets.ObservationReturn;
import net.sf.l2j.gameserver.serverpackets.PartySmallWindowUpdate;
import net.sf.l2j.gameserver.serverpackets.PetInventoryUpdate;
import net.sf.l2j.gameserver.serverpackets.PledgeShowMemberListUpdate;
import net.sf.l2j.gameserver.serverpackets.PrivateStoreListBuy;
import net.sf.l2j.gameserver.serverpackets.PrivateStoreListSell;
import net.sf.l2j.gameserver.serverpackets.RecipeShopSellList;
import net.sf.l2j.gameserver.serverpackets.Ride;
import net.sf.l2j.gameserver.serverpackets.SendTradeDone;
import net.sf.l2j.gameserver.serverpackets.ServerBasePacket;
import net.sf.l2j.gameserver.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.serverpackets.ShortCutInit;
import net.sf.l2j.gameserver.serverpackets.Snoop;
import net.sf.l2j.gameserver.serverpackets.SocialAction;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.StopMove;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.TargetSelected;
import net.sf.l2j.gameserver.serverpackets.TradeStart;
import net.sf.l2j.gameserver.serverpackets.UserInfo;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.templates.L2Armor;
import net.sf.l2j.gameserver.templates.L2ArmorType;
import net.sf.l2j.gameserver.templates.L2Henna;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2PcTemplate;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.gameserver.util.Broadcast;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.util.Point3D;

/**
 * This class represents all player characters in the world.
 * There is always a client-thread connected to this (except if a player-store is activated upon logout).<BR><BR>
 *
 * @version $Revision: 1.66.2.41.2.33 $ $Date: 2005/04/11 10:06:09 $
 */
public final class L2PcInstance extends L2PlayableInstance
{
	private static final String RESTORE_SKILLS_FOR_CHAR = "SELECT skill_id,skill_level FROM character_skills WHERE char_obj_id=? AND class_index=?";
	private static final String ADD_NEW_SKILL = "INSERT INTO character_skills (char_obj_id,skill_id,skill_level,skill_name,class_index) VALUES (?,?,?,?,?)";
	private static final String UPDATE_CHARACTER_SKILL_LEVEL = "UPDATE character_skills SET skill_level=? WHERE skill_id=? AND char_obj_id=? AND class_index=?";
	private static final String DELETE_SKILL_FROM_CHAR = "DELETE FROM character_skills WHERE skill_id=? AND char_obj_id=? AND class_index=?";
	private static final String DELETE_CHAR_SKILLS = "DELETE FROM character_skills WHERE char_obj_id=? AND class_index=?";
	
	private static final String ADD_SKILL_SAVE = "INSERT INTO character_skills_save (char_obj_id,skill_id,skill_level,effect_count,effect_cur_time,class_index) VALUES (?,?,?,?,?,?)";
	private static final String RESTORE_SKILL_SAVE = "SELECT skill_id,skill_level,effect_count,effect_cur_time FROM character_skills_save WHERE char_obj_id=? AND class_index=?";
	private static final String DELETE_SKILL_SAVE = "DELETE FROM character_skills_save WHERE char_obj_id=? AND class_index=?";

    private static final String UPDATE_CHARACTER = "UPDATE characters SET level=?,maxHp=?,curHp=?,maxCp=?,curCp=?,maxMp=?,curMp=?,str=?,con=?,dex=?,_int=?,men=?,wit=?,face=?,hairStyle=?,hairColor=?,heading=?,x=?,y=?,z=?,exp=?,sp=?,karma=?,pvpkills=?,pkkills=?,rec_have=?,rec_left=?,clanid=?,maxload=?,race=?,classid=?,deletetime=?,title=?,allyId=?,accesslevel=?,online=?,isin7sdungeon=?,clan_privs=?,wantspeace=?,deleteclan=?,base_class=?,onlinetime=?,in_jail=?,jail_timer=?,newbie=?, nobless=? WHERE obj_id=?";
    private static final String RESTORE_CHARACTER = "SELECT account_name, obj_Id, char_name, level, maxHp, curHp, maxCp, curCp, maxMp, curMp, acc, crit, evasion, mAtk, mDef, mSpd, pAtk, pDef, pSpd, runSpd, walkSpd, str, con, dex, _int, men, wit, face, hairStyle, hairColor, sex, heading, x, y, z, movement_multiplier, attack_speed_multiplier, colRad, colHeight, exp, sp, karma, pvpkills, pkkills, clanid, maxload, race, classid, deletetime, cancraft, title, allyId, rec_have, rec_left, accesslevel, online, char_slot, lastAccess, clan_privs, wantspeace, deleteclan, base_class, onlinetime, isin7sdungeon, in_jail, jail_timer, newbie, nobless FROM characters WHERE obj_id=?";
    private static final String RESTORE_CHAR_SUBCLASSES = "SELECT class_id,exp,sp,level,class_index FROM character_subclasses WHERE char_obj_id=? ORDER BY class_index ASC";
    private static final String ADD_CHAR_SUBCLASS = "INSERT INTO character_subclasses (char_obj_id,class_id,exp,sp,level,class_index) VALUES (?,?,?,?,?,?)";
    private static final String UPDATE_CHAR_SUBCLASS = "UPDATE character_subclasses SET exp=?,sp=?,level=?,class_id=? WHERE char_obj_id=? AND class_index =?";
    private static final String DELETE_CHAR_SUBCLASS = "DELETE FROM character_subclasses WHERE char_obj_id=? AND class_index=?";

	private static final String RESTORE_CHAR_HENNAS = "SELECT slot,symbol_id FROM character_hennas WHERE char_obj_id=? AND class_index=?";
	private static final String ADD_CHAR_HENNA = "INSERT INTO character_hennas (char_obj_id,symbol_id,slot,class_index) VALUES (?,?,?,?)";
	private static final String DELETE_CHAR_HENNA = "DELETE FROM character_hennas WHERE char_obj_id=? AND slot=? AND class_index=?";
	private static final String DELETE_CHAR_HENNAS = "DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=?";
	private static final String DELETE_CHAR_SHORTCUTS = "DELETE FROM character_shortcuts WHERE char_obj_id=? AND class_index=?";
	
	public static final int REQUEST_TIMEOUT = 15;
	
	public static final int STORE_PRIVATE_NONE = 0;
	public static final int STORE_PRIVATE_SELL = 1;
	public static final int STORE_PRIVATE_BUY = 3;
	public static final int STORE_PRIVATE_MANUFACTURE = 5;
	public static final int STORE_PRIVATE_PACKAGE_SELL = 8;

	/** The table containing all minimum level needed for each Expertise (None, D, C, B, A, S)*/
	private static final int[] EXPERTISE_LEVELS =
	{
	 SkillTreeTable.getInstance().getExpertiseLevel(0), //NONE
	 SkillTreeTable.getInstance().getExpertiseLevel(1), //D
	 SkillTreeTable.getInstance().getExpertiseLevel(2), //C
	 SkillTreeTable.getInstance().getExpertiseLevel(3), //B
	 SkillTreeTable.getInstance().getExpertiseLevel(4), //A
	 SkillTreeTable.getInstance().getExpertiseLevel(5), //S
	};
	
	private static final int[] COMMON_CRAFT_LEVELS = 
	{
	 5,20,28,36,43,49,55,62
	};
	
	//private static Logger _log = Logger.getLogger(L2PcInstance.class.getName());
	
	public class AIAccessor extends L2Character.AIAccessor
	{
		protected AIAccessor() {}
		public L2PcInstance getPlayer() { return L2PcInstance.this; }
		public void doPickupItem(L2Object object) {
			L2PcInstance.this.doPickupItem(object);
		}
		public void doInteract(L2Character target) {
			L2PcInstance.this.doInteract(target);
		}
        
		public void doAttack(L2Character target) 
        {
			super.doAttack(target);
			
			for (L2CubicInstance cubic : getCubics().values())
				if (cubic.getId() != L2CubicInstance.LIFE_CUBIC)
					cubic.doAction(target);
		}
		
		/*public void doCast(L2Skill skill) 
        {
			super.doCast(skill);
			
			if(!skill.isOffensive()) return;
			L2Object[] targets = skill.getTargetList(L2PcInstance.this);
			// rest of the code doesn't yet support multiple targets
			if(targets == null) return;
			L2Character mainTarget = (L2Character) targets[0];
			for (L2CubicInstance cubic : getCubics().values())
				if (cubic.getId() != L2CubicInstance.LIFE_CUBIC)
					cubic.doAction(mainTarget);
		}*/
	}
	
	private Connection _connection;
	
	//private L2Object _newTarget = null;
	
	/** The Identifier of the L2PcInstance */
	private int _charId = 0x00030b7a;
	
	/** The Experience of the L2PcInstance before the last Death Penalty */
	private long _expBeforeDeath = 0;
	
	/** The Karma of the L2PcInstance (if higher than 0, the name of the L2PcInstance appears in red) */
	private int _karma;
	
	/** The number of player killed during a PvP (the player killed was PvP Flagged) */
	private int _pvpKills;
	
	/** The hexadecimal Color of players name (white is 0xFFFFFF) */
	private int _nameColor;
	
	/** The hexadecimal Color of players name (white is 0xFFFFFF) */
	private int _titleColor;
	
	/** The PK counter of the L2PcInstance (= Number of non PvP Flagged player killed) */
	private int _pkKills;
	
	/** The PvP Flag state of the L2PcInstance (0=White, 1=Purple) */
	private int _pvpFlag;
	
	private boolean _inPvpZone;
	private boolean _inMotherTreeZone;

    /** L2PcInstance's pledge class (knight, Baron, etc.)*/
    private int _pledgeClass;
    private int _pledgeType = 0;
	/** The number of recommandation obtained by the L2PcInstance */
	private int _recomHave; // how much I was recommended by others
	
	/** The number of recommandation that the L2PcInstance can give */
	private int _recomLeft; // how many recomendations I can give to others
	
	/** List with the recomendations that I've give */
	private List<Integer> _recomChars = new FastList<Integer>();
	
	/** The random number of the L2PcInstance */
	//private static final Random _rnd = new Random();
	
	private final int _baseLoad;
	private int _curWeightPenalty = 0;
	
	private long _deleteTimer;
	private PcInventory _inventory = new PcInventory(this);
	private PcWarehouse _warehouse = new PcWarehouse(this);
	private PcFreight _freight = new PcFreight(this);
	
	/** True if the L2PcInstance is sitting */
	private boolean _waitTypeSitting;
	
	/** True if the L2PcInstance is using the relax skill */
	private boolean _relax;
	
	/** True if the L2PcInstance is in a boat */
	private boolean _inBoat;
	
	/** Last NPC Id talked on a quest */
	private int _questNpcObject = 0; 
	
	/** The face type Identifier of the L2PcInstance */
	private int _face;
	
	/** The hair style Identifier of the L2PcInstance */
	private int _hairStyle;
	
	/** The hair color Identifier of the L2PcInstance */
	private int _hairColor;
	
	/** True if the L2PcInstance is newbie */
	private boolean _newbie;
	
	/** The table containing all Quests began by the L2PcInstance */
	private Map<String, QuestState> _quests = new FastMap<String, QuestState>();
	
	/** The list containing all shortCuts of this L2PcInstance */
	private ShortCuts _shortCuts = new ShortCuts(this);
	
	/** The list containing all macroses of this L2PcInstance */
	private MacroList _macroses = new MacroList(this);
	
	/** The Alliance Identifier of the L2PcInstance */
	//private int _allyId;
	
	private TradeList _activeTradeList;
	private ItemContainer _activeWarehouse;
	private L2ManufactureList _createList;
	private TradeList _sellList;
	private TradeList _buyList;
	
	private List<L2PcInstance> _SnoopListener = new FastList<L2PcInstance>();
	private List<L2PcInstance> _SnoopedPlayer = new FastList<L2PcInstance>();
	
	/** The Private Store type of the L2PcInstance (STORE_PRIVATE_NONE=0, STORE_PRIVATE_SELL=1, sellmanage=2, STORE_PRIVATE_BUY=3, buymanage=4, STORE_PRIVATE_MANUFACTURE=5) */
	private int _privatestore;
	private ClassId _skillLearningClassId;
	
	// hennas
	private final L2HennaInstance[] _henna = new L2HennaInstance[3];
	private int _hennaSTR;
	private int _hennaINT;
	private int _hennaDEX;
	private int _hennaMEN;
	private int _hennaWIT;
	private int _hennaCON;
	
	/** The L2Summon of the L2PcInstance */
	private L2Summon _summon = null;
	
	// client radar
	//TODO: This needs to be better intergrated and saved/loaded
	private L2Radar _radar;
	
	// these values are only stored temporarily
	private boolean _partyMatchingAutomaticRegistration;
	private boolean _partyMatchingShowLevel;
	private boolean _partyMatchingShowClass;
	private String _partyMatchingMemo;
	
	private L2Party _party;
	// clan related attributes
	
	/** The Clan Identifier of the L2PcInstance */
	private int _clanId;
	
	/** The Clan object of the L2PcInstance */
	private L2Clan _clan;
	
	/** The Clan Leader Flag of the L2PcInstance (True : the L2PcInstance is the leader of the clan) */
	private boolean _clanLeader;
	
	private long _deleteClanTime;
	
	private long _onlineTime;
	private long _onlineBeginTime;
	
	//GM Stuff
	private boolean _isInvul;
	private boolean _isGm;
	private int _accessLevel;
	
	private boolean _chatBanned = false; 		// Chat Banned
    private ScheduledFuture _chatUnbanTask = null;
	private boolean _messageRefusal = false;    // message refusal mode
	private boolean _dietMode = false;          // ignore weight penalty
	private boolean _tradeRefusal = false;       // Trade refusal
	private boolean _exchangeRefusal = false;   // Exchange refusal
	
	public boolean _exploring = false;
	
	// this is needed to find the inviting player for Party response
	// there can only be one active party request at once
	private L2PcInstance _activeRequester;
	private long _requestExpireTime = 0;
	private L2ItemInstance _arrowItem;
	
	// Used for protection after teleport
	private long _protectEndTime = 0;
	
	/** The fists L2Weapon of the L2PcInstance (used when no weapon is equiped) */
	private L2Weapon _fistsWeaponItem;
	
	private long _uptime;
	private String _accountName;
	
	private final Map<Integer, String> _chars = new FastMap<Integer, String>();
	
	public byte _updateKnownCounter = 0;
	
	/** The table containing all L2RecipeList of the L2PcInstance */
	private Map<Integer, L2RecipeList> _dwarvenRecipeBook = new FastMap<Integer, L2RecipeList>(); 
	private Map<Integer, L2RecipeList> _commonRecipeBook = new FastMap<Integer, L2RecipeList>(); 
	
	private int _mountType;
	
	/** The current higher Expertise of the L2PcInstance (None=0, D=1, C=2, B=3, A=4, S=5)*/
	private int _expertiseIndex; // index in EXPERTISE_LEVELS
	private int _expertisePenalty = 0;
	
	private L2ItemInstance _activeEnchantItem = null;
	
	private boolean _isOnline = false;
    private boolean _isIn7sDungeon = false;
	
	protected boolean _inventoryDisable = false;
	
	protected Map<Integer, L2CubicInstance> _cubics = new FastMap<Integer, L2CubicInstance>();
	
	/** The L2FolkInstance corresponding to the last Folk wich one the player talked. */
	private L2FolkInstance _lastFolkNpc = null;
	
	private boolean _isSilentMoving = false;
	
	protected Set<Integer> _activeSoulShots = new FastSet<Integer>();
	private boolean _isPathNodeMode;
	private boolean _isPathNodesVisible;
	private Map<WayPointNode, List<WayPointNode>> _pathNodeMap;
	private WayPointNode _selectedNode;
	private int _clanPrivileges = 0;
	private boolean _linkToggle = true;
	
	/** 1 if  the player is invisible */
	private int _invisible = 0;
	
	/** Location before entering Observer Mode */
	private int _obsX;
	private int _obsY;
	private int _obsZ;
	private boolean _observerMode = false;
	
	/** Event parameters */
	public int eventX;
	public int eventY;
	public int eventZ;
	public int eventkarma;
	public int eventpvpkills;
	public int eventpkkills;
	public String eventTitle;
	public LinkedList<String> kills = new LinkedList<String>();
	public boolean eventSitForced = false;
	public boolean atEvent = false;
	
	public int _telemode = 0;
	
	/** new loto ticket **/
	public int _loto[] = new int[5];
	//public static int _loto_nums[] = {0,1,2,3,4,5,6,7,8,9,};
	/** new race ticket **/
	public int _race[] = new int[2];
	
	private final BlockList _blockList = new BlockList();
	private boolean _isConnected = true;
	
	private boolean _hero = false;
	private int _team = 0;
	private int _wantsPeace = 0;
	
	private boolean _noble = false;
    private boolean _inOlympiadMode = false;
    private int _olympiadGameId = -1;
    private int _olympiadSide = -1;
	
	/** The list of sub-classes this character has. */
    private Map<Integer, SubClass> _subClasses; 
	protected int _baseClass;
	protected int _activeClass;
	protected int _classIndex = 0;
	
	private long _lastAccess; 
	private int _boatId;
    
    private L2Fishing _fish;
    private boolean _fishing = false;
    private int _fishx = 0;
    private int _fishy = 0;
    private int _fishz = 0;
    
    private ScheduledFuture _taskRentPet;
    private ScheduledFuture _taskWater;
    private L2BoatInstance _boat;
    private Point3D _inBoatPosition;
	
	/** Stored from last ValidatePosition **/
	public Point3D _lastClientPosition = new Point3D(0, 0, 0);
	public Point3D _lastServerPosition = new Point3D(0, 0, 0);
	
	/** Bypass validations */
	private List<String> _validBypass = new FastList<String>();
	private List<String> _validBypass2 = new FastList<String>();
	
	private boolean _inCrystallize;
	private boolean _inCraftMode;
	private Forum _forumMail;
	private Forum _forumMemo;
    
    /** Current skill in use */
    private SkillDat _currentSkill;
    
    /** Skills queued because a skill is already in progress */
    private SkillDat _queuedSkill;
	
	/** Store object used to summon the strider you are mounting **/
	private int _mountObjectID = 0;
    
    private boolean _inJail = false;
    private long _jailTimer = 0;
    
    /* Flag to disable equipment/skills while wearing formal wear **/
    //private boolean _IsWearingFormalWear = false;
    
    private ScheduledFuture _jailTask;
	private int _powerGrade;
	
	private int _cursedWeaponEquipedId = 0;
    
    
	/** Skill casting information (used to queue when several skills are cast in a short time) **/
    public class SkillDat
    {
        private L2Skill _skill;
        private boolean _ctrlPressed;
        private boolean _shiftPressed;
        
        protected SkillDat(L2Skill skill, boolean ctrlPressed, boolean shiftPressed)
        {
            _skill = skill;
            _ctrlPressed = ctrlPressed;
            _shiftPressed = shiftPressed;
        }

        public boolean isCtrlPressed()
        {
            return _ctrlPressed;
        }

        public boolean isShiftPressed()
        {
            return _shiftPressed;
        }

        public L2Skill getSkill()
        {
            return _skill;
        }
        
        public int getSkillId()
        {
            return (getSkill() != null) ? getSkill().getId() : -1;
        }
    }
    
	/**
	 * Create a new L2PcInstance and add it in the characters table of the database.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Create a new L2PcInstance with an account name </li>
	 * <li>Set the name, the Hair Style, the Hair Color and  the Face type of the L2PcInstance</li>
	 * <li>Add the player in the characters table of the database</li><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 * @param template The L2PcTemplate to apply to the L2PcInstance
	 * @param accountName The name of the L2PcInstance
	 * @param name The name of the L2PcInstance
	 * @param hairStyle The hair style Identifier of the L2PcInstance
	 * @param hairColor The hair color Identifier of the L2PcInstance
	 * @param face The face type Identifier of the L2PcInstance
	 *
	 * @return The L2PcInstance added to the database or null
	 *
	 */
	public static L2PcInstance create(int objectId, L2PcTemplate template, String accountName,
	                                  String name, int hairStyle, int hairColor, int face)
	{
		// Create a new L2PcInstance with an account name
		L2PcInstance player = new L2PcInstance(objectId, template, accountName);
		
		// Set the name of the L2PcInstance
		player.setName(name);
		
		// Set the Hair Style of the L2PcInstance
		player.setHairStyle(hairStyle);
		
		// Set the Hair Color of the L2PcInstance
		player.setHairColor(hairColor);
		
		// Set the Face type of the L2PcInstance
		player.setFace(face);
		
		// Set the base class ID to that of the actual class ID.
		player.setBaseClass(player.getClassId());
		
		if (Config.ALT_GAME_NEW_CHAR_ALWAYS_IS_NEWBIE)
			player.setNewbie(true);
		
		// Add the player in the characters table of the database
		boolean ok = player.createDb();
		
		if (!ok)
			return null;
		
		return player;
	}
	
	public static L2PcInstance createDummyPlayer(int objectId, String name)
	{   
		// Create a new L2PcInstance with an account name
		L2PcInstance player = new L2PcInstance(objectId);
		player.setName(name);
		
		return player;
	} 
	
	public String getAccountName()
	{
		if (_connection == null) 
            return "";
        
		return _connection.getClient().getLoginName();
	}
	
	public Map<Integer, String> getAccountChars()
	{
		return _chars;
	}
	
	/**
	 * Retrieve a L2PcInstance from the characters table of the database and add it in _allObjects of the L2world (call restore method).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Retrieve the L2PcInstance from the characters table of the database </li>
	 * <li>Add the L2PcInstance object in _allObjects </li>
	 * <li>Set the x,y,z position of the L2PcInstance and make it invisible</li>
	 * <li>Update the overloaded status of the L2PcInstance</li><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 *
	 * @return The L2PcInstance loaded from the database
	 *
	 */
	public static L2PcInstance load(int objectId)
	{
		return restore(objectId);
	}
	
	/**
	 * Constructor of L2PcInstance (use L2Character constructor).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Call the L2Character constructor to create an empty _skills slot and copy basic Calculator set to this L2PcInstance </li>
	 * <li>Set the name of the L2PcInstance</li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method SET the level of the L2PcInstance to 1</B></FONT><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 * @param template The L2PcTemplate to apply to the L2PcInstance
	 * @param accountName The name of the account including this L2PcInstance
	 *
	 */
	private L2PcInstance(int objectId, L2PcTemplate template, String accountName)
	{
		super(objectId, template);
		super.setKnownList(new PcKnownList(new L2PcInstance[] {this}));
		super.setStat(new PcStat(new L2PcInstance[] {this}));
		super.setStatus(new PcStatus(new L2PcInstance[] {this}));
		
		_accountName  = accountName;
		_nameColor    = 0xFFFFFF;
		_titleColor    = 0xFFFF77;
		_baseLoad      = template.baseLoad;
		
		// Create an AI
		_ai = new L2PlayerAI(new L2PcInstance.AIAccessor());
		
		// Create a L2Radar object
		_radar = new L2Radar(this);
		
		// Retrieve from the database all skills of this L2PcInstance and add them to _skills
		// Retrieve from the database all items of this L2PcInstance and add them to _inventory
		getInventory().restore();
		getWarehouse().restore();
		getFreight().restore();
	}
	
	private L2PcInstance(int objectId)
	{
		super(objectId, null);
		super.setKnownList(new PcKnownList(new L2PcInstance[] {this}));
		super.setStat(new PcStat(new L2PcInstance[] {this}));
		super.setStatus(new PcStatus(new L2PcInstance[] {this}));
		
		_baseLoad = 0;
	}
	
	public final PcKnownList getKnownList() { return (PcKnownList)super.getKnownList(); }
	public final PcStat getStat() { return (PcStat)super.getStat(); }
	public final PcStatus getStatus() { return (PcStatus)super.getStatus(); }
	
	/**
	 * Return the base L2PcTemplate link to the L2PcInstance.<BR><BR>
	 */
	public final L2PcTemplate getBaseTemplate()
	{
		return CharTemplateTable.getInstance().getTemplate(_baseClass, getSex() == 1);
	}
	
	/** Return the L2PcTemplate link to the L2PcInstance. */
	public final L2PcTemplate getTemplate() { return (L2PcTemplate)super.getTemplate(); }
	
	public void setTemplate(ClassId newclass, boolean female) { super.setTemplate(CharTemplateTable.getInstance().getTemplate(newclass, female)); }
	
	public void changeSex() { super.setTemplate(CharTemplateTable.getInstance().getTemplate(getClassId(), (getSex() == 1))); }
	
	/**
	 * Return the AI of the L2PcInstance (create it if necessary).<BR><BR>
	 */
	public L2CharacterAI getAI()
	{
		if (_ai == null)
		{
			synchronized(this)
			{
				if (_ai == null)
					_ai = new L2PlayerAI(new L2PcInstance.AIAccessor());
			}
		}
        
		return _ai;
	}
	
	
	/**
	 * Calculate a destination to explore the area and set the AI Intension to AI_INTENTION_MOVE_TO.<BR><BR>
	 */
	public void explore()
	{
		if (!_exploring) return;
		
		if(getMountType() == 2)
			return;
		
		// Calculate the destination point (random)
		
		int x = getX()+Rnd.nextInt(6000)-3000;
		int y = getY()+Rnd.nextInt(6000)-3000;
        
		if (x > Universe.MAX_X) x = Universe.MAX_X;
		if (x < Universe.MIN_X) x = Universe.MIN_X;
		if (y > Universe.MAX_Y) y = Universe.MAX_Y;
		if (y < Universe.MIN_Y) y = Universe.MIN_Y;
		
		int z = getZ();
		
		L2CharPosition pos = new L2CharPosition(x,y,z,0);
		
		// Set the AI Intention to AI_INTENTION_MOVE_TO
		getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,pos);
		
	}
	
	/** Return the Level of the L2PcInstance. */
	public final int getLevel() { return getStat().getLevel(); }
	
	/**
	 * Return the Sex of the L2PcInstance (Male=0, Female=1).<BR><BR>
	 */
	public int getSex() { return getTemplate().isMale ? 0 : 1; }
	
	/**
	 * Return the Face type Identifier of the L2PcInstance.<BR><BR>
	 */
	public int  getFace()
	{
		return _face;
	}
	
	/**
	 * Set the Face type of the L2PcInstance.<BR><BR>
	 *
	 * @param face The Identifier of the Face type<BR><BR>
	 *
	 */
	public void setFace(int face)
	{
		_face = face;
	}
	
	/**
	 * Return the Hair Color Identifier of the L2PcInstance.<BR><BR>
	 */
	public int  getHairColor()
	{
		return _hairColor;
	}
	
	/**
	 * Set the Hair Color of the L2PcInstance.<BR><BR>
	 *
	 * @param hairColor The Identifier of the Hair Color<BR><BR>
	 *
	 */
	public void setHairColor(int hairColor)
	{
		_hairColor = hairColor;
	}
	
	/**
	 * Return the Hair Style Identifier of the L2PcInstance.<BR><BR>
	 */
	public int getHairStyle()
	{
		return _hairStyle;
	}
	
	/**
	 * Set the Hair Style of the L2PcInstance.<BR><BR>
	 *
	 * @param hairStyle The Identifier of the Hair Style<BR><BR>
	 *
	 */
	public void setHairStyle(int hairStyle)
	{
		_hairStyle = hairStyle;
	}
	
	/**
	 * Return the _newbie state of the L2PcInstance.<BR><BR>
	 */
	public boolean isNewbie()
	{
		return _newbie;
	}

	/**
	 * Set the _newbie state of the L2PcInstance.<BR><BR>
	 *
	 * @param isNewbie The Identifier of the _newbie state<BR><BR>
	 *
	 */
	public void setNewbie(boolean isNewbie)
	{
		_newbie = isNewbie;
	}

	
	public void setBaseClass(int baseClass)
	{
		_baseClass = baseClass;
	}
	
	public void setBaseClass(ClassId classId)
	{
		_baseClass = classId.ordinal();
	}
	
	public boolean isInStoreMode() { return (getPrivateStoreType() > 0); }
//	public boolean isInCraftMode() { return (getPrivateStoreType() == STORE_PRIVATE_MANUFACTURE); }
	
	public boolean isInCraftMode()
	{
		return _inCraftMode;
	}
	
	public void isInCraftMode(boolean b)
	{
		_inCraftMode = b;
	}
	
	/**
	 * Manage Logout Task.<BR><BR>
	 */
	public void logout()
	{
		// Delete all Path Nodes
		clearPathNodes();
		
		// Close the connection with the client
		if (_connection != null)
            _connection.close();
	}
	
	/**
	 * Return a table containing all Common L2RecipeList of the L2PcInstance.<BR><BR> 
	 */ 
	public L2RecipeList[] getCommonRecipeBook() 
	{ 
		return _commonRecipeBook.values().toArray(new L2RecipeList[_commonRecipeBook.values().size()]);
	}
	
	/** 
	 * Return a table containing all Dwarf L2RecipeList of the L2PcInstance.<BR><BR> 
	 */ 
	public L2RecipeList[] getDwarvenRecipeBook() 
	{ 
		return _dwarvenRecipeBook.values().toArray(new L2RecipeList[_dwarvenRecipeBook.values().size()]);
	} 
	
	/** 
	 * Add a new L2RecipList to the table _commonrecipebook containing all L2RecipeList of the L2PcInstance <BR><BR> 
	 * 
	 * @param recipe The L2RecipeList to add to the _recipebook 
	 * 
	 */ 
	public void registerCommonRecipeList(L2RecipeList recipe) 
	{ 
		_commonRecipeBook.put(recipe.getId(), recipe); 
	}
	
	/**
	 * Add a new L2RecipList to the table _recipebook containing all L2RecipeList of the L2PcInstance <BR><BR>
	 *
	 * @param recipe The L2RecipeList to add to the _recipebook
	 *
	 */
	public void registerDwarvenRecipeList(L2RecipeList recipe) 
	{
		_dwarvenRecipeBook.put(recipe.getId(), recipe); 
	} 
	
	/** 
	 * @param RecipeID The Identifier of the L2RecipeList to check in the player's recipe books 
	 * 
	 * @return  
	 * <b>TRUE</b> if player has the recipe on Common or Dwarven Recipe book else returns <b>FALSE</b> 
	 */ 
	public boolean hasRecipeList(int recipeId) 
	{ 
		if (_dwarvenRecipeBook.containsKey(recipeId)) 
			return true; 
		else if(_commonRecipeBook.containsKey(recipeId)) 
			return true; 
		else 
			return false; 
	} 
	
	/** 
	 * Tries to remove a L2RecipList from the table _DwarvenRecipeBook or from table _CommonRecipeBook, those table contain all L2RecipeList of the L2PcInstance <BR><BR> 
	 *
	 * @param RecipeID The Identifier of the L2RecipeList to remove from the _recipebook
	 *
	 */
	public void unregisterRecipeList(int recipeId) 
	{
		if (_dwarvenRecipeBook.containsKey(recipeId)) 
			_dwarvenRecipeBook.remove(recipeId); 
		else if(_commonRecipeBook.containsKey(recipeId)) 
			_commonRecipeBook.remove(recipeId); 
		else  
			_log.warning("Attempted to remove unknown RecipeList: "+recipeId);
	}
	
	/**
	 * Returns the Id for the last talked quest NPC.<BR><BR>
	 */
	public int getLastQuestNpcObject()
	{
		return _questNpcObject;
	}
	
	public void setLastQuestNpcObject(int npcId)
	{
		_questNpcObject = npcId;
	}
	
	/**
	 * Add Quest drops to the table containing all possible drops of a L2NpcInstance.<BR><BR>
	 *
	 * @param npc The L2NpcInstance killed by the L2PcInstance
	 * @param drops The table containing all possible drops of the L2NpcInstance
	 *
	 */
	public void fillQuestDrops(L2NpcInstance npc, List<L2DropData> drops)
	{
		for (QuestState qs : _quests.values())
			qs.fillQuestDrops(npc, drops);
	}
	
	/**
	 * Return the QuestState object corresponding to the quest name.<BR><BR>
	 *
	 * @param quest The name of the quest
	 *
	 */
	public QuestState getQuestState(String quest)
	{
		return _quests.get(quest);
	}
	
	/**
	 * Add a QuestState to the table _quest containing all quests began by the L2PcInstance.<BR><BR>
	 *
	 * @param qs The QuestState to add to _quest
	 *
	 */
	public void setQuestState(QuestState qs)
	{
		_quests.put(qs.getQuest().getName(), qs);
	}
	
	
	/**
	 * Remove a QuestState from the table _quest containing all quests began by the L2PcInstance.<BR><BR>
	 *
	 * @param quest The name of the quest
	 *
	 */
	public void delQuestState(String quest)
	{
		_quests.remove(quest);
	}
	
	private QuestState[] addToQuestStateArray(QuestState[] questStateArray, QuestState state) {
		int len = questStateArray.length;
		QuestState[] tmp = new QuestState[len+1];
		for (int i=0; i < len; i++)
			tmp[i] = questStateArray[i];
		tmp[len] = state;
		return tmp;
	}
	
	/**
	 * Return a table containing all Quest in progress from the table _quests.<BR><BR>
	 */
	public Quest[] getAllActiveQuests()
	{
		FastList<Quest> quests = new FastList<Quest>();
		
		for (QuestState qs : _quests.values())
		{
			if (qs.getQuest().getQuestIntId()>=1999)
				continue;
			
			if (qs.isCompleted() && !Config.DEVELOPER)
				continue;
			
			if (!qs.isStarted() && !Config.DEVELOPER)
				continue;
			
			quests.add(qs.getQuest());
		}
		
		return quests.toArray(new Quest[quests.size()]);
	}
	
	/**
	 * Return a table containing all QuestState to modify after a L2Attackable killing.<BR><BR>
	 *
	 * @param npcId The Identifier of the L2Attackable attacked
	 *
	 */
	public QuestState[] getQuestsForAttacks(L2NpcInstance npc)
	{
		// Create a QuestState table that will contain all QuestState to modify
		QuestState[] states = null;
		
		// Go through the QuestState of the L2PcInstance quests
		for (QuestState qs : _quests.values())
		{
			// Check if the Identifier of the L2Attackable attck is needed for the current quest
			if (qs.waitsForAttack(npc))
			{
				// Copy the current L2PcInstance QuestState in the QuestState table
				if (states == null)
					states = new QuestState[]{qs};
				else
					states = addToQuestStateArray(states, qs);
			}
		}
		
		// Return a table containing all QuestState to modify
		return states;
	}
	
	/**
	 * Return a table containing all QuestState to modify after a L2Attackable killing.<BR><BR>
	 *
	 * @param npcId The Identifier of the L2Attackable killed
	 *
	 */
	public QuestState[] getQuestsForKills(L2NpcInstance npc)
	{
		// Create a QuestState table that will contain all QuestState to modify
		QuestState[] states = null;
		
		// Go through the QuestState of the L2PcInstance quests
		for (QuestState qs : _quests.values())
		{
			// Check if the Identifier of the L2Attackable killed is needed for the current quest
			if (qs.waitsForKill(npc))
			{
				// Copy the current L2PcInstance QuestState in the QuestState table
				if (states == null)
					states = new QuestState[]{qs};
				else
					states = addToQuestStateArray(states, qs);
			}
		}
		
		// Return a table containing all QuestState to modify
		return states;
	}
	
	/**
	 * Return a table containing all QuestState from the table _quests in which the L2PcInstance must talk to the NPC.<BR><BR>
	 *
	 * @param npcId The Identifier of the NPC
	 *
	 */
	public QuestState[] getQuestsForTalk(int npcId)
	{
		// Create a QuestState table that will contain all QuestState to modify
		QuestState[] states = null;
		
		// Go through the QuestState of the L2PcInstance quests
		for (QuestState qs : _quests.values())
		{
			// Check if the Identifier of the L2Attackable talk is needed for the current quest
			if (qs.waitsForTalk(npcId))
			{
				// Copy the current L2PcInstance QuestState in the QuestState table
				if (states == null)
					states = new QuestState[]{qs};
				else
					states = addToQuestStateArray(states, qs);
			}
		}
		
		// Return a table containing all QuestState to modify
		return states;
	}
	
	public QuestState processQuestEvent(String quest, String event)
	{
		QuestState retval = null;
		if (event == null)
			event = "";
		if (!_quests.containsKey(quest))
			return retval;
		QuestState qs = getQuestState(quest);
		if (qs == null && event.length() == 0)
			return retval;
		if (qs == null) {
			Quest q = QuestManager.getInstance().getQuest(quest);
			if (q == null)
				return retval;
			qs = q.newQuestState(this);
		}
		if (qs != null) {
			if (getLastQuestNpcObject() > 0)
			{
				L2Object object = L2World.getInstance().findObject(getLastQuestNpcObject());
                if (object instanceof L2NpcInstance && isInsideRadius(object, L2NpcInstance.INTERACTION_DISTANCE, false, false))
				{
					L2NpcInstance npc = (L2NpcInstance)object;
					QuestState[] states = getQuestsForTalk(npc.getNpcId());
					
					if (states != null)
					{
						for (QuestState state : states)
						{
							if ((state.getQuest().getQuestIntId() == qs.getQuest().getQuestIntId()) && !qs.isCompleted())
							{
								if (qs.getQuest().notifyEvent(event, qs))
									showQuestWindow(quest, qs.getStateId());
								
								retval = qs;
							}
						}
					}
				}
			}
		}
		
		return retval;
	}
	
	private void showQuestWindow(String questId, String stateId)
	{
		String path = "data/jscript/quests/"+questId+"/"+stateId+".htm";
		String content = HtmCache.getInstance().getHtm(path);  //TODO path for quests html
		
		if (content != null)
		{
			if (Config.DEBUG)
				_log.fine("Showing quest window for quest "+questId+" state "+stateId+" html path: " + path);
			
			NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
			npcReply.setHtml(content);
			sendPacket(npcReply);
		}
		
		sendPacket( new ActionFailed() );
	}
	
	/**
	 * Return a table containing all L2ShortCut of the L2PcInstance.<BR><BR>
	 */
	public L2ShortCut[] getAllShortCuts()
	{
		return _shortCuts.getAllShortCuts();
	}
	
	/**
	 * Return the L2ShortCut of the L2PcInstance corresponding to the position (page-slot).<BR><BR>
	 *
	 * @param slot The slot in wich the shortCuts is equiped
	 * @param page The page of shortCuts containing the slot
	 *
	 */
	public L2ShortCut getShortCut(int slot, int page)
	{
		return _shortCuts.getShortCut(slot, page);
	}
	
	/**
	 * Add a L2shortCut to the L2PcInstance _shortCuts<BR><BR>
	 */
	public void registerShortCut(L2ShortCut shortcut)
	{
		_shortCuts.registerShortCut(shortcut);
	}
	
	/**
	 * Delete the L2ShortCut corresponding to the position (page-slot) from the L2PcInstance _shortCuts.<BR><BR>
	 */
	public void deleteShortCut(int slot, int page)
	{
		_shortCuts.deleteShortCut(slot, page);
	}
	
	/**
	 * Add a L2Macro to the L2PcInstance _macroses<BR><BR>
	 */
	public void registerMacro(L2Macro macro)
	{
		_macroses.registerMacro(macro);
	}
	
	/**
	 * Delete the L2Macro corresponding to the Identifier from the L2PcInstance _macroses.<BR><BR>
	 */
	public void deleteMacro(int id)
	{
		_macroses.deleteMacro(id);
	}
	
	/**
	 * Return all L2Macro of the L2PcInstance.<BR><BR>
	 */
	public MacroList getMacroses()
	{
		return _macroses;
	}
	
	/**
	 * Set the PvP Flag of the L2PcInstance.<BR><BR>
	 */
	public void setPvpFlag(int pvpFlag)
	{
		_pvpFlag = pvpFlag;
	}
	
	public boolean getInPvpZone()
	{
		return _inPvpZone;
	}

    /**
     * Update the _inPvpZone flag and send a message to player.<br><br>
     * @param inPvpZone : true if player is in a pvp zone
     * @see net.sf.l2j.gameserver.model.actor.instance.L2PcInstance.setInPvpZone(boolean, boolean)
     */
    public void setInPvpZone(boolean inPvpZone)
    {
        setInPvpZone(inPvpZone, true);
    }
    /**
     * Update the _inPvpZone flag.<br><br>
     * @param inPvpZone : true if player is in a pvp zone
     * @param warnPlayer : true if a message should be send to player
     */
	public void setInPvpZone(boolean inPvpZone, boolean warnPlayer)
	{
		if (_inPvpZone != inPvpZone)
		{
			_inPvpZone = inPvpZone;
            
            if (warnPlayer)
    			if (_inPvpZone) 
                    sendPacket(new SystemMessage(283));
    			else  
                    sendPacket(new SystemMessage(284));
		}
	}

	private void revalidateInPvpZone ()
	{
		setInPvpZone(ZoneManager.getInstance().checkIfInZonePvP(this));
	}

	public boolean getInMotherTreeZone()
	{
		return _inMotherTreeZone;
	}

	/**
	 * Update the _inMotherTreeZone flag and send a message to player.<br><br>
	 * @param inMotherTreeZone : true if player is in a mother tree zone
	 * @see net.sf.l2j.gameserver.model.actor.instance.L2PcInstance.setInMotherTreeZone(boolean, boolean)
	 */
	public void setInMotherTreeZone(boolean inMotherTreeZone)
	{
		setInMotherTreeZone(inMotherTreeZone, true);
	}
	/**
	 * Update the _inMotherTreeZone flag.<br><br>
	 * @param inMotherTreeZone : true if player is in a mother tree zone
	 * @param warnPlayer : true if a message should be send to player
	 */
	public void setInMotherTreeZone(boolean inMotherTreeZone, boolean warnPlayer)
	{
		if (getRace() != Race.elf)
		{
			// if player is not a member of elven race Mother Tree have no effect on him
			_inMotherTreeZone = false;
			return;
		}

		if (isInParty())
			for (L2PcInstance member : getParty().getPartyMembers())
				if (member.getRace() != Race.elf)
				{
					// if player is in party with a non-elven race Mother Tree effect is cancelled
					_inMotherTreeZone = false;
					return;
				}
		
		if (_inMotherTreeZone != inMotherTreeZone)
		{
			_inMotherTreeZone = inMotherTreeZone;

			if (warnPlayer)
				if (_inMotherTreeZone) 
					sendPacket(new SystemMessage(SystemMessage.ENTER_SHADOW_MOTHER_TREE));
				else 
					sendPacket(new SystemMessage(SystemMessage.EXIT_SHADOW_MOTHER_TREE));
		}
	}

	public void revalidateInMotherTreeZone()
	{
		setInMotherTreeZone(ZoneManager.getInstance().checkIfInZone(ZoneType.getZoneTypeName(ZoneType.ZoneTypeEnum.MotherTree), this));
	}
    
    public void revalidateZone()
    {
        revalidateInPvpZone();
        revalidateInMotherTreeZone();
    }
	
	/**
	 * Return True if the L2PcInstance can Craft Dwarven Recipes.<BR><BR> 
	 */ 
	public boolean hasDwarvenCraft() 
	{ 
		return getSkillLevel(L2Skill.SKILL_CREATE_DWARVEN) >= 1; 
	} 
	
	public int getDwarvenCraft() 
	{ 
		return getSkillLevel(L2Skill.SKILL_CREATE_DWARVEN); 
	} 
	
	/** 
	 * Return True if the L2PcInstance can Craft Dwarven Recipes.<BR><BR> 
	 */ 
	public boolean hasCommonCraft() 
	{ 
		return getSkillLevel(L2Skill.SKILL_CREATE_COMMON) >= 1;
	}
	
	public int getCommonCraft() 
	{ 
		return getSkillLevel(L2Skill.SKILL_CREATE_COMMON);
	}
	
	/**
	 * Return the PK counter of the L2PcInstance.<BR><BR>
	 */
	public int getPkKills()
	{
		return _pkKills;
	}
	
	/**
	 * Set the PK counter of the L2PcInstance.<BR><BR>
	 */
	public void setPkKills(int pkKills)
	{
		_pkKills = pkKills;
	}
	
	/**
	 * Return the _deleteTimer of the L2PcInstance.<BR><BR>
	 */
	public long getDeleteTimer()
	{
		return _deleteTimer;
	}
	
	/**
	 * Set the _deleteTimer of the L2PcInstance.<BR><BR>
	 */
	public void setDeleteTimer(long deleteTimer)
	{
		_deleteTimer = deleteTimer;
	}
	
	/**
	 * Return the current weight of the L2PcInstance.<BR><BR>
	 */
	public int getCurrentLoad()
	{
		return _inventory.getTotalWeight();
	}
	/**
	 * Return the number of recommandation obtained by the L2PcInstance.<BR><BR>
	 */
	public int getRecomHave()
	{
		return _recomHave;
	}
	
	/**
	 * Increment the number of recommandation obtained by the L2PcInstance (Max : 255).<BR><BR>
	 */
	protected void incRecomHave()
	{
		if (_recomHave < 255)
			_recomHave++;
	}
	
	/**
	 * Set the number of recommandation obtained by the L2PcInstance (Max : 255).<BR><BR>
	 */
	public void setRecomHave(int value)
	{
		if (value > 255)
			_recomHave = 255;
		else if (value < 0)
			_recomHave = 0;
		else
			_recomHave = value;
	}
	
	
	
	/**
	 * Return the number of recommandation that the L2PcInstance can give.<BR><BR>
	 */
	public int getRecomLeft()
	{
		return _recomLeft;
	}
	
	/**
	 * Increment the number of recommandation that the L2PcInstance can give.<BR><BR>
	 */
	protected void decRecomLeft()
	{
		if (_recomLeft > 0)
			_recomLeft--;
	}
	
	public void giveRecom(L2PcInstance target)
	{
		target.incRecomHave();
		decRecomLeft();
		_recomChars.add(target.getName().hashCode());
	}
	
	public boolean canRecom(L2PcInstance target)
	{
		return !_recomChars.contains(target.getName().hashCode());
	}
	
	/**
	 * Return the Karma of the L2PcInstance.<BR><BR>
	 */
	public int getKarma()
	{
		return _karma;
	}
	
	/**
	 * Set the Karma of the L2PcInstance and send a Server->Client packet StatusUpdate (broadcast).<BR><BR>
	 */
	public void setKarma(int karma)
	{
		if (karma < 0) karma = 0;
		if (_karma == 0 && karma > 0)
		{
			for (L2Object object : getKnownList().getKnownObjects())
			{
				if (object == null || !(object instanceof L2GuardInstance)) continue;
				
				if (((L2GuardInstance)object).getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
					((L2GuardInstance)object).getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
			}
		}
		else if (_karma > 0 && karma == 0)
		{
			// Send a Server->Client StatusUpdate packet with Karma and PvP Flag to the L2PcInstance and all L2PcInstance to inform (broadcast)
			setKarmaFlag(0);
		}
		
		_karma = karma;
		updateKarma();
	}
	
	/**
	 * Return the max weight that the L2PcInstance can load.<BR><BR>
	 */
	public int getMaxLoad()
	{
		return (int)calcStat(Stats.MAX_LOAD, _baseLoad, this, null);
	}
	
	public int getexpertisePenalty()
	{
		return _expertisePenalty;
	}
    
	public int getWeightPenalty()
	{
		if (_dietMode)
			return 0;
        
		return _curWeightPenalty;
	}
	
	/**
	 * Update the overloaded status of the L2PcInstance.<BR><BR>
	 */
	public void refreshOverloaded()
	{
		if (getMaxLoad() > 0 && !_dietMode)
		{
			setIsOverloaded(getCurrentLoad() > getMaxLoad());
			int weightproc = getCurrentLoad() * 1000 / getMaxLoad();
			int newWeightPenalty;
			if ( weightproc < 500) newWeightPenalty = 0;
			else if ( weightproc < 666) newWeightPenalty = 1;
			else if ( weightproc < 800) newWeightPenalty = 2;
			else if ( weightproc < 1000) newWeightPenalty = 3;
			else newWeightPenalty = 4;
			
			if (_curWeightPenalty != newWeightPenalty)
			{
				_curWeightPenalty = newWeightPenalty;
				if (newWeightPenalty > 0) 
					super.addSkill(SkillTable.getInstance().getInfo(4270,newWeightPenalty));
				else super.removeSkill(getKnownSkill(4270));
				
				super.updateEffectIcons();
			}
		}
	}
	
	public void refreshExpertisePenalty()
	{
		int newPenalty = 0;
        
		for (L2ItemInstance item : getInventory().getItems())
		{
			if (item.isEquipped())
			{
				int crystaltype = item.getItem().getCrystalType();
                
				if (crystaltype > newPenalty)
					newPenalty = crystaltype;
			}
		}
        
		newPenalty = newPenalty - getExpertiseIndex();
        
		if (newPenalty <= 0) 
            newPenalty = 0;
		
		if (getexpertisePenalty() != newPenalty)
		{
			_expertisePenalty = newPenalty;
            
			if (newPenalty > 0) 
				super.addSkill(SkillTable.getInstance().getInfo(4267, newPenalty));
			else 
                super.removeSkill(getKnownSkill(4267));
			
			super.updateEffectIcons();
		}
	}
    
    public void CheckIfWeaponIsAllowed()
    {
        // Override for Gamemasters
        if (isGM())
            return;
        
        // Iterate through all effects currently on the character.
        for (L2Effect currenteffect : this.getAllEffects())
        { 
            L2Skill effectSkill = currenteffect.getSkill();
            
            // Ignore all buff skills that are party related (ie. songs, dances) while still remaining weapon dependant on cast though.
            if (!(effectSkill.getTargetType() == SkillTargetType.TARGET_PARTY && effectSkill.getSkillType() == SkillType.BUFF))
            {
                // Check to rest to assure current effect meets weapon requirements.
                if (!effectSkill.getWeaponDependancy(this))
                {
                    sendMessage(effectSkill.getName() + " cannot be used with this weapon.");
                    
                    if (Config.DEBUG) 
                        _log.info("   | Skill "+effectSkill.getName()+" has been disabled for ("+this.getName()+"); Reason: Incompatible Weapon Type.");
                    
                    currenteffect.exit();
                }
            }
            
            continue;
        }
    }
	
	/**
	 * Return the the PvP Kills of the L2PcInstance (Number of player killed during a PvP).<BR><BR>
	 */
	public int getPvpKills()
	{
		return _pvpKills;
	}
	
	/**
	 * Set the the PvP Kills of the L2PcInstance (Number of player killed during a PvP).<BR><BR>
	 */
	public void setPvpKills(int pvpKills)
	{
		_pvpKills = pvpKills;
	}
	
	/**
	 * Return the ClassId object of the L2PcInstance contained in L2PcTemplate.<BR><BR>
	 */
	public ClassId getClassId()
	{
		return getTemplate().classId;
	}
	
	/**
	 * Set the template of the L2PcInstance.<BR><BR>
	 *
	 * @param Id The Identifier of the L2PcTemplate to set to the L2PcInstance
	 *
	 */
	public void setClassId(int Id)
	{
		_activeClass = Id;
		L2PcTemplate t = CharTemplateTable.getInstance().getTemplate(Id, getSex()==1);
		
		if (t == null)
		{
			_log.severe("Missing template for classId: "+Id);
			throw new Error();
		}
		
		// Set the template of the L2PcInstance
		setTemplate(t);
	}
	
	/** Return the Experience of the L2PcInstance. */
	public long getExp() { return getStat().getExp(); }
	
	
	public void setActiveEnchantItem(L2ItemInstance scroll)
	{
		_activeEnchantItem = scroll;
	}
	
	public L2ItemInstance getActiveEnchantItem()
	{
		return _activeEnchantItem;
	}
	
	/**
	 * Set the fists weapon of the L2PcInstance (used when no weapon is equiped).<BR><BR>
	 *
	 * @param weaponItem The fists L2Weapon to set to the L2PcInstance
	 *
	 */
	public void setFistsWeaponItem(L2Weapon weaponItem)
	{
		_fistsWeaponItem = weaponItem;
	}
	
	/**
	 * Return the fists weapon of the L2PcInstance (used when no weapon is equiped).<BR><BR>
	 */
	public L2Weapon getFistsWeaponItem()
	{
		return _fistsWeaponItem;
	}
	
	/**
	 * Return the fists weapon of the L2PcInstance Class (used when no weapon is equiped).<BR><BR>
	 */
	public L2Weapon findFistsWeaponItem(int classId)
	{
		L2Weapon weaponItem = null;
		if ((classId >= 0x00) && (classId <= 0x09))
		{
			//human fighter fists
			L2Item temp = ItemTable.getInstance().getTemplate(246);
			weaponItem = (L2Weapon)temp;
		}
		else if ((classId >= 0x0a) && (classId <= 0x11))
		{
			//human mage fists
			L2Item temp = ItemTable.getInstance().getTemplate(251);
			weaponItem = (L2Weapon)temp;
		}
		else if ((classId >= 0x12) && (classId <= 0x18))
		{
			//elven fighter fists
			L2Item temp = ItemTable.getInstance().getTemplate(244);
			weaponItem = (L2Weapon)temp;
		}
		else if ((classId >= 0x19) && (classId <= 0x1e))
		{
			//elven mage fists
			L2Item temp = ItemTable.getInstance().getTemplate(249);
			weaponItem = (L2Weapon)temp;
		}
		else if ((classId >= 0x1f) && (classId <= 0x25))
		{
			//dark elven fighter fists
			L2Item temp = ItemTable.getInstance().getTemplate(245);
			weaponItem = (L2Weapon)temp;
		}
		else if ((classId >= 0x26) && (classId <= 0x2b))
		{
			//dark elven mage fists
			L2Item temp = ItemTable.getInstance().getTemplate(250);
			weaponItem = (L2Weapon)temp;
		}
		else if ((classId >= 0x2c) && (classId <= 0x30))
		{
			//orc fighter fists
			L2Item temp = ItemTable.getInstance().getTemplate(248);
			weaponItem = (L2Weapon)temp;
		}
		else if ((classId >= 0x31) && (classId <= 0x34))
		{
			//orc mage fists
			L2Item temp = ItemTable.getInstance().getTemplate(252);
			weaponItem = (L2Weapon)temp;
		}
		else if ((classId >= 0x35) && (classId <= 0x39))
		{
			//dwarven fists
			L2Item temp = ItemTable.getInstance().getTemplate(247);
			weaponItem = (L2Weapon)temp;
		}
		
		return weaponItem;
	}
	
	/**
	 * Give Expertise skill of this level and remove beginner Lucky skill.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get the Level of the L2PcInstance </li>
	 * <li>If L2PcInstance Level is 5, remove beginner Lucky skill </li>
	 * <li>Add the Expertise skill corresponding to its Expertise level</li>
	 * <li>Update the overloaded status of the L2PcInstance</li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T give other free skills (SP needed = 0)</B></FONT><BR><BR>
	 *
	 */
	public void rewardSkills()
	{
		// Get the Level of the L2PcInstance
		int lvl = getLevel();
		
		// Remove beginner Lucky skill
		if (lvl == 5)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(194, 1);
			skill = removeSkill(skill);
			
			if (Config.DEBUG && skill != null) _log.fine("removed skill 'Lucky' from "+getName());
		}
		
		// Calculate the current higher Expertise of the L2PcInstance
		for (int i=0; i < EXPERTISE_LEVELS.length; i++)
		{
			if (lvl >= EXPERTISE_LEVELS[i])
				setExpertiseIndex(i);
		}
		
		// Add the Expertise skill corresponding to its Expertise level
		if (getExpertiseIndex() > 0)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(239, getExpertiseIndex());
			addSkill(skill);
			
			if (Config.DEBUG) _log.fine("awarded "+getName()+" with new expertise.");
			
		}
		else
		{
			if (Config.DEBUG) _log.fine("No skills awarded at lvl: "+lvl);
		}
		
		//Active skill dwarven craft
		
		if (getSkillLevel(1321) < 1 && getRace() == Race.dwarf)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(1321,1);
			addSkill(skill);
		}
		
		//Active skill common craft
		if (getSkillLevel(1322) < 1)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(1322,1);
			addSkill(skill);
		}
		
		for(int i = 0; i < COMMON_CRAFT_LEVELS.length; i++)
		{
			if(lvl >= COMMON_CRAFT_LEVELS[i] && getSkillLevel(1320) < (i+1))
			{
				L2Skill skill = SkillTable.getInstance().getInfo(1320, (i+1));
				addSkill(skill);
			}
		}
		
		// Auto-Learn skills if activated
		if (Config.AUTO_LEARN_SKILLS)
		{
			giveAvailableSkills();
		}
		
		// This function gets called on login, so not such a bad place to check weight
		refreshOverloaded();		// Update the overloaded status of the L2PcInstance
		refreshExpertisePenalty();  // Update the expertise status of the L2PcInstance
	}
	
	/**
	 * Give all available skills to the player.<br><br>
	 *
	 */
	private void giveAvailableSkills()
	{
		int unLearnable = 0;
		int skillCounter = 0;
		
		// Get available skills
		L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkills(this, getClassId());
		while (skills.length > unLearnable)
		{
			for (int i = 0; i < skills.length; i++)
			{
				L2SkillLearn s = skills[i];
				L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
				if (sk == null || !sk.getCanLearn(getClassId()))
				{
					unLearnable++;
					continue;
				}
				
				if (getSkillLevel(sk.getId()) == -1)
					skillCounter++;
				
				addSkill(sk);
			}
			
			// Get new available skills
			skills = SkillTreeTable.getInstance().getAvailableSkills(this, getClassId());
		}
		
		sendMessage("You have learned " + skillCounter + " new skills.");
	}

	/** Set the Experience value of the L2PcInstance. */
	public void setExp(long exp) { getStat().setExp(exp); }
	
	/**
	 * Return the Race object of the L2PcInstance.<BR><BR>
	 */
	public Race getRace()
	{
		if (!isSubClassActive())
			return getTemplate().race;
		
		boolean isFemale = (getSex() == 1);
		L2PcTemplate charTemp = CharTemplateTable.getInstance().getTemplate(_baseClass, isFemale);
		
		return charTemp.race;
	}
    
    public L2Radar getRadar()
    {
        return _radar;
    }
	
	/** Return the SP amount of the L2PcInstance. */
	public int getSp() { return getStat().getSp(); }
	
	/** Set the SP amount of the L2PcInstance. */
	public void setSp(int sp) { super.getStat().setSp(sp); }
	
	public int getPvpFlag()
	{
		return _pvpFlag;
	}
	
	/**
	 * Return true if this L2PcInstance is a clan leader in 
	 * ownership of the passed castle
	 */
	public boolean isCastleLord(int castleId)
	{
		L2Clan clan = getClan();
        
		// player has clan and is the clan leader, check the castle info
		if ((clan != null) && (clan.getLeader().getPlayerInstance() == this))
		{
			// if the clan has a castle and it is actually the queried castle, return true
			Castle castle = CastleManager.getInstance().getCastleByOwner(clan);
			if ((castle != null) && (castle == CastleManager.getInstance().getCastle(castleId)))
				return true;
		}
        
		return false;
	}
	/**
	 * Return the Clan Identifier of the L2PcInstance.<BR><BR>
	 */
	public int getClanId()
	{
		return _clanId;
	}
	
	/**
	 * Return the Clan Crest Identifier of the L2PcInstance or 0.<BR><BR>
	 */
	public int getClanCrestId()
	{        
		if (_clan != null && _clan.hasCrest())
			return _clan.getCrestId();

		return 0;
	}
	
	/**
	 * @return The Clan CrestLarge Identifier or 0
	 */
	public int getClanCrestLargeId()
	{        
		if (_clan != null && _clan.hasCrestLarge())
			return _clan.getCrestLargeId();

		return 0;
	}
	
	public long getDeleteClanTime()
	{
		return _deleteClanTime;
	}
    
	public void setDeleteClanTime(long time)
	{
		_deleteClanTime = time;
	}
    
	public void setDeleteClanCurTime()
	{
		_deleteClanTime = System.currentTimeMillis();
	}
    
	public void setOnlineTime(long time)
	{
		_onlineTime = time;
		_onlineBeginTime = System.currentTimeMillis();
	}
    
	public boolean canJoinClan()
	{
		return (_deleteClanTime == 0) || ((System.currentTimeMillis() - _deleteClanTime) >= Config.ALT_CLAN_JOIN_DAYS * 86400000); //24*60*60*1000 = 86400000
	}
    
	public boolean canCreateClan()
	{
		return (_deleteClanTime == 0) || ((System.currentTimeMillis() - _deleteClanTime) >= Config.ALT_CLAN_CREATE_DAYS * 86400000); //24*60*60*1000 = 86400000
	}
	
	/**
	 * Return the PcInventory Inventory of the L2PcInstance contained in _inventory.<BR><BR>
	 */
	public PcInventory getInventory()
	{
		return _inventory;
	}
	
	/**
	 * Delete a ShortCut of the L2PcInstance _shortCuts.<BR><BR>
	 */
	public void removeItemFromShortCut(int objectId)
	{
		_shortCuts.deleteShortCutByObjectId(objectId);
	}
	
	/**
	 * Return True if the L2PcInstance is sitting.<BR><BR>
	 */
	public boolean isSitting()
	{
		return _waitTypeSitting;
	}
	
	/**
	 * Sit down the L2PcInstance, set the AI Intention to AI_INTENTION_REST and send a Server->Client ChangeWaitType packet (broadcast)<BR><BR>
	 */
	public void sitDown()
	{
		if (isCastingNow())
		{
			sendMessage("Cannot sit while casting");
			return;
		}
        
		if (!_waitTypeSitting && !isAttackingDisabled() && !isOutOfControl() && !isImobilised())
		{
			breakAttack();
			
			_waitTypeSitting = true;
			getAI().setIntention(CtrlIntention.AI_INTENTION_REST);
			broadcastPacket(new ChangeWaitType (this, ChangeWaitType.WT_SITTING));
		}
	}
	
	/**
	 * Stand up the L2PcInstance, set the AI Intention to AI_INTENTION_IDLE and send a Server->Client ChangeWaitType packet (broadcast)<BR><BR>
	 */
	public void standUp()
	{
		if (L2Event.active && eventSitForced)
		{
			sendMessage("A dark force beyond your mortal understanding makes your knees to shake when you try to stand up ...");
		}
		else if (_waitTypeSitting && !isInStoreMode() && !isAlikeDead())
		{
			if (_relax)
			{
				setRelax(false);
				stopEffects(L2Effect.EffectType.RELAXING);
			}
            
			_waitTypeSitting = false;
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			broadcastPacket(new ChangeWaitType (this, ChangeWaitType.WT_STANDING));
		}
	}
	
	/**
	 * Set the value of the _relax value. Must be True if using skill Relax and False if not. 
	 */
	public void setRelax(boolean val)
	{
		_relax = val;
	}
	
	/**
	 * Return the PcWarehouse object of the L2PcInstance.<BR><BR>
	 */
	public PcWarehouse getWarehouse()
	{
		return _warehouse;
	}
	
	/**
	 * Return the PcFreight object of the L2PcInstance.<BR><BR>
	 */
	public PcFreight getFreight()
	{
		return _freight;
	}
	
	/**
	 * Return the Identifier of the L2PcInstance.<BR><BR>
	 */
	public int getCharId()
	{
		return _charId;
	}
	
	/**
	 * Set the Identifier of the L2PcInstance.<BR><BR>
	 */
	public void setCharId(int charId)
	{
		_charId = charId;
	}
	
	/**
	 * Return the Adena amount of the L2PcInstance.<BR><BR>
	 */
	public int getAdena()
	{
		return _inventory.getAdena();
	}
    
    /**
     * Return the Ancient Adena amount of the L2PcInstance.<BR><BR>
     */
	public int getAncientAdena()
	{
	    return _inventory.getAncientAdena();
	}
	
	/**
	 * Add adena to Inventory of the L2PcInstance and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param count : int Quantity of adena to be added
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 */
	public void addAdena(String process, int count, L2Object reference, boolean sendMessage)
	{
		if (sendMessage)
		{
			SystemMessage sm = new SystemMessage(SystemMessage.EARNED_ADENA);
			sm.addNumber(count);
			sendPacket(sm);
		}
		
		if (count > 0)
		{
			_inventory.addAdena(process, count, this, reference);
			
			// Send update packet
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				InventoryUpdate iu = new InventoryUpdate();
				iu.addItem(_inventory.getAdenaInstance());
				sendPacket(iu);
			}
			else sendPacket(new ItemList(this, false));
		}
	}
	
	/**
	 * Reduce adena in Inventory of the L2PcInstance and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param count : int Quantity of adena to be reduced
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	public boolean reduceAdena(String process, int count, L2Object reference, boolean sendMessage)
	{
		if (count > getAdena())
		{
			if (sendMessage) sendPacket(new SystemMessage(SystemMessage.YOU_NOT_ENOUGH_ADENA));
			return false;
		}
		
		if (count > 0)
		{
			L2ItemInstance adenaItem = _inventory.getAdenaInstance();
			_inventory.reduceAdena(process, count, this, reference);
			
			// Send update packet
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				InventoryUpdate iu = new InventoryUpdate();
				iu.addItem(adenaItem);
				sendPacket(iu);
			}
			else sendPacket(new ItemList(this, false));
			
			if (sendMessage)
			{
				SystemMessage sm = new SystemMessage(SystemMessage.DISSAPEARED_ADENA);
				sm.addNumber(count);
				sendPacket(sm);
			}
		}
		
		return true;
	}
	
	/**
	 * Add ancient adena to Inventory of the L2PcInstance and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * 
	 * @param process : String Identifier of process triggering this action
	 * @param count : int Quantity of ancient adena to be added
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 */
	public void addAncientAdena(String process, int count, L2Object reference, boolean sendMessage)
	{
	    if (sendMessage)
	    {
	        SystemMessage sm = new SystemMessage(SystemMessage.EARNED_S2_S1_s);
	        sm.addItemName(PcInventory.ANCIENT_ADENA_ID);
            sm.addNumber(count);
	        sendPacket(sm);
	    }
	    
	    if (count > 0)
	    {
	        _inventory.addAncientAdena(process, count, this, reference);
	        
	        if (!Config.FORCE_INVENTORY_UPDATE)
	        {
	            InventoryUpdate iu = new InventoryUpdate();
	            iu.addItem(_inventory.getAncientAdenaInstance());
	            sendPacket(iu);
	        }
	        else sendPacket(new ItemList(this, false));
	    }
	}
	
	/**
	 * Reduce ancient adena in Inventory of the L2PcInstance and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param count : int Quantity of ancient adena to be reduced
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	public boolean reduceAncientAdena(String process, int count, L2Object reference, boolean sendMessage)
	{
	    if (count > getAncientAdena())
	    {
	        if (sendMessage) 
	            sendPacket(new SystemMessage(SystemMessage.YOU_NOT_ENOUGH_ADENA));
	        
	        return false;
	    }
	    
	    if (count > 0)
	    {
	        L2ItemInstance ancientAdenaItem = _inventory.getAncientAdenaInstance();
	        _inventory.reduceAncientAdena(process, count, this, reference);
	        
	        if (!Config.FORCE_INVENTORY_UPDATE)
	        {
	            InventoryUpdate iu = new InventoryUpdate();
	            iu.addItem(ancientAdenaItem);
	            sendPacket(iu);
	        }
	        else 
	        {
	            sendPacket(new ItemList(this, false));
	        }
	        
	        if (sendMessage)
	        {
	            SystemMessage sm = new SystemMessage(SystemMessage.DISSAPEARED_ITEM);
	            sm.addNumber(count);
	            sm.addItemName(PcInventory.ANCIENT_ADENA_ID);
	            sendPacket(sm);
	        }
	    }
	    
	    return true;
	}
    
	/**
	 * Adds item to inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param item : L2ItemInstance to be added
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 */
	public void addItem(String process, L2ItemInstance item, L2Object reference, boolean sendMessage)
	{
		if (item.getCount() > 0)
		{
			// Sends message to client if requested
			if (sendMessage)
			{
				if (item.getCount() > 1) 
				{
					SystemMessage sm = new SystemMessage(SystemMessage.YOU_PICKED_UP_S1_S2);
					sm.addItemName(item.getItemId());
					sm.addNumber(item.getCount());
					sendPacket(sm);
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessage.YOU_PICKED_UP_S1);
					sm.addItemName(item.getItemId());
					sendPacket(sm);
				}
			}
			
			// Add the item to inventory
			L2ItemInstance newitem = _inventory.addItem(process, item, this, reference);
			
			// Send inventory update packet
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				InventoryUpdate playerIU = new InventoryUpdate();
				playerIU.addItem(newitem);
				sendPacket(playerIU);
			}
			else 
			{
			    sendPacket(new ItemList(this, false));
			}
			
			// Update current load as well
			StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
			sendPacket(su);
			
			// Cursed Weapon
			if(CursedWeaponsManager.getInstance().isCursed(newitem.getItemId()))
			{
				CursedWeaponsManager.getInstance().activate(this, newitem);
			}

	    	// If over capacity, trop the item 
	    	if (!isGM() && !_inventory.validateCapacity(0)) 
                dropItem("InvDrop", newitem, null, true);
		}
	}
	
	/**
	 * Adds item to Inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param itemId : int Item Identifier of the item to be added
	 * @param count : int Quantity of items to be added
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 */
	public void addItem(String process, int itemId, int count, L2Object reference, boolean sendMessage)
	{
		if (count > 0) 
		{
			// Sends message to client if requested
			if (sendMessage)
			{
				if (count > 1) 
				{
					if (process.equalsIgnoreCase("sweep") || process.equalsIgnoreCase("Quest"))
					{
						SystemMessage sm = new SystemMessage(SystemMessage.EARNED_S2_S1_s);
						sm.addItemName(itemId);
						sm.addNumber(count);
						sendPacket(sm);
					}
					else
					{
						SystemMessage sm = new SystemMessage(SystemMessage.YOU_PICKED_UP_S1_S2);
						sm.addItemName(itemId);
						sm.addNumber(count);
						sendPacket(sm);
					}
				}
				else
				{
					if (process.equalsIgnoreCase("sweep") || process.equalsIgnoreCase("Quest"))
					{
						SystemMessage sm = new SystemMessage(SystemMessage.EARNED_ITEM);
						sm.addItemName(itemId);
						sendPacket(sm);
					}
					else
					{
						SystemMessage sm = new SystemMessage(SystemMessage.YOU_PICKED_UP_S1);
						sm.addItemName(itemId);
						sendPacket(sm);
					}
				}
			}
			
			// Add the item to inventory
			L2ItemInstance item = _inventory.addItem(process, itemId, count, this, reference);
			
			// Send inventory update packet
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				InventoryUpdate playerIU = new InventoryUpdate();
				playerIU.addItem(item);
				sendPacket(playerIU);
			}
			else sendPacket(new ItemList(this, false));
			
			// Update current load as well
			StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
			sendPacket(su);

			// Cursed Weapon
			if(CursedWeaponsManager.getInstance().isCursed(item.getItemId()))
			{
				CursedWeaponsManager.getInstance().activate(this, item);
			}

	    	// If over capacity, trop the item 
	    	if (!isGM() && !_inventory.validateCapacity(0)) 
	    		dropItem("InvDrop", item, null, true);
		}
	}
	
	/**
	 * Destroy item from inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param item : L2ItemInstance to be destroyed
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	public boolean destroyItem(String process, L2ItemInstance item, L2Object reference, boolean sendMessage)
	{
		int oldCount = item.getCount();
		item = _inventory.destroyItem(process, item, this, reference);
        
		if (item == null)
		{
			if (sendMessage) 
                sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_ITEMS));
            
			return false;
		}
		
		// Send inventory update packet
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(item);
			sendPacket(playerIU);
		}
        else sendPacket(new ItemList(this, false));
		
		// Update current load as well
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		
		// Sends message to client if requested
		if (sendMessage)
		{
			SystemMessage sm = new SystemMessage(SystemMessage.DISSAPEARED_ITEM);
			sm.addNumber(oldCount);
			sm.addItemName(item.getItemId());
			sendPacket(sm);
		}
        
		return true;
	}
	
	/**
	 * Destroys item from inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param objectId : int Item Instance identifier of the item to be destroyed
	 * @param count : int Quantity of items to be destroyed
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	public boolean destroyItem(String process, int objectId, int count, L2Object reference, boolean sendMessage)
	{
        L2ItemInstance item = _inventory.getItemByObjectId(objectId);
        
		if (item == null || item.getCount() < count || _inventory.destroyItem(process, objectId, count, this, reference) == null)
		{
			if (sendMessage) 
                sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_ITEMS));
            
			return false;
		}
		
		// Send inventory update packet
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(item);
			sendPacket(playerIU);
		}
        else sendPacket(new ItemList(this, false));
		
		// Update current load as well
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		
		// Sends message to client if requested
		if (sendMessage)
		{
			SystemMessage sm = new SystemMessage(SystemMessage.DISSAPEARED_ITEM);
			sm.addNumber(count);
			sm.addItemName(item.getItemId());
			sendPacket(sm);
		}
        
		return true;
	}
	
	/**
	 * Destroy item from inventory by using its <B>itemId</B> and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param itemId : int Item identifier of the item to be destroyed
	 * @param count : int Quantity of items to be destroyed
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	public boolean destroyItemByItemId(String process, int itemId, int count, L2Object reference, boolean sendMessage)
	{
        L2ItemInstance item = _inventory.getItemByItemId(itemId);
		
        if (item == null || item.getCount() < count || _inventory.destroyItemByItemId(process, itemId, count, this, reference) == null)
		{
			if (sendMessage) 
                sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_ITEMS));
            
			return false;
		}
		
		// Send inventory update packet
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(item);
			sendPacket(playerIU);
		}
        else sendPacket(new ItemList(this, false));
		
		// Update current load as well
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		
		// Sends message to client if requested
		if (sendMessage)
		{
			SystemMessage sm = new SystemMessage(SystemMessage.DISSAPEARED_ITEM);
            sm.addNumber(count);
            sm.addItemName(itemId);
			sendPacket(sm);
		}
        
		return true;
	}
	
	/**
	 * Destroy all weared items from inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	public void destroyWearedItems(String process, L2Object reference, boolean sendMessage)
	{
        
        // Go through all Items of the inventory
        for (L2ItemInstance item : getInventory().getItems())
		{
            // Check if the item is a Try On item in order to remove it
			if (item.isWear())
			{
                if (item.isEquipped()) 
                    getInventory().unEquipItemInSlotAndRecord(item.getEquipSlot());
                
				if (_inventory.destroyItem(process, item, this, reference) == null)
				{
					_log.warning("Player " + getName() + " can't destroy weared item: " + item.getName() + "[ " + item.getObjectId() + " ]");
					continue;
				}
                
				// Send an Unequipped Message in system window of the player for each Item
				SystemMessage sm = new SystemMessage(0x1a1);
				sm.addItemName(item.getItemId());
				sendPacket(sm);
                
			}
		}
		
        
        // Send the StatusUpdate Server->Client Packet to the player with new CUR_LOAD (0x0e) information
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		
        // Send the ItemList Server->Client Packet to the player in order to refresh its Inventory
        ItemList il = new ItemList(getInventory().getItems(), true);
        sendPacket(il);
        
        // Send a Server->Client packet UserInfo to this L2PcInstance and CharInfo to all L2PcInstance in its _KnownPlayers
		broadcastUserInfo();
		
		// Sends message to client if requested
		sendMessage("Trying-on mode has ended.");
        
	}
	
	/**
	 * Transfers item to another ItemContainer and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param itemId : int Item Identifier of the item to be transfered
	 * @param count : int Quantity of items to be transfered
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return L2ItemInstance corresponding to the new item or the updated item in inventory
	 */
	public L2ItemInstance transferItem(String process, int objectId, int count, Inventory target, L2Object reference)
	{
		L2ItemInstance oldItem = checkItemManipulation(objectId, count, "transfer"); 
		if (oldItem == null) return null;
		L2ItemInstance newItem = getInventory().transferItem(process, objectId, count, target, this, reference);
		if (newItem == null) return null;
		
		// Send inventory update packet
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate playerIU = new InventoryUpdate();
            
			if (oldItem.getCount() > 0 && oldItem != newItem) 
                playerIU.addModifiedItem(oldItem);
			else 
                playerIU.addRemovedItem(oldItem);
            
			sendPacket(playerIU);
		}
		else sendPacket(new ItemList(this, false));
		
		// Update current load as well
		StatusUpdate playerSU = new StatusUpdate(getObjectId());
		playerSU.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(playerSU);
		
		// Send target update packet
		if (target instanceof PcInventory)
		{
			L2PcInstance targetPlayer = ((PcInventory)target).getOwner();
            
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				InventoryUpdate playerIU = new InventoryUpdate();
                
				if (newItem.getCount() > count) 
                    playerIU.addModifiedItem(newItem);
				else 
                    playerIU.addNewItem(newItem);
                
				targetPlayer.sendPacket(playerIU);
			}
			else targetPlayer.sendPacket(new ItemList(targetPlayer, false));
			
			// Update current load as well
			playerSU = new StatusUpdate(targetPlayer.getObjectId());
			playerSU.addAttribute(StatusUpdate.CUR_LOAD, targetPlayer.getCurrentLoad());
			targetPlayer.sendPacket(playerSU);
		}
		else if (target instanceof PetInventory)
		{
			PetInventoryUpdate petIU = new PetInventoryUpdate();
            
			if (newItem.getCount() > count) 
                petIU.addModifiedItem(newItem);
			else 
                petIU.addNewItem(newItem);
            
			((PetInventory)target).getOwner().getOwner().sendPacket(petIU);
		}
		
		return newItem;
	}
	
	/**
	 * Drop item from inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param item : L2ItemInstance to be dropped
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	public boolean dropItem(String process, L2ItemInstance item, L2Object reference, boolean sendMessage)
	{
		item = _inventory.dropItem(process, item, this, reference);
        
		if (item == null)
		{
			if (sendMessage) 
                sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_ITEMS));
            
			return false;
		}
		
		item.dropMe(this, getClientX() + Rnd.get(50) - 25, getClientY() + Rnd.get(50) - 25, getClientZ() + 20);
		// Avoids it from beeing removed by the auto item destroyer
		item.setDropTime(0); 
		
		// Send inventory update packet
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(item);
			sendPacket(playerIU);
		}
		else sendPacket(new ItemList(this, false));
		
		// Update current load as well
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		
		// Sends message to client if requested
		if (sendMessage)
		{
			SystemMessage sm = new SystemMessage(SystemMessage.YOU_DROPPED_S1);
			sm.addItemName(item.getItemId());
			sendPacket(sm);
		}
        
		return true;
	}
	
	/**
	 * Drop item from inventory by using its <B>objectID</B> and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param objectId : int Item Instance identifier of the item to be dropped
	 * @param count : int Quantity of items to be dropped
	 * @param x : int coordinate for drop X
	 * @param y : int coordinate for drop Y
	 * @param z : int coordinate for drop Z
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return L2ItemInstance corresponding to the new item or the updated item in inventory
	 */
	public L2ItemInstance dropItem(String process, int objectId, int count, int x, int y, int z, L2Object reference, boolean sendMessage)
	{
		L2ItemInstance invitem = _inventory.getItemByObjectId(objectId);
		L2ItemInstance item = _inventory.dropItem(process, objectId, count, this, reference);
        
		if (item == null)
		{
			if (sendMessage) 
                sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_ITEMS));
            
			return null;
		}
		
		item.dropMe(this, x, y, z);
		// Avoids it from beeing removed by the auto item destroyer
		item.setDropTime(0); 
		
		// Send inventory update packet
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(invitem);
			sendPacket(playerIU);
		}
		else 
		{
		    sendPacket(new ItemList(this, false));
		}
		
		// Update current load as well
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		
		// Sends message to client if requested
		if (sendMessage)
		{
			SystemMessage sm = new SystemMessage(SystemMessage.YOU_DROPPED_S1);
			sm.addItemName(item.getItemId());
			sendPacket(sm);
		}
        
		return item;
	}
	
	public L2ItemInstance checkItemManipulation(int objectId, int count, String action)
	{
        //TODO: if we remove objects that are not visisble from the L2World, we'll have to remove this check
		if (L2World.getInstance().findObject(objectId) == null)
		{
			_log.finest(getObjectId()+": player tried to " + action + " item not available in L2World");
			return null;
		}
		
		L2ItemInstance item = getInventory().getItemByObjectId(objectId);
        
		if (item == null || item.getOwnerId() != getObjectId())
		{
			_log.finest(getObjectId()+": player tried to " + action + " item he is not owner of");
			return null;
		}
		
		if (count < 0 || (count > 1 && !item.isStackable()))
		{
			_log.finest(getObjectId()+": player tried to " + action + " item with invalid count: "+ count);
			return null;
		}

		if (count > item.getCount())
		{
			_log.finest(getObjectId()+": player tried to " + action + " more items than he owns");
			return null;
		}

		// Pet is summoned and not the item that summoned the pet AND not the buggle from strider you're mounting
		if (getPet() != null && getPet().getControlItemId() == objectId || getMountObjectID() == objectId)
		{
			if (Config.DEBUG) 
				_log.finest(getObjectId()+": player tried to " + action + " item controling pet");
            
			return null;
		}
		
		if (getActiveEnchantItem() != null && getActiveEnchantItem().getObjectId() == objectId)
		{
			if (Config.DEBUG) 
				_log.finest(getObjectId()+":player tried to " + action + " an enchant scroll he was using");
            
			return null;
		}
		
		if (item.isWear())
		{
			Util.handleIllegalPlayerAction(this, "Warning!! Character "+getName()+" tried to  " + action + "  weared item: "+item.getObjectId(),Config.DEFAULT_PUNISH);
			return null;
		}
		
		return item;
	}
	
	/**
	 * Set _protectEndTime according settings.
	 */
	public void setProtection(boolean protect)
	{
		if (Config.DEVELOPER && (protect || _protectEndTime > 0)) 
            System.out.println(this.getName() + ": Protection " + (protect?"ON " + (GameTimeController.getGameTicks() + Config.PLAYER_SPAWN_PROTECTION * GameTimeController.TICKS_PER_SECOND) :"OFF") + " (currently " + GameTimeController.getGameTicks() + ")");
        
		_protectEndTime = protect ? GameTimeController.getGameTicks() + Config.PLAYER_SPAWN_PROTECTION * GameTimeController.TICKS_PER_SECOND : 0;
	}
	
	/**
	 * Return the active connection with the client.<BR><BR>
	 */
	public Connection getNetConnection()
	{
		return _connection;
	}
	
	/**
	 * Set the active connection with the client.<BR><BR>
	 */
	public void setNetConnection(Connection connection)
	{
		_connection = connection;
	}
	
	/**
	 * Close the active connection with the client.<BR><BR>
	 */
	public void closeNetConnection()
	{
		if (getNetConnection() != null) 
            getNetConnection().close();
	}
	
	/**
	 * Manage actions when a player click on this L2PcInstance.<BR><BR>
	 *
	 * <B><U> Actions on first click on the L2PcInstance (Select it)</U> :</B><BR><BR>
	 * <li>Set the target of the player</li>
	 * <li>Send a Server->Client packet MyTargetSelected to the player (display the select window)</li><BR><BR>
	 *
	 * <B><U> Actions on second click on the L2PcInstance (Follow it/Attack it/Intercat with it)</U> :</B><BR><BR>
	 * <li>Send a Server->Client packet MyTargetSelected to the player (display the select window)</li>
	 * <li>If this L2PcInstance has a Private Store, notify the player AI with AI_INTENTION_INTERACT</li>
	 * <li>If this L2PcInstance is autoAttackable, notify the player AI with AI_INTENTION_ATTACK</li><BR><BR>
	 * <li>If this L2PcInstance is NOT autoAttackable, notify the player AI with AI_INTENTION_FOLLOW</li><BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Client packet : Action, AttackRequest</li><BR><BR>
	 *
	 * @param player The player that start an action on this L2PcInstance
	 *
	 */
	public void onAction(L2PcInstance player)
	{
		// Check if the L2PcInstance is confused
		if (player.isConfused())
		{
			// Send a Server->Client packet ActionFailed to the player
			ActionFailed af = new ActionFailed();
			player.sendPacket(af);
		}
		
		// Check if the player already target this L2PcInstance
		if (player.getTarget() != this)
		{
			// Set the target of the player
			player.setTarget(this);
			
			// Send a Server->Client packet MyTargetSelected to the player
			// The color to display in the select window is White
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);
		}
		else
		{
			// Check if this L2PcInstance has a Private Store
			if (getPrivateStoreType() != 0)
            {
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
            }
			else
			{
				// Check if this L2PcInstance is autoAttackable
				if (isAutoAttackable(player))
				{
					// Player with lvl < 21 can't attack a cursed weapon holder
					// And a cursed weapon holder  can't attack players with lvl < 21
					if ((isCursedWeaponEquiped() && player.getLevel() < 21)
							|| (player.isCursedWeaponEquiped() && this.getLevel() < 21))
					{
						player.sendPacket(new ActionFailed());
					} else
					{
						player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
					}
				} else
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
				}
			}
		}
	}
	
	/**
	 * Send packet StatusUpdate with current HP,MP and CP to the L2PcInstance and only current HP, MP and Level to all other L2PcInstance of the Party.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Send the Server->Client packet StatusUpdate with current HP, MP and CP to this L2PcInstance </li><BR>
	 * <li>Send the Server->Client packet PartySmallWindowUpdate with current HP, MP and Level to all other L2PcInstance of the Party </li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND current HP and MP to all L2PcInstance of the _statusListener</B></FONT><BR><BR>
	 *
	 */
	public void broadcastStatusUpdate()
	{
		//TODO We mustn't send these informations to other players
		// Send the Server->Client packet StatusUpdate with current HP and MP to all L2PcInstance that must be informed of HP/MP updates of this L2PcInstance
		//super.broadcastStatusUpdate();
		
		// Send the Server->Client packet StatusUpdate with current HP, MP and CP to this L2PcInstance
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
		su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
		su.addAttribute(StatusUpdate.CUR_CP, (int) getCurrentCp());
		su.addAttribute(StatusUpdate.MAX_CP, 	   getMaxCp());
		sendPacket(su);
		
		// Check if a party is in progress
		if (isInParty())
		{
			// Send the Server->Client packet PartySmallWindowUpdate with current HP, MP and Level to all other L2PcInstance of the Party
			PartySmallWindowUpdate update = new PartySmallWindowUpdate(this);
			getParty().broadcastToPartyMembers(this, update);
		}
        
        if (isInOlympiadMode())
        {
            if(Olympiad.getInstance().getSpectators(_olympiadGameId) != null)
            {
                for(L2PcInstance spectator : Olympiad.getInstance().getSpectators(_olympiadGameId))
                {
                    if (spectator == null) continue;
                    spectator.sendPacket(new ExOlympiadUserInfo(this, getOlympiadSide()));
                }
            }
        }
	}
	
	/**
	 * Send a Server->Client packet UserInfo to this L2PcInstance and CharInfo to all L2PcInstance in its _KnownPlayers.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * Others L2PcInstance in the detection area of the L2PcInstance are identified in <B>_knownPlayers</B>.
	 * In order to inform other players of this L2PcInstance state modifications, server just need to go through _knownPlayers to send Server->Client Packet<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Send a Server->Client packet UserInfo to this L2PcInstance (Public and Private Data)</li>
	 * <li>Send a Server->Client packet CharInfo to all L2PcInstance in _KnownPlayers of the L2PcInstance (Public data only)</li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : DON'T SEND UserInfo packet to other players instead of CharInfo packet.
	 * Indeed, UserInfo packet contains PRIVATE DATA as MaxHP, STR, DEX...</B></FONT><BR><BR>
	 *
	 */
	public final void broadcastUserInfo()
	{
		// Send a Server->Client packet UserInfo to this L2PcInstance
		sendPacket(new UserInfo(this));
		
		// Send a Server->Client packet CharInfo to all L2PcInstance in _KnownPlayers of the L2PcInstance
		if (Config.DEBUG) 
            _log.fine("players to notify:" + getKnownList().getKnownPlayers().size() + " packet: [S] 03 CharInfo");
        
		Broadcast.toKnownPlayers(this, new CharInfo(this));
	}
	
	/**
	 * Return the Alliance Identifier of the L2PcInstance.<BR><BR>
	 */
	public int getAllyId()
	{
		if (_clan == null)
			return 0;
        
		return _clan.getAllyId();
	}
	
	public int getAllyCrestId()
	{
		if (getAllyId() == 0)
			return 0;
        
		return _clan.getAllyCrestId();
	}
	
	/**
	 * Manage hit process (called by Hit Task of L2Character).<BR><BR>
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
		super.onHitTimer(target, damage, crit, miss, soulshot, shld);
	}
	
	/**
	 * Send a Server->Client packet StatusUpdate to the L2PcInstance.<BR><BR>
	 */
	public void sendPacket(ServerBasePacket packet)
	{
		if(_isConnected)
		{
			try
			{
				if (_connection != null)
					_connection.sendPacket(packet);
			}
			catch (Exception e)
			{
				_log.log(Level.INFO, "", e);
			}
		}
	}
	
	/**
	 * Manage Interact Task with another L2PcInstance.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>If the private store is a STORE_PRIVATE_SELL, send a Server->Client PrivateBuyListSell packet to the L2PcInstance</li>
	 * <li>If the private store is a STORE_PRIVATE_BUY, send a Server->Client PrivateBuyListBuy packet to the L2PcInstance</li>
	 * <li>If the private store is a STORE_PRIVATE_MANUFACTURE, send a Server->Client RecipeShopSellList packet to the L2PcInstance</li><BR><BR>
	 *
	 * @param target The L2Character targeted
	 *
	 */
	public void doInteract(L2Character target)
	{
		if (target instanceof L2PcInstance)
		{
			L2PcInstance temp = (L2PcInstance) target;
			sendPacket(new ActionFailed());
			
			if (temp.getPrivateStoreType() == STORE_PRIVATE_SELL || temp.getPrivateStoreType() == STORE_PRIVATE_PACKAGE_SELL)
				sendPacket(new PrivateStoreListSell(this, temp));
			else if (temp.getPrivateStoreType() == STORE_PRIVATE_BUY)
				sendPacket(new PrivateStoreListBuy(this, temp));
			else if (temp.getPrivateStoreType() == STORE_PRIVATE_MANUFACTURE)
				sendPacket(new RecipeShopSellList(this, temp));
			
		}
		else
		{
			// _interactTarget=null should never happen but one never knows ^^;
			if (target != null)
				target.onAction(this);
		}
	}
	
	/**
	 * Manage AutoLoot Task.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Send a System Message to the L2PcInstance : YOU_PICKED_UP_S1_ADENA or YOU_PICKED_UP_S1_S2</li>
	 * <li>Add the Item to the L2PcInstance inventory</li>
	 * <li>Send a Server->Client packet InventoryUpdate to this L2PcInstance with NewItem (use a new slot) or ModifiedItem (increase amount)</li>
	 * <li>Send a Server->Client packet StatusUpdate to this L2PcInstance with current weight</li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : If a Party is in progress, distribute Items between party members</B></FONT><BR><BR>
	 *
	 * @param target The L2ItemInstance dropped
	 *
	 */
	public void doAutoLoot(L2Attackable target, L2Attackable.RewardItem item)
	{
		if (isInParty()) getParty().distributeItem(this, item, false, target);
		else if (item.getItemId() == 57) addAdena("Loot", item.getCount(), target, true);
		else addItem("Loot", item.getItemId(), item.getCount(), target, true);
	}
	
	/**
	 * Manage Pickup Task.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Send a Server->Client packet StopMove to this L2PcInstance </li>
	 * <li>Remove the L2ItemInstance from the world and send server->client GetItem packets </li>
	 * <li>Send a System Message to the L2PcInstance : YOU_PICKED_UP_S1_ADENA or YOU_PICKED_UP_S1_S2</li>
	 * <li>Add the Item to the L2PcInstance inventory</li>
	 * <li>Send a Server->Client packet InventoryUpdate to this L2PcInstance with NewItem (use a new slot) or ModifiedItem (increase amount)</li>
	 * <li>Send a Server->Client packet StatusUpdate to this L2PcInstance with current weight</li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : If a Party is in progress, distribute Items between party members</B></FONT><BR><BR>
	 *
	 * @param object The L2ItemInstance to pick up
	 *
	 */
	protected void doPickupItem(L2Object object)
	{
		if (isAlikeDead() || isFakeDeath()) return;
		
		// Set the AI Intention to AI_INTENTION_IDLE
		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		
		// Check if the L2Object to pick up is a L2ItemInstance
		if (! (object instanceof L2ItemInstance))
		{
			// dont try to pickup anything that is not an item :)
			_log.warning("trying to pickup wrong target."+getTarget());
			return;
		}
		
		L2ItemInstance target = (L2ItemInstance) object;
		
		// Send a Server->Client packet ActionFailed to this L2PcInstance
		sendPacket(new ActionFailed());
		
		// Send a Server->Client packet StopMove to this L2PcInstance
		StopMove sm = new StopMove(getObjectId(), getX(), getY(), getZ(), getHeading());
		if (Config.DEBUG) _log.fine("pickup pos: "+ target.getX() + " "+target.getY()+ " "+target.getZ() );
		sendPacket(sm);
		
		synchronized (target)
		{
			// Check if the target to pick up is visible
			if (!target.isVisible())
			{
				// Send a Server->Client packet ActionFailed to this L2PcInstance
				sendPacket(new ActionFailed());
				return;
			}
			
			if ( ((isInParty() && getParty().getLootDistribution() == L2Party.ITEM_LOOTER) || !isInParty()) && !_inventory.validateCapacity(target))
			{
				sendPacket(new ActionFailed());
				sendPacket(new SystemMessage(SystemMessage.SLOTS_FULL));
				return;
			}
            
	        if (target.getOwnerId() != 0 && target.getOwnerId() != getObjectId() && !isInLooterParty(target.getOwnerId()))
            {
                sendPacket(new ActionFailed());
                
                if (target.getItemId() == 57)
                {
                    SystemMessage smsg = new SystemMessage(SystemMessage.FAILED_TO_PICKUP_S1_ADENA);
                    smsg.addNumber(target.getCount());
                    sendPacket(smsg);
                }
                else if (target.getCount() > 1)
                {
                    SystemMessage smsg = new SystemMessage(SystemMessage.FAILED_TO_PICKUP_S2_S1_s);
                    smsg.addItemName(target.getItemId());
                    smsg.addNumber(target.getCount());
                    sendPacket(smsg);
                }
                else
                {
                    SystemMessage smsg = new SystemMessage(SystemMessage.FAILED_TO_PICKUP_S1);
                    smsg.addItemName(target.getItemId());
                    sendPacket(smsg);
                }
                
                return;
            }
	        if(target.getItemLootShedule() != null
	        		&& (target.getOwnerId() == getObjectId() || isInLooterParty(target.getOwnerId())))
	        	target.resetOwnerTimer();
            
			// Remove the L2ItemInstance from the world and send server->client GetItem packets
			target.pickupMe(this);
		}
		
		// Check if a Party is in progress
		if (isInParty()) getParty().distributeItem(this, target);
		// Target is adena 
		else if (target.getItemId() == 57 && getInventory().getAdenaInstance() != null)
		{
			addAdena("Pickup", target.getCount(), null, true);
			ItemTable.getInstance().destroyItem("Pickup", target, this, null);
		}
		// Target is regular item 
		else addItem("Pickup", target, null, true);
	}
	
	/**
	 * Set a target.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove the L2PcInstance from the _statusListener of the old target if it was a L2Character </li>
	 * <li>Add the L2PcInstance to the _statusListener of the new target if it's a L2Character </li>
	 * <li>Target the new L2Object (add the target to the L2PcInstance _target, _knownObject and L2PcInstance to _KnownObject of the L2Object)</li><BR><BR>
	 *
	 * @param newTarget The L2Object to target
	 *
	 */
	public void setTarget(L2Object newTarget)
	{
		// Check if the new target is visible
		if (newTarget != null && !newTarget.isVisible())
			newTarget = null;
        
        // Prevents /target exploiting while no geodata
		if (newTarget != null && Math.abs(newTarget.getZ() - getZ()) > 1000)
            newTarget = null;
		
		// Can't target and attack festival monsters if not participant
		if (newTarget instanceof L2FestivalMonsterInstance && !isFestivalParticipant())
			newTarget = null;
		
		// Get the current target
		L2Object oldTarget = getTarget();
        
		if (oldTarget != null)
		{
			if (oldTarget.equals(newTarget))
				return; // no target change
			
			// Remove the L2PcInstance from the _statusListener of the old target if it was a L2Character
			if (oldTarget instanceof L2Character)
				((L2Character) oldTarget).removeStatusListener(this);
		}
		
		// Add the L2PcInstance to the _statusListener of the new target if it's a L2Character
		if (newTarget != null && newTarget instanceof L2Character)
		{
			((L2Character) newTarget).addStatusListener(this);
			TargetSelected my = new TargetSelected(getObjectId(), newTarget.getObjectId(), getX(), getY(), getZ());
			broadcastPacket(my);
		}
		
		// Target the new L2Object (add the target to the L2PcInstance _target, _knownObject and L2PcInstance to _KnownObject of the L2Object)
		super.setTarget(newTarget);
	}
	
	/**
	 * Return the active weapon instance (always equiped in the right hand).<BR><BR>
	 */
	public L2ItemInstance getActiveWeaponInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
	}
	
	/**
	 * Return the active weapon item (always equiped in the right hand).<BR><BR>
	 */
	public L2Weapon getActiveWeaponItem()
	{
		L2ItemInstance weapon = getActiveWeaponInstance();
		
		if (weapon == null)
			return getFistsWeaponItem();
		
		return (L2Weapon) weapon.getItem();
	}
	
	public L2ItemInstance getChestArmorInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
	}
	
	public L2Armor getActiveChestArmorItem()
	{
		L2ItemInstance armor = getChestArmorInstance();
		
		if (armor == null)
			return null;
		
		return (L2Armor) armor.getItem();
	}
	
	public boolean isWearingHeavyArmor()
	{
		L2ItemInstance armor = getChestArmorInstance();
        
		if ((L2ArmorType)armor.getItemType() == L2ArmorType.HEAVY)
			return true;
        
		return false;
	}
	
	public boolean isWearingLightArmor()
	{
		L2ItemInstance armor = getChestArmorInstance();
        
		if ((L2ArmorType)armor.getItemType() == L2ArmorType.LIGHT)
			return true;
        
		return false;
	}
	
	public boolean isWearingMagicArmor()
	{
		L2ItemInstance armor = getChestArmorInstance();
        
		if ((L2ArmorType)armor.getItemType() == L2ArmorType.MAGIC)
			return true;
        
		return false;
	}
    /*
    public boolean isWearingFormalWear() 
    { 
        return _IsWearingFormalWear; 
    }

    public void setIsWearingFormalWear(boolean value) { 
        _IsWearingFormalWear = value; 
    }*/
	
	/**
	 * Return the secondary weapon instance (always equiped in the left hand).<BR><BR>
	 */
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
	}
	
	/**
	 * Return the secondary weapon item (always equiped in the left hand) or the fists weapon.<BR><BR>
	 */
	public L2Weapon getSecondaryWeaponItem()
	{
		L2ItemInstance weapon = getSecondaryWeaponInstance();
		
		if (weapon == null)
			return getFistsWeaponItem();
		
		L2Item item = weapon.getItem();
		
		if (item instanceof L2Weapon)
			return (L2Weapon) item;
		
		return null;
	}
	
	
	/**
	 * Kill the L2Character, Apply Death Penalty, Manage gain/loss Karma and Item Drop.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Reduce the Experience of the L2PcInstance in function of the calculated Death Penalty </li>
	 * <li>If necessary, unsummon the Pet of the killed L2PcInstance </li>
	 * <li>Manage Karma gain for attacker and Karam loss for the killed L2PcInstance </li>
	 * <li>If the killed L2PcInstance has Karma, manage Drop Item</li>
	 * <li>Kill the L2PcInstance </li><BR><BR>
	 *
	 *
	 * @param i The HP decrease value
	 * @param attacker The L2Character who attacks
	 *
	 */
	public void doDie(L2Character killer)
	{
		// Kill the L2PcInstance
		super.doDie(killer);
		
		if (killer != null)
		{
			L2PcInstance pk = null;
			if (killer instanceof L2PcInstance)
				pk = (L2PcInstance) killer;
			
			if (atEvent && pk != null)
			{
				pk.kills.add(getName());
			}
            
			if (isCursedWeaponEquiped())
			{
				CursedWeaponsManager.getInstance().drop(_cursedWeaponEquipedId, killer);
			} else
			{
				if (pk == null || !pk.isCursedWeaponEquiped())
				{
					onDieDropItem(killer);  // Check if any item should be dropped
				
					if (!ArenaManager.getInstance().checkIfInZone(this))
					{
						if (Config.ALT_GAME_DELEVEL)
						{
							// Reduce the Experience of the L2PcInstance in function of the calculated Death Penalty
							// NOTE: deathPenalty +- Exp will update karma
							if (getSkillLevel(L2Skill.SKILL_LUCKY) < 0 || getStat().getLevel() > 4)
								deathPenalty((pk != null && this.getClan() != null && pk.getClan() != null && pk.getClan().isAtWarWith(this.getClanId())));
						} else
						{
							onDieUpdateKarma(); // Update karma if delevel is not allowed
						}
					}
				}
			}
		}
		
		setPvpFlag(0);              // Clear the pvp flag
		
		
		// Unsummon Cubics
		if (_cubics.size() > 0) 
        {
			for (L2CubicInstance cubic : _cubics.values())
			{
				cubic.stopAction();
				cubic.cancelDisappear();
			}
            
			_cubics.clear();
		}
		
		stopRentPet();
		stopWaterTask();
	}
	
	private void onDieDropItem(L2Character killer)
	{
		if (atEvent || killer == null) 
            return;
        
		if (getKarma()<=0 
                && killer instanceof L2PcInstance 
                && ((L2PcInstance)killer).getClan()!=null 
                && this.getClan()!=null 
                && (
                        ((L2PcInstance)killer).getClan().isAtWarWith(this.getClanId()) 
//                      || this.getClan().isAtWarWith(((L2PcInstance)killer).getClanId())
                   )
           )
			return;
		
		if (!ZoneManager.getInstance().checkIfInZonePvP(this) && (!isGM() || Config.KARMA_DROP_GM))
		{
			boolean isKarmaDrop = false;
			boolean isKillerNpc = (killer instanceof L2NpcInstance);
			int pkLimit = Config.KARMA_PK_LIMIT;;
			
			int dropEquip           = 0;
			int dropEquipWeapon     = 0;
			int dropItem            = 0;
			int dropLimit           = 0;
			int dropPercent         = 0;
			
			if (getKarma() > 0 && this.getPkKills() >= pkLimit)
			{
				isKarmaDrop = true;
				dropPercent = Config.KARMA_RATE_DROP;
				dropEquip = Config.KARMA_RATE_DROP_EQUIP;
				dropEquipWeapon = Config.KARMA_RATE_DROP_EQUIP_WEAPON;
				dropItem = Config.KARMA_RATE_DROP_ITEM;
				dropLimit = Config.KARMA_DROP_LIMIT;
			}
			else if (isKillerNpc && getLevel() > 4 && !isFestivalParticipant())
			{
				dropPercent = Config.PLAYER_RATE_DROP;
				dropEquip = Config.PLAYER_RATE_DROP_EQUIP;
				dropEquipWeapon = Config.PLAYER_RATE_DROP_EQUIP_WEAPON;
				dropItem = Config.PLAYER_RATE_DROP_ITEM;
				dropLimit = Config.PLAYER_DROP_LIMIT;
			}
			
            int dropCount = 0;
			while (dropPercent > 0 && Rnd.get(100) < dropPercent && dropCount < dropLimit)
			{
                int itemDropPercent = 0;
				List<Integer> nonDroppableList = new FastList<Integer>();
				List<Integer> nonDroppableListPet = new FastList<Integer>();
				
				nonDroppableList = Config.KARMA_LIST_NONDROPPABLE_ITEMS;
				nonDroppableListPet = Config.KARMA_LIST_NONDROPPABLE_ITEMS;
				
				for (L2ItemInstance itemDrop : getInventory().getItems())
				{
					// Don't drop
					if (
							itemDrop.getItemId() == 57 ||                                           // Adena
							itemDrop.getItem().getType2() == L2Item.TYPE2_QUEST ||                  // Quest Items
							nonDroppableList.contains(itemDrop.getItemId()) ||                      // Item listed in the non droppable item list
							nonDroppableListPet.contains(itemDrop.getItemId()) ||                   // Item listed in the non droppable pet item list
							getPet() != null && getPet().getControlItemId() == itemDrop.getItemId() // Control Item of active pet
					) continue;
					
					if (itemDrop.isEquipped())
					{
						// Set proper chance according to Item type of equipped Item
                        itemDropPercent = itemDrop.getItem().getType2() == L2Item.TYPE2_WEAPON ? dropEquipWeapon : dropEquip; 
						getInventory().unEquipItemInSlotAndRecord(itemDrop.getEquipSlot());
					}
					else itemDropPercent = dropItem; // Item in inventory
					
					// NOTE: Each time an item is dropped, the chance of another item being dropped gets lesser (dropCount * 2)
					if (Rnd.get(100) < itemDropPercent)
					{                    
						dropItem("DieDrop", itemDrop, killer, true);
						
						if (isKarmaDrop)
							_log.warning(getName() + " has karma and dropped id = " + itemDrop.getItemId() + ", count = " + itemDrop.getCount());
						else
							_log.warning(getName() + " dropped id = " + itemDrop.getItemId() + ", count = " + itemDrop.getCount());
						
						dropCount++;
						break;
					}
				}
			}
		}
	}
	
	private void onDieUpdateKarma()
	{
		// Karma lose for server that does not allow delevel
		if ( getKarma() > 0 )
		{
			// this formula seems to work relatively well:
			// baseKarma * thisLVL * (thisLVL/100)
			// Calculate the new Karma of the attacker : newKarma = baseKarma*pkCountMulti*lvlDiffMulti
			double karmaLost = Config.KARMA_LOST_BASE;
			karmaLost *= getLevel(); // multiply by char lvl
			karmaLost *= (getLevel() / 100); // divide by 0.charLVL
			karmaLost = Math.round(karmaLost);
			if ( karmaLost < 0 ) karmaLost = 1;
			
			// Decrease Karma of the L2PcInstance and Send it a Server->Client StatusUpdate packet with Karma and PvP Flag if necessary
			setKarma(getKarma() - (int)karmaLost);
		}
	}
	
	public void onKillUpdatePvPKarma(L2Character target)
	{
		if (target == null) return;
		if (!(target instanceof L2PlayableInstance)) return;
		
		L2PcInstance targetPlayer = null;
		if (target instanceof L2PcInstance)
			targetPlayer = (L2PcInstance)target;
		else if (target instanceof L2Summon)
			targetPlayer = ((L2Summon)target).getOwner();
		
		if (targetPlayer == null) return;                                          // Target player is null
		if (targetPlayer == this) return;                                          // Target player is self
		
		if (isCursedWeaponEquiped())
		{
			CursedWeaponsManager.getInstance().increaseKills(_cursedWeaponEquipedId);
			return;
		}
		
        // If in Arena, do nothing
		if (ArenaManager.getInstance().getArenaIndex(this.getX(),this.getY())!=-1 ||
				ArenaManager.getInstance().getArenaIndex(target.getX(),target.getY())!=-1)
			return;

        // Check if it's pvp
		if (
				(
						checkIfPvP(target) &&                                       //   Can pvp and
						targetPlayer.getPvpFlag() != 0                              //   Target player has pvp flag set
				) ||                                                                // or
				(
						ZoneManager.getInstance().checkIfInZonePvP(this) &&         //   Player is inside pvp zone and
						ZoneManager.getInstance().checkIfInZonePvP(target)          //   Target player is inside pvp zone
				)
		)
		{
            increasePvpKills();
		}
		else                                                                        // Target player doesn't have pvp flag set
		{
            // check about wars
            if (targetPlayer.getClan() != null && this.getClan() != null)
            {
                if (this.getClan().isAtWarWith(targetPlayer.getClanId()))
                {
                    if (targetPlayer.getClan().isAtWarWith(this.getClanId()))
                    {
                        // 'Both way war' -> 'PvP Kill' 
                        increasePvpKills();
                        return;
                    }
                }
            }
            
            // 'No war' or 'One way war' -> 'Normal PK'
			if (targetPlayer.getKarma() > 0)                                        // Target player has karma
			{
				if ( Config.KARMA_AWARD_PK_KILL )
				{
                    increasePvpKills();
				}
			}
			else if (targetPlayer.getPvpFlag() == 0)                                                                    // Target player doesn't have karma
			{
                increasePkKillsAndKarma(targetPlayer.getLevel());
			}
		}
	}
    
    /**
     * Increase the pvp kills count and send the info to the player
     *
     */
    public void increasePvpKills()
    {
        // Add karma to attacker and increase its PK counter
        setPvpKills(getPvpKills() + 1);
        
        // Send a Server->Client UserInfo packet to attacker with its Karma and PK Counter
        sendPacket(new UserInfo(this));
    }

    /**
     * Increase pk count, karma and send the info to the player
     * 
     * @param targLVL : level of the killed player
     */
    public void increasePkKillsAndKarma(int targLVL)
    {
        int baseKarma           = Config.KARMA_MIN_KARMA;
        int newKarma            = baseKarma;
        int karmaLimit          = Config.KARMA_MAX_KARMA;
        
        int pkLVL               = getLevel();
        int pkPKCount           = getPkKills();
        
        int lvlDiffMulti = 0;
        int pkCountMulti = 0;
        
        // Check if the attacker has a PK counter greater than 0
        if ( pkPKCount > 0 )
            pkCountMulti = pkPKCount / 2;
        else
            pkCountMulti = 1;
        if ( pkCountMulti < 1 ) pkCountMulti = 1;
        
        // Calculate the level difference Multiplier between attacker and killed L2PcInstance
        if ( pkLVL > targLVL )
            lvlDiffMulti = pkLVL / targLVL;
        else
            lvlDiffMulti = 1;
        if ( lvlDiffMulti < 1 ) lvlDiffMulti = 1;
        
        // Calculate the new Karma of the attacker : newKarma = baseKarma*pkCountMulti*lvlDiffMulti
        newKarma *= pkCountMulti;
        newKarma *= lvlDiffMulti;
        
        // Make sure newKarma is less than karmaLimit and higher than baseKarma
        if ( newKarma < baseKarma ) newKarma = baseKarma;
        if ( newKarma > karmaLimit ) newKarma = karmaLimit;
        
        // Fix to prevent overflow (=> karma has a  max value of 2 147 483 647)
        if (getKarma() > (Integer.MAX_VALUE - newKarma))
            newKarma = Integer.MAX_VALUE - getKarma();
        
        // Add karma to attacker and increase its PK counter
        setPkKills(getPkKills() + 1);
        setKarma(getKarma() + newKarma);
        
        // Send a Server->Client UserInfo packet to attacker with its Karma and PK Counter
        sendPacket(new UserInfo(this));
    }
	
	public int calculateKarmaLost(long exp)
	{
		// KARMA LOSS
		// When a PKer gets killed by another player or a L2MonsterInstance, it loses a certain amount of Karma based on their level.
		// this (with defaults) results in a level 1 losing about ~2 karma per death, and a lvl 70 loses about 11760 karma per death...
		// You lose karma as long as you were not in a pvp zone and you did not kill urself.
		// NOTE: exp for death (if delevel is allowed) is based on the players level
		
		long expGained = Math.abs(exp);
		expGained /= Config.KARMA_XP_DIVIDER;
		
		// FIXME Micht : Maybe this code should be fixed and karma set to a long value
		int karmaLost = 0;
		if (expGained > Integer.MAX_VALUE)
			karmaLost = Integer.MAX_VALUE;
		else
			karmaLost = (int)expGained;
		
		if (karmaLost < Config.KARMA_LOST_BASE) karmaLost = Config.KARMA_LOST_BASE;
		if (karmaLost > getKarma()) karmaLost = getKarma();
		
		return karmaLost;
	}
	
	public void updatePvPStatus()
	{
		if (getPvpFlag() == 0) startPvPFlag();
		if (getPvpFlag() != 0) setlastPvpAttack (System.currentTimeMillis()); //update last pvp ATTACK controller
	}
	
	public void updatePvPStatus(L2Character target)
	{
		if (
				checkIfPvP(target) &&
				(
						!ZoneManager.getInstance().checkIfInZonePvP(this) ||       // Player is not inside pvp zone or
						!ZoneManager.getInstance().checkIfInZonePvP(target)        // Target player is not inside pvp zone
				)/* &&               // Micht : ALREADY CHECKED IN checkIfPvP(target)
                (
                        target instanceof L2PcInstance &&                          // Target is a player and
                        !(((L2PcInstance)target).getKarma() > 0)                   // target doesn't have Karma (Attacking a PK doesn't flag you.)
                )*/
		) updatePvPStatus();
	}
	
	/**
	 * Restore the specified % of experience this L2PcInstance has
	 * lost and sends a Server->Client StatusUpdate packet.<BR><BR>
	 */
	public void restoreExp(double restorePercent)
	{ 
		if (_expBeforeDeath > 0)
		{   
			// Restore the specified % of lost experience.
			getStat().addExp((int)Math.round((_expBeforeDeath - getExp()) * restorePercent / 100));
			_expBeforeDeath = 0;
		}
	}
	
	/**
	 * Reduce the Experience (and level if necessary) of the L2PcInstance in function of the calculated Death Penalty.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Calculate the Experience loss </li>
	 * <li>Set the value of _expBeforeDeath </li>
	 * <li>Set the new Experience value of the L2PcInstance and Decrease its level if necessary </li>
	 * <li>Send a Server->Client StatusUpdate packet with its new Experience </li><BR><BR>
	 *
	 */
	public void deathPenalty(boolean atwar)
	{
		// TODO Need Correct Penalty
		// Get the level of the L2PcInstance
		final int lvl = getLevel();
		
		//The death steal you some Exp
		double percentLost = -0.07 * lvl + 6.5;
        
		if (getKarma() > 0) 
            percentLost *= Config.RATE_KARMA_EXP_LOST;
        
		if (isFestivalParticipant() || atwar || SiegeManager.getInstance().checkIfInZone(this)) 
            percentLost /= 4.0;
		
		// Calculate the Experience loss
		long lostExp = 0;
		if (!atEvent) 
			if (lvl < Experience.MAX_LEVEL)
				lostExp = Math.round((getStat().getExpForLevel(lvl+1) - getStat().getExpForLevel(lvl)) * percentLost /100);
			else
				lostExp = Math.round((getStat().getExpForLevel(Experience.MAX_LEVEL) - getStat().getExpForLevel(Experience.MAX_LEVEL - 1)) * percentLost /100);
		
		// Get the Experience before applying penalty
		_expBeforeDeath = getExp();
		
        if (Config.DEBUG)
            _log.fine(getName() + " died and lost " + lostExp + " experience.");
		
		// Set the new Experience value of the L2PcInstance
		getStat().addExp(-lostExp);
	}
	
	/**
	 * @param b
	 */
	public void setPartyMatchingAutomaticRegistration(boolean b)
	{
		_partyMatchingAutomaticRegistration = b;
	}
	
	/**
	 * @param b
	 */
	public void setPartyMatchingShowLevel(boolean b)
	{
		_partyMatchingShowLevel = b;
	}
	
	/**
	 * @param b
	 */
	public void setPartyMatchingShowClass(boolean b)
	{
		_partyMatchingShowClass = b;
	}
	
	/**
	 * @param memo
	 */
	public void setPartyMatchingMemo(String memo)
	{
		_partyMatchingMemo = memo;
	}
	
	public boolean isPartyMatchingAutomaticRegistration()
	{
		return _partyMatchingAutomaticRegistration;
	}
	
	public String getPartyMatchingMemo()
	{
		return _partyMatchingMemo;
	}
	
	public boolean isPartyMatchingShowClass()
	{
		return _partyMatchingShowClass;
	}
	
	public boolean isPartyMatchingShowLevel()
	{
		return _partyMatchingShowLevel;
	}
	
	/**
	 * Manage the increase level task of a L2PcInstance (Max MP, Max MP, Recommandation, Expertise and beginner skills...).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Send a Server->Client System Message to the L2PcInstance : YOU_INCREASED_YOUR_LEVEL </li>
	 * <li>Send a Server->Client packet StatusUpdate to the L2PcInstance with new LEVEL, MAX_HP and MAX_MP </li>
	 * <li>Set the current HP and MP of the L2PcInstance, Launch/Stop a HP/MP/CP Regeneration Task and send StatusUpdate packet to all other L2PcInstance to inform (exclusive broadcast)</li>
	 * <li>Recalculate the party level</li>
	 * <li>Recalculate the number of Recommandation that the L2PcInstance can give</li>
	 * <li>Give Expertise skill of this level and remove beginner Lucky skill</li><BR><BR>
	 *
	 */
	public void increaseLevel()
	{
		// Set the current HP and MP of the L2Character, Launch/Stop a HP/MP/CP Regeneration Task and send StatusUpdate packet to all other L2PcInstance to inform (exclusive broadcast)
		setCurrentHpMp(getMaxHp(),getMaxMp());
		setCurrentCp(getMaxCp());
	}
	
	/**
	 * Stop the HP/MP/CP Regeneration task.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set the RegenActive flag to False </li>
	 * <li>Stop the HP/MP/CP Regeneration task </li><BR><BR>
	 *
	 */
	public void stopAllTimers()
	{
		stopHpMpRegeneration();
		stopWarnUserTakeBreak();
		stopWaterTask();
		stopRentPet();
        stopJailTask(true);
	}
	
	/**
	 * Return the L2Summon of the L2PcInstance or null.<BR><BR>
	 */
	public L2Summon getPet()
	{
		return _summon;
	}
	
	/**
	 * Set the L2Summon of the L2PcInstance.<BR><BR>
	 */
	public void setPet(L2Summon summon)
	{
		_summon = summon;
	}
	
	/**
	 * Set the L2PcInstance requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...).<BR><BR>
	 */
	public synchronized void setActiveRequester(L2PcInstance requester)
	{
		_activeRequester = requester;
	}
	
	/**
	 * Return the L2PcInstance requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...).<BR><BR>
	 */
	public L2PcInstance getActiveRequester()
	{
		return _activeRequester;
	}
	
	/**
	 * Return True if a transaction is in progress.<BR><BR>
	 */
	public boolean isProcessingRequest()
	{
		return _activeRequester != null || _requestExpireTime > GameTimeController.getGameTicks();
	}
	
	/**
	 * Return True if a transaction is in progress.<BR><BR>
	 */
	public boolean isProcessingTransaction() { return _activeRequester != null || _activeTradeList != null || _requestExpireTime > GameTimeController.getGameTicks(); }
	
	/**
	 * Select the Warehouse to be used in next activity.<BR><BR>
	 */
	public void onTransactionRequest(L2PcInstance partner) 
	{
		_requestExpireTime = GameTimeController.getGameTicks() + REQUEST_TIMEOUT * GameTimeController.TICKS_PER_SECOND;
		partner.setActiveRequester(this);
	}
	
	/**
	 * Select the Warehouse to be used in next activity.<BR><BR>
	 */
	public void onTransactionResponse() 
    { 
        _requestExpireTime = 0; 
    }
	
	/**
	 * Select the Warehouse to be used in next activity.<BR><BR>
	 */
	public void setActiveWarehouse(ItemContainer warehouse)
    { 
        _activeWarehouse = warehouse; 
    }
	
	/**
	 * Return active Warehouse.<BR><BR>
	 */
	public ItemContainer getActiveWarehouse()
    { 
        return _activeWarehouse; 
    }
	
	/**
	 * Select the TradeList to be used in next activity.<BR><BR>
	 */
	public void setActiveTradeList(TradeList tradeList)
    { 
        _activeTradeList = tradeList; 
    }
	
	/**
	 * Return active TradeList.<BR><BR>
	 */
	public TradeList getActiveTradeList()
    { 
        return _activeTradeList; 
    }
	
	public void onTradeStart(L2PcInstance partner)
	{
		_activeTradeList = new TradeList(this);
		_activeTradeList.setPartner(partner);
		
		SystemMessage msg = new SystemMessage(SystemMessage.BEGIN_TRADE_WITH_S1);
		msg.addString(partner.getName());
		sendPacket(msg);
		sendPacket(new TradeStart(this));
	}
	
	public void onTradeConfirm(L2PcInstance partner)
	{
		SystemMessage msg = new SystemMessage(SystemMessage.S1_CONFIRMED_TRADE);
		msg.addString(partner.getName());
		sendPacket(msg);
	}
	
	public void onTradeCancel(L2PcInstance partner)
	{
		if (_activeTradeList == null) 
            return;
        
		_activeTradeList.Lock();
		_activeTradeList = null;
        
		sendPacket(new SendTradeDone(0));
		SystemMessage msg = new SystemMessage(SystemMessage.S1_CANCELED_TRADE);
		msg.addString(partner.getName());
		sendPacket(msg);
	}
	
	public void onTradeFinish(boolean successfull)
	{
		_activeTradeList = null;
		sendPacket(new SendTradeDone(1));
        if (successfull)
            sendPacket(new SystemMessage(SystemMessage.TRADE_SUCCESSFUL));
	}
	
	public void startTrade(L2PcInstance partner)
	{
		this.onTradeStart(partner);
		partner.onTradeStart(this);
	}
	
	public void cancelActiveTrade()
	{
		if (_activeTradeList == null) 
            return;
		
		L2PcInstance partner = _activeTradeList.getPartner();
		if (partner != null) partner.onTradeCancel(this);
		onTradeCancel(this);
	}
	
	/**
	 * Return the _createList object of the L2PcInstance.<BR><BR>
	 */
	public L2ManufactureList getCreateList()
	{
		return _createList;
	}
	
	/**
	 * Set the _createList object of the L2PcInstance.<BR><BR>
	 */
	public void setCreateList(L2ManufactureList x)
	{
		_createList = x;
	}
	
	/**
	 * Return the _buyList object of the L2PcInstance.<BR><BR>
	 */
	public TradeList getSellList()
	{
		if (_sellList == null) _sellList = new TradeList(this);
		return _sellList;
	}
	
	/**
	 * Return the _buyList object of the L2PcInstance.<BR><BR>
	 */
	public TradeList getBuyList()
	{
		if (_buyList == null) _buyList = new TradeList(this);
		return _buyList;
	}
	
	/**
	 * Set the Private Store type of the L2PcInstance.<BR><BR>
	 *
	 * <B><U> Values </U> :</B><BR><BR>
	 * <li>0 : STORE_PRIVATE_NONE</li>
	 * <li>1 : STORE_PRIVATE_SELL</li>
	 * <li>2 : sellmanage</li><BR>
	 * <li>3 : STORE_PRIVATE_BUY</li><BR>
	 * <li>4 : buymanage</li><BR>
	 * <li>5 : STORE_PRIVATE_MANUFACTURE</li><BR>
	 *
	 */
	public void setPrivateStoreType(int type)
	{
		_privatestore = type;
	}
	
	/**
	 * Return the Private Store type of the L2PcInstance.<BR><BR>
	 *
	 * <B><U> Values </U> :</B><BR><BR>
	 * <li>0 : STORE_PRIVATE_NONE</li>
	 * <li>1 : STORE_PRIVATE_SELL</li>
	 * <li>2 : sellmanage</li><BR>
	 * <li>3 : STORE_PRIVATE_BUY</li><BR>
	 * <li>4 : buymanage</li><BR>
	 * <li>5 : STORE_PRIVATE_MANUFACTURE</li><BR>
	 *
	 */
	public int getPrivateStoreType()
	{
		return _privatestore;
	}
	
	/**
	 * Set the _skillLearningClassId object of the L2PcInstance.<BR><BR>
	 */
	public void setSkillLearningClassId(ClassId classId)
	{
		_skillLearningClassId = classId;
	}
	
	/**
	 * Return the _skillLearningClassId object of the L2PcInstance.<BR><BR>
	 */
	public ClassId getSkillLearningClassId()
	{
		return _skillLearningClassId;
	}
	
	/**
	 * Set the _clan object, _clanId, _clanLeader Flag and title of the L2PcInstance.<BR><BR>
	 */
	public void setClan(L2Clan clan)
	{
		_clan = clan;
		
		if (clan == null)
		{
			_clanId = 0;
			_clanLeader = false;
			_clanPrivileges = 0;
			return;
		}
		
		if (!clan.isMember(getName()))
		{
			// char has been kicked from clan
			setClan(null);
			setTitle("");
			return;
		}
		
		_clanId = clan.getClanId();
		_clanLeader = getObjectId() == clan.getLeaderId();
		setTitle("");
	}
	
	/**
	 * Return the _clan object of the L2PcInstance.<BR><BR>
	 */
	public L2Clan getClan()
	{
		return _clan;
	}
	
	/**
	 * Return True if the L2PcInstance is the leader of its clan.<BR><BR>
	 */
	public boolean isClanLeader()
	{
		return _clanLeader;
	}
	
	/**
	 * Reduce the number of arrows owned by the L2PcInstance and send it Server->Client Packet InventoryUpdate or ItemList (to unequip if the last arrow was consummed).<BR><BR>
	 */
	protected void reduceArrowCount()
	{
		L2ItemInstance arrows = getInventory().destroyItem("Consume", getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND), 1, this, null);
		
		if (Config.DEBUG) _log.fine("arrow count:" + (arrows==null? 0 : arrows.getCount()));
		
		if (arrows == null || arrows.getCount() == 0)
		{
			getInventory().unEquipItemInSlot(Inventory.PAPERDOLL_LHAND);
			_arrowItem = null;
			
			if (Config.DEBUG) _log.fine("removed arrows count");
			sendPacket(new ItemList(this,false));
		}
		else
		{
			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				InventoryUpdate iu = new InventoryUpdate();
				iu.addModifiedItem(arrows);
				sendPacket(iu);
			}
			else sendPacket(new ItemList(this, false));
		}
	}
	
	/**
	 * Equip arrows needed in left hand and send a Server->Client packet ItemList to the L2PcINstance then return True.<BR><BR>
	 */
	protected boolean checkAndEquipArrows()
	{
		// Check if nothing is equiped in left hand
		if (getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND) == null)
		{
			// Get the L2ItemInstance of the arrows needed for this bow
			_arrowItem = getInventory().findArrowForBow(getActiveWeaponItem());
			
			if (_arrowItem != null)
			{
				// Equip arrows needed in left hand
				getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, _arrowItem);
				
				// Send a Server->Client packet ItemList to this L2PcINstance to update left hand equipement
				ItemList il = new ItemList(this, false);
				sendPacket(il);
			}
		}
		else
		{
			// Get the L2ItemInstance of arrows equiped in left hand
			_arrowItem = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		}
		
		return _arrowItem != null;
	}
	
	
	/**
	 * Return True if the L2PcInstance use a dual weapon.<BR><BR>
	 */
	public boolean isUsingDualWeapon()
	{
		L2Weapon weaponItem = getActiveWeaponItem();
		if (weaponItem == null) return false;
        
		if (weaponItem.getItemType() == L2WeaponType.DUAL)
			return true;
		else if (weaponItem.getItemType() == L2WeaponType.DUALFIST)
			return true;
		else if (weaponItem.getItemId() == 248) // orc fighter fists
			return true;
		else if (weaponItem.getItemId() == 252) // orc mage fists
			return true;
		else
			return false;
	}
	
	public void setUptime(long time)
	{
		_uptime = time;
	}
	
	public long getUptime()
	{
		return System.currentTimeMillis()-_uptime;
	}
	
	/**
	 * Set the invulnerability Flag of the L2PcInstance.<BR><BR>
	 */
	public void setIsInvul(boolean b)
	{
		_isInvul = b;
	}
	
	/**
	 * Return True if the L2PcInstance is invulnerable.<BR><BR>
	 */
	public boolean isInvul()
	{
		return _isInvul || isTeleporting() || _protectEndTime > GameTimeController.getGameTicks();
	}
	
	/**
	 * Return True if the L2PcInstance has a Party in progress.<BR><BR>
	 */
	public boolean isInParty()
	{
		return _party != null;
	}
	
	/**
	 * Set the _party object of the L2PcInstance (without joining it).<BR><BR>
	 */
	public void setParty(L2Party party)
	{
		_party = party;
	}
	
	/**
	 * Set the _party object of the L2PcInstance AND join it.<BR><BR>
	 */
	public void joinParty(L2Party party)
	{
		if (party != null)
		{
            // First set the party otherwise this wouldn't be considered
            // as in a party into the L2Character.updateEffectIcons() call.
            _party = party;
			party.addPartyMember(this);
		}
	}
	
	
	/**
	 * Manage the Leave Party task of the L2PcInstance.<BR><BR>
	 */
	public void leaveParty()
	{
		if (isInParty())
		{
			_party.removePartyMember(this);
			_party = null;
		}
	}
	
	/**
	 * Return the _party object of the L2PcInstance.<BR><BR>
	 */
	public L2Party getParty()
	{
		return _party;
	}
	
	/**
	 * Set the _isGm Flag of the L2PcInstance.<BR><BR>
	 */
	public void setIsGM(boolean status)
	{
		_isGm = status;
	}
	
	/**
	 * Return True if the L2PcInstance is a GM.<BR><BR>
	 */
	public boolean isGM()
	{
		return _isGm;
	}
	
	/**
	 * Manage a cancel cast task for the L2PcInstance.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set the Intention of the AI to AI_INTENTION_IDLE </li>
	 * <li>Enable all skills (set _allSkillsDisabled to False) </li>
	 * <li>Send a Server->Client Packet MagicSkillCanceld to the L2PcInstance and all L2PcInstance in the _KnownPlayers of the L2Character (broadcast) </li><BR><BR>
	 *
	 */
	public void cancelCastMagic()
	{
		// Set the Intention of the AI to AI_INTENTION_IDLE
		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		
		// Enable all skills (set _allSkillsDisabled to False)
		enableAllSkills();
		
		// Send a Server->Client Packet MagicSkillCanceld to the L2PcInstance and all L2PcInstance in the _KnownPlayers of the L2Character (broadcast)
		MagicSkillCanceld msc = new MagicSkillCanceld(getObjectId());
		
        // Broadcast the packet to self and known players.
        Broadcast.toSelfAndKnownPlayersInRadius(this, msc, 810000/*900*/);
	}
	
	/**
	 * Set the _accessLevel of the L2PcInstance.<BR><BR>
	 */
	public void setAccessLevel(int level)
	{
		_accessLevel = level;
        
		if (_accessLevel > 0 || Config.EVERYBODY_HAS_ADMIN_RIGHTS)
			setIsGM(true);
	}
	
	public void setAccountAccesslevel(int level)
	{
		if (_connection != null)
			LoginServerThread.getInstance().sendAccessLevel(getAccountName(),level);
		else
			_log.info("Couldnt set the player's account access level");
	}
	
	/**
	 * Return the _accessLevel of the L2PcInstance.<BR><BR>
	 */
	public int getAccessLevel()
	{
		if (Config.EVERYBODY_HAS_ADMIN_RIGHTS && _accessLevel <= 200)
			return 200;
        
		return _accessLevel;
	}
	
	public double getLevelMod()
	{
		return (100.0 - 11 + getLevel()) / 100.0;
	}
	
	/**
	 * Update Stats of the L2PcInstance client side by sending Server->Client packet UserInfo/StatusUpdate to this L2PcInstance and CharInfo/StatusUpdate to all L2PcInstance in its _KnownPlayers (broadcast).<BR><BR>
	 */
	public void updateStats()
	{
		super.updateStats();
		refreshOverloaded();
		refreshExpertisePenalty();
		// Send a Server->Client packet UserInfo to this L2PcInstance and CharInfo to all L2PcInstance in its _KnownPlayers (broadcast)
		broadcastUserInfo();
		
		// Send a Server->Client StatusUpdate packet with Karma to the L2PcInstance and to all L2PcInstance in its _KnownPlayers (broadcast)
		updateKarma();
	}
	
	/**
	 * Send a Server->Client StatusUpdate packet with Karma and PvP Flag to the L2PcInstance and all L2PcInstance to inform (broadcast).<BR><BR>
	 */
	public void setKarmaFlag(int flag)
	{
		// Send a Server->Client StatusUpdate packet with Karma and PvP Flag to the L2PcInstance and all L2PcInstance to inform (broadcast)
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.KARMA, getKarma());
		su.addAttribute(StatusUpdate.PVP_FLAG, flag);
		sendPacket(su);
		broadcastPacket(su);
	}
	
	/**
	 * Send a Server->Client StatusUpdate packet with Karma to the L2PcInstance and all L2PcInstance to inform (broadcast).<BR><BR>
	 */
	public void updateKarma()
	{
		// Send a Server->Client StatusUpdate packet with Karma to the L2PcInstance
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.KARMA, getKarma());
		sendPacket(su);
		broadcastPacket(su);
	}
	
	/**
	 * Set the online Flag to True or False and update the characters table of the database with online status and lastAccess (called when login and logout).<BR><BR>
	 */
	public void setOnlineStatus(boolean isOnline)
	{
		if (_isOnline != isOnline)
			_isOnline = isOnline;            
		
		// Update the characters table of the database with online status and lastAccess (called when login and logout)
		updateOnlineStatus();
	}
    
    public void setIsIn7sDungeon(boolean isIn7sDungeon)
    {
        if (_isIn7sDungeon != isIn7sDungeon)
            _isIn7sDungeon = isIn7sDungeon;
        
        updateIsIn7sDungeonStatus();
    }
	
	/**
	 * Update the characters table of the database with online status and lastAccess of this L2PcInstance (called when login and logout).<BR><BR>
	 */
	public void updateOnlineStatus()
	{
		java.sql.Connection con = null;
		
		try 
        {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET online=?, lastAccess=? WHERE obj_id=?");
			statement.setInt(1, isOnline());
			statement.setLong(2, System.currentTimeMillis());
			statement.setInt(3, getObjectId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("could not set char online status:"+e);
		}
		finally
		{
			try { con.close(); } catch (Exception e) {}
		}
	}
	
    public void updateIsIn7sDungeonStatus()
    {
        java.sql.Connection con = null;
        
        try 
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("UPDATE characters SET isIn7sDungeon=?, lastAccess=? WHERE obj_id=?");
            statement.setInt(1, isIn7sDungeon() ? 1 : 0);
            statement.setLong(2, System.currentTimeMillis());
            statement.setInt(3, getObjectId());
            statement.execute();
            statement.close();
        }
        catch (Exception e)
        {
            _log.warning("could not set char isIn7sDungeon status:"+e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
	/**
	 * Create a new player in the characters table of the database.<BR><BR>
	 */
	private boolean createDb()
	{
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement(
			                                 "INSERT INTO characters " +
			                                 "(account_name,obj_Id,char_name,level,maxHp,curHp,maxCp,curCp,maxMp,curMp," +
			                                 "acc,crit,evasion,mAtk,mDef,mSpd,pAtk,pDef,pSpd,runSpd,walkSpd," +
			                                 "str,con,dex,_int,men,wit,face,hairStyle,hairColor,sex," +
			                                 "movement_multiplier,attack_speed_multiplier,colRad,colHeight," +
			                                 "exp,sp,karma,pvpkills,pkkills,clanid,maxload,race,classid,deletetime," +
			                                 "cancraft,title,allyId,accesslevel,online,isin7sdungeon,clan_privs,wantspeace,deleteclan," +
			                                 "base_class,newbie,nobless) " +
			"values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setString(1, _accountName);
			statement.setInt(2, getObjectId());
			statement.setString(3, getName());
			statement.setInt(4, getLevel());
			statement.setInt(5, getMaxHp());
			statement.setDouble(6, getCurrentHp());
			statement.setInt(7, getMaxCp());
			statement.setDouble(8, getCurrentCp());
			statement.setInt(9, getMaxMp());
			statement.setDouble(10, getCurrentMp());
			statement.setInt(11, getAccuracy());
			statement.setInt(12, getCriticalHit(null, null));
			statement.setInt(13, getEvasionRate(null));
			statement.setInt(14, getMAtk(null, null));
			statement.setInt(15, getMDef(null, null));
			statement.setInt(16, getMAtkSpd());
			statement.setInt(17, getPAtk(null));
			statement.setInt(18, getPDef(null));
			statement.setInt(19, getPAtkSpd());
			statement.setInt(20, getRunSpeed());
			statement.setInt(21, getWalkSpeed());
			statement.setInt(22, getSTR());
			statement.setInt(23, getCON());
			statement.setInt(24, getDEX());
			statement.setInt(25, getINT());
			statement.setInt(26, getMEN());
			statement.setInt(27, getWIT());
			statement.setInt(28, getFace());
			statement.setInt(29, getHairStyle());
			statement.setInt(30, getHairColor());
			statement.setInt(31, getSex());
			statement.setDouble(32, 1/*getMovementMultiplier()*/);
			statement.setDouble(33, 1/*getAttackSpeedMultiplier()*/);
			statement.setDouble(34, getTemplate().collisionRadius/*getCollisionRadius()*/);
			statement.setDouble(35, getTemplate().collisionHeight/*getCollisionHeight()*/);
			statement.setLong(36, getExp());
			statement.setInt(37, getSp());
			statement.setInt(38, getKarma());
			statement.setInt(39, getPvpKills());
			statement.setInt(40, getPkKills());
			statement.setInt(41, getClanId());
			statement.setInt(42, getMaxLoad());
			statement.setInt(43, getRace().ordinal());
			statement.setInt(44, getClassId().getId());
			statement.setLong(45, getDeleteTimer());
			statement.setInt(46, hasDwarvenCraft() ? 1 : 0);
			statement.setString(47, getTitle());
			statement.setInt(48, getAllyId());
			statement.setInt(49, getAccessLevel());
			statement.setInt(50, isOnline());
            statement.setInt(51, isIn7sDungeon() ? 1 : 0);
			statement.setInt(52, getClanPrivileges());
			statement.setInt(53, getWantsPeace());
			statement.setLong(54, getDeleteClanTime());
			statement.setInt(55, getBaseClass());
			statement.setInt(56, isNewbie() ? 1 : 0);
			statement.setInt(57, isNoble() ? 1 :0);
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("Could not insert char data: " + e);
			return false;
		}
		finally
		{
			try { con.close(); } catch (Exception e) {}
		}
		return true;
	}
	
	/**
	 * Retrieve a L2PcInstance from the characters table of the database and add it in _allObjects of the L2world.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Retrieve the L2PcInstance from the characters table of the database </li>
	 * <li>Add the L2PcInstance object in _allObjects </li>
	 * <li>Set the x,y,z position of the L2PcInstance and make it invisible</li>
	 * <li>Update the overloaded status of the L2PcInstance</li><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 *
	 * @return The L2PcInstance loaded from the database
	 *
	 */
	private static L2PcInstance restore(int objectId)
	{
		L2PcInstance player = null;
		java.sql.Connection con = null;
		
		try
		{
			// Retrieve the L2PcInstance from the characters table of the database
			con = L2DatabaseFactory.getInstance().getConnection();
			
			PreparedStatement statement = con.prepareStatement(RESTORE_CHARACTER);
			statement.setInt(1, objectId);
			ResultSet rset = statement.executeQuery();
			
			double currentCp = 0;
			double currentHp = 0;
			double currentMp = 0;
            
			while (rset.next())
			{
				final int activeClassId = rset.getInt("classid");
				final boolean female = rset.getInt("sex")!=0;
				final L2PcTemplate template = CharTemplateTable.getInstance().getTemplate(activeClassId, female);
				
				player = new L2PcInstance(objectId, template, rset.getString("account_name"));
				player.setName(rset.getString("char_name"));
				player._lastAccess = rset.getLong("lastAccess");
				
				player._activeClass = activeClassId;
				try { player.setBaseClass(rset.getInt("base_class")); }
				catch (Exception e) { player.setBaseClass(activeClassId); }
				
				player._classIndex = 0;
				player.getStat().setExp(rset.getLong("exp"));
				player.getStat().setLevel(rset.getInt("level"));
				player.getStat().setSp(rset.getInt("sp"));
				
				player.setFace(rset.getInt("face"));
				player.setHairStyle(rset.getInt("hairStyle"));
				player.setHairColor(rset.getInt("hairColor"));
				player.setClanPrivileges(rset.getInt("clan_privs"));
				player.setWantsPeace(rset.getInt("wantspeace"));
				
				player.setHeading(rset.getInt("heading"));
				
				player.setKarma(rset.getInt("karma"));
				player.setPvpKills(rset.getInt("pvpkills"));
				player.setPkKills(rset.getInt("pkkills"));
				player.setDeleteClanTime(rset.getLong("deleteclan"));
				player.setOnlineTime(rset.getLong("onlinetime"));
				player.setNewbie(rset.getInt("newbie")==1);
				player.setNoble(rset.getInt("nobless")==1);
				
				if (player.getDeleteClanTime() > 0 && player.canCreateClan())
					player.setDeleteClanTime(0);
				
				int clanId	= rset.getInt("clanid");
				
				if (clanId > 0)
					player.setClan(ClanTable.getInstance().getClan(clanId));
				
				player.setDeleteTimer(rset.getLong("deletetime"));
				
				player.setTitle(rset.getString("title"));
				player.setAccessLevel(rset.getInt("accesslevel"));
				player.setFistsWeaponItem(player.findFistsWeaponItem(activeClassId));
				player.setUptime(System.currentTimeMillis());
				
				currentHp = rset.getDouble("curHp");
				player.setCurrentHp(rset.getDouble("curHp"));
				currentCp = rset.getDouble("curCp");
				player.setCurrentCp(rset.getDouble("curCp"));
				currentMp = rset.getDouble("curMp");
				player.setCurrentMp(rset.getDouble("curMp"));
				
				//Check recs
				player.checkRecom(rset.getInt("rec_have"),rset.getInt("rec_left"));
				
                // Restore Subclass Data
                if (restoreSubClassData(player))
                {
                	if (player.getActiveClass() != player.getBaseClass())
                	{
                    	for (SubClass subClass : player.getSubClasses().values())
                    		if (subClass.getClassId() == player.getActiveClass())
                    			player._classIndex = subClass.getClassIndex();
                	}
                }				 
                
                player.setIsIn7sDungeon((rset.getInt("isin7sdungeon")==1)? true : false);
                player.setInJail((rset.getInt("in_jail")==1)? true : false);
                if (player.isInJail())
                	player.setJailTimer(rset.getLong("jail_timer"));
                else
                	player.setJailTimer(0);
                
                CursedWeaponsManager.getInstance().checkPlayer(player);
                
				// Add the L2PcInstance object in _allObjects
				//L2World.getInstance().storeObject(player);
				
				// Set the x,y,z position of the L2PcInstance and make it invisible
				player.setXYZInvisible(rset.getInt("x"), rset.getInt("y"), rset.getInt("z"));
				
				// Retrieve the name and ID of the other characters assigned to this account.
				PreparedStatement stmt = con.prepareStatement("SELECT obj_Id, char_name FROM characters WHERE account_name=? AND obj_Id<>?");
				stmt.setString(1, player._accountName);
				stmt.setInt(2, objectId);
				ResultSet chars = stmt.executeQuery();
				
				while (chars.next())
				{
					Integer charId = chars.getInt("obj_Id");
					String charName = chars.getString("char_name");
					player._chars.put(charId, charName);
				}
				
				break;
			}
			
			rset.close();
			statement.close();
			
			// Retrieve from the database all secondary data of this L2PcInstance 
			// and reward expertise/lucky skills if necessary.
			player.restoreCharData();
			player.rewardSkills();
			
			// Restore current Cp, HP and MP values
			player.setCurrentCp(currentCp);
			player.setCurrentHp(currentHp);
			player.setCurrentMp(currentMp);
			
			// Restore pet if exists in the world
			player.setPet(L2World.getInstance().getPet(player.getObjectId()));
			
			// Update the overloaded status of the L2PcInstance
			player.refreshOverloaded();
		}
		catch (Exception e)
		{
			_log.warning("Could not restore char data: " + e);
		}
		finally
		{
			try { con.close(); } catch (Exception e) {}
		}
        
		return player;
	}
	
	/**
	 * @return
	 */
	public Forum getMail()
	{		
		if (_forumMail == null)
		{
			setMail(ForumsBBSManager.getInstance().getForumByName("MailRoot").GetChildByName(getName()));
            
			if (_forumMail == null)
			{
				ForumsBBSManager.getInstance().CreateNewForum(getName(),ForumsBBSManager.getInstance().getForumByName("MailRoot"),Forum.MAIL,Forum.OWNERONLY,getObjectId());
				setMail(ForumsBBSManager.getInstance().getForumByName("MailRoot").GetChildByName(getName()));
			}
		}
        
		return _forumMail;
	}

	/**
	 * @param forum
	 */
	public void setMail(Forum forum)
	{
		_forumMail = forum;		
	}

	/**
	 * @return
	 */
	public Forum getMemo()
	{		
		if (_forumMemo == null)
		{
			setMemo(ForumsBBSManager.getInstance().getForumByName("MemoRoot").GetChildByName(_accountName));
            
        	if (_forumMemo == null)
        	{            		
        		ForumsBBSManager.getInstance().CreateNewForum(_accountName,ForumsBBSManager.getInstance().getForumByName("MemoRoot"),Forum.MEMO,Forum.OWNERONLY,getObjectId());
        		setMemo(ForumsBBSManager.getInstance().getForumByName("MemoRoot").GetChildByName(_accountName));
        	}
		}
        
		return _forumMemo;
	}

	/**
	 * @param forum
	 */
	public void setMemo(Forum forum)
	{
		_forumMemo = forum;			
	}

    /**
     * Restores sub-class data for the L2PcInstance, used to check the current
     * class index for the character.
     */
    private static boolean restoreSubClassData(L2PcInstance player)
    {
        java.sql.Connection con = null;
        
        try 
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(RESTORE_CHAR_SUBCLASSES);
            statement.setInt(1, player.getObjectId());
            
            ResultSet rset = statement.executeQuery();
            
            while (rset.next())
            {
                SubClass subClass = new SubClass();
                subClass.setClassId(rset.getInt("class_id"));
                subClass.setLevel(rset.getInt("level"));
                subClass.setExp(rset.getLong("exp"));
                subClass.setSp(rset.getInt("sp"));
                subClass.setClassIndex(rset.getInt("class_index"));
                
                // Enforce the correct indexing of _subClasses against their class indexes.
                player.getSubClasses().put(subClass.getClassIndex(), subClass);
            }
            
            statement.close();
        }
        catch (Exception e) {
            _log.warning("Could not restore classes for " + player.getName() + ": " + e);
            e.printStackTrace();
        }
        finally {
            try { con.close(); } catch (Exception e) {}
        }
        
        return true;
    }
	
	/**
	 * Restores secondary data for the L2PcInstance, based on the current class index.
	 */
	private void restoreCharData()
	{
		// Retrieve from the database all skills of this L2PcInstance and add them to _skills.
		restoreSkills();
		
		// Retrieve from the database all macroses of this L2PcInstance and add them to _macroses.
		_macroses.restore();
		
		// Retrieve from the database all shortCuts of this L2PcInstance and add them to _shortCuts.
		_shortCuts.restore();
		
		// Retrieve from the database all henna of this L2PcInstance and add them to _henna.
		restoreHenna();
		
		// Retrieve from the database the recipe book of this L2PcInstance.
		if (!isSubClassActive())
			restoreRecipeBook();
	}
	
	/**
	 * Store recipe book data for this L2PcInstance, if not on an active sub-class.
	 */
	private void storeRecipeBook()
	{
		// If the player is on a sub-class don't even attempt to store a recipe book.
		if (isSubClassActive())
			return;
		if (getCommonRecipeBook().length == 0 && getDwarvenRecipeBook().length == 0)
			return;
		
		java.sql.Connection con = null;
		
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM character_recipebook WHERE char_id=?");
			statement.setInt(1, getObjectId());
			statement.execute();
			statement.close();
			
			L2RecipeList[] recipes = getCommonRecipeBook();
			
			for (int count = 0; count < recipes.length; count++)
			{
				statement = con.prepareStatement("INSERT INTO character_recipebook (char_id, id, type) values(?,?,0)");
				statement.setInt(1, getObjectId());
				statement.setInt(2, recipes[count].getId());
				statement.execute();
				statement.close();
			}
			
			recipes = getDwarvenRecipeBook(); 
			for (int count = 0; count < recipes.length; count++) 
			{ 
				statement = con.prepareStatement("INSERT INTO character_recipebook (char_id, id, type) values(?,?,1)");
				statement.setInt(1, getObjectId()); 
				statement.setInt(2, recipes[count].getId()); 
				statement.execute(); 
				statement.close(); 
			} 
		}
		catch (Exception e) {
			_log.warning("Could not store recipe book data: " + e);
		} 
		finally {
			try { con.close(); } catch (Exception e) {}
		}
	}
	
	/**
	 * Restore recipe book data for this L2PcInstance.
	 */
	private void restoreRecipeBook()
	{
		java.sql.Connection con = null;
		
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT id, type FROM character_recipebook WHERE char_id=?");
			statement.setInt(1, getObjectId());
			ResultSet rset = statement.executeQuery();
			
			L2RecipeList recipe;
			while (rset.next()) {
				recipe = RecipeController.getInstance().getRecipeList(rset.getInt("id") - 1);
				
				if (rset.getInt("type") == 1) 
					registerDwarvenRecipeList(recipe); 
				else 
					registerCommonRecipeList(recipe); 
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e) {
			_log.warning("Could not restore recipe book data:" + e);
		} 
		finally {
			try { con.close(); } catch (Exception e) {}
		}
	}
	
	/**
	 * Update L2PcInstance stats in the characters table of the database.<BR><BR>
	 */
	public void store()
	{
		//update client coords, if these look like true
        if (isInsideRadius(getClientX(), getClientY(), 1000, true))
            setXYZ(getClientX(), getClientY(), getClientZ());
		
		storeCharBase();
		storeCharSub();
		storeEffect();
		storeRecipeBook();
	}
	
	private void storeCharBase()
	{
		java.sql.Connection con = null;
		
		try
		{
			// Get the exp, level, and sp of base class to store in base table
			int currentClassIndex = getClassIndex();
			_classIndex = 0;
			long exp     = getStat().getExp();
			int level   = getStat().getLevel();
			int sp      = getStat().getSp();
			_classIndex = currentClassIndex;
			
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			
			// Update base class
            statement = con.prepareStatement(UPDATE_CHARACTER);
			statement.setInt(1, level);
			statement.setInt(2, getMaxHp());
			statement.setDouble(3, getCurrentHp());
			statement.setInt(4, getMaxCp());
			statement.setDouble(5, getCurrentCp());
			statement.setInt(6, getMaxMp());
			statement.setDouble(7, getCurrentMp());
			statement.setInt(8, getSTR());
			statement.setInt(9, getCON());
			statement.setInt(10, getDEX());
			statement.setInt(11, getINT());
			statement.setInt(12, getMEN());
			statement.setInt(13, getWIT());
			statement.setInt(14, getFace());
			statement.setInt(15, getHairStyle());
			statement.setInt(16, getHairColor());
			statement.setInt(17, getHeading());
			statement.setInt(18, _observerMode ? _obsX : getX());
			statement.setInt(19, _observerMode ? _obsY : getY());
			statement.setInt(20, _observerMode ? _obsZ : getZ());
			statement.setLong(21, exp);
			statement.setInt(22, sp);
			statement.setInt(23, getKarma());
			statement.setInt(24, getPvpKills());
			statement.setInt(25, getPkKills());
			statement.setInt(26, getRecomHave());
			statement.setInt(27, getRecomLeft());
			statement.setInt(28, getClanId());
			statement.setInt(29, getMaxLoad());
			statement.setInt(30, getRace().ordinal());
			
//			if (!isSubClassActive())
			
//			else 
//			statement.setInt(30, getBaseTemplate().race.ordinal());
			
			statement.setInt(31, getClassId().getId());
			statement.setLong(32, getDeleteTimer());
			statement.setString(33, getTitle());
			statement.setInt(34, getAllyId());
			statement.setInt(35, getAccessLevel());
			statement.setInt(36, isOnline());
            statement.setInt(37, isIn7sDungeon() ? 1 : 0);
			statement.setInt(38, getClanPrivileges());
			statement.setInt(39, getWantsPeace());
			statement.setLong(40, getDeleteClanTime());
			statement.setInt(41, getBaseClass());
            
			long totalOnlineTime = _onlineTime;
            
			if (_onlineBeginTime > 0)
				totalOnlineTime += (System.currentTimeMillis()-_onlineBeginTime)/1000;
            
            statement.setLong(42, totalOnlineTime);
            statement.setInt(43, isInJail() ? 1 : 0);
            statement.setLong(44, getJailTimer());
            statement.setInt(45, isNewbie() ? 1 : 0);
            statement.setInt(46, isNoble() ? 1 : 0);
            statement.setInt(47, getObjectId());
			
			statement.execute();
			statement.close();
		}
		catch (Exception e) { _log.warning("Could not store char base data: "+ e); }
		finally { try { con.close(); } catch (Exception e) {} }
	}
	
    private void storeCharSub()
    {
        java.sql.Connection con = null;
        
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;

            if (getTotalSubClasses() > 0)
            {
            	for (SubClass subClass : getSubClasses().values())
            	{
                	statement = con.prepareStatement(UPDATE_CHAR_SUBCLASS);
                    statement.setLong(1, subClass.getExp());
                    statement.setInt(2, subClass.getSp());
                    statement.setInt(3, subClass.getLevel());
                    statement.setInt(4, subClass.getClassId());
                    statement.setInt(5, getObjectId());
                    statement.setInt(6, subClass.getClassIndex());
                    
                    statement.execute();
                    statement.close();
                }
            }
        }
        catch (Exception e) {
        	_log.warning("Could not store sub class data for " + getName() + ": "+ e); 
		}
        finally { try { con.close(); } catch (Exception e) {} }
    }
	
	private void storeEffect()
	{
		if (!Config.STORE_SKILL_COOLTIME) return;
		
		java.sql.Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			
			// Delete all current stored effects for char to avoid dupe
			statement = con.prepareStatement(DELETE_SKILL_SAVE);
			statement.setInt(1, getObjectId());
			statement.setInt(2, getClassIndex());
			statement.execute();
			statement.close();
			
			// Loop thru all effects for char and same them each to database
			for (L2Effect effect : getAllEffects())
			{
				if (effect != null && effect.getInUse() && !effect.getSkill().isToggle())
				{
					statement = con.prepareStatement(ADD_SKILL_SAVE);
					statement.setInt(1, getObjectId());
					statement.setInt(2, effect.getSkill().getId());
					statement.setInt(3, effect.getSkill().getLevel());
					statement.setInt(4, effect.getCount());
					statement.setInt(5, effect.getTime());
					statement.setInt(6, getClassIndex());
					statement.execute();
					statement.close();
				}
			}
		}
		catch (Exception e) { _log.warning("Could not store char effect data: "+ e); }
		finally { try { con.close(); } catch (Exception e) {} }
	}
	
	/**
	 * Return True if the L2PcInstance is on line.<BR><BR>
	 */
	public int isOnline()
	{
		return (_isOnline ? 1 : 0);
	}
	
    public boolean isIn7sDungeon()
    {
        return _isIn7sDungeon;
    }
    
	/**
	 * Add a skill to the L2PcInstance _skills and its Func objects to the calculator set of the L2PcInstance and save update in the character_skills table of the database.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills own by a L2PcInstance are identified in <B>_skills</B><BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Replace oldSkill by newSkill or Add the newSkill </li>
	 * <li>If an old skill has been replaced, remove all its Func objects of L2Character calculator set</li>
	 * <li>Add Func objects of newSkill to the calculator set of the L2Character </li><BR><BR>
	 *
	 * @param newSkill The L2Skill to add to the L2Character
	 *
	 * @return The L2Skill replaced or null if just added a new L2Skill
	 *
	 */
	public L2Skill addSkill(L2Skill newSkill)
	{
		// Add a skill to the L2PcInstance _skills and its Func objects to the calculator set of the L2PcInstance
		L2Skill oldSkill = super.addSkill(newSkill);
		
		// Add or update a L2PcInstance skill in the character_skills table of the database
		storeSkill(newSkill, oldSkill, -1);
		
		return oldSkill;
	}
	
	/**
	 * Remove a skill from the L2Character and its Func objects from calculator set of the L2Character and save update in the character_skills table of the database.<BR><BR>
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
		// Remove a skill from the L2Character and its Func objects from calculator set of the L2Character
		L2Skill oldSkill = super.removeSkill(skill);
		
		java.sql.Connection con = null;
		
		try
		{
			// Remove or update a L2PcInstance skill from the character_skills table of the database
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			
			if (oldSkill != null)
			{
				statement = con.prepareStatement(DELETE_SKILL_FROM_CHAR);
				statement.setInt(1, oldSkill.getId());
				statement.setInt(2, getObjectId());
				statement.setInt(3, getClassIndex());
				statement.execute();
				statement.close();
			}
		}
		catch (Exception e)
		{
			_log.warning("Error could not delete skill: " + e);
		}
		finally
		{
			try { con.close(); } catch (Exception e) {}
		}
		
		return oldSkill;
	}
	
	/**
	 * Add or update a L2PcInstance skill in the character_skills table of the database.
	 * <BR><BR>
	 * If newClassIndex > -1, the skill will be stored with that class index, not the current one.
	 */
	private void storeSkill(L2Skill newSkill, L2Skill oldSkill, int newClassIndex)
	{
		int classIndex = _classIndex;
		
		if (newClassIndex > -1)
			classIndex = newClassIndex;
		
		java.sql.Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			
			if (oldSkill != null && newSkill != null)
			{
				statement = con.prepareStatement(UPDATE_CHARACTER_SKILL_LEVEL);
				statement.setInt(1, newSkill.getLevel());
				statement.setInt(2, oldSkill.getId());
				statement.setInt(3, getObjectId());
				statement.setInt(4, classIndex);
				statement.execute();
				statement.close();
			}
			else if (newSkill != null)
			{
				statement = con.prepareStatement(ADD_NEW_SKILL);
				statement.setInt(1, getObjectId());
				statement.setInt(2, newSkill.getId());
				statement.setInt(3, newSkill.getLevel());
				statement.setString(4, newSkill.getName());
				statement.setInt(5, classIndex);
				statement.execute();
				statement.close();
			}
			else
			{
				_log.warning("could not store new skill. its NULL");
			}
		}
		catch (Exception e)
		{
			_log.warning("Error could not store char skills: " + e);
		}
		finally
		{
			try { con.close(); } catch (Exception e) {}
		}
	}
	
	/**
	 * Retrieve from the database all skills of this L2PcInstance and add them to _skills.<BR><BR>
	 */
	private void restoreSkills()
	{
		java.sql.Connection con = null;
		
		try
		{
			// Retrieve all skills of this L2PcInstance from the database
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(RESTORE_SKILLS_FOR_CHAR);
			statement.setInt(1, getObjectId());
			statement.setInt(2, getClassIndex());
			ResultSet rset = statement.executeQuery();
			
			// Go though the recordset of this SQL query
			while (rset.next())
			{
				int id = rset.getInt("skill_id");
				int level = rset.getInt("skill_level");
				
				if (id > 9000)
					continue; // fake skills for base stats
				
				// Create a L2Skill object for each record
				L2Skill skill = SkillTable.getInstance().getInfo(id, level);
				
				// Add the L2Skill object to the L2Character _skills and its Func objects to the calculator set of the L2Character
				super.addSkill(skill);
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("Could not restore character skills: " + e);
		}
		finally
		{
			try { con.close(); } catch (Exception e) {}
		}
	}
	
    /**
     * HRetrieve from the database all skill effects of this L2PcInstance and add them to the player.<BR><BR>
     */
	public void restoreEffects() 
	{
		L2Object[] targets = new L2Character[]{this};
		java.sql.Connection con = null;
		
		try {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			
			statement = con.prepareStatement(RESTORE_SKILL_SAVE);
			statement.setInt(1, getObjectId());
			statement.setInt(2, getClassIndex());
			
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				int skillId = rset.getInt("skill_id");
				int skillLvl = rset.getInt("skill_level");
				int effectCount = rset.getInt("effect_count");
				int effectCurTime = rset.getInt("effect_cur_time");
				
				L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
				callSkill(skill, targets);
				
				for (L2Effect effect : getAllEffects())
                {
					if (effect.getSkill().getId() == skillId)
					{
						effect.setCount(effectCount);
						effect.setFirstTime(effectCurTime);
					}
                }
			}
			
			statement = con.prepareStatement(DELETE_SKILL_SAVE);
			statement.setInt(1, getObjectId());
			statement.setInt(2, getClassIndex());
			statement.executeUpdate();
		} 
		catch (Exception e) {
			_log.warning("Could not restore active effect data: " + e);
		} 
		finally {
			try {con.close();} catch (Exception e) {}
		}
		
		updateEffectIcons();
	}
	
	/**
	 * Retrieve from the database all Henna of this L2PcInstance, add them to _henna and calculate stats of the L2PcInstance.<BR><BR>
	 */
	private void restoreHenna()
	{
		java.sql.Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(RESTORE_CHAR_HENNAS);
			statement.setInt(1, getObjectId());
			statement.setInt(2, getClassIndex());
			ResultSet rset = statement.executeQuery();
			
			for (int i=0;i<3;i++)
				_henna[i]=null;
			
			while (rset.next())
			{
				int slot = rset.getInt("slot");
                
				if (slot<1 || slot>3)
                    continue;
				
				int symbol_id = rset.getInt("symbol_id");
				
				L2HennaInstance sym = null;
				
				if (symbol_id != 0)
				{
					L2Henna tpl = HennaTable.getInstance().getTemplate(symbol_id);
                    
					if (tpl != null)
					{
						sym = new L2HennaInstance(tpl);
						_henna[slot-1] = sym;
					}
				}
			}
			
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("could not restore henna: "+e);
		}
		finally
		{
			try { con.close(); } catch (Exception e) {}
		}
		
		// Calculate Henna modifiers of this L2PcInstance
		recalcHennaStats();
	}
	
	/**
	 * Return the number of Henna empty slot of the L2PcInstance.<BR><BR>
	 */
	public int getHennaEmptySlots()
	{
		int totalSlots = 1 + getClassId().level();
        
		for (int i = 0; i < 3; i++)
			if (_henna[i] != null)
                totalSlots--;
		
		if (totalSlots <= 0)
            return 0;
		
		return totalSlots;
	}
	
	/**
	 * Remove a Henna of the L2PcInstance, save update in the character_hennas table of the database and send Server->Client HennaInfo/UserInfo packet to this L2PcInstance.<BR><BR>
	 */
	public boolean removeHenna(int slot)
	{
		if (slot<1 || slot>3)
			return false;
		
		slot--;
		
		if (_henna[slot]==null)
			return false;
		
		L2HennaInstance henna = _henna[slot];
		_henna[slot] = null;
		
		java.sql.Connection con = null;
        
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(DELETE_CHAR_HENNA);
			statement.setInt(1, getObjectId());
			statement.setInt(2, slot+1);
			statement.setInt(3, getClassIndex());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("could not remove char henna: "+e);
		}
		finally
		{
			try { con.close(); } catch (Exception e) {}
		}
		
		// Calculate Henna modifiers of this L2PcInstance
		recalcHennaStats();
		
		// Send Server->Client HennaInfo packet to this L2PcInstance
		sendPacket(new HennaInfo(this));
		
		// Send Server->Client UserInfo packet to this L2PcInstance
		sendPacket(new UserInfo(this));
		
		// Add the recovered dyes to the player's inventory and notify them.
		getInventory().addItem("Henna", henna.getItemIdDye(), henna.getAmountDyeRequire() / 2, this, null);
        
		SystemMessage sm = new SystemMessage(SystemMessage.EARNED_S2_S1_s);
        sm.addItemName(henna.getItemIdDye());
        sm.addNumber(henna.getAmountDyeRequire() / 2);
		sendPacket(sm);
		
		return true;
	}
	
	/**
	 * Add a Henna to the L2PcInstance, save update in the character_hennas table of the database and send Server->Client HennaInfo/UserInfo packet to this L2PcInstance.<BR><BR>
	 */
	public boolean addHenna(L2HennaInstance henna)
	{
		if (getHennaEmptySlots()==0)
		{
			this.sendMessage("You may not have more than three equipped symbols at a time.");
			return false;
		}
		
		// int slot = 0;
		for (int i=0;i<3;i++)
		{
			if (_henna[i]==null)
			{
				_henna[i]=henna;
				
				// Calculate Henna modifiers of this L2PcInstance
				recalcHennaStats();
				
				java.sql.Connection con = null;
				
				try
				{
					con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement statement = con.prepareStatement(ADD_CHAR_HENNA);
					statement.setInt(1, getObjectId());
					statement.setInt(2, henna.getSymbolId());
					statement.setInt(3, i+1);
					statement.setInt(4, getClassIndex());
					statement.execute();
					statement.close();
				}
				catch (Exception e)
				{
					_log.warning("could not save char henna: "+e);
				}
				finally
				{
					try { con.close(); } catch (Exception e) {}
				}
				
				// Send Server->Client HennaInfo packet to this L2PcInstance
				HennaInfo hi = new HennaInfo(this);
				this.sendPacket(hi);
				
				// Send Server->Client UserInfo packet to this L2PcInstance
				UserInfo ui = new UserInfo(this);
				this.sendPacket(ui);
				
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Calculate Henna modifiers of this L2PcInstance.<BR><BR>
	 */
	private void recalcHennaStats()
	{
		_hennaINT = 0;
		_hennaSTR = 0;
		_hennaCON = 0;
		_hennaMEN = 0;
		_hennaWIT = 0;
		_hennaDEX = 0;
		
		for (int i=0;i<3;i++)
		{
			if (_henna[i]==null)continue;
			_hennaINT += _henna[i].getStatINT();
			_hennaSTR += _henna[i].getStatSTR();
			_hennaMEN += _henna[i].getStatMEM();
			_hennaCON += _henna[i].getStatCON();
			_hennaWIT += _henna[i].getStatWIT();
			_hennaDEX += _henna[i].getStatDEX();
		}
		
		if (_hennaINT>5)_hennaINT=5;
		if (_hennaSTR>5)_hennaSTR=5;
		if (_hennaMEN>5)_hennaMEN=5;
		if (_hennaCON>5)_hennaCON=5;
		if (_hennaWIT>5)_hennaWIT=5;
		if (_hennaDEX>5)_hennaDEX=5;
	}
	
	/**
	 * Return the Henna of this L2PcInstance corresponding to the selected slot.<BR><BR>
	 */
	public L2HennaInstance getHenna(int slot)
	{
		if (slot < 1 || slot > 3)
            return null;
        
		return _henna[slot - 1];
	}
	
	/**
	 * Return the INT Henna modifier of this L2PcInstance.<BR><BR>
	 */
	public int getHennaStatINT()
	{
		return _hennaINT;
	}
	
	/**
	 * Return the STR Henna modifier of this L2PcInstance.<BR><BR>
	 */
	public int getHennaStatSTR()
	{
		return _hennaSTR;
	}
	
	/**
	 * Return the CON Henna modifier of this L2PcInstance.<BR><BR>
	 */
	public int getHennaStatCON()
	{
		return _hennaCON;
	}
	
	/**
	 * Return the MEN Henna modifier of this L2PcInstance.<BR><BR>
	 */
	public int getHennaStatMEN()
	{
		return _hennaMEN;
	}
	
	/**
	 * Return the WIT Henna modifier of this L2PcInstance.<BR><BR>
	 */
	public int getHennaStatWIT()
	{
		return _hennaWIT;
	}
	
	/**
	 * Return the DEX Henna modifier of this L2PcInstance.<BR><BR>
	 */
	public int getHennaStatDEX()
	{
		return _hennaDEX;
	}
	
	/**
	 * Return True if the L2PcInstance is autoAttackable.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Check if the attacker isn't the L2PcInstance Pet </li>
	 * <li>Check if the attacker is L2MonsterInstance</li>
	 * <li>If the attacker is a L2PcInstance, check if it is not in the same party </li>
	 * <li>Check if the L2PcInstance has Karma </li>
	 * <li>If the attacker is a L2PcInstance, check if it is not in the same siege clan (Attacker, Defender) </li><BR><BR>
	 *
	 */
	public boolean isAutoAttackable(L2Character attacker)
	{
		// Check if the attacker isn't the L2PcInstance Pet
		if (attacker == this || attacker == this.getPet())
			return false;
		
		// TODO: check for friendly mobs
		// Check if the attacker is a L2MonsterInstance
		if (attacker instanceof L2MonsterInstance)
			return true;
		
		// Check if the attacker is not in the same party
		if (getParty() != null && getParty().getPartyMembers().contains(attacker))
			return false;
		
		// Check if the attacker is not in the same clan
		if (getClan() != null && attacker != null && getClan().isMember(attacker.getName()))
			return false;
		
        if(attacker instanceof L2PlayableInstance && ZoneManager.getInstance().checkIfInZonePeace(this))
            return false;
        
		// Check if the L2PcInstance has Karma
		if (getKarma() > 0 || getPvpFlag() > 0)
			return true;
		
		// Check if the attacker is a L2PcInstance
		if (attacker instanceof L2PcInstance)
		{
			// Check if the L2PcInstance is in an arena or a siege area
			if (ZoneManager.getInstance().checkIfInZonePvP(this) && ZoneManager.getInstance().checkIfInZonePvP(attacker))
				return true;
			
			if (this.getClan() != null)
			{
				Siege siege = SiegeManager.getInstance().getSiege(getX(), getY());
				if (siege != null)
				{
					// Check if a siege is in progress and if attacker and the L2PcInstance aren't in the Defender clan
					if (siege.checkIsDefender(((L2PcInstance)attacker).getClan()) &&
							siege.checkIsDefender(this.getClan()))
						return false;
					
					// Check if a siege is in progress and if attacker and the L2PcInstance aren't in the Attacker clan
					if (siege.checkIsAttacker(((L2PcInstance)attacker).getClan()) &&
							siege.checkIsAttacker(this.getClan()))
						return false;
				}
				
				// Check if clan is at war
				if (this.getClan() != null && ((L2PcInstance)attacker).getClan() != null && (this.getClan().isAtWarWith(((L2PcInstance)attacker).getClanId()) && this.getWantsPeace() == 0 && ((L2PcInstance)attacker).getWantsPeace() == 0))
				return true;
			}
		}
		else if (attacker instanceof L2SiegeGuardInstance)
		{
			if (this.getClan() != null)
			{
				Siege siege = SiegeManager.getInstance().getSiege(this);
				return (siege != null && siege.checkIsAttacker(this.getClan()));
			}
		}
		
		return false;
	}
	
	
	/**
	 * Check if the active L2Skill can be casted.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Check if the skill isn't toggle and is offensive </li>
	 * <li>Check if the target is in the skill cast range </li>
	 * <li>Check if the skill is Spoil type and if the target isn't already spoiled </li>
	 * <li>Check if the caster owns enought consummed Item, enough HP and MP to cast the skill </li>
	 * <li>Check if the caster isn't sitting </li>
	 * <li>Check if all skills are enabled and this skill is enabled </li><BR><BR>
	 * <li>Check if the caster own the weapon needed </li><BR><BR>
	 * <li>Check if the skill is active </li><BR><BR>
	 * <li>Check if all casting conditions are completed</li><BR><BR>
	 * <li>Notify the AI with AI_INTENTION_CAST and target</li><BR><BR>
	 *
	 * @param skill The L2Skill to use
	 * @param forceUse used to force ATTACK on players
	 * @param dontMove used to prevent movement, if not in range
	 *
	 */
	public void useMagic(L2Skill skill, boolean forceUse, boolean dontMove)
	{
        if (isDead())
        {
            abortCast();
            sendPacket(new ActionFailed());
            return;
        }
        /*
        if (isWearingFormalWear() && !skill.isPotion())
        {
            sendPacket(new SystemMessage(SystemMessage.CANNOT_USE_ITEMS_SKILLS_WITH_FORMALWEAR));
    
            sendPacket(new ActionFailed());
            abortCast();
            return;
        }  
        */
        if (inObserverMode())
        {
            sendMessage("Cant use magic in observer mode");
            abortCast();
            sendPacket(new ActionFailed());
            return;
        }

        
		// Check if the skill type is TOGGLE
		if (skill.isToggle())
		{
			// Get effects of the skill
            L2Effect effect = getEffect(skill);
            
			if (effect != null)
			{
				effect.exit();
				
				// Send a Server->Client packet ActionFailed to the L2PcInstance
				sendPacket(new ActionFailed());
				return;
			}
		}
        
        // Check if the skill is active
        if (skill.isPassive())
        {
            // just ignore the passive skill request. why does the client send it anyway ??
            // Send a Server->Client packet ActionFailed to the L2PcInstance
            sendPacket(new ActionFailed());
            return;
        }
        
		// If summon siege golem (13), Summon Wild Hog Cannon (299), check its ok to place the flag
		if ((skill.getId() == 13 || skill.getId() == 299) && !SiegeManager.getInstance().checkIfOkToSummon(this, false))
			return;
        
        //************************************* Check Casting in Progress *******************************************
        
        // If a skill is currently being used, queue this one if this is not the same
        if (getCurrentSkill() != null && isCastingNow())
        {
            // Check if new skill different from current skill in progress
            if (skill.getId() == getCurrentSkill().getSkillId())
                return;
            
            if (Config.DEBUG && getQueuedSkill() != null)
                _log.info(getQueuedSkill().getSkill().getName() + " is already queued for " + getName() + ".");
            
            //setCurrentSkill(null, false, false);
            
            // Create a new SkillDat object and queue it in the player _queuedSkill
            setQueuedSkill(skill, forceUse, dontMove);
            return;
        }
               
        
        
        //************************************* Check Target *******************************************
        
        // Create and set a L2Object containing the target of the skill
        L2Object target = null;
        
        switch (skill.getTargetType())
        {
            // Target the player if skill type is AURA, PARTY, CLAN or SELF
            case TARGET_AURA:      
            case TARGET_PARTY:      
            case TARGET_ALLY:
            case TARGET_CLAN:
            case TARGET_SELF:
                target = this;
                break;
            default:
                target = getTarget();
            break;
        }
        
        // Check the validity of the target
        if (target == null)
        {
            sendPacket(new SystemMessage(SystemMessage.TARGET_CANT_FOUND));
            sendPacket(new ActionFailed());
            return;
        }   
        
        //Don't allow casting on players on different dungeon lvls etc
        if ((Math.abs(target.getZ() - getZ()) > 1000))
        {
            sendPacket(new SystemMessage(SystemMessage.CANT_SEE_TARGET));
            sendPacket(new ActionFailed());
            return;
        }
        
        
        
        //************************************* Check skill availability *******************************************
        
        // Check if this skill is enabled (ex : reuse time)
        if (isSkillDisabled(skill.getId()) && (getAccessLevel() < Config.GM_PEACEATTACK))
        {
            SystemMessage sm = new SystemMessage(SystemMessage.SKILL_NOT_AVAILABLE);
            sm.addString(skill.getName());
            sendPacket(sm);
            
            // Send a Server->Client packet ActionFailed to the L2PcInstance
            sendPacket(new ActionFailed());
            return;
        }
        
        // Check if all skills are disabled
        if (isAllSkillsDisabled() && (getAccessLevel() < Config.GM_PEACEATTACK))
        {
            // Send a Server->Client packet ActionFailed to the L2PcInstance
            sendPacket(new ActionFailed());
            return;
        }
        
        
        
        //************************************* Check Consumables *******************************************
        
        // Check if the caster has enough MP
        if (getCurrentMp() < getStat().getMpConsume(skill) + getStat().getMpInitialConsume(skill))
        {
            // Send a System Message to the caster
            sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_MP));
            
            // Send a Server->Client packet ActionFailed to the L2PcInstance
            sendPacket(new ActionFailed());
            return;
        }
        
        // Check if the caster has enough HP
        if (getCurrentHp() <= skill.getHpConsume())
        {
            // Send a System Message to the caster
            sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_HP));
            
            // Send a Server->Client packet ActionFailed to the L2PcInstance
            sendPacket(new ActionFailed());
            return;
        }
        
        // Check if the spell consummes an Item
        if (skill.getItemConsume() > 0)
        {
            // Get the L2ItemInstance consummed by the spell
            L2ItemInstance requiredItems = getInventory().getItemByItemId(skill.getItemConsumeId());
            
            // Check if the caster owns enought consummed Item to cast
            if (requiredItems == null || requiredItems.getCount() < skill.getItemConsume())
            {
            	// Checked: when a summon skill failed, server show required consume item count
            	if (skill.getSkillType() == L2Skill.SkillType.SUMMON)
                {
            		SystemMessage sm = new SystemMessage(SystemMessage.SUMMONING_SERVITOR_COSTS_S2_S1);
            		sm.addItemName(skill.getItemConsumeId());
            		sm.addNumber(skill.getItemConsume());
            		sendPacket(sm);
            		return;
                }
            	else
                {
            		// Send a System Message to the caster
            		sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_ITEMS));
            		return;
                }
            }
        }
        
        //************************************* Check Casting Conditions *******************************************
        
        // Check if the caster own the weapon needed
        if (!skill.getWeaponDependancy(this))
        {
            // Send a Server->Client packet ActionFailed to the L2PcInstance
            sendPacket(new ActionFailed());
            return;
        }
        
        // Check if all casting conditions are completed
        if (!skill.checkCondition(this, false))
        {
            // Send a Server->Client packet ActionFailed to the L2PcInstance
            sendPacket(new ActionFailed());
            return;
        }
        
        
        
        //************************************* Check Player State *******************************************
        
        // Abnormal effects(ex : Stun, Sleep...) are checked in L2Character useMagic()
        
        // Check if the player use "Fake Death" skill
        if (isAlikeDead())
        {
            // Send a Server->Client packet ActionFailed to the L2PcInstance
            sendPacket(new ActionFailed());
            return;
        }
        
        // Check if the caster is sitting
        if (isSitting() && !skill.isPotion())
        {
            // Send a System Message to the caster
            sendPacket(new SystemMessage(SystemMessage.CANT_MOVE_SITTING));
            
            // Send a Server->Client packet ActionFailed to the L2PcInstance
            sendPacket(new ActionFailed());
            return;
        }
        
        if (isFishing() && (skill.getSkillType() != SkillType.PUMPING && skill.getSkillType() != SkillType.REELING && skill.getSkillType() != SkillType.FISHING))
        {
            //Only fishing skills are available
            this.sendPacket(new SystemMessage(1448));
            return;
        }
	
        
        
        //************************************* Check Skill Type *******************************************
		
        // Check if this is offensive magic skill
        if (skill.isOffensive())  
		{
			if ((isInsidePeaceZone(this, target)) && (getAccessLevel() < Config.GM_PEACEATTACK))
			{
				// If L2Character or target is in a peace zone, send a system message TARGET_IN_PEACEZONE a Server->Client packet ActionFailed
				sendPacket(new SystemMessage(SystemMessage.TARGET_IN_PEACEZONE));
				sendPacket(new ActionFailed());
				return;
			}
			
            
            // Check if the target is attackable
            if (!target.isAttackable() && (getAccessLevel() < Config.GM_PEACEATTACK))
			{
				// If target is not attackable, send a Server->Client packet ActionFailed
				sendPacket(new ActionFailed());
				return;
			}
			
			// Check if a Forced ATTACK is in progress on non-attackable target
			if (!target.isAutoAttackable(this) && !forceUse &&
					skill.getTargetType() != SkillTargetType.TARGET_AURA &&
					skill.getTargetType() != SkillTargetType.TARGET_CLAN &&
					skill.getTargetType() != SkillTargetType.TARGET_ALLY &&
					skill.getTargetType() != SkillTargetType.TARGET_PARTY &&
					skill.getTargetType() != SkillTargetType.TARGET_SELF)
			{
				// Send a Server->Client packet ActionFailed to the L2PcInstance
				sendPacket(new ActionFailed());
				return;
			}
			
			// Check if the target is in the skill cast range
			if (dontMove)
			{
				// Calculate the distance between the L2PcInstance and the target
                if (skill.getCastRange() > 0 && !isInsideRadius(target, skill.getCastRange(), false, false))
				{
					// Send a System Message to the caster
					sendPacket(new SystemMessage(SystemMessage.TARGET_TOO_FAR));
					
					// Send a Server->Client packet ActionFailed to the L2PcInstance
					sendPacket(new ActionFailed());
					return;
				}
			}
		}
		
		// Check if the skill is defensive
		if (!skill.isOffensive())
		{
			// check if the target is a monster and if force attack is set.. if not then we don't want to cast.
			if ((target instanceof L2MonsterInstance) && !forceUse
                    && (skill.getTargetType() != SkillTargetType.TARGET_PET)
                    && (skill.getTargetType() != SkillTargetType.TARGET_AURA)
                    && (skill.getTargetType() != SkillTargetType.TARGET_CLAN)
                    && (skill.getTargetType() != SkillTargetType.TARGET_SELF)
                    && (skill.getTargetType() != SkillTargetType.TARGET_PARTY)
                    && (skill.getTargetType() != SkillTargetType.TARGET_ALLY)
                    && (skill.getTargetType() != SkillTargetType.TARGET_CORPSE_MOB)
                    && (skill.getTargetType() != SkillTargetType.TARGET_AREA_CORPSE_MOB))
			{
				// send the action failed so that the skill doens't go off.
				sendPacket (new ActionFailed());
				return;
			}
		}

		// Check if the skill is Spoil type and if the target isn't already spoiled
		if (skill.getSkillType() == SkillType.SPOIL)
		{
			if (!(target instanceof L2MonsterInstance))
			{
				// Send a System Message to the L2PcInstance
				sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
				
				// Send a Server->Client packet ActionFailed to the L2PcInstance
				sendPacket(new ActionFailed());
				return;
			} else if (((L2Attackable)target).isSpoil())
			{
				// Send a System Message to the caster
				sendPacket(new SystemMessage(SystemMessage.ALREDAY_SPOILED));
				
				// Send a Server->Client packet ActionFailed to the L2PcInstance
				sendPacket(new ActionFailed());
				return;
			}
		}
        
        // Check if the skill is Sweep type and if conditions not apply
        if (skill.getSkillType() == SkillType.SWEEP && target instanceof L2Attackable)
        {
            int spoilerId = ((L2Attackable)target).getIsSpoiledBy();

            if ((((L2Attackable)target).isDead() && !((L2Attackable)target).isSpoil()) || (getObjectId() != spoilerId && !isInLooterParty(spoilerId)))
            {
                // Send a System Message to the L2PcInstance
                sendPacket(new SystemMessage(SystemMessage.SWEEPER_FAILED_TARGET_NOT_SPOILED));

                // Send a Server->Client packet ActionFailed to the L2PcInstance
                sendPacket(new ActionFailed());
                return;
            }
        }

		// Check if the skill is Drain Soul (Soul Crystals) and if the target is a MOB
		if (skill.getSkillType() == SkillType.DRAIN_SOUL)
		{
			if (!(target instanceof L2MonsterInstance))
			{
				// Send a System Message to the L2PcInstance
				sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
				
				// Send a Server->Client packet ActionFailed to the L2PcInstance
				sendPacket(new ActionFailed());
				return;
			}
		}
        
		// Check if this is a Pvp skill and target isn't a non-flagged/non-karma player
		switch (skill.getTargetType()) 
        {
			case TARGET_PARTY:
			case TARGET_ALLY:   // For such skills, checkPvpSkill() is called from L2Skill.getTargetList()
			case TARGET_CLAN:   // For such skills, checkPvpSkill() is called from L2Skill.getTargetList()
			case TARGET_AURA:
			case TARGET_SELF:
				break;
			default:
				if (!checkPvpSkill(target, skill) && (getAccessLevel() < Config.GM_PEACEATTACK)) 
                {
					// Send a System Message to the L2PcInstance
					sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
					
					// Send a Server->Client packet ActionFailed to the L2PcInstance
					sendPacket(new ActionFailed());
					return;
				}
		}
        
        if (skill.getTargetType() == SkillTargetType.TARGET_HOLY && !TakeCastle.checkIfOkToCastSealOfRule(this, false))
        {
            sendPacket(new ActionFailed());
            abortCast();
            return;
        }
        
        if (skill.getSkillType() == SkillType.SIEGEFLAG && !SiegeFlag.checkIfOkToPlaceFlag(this, false))
        {
            sendPacket(new ActionFailed());
            abortCast();
            return;
        }
		
		     
        
        // If all conditions are checked, create a new SkillDat object and set the player _currentSkill
        setCurrentSkill(skill, forceUse, dontMove);
        
		// Check if the active L2Skill can be casted (ex : not sleeping...), Check if the target is correct and Notify the AI with AI_INTENTION_CAST and target
        super.useMagic(skill);
        
    }
    
    public boolean isInLooterParty(int LooterId)
    {
    	L2PcInstance looter = (L2PcInstance)L2World.getInstance().findObject(LooterId);
        
    	if (isInParty() && looter != null) 
            return getParty().getPartyMembers().contains(looter);
        
		return false;
    }

	/**
	 * Check if the requested casting is a Pc->Pc skill cast and if it's a valid pvp condition
	 * @param target L2Object instance containing the target
	 * @param skill L2Skill instance with the skill being casted
	 * @return False if the skill is a pvpSkill and target is not a valid pvp target
	 */
	public boolean checkPvpSkill(L2Object target, L2Skill skill)
	{
		// check for PC->PC Pvp status
		if (
				target != null &&                                           // target not null and
				target != this &&                                           // target is not self and
				target instanceof L2PcInstance &&                           // target is L2PcInstance and
				skill.isPvpSkill() &&                                       // pvp skill and
				!ZoneManager.getInstance().checkIfInZonePvP(this) &&        // Pc is not in PvP zone
				!ZoneManager.getInstance().checkIfInZonePvP(target)         // target is not in PvP zone
		)
		{
            if(this.getClan() != null && ((L2PcInstance)target).getClan() != null)
            {
                if(this.getClan().isAtWarWith(((L2PcInstance)target).getClan().getClanId()))
                    return true; // in clan war player can attack whites even with sleep etc.
            }
            if (
					((L2PcInstance)target).getPvpFlag() == 0 &&             //   target's pvp flag is not set and
					((L2PcInstance)target).getKarma() == 0                  //   target has no karma
			)
				return false;
		}
		return true;
	}
	
	/**
	 * Reduce Item quantity of the L2PcInstance Inventory and send it a Server->Client packet InventoryUpdate.<BR><BR>
	 */
	public void consumeItem(int itemConsumeId, int itemCount)
	{
		if (itemConsumeId != 0 && itemCount != 0)
			destroyItemByItemId("Consume", itemConsumeId, itemCount, null, false);
	}
	
	/**
	 * Return True if the L2PcInstance is a Mage.<BR><BR>
	 */
	public boolean isMageClass()
	{
		return getClassId().isMage();
	}
	
	
	public boolean isMounted()
	{
		return _mountType > 0;
	}
	
	/**
	 * Set the type of Pet mounted (0 : none, 1 : Stridder, 2 : Wyvern) and send a Server->Client packet InventoryUpdate to the L2PcInstance.<BR><BR>
	 */
	public boolean checkLandingState() 
	{ 
		// Check if char is in a no landing zone 
		if (ZoneManager.getInstance().checkIfInZoneNoLanding(this))
			return true;
		else 
			// if this is a castle that is currently being sieged, and the rider is NOT a castle owner
			// he cannot land.
			// castle owner is the leader of the clan that owns the castle where the pc is
			if (SiegeManager.getInstance().checkIfInZone(this) &&  
					!(getClan()!=null &&
							CastleManager.getInstance().getCastle(this) == CastleManager.getInstance().getCastleByOwner(getClan()) &&
							this == getClan().getLeader().getPlayerInstance()))
				return true;
		
		return false;
	} 
	
	public void setMountType(int mountType)
	{
		if (checkLandingState() && mountType !=0) 
			return; 
		
		switch(mountType)
		{
			case 0: 
                setIsFlying(false);
                setIsRiding(false);
                break; //Dismounted
			case 1: 
                setIsRiding(true);
                break;
			case 2: 
                setIsFlying(true);
                break; //Flying Wyvern
		}
		
		_mountType = mountType;
		
		// Send a Server->Client packet InventoryUpdate to the L2PcInstance in order to update speed
		UserInfo ui = new UserInfo(this);
		sendPacket(ui);
		
	}
	
	/**
	 * Return the type of Pet mounted (0 : none, 1 : Stridder, 2 : Wyvern).<BR><BR>
	 */
	public int getMountType()
	{
		return _mountType;
	}
	
	/**
	 * Send a Server->Client packet UserInfo to this L2PcInstance and CharInfo to all L2PcInstance in its _KnownPlayers.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * Others L2PcInstance in the detection area of the L2PcInstance are identified in <B>_knownPlayers</B>.
	 * In order to inform other players of this L2PcInstance state modifications, server just need to go through _knownPlayers to send Server->Client Packet<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Send a Server->Client packet UserInfo to this L2PcInstance (Public and Private Data)</li>
	 * <li>Send a Server->Client packet CharInfo to all L2PcInstance in _KnownPlayers of the L2PcInstance (Public data only)</li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : DON'T SEND UserInfo packet to other players instead of CharInfo packet.
	 * Indeed, UserInfo packet contains PRIVATE DATA as MaxHP, STR, DEX...</B></FONT><BR><BR>
	 *
	 */
	public void updateAbnormalEffect()
	{
		broadcastUserInfo();
	}
	
	/**
	 * Disable the Inventory and create a new task to enable it after 1.5s.<BR><BR>
	 */
	public void tempInvetoryDisable()
	{
		_inventoryDisable  = true;
		
		ThreadPoolManager.getInstance().scheduleGeneral(new InventoryEnable(), 1500);
	}
	
	/**
	 * Return True if the Inventory is disabled.<BR><BR>
	 */
	public boolean isInvetoryDisabled()
	{
		return _inventoryDisable;
	}
	
	class InventoryEnable implements Runnable
	{
		public void run()
		{
			_inventoryDisable = false;
		}
	}
	
	public Map<Integer, L2CubicInstance> getCubics()
	{
		return _cubics;
	}
	
	/**
	 * Add a L2CubicInstance to the L2PcInstance _cubics.<BR><BR>
	 */
	public void addCubic(int id, int level)
	{
		L2CubicInstance cubic = new L2CubicInstance(this, id, level);
		_cubics.put(id, cubic);
	}
	
	/**
	 * Remove a L2CubicInstance from the L2PcInstance _cubics.<BR><BR>
	 */
	public void delCubic(int id)
	{
		_cubics.remove(id);
	}
	
	/**
	 * Return the L2CubicInstance corresponding to the Identifier of the L2PcInstance _cubics.<BR><BR>
	 */
	public L2CubicInstance getCubic(int id)
	{
		return _cubics.get(id);
	}
	
	public String toString()
	{
		return "player "+getName();
	}
	
	/**
	 * Return the modifier corresponding to the Enchant Effect of the Active Weapon (Min : 127).<BR><BR>
	 */
	public int getEnchantEffect()
	{
		L2ItemInstance wpn = getActiveWeaponInstance();
		
		if (wpn == null)
			return 0;
		
		return Math.min(127, wpn.getEnchantLevel());
	}
	
	/**
	 * Set the _lastFolkNpc of the L2PcInstance corresponding to the last Folk wich one the player talked.<BR><BR>
	 */
	public void setLastFolkNPC(L2FolkInstance folkNpc)
	{
		_lastFolkNpc = folkNpc;
	}
	
	/**
	 * Return the _lastFolkNpc of the L2PcInstance corresponding to the last Folk wich one the player talked.<BR><BR>
	 */
	public L2FolkInstance getLastFolkNPC()
	{
		return _lastFolkNpc;
	}
	
	/**
	 * Set the Silent Moving mode Flag.<BR><BR>
	 */
	public void setSilentMoving(boolean flag)
	{
		_isSilentMoving = flag;
	}
	
	/**
	 * Return True if the Silent Moving mode is active.<BR><BR>
	 */
	public boolean isSilentMoving()
	{
		return _isSilentMoving;
	}
	
	/**
	 * Return True if L2PcInstance is a participant in the Festival of Darkness.<BR><BR>
	 */
	public boolean isFestivalParticipant()
	{
		return SevenSignsFestival.getInstance().isParticipant(this);
	}
	
	public void addAutoSoulShot(int itemId)
	{
		_activeSoulShots.add(itemId);
	}
	
	public void removeAutoSoulShot(int itemId)
	{
		_activeSoulShots.remove(itemId);
	}
	
	public Set<Integer> getAutoSoulShot()
	{
		return _activeSoulShots;
	}
	
	public void rechargeAutoSoulShot(boolean physical, boolean magic, boolean summon)
	{
		L2ItemInstance item;
		IItemHandler handler;
		
		if (_activeSoulShots == null || _activeSoulShots.size() == 0)
			return;
		
		for (int itemId : _activeSoulShots)
		{
			item = getInventory().getItemByItemId(itemId);
			
			if (item != null)
			{
                if (magic)
                {
                    if (!summon)
                    {
                        if (itemId == 2509 || itemId == 2510 || itemId == 2511 || 
                                itemId == 2512 || itemId == 2513 || itemId == 2514 || 
                                itemId == 3947 || itemId == 3948 || itemId == 3949 || 
                                itemId == 3950 || itemId == 3951 || itemId == 3952 || 
                                itemId == 5790)
                        {
                            handler = ItemHandler.getInstance().getItemHandler(itemId);
                            
                            if (handler != null)
                                handler.useItem(this, item);
                        }
                    } else
                    {
                        if (itemId == 6646 || itemId == 6647)
                        {
                            handler = ItemHandler.getInstance().getItemHandler(itemId);
                            
                            if (handler != null)
                                handler.useItem(this, item);
                        }
                    }
                }
                
                if (physical)
                {
                    if (!summon)
                    {
                        if (itemId == 1463 || itemId == 1464 || itemId == 1465 || 
                                itemId == 1466 || itemId == 1467 || itemId == 1835 || 
                                itemId == 5789)
                        {
                            handler = ItemHandler.getInstance().getItemHandler(itemId);
                            
                            if (handler != null)
                                handler.useItem(this, item);
                        }
                    } else
                    {
                        if (itemId == 6645)
                        {
                            handler = ItemHandler.getInstance().getItemHandler(itemId);
                            
                            if (handler != null)
                                handler.useItem(this, item);
                        }
                    }
                }
			}
			else
			{
				removeAutoSoulShot(itemId);
			}
		}
	}
	
    
    
	private ScheduledFuture _taskWarnUserTakeBreak;
	
	class WarnUserTakeBreak implements Runnable
	{
		public void run()
		{
			if (L2PcInstance.this.isOnline() == 1)
			{
				SystemMessage msg = new SystemMessage(764);
				L2PcInstance.this.sendPacket(msg);
			}
			else
				stopWarnUserTakeBreak();
		}
	}
    
	class RentPetTask implements Runnable
	{
		public void run()
		{
			stopRentPet();
		}
	}
	
    private ScheduledFuture _taskforfish;
    
	class WaterTask implements Runnable
	{
		public void run()
		{
			double reduceHp = getMaxHp()/100;
            
			if (reduceHp < 1)
				reduceHp = 1;
            
			reduceCurrentHp(reduceHp,L2PcInstance.this,false);
			//reduced hp, becouse not rest
			SystemMessage sm = new SystemMessage(297);
			sm.addNumber((int)reduceHp);
			sendPacket(sm);
			
		}
	}
	
    class LokingForFishTask implements Runnable
    {
        public void run()
        {
            int rate = 90; //Only for now
            int check = Rnd.get(100);
            StopLookingForFishTask();
            if(rate > check)            
            {
                StartFishCombat();              
            }
            else EndFishing(false);
        }
    }
    
	public void setInvisible()
	{
		_invisible = 1;
	}
	
	public void setVisible()
	{
		_invisible = 0;
	}
	
	public int getInvisible()
	{
		return _invisible;
	}
	
	public void togglePathNodeMode()
	{
		_isPathNodeMode	= !_isPathNodeMode;
	}
	
	public void toggleViewPathNodes()
	{
		_isPathNodesVisible	= !_isPathNodesVisible;
	}
	
	public boolean isPathNodeModeActive()
	{
		return _isPathNodeMode;
	}
	
	public boolean isPathNodeVisible()
	{
		return _isPathNodesVisible;
	}
	
	public Map<WayPointNode, List<WayPointNode>> getPathNodeMap()
	{
		if (_pathNodeMap == null)
			_pathNodeMap = new FastMap<WayPointNode, List<WayPointNode>>();
        
		return _pathNodeMap;
	}
	
	public void addPathNodePoint()
	{
		//add Node to Players PathNode List
		//TODO: Addcheck for nearby point in radius
		setSelectedNode(WayPointNode.spawn(this));
		getPathNodeMap().put(getSelectedNode(), new FastList<WayPointNode>());
	}
	
	/**
	 * Delete all Path Nodes.<BR><BR>
	 */
	public void clearPathNodes()
	{
		if (getPathNodeMap() != null)
		{
			for (WayPointNode node : getPathNodeMap().keySet())
				removePathNodePoint(node);
            
            getPathNodeMap().clear();
		}
        
		setSelectedNode(null);
	}
	
	public void setSelectedNode(WayPointNode decoInstance)
	{
		if (getSelectedNode() != null)
		{
			List<WayPointNode> linkedNodes = getPathNodeMap().get(getSelectedNode());
            
			if (linkedNodes != null)
				for (WayPointNode node : linkedNodes)
					node.setNormal();
            
            getSelectedNode().setNormal();
		}
        
		_selectedNode = decoInstance;
        
		if (_selectedNode != null)
		{
			_selectedNode.setSelected();
            
			List<WayPointNode> linkedNodes = getPathNodeMap().get(_selectedNode);
            
			if (linkedNodes != null)
				for (WayPointNode linked : linkedNodes)
					linked.setLinked();
		}
	}
	
	public WayPointNode getSelectedNode()
	{
		return _selectedNode;
	}
	
	public void addLink(WayPointNode target)
	{
		if (getSelectedNode() != null)
		{
			List<WayPointNode> list	= getPathNodeMap().get(getSelectedNode());
            
			if (list == null)
				list = new FastList<WayPointNode>();
            
			list.add(target);
			_pathNodeMap.put(getSelectedNode(), list);
			
			List<WayPointNode> list2 = getPathNodeMap().get(target);
            
			if (list2 == null)
                list2 = new FastList<WayPointNode>();
            
			list2.add(getSelectedNode());
			_pathNodeMap.put(target, list2);
            
			target.setLinked();
			WayPointNode.drawLine(target, getSelectedNode());
		}
	}
	
	public void removeLink(WayPointNode target)
	{
		if (getSelectedNode() != null)
		{
			List<WayPointNode> list	= getPathNodeMap().get(getSelectedNode());
            
			if (list != null && list.contains(target))
			{
				list.remove(target);
                _pathNodeMap.put(getSelectedNode(), list);
				
				List<WayPointNode> list2 = getPathNodeMap().get(target);
                
				if (list2 != null && list2.contains(getSelectedNode()))
				{
					list2.remove(getSelectedNode());
					_pathNodeMap.put(target, list2);
				}
                
				target.setNormal();
				WayPointNode.eraseLine(target, getSelectedNode());
			}
			else
			{
				sendPacket(SystemMessage.sendString("Target not a Linked Node."));
			}
		}
	}
	
	public void removePathNodePoint()
	{
		removePathNodePoint(getSelectedNode());
	}
	
	public void removePathNodePoint(WayPointNode node)
	{
		if (node != null)
		{
			List<WayPointNode> list  = getPathNodeMap().get(node);
            
			if (list != null)
			{
				synchronized (list)
				{
					try
					{
						for (WayPointNode link : list)
						{
							//WayPointNode.eraseLine(node, link);
							removeLink(link);
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
            
			node.decayMe();
			node = null;
		}
	}
	
	/**
	 * @param string
	 */
	public void savePathNodes(String fileName)
	{
		try
		{
			//File file   = new File(Config.DATAPACK_ROOT, "pathnode/" + fileName + ".bin");
			FileOutputStream fos = new FileOutputStream("pathnode/" + fileName + ".bin"); // Save to file
			GZIPOutputStream gzos = new GZIPOutputStream(fos); // Compressed
			ObjectOutputStream out = new ObjectOutputStream(gzos); // Save objects
			out.writeObject(getSaveList()); // Write the entire pathNodeMap of Positions
			out.flush(); // Always flush the output.
			out.close(); // And close the stream.
			sendPacket(SystemMessage.sendString("Path Node table saved to: L2J/pathnode/"+fileName+".bin"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			sendPacket(SystemMessage.sendString("Could not Save Path Node Table."));
		}
	}
	
	public Map<Point3D, List<Point3D>> getSaveList()
	{
		Map<Point3D, List<Point3D>> saveList   = null;
        
		if (getPathNodeMap() != null)
		{
			saveList = Collections.synchronizedMap(new WeakHashMap<Point3D, List<Point3D>>());
            
			for (WayPointNode node : getPathNodeMap().keySet())
			{
				Point3D nodePoint   = Point3D.getPosition(node);
				
				List<WayPointNode> links = getPathNodeMap().get(node);
				List<Point3D> linkPoints = null;
                
				if (links != null)
				{
					linkPoints  = new FastList<Point3D>();
                    
					for (WayPointNode link : links)
						linkPoints.add(Point3D.getPosition(link));
				}
                
				saveList.put(nodePoint, linkPoints);
			}
		}
        
		return saveList;
	}
	
	/**
	 * @param string
	 */
	@SuppressWarnings(value = { "unchecked" })
	public void loadPathNodes(String fileName)
	{
		Map<Point3D, List<Point3D>> newNodeMap = null;
        
		try
		{
			//Create necessary input streams
			//File file   = new File(Config.DATAPACK_ROOT, "pathnode/" + fileName + ".bin");
			FileInputStream fis = new FileInputStream("pathnode/" + fileName + ".bin"); // Read from file
			GZIPInputStream gzis = new GZIPInputStream(fis); // Uncompress
			ObjectInputStream in = new ObjectInputStream(gzis); // Read objects
			// Read in an object. It should be a vector of scribbles
			
			newNodeMap = (FastMap<Point3D, List<Point3D>>)in.readObject();
			in.close(); // Close the stream.
		}
		catch (Exception e)
		{
			sendPacket(SystemMessage.sendString("Could not Load Path Node Table from: L2J/pathnode/"+fileName+".bin"));
			e.printStackTrace();
			return;
		}
        
		clearPathNodes();
		loadNodeList(newNodeMap);
		sendPacket(SystemMessage.sendString("Path Node table loaded from: L2J/pathnode/"+fileName+".bin"));
	}
	
	/**
	 * @param newNodeMap
	 */
	private void loadNodeList(Map<Point3D, List<Point3D>> newNodeMap)
	{
		_pathNodeMap = Collections.synchronizedMap(new WeakHashMap<WayPointNode, List<WayPointNode>>());
		Map<Point3D, WayPointNode> map = new FastMap<Point3D, WayPointNode>();
		WayPointNode newNode = null;
        
		for (Point3D point : newNodeMap.keySet())
		{
			newNode = WayPointNode.spawn(point);
			newNode.setNormal();
			map.put(point, newNode);
		}
		
		for (Point3D point : newNodeMap.keySet())
		{
			WayPointNode node = map.get(point);
			List<Point3D> links = newNodeMap.get(point);
			List<WayPointNode> nodeLinks = null;
            
			if (links != null)
			{
				nodeLinks  = new FastList<WayPointNode>();
                
				for (Point3D link : links)
				{
					WayPointNode linkNode = map.get(link);
					nodeLinks.add(linkNode);
                    
					if (getPathNodeMap().get(linkNode) != null &&
							!getPathNodeMap().get(linkNode).contains(node))
						WayPointNode.drawLine(linkNode, node);
				}
			}
            
			_pathNodeMap.put(node, nodeLinks);
		}
	}
	
	public int getClanPrivileges()
    {
		return _clanPrivileges;
	}
    
	public void setClanPrivileges(int n)
    {
		_clanPrivileges = n;
	}

    public void setPledgeClass(int classId)
    {
        _pledgeClass = classId;
    }
    public int getPledgeClass()
    {
        return _pledgeClass;
    }

    public void setPledgeType(int typeId)
    {
        _pledgeType = typeId;
    }
    public int getPledgeType()
    {
        return _pledgeType;
    }

	
	public void refreshLinks()
	{
		if (_linkToggle)
		{
			for (WayPointNode node : getPathNodeMap().keySet())
			{
				for (WayPointNode lineNode : node.getLineNodes())
				{
					lineNode.decayMe();
					lineNode.refreshID();
					//lineNode.spawnMe();
				}
			}
		}
		else
		{
			for (WayPointNode node : getPathNodeMap().keySet())
			{
				for (WayPointNode lineNode : node.getLineNodes())
				{
					//lineNode.decayMe();
					//lineNode.refreshID();
					lineNode.spawnMe();
				}
			}
		}
        
		_linkToggle = !_linkToggle;
	}
	
	public void sendMessage(String message)
	{
		sendPacket(SystemMessage.sendString(message));
	}
	
	public void enterObserverMode(int x, int y, int z)
	{
		_obsX = getX();
		_obsY = getY();
		_obsZ = getZ();

        setTarget(null);
		stopMove(null);
		setIsParalyzed(true);
        setIsInvul(true);
		setInvisible();
		sendPacket(new ObservationMode(x, y, z));
		setXYZ(x, y, z);
        
		_observerMode = true;
		broadcastPacket(new CharInfo(this));
	}
	
	public void enterOlympiadObserverMode(int x, int y, int z, int id)
    {
        if (getPet() != null)
            getPet().unSummon(this);
    	_olympiadGameId = id;
        _obsX = getX();
        if (isSitting())
            standUp();
        _obsY = getY();
        _obsZ = getZ();
        setTarget(null);
        setIsInvul(true);
        setInvisible();
        teleToLocation(x, y, z);
        sendPacket(new ExOlympiadMode(3));
        _observerMode = true;
    }
	
	public void leaveObserverMode()
	{
		setTarget(null);
		setXYZ(_obsX, _obsY, _obsZ);
		setIsParalyzed(false);
		setVisible();
        setIsInvul(false);
        
		if (getAI() != null)
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

        _observerMode = false;
		sendPacket(new ObservationReturn(this));
		broadcastPacket(new CharInfo(this));
	}
	
	public void leaveOlympiadObserverMode()
    {
		setTarget(null);
		sendPacket(new ExOlympiadMode(0));
        teleToLocation(_obsX, _obsY, _obsZ);
        setVisible();
        setIsInvul(false);
        if (getAI() != null)
		{
            getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
        }
        Olympiad.getInstance().removeSpectator(_olympiadGameId, this);
        _olympiadGameId = -1;
        _observerMode = false;
    	broadcastPacket(new CharInfo(this));
    }
    
    public void setOlympiadSide(int i)
    {
        _olympiadSide = i;
    }
    
    public int getOlympiadSide()
    {
        return _olympiadSide;
    }
    
    public void setOlympiadGameId(int id)
    {
        _olympiadGameId = id;
    }
    
    public int getOlympiadGameId()
    {
        return _olympiadGameId;
    }
	
	public int getObsX()
	{
		return _obsX;
	}
	
	public int getObsY()
	{
		return _obsY;
	}
	
	public int getObsZ()
	{
		return _obsZ;
	}
	
	public boolean inObserverMode()
	{
		return _observerMode;
	}
	
	public int getTeleMode()
	{
		return _telemode;
	}
	
	public void setTeleMode(int mode)
	{
		_telemode = mode;
	}
	
	public void setLoto(int i, int val)
	{
		_loto[i] = val;
	}
	
	public int getLoto(int i)
	{
		return _loto[i];
	}
	
	public void setRace(int i, int val)
	{
		_race[i] = val;
	}
	
	public int getRace(int i)
	{
		return _race[i];
	}
	
	public void setChatBanned(boolean isBanned)
	{
		_chatBanned = isBanned;
		
		if (isChatBanned())
			sendMessage("You have been chat banned by a server admin.");
		else
        {
			sendMessage("Your chat ban has been lifted.");
            if (_chatUnbanTask != null)
                _chatUnbanTask.cancel(false);
            _chatUnbanTask = null;
		}
	}
	
	public boolean isChatBanned()
	{
		return _chatBanned;
	}
	
    public void setChatUnbanTask(ScheduledFuture task)
    {
         _chatUnbanTask = task;
    }
    public ScheduledFuture getChatUnbanTask()
    {
        return _chatUnbanTask;
    }
	
	public boolean getMessageRefusal()
	{
		return _messageRefusal;
	}
	
	public void setMessageRefusal(boolean mode)
	{
		_messageRefusal = mode;
        updateEffectIcons();
	}
	
	public void setDietMode(boolean mode)
	{
		_dietMode = mode;
	}
	
	public boolean getDietMode()
	{
		return _dietMode;
	}
	
	public void setTradeRefusal(boolean mode)
	{
		_tradeRefusal = mode;
	}
	
	public boolean getTradeRefusal()
	{
		return _tradeRefusal;
	}
	
	public void setExchangeRefusal(boolean mode)
	{
		_exchangeRefusal = mode;
	}
	
	public boolean getExchangeRefusal()
	{
		return _exchangeRefusal;
	}
	
	public BlockList getBlockList()
	{
		return _blockList;
	}
	
	public void setConnected(boolean connected)
	{
		_isConnected = connected;
	}
	
	public void setHero(boolean hero)
	{
		_hero = hero;
	}
	
	public void setIsInOlympiadMode(boolean b)
    {
    	_inOlympiadMode = b;
    }
    
    public boolean isHero()
    {
        return _hero;
    }
    
    public boolean isInOlympiadMode()
    {
    	return _inOlympiadMode;
    }
    
    public boolean isNoble()
    {
    	return _noble;
    }
	
    public void setNoble(boolean val)
    {
    	_noble = val;
    }
	public void setTeam(int team)
	{
		_team = team;
	}
	
	public int getTeam()
	{
		return _team;
	}
	
	public int getNameColor()
	{
		return _nameColor;
	}
	
	public void setNameColor(int nameColor)
	{
		_nameColor = nameColor;
	}
	
	public void setNameColor(int red, int green, int blue)
	{
		_nameColor = (red & 0xFF) + ((green & 0xFF) << 8) + ((blue & 0xFF) << 16);
	}
	
	public int getTitleColor()
	{
		return _titleColor;
	}
	
	public void setTitleColor(int titleColor)
	{
		_titleColor = titleColor;
	}
	
	public void setTitleColor(int red, int green, int blue)
	{
		_titleColor = (red & 0xFF) + ((green & 0xFF) << 8) + ((blue & 0xFF) << 16);
	}
	
	public void setWantsPeace(int wantsPeace)
	{
		_wantsPeace = wantsPeace;
	}
	
	public int getWantsPeace()
	{
		return _wantsPeace;
	}
    
    public boolean isFishing()
    {
        return _fishing;
    }
       
    public void setFishing(boolean fishing)
    {
        _fishing = fishing;
    }
    
    /**
     * 1. Add the specified class ID as a subclass (up to the maximum number of <b>three</b>)
     * for this character.<BR>
     * 2. This method no longer changes the active _classIndex of the player. This is only
     * done by the calling of setActiveClass() method as that should be the only way to do so.
	 *
     * @param int classId
     * @param int classIndex
     * @return boolean subclassAdded
     */
    public boolean addSubClass(int classId, int classIndex)
    {
    	if (getTotalSubClasses() == 3 || classIndex == 0) 
    		return false;
    	
    	if (getSubClasses().containsKey(classIndex))
    		return false;
    	
    	// Note: Never change _classIndex in any method other than setActiveClass().
    	
        SubClass newClass = new SubClass();
        newClass.setClassId(classId);
        newClass.setClassIndex(classIndex);
    
        java.sql.Connection con = null;

        try 
        {
            // Store the basic info about this new sub-class.
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(ADD_CHAR_SUBCLASS);
            statement.setInt(1, getObjectId());
            statement.setInt(2, newClass.getClassId());
            statement.setLong(3, newClass.getExp());
            statement.setInt(4, newClass.getSp());
            statement.setInt(5, newClass.getLevel());
            statement.setInt(6, newClass.getClassIndex()); // <-- Added
            statement.execute();
            statement.close();
        }
        catch (Exception e) {
            _log.warning("WARNING: Could not add character sub class for " + getName() + ": " + e);
            return false;
        }
        finally {
            try { con.close(); } catch (Exception e) {}
        }
        
        // Commit after database INSERT incase exception is thrown.
        getSubClasses().put(newClass.getClassIndex(), newClass);

        if (Config.DEBUG)
            _log.info(getName() + " added class ID " + classId + " as a sub class at index " + classIndex + ".");
        
		ClassId subTemplate = ClassId.values()[classId];
		List<L2SkillLearn> skillTree = SkillTreeTable.getInstance().getAllowedSkills(subTemplate);
		
		if (skillTree == null)
			return true;
        
		Map<Integer, L2Skill> prevSkillList = new FastMap<Integer, L2Skill>();

		for (L2SkillLearn skillInfo : skillTree)
		{
			if (skillInfo.getMinLevel() <= 40)
			{	
				L2Skill prevSkill = prevSkillList.get(skillInfo.getId());
				L2Skill newSkill = SkillTable.getInstance().getInfo(skillInfo.getId(), skillInfo.getLevel());

				if (prevSkill != null && (prevSkill.getLevel() > newSkill.getLevel())) 
					continue;

				prevSkillList.put(newSkill.getId(), newSkill);
				storeSkill(newSkill, prevSkill, classIndex);
			}
		}
		
        if (Config.DEBUG)
            _log.info(getName() + " was given " + getAllSkills().length + " skills for their new sub class.");
        
        return true; 
    }
    
    /**
     * 1. Completely erase all existance of the subClass linked to the classIndex.<BR>
     * 2. Send over the newClassId to addSubClass()to create a new instance on this classIndex.<BR> 
     * 3. Upon Exception, revert the player to their BaseClass to avoid further problems.<BR>
     * 
     * @param int classIndex
     * @param int newClassId
     * @return boolean subclassAdded
     */
    public boolean modifySubClass(int classIndex, int newClassId)
    {
        int oldClassId = getSubClasses().get(classIndex).getClassId();
        
        if (Config.DEBUG)
	    	_log.info(getName() + " has requested to modify sub class index " + classIndex + " from class ID " + oldClassId + " to " + newClassId + ".");
        
    	java.sql.Connection con = null;
        
        try 
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;

            // Remove all henna info stored for this sub-class.
            statement = con.prepareStatement(DELETE_CHAR_HENNAS);
            statement.setInt(1, getObjectId());
            statement.setInt(2, classIndex);
            statement.execute();
            statement.close();
            
            // Remove all shortcuts info stored for this sub-class.
            statement = con.prepareStatement(DELETE_CHAR_SHORTCUTS);
            statement.setInt(1, getObjectId());
            statement.setInt(2, classIndex);
            statement.execute();
            statement.close();

            // Remove all effects info stored for this sub-class.
            statement = con.prepareStatement(DELETE_SKILL_SAVE);
            statement.setInt(1, getObjectId());
            statement.setInt(2, classIndex);
            statement.execute();
            statement.close();
            
            // Remove all skill info stored for this sub-class.
            statement = con.prepareStatement(DELETE_CHAR_SKILLS);
            statement.setInt(1, getObjectId());
            statement.setInt(2, classIndex);
            statement.execute();
            statement.close();

            // Remove all basic info stored about this sub-class. 
            statement = con.prepareStatement(DELETE_CHAR_SUBCLASS);
            statement.setInt(1, getObjectId());
            statement.setInt(2, classIndex);
            statement.execute();
            statement.close();
        }
        catch (Exception e) 
        {
        	_log.warning("Could not modify sub class for " + getName() + " to class index " + classIndex + ": " + e);
        	
        	// This must be done in order to maintain data consistency.
            getSubClasses().remove(classIndex); 
        	return false;
        }
        finally {
            try { con.close(); } catch (Exception e) {}
        }    
        
        getSubClasses().remove(classIndex);
        return addSubClass(newClassId, classIndex);
    }
    
    public boolean isSubClassActive()
    {
        return _classIndex > 0;
    }
    
    public Map<Integer, SubClass> getSubClasses()
    {
        if (_subClasses == null)
            _subClasses = new FastMap<Integer, SubClass>();
        
        return _subClasses;
    }
    
    public int getTotalSubClasses()
    {
        return getSubClasses().size();
    }
    
	public int getBaseClass()
	{
		return _baseClass;
	}
	
	public int getActiveClass()
	{
		return _activeClass;
	}
	
	public int getClassIndex()
	{
		return _classIndex;
	}
	
    /**
     * Changes the character's class based on the given class index.
     * <BR><BR>
     * An index of zero specifies the character's original (base) class,
     * while indexes 1-3 specifies the character's sub-classes respectively.
     *  
     * @param classIndex
     */
    public boolean setActiveClass(int classIndex)
    {
        /*
         * 1. Call store() before modifying _classIndex to avoid skill effects rollover.
         * 2. Register the correct _classId against applied 'classIndex'.
         */
        store();    

        if (classIndex == 0) 
        {
            setClassId(getBaseClass());
        }
        else 
        {
            try {
                setClassId(getSubClasses().get(classIndex).getClassId());
            } 
            catch (Exception e) {
                _log.info("Could not switch " + getName() + "'s sub class to class index " + classIndex + ": " + e); 
                return false; 
            }
        }
        
        _classIndex = classIndex;
        
        /* 
		 * Update the character's change in class status.
         * 
		 * 1. Remove any active cubics from the player.
         * 2. Renovate the characters table in the database with the new class info, storing also buff/effect data.
         * 3. Remove all existing skills.
         * 4. Restore all the learned skills for the current class from the database.
         * 5. Restore effect/buff data for the new class.
         * 6. Restore henna data for the class, applying the new stat modifiers while removing existing ones.
         * 7. Reset HP/MP/CP stats and send Server->Client character status packet to reflect changes.
         * 8. Restore shortcut data related to this class.
         * 9. Resend a class change animation effect to broadcast to all nearby players.
         * 10.Unsummon any active servitor from the player.
         */
        
        if (getPet() != null && getPet() instanceof L2SummonInstance) 
        	getPet().unSummon(this);

        if (getCubics().size() > 0) 
        {
            for (L2CubicInstance cubic : getCubics().values())
            {
                cubic.stopAction();
                cubic.cancelDisappear();
            }
            
            getCubics().clear();
        }
        
        for (L2Skill oldSkill : getAllSkills())
            super.removeSkill(oldSkill);
        
        for (L2Effect effect : getAllEffects())
            effect.exit();
        
        if (isSubClassActive())
        {
            _dwarvenRecipeBook.clear();
            _commonRecipeBook.clear();
        }
        else
        {
            restoreRecipeBook();
		}
        
        restoreSkills();
        rewardSkills();
        restoreEffects();

        for (int i = 0; i < 3; i++)
            _henna[i] = null;
        
        restoreHenna();
        sendPacket(new HennaInfo(this));
        
        setCurrentHpMp(getMaxHp(), getMaxMp());
        setCurrentCp(getMaxCp());
        updateStats();

        //_macroses.restore();
        //_macroses.sendUpdate();
        _shortCuts.restore();
        sendPacket(new ShortCutInit(this));
        
        broadcastPacket(new SocialAction(getObjectId(), 15));
        
        //decayMe();
        //spawnMe(getX(), getY(), getZ());

        return true;
    }
	
	public void stopWarnUserTakeBreak()
	{
		if (_taskWarnUserTakeBreak != null)
		{
			_taskWarnUserTakeBreak.cancel(false);
			_taskWarnUserTakeBreak = null;
		}
	}
	
	public void startWarnUserTakeBreak()
	{
		if (_taskWarnUserTakeBreak == null)
			_taskWarnUserTakeBreak = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new WarnUserTakeBreak(), 7200000, 7200000);
	}
    
	public void stopRentPet()
	{
		if (_taskRentPet != null)
		{
			// if the rent of a wyvern expires while over a flying zone, tp to down before unmounting
			if (checkLandingState() && getMountType()==2)
				teleToLocation(MapRegionTable.getInstance().getTeleToLocation(this, MapRegionTable.TeleportWhereType.Town));
			_taskRentPet.cancel(false);
			Ride dismount = new Ride(getObjectId(), Ride.ACTION_DISMOUNT, 0);
			sendPacket(dismount);
			broadcastPacket(dismount);
			_taskRentPet = null;
			setMountType(0);
		}
	}
	
	public void startRentPet(int seconds)
	{
		if (_taskRentPet == null)
			_taskRentPet = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new RentPetTask(), seconds * 1000, seconds * 1000);
	}
	
	public boolean isRentedPet()
	{
		if (_taskRentPet != null)
			return true;
			
		return false;
	}
	
	public void stopWaterTask()
	{
		if (_taskWater != null)
		{
			_taskWater.cancel(false);
			
			_taskWater = null;
			sendPacket(new SetupGauge(2, 0));
		}
	}
	
	public void startWaterTask()
	{
		if (!isDead() && _taskWater == null)
		{
			int timeinwater = 86000;
			
			sendPacket(new SetupGauge(2, timeinwater));
			_taskWater = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new WaterTask(), timeinwater, 1000);
		}    
	}
	
	public boolean isInWater()
	{
		if (_taskWater != null)
			return true;
			
		return false;
	}
	
	public void checkWaterState()
	{
		//checking if char is  over base level of  water (sea, rivers)
		if (getZ() > -3793)
		{
			stopWaterTask();
			return;
		}
		
		// Check if char is in water or is underground and in water
		int[] coord;
		Zone zone = ZoneManager.getInstance().getZone(ZoneType.getZoneTypeName(ZoneType.ZoneTypeEnum.Water), getX(), getY()); //checking if char is in water zone
		
		if (zone == null) 
			zone = ZoneManager.getInstance().getZoneType(ZoneType.getZoneTypeName(ZoneType.ZoneTypeEnum.Underground)).getZone(getX(), getY()); //checking if char is in underground and in water
		
		if (zone != null)
		{
			coord = zone.getCoord(getX(), getY());
			
			if (coord != null && getZ() > coord[4])
			{
				stopWaterTask();
				return;
			}    
		}
		
		startWaterTask();
	}
	
	public void onPlayerEnter()
	{
		startWarnUserTakeBreak();
		
        if (SevenSigns.getInstance().isSealValidationPeriod() || SevenSigns.getInstance().isCompResultsPeriod())
        {
            if (!isGM() && isIn7sDungeon() && SevenSigns.getInstance().getPlayerCabal(this) != SevenSigns.getInstance().getCabalHighestScore())
            {
                teleToLocation(MapRegionTable.TeleportWhereType.Town);
                setIsIn7sDungeon(false);
                sendMessage("You have been teleported to the nearest town due to the beginning of the Seal Validation period.");
            }
        }
        else
        {
            if (!isGM() && isIn7sDungeon() && SevenSigns.getInstance().getPlayerCabal(this) == SevenSigns.CABAL_NULL)
            {
                teleToLocation(MapRegionTable.TeleportWhereType.Town);
                setIsIn7sDungeon(false);
                sendMessage("You have been teleported to the nearest town because you have not signed for any cabal.");
            }
        }
        
        // jail task
        updateJailState();

        if (_isInvul)
        	sendMessage("Entering world in Invulnerable mode.");
        if (getInvisible() == 1) 
            sendMessage("Entering world in Invisible mode.");
        if (getMessageRefusal()) 
            sendMessage("Entering world in Message Refusal mode.");
        
        // Reset Pvp Zone flag withour sending a message to the client
		setInPvpZone(false, false);
		// Reset MotherTree Zone flag withour sending a message to the client
		setInMotherTreeZone(false, false);
		revalidateZone();
	}
	
	public long getLastAccess()
	{
		return _lastAccess;
	}
	
	private void checkRecom(int recsHave, int recsLeft)
	{
		Calendar check = Calendar.getInstance();
		check.setTimeInMillis(_lastAccess);
		check.add(Calendar.DAY_OF_MONTH,1);
		
		Calendar min = Calendar.getInstance();
		min.set(Calendar.HOUR_OF_DAY, 13);
		min.set(Calendar.MINUTE, 0);
		
		Calendar max = Calendar.getInstance();
		max.add(Calendar.DAY_OF_MONTH,1);
		max.set(Calendar.HOUR_OF_DAY, 13);
		max.set(Calendar.MINUTE, 0);

		_recomHave = recsHave;
		_recomLeft = recsLeft;
		
		if (getStat().getLevel() < 10 || !(check.after(min) && check.before(max)) )
			return;
		
		restartRecom();
	}
	
	public void restartRecom()
	{
		_recomChars.clear();
		
		if (getStat().getLevel() < 20) 
        {
			_recomLeft = 3;
			_recomHave--;
		}
		else if (getStat().getLevel() < 40) 
        {
			_recomLeft = 3;
			_recomHave -= 2;
		}
		else
		{
			_recomLeft = 6;
			_recomHave -= 3;
		}
		
		if (_recomHave < 0) 
			_recomHave = 0;
	}
	
	public int getBoatId()
	{
		return _boatId;
	}
	
	public void setBoatId(int boatId)
	{
		_boatId = boatId;
	}
	
	public void doRevive()
	{
		super.doRevive();
		updateEffectIcons();
	}
	
	public void doRevive(L2Skill skill)
	{
		// Restore the player's lost experience, 
		// depending on the % return of the skill used (based on its power).
		restoreExp(skill.getPower());
		doRevive();
	}
	
	public void onActionRequest()
	{
		setProtection(false);
	}
	
	/**
	 * @param expertiseIndex The expertiseIndex to set.
	 */
	public void setExpertiseIndex(int expertiseIndex)
	{
		_expertiseIndex = expertiseIndex;
	}
	
	/**
	 * @return Returns the expertiseIndex.
	 */
	public int getExpertiseIndex()
	{
		return _expertiseIndex;
	}
	
	public final void onTeleported()
	{
		super.onTeleported();
		
		if (Config.PLAYER_SPAWN_PROTECTION > 0) 
            setProtection(true);
        
		if (Config.ALLOW_WATER) 
            checkWaterState();
	}
	
	public final boolean updatePosition(int gameTicks)
	{
		// Disables custom movement for L2PCInstance when Old Synchronization is selected
		if (Config.COORD_SYNCHRONIZE == -1) 
			return super.updatePosition(gameTicks);
		
		// Get movement data
		MoveData m = _move;
		
		if (_move == null)
			return true;
		
		if (!isVisible())
		{
			_move = null;
			return true;
		}
		
		// Check if the position has alreday be calculated
		if (m._moveTimestamp == 0)
			m._moveTimestamp = m._moveStartTime;
		
		// Check if the position has alreday be calculated
		if (m._moveTimestamp == gameTicks)
			return false;
		
		double dx = m._xDestination - getX();
		double dy = m._yDestination - getY();
		double dz = m._zDestination - getZ();
		int distPassed = (int)getStat().getMoveSpeed() * (gameTicks - m._moveTimestamp) / GameTimeController.TICKS_PER_SECOND;
		double distFraction = (distPassed) / Math.sqrt(dx*dx + dy*dy + dz*dz); 
//		if (Config.DEVELOPER) System.out.println("Move Ticks:" + (gameTicks - m._moveTimestamp) + ", distPassed:" + distPassed + ", distFraction:" + distFraction);
		
		if (distFraction > 1)
		{
			// Set the position of the L2Character to the destination
			super.setXYZ(m._xDestination, m._yDestination, m._zDestination);
			
			// Cancel the move action
			_move = null;
		}
		else
		{
			// Set the position of the L2Character to estimated after parcial move
			super.setXYZ(getX() + (int)(dx * distFraction + 0.5), getY() + (int)(dy * distFraction + 0.5), getZ() + (int)(dz * distFraction));
		}
		
		// Set the timer of last position update to now
		m._moveTimestamp = gameTicks;
		return (_move == null);
	}
	
	public void setLastClientPosition(int x, int y, int z)
	{
		_lastClientPosition.setXYZ(x,y,z);
	}
	
	public boolean checkLastClientPosition(int x, int y, int z)
	{
		return _lastClientPosition.equals(x,y,z);
	}
	
	public int getLastClientDistance(int x, int y, int z)
	{
		double dx = (x - _lastClientPosition.getX()); 
		double dy = (y - _lastClientPosition.getY()); 
		double dz = (z - _lastClientPosition.getZ()); 
        
		return (int)Math.sqrt(dx*dx + dy*dy + dz*dz);
	}
	
	public void setLastServerPosition(int x, int y, int z)
	{
		_lastServerPosition.setXYZ(x,y,z);
	}
	
	public boolean checkLastServerPosition(int x, int y, int z)
	{
		return _lastServerPosition.equals(x,y,z);
	}
	
	public int getLastServerDistance(int x, int y, int z)
	{
		double dx = (x - _lastServerPosition.getX()); 
		double dy = (y - _lastServerPosition.getY()); 
		double dz = (z - _lastServerPosition.getZ()); 
        
		return (int)Math.sqrt(dx*dx + dy*dy + dz*dz);
	}
	
	public void addExpAndSp(long addToExp, int addToSp) { getStat().addExpAndSp(addToExp, addToSp); }
    public void removeExpAndSp(long removeExp, int removeSp) { getStat().removeExpAndSp(removeExp, removeSp); }
    public void reduceCurrentHp(double i, L2Character attacker) { getStatus().reduceHp(i, attacker); }
	public void reduceCurrentHp(double value, L2Character attacker, boolean awake) { getStatus().reduceHp(value, attacker, awake); }
	
	public void broadcastSnoop(int type, String name, String _text)
	{
		if(_SnoopListener.size() > 0)
		{
			Snoop sn = new Snoop(getObjectId(),getName(),type,name,_text);
			
            for (L2PcInstance pci : _SnoopListener)
				if (pci != null)
					pci.sendPacket(sn);
		}
	}
	
	public void addSnooper(L2PcInstance pci )
	{
		if(!_SnoopListener.contains(pci))
			_SnoopListener.add(pci);
	}
	
	public void removeSnooper(L2PcInstance pci )
	{
		_SnoopListener.remove(pci);
	}
	
	public void addSnooped(L2PcInstance pci )
	{
		if(!_SnoopedPlayer.contains(pci))
			_SnoopedPlayer.add(pci);
	}
	
	public void removeSnooped(L2PcInstance pci )
	{
		_SnoopedPlayer.remove(pci);
	}
	
	public void addBypass(String bypass)
	{
		_validBypass.add(bypass);
		//_log.warning("[BypassAdd]"+getName()+" '"+bypass+"'");
	}
    
	public void addBypass2(String bypass)
	{
		_validBypass2.add(bypass);
		//_log.warning("[BypassAdd]"+getName()+" '"+bypass+"'");
	}
	
	public boolean validateBypass(String cmd)
	{
		if (!Config.BYPASS_VALIDATION)
			return true;
		
		for (String bp : _validBypass)
		{
		    if (bp == null)
		        return false;
            
			//_log.warning("[BypassValidation]"+getName()+" '"+bp+"'");
			if (bp.equals(cmd))
				return true;
		}
        
		for (String bp : _validBypass2)
		{
		    if (bp == null)
                return false;
            
			//_log.warning("[BypassValidation]"+getName()+" '"+bp+"'");
			if (cmd.startsWith(bp))
				return true;
		}
		
		_log.warning("[L2PcInstance] player ["+getName()+"] sent invalid bypass '"+cmd+"', ban this player!");
		return false;
	}
	
	public boolean validateItemManipulation(int objectId, String action)
	{
		L2ItemInstance item = getInventory().getItemByObjectId(objectId);
        
		if (item == null || item.getOwnerId() != getObjectId())
		{
			_log.finest(getObjectId()+": player tried to " + action + " item he is not owner of");
			return false;
		}
		
		// Pet is summoned and not the item that summoned the pet AND not the buggle from strider you're mounting
		if (getPet() != null && getPet().getControlItemId() == objectId || getMountObjectID() == objectId)
		{
			if (Config.DEBUG) 
				_log.finest(getObjectId()+": player tried to " + action + " item controling pet");
            
			return false;
		}
		
		if(getActiveEnchantItem() != null && getActiveEnchantItem().getObjectId() == objectId)
		{
			if (Config.DEBUG) 
				_log.finest(getObjectId()+":player tried to " + action + " an enchant scroll he was using");
            
			return false;
		}
		
		if (CursedWeaponsManager.getInstance().isCursed(item.getItemId()))
		{
			// can not trade a cursed weapon
			return false;
		}
		
		if (item.isWear())
		{
			Util.handleIllegalPlayerAction(this, "Warning!! Character "+getName()+" tried to "+action+" weared item: "+item.getObjectId(),Config.DEFAULT_PUNISH);
			return false;
		}
		
		return true;
	}
	
	public void clearBypass()
	{
        synchronized (this)
        {
            _validBypass.clear();
            _validBypass2.clear();
        }
	}
	
	/**
	 * @return Returns the inBoat.
	 */
	public boolean isInBoat()
	{
		return _inBoat;
	}
	
	/**
	 * @param inBoat The inBoat to set.
	 */
	public void setInBoat(boolean inBoat)
	{
		_inBoat = inBoat;
	}
	
	/**
	 * @return
	 */
	public L2BoatInstance getBoat()
	{		
		return _boat;
	}
	
	/**
	 * @param boat
	 */
	public void setBoat(L2BoatInstance boat)
	{
		_boat = boat;
	}
	
	public void setInCrystallize(boolean inCrystallize)
	{
		_inCrystallize = inCrystallize;
	}
	
	public boolean isInCrystallize()
	{
		return _inCrystallize;
	}
	
	/**
	 * @return
	 */
	public Point3D getInBoatPosition()
	{
		return _inBoatPosition;
	}
    
	public void setInBoatPosition(Point3D pt)
	{
		_inBoatPosition = pt;
	}
	
	/**
	 * Manage the delete task of a L2PcInstance (Leave Party, Unsummon pet, Save its inventory in the database, Remove it from the world...).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>If the L2PcInstance is in observer mode, set its position to its position before entering in observer mode </li>
	 * <li>Set the online Flag to True or False and update the characters table of the database with online status and lastAccess </li>
	 * <li>Stop the HP/MP/CP Regeneration task </li>
	 * <li>Cancel Crafting, Attak or Cast </li>
	 * <li>Remove the L2PcInstance from the world </li>
	 * <li>Stop Party and Unsummon Pet </li>
	 * <li>Update database with items in its inventory and remove them from the world </li>
	 * <li>Remove all L2Object from _knownObjects and _knownPlayer of the L2Character then cancel Attak or Cast and notify AI </li>
	 * <li>Close the connection with the client </li><BR><BR>
	 *
	 */
	public void deleteMe()
	{
		// Check if the L2PcInstance is in observer mode to set its position to its position before entering in observer mode
		if (inObserverMode())
			setXYZ(_obsX, _obsY, _obsZ);
		
		// Set the online Flag to True or False and update the characters table of the database with online status and lastAccess (called when login and logout)
		try { setOnlineStatus(false); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }
		
		// Stop the HP/MP/CP Regeneration task (scheduled tasks)
		try { stopAllTimers(); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }
		
		// Stop crafting, if in progress
		try { RecipeController.getInstance().requestMakeItemAbort(this); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }
		
		// Cancel Attak or Cast
		try { setTarget(null); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }
		
		// Remove the L2PcInstance from the world
		if (isVisible())
			try { decayMe(); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }
			
			// If a Party is in progress, leave it
			if (isInParty()) try { leaveParty(); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }
			
			// If the L2PcInstance has Pet, unsummon it
			if (getPet() != null)
			{
				try { getPet().unSummon(this); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }// returns pet to control item
			}
			
			if (getClanId() != 0 && getClan() != null)
			{
				// set the status for pledge member list to OFFLINE
				try
				{
					L2ClanMember clanMember = getClan().getClanMember(getName());
					if (clanMember != null) clanMember.setPlayerInstance(null);
				} catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }
			}
			
			if (getActiveRequester() != null)
			{
				// deals with sudden exit in the middle of transaction
				setActiveRequester(null);
			}
			
			// If the L2PcInstance is a GM, remove it from the GM List
			if (isGM())
			{
				try { GmListTable.getInstance().deleteGm(this); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }
			}
			
			// Update database with items in its inventory and remove them from the world
			try { getInventory().deleteMe(); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }

			// Update database with items in its warehouse and remove them from the world
			try { getWarehouse().deleteMe(); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }

			// Update database with items in its freight and remove them from the world
			try { getFreight().deleteMe(); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }
			
			// Remove all L2Object from _knownObjects and _knownPlayer of the L2Character then cancel Attak or Cast and notify AI
			try { getKnownList().removeAllKnownObjects(); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }
			
			// Close the connection with the client
			try { setNetConnection(null); } catch (Throwable t) {_log.log(Level.SEVERE, "deletedMe()", t); }
			
			clearPathNodes();
			
			if (getClanId() > 0)
				getClan().broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(this), this);
				//ClanTable.getInstance().getClan(getClanId()).broadcastToOnlineMembers(new PledgeShowMemberListAdd(this));
			
			for(L2PcInstance player : _SnoopedPlayer)
				player.removeSnooper(this);
			
			for(L2PcInstance player : _SnoopListener)
				player.removeSnooped(this);
			
			// Remove L2Object object from _allObjects of L2World
			L2World.getInstance().removeObject(this);
	}
	
    public void StartFishing()
    {   
        stopMove(null);
        int rnd = Rnd.get(50) + 150;        
        double angle = Util.convertHeadingToDegree(getHeading());
        //this.sendMessage("Angel: "+angle+" Heading: "+getHeading());
        double radian = Math.toRadians(angle - 90);
        double sin = Math.sin(radian);
        double cos = Math.cos(radian);      
        int x1 = -(int)(sin * rnd); //Somthing wrong with L2j Heding calculation o_0?
        int y1 = (int)(cos * rnd); //Somthing wrong with L2j Heding calculation o_0?
        int x = getX()+x1;
        int y = getY()+y1;
        int z = getZ()-30;
        _fishx = x;
        _fishy = y;
        _fishz = z;     
    
        //check the fishing floats x,y,z is in a fishing zone
        //if not the abort fishing mode else continue
        if (!ZoneManager.getInstance().checkIfInZoneFishing(x,y))
        {
            //abort fishing
            this.sendMessage("Your Lure didnt land in a fishing zone.");
            return;
        }
        this.sendMessage("Get Ready to Fish");
        this.setIsImobilised(true);
        _fishing = true;
        broadcastUserInfo();
        //Starts fishing
        sendPacket(new SystemMessage(1461));        
        ExFishingStart efs = new ExFishingStart(this,x,y,z);
        broadcastPacket(efs);       
        StartLookingForFishTask();
    }
    public void StopLookingForFishTask()
    {
        if (_taskforfish != null)
        {
            _taskforfish.cancel(false);
            _taskforfish = null;            
        }
    }
    public void StartLookingForFishTask()
    {
        if (!isDead() && _taskforfish == null)
        {
            int waittime = 30000;
            //Time for fish is dependet to lure type
            if (_lure != null)
            {
                int lureid = _lure.getItemId();
                if (lureid == 6519 || lureid == 6522 |lureid == 6525)//low grade
                    waittime = 25000;
                else if (lureid == 6520 || lureid == 6523 |lureid == 6526)//medium grade
                	waittime = 20000;
                else if (lureid == 6521 || lureid == 6524 |lureid == 6527)//high grade
                	waittime = 15000;
            }      
            _taskforfish = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new LokingForFishTask(), waittime, waittime);
        }
    }
    public void StartFishCombat()
    {                              
        _fish = new L2Fishing (this);       
    }    
    public void EndFishing(boolean win)
    {
        if (win)
        {
            //Succeeded in fishing
            sendPacket(new SystemMessage(1469));
        }       
        ExFishingEnd efe = new ExFishingEnd(win, this);
        broadcastPacket(efe);
        _fishing = false;       
        _fishx = 0;
        _fishy = 0;
        _fishz = 0;
        broadcastUserInfo();
        _fish = null;
        _lure = null;
        //Ends fishing
        sendPacket(new SystemMessage(1460));
        setIsImobilised(false);
        StopLookingForFishTask();
    }
    public L2Fishing GetFish()
    {
        return _fish;
    }
    public int GetFishx()
    {
        return _fishx;
    }
    public int GetFishy()
    {
        return _fishy;
    }
    public int GetFishz()
    {
        return _fishz;
    }
    public void SetLure (L2ItemInstance lure)
    {
        _lure = lure;
    }
    public L2ItemInstance GetLure()
    {
    return _lure;
    }   
    public int GetInventoryLimit()
    {
        int ivlim;
        if (isGM()) {           
            ivlim = Config.INVENTORY_MAXIMUM_GM;         
        }
        else if (getRace() == Race.dwarf)
        {
                ivlim = Config.INVENTORY_MAXIMUM_DWARF;
        }
        else
        {
            ivlim = Config.INVENTORY_MAXIMUM_NO_DWARF;
        }
        ivlim += (int)getStat().calcStat(Stats.INV_LIM, 0, null, null);
           
           return ivlim;
       }
       public int GetWareHouseLimit()
       {
           int whlim;
           if (getRace() == Race.dwarf){
               whlim = Config.WAREHOUSE_SLOTS_DWARF;
           }
           else{
               whlim = Config.WAREHOUSE_SLOTS_NO_DWARF;
           }
           whlim += (int)getStat().calcStat(Stats.WH_LIM, 0, null, null);
           
           return whlim;
       }
       public int GetPrivateSellStoreLimit()
       {
           int pslim;
           if (getRace() == Race.dwarf){
               pslim = Config.MAX_PVTSTORE_SLOTS_DWARF;
           }
           else{
               pslim = Config.MAX_PVTSTORE_SLOTS_OTHER;
           }
           pslim += (int)getStat().calcStat(Stats.P_SELL_LIM, 0, null, null);
           
           return pslim;
       }
       public int GetPrivateBuyStoreLimit()
       {
           int pblim;
           if (getRace() == Race.dwarf){
               pblim = Config.MAX_PVTSTORE_SLOTS_DWARF;
           }
           else
           {
               pblim = Config.MAX_PVTSTORE_SLOTS_OTHER;
           }
           pblim += (int)getStat().calcStat(Stats.P_BUY_LIM, 0, null, null);
           
           return pblim;
       }
       public int GetFreightLimit()
       {
        return Config.FREIGHT_SLOTS + (int)getStat().calcStat(Stats.FREIGHT_LIM, 0, null, null);
    }
    
    public int GetDwarfRecipeLimit()
    {
        int recdlim = Config.DWARF_RECIPE_LIMIT;
        recdlim += (int)getStat().calcStat(Stats.REC_D_LIM, 0, null, null);
        return recdlim;
    }
    
    public int GetCommonRecipeLimit()
    {
        int recclim = Config.COMMON_RECIPE_LIMIT;
        recclim += (int)getStat().calcStat(Stats.REC_C_LIM, 0, null, null);
        return recclim;
    }

	public void setMountObjectID(int newID) 
	{ 
	    _mountObjectID = newID; 
	}
	
	public int getMountObjectID() 
	{ 
	    return _mountObjectID; 
	}
    
    private L2ItemInstance _lure = null;
    
    /**
     * Get the current skill in use or return null.<BR><BR>
     * 
     */
    public SkillDat getCurrentSkill()
    {
        return _currentSkill;
    }
    
    
    /**
     * Create a new SkillDat object and set the player _currentSkill.<BR><BR>
     * 
     */ 
    public void setCurrentSkill(L2Skill currentSkill, boolean ctrlPressed, 
                                boolean shiftPressed)
    {
        if (currentSkill == null)
        {
            if (Config.DEBUG)
                _log.info("Setting current skill: NULL for " + getName() + ".");
            
            _currentSkill = null;
            return;
        }

        if (Config.DEBUG)
            _log.info("Setting current skill: " + currentSkill.getName() + " (ID: " + currentSkill.getId() + ") for " + getName() + ".");
        
        _currentSkill = new SkillDat(currentSkill, ctrlPressed, shiftPressed);
    }

    public SkillDat getQueuedSkill()
    {
        return _queuedSkill;
    }
    
    
    /**
     * Create a new SkillDat object and queue it in the player _queuedSkill.<BR><BR>
     * 
     */ 
    public void setQueuedSkill(L2Skill queuedSkill, boolean ctrlPressed, boolean shiftPressed)
    {
        if (queuedSkill == null)
        {
            if (Config.DEBUG)
                _log.info("Setting queued skill: NULL for " + getName() + ".");
            
            _queuedSkill = null;
            return;
        }
        
        if (Config.DEBUG)
            _log.info("Setting queued skill: " + queuedSkill.getName() + " (ID: " + queuedSkill.getId() + ") for " + getName() + ".");
        
        _queuedSkill = new SkillDat(queuedSkill, ctrlPressed, shiftPressed);
    }

    public boolean isInJail()
    {
        return _inJail;
    }

    public void setInJail(boolean state)
    {
        _inJail = state;
    }
    
    public void setInJail(boolean state, int delayInMinutes)
    {
        _inJail = state;
        _jailTimer = 0;
        // Remove the task if any
        stopJailTask(false);
        
        if (_inJail)
        {
            if (delayInMinutes > 0)
            {
                _jailTimer = delayInMinutes * 60000; // in millisec

                // start the countdown
                _jailTask = ThreadPoolManager.getInstance().scheduleGeneral(new JailTask(this), _jailTimer);
                sendMessage("You are in jail for "+delayInMinutes+" minutes.");
            }

            // Open a Html message to inform the player
            NpcHtmlMessage htmlMsg = new NpcHtmlMessage(0);
            String jailInfos = HtmCache.getInstance().getHtm("data/html/jail_in.htm");
            if (jailInfos != null)
                htmlMsg.setHtml(jailInfos);
            else
                htmlMsg.setHtml("<html><body>You have been put in jail by an admin.</body></html>");
            sendPacket(htmlMsg);

            teleToLocation(-114356, -249645, -2984);  // Jail
        } else
        {
            // Open a Html message to inform the player
            NpcHtmlMessage htmlMsg = new NpcHtmlMessage(0);
            String jailInfos = HtmCache.getInstance().getHtm("data/html/jail_out.htm");
            if (jailInfos != null)
                htmlMsg.setHtml(jailInfos);
            else
                htmlMsg.setHtml("<html><body>You are free for now, respect server rules!</body></html>");
            sendPacket(htmlMsg);
            
            teleToLocation(17836, 170178, -3507);  // Floran
        }
        
        // store in database
        storeCharBase();
    }

    public long getJailTimer()
    {
        return _jailTimer;
    }

    public void setJailTimer(long time)
    {
        _jailTimer = time;
    }

    private void updateJailState()
    {
        if (isInJail())
        {
            // If jail time is elapsed, free the player
            if (_jailTimer > 0)
            {
                // restart the countdown
                _jailTask = ThreadPoolManager.getInstance().scheduleGeneral(new JailTask(this), _jailTimer);
                sendMessage("You are still in jail for "+Math.round(_jailTimer/60000)+" minutes.");
            }
            
            // If player escaped, put him back in jail
            if (!ZoneManager.getInstance().checkIfInZone(ZoneType.getZoneTypeName(ZoneType.ZoneTypeEnum.Jail), this))
                teleToLocation(-114356,-249645,-2984);
        }
    }

    public void stopJailTask(boolean save)
    {
        if (_jailTask != null)
        {
            if (save)
            {
            	long delay = _jailTask.getDelay(TimeUnit.MILLISECONDS);
            	if (delay < 0)
            		delay = 0;
            	setJailTimer(delay);
            }
            _jailTask.cancel(false);
            _jailTask = null;
        }
    }

    private class JailTask implements Runnable
    {
        L2PcInstance _player;
        protected long _startedAt;
        
        protected JailTask(L2PcInstance player)
        {
            _player = player;
            _startedAt = System.currentTimeMillis();
        }

        public void run() 
        {
            _player.setInJail(false, 0);
        }
    }

	/**
	 * @return
	 */
	public int getPowerGrade()
	{
		return _powerGrade;
	}
	
	/**
	 * @return
	 */
	public void setPowerGrade(int power)
	{
		_powerGrade = power;
	}
	
	public boolean isCursedWeaponEquiped()
	{
		return _cursedWeaponEquipedId != 0;
	}
	
	public void setCursedWeaponEquipedId(int value)
	{
		_cursedWeaponEquipedId = value;
	}
	
	public int getCursedWeaponEquipedId()
	{
		return _cursedWeaponEquipedId;
	}
}
