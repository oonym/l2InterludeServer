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
package net.sf.l2j.gameserver.skills;

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.SevenSigns;
import net.sf.l2j.gameserver.SevenSignsFestival;
import net.sf.l2j.gameserver.SkillTable;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2SiegeClan;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2RaidBossInstance;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.ConditionPlayerState.CheckPlayerState;
import net.sf.l2j.gameserver.templates.L2Armor;
import net.sf.l2j.gameserver.templates.L2PcTemplate;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.gameserver.util.Util;

/**
 * Global calculations, can be modified by server admins
 */
public final class Formulas
{

	/** Regen Task period */
	protected static final Logger _log = Logger.getLogger(L2Character.class.getName());
	private static final int HP_REGENERATE_PERIOD = 3000; // 3 secs

	public static int MAX_STAT_VALUE = 100;

    private static final double[] STRCompute = new double[]{1.036, 34.845}; //{1.016, 28.515}; for C1
    private static final double[] INTCompute = new double[]{1.020, 31.375}; //{1.020, 31.375}; for C1
    private static final double[] DEXCompute = new double[]{1.009, 19.360}; //{1.009, 19.360}; for C1
    private static final double[] WITCompute = new double[]{1.050, 20.000}; //{1.050, 20.000}; for C1
    private static final double[] CONCompute = new double[]{1.030, 27.632}; //{1.015, 12.488}; for C1
    private static final double[] MENCompute = new double[]{1.010, -0.060}; //{1.010, -0.060}; for C1

    protected static final double[] WITbonus = new double[MAX_STAT_VALUE];
    protected static final double[] MENbonus = new double[MAX_STAT_VALUE];
    protected static final double[] INTbonus = new double[MAX_STAT_VALUE];
    protected static final double[] STRbonus = new double[MAX_STAT_VALUE];
    protected static final double[] DEXbonus = new double[MAX_STAT_VALUE];
    protected static final double[] CONbonus = new double[MAX_STAT_VALUE];
    
    // These values are 100% matching retail tables, no need to change and no need add 
    // calculation into the stat bonus when accessing (not efficient),
    // better to have everything precalculated and use values directly (saves CPU)
    static
    {
        for (int i=0; i < STRbonus.length; i++)
            STRbonus[i] = Math.floor(Math.pow(STRCompute[0], i - STRCompute[1]) *100 +.5d) /100;
        for (int i=0; i < INTbonus.length; i++)
            INTbonus[i] = Math.floor(Math.pow(INTCompute[0], i - INTCompute[1]) *100 +.5d) /100;
        for (int i=0; i < DEXbonus.length; i++)
            DEXbonus[i] = Math.floor(Math.pow(DEXCompute[0], i - DEXCompute[1]) *100 +.5d) /100;
        for (int i=0; i < WITbonus.length; i++)
            WITbonus[i] = Math.floor(Math.pow(WITCompute[0], i - WITCompute[1]) *100 +.5d) /100;
        for (int i=0; i < CONbonus.length; i++)
            CONbonus[i] = Math.floor(Math.pow(CONCompute[0], i - CONCompute[1]) *100 +.5d) /100;
        for (int i=0; i < MENbonus.length; i++)
            MENbonus[i] = Math.floor(Math.pow(MENCompute[0], i - MENCompute[1]) *100 +.5d) /100;
    }

	static class FuncAddLevel3 extends Func
	{
		static final FuncAddLevel3[] _instancies = new FuncAddLevel3[Stats.NUM_STATS];

		static Func getInstance(Stats stat)
		{
			int pos = stat.ordinal();
			if (_instancies[pos] == null) _instancies[pos] = new FuncAddLevel3(stat);
			return _instancies[pos];
		}

		private FuncAddLevel3(Stats stat)
		{
			super(stat, 0x10, null);
		}

		public void calc(Env env)
		{
			env.value += env._player.getLevel() / 3;
		}
	}

	static class FuncMultLevelMod extends Func
	{
		static final FuncMultLevelMod[] _instancies = new FuncMultLevelMod[Stats.NUM_STATS];

		static Func getInstance(Stats stat)
		{
			int pos = stat.ordinal();
			if (_instancies[pos] == null) _instancies[pos] = new FuncMultLevelMod(stat);
			return _instancies[pos];
		}

		private FuncMultLevelMod(Stats stat)
		{
			super(stat, 0x20, null);
		}

		public void calc(Env env)
		{
			env.value *= env._player.getLevelMod();
		}
	}

	static class FuncMultRegenResting extends Func
	{
		static final FuncMultRegenResting[] _instancies = new FuncMultRegenResting[Stats.NUM_STATS];

		/**
		 * Return the Func object corresponding to the state concerned.<BR><BR>
		 */
		static Func getInstance(Stats stat)
		{
			int pos = stat.ordinal();

			if (_instancies[pos] == null) _instancies[pos] = new FuncMultRegenResting(stat);

			return _instancies[pos];
		}

		/**
		 * Constructor of the FuncMultRegenResting.<BR><BR>
		 */
		private FuncMultRegenResting(Stats stat)
		{
			super(stat, 0x20, null);
			setCondition(new ConditionPlayerState(CheckPlayerState.RESTING, true));
		}

		/**
		 * Calculate the modifier of the state concerned.<BR><BR>
		 */
		public void calc(Env env)
		{
			if (!_cond.test(env)) return;

			env.value *= 1.45;
		}
	}

	static class FuncPAtkMod extends Func
	{
		static final FuncPAtkMod _fpa_instance = new FuncPAtkMod();

		static Func getInstance()
		{
			return _fpa_instance;
		}

		private FuncPAtkMod()
		{
			super(Stats.POWER_ATTACK, 0x30, null);
		}

		public void calc(Env env)
		{
			env.value *= STRbonus[env._player.getSTR()] * env._player.getLevelMod();
		}
	}

	static class FuncMAtkMod extends Func
	{
		static final FuncMAtkMod _fma_instance = new FuncMAtkMod();

		static Func getInstance()
		{
			return _fma_instance;
		}

		private FuncMAtkMod()
		{
			super(Stats.MAGIC_ATTACK, 0x20, null);
		}

		public void calc(Env env)
		{
			double intb = INTbonus[env._player.getINT()];
			double lvlb = env._player.getLevelMod();
			env.value *= (lvlb * lvlb) * (intb * intb);
		}
	}

	static class FuncMDefMod extends Func
	{
		static final FuncMDefMod _fmm_instance = new FuncMDefMod();

		static Func getInstance()
		{
			return _fmm_instance;
		}

		private FuncMDefMod()
		{
			super(Stats.MAGIC_DEFENCE, 0x20, null);
		}

