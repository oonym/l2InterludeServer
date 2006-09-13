package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.instancemanager.PetitionManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class handles commands for GMs to respond to petitions.
 * 
 * @author Tempy
 * 
 */
public class AdminPetition implements IAdminCommandHandler
{
	private static String[] _adminCommands = {"admin_view_petitions", "admin_view_petition",
	                                          "admin_accept_petition", "admin_reject_petition",
                                              "admin_reset_petitions"};
	private static final int REQUIRED_LEVEL = Config.GM_MIN;
	
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (!Config.ALT_PRIVILEGES_ADMIN)
			if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM()))
				return false;
		
		int petitionId = -1;
		
		try {
			petitionId = Integer.parseInt(command.split(" ")[1]);
		}
		catch (Exception e) { }
		
		if (command.equals("admin_view_petitions"))
		{
			PetitionManager.getInstance().sendPendingPetitionList(activeChar);
		}
		else if (command.startsWith("admin_view_petition"))
		{
			PetitionManager.getInstance().viewPetition(activeChar, petitionId);
		}
		else if (command.startsWith("admin_accept_petition"))
		{
			if (PetitionManager.getInstance().isPlayerInConsultation(activeChar))
			{
				activeChar.sendMessage("You may only deal with one active petition at a time.");
				return true;
			}
			
			if (PetitionManager.getInstance().isPetitionInProcess(petitionId))
			{
				activeChar.sendMessage("The petition has already answered.");
				return true;
			}
			
			if (!PetitionManager.getInstance().acceptPetition(activeChar, petitionId))
				activeChar.sendMessage("Invalid petition specified or error occurred.");
		}
		else if (command.startsWith("admin_reject_petition"))
		{
			if (!PetitionManager.getInstance().rejectPetition(activeChar, petitionId))
				activeChar.sendMessage("Invalid petition specified or error occurred.");
		}
		else if (command.equals("admin_reset_petitions"))
		{
			if (PetitionManager.getInstance().isPetitionInProcess())
			{
				activeChar.sendMessage("You cannot clear the petition queue when a petition is being dealt with.");
				return false;
			}
			
			PetitionManager.getInstance().clearPendingPetitions();
		}
		
		return true;
	}
	
	public String[] getAdminCommandList() {
		return _adminCommands;
	}
	
	private boolean checkLevel(int level) {
		return (level >= REQUIRED_LEVEL);
	}
}
