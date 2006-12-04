package net.sf.l2j.gameserver.model;

public class FishData
{
		private final int _id;
		private final int _level;
		private final String _name;
		private final int _HP;
		private final int _HpRegen;
		private final int _type;

		public FishData(int id, int lvl, String name, int HP, int HpRegen, int type)
		{
			_id = id;
			_level = lvl;
			_name = name.intern();
			_HP = HP;
			_HpRegen = HpRegen;
			_type = type;
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
	}

