/* This program is free software; you can redistribute it and/or modify
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

import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.ItemContainer;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.util.Rnd;


/**
 *
 * Thorgrim - 2005
 * Add 500k adena to the clan warehouse every 2 hours
 *
 */
public class CastleUpdater implements Runnable
{
		private L2Clan _clan;
        private int _runCount = 0;
		
		public CastleUpdater(L2Clan clan, int runCount)
		{
			_clan = clan;
            _runCount = runCount;
		}
		
		public void run()
		{
		    try {
                // Move current castle treasury to clan warehouse every 2 hour
		    	ItemContainer warehouse = _clan.getWarehouse();
		        if ((warehouse != null)&&(_clan.getHasCastle() > 0))
		        {
                    if (_runCount % 2 == 0)
                    {
                        Castle castle = CastleManager.getInstance().getCastleById(_clan.getHasCastle());

                        int amount = castle.getTreasury();
                        if (amount > 0)
                        {
                            // Move the current treasury amount to clan warehouse
                            warehouse.addItem("Castle", 57, amount, null, null);
                            castle.addToTreasury(amount * -1);
                        }
                    }

                    // Give clan 1 Dualsword Craft Stamp every 3 hour (8 per day)
                    // Give clan ~1 Secret Book of Giants daily (it's been confirmed that castle owners get these, but method is unknown)
                    if (_runCount % 3 == 0)
                    {
                    	warehouse.addItem("Castle", 5126, 1, null, null);
                    	if (Rnd.get(100) < 25) warehouse.addItem("Castle", 6622, 1, null, null);
                    }
                    
                    _runCount++;
                    if (_runCount == 7) _runCount = 1;

                    // re-run again in 1 hours
                    CastleUpdater cu = new CastleUpdater(_clan, _runCount);
                    ThreadPoolManager.getInstance().scheduleGeneral(cu, 3600000);
		        }
		    } catch (Throwable e) {
		        e.printStackTrace();
		    }
		}
}
