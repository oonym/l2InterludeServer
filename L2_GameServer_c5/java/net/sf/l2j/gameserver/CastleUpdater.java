/*
 * CastleUpdater.java
 *
 *
 */

package net.sf.l2j.gameserver;

import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.ItemContainer;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.entity.Castle;


/**
 *
 * Thorgrim - 2005
 * Add 500k adena to the clan warehouse every 2 hours
 *
 */
public class CastleUpdater implements Runnable {


		private L2Clan _clan;
        private int _RunCount = 0;
		
		public CastleUpdater(L2Clan clan, int runCount)
		{
			_clan = clan;
            _RunCount = runCount;
		}
		
		public void run()
		{
		    try {
                // Move current castle treasury to clan warehouse every 2 hour
		    	ItemContainer warehouse = _clan.getWarehouse();
		        if ((warehouse != null)&&(_clan.getHasCastle() > 0))
		        {
                    if (_RunCount % 2 == 0)
                    {
                        Castle castle = CastleManager.getInstance().getCastle(_clan.getHasCastle());

                        int amount = castle.getTreasury();
                        if (amount > 0)
                        {
                            // Move the current treasury amount to clan warehouse
                            warehouse.addItem("Castle", 57, amount, null, null);
                            castle.addToTreasury(amount * -1);
                        }
                    }

                    // Give clan 1 Dual Craft Stamp every 3 hour (8 per day)
                    if (_RunCount % 3 == 0)
                    {
                    	warehouse.addItem("Castle", 5126, 1, null, null);
                    }
                    
                    _RunCount++;
                    if (_RunCount == 7) _RunCount = 1;

                    // re-run again in 1 hours
                    CastleUpdater cu = new CastleUpdater(_clan, _RunCount);
                    ThreadPoolManager.getInstance().scheduleGeneral(cu, 3600000);
		        }
		    } catch (Throwable e) {
		        e.printStackTrace();
		    }
		}
}
