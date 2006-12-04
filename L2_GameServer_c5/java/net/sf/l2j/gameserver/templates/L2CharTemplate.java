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
package net.sf.l2j.gameserver.templates;

/**
 * This class ...
 * 
 * @version $Revision: 1.2.4.6 $ $Date: 2005/04/02 15:57:51 $
 */
public class L2CharTemplate
{
	// BaseStats
	public final byte baseSTR;
	public final byte baseCON;
	public final byte baseDEX;
	public final byte baseINT;
	public final byte baseWIT;
	public final byte baseMEN;
	public final float baseHpMax;
    public final float baseCpMax;
	public final float baseMpMax;
	
	/** HP Regen base */
	public final float baseHpReg;
	
	/** MP Regen base */
	public final float baseMpReg;
       
    /** CP Regen base */
    public final float baseCpReg;
	
	public final int basePAtk;
	public final int baseMAtk;
	public final short basePDef;
	public final short baseMDef;
	public final short basePAtkSpd;
	public final short baseMAtkSpd;
	public final float baseMReuseRate;
	public final byte baseShldDef;
	public final short baseAtkRange;
	public final byte baseShldRate;
	public final byte baseCritRate;
	public final short baseRunSpd;
	// SpecialStats
	public final byte baseBreath;
	public final byte baseAggression;
	public final byte baseBleed;
	public final byte basePoison;
	public final byte baseStun;
	public final byte baseRoot;
	public final byte baseMovement;
	public final byte baseConfusion;
	public final byte baseSleep;
	public final byte baseFire;
	public final byte baseWind;
	public final byte baseWater;
	public final byte baseEarth;
    public final byte baseHoly;
    public final byte baseDark;
	public final byte baseAggressionRes;
	public final byte baseBleedRes;
	public final byte basePoisonRes;
	public final byte baseStunRes;
	public final byte baseRootRes;
	public final byte baseMovementRes;
	public final byte baseConfusionRes;
	public final byte baseSleepRes;
	public final byte baseFireRes;
	public final byte baseWindRes;
	public final byte baseWaterRes;
	public final byte baseEarthRes;
	public final byte baseHolyRes;
	public final byte baseDarkRes;
    
    //C4 Stats
    public final byte baseMpConsumeRate;
    public final byte baseHpConsumeRate;
	
	public final short collisionRadius;   
	public final short collisionHeight;
	
	public L2CharTemplate(StatsSet set)
	{
		// Base stats
		baseSTR            = set.getByte("baseSTR");
		baseCON            = set.getByte("baseCON");
		baseDEX            = set.getByte("baseDEX");
		baseINT            = set.getByte("baseINT");
		baseWIT            = set.getByte("baseWIT");
		baseMEN            = set.getByte("baseMEN");
		baseHpMax          = set.getFloat ("baseHpMax");
    	baseCpMax          = set.getFloat("baseCpMax");
		baseMpMax          = set.getFloat ("baseMpMax");
		baseHpReg          = set.getFloat ("baseHpReg");
        baseCpReg          = set.getFloat("baseCpReg");
		baseMpReg          = set.getFloat ("baseMpReg");
		basePAtk           = set.getInteger("basePAtk");
		baseMAtk           = set.getInteger("baseMAtk");
		basePDef           = set.getShort("basePDef");
		baseMDef           = set.getShort("baseMDef");
		basePAtkSpd        = set.getShort("basePAtkSpd");
		baseMAtkSpd        = set.getShort("baseMAtkSpd");
		baseMReuseRate     = set.getFloat ("baseMReuseDelay", 1.f);
		baseShldDef        = set.getByte("baseShldDef");
		baseAtkRange       = set.getByte("baseAtkRange");
		baseShldRate       = set.getByte("baseShldRate");
		baseCritRate       = set.getByte("baseCritRate");
		baseRunSpd         = set.getByte("baseRunSpd");
		// SpecialStats
		baseBreath         = set.getByte("baseBreath",         (byte)100);
		baseAggression     = set.getByte("baseAggression",     (byte)0);
		baseBleed          = set.getByte("baseBleed",          (byte)0);
		basePoison         = set.getByte("basePoison",         (byte)0);
		baseStun           = set.getByte("baseStun",           (byte)0);
		baseRoot           = set.getByte("baseRoot",           (byte)0);
		baseMovement       = set.getByte("baseMovement",       (byte)0);
		baseConfusion      = set.getByte("baseConfusion",      (byte)0);
		baseSleep          = set.getByte("baseSleep",          (byte)0);
		baseFire           = set.getByte("baseFire",           (byte)0);
		baseWind           = set.getByte("baseWind",           (byte)0);
		baseWater          = set.getByte("baseWater",          (byte)0);
		baseEarth          = set.getByte("baseEarth",          (byte)0);
		baseHoly           = set.getByte("baseHoly",           (byte)0);
		baseDark           = set.getByte("baseDark",           (byte)0);
		baseAggressionRes  = set.getByte("baseAaggressionRes",  (byte)0);
		baseBleedRes       = set.getByte("baseBleedRes",       (byte)0);
		basePoisonRes      = set.getByte("basePoisonRes",      (byte)0);
		baseStunRes        = set.getByte("baseStunRes",        (byte)0);
		baseRootRes        = set.getByte("baseRootRes",        (byte)0);
		baseMovementRes    = set.getByte("baseMovementRes",    (byte)0);
		baseConfusionRes   = set.getByte("baseConfusionRes",   (byte)0);
		baseSleepRes       = set.getByte("baseSleepRes",       (byte)0);
		baseFireRes        = set.getByte("baseFireRes",        (byte)0);
		baseWindRes        = set.getByte("baseWindRes",        (byte)0);
		baseWaterRes       = set.getByte("baseWaterRes",       (byte)0);
		baseEarthRes       = set.getByte("baseEarthRes",       (byte)0);
		baseHolyRes        = set.getByte("baseHolyRes",        (byte)0);
		baseDarkRes        = set.getByte("baseDarkRes",        (byte)0);
        
        //C4 Stats
        baseMpConsumeRate      = set.getByte("baseMpConsumeRate",        (byte)0);
        baseHpConsumeRate      = set.getByte("baseHpConsumeRate",        (byte)0);
		
		// Geometry
		collisionRadius    = set.getShort("collision_radius");
		collisionHeight    = set.getShort("collision_height");
	}
}
