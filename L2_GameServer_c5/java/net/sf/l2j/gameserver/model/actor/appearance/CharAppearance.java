package net.sf.l2j.gameserver.model.actor.appearance;

import net.sf.l2j.gameserver.model.L2Character;

public class CharAppearance
{
    // =========================================================
    // Data Field
    private L2Character[] _ActiveChar;          // Use array as a dirty trick to keep object as byref instead of byval
    private int _ClassId;
    private int _Face;
    private int _HairColor;
    private int _HairStyle;
    private int _RaceId;
    private int _Sex;
    
    // =========================================================
    // Constructor
    public CharAppearance(L2Character[] activeChar)
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
        if (_ActiveChar == null || _ActiveChar.length <= 0) return null;
        return _ActiveChar[0];
    }
    
    public final int getClassId() { return _ClassId; }
    public final void setClassId(int value) { _ClassId = value; }

    public final int getFace() { return _Face; }
    public final void setFace(int value) { _Face = value; }

    public final int getHairColor() { return _HairColor; }
    public final void setHairColor(int value) { _HairColor = value; }

    public final int getHairStyle() { return _HairStyle; }
    public final void setHairStyle(int value) { _HairStyle = value; }
    
    public final int getRaceId() { return _RaceId; }
    public final void setRaceId(int value) { _RaceId = value; }

    public final int getSex() { return _Sex; }
    public final void setSex(int value) { _Sex = value; }
}
