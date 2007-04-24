package net.sf.l2j.gameserver.serverpackets;

public class CameraMode extends L2GameServerPacket
{
    int _mode;

    private static final String _S__F1_CAMERAMODE = "[S] F1 CameraMode";

    /**
     * Forces client camera mode change
     * @param mode
     * 0 - third person cam
     * 1 - first person cam
     */
    public CameraMode(int mode)
    {
        _mode = mode;
    }
    
    public void writeImpl()
    {
        writeC(0xf1);
        writeD(_mode);
    }
    
    public String getType()
    {
        return _S__F1_CAMERAMODE;
    }
}
