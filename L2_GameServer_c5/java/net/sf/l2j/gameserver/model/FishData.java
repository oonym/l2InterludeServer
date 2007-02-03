package net.sf.l2j.gameserver.model;

public class FishData
{
		private final int _id;
		private final int _level;
		private final String _name;
		private final int _HP;
		private final int _HpRegen;
		private final int _type;
		private final int _group;
		private final int _fish_guts;
		private final int _guts_check_time;
		private final int _wait_time;
		private final int _combat_time;

		public FishData(int id, int lvl, String name, int HP, int HpRegen, int type, int group, int fish_guts, int guts_check_time, int wait_time, int combat_time)
		{
			_id = id;
			_level = lvl;
			_name = name.intern();
			_HP = HP;
			_HpRegen = HpRegen;
			_type = type;
			_group = group;
			_fish_guts = fish_guts;
			_guts_check_time = guts_check_time;
			_wait_time = wait_time;
			_combat_time = combat_time;
		}

		/**
		 * @return Returns the id.
		 */
		public int getId()
		{
			return _id;
		}

		/**
		 * @return Returns the level.
		 */
		public int getLevel()
		{
			return _level;
		}

		/**
		 * @return Returns the name.
		 */
		public String getName()
		{
			return _name;
		}

		public int getHP()
		{
			return _HP;
		}
		public int getHpRegen()
		{
			return _HpRegen;
		}
		public int getType()
		{
			return _type;
		}
		public int getGroup()
		{
			return _group;
		}
		public int getFishGuts()
		{
			return _fish_guts;
		}
		public int getGutsCheckTime()
		{
			return _guts_check_time;
		}
		public int getWaitTime()
		{
			return _wait_time;
		}
		public int getCombatTime()
		{
			return _combat_time;
		}
	}

