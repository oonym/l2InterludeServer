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

public class L2PetData
{
    public static final String PET_TYPE = "typeID";
    public static final String PET_LEVEL = "level";
    //  public static final String PET_EXP              = "exp"; 
    public static final String PET_MAX_EXP = "expMax";
    //  public static final String PET_HP           = "hp";      
    public static final String PET_MAX_HP = "hpMax";
    //  public static final String PET_MP               = "mp";  
    public static final String PET_MAX_MP = "mpMax";
    public static final String PET_PATK = "patk";
    public static final String PET_PDEF = "pdef";
    public static final String PET_MATK = "matk";
    public static final String PET_MDEF = "mdef";
    public static final String PET_ACCURACY = "acc";
    public static final String PET_EVASION = "evasion";
    public static final String PET_CRITICAL = "crit";
    public static final String PET_SPEED = "speed";
    public static final String PET_ATK_SPEED = "atk_speed";
    public static final String PET_CAST_SPEED = "cast_speed";
    //  public static final String PET_FEED                 = "feed";  
    public static final String PET_MAX_FEED = "feedMax";
    public static final String PET_FEED_BATTLE = "feedbattle";
    public static final String PET_FEED_NORMAL = "feednormal";
    //  public static final String PET_LOAD                 = "load";  
    public static final String PET_MAX_LOAD = "loadMax";
    public static final String PET_REGEN_HP = "hpregen";
    public static final String PET_REGEN_MP = "mpregen";

    private int petID;
    private int petLevel;
    //  private int petExp; 
    private long petMaxExp;
    //    private int petHP; 
    private int petMaxHP;
    //    private int petMP;         
    private int petMaxMP;
    private int petPAtk;
    private int petPDef;
    private int petMAtk;
    private int petMDef;
    private int petAccuracy;
    private int petEvasion;
    private int petCritical;
    private int petSpeed;
    private int petAtkSpeed;
    private int petCastSpeed;
    //      private int petFeed; 
    private int petMaxFeed;
    private int petFeedBattle;
    private int petFeedNormal;
    private int petMaxLoad;
    private int petRegenHP;
    private int petRegenMP;

    public void setStat(String stat, int value)
    {
        //      if (stat.equalsIgnoreCase(PET_EXP)) { this.setPetExp(value); } 
        if (stat.equalsIgnoreCase(PET_MAX_EXP))
        {
            this.setPetMaxExp(value);
        }
        //        else if (stat.equalsIgnoreCase(PET_HP)) { this.setPetHP(value); } 
        else if (stat.equalsIgnoreCase(PET_MAX_HP))
        {
            this.setPetMaxHP(value);
        }
        //        else if (stat.equalsIgnoreCase(PET_MP)) { this.setPetMP(value); } 
        else if (stat.equalsIgnoreCase(PET_MAX_MP))
        {
            this.setPetMaxMP(value);
        }
        else if (stat.equalsIgnoreCase(PET_PATK))
        {
            this.setPetPAtk(value);
        }
        else if (stat.equalsIgnoreCase(PET_PDEF))
        {
            this.setPetPDef(value);
        }
        else if (stat.equalsIgnoreCase(PET_MATK))
        {
            this.setPetMAtk(value);
        }
        else if (stat.equalsIgnoreCase(PET_MDEF))
        {
            this.setPetMDef(value);
        }
        else if (stat.equalsIgnoreCase(PET_ACCURACY))
        {
            this.setPetAccuracy(value);
        }
        else if (stat.equalsIgnoreCase(PET_EVASION))
        {
            this.setPetEvasion(value);
        }
        else if (stat.equalsIgnoreCase(PET_CRITICAL))
        {
            this.setPetCritical(value);
        }
        else if (stat.equalsIgnoreCase(PET_SPEED))
        {
            this.setPetSpeed(value);
        }
        else if (stat.equalsIgnoreCase(PET_ATK_SPEED))
        {
            this.setPetAtkSpeed(value);
        }
        else if (stat.equalsIgnoreCase(PET_CAST_SPEED))
        {
            this.setPetCastSpeed(value);
        }
        //        else if (stat.equalsIgnoreCase(PET_FEED)) { this.setPetFeed(value); }          
        else if (stat.equalsIgnoreCase(PET_MAX_FEED))
        {
            this.setPetMaxFeed(value);
        }
        else if (stat.equalsIgnoreCase(PET_FEED_NORMAL))
        {
            this.setPetFeedNormal(value);
        }
        else if (stat.equalsIgnoreCase(PET_FEED_BATTLE))
        {
            this.setPetFeedBattle(value);
        }
        //        else if (stat.equalsIgnoreCase(PET_LOAD)) { this.setPetLoad(value); } 
        else if (stat.equalsIgnoreCase(PET_MAX_LOAD))
        {
            this.setPetMaxLoad(value);
        }
        else if (stat.equalsIgnoreCase(PET_REGEN_HP))
        {
            this.setPetRegenHP(value);
        }
        else if (stat.equalsIgnoreCase(PET_REGEN_MP))
        {
            this.setPetRegenMP(value);
        }
    }
    public void setStat(String stat, long value)
    {
        //      if (stat.equalsIgnoreCase(PET_EXP)) { this.setPetExp(value); } 
        if (stat.equalsIgnoreCase(PET_MAX_EXP))
        {
            this.setPetMaxExp(value);
        }
    }

