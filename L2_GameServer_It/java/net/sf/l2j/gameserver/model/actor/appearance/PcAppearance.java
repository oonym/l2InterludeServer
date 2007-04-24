package net.sf.l2j.gameserver.model.actor.appearance;

public class PcAppearance
{
    // =========================================================
    // Data Field
    private byte _Face;
    private byte _HairColor;
    private byte _HairStyle;
    private boolean _Sex; //Female true(1)
	/** true if  the player is invisible */
	private boolean _invisible = false;
	/** The hexadecimal Color of players name (white is 0xFFFFFF) */
	private int _nameColor = 0xFFFFFF;	
	/** The hexadecimal Color of players name (white is 0xFFFFFF) */
	private int _titleColor = 0xFFFF77;
    
    // =========================================================
    // Constructor
    public PcAppearance(byte Face, byte HColor, byte HStyle, boolean Sex)
    {
    	_Face = Face;
    	_HairColor = HColor;
    	_HairStyle = HStyle;
    	_Sex = Sex;
    }

    // =========================================================
    // Method - Public
    
    // =========================================================
    // Method - Private

    // =========================================================
    // Property - Public
    public final byte getFace() { return _Face; }
    /**
     * @param byte value
     */
    public final void setFace(int value) { _Face = (byte)value; }

    public final byte getHairColor() { return _HairColor; }
    /**
     * @param byte value
     */
    public final void setHairColor(int value) { _HairColor = (byte)value; }

    public final byte getHairStyle() { return _HairStyle; }
    /**
     * @param byte value
     */
    public final void setHairStyle(int value) { _HairStyle = (byte)value; }

    public final boolean getSex() { return _Sex; }
    /**
     * @param boolean isfemale
     */
    public final void setSex(boolean isfemale) { _Sex = isfemale; }
    
    public void setInvisible()
	{
		_invisible = true;
	}
	
	public void setVisible()
	{
		_invisible = false;
	}
	
	public boolean getInvisible()
	{
		return _invisible;
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
}
