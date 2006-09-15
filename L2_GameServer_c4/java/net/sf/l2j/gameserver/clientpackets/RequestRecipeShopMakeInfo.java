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
package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;

import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.RecipeShopItemInfo;

/**
 * This class ...
 * cdd
 * @version $Revision: 1.1.2.1.2.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestRecipeShopMakeInfo extends ClientBasePacket{
    private static final String _C__B5_RequestRecipeShopMakeInfo = "[C] b5 RequestRecipeShopMakeInfo";
    //private static Logger _log = Logger.getLogger(RequestRecipeShopMakeInfo.class.getName());
    
    @SuppressWarnings("unused")
    private final int _playerObjectId;
    private final int _recipeId;
    
    public RequestRecipeShopMakeInfo(ByteBuffer buf, ClientThread client)
    {
        super(buf, client);
        _playerObjectId = readD();
        _recipeId = readD();
	}

	void runImpl()
	{
        L2PcInstance player = getClient().getActiveChar();
	if (player == null)
	    return;
	    

        player.sendPacket(new RecipeShopItemInfo(_playerObjectId,_recipeId));
        
    }
    
    
    public String getType()
    {
        return _C__B5_RequestRecipeShopMakeInfo;
    }
    
    
}
