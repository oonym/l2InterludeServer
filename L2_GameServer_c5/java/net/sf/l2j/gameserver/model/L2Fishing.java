package net.sf.l2j.gameserver.model;

import java.util.List;
import java.util.concurrent.Future;

import net.sf.l2j.gameserver.FishTable;
import net.sf.l2j.gameserver.NpcTable;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PenaltyMonsterInstance;
import net.sf.l2j.gameserver.serverpackets.ExFishingHpRegen;
import net.sf.l2j.gameserver.serverpackets.ExFishingStartCombat;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2Fishing implements Runnable
{
	// =========================================================
	// Data Field
	private L2PcInstance _Fisher;
	private boolean _IsN00b;
	private int _time;
	private int _gooduse = 0;
	private int _anim = 0;
	private int _mode = 0;
	private Future _fishAItask;
	private boolean thinking;
	private int stop = 0;
	// Fish datas
	private int _FishID;
	private int _FishMaxHP;
	private int _FishCurHP;
	private double _regenHP;

	public void run()
	{
		_anim = 0;
		if (_FishCurHP >= _FishMaxHP * 2)
		{
			// The fish got away
			_Fisher.sendPacket(new SystemMessage(1451));
			PenaltyMonster(); //Random chance to spawn monster
			DoDie(false);
		}
		else if (_time <= 0)
		{
			// Time is up, so that fish got away
			_Fisher.sendPacket(new SystemMessage(1450));
			PenaltyMonster(); //Random chance to spawn monster
			DoDie(false);
		}
		else AiTask();
	}

	// =========================================================
	public L2Fishing(L2PcInstance Fisher)
	{
		_Fisher = Fisher;
		int lureid = _Fisher.GetLure().getItemId();
		_IsN00b = IsN00b(lureid);
		int luretype = GetLurePreferedType(lureid);
		int type = GetRandomFishType(luretype);
		int randomlvl = GetRandomFishLvl();
		List<FishData> fishs = FishTable.getInstance().getfish(randomlvl, type);
		if (fishs == null || fishs.size() == 0)
		{
			_Fisher.sendMessage("Error - Fishes are not definied");
			DoDie(false);
			return;
		}
		int check = Rnd.get(fishs.size());
		FishData fish = fishs.get(check);
		fishs.clear();
		fishs = null;
		_FishMaxHP = fish.getHP();
		_FishCurHP = _FishMaxHP;
		_regenHP = _FishMaxHP / fish.getHpRegen();
		_FishID = fish.getId();
		if (_IsN00b) _time = 35;
		else _time = 30;

		ExFishingStartCombat efsc = new ExFishingStartCombat(_Fisher, _time, _FishMaxHP, _IsN00b);
		_Fisher.broadcastPacket(efsc);

		// Succeeded in getting a bite
		_Fisher.sendPacket(new SystemMessage(1449));

		if (_fishAItask == null)
		{
			_fishAItask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(this, 1000, 1000);
		}

	}

	public void ChangeHp(int hp, int pen)
	{
		_FishCurHP -= hp;
		if (_FishCurHP < 0) _FishCurHP = 0;
		
		ExFishingHpRegen efhr = new ExFishingHpRegen(_Fisher, _time, _FishCurHP, _mode, _anim, _gooduse, pen);
		_Fisher.broadcastPacket(efhr);
		_gooduse = 0;
		_anim = 0;
		if (_FishCurHP > _FishMaxHP * 2)
		{
			_FishCurHP = _FishMaxHP * 2;
			PenaltyMonster();
			DoDie(false);
			return;
		}
		else if (_FishCurHP == 0)
		{
			DoDie(true);
			return;
		}
	}

	public void DoDie(boolean win)
	{
		_fishAItask = null;
        
        if (_Fisher == null) return;
        
		_Fisher.EndFishing(win);
		if (win)
		{
			_Fisher.addItem("Fishing", _FishID, 1, null, true);
		}
		_Fisher = null;
	}

	protected void AiTask()
	{
		if (thinking) return;
		thinking = true;
		_time--;

		try
		{
			if (stop == 0)
			{
				stop = 1;
				int check = Rnd.get(100);
				if (check > 50)// fighting
				{
					_mode = 1;
					_FishCurHP += (int) _regenHP;
				}
				else // Resting
				{
					_mode = 0;
				}
			}
			else
			{
				stop--;
			}
		}
		finally
		{
			thinking = false;
			ExFishingHpRegen efhr = new ExFishingHpRegen(_Fisher, _time, _FishCurHP, _mode, _anim,
															_gooduse, 0);
			_Fisher.broadcastPacket(efhr);
		}
	}

	public void UseRealing(int dmg, int pen)
	{
		// TODO@ There are some % to fish resisted
		if (_Fisher == null) return;
		_anim = 2;
		if (_mode == 1)
		{
            // Reeling is successful, Damage: $s1
			SystemMessage sm = new SystemMessage(1467);
			sm.addNumber(dmg);
			_Fisher.sendPacket(sm);
			_gooduse = 1;
			ChangeHp(dmg , pen);
		}
		else
		{
            // Reeling failed, Damage: $s1
			SystemMessage sm = new SystemMessage(1468);
			sm.addNumber(dmg);
			_Fisher.sendPacket(sm);
			ChangeHp(-dmg, pen);
		}
	}

	public void UsePomping(int dmg, int pen)
	{
		// TODO@ There are some % to fish resisted
		if (_Fisher == null) return;
		_anim = 1;
		if (_mode == 0)
		{
            // Pumping is successful. Damage: $s1
			SystemMessage sm = new SystemMessage(1465);
			sm.addNumber(dmg);
			_Fisher.sendPacket(sm);
			_gooduse = 1;
			ChangeHp(dmg, pen);
		}
		else
		{
            // Pumping failed, Damage: $s1
			SystemMessage sm = new SystemMessage(1466);
			sm.addNumber(dmg);
			_Fisher.sendPacket(sm);
			ChangeHp(-dmg, pen);
		}
	}

	private int GetLurePreferedType(int lureid)
	{
		int luretype;
		switch (lureid)
		{
			case 7807:
			case 6519:
			case 6520:
			case 6521:
				luretype = 1;// This bait is preferred by fast moving fish.
				break;
			case 7808:
			case 6522:
			case 6523:
			case 6524:
				luretype = 2;// This bait is preferred by fat fish.
				break;
			case 7809:
			case 6525:
			case 6526:
			case 6527:
				luretype = 3;// This bait is preferred by ugly fish.
				break;
			default:
				luretype = 0;
				break;
		}
		return luretype;
	}

	private boolean IsN00b(int lureid)
	{
		if (lureid >= 7807 && lureid <= 7809) return true;
		else return false;
	}

	private int GetRandomFishType(int luretype)
	{
		int check = Rnd.get(100);
		int type;
		if (check > 50)
		{
			type = luretype;
		}
		else
		{
			int check2 = Rnd.get(3);
			type = check2;
		}
		return type;
	}

	private int GetRandomFishLvl()
	{
		int skilllvl = _Fisher.getSkillLevel(1315);
		if (skilllvl <= 0) return 1;
		int randomlvl;
		int check = Rnd.get(100);
		if (check <= 50)
		{
			randomlvl = skilllvl;
		}
		else if (check >= 95)
		{
			randomlvl = skilllvl + 1;
		}
		else
		{
			randomlvl = skilllvl - 1;
			if (randomlvl <= 0)
			{
				randomlvl = 1;
			}
		}
		return randomlvl;
	}

	private void PenaltyMonster()
	{
		int check = Rnd.get(100);
		if (check > 5) return;
		int lvl = (int)Math.round(_Fisher.getLevel()*0.1);
		int npcid;
		switch (lvl)
		{
		case 0:
		case 1:
			npcid = 18319;
			break;
		case 2:
			npcid = 18320;
			break;
		case 3:
			npcid = 18321;
			break;
		case 4:
			npcid = 18322;
			break;
		case 5:
			npcid = 18323;
			break;
		case 6:
			npcid = 18324;
			break;
		case 7:
			npcid = 18325;
			break;
		case 8:
			npcid = 18326;
			break;
		default:
			npcid = 18319;
		    break;
		}
		L2NpcTemplate temp;
		temp = NpcTable.getInstance().getTemplate(npcid);
		if (temp != null)
		{
			try
			{
				L2Spawn spawn = new L2Spawn(temp);
				spawn.setLocx(_Fisher.GetFishx());
				spawn.setLocy(_Fisher.GetFishy());
				spawn.setLocz(_Fisher.GetFishz());
				spawn.setAmount(1);
				spawn.setHeading(_Fisher.getHeading());
				spawn.stopRespawn();
				((L2PenaltyMonsterInstance)spawn.doSpawn()).SetPlayerToKill(_Fisher);
			}
			catch (Exception e)
			{
				// Nothing
			}
		}

	}
}