    //  ID   
    public int getPetID()
    {
        return petID;
    }

    public void setPetID(int pPetID)
    {
        this.petID = pPetID;
    }

    //  Level   
    public int getPetLevel()
    {
        return petLevel;
    }

    public void setPetLevel(int pPetLevel)
    {
        this.petLevel = pPetLevel;
    }

    //  Exp     
    //    public int getPetExp() { return petExp; }     
    //    public void setPetExp(int petExp) { this.petExp = petExp; } 

    //  Max Exp     
    public long getPetMaxExp()
    {
        return petMaxExp;
    }

    public void setPetMaxExp(long pPetMaxExp)
    {
        this.petMaxExp = pPetMaxExp;
    }

    //  HP     
    //    public int getPetHP() { return petHP; }     
    //    public void setPetHP(int petHP) { this.petHP = petHP; } 

    //  Max HP     
    public int getPetMaxHP()
    {
        return petMaxHP;
    }

    public void setPetMaxHP(int pPetMaxHP)
    {
        this.petMaxHP = pPetMaxHP;
    }

    //  Mp     
    //    public int getPetMP() { return petMP; } 
    //    public void setPetMP(int petMP) { this.petMP = petMP; } 

    //  Max Mp     
    public int getPetMaxMP()
    {
        return petMaxMP;
    }

    public void setPetMaxMP(int pPetMaxMP)
    {
        this.petMaxMP = pPetMaxMP;
    }

    //  PAtk     
    public int getPetPAtk()
    {
        return petPAtk;
    }

    public void setPetPAtk(int pPetPAtk)
    {
        this.petPAtk = pPetPAtk;
    }

    //  PDef     
    public int getPetPDef()
    {
        return petPDef;
    }

    public void setPetPDef(int pPetPDef)
    {
        this.petPDef = pPetPDef;
    }

    //  MAtk   
    public int getPetMAtk()
    {
        return petMAtk;
    }

    public void setPetMAtk(int pPetMAtk)
    {
        this.petMAtk = pPetMAtk;
    }

    //  MDef 
    public int getPetMDef()
    {
        return petMDef;
    }

    public void setPetMDef(int pPetMDef)
    {
        this.petMDef = pPetMDef;
    }

    //  Accuracy 
    public int getPetAccuracy()
    {
        return petAccuracy;
    }

    public void setPetAccuracy(int pPetAccuracy)
    {
        this.petAccuracy = pPetAccuracy;
    }

    //  Evasion 
    public int getPetEvasion()
    {
        return petEvasion;
    }

    public void setPetEvasion(int pPetEvasion)
    {
        this.petEvasion = pPetEvasion;
    }

    //  Critical 
    public int getPetCritical()
    {
        return petCritical;
    }

    public void setPetCritical(int pPetCritical)
    {
        this.petCritical = pPetCritical;
    }

    //  Speed 
    public int getPetSpeed()
    {
        return petSpeed;
    }

    public void setPetSpeed(int pPetSpeed)
    {
        this.petSpeed = pPetSpeed;
    }

