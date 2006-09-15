package net.sf.l2j.gameserver.templates;

/**
 * This class represents a Newbie Helper Buff
 *
 * Author: Ayor 
 */

public class L2HelperBuff
{
    /** Min level that the player must achieve to obtain this buff from Newbie Helper */
    private int _lowerLevel;
    
    /** Max level that the player mustn't exceed if it want to obtain this buff from Newbie Helper */
    private int _upperLevel;
    
    /** Identifier of the skill (buff) that the Newbie Helper must cast */
    private int _skillID;
    
    /** Level of the skill (buff) that the Newbie Helper must cast */
    private int _skillLevel;
    
    /** If True only Magus class will obtain this Buff <BR>
     *  If False only Fighter class will obtain this Buff */
    private boolean _isMagicClass;

    
    /**
     * Constructor of L2HelperBuff.<BR><BR>
     */
    public L2HelperBuff(StatsSet set)
    {
        
        _lowerLevel          = set.getInteger("lowerLevel");
        _upperLevel          = set.getInteger("upperLevel");
        _skillID             = set.getInteger("skillID");
        _skillLevel          = set.getInteger("skillLevel");
        
        if("false".equals(set.getString("isMagicClass")))
            _isMagicClass = false;
        else
            _isMagicClass = true;
        
    }
    

    /**
     * Returns the lower level that the L2PcInstance must achieve in order to obtain this buff
     * @return int
     */
    public int getLowerLevel()
    {
       return _lowerLevel;
    }

    /**
     * Sets the lower level that the L2PcInstance must achieve in order to obtain this buff
     * @param itemId : int designating the lower level
     */
    public void setLowerLevel(int lowerLevel)
    {
        _lowerLevel = lowerLevel;
    }
    

    /**
     * Returns the upper level that the L2PcInstance mustn't exceed in order to obtain this buff
     * @return int
     */
    public int getUpperLevel()
    {
       return _upperLevel;
    }

    /**
     * Sets the upper level that the L2PcInstance mustn't exceed in order to obtain this buff
     * @param itemId : int designating the upper level
     */
    public void setUpperLevel(int upperLevel)
    {
        _upperLevel = upperLevel;
    }
    
    
    /**
     * Returns the ID of the buff that the L2PcInstance will receive
     * @return int
     */
    public int getSkillID()
    {
       return _skillID;
    }

    /**
     * Sets the ID of the buff that the L2PcInstance will receive
     * @param itemId : int designating the skill Identifier
     */
    public void setSkillID(int skillID)
    {
        _skillID = skillID;
    }
    
    
    /**
     * Returns the Level of the buff that the L2PcInstance will receive
     * @return int
     */
    public int getSkillLevel()
    {
       return _skillLevel;
    }

    /**
     * Sets the Level of the buff that the L2PcInstance will receive
     * @param itemId : int designating the level of the skill
     */
    public void setSkillLevel(int skillLevel)
    {
        _skillLevel = skillLevel;
    }
    
    
    /**
     * Returns if this Buff can be cast on a fighter or a mystic
     * @return boolean : False if it's a fighter class Buff
     */
    public boolean isMagicClassBuff()
    {
        return _isMagicClass;
    }
    
    /**
     * Sets if this Buff can be cast on a fighter or a mystic
     * @param sweep
     */
    public void setIsMagicClass(boolean isMagicClass)
    {
        _isMagicClass = isMagicClass;
    }

}