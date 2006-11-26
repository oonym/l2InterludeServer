package net.sf.l2j.gameserver.model.actor.appearance;

import net.sf.l2j.gameserver.model.L2Character;

public class CharAppearance
{
    // =========================================================
    // Data Field
    private L2Character _ActiveChar;
    private byte _ClassId;
    private byte _Face;
    private byte _HairColor;
    private byte _HairStyle;
    private byte _RaceId;
    private byte _Sex;
    
    // =========================================================
    // Constructor
    public CharAppearance(L2Character activeChar)
    {
        _ActiveChar = activeChar;
    }

    // =========================================================
    // Method - Public
    
    // =========================================================
    // Method - Private

    // =========================================================
    // Property - Public
    public final L2Character getActiveChar()
    {        
        return _ActiveChar;
    }
    
    public final byte getClassId() { return _ClassId; }
    public final void setClassId(byte value) { _ClassId = value; }

    public final byte getFace() { return _Face; }
    public final void setFace(byte value) { _Face = value; }

    public final byte getHairColor() { return _HairColor; }
    public final void setHairColor(byte value) { _HairColor = value; }

    public final byte getHairStyle() { return _HairStyle; }
    public final void setHairStyle(byte value) { _HairStyle = value; }
    
    public final byte getRaceId() { return _RaceId; }
    public final void setRaceId(byte value) { _RaceId = value; }

    public final byte getSex() { return _Sex; }
    public final void setSex(byte value) { _Sex = value; }
}
