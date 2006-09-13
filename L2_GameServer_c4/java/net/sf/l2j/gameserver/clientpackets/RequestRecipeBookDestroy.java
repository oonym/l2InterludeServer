package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;

import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.RecipeController;
import net.sf.l2j.gameserver.model.L2RecipeList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.RecipeBookItemList;

public class RequestRecipeBookDestroy extends ClientBasePacket 
{
    private static final String _C__AC_REQUESTRECIPEBOOKDESTROY = "[C] AD RequestRecipeBookDestroy";
    //private static Logger _log = Logger.getLogger(RequestSellItem.class.getName());

    private int _RecipeID;

    /**
    * Unknown Packet:ad
    * 0000: ad 02 00 00 00
    */
    public RequestRecipeBookDestroy(ByteBuffer buf, ClientThread client)
    {
        super(buf, client);

        _RecipeID = readD();
    }
            
    void runImpl()
    {
        L2PcInstance activeChar = getClient().getActiveChar();
        if (activeChar != null)
        {
        	L2RecipeList rp = RecipeController.getInstance().getRecipeList(_RecipeID-1); 
         	if (rp == null) 
         		return;
            activeChar.unregisterRecipeList(_RecipeID);
            
            RecipeBookItemList response = new RecipeBookItemList(rp.isDwarvenRecipe(),activeChar.getMaxMp()); 
         	if (rp.isDwarvenRecipe()) 
         		response.addRecipes(activeChar.getDwarvenRecipeBook()); 
         	else 
         		response.addRecipes(activeChar.getCommonRecipeBook()); 
            
            activeChar.sendPacket(response);
        }
    }
    
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
     */
    public String getType() 
    {
        return _C__AC_REQUESTRECIPEBOOKDESTROY;
    }
}