		public void calc(Env env)
		{
            if (env._player instanceof L2PcInstance)
            {
    			L2PcInstance p = (L2PcInstance) env._player;
    			if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LFINGER) != null) env.value -= 5;
    			if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RFINGER) != null) env.value -= 5;
    			if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEAR) != null) env.value -= 9;
    			if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_REAR) != null) env.value -= 9;
    			if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_NECK) != null) env.value -= 13;
            }
			env.value *= MENbonus[env._player.getMEN()] * env._player.getLevelMod();
		}
	}

	static class FuncPDefMod extends Func
	{
		static final FuncPDefMod _fmm_instance = new FuncPDefMod();

		static Func getInstance()
		{
			return _fmm_instance;
		}

		private FuncPDefMod()
		{
			super(Stats.POWER_DEFENCE, 0x20, null);
		}

		public void calc(Env env)
		{
			if (env._player instanceof L2PcInstance)
            {
                L2PcInstance p = (L2PcInstance) env._player;
    			if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_HEAD) != null) env.value -= 12;
    			if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST) != null)
    				env.value -= ((p.getClassId().isMage()) ? 15 : 31);
    			if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS) != null)
    				env.value -= ((p.getClassId().isMage()) ? 8 : 18);
    			if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_GLOVES) != null) env.value -= 8;
    			if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_FEET) != null) env.value -= 7;
            }
			env.value *= env._player.getLevelMod();
		}
	}

	static class FuncBowAtkRange extends Func
	{
		private static final FuncBowAtkRange _fbar_instance = new FuncBowAtkRange();

		static Func getInstance()
		{
			return _fbar_instance;
		}

		private FuncBowAtkRange()
		{
			super(Stats.POWER_ATTACK_RANGE, 0x10, null);
			setCondition(new ConditionUsingItemType(L2WeaponType.BOW.mask()));
		}

		public void calc(Env env)
		{
			if (!_cond.test(env)) return;
			env.value += 450;
		}
	}

	static class FuncAtkAccuracy extends Func
	{
		static final FuncAtkAccuracy _faa_instance = new FuncAtkAccuracy();

		static Func getInstance()
		{
			return _faa_instance;
		}

		private FuncAtkAccuracy()
		{
			super(Stats.ACCURACY_COMBAT, 0x10, null);
		}

		public void calc(Env env)
		{
			L2Character p = env._player;
			//[Square(DEX)]*6 + lvl + weapon hitbonus;
			env.value += Math.sqrt(p.getDEX()) * 6;
			env.value += p.getLevel();
			if( p instanceof L2Summon) env.value += (p.getLevel() < 60) ? 4 : 5;
		}
	}

	static class FuncAtkEvasion extends Func
	{
		static final FuncAtkEvasion _fae_instance = new FuncAtkEvasion();

		static Func getInstance()
		{
			return _fae_instance;
		}

		private FuncAtkEvasion()
		{
			super(Stats.EVASION_RATE, 0x10, null);
		}

		public void calc(Env env)
		{
			L2Character p = env._player;
			//[Square(DEX)]*6 + lvl;
			env.value += Math.sqrt(p.getDEX()) * 6;
			env.value += p.getLevel();
		}
	}

	static class FuncAtkCritical extends Func
	{
		static final FuncAtkCritical _fac_instance = new FuncAtkCritical();

		static Func getInstance()
		{
			return _fac_instance;
		}

		private FuncAtkCritical()
		{
			super(Stats.CRITICAL_RATE, 0x30, null);
		}

		public void calc(Env env)
		{
			L2Character p = env._player;
			if( p instanceof L2Summon) env.value = 40;
			else if (p instanceof L2PcInstance && p.getActiveWeaponInstance() == null) env.value = 40;
			else
			{
				env.value *= DEXbonus[p.getDEX()];
				env.value *= 10;
			}
		}
	}

	static class FuncMoveSpeed extends Func
	{
		static final FuncMoveSpeed _fms_instance = new FuncMoveSpeed();

		static Func getInstance()
		{
			return _fms_instance;
		}

		private FuncMoveSpeed()
		{
			super(Stats.RUN_SPEED, 0x30, null);
		}

		public void calc(Env env)
		{
			L2PcInstance p = (L2PcInstance) env._player;
			env.value *= DEXbonus[p.getDEX()];
		}
	}

	static class FuncPAtkSpeed extends Func
	{
		static final FuncPAtkSpeed _fas_instance = new FuncPAtkSpeed();

		static Func getInstance()
		{
			return _fas_instance;
		}

		private FuncPAtkSpeed()
		{
			super(Stats.POWER_ATTACK_SPEED, 0x20, null);
		}

		public void calc(Env env)
		{
			L2PcInstance p = (L2PcInstance) env._player;
			env.value *= DEXbonus[p.getDEX()];
		}
	}

	static class FuncMAtkSpeed extends Func
	{
		static final FuncMAtkSpeed _fas_instance = new FuncMAtkSpeed();

		static Func getInstance()
		{
			return _fas_instance;
		}

		private FuncMAtkSpeed()
		{
			super(Stats.MAGIC_ATTACK_SPEED, 0x20, null);
		}

		public void calc(Env env)
		{
			L2PcInstance p = (L2PcInstance) env._player;
			env.value *= WITbonus[p.getWIT()];
		}
	}

	static class FuncHennaSTR extends Func
	{
		static final FuncHennaSTR _fh_instance = new FuncHennaSTR();

		static Func getInstance()
		{
			return _fh_instance;
		}

		private FuncHennaSTR()
		{
			super(Stats.STAT_STR, 0x10, null);
		}

		public void calc(Env env)
		{
			//			L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
			L2PcInstance pc = (L2PcInstance) env._player;
			if (pc != null) env.value += pc.getHennaStatSTR();
		}
	}

	static class FuncHennaDEX extends Func
	{
		static final FuncHennaDEX _fh_instance = new FuncHennaDEX();

		static Func getInstance()
		{
			return _fh_instance;
		}

		private FuncHennaDEX()
		{
			super(Stats.STAT_DEX, 0x10, null);
		}

		public void calc(Env env)
		{
			//			L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
			L2PcInstance pc = (L2PcInstance) env._player;
			if (pc != null) env.value += pc.getHennaStatDEX();
		}
	}

	static class FuncHennaINT extends Func
	{
		static final FuncHennaINT _fh_instance = new FuncHennaINT();

		static Func getInstance()
		{
			return _fh_instance;
		}

		private FuncHennaINT()
		{
			super(Stats.STAT_INT, 0x10, null);
		}

		public void calc(Env env)
		{
			//			L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
			L2PcInstance pc = (L2PcInstance) env._player;
			if (pc != null) env.value += pc.getHennaStatINT();
		}
	}

	static class FuncHennaMEN extends Func
	{
		static final FuncHennaMEN _fh_instance = new FuncHennaMEN();

		static Func getInstance()
		{
			return _fh_instance;
		}

		private FuncHennaMEN()
		{
			super(Stats.STAT_MEN, 0x10, null);
		}

		public void calc(Env env)
		{
			//			L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
			L2PcInstance pc = (L2PcInstance) env._player;
			if (pc != null) env.value += pc.getHennaStatMEN();
		}
	}

	static class FuncHennaCON extends Func
	{
		static final FuncHennaCON _fh_instance = new FuncHennaCON();

		static Func getInstance()
		{
			return _fh_instance;
		}

		private FuncHennaCON()
		{
			super(Stats.STAT_CON, 0x10, null);
		}

		public void calc(Env env)
		{
			//			L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
			L2PcInstance pc = (L2PcInstance) env._player;
			if (pc != null) env.value += pc.getHennaStatCON();
		}
	}

	static class FuncHennaWIT extends Func
	{
		static final FuncHennaWIT _fh_instance = new FuncHennaWIT();

		static Func getInstance()
		{
			return _fh_instance;
		}

		private FuncHennaWIT()
		{
			super(Stats.STAT_WIT, 0x10, null);
		}

		public void calc(Env env)
		{
			//			L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
			L2PcInstance pc = (L2PcInstance) env._player;
			if (pc != null) env.value += pc.getHennaStatWIT();
		}
	}

	static class FuncMaxHpAdd extends Func
	{
		static final FuncMaxHpAdd _fmha_instance = new FuncMaxHpAdd();

		static Func getInstance()
		{
			return _fmha_instance;
		}

		private FuncMaxHpAdd()
		{
			super(Stats.MAX_HP, 0x10, null);
		}

		public void calc(Env env)
		{
			L2PcTemplate t = (L2PcTemplate) env._player.getTemplate();
			int lvl = env._player.getLevel() - t.classBaseLevel;
			double hpmod = t.lvlHpMod * lvl;
			double hpmax = (t.lvlHpAdd + hpmod) * lvl;
			double hpmin = (t.lvlHpAdd * lvl) + hpmod;
			env.value += (hpmax + hpmin) / 2;
		}
	}

	static class FuncMaxHpMul extends Func
	{
		static final FuncMaxHpMul _fmhm_instance = new FuncMaxHpMul();

		static Func getInstance()
		{
			return _fmhm_instance;
		}

		private FuncMaxHpMul()
		{
			super(Stats.MAX_HP, 0x20, null);
		}

		public void calc(Env env)
		{
			L2PcInstance p = (L2PcInstance) env._player;
			env.value *= CONbonus[p.getCON()];
		}
	}

	static class FuncMaxCpAdd extends Func
	{
		static final FuncMaxCpAdd _fmca_instance = new FuncMaxCpAdd();

		static Func getInstance()
		{
			return _fmca_instance;
		}

		private FuncMaxCpAdd()
		{
			super(Stats.MAX_CP, 0x10, null);
		}

		public void calc(Env env)
		{
			L2PcTemplate t = (L2PcTemplate) env._player.getTemplate();
			int lvl = env._player.getLevel() - t.classBaseLevel;
			double cpmod = t.lvlCpMod * lvl;
			double cpmax = (t.lvlCpAdd + cpmod) * lvl;
			double cpmin = (t.lvlCpAdd * lvl) + cpmod;
			env.value += (cpmax + cpmin) / 2;
		}
	}

	static class FuncMaxCpMul extends Func
	{
		static final FuncMaxCpMul _fmcm_instance = new FuncMaxCpMul();

		static Func getInstance()
		{
			return _fmcm_instance;
		}

		private FuncMaxCpMul()
		{
			super(Stats.MAX_CP, 0x20, null);
		}

		public void calc(Env env)
		{
			L2PcInstance p = (L2PcInstance) env._player;
			env.value *= CONbonus[p.getCON()];
		}
	}

	static class FuncMaxMpAdd extends Func
	{
		static final FuncMaxMpAdd _fmma_instance = new FuncMaxMpAdd();

		static Func getInstance()
		{
			return _fmma_instance;
		}

		private FuncMaxMpAdd()
		{
			super(Stats.MAX_MP, 0x10, null);
		}

		public void calc(Env env)
		{
			L2PcTemplate t = (L2PcTemplate) env._player.getTemplate();
			int lvl = env._player.getLevel() - t.classBaseLevel;
			double mpmod = t.lvlMpMod * lvl;
			double mpmax = (t.lvlMpAdd + mpmod) * lvl;
			double mpmin = (t.lvlMpAdd * lvl) + mpmod;
			env.value += (mpmax + mpmin) / 2;
		}
	}

	static class FuncMaxMpMul extends Func
	{
		static final FuncMaxMpMul _fmmm_instance = new FuncMaxMpMul();

		static Func getInstance()
		{
			return _fmmm_instance;
		}

		private FuncMaxMpMul()
		{
			super(Stats.MAX_MP, 0x20, null);
		}

		public void calc(Env env)
		{
			L2PcInstance p = (L2PcInstance) env._player;
			env.value *= MENbonus[p.getMEN()];
		}
	}

	private static final Formulas _instance = new Formulas();

	public static Formulas getInstance()
	{
		return _instance;
	}

	private Formulas()
	{
	}

	/**
	 * Return the period between 2 regenerations task (3s for L2Character, 5 min for L2DoorInstance).<BR><BR>
	 */
	public int getRegeneratePeriod(L2Character cha)
	{
		if (cha instanceof L2DoorInstance) return HP_REGENERATE_PERIOD * 100; // 5 mins

		return HP_REGENERATE_PERIOD; // 3s
	}

	/**
	 * Return the standard NPC Calculator set containing ACCURACY_COMBAT and EVASION_RATE.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A calculator is created to manage and dynamically calculate the effect of a character property (ex : MAX_HP, REGENERATE_HP_RATE...).
	 * In fact, each calculator is a table of Func object in which each Func represents a mathematic function : <BR><BR>
	 *
	 * FuncAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()<BR><BR>
	 *
	 * To reduce cache memory use, L2NPCInstances who don't have skills share the same Calculator set called <B>NPC_STD_CALCULATOR</B>.<BR><BR>
	 *
	 */
	public Calculator[] getStdNPCCalculators()
	{
		Calculator[] std = new Calculator[Stats.NUM_STATS];

		// Add the FuncAtkAccuracy to the Standard Calculator of ACCURACY_COMBAT
		std[Stats.ACCURACY_COMBAT.ordinal()] = new Calculator();
		std[Stats.ACCURACY_COMBAT.ordinal()].addFunc(FuncAtkAccuracy.getInstance());

		// Add the FuncAtkEvasion to the Standard Calculator of EVASION_RATE
		std[Stats.EVASION_RATE.ordinal()] = new Calculator();
		std[Stats.EVASION_RATE.ordinal()].addFunc(FuncAtkEvasion.getInstance());

		return std;
	}

	/**
	 * Add basics Func objects to L2PcInstance and L2Summon.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A calculator is created to manage and dynamically calculate the effect of a character property (ex : MAX_HP, REGENERATE_HP_RATE...).
	 * In fact, each calculator is a table of Func object in which each Func represents a mathematic function : <BR><BR>
	 *
	 * FuncAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()<BR><BR>
	 *
	 * @param cha L2PcInstance or L2Summon that must obtain basic Func objects
	 */
	public void addFuncsToNewCharacter(L2Character cha)
	{
		if (cha instanceof L2PcInstance)
		{
			cha.addStatFunc(FuncMaxHpAdd.getInstance());
			cha.addStatFunc(FuncMaxHpMul.getInstance());
			cha.addStatFunc(FuncMaxCpAdd.getInstance());
			cha.addStatFunc(FuncMaxCpMul.getInstance());
			cha.addStatFunc(FuncMaxMpAdd.getInstance());
			cha.addStatFunc(FuncMaxMpMul.getInstance());
			//cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_HP_RATE));
			//cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_CP_RATE));
			//cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_MP_RATE));
			cha.addStatFunc(FuncBowAtkRange.getInstance());
			//cha.addStatFunc(FuncMultLevelMod.getInstance(Stats.POWER_ATTACK));
			//cha.addStatFunc(FuncMultLevelMod.getInstance(Stats.POWER_DEFENCE));
			//cha.addStatFunc(FuncMultLevelMod.getInstance(Stats.MAGIC_DEFENCE));
			cha.addStatFunc(FuncPAtkMod.getInstance());
			cha.addStatFunc(FuncMAtkMod.getInstance());
            cha.addStatFunc(FuncPDefMod.getInstance());
			cha.addStatFunc(FuncMDefMod.getInstance());
			cha.addStatFunc(FuncAtkCritical.getInstance());
			cha.addStatFunc(FuncAtkAccuracy.getInstance());
			cha.addStatFunc(FuncAtkEvasion.getInstance());
			cha.addStatFunc(FuncPAtkSpeed.getInstance());
			cha.addStatFunc(FuncMAtkSpeed.getInstance());
			cha.addStatFunc(FuncMoveSpeed.getInstance());

			cha.addStatFunc(FuncHennaSTR.getInstance());
			cha.addStatFunc(FuncHennaDEX.getInstance());
			cha.addStatFunc(FuncHennaINT.getInstance());
			cha.addStatFunc(FuncHennaMEN.getInstance());
			cha.addStatFunc(FuncHennaCON.getInstance());
			cha.addStatFunc(FuncHennaWIT.getInstance());
		}
        else if (cha instanceof L2PetInstance)
        {
            cha.addStatFunc(FuncPAtkMod.getInstance());
            cha.addStatFunc(FuncMAtkMod.getInstance());
            cha.addStatFunc(FuncPDefMod.getInstance());
            cha.addStatFunc(FuncMDefMod.getInstance());
        }
		else if (cha instanceof L2Summon)
		{
			//cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_HP_RATE));
			//cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_MP_RATE));
			cha.addStatFunc(FuncAtkCritical.getInstance());
			cha.addStatFunc(FuncAtkAccuracy.getInstance());
			cha.addStatFunc(FuncAtkEvasion.getInstance());
		}

	}

	/**
	 * Calculate the HP regen rate (base + modifiers).<BR><BR>
	 */
	public final double calcHpRegen(L2Character cha)
	{
        double init = cha.getTemplate().baseHpReg;
		double hpRegenMultiplier = cha.isRaid() ? Config.RAID_HP_REGEN_MULTIPLIER : Config.HP_REGEN_MULTIPLIER;
		double hpRegenBonus = 0;

		if (cha instanceof L2PcInstance)
		{
            L2PcInstance player = (L2PcInstance) cha;

            // Calculate correct baseHpReg value for certain level of PC
            init += (player.getLevel() > 10) ? ((player.getLevel()-1)/10) : 0.5;
            
            // SevenSigns Festival modifier
			if (SevenSignsFestival.getInstance().isFestivalInProgress() && player.isFestivalParticipant()) 
                hpRegenMultiplier *= calcFestivalRegenModifier(player);
			else
			{
				double siegeModifier = this.calcSiegeRegenModifer(player);
				if (siegeModifier > 0) hpRegenMultiplier *= siegeModifier;
			}

			// Mother Tree effect is calculated at last
			if (player.getInMotherTreeZone()) hpRegenBonus += 2;

            // Calculate Movement bonus
            if (player.isSitting()) hpRegenMultiplier *= 1.5;      // Sitting
            else if (!player.isMoving()) hpRegenMultiplier *= 1.1; // Staying
            else if (player.isRunning()) hpRegenMultiplier *= 0.7; // Running
		}

        init *= cha.getLevelMod() * CONbonus[cha.getCON()];
        if (init < 1) init = 1;

        return cha.calcStat(Stats.REGENERATE_HP_RATE, init, null, null) * (hpRegenMultiplier / 100) + hpRegenBonus;
	}

	/**
	 * Calculate the MP regen rate (base + modifiers).<BR><BR>
	 */
	public final double calcMpRegen(L2Character cha)
	{
        double init = cha.getTemplate().baseMpReg;
        double mpRegenMultiplier = cha.isRaid() ? Config.RAID_MP_REGEN_MULTIPLIER : Config.MP_REGEN_MULTIPLIER;
		double mpRegenBonus = 0;

		if (cha instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) cha;

            // Calculate correct baseMpReg value for certain level of PC
            init += 0.3*((player.getLevel()-1)/10);
			
            // SevenSigns Festival modifier
            if (SevenSignsFestival.getInstance().isFestivalInProgress() && player.isFestivalParticipant())
				mpRegenMultiplier *= calcFestivalRegenModifier(player);

			// Mother Tree effect is calculated at last
			if (player.getInMotherTreeZone()) mpRegenBonus += 1;

			// Calculate Movement bonus
            if (player.isSitting()) mpRegenMultiplier *= 1.5;      // Sitting
            else if (!player.isMoving()) mpRegenMultiplier *= 1.1; // Staying
            else if (player.isRunning()) mpRegenMultiplier *= 0.7; // Running
		}

		init *= cha.getLevelMod() * MENbonus[cha.getMEN()];
		if (init < 1) init = 1;

		return cha.calcStat(Stats.REGENERATE_MP_RATE, init, null, null) * (mpRegenMultiplier / 100) + mpRegenBonus;
	}

	/**
	 * Calculate the CP regen rate (base + modifiers).<BR><BR>
	 */
	public final double calcCpRegen(L2Character cha)
	{
        double init = cha.getTemplate().baseHpReg;
        double cpRegenMultiplier = Config.CP_REGEN_MULTIPLIER;
        double cpRegenBonus = 0;

        L2PcInstance player = (L2PcInstance) cha;

        // Calculate correct baseHpReg value for certain level of PC
        init += (player.getLevel() > 10) ? ((player.getLevel()-1)/10) : 0.5;
        
        // Calculate Movement bonus
        if (player.isSitting()) cpRegenMultiplier *= 1.5;      // Sitting
        else if (!player.isMoving()) cpRegenMultiplier *= 1.1; // Staying
        else if (player.isRunning()) cpRegenMultiplier *= 0.7; // Running

        // Apply CON bonus
        init *= cha.getLevelMod() * CONbonus[cha.getCON()];
        if (init < 1) init = 1;

        return cha.calcStat(Stats.REGENERATE_CP_RATE, init, null, null) * (cpRegenMultiplier / 100) + cpRegenBonus;
	}

	@SuppressWarnings("deprecation")
	public final double calcFestivalRegenModifier(L2PcInstance activeChar)
	{
		final int[] festivalInfo = SevenSignsFestival.getInstance().getFestivalForPlayer(activeChar);
		final int oracle = festivalInfo[0];
		final int festivalId = festivalInfo[1];
		int[] festivalCenter;

		// If the player isn't found in the festival, leave the regen rate as it is.
		if (festivalId < 0) return 0;

		// Retrieve the X and Y coords for the center of the festival arena the player is in.
		if (oracle == SevenSigns.CABAL_DAWN) festivalCenter = SevenSignsFestival.festivalDawnPlayerSpawns[festivalId];
		else festivalCenter = SevenSignsFestival.festivalDuskPlayerSpawns[festivalId];

		// Check the distance between the player and the player spawn point, in the center of the arena.
		double distToCenter = activeChar.getDistance(festivalCenter[0], festivalCenter[1]);

		if (Config.DEBUG)
			_log.info("Distance: " + distToCenter + ", RegenMulti: " + (distToCenter * 2.5) / 50);

		return 1.0 - (distToCenter * 0.0005); // Maximum Decreased Regen of ~ -65%;
	}

	public final double calcSiegeRegenModifer(L2PcInstance activeChar)
	{
		if (activeChar == null || activeChar.getClan() == null) return 0;

		Siege siege = SiegeManager.getInstance().getSiege(activeChar.getPosition().getX(),
															activeChar.getPosition().getY());
		if (siege == null || !siege.getIsInProgress()) return 0;

		L2SiegeClan siegeClan = siege.getAttackerClan(activeChar.getClan().getClanId());
		if (siegeClan == null || siegeClan.getFlag().size() == 0
			|| !Util.checkIfInRange(200, activeChar, siegeClan.getFlag().get(0), true)) return 0;

		return 1.5; // If all is true, then modifer will be 50% more
	}

	/** Calculated damage caused by ATTACK of attacker on target,
	 * called separatly for each weapon, if dual-weapon is used.
	 *
	 * @param attacker player or NPC that makes ATTACK
	 * @param target player or NPC, target of ATTACK
	 * @param miss one of ATTACK_XXX constants
	 * @param crit if the ATTACK have critical success
	 * @param dual if dual weapon is used
	 * @param ss if weapon item was charged by soulshot
	 * @return damage points
	 */
	public final double calcPhysDam(L2Character attacker, L2Character target, L2Skill skill,
									boolean shld, boolean crit, boolean dual, boolean ss)
	{
		if (attacker instanceof L2PcInstance)
		{
			L2PcInstance pcInst = (L2PcInstance)attacker;
			if (pcInst.isGM() && pcInst.getAccessLevel() < Config.GM_CAN_GIVE_DAMAGE)
					return 0;
		}
		
		double damage = attacker.getPAtk(target);
		double defence = target.getPDef(attacker);
		if (ss) damage *= 2;
		if (skill != null)
		{
			damage += skill.getPower();
			//damage += skill.getPower() * 0.7 * attacker.getPAtk(target)/defence;
		}
		//		damage = damage * attacker.getSTR()*(1 - attacker.getLevel()/100)/60*1.15;
		if (target instanceof L2NpcInstance)
		{
			Integer resistPAtk = ((L2NpcInstance) target).getTemplate().getResist(Stats.POWER_DEFENCE);
			damage *= resistPAtk.doubleValue() / 100;
		}
		// defence modifier depending of the attacker weapon
		L2Weapon weapon = attacker.getActiveWeaponItem();
		if (weapon != null)
		{
			switch (weapon.getItemType())
			{
				case BOW:
					defence = target.calcStat(Stats.BOW_WPN_RES, defence, target, null);
					if (target instanceof L2NpcInstance)
					{
						Integer resistBow = ((L2NpcInstance) target).getTemplate().getResist(
																								Stats.BOW_WPN_RES);
						damage *= resistBow.doubleValue() / 100;
					}
					break;
				case BLUNT:
					defence = target.calcStat(Stats.BLUNT_WPN_RES, defence, target, null);
					if (target instanceof L2NpcInstance)
					{
						Integer resistBlunt = ((L2NpcInstance) target).getTemplate().getResist(
																								Stats.BLUNT_WPN_RES);
						damage *= resistBlunt.doubleValue() / 100;
					}
					break;
				case DAGGER:
					defence = target.calcStat(Stats.DAGGER_WPN_RES, defence, target, null);
					if (target instanceof L2NpcInstance)
					{
						Integer resistDagger = ((L2NpcInstance) target).getTemplate().getResist(
																								Stats.DAGGER_WPN_RES);
						damage *= resistDagger.doubleValue() / 100;
					}
					break;
				case DUAL:
					defence = target.calcStat(Stats.DUAL_WPN_RES, defence, target, null);
					break;
				case DUALFIST:
					defence = target.calcStat(Stats.DUALFIST_WPN_RES, defence, target, null);
					break;
				case ETC:
					defence = target.calcStat(Stats.ETC_WPN_RES, defence, target, null);
					break;
				case FIST:
					defence = target.calcStat(Stats.FIST_WPN_RES, defence, target, null);
					break;
				case POLE:
					defence = target.calcStat(Stats.POLE_WPN_RES, defence, target, null);
					break;
				case SWORD:
					defence = target.calcStat(Stats.SWORD_WPN_RES, defence, target, null);
					break;
				case BIGSWORD: //TODO: have a proper resitance for Big swords
					defence = target.calcStat(Stats.SWORD_WPN_RES, defence, target, null);
					break;
			}
		}
		if (crit) damage += attacker.getCriticalDmg(target, damage);
		if (shld && !Config.ALT_GAME_SHIELD_BLOCKS)
		{
			defence += target.getShldDef();
		}
		//if (!(attacker instanceof L2RaidBossInstance) && 
		/*
		if ((attacker instanceof L2NpcInstance || attacker instanceof L2SiegeGuardInstance))
		{
			if (attacker instanceof L2RaidBossInstance) damage *= 1; // was 10 changed for temp fix
			//			else
			//			damage *= 2;
			//			if (attacker instanceof L2NpcInstance || attacker instanceof L2SiegeGuardInstance){
			//damage = damage * attacker.getSTR() * attacker.getAccuracy() * 0.05 / defence;
			//			damage = damage * attacker.getSTR()*  (attacker.getSTR() + attacker.getLevel()) * 0.025 / defence;
			//			damage += _rnd.nextDouble() * damage / 10 ;
		}
		*/
		//		else {
		//if (skill == null)
		damage = 70 * damage / defence;

		damage += Rnd.nextDouble() * damage / 10;
		//		damage += _rnd.nextDouble()* attacker.getRandomDamage(target);
		//		}
		if (shld && Config.ALT_GAME_SHIELD_BLOCKS)
		{
			damage -= target.getShldDef();
			if (damage < 0) damage = 0;
		}
		// Sami: These values are a quick fix to balance dagger gameplay and give
		// armor resistances vs dagger. daggerWpnRes could also be used if a skill 
		// was given to all classes. The values here try to be a compromise.
		// They were added in a late C4 rev (2289).
		if (target instanceof L2PcInstance && weapon != null && weapon.getItemType() == L2WeaponType.DAGGER && skill != null)
		{
			L2Armor armor = ((L2PcInstance)target).getActiveChestArmorItem();
			if (armor != null)
			{
				if(((L2PcInstance)target).isWearingHeavyArmor())
					damage /= 2; // originally 2.2, 2.5 during early C5
				if(((L2PcInstance)target).isWearingLightArmor())
					damage /= 1.5; // originally 1.5, 2 during early C5
				if(((L2PcInstance)target).isWearingMagicArmor())
					damage /= 1.3; // originally 1, 1.8 during early C5
			}            
		}

		if (attacker instanceof L2NpcInstance)
		{
			int raceId = ((L2NpcInstance) attacker).getTemplate().race;
			//Skill Race : Undead
			if (raceId == 4290) damage /= attacker.getPDefUndead(target);
		}
		if (target instanceof L2NpcInstance)
		{
			int raceId = ((L2NpcInstance) target).getTemplate().race;
			//Skill Race : Undead
			if (raceId == 4290) damage *= attacker.getPAtkUndead(target);
			//Skill Race : Beast
			if (raceId == 4292) damage *= attacker.getPAtkMonsters(target);
			//Skill Race : Animal
			if (raceId == 4293) damage *= attacker.getPAtkAnimals(target);
			//Skill Race : Plant
			if (raceId == 4294) damage *= attacker.getPAtkPlants(target);
			//Skill Race : Dragon
			if (raceId == 4299) damage *= attacker.getPAtkDragons(target);
			//Skill Race : Bug
			if (raceId == 4301) damage *= attacker.getPAtkInsects(target);
		}

		if (damage > 0 && damage < 1)
		{
			damage = 1;
		}
		else if (damage < 0)
		{
			damage = 0;
		}

		return damage;
	}

	public final double calcMagicDam(L2Character attacker, L2Character target, L2Skill skill,
										boolean ss, boolean bss, boolean mcrit)
	{
		if (attacker instanceof L2PcInstance)
		{
			L2PcInstance pcInst = (L2PcInstance)attacker;
			if (pcInst.isGM() && pcInst.getAccessLevel() < Config.GM_CAN_GIVE_DAMAGE)
				return 0;
		}
		
		double mAtk = attacker.getMAtk(target, skill);
		double mDef = target.getMDef(attacker, skill);
		if (bss) mAtk *= 4;
		else if (ss) mAtk *= 2;

		double damage = 91 * Math.sqrt(mAtk) / mDef * skill.getPower(attacker);
		//		if(attacker instanceof L2PcInstance && target instanceof L2PcInstance) damage *= 0.9; // PvP modifier (-10%)

		// Failure calculation
		if (Config.ALT_GAME_MAGICFAILURES && !calcMagicSuccess(attacker, target, skill))
		{
			if (attacker instanceof L2PcInstance)
			{
				if (calcMagicSuccess(attacker, target, skill)
					&& (target.getLevel() - attacker.getLevel()) <= 9)
				{
					if (skill.getSkillType() == SkillType.DRAIN) attacker.sendPacket(new SystemMessage(
																										SystemMessage.DRAIN_HALF_SUCCESFUL));
					else attacker.sendPacket(new SystemMessage(SystemMessage.ATTACK_FAILED));

					damage /= 2;
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessage.S1_WAS_UNAFFECTED_BY_S2);
					sm.addString(target.getName());
					sm.addSkillName(skill.getId());
					attacker.sendPacket(sm);

					damage = 1;
				}
			}

			if (target instanceof L2PcInstance)
			{
				if (skill.getSkillType() == SkillType.DRAIN)
				{
					SystemMessage sm = new SystemMessage(SystemMessage.RESISTED_S1_DRAIN);
					sm.addString(attacker.getName());
					target.sendPacket(sm);
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessage.RESISTED_S1_MAGIC);
					sm.addString(attacker.getName());
					target.sendPacket(sm);
				}
			}
		}
		else if (mcrit) damage *= 4;

		return damage;
	}

	/** Returns true in case of critical hit */
	public final boolean calcCrit(double rate)
	{
		int critHit = Rnd.get(1000);
		return rate > critHit;
	}

	public final boolean calcMCrit(double mRate)
	{
		int mcritHit = Rnd.get(1000);
		return mRate > mcritHit;
	}

	/** Returns true in case when ATTACK is canceled due to hit */
	public final boolean calcAtkBreak(L2Character cha, double cancel)
	{
		if (Config.ALT_GAME_CANCEL_CAST && cha.isCastingNow()) return cancel > Rnd.get(100);
		if (Config.ALT_GAME_CANCEL_BOW && cha.isAttackingNow())
		{
			L2Weapon wpn = cha.getActiveWeaponItem();
			if (wpn != null && wpn.getItemType() == L2WeaponType.BOW) return cancel > Rnd.get(100);
		}
		return false;
	}

	/** Calculate delay (in milliseconds) before next ATTACK */
	public final int calcPAtkSpd(@SuppressWarnings("unused")
	L2Character attacker, @SuppressWarnings("unused")
	L2Character target, double rate)
	{
		// measured Oct 2006 by Tank6585, formula by Sami
		if(rate < 2) return 2700;
	    else return (int)(460000/rate);
	}

	/** Calculate delay (in milliseconds) for skills cast */
	public final int calcMAtkSpd(L2Character attacker, @SuppressWarnings("unused")
	L2Character target, L2Skill skill, double skillTime)
	{
		if (skill.isMagic()) return (int) (skillTime * 333 / attacker.getMAtkSpd());
		return (int) (skillTime * 333 / attacker.getPAtkSpd());
	}

	/** Calculate delay (in milliseconds) for skills cast */
	public final int calcMAtkSpd(L2Character attacker, L2Skill skill, double skillTime)
	{
		if (skill.isMagic()) return (int) (skillTime * 333 / attacker.getMAtkSpd());
		return (int) (skillTime * 333 / attacker.getPAtkSpd());
	}

	/** Returns true if hit missed (taget evaded) */
	public boolean calcHitMiss(L2Character attacker, L2Character target)
	{
		// accuracy+dexterity => probability to hit in percents
		int acc_attacker;
		int evas_target;
		acc_attacker = attacker.getAccuracy();
		evas_target = target.getEvasionRate(attacker);
		int d = 85 + acc_attacker - evas_target;
		return d < Rnd.get(100);
	}

	/** Returns true if shield defence successfull */
	public boolean calcShldUse(L2Character attacker, L2Character target)
	{
		double shldRate = target.calcStat(Stats.SHIELD_RATE, 0, attacker, null);
		return shldRate > Rnd.get(80);
	}

	public boolean calcMagicAffected(L2Character actor, L2Character target, L2Skill skill)
	{
		double defence = 0;
		if (skill.isActive() && skill.isOffensive()) defence = target.getMDef(actor, skill);
		double attack = 2 * actor.getMAtk(target, skill);
		double d = attack - defence;
		d /= attack + defence;
		d += 0.5 * Rnd.nextGaussian();
		return d > 0;
	}

	public boolean calcSkillSuccessOld(L2Character player, L2Character target, L2Skill skill,
										boolean ss, boolean sps, boolean bss)
	{
		if (Config.ALT_GAME_SKILL_FORMULAS.equalsIgnoreCase("alt")
			|| Config.ALT_GAME_SKILL_FORMULAS.equalsIgnoreCase("true"))
			return calcAltSkillSuccess(player, target, skill);
		if (target instanceof L2RaidBossInstance) return false;

		SkillType type = skill.getSkillType();

		boolean success = false;
		int rate = 0;
		int check = Rnd.get(100);
		double modifier = 1;
		double ssmodifier = 1;
		int value = 1;
		int maxLevel = SkillTable.getInstance().getMaxLevel(skill.getId(), skill.getLevel());
		double /*pAtk,*/pDef, mAtk, mDef;
		if (bss) ssmodifier *= 2;
		else if (sps) ssmodifier *= 1.5;
		else if (ss) ssmodifier *= 1.5;

		switch (type)
		{
			case STUN:
				// For normal Stun Attack, with skillType = STUN
				pDef = 1;
				pDef = target.calcStat(Stats.STUN_RES, pDef, target, null);
				value = 4800 + (int) (50 * (player.getLevel() - target.getLevel()) + 5100 * ((float) skill.getLevel() / maxLevel));
				if (pDef > 0) value /= pDef;
				modifier = 10 * target.getCON() / 3;
				if (modifier > 0) value /= modifier;
				value *= ssmodifier;
				value /= 100;
				if (!(target instanceof L2RaidBossInstance))
				{
					//min success
					if (value < 40) value = 40;
					//max success
					if (value > 99) value = 99;
				}
				rate = value;
				break;
			case MDOT:
			case CONFUSION:
				mAtk = player.getMAtk(target, skill);
				mDef = target.getMDef(player, skill);
				value = 3000 + (int) (7000 * ((float) skill.getLevel() / maxLevel));
				if (mDef > 0 && mAtk > 0) value *= 0.6 * mAtk / mDef;
				modifier = 20 * target.getMEN() / 3;
				//_log.fine(player.getName()+" matk:"+mAtk+",mdef="+mDef+",value="+value+",modifier="+modifier+",maxlevel="+maxLevel+",level="+skill.getLevel());
				break;
			case MUTE:
				mAtk = player.getMAtk(target, skill);
				mDef = target.getMDef(player, skill);
				value = 3000 + (int) (7000 * ((float) skill.getLevel() / maxLevel));
				if (mDef > 0 && mAtk > 0) value *= 0.6 * mAtk / mDef;
				modifier = 20 * target.getMEN() / 3;
				//_log.fine(player.getName()+" matk:"+mAtk+",mdef="+mDef+",value="+value+",modifier="+modifier+",maxlevel="+maxLevel+",level="+skill.getLevel());
				break;
			case MDAM:
			case PARALYZE:
				mAtk = player.getMAtk(target, skill);
				mDef = target.getMDef(player, skill);
				value = 5000 + (int) (5000 * ((float) skill.getLevel() / maxLevel));
				if (mDef > 0 && mAtk > 0) value *= 0.6 * mAtk / mDef;
				modifier = 20 * target.getMEN() / 3;
				if (modifier > 0) value /= modifier;
				value *= ssmodifier;
				value /= 100;
				if (!(target instanceof L2RaidBossInstance))
				{
					//min success
					if (value < 35) value = 35;
					//max success
					if (value > 90) value = 90;
					rate = value;
				}
				//_log.fine(player.getName()+" matk:"+mAtk+",mdef="+mDef+",value="+value+",modifier="+modifier+",maxlevel="+maxLevel+",level="+skill.getLevel());
				break;
			case SLEEP:
				mAtk = player.getMAtk(target, skill);
				mDef = target.getMDef(player, skill);
				value = 5000 + (int) (5000 * ((float) skill.getLevel() / maxLevel));
				mDef = target.calcStat(Stats.SLEEP_RES, mDef, target, null);
				if (mDef > 0 && mAtk > 0) value *= 0.6 * mAtk / mDef;
				modifier = 20 * target.getWIT() / 3;
				if (modifier > 0) value /= modifier;
				value *= ssmodifier;
				value /= 100;
				if (!(target instanceof L2RaidBossInstance))
				{
					//min success
					if (value < 45) value = 45;
					//max success
					if (value > 90) value = 90;
				}
				rate = value;
				//_log.fine(player.getName()+" matk:"+mAtk+",mdef="+mDef+",value="+value+",modifier="+modifier+",maxlevel="+maxLevel+",level="+skill.getLevel());
				break;
			case ROOT:
				mAtk = player.getMAtk(target, skill);
				mDef = target.getMDef(player, skill);
				value = 5000 + (int) (5000 * ((float) skill.getLevel() / maxLevel));
				mDef = target.calcStat(Stats.ROOT_RES, mDef, target, null);
				if (mDef > 0 && mAtk > 0) value *= 0.6 * mAtk / mDef;
				modifier = 10 * target.getDEX() / 3;
				if (modifier > 0) value /= modifier;
				value *= ssmodifier;
				value /= 100;
				if (!(target instanceof L2RaidBossInstance))
				{
					//min success
					if (value < 35) value = 35;
					//max success
					if (value > 90) value = 90;
				}
				rate = value;
				break;
			default:
				// For normal Stun attack with skillType = PDAM
				//pAtk = (int)skill.getPower();
				pDef = 1;
				pDef = target.calcStat(Stats.STUN_RES, pDef, target, null);
				value = 5000 + (int) (50 * (player.getLevel() - target.getLevel())+ 5000 * ((float) skill.getLevel() / maxLevel));
				if (pDef > 0) value /= pDef;
				modifier = 10 * target.getCON() / 3;
				if (modifier > 0) value /= modifier;
				value *= ssmodifier;
				value /= 100;
				if (!(target instanceof L2RaidBossInstance))
				{
					//min success
					if (value < 35) value = 35;
					//max success
					if (value > 90) value = 90;
				}
				rate = value;
				//			pDef = target.getPDef(player);
				//			pDef = target.calcStat(Stats.STUN_RES,pDef,target,null);
				//			value = 30 * 100 + (int)(70 * 100 * ((float)skill.getLevel()/maxLevel));
				//			value = 100 * 100;
				//			if (pDef > 0 && pAtk > 0)
				//			value *= 0.6 * pAtk/pDef;
				//			modifier    = 100 * target.getCON()/30;
				break;
		}

		if (modifier == 0)
		{
			_log.warning("Name: " + target.getName()
				+ " has bad base stat value. Fix datapack or notify dp ppl");
			modifier = 1;
		}

		if (rate == 0)
			rate = (int) (((player.getLevel() - target.getLevel()) + (int) (value / modifier)) * ssmodifier);
		//_log.fine(player.getName()+" rate:"+rate);

		if (rate > 100) rate = 100;
		else if (rate < 0) rate = 0;

		if (target instanceof L2RaidBossInstance)
		{
			int cLevel = player.getLevel();
			int tLevel = target.getLevel();
			int rRate = 1;
			if (cLevel > tLevel)
			{
				rRate = cLevel - tLevel;
				if (rRate > 9) rate /= rRate;
				if (rate < 1) rate = 0;
			}
		}

		if (check > rate) success = false;
		else success = true;

		return success;
	}

	public int calcSkillResistance(SkillType type, L2Character target)
	{
		if (type == null) return 0;
		switch (type)
		{
			case BLEED:
				return (int) target.calcStat(Stats.BLEED_RES, 0, target, null);
			case POISON:
				return (int) target.calcStat(Stats.POISON_RES, 0, target, null);
			case STUN:
				return (int) target.calcStat(Stats.STUN_RES, 0, target, null);
			case PARALYZE:
				return (int) target.calcStat(Stats.PARALYZE_RES, 0, target, null);
			case ROOT:
				return (int) target.calcStat(Stats.ROOT_RES, 0, target, null);
			case SLEEP:
				return (int) target.calcStat(Stats.SLEEP_RES, 0, target, null);
			case MUTE:
				return (int) target.calcStat(Stats.MUTE_RES, 0, target, null);
			case FEAR:
			case CONFUSION:
				return (int) target.calcStat(Stats.CONFUSION_RES, 0, target, null);
			default:
				return 0;
		}
	}

	public int calcSkillStatModifier(SkillType type, L2Character target)
	{
		if (type == null) return 0;
		switch (type)
		{
			case STUN:
				return (int) (Math.sqrt(CONbonus[target.getCON()]) * 100 - 100);
			case ROOT:
				return (int) (Math.sqrt(DEXbonus[target.getDEX()]) * 100 - 100);
			case SLEEP:
				return (int) (Math.sqrt(WITbonus[target.getWIT()]) * 100 - 100);
			case MUTE:
			case CONFUSION:
			case PARALYZE:
				return (int) (Math.sqrt(MENbonus[target.getMEN()]) * 100 - 100);
			default:
				return 0;
		}
	}

	public boolean calcSkillSuccess(L2Character attacker, L2Character target, L2Skill skill, boolean ss,
									boolean sps, boolean bss)
	{
		if (Config.ALT_GAME_SKILL_FORMULAS.equalsIgnoreCase("alt")
			|| Config.ALT_GAME_SKILL_FORMULAS.equalsIgnoreCase("true"))
			return calcAltSkillSuccess(attacker, target, skill);

		// Uncomment this if you want to revert to old skill success calculation: 
		// return calcSkillSuccessOld(attacker, target, skill, ss, sps, bss);

		SkillType type = skill.getSkillType();

		if (target.isRaid()
			&& (type == SkillType.CONFUSION || type == SkillType.MUTE || type == SkillType.PARALYZE
				|| type == SkillType.ROOT || type == SkillType.FEAR || type == SkillType.SLEEP
				|| type == SkillType.STUN || type == SkillType.DEBUFF || type == SkillType.AGGDEBUFF))
			return false; // these skills should not work on RaidBoss

		int value = (int) skill.getPower();
		int lvlDepend = skill.getLevelDepend();

		if (type == SkillType.PDAM || type == SkillType.MDAM) // For additional effects on PDAM skills (like STUN, SHOCK,...)
		{
			value = skill.getEffectPower();
			type = skill.getEffectType();
		}
		// TODO: Temporary fix for skills with EffectPower = 0 or EffectType not set
		if (value == 0 || type == null)
		{
			if (skill.getSkillType() == SkillType.PDAM)
			{
				value = 50;
				type = SkillType.STUN;
			}
			if (skill.getSkillType() == SkillType.MDAM)
			{
				value = 30;
				type = SkillType.PARALYZE;
			}
		}

		// TODO: Temporary fix for skills with Power = 0 or LevelDepend not set
		if (value == 0) value = (type == SkillType.PARALYZE) ? 50 : 80;
		if (lvlDepend == 0) lvlDepend = (type == SkillType.PARALYZE) ? 1 : 2;

		// TODO: Temporary fix for NPC skills with MagicLevel not set
		// int lvlmodifier = (skill.getMagicLevel() - target.getLevel()) * lvlDepend;
		int lvlmodifier = ((skill.getMagicLevel() > 0 ? skill.getMagicLevel() : attacker.getLevel()) - target.getLevel())
			* lvlDepend;
		int statmodifier = -calcSkillStatModifier(type, target);
		int resmodifier = -calcSkillResistance(type, target);

		int ssmodifier = 100;
		if (bss) ssmodifier = 200;
		else if (sps) ssmodifier = 150;
		else if (ss) ssmodifier = 150;

		int rate = value + statmodifier + lvlmodifier + resmodifier;
		if (skill.isMagic())
			rate += (int) (Math.pow((double) attacker.getMAtk(target, skill)
				/ target.getMDef(attacker, skill), 0.2) * 100) - 100;

		if (rate > 99) rate = 99;
		else if (rate < 1) rate = 1;

		if (ssmodifier != 100)
		{
			if (rate > 10000 / (100 + ssmodifier)) rate = 100 - (100 - rate) * 100 / ssmodifier;
			else rate = rate * ssmodifier / 100;
		}

		if (Config.DEVELOPER)
			System.out.println(skill.getName()
				+ ": "
				+ value
				+ ", "
				+ statmodifier
				+ ", "
				+ lvlmodifier
				+ ", "
				+ resmodifier
				+ ", "
				+ ((int) (Math.pow((double) attacker.getMAtk(target, skill)
					/ target.getMDef(attacker, skill), 0.2) * 100) - 100) + ", " + ssmodifier + " ==> "
				+ rate);
		return (Rnd.get(100) < rate);
	}

	public boolean calcMagicSuccess(L2Character attacker, L2Character target, L2Skill skill)
	{
		/* Level difference: 
		 *  less then 10 : full chance
		 *  10 - 20      : reduced chance
		 *  20 and more  : min chance
		 */
		int value = target.isRaid() ? 120 : 200; // chance is reduced for RaidBoss
		int lvlDepend = 8;

		// TODO: Temporary fix for NPC skills with MagicLevel not set
		// int lvlmodifier = (skill.getMagicLevel() - target.getLevel()) * lvlDepend;
		int lvlmodifier = ((skill.getMagicLevel() > 0 ? skill.getMagicLevel() : attacker.getLevel()) - target.getLevel())
			* lvlDepend;

		int rate = value + lvlmodifier;

		if (rate > 99) rate = 99;
		else if (rate < 1) rate = 1;

		return (Rnd.get(100) < rate);
	}

	public boolean calcAltSkillSuccess(L2Character activeChar, L2Character target, L2Skill skill)
	{
		// Get our numbers and base success rate
		SkillType type = skill.getSkillType();
		boolean success = true;
		int skillPower = (int) skill.getPower();
		int skillLevel = skill.getLevel();
		int attackerLevel = activeChar.getLevel();
		int targetLevel = target.getLevel();
		int CONModifier = (100 - target.getCON());
		int DEXModifier = (90 - target.getDEX());
		int WITModifier = (80 - target.getWIT());
		int powerModifier = Math.round(skillPower / 100);
		int levelModifier = Math.round(skillLevel / 2);
		int baseRate = (attackerLevel - targetLevel) + powerModifier + levelModifier;
		int rate = baseRate;
		int check = Rnd.get(100);

		switch (type)
		{
			case STUN:
				rate += CONModifier; // uses CON Modifier for STUN types
				break;
			case ROOT:
				rate += DEXModifier; // uses DEX Modifier for ROOT types
				break;
			case PARALYZE:
			case SLEEP:
				rate += WITModifier; // uses WIT Modifier for SLEEP and PARALYZE types
				break;
			default:
				rate += CONModifier; // uses CON Modifier for any other types (like PDAM ones)
				break;
		}

		if (rate > 100) rate = 100; // We shouldn't have more than 100% success rate
		if (rate < 1) rate = 1; // We shouldn't have less than 1% success rate

		if (check > rate) success = false;
		return success;
	}

	public boolean calculateUnlockChance(L2Skill skill)
	{
		int level = skill.getLevel();
		int chance = 0;
		switch (level)
		{
			case 1:
				chance = 30;
				break;

			case 2:
				chance = 50;
				break;

			case 3:
				chance = 75;
				break;

			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
			case 11:
			case 12:
			case 13:
			case 14:
				chance = 100;
				break;
		}
		if (Rnd.get(120) > chance)
		{
			return false;
		}

		return true;
	}
}
