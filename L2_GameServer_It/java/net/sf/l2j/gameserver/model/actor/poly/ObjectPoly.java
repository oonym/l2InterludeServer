package net.sf.l2j.gameserver.model.actor.poly;

import net.sf.l2j.gameserver.model.L2Object;

public class ObjectPoly
{
    // =========================================================
    // Data Field
    private L2Object _activeObject;          
    private int _polyId;
    private String _polyType;
    
    // =========================================================
    // Constructor
    public ObjectPoly(L2Object activeObject)
    {
        _activeObject = activeObject;
    }
    
    // =========================================================
    // Method - Public
    public void setPolyInfo(String polyType, String polyId)
    {
        setPolyId(Integer.parseInt(polyId));     
        setPolyType(polyType);
    }
    
    // =========================================================
    // Method - Private

    // =========================================================
    // Property - Public
    public final L2Object getActiveObject()
    {
        return _activeObject;
    }
    
    public final boolean isMorphed() { return getPolyType() != null; }
    
    public final int getPolyId() { return _polyId; }
    public final void setPolyId(int value) { _polyId = value; }
    
    public final String getPolyType() { return _polyType; }
    public final void setPolyType(String value) { _polyType = value; }
}
