package net.sf.l2j.gameserver.clientpackets;

import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

public final class RequestDeleteMacro extends L2GameClientPacket
{  
	private int _id;
	
	private static final String _C__C2_REQUESTDELETEMACRO = "[C] C2 RequestDeleteMacro";
	
	protected void readImpl()
	{
		_id = readD();
	}
	
	protected void runImpl()
	{
		if (getClient().getActiveChar() == null)
		    return;
		getClient().getActiveChar().deleteMacro(_id);
	    SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
	    sm.addString("Delete macro id="+_id);
		sendPacket(sm);
		sm = null;
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__C2_REQUESTDELETEMACRO;
	}

}
