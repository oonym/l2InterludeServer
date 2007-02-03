package net.sf.l2j.gameserver.model;

import java.util.concurrent.Future;

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
	private int _time;
	private int _stop = 0;
	private int _gooduse = 0;
	private int _anim = 0;
	private int _mode = 0;
	private int _deceptiveMode = 0;
	private Future _fishAItask;
	private boolean thinking;
	// Fish datas
	private int _FishID;
	private int _FishMaxHP;
	private int _FishCurHP;
	private double _regenHP;
	private boolean _isUpperGrade;
	private int _lureType;

	public void run()
	{
		_anim = 0;
		if (_FishCurHP >= _FishMaxHP * 2)
		{
			// The fish got away
			_Fisher.sendPacket(new SystemMessage(SystemMessage.BAIT_STOLEN_BY_FISH));
			DoDie(false);
		}
		else if (_time <= 0)
		{
			// Time is up, so that fish got away
			_Fisher.sendPacket(new SystemMessage(SystemMessage.FISH_SPIT_THE_HOOK));
			DoDie(false);
		}
		else AiTask();
	}

	// =========================================================
	public L2Fishing(L2PcInstance Fisher, FishData fish, boolean isNoob, boolean isUpperGrade)
	{
		_Fisher = Fisher;
		_FishMaxHP = fish.getHP();
		_FishCurHP = _FishMaxHP;
		_regenHP = fish.getHpRegen();
		_FishID = fish.getId();
		_time = fish.getCombatTime() / 1000;
		_isUpperGrade = isUpperGrade;
		if (isUpperGrade) {
			_deceptiveMode = Rnd.get(100) >= 90 ? 1 : 0;
			_lureType = 2;
		}
		else {
			_deceptiveMode = 0;
			_lureType = isNoob ? 0 : 1;
		}
		_mode = Rnd.get(100) >= 80 ? 1 : 0;

		ExFishingStartCombat efsc = new ExFishingStartCombat(_Fisher, _time, _FishMaxHP, _mode, _lureType, _deceptiveMode);
		_Fisher.broadcastPacket(efsc);

		// Succeeded in getting a bite
		_Fisher.sendPacket(new SystemMessage(SystemMessage.GOT_A_BITE));

		if (_fishAItask == null)
		{
			_fishAItask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(this, 1000, 1000);
		}

	}

	public void ChangeHp(int hp, int pen)
	{
		_FishCurHP -= hp;
		if (_FishCurHP < 0) _FishCurHP = 0;
		
		ExFishingHpRegen efhr = new ExFishingHpRegen(_Fisher, _time, _FishCurHP, _mode, _gooduse, _anim, pen, _deceptiveMode);
		_Fisher.broadcastPacket(efhr);
		_anim = 0;
		if (_FishCurHP > _FishMaxHP * 2)
		{
			_FishCurHP = _FishMaxHP * 2;
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
        
		if (win)
		{
			int check = Rnd.get(100);
			if (check <= 5) {
				PenaltyMonster();
			}
			else {
				_Fisher.sendPacket(new SystemMessage(SystemMessage.YOU_CAUGHT_SOMETHING));
				_Fisher.addItem("Fishing", _FishID, 1, null, true);
			}
		}
		_Fisher.EndFishing(win);
		_Fisher = null;
	}

	protected void AiTask()
	{
		if (thinking) return;
		thinking = true;
		_time--;

		try
		{
			if (_mode == 1) {
				if (_deceptiveMode == 0)
					_FishCurHP += (int) _regenHP;
			}
			else {
				if (_deceptiveMode == 1)
					_FishCurHP += (int) _regenHP;
			}
			if (_stop == 0) {
				_stop = 1;
				int check = Rnd.get(100);
				if (check >= 80) {
					_mode = _mode == 0 ? 1 : 0;
					_anim = _mode == 0 ? 1 : 2;
				}
				if (_isUpperGrade) {
					check = Rnd.get(100);
					if (check >= 90)
						_deceptiveMode = _deceptiveMode == 0 ? 1 : 0;
				}
			}
			else {
				_stop--;
			}
		}
		finally
		{
			thinking = false;
			ExFishingHpRegen efhr = new ExFishingHpRegen(_Fisher, _time, _FishCurHP, _mode, 0, _anim, 0, _deceptiveMode);
			if (_anim != 0)
				_Fisher.broadcastPacket(efhr);
			else
				_Fisher.sendPacket(efhr);
		}
	}

	public void UseRealing(int dmg, int pen)
	{
		if (Rnd.get(100) > 90) {
			_Fisher.sendPacket(new SystemMessage(SystemMessage.FISH_RESISTED_ATTEMPT_TO_BRING_IT_IN));
			return;
		}
		if (_Fisher == null) return;
		if (_mode == 1)
		{
			if (_deceptiveMode == 0) {
	            // Reeling is successful, Damage: $s1
				SystemMessage sm = new SystemMessage(SystemMessage.REELING_SUCCESFUL_S1_DAMAGE);
				sm.addNumber(dmg);
				_Fisher.sendPacket(sm);
				if (pen == 50) {
					sm = new SystemMessage(SystemMessage.REELING_SUCCESSFUL_PENALTY_S1);
					sm.addNumber(pen);
					_Fisher.sendPacket(sm);
				}
				_gooduse = 1;
				ChangeHp(dmg , pen);
			}
			else {
	            // Reeling failed, Damage: $s1
				SystemMessage sm = new SystemMessage(SystemMessage.FISH_RESISTED_REELING_S1_HP_REGAINED);
				sm.addNumber(dmg);
				_Fisher.sendPacket(sm);
				_gooduse = 2;
				ChangeHp(-dmg, pen);
			}
		}
		else
		{
			if (_deceptiveMode == 0) {
	            // Reeling failed, Damage: $s1
				SystemMessage sm = new SystemMessage(SystemMessage.FISH_RESISTED_REELING_S1_HP_REGAINED);
				sm.addNumber(dmg);
				_Fisher.sendPacket(sm);
				_gooduse = 2;
				ChangeHp(-dmg, pen);
			}
			else {
	            // Reeling is successful, Damage: $s1
				SystemMessage sm = new SystemMessage(SystemMessage.REELING_SUCCESFUL_S1_DAMAGE);
				sm.addNumber(dmg);
				_Fisher.sendPacket(sm);
				if (pen == 50) {
					sm = new SystemMessage(SystemMessage.REELING_SUCCESSFUL_PENALTY_S1);
					sm.addNumber(pen);
					_Fisher.sendPacket(sm);
				}
				_gooduse = 1;
				ChangeHp(dmg , pen);
			}
		}
	}

	public void UsePomping(int dmg, int pen)
	{
		if (Rnd.get(100) > 90) {
			_Fisher.sendPacket(new SystemMessage(SystemMessage.FISH_RESISTED_ATTEMPT_TO_BRING_IT_IN));
			return;
		}
		if (_Fisher == null) return;
		if (_mode == 0)
		{
			if (_deceptiveMode == 0) {
	            // Pumping is successful. Damage: $s1
				SystemMessage sm = new SystemMessage(SystemMessage.PUMPING_SUCCESFUL_S1_DAMAGE);
				sm.addNumber(dmg);
				_Fisher.sendPacket(sm);
				if (pen == 50) {
					sm = new SystemMessage(SystemMessage.PUMPING_SUCCESSFUL_PENALTY_S1);
					sm.addNumber(pen);
					_Fisher.sendPacket(sm);
				}
				_gooduse = 1;
				ChangeHp(dmg, pen);
			}
			else {
	            // Pumping failed, Regained: $s1
				SystemMessage sm = new SystemMessage(SystemMessage.FISH_RESISTED_PUMPING_S1_HP_REGAINED);
				sm.addNumber(dmg);
				_Fisher.sendPacket(sm);
				_gooduse = 2;
				ChangeHp(-dmg, pen);
			}
		}
		else
		{
			if (_deceptiveMode == 0) {
	            // Pumping failed, Regained: $s1
				SystemMessage sm = new SystemMessage(SystemMessage.FISH_RESISTED_PUMPING_S1_HP_REGAINED);
				sm.addNumber(dmg);
				_Fisher.sendPacket(sm);
				_gooduse = 2;
				ChangeHp(-dmg, pen);
			}
			else {
	            // Pumping is successful. Damage: $s1
				SystemMessage sm = new SystemMessage(SystemMessage.PUMPING_SUCCESFUL_S1_DAMAGE);
				sm.addNumber(dmg);
				_Fisher.sendPacket(sm);
				if (pen == 50) {
					sm = new SystemMessage(SystemMessage.PUMPING_SUCCESSFUL_PENALTY_S1);
					sm.addNumber(pen);
					_Fisher.sendPacket(sm);
				}
				_gooduse = 1;
				ChangeHp(dmg, pen);
			}
		}
	}

	private void PenaltyMonster()
	{
		int lvl = (int)Math.round(_Fisher.getLevel()*0.1);
		int npcid;
		
		_Fisher.sendPacket(new SystemMessage(SystemMessage.YOU_CAUGHT_SOMETHING_SMELLY_THROW_IT_BACK));
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
