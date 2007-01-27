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

import java.util.NoSuchElementException;

/**
 * Enum of basic stats.
 * 
 * @author mkizub
 */
public enum Stats {
	//
	// Base stats, for each in Calculator a slot is allocated
	//
	
	// HP & MP
	MAX_HP,
	MAX_MP,
    MAX_CP,
	REGENERATE_HP_RATE,
    REGENERATE_CP_RATE,
	REGENERATE_MP_RATE,
    RECHARGE_MP_RATE,
	HEAL_EFFECTIVNESS,
	// Atk & Def
	POWER_DEFENCE,
	MAGIC_DEFENCE,
	POWER_ATTACK,
	MAGIC_ATTACK,
	POWER_ATTACK_SPEED,
	MAGIC_ATTACK_SPEED, // how fast a spell is casted (including animation)
	MAGIC_REUSE_RATE, // how fast spells becomes ready to reuse
	SHIELD_DEFENCE,
	CRITICAL_DAMAGE,
	// Atk & Def rates
	EVASION_RATE,
	SHIELD_RATE,
	CRITICAL_RATE,
	MCRITICAL_RATE,
    EXPSP_RATE,
	ATTACK_CANCEL,
	// Accuracy and range
	ACCURACY_COMBAT,
	POWER_ATTACK_RANGE,
	MAGIC_ATTACK_RANGE,
    POWER_ATTACK_ANGLE,
	// Run speed,
	// walk & escape speed are calculated proportionally,
	// magic speed is a buff
	RUN_SPEED,
	
	//
	// Player-only stats
	//
	STAT_STR,
	STAT_CON,
	STAT_DEX,
	STAT_INT,
	STAT_WIT,
	STAT_MEN,

	//
	// Special stats, share one slot in Calculator
	//
	
	// stats of various abilities
	BREATH,
	//
	AGGRESSION, // locks a mob on tank caster
	BLEED, // by daggers, like poison
	POISON, // by magic, hp dmg over time
	STUN, // disable move/ATTACK for a period of time
	ROOT, // disable movement, but not ATTACK
	MOVEMENT, // slowdown movement, debuff
	CONFUSION, // mob changes target, opposite to aggression/hate
	SLEEP, // sleep (don't move/ATTACK) until attacked
	FIRE,
	WIND,
	WATER,
	EARTH,
 HOLY,
 DARK,
	//
	AGGRESSION_RES,
	BLEED_RES,
	POISON_RES,
	STUN_RES,
	PARALYZE_RES, 
	ROOT_RES,
	SLEEP_RES,
	CONFUSION_RES,
	MUTE_RES, 
	MOVEMENT_RES,
	FIRE_RES,
	WIND_RES,
	WATER_RES,
	EARTH_RES,
 HOLY_RES,
 DARK_RES,

	NONE_WPN_RES, // Shields!!!
	SWORD_WPN_RES,
	BLUNT_WPN_RES,
	DAGGER_WPN_RES,
	BOW_WPN_RES,
	POLE_WPN_RES,
	ETC_WPN_RES,
	FIST_WPN_RES,
	DUAL_WPN_RES,
	DUALFIST_WPN_RES,
	
	REFLECT_DAMAGE_PERCENT,
	ABSORB_DAMAGE_PERCENT,
    TRANSFER_DAMAGE_PERCENT,
	
	MAX_LOAD,
	
	PATK_PLANTS,
	PATK_INSECTS,
	PATK_ANIMALS,
	PATK_MONSTERS,
	PATK_DRAGONS,
	PATK_UNDEAD,

	PDEF_UNDEAD,
	

	ATK_REUSE,
	
	//ExSkill :)
	INV_LIM,
	WH_LIM,
	FREIGHT_LIM,
	P_SELL_LIM,
	P_BUY_LIM,
	REC_D_LIM,
	REC_C_LIM,
    
    //C4 Stats
    MP_CONSUME_RATE,
    HP_CONSUME_RATE,
    MP_CONSUME,
    SOULSHOT_COUNT
	;
	
	public static final int NUM_STATS = values().length; 

