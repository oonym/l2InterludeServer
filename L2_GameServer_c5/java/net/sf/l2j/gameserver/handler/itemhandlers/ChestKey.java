/**
 * @author AlterEgo
 */
package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.SkillTable;
import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2ChestInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.PlaySound;
import net.sf.l2j.gameserver.serverpackets.SocialAction;

public class ChestKey implements IItemHandler
{
	public static final int INTERACTION_DISTANCE = 100;

	private static int[] _itemIds = {5197, 5198, 5199, 5200, 5201, 5202, 5203, 5204, //chest key
										6665, 6666, 6667, 6668, 6669, 6670, 6671, 6672 //deluxe key
	};

	public boolean useSkill(L2PcInstance activeChar, int magicId, int level)
	{
		L2Skill skill = SkillTable.getInstance().getInfo(magicId, level);
		if (skill != null)
		{
			activeChar.doCast(skill);
			if (!(activeChar.isSitting())) return true;
		}
		return false;
	}

	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance)) return;

		L2PcInstance activeChar = (L2PcInstance) playable;
		L2Skill skill = SkillTable.getInstance().getInfo(2065, 1);//box key skill
		int itemId = item.getItemId();
		L2Object target = activeChar.getTarget();

		if (!(target instanceof L2ChestInstance) || target == null)
		{
			activeChar.sendMessage("Invalid target.");
			activeChar.sendPacket(new ActionFailed());
		}
		else
		{
			L2ChestInstance chest = (L2ChestInstance) target;
			if (chest.isDead() || chest.isOpen())
			{
				activeChar.sendMessage("The chest Is empty.");
				activeChar.sendPacket(new ActionFailed());
				return;
			}

			if (!(activeChar.isInsideRadius(chest, INTERACTION_DISTANCE, false, false)))
			{
				activeChar.sendMessage("Too far.");
				activeChar.sendPacket(new ActionFailed());
				return;
			}

			if (!chest.isBox()) {
				activeChar.sendMessage("Use " + item.getItem().getName() + ".");
				playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
				activeChar.sendMessage("Failed to open chest");
				activeChar.sendPacket(new ActionFailed());
				chest.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				chest.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar);
				return;
			}
			if (activeChar.getAbnormalEffect() > 0 || activeChar.isInCombat())
			{
				activeChar.sendMessage("You cannot use the key,now.");
				activeChar.sendPacket(new ActionFailed());
				return;
			}


			// Everything is OK
			activeChar.sendMessage("Use " + item.getItem().getName() + ".");
			activeChar.useMagic(skill, false, false);

			if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, false)) return;
			chest.setOpen();
			int openChance = 0;
			int chestGroup = 0;

			if (chest.getLevel() >= 80) chestGroup = 8;
			else if (chest.getLevel() >= 70) chestGroup = 7;
			else if (chest.getLevel() >= 60) chestGroup = 6;
			else if (chest.getLevel() >= 50) chestGroup = 5;
			else if (chest.getLevel() >= 40) chestGroup = 4;
			else if (chest.getLevel() >= 30) chestGroup = 3;
			else if (chest.getLevel() >= 20) chestGroup = 2;
			else chestGroup = 1;

			//Chest Key
			switch (itemId)
			{
				case 5197://grade 8 - 60% for open ONLY chest lv1,decrese % on high chest lvl
				{
					if (chest.getLevel() == 1)
					{
						openChance = 60;
					}
					else
					{
						openChance = 10;
					}
				}
					break;
				case 5198://grade 7 - 60% for open ONLY chest lv 10,decrese % on high chest lvl
				{
					if (chest.getLevel() == 10)
					{
						openChance = 60;
					}
					else if (chest.getLevel() < 10)
					{
						sendKeyNotAdpated(activeChar,chest);
						return;
					}
					else if (chest.getLevel() > 10)
					{
						openChance = 10;
					}
				}
					break;
				case 5199://grade 6 - 60% for open ONLY chest lv 20,decrese % on high chest lvl
				{
					if (chest.getLevel() == 20)
					{
						openChance = 60;
					}
					else if (chest.getLevel() < 20)
					{
						sendKeyNotAdpated(activeChar,chest);
						return;
					}
					else if (chest.getLevel() > 20)
					{
						openChance = 10;
					}
				}
					break;
				case 5200://grade 5 - 60% for open ONLY chest lv 30,decrese % on high chest lvl
				{
					if (chest.getLevel() == 30)
					{
						openChance = 60;
					}
					else if (chest.getLevel() < 30)
					{
						sendKeyNotAdpated(activeChar,chest);
						return;
					}
					else if (chest.getLevel() > 30)
					{
						openChance = 10;
					}
				}
					break;
				case 5201://grade 4 - 60% for open ONLY chest lv 40,decrese % on high chest lvl
				{
					if (chest.getLevel() == 40)
					{
						openChance = 60;
					}
					else if (chest.getLevel() < 40)
					{
						sendKeyNotAdpated(activeChar,chest);
						return;
					}
					else if (chest.getLevel() > 40)
					{
						openChance = 10;
					}
				}
					break;
				case 5202://grade 3 - 60% for open ONLY chest lv 50,decrese % on high chest lvl
				{
					if (chest.getLevel() == 50)
					{
						openChance = 60;
					}
					else if (chest.getLevel() < 50)
					{
						sendKeyNotAdpated(activeChar,chest);
						return;
					}
					else if (chest.getLevel() > 50)
					{
						openChance = 10;
					}
				}
					break;
				case 5203://grade 2 - 60% for open ONLY chest lv 60,decrese % on high chest lvl
				{
					if (chest.getLevel() == 60)
					{
						openChance = 60;
					}
					else if (chest.getLevel() < 60)
					{
						sendKeyNotAdpated(activeChar,chest);
						return;
					}
					else if (chest.getLevel() > 60)
					{
						openChance = 10;
					}
				}
					break;
				case 5204://grade 1 - 60% for open ONLY chest lv 70,decrese % on high chest lvl
				{
					if (chest.getLevel() == 70)
					{
						openChance = 60;
					}
					else if (chest.getLevel() < 70)
					{
						sendKeyNotAdpated(activeChar,chest);
						return;
					}
					else if (chest.getLevel() > 70)
					{
						openChance = 10;
					}
				}
					break;
				//Deuxe Chest Key
				case 6665://grade 1 - 100% for open chest lv 1-19,decrese % on high chest lvl
				{
					if (chestGroup == 1)
					{
						openChance = 100;
					}
					else if (chestGroup == 2)
					{
						openChance = 60;
					}
					else if (chestGroup == 3)
					{
						openChance = 20;
					}
					else
					{
						openChance = 0;
					}
				}
					break;
				case 6666://grade 2 - 100% for open chest lv 20-29,decrese % on high chest lvl
				{
					if (chestGroup < 2)
					{
						sendKeyNotAdpated(activeChar,chest);
						return;
					}
					else if (chestGroup == 2)
					{
						openChance = 100;
					}
					else if (chestGroup == 3)
					{
						openChance = 60;
					}
					else if (chestGroup == 4)
					{
						openChance = 20;
					}
					else
					{
						openChance = 0;
					}
				}
					break;
				case 6667://grade 3 - 100% for open chest lv 30-39,decrese % on high chest lvl
				{
					if (chestGroup < 3)
					{
						sendKeyNotAdpated(activeChar,chest);
						return;
					}
					else if (chestGroup == 3)
					{
						openChance = 100;
					}
					else if (chestGroup == 4)
					{
						openChance = 60;
					}
					else if (chestGroup == 5)
					{
						openChance = 20;
					}
					else
					{
						openChance = 0;
					}
				}
					break;
				case 6668://grade 4 - 100% for open chest lv 40-49,decrese % on high chest lvl
				{
					if (chestGroup < 4)
					{
						sendKeyNotAdpated(activeChar,chest);
						return;
					}
					else if (chestGroup == 4)
					{
						openChance = 100;
					}
					else if (chestGroup == 5)
					{
						openChance = 60;
					}
					else if (chestGroup == 6)
					{
						openChance = 20;
					}
					else
					{
						openChance = 0;
					}
				}
					break;
				case 6669://grade 5 - 100% for open chest lv 50-59,decrese % on high chest lvl
				{
					if (chestGroup < 5)
					{
						sendKeyNotAdpated(activeChar,chest);
						return;
					}
					else if (chestGroup == 5)
					{
						openChance = 100;
					}
					else if (chestGroup == 6)
					{
						openChance = 60;
					}
					else if (chestGroup == 7)
					{
						openChance = 20;
					}
					else
					{
						openChance = 0;
					}
				}
					break;
				case 6670://grade 6 - 100% for open chest lv 60-69,decrese % on high chest lvl
				{
					if (chestGroup < 6)
					{
						sendKeyNotAdpated(activeChar,chest);
						return;
					}
					else if (chestGroup == 6)
					{
						openChance = 100;
					}
					else if (chestGroup == 7)
					{
						openChance = 60;
					}
					else if (chestGroup == 8)
					{
						openChance = 20;
					}
					else
					{
						openChance = 0;
					}
				}
					break;
				case 6671://grade 7 - 100% for open chest lv 70-79,decrese % on high chest lvl
				{
					if (chestGroup < 7)
					{
						sendKeyNotAdpated(activeChar,chest);
						return;
					}
					else if (chestGroup == 7)
					{
						openChance = 100;
					}
					else if (chestGroup == 8)
					{
						openChance = 60;
					}
				}
					break;
				case 6672://grade 8 - 100% for open chest lv 80-89,decrese % on high chest lvl
				{
					if (chestGroup < 8)
					{
						sendKeyNotAdpated(activeChar,chest);
						return;
					}
					else if (chestGroup == 8)
					{
						openChance = 100;
					}
				}
					break;
				default:
				{
					sendKeyNotAdpated(activeChar,chest);
					return;
				}
			}

			// Remove the required item

			if (openChance > 0 && Rnd.get(100) < openChance)
			{
				activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 3));
				PlaySound playSound = new PlaySound("interfacesound.inventory_open_01");
				activeChar.sendPacket(playSound);
				activeChar.sendMessage("You open the chest!");
				
				chest.setHaveToDrop(true);
				chest.setMustRewardExpSp(false);
				chest.setSpecialDrop();
				chest.doItemDrop(activeChar);
				chest.doDie(activeChar);
			}
			else
			{
				activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 13));
				PlaySound playSound = new PlaySound("interfacesound.system_close_01");
				activeChar.sendPacket(playSound);
				activeChar.sendMessage("The key has been broken off!");

				// 50% chance of getting a trap
				if (Rnd.get(10) < 5) chest.chestTrap(activeChar);
				chest.setHaveToDrop(false);
				chest.setMustRewardExpSp(false);
				chest.doDie(activeChar);
			}
		}
	}

	private void sendKeyNotAdpated(L2PcInstance player, L2ChestInstance chest)
	{
		player.sendMessage("The key seems not to be adapted.");
		PlaySound playSound = new PlaySound("interfacesound.system_close_01");
		player.sendPacket(playSound);
		player.sendPacket(new ActionFailed());
		chest.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		chest.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, player);
	}

	public int[] getItemIds()
	{
		return _itemIds;
	}
}
