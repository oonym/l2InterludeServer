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
package net.sf.l2j.gameserver.datatables;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.FishData;

/**
 * @author -Nemesiss-
 *
 */
public class FishTable
{
	private static Logger _log = Logger.getLogger(SkillTreeTable.class.getName());
	private static final FishTable _instance = new FishTable();

	private static List<FishData> _fishsNormal;
	private static List<FishData> _fishsEasy;
	private static List<FishData> _fishsHard;

	public static FishTable getInstance()
	{
		return _instance;
	}
	private FishTable()
	{
		//Create table that contains all fish datas
		int count   = 0;
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			_fishsEasy = new FastList<FishData>();
			_fishsNormal = new FastList<FishData>();
			_fishsHard = new FastList<FishData>();
			FishData fish;
			PreparedStatement statement = con.prepareStatement("SELECT id, level, name, hp, hpregen, fish_type, fish_group, fish_guts, guts_check_time, wait_time, combat_time FROM fish ORDER BY id");
			ResultSet Fishes = statement.executeQuery();

				while (Fishes.next())
				{
					int id = Fishes.getInt("id");
					int lvl = Fishes.getInt("level");
					String name = Fishes.getString("name");
					int hp = Fishes.getInt("hp");
					int hpreg = Fishes.getInt("hpregen");
					int type = Fishes.getInt("fish_type");
					int group = Fishes.getInt("fish_group");
					int fish_guts = Fishes.getInt("fish_guts");
					int guts_check_time = Fishes.getInt("guts_check_time");
					int wait_time = Fishes.getInt("wait_time");
					int combat_time = Fishes.getInt("combat_time");
					fish = new FishData(id, lvl, name, hp, hpreg, type, group, fish_guts, guts_check_time, wait_time, combat_time);
					switch (fish.getGroup()) {
						case 0:
							_fishsEasy.add(fish);
							break;
						case 1:
							_fishsNormal.add(fish);
							break;
						case 2:
							_fishsHard.add(fish);
					}
				}
				Fishes.close();
				statement.close();
                count = _fishsEasy.size() + _fishsNormal.size() + _fishsHard.size();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "error while creating fishes table"+ e);
		}
		finally
		{
			try { con.close(); } catch (Exception e) {}
		}
        _log.config("FishTable: Loaded " + count + " Fishes.");
	}
	/**
	 * @param Fish - lvl
	 * @param Fish - type
	 * @param Fish - group
	 * @return List of Fish that can be fished
	 */
	public List<FishData> getfish(int lvl, int type, int group)
	{
		List<FishData> result = new FastList<FishData>();
		List<FishData> _Fishs = null;
		switch (group) {
			case 0:
				_Fishs = _fishsEasy;
				break;
			case 1:
				_Fishs = _fishsNormal;
				break;
			case 2:
				_Fishs = _fishsHard;
		}
		if (_Fishs == null)
		{
			// the fish list is empty
			_log.warning("Fish are not defined !");
			return null;
		}
		for (FishData f : _Fishs)
		{
			if (f.getLevel()!= lvl) continue;
			if (f.getType() != type) continue;

			result.add(f);
		}
		if (result.size() == 0)	_log.warning("Cant Find Any Fish!? - Lvl: "+lvl+" Type: " +type);
		return result;
	}

}
