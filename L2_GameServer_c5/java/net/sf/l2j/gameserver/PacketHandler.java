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
package net.sf.l2j.gameserver;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.clientpackets.*;
import net.sf.l2j.util.Util;


/**
 * This class ...
 * 
 * @version $Revision: 1.18.2.6.2.8 $ $Date: 2005/04/06 16:13:25 $
 */
public class PacketHandler
{
	private static Logger _log = Logger.getLogger(PacketHandler.class.getName());
    private static LinkedList<CustomPacketHandlerInterface> customhandlers = new LinkedList<CustomPacketHandlerInterface>();

	private PacketHandler()
	{
	}
	
	public static void addCustomHandler(CustomPacketHandlerInterface handler) {
	    customhandlers.add(handler);   
	}

    public static void removeCustomHandler(CustomPacketHandlerInterface handler) {
        customhandlers.remove(handler);   
    }

	public static ClientBasePacket handlePacket(ByteBuffer data, ClientThread client)
	{
		ClientBasePacket msg = null;
		int id = data.get() & 0xff;
		
		if(id != 0x00 && id != 0x08 && !client.isAuthed())
		{
			_log.warning("Player with IP: "+client.getConnection().getIP()+" tried to hack account : "+client.getLoginName());
			client.getConnection().close();
			//Just for sure
			return null;
		}

        for (CustomPacketHandlerInterface handler : customhandlers) {
            msg = handler.handlePacket(data, client);
            if (msg != null)
                break;
        }

        if (msg == null)
		switch(id)
		{
			case 0x00:
				msg = new ProtocolVersion(data, client);
				break;
			case 0x01:
				msg = new MoveBackwardToLocation(data, client); 
				break;
//			case 0x02:
//				// Say  ... not used any more ??
//				break;
			case 0x03:
				msg = new EnterWorld(data, client);
				break;
			case 0x04:
				msg = new Action(data, client);
				break;
			case 0x08:
				msg = new AuthLogin(data, client);
				break;
			case 0x09:
				msg = new Logout(data, client);
				break;
			case 0x0a:
				msg = new AttackRequest(data, client);
				break;
			case 0x0b:
				msg = new CharacterCreate(data, client);	
				break;
			case 0x0c:
				msg = new CharacterDelete(data, client);	
				break;
			case 0x0d:
				msg = new CharacterSelected(data, client);
				break;
			case 0x0e:
				msg = new NewCharacter(data, client);
				break;
			case 0x0f:
				msg = new RequestItemList(data, client);
				break;

//			case 0x10:
//				// RequestEquipItem ... not used any more, instead "useItem"  
//				break;
			case 0x11:
				msg = new RequestUnEquipItem(data, client);
				break;
			case 0x12:
				msg = new RequestDropItem(data, client);
				break;
			case 0x14:
				msg = new UseItem(data, client);
				break;
			case 0x15:
				msg = new TradeRequest(data, client);
				break;
			case 0x16:
				msg = new AddTradeItem(data, client);
				break;
			case 0x17:
				msg = new TradeDone(data, client);
				break;
			case 0x1a:
				msg = new DummyPacket(data, client, id);
				break;
			case 0x1b:
				msg = new RequestSocialAction(data, client);
				break;
			case 0x1c:
				msg = new ChangeMoveType2(data, client);
				break;
			case 0x1d:
				msg = new ChangeWaitType2(data, client);
				break;
			case 0x1e:
				msg = new RequestSellItem(data, client);
				break;
			case 0x1f:
				msg = new RequestBuyItem(data, client);
				break;
				
			case 0x20:
				msg = new RequestLinkHtml(data,client);
				break;
			case 0x21:
				msg = new RequestBypassToServer(data, client);
				break;
			case 0x22:
				msg = new RequestBBSwrite(data, client);
			break;
			case 0x23:
				msg = new DummyPacket(data,client, id);
				break;
			case 0x24:
				msg = new RequestJoinPledge(data, client);
				break;
			case 0x25:
				msg = new RequestAnswerJoinPledge(data, client);
				break;
			case 0x26:
				msg = new RequestWithdrawalPledge(data, client);  
				break;
			case 0x27:
				msg = new RequestOustPledgeMember(data, client);  
				break;
//			case 0x28:
//				// RequestDismissPledge  
//				break;
			case 0x29:
				msg = new RequestJoinParty(data, client);    
				break;
			case 0x2a:
				msg = new RequestAnswerJoinParty(data, client);     
				break;
			case 0x2b:
				msg = new RequestWithDrawalParty(data, client);     
				break;
			case 0x2c:
				msg = new RequestOustPartyMember(data, client);
				break;
			case 0x2d:
				// RequestDismissParty  
				break;
			case 0x2e:
				msg = new DummyPacket(data, client, id);   
				break;
			case 0x2f:
				msg = new RequestMagicSkillUse(data, client);
				break;
			case 0x30:
				msg = new Appearing(data, client);  //  (after death)
				break;
			case 0x31:
				if (Config.ALLOW_WAREHOUSE) 
					msg = new SendWareHouseDepositList(data, client);  
				break;
			case 0x32:
                msg = new SendWareHouseWithDrawList(data, client);  
				break;
			case 0x33:
				msg = new RequestShortCutReg(data, client);    
				break;
			case 0x34:
				msg = new DummyPacket(data, client, id);    
				break;
			case 0x35:
				msg = new RequestShortCutDel(data, client);    
				break;
			case 0x36:
				msg = new CannotMoveAnymore(data, client);    
				break;
			case 0x37:
				msg = new RequestTargetCanceld(data, client);
				break;
			case 0x38:
				msg = new Say2(data, client);
				break;
			case 0x3c:
				msg = new RequestPledgeMemberList(data, client);
				break;
			case 0x3e:
				msg = new DummyPacket(data, client, id);
				break;
			case 0x3f:
				msg = new RequestSkillList(data, client);
				break;

//			case 0x41:
//				// MoveWithDelta    ... unused ?? or only on ship ??
//				break;
			case 0x42:
				msg = new RequestGetOnVehicle(data, client);  
				break;
			case 0x43:
				msg = new RequestGetOffVehicle(data, client);  
				break;
			case 0x44:
				msg = new AnswerTradeRequest(data, client);
				break;
			case 0x45:
				msg = new RequestActionUse(data, client);
				break;
			case 0x46:
				msg = new RequestRestart(data, client);
				break;
//			case 0x47:
//				// RequestSiegeInfo   
//				break;
			case 0x48:
				msg = new ValidatePosition(data, client);
				break;
//			case 0x49:
//				// RequestSEKCustom   
//				break;
//				THESE ARE NOW TEMPORARY DISABLED 
			case 0x4a:
				new StartRotating(data, client);  
				break;
			case 0x4b:
				new FinishRotating(data, client); 
				break;
			case 0x4d:
				msg = new RequestStartPledgeWar(data, client);   
				break;
			case 0x4e:
				msg = new RequestReplyStartPledgeWar(data, client);      
				break;
			case 0x4f:
                msg = new RequestStopPledgeWar(data, client);      
				break;
				
			case 0x50:
                msg = new RequestReplyStopPledgeWar(data, client);           
                break; 
            case 0x51:
                msg = new RequestSurrenderPledgeWar(data, client);           
                break;
            case 0x52:
                msg = new RequestReplySurrenderPledgeWar(data,client);
                break;
            case 0x53:
				msg = new RequestSetPledgeCrest(data, client);           
				break;
			case 0x55:
				msg = new RequestGiveNickName(data, client);             
				break;
			case 0x57:
				msg = new RequestShowBoard(data, client);                
				break;
			case 0x58:
				msg = new RequestEnchantItem(data, client);
				break;
			case 0x59:
				msg = new RequestDestroyItem(data, client);
				break;				
			case 0x5b:
				msg = new SendBypassBuildCmd(data, client);   
				break;
			case 0x5c:
				msg = new RequestMoveToLocationInVehicle(data, client);
				break;
			case 0x5d:
				msg = new CannotMoveAnymoreInVehicle(data, client);
				break;		
			case 0x5e:
				msg = new RequestFriendInvite(data, client);
				break;
			case 0x5f:
				msg = new RequestAnswerFriendInvite(data, client);
				break;
				
			case 0x60:
				msg = new RequestFriendList(data, client);
				break;
			case 0x61:
				msg = new RequestFriendDel(data, client);
				break;
			case 0x62:
				msg = new CharacterRestore(data, client);
				break;
			case 0x63:
				msg = new RequestQuestList(data, client);
				break;
			case 0x64:  
				msg = new RequestQuestAbort(data, client);  
				break;				
			case 0x66:
				msg = new RequestPledgeInfo(data, client);
				break;
//			case 0x67:
//				// RequestPledgeExtendedInfo   
//				break;
			case 0x68:
				msg = new RequestPledgeCrest(data, client);
				break;
			case 0x69:
                msg = new RequestSurrenderPersonally(data,client);  
				break;
//			case 0x6a:
//				// Ride  
//				break;
			case 0x6b: // send when talking to trainer npc, to show list of available skills
				msg = new RequestAquireSkillInfo(data, client);//  --> [s] 0xa4;
				break;
			case 0x6c: // send when a skill to be learned is selected
				msg = new RequestAquireSkill(data, client);    
				break;
			case 0x6d:
				msg = new RequestRestartPoint(data, client); 
				break;
			case 0x6e: 
				msg = new RequestGMCommand(data, client);  
				break;
			case 0x6f: 
				msg = new RequestPartyMatchList(data, client);
				break;
				
			case 0x70: 
				msg = new RequestPartyMatchConfig(data, client);      
				break;
			case 0x71:
				msg = new RequestPartyMatchDetail(data, client);     
				break;
			case 0x72:
				msg = new RequestCrystallizeItem(data, client);
				break;
			case 0x73:
				msg = new RequestPrivateStoreManageSell(data, client);        
				break;
			case 0x74:
				msg = new SetPrivateStoreListSell(data, client);        
				break;
//			case 0x75:
//				msg = new RequestPrivateStoreManageCancel(data, _client);        
//				break;
			case 0x76:
				msg = new RequestPrivateStoreQuitSell(data, client);          
				break;
			case 0x77:
				msg = new SetPrivateStoreMsgSell(data, client);           
				break;
//			case 0x78:
//				// RequestPrivateStoreList         
//				break;
			case 0x79:
				msg = new RequestPrivateStoreBuy(data, client);             
				break;
//			case 0x7a:
//				// ReviveReply            
//				break;
//			case 0x7b:
//				// RequestTutorialLinkHtml           
//				break;
//			case 0x7c:
//				// RequestTutorialPassCmdToServer              
//				break;
//			case 0x7d:
//				// RequestTutorialQuestionMark             
//				break;
//			case 0x7e:
//				// RequestTutorialClientEvent             
//				break;
			case 0x7f:
				msg = new RequestPetition(data, client); //cSd
				break;
				
			case 0x80:
				msg = new RequestPetitionCancel(data, client); //cS
				break;
			case 0x81:
				msg = new RequestGmList(data, client);
				break;
			case 0x82:
				msg = new RequestJoinAlly(data, client);
				break;
			case 0x83:
				msg = new RequestAnswerJoinAlly(data, client);
				break;
            case 0x84:
                msg = new AllyLeave(data, client);
                break;
            case 0x85:
                msg = new AllyDismiss(data, client);
                break;
//			case 0x86:
//				// RequestDismissAlly
//				break;
			case 0x87:
				msg = new RequestSetAllyCrest(data, client);
				break;
			case 0x88:
				msg = new RequestAllyCrest(data, client);
				break;
			case 0x89:
				msg = new RequestChangePetName(data, client);
				break;
			case 0x8a:
				msg = new RequestPetUseItem(data, client);
				break;
			case 0x8b:
				msg = new RequestGiveItemToPet(data, client);
				break;
			case 0x8c:
				msg = new RequestGetItemFromPet(data, client);
				break;
			case 0x8e:
				msg = new RequestAllyInfo(data, client);
				break;
			case 0x8f:
				msg = new RequestPetGetItem(data, client);
				break;
				
			case 0x90:
				msg = new RequestPrivateStoreManageBuy(data, client);
				break;
			case 0x91:
				msg = new SetPrivateStoreListBuy (data, client);
				break;
//			case 0x92:
//				// RequestPrivateStoreBuyManageCancel
//				break;
			case 0x93:
				msg = new RequestPrivateStoreQuitBuy (data, client);
				break;
			case 0x94:
				msg = new SetPrivateStoreMsgBuy(data, client);
				break;
//			case 0x95:
//				// RequestPrivateStoreBuyList
//				break;
			case 0x96:
				msg = new RequestPrivateStoreSell(data, client);
				break;
//			case 0x97:
//				// SendTimeCheckPacket
//				break;
//			case 0x98:
//				// RequestStartAllianceWar
//				break;
//			case 0x99:
//				// ReplyStartAllianceWar
//				break;
//			case 0x9a:
//				// RequestStopAllianceWar
//				break;
//			case 0x9b:
// 				// ReplyStopAllianceWar
//				break;
//			case 0x9c:
//				// RequestSurrenderAllianceWar
//				break;
			case 0x9d:
				// RequestSkillCoolTime
				if (Config.DEBUG) _log.info("Request Skill Cool Time .. ignored");
				msg = null;
				break;
//			case 0x9e:
//				// RequestPackageSendableItemList
//				break;
//			case 0x9f:
//				// RequestPackageSend
//				break;
				
			case 0xa0:
				msg = new RequestBlock(data,client);
				break;
//			case 0xa1:
//				// RequestCastleSiegeInfo
//				break;
			case 0xa2:
                msg = new RequestSiegeAttackerList(data,client);
                break;
			case 0xa3:
                msg = new RequestSiegeDefenderList(data,client);
                break;
			case 0xa4:
                msg = new RequestJoinSiege(data,client);
                break;
			case 0xa5:
			    msg = new RequestConfirmSiegeWaitingList(data,client);
			    break;
//			case 0xa6:
//				// RequestSetCastleSiegeTime
//				break;
			case 0xa7:
				msg = new MultiSellChoose(data,client);
				break;
//			case 0xa8:
//				// NetPing
//				break;
            case 0xaa:
            	msg = new RequestUserCommand(data, client);
                break;
            case 0xab:
            	msg = new SnoopQuit(data, client);
            	break;
            case 0xac:  // we still need this packet to handle BACK button of craft dialog
             msg = new RequestRecipeBookOpen(data, client);
                break;
            case 0xad:
                msg = new RequestRecipeBookDestroy(data, client);
                break;
			case 0xae:
				msg = new RequestRecipeItemMakeInfo(data, client);
			    break;
			case 0xaf:
				msg = new RequestRecipeItemMakeSelf(data, client);
			    break;
			    
			//case 0xb0:
			//	msg = new RequestRecipeShopManageList(data, client);
			//    break;
            case 0xb1:
            	msg = new RequestRecipeShopMessageSet(data, client);
                break;
			case 0xb2:
				msg = new RequestRecipeShopListSet(data, client);
			    break;
			case 0xb3:
				msg = new RequestRecipeShopManageQuit(data, client);
			    break;
			case 0xb5:
				msg = new RequestRecipeShopMakeInfo(data, client);
			    break;
			case 0xb6:
				msg = new RequestRecipeShopMakeItem(data, client);
			    break;
			case 0xb7:
				msg = new RequestRecipeShopManagePrev(data, client);
			    break;
			case 0xb8:
				msg = new ObserverReturn(data, client);
			    break;
			case 0xb9:
				msg = new RequestEvaluate(data, client);
			    break;
			case 0xba:
			    msg = new RequestHennaList(data, client);
			    break;
			case 0xbb:
				msg = new RequestHennaItemInfo(data, client);
				break;
			case 0xbc:
				msg = new RequestHennaEquip(data, client);
				break;

			case 0xc0:
				// Clan Privileges
				msg = new RequestPledgePower(data, client);  
				break;
            case 0xc1:
                msg = new RequestMakeMacro(data, client);
                break;
            case 0xc2:
				msg = new RequestDeleteMacro(data, client);
				break;
            case 0xc5:
				msg = new DlgAnswer(data, client);
				break;
            case 0xc6:
				msg = new RequestWearItem(data, client);
				break;
            case 0xc7:
            	msg = new RequestSSQStatus(data, client);
            	break;
            case 0xCA:
                msg = new GameGuardReply(data,client);
                break;
            case 0xcc:
            	msg = new RequestSendFriendMsg(data, client);
            	break;
            case 0xcd:
                msg = new RequestShowMiniMap(data, client);
                break;
            case 0xce: // MSN dialogs so that you dont see them in the console.
				break;
            case 0xcf: //record video
            	msg = new RequestRecordInfo(data, client);
                break;
                
            case 0xd0: // Request auto ss and (b)sps.
                int id2 = data.getShort() & 0xffff;
                switch (id2)
                {
                	case 1:
                		msg = new RequestOustFromPartyRoom(data, client);
                		break;
                	case 2:
                		msg = new RequestDismissPartyRoom(data, client);
                		break;
                	case 3:
                		msg = new RequestWithdrawPartyRoom(data, client);
                		break;
                    case 4:
                        msg = new RequestChangePartyLeader(data, client);
                        break;
                    case 5:
                        msg = new RequestAutoSoulShot(data, client);
                        break;
                    case 6:
                        msg = new RequestExEnchantSkillInfo(data, client);
                        break;
                    case 7:
                    	msg = new RequestExEnchantSkill(data, client);
                    	break;
                    case 8:
                    	msg = new RequestManorList(data, client);
                    	break;
                    case 9:
                    	msg = new RequestProcureCropList(data, client);
                    	break;
                    case 0x0a:
                    	msg = new RequestSetSeed(data, client);
                    	break;
                    case 0x0b:
                    	msg = new RequestSetCrop(data, client);
                    	break;
                    case 0x0c:
                    	msg = new RequestWriteHeroWords(data, client);
                    	break;
                    case 0x0d:
                    	msg = new RequestExAskJoinMPCC(data, client);
                    	break;
                    case 0x0e:
                    	msg = new RequestExAcceptJoinMPCC(data, client);
                    	break;
                    case 0x0f:
                    	msg = new RequestExOustFromMPCC(data, client);
                    	break;
                    case 0x10:
                    	msg = new RequestExPledgeCrestLarge(data, client);
                    	break;
                    case 0x11:
                    	msg = new RequestExSetPledgeCrestLarge(data, client);
                    	break;
                    case 0x12:
                    	msg = new RequestOlympiadObserverEnd(data, client);
                    	break;
                    case 0x13:
                    	msg = new RequestOlympiadMatchList(data, client);
                    	break;
                    case 0x14:
                        msg = new RequestAskJoinPartyRoom(data, client);
                        break;
                    case 0x15:
                        msg = new AnswerJoinPartyRoom(data, client);
                        break;
                    case 0x16:
                        msg = new RequestListPartyMatchingWaitingRoom(data, client);
                        break;
                    case 0x17:
                        msg = new RequestExitPartyMatchingWaitingRoom(data, client);
                        break;
                    case 0x18:
                        msg = new RequestGetBossRecord(data, client);
                        break;
                    case 0x19:
                        msg = new RequestPledgeSetAcademyMaster(data, client);
                        break;
                    case 0x1a:
                        msg = new RequestPledgePowerGradeList(data, client);
                        break;
                    case 0x1b:
                        msg = new RequestPledgeMemberPowerInfo(data, client);
                        break;
                    case 0x1c:
                        msg = new RequestPledgeSetMemberPowerGrade(data, client);
                        break;
                    case 0x1d:
                        msg = new RequestPledgeMemberInfo(data, client);
                        break;
                    case 0x1e:
                        msg = new RequestPledgeWarList(data, client);
                        break;
                    case 0x1f:
                        msg = new RequestExFishRanking(data, client);
                        break;
                    case 0x20:
                        msg = new RequestPCCafeCouponUse(data, client);
                        break;
                    case 0x21:
                    	// couldnt find it :S
                    	break;
                    case 0x22:
                    	msg = new RequestCursedWeaponList(data, client);
                    	break;
                    case 0x23:
                    	msg = new RequestCursedWeaponLocation(data, client);
                    	break;
                    case 0x24:
                    	msg = new RequestPledgeReorganizeMember(data, client);
                    	break;
                    default: 
                     	msg = null; 
                     	int size = data.remaining(); 
                     	_log.warning("Unknown Packet: 0xd0:" + Integer.toHexString(id2)); 
                     	byte[] array = new byte[size]; 
                     	data.get(array); 
                     	_log.warning(Util.printData(array, size)); 
                     	break; 
                }
				break;
				
            /*case 0xee:
				msg = new RequestChangePartyLeader(data, _client);     
				break;*/
				
			default:
			{
				msg = null;
				int sz = data.remaining(); 
				_log.warning("Unknown Packet:" + Integer.toHexString(id));
				byte[] arr = new byte[sz];
				data.get(arr);
				_log.warning(Util.printData(arr, sz));
				break;
			}
		}
		if (msg != null) {
			//assert !msg._buf.hasRemaining(); // too many errors :(
			msg._buf = null;
		}
		return msg;
	}
}