    //  Atk Speed 
    public int getPetAtkSpeed()
    {
        return petAtkSpeed;
    }

    public void setPetAtkSpeed(int pPetAtkSpeed)
    {
        this.petAtkSpeed = pPetAtkSpeed;
    }

    //  Cast Speed 
    public int getPetCastSpeed()
    {
        return petCastSpeed;
    }

    public void setPetCastSpeed(int pPetCastSpeed)
    {
        this.petCastSpeed = pPetCastSpeed;
    }

    //  Feed     
    //    public int getPetFeed(){ return petFeed; } 
    //    public void setPetFeed(int petFeed) { this.petFeed = petFeed; } 

    //  MaxFeed     
    public int getPetMaxFeed()
    {
        return petMaxFeed;
    }

    public void setPetMaxFeed(int pPetMaxFeed)
    {
        this.petMaxFeed = pPetMaxFeed;
    }

    //  Normal Feed 
    public int getPetFeedNormal()
    {
        return petFeedNormal;
    }

    public void setPetFeedNormal(int pPetFeedNormal)
    {
        this.petFeedNormal = pPetFeedNormal;
    }

    //  Battle Feed 
    public int getPetFeedBattle()
    {
        return petFeedBattle;
    }

    public void setPetFeedBattle(int pPetFeedBattle)
    {
        this.petFeedBattle = pPetFeedBattle;
    }

    //  Load     
    //    public int getPetLoad() { return petLoad; } 
    //    public void setPetLoad(int petLoad) { this.petLoad = petLoad; } 

    //  Max Load     
    public int getPetMaxLoad()
    {
        return petMaxLoad;
    }

    public void setPetMaxLoad(int pPetMaxLoad)
    {
        this.petMaxLoad = pPetMaxLoad;
    }

    //  Regen HP     
    public int getPetRegenHP()
    {
        return petRegenHP;
    }

    public void setPetRegenHP(int pPetRegenHP)
    {
        this.petRegenHP = pPetRegenHP;
    }

    //  Regen MP     
    public int getPetRegenMP()
    {
        return petRegenMP;
    }

    public void setPetRegenMP(int pPetRegenMP)
    {
        this.petRegenMP = pPetRegenMP;
    }

    public String toString()
    {
        return "PetID: " + getPetID() + " \t" + "PetLevel: " + getPetLevel() + " \t" +
        //        PET_EXP + ": " + getPetExp() + " \t" + 
            PET_MAX_EXP + ": " + getPetMaxExp() + " \t" +
            //        PET_HP + ": " + getPetHP() + " \t" + 
            PET_MAX_HP + ": " + getPetMaxHP() + " \t" +
            //        PET_MP + ": " + getPetMP() + " \t" + 
            PET_MAX_MP + ": " + getPetMaxMP() + " \t" + PET_PATK + ": " + getPetPAtk() + " \t"
            + PET_PDEF + ": " + getPetPDef() + " \t" + PET_MATK + ": " + getPetMAtk() + " \t" + PET_MDEF
            + ": " + getPetMDef() + " \t" + PET_ACCURACY + ": " + getPetAccuracy() + " \t" + PET_EVASION
            + ": " + getPetEvasion() + " \t" + PET_CRITICAL + ": " + getPetCritical() + " \t"
            + PET_SPEED + ": " + getPetSpeed() + " \t" + PET_ATK_SPEED + ": " + getPetAtkSpeed() + " \t"
            + PET_CAST_SPEED + ": " + getPetCastSpeed() + " \t" +
            //        PET_FEED + ": " + getPetFeed() + " \t" +             
            PET_MAX_FEED + ": " + getPetMaxFeed() + " \t" + PET_FEED_BATTLE + ": " + getPetFeedBattle()
            + " \t" + PET_FEED_NORMAL + ": " + getPetFeedNormal() + " \t" +
            //        PET_LOAD + ": " + getPetLoad() + " \t" + 
            PET_MAX_LOAD + ": " + getPetMaxLoad() + " \t" + PET_REGEN_HP + ": " + getPetRegenHP()
            + " \t" + PET_REGEN_MP + ": " + getPetRegenMP();
    }

}