	public static Stats valueOfXml(String name)
	{
		name = name.intern();
		if (name == "maxHp"    ) return MAX_HP;
        if (name == "maxCp"    ) return MAX_CP;
		if (name == "maxMp"    ) return MAX_MP;
		if (name == "regHp"    ) return REGENERATE_HP_RATE;
        if (name == "regCp"    ) return REGENERATE_CP_RATE;
		if (name == "regMp"    ) return REGENERATE_MP_RATE;
        if (name == "gainMp"   ) return RECHARGE_MP_RATE;
		if (name == "gainHp"   ) return HEAL_EFFECTIVNESS;
		if (name == "pDef"     ) return POWER_DEFENCE;
		if (name == "mDef"     ) return MAGIC_DEFENCE;
		if (name == "pAtk"     ) return POWER_ATTACK;
		if (name == "mAtk"     ) return MAGIC_ATTACK;
		if (name == "pAtkSpd"  ) return POWER_ATTACK_SPEED;
		if (name == "mAtkSpd"  ) return MAGIC_ATTACK_SPEED;
		if (name == "mReuse"   ) return MAGIC_REUSE_RATE;
		if (name == "sDef"     ) return SHIELD_DEFENCE;
		if (name == "cAtk"     ) return CRITICAL_DAMAGE;
		if (name == "rEvas"    ) return EVASION_RATE;
		if (name == "rShld"    ) return SHIELD_RATE;
		if (name == "rCrit"    ) return CRITICAL_RATE;
        if (name == "rExp"    ) return EXPSP_RATE;
		if (name == "cancel"   ) return ATTACK_CANCEL;
		if (name == "accCombat") return ACCURACY_COMBAT;
		if (name == "pAtkRange"    ) return POWER_ATTACK_RANGE;
		if (name == "mAtkRange"    ) return MAGIC_ATTACK_RANGE;
        if (name == "pAtkAngle"    ) return POWER_ATTACK_ANGLE;
		if (name == "runSpd"       ) return RUN_SPEED;
		if (name == "breath"       ) return BREATH;
		if (name == "aggression"   ) return AGGRESSION;
		if (name == "bleed"        ) return BLEED;
		if (name == "poison"       ) return POISON;
		if (name == "stun"         ) return STUN;
		if (name == "root"         ) return ROOT;
		if (name == "movement"     ) return MOVEMENT;
		if (name == "confusion"    ) return CONFUSION;
		if (name == "sleep"        ) return SLEEP;
		if (name == "fire"         ) return FIRE;
		if (name == "wind"         ) return WIND;
		if (name == "water"        ) return WATER;
		if (name == "earth"        ) return EARTH;
 if (name == "holy"         ) return HOLY;
 if (name == "dark"         ) return DARK;
		if (name == "aggressionRes") return AGGRESSION_RES;
		if (name == "bleedRes"     ) return BLEED_RES;
		if (name == "poisonRes"    ) return POISON_RES;
		if (name == "stunRes"      ) return STUN_RES;
		if (name == "paralyzeRes"  ) return PARALYZE_RES;
		if (name == "rootRes"      ) return ROOT_RES;
		if (name == "sleepRes"     ) return SLEEP_RES;
		if (name == "confusionRes" ) return CONFUSION_RES;
		if (name == "muteRes"      ) return MUTE_RES;
		if (name == "movementRes"  ) return MOVEMENT_RES;
		if (name == "fireRes"      ) return FIRE_RES;
		if (name == "windRes"      ) return WIND_RES;
		if (name == "waterRes"     ) return WATER_RES;
		if (name == "earthRes"     ) return EARTH_RES;
 if (name == "holyRes"      ) return HOLY_RES;
 if (name == "darkRes"      ) return DARK_RES;

		if (name == "noneWpnRes"   ) return NONE_WPN_RES;
		if (name == "swordWpnRes"  ) return SWORD_WPN_RES;
		if (name == "bluntWpnRes"  ) return BLUNT_WPN_RES;
		if (name == "daggerWpnRes" ) return DAGGER_WPN_RES;
		if (name == "bowWpnRes"    ) return BOW_WPN_RES;
		if (name == "poleWpnRes"   ) return POLE_WPN_RES;
		if (name == "etcWpnRes"    ) return ETC_WPN_RES;
		if (name == "fistWpnRes"   ) return FIST_WPN_RES;
		if (name == "dualWpnRes"   ) return DUAL_WPN_RES;
		if (name == "dualFistWpnRes")return DUALFIST_WPN_RES;
		
		if (name == "STR"          ) return STAT_STR;
		if (name == "CON"          ) return STAT_CON;
		if (name == "DEX"          ) return STAT_DEX;
		if (name == "INT"          ) return STAT_INT;
		if (name == "WIT"          ) return STAT_WIT;
		if (name == "MEN"          ) return STAT_MEN;
		
		if (name == "reflectDam"   ) return REFLECT_DAMAGE_PERCENT;
		if (name == "absorbDam"    ) return ABSORB_DAMAGE_PERCENT;
        if (name == "transDam"    ) return TRANSFER_DAMAGE_PERCENT;
		if (name == "mCritRate"    ) return MCRITICAL_RATE;
		
		if (name == "maxLoad"      ) return MAX_LOAD;
		
		if (name == "pAtk-undead"     ) return PATK_UNDEAD;
		if (name == "pDef-undead"     ) return PDEF_UNDEAD;
		if (name == "pAtk-plants"     ) return PATK_PLANTS;
		if (name == "pAtk-insects"     ) return PATK_INSECTS;
		if (name == "pAtk-animals"     ) return PATK_ANIMALS;
		if (name == "pAtk-monsters"     ) return PATK_MONSTERS;
		if (name == "pAtk-dragons"     ) return PATK_DRAGONS;
		
		if (name == "atkReuse"     ) return ATK_REUSE;
		//Nowe xD
		if (name == "inventoryLimit"     ) return INV_LIM;
		if (name == "whLimit"            ) return WH_LIM;
		if (name == "FreightLimit"       ) return FREIGHT_LIM;
		if (name == "PrivateSellLimit"   ) return P_SELL_LIM;
		if (name == "PrivateBuyLimit"    ) return P_BUY_LIM;
		if (name == "DwarfRecipeLimit"   ) return REC_D_LIM;
		if (name == "CommonRecipeLimit"  ) return REC_C_LIM;
        
        //C4 Stats
        if (name == "MpConsumeRate"  ) return MP_CONSUME_RATE;
        if (name == "HpConsumeRate"  ) return HP_CONSUME_RATE;
        if (name == "MpConsume"  ) return MP_CONSUME;
        if (name == "soulShotCount"  ) return SOULSHOT_COUNT;
		throw new NoSuchElementException("Unknown name '"+name+"' for enum BaseStats");
	}
}
